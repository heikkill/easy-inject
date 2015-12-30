package com.easyinject.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

public class EasyInjectUtils {

	public static Class<?> getParametrizedType(Field field) {
		ParameterizedType listType = (ParameterizedType) field.getGenericType();
        return (Class<?>) listType.getActualTypeArguments()[0];
	}
	
	public static Object getFieldValue(Object target, Field field) throws IllegalArgumentException, IllegalAccessException {
		if (field.isAccessible()) {
			return field.get(target);
		}
		else {
			field.setAccessible(true);
			Object value = field.get(target);
			field.setAccessible(false);
			return value;
		}
	}
	
	public static void setFieldValue(Object target, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		if (field.isAccessible()) {
			field.set(target, value);
		}
		else {
			field.setAccessible(true);
			field.set(target, value);
			field.setAccessible(false);
		}
	}
	
	public static Object invokeMethod(Object target, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (method.isAccessible()) {
			return method.invoke(target);
		}
		else {
			method.setAccessible(true);
			Object rtn = method.invoke(target);
			method.setAccessible(false);
			return rtn;
		}
	}

	public static <E> String toString(Set<E> set) {
		StringBuffer sb = new StringBuffer();
		for (E item : set) {
			sb.append(sb.length() > 0 ? ", " : "");
			sb.append(item.toString());
		}
		return sb.toString();
	}
}