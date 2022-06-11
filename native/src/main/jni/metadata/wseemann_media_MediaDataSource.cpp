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

//#define LOG_NDEBUG 0
#define LOG_TAG "JMediaDataSource-JNI"

#include <wseemann_media_MediaDataSource.h>

#include <assert.h>
#include <string.h>

#include <android/log.h>

using namespace std;

void JMediaDataSource::jniThrowException(JNIEnv* env, const char* className,
    const char* msg) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, msg);
}

void JMediaDataSource::CHECK(bool expr) {
	if (!expr) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
	}
}

JMediaDataSource::JMediaDataSource(JNIEnv* env, jobject source)
	: mJavaObjStatus(OK), mSizeIsCached(false), mCachedSize(0) {
	this->env = env;

    mMediaDataSourceObj = env->NewGlobalRef(source);
    CHECK(mMediaDataSourceObj != NULL);

    jclass mediaDataSourceClass = (jclass)env->GetObjectClass(mMediaDataSourceObj);
    CHECK(mediaDataSourceClass != NULL);

    mReadMethod = env->GetMethodID(mediaDataSourceClass, "readAt", "(J[BII)I");
    CHECK(mReadMethod != NULL);
    mGetSizeMethod = env->GetMethodID(mediaDataSourceClass, "getSize", "()J");
    CHECK(mGetSizeMethod != NULL);
    mCloseMethod = env->GetMethodID(mediaDataSourceClass, "close", "()V");
    CHECK(mCloseMethod != NULL);

    jbyteArray tmp = (jbyteArray)env->NewByteArray(kBufferSize);
    mByteArrayObj = (jbyteArray)env->NewGlobalRef(tmp);
    CHECK(mByteArrayObj != NULL);
}

JMediaDataSource::~JMediaDataSource()
{
	env->DeleteGlobalRef(mMediaDataSourceObj);
    env->DeleteGlobalRef(mByteArrayObj);
}

int JMediaDataSource::readAt(long position, void *buffer, int offset, int size)
{
	Mutex::Autolock _l(mLock);

	if (mJavaObjStatus != OK) {
		__android_log_write(ANDROID_LOG_ERROR, LOG_TAG, "mJavaObjStatus is not OK.");
		return -1;
	}

	if (size > kBufferSize) {
		size = kBufferSize;
	}

	jint numread = env->CallIntMethod(mMediaDataSourceObj, mReadMethod,
			(jlong)offset, mByteArrayObj, (jint)0, (jint)size);
	if (env->ExceptionCheck()) {
		__android_log_write(ANDROID_LOG_WARN, LOG_TAG, "An exception occurred in readAt()");
		//LOGW_EX(env);
		env->ExceptionClear();
		mJavaObjStatus = UNKNOWN_ERROR;
		return -1;
	}
	if (numread < 0) {
		if (numread != -1) {
			__android_log_write(ANDROID_LOG_WARN, LOG_TAG, "An error occurred in readAt()");
			mJavaObjStatus = UNKNOWN_ERROR;
			return -1;
		} else {
			// numread == -1 indicates EOF
			return -1; // NOTE: Was 0, switched to meeting FFmpeg criteria
		}
	}
	if ((size_t)numread > size) {
		__android_log_write(ANDROID_LOG_ERROR, LOG_TAG, "readAt read too many bytes.");
		mJavaObjStatus = UNKNOWN_ERROR;
		return -1;
	}

	__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "readAt %lld / %zu => %d.", (long long)offset, size, numread);
    jbyte* data = env->GetByteArrayElements(mByteArrayObj, NULL);
    if (data != NULL) {
    	memcpy(buffer, data, numread);
    	env->ReleaseByteArrayElements(mByteArrayObj, data, JNI_ABORT);
    }
    // TODO return AVERRORs here to match documentation
	return numread;
}

status_t JMediaDataSource::getSize(off64_t* size) {
	Mutex::Autolock _l(mLock);

	if (mJavaObjStatus != OK) {
		return UNKNOWN_ERROR;
	}

	if (mSizeIsCached) {
		*size = mCachedSize;
		return OK;
	}

	*size = env->CallLongMethod(mMediaDataSourceObj, mGetSizeMethod);
	if (env->ExceptionCheck()) {
		__android_log_write(ANDROID_LOG_WARN, LOG_TAG, "An exception occurred in getSize()");
		//LOGW_EX(env);
		env->ExceptionClear();
		// After returning an error, size shouldn't be used by callers.
		*size = UNKNOWN_ERROR;
		mJavaObjStatus = UNKNOWN_ERROR;
		return UNKNOWN_ERROR;
	}

	// The minimum size should be -1, which indicates unknown size.
	if (*size < 0) {
		*size = -1;
	}

	mCachedSize = *size;
	mSizeIsCached = true;
	return OK;
}

void JMediaDataSource::close() {
	Mutex::Autolock _l(mLock);
	env->CallVoidMethod(mMediaDataSourceObj, mCloseMethod);
    // The closed state is effectively the same as an error state.
	mJavaObjStatus = UNKNOWN_ERROR;
}

uint32_t JMediaDataSource::getFlags() {
    return 0;
}
