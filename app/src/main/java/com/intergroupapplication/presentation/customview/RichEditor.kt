package com.intergroupapplication.presentation.customview

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.intergroupapplication.data.model.TextType
import com.intergroupapplication.presentation.exstension.dpToPx
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class RichEditor
@SuppressLint("SetJavaScriptEnabled") constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : WebView(context, attrs, defStyleAttr) {

    interface OnTextChangeListener {
        fun onTextChange(text: String?)
    }

    interface OnDecorationStateListener {
        fun onStateChangeListener(
            text: String,
            types: List<TextType>
        )
    }

    interface AfterInitialLoadListener {
        fun onAfterInitialLoad(isReady: Boolean)
    }


    private var isReady = false
    private var content: String? = null
    var textChangeListener: OnTextChangeListener? = null
    var decorationStateListener:OnDecorationStateListener? = null
    private var loadListener: AfterInitialLoadListener? = null

    companion object {
        private const val SETUP_HTML = "file:///android_asset/editor.html"
        private const val CALLBACK_SCHEME = "re-callback://"
        private const val STATE_SCHEME = "re-state://"
    }

    init {
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = false
        settings.javaScriptEnabled = true
        webChromeClient = WebChromeClient()
        webViewClient = EditorWebViewClient()
        loadUrl(SETUP_HTML)
        applyAttributes(context, attrs)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs,
        R.attr.webViewStyle
    )


    fun setOnInitialLoadListener(listener: AfterInitialLoadListener?) {
        loadListener = listener
    }


    private fun callback(text: String) {
        content = text.replaceFirst(CALLBACK_SCHEME.toRegex(), "")
        textChangeListener?.onTextChange(content)
    }

    private fun stateCheck(text: String) {
        val state = text.replaceFirst(STATE_SCHEME.toRegex(), "").
            toUpperCase(Locale.ENGLISH)
        val types: MutableList<TextType> = mutableListOf()
        TextType.values().forEach { type ->
            if (type.name.contains("FONT_COLOR") && state.contains("FONT_COLOR_RGB")) {
                val (r,g,b) = state.substringAfter("FONT_COLOR_RGB(")
                    .substringBefore(")").split(" ")
                var firstColor = Integer.toHexString(r.substringBefore(",").toInt())
                var secondColor = Integer.toHexString(g.substringBefore(",").toInt())
                var thirdColor = Integer.toHexString(b.substringBefore(",").toInt())
                if(firstColor.length == 1) firstColor = "0$firstColor"
                if(secondColor.length == 1) secondColor = "0$secondColor"
                if(thirdColor.length == 1) thirdColor = "0$thirdColor"
                val hex = "#$firstColor$secondColor$thirdColor"
                type.color = Color.parseColor(hex)
                types.add(type)
            }
            else {
                if (TextUtils.indexOf(state, type.name) != -1) {
                    types.add(type)
                }
            }
        }
        decorationStateListener?.onStateChangeListener(state, types)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        val attrsArray = intArrayOf(
            R.attr.gravity
        )
        val ta = context.obtainStyledAttributes(attrs, attrsArray)
        when (ta.getInt(0, NO_ID)) {
            Gravity.LEFT -> exec("javascript:RE.setTextAlign(\"left\")")
            Gravity.RIGHT -> exec("javascript:RE.setTextAlign(\"right\")")
            Gravity.TOP -> exec("javascript:RE.setVerticalAlign(\"top\")")
            Gravity.BOTTOM -> exec("javascript:RE.setVerticalAlign(\"bottom\")")
            Gravity.CENTER_VERTICAL -> exec("javascript:RE.setVerticalAlign(\"middle\")")
            Gravity.CENTER_HORIZONTAL -> exec("javascript:RE.setTextAlign(\"center\")")
            Gravity.CENTER -> {
                exec("javascript:RE.setVerticalAlign(\"middle\")")
                exec("javascript:RE.setTextAlign(\"center\")")
            }
        }
        ta.recycle()
    }

    var html: String?
        get() = content
        set(contents) {
            var thisContents = contents
            if (thisContents == null) {
                thisContents = ""
            }
            try {
                exec("javascript:RE.setHtml('" + URLEncoder.encode(thisContents, "UTF-8") + "');")
            } catch (e: UnsupportedEncodingException) {

            }
        }

    fun setEditorFontColor(color: Int): RichEditor {
        val hex = convertHexColorString(color)
        exec("javascript:RE.setBaseTextColor('$hex');")
        return this
    }

    fun setEditorFontSize(px: Int): RichEditor {
        exec("javascript:RE.setBaseFontSize('" + px + "px');")
        return this
    }

    fun setEditorPadding(left: Int, top: Int, right: Int, bottom: Int): RichEditor {
        super.setPadding(left, top, right, bottom)
        exec(
            "javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                    + "px');"
        )
        return this
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        // still not support RTL.
        setPadding(start, top, end, bottom)
    }

    fun setEditorBackgroundColor(color: Int): RichEditor {
        setBackgroundColor(color)
        return this
    }

    fun setBackground(url: String) {
        exec("javascript:RE.setBackgroundImage('url($url)');")
    }

    fun setEditorHeight(px: Int): RichEditor {
        exec("javascript:RE.setHeight('" + px + "px');")
        return this
    }

    fun setPlaceholder(placeholder: String) {
        exec("javascript:RE.setPlaceholder('$placeholder');")
    }

    fun setInputEnabled(inputEnabled: Boolean) {
        exec("javascript:RE.setInputEnabled($inputEnabled)")
    }

    fun setBold() {
        clearAndFocusEditor()
        exec("javascript:RE.setBold();")
    }

    fun setItalic() {
        clearAndFocusEditor()
        exec("javascript:RE.setItalic();")
    }

    fun setSubscript() {
        exec("javascript:RE.setSubscript();")
    }

    fun setSuperscript() {
        exec("javascript:RE.setSuperscript();")
    }

    fun setStrikeThrough() {
        clearAndFocusEditor()
        exec("javascript:RE.setStrikeThrough();")
    }

    fun setUnderline() {
        clearAndFocusEditor()
        exec("javascript:RE.setUnderline();")
    }

    fun setTextColor(color: Int) {
        exec("javascript:RE.backuprange();")
        val hex = convertHexColorString(color)
        exec("javascript:RE.setTextColor('$hex');")
    }

    fun setFontSize(fontSize: Int) {
        if (fontSize > 7 || fontSize < 1) {
            Log.e("RichEditor", "Font size should have a value between 1-7")
        }
        exec("javascript:RE.setFontSize('$fontSize');")
    }

    fun removeFormat() {
        exec("javascript:RE.removeFormat();")
    }

    fun setHeading(heading: Int) {
        exec("javascript:RE.setHeading('$heading');")
    }

    fun setAlignLeft() {
        exec("javascript:RE.setJustifyLeft();")
    }

    fun setAlignCenter() {
        exec("javascript:RE.setJustifyCenter();")
    }

    fun setAlignRight() {
        exec("javascript:RE.setJustifyRight();")
    }

    fun setBlockquote() {
        exec("javascript:RE.setBlockquote();")
    }

    fun setBullets() {
        exec("javascript:RE.setBullets();")
    }

    fun setNumbers() {
        exec("javascript:RE.setNumbers();")
    }

    fun insertImage(url: String, alt: String) {
        exec("javascript:RE.prepareInsert();")
        exec("javascript:RE.insertImage('$url', '$alt');")
    }

    fun insertAudio(url: String) {
        exec("javascript:RE.prepareInsert();")
        exec("javascript:RE.insertAudio('$url');")
    }

    fun insertVideo(url: String) {
        exec("javascript:RE.prepareInsert();")
        val width = context.dpToPx(115)
        exec("javascript:RE.insertVideo('$url', '$width');")
    }

    fun clearFocusEditor() {
        exec("javascript:RE.blurFocus();")
    }

    private fun clearAndFocusEditor() {
        exec("javascript:RE.clearAndFocusEditor();")
    }

    fun createFinalText(namesMap: Map<String,String>, finalNamesMedia:MutableList<String>):String{
        var text = html?.substringBeforeLast("re-state://")?:""
        namesMap.forEach { (key,value)->
            if (text.contains(key)){
                finalNamesMedia.add(value)
                text = text.substringBefore(key)+"$value${PostCustomView.MEDIA_PREFIX}"+
                        text.substringAfter(key)

            }
        }
        return text
    }

    private fun convertHexColorString(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and  color))
    }

    private fun exec(trigger: String) {
        if (isReady) {
            load(trigger)
        } else {
            postDelayed({ exec(trigger) }, 100)
        }
    }

    private fun load(trigger: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null)
        } else {
            loadUrl(trigger)
        }
    }

    inner class EditorWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            isReady = url.equals(SETUP_HTML, ignoreCase = true)
            loadListener?.onAfterInitialLoad(isReady)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                val decode: String
                var reCallback = ""
                var reState = ""
                val isRegexFound: Boolean
                try {
                    decode = URLDecoder.decode(it.url.toString(), "UTF-8")
                    val pattern = "(re-callback://.*)(re-state://.*)"
                    val m: Matcher
                    val p: Pattern = Pattern.compile(pattern)
                    m = p.matcher(decode)
                    isRegexFound = m.find()
                    if (isRegexFound) {
                        m.group(1)?.let {string -> reCallback = string }
                        m.group(2)?.let{ string -> reState = string }
                    }
                } catch (e: UnsupportedEncodingException) {
                    return false
                }

                when {
                    isRegexFound -> {
                        callback(reCallback)
                        stateCheck(reState)
                        return true
                    }
                    TextUtils.indexOf(it.url.toString(), STATE_SCHEME) == 0 -> {
                        stateCheck(decode)
                        return true
                    }
                    TextUtils.indexOf(it.url.toString(), CALLBACK_SCHEME) == 0 -> {
                        callback(decode)
                        return true
                    }
                    else -> return super.shouldOverrideUrlLoading(view, request)
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }
}