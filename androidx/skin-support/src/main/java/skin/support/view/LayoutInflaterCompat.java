package skin.support.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A utility class to manage custom {@link LayoutInflater.Factory2} instances for view inflation.
 * This class provides methods to set and retrieve a custom factory for creating views during
 * inflation, ensuring compatibility with modern Android APIs (21+).
 */
public final class LayoutInflaterCompat {
    private static final String TAG = "LayoutInflaterCompat";

    /**
     * Wraps a {@link LayoutInflaterFactory} to implement {@link LayoutInflater.Factory2}.
     */
    private static class Factory2Wrapper implements LayoutInflater.Factory2 {
        private final LayoutInflaterFactory mDelegateFactory;

        Factory2Wrapper(@NonNull LayoutInflaterFactory delegateFactory) {
            mDelegateFactory = delegateFactory;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return mDelegateFactory.onCreateView(null, name, context, attrs);
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return mDelegateFactory.onCreateView(parent, name, context, attrs);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" + mDelegateFactory + "}";
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private LayoutInflaterCompat() {
        // Prevent instantiation
    }

    /**
     * Sets a custom {@link LayoutInflater.Factory2} for creating views during inflation.
     * This method must be called only once, as subsequent calls may be ignored by the system.
     *
     * @param inflater The {@link LayoutInflater} to set the factory on.
     * @param factory The {@link LayoutInflater.Factory2} to use for view creation.
     * @throws IllegalArgumentException if inflater or factory is null.
     */
    public static void setFactory2(@NonNull LayoutInflater inflater, @NonNull LayoutInflater.Factory2 factory) {
        if (inflater == null || factory == null) {
            throw new IllegalArgumentException("Inflater and factory must not be null");
        }
        inflater.setFactory2(factory);
    }

    /**
     * Sets a custom {@link LayoutInflaterFactory} for creating views during inflation.
     * This method wraps the provided factory into a {@link LayoutInflater.Factory2} implementation.
     *
     * @param inflater The {@link LayoutInflater} to set the factory on.
     * @param factory The {@link LayoutInflaterFactory} to use for view creation.
     * @throws IllegalArgumentException if inflater or factory is null.
     */
    public static void setFactory(@NonNull LayoutInflater inflater, @NonNull LayoutInflaterFactory factory) {
        if (inflater == null || factory == null) {
            throw new IllegalArgumentException("Inflater and factory must not be null");
        }
        setFactory2(inflater, new Factory2Wrapper(factory));
    }

    /**
     * Retrieves the {@link LayoutInflaterFactory} associated with the given {@link LayoutInflater}.
     *
     * @param inflater The {@link LayoutInflater} to query.
     * @return The {@link LayoutInflaterFactory} if set and wrapped, or null otherwise.
     */
    @Nullable
    public static LayoutInflaterFactory getFactory(@NonNull LayoutInflater inflater) {
        if (inflater == null) {
            return null;
        }
        LayoutInflater.Factory factory = inflater.getFactory();
        if (factory instanceof Factory2Wrapper) {
            return ((Factory2Wrapper) factory).mDelegateFactory;
        }
        return null;
    }
}