package au.com.bluedot.minimalintegration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.UUID

class ChatAdapter(private val messages: MutableList<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
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
        return ChatViewHolder(itemView)
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
        } else {
            val msgItem = messagesList.find { it.id == id }
            msgItem?.text = msg.text
            msgItem?.link = link
            notifyDataSetChanged()
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            val textView = itemView.findViewById<TextView>(R.id.messageTextView)
            if (message.isSentByUser)
                textView.text = message.text
            else {
                textView.text= parseHtmlStringToMarkdown(message.text)
                if(message.link != null) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.link)
                        .into(imageView)
                } else {
                    imageView.visibility = View.GONE
                }
            }
        }
    }
}