package com.maicard.tx.constants;

import org.apache.commons.lang.StringUtils;

public enum StockMode {
    /**
     * 全局库存
     */
    GLOBAL,
    /**
     * 店铺库存
     */
    SHOP,

    /**
     * 先使用全局库存，不足再使用店铺库存
     */
    GLOBAL_FIRST,

    /**
     * 优先使用店铺库存，不足由全局补货
     */
    SHOP_FIRST,

    /**
     * 同时使用全局和店铺库存
     */
    BOTH;

    public static  StockMode find(String name){
        if(StringUtils.isBlank(name)){
            return GLOBAL;
        }
        for(StockMode stockMode : StockMode.values()){
            if(stockMode.name().equalsIgnoreCase(name)){
                return stockMode;
            }
        }
        return GLOBAL;
    }
}
