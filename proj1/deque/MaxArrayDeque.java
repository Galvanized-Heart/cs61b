package deque;

import java.util.Iterator;
import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> implements Iterable<T> {
    private Comparator<T> comp;

    // Constructs a MaxArrayDeque with a given comparator
    public MaxArrayDeque(Comparator<T> c) {
        super(); // Ensure AD is constructed
        this.comp = c;
    }

    // Returns max item in deque as governed by previous comparator
    public T max() {
        if (isEmpty()) {
            return null;
        }
        T max = null;
        for (T i : this) {
            if (max == null || comp.compare(i, max) > 0) {
                max = i;
            }
        }
        return max;
    }

    // Returns max item in deque as governed by current comparator
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T max = null;
        for (T i : this) {
            if (max == null || c.compare(i, max) > 0) {
                max = i;
            }
        }
        return max;
    }
}
