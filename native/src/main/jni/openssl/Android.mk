LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS) 
LOCAL_MODULE := libssl
LOCAL_SRC_FILES := openssl/$(TARGET_ARCH_ABI)/lib/libssl.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto
LOCAL_SRC_FILES := openssl/$(TARGET_ARCH_ABI)/lib/libcrypto.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

LOCAL_PATH:= $(call my-dir)