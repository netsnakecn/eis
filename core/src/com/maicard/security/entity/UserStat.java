package com.maicard.security.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;

@Data
public class UserStat extends BaseEntity {

    public static final String COLLECT_USER_REGISTER_STAT = "USER_REGISTER_STAT";
    public static final String COLLECT_USER_ACTIVE_STAT = "USER_ACTIVE_STAT";
    public static final String STAT_HOUR_FORMAT = "yyyy-MM-dd HH";


    private int serverId;
    private long uuid;
    private int count;
    private String statTime;

}
