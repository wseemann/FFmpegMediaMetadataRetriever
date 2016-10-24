include $(call all-subdir-makefiles)

ifeq ($(TARGET_ARCH),arm)
    APP_PLATFORM=12
endif

ifeq ($(TARGET_ARCH),x86)
    APP_PLATFORM=12
endif

ifeq ($(TARGET_ARCH),mips)
    APP_PLATFORM=12
endif

ifeq ($(TARGET_ARCH),x86_64)
    APP_PLATFORM=21
endif

ifeq ($(TARGET_ARCH),arm64-v8a)
    APP_PLATFORM=21
endif

$(info $(TARGET_ARCH))
$(info $(APP_PLATFORM))