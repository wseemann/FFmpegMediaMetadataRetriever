/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2015 William Seemann
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

package wseemann.media.demo;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class Constants {

	public static final String [] METADATA_KEYS = {
		FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM,
		FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST,
		FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST,
		FFmpegMediaMetadataRetriever.METADATA_KEY_COMMENT,
		FFmpegMediaMetadataRetriever.METADATA_KEY_COMPOSER,
		FFmpegMediaMetadataRetriever.METADATA_KEY_COPYRIGHT,
		FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME,
		FFmpegMediaMetadataRetriever.METADATA_KEY_DATE,
		FFmpegMediaMetadataRetriever.METADATA_KEY_DISC,
		FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODER,
		FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODED_BY,
		FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME,
		FFmpegMediaMetadataRetriever.METADATA_KEY_GENRE,
		FFmpegMediaMetadataRetriever.METADATA_KEY_LANGUAGE,
		FFmpegMediaMetadataRetriever.METADATA_KEY_PERFORMER,
		FFmpegMediaMetadataRetriever.METADATA_KEY_PUBLISHER,
		FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_NAME,
		FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_PROVIDER,
		FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE,
		FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK,
		FFmpegMediaMetadataRetriever.METADATA_KEY_VARIANT_BITRATE,
		FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION,
		FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC,
		FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC,
		FFmpegMediaMetadataRetriever.METADATA_KEY_ICY_METADATA,
		FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
		FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE,
        FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME,
        FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_END_TIME,
		FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT,
		FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE
	};
	
}
