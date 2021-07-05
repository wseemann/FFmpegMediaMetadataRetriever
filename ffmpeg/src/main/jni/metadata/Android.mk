LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg_mediametadataretriever_jni
LOCAL_CFLAGS := 
LOCAL_SRC_FILES := wseemann_media_MediaMetadataRetriever.cpp \
	mediametadataretriever.cpp \
        ffmpeg_mediametadataretriever.c \
        ffmpeg_utils.c
LOCAL_SHARED_LIBRARIES := libswscale libavcodec libavformat libavutil
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ffmpeg/$(TARGET_ARCH_ABI)/include
LOCAL_LDLIBS := -llog
LOCAL_LDLIBS += -landroid
LOCAL_LDLIBS += -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
