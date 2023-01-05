package com.maicard.money.constants;


/**
 * 付款卡类型
 * 
 * 
 * @author: GHOST
 * @date 2019-01-16
 */
public enum  PayCardTypeEnum {
    UNKNOWN("UN", "未知类型", (String)null),
    BANK_ACCOUNT("AC", "银行账户", "BACK_ACCOUNT"),
    DEBIT("DE", "储蓄卡", "DEBIT_CARD"),
    CREDIT("CR", "信用卡", "CREDIT_CARD"),
    QUASI_CREDIT("QC", "准贷记卡", "SEMI_CREDIT_CARD"),
    PREPAID("PR", "预付卡", "PREPAID_CARD"),
    OVERSEAS_DEBIT("OD", "境外储蓄卡", "OVERSEAS_DEBIT_CARD"),
    OVERSEAS_CREDIT("OC", "境外信用卡", "OVERSEAS_CREDIT_CARD");



    final   private String code;
    final private String cnName;
    final private String channelCode;

    public static PayCardTypeEnum getEnum(String code) {
        if(null == code) {
            return null;
        } else {
            PayCardTypeEnum[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                PayCardTypeEnum c = arr$[i$];
                if(code.equals(c.code)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("No enum code \'" + code + "\'. " + PayCardTypeEnum.class);
        }
    }

    public static PayCardTypeEnum getCardTypeByChannelCode(String channelCode) {
        PayCardTypeEnum[] arr$ = values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            PayCardTypeEnum c = arr$[i$];
            if(channelCode.equals(c.channelCode)) {
                return c;
            }
        }

        return null;
    }

    private PayCardTypeEnum(String code, String cnName, String channelCode) {
        this.code = code;
        this.cnName = cnName;
        this.channelCode = channelCode;
    }

    public String getCode() {
        return this.code;
    }

    public String getCnName() {
        return this.cnName;
    }

    public String getChannelCode() {
        return this.channelCode;
    }
}
