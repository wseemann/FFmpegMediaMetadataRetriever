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

#ifndef _WSEEMANN_MEDIA_MEDIADATASOURCE_H_
#define _WSEEMANN_MEDIA_MEDIADATASOURCE_H_

#include <Mutex.h>

#include "jni.h"

class JMediaDataSource
{
public:
    enum {
        kBufferSize = 64 * 1024,
    };

	JMediaDataSource(JNIEnv* env, jobject source);
    ~JMediaDataSource();
    int readAt(long position, void *buffer, int offset, int size);
    status_t getSize(off64_t* size);
    void close();
    uint32_t getFlags();

private:
    JMediaDataSource();
    void jniThrowException(JNIEnv* env, const char* className, const char* msg);
    void CHECK(bool expr);
    JNIEnv* env;
    // Protect all member variables with mLock because this object will be
    // accessed on different binder worker threads.
    Mutex mLock;

    // The status of the java DataSource. Set to OK unless an error occurred or
    // close() was called.
    status_t mJavaObjStatus;
    // Only call the java getSize() once so the app can't change the size on us.
    bool mSizeIsCached;
    off64_t mCachedSize;

    jobject mMediaDataSourceObj;
    jmethodID mReadMethod;
    jmethodID mGetSizeMethod;
    jmethodID mCloseMethod;
    jbyteArray mByteArrayObj;
};

#endif // _WSEEMANN_MEDIA_MEDIADATASOURCE_H_
