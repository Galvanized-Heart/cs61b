package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {
    private class node {
        private T item;
        private node front;
        private node back;
        public node(T i, node f, node b) {
            item = i;
            front = f;
            back = b;
        }
    }
    private int size;
    private node sen;

    // Constructs an empty LLD
    public LinkedListDeque() {
        sen = new node(null, null, null); // make sen a new node with all_params = null
        sen.front = sen; // point sen.front to sen (circular)
        sen.back = sen; // point sen.back to sen (circular)
        size = 0; // set size to 0
    }

    // Construct an iterator for LLD
    private class LinkedListDequeIterator implements Iterator<T> {
        private node itPos;
        private node endPos;
        public LinkedListDequeIterator(node sen) {
            itPos = sen.back;
            endPos = sen;
        }
        // Returns true when there are items left in LLD
        public boolean hasNext() {
            return itPos != endPos;
        }
        // Advances itPos and returns items in LLD
        public T next() {
            T returnItem = itPos.item;
            itPos = itPos.back;
            return returnItem;
        }
    }

    // Iterates over LLD
    public Iterator<T> iterator() {
        return new LinkedListDeque.LinkedListDequeIterator(sen);
    }

    // Returns true if Object contains all equivalent items to LLD
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o instanceof LinkedListDeque a) {
            if (a.size != this.size) {
                return false;
            }
            for (int i = 0; i < this.size(); i+=1) {
                if (a.get(i) != this.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Adds item to front of LLD
    @Override
    public void addFirst(T item) {
        node lead = new node(item, sen, sen.back); // lead.front = sen, lead.back = (lead-1 node)
        sen.back.front = lead; // point (lead-1 node).front to lead
        sen.back = lead; // point sen.back to lead
        size += 1;
    }

    // Adds item to back of LLD
    @Override
    public void addLast(T item) {
        node lagg = new node(item, sen.front, sen); // lagg.front = (lagg+1), lagg.back = sen
        sen.front.back = lagg; // point (lagg+1 node).front to lagg
        sen.front = lagg; // point sen.front to lagg
        size += 1;
    }

    // Returns number of items in LLD
    @Override
    public int size() {
        return size; // you already know
    }

    // Prints out LLD
    @Override
    public void printDeque() {
        node N = sen.back;
        for (int i = 0; i < size; i+=1) {
            System.out.print(N.item + " ");
            N = N.back;
        }
    }

    // Removes item from front of LLD
    @Override
    public T removeFirst() {
        if (!isEmpty()) {
            node secondLast = sen.back.back; // copy (lead-1 node)
            T t = secondLast.front.item;
            secondLast.front = sen; // set (lead-1).front = sen
            sen.back = sen.back.back; // set sen.back = (lead-1)
            size -= 1;
            return t;
        }
        return null;
    }

    // Removes item from back of LLD
    @Override
    public T removeLast() {
        if (!isEmpty()) {
            node second = sen.front.front; // copy (lagg+1 node)
            T t = second.back.item;
            second.back = sen; // set (lagg+1).front = sen
            sen.front = sen.front.front; // set sen.back = (lagg+1)
            size -= 1;
            return t;
        }
        return null;
    }

    // Gets item at specified index in LLD using loop
    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        node N = sen;
        for (int i = 0; i < index+1; i+=1) {
            N = N.back;
        }
        return N.item;
    }

    // Gets item at specified index in LLD using recursion
    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return getRecursive(index, sen.back);
    }
    // getRecursive() helper
    private T getRecursive(int index, node currNode) {
        if (index == 0) {
            return currNode.item;
        }
        return getRecursive(index - 1, currNode.back);
    }

}


