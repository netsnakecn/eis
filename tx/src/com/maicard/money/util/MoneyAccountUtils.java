package com.maicard.money.util;

import com.maicard.core.constants.EisError;
import com.maicard.core.exception.EisException;
import com.maicard.money.constants.AccountTypeEnum;
import com.maicard.utils.NumericUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MoneyAccountUtils {



    public static int getByUuid(long uuid) {
        String customerNo = String.valueOf(uuid);
        if(customerNo.length() < 5) {
            throw new EisException(EisError.ACCOUNT_ERROR.id,"不规范的UUID:" + uuid);
        }
        customerNo = customerNo.substring(2,4);
        return NumericUtils.parseInt(customerNo);
    }

    public static String getAccountNo(AccountTypeEnum accountTypeEnum, long uuid){
        StringBuffer accountNo = new StringBuffer();
        if (uuid < 1){
            System.out.println("无法根据用户UUID:" + uuid + "获取资金账号");
            return null;
        }

        int memberType = getByUuid(uuid);
        log.info("准备根据用户:" + uuid + ",类型:" + memberType + "生成资金账户");
        String customerNo = String.valueOf(uuid);
        accountNo.append(customerNo).append(accountTypeEnum.code);
        return accountNo.toString();

    }



}
