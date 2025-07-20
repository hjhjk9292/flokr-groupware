package com.kh.flokrGroupware.notice.model.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.notice.model.vo.Notice;

@Repository
public class NoticeDao {
    
    public int getNoticeCount(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.selectOne("noticeMapper.getNoticeCount", params);
    }
    
    public List<Notice> selectNoticeList(SqlSessionTemplate sqlSession, Map<String, Object> params) {
        return sqlSession.selectList("noticeMapper.selectNoticeList", params);
    }
    
    public Notice selectNotice(SqlSessionTemplate sqlSession, int noticeNo) {
        return sqlSession.selectOne("noticeMapper.selectNotice", noticeNo);
    }
    
    public void increaseCount(SqlSessionTemplate sqlSession, int noticeNo) {
        sqlSession.update("noticeMapper.increaseCount", noticeNo);
    }
    
    public int insertNotice(SqlSessionTemplate sqlSession, Notice notice) {
        return sqlSession.insert("noticeMapper.insertNotice", notice);
    }
    
    public int updateNotice(SqlSessionTemplate sqlSession, Notice notice) {
        return sqlSession.update("noticeMapper.updateNotice", notice);
    }
    
    public int deleteNotice(SqlSessionTemplate sqlSession, int noticeNo) {
        return sqlSession.update("noticeMapper.deleteNotice", noticeNo);
    }
    
    public List<Notice> selectRecentNotices(SqlSessionTemplate sqlSession, int limit) {
        return sqlSession.selectList("noticeMapper.selectRecentNotices", limit);
    }
    
    public List<Notice> selectMandatoryNotices(SqlSessionTemplate sqlSession) {
        return sqlSession.selectList("noticeMapper.selectMandatoryNotices");
    }
}