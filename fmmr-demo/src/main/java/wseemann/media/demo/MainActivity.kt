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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import wseemann.media.demo.ui.composables.MainScreen
import wseemann.media.demo.ui.theme.FmmrdemoTheme
import wseemann.media.demo.usecase.GetFrameAtTimeUseCase
import wseemann.media.demo.usecase.RetrieveMetadataUseCase
import wseemann.media.demo.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel = MainViewModel(
            retrieveMetadataUseCase = RetrieveMetadataUseCase(),
            getFrameAtTimeUseCase = GetFrameAtTimeUseCase()
        )

        enableEdgeToEdge()
        setContent {
            FmmrdemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val uiState = mainViewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        // mainViewModel.retrieveMetadata("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    }

                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState.value,
                        onClick = { uri ->
                            mainViewModel.retrieveMetadata(uri)
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
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    FmmrdemoTheme {
        // Greeting("Android")
    }
}