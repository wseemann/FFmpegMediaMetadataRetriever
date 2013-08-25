/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2013 William Seemann
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

//#define LOG_NDEBUG 0
#define LOG_TAG "MediaMetadataRetriever"

#include <mediametadataretriever.h>

extern "C" {
	#include "libavcodec/avcodec.h"
	#include "libavformat/avformat.h"
	#include "ffmpeg_mediametadataretriever.h"
}

using namespace std;

MediaMetadataRetriever::MediaMetadataRetriever()
{
	state = NULL;
}

MediaMetadataRetriever::~MediaMetadataRetriever()
{
	::release(&state);
}

int MediaMetadataRetriever::setDataSource(const char *srcUrl)
{
	return ::set_data_source(&state, srcUrl);
}

AVPacket* MediaMetadataRetriever::getFrameAtTime(int64_t timeUs, int option)
{
    return ::get_frame_at_time(&state, timeUs, option);
}

const char* MediaMetadataRetriever::extractMetadata(const char *key)
{
    return ::extract_metadata(&state, key);
}

AVPacket* MediaMetadataRetriever::extractAlbumArt()
{
    return ::get_embedded_picture(&state);
}
