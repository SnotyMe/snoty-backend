package me.snoty.backend.utils;

public class ClassUtils {
	private ClassUtils() {}

	/**
	 * Returns the boxed type of a primitive type, or the type itself if it is not a primitive type.
	 */
	public static Class<?> boxOrGetType(Class<?> type) {
		if (type == Integer.TYPE) return Integer.class;
		if (type == Long.TYPE) return Long.class;
		if (type == Boolean.TYPE) return Boolean.class;
		if (type == Double.TYPE) return Double.class;
		if (type == Float.TYPE) return Float.class;
		if (type == Byte.TYPE) return Byte.class;
		if (type == Short.TYPE) return Short.class;
		if (type == Character.TYPE) return Character.class;
		return type;
	}
}
