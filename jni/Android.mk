LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := process-buffer
LOCAL_SRC_FILES := fft.c process-buffer.c

include $(BUILD_SHARED_LIBRARY)
