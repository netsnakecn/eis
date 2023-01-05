package com.maicard.utils;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.io.FileUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.core.constants.Constants;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

public class EasypoiUtils {

	protected static final Logger logger = LoggerFactory.getLogger(EasypoiUtils.class);


	public static final String HEADER_TYPE = "Content-disposition";

	public static final String HEADER_VALUE = "attachment;filename=";

	public static final String HEADER_CONTENT_TYPE = "application/oct-stream";

	public static boolean exportExcel(ExportParams exportParams,Collection<?> dataSet,Class<?> entityClass,String filePath) {
		//递归建立文件夹
		try {
			//递归建立文件夹
			FileUtil.mkdir(filePath);
			Workbook workbook = ExcelExportUtil.exportExcel(exportParams, entityClass, dataSet);
			FileOutputStream fos = new FileOutputStream(filePath);
			workbook.write(fos);
			fos.close();
			return true;
		} catch (Exception e) {
			logger.error("处理失败",e);
		}
		return false;
	}

	public static boolean exportExcelForResponse(HttpServletResponse response,ExportParams exportParams,Collection<?> dataSet, Class<?> entityClass, String fileName){
		try {
			response.reset();
			response.setCharacterEncoding(Constants.DEFAULT_CHARSET);
			response.setHeader(HEADER_TYPE, HEADER_VALUE + fileName);
			response.setContentType(HEADER_CONTENT_TYPE);
			OutputStream out = response.getOutputStream();
			Workbook workbook = ExcelExportUtil.exportExcel(exportParams, entityClass, dataSet);
			workbook.write(out);
			out.close();
			logger.info("导出文件:" + fileName);
			return true;
		}catch (Exception e){
			logger.error("处理失败",e);
		}
		return false;
	}
}
