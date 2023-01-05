package com.maicard.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MsgVo extends BaseVo{

    private int code;


    public static MsgVo success(int code) {
        MsgVo msg = new MsgVo();
        msg.code = code;
        return msg;
    }
}
