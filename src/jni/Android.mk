# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := mangodicteng
LOCAL_SRC_FILES := \
		MangoDictEng.c \
		DictEng.c \
		DictZipLib.c \
		DictIndexUtil.c \
		OffsetIndex.c \
		IdxsynFile.c \
		InfoFile.c \
		plugs/DictPlugs.c \
		plugs/Powerword.c \
		utils/MapFile.c \
		utils/DictUtils.c \
		utils/Distance.c \
		utils/QSort.c \
		utils/Pattern.c \

LOCAL_LDLIBS := -lz -llog

include $(BUILD_SHARED_LIBRARY)



include $(CLEAR_VARS)

LOCAL_MODULE    := memorizeeng
LOCAL_SRC_FILES := \
		memorize/DictMemorizeEng.c \
		memorize/DictMemorize.c \
		memorize/DictCards.c \
		InfoFile.c \
		DictIndexUtil.c \
		utils/MapFile.c \
		utils/DictUtils.c \
		utils/QSort.c \

LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
