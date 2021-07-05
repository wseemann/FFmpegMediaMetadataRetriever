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

#ifndef MEDIAMETADATARETRIEVER_H
#define MEDIAMETADATARETRIEVER_H

#include <android/native_window_jni.h>
#include <Mutex.h>

extern "C" {
    #include "ffmpeg_mediametadataretriever.h"
}

class MediaMetadataRetriever
{
	State* state;
public:
    MediaMetadataRetriever();
    ~MediaMetadataRetriever();
    void disconnect();
    int setDataSource(const char* dataSourceUrl, const char* headers);
    int setDataSource(int fd, int64_t offset, int64_t length);
    int getFrameAtTime(int64_t timeUs, int option, AVPacket *pkt);
    int getScaledFrameAtTime(int64_t timeUs, int option, AVPacket *pkt, int width, int height);
    int extractAlbumArt(AVPacket *pkt);
    const char* extractMetadata(const char* key);
    const char* extractMetadataFromChapter(const char* key, int chapter);
    int getMetadata(bool update_only, bool apply_filter, AVDictionary **metadata);
    int setNativeWindow(ANativeWindow* native_window);

private:
    Mutex                                     mLock;
};

#endif // MEDIAMETADATARETRIEVER_H
