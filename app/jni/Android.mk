LOCAL_PATH := $(call my-dir)

# Prebuilt libssl
#include $(CLEAR_VARS)
#LOCAL_MODULE := ssl
#LOCAL_SRC_FILES := precompiled/libPrivateSsl.so
#include $(PREBUILT_SHARED_LIBRARY)

# Prebuilt libcrypto
include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto
LOCAL_SRC_FILES := libcrypto.so
#include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS)

#LOCAL_MODULE := myLibrary
#TARGET_PLATFORM := android-3
#LOCAL_SRC_FILES := native-lib.cpp
#LOCAL_C_INCLUDES = $(LOCAL_PATH)/include
#LOCAL_LDLIBS := -llog
#LOCAL_SHARED_LIBRARIES := ssl crypto


include $(BUILD_SHARED_LIBRARY)