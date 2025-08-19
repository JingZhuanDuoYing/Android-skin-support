package skin.support;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import skin.support.annotation.NonNull;
import skin.support.annotation.Nullable;
import skin.support.app.SkinActivityLifecycle;
import skin.support.app.SkinLayoutInflater;
import skin.support.app.SkinWrapper;
import skin.support.load.SkinAssetsLoader;
import skin.support.load.SkinBuildInLoader;
import skin.support.load.SkinNoneLoader;
import skin.support.load.SkinPrefixBuildInLoader;
import skin.support.observe.SkinObservable;
import skin.support.utils.SkinPreference;
import skin.support.content.res.SkinCompatResources;

public class SkinCompatManager extends SkinObservable {
    private static final String TAG = "SkinCompatManager";

    public static final int SKIN_LOADER_STRATEGY_NONE = -1;
    public static final int SKIN_LOADER_STRATEGY_ASSETS = 0;
    public static final int SKIN_LOADER_STRATEGY_BUILD_IN = 1;
    public static final int SKIN_LOADER_STRATEGY_PREFIX_BUILD_IN = 2;

    private static volatile SkinCompatManager sInstance;

    public static SkinCompatManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("SkinCompatManager not initialized. Call init(Context) first.");
        }
        return sInstance;
    }

    private final ReentrantLock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();
    private final Context mAppContext;
    private boolean mLoading = false;
    private final List<SkinWrapper> mWrappers = new ArrayList<>();
    private final List<SkinLayoutInflater> mInflaters = new ArrayList<>();
    private final List<SkinLayoutInflater> mHookInflaters = new ArrayList<>();
    private final SparseArray<SkinLoaderStrategy> mStrategyMap = new SparseArray<>();
    private boolean mSkinAllActivityEnable = true;
    private boolean mSkinStatusBarColorEnable = false;
    private boolean mSkinWindowBackgroundColorEnable = true;

    /**
     * 皮肤包加载监听.
     */
    public interface SkinLoaderListener {
        /**
         * 开始加载.
         */
        void onStart();

        /**
         * 加载成功.
         */
        void onSuccess();

        /**
         * 加载失败.
         *
         * @param errMsg 错误信息.
         */
        void onFailed(String errMsg);
    }

    /**
     * 皮肤包加载策略.
     */
    public interface SkinLoaderStrategy {
        /**
         * 加载皮肤包.
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @return 加载成功，返回皮肤包名称；失败，则返回空。
         */
        String loadSkinInBackground(Context context, String skinName);

        /**
         * 根据应用中的资源ID，获取皮肤包相应资源的资源名.
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @param resId    应用中需要换肤的资源ID.
         * @return 皮肤包中相应的资源名.
         */
        String getTargetResourceEntryName(Context context, String skinName, int resId);

        /**
         * 开发者可以拦截应用中的资源ID，返回对应color值。
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @param resId    应用中需要换肤的资源ID.
         * @return 获得拦截后的颜色值，添加到ColorStateList的defaultColor中。不需要拦截，则返回空
         */
        ColorStateList getColor(Context context, String skinName, int resId);

        /**
         * 开发者可以拦截应用中的资源ID，返回对应ColorStateList。
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @param resId    应用中需要换肤的资源ID.
         * @return 返回对应ColorStateList。不需要拦截，则返回空
         */
        ColorStateList getColorStateList(Context context, String skinName, int resId);

        /**
         * 开发者可以拦截应用中的资源ID，返回对应Drawable。
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @机器人

        /**
         * 开发者可以拦截应用中的资源ID，返回对应Drawable。
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @param resId    应用中需要换肤的资源ID.
         * @return 返回对应Drawable。不需要拦截，则返回空
         */
        Drawable getDrawable(Context context, String skinName, int resId);

        /**
         * {@link #SKIN_LOADER_STRATEGY_NONE}
         * {@link #SKIN_LOADER_STRATEGY_ASSETS}
         * {@link #SKIN_LOADER_STRATEGY_BUILD_IN}
         * {@link #SKIN_LOADER_STRATEGY_PREFIX_BUILD_IN}
         *
         * @return 皮肤包加载策略类型.
         */
        int getType();
    }

    /**
     * 初始化换肤框架. 通过该方法初始化，应用中Activity需继承自{@link skin.support.app.SkinCompatActivity}.
     *
     * @param context Context
     */
    public static SkinCompatManager init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (sInstance == null) {
            synchronized (SkinCompatManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinCompatManager(context);
                }
            }
        }
        SkinPreference.init(context);
        return sInstance;
    }

    /**
     * 初始化换肤框架，监听Activity生命周期. 通过该方法初始化，应用中Activity无需继承{@link skin.support.app.SkinCompatActivity}.
     *
     * @param application 应用Application.
     */
    public static SkinCompatManager withoutActivity(Application application) {
        init(application);
        SkinActivityLifecycle.init(application);
        return sInstance;
    }

    private SkinCompatManager(Context context) {
        mAppContext = context.getApplicationContext();
        initLoaderStrategy();
    }

    private void initLoaderStrategy() {
        mStrategyMap.put(SKIN_LOADER_STRATEGY_NONE, new SkinNoneLoader());
        mStrategyMap.put(SKIN_LOADER_STRATEGY_ASSETS, new SkinAssetsLoader());
        mStrategyMap.put(SKIN_LOADER_STRATEGY_BUILD_IN, new SkinBuildInLoader());
        mStrategyMap.put(SKIN_LOADER_STRATEGY_PREFIX_BUILD_IN, new SkinPrefixBuildInLoader());
    }

    public Context getContext() {
        return mAppContext;
    }

    /**
     * 添加皮肤包加载策略.
     *
     * @param strategy 自定义加载策略.
     */
    public SkinCompatManager addStrategy(SkinLoaderStrategy strategy) {
        mStrategyMap.put(strategy.getType(), strategy);
        return this;
    }

    public SparseArray<SkinLoaderStrategy> getStrategies() {
        return mStrategyMap;
    }

    /**
     * 自定义View换肤时，可选择添加一个{@link SkinLayoutInflater}
     *
     * @param inflater 在{@link skin.support.app.SkinCompatViewInflater#createView(Context, String, String)}中调用.
     */
    public SkinCompatManager addInflater(SkinLayoutInflater inflater) {
        if (inflater instanceof SkinWrapper) {
            mWrappers.add((SkinWrapper) inflater);
        }
        mInflaters.add(inflater);
        return this;
    }

    public List<SkinWrapper> getWrappers() {
        return mWrappers;
    }

    public List<SkinLayoutInflater> getInflaters() {
        return mInflaters;
    }

    /**
     * 自定义View换肤时，可选择添加一个{@link SkinLayoutInflater}
     *
     * @param inflater 在{@link skin.support.app.SkinCompatViewInflater#createView(Context, String, String)}中优先调用.
     */
    @Deprecated
    public SkinCompatManager addHookInflater(SkinLayoutInflater inflater) {
        mHookInflaters.add(inflater);
        return this;
    }

    @Deprecated
    public List<SkinLayoutInflater> getHookInflaters() {
        return mHookInflaters;
    }

    /**
     * 获取当前皮肤包名称。
     *
     * @return 当前皮肤包名称
     * @deprecated 请使用 {@link SkinPreference#getSkinName()} 替代
     */
    @Deprecated
    public String getCurSkinName() {
        return SkinPreference.getInstance().getSkinName();
    }

    /**
     * 恢复默认主题，使用应用自带资源.
     */
    public void restoreDefaultTheme() {
        loadSkin("", SKIN_LOADER_STRATEGY_NONE);
    }

    /**
     * 设置是否所有Activity都换肤.
     *
     * @param enable true: 所有Activity都换肤; false: 实现SkinCompatSupportable接口的Activity支持换肤.
     */
    public SkinCompatManager setSkinAllActivityEnable(boolean enable) {
        mSkinAllActivityEnable = enable;
        return this;
    }

    public boolean isSkinAllActivityEnable() {
        return mSkinAllActivityEnable;
    }

    @Deprecated
    public SkinCompatManager setSkinStatusBarColorEnable(boolean enable) {
        mSkinStatusBarColorEnable = enable;
        return this;
    }

    @Deprecated
    public boolean isSkinStatusBarColorEnable() {
        return mSkinStatusBarColorEnable;
    }

    /**
     * 设置WindowBackground换肤，使用Theme中的{@link android.R.attr#windowBackground}属性.
     *
     * @param enable true: 使用Theme中的{@link android.R.attr#windowBackground}属性换肤; false: 关闭.
     */
    public SkinCompatManager setSkinWindowBackgroundEnable(boolean enable) {
        mSkinWindowBackgroundColorEnable = enable;
        return this;
    }

    public boolean isSkinWindowBackgroundEnable() {
        return mSkinWindowBackgroundColorEnable;
    }

    /**
     * 加载记录的皮肤包，一般在Application中初始化换肤框架后调用.
     */
    public AsyncTask loadSkin() {
        String skin = SkinPreference.getInstance().getSkinName();
        int strategy = SkinPreference.getInstance().getSkinStrategy();
        if (TextUtils.isEmpty(skin) || strategy == SKIN_LOADER_STRATEGY_NONE) {
            return null;
        }
        return loadSkin(skin, null, strategy);
    }

    /**
     * 加载记录的皮肤包，一般在Application中初始化换肤框架后调用.
     *
     * @param listener 皮肤包加载监听.
     */
    public AsyncTask loadSkin(SkinLoaderListener listener) {
        String skin = SkinPreference.getInstance().getSkinName();
        int strategy = SkinPreference.getInstance().getSkinStrategy();
        if (TextUtils.isEmpty(skin) || strategy == SKIN_LOADER_STRATEGY_NONE) {
            return null;
        }
        return loadSkin(skin, listener, strategy);
    }

    @Deprecated
    public AsyncTask loadSkin(String skinName) {
        return loadSkin(skinName, null);
    }

    @Deprecated
    public AsyncTask loadSkin(String skinName, final SkinLoaderListener listener) {
        return loadSkin(skinName, listener, SKIN_LOADER_STRATEGY_ASSETS);
    }

    /**
     * 加载皮肤包.
     *
     * @param skinName 皮肤包名称.
     * @param strategy 皮肤包加载策略.
     */
    public AsyncTask loadSkin(String skinName, int strategy) {
        return loadSkin(skinName, null, strategy);
    }

    /**
     * 加载皮肤包.
     *
     * @param skinName 皮肤包名称，不能为空。
     * @param listener 皮肤加载监听器，用于接收加载状态回调，可为 null。
     * @param strategy 皮肤加载策略，取值范围为 {@link #SKIN_LOADER_STRATEGY_NONE}、{@link #SKIN_LOADER_STRATEGY_ASSETS} 等。
     * @return 返回异步任务对象，若策略无效则返回 null。
     */
    public AsyncTask loadSkin(String skinName, SkinLoaderListener listener, int strategy) {
        SkinLoaderStrategy loaderStrategy = mStrategyMap.get(strategy);
        if (loaderStrategy == null) {
            Log.w(TAG, "Invalid strategy: " + strategy);
            return null;
        }
        return new SkinLoadTask(listener, loaderStrategy).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, skinName);
    }

    private class SkinLoadTask extends AsyncTask<String, Void, String> {
        private final SkinLoaderListener mListener;
        private final SkinLoaderStrategy mStrategy;

        SkinLoadTask(@Nullable SkinLoaderListener listener, @NonNull SkinLoaderStrategy strategy) {
            mListener = listener;
            mStrategy = strategy;
        }

        @Override
        protected void onPreExecute() {
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            mLock.lock();
            try {
                while (mLoading) {
                    try {
                        mCondition.await();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Skin loading interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
                mLoading = true;
            } finally {
                mLock.unlock();
            }
            try {
                if (params.length == 1) {
                    String skinName = mStrategy.loadSkinInBackground(mAppContext, params[0]);
                    if (TextUtils.isEmpty(skinName)) {
                        SkinCompatResources.getInstance().reset(mStrategy);
                        return "";
                    }
                    return params[0];
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load skin: " + params[0], e);
            }
            SkinCompatResources.getInstance().reset();
            return null;
        }

        @Override
        protected void onPostExecute(String skinName) {
            mLock.lock();
            try {
                if (skinName != null) {
                    SkinPreference.getInstance().setSkinName(skinName).setSkinStrategy(mStrategy.getType()).commitEditor();
                    notifyUpdateSkin();
                    if (mListener != null) {
                        mListener.onSuccess();
                    }
                } else {
                    SkinPreference.getInstance().setSkinName("").setSkinStrategy(SKIN_LOADER_STRATEGY_NONE).commitEditor();
                    if (mListener != null) {
                        mListener.onFailed("Failed to load skin resources, check logs for details");
                    }
                }
                mLoading = false;
                mCondition.signalAll();
            } finally {
                mLock.unlock();
            }
        }
    }

    /**
     * 获取皮肤包包名.
     *
     * @param skinPkgPath sdcard中皮肤包路径.
     */
    public String getSkinPackageName(String skinPkgPath) {
        PackageManager mPm = mAppContext.getPackageManager();
        PackageInfo info = mPm.getPackageArchiveInfo(skinPkgPath, PackageManager.GET_ACTIVITIES);
        return info.packageName;
    }

    /**
     * 获取皮肤包资源{@link Resources}.
     *
     * @param skinPkgPath sdcard中皮肤包路径.
     */
    @Nullable
    public Resources getSkinResources(String skinPkgPath) {
        try {
            PackageInfo packageInfo = mAppContext.getPackageManager().getPackageArchiveInfo(skinPkgPath, 0);
            if (packageInfo == null) {
                Log.e(TAG, "Failed to get package info for: " + skinPkgPath);
                return null;
            }
            packageInfo.applicationInfo.sourceDir = skinPkgPath;
            packageInfo.applicationInfo.publicSourceDir = skinPkgPath;
            Resources res = mAppContext.getPackageManager().getResourcesForApplication(packageInfo.applicationInfo);
            Resources superRes = mAppContext.getResources();
            return new WeakReference<>(new Resources(res.getAssets(), superRes.getDisplayMetrics(), superRes.getConfiguration())).get();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load skin resources: " + skinPkgPath, e);
            return null;
        }
    }
}