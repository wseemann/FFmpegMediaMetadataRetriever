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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Composable
fun SelectFrameSlider(
    totalDurationMs: Long, // Total duration in milliseconds
    onFrameSelectedMillis: (timeUs: Long) -> Unit, // Callback with selected time in millis
    modifier: Modifier = Modifier,
    initialPositionMillis: Long = 0L
) {
    if (totalDurationMs <= 0) {
        Text("Invalid duration", modifier = modifier)
        return
    }

    // Slider position is a float from 0.0 to totalDurationMillis.toFloat()
    var sliderPosition by remember { mutableFloatStateOf(initialPositionMillis.toFloat()) }

    // Derived state for the currently selected time in milliseconds
    val selectedTimeMillis by remember { derivedStateOf { sliderPosition.roundToLong() } }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = sliderPosition,
            onValueChange = { newValue ->
                sliderPosition = newValue
            },
            valueRange = 0f..totalDurationMs.toFloat(), // Range from 0 to duration
            onValueChangeFinished = {
                onFrameSelectedMillis(selectedTimeMillis * 1000)
            }
            // Optional: Add steps if you want discrete frame selection
            // steps = (totalDurationMillis / 1000).toInt() - 1 // Example: 1 step per second
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selected: ${formatMillisToTimestamp(selectedTimeMillis)} / ${
                formatMillisToTimestamp(
                    totalDurationMs
                )
            }"
        )
    }
}

private fun formatMillisToTimestamp(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// Optional: Helper function to convert millis to an approximate frame number
private fun millisToFrame(millis: Long, fps: Int): Long {
    if (fps <= 0) return 0
    return (millis / 1000.0 * fps).roundToLong()
}