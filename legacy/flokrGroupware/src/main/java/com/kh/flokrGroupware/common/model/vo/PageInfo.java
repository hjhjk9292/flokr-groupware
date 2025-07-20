package com.kh.flokrGroupware.common.model.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageInfo implements Serializable{
    private int listCount;    // 총 게시글 수
    private int currentPage;  // 현재 페이지
    private int pageLimit;    // 한 페이지에 보여질 페이징바의 페이지 개수
    private int boardLimit;   // 한 페이지에 보여질 게시글 개수
    private int maxPage;      // 가장 마지막 페이지 (총 페이지 수)
    private int startPage;    // 페이징바의 시작 페이지
    private int endPage;      // 페이징바의 끝 페이지
    private int startRow;     // 조회 시작 행 번호
    private int endRow;       // 조회 끝 행 번호
}