package skin.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import skin.support.content.res.SkinCompatResources;
import com.google.android.material.R;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.MaterialShapeDrawable;

import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatHelper;
import skin.support.widget.SkinCompatSupportable;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

/**
 * Created by ximsfei on 17-3-1.
 */

public class SkinMaterialBottomNavigationView extends BottomNavigationView implements SkinCompatSupportable {
    private static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private SkinCompatBackgroundHelper mBackgroundTintHelper;

    private int mTextColorResId = INVALID_ID;
    private int mIconTintResId = INVALID_ID;
    private int mDefaultTintResId = INVALID_ID;

    public SkinMaterialBottomNavigationView(@NonNull Context context) {
        this(context, null);
    }

    public SkinMaterialBottomNavigationView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinMaterialBottomNavigationView(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_Design_BottomNavigationView);
    }

    public SkinMaterialBottomNavigationView(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();

        /* Custom attributes */
        TintTypedArray attributes =
                ThemeEnforcement.obtainTintedStyledAttributes(
                        context, attrs, R.styleable.BottomNavigationView, defStyleAttr, defStyleRes);

        setItemHorizontalTranslationEnabled(
                attributes.getBoolean(
                        R.styleable.BottomNavigationView_itemHorizontalTranslationEnabled, true));

        if (attributes.hasValue(R.styleable.BottomNavigationView_android_minHeight)) {
            setMinimumHeight(
                    attributes.getDimensionPixelSize(R.styleable.BottomNavigationView_android_minHeight, 0));
        }

        attributes.recycle();

        if (shouldDrawCompatibilityTopDivider()) {
            addCompatibilityTopDivider(context);
        }

        applyWindowInsets();
    }

    private boolean shouldDrawCompatibilityTopDivider() {
        return Build.VERSION.SDK_INT < 21 && !(getBackground() instanceof MaterialShapeDrawable);
    }

    private void addCompatibilityTopDivider(@NonNull Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(
                ContextCompat.getColor(context, R.color.design_bottom_navigation_shadow_color));
        FrameLayout.LayoutParams dividerParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_shadow_height));
        divider.setLayoutParams(dividerParams);
        addView(divider);
    }

    private void applyWindowInsets() {
        ViewUtils.doOnApplyWindowInsets(
                this,
                (view, insets, initialPadding) -> {
                    // Apply the bottom, start, and end padding for a BottomNavigationView
                    // to dodge the system navigation bar
                    initialPadding.bottom += insets.getSystemWindowInsetBottom();

                    boolean isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
                    int systemWindowInsetLeft = insets.getSystemWindowInsetLeft();
                    int systemWindowInsetRight = insets.getSystemWindowInsetRight();
                    initialPadding.start += isRtl ? systemWindowInsetRight : systemWindowInsetLeft;
                    initialPadding.end += isRtl ? systemWindowInsetLeft : systemWindowInsetRight;
                    initialPadding.applyToView(view);
                    return insets;
                });
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    private void applyItemTextColorResource() {
        mTextColorResId = SkinCompatHelper.checkResourceId(mTextColorResId);
        if (mTextColorResId != INVALID_ID) {
            setItemTextColor(SkinCompatResources.getColorStateList(getContext(), mTextColorResId));
        } else {
            mDefaultTintResId = SkinCompatHelper.checkResourceId(mDefaultTintResId);
            if (mDefaultTintResId != INVALID_ID) {
                setItemTextColor(createDefaultColorStateList(android.R.attr.textColorSecondary));
            }
        }
    }

    private void applyItemIconTintResource() {
        mIconTintResId = SkinCompatHelper.checkResourceId(mIconTintResId);
        if (mIconTintResId != INVALID_ID) {
            setItemIconTintList(SkinCompatResources.getColorStateList(getContext(), mIconTintResId));
        } else {
            mDefaultTintResId = SkinCompatHelper.checkResourceId(mDefaultTintResId);
            if (mDefaultTintResId != INVALID_ID) {
                setItemIconTintList(createDefaultColorStateList(android.R.attr.textColorSecondary));
            }
        }
    }

    private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
        final TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
            return null;
        }
        ColorStateList baseColor = SkinCompatResources.getColorStateList(getContext(), value.resourceId);

        int colorPrimary = SkinCompatResources.getColor(getContext(), mDefaultTintResId);
        int defaultColor = baseColor.getDefaultColor();
        return new ColorStateList(new int[][]{
                DISABLED_STATE_SET,
                CHECKED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[]{
                baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
                colorPrimary,
                defaultColor
        });
    }

    private int resolveColorPrimary() {
        final TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(
                R.attr.colorPrimary, value, true)) {
            return INVALID_ID;
        }
        return value.resourceId;
    }

    @Override
    public void applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySkin();
        }
        applyItemIconTintResource();
        applyItemTextColorResource();
    }

}
