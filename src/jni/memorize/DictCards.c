#include "DictCards.h"
#include "utils/DictUtils.h"
#include "InfoFile.h"

#define  TAG		"[DictCards.c]"

#define IS_LEAP(y)         ((((y)%4)==0) && (((y)%100) || (((y)%400)==0)))


// days_since_last_rep
int DaysSinceLastRep(DictCards * pMe, CardData * pCardData)
{
    return pMe->daysSinceStart - pCardData->last_rep;
}

// days_until_next_rep
int DaysUntilNextRep(DictCards * pMe, CardData * pCardData)
{
    return pCardData->next_rep - pMe->daysSinceStart;
}

// is_due_for_retention_rep
// Due for a retention repetion within 'days' days?
boolean IsDueForRetentionRep(DictCards * pMe, CardData * pCardData, int days)
{
	return (pCardData->grade >= 2 && pMe->daysSinceStart >= pCardData->next_rep - days);
}

// is_due_for_acquisition_rep
boolean IsDueForAcquisitionRep(DictCards * pMe, CardData * pCardData)
{
	return (pCardData->grade < 2);
}

// qualifies_for_learn_ahead
boolean QualifiesForLearnAhead(DictCards * pMe, CardData * pCardData)
{
	return (pCardData->grade >= 2 && pMe->daysSinceStart < pCardData->next_rep);
}

// is_new
boolean IsNew(DictCards * pMe, CardData * pCardData)
{
	return (pCardData->acq_reps == 0 && pCardData->ret_reps == 0);
}

// sort_key_interval
int SortKeyInterval(const void * a, const void * b)
{
	RevQueueData * a1 = (RevQueueData*)a;
	RevQueueData * b1 = (RevQueueData*)b;
	int c1 = a1->pCardData->next_rep - a1->pCardData->last_rep;
	int c2 = b1->pCardData->next_rep - b1->pCardData->last_rep;

	return c1 - c2;
}

// sort_key
int SortKey(const void * a, const void * b)
{
	RevQueueData * a1 = (RevQueueData*)a;
	RevQueueData * b1 = (RevQueueData*)b;

	return a1->pCardData->next_rep - b1->pCardData->next_rep;
}

int GetMonthLength(int nYear, int nMonth)
{
  if (nMonth == 2)
    return (IS_LEAP(nYear) ? 29 : 28);

  // If we shift the year to start on Mar1, then
  // there is a repeating 5-month pattern of
  // {31,30,31,30,31} {31,30,31,30,31} {31,
  nMonth = (nMonth > 2) ? nMonth - 3 : nMonth + 9;

  return ((nMonth % 5) & 1) ? 30 : 31;
}

void UpdateDaysSince(DictCards * pMe)
{
	time_t now = time(NULL);

	if(0 == pMe->timeOfStart)
	{
		struct tm *lTime = NULL;
		lTime = localtime(&now);
		lTime->tm_hour = 0;
		lTime->tm_min = 0;
		lTime->tm_sec = 1;
		lTime->tm_isdst = 0;

		pMe->timeOfStart = mktime(lTime);
		pMe->daysSinceStart = 0;
	}
	else
	{
		pMe->daysSinceStart = (long)(difftime(now, pMe->timeOfStart) / SECONDS_OF_DAY);
	}
}

int DictCards_GetSeenCardsCount(DictCards * pMe)
{
	int i = 0;
	int cnt = 0;
	CardData * pCardData = NULL;

	for (i = 0; i < pMe->cardCnt; i++)
	{
		pCardData = DictCards_GetCardByIndex(pMe, i);
		if(FALSE == pCardData->unseen)
		{
			cnt++;
		}
	}

	return cnt;
}

void DictCards_GetDateOfStart(DictCards * pMe, int date[3])
{
	struct tm *lTime = NULL;
	lTime = localtime(&pMe->timeOfStart);

	date[0] = lTime->tm_year + 1900;
	date[1] = lTime->tm_mon + 1;
	date[2] = lTime->tm_mday;
}

boolean DictCards_GetCurrentCardData(DictCards * pMe, int cardData[6])
{
	CardData * pCardData = DictCards_GetCurCard(pMe);

	if(NULL == pCardData)
		return FALSE;

	cardData[0] = pCardData->grade;								// Grade
	cardData[1] = pCardData->easiness;							// Easiness
	cardData[2] = pCardData->acq_reps + pCardData->ret_reps;	// Repetitions
	cardData[3] = pCardData->lapses;							// Lapses
	cardData[4] = DaysSinceLastRep(pMe, pCardData);				// Days since last repetition
	cardData[5] = DaysUntilNextRep(pMe, pCardData);				// Days until next repetition

	return TRUE;
}

void DictCards_GetGradeData(DictCards * pMe, int grade[6])
{
	int i = 0;
	CardData * pCardData = NULL;

	memset(grade, 0, sizeof(int) * 6);

	for (i = 0; i < pMe->cardCnt; i++)
	{
		pCardData = DictCards_GetCardByIndex(pMe, i);

		if(FALSE == pCardData->unseen && pCardData->grade >= 0 && pCardData->grade <= 5)
		{
			grade[pCardData->grade] ++;
		}
	}
}

void DictCards_GetScheduleData(DictCards * pMe, int ** ppData, int * length, int nYear, int nMonth)
{
    struct tm lTime;
    time_t curDay;
    int daysOfMonth = 0;
	long daysSinceStart = 0;
	long indexOfToday = -1;
	CardData * pCardData = NULL;
	int * pData = NULL;
	int i = 0;

    daysOfMonth = GetMonthLength(nYear, nMonth);

	lTime.tm_year = nYear - 1900;
    lTime.tm_mon = nMonth - 1;
    lTime.tm_mday = 1;			// The first day of this month.
    lTime.tm_hour = 0;
    lTime.tm_min = 0;
    lTime.tm_sec = 1;
    lTime.tm_isdst = 0;

    curDay = mktime(&lTime);

	daysSinceStart = (long)(difftime(curDay, pMe->timeOfStart) / SECONDS_OF_DAY);

	if(daysSinceStart + daysOfMonth < pMe->daysSinceStart)	// Before the current month.
	{
		*length = 0;
		return;
	}
	else if(daysSinceStart <= pMe->daysSinceStart && daysSinceStart + daysOfMonth >= pMe->daysSinceStart)	// In the current month.
	{
		indexOfToday = pMe->daysSinceStart - daysSinceStart;
	}
	else			// After the current month.
	{
		indexOfToday = -1;
	}

    *length = daysOfMonth;
    *ppData = malloc(sizeof(int) * daysOfMonth);
	pData = *ppData;
	memset(pData, 0, sizeof(int) * daysOfMonth);

	for (i = 0; i < pMe->cardCnt; i++)
	{
		pCardData = DictCards_GetCardByIndex(pMe, i);

		//if(pCardData->grade < 2)
		//{
		//	continue;
		//}

		if(TRUE == pCardData->unseen)
		{
			continue;
		}

		if(indexOfToday > 0 && pMe->daysSinceStart >= pCardData->next_rep)		// This card is the overtime card, add it to 'indexOfToday' of this month, it's today.
		{
			pData[indexOfToday] ++;
		}
		else if(pCardData->next_rep >= daysSinceStart && pCardData->next_rep <= daysSinceStart + daysOfMonth)		// This card is in this month.
		{
			pData[pCardData->next_rep - daysSinceStart] ++;
		}
	}
}

CardData * DictCards_GetCardByIndex(DictCards * pMe, int index)
{
	return &pMe->pCardDatas[index];
}

SortQueue(RevQueueData *pRevQueue, size_t revQueueCnt, int (*compar) (const void *, const void *))
{
	QSort(pRevQueue, revQueueCnt, sizeof(RevQueueData), compar);
}

// RandInt
int RandInt(int n)
{
	if (n <= 1)
		return n - 1;

	return rand() % n;
}

RandomQueue(RevQueueData *pRevQueue, size_t revQueueCnt)
{
	int i = 0, j = 0;
	int randIndex = 0;
	int index = 0;
	int randIndexCnt = 0;
	RevQueueData *pTmpRevQueue = malloc(sizeof(RevQueueData) * revQueueCnt);
	int * pRandomIndex = malloc(sizeof(int) * revQueueCnt);

	for(i = 0; i < (int)revQueueCnt; i++)
	{
		pRandomIndex[i] = i;
	}

	for(i = 0; i < (int)revQueueCnt; i++)
	{
		randIndexCnt = revQueueCnt - i;
		randIndex = RandInt(randIndexCnt);

		index = pRandomIndex[randIndex];

		// Copy RevQueueData from 'pRevQueue' with the index 'randIndex' to 'pTmpRevQueue[i]'
		pTmpRevQueue[i] = pRevQueue[index];

		// Remove the index with the value 'randIndex' from 'pRandomIndex'.
		for(j = randIndex; j < randIndexCnt - 1; j++)
		{
			pRandomIndex[j] = pRandomIndex[j + 1];
		}
	}

	// Copy the randomed data from pTmpRevQueue to pRevQueue.
	memcpy(pRevQueue, pTmpRevQueue, sizeof(RevQueueData) * revQueueCnt);

	free(pTmpRevQueue);
	free(pRandomIndex);
}

// calculate_initial_interval
int CalculateInitialInterval(grade)
{
    // If this is the first time we grade this item, allow for slightly
    // longer scheduled intervals, as we might know this item from before.

    int interval[] = { 0, 0, 1, 3, 4, 5};

    return interval[grade];
}

// calculate_interval_noise
int CalculateIntervalNoise(int interval)
{
	int noise = 0;

    if (interval == 0)
        noise = 0;
    else if (interval == 1)
        noise = RandInt(2);
    else if (interval <= 10)
        noise = RandInt(3) - 1;
    else if (interval <= 60)
        noise = RandInt(7) - 3;
    else
	{
        int a = interval / 20;
        noise = RandInt(2 * a + 1) - a;
	}

    return noise;
}

// process_answer
int ProcessAnswer(DictCards * pMe, int index, int new_grade, boolean dry_run)
{
	CardData cardData;
	CardData * pCardData;

	long scheduled_interval;
	long actual_interval;
	float new_interval = 0.0f;
	int noise;

	pCardData = DictCards_GetCardByIndex(pMe, index);

	// When doing a dry run, make a copy to operate on. Note that this
    // leaves the original in items and the reference in the GUI intact.
	if(TRUE == dry_run)
	{
		memcpy(&cardData, pCardData, sizeof(CardData));
		pCardData = &cardData;
	}

    // Calculate scheduled and actual interval, taking care of corner
    // case when learning ahead on the same day.
    scheduled_interval = pCardData->next_rep - pCardData->last_rep;
	actual_interval    = pMe->daysSinceStart - pCardData->last_rep;

    if (0 == actual_interval)
        actual_interval = 1;		// Otherwise new interval can become zero.

	if(IsNew(pMe, pCardData))
	{
        // The item is not graded yet, e.g. because it is imported.

        pCardData->acq_reps = 1;
        pCardData->acq_reps_since_lapse = 1;

        new_interval = (float)CalculateInitialInterval(new_grade);
	}
    else if (pCardData->grade < 2 && new_grade < 2)
	{
        // In the acquisition phase and staying there.
    
        pCardData->acq_reps += 1;
        pCardData->acq_reps_since_lapse += 1;
        
        new_interval = 0;
	}
    else if (pCardData->grade < 2 && new_grade >=2 && new_grade <= 5)
	{
         // In the acquisition phase and moving to the retention phase.

         pCardData->acq_reps += 1;
         pCardData->acq_reps_since_lapse += 1;

         new_interval = 1;
	}
    else if (pCardData->grade >= 2 && pCardData->grade <= 5 && new_grade < 2)
	{
         // In the retention phase and dropping back to the acquisition phase.

         pCardData->ret_reps += 1;
         pCardData->lapses += 1;
         pCardData->acq_reps_since_lapse = 0;
         pCardData->ret_reps_since_lapse = 0;

         new_interval = 0;

         // Move this item to the front of the list, to have precedence over
         // items which are still being learned for the first time.

		 // TODO
         //if(FALSE == dry_run)
		 //{
             //items.remove(item)
             //items.insert(0,item)
		 //}
	}
    else if (pCardData->grade >= 2 && pCardData->grade <= 5  && new_grade >=2 && new_grade <= 5)
	{
       // In the retention phase and staying there.

        pCardData->ret_reps += 1;
        pCardData->ret_reps_since_lapse += 1;

        if (actual_interval >= scheduled_interval)
		{
            if (new_grade == 2)
                pCardData->easiness -= 16;
            else if (new_grade == 3)
                pCardData->easiness -= 14;
            else if (new_grade == 5)
                pCardData->easiness += 10;

            if (pCardData->easiness < 130)
            {
                pCardData->easiness = 130;
            }
            if (pCardData->easiness > 65000)
            {
            	pCardData->easiness = 65000;
            }
		}

        new_interval = 0;

        if (pCardData->ret_reps_since_lapse == 1)
		{
            new_interval = 6;
		}
        else
		{
            if (new_grade == 2 || new_grade == 3)
                if (actual_interval <= scheduled_interval)
                    new_interval = (float)(actual_interval * pCardData->easiness / 100);
                else
                    new_interval = (float)scheduled_interval;

            if (new_grade == 4)
                new_interval = (float)(actual_interval * pCardData->easiness / 100);

            if (new_grade == 5)
			{
                if (actual_interval < scheduled_interval)
                    new_interval = (float)scheduled_interval;		// Avoid spacing.
                else
                    new_interval = (float)(actual_interval * pCardData->easiness / 100);
			}
		}

        // Shouldn't happen, but build in a safeguard.

        if (new_interval == 0)
            new_interval = (float)scheduled_interval;
	}

	// Need to be adjust, can't exceed 256.
	if(pCardData->acq_reps >= MAX_UINT8)
	{
		pCardData->acq_reps --;
	}
	if(pCardData->ret_reps >= MAX_UINT8)
	{
		pCardData->ret_reps --;
	}
	if(pCardData->lapses >= MAX_UINT8)
	{
		pCardData->lapses --;
	}
	if(pCardData->acq_reps_since_lapse >= MAX_UINT8)
	{
		pCardData->acq_reps_since_lapse --;
	}
	if(pCardData->ret_reps_since_lapse >= MAX_UINT8)
	{
		pCardData->ret_reps_since_lapse --;
	}

    // When doing a dry run, stop here and return the scheduled interval.

    if(TRUE == dry_run)
    {
        return (int)new_interval;
    }

    // Add some randomness to interval.

    noise = CalculateIntervalNoise((int)new_interval);

    // Update grade and interval.

    pCardData->grade    = new_grade;
	pCardData->last_rep = (uint16)pMe->daysSinceStart;
    pCardData->next_rep = (uint16)(pMe->daysSinceStart + new_interval + noise);
    pCardData->unseen   = FALSE;

	// Need to be adjust, can't exceed 65536.
    if(pCardData->last_rep > 65000)
    {
    	pCardData->last_rep = 65000;
    }
    if(pCardData->next_rep > 65000)
    {
    	pCardData->next_rep = 65000;
    }

    return (int)(new_interval + noise);
}

// The remainder count of Question.
int DictCards_GetQuestionCount(DictCards * pMe)
{
	int i = 0;
	int cnt = 0;

	for (i = 0; i < pMe->revQueueCnt; i++)
	{
		if(GRADE_MAX == pMe->pRevQueue[i].grade || pMe->pRevQueue[i].grade <= 1)
		{
			cnt++;
		}
	}

	return cnt;
}

// newQuestion
boolean DictCards_NewQuestion(DictCards * pMe)
{
	int i = 0;

	// Looking the un-learnt card.
	for (i = 0; i < pMe->revQueueCnt; i++)
	{
		if(GRADE_MAX == pMe->pRevQueue[i].grade)
		{
			pMe->curRevQueueIndex = i;
			return TRUE;
		}
	}

	// Looking the learned card which grades are grade0 or grade1.
	for (i = 0; i < pMe->revQueueCnt; i++)
	{
		if(pMe->pRevQueue[i].grade <= 1)
		{
			pMe->curRevQueueIndex = i;
			return TRUE;
		}
	}

	// Continue the LearnAhead card since it still hasn't go to the end.
	if(BUILD_QUEUE_LEARNAHEAD == pMe->curQueueType && pMe->curAheadIndex > 0)
	{
		DictCards_RebuildRevisionQueue(pMe, BUILD_QUEUE_LEARNAHEAD);
		return DictCards_NewQuestion(pMe);
	}

	return FALSE;
}


// gradeAnswer
void  DictCards_GradeAnswer(DictCards * pMe, int grade)
{
	int curCardIndex = 0;
    int interval = 0;

    if(pMe->curRevQueueIndex < 0)
    {
    	return;
    }

	if(GRADE_MAX == pMe->pRevQueue[pMe->curRevQueueIndex].grade)
	{
		curCardIndex = DictCards_GetCurCardIndex(pMe);
		interval = ProcessAnswer(pMe, curCardIndex, grade, FALSE);
	}
	else  	// This card is learnt card which grade <= 1, move this card to the bottom.
	{
		int i = 0;
		RevQueueData tmpRevQueue = pMe->pRevQueue[pMe->curRevQueueIndex];

		for (i = pMe->curRevQueueIndex; i < pMe->revQueueCnt - 1; i++)
		{
			pMe->pRevQueue[i] = pMe->pRevQueue[i + 1];
		}

		pMe->pRevQueue[pMe->revQueueCnt - 1] = tmpRevQueue;
	}

	// Mark grade as new value to show that it has been learnt.
	pMe->pRevQueue[pMe->curRevQueueIndex].grade = grade;
}

// rebuild_revision_queue
// learn_ahead should only be set to TRUE when:
//      no cards are isDueForRetentionRep
//      nor for isDueForAcquisitionRep
// For one day, first learn retention card,then learn new card.
// Set learn_newcard as FALSE to get retention card, TRUE to get new card.
// On the UI, should let the user to select learn retention card or new card.
boolean DictCards_RebuildRevisionQueue(DictCards * pMe, int queueType)
{
	int i = 0;
	int revQueueCnt = 0;
	int size = 0;
	CardData * pCardData = NULL;

	size = sizeof(RevQueueData) * pMe->revQueueMax;
	pMe->revQueueCnt = 0;
	pMe->curQueueType = queueType;
	pMe->curRevQueueIndex = -1;

	if(pMe->cardCnt <= 0)
		return FALSE;

	if(NULL != pMe->pRevQueue)
	{
		free(pMe->pRevQueue);
		pMe->pRevQueue = NULL;
	}

	pMe->pRevQueue = malloc(size);

	if(NULL == pMe->pRevQueue)
		return FALSE;

	memset(pMe->pRevQueue, 0, size); 

	UpdateDaysSince(pMe);

	if(BUILD_QUEUE_NEWCARD == queueType)
	{
		// Now add some new cards. This is a bit inefficient at the moment as
		// 'unseen' is wastefully created as opposed to being a generator
		// expression. However, in order to use random.choice, there doesn't
		// seem to be another option.

		// Add unseen card.
		// TODO: the best way is to select the unseen card randomly.

		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(TRUE == pCardData->unseen)
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				if (revQueueCnt >= pMe->revQueueMax || revQueueCnt >= pMe->revQueueNewcardMax)
				{
					break;
				}
			}
		}

		if(TRUE == pMe->randomNewCards)
		{
			RandomQueue(pMe->pRevQueue, revQueueCnt);
		}
		pMe->revQueueCnt = revQueueCnt;

		return (pMe->revQueueCnt > 0);
	}
	else if(BUILD_QUEUE_SCHEDULED == queueType)
	{
		int limit = 0;
		int grade0Cnt = 0;

		// Do the cards that are scheduled for today (or are overdue), but
		// first do those that have the shortest interval, as being a day
		// late on an interval of 2 could be much worse than being a day late
		// on an interval of 50.

		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(TRUE == IsDueForRetentionRep(pMe, pCardData, 0))
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				if(revQueueCnt >= pMe->revQueueMax)
				{
					break;
				}
			}
		}

		if(revQueueCnt >= pMe->revQueueMax)
		{
			//SortQueue(pMe->pRevQueue, revQueueCnt, SortKeyInterval);
			RandomQueue(pMe->pRevQueue, revQueueCnt);
			pMe->revQueueCnt = revQueueCnt;
			return TRUE;
		}

		// Now rememorise the cards that we got wrong during the last stage.
		// Concentrate on only a limited number of grade 0 cards, in order to
		// avoid too long intervals between revisions.

		limit = pMe->revQueueGarde0Max;

		// Add grade0 card.
		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(0 == pCardData->grade && pCardData->lapses > 0)
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				// Duplicate grade0 card.
				if (revQueueCnt < pMe->revQueueMax)
				{
					pMe->pRevQueue[revQueueCnt].index = i;
					pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
					pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
					revQueueCnt ++;
				}

				grade0Cnt ++;

				if (revQueueCnt >= pMe->revQueueMax || grade0Cnt >= limit)
				{
					break;
				}
			}
		}

		if(revQueueCnt >= pMe->revQueueMax)
		{
			RandomQueue(pMe->pRevQueue, revQueueCnt);
			pMe->revQueueCnt = revQueueCnt;
			return TRUE;
		}

		// Add grade1 card.
		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(1 == pCardData->grade && pCardData->lapses > 0)
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				if(revQueueCnt >= pMe->revQueueMax)
				{
					break;
				}
			}
		}

		if(revQueueCnt >= pMe->revQueueMax || grade0Cnt >= limit)
		{
			RandomQueue(pMe->pRevQueue, revQueueCnt);
			pMe->revQueueCnt = revQueueCnt;
			return TRUE;
		}

		// Now do the cards which have never been committed to long-term memory,
		// but which we have seen before.

		// Add grade0 card.
		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(0 == pCardData->grade && 0 == pCardData->lapses && FALSE == pCardData->unseen)
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				// Duplicate grade0 card.
				if (revQueueCnt < pMe->revQueueMax)
				{
					pMe->pRevQueue[revQueueCnt].index = i;
					pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
					pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
					revQueueCnt ++;
				}

				grade0Cnt ++;

				if (revQueueCnt >= pMe->revQueueMax || grade0Cnt >= limit)
				{
					break;
				}
			}
		}

		if(revQueueCnt >= pMe->revQueueMax)
		{
			RandomQueue(pMe->pRevQueue, revQueueCnt);
			pMe->revQueueCnt = revQueueCnt;
			return TRUE;
		}

		// Add grade1 card.
		for (i = 0; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(1 == pCardData->grade && 0 == pCardData->lapses && FALSE == pCardData->unseen)
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				if(revQueueCnt >= pMe->revQueueMax)
				{
					break;
				}
			}
		}

		RandomQueue(pMe->pRevQueue, revQueueCnt);
		pMe->revQueueCnt = revQueueCnt;
		return (pMe->revQueueCnt > 0);
	}
	else
	{
		// If we get to here, there are no more scheduled cards or new cards to
		// learn. The user can signal that he wants to learn ahead by calling
		// rebuild_revision_queue with 'learn_ahead' set to True. Don't shuffle
		// this queue, as it's more useful to review the earliest scheduled cards
		// first.

		for (i = pMe->curAheadIndex; i < pMe->cardCnt; i++)
		{
			pCardData = DictCards_GetCardByIndex(pMe, i);

			if(TRUE == QualifiesForLearnAhead(pMe, pCardData))
			{
				pMe->pRevQueue[revQueueCnt].index = i;
				pMe->pRevQueue[revQueueCnt].grade = GRADE_MAX;
				pMe->pRevQueue[revQueueCnt].pCardData = pCardData;
				revQueueCnt ++;

				if(revQueueCnt >= pMe->revQueueMax)
				{
					i++;
					break;
				}
			}
		}

		if(i == pMe->cardCnt)
		{
			i = 0;
		}

		pMe->curAheadIndex = i;

		//SortQueue(pMe->pRevQueue, revQueueCnt, SortKey);
		pMe->revQueueCnt = revQueueCnt;

		return (pMe->revQueueCnt > 0);
	}

	return (pMe->revQueueCnt > 0);
}

CardData * DictCards_GetCurCard(DictCards * pMe)
{
    if(pMe->curRevQueueIndex < 0)
    {
    	return NULL;
    }

	return pMe->pRevQueue[pMe->curRevQueueIndex].pCardData;
}

int DictCards_GetCurCardIndex(DictCards * pMe)
{
    if(pMe->curRevQueueIndex < 0)
    {
    	return -1;
    }

	return pMe->pRevQueue[pMe->curRevQueueIndex].index;
}

void DictCards_GetCardsProgressFromMfo(DictCards * pMe, char * pMfoFilePath, int cardInfo[2])
{
	int fsize = 0;
	struct stat stats;
	FILE * pf = NULL;
	long value = 0;
	char * pInfoBuffer = NULL;

	if(stat(pMfoFilePath, &stats))
	   return;
    fsize = stats.st_size;

    pInfoBuffer = malloc(fsize + 1);
	if(NULL == pInfoBuffer)
		return;

    memset((void*)pInfoBuffer, 0, fsize + 1);
    pf = fopen(pMfoFilePath, "rb");
    fread((void*)pInfoBuffer, fsize, 1, pf);
    fclose(pf);

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "wordcount"))
	{
		cardInfo[1] = value;
	}
	else
	{
		cardInfo[1] = 0;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "seencount"))
	{
		cardInfo[0] = value;
	}
	else
	{
		cardInfo[0] = 0;
	}

	free(pInfoBuffer);
}

void DictCards_LoadInfoFile(DictCards * pMe)
{
	int fsize = 0;
	struct stat stats;
	FILE * pf = NULL;
	long value = 0;
	char * pInfoBuffer = NULL;

	if(stat(pMe->pInfoPath, &stats))
	   return;
    fsize = stats.st_size;

    pInfoBuffer = malloc(fsize + 1);
	if(NULL == pInfoBuffer)
		return;

    memset((void*)pInfoBuffer, 0, fsize + 1);
    pf = fopen(pMe->pInfoPath, "rb");
    fread((void*)pInfoBuffer, fsize, 1, pf);
    fclose(pf);

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "wordcount"))
	{
		pMe->cardCnt = value;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "timeofstart"))
	{
		pMe->timeOfStart = value;
	}
	else
	{
		pMe->timeOfStart = 0;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "curaheadindex"))
	{
		pMe->curAheadIndex = value;
	}
	else
	{
		pMe->curAheadIndex = 0;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "queuegrade0max"))
	{
		pMe->revQueueGarde0Max = value;
	}
	else
	{
		pMe->revQueueGarde0Max = REV_QUEUE_GARDE_0_MAX;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "queuenewcardmax"))
	{
		pMe->revQueueNewcardMax = value;
	}
	else
	{
		pMe->revQueueNewcardMax = REV_QUEUE_NEWCARD_MAX;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "queuemax"))
	{
		pMe->revQueueMax = value;
	}
	else
	{
		pMe->revQueueMax = REV_QUEUE_MAX;
	}

	if(TRUE == InfoFile_GetValueByKey(pInfoBuffer, &value, "randomnewcards"))
	{
		pMe->randomNewCards = (boolean)value;
	}
	else
	{
		pMe->randomNewCards = TRUE;
	}

	free(pInfoBuffer);
}

void DictCards_SaveInfoFile(DictCards * pMe)
{
	FILE * pFileMfo = NULL;
	char strWordCount[32];

	pFileMfo = fopen(pMe->pInfoPath, "wb");

	// CardCount
	sprintf(strWordCount, "wordcount=%d", pMe->cardCnt);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// SeenCardCount
	sprintf(strWordCount, "seencount=%d", DictCards_GetSeenCardsCount(pMe));
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// TimeOfStart
	sprintf(strWordCount, "timeofstart=%d", pMe->timeOfStart);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// CurrentAheadIndex
	sprintf(strWordCount, "curaheadindex=%d", pMe->curAheadIndex);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// RevQueueGarde0Max
	sprintf(strWordCount, "queuegrade0max=%d", pMe->revQueueGarde0Max);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// revQueueNewcardMax
	sprintf(strWordCount, "queuenewcardmax=%d", pMe->revQueueNewcardMax);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// revQueueMax
	sprintf(strWordCount, "queuemax=%d", pMe->revQueueMax);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	// randomNewCards
	sprintf(strWordCount, "randomnewcards=%d", pMe->randomNewCards);
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);

	fclose(pFileMfo);
}

void DictCards_SaveCardsFile(DictCards * pMe)
{
	int size = pMe->cardCnt * sizeof(CardData);
	FILE * pFileMcd = NULL;

	if(NULL == pMe->pCardDatas || size <= 0)
		return;

	DictCards_SaveInfoFile(pMe);

    MyLog_v("%s:DictCards_SaveCardsFile::size=%d", TAG, size);

	pFileMcd = fopen(pMe->pCardPath, "wb");

	if(NULL == pFileMcd)
		return;

    MyLog_v("%s:DictCards_SaveCardsFile::pFileMcd=%p", TAG, pFileMcd);

	fwrite(pMe->pCardDatas, size, 1, pFileMcd);

	fclose(pFileMcd);
}

boolean DictCards_LoadCardsFile(DictCards * pMe, char * cardPath)
{
	int len = 0;
	int size = 0;
	FILE * pFileMcd = fopen(cardPath, "rb");

	if(NULL == pFileMcd)
		return FALSE;

	if(NULL != pMe->pCardPath)
	{
		free(pMe->pCardPath);
		pMe->pCardPath = NULL;
	}

	if(NULL != pMe->pInfoPath)
	{
		free(pMe->pInfoPath);
		pMe->pInfoPath = NULL;
	}

	pMe->pCardPath = strdup(cardPath);
	pMe->pInfoPath = strdup(cardPath);
	len = strlen(cardPath);
	pMe->pInfoPath[len - 2] = 'f';
	pMe->pInfoPath[len - 1] = 'o';

	// The following value should get from *.mfo file.
	DictCards_LoadInfoFile(pMe);

	size = pMe->cardCnt * sizeof(CardData);

	if(NULL != pMe->pCardDatas)
	{
		free(pMe->pCardDatas);
		pMe->pCardDatas = NULL;
	}

	pMe->pCardDatas = malloc(size);
	if(NULL == pMe->pCardDatas)
	{
		fclose(pFileMcd);
		return FALSE;
	}

	fread(pMe->pCardDatas, size, 1, pFileMcd);

	fclose(pFileMcd);

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////////////////////////

boolean DictCards_GenerateCardsFile(DictCards * pMe, char * path, int cardCnt)
{
	CardData cardData = {TRUE, 0, 250, 0, 0, 0, 0, 0, 0, 0};
	int i = 0;

	FILE * pFileMcd = fopen(path, "wb");

	for(i = 0; i < cardCnt; i++)
		fwrite(&cardData, sizeof(CardData), 1, pFileMcd);

	fclose(pFileMcd);

	return TRUE;
}

/////////////////////////////////////////////////////////////////////////////////////////////////

void DictCards_Release(DictCards * pMe)
{
	if(NULL == pMe)
		return;

    MyLog_v("%s:DictCards_Release", TAG);

	// Save cards information.
	DictCards_SaveCardsFile(pMe);

	if(NULL != pMe->pCardPath)
	{
		free(pMe->pCardPath);
		pMe->pCardPath = NULL;
	}

	if(NULL != pMe->pInfoPath)
	{
		free(pMe->pInfoPath);
		pMe->pInfoPath = NULL;
	}

	if(NULL != pMe->pRevQueue)
	{
		free(pMe->pRevQueue);
		pMe->pRevQueue = NULL;
	}

	if(NULL != pMe->pCardDatas)
	{
		free(pMe->pCardDatas);
		pMe->pCardDatas = NULL;
	}

	free( pMe );
}

boolean DictCards_New(DictCards ** ppMe)
{
    DictCards *pMe = 0; // Declare pointer to this extension.

    // Validate incoming parameters
    if(0 == ppMe)
    {
        return FALSE;
    }

    *ppMe = 0; //Initialize the module pointer to 0.

    // Allocate memory for size of class and function table:
    pMe = (DictCards *)malloc( sizeof(DictCards) );
	memset((void*)pMe, 0, sizeof(DictCards));

    // If there wasn't enough memory left for this extension:
    if(0 == pMe) {
        return FALSE;   //Return "no memory" error.
    }

	pMe->pCardDatas = NULL;
	pMe->pRevQueue = NULL;
	pMe->pCardPath = NULL;
	pMe->pInfoPath = NULL;
	pMe->cardCnt = 0;

    // Fill the pointer that will be output with this extension pointer:
    *ppMe = pMe; 

    return TRUE; 
}
