package edu.innotech;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class KeyCache{
    Map<String, Object> fields = new HashMap<>();;

    public KeyCache(Object obj)  {
        for(Field f:obj.getClass().getDeclaredFields()){
            f.setAccessible(true);
            try {
                this.fields.put(f.getName(), f.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "KeyCache{" +
                "fields=" + fields +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        System.out.println("!!!!equals");
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyCache)) {
            return false;
        }
        return this.fields.entrySet().stream().allMatch(e -> e.getValue().equals(((KeyCache) o).fields.get(e.getKey())));
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }
}