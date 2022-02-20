package darkkronicle.github.io.cloudfight.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Bag<T> {

    private final List<T> original;
    private ArrayList<T> current;
    private Function<T, Boolean> criteria = (T) -> Boolean.TRUE;

    public static <T> Bag<T> fromCollection(Collection<T> original) {
        return Bag.fromCollection(original, (T) -> Boolean.TRUE);
    }

    public static <T> Bag<T> fromCollection(Collection<T> original, Function<T, Boolean> criteria) {
        Bag<T> bag = new Bag<>(new ArrayList<>(original));
        bag.setCriteria(criteria);
        return bag;
    }

    public static <T> Bag<T> fromMap(Map<T, Integer> original) {
        return Bag.fromMap(original, (T) -> Boolean.TRUE);
    }

    public static <T> Bag<T> empty() {
        return new Bag<>(new ArrayList<>());
    }

    public static <T> Bag<T> fromMap(Map<T, Integer> original, Function<T, Boolean> criteria) {
        ArrayList<T> arr = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : original.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                arr.add(entry.getKey());
            }
        }
        Bag<T> bag = new Bag<>(arr);
        bag.setCriteria(criteria);
        return bag;
    }

    private Bag(List<T> original) {
        this.original = original;
        current = new ArrayList<>();
    }

    public void setCriteria(Function<T, Boolean> criteria) {
        this.criteria = criteria;
        reset();
    }

    public void update(Collection<T> list) {
        // Removed ones that don't exist
        for (T value : original) {
            if (!list.contains(value)) {
                remove(value);
            }
        }
        // Add them all in
        for (T value : list) {
            add(value);
        }
    }

    public void add(T value) {
        add(value, false);
    }

    public void add(T value, boolean allowDuplicates) {
        if (!allowDuplicates && original.contains(value)) {
            return;
        }
        original.add(value);
        current.add(value);
        Collections.shuffle(current);
    }

    public void remove(T value) {
        if (!original.contains(value)) {
            return;
        }
        original.remove(value);
        current.remove(value);
    }

    public void setNext(T value) {
        current.add(0, value);
    }

    public void reset() {
        current = new ArrayList<>();
        for (T t : original) {
            if (criteria.apply(t)) {
                current.add(t);
            }
        }
        Collections.shuffle(current);
    }

    public List<T> getValues() {
        return original;
    }

    public T get() {
        if (current.size() == 0) {
            reset();
        }
        // Bag is already shuffled so we can just get the first each time
        T item = current.get(0);
        current.remove(0);
        return item;
    }

    public Object getRecursive() {
        if (current.size() == 0) {
            reset();
        }
        // Bag is already shuffled so we can just get the first each time
        T item = current.get(0);
        current.remove(0);
        if (item instanceof Bag) {
            return ((Bag<?>) item).getRecursive();
        }
        return item;
    }

    public int size() {
        return original.size();
    }

}
