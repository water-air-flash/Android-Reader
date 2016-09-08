package psycho.euphoria.funny.utils;

import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

/**
 * Created by Administrator on 2015/1/25.
 */
public class InstanceProviders {
    private static DisplayMetrics mDisplayMetrics;
    private static Context mContext;
    private static boolean DEBUG = false;

    private static SharedPreferences mSharedPreferences;
    private static String TAG;
    private static ClipboardManager mClipboardManager;
    private static int mScaledDoubleTapSlop = -1;

    public static double getMemoryUsage() {
        final long max = Runtime.getRuntime().maxMemory();
        final long used = Runtime.getRuntime().totalMemory();

        return (double) used / (double) max;
    }

    public static double getBitmapMemoryUsage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return getMemoryUsage();
        }

        final long max = Runtime.getRuntime().maxMemory();
        final long used = Debug.getNativeHeapAllocatedSize();

        return (double) used / (double) max;
    }

    public static SharedPreferences getSharedPreferences(String name) {
        if (mSharedPreferences == null) {
            mSharedPreferences = mContext.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        }
        return mSharedPreferences;
    }

    public static SharedPreferences getSharedPreferences() {
       /* if (mSharedPreferences == null) {
            throw new IllegalStateException("");
        }*/
        return mSharedPreferences;
    }

    public static WindowManager getWindowManager() {

        return (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public static Context getContext() {
        return mContext;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(mContext);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        if (mDisplayMetrics == null) {
            mDisplayMetrics = context.getResources().getDisplayMetrics();
        }
        return mDisplayMetrics;
    }

    public static LayoutInflater getLayoutInflater() {
        return getLayoutInflater(mContext);
    }

    public static LayoutInflater getLayoutInflater(Context context) {

        return LayoutInflater.from(context);
    }

    public static int getScreenHeightPixels() {
        return getScreenHeightPixels(mContext);
    }

    public static int getScreenHeightPixels(Context context) {
        if (mDisplayMetrics == null) {
            getDisplayMetrics(context);
        }
        return mDisplayMetrics.heightPixels;
    }

    public static int getScreenWidthPixels() {
        return getScreenWidthPixels(mContext);
    }

    public static int getScreenWidthPixels(Context context) {
        if (mDisplayMetrics == null) {
            getDisplayMetrics(context);
        }
        return mDisplayMetrics.widthPixels;
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    public static String format(int i) {
        return String.format("%d\n", i);
    }

    public static String format(String name, int i) {
        return name + " = " + format(i);
    }

    public static Intent shareNormalPlainText(String shareContent) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareContent);
        return intent;
    }

    public static int getScaledDoubleTapSlop() {
        if (mScaledDoubleTapSlop == -1) {
            mScaledDoubleTapSlop = ViewConfiguration.get(mContext).getScaledDoubleTapSlop();
        }
        return mScaledDoubleTapSlop;
    }

    public static int getDimensionPixelSize(int resId) {
        return mContext.getResources().getDimensionPixelSize(resId);
    }

    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    public static int unpackRangeEndFromLong(long range) {
        return (int) (range & 0x00000000FFFFFFFFL);
    }

    /**
     * Pack 2 int values into a long, useful as a return value for a range
     *
     * @hide
     * @see #unpackRangeStartFromLong(long)
     * @see #unpackRangeEndFromLong(long)
     */
    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | end;
    }

    public static ClipboardManager getClipboardManager() {
        if (mClipboardManager == null)
            mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        return mClipboardManager;
    }

    public static void pushStringToClipboard(String plainText) {

        getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, plainText));
    }

    public static void showToast(String message, boolean isLong) {
        Toast.makeText(mContext, message, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static int dip2px(float dpValue) {
        final float scale = getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static Intent forFileManager() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static boolean isTouchscreenMultiTouchDistinct() {
        return mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
    }

    public static void logToPush(String tag, Object o) {
        if (DEBUG)
            Log.e(tag, String.format("%s=%s\n", o.toString(), o));
    }

    public static void logToPush(String tag, String name, Object o) {
        if (DEBUG)
            Log.e(tag, String.format("%s=%s\n", name, o));
    }

    public static void logToPushSharedPreferences(String message) {

        getSharedPreferences("pre_debug").edit().putString(String.format("%d", System.currentTimeMillis()), message).commit();
    }

    public static String getTextFromClipboardManger(ClipboardManager clipboardManager) {
        if (!(clipboardManager.hasPrimaryClip())) {
            return null;
        } /*else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            return null;
        }*/ else {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            return item.getText().toString();

        }
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public static void renameFiles(String directory) {

        File dir = new File(directory);
        if (!dir.isDirectory()) return;
        File[] lsf = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile())
                    return true;
                return false;

            }
        });

        Arrays.sort(lsf, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                long l = lhs.lastModified();
                long r = rhs.lastModified();
                if (l > r)
                    return 1;
                else if (l < r) return -1;
                return 0;
            }
        });
        int index = 0;
        for (File f : lsf) {
            String fileName = f.getName();
            String extension = "";

            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                extension = fileName.substring(i );
            }
            fileName = directory + "/" + String.format("%05d", index)+ extension;
            File ff = new File(fileName);
            if (!ff.isFile()) {

                f.renameTo(ff);
            }
index++;
        }
    }
}
