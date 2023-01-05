package com.maicard.mb.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.mb.entity.UserMessage;
 

public interface UserMessageMapper extends IDao<UserMessage> {


	int delete(String messageId) throws DataAccessException;


	List<String> getUniqueIdentify(CriteriaMap criteria);

	int deleteSubscribe(CriteriaMap userCriteriaMap);
		


}
