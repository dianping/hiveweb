package com.dianping.cosmos.hive.shared.util;

import java.lang.reflect.Field;

public final class ReflectUtils {
	public static Object getFieldValue(Object aObject, String aFieldName) {
		Field field = getClassField(aObject.getClass(), aFieldName);
		
		if (field != null) {
			field.setAccessible(true);
			try {
				return field.get(aObject);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static Field getClassField(Class<?> aClazz, String aFieldName) {
		Field[] declaredFields = aClazz.getDeclaredFields();
		
		for (Field field : declaredFields) {
			if (field.getName().equals(aFieldName)) {
				return field;
			}
		}
		Class<?> superclass = aClazz.getSuperclass();
		if (superclass != null) {
			return getClassField(superclass, aFieldName);
		}
		return null;
	}

}
