package au.com.bluedot.minimalintegration

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.inputStream

fun parseHtmlStringToMarkdown(htmlString: String): Spanned {
    // Replace <br> with newlines and <b> with markdown bold
    val formattedString = htmlString
        .replace("<br>", "\n")
        .replace("<b>", "**")
        .replace("</b>", "**")
        .replace("<i>","*")
        .replace("</i>","*")
    Log.i("Neha","parseHtmlStringToMarkdown $formattedString")

    // Convert markdown to HTML-like format
  //  val htmlText = convertMarkdownToHtml(formattedString)

 //  val spanned: Spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)

    val spannableStringBuilder = SpannableStringBuilder(formattedString).also {
        applyBoldStyle(it)
        applyItalicStyle(it)
    }
   // Log.i("Neha", "spannableStringBuilder $spannableStringBuilder")
    return spannableStringBuilder
}

fun applyItalicStyle(spannableStringBuilder: SpannableStringBuilder) {
    // Find the Italic text
    val italicTexts = spannableStringBuilder.toString().split("*")

    Log.i("Neha", "italicTexts ${italicTexts.size}")

    if (italicTexts.size == 1) return
    var skip = true
    // Apply Italic style to the text
    for (i in italicTexts.indices) {
       // if (i == 0) continue
        if (skip) {
           // Log.i("Neha", "skip")
            skip = false
            continue
        }
        val italicText = italicTexts[i]
        val start = spannableStringBuilder.toString().indexOf(italicText)
        val end = start + italicText.length
        if (start - 1 >= 0 && start < spannableStringBuilder.length)
        spannableStringBuilder.replace(start - 1, start, " ")

        if (end+1 <= spannableStringBuilder.length)
        spannableStringBuilder.replace(end, end + 1, " ")
       // Log.i("Neha", "spannableStringBuilder Italic replace $spannableStringBuilder")

        val styleSpan = StyleSpan(Typeface.ITALIC)
        //Log.i("Neha", "italicText ${italicText.length} start $start end $end")
        spannableStringBuilder.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        skip = true
    }

}

fun applyBoldStyle(spannableStringBuilder: SpannableStringBuilder) {
    // Find the bold text
    val boldTexts = spannableStringBuilder.toString().split("**").filter { it.isNotEmpty() }

    Log.i("Neha", "boldTexts ${boldTexts.size}")

    var skip = true
    var prevEnd = 0
    // Apply bold style to the text
    for (i in boldTexts.indices) {
      //  Log.i("Neha"," boldTexts[$i] is ${boldTexts[i]}")
        if (skip) {
             //Log.i("Neha", "skip")
            skip = false
            continue
        }
        val boldText = boldTexts[i]
        val start = spannableStringBuilder.toString().indexOf(startIndex = prevEnd, string = boldText)
        val end = start + boldText.length
        prevEnd = end
        Log.i("Neha", "boldText ${boldText.length} start $start end $end")
        if (start - 2 >= 0 && start < spannableStringBuilder.length)
            spannableStringBuilder.replace(start - 2, start, "  ")

        if (end+2 <= spannableStringBuilder.length)
            spannableStringBuilder.replace(end, end + 2, "  ")
        Log.i("Neha", "spannableStringBuilder replace $spannableStringBuilder")
        val styleSpan = StyleSpan(Typeface.BOLD)
        spannableStringBuilder.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        skip = true
    }

}

fun convertMarkdownToHtml(markdownText: String): String {
    var htmlText = markdownText
        .replace("(?m)^#+\\s*(.*?)\\s*#+\$", "<h1>$1</h1>") // Headers
        .replace("(?m)^\\*\\s*(.*?)\\s*\\*$", "<li>$1</li>") // List items
        .replace("\\*\\*(.*?)\\*\\*", "<b>$1</b>") // Bold
        .replace("_([^_]+)_", "<i>$1</i>") // Italic
        .replace("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>") // Links
        .replace("`([^`]+)`", "<code>$1</code>") // Code
        .replace("---", "<hr>") // Horizontal rule
        .replace("\n", "<br>") // Line breaks

    return htmlText
}


suspend fun getImage() =
    Html.ImageGetter { source ->
        try {

            val url = URL(source)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val drawable = Drawable.createFromStream(input, "src")
            drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
           return@ImageGetter drawable
        } catch (e: Exception) {
            Log.e("ImageGetter", "Failed to load image: ${e.message}", e)
            null
        }
    }
