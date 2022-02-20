package darkkronicle.github.io.cloudfight.utility;

import darkkronicle.github.io.cloudfight.CloudFight;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * Iterates through a iterator with a BukkitRunnable to delay
 *
 * @param <T> Object that the Iterator holds
 */
public class DelayedIterator<T> {

    private final Iterator<T> iterator;
    private final Run<T> run;

    public DelayedIterator(Iterator<T> iterator, Run<T> run) {
        this.iterator = iterator;
        this.run = run;
    }

    public void start(CloudFight plugin, int delay_until_start, int time_inbetween) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (!iterator.hasNext()) {
                    cancel();
                    return;
                }
                T item = iterator.next();
                if (!run.iter(plugin, iterator, item)) {
                    cancel();
                }
            }
        };
        r.runTaskTimer(plugin, delay_until_start, time_inbetween);
    }

    public interface Run<T> {
        boolean iter(CloudFight plugin, Iterator<T> iterator, T item);
    }

}
