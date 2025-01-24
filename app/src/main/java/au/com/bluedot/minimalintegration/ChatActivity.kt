package au.com.bluedot.minimalintegration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.bluedot.point.net.engine.BDStreamingResponseDtoContext
import au.com.bluedot.point.net.engine.ServiceManager
import au.com.bluedot.point.net.engine.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.io.IOException

const val TAG = "ChatActivity"
class ChatActivity: AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        // Find the custom toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Find the plus icon
        val plusIcon = findViewById<TextView>(R.id.plusIcon)

        // Set a click listener for the plus icon
        plusIcon.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            this.startActivity(intent)
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        val brainAI = ServiceManager.getInstance(this).brainAI
        val chat = brainAI.createNewChat()

        if (brainAI == null) {
            Toast.makeText(this, "BrainAI not initialized", Toast.LENGTH_SHORT).show()
            return
        }

        if (chat == null) {
            Toast.makeText(this, "Chat not created, ensure rezolveChatApiKey and rezolveChatApiUrl are setup in global config", Toast.LENGTH_SHORT).show()
            return
        }

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(messageText)
                chatAdapter.addMessage(message)
                messageEditText.text.clear()
                chatRecyclerView.scrollToPosition(messages.size - 1)
                val context = this.applicationContext
                val resMsg = Message("...", isSentByUser = false)
                val msgId = resMsg.id

                chatAdapter.addMessage(resMsg)
                chatRecyclerView.scrollToPosition(messages.size - 1)

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            var resMessageText = ""
                            var contextData: List<BDStreamingResponseDtoContext>

                            chat.sendMessage(context, messageText).forEach { res ->

                                if (res.stream_type == StreamType.RESPONSE_TEXT) {
                                    resMessageText += res.response
                                    runOnUiThread {
                                        chatAdapter.updateMessage(resMessageText, msgId)
                                        chatRecyclerView.scrollToPosition(messages.size - 1)
                                    }
                                }
                                if (res.stream_type == StreamType.CONTEXT) {
                                    contextData = res.contexts
                                    runOnUiThread {
                                        if (contextData.isNotEmpty()) {
                                            Log.i(TAG, "Response: contextData  ${contextData.size}")
                                            contextData.forEach {
                                                val imageLink = it.image_links?.get(0)
                                                val productDto = it.title +  "\n Price: " + it.price + "\n "
                                                resMessageText += productDto + "\n"
                                                chatAdapter.updateMessage(resMessageText, msgId, imageLink)
                                                chatRecyclerView.scrollToPosition(messages.size - 1)
                                            }
                                        }
                                    }
                                }
                                if (res.stream_type == StreamType.RESPONSE_END) {
                                    Log.i(TAG, "Response: End ")
                                    return@forEach
                                }
                            }
                        } catch (exp: Exception) {
                            exp.printStackTrace()
                            Log.i(TAG, "Exception: ${exp.localizedMessage}")

                            var msg = "Error Occurred"
                            //Ignore IOException as it is expected at the end of stream
                            if (exp is IOException) {
                                if (exp is EOFException)
                                    msg = "NetworkError EOFException encountered"
                                else
                                    return@withContext
                            }

                            //For any other exception report to user
                            runOnUiThread {
                                chatAdapter.updateMessage(msg, msgId)
                                chatRecyclerView.scrollToPosition(messages.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    // Handle the back button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
