package skin.support.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom {@link View} that supports dynamic skinning by applying background resources
 * through a {@link SkinCompatBackgroundHelper}. It implements {@link SkinCompatSupportable}
 * to handle skin changes.
 */
public class SkinCompatView extends View implements SkinCompatSupportable {
    private final SkinCompatBackgroundHelper mBackgroundTintHelper;

    /**
     * Constructs a new {@link SkinCompatView} with the given context.
     *
     * @param context The context to initialize the view.
     */
    public SkinCompatView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructs a new {@link SkinCompatView} with the given context and attributes.
     *
     * @param context The context to initialize the view.
     * @param attrs   The attribute set to apply to the view.
     */
    public SkinCompatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new {@link SkinCompatView} with the given context, attributes, and style.
     *
     * @param context      The context to initialize the view.
     * @param attrs        The attribute set to apply to the view.
     * @param defStyleAttr The default style attribute to apply.
     */
    public SkinCompatView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    /**
     * Sets the background resource for the view and notifies the background helper.
     *
     * @param resId The resource ID of the background to set.
     */
    @Override
    public void setBackgroundResource(int resId) {
        super.setBackgroundResource(resId);
        mBackgroundTintHelper.onSetBackgroundResource(resId);
    }

    /**
     * Applies the current skin to the view by updating its background.
     */
    @Override
    public void applySkin() {
        mBackgroundTintHelper.applySkin();
    }
}