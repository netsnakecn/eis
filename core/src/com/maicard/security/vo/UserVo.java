package com.maicard.security.vo;

import com.maicard.base.BaseVo;
import com.maicard.security.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserVo extends UserVoSimple {

   // private int userExtraTypeId;
   // private int authType;
    private Date createTime;
   // private long parentUuid;
    private long level;
    private int member;
    private long inviter;
   // private int extraStatus;

    private String userPassword;

  //  private long headUuid; //分权限帐号的总账号
    private String authKey;
    private String memory;
    private int gender;
    private String avatar;
    private String email;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;


    private Date lastLoginTimestamp;
    private String lastLoginIp;


    public static UserVo from(User frontUser) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(frontUser,vo);
        vo.id = frontUser.getUuid();
        return vo;
    }
}
