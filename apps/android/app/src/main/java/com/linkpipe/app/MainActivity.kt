package com.linkpipe.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShare(intent)
        setContent { LinkPipeApp(viewModel) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShare(intent)
    }

    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val source = intent.getStringExtra(EXTRA_SHARE_SOURCE) ?: resolveSource(intent)
        viewModel.receiveShare(SharedPayload(text, source))
    }

    private fun resolveSource(intent: Intent): String {
        val uri = referrer
            ?: @Suppress("DEPRECATION") (intent.getParcelableExtra(Intent.EXTRA_REFERRER) as? Uri)
            ?: intent.getStringExtra(Intent.EXTRA_REFERRER_NAME)?.let(Uri::parse)
        val packageName = uri?.host.orEmpty()
        if (packageName.isBlank()) return ""
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }
}
