package au.com.bluedot.minimalintegration

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun updateMessage(messageTest: String, id: String) {
        val msg = Message(messageTest, false, id)
        if (messagesList.find { it.id == id } == null) {
            messagesList.add(msg)
        } else {
            messagesList.find { it.id == id }?.text = msg.text
            notifyDataSetChanged()
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            val textView = itemView.findViewById<TextView>(R.id.messageTextView)
            if(message.isSentByUser)
                textView.text = message.text
            else {
                val imageUrl = "https://imgproxy.dev.eu2.rezolve.com/unsafe/rs:fit:256:256:0/g:no/aHR0cHM6Ly9zdGF0aWMuc2t5YXNzZXRzLmNvbS9jb250ZW50c3RhY2svYXNzZXRzL2JsdDE0M2UyMGIwM2Q3MjA0N2UvYmx0ZmYyZjEzMjc4YTQxMzE1OS82NjI5MTAyODUyOGZjMWUzMzk1NWI5MDEvUERQX1NhbXN1bmdfRm9sZF9DcmVhbV9hLnBuZw"
                val htmlContent = "This is some text with an image: <img src=\"$imageUrl\">"
                textView.text = parseHtmlStringToMarkdown(message.text)
//                CoroutineScope(Dispatchers.IO).launch {
//                    val img = getImage()
//                    withContext(Dispatchers.Main) {
//                       // img?.setImageBitmap(image) // Or setImageDrawable(), etc.
//                        textView.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, img , null)
//                    }
//                }

            }

        }
    }
}