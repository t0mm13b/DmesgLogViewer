/*    Copyright 2013 Tom Brennan.
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
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include <errno.h>
#include <sys/klog.h>
#include <android/log.h>

// https://android.googlesource.com/platform/bionic/+/master/libc/include/sys/klog.h
#define KLOG_CLOSE         0
#define KLOG_OPEN          1
#define KLOG_READ          2
#define KLOG_READ_ALL      3
#define KLOG_READ_CLEAR    4
#define KLOG_CLEAR         5
#define KLOG_CONSOLE_OFF   6
#define KLOG_CONSOLE_ON    7
#define KLOG_CONSOLE_LEVEL 8
#define KLOG_SIZE_UNREAD   9
#define KLOG_SIZE_BUFFER   10

#define  LOG_TAG    "Java_t0mm13b_dmesglog_jni_DmesgWrapper_dmesg"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#include "t0mm13b_dmesglog_jni_DmesgWrapper.h"

#define FALLBACK_KLOG_BUF_SHIFT 17      /* CONFIG_LOG_BUF_SHIFT from our kernel */
#define FALLBACK_KLOG_BUF_LEN   (1 << FALLBACK_KLOG_BUF_SHIFT)
/*
#define KLOG_CLOSE         0
#define KLOG_OPEN          1
#define KLOG_READ          2
#define KLOG_READ_ALL      3
#define KLOG_READ_CLEAR    4
#define KLOG_CLEAR         5
#define KLOG_CONSOLE_OFF   6
#define KLOG_CONSOLE_ON    7
#define KLOG_CONSOLE_LEVEL 8
#define KLOG_SIZE_UNREAD   9
#define KLOG_SIZE_BUFFER   10
 */

char *dmesg_buf_ptr = NULL;

static int dmesg(int clearFlag);

static int dmesg(int clearFlag){
	int rv_dmesg, klog_buf_len, op;
	rv_dmesg = klog_buf_len = op = -1;

	klog_buf_len = klogctl(KLOG_SIZE_BUFFER, NULL, 0); /* read ring buffer size */
	LOGI("klog_buf_len = %d after KLOG_SIZE_BUFFER", klog_buf_len);
	if (klog_buf_len <= 0)
		klog_buf_len = FALLBACK_KLOG_BUF_LEN;
	op = (clearFlag) ? KLOG_READ_CLEAR : KLOG_READ_ALL;
 	if (!(dmesg_buf_ptr = (char *)malloc(klog_buf_len + 1))) return -1;
 	else{
 		rv_dmesg = klogctl(op, dmesg_buf_ptr, klog_buf_len); /* read ring buffer & clear it */
		//LOGI("len = %d", len);
		if (rv_dmesg > 0)
			dmesg_buf_ptr[rv_dmesg] = '\0';
		else{
			LOGI("klogctl error: %s", strerror(errno));
		}
	}
	return rv_dmesg;
}

jstring JNICALL Java_t0mm13b_dmesglog_jni_DmesgWrapper_dmesg(JNIEnv* env, jclass classz, jboolean clearFlagz){
	jstring mesg = NULL;
	int rv = dmesg(clearFlagz);
	//LOGI("rv = %d", rv);
	if (rv > 0){
		if (dmesg_buf_ptr){
			mesg = (*env)->NewStringUTF(env, dmesg_buf_ptr);
			free(dmesg_buf_ptr);
		}
	}else{
		// whoops! check!
		if (dmesg_buf_ptr){
			free(dmesg_buf_ptr);
		}
	}
	dmesg_buf_ptr = NULL;
	return mesg;
}
