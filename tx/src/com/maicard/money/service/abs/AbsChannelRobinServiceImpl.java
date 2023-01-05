package com.maicard.money.service.abs;
 
import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.BasicStatus;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.UserTypes;
import com.maicard.core.service.CenterDataService;
import com.maicard.core.service.ConfigService;
import com.maicard.money.entity.Pay;
import com.maicard.money.entity.PayMethod;
import com.maicard.money.service.ChannelRobinService;
import com.maicard.money.service.PayMethodService;
import com.maicard.security.entity.User;
import com.maicard.utils.HttpUtils;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.StringTools;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

 
@Service
public class AbsChannelRobinServiceImpl extends BaseService implements ChannelRobinService {
	@Resource
	private CenterDataService centerDataService;


	@Resource
	private PayMethodService payMethodService;


	@Resource
	private ConfigService configService;
	/**
	 * 均衡类型-商户
	 */
	public static final String ROBIN_TYPE_MERCHANT = "merchant";
	/**
	 * 均衡类型-全局
	 */
	public static final String ROBIN_TYPE_GLOBAL = "global";

	/**
	 * FIFO，使用那个最早用过的，最近刚用过的往后排
	 */
	public static final String ROBIN_TYPE_FIFO = "fifo";

	/**
	 * 默认超时30天
	 */
	private static final long ttl = 3600 * 24 * 30;


	@SuppressWarnings("unchecked")
	@Override
	public PayMethod getPayMethod(Pay pay, User partner) {
		
		if(pay.getPayMethodId() > 0){
			logger.debug("支付订单[" + pay.getTransactionId() + "]指定了payMethodId:" + pay.getPayMethodId() + ",返回该支付方式");
			return payMethodService.select(pay.getPayMethodId());
		}

		boolean internal = pay.getBooleanExtra("internal");

		/* 根据payType的ID和contextType来确定哪一种payMethod */
		CriteriaMap payMethodCriteria =  CriteriaMap.create(pay.getOwnerId());
		payMethodCriteria.put("payTypeId",pay.getPayTypeId());
		if(StringUtils.isNotBlank(pay.getContextType())){
			payMethodCriteria.put("contextType",pay.getContextType());
		}

		if(internal){
			payMethodCriteria.putArray("currentStatus",BasicStatus.hidden.getId());
		} else {
			payMethodCriteria.putArray("currentStatus",BasicStatus.normal.getId());
		}

		int[] excludePayMethods = null;
		String exclude = pay.getExtra("excludePayMethod");
		if(StringUtils.isNotBlank(exclude)){
			String[] data =  exclude.split(",");
			excludePayMethods = new int[data.length];
			for(int i = 0; i < data.length; i++){
				excludePayMethods[i] = Integer.parseInt(data[i].trim());
			}
		}
		List<PayMethod> payMethodList = payMethodService.list(payMethodCriteria);
		if(payMethodList == null || payMethodList.size() < 1){
			logger.error("根据[payTypeId:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + ",ownerId=" + payMethodCriteria.getLongValue("ownerId") + "]找不到任何支付方式");
			return null;
		}

		//该商户是否只允许从指定通道channel走
		boolean limitPayToChannelOnly = false;
		//该商户是否有优先选择的支付通道，这个channel不是withdrawMethodId，是channelId
		int perferChannelId = 0;
		if(pay.getPayFromAccountType() == UserTypes.partner.id && partner != null){
			limitPayToChannelOnly = partner.getBooleanExtra("limitPayToChannelOnly");
			perferChannelId = (int)partner.getLongExtra("perferChannelId");
		}


		logger.info("检查支付订单:" + pay.getTransactionId() + ",商户的绑定支付通道是:" + perferChannelId + ",限制为只能在此channel通道支付:" + limitPayToChannelOnly);
		if(payMethodList.size() == 1){
			PayMethod onlyPayMethod = payMethodList.get(0);
			if(excludePayMethods != null && excludePayMethods.length > 0){
				for(int ex2 : excludePayMethods){
					if(onlyPayMethod.getId() == ex2){
						logger.warn("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，但该支付方式[" + onlyPayMethod + "]被参数excludePayMethod设置为跳过");
						return null;
					}
				}
			}
			if(limitPayToChannelOnly && perferChannelId > 0 && onlyPayMethod.getPayChannelId() == perferChannelId){
				logger.warn("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，但商户[" + pay.getPayFromAccount() + "]被限定只能使用channelId=" + perferChannelId + "的支付方式，该支付方式不可使用");
				return null;
			}
			logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，直接返回第一个支付方式:" + payMethodList.get(0));
			return onlyPayMethod;
		} 
		List<PayMethod> payMethodList2 = new ArrayList<PayMethod>();

		//在最后确定可以使用的通道中，最高的优先级是多少
		int highestWeight = 0;

		String hour = new SimpleDateFormat("HH").format(new Date());
		String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
		Map<String,String> requestMap = null;
		if(pay.getParameter() != null){
			try{
				requestMap = (Map<String,String>)pay.getParameter();
			}catch(Exception e){
				e.printStackTrace();
			}
		} 
		if(requestMap == null || requestMap.size() < 1){
			logger.info("当前支付对象中没有设置Map类型的parameter");
		}
		boolean wapDirect = HttpUtils.getBooleanValueFromRequestMap(requestMap, "wapDirect");
		for(PayMethod payMethod: payMethodList){			
			//	if(payMethod.getWeight() == highestWeight){
			//判断这一个小时的成功金额是否已经超限
			String hourSuccessKey = "PayMethod#SuccessMoney#" + hour + "#" + payMethod.getId();
			float hourSuccessMoney = NumericUtils.parseFloat(centerDataService.get(hourSuccessKey));
			float maxSuccessMoneyInHour = payMethod.getFloatExtra("maxSuccessMoneyInHour");
			logger.debug("支付方式[" + payMethod.getId() + "]本小时成功金额" + hourSuccessMoney + "+订单[" + pay.getTransactionId() + "]金额:" + pay.getFaceMoney() + ",最大限定金额是:" + maxSuccessMoneyInHour + "");

			if(hourSuccessMoney > 0 && maxSuccessMoneyInHour > 0 && (hourSuccessMoney + pay.getFaceMoney()) >= maxSuccessMoneyInHour){
				logger.info("支付方式[" + pay.getPayMethodId() + "]本小时成功金额" + hourSuccessMoney + "+订单金额" + pay.getFaceMoney() + ",大于它被限定的最大金额:" + maxSuccessMoneyInHour + ",不再使用");
				continue;
			}

			String daySuccessKey = "PayMethod#SuccessMoney#" + day + "#" + payMethod.getId();
			float daySuccessMoney = NumericUtils.parseFloat(centerDataService.get(daySuccessKey));
			float maxSuccessMoneyInDay = payMethod.getFloatExtra("maxSuccessMoneyInDay");
			logger.debug("支付方式[" + payMethod.getId() + "]当天成功金额" + daySuccessMoney + "+订单[" + pay.getTransactionId() + "]金额:" + pay.getFaceMoney() + ",最大限定金额是:" + maxSuccessMoneyInDay + "");

			if(daySuccessMoney > 0 && maxSuccessMoneyInDay > 0 && (daySuccessMoney + pay.getFaceMoney()) >= maxSuccessMoneyInDay){
				logger.info("支付方式[" + pay.getPayMethodId() + "]当天成功金额" + daySuccessMoney + "+订单金额" + pay.getFaceMoney() + ",大于它被限定的最大金额:" + maxSuccessMoneyInDay + ",不再使用");
				continue;
			}


			float minPayMoney = payMethod.getFloatExtra("minPayMoney");
			if(minPayMoney > 0 && pay.getFaceMoney() < minPayMoney){
				logger.info("当前订单支付金额:" + pay.getFaceMoney() + ",小于当前支付方式[" + payMethod.getId() + "]的最小订单金额:" + minPayMoney + ",不再使用");
				continue;
			}
			boolean noWapDirect = payMethod.getBooleanExtra("noWapDirect");
			if(noWapDirect && wapDirect){
				logger.info("当前订单要求使用WAP直接跳转，但当前支付方式[" + payMethod.getId() + "]不支持该方式，不再使用");
				continue;
			}
			float maxPayMoney = payMethod.getFloatExtra("maxPayMoney");
			if(maxPayMoney > 0 && pay.getFaceMoney() > maxPayMoney){
				logger.info("当前订单支付金额:" + pay.getFaceMoney() + ",大于当前支付方式[" + payMethod.getId() + "]的最大订单金额:" + minPayMoney + ",不再使用");
				continue;
			}
			if(excludePayMethods != null && excludePayMethods.length > 0){
				boolean shouldExclude =false;
				for(int ex2 : excludePayMethods){
					if(payMethod.getId() == ex2){
						logger.debug("该支付方式[" + payMethod + "]被参数excludePayMethod设置为跳过");
						shouldExclude = true;
						break;
					}
				}
				if(shouldExclude){
					continue;
				}
			}
			if(limitPayToChannelOnly && perferChannelId > 0 && payMethod.getPayChannelId() == perferChannelId){
				logger.warn("支付方式[" + payMethod + "]被商户[" + pay.getPayFromAccount() + "]被限定只能使用channelId=" + perferChannelId + "的支付方式，该支付方式不可使用");
				return null;
			}
			payMethodList2.add(payMethod);
			if(payMethod.getWeight() >= highestWeight){
				highestWeight = payMethod.getWeight();
			}
			//	}
			/*if(payMethod.getContextType() != null && payMethod.getContextType().equalsIgnoreCase(pay.getContextType())){
				logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回与其指定支付环境一致的支付方式:" + payMethod);
				return payMethod;
			}*/
		}
		/*if(payMethodList2.size() <  1){
			logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的最高优先级[" + highestWeight + "]并且成功进未超限的支付方式数量为0，直接返回系统中优先级最高的第一个支付方式:" + payMethodList.get(0));
			PayMethod onlyPayMethod = payMethodList.get(0);
			if(excludePayMethods != null && excludePayMethods.length > 0){
				for(int ex2 : excludePayMethods){
					if(onlyPayMethod.getPayMethodId() == ex2){
						logger.warn("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，但该支付方式[" + onlyPayMethod + "]被参数excludePayMethod设置为跳过");
						return null;
					}
				}
			}
			if(limitToChannelOnly && perferChannelId > 0 && onlyPayMethod.getPayChannelId() == perferChannelId){
				logger.warn("支付方式[" + onlyPayMethod + "]被商户[" + pay.getPayFromAccount() + "]被限定只能使用channelId=" + perferChannelId + "的支付方式，该支付方式不可使用");
				return null;
			}
			logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，直接返回第一个支付方式:" + payMethodList.get(0));
			return onlyPayMethod;
		}*/
		if(payMethodList2.size() ==  1){
			logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的成功进未超限的支付方式数量为1，直接返回第一个支付方式:" + payMethodList2.get(0));
			PayMethod onlyPayMethod = payMethodList2.get(0);
			if(excludePayMethods != null && excludePayMethods.length > 0){
				for(int ex2 : excludePayMethods){
					if(onlyPayMethod.getId() == ex2){
						logger.warn("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，但该支付方式[" + onlyPayMethod + "]被参数excludePayMethod设置为跳过");
						return null;
					}
				}
			}
			if(limitPayToChannelOnly && perferChannelId > 0 && onlyPayMethod.getPayChannelId() == perferChannelId){
				logger.warn("支付方式[" + onlyPayMethod + "]被商户[" + pay.getPayFromAccount() + "]被限定只能使用channelId=" + perferChannelId + "的支付方式，该支付方式不可使用");
				return null;
			}
			logger.debug("根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回的支付方式数量为1，直接返回第一个支付方式:" + payMethodList.get(0));
			return onlyPayMethod;
		}

		PayMethod usePayMethod = null;
		String channelRobinType = configService.getValue(DataName.CHANNEL_ROBIN_TYPE.name(), pay.getOwnerId());
		if (AbsChannelRobinServiceImpl.ROBIN_TYPE_GLOBAL.equals(channelRobinType)) {
			usePayMethod = this.roundChannel(payMethodList2, AbsChannelRobinServiceImpl.ROBIN_TYPE_GLOBAL,AbsChannelRobinServiceImpl.ROBIN_TYPE_GLOBAL, String.valueOf(pay.getPayTypeId()));
		} else if (AbsChannelRobinServiceImpl.ROBIN_TYPE_MERCHANT.equals(channelRobinType)) {
			usePayMethod = this.roundChannel(payMethodList2, AbsChannelRobinServiceImpl.ROBIN_TYPE_MERCHANT,String.valueOf(pay.getPayFromAccount()), String.valueOf(pay.getPayTypeId()));
		} else if (AbsChannelRobinServiceImpl.ROBIN_TYPE_FIFO.equals(channelRobinType)) {
			usePayMethod = this.fifoUseChannelRobin(payMethodList2);
		} else {
			usePayMethod = this.defaultChannelRobin(payMethodList2, perferChannelId, pay, highestWeight);
		}
		if (null == usePayMethod) {
			logger.debug("为支付订单:" + pay.getTransactionId() + "根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]未找到匹配的支付方式，返回第一个支付方式:" + payMethodList.get(0));
			payMethodList.get(0);
		}
		return usePayMethod;
	}

	/**
	 * 轮询通道  根据商户
	 *
	 * @param keyList
	 * @param robinType
	 * @param payType
	 * @return
	 */
	public PayMethod roundChannel(List<PayMethod> keyList, String robinType, String robinKey, String payType) {
		String key = new StringBuffer(robinType).append("_").append(robinKey).append("_").append(payType).toString();
		long posValue = centerDataService.increaseBy(key, 1, 1L, ttl);
		logger.debug("通道轮询选择 roundChannel by key=" + key + ", posValue=" + posValue);
		long selectVal = 0;
		if (posValue >= Integer.MAX_VALUE) {
			centerDataService.delete(key);
			selectVal = 1;
		} else {
			selectVal = posValue % keyList.size();
		}
		logger.debug("通道轮询选择 roundChannel keyListSize=" + keyList.size() + ", 选择的通道=" + selectVal);
		PayMethod channel = keyList.get((int) selectVal);
		return channel;
	}

	/**
	 * 获取子通道id
	 * @param childChannelList
	 * @param payMethodId
	 * @return
	 */
	public String roundPayMethodChildChannel(String childChannelList, String payMethodId) {
		childChannelList = StringUtils.trimToEmpty(childChannelList);
		if (StringUtils.isEmpty(childChannelList)) {
			return childChannelList;
		}
		String[] idList = childChannelList.split(",");
		int channelChildSize = idList.length;
		if (channelChildSize == 1) {
			return idList[0];
		}
		String key = new StringBuffer("roundPayMethodChildChannel").append("_").append(payMethodId).toString();
		long posValue = centerDataService.increaseBy(key, 1, 1L, ttl);
		logger.debug("通道轮询选择 roundChannel by key=" + key + ", posValue=" + posValue);
		int selectVal = 0;
		if (posValue >= Integer.MAX_VALUE) {
			centerDataService.delete(key);
			selectVal = 1;
		} else {
			selectVal = (int) posValue % idList.length;
		}
		String roundChildId = idList[selectVal];
		logger.debug("通道轮询选择 roundChannel keyListSize=" + channelChildSize + ", 选择的子通道=" + roundChildId);
		return roundChildId;
	}

	/**
	 * 根据最后使用时间进行排序
	 * 优先选择最早或者没用过的通道
	 * 
	 * 
	 * @author GHOST
	 * @date 2018-11-12
	 */
	public PayMethod fifoUseChannelRobin(List<PayMethod> payMethodList2) {
		String keyPrefix = "PAY_METHO_ROBIN_LAST";
		String key = null;

		PayMethod selectPayMethod = null;
		if(payMethodList2.size() < 2) {
			selectPayMethod = payMethodList2.get(0);
		} else {
			Date earliestTime = null;
			for(PayMethod payMethod : payMethodList2) {
				key = keyPrefix + "#" + payMethod.getId();
				String value = centerDataService.get(key);
				if(value == null) {
					selectPayMethod = payMethod;
					break;
				}
				Date useTime = StringTools.parseTime(value);
				if(useTime == null) {
					selectPayMethod = payMethod;
					break;
				}
				if(earliestTime == null) {
					earliestTime = useTime;
					selectPayMethod = payMethod;

				} else if(earliestTime.after(useTime)){
					earliestTime = useTime;
					selectPayMethod = payMethod;
				}
			}
		}
		if(selectPayMethod == null) {
			selectPayMethod = payMethodList2.get(0);
			logger.warn("无法根据最早规则得到payMethod，使用第一个payMethod:{}", selectPayMethod.getId());
		}
		key = keyPrefix + "#" + selectPayMethod.getId();
		centerDataService.setForce(key, StringTools.time2String(new Date()), ttl);
		logger.debug("本次根据使用时间FIFO原则选择的payMethod是:{}",selectPayMethod.getId());
		return selectPayMethod;
	}

	/**
	 * 默认筛选支付通道方式
	 *
	 * @param payMethodList2
	 * @param perferChannelId
	 * @param pay
	 * @param highestWeight
	 * @return
	 */
	public PayMethod defaultChannelRobin(List<PayMethod> payMethodList2, int perferChannelId, Pay pay, int highestWeight) {
		int rand = RandomUtils.nextInt(100);
		logger.debug("当前比例随机数是:" + rand);
		//按照优先级、占比进行排序，把优先级高的放前面，相同优先级的把占比最小的放在前面，这样可以通过rand处理比例
		Collections.sort(payMethodList2, new Comparator<PayMethod>() {

			@Override
			public int compare(PayMethod p1, PayMethod p2) {
				if (p1.getWeight() > p2.getWeight()) {
					return -1;
				} else if (p1.getWeight() < p2.getWeight()) {
					return 1;
				} else {
					if (p1.getPercent() > p2.getPercent()) {
						return 1;
					}
					return -1;
				}
			}
		});

		//如果有绑定channleId，那么应当放在前面
		if (perferChannelId > 0) {
			PayMethod firstPayMethod = null;
			for (PayMethod payMethod : payMethodList2) {
				if (payMethod.getPayChannelId() == perferChannelId) {
					if (firstPayMethod == null) {
						firstPayMethod = payMethod;
					}
					if (rand <= payMethod.getPercent() || payMethod.getPercent() == 100) {
						logger.debug("为支付订单:" + pay.getTransactionId() + ",根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回符合绑定channelId=" + perferChannelId + "的支付方式:" + payMethod + ",当前随机数是:" + rand + ",该支付方式的占比:" + payMethod.getPercent());
						return payMethod;
					}
				}
			}
			if (firstPayMethod != null) {
				logger.debug("为支付订单:" + pay.getTransactionId() + ",根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回符合绑定channelId=" + perferChannelId + "的第一个支付方式:" + firstPayMethod + ",当前随机数是:" + rand + ",该支付方式的占比:" + firstPayMethod.getPercent());
				return firstPayMethod;
			}
		}
		//第一个符合最高优先级的方式
		PayMethod firstHighestPayMethod = null;
		for (PayMethod payMethod : payMethodList2) {
			if (payMethod.getWeight() < highestWeight) {
				logger.debug("支付方式:" + payMethod.getId() + "的优先级:" + payMethod.getWeight() + "比当前最高优先级:" + highestWeight + "低，忽略");
				continue;
			}
			if (firstHighestPayMethod == null) {
				firstHighestPayMethod = payMethod;
			}
			if (rand <= payMethod.getPercent() || payMethod.getPercent() == 100) {
				logger.debug("为支付订单:" + pay.getTransactionId() + ",根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回支付方式:" + payMethod + ",当前随机数是:" + rand + ",支付方式占比是:" + payMethod.getPercent());
				return payMethod;
			}
		}
		if (firstHighestPayMethod != null) {
			logger.debug("为支付订单:" + pay.getTransactionId() + "根据[支付类型:" + pay.getPayTypeId() + ",contextType:" + pay.getContextType() + "]返回符合最高优先级:" + highestWeight + "的第一个支付方式:" + firstHighestPayMethod);
			return firstHighestPayMethod;
		}
		return null;
	}


}
