package au.com.bluedot.minimalintegration

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log

fun parseHtmlStringToMarkdown(htmlString: String): Spanned {
    // Replace <br> with newlines and <b> with markdown bold
    val formattedString = htmlString
        .replace("<br>", "\n")
        .replace("<b>", "**")
        .replace("</b>", "**")
        .replace("<i>","*")
        .replace("</i>","*")
    Log.i("Neha","parseHtmlStringToMarkdown $formattedString")

    val spannableStringBuilder = SpannableStringBuilder(formattedString).also {
        applyBoldStyle(it)
        applyItalicStyle(it)
    }
    return spannableStringBuilder
}

fun applyItalicStyle(spannableStringBuilder: SpannableStringBuilder) {
    // Find the Italic text
    val italicTexts = spannableStringBuilder.toString().split("*")

    if (italicTexts.size == 1) return
    var skip = true
    // Apply Italic style to the text
    for (i in italicTexts.indices) {
        if (skip) {
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

        val styleSpan = StyleSpan(Typeface.ITALIC)
        spannableStringBuilder.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        skip = true
    }

}

fun applyBoldStyle(spannableStringBuilder: SpannableStringBuilder) {
    // Find the bold text
    val boldTexts = spannableStringBuilder.toString().split("**").filter { it.isNotEmpty() }
    var skip = true
    var prevEnd = 0
    // Apply bold style to the text
    for (i in boldTexts.indices) {
        if (skip) {
            skip = false
            continue
        }
        val boldText = boldTexts[i]
        val start = spannableStringBuilder.toString().indexOf(startIndex = prevEnd, string = boldText)
        val end = start + boldText.length
        prevEnd = end
        if (start - 2 >= 0 && start < spannableStringBuilder.length)
            spannableStringBuilder.replace(start - 2, start, "  ")

        if (end+2 <= spannableStringBuilder.length)
            spannableStringBuilder.replace(end, end + 2, "  ")
        val styleSpan = StyleSpan(Typeface.BOLD)
        spannableStringBuilder.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        skip = true
    }

}