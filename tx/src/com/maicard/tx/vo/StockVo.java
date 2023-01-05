package com.maicard.tx.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockVo extends BaseEntity {


    public float available;
    public float lockAmount;

    public long objectId;
    public String objectType;

    public String shopId;

    public StockVo clone(){
        return (StockVo)super.clone();
    }

    public StockVo(){
    }

    public StockVo(String t){
        objectType = t;
    }
    public StockVo(String t, long id){
        objectType = t;objectId =id;
    }

    public StockVo(float lockAmount, String stockType, String shopId, long productId, long ownerId) {
        this.lockAmount = lockAmount;
        this.objectType = stockType;
        this.shopId = shopId;
        this.objectId = productId;
        this.ownerId = ownerId;
    }

    public void amount(int availableStock, int lockedStock) {
        this.available = availableStock;
        this.lockAmount = lockedStock;

    }
}
