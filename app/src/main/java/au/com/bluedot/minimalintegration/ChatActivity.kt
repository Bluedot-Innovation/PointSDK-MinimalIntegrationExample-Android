package au.com.bluedot.minimalintegration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
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
import java.io.IOException
import java.util.UUID

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

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(messageText, true, UUID.randomUUID().toString())
                chatAdapter.addMessage(message)
                messageEditText.text.clear()
                chatRecyclerView.scrollToPosition(messages.size - 1)

                val brainAI = ServiceManager.getInstance(this).brainAI
                if (brainAI == null) {
                    Toast.makeText(this, "BrainAI not initialized", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val chat = brainAI.createNewChat()

                if (chat == null) {
                    Toast.makeText(this, "Chat not created, ensure rezolveChatApiKey and rezolveChatApiUrl are setup in global config", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val context = this.applicationContext
                val id = UUID.randomUUID().toString()

                chatAdapter.updateMessage("...", id)
                chatRecyclerView.scrollToPosition(messages.size - 1)

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            var resMessageText = ""
                            var contextData: List<BDStreamingResponseDtoContext>
//                           resMessageText = "Thanks a bunch for getting in touch with us! We're always excited to help our customers find the perfect gadget that meets their needs. If you're on the lookout for a device that blends innovation with style, you might be interested in the latest release we have at Bluedot Store.                                                                \n" +
//                                   "Currently, we have the **Samsung Galaxy Z Fold5 5G with Galaxy AI**   in stock, a cutting-edge model that represents the pinnacle of Samsung's foldable technology. This sophistically designed device comes in color variants of Icy Blue, Phantom Black, and Cream, allowing you to choose the one that best reflects your personal style. Whether you require ample storage for your work and play, you can opt for either 256GB or 512GB storage variants, ensuring you have all the space you need for your photos, videos, apps, and much more.\n" +
//                                   "The **Samsung Galaxy Z Fold5 5G with Galaxy AI** boasts a robust suite of features. With its massive 7.6” main display, it promises unparalleled productivity and entertainment experiences. It's not just a phone; it transforms into a tablet, providing you with a massive screen for all your needs, from gaming to professional tasks. The device’s Pro-grade camera setup includes a 50MP main camera, a 12MP Ultra Wide, and a 10MP 3x Optical Zoom lens, ensuring stunning photography from any distance. Its unique Flex Mode allows for hands-free video calls and streaming, enhancing multitasking capabilities.                                                                                                    \n" +
//                                   "This model is designed to be Samsung's slimmest and lightest fold yet, without compromising on durability with its Armour Aluminium frame and Gorilla® Glass Victus® 2. Moreover, it offers IPX8 water resistance, making it resilient against rain and accidental splashes. For gamers and heavy users, the Galaxy Z Fold5 is equipped with an upgraded Qualcomm Snapdragon® 8 Gen 2 Processor for Galaxy, providing longer gaming sessions without the need for constant recharging.\n" +
//                                   "In summary, while we do not have the initial Samsung Galaxy Fold, the **Samsung Galaxy Z Fold5 5G with Galaxy AI** we offer might just exceed your expectations with its latest advancements in foldable smartphone technology, promising an unmatched combination of productivity, entertainment, durability, and style\n"
//                            runOnUiThread {
//                                chatAdapter.updateMessage(resMessageText, id)
//                                chatRecyclerView.scrollToPosition(messages.size -1)
//                            }
                            chat.sendMessage(context, messageText).forEach { res ->

                                if (res.stream_type == StreamType.RESPONSE_TEXT) {
                                    resMessageText += res.response
                                    runOnUiThread {
                                        chatAdapter.updateMessage(resMessageText, id)
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
                                                val productDto = it.title +  "\n Price: " + it.price + "\n " + imageLink
                                                resMessageText += productDto + "\n"

                                                chatAdapter.updateMessage(resMessageText, id)
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

                            //Ignore IOException as it is expected at the end of stream
                            if (exp is IOException) {
                               return@withContext
                            }

                            //For any other exception report to user
                            runOnUiThread {
                                chatAdapter.updateMessage("Error occurred", id)
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
