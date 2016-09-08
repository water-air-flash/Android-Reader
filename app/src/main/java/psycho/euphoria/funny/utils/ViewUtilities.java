package psycho.euphoria.funny.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Administrator on 2015/1/29.
 */
public class ViewUtilities {

    public static boolean toggleVisibility(View view) {

        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
            return false;
        } else {
            view.setVisibility(View.VISIBLE);
            return true;
        }
    }

    public static void show(View view) {

        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);

        }
    }

    public static void hide(View view) {
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }
    }

    public static View findView(View parent, int resId) {

        return parent.findViewById(resId);
    }

    public static void hideKeyboard() {
        final InputMethodManager inputManager = (InputMethodManager) InstanceProviders.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
