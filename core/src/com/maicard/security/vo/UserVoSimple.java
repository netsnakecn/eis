package com.maicard.security.vo;

import com.maicard.base.BaseVo;
import com.maicard.security.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserVoSimple extends BaseVo {

    protected  long uuid;
    protected  String username;
    protected  String nickName;
    protected  String avatar;
    protected String[] roles;
    protected String ssoToken;
    protected int userTypeId;


    public UserVoSimple(User user){
        BeanUtils.copyProperties(user,this);
        this.id = user.getUuid();
    }

}
