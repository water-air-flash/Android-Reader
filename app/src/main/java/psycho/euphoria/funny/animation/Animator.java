package psycho.euphoria.funny.animation;

import android.graphics.Canvas;

/**
 * Created by Administrator on 2015/1/29.
 */
public interface Animator {

    /**
     * Returns the speed of this animation in FPS.
     *
     * @return
     */
    int getAnimationSpeed();

    /**
     * Advances the animation by 1 frame.
     */
    void advanceOneFrame();


    /**
     * Draw an animation frame on the given Canvas.
     *
     * @param canvas
     */
    void draw(Canvas canvas);

    /**
     * Checks if this Animator is done animating.
     *
     * @return
     */
    boolean isFinished();

    /**
     * Stop the animation.
     */
    void stop();

}