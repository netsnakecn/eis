package com.maicard.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.exception.EisException;
import com.maicard.core.iface.ExtraValueAccess;
import com.maicard.pe.IntArrayPropertyEditor;
import com.maicard.pe.StringDatePropertyEditor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
 
public class ClassUtils {

	protected static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

	static {
		PropertyEditorManager.registerEditor(int[].class, IntArrayPropertyEditor.class);
		PropertyEditorManager.registerEditor(Date.class, StringDatePropertyEditor.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes"})
	public static void findAllClass(Object object, HashMap<String,Class> classMap){
		if(object == null){
			return;
		}
		//List<Class<Object>> allClass = new ArrayList<Class<Object>>();
		if(object.getClass().isPrimitive()){
			return;
		}
		if(object instanceof java.util.List){
			logger.debug("递归查找List[" + object.toString() + "]");
			for(Object subObj : (List<Object>)object){
				findAllClass(subObj, classMap);

			}

		} else if(object instanceof java.util.Collection){
			logger.debug("递归查找Collection[" + object.toString() + "]");
			Iterator it = ((Collection)object).iterator();
			while(it.hasNext()){
				findAllClass(it.next(), classMap);
			}
		} else {
			//查询对象中的属性
			try{
				BeanInfo bif = Introspector.getBeanInfo(object.getClass());
				PropertyDescriptor pds[] = bif.getPropertyDescriptors();
				if(pds != null){
					for(PropertyDescriptor pd:pds){
						if(pd.getPropertyType().isPrimitive()){
							continue;
						}

						if(pd.getPropertyType().getName().indexOf("List") >= 0){
							logger.debug("递归查找属性List[" + object.toString() + "]");
							Method method = pd.getReadMethod();
							List<Object> listObject = null;
							try{
								listObject = (List<Object>)method.invoke(object, new Object[0]);
							}catch(Exception e){}
							if(listObject != null && listObject.size() > 0){
								for(Object subObj : listObject){
									findAllClass(subObj, classMap);
								}
							}
						}
						if(pd.getPropertyType().getClass().getName().indexOf("Map") >= 0){
							logger.error("递归查找属性Map[" + object.toString() + "]");
							Method method = pd.getReadMethod();
							Map<String, Object> mapObject = null;
							try{
								mapObject = (Map<String,Object>)method.invoke(object, new Object[0]);
							}catch(Exception e){}
							if(mapObject != null && mapObject.size() > 0){
								for(Object subObj : mapObject.values()){
									findAllClass(subObj, classMap);
								}
							}
						}



						logger.debug("放入对象的属性:" + pd.getPropertyType().getName() );
						classMap.put(pd.getPropertyType().getName(), pd.getPropertyType());					
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			logger.debug("放入对象:" + object.getClass().getName() );
			classMap.put(object.getClass().getName(), object.getClass());
		}

		return;

	}
	public static void  bindBeanFromMap(Object object, Map<String,?> requestDataMap ){
		bindBeanFromMap(object, requestDataMap, null);		
	}

	public static void  bindBeanFromMap(Object object, Map<String,?> requestDataMap, String prefix){

		BeanInfo bif = null;
		try {
			bif = Introspector.getBeanInfo(object.getClass());
		} catch (IntrospectionException e1) {
			e1.printStackTrace();
		}
		if(bif == null){
			logger.error("无法获取[" + object.getClass().getName() + "]的类信息");
			return;
		}
		PropertyDescriptor pds[] = bif.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			for (String attributeName : requestDataMap.keySet()) {
				String modifyAttributeName = attributeName;
				if(prefix != null) {
					if(!attributeName.startsWith(prefix)) {
						continue;
					}
					modifyAttributeName = attributeName.replaceFirst("^" + prefix , "");
				}
			//	logger.info("检查写入数据名字:{}，当前属性名字:{}", attributeName, pd.getName());
				if (pd.getName().equals(modifyAttributeName)) {
					Method method = pd.getWriteMethod();
					String text = requestDataMap.get(attributeName).toString();
					try {
						/*String parameterDesc = null;
						Class<?>[] paraTypes = method.getParameterTypes();
						if(paraTypes != null && paraTypes.length > 0){
							for(Class<?> para : paraTypes){
								parameterDesc += para.getName() + ",";
							}
						}
						logger.debug("尝试对对象[" + object.getClass().getName() + "]反射执行[" + method.getName() + ",参数:" + parameterDesc + "],提交参数:" + map.get(attributeName));
						 */
						PropertyEditor pe = PropertyEditorManager.findEditor(pd.getPropertyType());
						if(pe == null){
							logger.error("找不到对象[" + object.getClass().getName() + "]的反射方法:" + method.getName());
							continue;
						} 
						pe.setAsText(text);
						method.invoke(object, pe.getValue());
					}catch(Exception e){
						logger.error("在尝试写入对象:{}的属性:{}的值:{} 出错:{}",object.getClass().getName(), modifyAttributeName, text, e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
	 

	


	//为指定的类的某个属性设置指定的值
	public static boolean setAttribute(Object targetObject, String attributeName, String attributeValue, String columnType) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		if(columnType != null && columnType.equalsIgnoreCase(Constants.COLUMN_TYPE_EXTRA)){
			if(targetObject instanceof ExtraValueAccess ea){
				ea.setExtra(attributeName, attributeValue);
				return true;
			} else {
				/*try {
					Method setMethod = targetObject.getClass().getMethod("setExtra", String.class, String.class);
						setMethod.invoke(targetObject, attributeName, attributeValue);
						return true;
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}*/
			}
			logger.error("设置属性为扩展，但目标对象不是一个ExtraValueAccess");
			return false;
		}


		BeanInfo bif = Introspector.getBeanInfo(targetObject.getClass());
		if(bif == null){
			logger.error("找不到对象的类信息:" + targetObject.getClass().getName());
			return false;
		}
		PropertyDescriptor pds[] = bif.getPropertyDescriptors();
		if(pds == null || pds.length < 1){
			logger.error("找不到对象的类信息的属性描述:" + targetObject.getClass().getName());
			return false;
		}
		for(PropertyDescriptor pd:pds){
			if(pd.getName().equals(attributeName)){
				Method writeMethod = pd.getWriteMethod();
				PropertyEditor pe = PropertyEditorManager.findEditor(pd.getPropertyType());
				if(pe != null && writeMethod != null ){
					pe.setAsText(attributeValue);
					writeMethod.invoke(targetObject, pe.getValue());
					if(logger.isDebugEnabled()){
						logger.debug("完成对象的原始属性写入[" + attributeName + "=>" + attributeValue + "]");
					}
					return true;
				} else {
					logger.error("找不到属性[" + attributeName + "]对应的写入编辑器");
					return false;
				}
			}
		}
		return false;
	}

	
	/**
	 * 返回对象中指定的属性值
	 */
	public static String getValue(Object object, String attributeName, String columnType) {
		if(columnType == null || columnType.equalsIgnoreCase(Constants.COLUMN_TYPE_EXTRA)){
			if(object instanceof ExtraValueAccess){
				ExtraValueAccess ea = (ExtraValueAccess)object;
				String extraValue = ea.getExtra(attributeName);
				if(extraValue != null){
					return extraValue;
				}
			}
			if(columnType != null && columnType.equalsIgnoreCase(Constants.COLUMN_TYPE_EXTRA)){
				logger.error("读取属性为扩展属性，但目标对象不是一个ExtraValueAccess");
				return null;
			}
		}
		BeanInfo bif = null;
		try {
			bif = Introspector.getBeanInfo(object.getClass());
		} catch (IntrospectionException e1) {
			e1.printStackTrace();
		}
		if(bif == null){
			logger.error("无法获取[" + object.getClass().getName() + "]的类信息");
			return null;
		}
		PropertyDescriptor pds[] = bif.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd.getName().equals(attributeName)) {
				Method method = pd.getReadMethod();
				try {
					Object result = method.invoke(object, new Object[]{});
					if(result != null){
						return result.toString();
					} 
				}catch(Exception e){
					e.printStackTrace();
				}
				return null;
			}

		}
		return null;
	}

	/**
	 * 尝试获取一个eis对象的类型
	 */
	public static String getEntityType(Class<?> clazz) {
		/*if(eisObject.getObjectType() != null){
			return eisObject.getObjectType();
		}*/
		return StringUtils.uncapitalize(clazz.getSimpleName());
	}

	/**
	 * 尝试获取一个eis对象的id
	 * 
	 * @param eisObject
	 * @return
	 */
	public static long getObjectId(BaseEntity eisObject) {
		if(eisObject.getId() > 0){
			return eisObject.getId();
		}
		String value = getValue(eisObject, getEntityType(eisObject.getClass()) + "Id", Constants.COLUMN_TYPE_NATIVE);
		if(NumericUtils.isNumeric(value)){
			return Long.parseLong(value);
		}
		return 0;
	}

	public static void copyProperties(Object fromObject, Object toObject, Set<String> copyProperties) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		if(fromObject.getClass() != toObject.getClass()){
			return;
		}
		BeanInfo bif = Introspector.getBeanInfo(fromObject.getClass());
		if(bif == null){
			logger.error("找不到对象的类信息:" + fromObject.getClass().getName());
			return ;
		}
		PropertyDescriptor pds[] = bif.getPropertyDescriptors();
		if(pds == null || pds.length < 1){
			logger.error("找不到对象的类信息的属性描述:" + fromObject.getClass().getName());
			return;
		}
		for(PropertyDescriptor pd:pds){
			for(String attributeName : copyProperties){
				if(pd.getName().equals(attributeName)){
					Method writeMethod = pd.getWriteMethod();
					Method readMethod = pd.getReadMethod();
					PropertyEditor pe = PropertyEditorManager.findEditor(pd.getPropertyType());
					if(pe != null ){
						Object attributeValue = readMethod.invoke(fromObject, new Object[]{});
						if(attributeValue == null){
							logger.error("无法读取来源对象的:" + attributeName);
							break;
						}
						pe.setAsText(attributeValue.toString());
						writeMethod.invoke(toObject, pe.getValue());
						if(logger.isDebugEnabled()){
							logger.debug("完成对象的属性写入[" + attributeName + "=>" + attributeValue + "]");
						}
					} else {
						logger.error("找不到属性[" + attributeName + "]对应的写入编辑器");
					}
					break;
				}
			}
		}
	}

	/**
	 * 根据指定条件在一个对象列表中
	 * 根据查询条件返回符合条件的对象
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> search(List<?> targetList, CriteriaMap params) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		BeanInfo elementBi = Introspector.getBeanInfo(targetList.get(0).getClass());
		 
		PropertyDescriptor elementPd[] = elementBi.getPropertyDescriptors();
		if(elementPd == null || elementPd.length < 1){
			logger.error("找不到查询列表对象的类信息的属性描述:" + targetList.get(0).getClass().getName());
			return Collections.emptyList();
		}
		List<Object> copyList = new ArrayList<Object>();
		copyList.addAll(targetList);
		//System.out.println("待处理:" + JSON.toJSONString(copyList));
		for(Entry<String,Object>entry : params.entrySet()){
			for(PropertyDescriptor epd : elementPd){
				 
				if(entry.getKey().equals(epd.getName())){
					//logger.info("比较相同的属性:" + pd.getName());
					Method elementRm = epd.getReadMethod();
					Object paramV = entry.getValue();
					int i = 0;
					for(Object element : targetList) {
						Object elementV = elementRm.invoke(element, new Object[] {});
						if(elementV == null) {
							//没有该属性
							continue;
						}
						if(paramV == null) {
							//没有设置该查询条件
							continue;
						}
						//logger.debug("检查第:" + i + "个元素:" + JSON.toJSONString(element) + ",查询参数类型是:" + paramV.getClass().getName());
						if(paramV.getClass().isArray()) {
							logger.debug("检查数组型查询参数:" + entry.getKey() + "=>" + paramV.getClass().getComponentType());
							int length = Array.getLength(paramV);
							for(int j = 0; j < length; j++) {
								Object paramElementV = Array.get(paramV, j);
								if(paramElementV instanceof String && !paramElementV.toString().equals(elementV.toString())) {
									logger.debug("1第" + i + "个元素的字符串属性:" + entry.getKey() + "，值:" + elementV.toString() + "与参数的值:" + paramElementV.toString() + "不一致，去掉该元素");
									copyList.set(i, null);
									break;
								} else if(paramElementV.toString().equals(elementV.toString())){
									
								} else  if(!paramElementV.equals(elementV)) {
									logger.debug("2第" + i + "个元素的属性:" + entry.getKey() + "，值:" + elementV.toString() + "与参数的值:" + paramElementV.toString() + "不一致，去掉该元素");
									copyList.set(i, null);
									break;
								}
							}
						} else if(elementV instanceof String && !paramV.toString().equalsIgnoreCase(elementV.toString())) {
							//属性不一致，删除
							logger.debug("3第" + i + "个元素的字符串属性:" + entry.getKey() + "，值:" + elementV.toString() + "与参数的值:" + paramV.toString() + "不一致，去掉该元素");
							copyList.set(i, null);
						} else if(paramV.toString().equals("0") || paramV.toString().equals(elementV.toString())){
							
						}
						else  if(!paramV.equals(elementV)) {
							logger.debug("4第" + i + "个元素的属性:" + entry.getKey() + "，值:" + elementV.toString() + "与参数的值:" + paramV.toString() + "不一致，去掉该元素");
						copyList.set(i, null);
						}

						i++;
					}

				}
			}
		}
		List<T> returnList = new ArrayList<T>();
		if(copyList.size() < 1) {
			return returnList;
		}
		for(Object o : copyList) {
			if(o != null) {
				returnList.add((T)o);
			}
		}
		return returnList;

	}

    public static Set<String> getPropertyNames(Class<?> clazz) {
		BeanInfo bif = null;
		try {
			bif = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e1) {
			e1.printStackTrace();
		}
		if(bif == null){
			throw new EisException(EisError.DATA_ILLEGAL.getId(),"");
		}
		Set<String> names = new HashSet<>();
		PropertyDescriptor[] pds = bif.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			names.add(pd.getName());
		}
		return names;
    }

	public static <T> T textToObj(Class<T> clazz, String text) {
		if(StringUtils.isBlank(text)){
			return  null;
		}
		try {
			return JsonUtils.getInstance().readValue(text,clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		// log.info("转换后:" + JsonUtils.toStringFull(t));
	}

	public static <T> T jsonToObj(Class<T> clazz, JsonNode json) {
		if(json == null || json.isNull() || json.isEmpty()){
			return null;
		}
		return textToObj(clazz, json.toString());
	}
}