/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame
 * and meta data from an input media file.
 *
 * Copyright 2022 William Seemann
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

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import wseemann.media.FFmpegMediaMetadataRetriever
import wseemann.media.demo.Constants.METADATA_KEYS

class FMMRViewModel : ViewModel() {

    private companion object {
        val TAG: String = FMMRViewModel::class.java.name
    }

    val metadata by lazy {
        MutableLiveData<List<Metadata>>()
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, throwable ->
        throwable.printStackTrace()
        metadata.postValue(listOf())
    }

    fun fetchMetadata(uri: String) {
        viewModelScope.launch(coroutineExceptionHandler) {
            val metadata = arrayListOf<Metadata>()

            val mediaMetadataRetriever = FFmpegMediaMetadataRetriever()

            try {
                if (FMMRFragment.finalSurface != null) {
                    mediaMetadataRetriever.setSurface(FMMRFragment.finalSurface)
                }

                mediaMetadataRetriever.setDataSource(uri)

                for (i in METADATA_KEYS.indices) {
                    val key = METADATA_KEYS[i]
                    val value = mediaMetadataRetriever.extractMetadata(key)

                    if (value != null) {
                        metadata.add(Metadata(key, value))
                        Log.i(TAG, "Key: $key Value: $value")
                    }
                }

                // also extract metadata for chapters
                val count = mediaMetadataRetriever.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT
                    )

                if (count != null) {
                    val chapterCount = count.toInt()

                    for (j in 0 until chapterCount) {
                        for (i in METADATA_KEYS.indices) {
                            val key = METADATA_KEYS[i]
                            val value = mediaMetadataRetriever.extractMetadataFromChapter(key, j)
                            if (value != null) {
                                metadata.add(Metadata(key, value))
                                Log.i(
                                    TAG,
                                    "Key: $key Value: $value"
                                )
                            }
                        }
                    }
                }

                var b = mediaMetadataRetriever.frameAtTime

                if (b != null) {
                    val b2 =
                        mediaMetadataRetriever.getFrameAtTime(
                            4000000,
                            FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                    if (b2 != null) {
                        b = b2
                    }
                }

                if (b != null) {
                    //metadata.add(Metadata("image", b))
                    Log.i(TAG, "Extracted frame")
                } else {
                    Log.e(TAG, "Failed to extract frame")
                }
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            } finally {
                mediaMetadataRetriever.release()
            }

            this@FMMRViewModel.metadata.postValue(metadata)
        }
    }
}
