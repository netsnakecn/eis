package com.maicard.misc;

import com.maicard.utils.NumericUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class Ognl {

    public static boolean isNotZero(Object o) throws IllegalArgumentException {

        if(isEmpty(o)){
            return false;
        }
        if(NumericUtils.parseLong(o) > 0){
            return true;
        }
        if(NumericUtils.parseFloat(o) > 0){
            return true;
        }
        return false;
    }

    public static boolean isValueArray(Object o) throws IllegalArgumentException {
        if (o == null) return false;
        if (o instanceof Collection) {
            if (((Collection) o).isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else if (o.getClass().isArray()) {
            if (Array.getLength(o) == 0) {
                return false;
            } else {
                return true;
            }
        } else if (o instanceof Map) {
            if (((Map) o).isEmpty()) {
                return false;
            } else {
                return true;
            }
        }
        return false;

    }

    public static boolean isEmpty(Object o) throws IllegalArgumentException {
        if (o == null) return true;

        if (o instanceof String) {
            if (((String) o).length() == 0) {
                return true;
            }
        } else if (o instanceof Collection) {
            if (((Collection) o).isEmpty()) {
                return true;
            }
        } else if (o.getClass().isArray()) {
            if (Array.getLength(o) == 0) {
                return true;
            }
        } else if (o instanceof Map) {
            if (((Map) o).isEmpty()) {
                return true;
            }
        } else {

            return false;
        }

        return false;
    }

    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }
}
