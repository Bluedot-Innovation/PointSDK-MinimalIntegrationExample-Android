package au.com.bluedot.minimalintegration

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import au.com.bluedot.point.net.engine.Chat
import com.bumptech.glide.Glide
import java.util.UUID

class ChatAdapter(private val messages: MutableList<Message>, private val listener: ChatAdapterListener) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private val messagesList = messages

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = when (viewType) {
            VIEW_TYPE_SENT -> layoutInflater.inflate(R.layout.item_message_sent, parent, false)
            VIEW_TYPE_RECEIVED -> layoutInflater.inflate(R.layout.item_message_received, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return ChatViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.isSentByUser) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    fun addMessage(message: Message) {
        messagesList.add(message)
    }

    fun updateMessage(messageTest: String, id: UUID, link: String? = null) {
        val msg = Message(messageTest, false, id, link)
        if (messagesList.find { it.id == id } == null) {
            messagesList.add(msg)
            notifyItemChanged(messagesList.indexOf(msg) - 1)
        } else {
            val msgItem = messagesList.find { it.id == id }
            msgItem?.text = msg.text
            msgItem?.link = link
            notifyItemChanged(messagesList.indexOf(messagesList.find { it.id == id }) )
        }
    }

    fun removeMessage(id: UUID) {
        messagesList.remove(messagesList.find { it.id == id })
        notifyItemRemoved(messagesList.indexOf(messagesList.find { it.id == id }))
    }

    fun updateMessageResponseId(responseId: String, id: UUID) {
        Log.i("ChatAdapter", "updateMessageResponseId: $responseId index is: ${messagesList.indexOf(messagesList.find { it.id == id })}")
        messagesList.find { it.id == id }.also { it?.responseId = responseId }
        notifyItemChanged(messagesList.indexOf(messagesList.find { it.id == id }) )
    }

    fun updateMessageEnd(id: UUID) {
        val msg = messagesList.find { it.id == id }.also { it?.isLoading = false }
        notifyItemChanged(messagesList.indexOf(msg))
    }

    class ChatViewHolder(itemView: View, listener: ChatAdapterListener) : RecyclerView.ViewHolder(itemView) {
        private val buttonListener = listener
        fun bind(message: Message) {
            val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            val textView = itemView.findViewById<TextView>(R.id.messageTextView)
            if (message.isSentByUser)
                textView.text = message.text
            else {
                textView.text= parseHtmlStringToMarkdown(message.text)
                val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
                val dislikeButton: ImageButton = itemView.findViewById(R.id.dislikeButton)

                if(message.link != null) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.link)
                        .error(ContextCompat.getDrawable(itemView.context, R.drawable.download_error))
                        .into(imageView)
                        .onLoadFailed(ContextCompat.getDrawable(itemView.context, R.drawable.download_error))

                    likeButton.visibility = View.VISIBLE
                    dislikeButton.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }

                if (message.isLoading) {
                    likeButton.visibility = View.GONE
                    dislikeButton.visibility = View.GONE
                } else {
                    likeButton.visibility = View.VISIBLE
                    dislikeButton.visibility = View.VISIBLE
                }

                likeButton.isSelected = message.like
                dislikeButton.isSelected = message.dislike

                likeButton.setOnClickListener {
                    buttonListener.processFeedback(message.id, Chat.ChatFeedback.LIKED)
                    message.like = !message.like
                }
                dislikeButton.setOnClickListener {
                    buttonListener.processFeedback(message.id, Chat.ChatFeedback.DISLIKED)
                    message.dislike = !message.dislike
                }
            }
        }
    }

    interface ChatAdapterListener {
        fun processFeedback(id: UUID, feedback: Chat.ChatFeedback)
    }
}