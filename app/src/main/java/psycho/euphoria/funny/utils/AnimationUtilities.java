package psycho.euphoria.funny.utils;

import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;

/**
 * Created by Administrator on 2014/12/11.
 */
public class AnimationUtilities {
    public static RotateAnimation getFlipAnimation(boolean isStart, Interpolator interpolator) {
        RotateAnimation rotateAnimation = null;
        if (isStart) {
            rotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        } else {
            rotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        }


        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(interpolator);
        rotateAnimation.setDuration(150);
        return rotateAnimation;
    }
}
