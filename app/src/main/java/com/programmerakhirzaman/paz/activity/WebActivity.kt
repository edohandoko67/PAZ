package com.programmerakhirzaman.paz.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.programmerakhirzaman.paz.R
import com.programmerakhirzaman.paz.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl("https://paz-official.000webhostapp.com")
        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
    }
}