package com.maicard.site.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.IDao;
import com.maicard.base.IService;
import com.maicard.site.entity.DocumentType;

public interface DocumentTypeService  extends IService<DocumentType> {

	DocumentType select(String documentTypeCode);


}
