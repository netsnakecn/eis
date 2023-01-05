package com.maicard.security.service;

import java.util.Map;

import com.maicard.base.CriteriaMap;

public interface UserExtraTypeService {
	public Map<Long,String> getUserExtraTypes(CriteriaMap criteria);
}
