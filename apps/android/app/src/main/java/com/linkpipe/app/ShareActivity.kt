package com.linkpipe.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.linkpipe.app.data.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var preferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (intent.action != Intent.ACTION_SEND || intent.type != "text/plain" || text == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            val quickSave = preferences.shareBehavior.first().quickSave
            if (!quickSave || auth.currentUser == null) {
                openMainFlow(text)
                return@launch
            }

            setContent { ShareProgressScreen(viewModel, onFinished = ::finish) }
            viewModel.receiveShare(SharedPayload(text, resolveSource()))
        }
    }

    private fun openMainFlow(text: String) {
        val source = resolveSource()
        startActivity(
            Intent(this, MainActivity::class.java)
                .setAction(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(EXTRA_SHARE_SOURCE, source)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
        finish()
    }

    private fun resolveSource(): String {
        val packageName = referrer?.host.orEmpty()
        if (packageName.isBlank()) return ""
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }
}

@Composable
private fun ShareProgressScreen(viewModel: AppViewModel, onFinished: () -> Unit) {
    val state by viewModel.shareSaveState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state == ShareSaveState.Saved) onFinished()
    }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (val current = state) {
                        ShareSaveState.Idle, ShareSaveState.Saving -> {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.saving_link))
                        }
                        ShareSaveState.Saved -> Text(stringResource(R.string.saved))
                        is ShareSaveState.Failed -> {
                            Text(current.message)
                            Button(onClick = onFinished) { Text(stringResource(R.string.close)) }
                        }
                    }
                }
            }
        }
    }
}
