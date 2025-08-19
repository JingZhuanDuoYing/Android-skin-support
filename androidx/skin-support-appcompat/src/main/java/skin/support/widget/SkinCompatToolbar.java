package skin.support.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import skin.support.content.res.SkinCompatResources;
import skin.support.content.res.SkinCompatVectorResources;

import skin.support.R;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

/**
 * 扩展 {@link Toolbar}，支持动态换肤的工具栏。
 * 支持背景、标题颜色、副标题颜色和导航图标的换肤功能。
 *
 * @author ximsfei
 * @since 2017/1/12
 */
public class SkinCompatToolbar extends Toolbar implements SkinCompatSupportable {
    private static final String TAG = "SkinCompatToolbar";
    private int mTitleTextColorResId = INVALID_ID;
    private int mSubtitleTextColorResId = INVALID_ID;
    private int mNavigationIconResId = INVALID_ID;
    private final SkinCompatBackgroundHelper mBackgroundTintHelper;

    public SkinCompatToolbar(@NonNull Context context) {
        this(context, null);
    }

    public SkinCompatToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
    }

    public SkinCompatToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        try {
            mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load background attributes", e);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, androidx.appcompat.R.styleable.Toolbar, defStyleAttr, 0);
        mNavigationIconResId = a.getResourceId(androidx.appcompat.R.styleable.Toolbar_navigationIcon, INVALID_ID);

        int titleAp = a.getResourceId(androidx.appcompat.R.styleable.Toolbar_titleTextAppearance, INVALID_ID);
        int subtitleAp = a.getResourceId(androidx.appcompat.R.styleable.Toolbar_subtitleTextAppearance, INVALID_ID);
        a.recycle();
        if (titleAp != INVALID_ID) {
            a = context.obtainStyledAttributes(titleAp, skin.support.R.styleable.SkinTextAppearance);
            mTitleTextColorResId = a.getResourceId(skin.support.R.styleable.SkinTextAppearance_android_textColor, INVALID_ID);
            a.recycle();
        }
        if (subtitleAp != INVALID_ID) {
            a = context.obtainStyledAttributes(subtitleAp, R.styleable.SkinTextAppearance);
            mSubtitleTextColorResId = a.getResourceId(R.styleable.SkinTextAppearance_android_textColor, INVALID_ID);
            a.recycle();
        }
        a = context.obtainStyledAttributes(attrs, androidx.appcompat.R.styleable.Toolbar, defStyleAttr, 0);
        if (a.hasValue(androidx.appcompat.R.styleable.Toolbar_titleTextColor)) {
            mTitleTextColorResId = a.getResourceId(androidx.appcompat.R.styleable.Toolbar_titleTextColor, INVALID_ID);
        }
        if (a.hasValue(androidx.appcompat.R.styleable.Toolbar_subtitleTextColor)) {
            mSubtitleTextColorResId = a.getResourceId(androidx.appcompat.R.styleable.Toolbar_subtitleTextColor, INVALID_ID);
        }
        a.recycle();
        applyTitleTextColor();
        applySubtitleTextColor();
        applyNavigationIcon();
    }

    /**
     * 应用标题文本颜色。
     */
    private void applyTitleTextColor() {
        mTitleTextColorResId = SkinCompatHelper.checkResourceId(mTitleTextColorResId);
        if (mTitleTextColorResId != INVALID_ID) {
            try {
                setTitleTextColor(SkinCompatResources.getColor(getContext(), mTitleTextColorResId));
            } catch (Exception e) {
                Log.e(TAG, "Failed to apply title text color: " + mTitleTextColorResId, e);
            }
        }
    }

    /**
     * 应用副标题文本颜色。
     */
    private void applySubtitleTextColor() {
        mSubtitleTextColorResId = SkinCompatHelper.checkResourceId(mSubtitleTextColorResId);
        if (mSubtitleTextColorResId != INVALID_ID) {
            try {
                setSubtitleTextColor(SkinCompatResources.getColor(getContext(), mSubtitleTextColorResId));
            } catch (Exception e) {
                Log.e(TAG, "Failed to apply subtitle text color: " + mSubtitleTextColorResId, e);
            }
        }
    }

    /**
     * 应用导航图标。
     */
    private void applyNavigationIcon() {
        mNavigationIconResId = SkinCompatHelper.checkResourceId(mNavigationIconResId);
        if (mNavigationIconResId != INVALID_ID) {
            try {
                setNavigationIcon(SkinCompatVectorResources.getDrawableCompat(getContext(), mNavigationIconResId));
            } catch (Exception e) {
                Log.e(TAG, "Failed to apply navigation icon: " + mNavigationIconResId, e);
            }
        }
    }

    /**
     * 设置背景资源，并通知换肤助手更新背景。
     *
     * @param resId 背景资源 ID
     */
    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        try {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set background resource: " + resId, e);
        }
    }

    /**
     * 设置导航图标，并更新换肤资源。
     *
     * @param resId 导航图标资源 ID
     */
    @Override
    public void setNavigationIcon(@DrawableRes int resId) {
        super.setNavigationIcon(resId);
        mNavigationIconResId = resId;
        applyNavigationIcon();
    }

    /**
     * 应用当前皮肤，更新背景、标题颜色、副标题颜色和导航图标。
     */
    @Override
    public void applySkin() {
        try {
            mBackgroundTintHelper.applySkin();
            applyTitleTextColor();
            applySubtitleTextColor();
            applyNavigationIcon();
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply skin", e);
        }
    }
}