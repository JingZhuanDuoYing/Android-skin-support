package skin.support.constraint.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import skin.support.app.SkinLayoutInflater;
import skin.support.constraint.SkinCompatConstraintLayout;

public class SkinConstraintViewInflater implements SkinLayoutInflater {
    private static final String TAG = "SkinConstraintView";
    private static final String CONSTRAINT_LAYOUT_CLASS = "androidx.constraintlayout.widget.ConstraintLayout";

    @Override
    public View createView(Context context, final String name, AttributeSet attrs) {
        View view = null;
        if (CONSTRAINT_LAYOUT_CLASS.equals(name)) {
            try {
                view = new SkinCompatConstraintLayout(context, attrs);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create SkinCompatConstraintLayout for name: " + name, e);
            }
        }
        return view;
    }
}
