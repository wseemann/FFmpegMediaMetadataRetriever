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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import wseemann.media.demo.R
import wseemann.media.demo.model.MetadataModel
import wseemann.media.demo.ui.theme.FmmrdemoTheme
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun MetadataList(
    metadataModel: MetadataModel,
    modifier: Modifier = Modifier
) {
    val metadata = metadataModel.metadata
    val frameBitmap = metadataModel.frameBitmap

    if (metadata.isEmpty() && frameBitmap == null) {
        EmptyMetadataListContent(modifier = modifier)
    } else {
        MetadataListContent(
            metadataModel = metadataModel,
            modifier = modifier
        )
    }
}

@Composable
private fun MetadataListContent(
    metadataModel: MetadataModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        if (metadataModel.frameBitmap != null) {
            item {
                MetadataListImageHeader(bitmap = metadataModel.frameBitmap)
            }
        }
        metadataModel.metadata.forEach { (metadataKey, metadataValue) ->
            item {
                HorizontalDivider(
                    modifier = Modifier.height(1.dp),
                    color = MaterialTheme.colorScheme.outline
                )
                MetadataListItem(
                    metadataKey = metadataKey,
                    metadataValue = metadataValue
                )
            }
        }
        item {
            HorizontalDivider(
                modifier = Modifier.height(1.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun EmptyMetadataListContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.no_metadata))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMetadataList() {
    FmmrdemoTheme {
        MetadataList(
            metadataModel = MetadataModel(metadata = mapOf("key" to "value"))
        )
    }
}