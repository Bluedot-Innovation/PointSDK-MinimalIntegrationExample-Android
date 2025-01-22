package au.com.bluedot.minimalintegration

data class Message(
    var text: String,
    val isSentByUser: Boolean, // True if the message is sent by the user
    val id: String
)
