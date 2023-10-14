package hashmap;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.*;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;


/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Maxim Kirby
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /** Instance Variables */
    private Collection<Node>[] buckets;
    private int num_items;
    private int num_bucks;
    private double loadCapacity;
    private Set<K> keys;

    /***********************************************************************************/

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }
    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }
    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        num_items = 0;
        num_bucks = initialSize;
        loadCapacity = maxLoad;
        keys = new HashSet<>();
    }

    /***********************************************************************************/

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) { // constructor
            key = k;
            value = v;
        }
    }

    /***********************************************************************************/

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    /***********************************************************************************/

    /** Removes all the mappings from this map. */
    @Override
    public void clear() {
        num_items = 0;
        num_bucks = 16;
        keys = new HashSet<>();
        buckets = createTable(num_bucks);
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        for (K k : keys) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        int code = Math.floorMod(key.hashCode(), num_bucks);
        if (buckets[code] == null) {
            return null;
        }
        for (Node node : buckets[code]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return num_items;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    @Override
    public void put(K key, V value) {
        int code = Math.floorMod(key.hashCode(), num_bucks);
        Node pair = createNode(key, value);
        if (buckets[code] == null) {
            buckets[code] = createBucket();
        }
        if (!containsKey(key)) {
            num_items++;
            buckets[code].add(pair);
            keys.add(key);
        }
        else {
            for (Node node : buckets[code]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        }
        double loadFactor = (double) num_items/num_bucks;
        if (loadFactor > loadCapacity) {
            resize(num_bucks*2);
        }
    }

    private void resize(int newSize) {
        Collection<Node>[] temp = new Collection[newSize];
        int newCode;
        for (Collection<Node> bucket : buckets) {
            if (bucket != null) {
                for (Node node : bucket) {
                    newCode = Math.floorMod(node.key.hashCode(), newSize);
                    if (temp[newCode] == null) {
                        temp[newCode] = createBucket();
                    }
                    temp[newCode].add(node);
                }
            }
        }
        num_bucks = newSize;
        buckets = temp;
    }

    /**
     * Returns a Set view of the keys contained in this map.
     * Markdown recommends creating instance variable (HashSet) for this
     */
    @Override
    public Set<K> keySet() {
        return keys;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int code = Math.floorMod(key.hashCode(), num_bucks);
        if (buckets[code] == null) {
            return null;
        }
        for (Node node : buckets[code]) {
            if (node.key.equals(key)) {
                V value = node.value;
                buckets[code].remove(node);
                keys.remove(key);
                return value;
            }
        }
        return null;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        int code = Math.floorMod(key.hashCode(), num_bucks);
        if (buckets[code] == null) {
            return null;
        }
        for (Node node : buckets[code]) {
            if (node.key.equals(key)) {
                if (node.value.equals(value)) {
                    V val = node.value;
                    buckets[code].remove(node);
                    keys.remove(key);
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * Returns Iterator for keys in HashMap
     * Markdown recommends creating instance variable (HashSet) for this
     */
    public Iterator<K> iterator() {
        return keys.iterator();
    }
}
