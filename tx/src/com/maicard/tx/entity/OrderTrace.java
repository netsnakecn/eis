package com.maicard.tx.entity;

import com.maicard.core.entity.BaseEntity;
import lombok.Data;

import java.util.Date;

@Data
public class OrderTrace extends BaseEntity {

    private long orderId;

    private Date createTime;

    private Date finishTime;

    private String operator;

    private String operateType;

    private String content;



}
