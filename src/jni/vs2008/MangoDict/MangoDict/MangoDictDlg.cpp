// MangoDictDlg.cpp : implementation file
//

#include "stdafx.h"
#include "MangoDict.h"
#include "utils\DictUtils.h"
#include "memorize\DictMemorize.h"
#include "InfoFile.h"
#include "MangoDictDlg.h"


#include "DictEng.h"

static DictEng * g_pDictEng = NULL;
static DictMemorize * g_pDictMemorize = NULL;
static DictCards * g_pDictCards = NULL;


static char * pPaths[] = {
			"C:\\Dropbox\\stardict-powerword2007_pwdeeahd-2.4.2\\",
	};

static char * pNames[] = {
			"powerword2007_pwdeeahd",
	};

static int pTypes[] = {
		DICT_TYPE_INDEX,
		DICT_TYPE_INDEX,
		DICT_TYPE_CAPTURE,
		DICT_TYPE_INDEX | DICT_TYPE_MEMORIZE,
		DICT_TYPE_INDEX | DICT_TYPE_MEMORIZE
	};

static CHtmlEditor *g_pi_Editor = NULL;


// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
END_MESSAGE_MAP()


// CMangoDictDlg dialog




CMangoDictDlg::CMangoDictDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CMangoDictDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

CMangoDictDlg::~CMangoDictDlg()
{
	// Release the dict resources.
	DictEng_Release(g_pDictEng);
	DictMemorize_Release(g_pDictMemorize);
	DictCards_Release(g_pDictCards);
}

void CMangoDictDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);

	DDX_Control(pDX, IDC_STATIC_EDITOR, mCtrl_StaticEditor);
	DDX_Control(pDX, IDC_BUTTON_SEARCH, mCtrl_Search);
	DDX_Control(pDX, IDC_EDIT_URL, mCtrl_EditUrl);
}

BEGIN_MESSAGE_MAP(CMangoDictDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP

	ON_BN_CLICKED(IDC_BUTTON_SEARCH, &CMangoDictDlg::OnBnClickedButtonSearch)
	ON_BN_CLICKED(IDC_BUTTON_EXIT, &CMangoDictDlg::OnBnClickedButtonExit)
	ON_BN_CLICKED(IDC_BUTTON_WORDS, &CMangoDictDlg::OnBnClickedButtonWords)
	ON_BN_CLICKED(IDC_BUTTON_GENMEMORIZE, &CMangoDictDlg::OnBnClickedButtonGenmemorize)
	ON_BN_CLICKED(IDC_BUTTON_MEMORIZE, &CMangoDictDlg::OnBnClickedButtonMemorize)
	ON_BN_CLICKED(IDC_BUTTON_GRADE0, &CMangoDictDlg::OnBnClickedButtonGrade0)
	ON_BN_CLICKED(IDC_BUTTON_GRADE1, &CMangoDictDlg::OnBnClickedButtonGrade1)
	ON_BN_CLICKED(IDC_BUTTON_GRADE2, &CMangoDictDlg::OnBnClickedButtonGrade2)
	ON_BN_CLICKED(IDC_BUTTON_GRADE3, &CMangoDictDlg::OnBnClickedButtonGrade3)
	ON_BN_CLICKED(IDC_BUTTON_GRADE4, &CMangoDictDlg::OnBnClickedButtonGrade4)
	ON_BN_CLICKED(IDC_BUTTON_GRADE05, &CMangoDictDlg::OnBnClickedButtonGrade05)
	ON_BN_CLICKED(IDC_BUTTON_LEARNNEWCARD, &CMangoDictDlg::OnBnClickedButtonLearnnewcard)
	ON_BN_CLICKED(IDC_BUTTON_LEARNAHEADCARD, &CMangoDictDlg::OnBnClickedButtonLearnaheadcard)
	ON_BN_CLICKED(IDC_BUTTON_SCHEDULEDCARD, &CMangoDictDlg::OnBnClickedButtonScheduledcard)
	ON_BN_CLICKED(IDC_BUTTON_SCHEDULE, &CMangoDictDlg::OnBnClickedButtonSchedule)
END_MESSAGE_MAP()


void CALLBACK TimerProc(HWND hWnd,UINT nMsg,UINT nTimerid,DWORD dwTime)
{
	KillTimer(hWnd, nTimerid);

	switch(nTimerid)
	{
		case 1:
		g_pi_Editor->SetDisplayMode(FALSE,FALSE);
		break;

		default:
		break;
	}
}

// CMangoDictDlg message handlers
BOOL CMangoDictDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	// TODO: Add extra initialization here
	pi_Editor = new CHtmlEditor();
	if (!pi_Editor->CreateEditor(&mCtrl_StaticEditor, this, FALSE, TRUE))
	{
		MessageBox(_T("Error creating CHtmlEditor"), _T("Error"), MB_ICONSTOP);
		CDialog::EndDialog(0);
		return TRUE;
	}

	// Initialize the dict engine.
    DictEng_New(&g_pDictEng);
    DictEng_LoadDicts(g_pDictEng, pPaths, pNames, pTypes, sizeof(pPaths) / sizeof(void*));

	g_pi_Editor = pi_Editor;
	SetTimer(1,100,TimerProc);

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CMangoDictDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}


// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CMangoDictDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CMangoDictDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CMangoDictDlg::OnDestroy(void)
{

}


void CMangoDictDlg::OnBnClickedButtonExit()
{
	// delete pi_Editor is not required as DestroyWindow() does the job !
	if (pi_Editor) pi_Editor->DestroyWindow();

	OnCancel();
}


bool Utf8ToCString( CString& cstr, const char* utf8Str )
{
    size_t utf8StrLen = strlen(utf8Str);

    if( utf8StrLen == 0 )
    {
        cstr.Empty();
        return true;
    }

    LPWSTR ptr = cstr.GetBuffer(utf8StrLen+1);

    // CString is UNICODE string so we decode
    int newLen = MultiByteToWideChar(CP_UTF8,  0, utf8Str, utf8StrLen,  (LPWSTR)ptr, utf8StrLen + 1);
    if( !newLen )
    {
        cstr.ReleaseBuffer(0);
        return false;
    }

    cstr.ReleaseBuffer(newLen);
    return true;
}


#define HTML_KEYWORD_B		"<span style='color:#0000ff;'><b>"
#define HTML_KEYWORD_E		"</b></span><br>"
#define HTML_NEW_LINE		"<br><hr>"


void LookupDict(char * pWord, int type)
{
	cHtmlDocument *pi_Doc  = g_pi_Editor->GetDocument();
	CString strTmp;
	CString strHtmlData = _T("<head><meta http-equiv=\"content-Type\" content=\"text/html; charset=UTF-8\"></head>");

	{
	int i = 0, k = 0, j = 0;
	int dictCount = 0;
	char * pHtmlData = NULL;
	int dataSize = 0;
	WordInfo wordInfo = {0};


	dictCount = DictEng_DictCount(g_pDictEng);
	if (dictCount <= 0)
		return;

	DictEng_Lookup(g_pDictEng, pWord, &wordInfo, type);

	for (i = 0; i < wordInfo.dictCount; i++)
	{
		int wordcount = wordInfo.wordData[i].wordcount;
		char * p = NULL;

		if(wordcount <= 0)
			continue;

		for (k = 0; k < wordcount; k++)
		{
			dataSize += strlen(wordInfo.wordData[i].pKeyWord[k]);
			dataSize += strlen(wordInfo.wordData[i].pWordData[k]);
			dataSize += strlen(HTML_KEYWORD_B) + strlen(HTML_KEYWORD_E);
			dataSize += strlen(HTML_NEW_LINE);
		}
		dataSize -= strlen(HTML_NEW_LINE);		// Last line don't add new line.

		if(0 != i){
			Utf8ToCString(strTmp, "<br><br>");
			strHtmlData.Append( strTmp );
		}

		Utf8ToCString(strTmp, "<b>>>> ");
		strHtmlData.Append( strTmp );
		Utf8ToCString(strTmp, wordInfo.wordData[i].bookName);
		strHtmlData.Append( strTmp );
		Utf8ToCString(strTmp, " <<<</b><br>");
		strHtmlData.Append( strTmp );

		pHtmlData = (char*)malloc(dataSize + 1);
		pHtmlData[dataSize] = '\0';
		p = pHtmlData;

		for (k = 0; k < wordcount; k++)
		{
			strcpy(p, HTML_KEYWORD_B);
			p += strlen(HTML_KEYWORD_B);
			strcpy(p, wordInfo.wordData[i].pKeyWord[k]);
			p += strlen(wordInfo.wordData[i].pKeyWord[k]);
			strcpy(p, HTML_KEYWORD_E);
			p += strlen(HTML_KEYWORD_E);

			strcpy(p, wordInfo.wordData[i].pWordData[k]);
			p += strlen(wordInfo.wordData[i].pWordData[k]);

			if(k < wordcount - 1)
			{
				strcpy(p, HTML_NEW_LINE);
				p += strlen(HTML_NEW_LINE);
			}
		}

		Utf8ToCString(strTmp, pHtmlData);
		strHtmlData.Append( strTmp );

		free(pHtmlData);
	}

	// wordInfo must be freed. Please refer to 'DictEng_Lookup' in 'DictEng.c'.
	DictEng_FreeWordInfo(g_pDictEng, &wordInfo);
	}

	pi_Doc->SetHtml(strHtmlData);
}

void CMangoDictDlg::OnBnClickedButtonSearch()
{
	CString strWord;
	char * pWord = NULL;
	int wordLen = 0;

	mCtrl_EditUrl.GetWindowText(strWord);
	if(strWord.GetLength() <= 0 )
		return;

	wordLen = strWord.GetLength() * 3 + 1;
	pWord = (char*)malloc(wordLen);
	memset(pWord, 0, wordLen);
	WStrToUTF8((wchar*)strWord.GetBuffer(), strWord.GetLength(), (byte*)pWord, wordLen);

	LookupDict(pWord, DICT_TYPE_INDEX);

	free(pWord);
}

static void Progress_CB(double progress)
{
	int i = (int)progress * 100;



}

void CMangoDictDlg::OnBnClickedButtonWords()
{
	CString strWord;
	char * pWord = NULL;
	char flag;
	int wordLen = 0;

	cHtmlDocument *pi_Doc  = pi_Editor->GetDocument();
	CString strTmp;
	CString strHtmlData = _T("<head><meta http-equiv=\"content-Type\" content=\"text/html; charset=UTF-8\"></head>");

	mCtrl_EditUrl.GetWindowText(strWord);

	if(strWord.GetLength() <= 0 )
		return;

	flag = (char)strWord.GetAt(0);
	if('/' == flag || ':' == flag)
	{
		strWord = strWord.Right(strWord.GetLength() - 1);
	}

	wordLen = strWord.GetLength() * 3 + 1;
	pWord = (char*)malloc(wordLen);
	memset(pWord, 0, wordLen);
	WStrToUTF8((wchar*)strWord.GetBuffer(), strWord.GetLength(), (byte*)pWord, wordLen);


	{
	int wordcount = 0;
	char ** pWordsList = NULL;
	int i = 0;
	boolean bFound = FALSE;

	if (DictEng_DictCount(g_pDictEng) <= 0)
		return;

	DictEng_SetLookupCancel(g_pDictEng, FALSE);
	if('/' == flag)
	{
		wordcount = DictEng_LookupWithFuzzy(g_pDictEng, pWord, &pWordsList, (PFN_PROGRESS)Progress_CB);
	}
	else if(':' == flag)
	{
		boolean bFind = FALSE;
		boolean bCancel = FALSE;
		SearchInfo searchInfo = {0};
		bFind = DictEng_LookupData(g_pDictEng, pWord, &searchInfo, (PFN_PROGRESS)Progress_CB);

		free(pWord);
		DictEng_FreeSearchInfo(g_pDictEng, &searchInfo);

		return;
	}
	else if(strchr(pWord, '*') || strchr(pWord, '?'))
	{
		wordcount = DictEng_LookupWithRule(g_pDictEng, pWord, &pWordsList, (PFN_PROGRESS)Progress_CB);
	}
	else
	{
		wordcount = DictEng_ListWords(g_pDictEng, pWord, &pWordsList);
	}

	if (wordcount)
	{
		for (i = 0; i < wordcount; i++) {
			Utf8ToCString(strTmp, pWordsList[i]);
			strHtmlData.Append( strTmp );
			Utf8ToCString(strTmp, "<br>");
			strHtmlData.Append( strTmp );
			free(pWordsList[i]);
		}
	}

	free(pWordsList);
	}

	free(pWord);

	pi_Doc->SetHtml(strHtmlData);
}



void ShowHtmlContent(char * pContent)
{
	cHtmlDocument *pi_Doc  = g_pi_Editor->GetDocument();
	CString strHtmlContent;

	Utf8ToCString(strHtmlContent, pContent);

	pi_Doc->SetHtml(strHtmlContent);
}


// Generate Memorize File according to text file.
void CMangoDictDlg::OnBnClickedButtonGenmemorize()
{
	char * pSrc = "C:\\Dropbox\\test.txt";
	char * pDst = "C:\\Dropbox\\collins1.mem";

	DictMemorize_GenerateMemorizeFile(NULL, pSrc, pDst);
}

void SaveMemorizeInfo()
{
	//DictMemorize_SaveMemorizeInfo(g_pDictMemorize, dontknow, almost, knew, flag, off); 
}

void ShowMemorizeWord()
{
	char * word;
	int index = 0;

	if(NULL == g_pDictMemorize)
		return;

	index = DictCards_GetCurCardIndex(g_pDictCards);
	DictMemorize_GetKeyNData(g_pDictMemorize, index, (const char**)&word);

	LookupDict(word, DICT_TYPE_MEMORIZE);
}

void GradeAnswerAndShowWord(int grade)
{
	DictCards_GradeAnswer(g_pDictCards, grade);

	if(TRUE == DictCards_NewQuestion(g_pDictCards))
	{
		ShowMemorizeWord();
	}
	else
	{
		if(BUILD_QUEUE_SCHEDULED == g_pDictCards->curQueueType)
		{
			ShowHtmlContent("Current task has been finished. Please select more scheduled cards, new cards or ahead cards to learn.");
		}
		else if(BUILD_QUEUE_NEWCARD == g_pDictCards->curQueueType)
		{
			ShowHtmlContent("Current task has been finished. Please select more scheduled cards, new cards or ahead cards to learn.");
		}
		else
		{
			ShowHtmlContent("Current task has been finished. Please select more scheduled cards, new cards or ahead cards to learn.");
		}
	}
}

void BuildScheduledCards()
{
	DictCards_RebuildRevisionQueue(g_pDictCards, BUILD_QUEUE_SCHEDULED);
	if(g_pDictCards->revQueueCnt > 0)
	{
		DictCards_NewQuestion(g_pDictCards);
		ShowMemorizeWord();
	}
	else
	{
		ShowHtmlContent("No scheduled card to be learnt, do you want to learn new card?");
	}
}

void BuildNewCards()
{
	DictCards_RebuildRevisionQueue(g_pDictCards, BUILD_QUEUE_NEWCARD);
	if(g_pDictCards->revQueueCnt > 0)
	{
		DictCards_NewQuestion(g_pDictCards);
		ShowMemorizeWord();
	}
	else
	{
		ShowHtmlContent("No new card to be learnt, do you want to learn ahead card?");
	}
}

void BuildAheadCards()
{
	DictCards_RebuildRevisionQueue(g_pDictCards, BUILD_QUEUE_LEARNAHEAD);
	if(g_pDictCards->revQueueCnt > 0)
	{
		DictCards_NewQuestion(g_pDictCards);
		ShowMemorizeWord();
	}
	else
	{
		ShowHtmlContent("No ahead card to be learnt.");
	}
}

void CMangoDictDlg::OnBnClickedButtonMemorize()
{
    struct stat stats;
	long wc = 0;
	boolean bFind = FALSE;
	char * pMemPath = "C:\\Dropbox\\collins1.mem";
	char * pMfoPath = "C:\\Dropbox\\collins1.mfo";
	char * pMcdPath = "C:\\Dropbox\\collins1.mcd";

    if(stat(pMemPath, &stats))
       return;

	if(NULL != g_pDictMemorize)
		return;

	bFind = InfoFile_Load(pMfoPath, &wc, NULL, NULL, NULL, NULL);

	if(TRUE == bFind)
	{
	    DictMemorize_New(&g_pDictMemorize);
		DictMemorize_Load(g_pDictMemorize, pMemPath, wc, stats.st_size);

		DictCards_New(&g_pDictCards);
		DictCards_LoadCardsFile(g_pDictCards, pMcdPath);

		BuildScheduledCards();
	}
}

void CMangoDictDlg::OnBnClickedButtonScheduledcard()
{
	BuildScheduledCards();
}


void CMangoDictDlg::OnBnClickedButtonLearnnewcard()
{
	BuildNewCards();
}

void CMangoDictDlg::OnBnClickedButtonLearnaheadcard()
{
	BuildAheadCards();
}

void CMangoDictDlg::OnBnClickedButtonGrade0()
{
	GradeAnswerAndShowWord(0);
}

void CMangoDictDlg::OnBnClickedButtonGrade1()
{
	GradeAnswerAndShowWord(1);
}

void CMangoDictDlg::OnBnClickedButtonGrade2()
{
	GradeAnswerAndShowWord(2);
}

void CMangoDictDlg::OnBnClickedButtonGrade3()
{
	GradeAnswerAndShowWord(3);
}

void CMangoDictDlg::OnBnClickedButtonGrade4()
{
	GradeAnswerAndShowWord(4);
}

void CMangoDictDlg::OnBnClickedButtonGrade05()
{
	GradeAnswerAndShowWord(5);
}



void CMangoDictDlg::OnBnClickedButtonSchedule()
{
	int * pData = NULL;
	int length = 0;
	int i = 0;

	DictCards_GetScheduleData(g_pDictCards, &pData, &length, 2011, 7);

 
    if(length == 0)
		return;

    for(i = 0; i < length; i++)
	{
		if(pData[i] > 0)
		{
			int cnt = pData[i];


			cnt += 0;
		}
	}


	free(pData);
}
