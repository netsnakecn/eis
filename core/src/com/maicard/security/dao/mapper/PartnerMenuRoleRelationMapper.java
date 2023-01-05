package com.maicard.security.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.MenuRoleRelation;


public interface PartnerMenuRoleRelationMapper extends IDao<MenuRoleRelation> {

	void deleteByGroupId(long groupId) throws DataAccessException;

}
