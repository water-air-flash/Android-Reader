package psycho.euphoria.funny.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import psycho.euphoria.funny.R;


/**
 * Created by Administrator on 2014/12/10.
 */
public class RotateImageView extends ImageView {
    Animation mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotateimageview_animation);


    private boolean mIsRotating = false;

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAnimation.setInterpolator(new LinearInterpolator());
        this.setImageResource(R.drawable.rotateimageview_image);
    }

    public void startRotateAnimation() {
        if (mIsRotating)
            return;
        startAnimation(mAnimation);
        mIsRotating = true;
    }

    public void stopRotateAnimation() {
        if (!mIsRotating)
            return;
        clearAnimation();
        mIsRotating = false;
    }
}
