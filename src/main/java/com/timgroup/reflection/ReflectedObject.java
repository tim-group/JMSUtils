package com.timgroup.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectedObject {
    
    public static Object[] convert(Method method, List<String> arguments) {
        Object[] convertedArguments = new Object[arguments.size()];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < convertedArguments.length; i++) {
            String argument = arguments.get(i);
            Class<?> parameterType = parameterTypes[i];
            try {
                convertedArguments[i] = ConvertibleType.convert(argument, parameterType);
            }
            catch (RuntimeException e) {
                String extra = e.getMessage() != null ? " (" + e.getMessage() + ")" : "";
                throw new IllegalArgumentException("cannot convert \"" + argument + "\" to " + parameterType + extra, e);
            }
        }
        return convertedArguments;
    }
    
    private final Object image;
    
    public ReflectedObject(Object image) {
        this.image = image;
    }
    
    public Object invokeMethod(String methodName, List<String> arguments) throws IllegalArgumentException, InvocationTargetException {
        Method method = selectMethod(methodName, arguments);
        Object[] convertedArguments = ReflectedObject.convert(method, arguments);
        return invoke(method, convertedArguments);
    }
    
    public Object invoke(Method method, Object[] convertedArguments) throws IllegalArgumentException, InvocationTargetException {
        try {
            return method.invoke(image, convertedArguments);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("cannot invoke method " + method + " on object " + image, e);
        }
    }
    
    public Method selectMethod(String methodName, List<String> arguments) {
        Method[] methods = image.getClass().getMethods();
        for (Method method: methods) {
            if (method.getName().equals(methodName)) {
                if (method.getParameterTypes().length != arguments.size()) {
                    throw new IllegalArgumentException("wrong number of arguments for method: " + methodName);
                }
                return method;
            }
        }
        throw new IllegalArgumentException("no such method: " + methodName);
    }
    
}
