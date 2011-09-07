#include "DictMemorize.h"
#include "utils/DictUtils.h"
#include "memorize/DictMemorize.h"


static const char *DictMemorize_ReadFirstOnPageKey(DictMemorize * pMe, long page_idx)
{
	uint32 page_size = 0, minsize = 0;

	fseek(pMe->idxfile, pMe->oft_file.wordoffset[page_idx], SEEK_SET);
	page_size = pMe->oft_file.wordoffset[page_idx + 1] - pMe->oft_file.wordoffset[page_idx];
	minsize = sizeof(pMe->wordentry_buf);
	if (page_size < minsize)
		minsize = page_size;
	fread(pMe->wordentry_buf, minsize, 1, pMe->idxfile);
	if(!check_key_str_len(pMe->wordentry_buf, minsize)) {
		pMe->wordentry_buf[minsize-1] = '\0';
		// Error.Index key length exceeds allowed limit.
		return NULL;
	}

	return pMe->wordentry_buf;
}

static const char *DictMemorize_GetFirstOnPageKey(DictMemorize * pMe, long page_idx)
{
	if (page_idx < pMe->middle.idx) {
		if (page_idx == pMe->first.idx)
			return pMe->first.keystr;
		return DictMemorize_ReadFirstOnPageKey(pMe, page_idx);
	} else if (page_idx > pMe->middle.idx) {
		if (page_idx == pMe->last.idx)
			return pMe->last.keystr;
		return DictMemorize_ReadFirstOnPageKey(pMe, page_idx);
	} else
		return pMe->middle.keystr;
}

static boolean DictMemorize_PageDataResize(DictMemorize * pMe, uint32 size)
{
	if(NULL != pMe->page_data)
	{
		free( pMe->page_data );
		pMe->page_data = NULL;
	}

	if (pMe->page_data = malloc(size))
	if (NULL == pMe->page_data)
		return FALSE;

	return TRUE;
}

static void DictMemorize_PageFill(DictMemorize * pMe, char *data, int nent, long idx)
{
	int i = 0;
	char *p = data;

	pMe->page.idx = idx;

	for (i = 0; i < nent; ++i) {
		pMe->page.entries[i].keystr=p;
		p += strlen(p) + 1;
	}
}

static uint32 DictMemorize_LoadPage(DictMemorize * pMe, long page_idx)
{
	uint32 nentr = ENTR_PER_PAGE;
	if (page_idx == (long)(pMe->npages - 2))
		if ((nentr = pMe->wordcount % ENTR_PER_PAGE) == 0)
			nentr = ENTR_PER_PAGE;

	if (page_idx != pMe->page.idx) {
		int page_size = pMe->oft_file.wordoffset[page_idx+1] - pMe->oft_file.wordoffset[page_idx];

		if(page_size > pMe->page_size)
		{
			pMe->page_size = page_size;
			DictMemorize_PageDataResize(pMe, page_size);
		}

		fseek(pMe->idxfile, pMe->oft_file.wordoffset[page_idx], SEEK_SET);
		fread(pMe->page_data, 1, page_size, pMe->idxfile);
		DictMemorize_PageFill(pMe, pMe->page_data, nentr, page_idx);
	}

	return nentr;
}

const char *DictMemorize_GetKey(DictMemorize * pMe, long idx)
{
	long idx_in_page = 0;

	DictMemorize_LoadPage(pMe, idx / ENTR_PER_PAGE);
	idx_in_page = idx % ENTR_PER_PAGE;

	return pMe->page.entries[idx_in_page].keystr;
}

boolean DictMemorize_Load(DictMemorize * pMe, char* url, uint32 wc, uint32 fsize)
{
	MapFile mapfile;
	char * idxdatabuffer = NULL;
	char * p1 = NULL;
	uint32 index_size = 0;
	uint32 i = 0, j = 0;
	uint32 npages = 0;

	pMe->wordcount = wc;
	pMe->npages = (wc - 1) / ENTR_PER_PAGE + 2;
	npages = pMe->npages;

    pMe->idxfile = fopen(url, "rb");
	if (NULL == pMe->idxfile) {
       return FALSE;
	}

	if (!MapFile_Open(&mapfile, url, fsize)) {
	    return FALSE;
	}
	idxdatabuffer = MapFile_Begin(&mapfile);
	pMe->oft_file.wordoffset = malloc(npages * sizeof(uint32));
	p1 = idxdatabuffer;
	for (i = 0; i < wc; i++) {
		index_size = strlen(p1) + 1;
		if (i % ENTR_PER_PAGE == 0) {
			pMe->oft_file.wordoffset[j] = p1 - idxdatabuffer;
			++j;
		}
		p1 += index_size;
	}
	pMe->oft_file.wordoffset[j] = p1 - idxdatabuffer;
	MapFile_Close(&mapfile);

	pMe->first.idx = 0;
	strcpy(pMe->first.keystr, DictMemorize_ReadFirstOnPageKey(pMe, 0));
	pMe->last.idx = npages - 2;
	strcpy(pMe->last.keystr, DictMemorize_ReadFirstOnPageKey(pMe, npages - 2));
	pMe->middle.idx = (npages - 2) / 2;
	strcpy(pMe->middle.keystr, DictMemorize_ReadFirstOnPageKey(pMe, (npages - 2) / 2));
	pMe->real_last.idx = wc - 1;
	strcpy(pMe->real_last.keystr, DictMemorize_GetKey(pMe, wc - 1));

	return TRUE;
}

boolean DictMemorize_Lookup(DictMemorize * pMe, const char *str, long *idx, long *idx_suggest)
{
	long idx2 = 0, idx_suggest2 = 0;
	boolean bFound = FALSE;
	long iFrom = 0;
	long iTo = pMe->npages - 2;
	int cmpint = 0;
	long iThisIndex = 0;

	if(NULL == pMe->idxfile)
		return FALSE;

	if (stardict_strcmp(str, pMe->first.keystr)<0) {
		*idx = 0;
		*idx_suggest = 0;
		return FALSE;
	} else if (stardict_strcmp(str, pMe->real_last.keystr) >0) {
		*idx = INVALID_INDEX;
		*idx_suggest = pMe->wordcount - 1;
		return FALSE;
	} else {
		// find the page number where the search word might be
		iFrom = 0;
		iThisIndex = 0;
		while (iFrom <= iTo) {
			iThisIndex = (iFrom + iTo) / 2;
			cmpint = stardict_strcmp(str, DictMemorize_GetFirstOnPageKey(pMe, iThisIndex));
			if (cmpint > 0)
				iFrom = iThisIndex + 1;
			else if (cmpint < 0)
				iTo = iThisIndex - 1;
			else {
				bFound = TRUE;
				break;
			}
		}
		if (!bFound) {
			idx2 = iTo;    //prev
		} else {
			idx2 = iThisIndex;
		}
	}
	if (!bFound) {
		// the search word is on the page number idx if it's anywhere
		uint32 netr = DictMemorize_LoadPage(pMe, idx2);
		iFrom =  1; // Needn't search the first word anymore.
		iTo = netr - 1;
		iThisIndex = 0;
		while (iFrom <= iTo) {
			iThisIndex = (iFrom + iTo) / 2;
			cmpint = stardict_strcmp(str, pMe->page.entries[iThisIndex].keystr);
			if (cmpint > 0)
				iFrom = iThisIndex + 1;
			else if (cmpint < 0)
				iTo = iThisIndex - 1;
			else {
				bFound = TRUE;
				break;
			}
		}
		idx2 *= ENTR_PER_PAGE;
		if (!bFound) {
			int best, back;
			idx2 += iFrom;    //next
			idx_suggest2 = idx2;

			best = prefix_match (str, pMe->page.entries[idx_suggest2 % ENTR_PER_PAGE].keystr);
			for (;;) {
				if ((iTo = idx_suggest2 - 1) < 0)
					break;
				if (idx_suggest2 % ENTR_PER_PAGE == 0)
					DictMemorize_LoadPage(pMe, iTo / ENTR_PER_PAGE);
				back = prefix_match (str, pMe->page.entries[iTo % ENTR_PER_PAGE].keystr);
				if (!back || back < best)
					break;
				best = back;
				idx_suggest2 = iTo;
			}
		} else {
			idx2 += iThisIndex;
			idx_suggest2 = idx2;
		}
	} else {
		idx2 *=ENTR_PER_PAGE;
		idx_suggest2 = idx2;
	}

	*idx = idx2;
	*idx_suggest = idx_suggest2;

	return bFound;
}

void DictMemorize_GetKeyNData(DictMemorize * pMe, long index, const char **key)
{
	*key = DictMemorize_GetKey(pMe, index);
}

int32 DictMemorize_GenerateMemorizeFile(DictMemorize * pMe, char* urlSrc, char* urlDst)
{
	int i = 0;
	int32 cnt = 0;
	int len = 0;
    struct stat stats;
	MapFile mapfile;
	char strWordCount[22];
	char * pInfoFile = NULL;
	char * p1 = NULL;
	char * p2 = NULL;
	char * pSrcData = NULL;
	FILE * pFileMem = NULL;
	FILE * pFileMfo = NULL;

    if(stat(urlSrc, &stats))
       return 0;

	pFileMem = fopen(urlDst, "wb");
	if(NULL == pFileMem)
	{
		return 0;
	}

	if (!MapFile_Open(&mapfile, urlSrc, stats.st_size)) {
	    return 0;
	}
	pSrcData = MapFile_Begin(&mapfile);
	p1 = pSrcData;
	p2 = p1;
	for (i = 0; i < stats.st_size; i++)
	{
		if(*p1 == '\n')
		{
			int size = p1 - p2;

			if(*(p1 - 1) == '\r')
				size = size - 1; // Ignore '\r'.

			if(size > 0)
			{
				fwrite(p2, size, 1, pFileMem);
				fwrite("\0", 1, 1, pFileMem);				 // Write '\0' for the end of this word.
				cnt++;
			}

			p1++;
			p2 = p1;
		}
		else
		{
			p1++;
		}
	}
	if(p1 > p2)	// The last word when there is no '\r' at the end of this file.
	{
		fwrite(p2, (p1 - p2), 1, pFileMem);
		fwrite("\0", 1, 1, pFileMem);				 // Write '\0' for the end of this word.
		cnt++;
	}
	MapFile_Close(&mapfile);
	fclose(pFileMem);

	pInfoFile = strdup(urlDst);
	len = strlen(pInfoFile);

	pInfoFile[len - 2] = 'f';	// Memorize file ext name is '.mem', info file ext name is '.mfo', cards file ext name is '.mcd'
	pInfoFile[len - 1] = 'o';
	sprintf(strWordCount, "wordcount=%d", cnt);
	pFileMfo = fopen(pInfoFile, "wb");
	fwrite(strWordCount, strlen(strWordCount), 1, pFileMfo);
	fwrite("\r\n", 2, 1, pFileMfo);
	fclose(pFileMfo);

	pInfoFile[len - 2] = 'c';	// Memorize file ext name is '.mem', info file ext name is '.mfo', cards file ext name is '.mcd'
	pInfoFile[len - 1] = 'd';
	DictCards_GenerateCardsFile(NULL, pInfoFile, cnt);

	free(pInfoFile);

	return cnt;
}


void DictMemorize_Release(DictMemorize * pMe)
{
	if(NULL == pMe)
		return;

	if(NULL != pMe->oft_file.wordoffset)
	{
		free(pMe->oft_file.wordoffset);
		pMe->oft_file.wordoffset = NULL;
	}

	if(NULL != pMe->page_data)
	{
		free(pMe->page_data);
		pMe->page_data = NULL;
	}

	if(NULL != pMe->idxfile)
	{
		fclose(pMe->idxfile);
		pMe->idxfile = NULL;
	}

	free( pMe );
}

boolean DictMemorize_New(DictMemorize ** ppMe)
{
    DictMemorize *pMe = 0; // Declare pointer to this extension.

    // Validate incoming parameters
    if(0 == ppMe)
    {
        return FALSE;
    }

    *ppMe = 0; //Initialize the module pointer to 0.

    // Allocate memory for size of class and function table:
    pMe = (DictMemorize *)malloc( sizeof(DictMemorize) );
	memset((void*)pMe, 0, sizeof(DictMemorize));

    // If there wasn't enough memory left for this extension:
    if(0 == pMe) {
        return FALSE;   //Return "no memory" error.
    }

	pMe->idxfile = NULL;
	pMe->npages = 0;
	pMe->page_data = NULL;
	pMe->page_size = 0;
	pMe->oft_file.wordoffset = NULL;
	pMe->page.idx = -1;

    // Fill the pointer that will be output with this extension pointer:
    *ppMe = pMe; 

    return TRUE; 
}
