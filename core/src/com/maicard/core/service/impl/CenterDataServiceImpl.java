package com.maicard.core.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.core.annotation.IgnoreDs;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.KeyConstants;
import com.maicard.core.entity.CacheValue;
import com.maicard.core.service.CenterDataService;
import com.maicard.mb.annotation.IgnoreJmsDataSync;
import com.maicard.misc.ThreadHolder;
import com.maicard.utils.JsonUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * REDIS数据接口
 * 
 * 2018.5.6 由spring-data-redis方式改为直接的jedis pool方式, NetSnake.
 *
 *
 * @author NetSnake
 * @date 2016年4月26日
 *
 */
@IgnoreDs
public class CenterDataServiceImpl extends BaseService implements CenterDataService {

	/*@Autowired(required=false)
	private JedisPool jedisPool;*/

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;



	//RedisSerializer<String> serializer;

	private ObjectMapper om = JsonUtils.getNoDefaultValueInstance();

	final static String TIME_OUT_PREFIX = "GLOBAL_TIMEOUT";

	boolean CHECK_CONNECTION = true;

	@Override
	public Jedis getResource() {
		RedisConnection connection = redisConnectionFactory.getConnection();

		//logger.info("获取到的连接:" + connection + ",当前REDIS连接池信息:活动连接{}个，休眠连接{}个", connection.getNativeConnection()..getNumActive(),jedisPool.getNumIdle());
		return (Jedis)connection.getNativeConnection();
	}


	/**
	 * 
	 * @param key
	 * @param value
	 * @param timeoutSec 键值超时秒数，如果<1，则不设置超时
	 */
	@Override
	@IgnoreJmsDataSync
	public boolean setIfNotExist(String key, String value, long timeoutSec){

		if(value == null){
			return false;
		}
		Jedis connection = getResource();

		long setSuccess = 0;
		try {
			setSuccess = connection.setnx(key, value);
			logger.debug("向REDIS中NX写入,键:" + key + "，值是:" + value + ",有效期:" + timeoutSec + "秒，结果:" + setSuccess);
			if(setSuccess > 0 && timeoutSec > 0){
				connection.expire(key, (int)timeoutSec);
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			connection.close();
		}



		return setSuccess > 0;
	}

	


	

	@Override
	public void setForce(String key, String value, long timeoutSec){
		if(value == null){
			return;
		}

		Jedis connection = getResource();

		if(timeoutSec < 1){
			connection.set(key, value);
		} else {
			connection.setex(key, (int)timeoutSec, value);
		}

		connection.close();
		logger.debug("向REDIS中强制写入,键:" + key + "，值是:" + value + ",超时:" + timeoutSec);

	}

	@Override
	public void  setHmValue(String tableName, String keyInMap, Object value, int timeoutSec) throws Exception{
		Jedis connection = getResource();
		if(value == null){
			logger.debug("尝试放入Map[" + tableName + "]中的对象为空，删除该对象:" + keyInMap);
			connection.hdel(tableName, keyInMap);
			connection.close();
			//redisTemplate.opsForHash().delete(tableName, keyInMap);
			return;
		}

		Date expireDate = null;
		if(timeoutSec > 0){
			expireDate = DateUtils.addSeconds(new Date(), timeoutSec);
			String expireTime = ThreadHolder.defaultTimeFormatterHolder.get().format(expireDate);
			String timeoutKey = TIME_OUT_PREFIX + "#" + tableName + "#" + keyInMap;
			this.setForce(timeoutKey, expireTime, timeoutSec);
		}
		CacheValue cv = new CacheValue(keyInMap, value, expireDate);
		cv.objectType = value.getClass().getName();
		String text = om.writeValueAsString(cv);

		long rs = connection.hset(tableName, keyInMap, text);//.opsForHash().put(tableName, keyInMap, text);
		connection.close();
		logger.debug("向REDIS中写入HashMap[" + tableName + "]，键值" + keyInMap + " => " + text + ",对象类型:" + value.getClass().getName() + ",超时:" + timeoutSec + ",写入结果:" + rs);// + ",表超时:" + tableExpire);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getHmValues(String tableName){
		Jedis connection = getResource();
		Map<String, String> objectList = connection.hgetAll(tableName);//, fields) redisTemplate.opsForHash().values(tableName);
		if(objectList == null || objectList.size() < 1){
			connection.close();
			return null;
		}

		List<E> resultList = new ArrayList<E>();
		try{
			for(String o : objectList.values()){
				// Class.forName(objectList.get(0).
				CacheValue cv = om.readValue(o, CacheValue.class);

				if(cv == null){
					continue;
				}
				if(cv.expireTime != null && new Date().after(cv.expireTime)){
					//已过期
					logger.debug("从缓存表[" + tableName + "]获取键[" + cv.key + "]的值过期时间是:" + ThreadHolder.defaultTimeFormatterHolder.get().format(cv.expireTime) + ",,已过期");
					connection.hdel(tableName, cv.key);
					//redisTemplate.opsForHash().delete(tableName ,cv.key);
					continue;
				}
				Object value = cv.value;
				if(value == null){
					if(logger.isDebugEnabled()) logger.debug("从缓存表[" + tableName + "]获取键[" + o.toString() + "]的值为空，删除");
					connection.hdel(tableName, cv.key);
					//redisTemplate.opsForHash().delete(tableName , o.toString());
					continue;
				}

				if(value instanceof LinkedHashMap){
					logger.warn("缓存[" + tableName + "#" + cv.key + "]的值类型是:" + cv.value.getClass().getName() + ",将转换为:" + cv.objectType);
					try {
						value = om.readValue(om.writeValueAsBytes(value), Class.forName(cv.objectType));
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}

					if(value == null){
						logger.warn("无法将请求执行的对象转换为:" + cv.objectType);
						continue;
					}

				} else {
					logger.debug("缓存[" + tableName + "#" + cv.key + "]的值类型是:" + cv.value.getClass().getName() + "，值是:" + value);
				}
				resultList.add((E)value);

			}
		}catch(Exception e){
			e.printStackTrace();
		}
		connection.close();

		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T>T getHmValue(String tableName, String mapKey){
		Jedis connection = getResource();
		Object o = null;
		o = connection.hget(tableName, mapKey);

		logger.debug("从缓存表[" + tableName + "]获取键[" + mapKey + "]的值是:" + o);
		if(o == null){
			connection.close();
			return null;
		}
		CacheValue cv = null;
		try {
			cv = om.readValue(o.toString(), CacheValue.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(cv == null){
			logger.error("无法将缓存对象转换为CacheValue对象:" + o.toString());
			connection.hdel(tableName ,mapKey);
			connection.close();
			return null;
		}
		Date currentTime = new Date();
		if(cv.expireTime != null && currentTime.after(cv.expireTime)){
			//已过期
			logger.debug("从缓存表[" + tableName + "]获取键[" + cv.key + "]的值过期时间是:" + ThreadHolder.defaultTimeFormatterHolder.get().format(cv.expireTime) + ",当前时间是:" + ThreadHolder.defaultTimeFormatterHolder.get().format(currentTime) + ",已过期");

			connection.hdel(tableName ,mapKey);
			String timeoutKey = TIME_OUT_PREFIX + "#" + tableName + "#" + mapKey;
			connection.del(timeoutKey);
			connection.close();
			return null;
		}
		Object value = cv.value;

		if(value == null){
			connection.close();
			return null;
		}
		if(value instanceof LinkedHashMap){
			logger.info("缓存[" + tableName + "#" + cv.key + "]的值类型是:" + cv.value.getClass().getName() + ",将转换为:" + cv.objectType);
			try {
				value = om.readValue(om.writeValueAsBytes(value), Class.forName(cv.objectType));
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

			if(value == null){
				logger.warn("无法将请求执行的对象转换为:" + cv.objectType);
				connection.close();
				return null;
			}

		} else {
			logger.debug("缓存[" + tableName + "#" + cv.key + "]的值类型是:" + cv.value.getClass().getName() + "，值是:" + cv.value);
		}
		connection.close();
		return (T)value;
	}

	@Override
	public   Set<String> getHmKeys(String tableName){
		Jedis connection = getResource();
		Set<String> set = null;
		try {
			  set = connection.hkeys(tableName);
		}catch (Exception e){
			e.printStackTrace();;
		}finally {
			if(connection != null){
				connection.close();
			}
		}
		if(set == null){
			return Collections.emptySet();
		} else {
			return set;
		}

	}

	@Override
	public long countHm(String tableName){
		Jedis connection = getResource();
		long size = connection.hlen(tableName);
		connection.close();
		return size;
		//return redisTemplate.opsForHash().size(tableName);
	}

	@Override
	public void setBatch(final Map<String,String> map, final long timeoutSec, final boolean force){
		if(map == null || map.size() < 1){
			logger.warn("尝试批量写入的数据集为空");
			return;
		}

		long ts = new Date().getTime();
		Jedis connection = getResource();

		for(Entry<String,String> entry : map.entrySet()){
			if(force){
				connection.set(entry.getKey(),entry.getValue());
			} else {
				connection.setnx(entry.getKey(),entry.getValue());
			}
			if(timeoutSec > 0){
				connection.expire(entry.getKey(), (int)timeoutSec);
			}
		}
		connection.close();





		long timing = (new Date().getTime() - ts);
		timing /= 1000;
		if(logger.isDebugEnabled()){
			logger.debug("共插入" + map.size() + "条数据，耗时" + timing + "秒");
		}

	}

	public boolean lock(final String key){
		return this.lock(key, Constants.DISTRIBUTED_DEFAULT_LOCK_SEC);
	}


	public boolean lock(final String key, final long lockSec){

		long totalTime = Constants.DISTRIBUTED_LOCK_RETRY_TIME * Constants.DISTRIBUTED_LOCK_WAIT_MS;
		boolean rs = false;
		long t = System.currentTimeMillis();
		int tryCount = 0;
		for (int i = 0; i < Constants.DISTRIBUTED_LOCK_RETRY_TIME; i++) {
			rs = setIfNotExist(key, key, lockSec);
			/*if(logger.isDebugEnabled()){
				logger.debug("第" + (i + 1) + "次锁定对象[" + key + "]的结果:" + rs);
			}*/
			if (rs) {
				break;
			}
			try {
				Thread.sleep(Constants.DISTRIBUTED_LOCK_WAIT_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tryCount++;
		}
		long useTime = (System.currentTimeMillis() - t);
		if(useTime >= totalTime - 200){
			//正常情况下，不应该出现锁定时间超过最长时间的情况
			logger.warn("TIME_ERROR:::锁定数据[{}]结果:{},尝试次数:{},耗时:{}ms",key,rs, tryCount+1,  useTime);
		} else {
			logger.debug("锁定数据[{}]结果:{},尝试次数:{},耗时:{}ms",key,rs, tryCount+1, useTime );
		}
		return rs;		
	}


	/** 
	 * 对数字进行加减锁定
	 * @param key
	 * @param offset		对已存在的数字进行增减的数量
	 * @param backupNumber 如果该数字不存在，写入一个数字
	 * @return
	 */
	@Override
	public long increaseBy(String key,  int offset, long backupNumber, long timeSec){
		Jedis connection = getResource();

		long nxLockSuccess = 0;
		String oldNumber = get(key);
		if(oldNumber == null || Long.parseLong(oldNumber) < 1){
			nxLockSuccess = connection.setnx(key,String.valueOf(backupNumber));
			if(nxLockSuccess > 0){
				if(logger.isDebugEnabled()){
					logger.debug("NX写入数字成功，返回备选数字:" + backupNumber + "，超时:" + timeSec);
				}
				if(timeSec > 0){
					connection.expire(key, (int)timeSec);
				}
				connection.close();
				return backupNumber;
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("NX写入备选数字[" + backupNumber + "]失败");
				}
			}
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("键[" + key + "]已存在数值:" + oldNumber);
			}
		}


		/*
		redisTemplate.opsForValue().increment(arg0, arg1)(key, arg1);
		redisTemplate.watch(key);
		redisTemplate.multi();*/
		long rs = connection.incrBy(key,  offset);
		if(timeSec > 0){
			connection.expire(key, (int)timeSec);
		}

		connection.close();
		return rs;


	}
	@Override
	public String get(final String key){
		long ts = new Date().getTime();
		Jedis connection = getResource();

		if(logger.isDebugEnabled()){
			logger.debug("准备获取键值:" + key);
		}
		String value = connection.get(key);
		connection.close();
		logger.info("获取键值:" + key + "耗时:" + (new Date().getTime() - ts) + "ms");
		return value;
		//connect.opsForValue().get(key);
		/*long t1 = new Date().getTime();
		if(key == null){
			return null;
		}
		String value = redisTemplate.execute(new RedisCallback<String>() {  
			public String doInRedis(RedisConnection connection)  
			{  
				byte[] keyBin = serializer.serialize(key);  
				byte[] value = connection.get(keyBin);  
				connection.close();
				if (value == null) {  
					if(logger.isDebugEnabled()){
						logger.debug("缓存中没有KEY=" + key + "的数据");
					}
					return null;
				}  
				String version = serializer.deserialize(value);  
				return version;
			}  
		}); 	

		long time = new Date().getTime() - t1;
		if(logger.isDebugEnabled()){
			logger.debug("获取键[" + key + "]的值是:" + value + ",耗时" + time + "毫秒");
		}
		return value;*/

	}

	@Override
	public String getExclusive(final String key, final boolean deleteAfterGet){
		Jedis connection = getResource();


		connection.watch(key);	

		/////////// 事务性执行并提交 //////////////
		Transaction t = connection.multi();		
		String value = connection.get(key);
		if(deleteAfterGet){
			connection.del(key);
		}
		List<Object> execResult =	t.exec();
		/////////////////////////////////////////
		connection.close();
		if (execResult == null) {  
			logger.warn("请求获取数据[" + key + "]时数据已改变");
			return null;
		}  
		//logger.debug("排他获取的结果数量是:" + execResult.size());
		if(execResult.size() > 0){
			try{
				value = execResult.get(0).toString();
			}catch(Exception e){
				e.printStackTrace();
			}

		}

		if (value == null) {  
			logger.error("无法排他性获取数据[" + key + "]，返回值为空");
			return null;
		}  		
		return  value;
	}
	
	@Override
	public String removeHmExclusive(final String table, final String key) throws Exception{
		Jedis connection = getResource();

		String watchKey = KeyConstants.WATCH_KEY + "#" + table + "#" + key;
		String timeoutKey = new StringBuffer().append(TIME_OUT_PREFIX).append("#").append(table).append("#").append(key).toString();
		long setSuccess = connection.setnx(watchKey, "1");
		if(setSuccess <= 0) {
			//标记着加锁失败
			logger.info("无法排他性获取数据:{} => {},无法上锁:{}", table, key, watchKey);
			return null;
		}
		String value = connection.hget(table,key);
		connection.hdel(table, key);
		connection.del(timeoutKey);
		connection.del(watchKey);
		connection.close();
		if (value == null) {  
			logger.info("无法排他性获取表:" + table + "中key=" + key + "的数据，返回值为空");
		}  		
		return  value;
	}



	@Override
	@IgnoreJmsDataSync
	public long delete(final String key){
		if(logger.isDebugEnabled()){
			logger.debug("删除分布式KEY=" + key);
		}
		Jedis connection = getResource();

		long result =  connection.del(key);
		connection.close();
		return result;
	}

	@Override
	public Set<String> listKeys(String pattern){
		Jedis connection = getResource();
		Set<String> keys = connection.keys(pattern);
		connection.close();
		if(keys == null){
			return Collections.emptySet();
		} else {
			return keys;
		}
	}

	@Override
	public String pushFromZQueue(final String key, boolean reverse) {
		Jedis connection = getResource();

		Set<String> set = null;
		if(reverse){
			set = connection.zrevrange(key, 0, 0);
		} else {
			set = connection.zrange(key, 0, 0);
		}
		if(set == null || set.size() < 1){
			connection.close();
			//logger.debug("有序队列[" + key + "]中的数据为空,无法获取第一条数据");
			return null;
		}
		String result =  set.iterator().next();
		connection.zrem(key, result);
		connection.close();
		//redisTemplate.opsForZSet().remove(key, result);
		/*if(result != null){
			logger.debug("从有序队列[" + key + "]中获取第一条数据:" + result + ",是否反向排序:" + reverse);
		}*/
		logger.debug("从有序队列[" + key + "]中获取第一条数据:" + result + ",是否反向排序:" + reverse);

		return result;
	}

	@Override
	public Set<String> pushSetFromZQueue(final String key, boolean reverse, int begin, int end) {

		logger.debug("准备从有序队列[" + key + "]中获取" + begin + "到" + end + "条数据,是否反向排序:" + reverse);

		Jedis connection = getResource();

		if(!connection.exists(key)){
			logger.debug("有序队列[" + key + "]不存在");
			connection.close();
			return Collections.emptySet();

		}
		String type = connection.type(key);
		if(type == null){
			logger.error("有序队列[" + key + "]类型为空");
			connection.close();
			return Collections.emptySet();
		}

		/*if(type != DataType.ZSET){
			logger.error("有序队列#{}类型错误，不是ZSET，而是:{}", key, type.toString());
			return Collections.emptySet();
		}*/
		connection.expire(key, (int)Constants.CACHE_MAX_TTL);

		Set<String> set = null;
		if(reverse){
			set = connection.zrevrange(key, begin, end);
		} else {
			set = connection.zrange(key, begin, end);
		}
		if(set == null || set.size() < 1){
			connection.close();
			logger.debug("有序队列[" + key + "]为空");
			return Collections.emptySet();
		}

		//删除已经得到的数据
		String[] data = new String[set.size()];
		data = set.toArray(data);
		long removed = connection.zrem(key, data);
		connection.close();

		logger.debug("从有序队列[" + key + "]中获取" + begin + "到" + end + "共" + set.size() + "条数据,删除" + removed + "条，是否反向排序:" + reverse);

		return set;
	}



	@Override
	public boolean addToZQueue(String key, String value, long score) {

		Jedis connection = getResource();

		long result =  connection.zadd(key, score, value);

		logger.debug("向有序队列[" + key + "]中放入数据:" + value + ",分数:" + score + ",结果:" + result);

		connection.expire(key, (int)Constants.CACHE_MAX_TTL);
		
		connection.close();

		return result > 0;
	}
	
	

	@Override
	public Set<String> zRange(String key, long beginScore, long endScore){
		if(endScore == 0){
			endScore = -1;
		}
		Jedis connection = getResource();

		Set<String> set = connection.zrangeByScore(key, beginScore, endScore);
		connection.close();
		return set;
	}

	@Override
	public long zCount(String key, long beginScore, long endScore){
		if(endScore == 0){
			endScore = -1;
		}
		Jedis connection = getResource();
	
		long count = connection.zcount(key, beginScore, endScore);
		connection.close();
		return count;
	}



	@Override
	public long listAdd(String listName, String value) {
		
		Jedis connection = getResource();

		long rs =  connection.rpush(listName, value);

		connection.close();
		logger.debug("向列表[" + listName + "]中放入数据:" + value + ",结果:" + + rs);
		return rs;
	}

	@Override
	public long listRemove(String listName, long index, String value) {
		Jedis connection = getResource();
		long rs =  connection.lrem(listName, index, value);
		connection.close();
		logger.debug("删除列表[" + listName + "]中的数据[index=" + index + ",value=" + value + "],结果:" + + rs);
		return rs;

	}
	
	@Override
	public List<String> listRange(String listName, int begin, int end) {
		Jedis connection = getResource();
		List<String> rs =  connection.lrange(listName,begin,end);
		connection.close();
		if(rs == null) {
			return Collections.emptyList();
		}
		return rs;
	}


	@Override
	public long sRemove(String setName, String value){
		Jedis connection = getResource();
		long rs = connection.srem(setName, value);
		connection.close();
		logger.debug("删除集合[" + setName + "]中的数据[value=" + value + "],结果:" + + rs);
		return rs;
	}

	@Override
	public String sPop(String setName){
		Jedis connection = getResource();
		String value = connection.spop(setName);
		connection.close();
		logger.debug("从集合[" + setName + "]中弹出一个数据[value=" + value + "]");

		return value;

	}

	@Override
	public long sAdd(String setName, String value){
		Jedis connection = getResource();
		long rs = connection.sadd(setName, value);
		connection.close();
		logger.debug("向集合[" + setName + "]中加入一个数据[value=" + value + "]，结果:" + rs);
		return rs;
	}

	@Override
	public Set<String> sList(String setName){
		Jedis connection = getResource();
		Set<String> set = connection.smembers(setName);
		connection.close();
		return set;
	}


	@Override
	public long sCount(String setName){
		Jedis connection = getResource();
		long size = connection.hlen(setName);
		connection.close();
		return size;
		//return redisTemplate.opsForSet().size(setName);
	}


	@Override
	public long countList(String listName) {
		Jedis connection = getResource();
		long size = connection.llen(listName);
		connection.close();
		return size;
		//return redisTemplate.opsForList().size(listName);
	}

	@Override
	public List<String> listOnPage(String listName, int begin, int end) {
		Jedis connection = getResource();
		if(begin == 0 && end == 0){
			Long size = connection.llen(listName);
			if(size == null){
				return Collections.emptyList();
			} 
			end = size.intValue();
		}
		List<String> list = connection.lrange(listName, begin,  end);
		connection.close();
	
		if(list == null){
			return Collections.emptyList();
		} else {
			return list;
		}
	}

	@Override
	public void setHmPlainValue(String tableName, String keyInMap, String text, int timeoutSec) throws Exception {

		Assert.notNull(tableName,"尝试设置属性的hash表不能为空");
		Assert.notNull(keyInMap,"尝试设置属性的hash表#" + tableName + "属性不能为空");

		String timeoutKey = new StringBuffer().append(TIME_OUT_PREFIX).append("#").append(tableName).append("#").append(keyInMap).toString();
		Jedis connection = getResource();

		if(text == null){
			logger.debug("尝试放入Map[" + tableName + "]中的对象为空，删除该对象:" + keyInMap);
			connection.hdel(tableName, keyInMap);
			connection.del(timeoutKey);
			connection.close();
			return;
		}

		;
		if(timeoutSec != 0){
			String expireTime  = null;

			if(timeoutSec > 0){
				expireTime = ThreadHolder.defaultTimeFormatterHolder.get().format(DateUtils.addSeconds(new Date(), timeoutSec));
			} else {
				expireTime = "-1";
			}
			connection.set(timeoutKey, expireTime);
			connection.expire(timeoutKey, timeoutSec);
		}


		connection.hset(tableName, keyInMap, text);
		connection.close();
		logger.debug("向REDIS hash表:" + tableName + "]写入键值" + keyInMap + ",超时:" + timeoutSec + ",超时KEY=" + timeoutKey);

	}

	@Override
	public void setHmPlainValueBatch(String tableName, Map<String,String> data) throws Exception {

		Assert.notNull(tableName,"尝试设置属性的hash表不能为空");

		Jedis connection = getResource();

		connection.hmset(tableName,data);
		connection.close();
		logger.debug("向REDIS hash表:" + tableName + "]写入批量键值" + data.size() + "个");

	}
	@Override
	public String getHmPlainValue(String tableName, String keyInMap) throws Exception {

		String timeoutKey = TIME_OUT_PREFIX + "#" + tableName + "#" + keyInMap;
		String expireTime = this.get(timeoutKey);
		logger.debug("从REDIS hash表:" + tableName + "]读取键值" + keyInMap + ",超时KEY=" + timeoutKey + "=>" + expireTime);
		
		Jedis connection = getResource();

		try {
			if (expireTime == null) {
				//该对象已过期
				connection.hdel(tableName, keyInMap);
				//connection.close();
				return null;
			} else {
				if (expireTime.equals("-1")) {
					//永不过期
				} else {
					Date time = ThreadHolder.defaultTimeFormatterHolder.get().parse(expireTime);

					if (time.getTime() < new Date().getTime()) {
						//对象已过期

						connection.hdel(tableName, keyInMap);
						//connection.close();
						return null;
					}
				}
			}
			String o = connection.hget(tableName, keyInMap);
			//connection.close();
			return o;
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(connection != null)connection.close();
		}
		return null;
	}

/*
	public JedisPool getJedisPool() {
		return jedisPool;
	}


	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}*/


	@Override
	public long hCount(String tableName) {
		Jedis connection = getResource();


		long cnt = connection.hlen(tableName);
		

		connection.close();

		return cnt;
	}


	@Override
	public Map<String,String> hScan(String rawKeyTable, int cursor, int count) {
		Jedis connection = getResource();

		
		//获取游标
		String cursorKey = rawKeyTable + "#CURSOR";
		String currentCursor = connection.get(cursorKey);
		if(currentCursor == null) {
			currentCursor = String.valueOf(cursor);
		}
		
		ScanParams params = new ScanParams();
		params.count(count);
		logger.info("当前游标:{},静态游标:{},请求读取数量是:{}", cursor, ScanParams.SCAN_POINTER_START, count);
		ScanResult<Entry<String, String>> cnt = connection.hscan(rawKeyTable,currentCursor, params);
		
		connection.set(cursorKey, cnt.getCursor());
		
		
		
		connection.close();
		logger.info("读取表格:{}，从游标:{}读取数据，新游标是:{}", rawKeyTable, currentCursor, cnt.getCursor());

		List<Entry<String, String>> result = cnt.getResult();
		Map<String,String>map = new HashMap<String,String>();
		if(result == null || result.size() < 1) {
			return map;
		}
		//XXX scan/hscan返回的结果集不会完全等于要求的count，可能会略多于指定count，所以要做一个判断
		int start = 0;
		for(Entry<String,String> entry : result) {
			map.put(entry.getKey(), entry.getValue());
			start++;
			if(start > count) {
				break;
			}
		}
		return map;
	}









}
