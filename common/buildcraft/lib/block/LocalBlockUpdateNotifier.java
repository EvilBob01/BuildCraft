package buildcraft.lib.block;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.level.Level;

import buildcraft.lib.world.WorldEventListenerAdapter;


/**
 * Listens for BlockUpdates in a given world and notifies all registered IBlockUpdateSubscribers of the update provided
 * it was within the update range of the ILocalBlockUpdateSubscriber
 */
public class LocalBlockUpdateNotifier {

    private static final Map<Level, LocalBlockUpdateNotifier> instanceMap = new WeakHashMap<>();
    private final Set<ILocalBlockUpdateSubscriber> subscriberSet = new HashSet<>();


    private LocalBlockUpdateNotifier(Level world) {

        IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
            @Override
            public void notifyBlockUpdate(@Nonnull Level world, @Nonnull BlockPos eventPos, @Nonnull BlockState oldState,
                                          @Nonnull BlockState newState, int flags) {
                notifySubscribersInRange(world, eventPos, oldState, newState, flags);
            }
        };
        world.addEventListener(worldEventListener);
    }

    /**
     * Gets the LocalBlockUpdateNotifier for the given world
     *
     * @param world the Level where BlockUpdate events will be listened for
     * @return the instance of LocalBlockUpdateNotifier for the given world
     */
    public static LocalBlockUpdateNotifier instance(Level world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new LocalBlockUpdateNotifier(world));
        }
        return instanceMap.get(world);
    }

    /**
     * Register an @{ILocalBlockUpdateSubscriber} to receive notifications about block updates
     *
     * @param subscriber the subscriber to receive notifications about local block updates
     */
    public void registerSubscriberForUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.add(subscriber);
    }

    /**
     * Stop an @{ILocalBlockUpdateSubscriber} from receiving notifications about block updates
     *
     * @param subscriber the subscriber to no longer receive notifications about local block update
     */
    public void removeSubscriberFromUpdateNotifications(ILocalBlockUpdateSubscriber subscriber) {
        subscriberSet.remove(subscriber);
    }

    /**
     * Notifies all subscribers near the given position that a world update took place. The distance used to determine
     * if a subscriber is close enough to notify is determined by a call to the subscriber's implementation of
     * getUpdateRange
     *
     * @param world    from the Block Update
     * @param eventPos from the Block Update
     * @param oldState from the Block Update
     * @param newState from the Block Update
     * @param flags    from the Block Update
     */
    private void notifySubscribersInRange(Level world, BlockPos eventPos, BlockState oldState, BlockState newState,
                                          int flags) {
        for (ILocalBlockUpdateSubscriber subscriber : subscriberSet) {
            BlockPos keyPos = subscriber.getSubscriberPos();
            int updateRange = subscriber.getUpdateRange();
            if (Math.abs(keyPos.getX() - eventPos.getX()) <= updateRange &&
                    Math.abs(keyPos.getY() - eventPos.getY()) <= updateRange &&
                    Math.abs(keyPos.getZ() - eventPos.getZ()) <= updateRange) {
                subscriber.setWorldUpdated(world, eventPos, oldState, newState, flags);
            }
        }
    }

}
