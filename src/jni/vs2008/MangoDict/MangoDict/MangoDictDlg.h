// MangoDictDlg.h : header file
//

#pragma once

#include "afxhtml.h"
#include "cHtmlEditor.h"
#include "afxwin.h"


// CMangoDictDlg dialog
class CMangoDictDlg : public CDialog
{
// Construction
public:
	CMangoDictDlg(CWnd* pParent = NULL);	// standard constructor
	~CMangoDictDlg();

// Dialog Data
	enum { IDD = IDD_MANGODICT_DIALOG };

	CStatic	mCtrl_StaticEditor;
	CButton	mCtrl_Search;
	CEdit	mCtrl_EditUrl;


	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support

	CHtmlEditor *pi_Editor;

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg void OnDestroy();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSearch();
	afx_msg void OnBnClickedButtonExit();
	afx_msg void OnBnClickedButtonWords();
	afx_msg void OnBnClickedButtonGenmemorize();
	afx_msg void OnBnClickedButtonMemorize();
	afx_msg void OnBnClickedButtonDontknow();
	afx_msg void OnBnClickedButtonAlmost();
	afx_msg void OnBnClickedButtonKnew();
	afx_msg void OnBnClickedButtonGrade0();
	afx_msg void OnBnClickedButtonGrade1();
	afx_msg void OnBnClickedButtonGrade2();
	afx_msg void OnBnClickedButtonGrade3();
	afx_msg void OnBnClickedButtonGrade4();
	afx_msg void OnBnClickedButtonGrade05();
	afx_msg void OnBnClickedButtonLearnnewcard();
	afx_msg void OnBnClickedButtonLearnaheadcard();
	afx_msg void OnBnClickedButtonScheduledcard();
	afx_msg void OnBnClickedButtonSchedule();
};
