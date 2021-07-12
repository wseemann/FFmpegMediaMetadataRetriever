/*
 * FFmpegMediaMetadataRetriever: A unified interface for retrieving frame 
 * and meta data from an input media file.
 *
 * Copyright 2016 William Seemann
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
	#include "ffmpeg_mediametadataretriever.h"
}

MediaMetadataRetriever::MediaMetadataRetriever()
{
	state = NULL;
}

MediaMetadataRetriever::~MediaMetadataRetriever()
{
	Mutex::Autolock _l(mLock);
	::release(&state);
}

int MediaMetadataRetriever::setDataSource(const char *srcUrl, const char *headers)
{
	Mutex::Autolock _l(mLock);
	return ::set_data_source_uri(&state, srcUrl, headers);
}

int MediaMetadataRetriever::setDataSource(int fd, int64_t offset, int64_t length)
{
	Mutex::Autolock _l(mLock);
    return ::set_data_source_fd(&state, fd, offset, length);
}

int MediaMetadataRetriever::getFrameAtTime(int64_t timeUs, int option, AVPacket *pkt)
{
	Mutex::Autolock _l(mLock);
    return ::get_frame_at_time(&state, timeUs, option, pkt);
}

int MediaMetadataRetriever::getScaledFrameAtTime(int64_t timeUs, int option, AVPacket *pkt, int width, int height)
{
	Mutex::Autolock _l(mLock);
    return ::get_scaled_frame_at_time(&state, timeUs, option, pkt, width, height);
}

const char* MediaMetadataRetriever::extractMetadata(const char *key)
{
	Mutex::Autolock _l(mLock);
    return ::extract_metadata(&state, key);
}

const char* MediaMetadataRetriever::extractMetadataFromChapter(const char *key, int chapter)
{
	Mutex::Autolock _l(mLock);
    return ::extract_metadata_from_chapter(&state, key, chapter);
}

int MediaMetadataRetriever::extractAlbumArt(AVPacket *pkt)
{
	Mutex::Autolock _l(mLock);
    return ::get_embedded_picture(&state, pkt);
}

int MediaMetadataRetriever::getMetadata(bool update_only, bool apply_filter, AVDictionary **metadata)
{
	Mutex::Autolock _l(mLock);
    return get_metadata(&state, metadata);
}

int MediaMetadataRetriever::setNativeWindow(ANativeWindow* native_window)
{
    Mutex::Autolock _l(mLock);
	return ::set_native_window(&state, native_window);
}
