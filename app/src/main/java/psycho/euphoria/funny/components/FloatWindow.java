package psycho.euphoria.funny.components;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.DisplayUtilities;

/**
 * Created by Administrator on 2014/12/13.
 */
public class FloatWindow {

    final int mLayoutId = R.layout.float_window;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private TextView mTextView;

    private RotateImageView mRotateImageView;
    /* @Inject(id = R.id.ui_float_content)*/
    private RelativeLayout relativeLayout_;
    private View mView;

    private int width_;
    private int height_;
    private View.OnLongClickListener mOnLongClickListener;

    final int widthPhone_ = DisplayUtilities.getPxWidth();
    final int heightPhone_ = DisplayUtilities.getPxHeight();

    private final Context mContext;

    private final WindowManager mWindowManager;

    public FloatWindow(Context context) {


        this.mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }
    public void setOnLongClickListener(View.OnLongClickListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }

    public void close() {
        if (mView != null) {

            mWindowManager.removeView(mView);
            mView=null;
        }
    }

    public void startProgressing() {

        if (mRotateImageView.getVisibility() == View.GONE) {
            mRotateImageView.setVisibility(View.VISIBLE);
        }

        mRotateImageView.startRotateAnimation();
    }

    public void stopProgressing() {
        if (mRotateImageView != null) {
            mRotateImageView.stopRotateAnimation();
            mRotateImageView.setVisibility(View.GONE);
        }
    }

    public void setText(String text) {
        mTextView.setText(text);
    }

    private void calculateSize() {
        if (DisplayUtilities.isHORIZONTAL()) {
            width_ = widthPhone_ / 3;
            height_ = heightPhone_ / 3;
        } else {
            width_ = (widthPhone_ / 4) * 3;
            height_ = heightPhone_ / 5;
        }
    }



    public void show() {

        final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = 0;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.setTitle("Toast");
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.gravity = Gravity.BOTTOM;


        mView = LayoutInflater.from(mContext).inflate(mLayoutId, null);

        calculateSize();
        relativeLayout_ = (RelativeLayout) mView.findViewById(R.id.ui_float_content);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) relativeLayout_.getLayoutParams();
        /*layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);*/
        /*layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);*/

        mTextView = (TextView) mView.findViewById(R.id.ui_float_textview);
        if (mOnLongClickListener != null)
            mTextView.setOnLongClickListener(mOnLongClickListener);
        mRotateImageView = (RotateImageView) mView.findViewById(R.id.ui_float_rotateimageview);
        layoutParams.height = height_;
        layoutParams.width = width_;
        layoutParams.topMargin = heightPhone_ - height_ - height_ / 3;
        layoutParams.gravity = Gravity.CENTER;
        relativeLayout_.setLayoutParams(layoutParams);

        mWindowManager.addView(mView, params);

    /*    mTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                final  int keyCode=keyEvent.getKeyCode();
                Log.e("keycode", TextUtilities.logFormat(keyEvent,keyCode));

                return false;
            }
        });*/

        mView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FloatWindow.this.close();
            }
        });
    }
}
