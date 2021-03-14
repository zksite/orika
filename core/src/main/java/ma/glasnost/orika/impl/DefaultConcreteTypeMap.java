package ma.glasnost.orika.impl;

import ma.glasnost.orika.MapEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class DefaultConcreteTypeMap {

    private static final Map<Class, Class> map;
    static {
        Map<Class, Class> tmpMap = new HashMap<>();
        tmpMap.put(Collection.class, ArrayList.class);
        tmpMap.put(List.class, ArrayList.class);
        tmpMap.put(Set.class, LinkedHashSet.class);
        tmpMap.put(Map.class, LinkedHashMap.class);
        tmpMap.put(Map.Entry.class, MapEntry.class);
        tmpMap.put(SortedMap.class, TreeMap.class);
        tmpMap.put(SortedSet.class, TreeSet.class);
        map = Collections.unmodifiableMap(tmpMap);
    }
    public static Set<Map.Entry<Class, Class>> getAll() {
        return map.entrySet();
    }

    public static Class get(Class<?> type) {
        return map.get(type);
    }
}
