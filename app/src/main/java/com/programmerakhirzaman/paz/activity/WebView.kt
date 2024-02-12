package com.programmerakhirzaman.paz.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.programmerakhirzaman.paz.databinding.ActivityWebBinding

class WebView : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val url = "https://rb.gy/43lg0b"
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl(url)
        Log.d("privy:", url )
        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
    }
}