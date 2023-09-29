package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private int minCap = 8;
    private int cap = 8;
    private int back = cap/2;
    private int front = back-1;

    private T[] items;

    // Constructs an empty AD
    public ArrayDeque() {
        items = (T[]) new Object[cap];
    }

    // Construct an iterator for AD
    private class ArrayDequeIterator implements Iterator<T> {
        private int itPos;
        private int endPos;
        public ArrayDequeIterator(int front, int back) {
            itPos = front+1;
            endPos = back;
        }
        // Returns true when there are items left in AD
        public boolean hasNext() { return itPos < endPos; }
        // Advances itPos and returns items in AD
        public T next() {
            T returnItem = items[itPos];
            itPos += 1;
            return returnItem;
        }
    }

    // Iterates over AD
    public Iterator<T> iterator() {
        return new ArrayDequeIterator(front, back);
    }

    // Returns true if Object contains all equivalent items to AD
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o instanceof ArrayDeque) {
            ArrayDeque a = (ArrayDeque) o;
            if (a.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i+=1) {
                if (!a.get(i).equals(this.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Increase/Decrease array size
    /*
    addFirst() ->
        front += cap;
        back += cap;
        System.arraycopy(items, 0, temp, front+1, cap);
    addLast() ->
        System.arraycopy(items, 0, temp, 0, cap);
    removeFirst() ->
        front -= cap;
        back -= cap;
        System.arraycopy(items, front, temp, 0, newCap);
    removeLast() ->
        System.arraycopy(items, 0, temp, 0, newCap);
     */
    private void resize(int newCap, boolean isFront, boolean isAdding) {
        T[] temp = (T[]) new Object[newCap];
        int src = 0;
        int dest = 0;
        int newSize = newCap;

        // Formatting variable (see comment above method)
        if (isFront) {
            if (isAdding) {
                front += cap;
                back += cap;
                dest = front+1;
            }
            else {
                src = front;
                front -= newCap;
                back -= newCap;
            }
        }
        if (isAdding) {
            newSize = cap;
        }

        // Execute resize
        System.arraycopy(items, src, temp, dest, newSize);
        items = temp;
        cap = newCap;
    }

    // Adds item to front of AD
    @Override
    public void addFirst(T item) {
        if (front < 0) {
            resize(cap*2, true, true);
        }
        items[front] = item;
        front -= 1;
    }

    // Adds item to back of AD
    @Override
    public void addLast(T item) {
        if (back > cap-1) {
            resize(cap*2, false, true);
        }
        items[back] = item;
        back += 1;
    }

    // Returns number of items in AD
    @Override
    public int size() {
        return back-front-1;
    }

    // Prints out AD
    @Override
    public void printDeque() {
        int size = size();
        for (int i = front+1; i < back; i+=1) {
            System.out.print(items[i] + " ");
        }
    }

    // Removes item from front of AD
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        front += 1;
        T item = items[front];
        items[front] = null;
        if (front >= cap/2 && cap > minCap) {
            resize(cap/2, true, false);
        }
        return item;
    }

    // Removes item from back of AD
    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        back -= 1;
        T item = items[back];
        items[back] = null;
        if (back < cap/2 && cap > minCap) {
            resize(cap/2, false, false);
        }
        return item;
    }

    // Gets item at specified index in AD
    @Override
    public T get(int index) {
        return items[front+index+1];
    }
}
