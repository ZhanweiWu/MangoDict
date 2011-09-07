#include <string.h>
#include <jni.h>
#include <errno.h>
#include<sys/stat.h>
#include "DictMemorize.h"
#include "utils/DictUtils.h"
#include "memorize/DictMemorize.h"

#define  TAG		"[DictMemorizeEng.c]"

static DictMemorize * 	g_pDictMemorize = NULL;
static DictCards * 		g_pDictCards = NULL;

void
Java_com_mango_MangoDict_MemorizeEng_GenerateMemorizeFile(JNIEnv* env, jobject thiz, jstring srcUrl, jstring dstUrl)
{
	char * srcPath = NULL;
	char * dstPath = NULL;

	srcPath = (char*)(*env)->GetStringUTFChars(env, srcUrl, NULL);
	dstPath = (char*)(*env)->GetStringUTFChars(env, dstUrl, NULL);
	DictMemorize_GenerateMemorizeFile(NULL, srcPath, dstPath);
	(*env)->ReleaseStringUTFChars(env, srcUrl, srcPath);
	(*env)->ReleaseStringUTFChars(env, dstUrl, dstPath);
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetDateOfStart(JNIEnv* env, jobject thiz)
{
	jintArray dataArray = NULL;
	jint date[3] = {0};

	DictCards_GetDateOfStart(g_pDictCards, date);

	dataArray = (*env)->NewIntArray(env, 3);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 3, (const jint*)&date);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetScheduleStatus(JNIEnv* env, jobject thiz)
{
	jintArray dataArray = NULL;
	jint infoData[2] = {0};

	infoData[0] = DictCards_GetQuestionCount(g_pDictCards);	// Remainder count in the revQueue.
	infoData[1] = g_pDictCards->revQueueCnt;				// Total count in the revQueue.

	dataArray = (*env)->NewIntArray(env, 2);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 2, (const jint*)&infoData);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetCardsProgressFromMfo(JNIEnv* env, jobject thiz, jstring mfoFile)
{
	jintArray dataArray = NULL;
	jint cardInfo[2] = {0};
	char * mfoPath = NULL;

	mfoPath = (char*)(*env)->GetStringUTFChars(env, mfoFile, NULL);

	DictCards_GetCardsProgressFromMfo(NULL, mfoPath, cardInfo);

    (*env)->ReleaseStringUTFChars(env, mfoFile, mfoPath);

	dataArray = (*env)->NewIntArray(env, 2);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 2, (const jint*)&cardInfo);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetCardsProgress(JNIEnv* env, jobject thiz)
{
	jintArray dataArray = NULL;
	jint infoData[2] = {0};

	infoData[0] = DictCards_GetSeenCardsCount(g_pDictCards);	// Seen count in the pCardDatas.
	infoData[1] = g_pDictCards->cardCnt;						// Total count in the pCardDatas.

	dataArray = (*env)->NewIntArray(env, 2);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 2, (const jint*)&infoData);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetCurrentCardData(JNIEnv* env, jobject thiz)
{
	jintArray dataArray = NULL;
	jint cardData[6] = {0};
	boolean bSuccess = FALSE;

	bSuccess = DictCards_GetCurrentCardData(g_pDictCards, cardData);

	if(FALSE == bSuccess)
	{
		return NULL;
	}

	dataArray = (*env)->NewIntArray(env, 6);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 6, (const jint*)&cardData);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetGradeData(JNIEnv* env, jobject thiz)
{
	jintArray dataArray = NULL;
	jint gradeData[6] = {0};

	DictCards_GetGradeData(g_pDictCards, gradeData);

	dataArray = (*env)->NewIntArray(env, 6);

	(*env)->SetIntArrayRegion(env, dataArray, 0, 6, (const jint*)&gradeData);

	return dataArray;
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetScheduleData(JNIEnv* env, jobject thiz, jint nYear, jint nMonth)
{
	jintArray scheduleArray = NULL;
	int * pScheduleData = NULL;
	int length = 0;

	DictCards_GetScheduleData(g_pDictCards, &pScheduleData, &length, nYear, nMonth);

	if(0 == length)
	{
		return NULL;
	}

	scheduleArray = (*env)->NewIntArray(env, length);

	(*env)->SetIntArrayRegion(env, scheduleArray, 0, length, (const jint*)pScheduleData);

	free(pScheduleData);

	return scheduleArray;
}

void
Java_com_mango_MangoDict_MemorizeEng_SetSettings(JNIEnv* env, jobject thiz, jintArray settings)
{
	int * pSettings = NULL;

	pSettings = (int*)(*env)->GetIntArrayElements(env, settings, 0);

	g_pDictCards->revQueueGarde0Max = pSettings[0];
	g_pDictCards->revQueueNewcardMax = pSettings[1];
	g_pDictCards->revQueueMax = pSettings[2];
	g_pDictCards->randomNewCards = pSettings[3];

	DictCards_SaveInfoFile(g_pDictCards);
	DictCards_RebuildRevisionQueue(g_pDictCards, g_pDictCards->curQueueType);

    (*env)->ReleaseIntArrayElements(env, settings, pSettings, 0);
}

jintArray
Java_com_mango_MangoDict_MemorizeEng_GetSettings(JNIEnv* env, jobject thiz)
{
	jint localArray[4];
	jintArray settingsArray = (*env)->NewIntArray(env, 4);

	localArray[0] = g_pDictCards->revQueueGarde0Max;
	localArray[1] = g_pDictCards->revQueueNewcardMax;
	localArray[2] = g_pDictCards->revQueueMax;
	localArray[3] = g_pDictCards->randomNewCards;

	(*env)->SetIntArrayRegion(env, settingsArray, 0, 4, (const jint*)localArray);

	return settingsArray;
}

jint
Java_com_mango_MangoDict_MemorizeEng_GetCurQueueType(JNIEnv* env, jobject thiz)
{
	return g_pDictCards->curQueueType;
}

jboolean
Java_com_mango_MangoDict_MemorizeEng_NewQuestion(JNIEnv* env, jobject thiz)
{
	return DictCards_NewQuestion(g_pDictCards);
}

void
Java_com_mango_MangoDict_MemorizeEng_GradeAnswer(JNIEnv* env, jobject thiz, jint grade)
{
	DictCards_GradeAnswer(g_pDictCards, grade);
}

jboolean
Java_com_mango_MangoDict_MemorizeEng_RebuildRevisionQueue(JNIEnv* env, jobject thiz, jint queueType)
{
	return DictCards_RebuildRevisionQueue(g_pDictCards, queueType);
}

jstring
Java_com_mango_MangoDict_MemorizeEng_GetMemorizeWord(JNIEnv* env, jobject thiz)
{
	char * pWord = NULL;
	int index = 0;

	if(NULL == g_pDictMemorize)
		return;

	index = DictCards_GetCurCardIndex(g_pDictCards);

	if(index < 0)
	{
		return NULL;
	}

	DictMemorize_GetKeyNData(g_pDictMemorize, index, (const char**)&pWord);

	return (*env)->NewStringUTF(env, pWord);
}

void
Java_com_mango_MangoDict_MemorizeEng_UnloadMemorizeFile(JNIEnv* env, jobject thiz)
{
	if(NULL != g_pDictMemorize)
	{
	    MyLog_v("%s:UnloadMemorizeFile::g_pDictMemorize=%p", TAG, g_pDictMemorize);

		DictMemorize_Release(g_pDictMemorize);
		g_pDictMemorize = NULL;
		DictCards_Release(g_pDictCards);
		g_pDictCards = NULL;
	}
}

jboolean
Java_com_mango_MangoDict_MemorizeEng_LoadMemorizeFile(JNIEnv* env, jobject thiz, jstring memFile)
{
	char * memPath = NULL;
	char * filePath = NULL;
	int len = 0;
	long wc = 0;
	boolean bSuccess = FALSE;
    struct stat stats;

	Java_com_mango_MangoDict_MemorizeEng_UnloadMemorizeFile(env, thiz);

	memPath = (char*)(*env)->GetStringUTFChars(env, memFile, NULL);

    MyLog_v("%s:LoadMemorizeFile::memPath=%s", TAG, memPath);

    if(stat(memPath, &stats))
    {
       (*env)->ReleaseStringUTFChars(env, memFile, memPath);
       return FALSE;
    }

	// Memorize file ext name is '.mem', info file ext name is '.mfo', cards file ext name is '.mcd'
	filePath = strdup(memPath);
	len = strlen(filePath);
	filePath[len - 2] = 'f';
	filePath[len - 1] = 'o';
	bSuccess = InfoFile_Load(filePath, &wc, NULL, NULL, NULL, NULL);

	if(TRUE == bSuccess)
	{
	    DictMemorize_New(&g_pDictMemorize);
	    bSuccess = DictMemorize_Load(g_pDictMemorize, memPath, wc, stats.st_size);

	    if(TRUE == bSuccess)
	    {
			filePath[len - 2] = 'c';
			filePath[len - 1] = 'd';

			DictCards_New(&g_pDictCards);
			bSuccess = DictCards_LoadCardsFile(g_pDictCards, filePath);
	    }
	}

	free(filePath);

	(*env)->ReleaseStringUTFChars(env, memFile, memPath);

	return bSuccess;
}

//-----------------------------------------------------------------------------------------------------//

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        // ERROR: GetEnv failed.
        return result;
    }

    result = JNI_VERSION_1_4;

    return result;
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
    MyLog_v("%s:JNI_OnUnload", TAG);

	Java_com_mango_MangoDict_MemorizeEng_UnloadMemorizeFile(NULL, NULL);
}
