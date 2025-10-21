package skin.support.app;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.TintContextWrapper;
import androidx.appcompat.widget.VectorEnabledTintResources;

import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.R;
import skin.support.content.res.SkinCompatVectorResources;
import skin.support.utils.Slog;
import skin.support.widget.SkinCompatAutoCompleteTextView;
import skin.support.widget.SkinCompatButton;
import skin.support.widget.SkinCompatCheckBox;
import skin.support.widget.SkinCompatCheckedTextView;
import skin.support.widget.SkinCompatEditText;
import skin.support.widget.SkinCompatFrameLayout;
import skin.support.widget.SkinCompatImageButton;
import skin.support.widget.SkinCompatImageView;
import skin.support.widget.SkinCompatLinearLayout;
import skin.support.widget.SkinCompatMultiAutoCompleteTextView;
import skin.support.widget.SkinCompatProgressBar;
import skin.support.widget.SkinCompatRadioButton;
import skin.support.widget.SkinCompatRadioGroup;
import skin.support.widget.SkinCompatRatingBar;
import skin.support.widget.SkinCompatRelativeLayout;
import skin.support.widget.SkinCompatScrollView;
import skin.support.widget.SkinCompatSeekBar;
import skin.support.widget.SkinCompatSpinner;
import skin.support.widget.SkinCompatTextView;
import skin.support.widget.SkinCompatToolbar;
import skin.support.widget.SkinCompatView;

/**
 * A custom view inflater that creates skinnable views and wraps contexts for theme application.
 * Implements {@link SkinLayoutInflater} for view creation and {@link SkinWrapper} for context wrapping.
 */
public final class SkinAppCompatViewInflater implements SkinLayoutInflater, SkinWrapper {
    private static final String LOG_TAG = "SkinAppCompatViewInflater";

    /**
     * Constructs a new {@link SkinAppCompatViewInflater} and initializes vector resources.
     */
    public SkinAppCompatViewInflater() {
        SkinCompatVectorResources.getInstance();
    }

    /**
     * Creates a view with the specified name and attributes, trying framework views first,
     * then AppCompat-specific views.
     *
     * @param context The context to use for view creation.
     * @param name    The name of the view to create.
     * @param attrs   The attribute set for the view.
     * @return The created view, or null if creation fails.
     * @throws IllegalArgumentException if context, name, or attrs is null.
     */
    @Override
    public View createView(Context context, String name, AttributeSet attrs) {
        View view = createViewFromFV(context, name, attrs);

        if (view == null) {
            view = createViewFromV7(context, name, attrs);
        }
        return view;
    }

    private View createViewFromFV(Context context, String name, AttributeSet attrs) {
        if (name.contains(".")) {
            return null;
        }

        return switch (name) {
            case "View" -> new SkinCompatView(context, attrs);
            case "LinearLayout" -> new SkinCompatLinearLayout(context, attrs);
            case "RelativeLayout" -> new SkinCompatRelativeLayout(context, attrs);
            case "FrameLayout" -> new SkinCompatFrameLayout(context, attrs);
            case "TextView" -> new SkinCompatTextView(context, attrs);
            case "ImageView" -> new SkinCompatImageView(context, attrs);
            case "Button" -> new SkinCompatButton(context, attrs);
            case "EditText" -> new SkinCompatEditText(context, attrs);
            case "Spinner" -> new SkinCompatSpinner(context, attrs);
            case "ImageButton" -> new SkinCompatImageButton(context, attrs);
            case "CheckBox" -> new SkinCompatCheckBox(context, attrs);
            case "RadioButton" -> new SkinCompatRadioButton(context, attrs);
            case "RadioGroup" -> new SkinCompatRadioGroup(context, attrs);
            case "CheckedTextView" -> new SkinCompatCheckedTextView(context, attrs);
            case "AutoCompleteTextView" -> new SkinCompatAutoCompleteTextView(context, attrs);
            case "MultiAutoCompleteTextView" -> new SkinCompatMultiAutoCompleteTextView(context, attrs);
            case "RatingBar" -> new SkinCompatRatingBar(context, attrs);
            case "SeekBar" -> new SkinCompatSeekBar(context, attrs);
            case "ProgressBar" -> new SkinCompatProgressBar(context, attrs);
            case "ScrollView" -> new SkinCompatScrollView(context, attrs);
            default -> null;
        };
    }

    private View createViewFromV7(Context context, String name, AttributeSet attrs) {
        View view = null;
        if ("androidx.appcompat.widget.Toolbar".equals(name)) {
            view = new SkinCompatToolbar(context, attrs);
        }
        return view;
    }

    @Override
    public Context wrapContext(Context context, View parent, AttributeSet attrs) {
        boolean readAppTheme = true; /* Read read app:theme as a fallback at all times for legacy reasons */
        boolean wrapContext = VectorEnabledTintResources.shouldBeUsed(); /* Only tint wrap the context if enabled */

        // We can emulate Lollipop's android:theme attribute propagating down the view hierarchy
        // by using the parent's context
        // We then apply the theme on the context, if specified
        context = themifyContext(context, attrs, readAppTheme);
        if (wrapContext) {
            context = TintContextWrapper.wrap(context);
        }
        return context;
    }

    /**
     * Applies the android:theme attribute to the context, if specified.
     *
     * @param context      The original context.
     * @param attrs        The attribute set containing theme information.
     * @param useAppTheme  Whether to read the app:theme attribute (for legacy support).
     * @return The themed context, or the original context if no theme is applied.
     */
    @NonNull
    private static Context themifyContext(@NonNull Context context, @NonNull AttributeSet attrs,
                                          boolean useAppTheme) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
        try {
            int themeId = useAppTheme ? typedArray.getResourceId(R.styleable.View_theme, 0) : 0;
            if (themeId != 0) {
                if (Slog.DEBUG) {
                    Slog.i("app:theme is deprecated; use android:theme instead");
                }
                if (!(context instanceof ContextThemeWrapper) ||
                        ((ContextThemeWrapper) context).getThemeResId() != themeId) {
                    context = new ContextThemeWrapper(context, themeId);
                }
            }
            return context;
        } finally {
            typedArray.recycle();
        }
    }
}