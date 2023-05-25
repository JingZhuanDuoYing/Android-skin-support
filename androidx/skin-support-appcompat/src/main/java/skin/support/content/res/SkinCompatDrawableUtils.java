package skin.support.content.res;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.core.graphics.drawable.WrappedDrawable;

class SkinCompatDrawableUtils {

    private static final String VECTOR_DRAWABLE_CLAZZ_NAME
            = "android.graphics.drawable.VectorDrawable";

    /**
     * Attempt the fix any issues in the given drawable, usually caused by platform bugs in the
     * implementation. This method should be call after retrieval from
     * {@link android.content.res.Resources} or a {@link android.content.res.TypedArray}.
     */
    static void fixDrawable(@NonNull final Drawable drawable) {
        if (Build.VERSION.SDK_INT == 21
                && VECTOR_DRAWABLE_CLAZZ_NAME.equals(drawable.getClass().getName())) {
            fixVectorDrawableTinting(drawable);
        }
    }

    /**
     * Some drawable implementations have problems with mutation. This method returns false if
     * there is a known issue in the given drawable's implementation.
     */
    public static boolean canSafelyMutateDrawable(@NonNull Drawable drawable) {

        if (drawable instanceof DrawableContainer) {
            // If we have a DrawableContainer, let's traverse its child array
            final Drawable.ConstantState state = drawable.getConstantState();
            if (state instanceof DrawableContainer.DrawableContainerState) {
                final DrawableContainer.DrawableContainerState containerState =
                        (DrawableContainer.DrawableContainerState) state;
                for (final Drawable child : containerState.getChildren()) {
                    if (!canSafelyMutateDrawable(child)) {
                        return false;
                    }
                }
            }
        } else if (drawable instanceof WrappedDrawable) {
            return canSafelyMutateDrawable(((WrappedDrawable) drawable).getWrappedDrawable());
        } else if (drawable instanceof DrawableWrapper) {
            return canSafelyMutateDrawable(((DrawableWrapper) drawable).getWrappedDrawable());
        } else if (drawable instanceof ScaleDrawable) {
            return canSafelyMutateDrawable(((ScaleDrawable) drawable).getDrawable());
        }

        return true;
    }

    /**
     * VectorDrawable has an issue on API 21 where it sometimes doesn't create its tint filter.
     * Fixed by toggling it's state to force a filter creation.
     */
    private static void fixVectorDrawableTinting(final Drawable drawable) {
        final int[] originalState = drawable.getState();
        if (originalState == null || originalState.length == 0) {
            // The drawable doesn't have a state, so set it to be checked
            drawable.setState(SkinCompatThemeUtils.CHECKED_STATE_SET);
        } else {
            // Else the drawable does have a state, so clear it
            drawable.setState(SkinCompatThemeUtils.EMPTY_STATE_SET);
        }
        // Now set the original state
        drawable.setState(originalState);
    }
}
