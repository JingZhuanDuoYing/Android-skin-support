package androidx.appcompat.app;

import android.app.Activity;
import android.content.Context;
import android.view.Window;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class SkinAppCompatDelegateImpl extends AppCompatDelegateImpl {
    private static Map<Activity, WeakReference<AppCompatDelegate>> sDelegateMap = new WeakHashMap<>();
    private final Context mContext;

    public static AppCompatDelegate get(Activity activity, AppCompatCallback callback) {
        WeakReference<AppCompatDelegate> delegateRef = sDelegateMap.get(activity);
        AppCompatDelegate delegate = (delegateRef == null ? null : delegateRef.get());
        if (delegate == null) {
            delegate = new SkinAppCompatDelegateImpl(activity, activity.getWindow(), callback);
            sDelegateMap.put(activity, new WeakReference<>(delegate));
        }
        return delegate;
    }

    private SkinAppCompatDelegateImpl(Context context, Window window, AppCompatCallback callback) {
        super(context, window, callback);
        this.mContext = context;
    }

    @Override
    public void installViewFactory() {
    }

    @Override
    public void onDestroy() {
        if (mContext != null && mContext instanceof Activity) {
            sDelegateMap.remove(mContext);
        }
        super.onDestroy();
    }
}
