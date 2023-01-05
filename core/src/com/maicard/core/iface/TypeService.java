package com.maicard.core.iface;

import java.util.List;
import java.util.Map;

import com.maicard.base.CriteriaMap;
import com.maicard.base.Pair;

public interface TypeService {
	
	
	@SuppressWarnings("rawtypes")
	List<Pair> typeList(CriteriaMap criteria);
	
	String getObjectTypeName();

	Map<Integer, String> initExtraTypes();
	
	
}
