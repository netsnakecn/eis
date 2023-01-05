package com.maicard.security.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.maicard.core.constants.Constants;
import com.maicard.core.entity.BaseEntity;
import com.maicard.security.vo.UserVoSimple;
import lombok.Data;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 用户，后台系统用户、合作伙伴、前台网站用户都是用户类型
 * 用户对象只保存基本的数据
 * 其他所有需要的数据都由userData来保存
 * 
 * 
 * @author NetSnake
 * @date 2012-9-23
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class User  extends BaseEntity{//} implements ExtraDataAccess{

	@JsonIgnore
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@JsonIgnore
	public boolean isCacheable(){
		return true;
	}

	protected static final long serialVersionUID = -1L;


	public static final String FRONT_USER_PREFIX = "FRONT_USER";



	public static final int GENDER_BOY  = 1;	//男性
	public static final int GENDER_GIRL = 2;	//女性
	public static final int GENDER_OTHER = 3;	//不知道、变性人




	protected long uuid;

	@JsonIgnore
	protected int deleted = 0;

	protected int userTypeId;

	protected int userExtraTypeId;
	protected String username;
	protected String userPassword;
	protected int authType;

	@JsonFormat(pattern= Constants.DEFAULT_TIME_FORMAT)
	protected Date createTime;
	@JsonFormat(pattern= Constants.DEFAULT_TIME_FORMAT)
	protected Date lastLoginTimestamp;
	protected String lastLoginIp;
	protected String nickName;

	protected long parentUuid;
	protected long level;

	protected int member;


	protected long inviter;
	protected int extraStatus;

	protected long headUuid; //分权限帐号的总账号
	protected String authKey;
	protected String memory;
	protected int gender;

	@JsonIgnore
	protected int lockStatus;


	protected String avatar;

	protected String email;

	protected String phone;

	protected String country;

	protected String province;

	protected String city;

	protected String district;

	protected String street;


	//非持久化属性
	protected String ssoToken;
	//完成二次验证的时间

	protected String dataMode;

	//该用户拥有的角色名字，或前端用户的角色、类型
	protected String[] roles;


	//@JsonSerialize(using = ExtraDataMapSerializer.class)
	//@JsonIgnore
	//protected Map<String,ExtraData> userData;
	protected List<Role> relatedRoleList;
	protected List<Privilege> relatedPrivilegeList;

	//所有子孙账户
	protected List<UserVoSimple> belowUser;
	
	



	public User(){

	}

	public User(long ownerId) {
		this.ownerId = ownerId;
	}

	public User(int type, long uuid, long ownerId) {
		this.userTypeId = type;
		this.uuid = uuid;
		this.ownerId = ownerId;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final User other = (User) obj;
		if (uuid != other.uuid)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
				"(" + 
				"userTypeId='" + userTypeId + "'" +
				"uuid=" + "'" + uuid + "'" + 
				")";
	} 
	public void setUsername(String username) {
		if(username != null && username.trim() != "")
			this.username = username.trim();
	}

	@Override
	public User clone() {
		 return (User)super.clone();
	}




	public void setId(long id){
		this.uuid = id;
	}

	public long getId(){
		return uuid;
	}

	@JsonIgnore
    public boolean haveRole(String role) {
		return ArrayUtils.contains(this.roles,role);
    }
}
