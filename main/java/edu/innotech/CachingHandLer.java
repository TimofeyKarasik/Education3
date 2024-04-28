package edu.innotech;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CacheObject {
    Object value;
    long expirationTime;
    CacheObject(Object value, long validTime){
        this.value = value;
        this.expirationTime = System.currentTimeMillis() + validTime;
    }

    public void renewExpiration(long validTime) {
        this.expirationTime = System.currentTimeMillis() + validTime;
    }

    public boolean isExpired(){
        return System.currentTimeMillis() > this.expirationTime;
    }
}

class CachingHandLer implements InvocationHandler, Cleanable {


    private Object objectIncome;
    private Map<KeyCache, Map<Method, CacheObject>> cacheObj = new ConcurrentHashMap<>();
    private KeyCache keyCache;

    public CachingHandLer(Object objectIncome) {
        this.objectIncome = objectIncome;
        this.keyCache = new KeyCache(objectIncome);
        this.cacheObj.put(keyCache, new HashMap<>());
        CacheCleaner.submitCleaning(this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method methodClass =this.objectIncome.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (methodClass.isAnnotationPresent(Cache.class)){
            Cache cacheAnnotation = methodClass.getAnnotation(Cache.class);
            if (! cacheObj.containsKey(keyCache)){
                cacheObj.put(keyCache, new HashMap<>());
            }
            Map<Method, CacheObject> tmpCache = new HashMap<>(cacheObj.get(keyCache));
            if (!tmpCache.containsKey(methodClass)){
                tmpCache.put(methodClass, new CacheObject(method.invoke(this.objectIncome, args), cacheAnnotation.value()));
            }
            tmpCache.get(methodClass).renewExpiration(cacheAnnotation.value());
            cacheObj.put(keyCache, tmpCache);
            return tmpCache.get(methodClass).value;
        }
        Object res = method.invoke(this.objectIncome, args);
        if (methodClass.isAnnotationPresent(Mutator.class)){
            keyCache = new KeyCache(this.objectIncome);
            if (!cacheObj.containsKey(keyCache)){
                cacheObj.put(keyCache, new HashMap<>());
            }
        }
        return res;
    }

    public boolean clean(){
        boolean updated = false;
        for (KeyCache capture: cacheObj.keySet()){
            Map<Method, CacheObject> tmpCache = new ConcurrentHashMap<>(cacheObj.get(capture));
            for (Method method : tmpCache.keySet()){
                if (tmpCache.get(method).isExpired()){
                    tmpCache.remove(method);
                    updated = true;
                }
            }
            if (updated) {
                if (tmpCache.isEmpty()) {
                    cacheObj.remove(capture);
                } else {
                    cacheObj.replace(capture, tmpCache);
                }
            }
        }
        return cacheObj.isEmpty();
    }
}
