package skin.support.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 扩展 {@link AppCompatTextView}，支持动态换肤的文本视图。
 * 通过 {@link SkinCompatBackgroundHelper} 处理背景换肤，
 * 通过 {@link SkinCompatTextHelper} 处理文本和 drawable 换肤。
 *
 * @author ximsfei
 * @since 2017/1/10
 */
public class SkinCompatTextView extends AppCompatTextView implements SkinCompatSupportable {
    private static final String TAG = "SkinCompatTextView";
    private final SkinCompatBackgroundHelper mBackgroundTintHelper;
    private final SkinCompatTextHelper mTextHelper;

    public SkinCompatTextView(@NonNull Context context) {
        this(context, null);
    }

    public SkinCompatTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public SkinCompatTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mTextHelper = SkinCompatTextHelper.create(this);
        try {
            mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
            mTextHelper.loadFromAttributes(attrs, defStyleAttr);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load attributes for SkinCompatTextView", e);
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
     * 设置文本外观样式。
     *
     * @param resId 文本外观资源 ID
     */
    @Override
    public void setTextAppearance(int resId) {
        setTextAppearance(getContext(), resId);
    }

    /**
     * 设置文本外观样式，并通知换肤助手更新。
     *
     * @param context 上下文
     * @param resId   文本外观资源 ID
     */
    @Override
    public void setTextAppearance(@NonNull Context context, int resId) {
        super.setTextAppearance(context, resId);
        try {
            mTextHelper.onSetTextAppearance(context, resId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set text appearance: " + resId, e);
        }
    }

    /**
     * 设置相对方向的复合 drawable，并通知换肤助手更新。
     *
     * @param start  开始方向的 drawable 资源 ID
     * @param top    顶部 drawable 资源 ID
     * @param end    结束方向的 drawable 资源 ID
     * @param bottom 底部 drawable 资源 ID
     */
    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes int start, @DrawableRes int top, @DrawableRes int end, @DrawableRes int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        try {
            mTextHelper.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set compound drawables relative: start=" + start + ", top=" + top +
                    ", end=" + end + ", bottom=" + bottom, e);
        }
    }

    /**
     * 设置复合 drawable，并通知换肤助手更新。
     *
     * @param left  左侧 drawable 资源 ID
     * @param top   顶部 drawable 资源 ID
     * @param right 右侧 drawable 资源 ID
     * @param bottom 底部 drawable 资源 ID
     */
    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes int left, @DrawableRes int top, @DrawableRes int right, @DrawableRes int bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        try {
            mTextHelper.onSetCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set compound drawables: left=" + left + ", top=" + top +
                    ", right=" + right + ", bottom=" + bottom, e);
        }
    }

    /**
     * 应用当前皮肤，更新背景和文本样式。
     */
    @Override
    public void applySkin() {
        try {
            mBackgroundTintHelper.applySkin();
            mTextHelper.applySkin();
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply skin", e);
        }
    }
}