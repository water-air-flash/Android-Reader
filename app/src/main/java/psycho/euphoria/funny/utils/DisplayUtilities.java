package psycho.euphoria.funny.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.opengl.GLES10;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Administrator on 2014/12/10.
 */
public class DisplayUtilities {
    private static final Object sLock = new Object();
    private static Pair<Integer, Integer> maxBitmapSize;
    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        int maxBitmapDimension = Math.max(maxTextureSize[0], DEFAULT_MAX_BITMAP_DIMENSION);
        maxBitmapSize = Pair.create(maxBitmapDimension, maxBitmapDimension);
    }

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int getTouchSlop() {
        return ViewConfiguration.get(InstanceProviders.getContext()).getScaledTouchSlop();
    }

    public static boolean isHORIZONTAL() {
       /* *
         * Rotation constant: 0 degree rotation (natural orientation)

        public static final int ROTATION_0 = 0;
        *
         * Rotation constant: 90 degree rotation.

        public static final int ROTATION_90 = 1;
        *
         * Rotation constant: 180 degree rotation.

        public static final int ROTATION_180 = 2;
        *
         * Rotation constant: 270 degree rotation.

        public static final int ROTATION_270 = 3;*/
        int orientation = InstanceProviders.getWindowManager().getDefaultDisplay().getRotation();

        return orientation == 1 || orientation == 3;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int getPxHeight() {
        return InstanceProviders.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static int getPxWidth() {
        return InstanceProviders.getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float getSP(Context context, float f) {
        final TypedValue typedValue = new TypedValue();
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        return typedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, f, displayMetrics);
    }

    public static float SP2PX(Context context, float f) {
        final TypedValue typedValue = new TypedValue();
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        return f / displayMetrics.scaledDensity;
    }

    public static int computeImageSampleSize(Pair<Integer, Integer> srcSize, Pair<Integer, Integer> targetSize,
                                             boolean powerOf2Scale) {
        final int srcWidth = srcSize.first;
        final int srcHeight = srcSize.second;
        final int targetWidth = targetSize.first;
        final int targetHeight = targetSize.second;

        int scale = 1;


        if (powerOf2Scale) {
            final int halfWidth = srcWidth / 2;
            final int halfHeight = srcHeight / 2;
            while ((halfWidth / scale) > targetWidth || (halfHeight / scale) > targetHeight) { // ||
                scale *= 2;
            }
        } else {
            scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight); // max
        }

           /* case CROP:
                if (powerOf2Scale) {
                    final int halfWidth = srcWidth / 2;
                    final int halfHeight = srcHeight / 2;
                    while ((halfWidth / scale) > targetWidth && (halfHeight / scale) > targetHeight) { // &&
                        scale *= 2;
                    }
                } else {
                    scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight); // min
                }
                break;*/


        if (scale < 1)

        {
            scale = 1;
        }

        scale =

                considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);

        return scale;
    }

    public static Bitmap scaleImage(Bitmap bitmap, Pair<Integer, Integer> sourceSize, Pair<Integer, Integer> destinationSize) {

        final float scale;
        final Matrix matrix = new Matrix();
        if (destinationSize.first <= sourceSize.first && destinationSize.second <= sourceSize.second) {
            scale = 1.0f;
        } else {
            scale = Math.min((float) sourceSize.first / (float) destinationSize.first,
                    (float) sourceSize.second / (float) destinationSize.second);
        }
        int dx = (int) ((sourceSize.first - destinationSize.first * scale) * 0.5f + 0.5f);
        int dy = (int) ((sourceSize.second - destinationSize.second * scale) * 0.5f + 0.5f);
        matrix.postScale(scale, scale);
        matrix.postTranslate(dx, dy);


        return Bitmap.createBitmap(bitmap, 0, 0, destinationSize.first, destinationSize.second, matrix, true);

    }
    public static Bitmap decodeSampledBitmapFromByteArray(byte[] data,  Pair<Integer, Integer>  maxSize, Bitmap.Config config) {
        synchronized (sLock) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            options.inSampleSize = calculateInSampleSize(options, maxSize.first, maxSize.second);
            options.inJustDecodeBounds = false;
            if (config != null) {
                options.inPreferredConfig = config;
            }
            try {
                return BitmapFactory.decodeByteArray(data, 0, data.length, options);
            } catch (Throwable e) {
                return null;
            }
        }
    }
    public static Bitmap decodeSampledBitmapFromFile(String filename, Pair<Integer, Integer> maxSize, Bitmap.Config config) {
        synchronized (sLock) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;
            options.inInputShareable = true;
            BitmapFactory.decodeFile(filename, options);
            options.inSampleSize = calculateInSampleSize(options, maxSize.first, maxSize.second);
            options.inJustDecodeBounds = false;
            if (config != null) {
                options.inPreferredConfig = config;
            }
            try {
                return BitmapFactory.decodeFile(filename, options);
            } catch (Throwable e) {

                return null;
            }
        }
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > maxWidth || height > maxHeight) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) maxHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) maxWidth);
            }

            final float totalPixels = width * height;

            final float maxTotalPixels = maxWidth * maxHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > maxTotalPixels) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
        final int maxWidth = maxBitmapSize.first;
        final int maxHeight = maxBitmapSize.second;
        while ((srcWidth / scale) > maxWidth || (srcHeight / scale) > maxHeight) {
            if (powerOf2) {
                scale *= 2;
            } else {
                scale++;
            }
        }
        return scale;
    }
}
