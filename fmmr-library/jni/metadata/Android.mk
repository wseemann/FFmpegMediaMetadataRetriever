LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg_mediametadataretriever_jni
LOCAL_CFLAGS := 
LOCAL_SRC_FILES := wseemann_media_MediaMetadataRetriever.cpp \
	mediametadataretriever.cpp \
        ffmpeg_mediametadataretriever.c
LOCAL_SHARED_LIBRARIES := libswscale libavcodec libavformat libavutil
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ffmpeg/$(TARGET_ARCH_ABI)/include
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
