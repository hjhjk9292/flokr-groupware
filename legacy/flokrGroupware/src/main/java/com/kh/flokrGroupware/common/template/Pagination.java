package com.kh.flokrGroupware.common.template;

import com.kh.flokrGroupware.common.model.vo.PageInfo;

public class Pagination {

	public static PageInfo getPageInfo(int listCount, int currentPage, int pageLimit, int boardLimit) {
	    int maxPage = (int)Math.ceil((double)listCount / boardLimit);
	    int startPage = (currentPage - 1) / pageLimit * pageLimit + 1;
	    int endPage = startPage + pageLimit - 1;
	    if(endPage > maxPage) {
	        endPage = maxPage;
	    }
	    int startRow = (currentPage - 1) * boardLimit + 1;
	    int endRow = startRow + boardLimit - 1;
	    return new PageInfo(listCount, currentPage, pageLimit, boardLimit, maxPage, startPage, endPage, startRow, endRow);
	}
}