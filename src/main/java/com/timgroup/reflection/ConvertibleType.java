package com.timgroup.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.timgroup.util.Utils;

public enum ConvertibleType {
    
    STRING(String.class) {
        @Override
        protected Object convert(String string) {
            return string;
        }
    },
    BYTE(Byte.class) {
        @Override
        protected Object convert(String string) {
            return Byte.valueOf(string);
        }
    },
    SHORT(Short.class) {
        @Override
        protected Object convert(String string) {
            return Short.valueOf(string);
        }
    },
    INT(Integer.class) {
        @Override
        protected Object convert(String string) {
            return Integer.valueOf(string);
        }
    },
    LONG(Long.class) {
        @Override
        protected Object convert(String string) {
            return Long.valueOf(string);
        }
    },
    FLOAT(Float.class) {
        @Override
        protected Object convert(String string) {
            return Float.valueOf(string);
        }
    },
    DOUBLE(Double.class) {
        @Override
        protected Object convert(String string) {
            return Double.valueOf(string);
        }
    },
    BOOLEAN(Boolean.class) {
        @Override
        protected Object convert(String string) {
            return Boolean.valueOf(string);
        }
    },
    CHAR(Character.class) {
        @Override
        protected Object convert(String string) {
            if (string.length() != 0) throw new IllegalArgumentException("cannot convert a non-singleton string to a character: " + string);
            return string.charAt(0);
        }
    },
    OBJECT(Object.class) {
        @Override
        public <T> T convertTo(String string, Class<T> targetType) {
            try {
                Constructor<T> constructor = targetType.getConstructor(String.class);
                return constructor.newInstance(string);
            }
            catch (InvocationTargetException e) {
                throw new IllegalArgumentException("value \"" + string + "\" cannot be converted to type " + targetType, e.getCause());
            }
            catch (Exception e) {
                throw new IllegalArgumentException("value \"" + string + "\" cannot be converted to type " + targetType, e);
            }
        }
        
        @Override
        protected Object convert(String string) {
            throw new UnsupportedOperationException();
        }
    };
    
    public static final Map<Class<?>, ConvertibleType> BY_TYPE;
    static {
        Map<Class<?>, ConvertibleType> byType = new HashMap<Class<?>, ConvertibleType>();
        ConvertibleType[] values = values();
        for (ConvertibleType convertibleType: values) {
            Class<?> type = convertibleType.getType();
            byType.put(type, convertibleType);
        }
        BY_TYPE = Collections.unmodifiableMap(byType);
    }
    
    public static ConvertibleType forTargetType(Class<?> parameterType) {
        return Utils.defaulting(BY_TYPE.get(WrapperUtil.wrap(parameterType)), null, OBJECT);
    }
    
    public static Object convert(String argument, Class<?> parameterType) {
        return forTargetType(parameterType).convertTo(argument, parameterType);
    }
    
    private final Class<?> type;
    
    private ConvertibleType(Class<?> type) {
        this.type = type;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public <T> T convertTo(String string, Class<T> targetType) {
        targetType = WrapperUtil.wrap(targetType);
        if (!targetType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("cannot convert value \"" + string + "\" to " + targetType + " as " + type);
        }
        return targetType.cast(convert(string));
    }
    
    protected abstract Object convert(String string);
    
}
