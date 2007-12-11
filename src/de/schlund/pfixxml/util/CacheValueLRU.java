package de.schlund.pfixxml.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Created: Tue Jul  4 10:35:21 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class CacheValueLRU<K,V> {
    private static Logger LOG = Logger.getLogger(CacheValueLRU.class);
    CacheValueLRUStack valuestack;
    HashMap<K, V> keytovalue;
    HashMap<V, HashSet<K>> valuetokeys;
    
    public CacheValueLRU(int maxsize) {
        keytovalue  = new HashMap<K, V>();
        valuetokeys = new HashMap<V, HashSet<K>>();
        valuestack  = new CacheValueLRUStack(maxsize);
    }

    public synchronized int sizeOfKeyEntries() {
        return keytovalue.size();
    }
    
    public synchronized int sizeOfKeyEntriesForValue(V value) {
        HashSet<K> keys = valuetokeys.get(value);
        if (keys != null) {
            return keys.size();
        } else {
            return -1;
        }
    }

    public synchronized int sizeOfUniqueValueEntries() {
        assert(valuetokeys.size() == valuestack.size());
        return valuetokeys.size();
    }

    public synchronized boolean containsKey(Object key) {
        return keytovalue.containsKey(key);
    }

    public synchronized V get(K key) {
        V value = keytovalue.get(key);
        if (value != null) {
            // This is done to update the linked list order of the 
            // valuestack map.
            valuestack.get(value);
        }
        LOG.debug("\nLRU: getting for key: " + key + "\n===================> LRU contains:\n" + valuestack);
        return value;
    }

    public synchronized V put(K key, V value) {
        HashSet<K> other_keys = null;
        V old = keytovalue.put(key, value);
        // we are only interested in the key for an LRU Stack
        valuestack.put(value, null);
        if (old != null) {
            other_keys = valuetokeys.get(old);
            other_keys.remove(key);
            if (other_keys.isEmpty()) {
                valuetokeys.remove(old);
                valuestack.remove(old);
            }
        }
        
        HashSet<K> keyset = valuetokeys.get(value);
        if (keyset == null) {
            keyset = new HashSet<K>();
        }
        keyset.add(key);
        valuetokeys.put(value, keyset);
        LOG.debug("\nLRU: putting for key: " + key + "\n===================> LRU contains:\n" + valuestack);
        return old;
    }

    public synchronized V remove(K key) {
        V value = keytovalue.get(key);
        HashSet<K> keyset = valuetokeys.get(value);
        if (keyset != null) {
            keyset.remove(key);
            if (keyset.isEmpty()) {
                valuetokeys.remove(value);
                valuestack.remove(value);
            }
        }
        LOG.debug("\nLRU: removing for key: " + key + "\n===================> LRU contains:\n" + valuestack);
        return keytovalue.remove(key);
    }
    
    public String toString() {
        return valuestack.toString();
    }

    private class CacheValueLRUStack extends LinkedHashMap<V,Object> {
        private int maxsize = 1;
        
        public CacheValueLRUStack(int maxsize) {
            super(5, 0.75f, true);
            if (maxsize > 0)
                this.maxsize = maxsize;
        }
        
        @Override
        protected boolean removeEldestEntry(Entry<V,Object> eldest) {
            if (size() > maxsize) {
                V value = eldest.getKey();
                HashSet<K> keys = valuetokeys.get(value);
                for (Iterator<K> iter = keys.iterator(); iter.hasNext();) {
                    K key = iter.next();
                    keytovalue.remove(key);
                }
                valuetokeys.remove(value);
                return true;
            }
            return false;
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer();
            
            for (Iterator<Entry<V,Object>> iter = entrySet().iterator(); iter.hasNext();) {
                Entry<V,Object> entry = iter.next();
                V value = entry.getKey();
                HashSet<K> keys = valuetokeys.get(value);
                buf.append(value.hashCode() +  " [");
                if (keys != null) {
                    for (Iterator<K> iterator = keys.iterator(); iterator.hasNext();) {
                        K key = iterator.next();
                        buf.append(key + " ");
                    }
                }
                buf.append("]\n");
            }
            return buf.toString();
        }
    }
}
