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

package wseemann.media.demo.api

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import wseemann.media.FFmpegMediaMetadataRetriever

object FFmpegMediaMetadataRetrieverSingleton {
    private var ffmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
    private var surfaceSet = false

    fun setSurface(surface: Surface) {
        if (!surfaceSet) {
            ffmpegMediaMetadataRetriever.setSurface(surface)
            surfaceSet = true
        }
    }

    fun setDataSource(uri: String) {
        ffmpegMediaMetadataRetriever.setDataSource(uri)
    }

    fun getFrameAtTime(timeUs: Long): Bitmap? {
        return ffmpegMediaMetadataRetriever.getFrameAtTime(timeUs)
    }

    fun release() {
        ffmpegMediaMetadataRetriever.release()
        ffmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()
        surfaceSet = false
    }
}