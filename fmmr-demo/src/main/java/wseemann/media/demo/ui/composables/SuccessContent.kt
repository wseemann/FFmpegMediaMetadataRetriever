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

package wseemann.media.demo.ui.composables

import android.view.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import wseemann.media.FFmpegMediaMetadataRetriever
import wseemann.media.demo.R
import wseemann.media.demo.model.MetadataModel

@Composable
fun SuccessContent(
    metadataModel: MetadataModel,
    onFrameSelected: (uri: String, timeUs: Long, surface: Surface) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            MetadataList(metadataModel = metadataModel)
        }
        if (metadataModel.frameBitmap != null || metadataModel.metadata[FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION] != null) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
                onClick = {
                    showBottomSheet = true
                }
            ) {
                Text(stringResource(R.string.select_frame))
            }
        }
    }

    if (showBottomSheet) {
        SelectFrameBottomSheet(
            metadataModel = metadataModel,
            onFrameSelected = { uri, timeUs, surface ->
                onFrameSelected(uri, timeUs, surface)
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}