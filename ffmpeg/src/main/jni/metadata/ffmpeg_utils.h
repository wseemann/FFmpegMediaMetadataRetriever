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

#ifndef FFMPEG_UTILS_H_
#define FFMPEG_UTILS_H_

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>

static const char *DURATION = "duration";
static const char *AUDIO_CODEC = "audio_codec";
static const char *VIDEO_CODEC = "video_codec";
static const char *ICY_METADATA = "icy_metadata";
static const char *ROTATE = "rotate";
static const char *FRAMERATE = "framerate";
static const char *CHAPTER_START_TIME = "chapter_start_time";
static const char *CHAPTER_END_TIME = "chapter_end_time";
static const char *CHAPTER_COUNT = "chapter_count";
static const char *FILESIZE = "filesize";
static const char *VIDEO_WIDTH = "video_width";
static const char *VIDEO_HEIGHT = "video_height";

static const int SUCCESS = 0;
static const int FAILURE = -1;

void set_shoutcast_metadata(AVFormatContext *ic);
void set_duration(AVFormatContext *ic);
void set_codec(AVFormatContext *ic, int i);
void set_rotation(AVFormatContext *ic, AVStream *audio_st, AVStream *video_st);
void set_framerate(AVFormatContext *ic, AVStream *audio_st, AVStream *video_st);
void set_filesize(AVFormatContext *ic);
void set_chapter_count(AVFormatContext *ic);
void set_video_dimensions(AVFormatContext *ic, AVStream *video_st);
const char* extract_metadata_internal(AVFormatContext *ic, AVStream *audio_st, AVStream *video_st, const char* key);
int get_metadata_internal(AVFormatContext *ic, AVDictionary **metadata);
const char* extract_metadata_from_chapter_internal(AVFormatContext *ic, AVStream *audio_st, AVStream *video_st, const char* key, int chapter);    

#endif /*FFMPEG_UTILS_H_*/
