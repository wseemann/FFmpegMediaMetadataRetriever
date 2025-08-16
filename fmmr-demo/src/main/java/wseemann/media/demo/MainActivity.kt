/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame
 * and meta data from an input media file.
 *
 * Copyright 2025 William Seemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import wseemann.media.demo.ui.composables.MainScreen
import wseemann.media.demo.ui.theme.FmmrdemoTheme
import wseemann.media.demo.usecase.GetFrameAtTimeUseCase
import wseemann.media.demo.usecase.RetrieveMetadataUseCase
import wseemann.media.demo.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = MainViewModel(
            retrieveMetadataUseCase = RetrieveMetadataUseCase(),
            getFrameAtTimeUseCase = GetFrameAtTimeUseCase()
        )

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current

            FmmrdemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val uiState = mainViewModel.uiState.collectAsState()

                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState.value,
                        onClick = { uri ->
                            mainViewModel.retrieveMetadata(
                                context = context,
                                uri = uri
                            )
                        },
                        onFrameSelected = { uri, timeUs, surface ->
                            mainViewModel.retrieveFrame(
                                uri = uri,
                                timeUs = timeUs,
                                surface = surface
                            )
                        }
                    )
                }
            }
        }

        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {
            when {
                // File share (audio/video/etc.)
                intent.type?.startsWith("audio/") == true || intent.type?.startsWith("video/") == true -> {
                    val uri = intent.getParcelableExtra<Uri?>(Intent.EXTRA_STREAM)
                    if (uri != null) {
                        setIntent(null)
                        mainViewModel.retrieveMetadata(context = this, uri = uri.toString())
                        return
                    }
                }
            }
        }

        /* Populate the edit text field with the intent uri, if available
        val uri = intent.data

        if (intent.extras != null && intent.extras!!.getCharSequence(Intent.EXTRA_TEXT) != null) {
            val uri = intent.extras!!.getCharSequence(Intent.EXTRA_TEXT).toString().toUri()
        }

        if (uri != null) {
            try {
                // uriText.setText(URLDecoder.decode(uri.toString(), "UTF-8"))
            } catch (ex: UnsupportedEncodingException) {
            }
        }

        setIntent(null)*/
    }
}