package com.maicard.core.service;


import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.GlobalUnique;
import com.maicard.core.annotation.IgnoreDs;

@IgnoreDs
public interface GlobalUniqueService {
	
	boolean create(GlobalUnique globalUnique);

	boolean exist(GlobalUnique globalUnique);

	void syncDbToDistributed() throws Exception;

	void syncDistributedToDb();

	int getDistributedCount();

	long plusDistributedCount(int count);

	int count(CriteriaMap params);

	int delete(GlobalUnique globalUnique);

	long incrOrderSequence(int count);

	@IgnoreDs
	long incrSequence(String prefix);



}
