package com.timgroup.reflection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WrapperUtil {
    
    public static final Map<Class<?>, Class<?>> WRAPPERS;
    static {
        Map<Class<?>, Class<?>> wrappers = new HashMap<Class<?>, Class<?>>();
        wrappers.put(byte.class, Byte.class);
        wrappers.put(short.class, Short.class);
        wrappers.put(int.class, Integer.class);
        wrappers.put(long.class, Long.class);
        wrappers.put(float.class, Float.class);
        wrappers.put(double.class, Double.class);
        wrappers.put(boolean.class, Boolean.class);
        wrappers.put(char.class, Character.class);
        WRAPPERS = Collections.unmodifiableMap(wrappers);
    }
    
    /**
     * VOODOO.
     */
    public static <T> Class<T> wrap(Class<T> cl) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<T> wrapped = (Class) (cl.isPrimitive() ? WRAPPERS.get(cl) : cl);
        return wrapped;
    }
    
    private WrapperUtil() {}
    
}
