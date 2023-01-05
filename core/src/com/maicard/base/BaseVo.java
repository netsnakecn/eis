package com.maicard.base;

import com.maicard.security.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@Slf4j
public abstract  class BaseVo implements Serializable {

    protected  long id;

    protected int currentStatus;



    public static <T extends BaseVo, M> List<T>toVoList(Class<T> clazz, List<M> objectList) {
        if(objectList == null || objectList.size() < 1){
            return Collections.emptyList();
        }

        List<T> voList = new ArrayList<>();
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(objectList.get(0).getClass());
            /*if(constructor == null){
                log.error("对象:" + clazz.getName() + "没有适配类型:" + objectList.get(0).getClass().getName() + "的构造方法");
                return Collections.emptyList();
            }*/
            for (M object : objectList) {
                T t = constructor.newInstance(object);
                voList.add(t);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return voList;
    }

    public static  <T>  T toVo(Class<T> clazz, Object o){
        T t = null;
        try {
            t = clazz.getDeclaredConstructor(o.getClass()).newInstance(o);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                t = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return t;
    }
}
