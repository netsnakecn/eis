package com.maicard.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.site.entity.ContentPaging;

public class PagingUtils {
	
	public static final int DISPLAY_PAGE_COUNT = 5;
	
	
	
	protected static final Logger logger = LoggerFactory.getLogger(PagingUtils.class);

	public static ContentPaging generateContentPaging(int totalRows, int rowPerPage, int currentPage){
		ContentPaging contentPaging = new ContentPaging(totalRows);
		contentPaging.setRowsPerPage(rowPerPage);
		contentPaging.setCurrentPage(currentPage);
		contentPaging.setDisplayPageCount(DISPLAY_PAGE_COUNT);
		contentPaging.caculateDisplayPage();
		logger.debug("一共" + totalRows + "行数据, " +  contentPaging.getTotalPage() + "页，每页显示" + rowPerPage + "行数据,当前第" + currentPage + "页, 每页显示5个页码, 第一个页码:" + contentPaging.getDisplayFirstPage() + ",最后一个页码:" + contentPaging.getDisplayLastPage());
		return contentPaging;
	}

	public static void paging(CriteriaMap params, int row) {
		
		paging(params, row, params.getIntValue("page"));
		 
		
	}
	
	public static void paging(CriteriaMap params, int rows, int page) {
		int limits = params.getIntValue("limits");
		logger.debug("进行分页，已有分页是:{}，请求rows={},page={}", JSON.toJSONString(params), rows, page);
		if(limits > 0 && limits < Constants.MAX_ROW_LIMIT) {
			if(!params.containsKey("starts")) {
				params.put("starts",0);
			}
			return;
		}
		
		if(rows <= 0) {
			rows = Constants.DEFAULT_PARTNER_ROWS_PER_PAGE;
		}
		if(rows > Constants.MAX_ROW_LIMIT) {
			rows = Constants.MAX_ROW_LIMIT;
		}
		
		int downloadMode = params.getIntValue("download");
		if(downloadMode == 2) {
			//params.remove("limits");
			params.put("starts",0);
			params.put("limits", Constants.MAX_ROW_LIMIT);
			return;	      	
		}
		downloadMode = params.getIntValue("downloadMode");
		if(downloadMode == 2) {
			//params.remove("limits");
			params.put("starts",0);
			params.put("limits", Constants.MAX_ROW_LIMIT);
			return;	      	
		}
		
		// 每页行数
		params.put("limits", rows);
		if (page < 1) {
			page = 1;
		}
		if(rows == Constants.MAX_ROW_LIMIT) {
			params.put("starts", 0);
		} else {
			params.put("starts", (page - 1) * rows);
		}
		
	}
	
	public static void clearPaging(CriteriaMap params) {
		params.remove("limits");
		params.remove("starts");
		params.remove("rows");
		return;	     
	}

	
}
