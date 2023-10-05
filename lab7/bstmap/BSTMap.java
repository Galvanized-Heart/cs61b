package bstmap;

import java.util.Iterator;
import java.util.Set;


/**
 * This data structure uses a BST to organize data based on keys
 * and searching for keys will allow one to find an associated
 * value for said key.
 * */
public class BSTMap<K extends Comparable<K>,V> implements Map61B<K, V> {
    private BSTNode node;
    private int size;

    /* BSTMap Constructor. */
    public BSTMap() {
        node = new BSTNode(null, null ,null, null);
        size = 0;
    }

    /* Nodes for key, value pairs and pointers to children inside BSTMap. */
    private class BSTNode {
        public K key;
        public V val;
        public BSTNode left;
        public BSTNode right;

        /* BSTNode Constructor. */
        private BSTNode(K k, V v, BSTNode l, BSTNode r) {
            key = k;
            val = v;
            left = l;
            right = r;
        }
    }

    /* Removes all of the mappings from this map. */
    @Override
    public void clear() {
        size = 0;
        node = new BSTNode(null,null,null,null);
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return containsKey(key, node);
    }

    private boolean containsKey(K k, BSTNode n) {
        if (n == null || n.key == null) {
            return false;
        }
        int cmp = k.compareTo(n.key);
        if (cmp < 0) {
            return containsKey(k, n.left);
        }
        else if (cmp > 0) {
            return containsKey(k, n.right);
        }
        else {
            return true;
        }
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        return get(key, node);
    }

    private V get(K k, BSTNode n) {
        if (n == null || n.key == null) {
            return null;
        }
        int cmp = k.compareTo(n.key);
        if (cmp < 0) {
            return get(k, n.left);
        }
        else if (cmp > 0) {
            return get(k, n.right);
        }
        else {
            return n.val;
        }
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V val) {
        node = put(key, val, node);
    }

    private BSTNode put(K k, V v, BSTNode n) {
        if (n == null || n.key == null) {
            size++;
            return new BSTNode(k, v, null, null);
        }
        int cmp = k.compareTo(n.key);
        if (cmp < 0) {
            n.left = put(k, v, n.left);
        }
        else if (cmp > 0) {
            n.right =  put(k, v, n.right);
        }
        else {
            n.val = v;
        }
        return n;
    }

    /* Prints out BSTMap in order of increasing key. */
    public void printInOrder() {
        return;
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Ey, you can't do dat here");
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("Ey, you can't do dat here");
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("Ey, you can't do dat here");
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Ey, you can't do dat here");
    }
}
