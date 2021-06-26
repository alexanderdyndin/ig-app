package com.intergroupapplication.presentation.customview

import android.R
import android.annotation.SuppressLint
import android.content.Context
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
    private var textChangeListener: OnTextChangeListener? = null
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

    fun setOnTextChangeListener(listener: OnTextChangeListener?) {
        textChangeListener = listener
    }


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
            if (type.r == -1) {
                if (TextUtils.indexOf(state, type.name) != -1) {
                    types.add(type)
                }
            } else {
                val color: String
                if (type.name.contains("FONT_COLOR")) {
                    color = "FONT_COLOR_RGB(" + type.r + ", " + type.g + ", " + type.b + ")"
                    if (TextUtils.indexOf(state, color) != -1) {
                        types.add(type)
                    }
                } else if (type.name.contains("BACKGROUND_COLOR")) {
                    color = "BACKGROUND_COLOR_RGB(" + type.r + ", " + type.g + ", " + type.b + ")"
                    if (TextUtils.indexOf(state, color) != -1) {
                        types.add(type)
                    }
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

    fun loadCSS(cssFile: String) {
        val jsCSSImport = ("(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();")
        exec("javascript:$jsCSSImport")
    }

    fun setBold() {
        exec("javascript:RE.setBold();")
    }

    fun setItalic() {
        exec("javascript:RE.setItalic();")
    }

    fun setSubscript() {
        exec("javascript:RE.setSubscript();")
    }

    fun setSuperscript() {
        exec("javascript:RE.setSuperscript();")
    }

    fun setStrikeThrough() {
        exec("javascript:RE.setStrikeThrough();")
    }

    fun setUnderline() {
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
        val width = context.dpToPx(130)
        exec("javascript:RE.insertVideo('$url', '$width');")
    }

    fun insertLink(href: String, title: String) {
        exec("javascript:RE.prepareInsert();")
        exec("javascript:RE.insertLink('$href', '$title');")
    }

    fun focusEditor() {
        requestFocus()
        exec("javascript:RE.focus();")
    }

    fun clearFocusEditor() {
        exec("javascript:RE.blurFocus();")
    }

    fun clearAndFocusEditor() {
        exec("javascript:RE.clearAndFocusEditor();")
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
                        m.group(1)?.let { reCallback = it }
                        m.group(2)?.let{ reState = it }
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