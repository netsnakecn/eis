package com.maicard.security.service.impl;

import com.maicard.base.CriteriaMap;
import com.maicard.base.ImplNameTranslate;
import com.maicard.core.constants.*;
import com.maicard.core.entity.ExtraData;
import com.maicard.core.entity.GlobalUnique;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.core.service.abs.AbsGlobalSyncService;
import com.maicard.mb.service.MessageService;
import com.maicard.security.constants.UserStatus;
import com.maicard.security.dao.mapper.FrontUserMapper;
import com.maicard.security.entity.User;
import com.maicard.security.service.FrontUserService;
import com.maicard.utils.ClassUtils;
import com.maicard.utils.SecurityUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class FrontUserServiceImpl extends AbsGlobalSyncService<User, FrontUserMapper> implements FrontUserService {

    @Resource
    private ApplicationContextService applicationContextService;
    @Resource
    private ConfigService configService;
    @Resource
    private GlobalUniqueService globalUniqueService;
    @Resource
    private MessageService messageService;

    @Resource
    private DataDefineService dataDefineService;





    @Override
    public int insertAsync(User frontUser) {
         if (globalUniqueService.exist(new GlobalUnique(frontUser.getUsername(), frontUser.getOwnerId()))) {
            logger.error("无法创建前台用户，因为全局数据不唯一[" + frontUser.getUsername() + "]");
            return EisError.dataUniqueConflict.id;
        }
        if(frontUser.getCurrentStatus() == 0){
            frontUser.setCurrentStatus(UserStatus.normal.id);
        }
        if(frontUser.getUuid() <= 0){
            frontUser.setUuid(globalUniqueService.incrSequence(ClassUtils.getEntityType(User.class)));
        }
        if(!isMqEnabled()){
            return insert(frontUser);
        }
        messageService.sendJmsDataSyncMessage(messageBusEnum.name(), ImplNameTranslate.translate(getClass().getSimpleName()),"insertSync",frontUser);
        return 1;
    }

    @Override
    public int insertSync(User entity) {
       /* if (globalUniqueService.exist(new GlobalUnique(entity.getUsername(), entity.getOwnerId()))) {
            logger.error("无法创建前台用户，因为全局数据不唯一[" + entity.getUsername() + "]");
            return EisError.dataUniqueConflict.id;
        }*/
        boolean HANDLE_USER = configService.getBoolProperty(HandlerEnum.HANDLE_USER_REGISTER.name());
        if(!HANDLE_USER){
            return EisError.NOT_HANDLE_NODE.id;
        }
        if (!globalUniqueService.create(new GlobalUnique(entity.getUsername(), entity.getOwnerId()))) {
            logger.warn("全局唯一数据[" + entity.getUsername() + "]不唯一");
            return EisError.dataUniqueConflict.id;
        }

        int rs = insert(entity);
        if(rs == 1 && isMqEnabled()){
            if(entity.isCacheable())            putCache(entity);
            entity.setSyncFlag(0);
            messageService.sendJmsDataSyncMessage(messageBusEnum.name(),ImplNameTranslate.translate(getClass().getSimpleName()),"insert",entity);
        }
        return rs;
    }
    /*@Override
    public void registerLocal(UserDto userDto) {
        String handlerRegister = configService.getProperty(HandlerEnum.HANDLE_USER_REGISTER.name());
        if (handlerRegister == null || !handlerRegister.equalsIgnoreCase("1")) {
            logger.debug("本节点不负责注册账户");
            return;
        }
        logger.info("开始注册新用户:" + JsonUtils.toStringFull(userDto));
        User frontUser = new User();
        BeanUtils.copyProperties(userDto, frontUser);
        frontUser.setUserTypeId(UserTypes.frontUser.id);
        frontUser.setCreateTime(new Date());
        frontUser.setUserPassword(SecurityUtils.correctPassword(userDto.getUserPassword()));
        int rs = 0;
        try {
            rs = this.insertLocal(frontUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rs != 1) {
            logger.error("无法创建本地用户:" + JsonUtils.toStringFull(frontUser));
            return;
        }
        logger.info("创建新用户成功,发送同步请求");
        //创建成功，则发送JMS同步请求
        messageService.sendJmsDataSyncMessage(null, "frontUserService", "insertLocal", frontUser);

        //如果本机负责注册，则进行注册工作，否则忽略

    }*/



    @Override
    public int insert(User frontUser)  {
        if (frontUser == null) {
            return -1;
        }
        if (frontUser == null) {
            return -1;
        }
        //查询是否存在
        if (frontUser.getUuid() > 0) {
            User exist = select(frontUser.getUuid());
            if (exist != null) {
                logger.info("本地已存在用户:" + frontUser.getUuid());
                return -1;
            }
        }

        Date startTime = new Date();
        frontUser.setUserTypeId(UserTypes.frontUser.id);
        String inviteByCode = frontUser.getExtra(DataName.userInviteByCode.toString());
        if (StringUtils.isNotBlank(inviteByCode)) {
            inviteByCode = inviteByCode.trim();
            CriteriaMap userDataCriteria = new CriteriaMap();
            userDataCriteria = new CriteriaMap();
            userDataCriteria.put("userTypeId", UserTypes.partner.id);
            userDataCriteria.put("dataCode", DataName.userInviteCode.toString());
            userDataCriteria.put("dataValue", inviteByCode);
            List<ExtraData> userDataList = null;//userDataService.list(userDataCriteria);
            if (userDataList != null && userDataList.size() == 1) {
                logger.info("根据邀请码[" + inviteByCode + "]查找对应的推广ID:" + userDataList.get(0).getUuid());
                long inviteUuid = userDataList.get(0).getUuid();
                frontUser.setInviter(inviteUuid);
            } else {
                logger.info("根据邀请码[" + inviteByCode + "]找不到对应的推广ID.");
            }
        }
        frontUser.setCreateTime(new Date());
        if (frontUser.getUserPassword().length() != 64) {//防止二次加密
            //将密码改为加密形式
            //String plainPassword = frontUser.getUserPassword();
            frontUser.setUserPassword(SecurityUtils.passwordEncode(frontUser.getUserPassword()));
            //logger.info("新增用户密码是:" + plainPassword + "/" + frontUser.getUserPassword());
        }
		/*for(int i = 0; i < dataWriteMaxWaitingCount; i++){
			int waitingCount = applicationContextService.getDatabaseWaiting(DataSource.user.toString());
			int activeCount = applicationContextService.getDatabaseActive(DataSource.user.toString());
			if(logger.isDebugEnabled()){
				logger.debug("在写入数据前检查数据库状态[等待数:" + waitingCount + ",系统禁止写入的最大等待数:" + dataWriteIdleForWaitingCount + ",活动连接:" + activeCount + ",系统禁止写入的最大活动数:" + dataWriteIdelForActiveCount + ",当前等待次数:" + (i+1) + ",系统定义的最大等待次数:" + dataWriteMaxWaitingCount);
			}
			if(waitingCount <= dataWriteIdleForWaitingCount){
				if(activeCount <= dataWriteIdelForActiveCount) {
					break;
				}
			}
			try {
				Thread.sleep(dataWriteIdelIntervalMs + (i*1000));
			} catch (InterruptedException e) {}

		}*/
        if (frontUser.getUuid() < 1) {
            //获取本地UUID
            long newUuid =         globalUniqueService.incrSequence(ObjectType.frontUser.name());
            frontUser.setUuid(newUuid);
        }


        int rs = 0;
        try {
            rs = mapper.insert(frontUser);
        } catch (Exception e) {
            logger.error("无法创建新用户:" + e.getMessage());
            return 0;
        }
        if (rs != 1) {
            logger.error("无法创建新用户,数据操作未返回1[" + rs + "]");
        }
        long mainInsertTime = (new Date().getTime() - startTime.getTime());

        CriteriaMap userDataCriteria = new CriteriaMap();
        userDataCriteria.put("uuid", frontUser.getUuid());
/*        if (frontUser.getUserData() != null) {
            CriteriaMap dataDefineCriteria = new CriteriaMap();
            dataDefineCriteria.put("objectType", ObjectType.user.name());
            dataDefineCriteria.put("objectId", UserTypes.frontUser.id);
            DataDefine dd = null;
            for (String key : frontUser.getUserData().keySet()) {
                dataDefineCriteria.put("dataCode", key);
                dd = dataDefineService.selectOne(dataDefineCriteria);
                if (dd == null) {
                    logger.error("找不到[" + key + "]的数据定义，无法完成用户扩展数据插入:" + key + "=" + frontUser.getUserData().get(key).getDataValue());
                    continue;
                }
                ExtraData userData = new ExtraData();
				*//*if(frontUser.getUserConfigMap().get(key) != null && frontUser.getUserConfigMap().get("key").getExtraDataId() > 0){
					userData.setExtraDataId(frontUser.getUserConfigMap().get("key").getExtraDataId());
				}*//*
                if (frontUser.getUserData().get(key).getId() > 0) {
                    userData.setId(frontUser.getUserData().get(key).getId());
                }
                userData.setObjectId(frontUser.getUuid());
                userData.setDataDefineId(dd.getId());
                userData.setDataCode(key);
                userData.setDataValue(frontUser.getUserData().get(key).getDataValue());
                //userData.setCurrentStatus(dd.getCurrentStatus());
                userDataService.insert(userData);
            }

            dataDefineCriteria = null;
            dd = null;
        }*/
        userDataCriteria = null;
        long totalTime = (new Date().getTime() - startTime.getTime() - mainInsertTime);

        logger.debug("成功创建新用户[" + frontUser.getUuid() + "]，耗时" + mainInsertTime + "毫秒，创建扩展数据耗时:" + totalTime + "毫秒");
        return rs;

    }


    @Override
    public int deleteLocal(User entity){
        globalUniqueService.delete(new GlobalUnique(entity.getUsername(), entity.getOwnerId()));
         return super.deleteLocal(entity);
    }




    public User login(CriteriaMap frontCriteriaMap) {

        String username = frontCriteriaMap.getStringValue("uername");
        if (StringUtils.isBlank(username)) {
            return null;
        }
        String password = frontCriteriaMap.getStringValue("userPassword");
        if (StringUtils.isBlank(password)) {
            return null;
        }
        frontCriteriaMap.put("userTypeId", UserTypes.frontUser.id);
        //将密码改为加密形式
        //logger.info("加密后的密码：" + Crypt.passwordEncode(frontCriteriaMap.getUserPassword()));
        frontCriteriaMap.put("userPassword", password);
        //强制设置为返回正常状态的用户
        frontCriteriaMap.put("currentStatus", new int[]{UserStatus.normal.id});
        List<User> frontUserList = list(frontCriteriaMap);
        if (frontUserList == null) {
            return null;
        }
        if (frontUserList.size() != 1) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("前台用户[" + frontUserList.get(0).getUuid() + "/" + frontUserList.get(0).getUsername() + "]成功登录");
        }
        return frontUserList.get(0);

    }

/*
    @Override
    @CacheEvict(value = cacheName, key = "'FrontUser#' + #frontUser.uuid")
    public EisMessage update(User frontUser) throws Exception {
        if (frontUser == null) {
            throw new EisException(EisError.OBJECT_IS_NULL.id, "更新的前台用户是空");
        }
        if (frontUser.getUuid() < 1) {
            throw new EisException(EisError.PARAMETER_ERROR.id, "更新的前台用户UUID=0");
        }
        frontUser.setUserTypeId(UserTypes.frontUser.id);
        int rs = updateLocal(frontUser);
        if (rs == 1) {
            return EisMessage.success(OpResult.success.getId(), "更新成功");
        }
        return EisMessage.error(rs, "更新用户[" + frontUser.getUuid() + "]失败");


    }*/

/*

    @Override
    @CacheEvict(value = cacheName, key = "'FrontUser#' + #frontUser.uuid")
    public void updateSync(User frontUser) throws Exception {
        logger.info("本地更新用户:" + frontUser.getUuid() + "，并通知总线");
        this.updateLocal(frontUser);
        messageService.sendJmsDataSyncMessage(null, "frontUserService", "updateLocal", frontUser);
    }
*/


   /* @Override
    @CacheEvict(value = cacheName, key = "'FrontUser#' + #frontUser.uuid")
    public EisMessage delete(User frontUser) {
        if (frontUser == null) {
            throw new EisException(EisError.OBJECT_IS_NULL.id, "前台对象为空");
        }
        String username = null;
        if (frontUser.getUuid() > 0 && StringUtils.isBlank(frontUser.getUsername())) {
            User _oldUser = frontUserMapper.select(frontUser.getUuid());
            if (_oldUser == null) {
                logger.error("根据UUID[" + frontUser.getUuid() + "]找不到前端用户");
                return EisMessage.error(OpResult.failed.getId(), "更新用户[" + frontUser.getUuid() + "]失败");
            }
            username = _oldUser.getUsername();
        } else {
            username = frontUser.getUsername();
        }
        int rs = frontUserMapper.delete(frontUser.getUuid());
        if (rs == 1) {
            globalUniqueService.delete(new GlobalUnique(username, frontUser.getOwnerId()));
            return EisMessage.success(OpResult.success.getId(), "成功删除用户[" + frontUser.getUuid() + "]");
        } else {
            return EisMessage.error(OpResult.failed.getId(), "删除用户[" + frontUser.getUuid() + "]失败");
        }
    }*/

/*
    @Override
    @Cacheable(value = cacheName, key = "'FrontUser#' + #uuid")
    public User select(long uuid) {
        if (logger.isInfoEnabled()) {
            logger.info("从数据库选择用户[" + uuid + "]");
        }
        User frontUser = frontUserMapper.select(uuid);
        if (frontUser == null) {
            logger.debug("找不到用户[" + uuid + "]");
            return null;
        }

        afterFetch(frontUser);

        return frontUser;
    }*/

    public void afterFetch(User frontUser) {
        if (frontUser == null) {
            return;
        }
        //frontUser.setId(frontUser.getUuid());
        if (frontUser.getNickName() == null || frontUser.getNickName().equals("")) {
            frontUser.setNickName(frontUser.getUsername());
        }

        CriteriaMap userDataCriteria = new CriteriaMap();
        userDataCriteria.put("objectId", frontUser.getUuid());
       // frontUser.setUserData(userDataService.map(userDataCriteria));

		/*String headPic = frontUser.getExtraValue(DataName.userHeadPic.toString());
		if(headPic != null){
			if(headPic.startsWith("/") || headPic.indexOf("://") > 0){
				;
			} else {
				String userUploadDir = configService.getValue(DataName.userHeadPic.toString(), frontUser.getOwnerId());
				if(userUploadDir != null){
					headPic = userUploadDir.replaceAll(File.pathSeparator, "") + File.pathSeparator + headPic;
					frontUser.getUserConfigMap().get(DataName.userHeadPic.toString()).setDataValue(headPic);
				}
			}
		}*/
        //frontUser.setUserLevelProject(userLevelProjectService.selectByLevel(frontUser.getLevel()));
        //userDynamicDataService.applyToUser(frontUser);

    }

    //更新本地数据库
//    @Override
//    public int updateLocal(User frontUser) throws Exception {
//        //FIXME
//		/*if(frontUser.getEmail() == null || frontUser.getEmail().equals("")){
//				frontUser.setEmail(frontUser.getUsername());
//			}*/
//        //  logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!,"+frontUser.getUuid()+",!!!!"+ frontUser.getUserConfigMap().get("userBindMailBox").getDataValue()+"!!!!");
//        frontUser.setUserTypeId(UserTypes.frontUser.id);
//
//        long uuid = frontUser.getUuid();
//
//        User _oldFrontUser = frontUserMapper.select(uuid);
//        if (_oldFrontUser == null) {
//            logger.error("找不到本地用户:" + uuid);
//            return 0;
//        }
//        if (_oldFrontUser.getVersion() > frontUser.getVersion()) {
//            logger.error("本地版本:" + _oldFrontUser.getVersion() + "比更新版本:" + frontUser.getVersion() + "高");
//            return 0;
//        }
//
//        if (frontUser.getUserPassword() == null || frontUser.getUserPassword().equals("")) {
//            //未修改密码
//            frontUser.setUserPassword(_oldFrontUser.getUserPassword());
//        } else {
//            //与原密码不一致
//            if (!frontUser.getUserPassword().equals(_oldFrontUser.getUserPassword())) {
//                if (frontUser.getUserPassword().length() != 64) {//防止二次加密
//                    //将密码改为加密形式
//                    //String plainPassword = frontUser.getUserPassword();
//                    frontUser.setUserPassword(SecurityUtils.passwordEncode(frontUser.getUserPassword()));
//                }
//            }
//        }
//        //logger.info("更新用户，密码是:" + frontUser.getUserPassword());
//        try {
//            frontUserMapper.update(frontUser);
//
//        } catch (Exception e) {
//            logger.error("更新用户失败:" + e.getMessage());
//            if (e.getMessage() != null && e.getMessage().indexOf("Duplicate entry") > 0) {
//                return -EisError.dataDuplicate.id;
//            }
//            return -1;
//        }
//
//
//        //更新用户角色
//		/*frontUserRoleRelationService.deleteByFuuid(fuuid);
//			if(frontUser.getRelatedRoleList() != null){
//				List<Role> relatedFrontRoleList = frontUser.getRelatedRoleList();
//				for(int i = 0; i < relatedFrontRoleList.size(); i++){
//					UserRoleRelation userRoleRelation = new  UserRoleRelation();
//					userRoleRelation.setCurrentStatus(Constants.BasicStatus.normal.getId());
//					userRoleRelation.setUuid(fuuid);
//					userRoleRelation.setRoleId(relatedFrontRoleList.get(i).getRoleId());
//					frontUserRoleRelationService.insert(userRoleRelation);
//				}
//			} else {
//				logger.warn("修改的前台用户未包含对应角色" + fuuid);
//			}*/
//
//        //如果当前是用户更新模式，且当前不存在任何普通配置，将不进行任何更新以防止误删除
//		/*if(frontUser.getUpdateMode().equals(Constants.InputLevel.user.toString())){
//				if(frontUser.getUserConfigMap() != null && frontUser.getUserConfigMap().size() > 0){
//					int normalConfigCount = 0;
//					for(String key : frontUser.getUserConfigMap().keySet()){
//						if(frontUser.getUserConfigMap().get(key).getCurrentStatus() == Constants.BasicStatus.normal.getId()){
//							normalConfigCount++;
//						}
//					}
//					//只有当提交配置中有至少一个普通配置时才进行更新
//					if(normalConfigCount > 0){
//						userDataService.processUserConfig(frontUser);
//					}
//				}
//			} else {
//				userDataService.processUserConfig(frontUser);
//			}*/
//        userDataService.processUserConfig(frontUser);
//
//        //sendMessage(Constants.operateEdit, frontUser);
//        return 1;
//    }


}

