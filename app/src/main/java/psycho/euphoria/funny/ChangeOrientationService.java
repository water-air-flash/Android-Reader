package psycho.euphoria.funny;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Administrator on 2014/12/21.
 */
public class ChangeOrientationService extends Service {


    private View view_;

    public ChangeOrientationService() {


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        view_ = new View(this);
        final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGB_888);
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        windowManager.addView(view_, layoutParams);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (view_ != null ) {
            windowManager.removeView(view_);
        }
    }

    /**
     * Created by Administrator on 2015/1/28.
     */
    public static class ReadActivity {
    }
}
