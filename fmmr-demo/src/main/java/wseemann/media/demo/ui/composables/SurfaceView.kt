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
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SurfaceView(
    onSurfaceAvailable: (Surface) -> Unit,
    modifier: Modifier = Modifier
) {
    var surface by remember { mutableStateOf<Surface?>(null) }
    var isSurfaceAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(surface, isSurfaceAvailable) {
        val currentSurface = surface
        if (currentSurface != null && isSurfaceAvailable) {
            onSurfaceAvailable(currentSurface)
        }
    }

    Box(modifier = modifier) { // Use Box to potentially overlay controls later
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            surface = holder.surface
                            isSurfaceAvailable = true
                            println("Surface created: ${holder.surface}")
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            println("Surface changed: $width x $height")
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            isSurfaceAvailable = false
                            surface = null // Invalidate the surface state
                            println("Surface destroyed")
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize(), // Make SurfaceView fill the Box
            update = { surfaceView ->
                // This block is called when the AndroidView is recomposed.
                // You can update the SurfaceView here if needed, based on state changes.
                // For simple MediaPlayer playback tied to the surface's existence,
                // often not much is needed here once the callback is set.
            }
        )
    }
}