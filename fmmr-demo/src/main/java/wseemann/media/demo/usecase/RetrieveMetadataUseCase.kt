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

package wseemann.media.demo.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import wseemann.media.FFmpegMediaMetadataRetriever
import wseemann.media.demo.api.FFmpegMediaMetadataRetrieverSingleton
import wseemann.media.demo.constants.MetadataConstants
import wseemann.media.demo.model.MetadataModel

class RetrieveMetadataUseCase {

    operator fun invoke(uri: String): Result<MetadataModel> {
        val metadata = hashMapOf<String, String>()
        val ffmpegMediaMetadataRetriever = FFmpegMediaMetadataRetriever()

        try {
            ffmpegMediaMetadataRetriever.setDataSource(uri)

            FFmpegMediaMetadataRetrieverSingleton.release()
            FFmpegMediaMetadataRetrieverSingleton.setDataSource(uri)

            metadata.putAll(extractAllMetadata(ffmpegMediaMetadataRetriever))
            extractChapterMetadata(ffmpegMediaMetadataRetriever).forEach { metadata.putAll(it) }
            val frameBitmap = getFrameAtTime(ffmpegMediaMetadataRetriever)
            // val frameBitmap = getBitmapFromEmbeddedPicture(ffmpegMediaMetadataRetriever)
            ffmpegMediaMetadataRetriever.release()

            return Result.success(
                MetadataModel(
                    uri = uri,
                    metadata = metadata,
                    frameBitmap = frameBitmap
                )
            )
        } catch (ex: Exception) {
            ffmpegMediaMetadataRetriever.release()
            return Result.failure(ex)
        }
    }

    private fun extractAllMetadata(
        ffmpegMediaMetadataRetriever: FFmpegMediaMetadataRetriever
    ): Map<String, String> {
        val metadata = hashMapOf<String, String>()

        MetadataConstants.METADATA_KEYS.forEach { metadataKey ->
            val value = ffmpegMediaMetadataRetriever.extractMetadata(metadataKey)

            if (value != null) {
                metadata.put(metadataKey, value)
            }
        }

        return metadata
    }

    private fun extractChapterMetadata(
        ffmpegMediaMetadataRetriever: FFmpegMediaMetadataRetriever
    ): List<Map<String, String>> {
        val metadataList = arrayListOf<Map<String, String>>()

        // also extract metadata for chapters
        val count = ffmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT)

        if (count != null) {
            val metadata = hashMapOf<String, String>()
            val chapterCount = count.toInt()

            for (chapterIndex in 0..<chapterCount) {
                MetadataConstants.METADATA_KEYS.forEach { metadataKey ->
                    val value = ffmpegMediaMetadataRetriever.extractMetadataFromChapter(metadataKey, chapterIndex)

                    if (value != null) {
                        metadata.put(metadataKey, value)
                    }
                }

                metadataList.add(metadata)
            }
        }

        return metadataList
    }

    private fun getFrameAtTime(ffmpegMediaMetadataRetriever: FFmpegMediaMetadataRetriever): Bitmap? {
        var bitmap = ffmpegMediaMetadataRetriever.frameAtTime

        if (bitmap != null) {
            val bitmap2 = ffmpegMediaMetadataRetriever.getFrameAtTime(4000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap2 != null) {
                bitmap = bitmap2
            }
        }

        return bitmap
    }

    private fun getBitmapFromEmbeddedPicture(retriever: FFmpegMediaMetadataRetriever): Bitmap? {
        val bytes = retriever.embeddedPicture

        return if (bytes != null && bytes.isNotEmpty()) {
            try {
                BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.size
                )
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }
}