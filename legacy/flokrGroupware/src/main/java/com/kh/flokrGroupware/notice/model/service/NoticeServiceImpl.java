package com.kh.flokrGroupware.notice.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.notice.model.dao.NoticeDao;
import com.kh.flokrGroupware.notice.model.vo.Notice;

@Service
public class NoticeServiceImpl implements NoticeService {
    
    @Autowired
    private SqlSessionTemplate sqlSession;
    
    @Autowired
    private NoticeDao noticeDao;
    
    @Override
    public int getNoticeCount(String keyword, String category) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("category", category);
        
        return noticeDao.getNoticeCount(sqlSession, params);
    }
    
    @Override
    public List<Notice> selectNoticeList(PageInfo pi, String keyword, String category) {
        Map<String, Object> params = new HashMap<>();
        params.put("pi", pi);
        params.put("keyword", keyword);
        params.put("category", category);
        
        return noticeDao.selectNoticeList(sqlSession, params);
    }
    
    @Override
    public Notice selectNotice(int noticeNo) {
        return noticeDao.selectNotice(sqlSession, noticeNo);
    }
    
    @Override
    public void increaseCount(int noticeNo) {
        noticeDao.increaseCount(sqlSession, noticeNo);
    }
    
    @Override
    public int insertNotice(Notice notice) {
        int result = noticeDao.insertNotice(sqlSession, notice);
        return result > 0 ? notice.getNoticeNo() : 0;
    }
    
    @Override
    public int updateNotice(Notice notice) {
        return noticeDao.updateNotice(sqlSession, notice);
    }
    
    @Override
    public int deleteNotice(int noticeNo) {
        return noticeDao.deleteNotice(sqlSession, noticeNo);
    }
    
    @Override
    public List<Notice> selectRecentNotices(int limit) {
        return noticeDao.selectRecentNotices(sqlSession, limit);
    }
    
    @Override
    public List<Notice> selectMandatoryNotices() {
        return noticeDao.selectMandatoryNotices(sqlSession);
    }
}