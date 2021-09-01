package com.ibm.cohort.cql.util;

import java.util.Map;

public class MapUtils {
    /**
     * Retrieve the value for the specified key or throw an exception
     * 
     * @param <K> Map key type
     * @param <V> Map value type
     * @param map Map containing dataType to path mappings.
     * @param key required key
     * @return value matching required key
     */
    public static <K, V> V getRequiredKey(Map<K, V> map, K key) {
        return getRequiredKey(map, key, null);
    }

    /**
     * Retrieve the value for the specified key or throw an exception
     * 
     * @param <K> Map key type
     * @param <V>     Map value type
     * @param map     Map containing dataType to path mappings.
     * @param key     required key
     * @param context application specific description of the mapping. This will be
     *                included in the exception that is thrown and can be used to
     *                distinguish between different mappings that are used.
     * @return value matching required key
     */
    public static <K, V> V getRequiredKey(Map<K, V> map, K key, String context) {
        V value = map.get(key);
        if (value == null) {
            String contextValue = context == null ? "" : context + " ";
            throw new IllegalArgumentException(String.format("No %smapping found for key %s", contextValue, key));
        }
        return value;
    }
}
