package com.maicard.tx.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;

@Data
public class OrderStat extends BaseEntity {

    private long uuid;
    private int count;
    private String statTime;

    private long objectId;
    private String objectType;

    

}
