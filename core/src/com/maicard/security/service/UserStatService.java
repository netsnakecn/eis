package com.maicard.security.service;

import com.maicard.base.CriteriaMap;
import com.maicard.security.entity.UserStat;

import java.util.List;

public interface UserStatService {

    List<UserStat> stat(CriteriaMap criteriaMap);
}
