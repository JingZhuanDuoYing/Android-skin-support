package skin.support.app;

import android.content.Context;
import android.content.res.TypedArray;

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

public class SkinAppCompatViewInflater implements SkinLayoutInflater, SkinWrapper {
    private static final String LOG_TAG = "SkinAppCompatViewInflater";

    public SkinAppCompatViewInflater() {
        SkinCompatVectorResources.getInstance();
    }

    @Override
    public View createView(Context context, String name, AttributeSet attrs) {
        View view = createViewFromFV(context, name, attrs);

        if (view == null) {
            view = createViewFromV7(context, name, attrs);
        }
        return view;
    }

    private View createViewFromFV(Context context, String name, AttributeSet attrs) {
        View view = null;
        if (name.contains(".")) {
            return null;
        }
        switch (name) {
            case "View" -> view = new SkinCompatView(context, attrs);
            case "LinearLayout" -> view = new SkinCompatLinearLayout(context, attrs);
            case "RelativeLayout" -> view = new SkinCompatRelativeLayout(context, attrs);
            case "FrameLayout" -> view = new SkinCompatFrameLayout(context, attrs);
            case "TextView" -> view = new SkinCompatTextView(context, attrs);
            case "ImageView" -> view = new SkinCompatImageView(context, attrs);
            case "Button" -> view = new SkinCompatButton(context, attrs);
            case "EditText" -> view = new SkinCompatEditText(context, attrs);
            case "Spinner" -> view = new SkinCompatSpinner(context, attrs);
            case "ImageButton" -> view = new SkinCompatImageButton(context, attrs);
            case "CheckBox" -> view = new SkinCompatCheckBox(context, attrs);
            case "RadioButton" -> view = new SkinCompatRadioButton(context, attrs);
            case "RadioGroup" -> view = new SkinCompatRadioGroup(context, attrs);
            case "CheckedTextView" -> view = new SkinCompatCheckedTextView(context, attrs);
            case "AutoCompleteTextView" -> view = new SkinCompatAutoCompleteTextView(context, attrs);
            case "MultiAutoCompleteTextView" -> view = new SkinCompatMultiAutoCompleteTextView(context, attrs);
            case "RatingBar" -> view = new SkinCompatRatingBar(context, attrs);
            case "SeekBar" -> view = new SkinCompatSeekBar(context, attrs);
            case "ProgressBar" -> view = new SkinCompatProgressBar(context, attrs);
            case "ScrollView" -> view = new SkinCompatScrollView(context, attrs);
            default -> {
            }
        }
        return view;
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
        final boolean isPre21 = false;

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
     * Allows us to emulate the {@code android:theme} attribute for devices before L.
     */
    private static Context themifyContext(Context context, AttributeSet attrs,
                                          boolean useAppTheme) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
        int themeId = 0;
        if (useAppTheme) {
            // ...if that didn't work, try reading app:theme (for legacy reasons) if enabled
            themeId = a.getResourceId(R.styleable.View_theme, 0);

            if (themeId != 0) {
                Slog.i(LOG_TAG, "app:theme is now deprecated. "
                        + "Please move to using android:theme instead.");
            }
        }
        a.recycle();

        if (themeId != 0 && (!(context instanceof ContextThemeWrapper)
                || ((ContextThemeWrapper) context).getThemeResId() != themeId)) {
            // If the context isn't a ContextThemeWrapper, or it is but does not have
            // the same theme as we need, wrap it in a new wrapper
            context = new ContextThemeWrapper(context, themeId);
        }
        return context;
    }

}
