package com.maicard.site.service;

import java.util.List;
import javax.jms.ObjectMessage;
import javax.print.Doc;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.site.entity.Document;


public interface DocumentService extends GlobalSyncService<Document> {
		


	int changeStatus(long udid, int status) throws Exception;
		

	boolean postProcess(ObjectMessage objectMessage) throws Exception;

	Document select(String documentCode, long ownerId);



    /**
	 * 在es中删除指定的索引
	 * 
	 * 
	 * @author GHOST
	 */
	void deleteIndex(long id);

	/**
	 * 在es中创建索引
	 * 
	 * 
	 * @author GHOST
	 */
	void createIndex(IndexableEntity document);

	void afterFetch(Document document);


	/**
	 * 将文档扩展数据中的文件路径修改为带下载前缀的URL绝对路径以供显示
	 * @param document
	 */
	//void processDataPath(Document document);
}
