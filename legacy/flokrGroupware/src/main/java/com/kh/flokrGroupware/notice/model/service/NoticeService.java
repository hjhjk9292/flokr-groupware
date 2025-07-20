package com.kh.flokrGroupware.notice.model.service;

import java.util.List;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.notice.model.vo.Notice;

public interface NoticeService {
    // 공지사항 전체 개수 조회
    int getNoticeCount(String keyword, String category);
    
    // 공지사항 목록 조회 (페이징 처리)
    List<Notice> selectNoticeList(PageInfo pi, String keyword, String category);
    
    // 공지사항 상세 조회
    Notice selectNotice(int noticeNo);
    
    // 공지사항 조회수 증가
    void increaseCount(int noticeNo);
    
    // 공지사항 등록
    int insertNotice(Notice notice);
    
    // 공지사항 수정
    int updateNotice(Notice notice);
    
    // 공지사항 삭제 (상태 변경)
    int deleteNotice(int noticeNo);
    
    // 최근 공지사항 조회 (메인 페이지용)
    List<Notice> selectRecentNotices(int limit);
    
    // 필독 공지사항 조회
    List<Notice> selectMandatoryNotices();
}