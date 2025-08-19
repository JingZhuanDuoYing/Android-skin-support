package skin.support.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;

import skin.support.graphics.ColorUtils;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 工具类，用于从 Android 主题中获取换肤相关的资源（如颜色、ColorStateList）。
 *
 * @author ximsfei
 * @since 2017/3/25
 */
public class SkinCompatThemeUtils {
    private static final String TAG = "SkinCompatThemeUtils";
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();

    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] ENABLED_STATE_SET = new int[]{android.R.attr.state_enabled};
    static final int[] WINDOW_FOCUSED_STATE_SET = new int[]{android.R.attr.state_window_focused};
    static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};
    static final int[] ACCELERATED_STATE_SET = new int[]{android.R.attr.state_accelerated};
    static final int[] HOVERED_STATE_SET = new int[]{android.R.attr.state_hovered};
    static final int[] DRAG_CAN_ACCEPT_STATE_SET = new int[]{android.R.attr.state_drag_can_accept};
    static final int[] DRAG_HOVERED_STATE_SET = new int[]{android.R.attr.state_drag_hovered};
    static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    static final int[] SELECTED_STATE_SET = new int[]{android.R.attr.state_selected};
    static final int[] NOT_PRESSED_OR_FOCUSED_STATE_SET = new int[]{
            -android.R.attr.state_pressed, -android.R.attr.state_focused};
    static final int[] EMPTY_STATE_SET = new int[0];

    private static final int[] TEMP_ARRAY = new int[1];

    /**
     * 获取主题中的主文本颜色资源 ID。
     *
     * @param context 上下文
     * @return 主文本颜色资源 ID，若未找到返回 {@link SkinCompatHelper#INVALID_ID}
     */
    public static int getTextColorPrimaryResId(@NonNull Context context) {
        return getResId(context, new int[]{android.R.attr.textColorPrimary});
    }

    /**
     * 获取主题中的窗口背景资源 ID。
     *
     * @param context 上下文
     * @return 窗口背景资源 ID，若未找到返回 {@link SkinCompatHelper#INVALID_ID}
     */
    public static int getWindowBackgroundResId(@NonNull Context context) {
        return getResId(context, new int[]{android.R.attr.windowBackground});
    }

    /**
     * 从主题中获取指定属性的资源 ID。
     *
     * @param context 上下文
     * @param attrs   属性数组
     * @return 资源 ID，若未找到返回 {@link SkinCompatHelper#INVALID_ID}
     */
    static int getResId(@NonNull Context context, @NonNull int[] attrs) {
        try {
            TypedArray a = context.obtainStyledAttributes(attrs);
            try {
                return a.getResourceId(0, INVALID_ID);
            } finally {
                a.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get resource ID for attributes: " + attrs, e);
            return INVALID_ID;
        }
    }

    /**
     * 从主题中获取指定属性的颜色值。
     *
     * @param context 上下文
     * @param attr    属性 ID
     * @return 颜色值，若未找到返回 0
     */
    public static int getThemeAttrColor(@NonNull Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    /**
     * 从主题中获取指定属性的 ColorStateList。
     *
     * @param context 上下文
     * @param attr    属性 ID
     * @return ColorStateList，若未找到返回 null
     */
    @Nullable
    public static ColorStateList getThemeAttrColorStateList(@NonNull Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            int resId = a.getResourceId(0, 0);
            if (resId != 0) {
                return SkinCompatResources.getColorStateList(context, resId);
            }
            return null;
        } finally {
            a.recycle();
        }
    }

    /**
     * 获取主题中指定属性的禁用状态颜色。
     *
     * @param context 上下文
     * @param attr    属性 ID
     * @return 禁用状态颜色值，若未找到返回默认颜色
     */
    public static int getDisabledThemeAttrColor(@NonNull Context context, int attr) {
        ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            final TypedValue tv = getTypedValue();
            // Now retrieve the disabledAlpha value from the theme
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, tv, true);
            final float disabledAlpha = tv.getFloat();

            return getThemeAttrColor(context, attr, disabledAlpha);
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue == null) {
            typedValue = new TypedValue();
            TL_TYPED_VALUE.set(typedValue);
        }
        return typedValue;
    }

    static int getThemeAttrColor(Context context, int attr, float alpha) {
        final int color = getThemeAttrColor(context, attr);
        final int originalAlpha = Color.alpha(color);
        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }
}
