package com.maicard.money.constants;

/**
 * @author: iron
 * @description: 账户类型
 * @date : created in  2017/12/21 14:06.
 */
public enum  AccountTypeEnum {
    CONSUMER_BASIC_ACCOUNT("0001", "个人基本账户"),
    CONSUMER_COUPON_ACCOUNT("0002", "个人点券账户"),
    CONSUMER_SCORE_ACCOUNT("0003", "个人积分账户"),

    RESELLER_COMMISSION_ACCOUNT("1001","经销商佣金户"),

    CHANNEL_TRADE_PAYABLE("8001", "通道交易应付账户"),
    CHANNEL_TRADE_RECEIVABLE("8002", "通道交易应收账户"),
    PAYMENT_FEE_EXPENS("8858", "手续费支出"),
    COLLECTION_FEE_INCOME("8859", "手续费收入");

    public String code = null;
    public String desc = null;


    public static AccountTypeEnum getByName(String name) {
        if(null == name) {
            return null;
        } else {
            AccountTypeEnum[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                AccountTypeEnum a = arr$[i$];
                if(a.name().equalsIgnoreCase(name.trim())) {
                    return a;
                }
            }

            return null;
        }
    }

    public static AccountTypeEnum getEnum(String code) {
        if(null == code) {
            return null;
        } else {
            AccountTypeEnum[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                AccountTypeEnum a = arr$[i$];
                if(a.code.equals(code)) {
                    return a;
                }
            }

            return null;
        }
    }

    private AccountTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}
