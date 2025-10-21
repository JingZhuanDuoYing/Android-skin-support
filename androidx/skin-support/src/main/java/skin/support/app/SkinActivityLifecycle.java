package skin.support.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import skin.support.SkinCompatManager;
import skin.support.annotation.Skinable;
import skin.support.content.res.SkinCompatResources;
import skin.support.observe.SkinObservable;
import skin.support.observe.SkinObserver;
import skin.support.utils.Slog;
import skin.support.view.LayoutInflaterCompat;
import skin.support.widget.SkinCompatHelper;
import skin.support.widget.SkinCompatSupportable;
import skin.support.content.res.SkinCompatThemeUtils;

/**
 * A lifecycle callback implementation that manages skinning for activities.
 * It installs custom layout factories and observers to handle dynamic skin updates,
 * using weak references to prevent memory leaks.
 */
public final class SkinActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "SkinActivityLifecycle";
    private static volatile SkinActivityLifecycle sInstance;

    private final WeakHashMap<Context, SkinCompatDelegate> mSkinDelegateMap = new WeakHashMap<>();
    private final WeakHashMap<Context, LazySkinObserver> mSkinObserverMap = new WeakHashMap<>();
    private WeakReference<Activity> mCurActivityRef;

    /**
     * Initializes the singleton instance and registers lifecycle callbacks.
     *
     * @param application The {@link Application} to register callbacks with.
     * @return The singleton instance.
     * @throws IllegalArgumentException if application is null.
     */
    @NonNull
    public static SkinActivityLifecycle init(@NonNull Application application) {
        if (sInstance == null) {
            synchronized (SkinActivityLifecycle.class) {
                if (sInstance == null) {
                    sInstance = new SkinActivityLifecycle(application);
                }
            }
        }
        return sInstance;
    }

    private SkinActivityLifecycle(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        installLayoutFactory(application);
        SkinCompatManager.getInstance().addObserver(getObserver(application));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (isContextSkinEnable(activity)) {
            installLayoutFactory(activity);
            updateWindowBackground(activity);
            if (activity instanceof SkinCompatSupportable) {
                ((SkinCompatSupportable) activity).applySkin();
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mCurActivityRef = new WeakReference<>(activity);
        if (isContextSkinEnable(activity)) {
            LazySkinObserver observer = getObserver(activity);
            SkinCompatManager.getInstance().addObserver(observer);
            observer.updateSkinIfNeeded();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isContextSkinEnable(activity)) {
            SkinCompatManager.getInstance().removeObserver(getObserver(activity));
            mSkinObserverMap.remove(activity);
            mSkinDelegateMap.remove(activity);
        }
    }

    /**
     * Installs a custom layout factory for skinning on the given context's LayoutInflater.
     *
     * @param context The context to install the factory on.
     */
    private void installLayoutFactory(@NonNull Context context) {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            LayoutInflaterCompat.setFactory2(layoutInflater, getSkinDelegate(context));
        } catch (IllegalStateException e) {
            if (Slog.DEBUG) {
                Slog.i(TAG, "Failed to set factory on LayoutInflater for " + context + ": " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves or creates a {@link SkinCompatDelegate} for the given context.
     *
     * @param context The context to associate with the delegate.
     * @return The {@link SkinCompatDelegate}.
     */
    @NonNull
    private SkinCompatDelegate getSkinDelegate(@NonNull Context context) {
        SkinCompatDelegate delegate = mSkinDelegateMap.get(context);
        if (delegate == null) {
            delegate = SkinCompatDelegate.create(context);
            mSkinDelegateMap.put(context, delegate);
        }
        return delegate;
    }

    /**
     * Retrieves or creates a {@link LazySkinObserver} for the given context.
     *
     * @param context The context to associate with the observer.
     * @return The {@link LazySkinObserver}.
     */
    @NonNull
    private LazySkinObserver getObserver(@NonNull Context context) {
        LazySkinObserver observer = mSkinObserverMap.get(context);
        if (observer == null) {
            observer = new LazySkinObserver(context);
            mSkinObserverMap.put(context, observer);
        }
        return observer;
    }

    /**
     * Updates the window background of the activity if skinning is enabled.
     *
     * @param activity The activity to update.
     */
    private void updateWindowBackground(@NonNull Activity activity) {
        if (SkinCompatManager.getInstance().isSkinWindowBackgroundEnable()) {
            int windowBackgroundResId = SkinCompatThemeUtils.getWindowBackgroundResId(activity);
            if (SkinCompatHelper.checkResourceId(windowBackgroundResId) != SkinCompatHelper.INVALID_ID) {
                Drawable drawable = SkinCompatResources.getDrawable(activity, windowBackgroundResId);
                if (drawable != null) {
                    activity.getWindow().setBackgroundDrawable(drawable);
                }
            }
        }
    }

    /**
     * Checks if skinning is enabled for the given context.
     *
     * @param context The context to check.
     * @return True if skinning is enabled, false otherwise.
     */
    private boolean isContextSkinEnable(@NonNull Context context) {
        return SkinCompatManager.getInstance().isSkinAllActivityEnable()
                || context.getClass().getAnnotation(Skinable.class) != null
                || context instanceof SkinCompatSupportable;
    }

    /**
     * An observer that lazily applies skin updates to a context, deferring updates for inactive activities
     * until they resume.
     */
    private static class LazySkinObserver implements SkinObserver {
        private final Context mContext;
        private boolean mMarkNeedUpdate;

        LazySkinObserver(@NonNull Context context) {
            this.mContext = context;
        }

        @Override
        public void updateSkin(@NonNull SkinObservable observable, @Nullable Object arg) {
            // Apply immediately for current activity, non-activity contexts, or if no current activity
            Activity currentActivity = SkinActivityLifecycle.sInstance != null
                    && SkinActivityLifecycle.sInstance.mCurActivityRef != null
                    ? SkinActivityLifecycle.sInstance.mCurActivityRef.get()
                    : null;
            if (currentActivity == null || mContext == currentActivity || !(mContext instanceof Activity)) {
                updateSkinForce();
            } else {
                mMarkNeedUpdate = true;
            }
        }

        /**
         * Applies skin updates if previously marked as needed.
         */
        void updateSkinIfNeeded() {
            if (mMarkNeedUpdate) {
                updateSkinForce();
            }
        }

        /**
         * Forces a skin update for the associated context.
         */
        private void updateSkinForce() {
            if (Slog.DEBUG) {
                Slog.i(TAG, "Applying skin to context: " + mContext);
            }
            if (mContext == null) {
                return;
            }
            if (mContext instanceof Activity && sInstance.isContextSkinEnable(mContext)) {
                sInstance.updateWindowBackground((Activity) mContext);
            }
            sInstance.getSkinDelegate(mContext).applySkin();
            if (mContext instanceof SkinCompatSupportable) {
                ((SkinCompatSupportable) mContext).applySkin();
            }
            mMarkNeedUpdate = false;
        }
    }
}