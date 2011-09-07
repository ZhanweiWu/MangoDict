#ifndef __MEMORIZE_H__
#define __MEMORIZE_H__

#include "DictIndexUtil.h"
#include "DictCards.h"

typedef struct _DictMemorize {
	CacheFile oft_file;
	FILE *idxfile;
	uint32 npages;

	char wordentry_buf[MAX_INDEX_KEY_SIZE + sizeof(uint32) * 2];
	index_entry first, last, middle, real_last;

	char *page_data;
	int page_size;

	long wordcount;		// number of words in the index

	page_t page;

} DictMemorize;


#ifdef __cplusplus
extern "C" {
#endif

int32 DictMemorize_GenerateMemorizeFile(DictMemorize * pMe, char* urlSrc, char* urlDst);
boolean DictMemorize_New(DictMemorize ** ppMe);
boolean DictMemorize_Load(DictMemorize * pMe, char* url, uint32 wc, uint32 fsize);
boolean DictMemorize_Lookup(DictMemorize * pMe, const char *str, long *idx, long *idx_suggest);
const char *DictMemorize_GetKey(DictMemorize * pMe, long idx);
void DictMemorize_GetKeyNData(DictMemorize * pMe, long index, const char **key);
void DictMemorize_Release(DictMemorize * pMe);

#ifdef __cplusplus
}
#endif

#endif//!__MEMORIZE_H__
