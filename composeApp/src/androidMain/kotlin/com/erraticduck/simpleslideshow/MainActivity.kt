package com.erraticduck.simpleslideshow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {

    private val viewModel: SlideShowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FileKit.init(this)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)

        setContent {
            App(
                viewModel,
                onToggleImmersive = {
                    if (it) {
                        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                },
                onToggleAudio = {
                    if (it) {
                        AudioService.startAudio(this, viewModel.uiState.value.audio)
                    } else {
                        AudioService.stopAudio(this)
                    }
                }
            )
        }

        parseIntent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent()
    }

    private fun parseIntent() = intent?.let {
        when (it.action) {
            Intent.ACTION_SEND_MULTIPLE -> {
                val images = mutableListOf<String>()
                val audio = mutableListOf<String>()

                val uris = IntentCompat.getParcelableArrayListExtra(it, Intent.EXTRA_STREAM, Uri::class.java)
                uris?.forEach { uri ->
                    val mimeType = contentResolver.getType(uri)
                    if (mimeType?.startsWith("image/") == true) {
                        images += uri.toString()
                    } else {
                        audio += uri.toString()
                    }
                }

                viewModel.addImages(images)
                viewModel.addAudio(audio)
            }
            else -> null
        }
    }
}