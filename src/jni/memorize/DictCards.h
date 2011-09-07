#ifndef __DICTCARDS_H__
#define __DICTCARDS_H__

#include "DictIndexUtil.h"
#include <time.h>


#define SECONDS_OF_DAY				86400

#define REV_QUEUE_GARDE_0_MAX		5
#define REV_QUEUE_NEWCARD_MAX		20
#define REV_QUEUE_MAX				120

#define BUILD_QUEUE_SCHEDULED		0
#define BUILD_QUEUE_NEWCARD			1
#define BUILD_QUEUE_LEARNAHEAD		2

#define GRADE_MAX					6


typedef struct _CardData {
        byte unseen;

		byte grade;
		uint16 easiness;				// Need to be adjust, can't exceed 65536. This should be float type (easiness / 100).

        byte acq_reps;					// Need to be adjust, can't exceed 256.
        byte ret_reps;					// Need to be adjust, can't exceed 256.
        byte lapses;					// Need to be adjust, can't exceed 256.
        byte acq_reps_since_lapse;		// Need to be adjust, can't exceed 256.
        byte ret_reps_since_lapse;		// Need to be adjust, can't exceed 256.

        uint16 last_rep;				// Need to be adjust, can't exceed 65536.
        uint16 next_rep;				// Need to be adjust, can't exceed 65536.
} CardData;


typedef struct _RevQueueData {
	CardData *		pCardData;
	uint32			index;		// The index of this card.
	byte			grade;
} RevQueueData;


typedef struct _DictCards {
	char *			pCardPath;
	char *			pInfoPath;
	CardData *		pCardDatas;
	int				cardCnt;

	RevQueueData *	pRevQueue;
	int				revQueueCnt;

	int				curRevQueueIndex;		// The index in the pRevQueue.
	int				curQueueType;
	int				curAheadIndex;

	int				revQueueGarde0Max;
	int				revQueueNewcardMax;
	int				revQueueMax;
	boolean			randomNewCards;

	long			daysSinceStart;
	time_t			timeOfStart;		// This should be get from '.mfo'file. This value should be save when the user begin memorizing at the first time.

} DictCards;


#ifdef __cplusplus
extern "C" {
#endif


void DictCards_GetCardsProgressFromMfo(DictCards * pMe, char * pMfoFilePath, int cardInfo[2]);
void DictCards_GetDateOfStart(DictCards * pMe, int date[3]);
int DictCards_GetQuestionCount(DictCards * pMe);
int DictCards_GetSeenCardsCount(DictCards * pMe);
boolean DictCards_GetCurrentCardData(DictCards * pMe, int cardData[6]);
void DictCards_GetGradeData(DictCards * pMe, int grade[6]);
void DictCards_GetScheduleData(DictCards * pMe, int ** ppData, int * length, int nYear, int nMonth);

CardData * DictCards_GetCardByIndex(DictCards * pMe, int index);
CardData * DictCards_GetCurCard(DictCards * pMe);
int DictCards_GetCurCardIndex(DictCards * pMe);

boolean DictCards_NewQuestion(DictCards * pMe);
void  DictCards_GradeAnswer(DictCards * pMe, int grade);
boolean DictCards_RebuildRevisionQueue(DictCards * pMe, int queueType);

boolean DictCards_GenerateCardsFile(DictCards * pMe, char * cardPath, int cardCnt);
boolean DictCards_LoadCardsFile(DictCards * pMe, char * path);
void DictCards_SaveInfoFile(DictCards * pMe);
void DictCards_SaveCardsFile(DictCards * pMe);

boolean DictCards_New(DictCards ** ppMe);
void DictCards_Release(DictCards * pMe);


#ifdef __cplusplus
}
#endif

#endif//!__DICTCARDS_H__
