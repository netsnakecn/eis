package com.maicard.money.vo;

import lombok.Data;

import java.util.Date;

@Data
public class DeliveryTraceVo {

    public int index;
    public Date time;
    public String desc;

}
