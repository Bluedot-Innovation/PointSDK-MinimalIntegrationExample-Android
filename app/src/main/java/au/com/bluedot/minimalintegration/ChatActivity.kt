package au.com.bluedot.minimalintegration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.com.bluedot.point.ChatAIError
import au.com.bluedot.point.net.engine.BDStreamingResponseDtoContext
import au.com.bluedot.point.net.engine.Chat
import au.com.bluedot.point.net.engine.ServiceManager
import au.com.bluedot.point.net.engine.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.io.IOException
import java.util.UUID

const val TAG = "ChatActivity"
class ChatActivity: AppCompatActivity(), ChatAdapter.ChatAdapterListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private var chat: Chat? = null
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

        chatAdapter = ChatAdapter(messages, this)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        val brainAI = ServiceManager.getInstance(this).brainAI
        chat = brainAI.createNewChat()

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


                // Simulate response processing until response arrives
                val resMsg = Message("...", isSentByUser = false, isLoading = true)
                val msgId = resMsg.id
                lifecycleScope.launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        chatAdapter.addMessage(resMsg)
                        chatRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            var resMessageText = ""
                            var contextData: List<BDStreamingResponseDtoContext>

                            chat!!.sendMessage(messageText).forEach { res ->

                                if (res.stream_type == StreamType.RESPONSE_TEXT) {
                                    resMessageText += res.response
                                    runOnUiThread {
                                        chatAdapter.updateMessage(resMessageText, msgId)
                                        chatRecyclerView.scrollToPosition(messages.size - 1)
                                    }
                                }

                                if (res.stream_type == StreamType.RESPONSE_IDENTIFIER) {
                                    val responseId = res.response_id
                                    Log.i(TAG, "Response: responseId  $responseId")
                                    runOnUiThread {
                                        chatAdapter.updateMessageResponseId(responseId, msgId)
                                    }
                                }

                                if (res.stream_type == StreamType.CONTEXT) {
                                    contextData = res.contexts
                                    runOnUiThread {
                                        if (contextData.isNotEmpty()) {
                                            Log.i(TAG, "Response: contextData  ${contextData.size}")
                                            contextData[0].also {
                                                val imageLink = it.image_links?.get(0)
                                                val productDto = "\n "+ it.title +  "\n Price: " + it.price + "\n "
                                                resMessageText += productDto + "\n"
                                                chatAdapter.updateMessage(resMessageText, msgId, imageLink)
                                                chatRecyclerView.scrollToPosition(messages.size - 1)
                                            }
                                        }
                                    }
                                }

                                if (res.stream_type == StreamType.RESPONSE_END) {
                                    runOnUiThread {
                                        chatAdapter.updateMessageEnd(msgId)
                                        chatRecyclerView.scrollToPosition(messages.size - 1)
                                        Log.i(TAG, "Response: End ")
                                    }
                                }
                            }
                        } catch (exp: Exception) {
                            Log.i(TAG, "Exception: ${exp.localizedMessage}")
                            var msg = "Error Occurred"
                            //Ignore IOException as it is expected at the end of stream except EOFException
                            if (exp is IOException) {
                                //EOFException due to Network issues, report to user
                                if (exp is EOFException)
                                    msg = "Oh no!!! Network error occurred"
                                else
                                    return@withContext
                            }

                            //For any other exception report to user
                            runOnUiThread {
                                chatAdapter.removeMessage(msgId)
                                Toast.makeText(this@ChatActivity, msg, Toast.LENGTH_SHORT)
                                    .show()
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

    override fun processFeedback(id: UUID, feedback: Chat.ChatFeedback) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (chat == null) {
                    Log.i(TAG, "Chat is not initialized")
                    return@withContext
                }

                val msg = messages.find { it.id == id }
                val responseId = msg?.responseId
                Log.i(TAG,"responseId for submitFeedback is $responseId")


                if (responseId == null) {
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "Response Id is not available for this response", Toast.LENGTH_SHORT)
                            .show()
                        return@runOnUiThread
                    }
                    return@withContext
                }

                val error: ChatAIError? = chat?.submitFeedback(responseId, feedback)
                runOnUiThread {
                    if (error != null) {
                        Toast.makeText(
                            this@ChatActivity,
                            "Error occurred while reporting feedback",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "Error occurred while reporting feedback: $error")
                    } else {
                        chatRecyclerView.adapter.apply {
                            val linearLayoutManager =
                                chatRecyclerView.layoutManager as LinearLayoutManager
                            if (feedback == Chat.ChatFeedback.LIKED)
                                linearLayoutManager.findViewByPosition(messages.indexOf(msg))
                                    ?.findViewById<ImageView>(R.id.likeButton)?.isSelected =
                                    true
                            else
                                linearLayoutManager.findViewByPosition(messages.indexOf(msg))
                                    ?.findViewById<ImageView>(R.id.dislikeButton)?.isSelected =
                                    true
                        }?.notifyItemChanged(messages.indexOf(msg))
                        Toast.makeText(
                            this@ChatActivity,
                            "Feedback reported successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
