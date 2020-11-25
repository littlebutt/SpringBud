package org.springbud.util;

public class ConvertUtil {
    public static Object primitiveNull(Class<?> type) {
        if (type == byte.class || type == short.class || type == int.class ||
                type == long.class || type == float.class || type == double.class)
            return 0;
        else if (type == boolean.class)
            return false;
        else
            return null;
    }
    public static Object convertValue(Class<?> type, String resultValue) {

        if (isPrimitive(type)) {
            if (resultValue.isEmpty())
                return primitiveNull(type);
            else {
                if (type == byte.class || type == Byte.class)
                    return Byte.parseByte(resultValue);
                else if (type == short.class || type == Short.class)
                    return Short.parseShort(resultValue);
                else if (type == int.class || type == Integer.class)
                    return Integer.parseInt(resultValue);
                else if (type == long.class || type == Long.class)
                    return Long.parseLong(resultValue);
                else if (type == float.class || type == Float.class)
                    return Float.parseFloat(resultValue);
                else if (type == double.class || type == Double.class)
                    return Double.parseDouble(resultValue);
                else if (type == String.class)
                    return resultValue;
                else
                    return Boolean.parseBoolean(resultValue);
            }
        } else {
            throw new RuntimeException("Not Implemented Type");
        }

    }

    private static boolean isPrimitive(Class<?> type) {
        return type == int.class || type == Integer.class ||
                type == byte.class || type == Byte.class ||
                type == short.class || type == Short.class ||
                type == long.class || type == Long.class ||
                type == float.class || type == Float.class ||
                type == double.class || type == Double.class ||
                type == String.class ||
                type == boolean.class || type == Boolean.class;
    }
}
