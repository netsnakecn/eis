package com.maicard.money.vo;

import com.maicard.money.entity.Price;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class PriceVo {
    public List<PriceVo> sub = new ArrayList<PriceVo>();

    public long id;
    public String priceName;
    public float money;

    public List<PriceVo> getSub() {
        return sub;
    }

    public void setSub(List<PriceVo> sub) {
        this.sub = sub;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPriceName() {
        return priceName;
    }

    public void setPriceName(String priceName) {
        this.priceName = priceName;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public static PriceVo of(Price p) {
        PriceVo vo = new PriceVo();
        BeanUtils.copyProperties(p,vo);
        return vo;
    }
}
