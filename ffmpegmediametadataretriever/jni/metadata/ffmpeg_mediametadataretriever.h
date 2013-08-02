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

#ifndef FFMPEG_MEDIAMETADATARETRIEVER_H_
#define FFMPEG_MEDIAMETADATARETRIEVER_H_

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>

typedef struct State {
	AVFormatContext *pFormatCtx;
	int             audio_stream;
	int             video_stream;
	AVStream        *audio_st;
	AVStream        *video_st;
} State;

int set_data_source(State **ps, const char* path);
const char* extract_metadata(State **ps, const char* key);
AVPacket* get_embedded_picture(State **ps);
AVPacket* get_frame_at_time(State **ps, long timeUs);
void release(State **ps);

#endif /*FFMPEG_MEDIAMETADATARETRIEVER_H_*/
