package com.timgroup.reflection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WrapperUtil {
    
    public static final Map<Class<?>, Class<?>> WRAPPERS;
    public static final Map<String, Class<?>> WRAPPERS_BY_NAME;
    public static final Map<Class<?>, String> NAMES_BY_WRAPPER;
    static {
        Map<Class<?>, Class<?>> wrappers = new HashMap<Class<?>, Class<?>>();
        Map<String, Class<?>> wrappersByName = new HashMap<String, Class<?>>();
        Map<Class<?>, String> namesByWrapper = new HashMap<Class<?>, String>();
        put(wrappers, wrappersByName, namesByWrapper, byte.class, Byte.class);
        put(wrappers, wrappersByName, namesByWrapper, short.class, Short.class);
        put(wrappers, wrappersByName, namesByWrapper, int.class, Integer.class);
        put(wrappers, wrappersByName, namesByWrapper, long.class, Long.class);
        put(wrappers, wrappersByName, namesByWrapper, float.class, Float.class);
        put(wrappers, wrappersByName, namesByWrapper, double.class, Double.class);
        put(wrappers, wrappersByName, namesByWrapper, boolean.class, Boolean.class);
        put(wrappers, wrappersByName, namesByWrapper, char.class, Character.class);
        WRAPPERS = Collections.unmodifiableMap(wrappers);
        WRAPPERS_BY_NAME = Collections.unmodifiableMap(wrappersByName);
        NAMES_BY_WRAPPER = Collections.unmodifiableMap(namesByWrapper);
    }
    
    private static void put(Map<Class<?>, Class<?>> wrappers,
                            Map<String, Class<?>> wrappersByName,
                            Map<Class<?>, String> namesByWrapper,
                            Class<?> primitiveClass,
                            Class<?> wrapperClass) {
        wrappers.put(primitiveClass, wrapperClass);
        wrappersByName.put(primitiveClass.getName(), wrapperClass);
        namesByWrapper.put(wrapperClass, primitiveClass.getName());
    }
    
    /**
     * VOODOO.
     */
    public static <T> Class<T> wrap(Class<T> cl) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<T> wrapped = (Class) (cl.isPrimitive() ? WRAPPERS.get(cl) : cl);
        return wrapped;
    }
    
    public static Class<?> forName(String typeName) {
        Class<?> cl = WRAPPERS_BY_NAME.get(typeName);
        if (cl == null) {
            try {
                cl = Class.forName(typeName);
            }
            catch (ClassNotFoundException e) {
                cl = String.class;
            }
        }
        return cl;
    }
    
    public static String getName(Class<?> type) {
        String name = NAMES_BY_WRAPPER.get(type);
        if (name == null) name = type.getName();
        return name;
    }
    
    private WrapperUtil() {}
    
}
