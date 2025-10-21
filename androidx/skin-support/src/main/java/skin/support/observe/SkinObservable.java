package skin.support.observe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class that manages a list of {@link SkinObserver} instances and notifies them of skin updates.
 * Observers are notified in reverse order of registration to maintain consistency with the original
 * implementation. All observer management methods are thread-safe.
 */
public class SkinObservable {
    private final List<SkinObserver> mObservers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds an observer to the list if it is not already present.
     *
     * @param observer The {@link SkinObserver} to add.
     * @throws NullPointerException if the observer is null.
     */
    public void addObserver(@NonNull SkinObserver observer) {
        Objects.requireNonNull(observer, "Observer must not be null");
        synchronized (mObservers) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }
    }

    /**
     * Removes the specified observer from the list.
     *
     * @param observer The {@link SkinObserver} to remove. Ignored if null.
     */
    public void removeObserver(@Nullable SkinObserver observer) {
        if (observer != null) {
            mObservers.remove(observer);
        }
    }

    /**
     * Notifies all registered observers of a skin update with no additional data.
     */
    public void notifyUpdateSkin() {
        notifyUpdateSkin(null);
    }

    /**
     * Notifies all registered observers of a skin update with optional data.
     * Notifications are delivered in reverse order of registration.
     *
     * @param arg Optional data to pass to observers.
     */
    public void notifyUpdateSkin(@Nullable Object arg) {
        // Create a snapshot to avoid ConcurrentModificationException
        List<SkinObserver> snapshot;
        synchronized (mObservers) {
            snapshot = new ArrayList<>(mObservers);
        }

        // Notify observers in reverse order
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            try {
                snapshot.get(i).updateSkin(this, arg);
            } catch (Exception e) {
                // Suppress exceptions to prevent one observer from breaking the chain
            }
        }
    }

    public synchronized void deleteObservers() {
        mObservers.clear();
    }

    /**
     * Removes all observers from the list.
     */
    public void clearObservers() {
        mObservers.clear();
    }

    /**
     * Returns the number of registered observers.
     *
     * @return The number of observers.
     */
    public int countObservers() {
        return mObservers.size();
    }
}