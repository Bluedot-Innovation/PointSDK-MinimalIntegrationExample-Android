package au.com.bluedot.minimalintegration

import java.util.UUID

data class Message(
    var text: String,
    val isSentByUser: Boolean = true, // True if the message is sent by the user
    val id: UUID = UUID.randomUUID(), // Unique identifier for the message
    var link: String? = null,
    var like: Boolean = false,
    var dislike: Boolean = false,
    var responseId: String? = null,
    var isLoading: Boolean = false
)
