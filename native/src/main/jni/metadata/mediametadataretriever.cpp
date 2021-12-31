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

#include <android/log.h>

int mediaDataSourceCallback(void *opaque, uint8_t *buf, int buf_size)
{
	State *state = (State *) opaque;
	JMediaDataSource *callbackDataSource = (JMediaDataSource *) state->callback_data_source;

	//__android_log_print(ANDROID_LOG_VERBOSE, "fmmr" ,"mediaDataSourceCallback offset: %" PRId64, state->position);

	int ret = callbackDataSource->readAt(state->position, buf, 0, buf_size);

	//__android_log_print(ANDROID_LOG_VERBOSE, "fmmr", "mediaDataSourceCallback after read! %d", ret);

	return ret;
}

// https://stackoverflow.com/questions/69081037/ffmpeg-error-when-finding-stream-information-with-custom-aviocontext
int64_t mediaDataSourceSeekCallback(void *opaque, int64_t offset, int whence)
{
	//__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "mediaDataSourceCallback before seek, whence: %d", whence);
	//__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG ,"offset: %" PRId64, offset);

	State *state = (State *) opaque;
	JMediaDataSource *callbackDataSource = (JMediaDataSource *) state->callback_data_source;

	switch(whence)
	{
	case AVSEEK_SIZE:
		__android_log_write(ANDROID_LOG_VERBOSE, LOG_TAG, "AVSEEK_SIZE");
		off64_t size;
		callbackDataSource->getSize(&size);
		return -1;
		break;
	case SEEK_SET:
		__android_log_write(ANDROID_LOG_VERBOSE, LOG_TAG, "SEEK_SET");
		break;
	case SEEK_CUR:
		__android_log_write(ANDROID_LOG_VERBOSE, LOG_TAG, "SEEK_CUR");
		break;
	case SEEK_END:
		__android_log_write(ANDROID_LOG_VERBOSE, LOG_TAG, "SEEK_END");
		break;
	default:
		__android_log_write(ANDROID_LOG_VERBOSE, LOG_TAG, "default");
		break;
	}

	return -1;
}

MediaMetadataRetriever::MediaMetadataRetriever()
{
	state = NULL;
}

MediaMetadataRetriever::~MediaMetadataRetriever()
{
	Mutex::Autolock _l(mLock);
	freeMediaDataSource();
	::release(&state);
}

int MediaMetadataRetriever::setDataSource(const char *srcUrl, const char *headers)
{
	Mutex::Autolock _l(mLock);
	freeMediaDataSource();
	return ::set_data_source_uri(&state, srcUrl, headers);
}

int MediaMetadataRetriever::setDataSource(int fd, int64_t offset, int64_t length)
{
	Mutex::Autolock _l(mLock);
	freeMediaDataSource();
    return ::set_data_source_fd(&state, fd, offset, length);
}

int MediaMetadataRetriever::setDataSource(JMediaDataSource* callbackDataSource)
{
	Mutex::Autolock _l(mLock);
	freeMediaDataSource();
    return ::set_data_source_callback(&state, callbackDataSource, mediaDataSourceCallback, mediaDataSourceSeekCallback);
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

void MediaMetadataRetriever::freeMediaDataSource() {
	if (state && state->callback_data_source) {
		JMediaDataSource *callbackDataSource = (JMediaDataSource *) state->callback_data_source;
		delete callbackDataSource;
		state->callback_data_source = NULL;
	}
}
