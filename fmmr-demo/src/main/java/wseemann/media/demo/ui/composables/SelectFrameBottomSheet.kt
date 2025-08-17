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

@file:OptIn(ExperimentalMaterial3Api::class)

package wseemann.media.demo.ui.composables

import android.view.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wseemann.media.FFmpegMediaMetadataRetriever
import wseemann.media.demo.R
import wseemann.media.demo.model.MetadataModel

@Composable
fun SelectFrameBottomSheet(
    metadataModel: MetadataModel,
    onFrameSelected: (uri: String, timeUs: Long, surface: Surface) -> Unit,
    onDismiss: () -> Unit
) {
    val duration = metadataModel.metadata[FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION]?.toLong() ?: 0L

    var currentSurface by remember { mutableStateOf<Surface?>(null) }
    var showFrameSlider by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SurfaceView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onSurfaceAvailable = { surface ->
                    currentSurface = surface
                    showFrameSlider = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (showFrameSlider) {
                SelectFrameSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    totalDurationMs = duration,
                    initialPositionMillis = 0L,
                    onFrameSelectedMillis = { timeUs ->
                        currentSurface?.let { surface ->
                            onFrameSelected(metadataModel.uri.orEmpty(), timeUs, surface)
                        }
                    }
                )
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.close))
            }
        }
    }
}