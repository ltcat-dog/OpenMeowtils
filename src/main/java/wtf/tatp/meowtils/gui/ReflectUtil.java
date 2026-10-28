package wtf.tatp.meowtils.gui;

import java.lang.reflect.Field;
import wtf.tatp.meowtils.Meowtils;

public class ReflectUtil {
    public static Field bindField(Object owner, String fieldName, Class<?> expectedType) {
        if (owner != null && fieldName != null && !fieldName.isEmpty()) {
            try {
                Field field = owner.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                if (expectedType != null && !isTypeCompatible(field.getType(), expectedType)) {
                    Meowtils.warn(
                        "Field " + fieldName + " type mismatch, expected: " + expectedType.getSimpleName() + " but got " + field.getType().getSimpleName()
                    );
                }

                return field;
            } catch (NoSuchFieldException var4) {
                throw new RuntimeException("Invalid config field (" + fieldName + ") in " + owner.getClass().getSimpleName());
            }
        } else {
            return null;
        }
    }

    public static <T> T get(Field field, Object instance, T def) {
        if (field != null && instance != null) {
            try {
                Object value = field.get(instance);
                return (T)(value == null ? def : value);
            } catch (Exception var4) {
                Meowtils.error("Failed to get field value: " + var4);
                return def;
            }
        } else {
            return def;
        }
    }

    public static boolean set(Field field, Object instance, Object value) {
        if (field != null && instance != null) {
            try {
                if (value instanceof Number && Number.class.isAssignableFrom(field.getType())) {
                    value = castNumber(((Number)value).doubleValue(), field.getType());
                }

                field.set(instance, value);
                return true;
            } catch (Exception var4) {
                Meowtils.error("Failed to set field " + field.getName() + ": " + var4);
                return false;
            }
        } else {
            return false;
        }
    }

    public static Object castNumber(double value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return (int)value;
        } else if (type == float.class || type == Float.class) {
            return (float)value;
        } else if (type == long.class || type == Long.class) {
            return (long)value;
        } else if (type != double.class && type != Double.class) {
            throw new IllegalArgumentException("Unsupported numeric type: " + type.getName());
        } else {
            return value;
        }
    }

    private static boolean isTypeCompatible(Class<?> fieldType, Class<?> expected) {
        if (fieldType == expected) {
            return true;
        } else if (fieldType.isPrimitive()) {
            return wrap(fieldType) == expected;
        } else if (expected.isPrimitive()) {
            return wrap(expected) == fieldType;
        } else if (Number.class.isAssignableFrom(fieldType) && Number.class.isAssignableFrom(expected)) {
            return true;
        } else {
            return fieldType != Boolean.class && fieldType != boolean.class || expected != Boolean.class && expected != boolean.class
                ? expected.isAssignableFrom(fieldType)
                : true;
        }
    }

    private static Class<?> wrap(Class<?> c) {
        if (c == int.class) {
            return Integer.class;
        } else if (c == float.class) {
            return Float.class;
        } else if (c == double.class) {
            return Double.class;
        } else if (c == long.class) {
            return Long.class;
        } else if (c == boolean.class) {
            return Boolean.class;
        } else if (c == byte.class) {
            return Byte.class;
        } else if (c == short.class) {
            return Short.class;
        } else {
            return c == char.class ? Character.class : c;
        }
    }
}
