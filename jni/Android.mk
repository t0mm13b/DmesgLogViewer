LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := dmesgLib
LOCAL_SRC_FILES := dmesg.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
 
include $(BUILD_SHARED_LIBRARY)