package skin.support.observe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ximsfei on 2017/1/10.
 */
public class SkinObservable {
    private final List<SkinObserver> mObservers;

    public SkinObservable() {
        mObservers = new ArrayList<>();
    }

    /**
     * Adds an observer to the set of observers for this object, provided
     * that it is not the same as some observer already in the set.
     * The order in which notifications will be delivered to observers is not specified.
     *
     * @param o an observer to be added.
     * @throws NullPointerException if the parameter o is null.
     */
    public synchronized void addObserver(SkinObserver o) {
        Objects.requireNonNull(o, "observer == null");
        if (!mObservers.contains(o)) {
            mObservers.add(o);
        }
    }

    public synchronized void deleteObserver(SkinObserver o) {
        mObservers.remove(o);
    }

    public void notifyUpdateSkin() {
        notifyUpdateSkin(null);
    }

    public void notifyUpdateSkin(Object arg) {
        SkinObserver[] localObservers;

        synchronized (this) {
            localObservers = mObservers.toArray(new SkinObserver[0]);
        }

        for (int i = localObservers.length - 1; i >= 0; i--) {
            try {
                localObservers[i].updateSkin(this, arg);
            } catch (Exception e) {
                // Ignore exceptions thrown by observers
            }
        }
    }

    public synchronized void deleteObservers() {
        mObservers.clear();
    }

    public synchronized int countObservers() {
        return mObservers.size();
    }
}
