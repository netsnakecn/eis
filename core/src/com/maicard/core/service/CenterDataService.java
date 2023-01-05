package com.maicard.core.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.maicard.core.annotation.IgnoreDs;



@IgnoreDs
public interface CenterDataService {
	
	/**
	 * 使用默认配置的锁定时间({@link CommonStandard.DISTRIBUTED_DEFAULT_LOCK_SEC})，设置一个键到REDIS<br>
	 * 如果放入失败则返回false，放入失败说明有其他程序正在访问该键对应的数据，此时则应考虑数据同步性，如有必要应放弃处理该数据。<br>
	 * 只有放入成功即该键之前不存在，才能返回true<br>
	 * 如果放入失败会尝试多次，尝试次数由 {@link CommonStandard.DISTRIBUTED_LOCK_RETRY_TIME}指定
	 * @param key
	 * @return
	 * @see CommonStandard.DISTRIBUTED_LOCK_RETRY_TIME 
	 * @see CommonStandard.DISTRIBUTED_DEFAULT_LOCK_SEC
	 */
	boolean lock(String key);

	/**
	 * 使用指定的的锁定时间(lockSec)，设置一个键到REDIS<br>
	 * 如果放入失败则返回false，放入失败说明有其他程序正在访问该键对应的数据，此时则应考虑数据同步性，如有必要应放弃处理该数据。<br>
	 * 只有放入成功即该键之前不存在，才能返回true<br>
	 * 如果放入失败会尝试多次，尝试次数由 {@link CommonStandard.DISTRIBUTED_LOCK_RETRY_TIME}指定
	 * @param key
	 * @return
	 * @see CommonStandard.DISTRIBUTED_LOCK_RETRY_TIME
	 */
	boolean lock(String key, long lockSec);

	/**
	 * 删除一个键
	 * @param key
	 * @return
	 */
	long delete(String key);

	/**
	 * 获取一个键
	 * @param key
	 * @return
	 */
	String get(String key);
	
	/**
	 * 以排他方式获取某个值，如果在获取过程中被改变则返回空
	 * 如果deleteAfterGet为真，则在获取到该数据后删除该键值
	 * @param key
	 * @param deleteAfterGet
	 * @return
	 */
	String getExclusive(String key, boolean deleteAfterGet);
	
	//boolean setExclusive(String key, String value, long timeoutSec);


	//long writeNumber(String key, int offset, long backupNumber);

	/**
	 * 增加一个键中的数值<br>
	 * 
	 * @param key 键名
	 * @param offset 增加数量，负数为减少
	 * @param backupNumber 如果键不存在，返回一个数字
	 * @param timeSec 该键的存活周期
	 * @return 增加offset之后的数字
	 */
	long increaseBy(String key, int offset, long backupNumber, long timeSec);

	/**
	 * 向REDIS中放入一个键key，值为value，不考虑该键是否存在，无论是否存在都会放入
	 * @param key
	 * @param value
	 * @param timeoutSec
	 */
	void setForce(String key, String value, long timeoutSec);

	/**
	 * 向REDIS中放入一个键key，值为value，如果该键已存在则中止存放并返回false<br>
	 * 只有该键不存在的时候才能放入成功
	 * @param key
	 * @param value
	 * @param timeoutSec
	 * @return
	 */
	boolean setIfNotExist(String key, String value, long timeoutSec);

	/**
	 * 
	 * @param map
	 * @param timeoutSec
	 * @param force
	 */
	void setBatch(Map<String, String> map, long timeoutSec, boolean force);

	Set<String> listKeys(String pattern);

	

	/**
	 * 向REDIS中的一个HashMap，放入一个指定key(keyInMap)的值，并可指定超时秒数timeoutSec
	 * @param tableName
	 * @param keyInMap
	 * @param value
	 * @param timeoutSec
	 * @throws Exception
	 */
	void setHmValue(String tableName, String keyInMap, Object value, int timeoutSec) throws Exception;
	
	void setHmPlainValue(String tableName, String keyInMap, String text, int timeoutSec) throws Exception;

	
	/**
	 * 从REDIS中指定的HashMap中返回一个指定键对应的对象
	 * @param tableName
	 * @param mapKey
	 * @return
	 */
	<T>T getHmValue(String tableName, String mapKey);

	//String getHmValue(String tableName, String mapKey);

	/**
	 * 计算REDIS中指定Hash表的元素的数量
	 * @param tableName
	 * @return
	 */
	long countHm(String tableName);

	/**
	 * 获取REDIS中指定Hash表的所有元素的键
	 * @param tableName
	 * @return
	 */
	Set<String> getHmKeys(String tableName);

	/**
	 * 从REDIS中获取一个Z队列（名称是key）中得到第一个数据，同时在队列中删除改数据，如果rerverse为真，则从Z队列的末尾开始
	 * @param key
	 * @param rerverse
	 * @return
	 */
	String pushFromZQueue(String key, boolean rerverse);

	/**
	 * 向REDIS中的的Z队列（队列名称为key）放入一个值，并指定这个值的分数，也就是它在队列中的顺序
	 * @param key
	 * @param value
	 * @param score
	 * @return
	 */
	boolean addToZQueue(String key, String value, long score);

	/**
	 * 返回REDIS中指定Hash表的所有元素的键对象
	 * @param tableName
	 * @return
	 */
	<E>List<E> getHmValues(String tableName);


	/**
	 * 向REDIS中指定的列表(listName)放入一个值，使用rightPush
	 * @param key
	 * @param value
	 * @return
	 */
	long listAdd(String listName, String value);
	
	/**
	 * 计算REDIS中一个列表(listName)的元素个数
	 * @param listName
	 * @return
	 */
	
	long countList(String listName);
	
	/**
	 * 返回REDIS中指定列表(listName)中顺序为从begin到end的元素
	 * @param listName
	 * @param begin
	 * @param end
	 * @return
	 */
	List<String> listOnPage(String listName, int begin, int end);

	/**
	 * 从REDIS中指定的列表(listName)中删除指定索引(index)和值
	 * @param listName
	 * @param index
	 * @param value
	 * @return
	 */
	long listRemove(String listName, long index, String value);

	Set<String> pushSetFromZQueue(String key, boolean reverse, int begin, int end);

	String getHmPlainValue(String tableName, String keyInMap) throws Exception;

	/**
	 * 从一个SET中随机返回一个元素，该元素会被移除
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	String sPop(String setName);

	/**
	 * 向一个SET中放入一个元素，如果没有重复并放入成功将返回一个index
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	long sAdd(String setName, String value);

	/**
	 * 从一个SET中移除一个元素，如果存在那么将移除并返回这个元素的index
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	long sRemove(String setName, String value);

	/**
	 * 从一个SET中返回所有元素，但不删除
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	Set<String> sList(String setName);

	/**
	 * 计算一个SET的数量
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	long sCount(String setName);

	/**
	 * 返回一个有序列表中指定区间内的数据
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	Set<String> zRange(String key, long beginScore, long endScore);

	
	/**
	 * 返回一个有序列表中指定区间内的数据的个数
	 * 
	 *
	 * @author GHOST
	 * @date 2017-12-06
	 */
	long zCount(String key, long beginScore, long endScore);

	/**
	 * 排他性从一个hash表中获取一个键，并在获取后删除该键
	 * 
	 * 
	 * @author GHOST
	 * @throws Exception 
	 * @date 2019-08-25
	 */
	String removeHmExclusive(String table, String key) throws Exception;

	long hCount(String rawKeyTable);

	Map<String, String> hScan(String rawKeyTable, int i, int count);

	void setHmPlainValueBatch(String tableName, Map<String, String> data) throws Exception;

	Object getResource();

	/**
	 * 请求列出一个list中指定范围内的元素，如果要；列出所有，可以使用0,-1这个范围
	 * @param listName 列表名
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @return
	 */
	List<String> listRange(String listName, int begin, int end);
	
	


	






}
