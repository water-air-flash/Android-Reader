package psycho.euphoria.funny.utils;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Administrator on 2015/1/22.
 */
public class LogUtilities {
    public static String TAG;
    public final static boolean DEBUG = true;

    public static void logToSharedPreferences(Exception e) {
        logToSharedPreferences(InstanceProviders.getSharedPreferences(), e);

    }

    public static void logToSharedPreferences(SharedPreferences sharedPreferences, Exception e) {
        sharedPreferences.edit().putString(TextUtilities.formatMillis((int) System.currentTimeMillis()) + " : " + String.format("%s", System.currentTimeMillis()), String.format("%s=%s", e, e.getMessage())).commit();
    }

    public static void buildShortClassTag(Object cls, StringBuilder out) {
        if (cls == null) {
            out.append("null");
        } else {
            String simpleName = cls.getClass().getSimpleName();
            if (simpleName == null || simpleName.length() <= 0) {
                simpleName = cls.getClass().getName();
                int end = simpleName.lastIndexOf('.');
                if (end > 0) {
                    simpleName = simpleName.substring(end + 1);
                }
            }
            out.append(simpleName);
            out.append('{');
            out.append(Integer.toHexString(System.identityHashCode(cls)));
        }
    }

   /* public static void logToPush(Object o) {
        if (TAG == null) {
            TAG = InstanceProviders.getContext().getPackageName();
        }

        Log.e(TAG, String.format("%s=%s\n", o.toString(), o));

    }*/

    public static void logToPush(Object... objects) {
        if (TAG == null) {
            TAG = InstanceProviders.getContext().getPackageName();
        }

        String message = "";

        for (Object o : objects) {

            if (o instanceof String) {
                message += o.toString() + " -> ";
            } else {
                message += String.format(" %s; ", o);
            }
        }
        Log.e(TAG, message);

    }

/*    public static void logToPush(Object o, String name) {

        if (TAG == null) {
            TAG = InstanceProviders.getContext().getPackageName();
        }
        Log.e(TAG, String.format("%s=%s\n", name, o));

    }*/

    public static void logToSharedPreferences(SharedPreferences sharedPreferences, Throwable throwable) {
        sharedPreferences.edit().putString(TextUtilities.formatMillis((int) System.currentTimeMillis()) + " : " + String.format("%s", System.currentTimeMillis()), String.format("%s=%s", throwable, throwable.getMessage())).commit();
    }
}
