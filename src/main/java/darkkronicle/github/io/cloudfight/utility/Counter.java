package darkkronicle.github.io.cloudfight.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Interesting read https://www.programcreek.com/2013/10/efficient-counter-in-java/

/**
 * A class to store Integers for an Object
 *
 * @param <T> Object as key
 */
public class Counter<T> {

    private final HashMap<T, MutableInteger> count = new HashMap<>();

    /**
     * An Integer that can be incremented or have it's value set
     */
    public static class MutableInteger {
        private int value;

        public MutableInteger(int value) {
            this.value = value;
        }

        /**
         * Set the value to a specific integer
         *
         * @param value Integer to set to
         */
        public void set(int value) {
            this.value = value;
        }

        /**
         * Increments the integer by the value given
         *
         * @param value Value to add to
         */
        public void increment(int value) {
            this.value = this.value + value;
        }

        /**
         * Get's the current value
         * @return
         */
        public int get() {
            return value;
        }
    }

    /**
     * Get's a specific object. If the value doesn't exist it will return 0.
     *
     * @param key Object to get
     * @return Objects current count. 0 if the object doesn't exist.
     */
    public int get(T key) {
        MutableInteger val = count.get(key);
        if (val != null) {
            return val.get();
        }
        return 0;
    }

    /**
     * Increment a value
     *
     * @param key Key to get the value to increment
     */
    public void increment(T key) {
        increment(key, 1);
    }

    /**
     * Increments a value by a number. If the value doesn't exist it will set it will behave as if it was zero.
     *
     * @param key Key of value to increment
     * @param value Number to add to
     */
    public void increment(T key, int value) {
        MutableInteger val = count.get(key);
        if (val != null) {
            val.increment(value);
        } else {
            count.put(key, new MutableInteger(value));
        }
    }

    /**
     * Set's a specific key with a value
     *
     * @param key Key to get
     * @param value Value to set to
     */
    public void set(T key, int value) {
        MutableInteger val = count.get(key);
        if (val != null) {
            val.increment(value);
        } else {
            count.put(key, new MutableInteger(value));
        }
    }

    /**
     * Add up all values in the counter
     *
     * @return The total number of values in the counter.
     */
    public int size() {
        int amount = 0;
        for (MutableInteger entry : count.values()) {
            if (entry.get() > 0) {
                amount++;
            }
        }
        return amount;
    }

    /**
     * Return the entrySet of the counter.
     *
     * @return EntrySet of the keys/values
     */
    public Set<Map.Entry<T, MutableInteger>> entrySet() {
        return count.entrySet();
    }


    public Map.Entry<T, MutableInteger> highest() {
        Map.Entry<T, MutableInteger> high = null;
        for (Map.Entry<T, MutableInteger> entry : entrySet()) {
            if (high == null || entry.getValue().get() > high.getValue().get()) {
                high = entry;
            }
        }
        return high;
    }

    public Map.Entry<T, MutableInteger> lowest() {
        Map.Entry<T, MutableInteger> high = null;
        for (Map.Entry<T, MutableInteger> entry : entrySet()) {
            if (high == null || entry.getValue().get() < high.getValue().get()) {
                high = entry;
            }
        }
        return high;
    }

}
