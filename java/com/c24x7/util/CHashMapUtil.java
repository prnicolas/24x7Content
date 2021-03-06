// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util;

import java.util.*;
import java.io.*;

public class CHashMapUtil<K, V>  implements Cloneable, Serializable, Map<K, V> {

		    static final float DEFAULT_LOAD_FACTOR = 0.75f;
	
		    static class Entry<K, V> {
		        K key;
		        V value;
		        int hc;
		        Entry<K, V> next;
		    }
	
		    transient protected Object[] keyValueTable;
		    transient protected int[] indexTable;
		    transient protected Entry<K, V>[] overflowTable;
	
		    transient int size = 0;
		    final float loadEstimator;
		    int threshold = 0;
	
		    static int DEFAULT_INITIAL_CAPACITY = 1024;
	
		    public CHashMapUtil() {
		        loadEstimator = DEFAULT_LOAD_FACTOR;
		    }
		    
		    public CHashMapUtil(int initialCapacity) {
		        loadEstimator = DEFAULT_LOAD_FACTOR;
		        DEFAULT_INITIAL_CAPACITY = initialCapacity;
		    }
	
		    /**
		* Applies a supplemental hash function to a given object's hashCode, which
		* defends against poor quality hash functions. This is critical because
		* HashMap uses power-of-two length hash tables, that otherwise encounter
		* collisions for hashCodes that do not differ in lower bits. Note: Null
		* keys always map to hash 0, thus index 0.
		*/
		    final static int hash(int h) {
		        // This function ensures that hashCodes that differ only by
		        // constant multiples at each bit position have a bounded
		        // number of collisions (approximately 8 at default load estimator).
		        h ^= (h >>> 20) ^ (h >>> 12);
		        return h ^ (h >>> 7) ^ (h >>> 4);
		    }
	
		    transient protected boolean nullKeyPresent = false;
		    transient protected V nullValue;
	
		    protected final static int FOREIGN = 0x80000000;
		    protected final static int AVAILABLE_BITS = 0x7FFFFFFF;
	
		    public V get(Object key) {
		        return get(key, null);
		    }
	
		    /**
		* @param key
		* @param notFoundValue
		* @return
		*/
		    protected final V get(Object key, V notFoundValue) {
		        // since null in keyValueTable represents an empty cell
		        // we have to handle null keys as a special case
		        if (key == null)
		            return nullKeyPresent ? nullValue : notFoundValue;
	
		        // local array copies are faster (?)
		        int[] indices = indexTable;
		        Object[] kv = keyValueTable;
	
		        // check if arrays were already initialised
		        if (kv == null)
		            return notFoundValue;
	
		        //
		        int mask = indices.length - 1;
		        int hc = hash(key.hashCode());
		        int hcMask = AVAILABLE_BITS & ~mask;
		        int i0 = hc & mask, i = i0;
	
		        // referential equality is very frequent case, check it early
		        Object key1 = kv[i << 1];
		        if (key == key1) {
		            @SuppressWarnings("unchecked")
		            V result = (V) kv[(i << 1) + 1];
		            return result;
		        }
	
		        //
		        if (key1 == null)
		            return notFoundValue;
		        int ci = indices[i];
		        if ((ci & FOREIGN) != 0)
		            return notFoundValue;
	
		        //
		        int hcBits = hc & hcMask;
		        if ((ci & hcMask) == hcBits && key.equals(key1)) {
		            @SuppressWarnings("unchecked")
		            V result = (V) kv[(i << 1) + 1];
		            return result;
		        }
	
		        //
		        while ((i = ci & mask) != i0) {
		            ci = indices[i];
		            if ((ci & hcMask) == hcBits) {
		                key1 = kv[i << 1];
		                if (key == key1 || key.equals(key1)) {
		                    @SuppressWarnings("unchecked")
		                    V result = (V) kv[(i << 1) + 1];
		                    return result;
		                }
		            }
		        }
	
		        // Look in old-style Entry-based overflow (very huge maps)
		        Entry<K, V>[] over = overflowTable;
		        if (over != null) {
		            for (Entry<K, V> e = over[i]; e != null; e = e.next) {
		                if (key == e.key || (hc == e.hc && key.equals(e.key)))
		                    return e.value;
		            }
		        }
	
		        // Nothing was found
		        return notFoundValue;
		    }
	
		    public V put(K key, V value) {
		        // null key in keyValueTable represents empty cell
		        // so we handle null keys as a special case
		        if (key == null) {
		            V oldNullValue = nullValue;
		            if (!nullKeyPresent) {
		                nullKeyPresent = true;
		                size++;
		            }
		            nullValue = value;
		            return oldNullValue;
		        }
	
		        // ... also lazy initialise key/value/index arrays there
		        // ToDo: maybe we can somehow call resize() only when new key is added?
		        if (size >= threshold)
		            resize();
	
		        // local array copies are faster (?)
		        int[] indices = indexTable;
		        Object[] kv = keyValueTable;
	
		        //
		        int mask = indices.length - 1;
		        int hc = hash(key.hashCode());
		        int hcMask = ~mask & AVAILABLE_BITS;
		        int i0 = hc & mask, i = i0;
		        int hcBits = hc & hcMask;
	
		        //
		        Object key1 = kv[i << 1];
		        if (key1 == key) {
		            @SuppressWarnings("unchecked")
		            V oldValue = (V) kv[(i << 1) + 1];
		            kv[(i << 1) + 1] = value;
		            return oldValue;
		        }
	
		        //
		        if (key1 == null) {
		            kv[i << 1] = key;
		            kv[(i << 1) + 1] = value;
		            indices[i] = hcBits | i0;
		            size++;
		            return null;
		        }
	
		        // Check if this cell is occupied by another hash chain
		        int ci = indices[i], c0 = ci;
		        if ((ci & FOREIGN) != 0) {
		            relocate(i, ci);
		            kv[i << 1] = key;
		            kv[(i << 1) + 1] = value;
		            indices[i] = hcBits | i0;
		            size++;
		            return null;
		        }
	
		        // OK, now we know that this hash bin is not empty => search
		        while (true) {
		            if ((ci & hcMask) == hcBits) {
		                key1 = kv[i << 1];
		                if (key1 == key || key.equals(key1)) {
		                    @SuppressWarnings("unchecked")
		                    V oldValue = (V) kv[(i << 1) + 1];
		                    kv[(i << 1) + 1] = value;
		                    return oldValue;
		                }
		            }
		            int next = ci & mask;
		            if (next == i0)
		                break;
		            ci = indices[i = next];
		        }
	
		        // So, hash chain is not empty but our key was not found => insert
		        int newIndex = findFreeSpot(i, mask, hc);
		        indices[newIndex] = FOREIGN | hcBits | (c0 & mask);
		        indices[i0] = (c0 & ~mask) | newIndex;
		        kv[newIndex << 1] = key;
		        kv[(newIndex << 1) + 1] = value;
		        size++;
		        return null;
	
		        // ToDo: overflowTable
		    }
	
		    /**
		*
		*/
		    final protected void resize() {
		        // ToDo: argument with new size
	
		        Object[] oldKV = keyValueTable;
		        int[] oldIT = indexTable;
	
		        int oldLen = 0;
		        int newCapacity = DEFAULT_INITIAL_CAPACITY;
		        if (oldIT != null) {
		            oldLen = oldIT.length;
		            newCapacity = oldLen << 1; // ToDo: check for 1<<29
		        }
		        int oldMask = oldLen - 1, newMask = newCapacity - 1;
		        int deltaMask = oldMask ^ newMask;
		        int newHcMask = AVAILABLE_BITS & ~newMask;
	
		        Object[] newKV = new Object[newCapacity << 1];
		        int[] newIT = new int[newCapacity];
	
		        //
		        for (int i = 0; i < oldLen; i++) {
		            Object key = oldKV[i << 1];
		            if (key != null) {
		                int ci = oldIT[i];
		                if ((ci & FOREIGN) == 0) {
		                    int j = i;
		                    int c0 = -1, c1 = -1;
		                    while (true) {
		                        int delta = ci & deltaMask;
		                        int newHash = i | delta;
	
		                        //
		                        if (delta == 0) {
		                            if (c0 < 0) {
		                                newKV[newHash << 1] = key;
		                                newKV[(newHash << 1) + 1] = oldKV[(j << 1) + 1];
		                                c0 = (ci & newHcMask) | newHash;
		                            } else {
		                                newKV[j << 1] = key;
		                                newKV[(j << 1) + 1] = oldKV[(j << 1) + 1];
		                                newIT[j] = FOREIGN | (ci & newHcMask)
		                                        | (c0 & newMask);
		                                c0 = (c0 & newHcMask) | j;
		                            }
		                        } else if (delta == deltaMask) {
		                            if (c1 < 0) {
		                                newKV[newHash << 1] = key;
		                                newKV[(newHash << 1) + 1] = oldKV[(j << 1) + 1];
		                                c1 = (ci & newHcMask) | newHash;
		                            } else {
		                                newKV[j << 1] = key;
		                                newKV[(j << 1) + 1] = oldKV[(j << 1) + 1];
		                                newIT[j] = FOREIGN | (ci & newHcMask)
		                                        | (c1 & newMask);
		                                c1 = (c1 & newHcMask) | j;
		                            }
		                        } else {
		                            if (newKV[newHash << 1] == null) {
		                                newKV[newHash << 1] = key;
		                                newKV[(newHash << 1) + 1] = oldKV[(j << 1) + 1];
		                                newIT[newHash] = (ci & newHcMask) | newHash;
		                            } else {
		                                newKV[j << 1] = key;
		                                newKV[(j << 1) + 1] = oldKV[(j << 1) + 1];
		                                int tmp = newIT[newHash];
		                                newIT[j] = FOREIGN | (ci & newHcMask)
		                                        | (tmp & newMask);
		                                newIT[newHash] = (tmp & newHcMask) | j;
		                            }
		                        }
	
		                        // next
		                        j = ci & oldMask;
		                        if (j == i)
		                            break;
		                        key = oldKV[j << 1];
		                        ci = oldIT[j];
		                    }
		                    if (c0 >= 0)
		                        newIT[i] = c0;
		                    if (c1 >= 0)
		                        newIT[i | deltaMask] = c1;
		                }
		            }
		        }
	
		        // ToDo
	
		        keyValueTable = newKV;
		        indexTable = newIT;
		        threshold = (int) (newCapacity * loadEstimator);
	
		        // ToDo: overflowTable
	
		        // validate("resize " + oldLen + " -> " + newCapacity);
		    }
	
		    /**
		*
		* @param i
		* @param v
		*/
		    final protected void relocate(int i, int v) {
		        int[] indices = indexTable;
		        Object[] kv = keyValueTable;
		        int mask = indices.length - 1;
	
		        //
		        int newIndex = findFreeSpot(i, mask, v);
		        indices[newIndex] = v;
		        kv[newIndex << 1] = kv[i << 1];
		        kv[(newIndex << 1) + 1] = kv[(i << 1) + 1];
	
		        //
		        int prev = v & mask, p1, p2;
		        while ((p2 = (p1 = indices[prev]) & mask) != i)
		            prev = p2;
		        indices[prev] = (p1 & ~mask) | newIndex;
		    }
	
		    /**
		*
		* @param i
		* @return
		*/
		    final protected int findFreeSpot(int i, int mask, int perturb) {
		        Object[] kv = keyValueTable;
	
		        while (true) {
		            if (i < mask && kv[(i + 1) << 1] == null)
		                return i + 1;
		            if (i > 0 && kv[(i - 1) << 1] == null)
		                return i - 1;
	
		            // i = nextProbe(i) & mask;
		            // i = (i * 5 + 1) & mask;
		            i = ((i << 2) + i + 1 + perturb) & mask;
		            if (kv[i << 1] == null)
		                return i;
		            perturb >>>= 5;
		        }
		    }
	
		    public V remove(Object key) {
		        return remove(key, null);
		    }
	
		    protected final V remove(Object key, V notFoundValue) {
		        // null key is a special case
		        if (key == null) {
		            if (nullKeyPresent) {
		                V oldValue = nullValue;
		                size--;
		                nullKeyPresent = false;
		                nullValue = null;
		                return oldValue;
		            } else
		                return notFoundValue;
		        }
	
		        // Check if arrays were not initialised yet
		        Object[] kv = keyValueTable;
		        if (kv == null)
		            return notFoundValue;
	
		        //
		        int[] indices = indexTable;
		        int mask = indices.length - 1;
		        int hc = hash(key.hashCode());
		        int i0 = hc & mask, i = i0;
	
		        // Check if this hash bin is empty
		        Object key1 = kv[i << 1];
		        if (key1 == null)
		            return notFoundValue;
	
		        //
		        int hcMask = AVAILABLE_BITS & ~mask;
		        int hcBits = hc & hcMask;
		        int ci = indices[i];
		        int next = ci & mask;
	
		        // Check head element
		        if (key == key1 || hcBits == (ci & hcMask) && key.equals(key1)) {
		            @SuppressWarnings("unchecked")
		            V oldValue = (V) kv[(i << 1) + 1];
		            if (next == i) {
		                // This was the only entry in this hash bin => just empty it
		                kv[i << 1] = null;
		                kv[(i << 1) + 1] = null;
		            } else {
		                // Shift next element to the head
		                kv[i << 1] = kv[next << 1];
		                kv[next << 1] = null;
		                kv[(i << 1) + 1] = kv[(next << 1) + 1];
		                kv[(next << 1) + 1] = null;
		                indices[i] = indices[next] & ~FOREIGN;
		            }
		            size--;
		            return oldValue;
		        }
	
		        // Check other elements
		        while (next != i0) {
		            key1 = kv[next << 1];
		            int ci1 = indices[next];
		            int next2 = ci1 & mask;
		            if (key == key1 || hcBits == (ci1 & hcMask) && key.equals(key1)) {
		                @SuppressWarnings("unchecked")
		                V oldValue = (V) kv[(next << 1) + 1];
		                kv[next << 1] = null;
		                kv[(next << 1) + 1] = null;
		                indices[i] = (ci & ~mask) | next2;
		                size--;
		                return oldValue;
		            }
	
		            //
		            i = next;
		            next = next2;
		            ci = ci1;
		        }
	
		        // ToDo: overflowTable
	
		        return notFoundValue;
		    }
	
		    public int size() {
		        return size;
		    }
	
		    /**
		* Remove all mappings.
		*/
		    public void clear() {
		        size = 0;
		        nullKeyPresent = false;
		        nullValue = null;
		        threshold = 0;
		        keyValueTable = null;
		        indexTable = null;
		        overflowTable = null;
		    }
	
		    public boolean isEmpty() {
		        return size == 0;
		    }
	
		    public CHashMapUtil<K, V> clone() {
		        try {
		            @SuppressWarnings("unchecked")
		            CHashMapUtil<K, V> that = (CHashMapUtil<K, V>) super.clone();
		            if (keyValueTable != null) {
		                that.keyValueTable = Arrays.copyOf(keyValueTable,
		                        keyValueTable.length);
		                that.indexTable = Arrays.copyOf(indexTable, indexTable.length);
		            }
	
		            that.values = null;
		            that.keySet = null;
		            that.entrySet = null;
	
		            // ToDo: overflowTable
	
		            return that;
		        } catch (CloneNotSupportedException e) {
		            throw new RuntimeException(e);
		        }
		    }
	
		    /**
		*
		* @param value
		* @return
		*/
		    public boolean containsValue(Object value) {
		        if (nullKeyPresent
		                && (nullValue == value || value != null
		                        && value.equals(nullValue)))
		            return true;
		        Object[] kv = keyValueTable;
		        if (kv == null)
		            return false;
		        int len = kv.length;
		        for (int i = 0; i < len; i += 2) {
		            if (kv[i] != null) {
		                Object v = kv[i + 1];
		                if (v == value || value != null && value.equals(v))
		                    return true;
		            }
		        }
	
		        // ToDo: overflowTable
	
		        return false;
		    }
	
		    protected transient volatile Collection<V> values = null;
	
		    public Collection<V> values() {
		        Collection<V> vs = values;
		        return (vs != null ? vs : (values = new Values()));
		    }
	
		    protected final class Values extends AbstractCollection<V> {
		        public Iterator<V> iterator() {
		            return new ValueIterator();
		        }
	
		        public int size() {
		            return size;
		        }
	
		        public boolean isEmpty() {
		            return size == 0;
		        }
	
		        public boolean contains(Object o) {
		            return containsValue(o);
		        }
	
		        public void clear() {
		            CHashMapUtil.this.clear();
		        }
		    }
	
		    public int hashCode() {
		        int h = 0;
	
		        // null
		        if (nullKeyPresent && nullValue != null)
		            h += nullValue.hashCode();
	
		        //
		        Object[] kv = keyValueTable;
		        if (kv != null) {
		            int len = kv.length;
		            for (int i = 0; i < len; i += 2) {
		                Object key = kv[i];
		                if (key != null) {
		                    int hc = key.hashCode();
		                    Object value = kv[i + 1];
		                    if (value != null)
		                        hc ^= value.hashCode();
		                    h += hc;
		                }
		            }
		        }
	
		        // ToDo: overflowTable
	
		        return h;
		    }
	
		    protected static final long serialVersionUID = 362498820763181265L;
	
		    abstract class HashIterator<T> implements Iterator<T> {
		        int nextIndex = nullKeyPresent ? -2 : findNextIndex(-2);
		        int lastReturnedIndex = -4;
	
		        // ToDo: overflowTable
	
		        protected final int findNextIndex(int i) {
		            Object[] kv = keyValueTable;
		            if (kv == null)
		                return -4;
		            int len = kv.length;
		            while (true) {
		                i += 2;
		                if (i >= len)
		                    return -4;
		                if (kv[i] != null)
		                    return i;
		            }
		        }
	
		        public final boolean hasNext() {
		            return nextIndex >= -2;
		        }
	
		        final K nextKey() {
		            if (nextIndex < -2)
		                throw new NoSuchElementException();
		            @SuppressWarnings("unchecked")
		            K key = nextIndex < 0 ? null : (K) keyValueTable[nextIndex];
		            nextIndex = findNextIndex(lastReturnedIndex = nextIndex);
		            return key;
		        }
	
		        final V nextValue() {
		            if (nextIndex < -2)
		                throw new NoSuchElementException();
		            @SuppressWarnings("unchecked")
		            V value = nextIndex < 0 ? nullValue
		                    : (V) keyValueTable[nextIndex + 1];
		            nextIndex = findNextIndex(lastReturnedIndex = nextIndex);
		            return value;
		        }
	
		        final Map.Entry<K, V> nextEntry() {
		            if (nextIndex < -2)
		                throw new NoSuchElementException();
		            Map.Entry<K, V> entry = nextIndex < 0 ? new NullEntry()
		                    : new ArrayEntry(nextIndex);
		            nextIndex = findNextIndex(lastReturnedIndex = nextIndex);
		            return entry;
		        }
	
		        public void remove() {
		            if (lastReturnedIndex < -2)
		                throw new IllegalStateException();
		            if (lastReturnedIndex < 0) {
		                nullKeyPresent = false;
		                nullValue = null;
		                size--;
		            } else {
		                CHashMapUtil.this.remove(keyValueTable[lastReturnedIndex]);
		            }
		            lastReturnedIndex = -4;
	
		            // ToDo: expectedModCount
		        }
		    }
	
		    final class KeyIterator extends HashIterator<K> {
		        public final K next() {
		            return super.nextKey();
		        }
		    }
	
		    final class ValueIterator extends HashIterator<V> {
		        public final V next() {
		            return super.nextValue();
		        }
		    }
	
		    final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
		        public final Map.Entry<K, V> next() {
		            return super.nextEntry();
		        }
		    }
	
		    final class NullEntry implements Map.Entry<K, V> {
		        public final K getKey() {
		            return null;
		        }
	
		        public final V getValue() {
		            return nullValue;
		        }
	
		        public final V setValue(V newValue) {
		            V oldValue = nullValue;
		            nullValue = newValue;
		            return oldValue;
		        }
	
		        public int hashCode() {
		            return nullValue == null ? 0 : nullValue.hashCode();
		        }
	
		        public String toString() {
		            return "null=" + nullValue;
		        }
	
		        public boolean equals(Object o) {
		            if (o instanceof Map.Entry<?, ?>) {
		                @SuppressWarnings("unchecked")
		                Map.Entry<K, V> that = (Map.Entry<K, V>) o;
		                if (that.getKey() == null) {
		                    Object value2 = that.getValue();
		                    return value2 == nullValue || value2 != null
		                            && value2.equals(nullValue);
		                }
		            }
		            return false;
		        }
		    }
	
		    final class ArrayEntry implements Map.Entry<K, V> {
		        final int index;
		        final K key;
		        V value;
	
		        @SuppressWarnings("unchecked")
		        ArrayEntry(int index) {
		            this.index = index;
		            this.key = (K) keyValueTable[index];
		            this.value = (V) keyValueTable[index + 1];
		        }
	
		        public final K getKey() {
		            return key;
		        }
	
		        @SuppressWarnings("unchecked")
		        public final V getValue() {
		            if (keyValueTable[index] == key)
		                value = (V) keyValueTable[index + 1];
		            return value;
		        }
	
		        public final V setValue(V newValue) {
		            if (keyValueTable[index] == key) {
		                @SuppressWarnings("unchecked")
		                V oldValue = (V) keyValueTable[index + 1];
		                keyValueTable[index + 1] = value = newValue;
		                return oldValue;
		            }
		            V oldValue = value;
		            value = newValue;
		            return oldValue;
		        }
	
		        public boolean equals(Object o) {
		            if (o instanceof Map.Entry<?, ?>) {
		                @SuppressWarnings("unchecked")
		                Map.Entry<K, V> that = (Map.Entry<K, V>) o;
		                K key2 = that.getKey();
		                if (key == key2 || key.equals(key2)) {
		                    V value2 = that.getValue();
		                    return getValue() == value2
		                            || (value != null && value.equals(value2));
		                }
		            }
		            return false;
		        }
	
		        public int hashCode() {
		            return key.hashCode() ^ (getValue() == null ? 0 : value.hashCode());
		        }
	
		        public String toString() {
		            return key + "=" + getValue();
		        }
		    }
	
		    final static Object DUMMY_VALUE = new Object();
	
		    @SuppressWarnings("unchecked")
		    public boolean containsKey(Object key) {
		        return get(key, (V) DUMMY_VALUE) != DUMMY_VALUE;
		    }
	
		    protected transient volatile Set<K> keySet = null;
	
		    public Set<K> keySet() {
		        Set<K> ks = keySet;
		        return (ks != null ? ks : (keySet = new KeySet()));
		    }
	
		    protected final class KeySet extends AbstractSet<K> {
		        public Iterator<K> iterator() {
		            return new KeyIterator();
		        }
	
		        public int size() {
		            return size;
		        }
	
		        public boolean isEmpty() {
		            return size == 0;
		        }
	
		        public boolean contains(Object o) {
		            return containsKey(o);
		        }
	
		        @SuppressWarnings("unchecked")
		        public boolean remove(Object o) {
		            return CHashMapUtil.this.remove(o, (V) DUMMY_VALUE) != DUMMY_VALUE;
		        }
	
		        public void clear() {
		            CHashMapUtil.this.clear();
		        }
		    }
	
		    protected transient volatile Set<Map.Entry<K, V>> entrySet = null;
	
		    public Set<Map.Entry<K, V>> entrySet() {
		        Set<Map.Entry<K, V>> es = entrySet;
		        return es != null ? es : (entrySet = new EntrySet());
		    }
	
		    protected final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		        public Iterator<Map.Entry<K, V>> iterator() {
		            return new EntryIterator();
		        }
	
		        public boolean contains(Object o) {
		            if (o instanceof Map.Entry<?, ?>) {
		                @SuppressWarnings("unchecked")
		                Map.Entry<K, V> that = (Map.Entry<K, V>) o;
		                K key2 = that.getKey();
		                V value2 = that.getValue();
		                if (key2 == null) {
		                    return nullKeyPresent
		                            && (value2 == nullValue || value2 != null
		                                    && value2.equals(nullValue));
		                } else {
		                    @SuppressWarnings("unchecked")
		                    V value = get(key2, (V) DUMMY_VALUE);
		                    return value == value2
		                            || (value != DUMMY_VALUE && value != null && value
		                                    .equals(value2));
		                }
		            }
		            return false;
		        }
	
		        public boolean remove(Object o) {
		            if (o instanceof Map.Entry<?, ?>) {
		                @SuppressWarnings("unchecked")
		                Map.Entry<K, V> that = (Map.Entry<K, V>) o;
		                K key2 = that.getKey();
		                @SuppressWarnings("unchecked")
		                V value = get(key2, (V) DUMMY_VALUE);
		                V value2 = that.getValue();
		                if (value == value2
		                        || (value != DUMMY_VALUE && value != null && value
		                                .equals(value2))) {
		                    CHashMapUtil.this.remove(key2);
		                    return true;
		                }
		            }
		            return false;
		        }
	
		        public int size() {
		            return size;
		        }
	
		        public boolean isEmpty() {
		            return size == 0;
		        }
	
		        public void clear() {
		            CHashMapUtil.this.clear();
		        }
		    }
	
		    protected void writeObject(ObjectOutputStream s) throws IOException {
		        // Write out the threshold, loadestimator, and any hidden stuff
		        s.defaultWriteObject();
	
		        // Write out number of buckets
		        int len = indexTable != null ? indexTable.length : 0;
		        s.writeInt(len);
	
		        // Write out size (number of Mappings)
		        s.writeInt(size);
	
		        // Write out keys and values (alternating)
		        if (nullKeyPresent) {
		            s.writeObject(null);
		            s.writeObject(nullValue);
		        }
		        for (int i = 0; i < len; i++) {
		            Object key = keyValueTable[i << 1];
		            if (key != null) {
		                s.writeObject(key);
		                s.writeObject(keyValueTable[(i << 1) + 1]);
		            }
		        }
	
		        // ToDo: overflowTable
		    }
	
		    protected void readObject(ObjectInputStream s) throws IOException,
		            ClassNotFoundException {
		        // Read in the threshold, loadestimator, and any hidden stuff
		        s.defaultReadObject();
	
		        // Read in number of buckets
		        int numBuckets = s.readInt();
	
		        // ToDo: init or resize...
		        if (numBuckets > 0) {
		            keyValueTable = new Object[numBuckets << 1];
		            indexTable = new int[numBuckets];
		        }
	
		        // Read in size (number of Mappings)
		        int size = s.readInt();
	
		        // Read the keys and values, and put the mappings in the HashMap
		        for (int i = 0; i < size; i++) {
		            @SuppressWarnings("unchecked")
		            K key = (K) s.readObject();
		            @SuppressWarnings("unchecked")
		            V value = (V) s.readObject();
		            put(key, value);
		        }
		    }
	
		    public void putAll(Map<? extends K, ? extends V> m) {
		        if (m.size() == 0)
		            return;
	
		        // ToDo: resize
	
		        if (m instanceof CHashMapUtil<?, ?>) {
		            @SuppressWarnings("unchecked")
		            CHashMapUtil<K, V> fm = (CHashMapUtil<K, V>) m;
	
		            // null key special case
		            if (fm.nullKeyPresent) {
		                if (!nullKeyPresent) {
		                    nullKeyPresent = true;
		                    size++;
		                }
		                nullValue = fm.nullValue;
		            }
	
		            Object[] kv = fm.keyValueTable;
		            int len = kv == null ? 0 : kv.length;
		            for (int i = 0; i < len; i += 2) {
		                @SuppressWarnings("unchecked")
		                K key = (K) kv[i];
		                if (key != null) {
		                    @SuppressWarnings("unchecked")
		                    V value = (V) kv[i + 1];
		                    put(key, value);
		                }
		            }
	
		            // ToDo: overflowTable
	
		        } else {
		            for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
		                put(e.getKey(), e.getValue());
		        }
		    }
	
		    public boolean equals(Object o) {
		        if (o == this)
		            return true;
		        if (!(o instanceof Map<?, ?>))
		            return false;
		        @SuppressWarnings("unchecked")
		        Map<K, V> m = (Map<K, V>) o;
		        if (m.size() != size)
		            return false;
	
		        if (nullKeyPresent) {
		            V value2 = m.get(null);
		            if (value2 == null) {
		                if (nullValue != null || !m.containsKey(null))
		                    return false;
		            } else {
		                if (nullValue != value2 && !value2.equals(nullValue))
		                    return false;
		            }
		        }
	
		        Object[] kv = keyValueTable;
		        int len = kv == null ? 0 : kv.length;
		        for (int i = 0; i < len; i += 2) {
		            @SuppressWarnings("unchecked")
		            K key = (K) kv[i];
		            if (key != null) {
		                @SuppressWarnings("unchecked")
		                V value = (V) kv[i + 1];
		                V value2 = m.get(key);
		                if (value2 == null) {
		                    if (value != null || !m.containsKey(key))
		                        return false;
		                } else {
		                    if (value != value2 && !value2.equals(value))
		                        return false;
		                }
		            }
		        }
	
		        // ToDo: overflowTable
	
		        return true;
		    }
	
		    public String toString() {
		        if (size == 0)
		            return "{}";
		        StringBuilder sb = new StringBuilder();
		        sb.append('{');
		        boolean first = true;
	
		        // null
		        if (nullKeyPresent) {
		            sb.append("null=");
		            sb.append(nullValue == this ? "(this Map)" : nullValue);
		            first = false;
		        }
	
		        //
		        Object[] kv = keyValueTable;
		        int len = kv == null ? 0 : kv.length;
		        for (int i = 0; i < len; i += 2) {
		            @SuppressWarnings("unchecked")
		            K key = (K) kv[i];
		            if (key != null) {
		                @SuppressWarnings("unchecked")
		                V value = (V) kv[i + 1];
	
		                //
		                if (first)
		                    first = false;
		                else
		                    sb.append(", ");
		                sb.append(key == this ? "(this Map)" : key);
		                sb.append('=');
		                sb.append(value == this ? "(this Map)" : value);
		            }
		        }
	
		        // ToDo: overflowTable
	
		        return sb.append('}').toString();
		    }
	
		    /**
		* Internal self-test.
		*/
		    public void validate(String s) {
		        //
		        if (keyValueTable == null || indexTable == null) {
		            if (threshold > 0)
		                throw new RuntimeException("threshold=" + threshold
		                        + " with null tables - " + s);
		            return;
		        }
	
		        //
		        int size1 = nullKeyPresent ? 1 : 0, size2 = size1;
		        int len = indexTable.length, mask = len - 1, hcMask = AVAILABLE_BITS
		                & ~mask;
	
		        //
		        if (keyValueTable.length != (len << 1))
		            throw new RuntimeException("keyValueTable.len="
		                    + keyValueTable.length + ", must be " + (len << 1) + " - "
		                    + s);
		        if (overflowTable != null && overflowTable.length != len)
		            throw new RuntimeException("overflowTable.len="
		                    + overflowTable.length + ", must be " + len + " - " + s);
		        //
		        for (int i = 0; i < len; i++) {
		            Object key = keyValueTable[i << 1];
		            if (key != null) {
		                size1++;
		                int ci = indexTable[i];
		                if ((ci & FOREIGN) == 0) {
		                    int j = i;
		                    while (true) {
		                        size2++;
		                        int hc = hash(key.hashCode());
		                        if ((hc & mask) != i)
		                            throw new RuntimeException("Key " + key
		                                    + " in wrong hash bin (" + i
		                                    + "), must be " + (hc & mask) + " - " + s);
		                        if ((hc & hcMask) != (ci & hcMask))
		                            throw new RuntimeException("Wrong hc bits ("
		                                    + (ci & hcMask) + " at " + j + " hash bin "
		                                    + i + ", must be " + (hc & hcMask) + " - "
		                                    + s);
	
		                        // next
		                        if ((ci & mask) == i)
		                            break;
		                        j = ci & mask;
		                        ci = indexTable[j];
		                        if ((ci & FOREIGN) == 0)
		                            throw new RuntimeException(
		                                    "FOREIGN flag not set at " + j
		                                            + " hash bin " + i + " - " + s);
		                        key = keyValueTable[j << 1];
		                    }
		                }
		            }
	
		            //
		            if (overflowTable != null && overflowTable[i] != null) {
		                // ToDo
		            }
		        }
		        if (size1 != size)
		            throw new RuntimeException("# of not null cella: " + size1
		                    + ", must be " + size + " - " + s);
	
		        if (size2 != size)
		            throw new RuntimeException("# of elements: " + size2 + ", must be "
		                    + size + " - " + s);
		    }
		}

// ------------------------ EOF ----------------------------------------
