package facemywrath.slots.util;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedRandom<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public WeightedRandom() {
        this(new Random());
    }

    public WeightedRandom(Random random) {
        this.random = random;
    }
    
    public boolean isEmpty()
    {
    	return map.isEmpty();
    }

    public void add(double weight, E result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }
    
    public Collection<E> values() { return map.values(); }

    public E next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}