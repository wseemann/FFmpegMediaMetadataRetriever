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

#ifndef FFMPEG_MEDIAMETADATARETRIEVER_H_
#define FFMPEG_MEDIAMETADATARETRIEVER_H_

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/dict.h>

#include <android/native_window_jni.h>

// Keep these in synch with the constants defined in FFmpegMediaMetadataRetriever.java
// class.
typedef enum {
	OPTION_PREVIOUS_SYNC = 0,
	OPTION_NEXT_SYNC = 1,
	OPTION_CLOSEST_SYNC = 2,
	OPTION_CLOSEST = 3,

    // Add more here...
} Options;

typedef struct State {
	AVFormatContext *pFormatCtx;
	int             audio_stream;
	int             video_stream;
	AVStream        *audio_st;
	AVStream        *video_st;
	int             fd;
	int64_t         offset;
	const char      *headers;
	struct SwsContext *sws_ctx;
	AVCodecContext  *codecCtx;

	struct SwsContext *scaled_sws_ctx;
	AVCodecContext  *scaled_codecCtx;
	ANativeWindow   *native_window;
} State;

struct AVDictionary {
	int count;
	AVDictionaryEntry *elems;
};

int set_data_source_uri(State **ps, const char* path, const char* headers);
int set_data_source_fd(State **ps, int fd, int64_t offset, int64_t length);
const char* extract_metadata(State **ps, const char* key);
const char* extract_metadata_from_chapter(State **ps, const char* key, int chapter);
int get_metadata(State **ps, AVDictionary **metadata);
int get_embedded_picture(State **ps, AVPacket *pkt);
int get_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt);
int get_scaled_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt, int width, int height);
int set_native_window(State **ps, ANativeWindow* native_window);
void release(State **ps);

#endif /*FFMPEG_MEDIAMETADATARETRIEVER_H_*/
