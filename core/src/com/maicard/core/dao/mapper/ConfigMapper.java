package com.maicard.core.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.core.entity.Config;

import javax.validation.constraints.NotNull;

public interface ConfigMapper extends IDao<Config> {


	List<Config> listOnPage(CriteriaMap params) throws DataAccessException;
	

	Config selectByName( @NotNull CriteriaMap params);

}
