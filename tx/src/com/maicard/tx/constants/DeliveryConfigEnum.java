package com.maicard.tx.constants;

public enum DeliveryConfigEnum {
    NO_DELIVERY_FEE, MEMBER_NO_DELIVERY_FEE, GLOBAL_FIX_DELIVERY_FEE, GLOBAL_COMPUTER_DELIVERY_FEE;

    public static DeliveryConfigEnum find(Object name) {
        if(name == null){
            return NO_DELIVERY_FEE;
        }
        for(DeliveryConfigEnum e : DeliveryConfigEnum.values()){
            if(e.name().equalsIgnoreCase(name.toString())){
                return e;
            }
        }
        return NO_DELIVERY_FEE;
    }
}
