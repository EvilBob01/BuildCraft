package buildcraft.lib.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.world.level.Level;

/**
 * Listens for BlockUpdates in a given world and notifies all registered IBlockUpdateSubscribers of the update provided
 * it was within the update range of the ILocalBlockUpdateSubscriber.
 *
 * NOTE: In 1.21, IWorldEventListener and Level#addEventListener were removed. Block-change notifications are
 * currently stubbed — subscribers will not receive notifications until a NeoForge-event-based rewrite is done.
 */
public class LocalBlockUpdateNotifier {

    private static final Map<Level, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
    private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();

    private LocalBlockUpdateNotifier(Level world) {
        // Block-change listener registration removed (IWorldEventListener no longer exists).
        // Subscribers are tracked but never notified. Rewrite via NeoForge event bus is deferred.
    }

    public static LocalBlockUpdateNotifier instance(Level world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new LocalBlockUpdateNotifier(world));
        }
        return instanceMap.get(world);
    }

    public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.add(subscriber);
    }

    public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.remove(subscriber);
    }
}
