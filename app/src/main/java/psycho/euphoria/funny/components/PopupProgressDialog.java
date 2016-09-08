package psycho.euphoria.funny.components;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.InstanceProviders;

/**
 * Created by Administrator on 2014/12/10.
 */
public class PopupProgressDialog {

    private PopupWindow mPopupWindow;
    private Context mContext;
    private final LayoutInflater mLayoutInflater;

    private ViewGroup mViewGroup;
    private RotateImageView mRotateImageView;
    private TextView mTextView;
    private View.OnLongClickListener mOnLongClickListener;
    private static final float mHeight = InstanceProviders.getContext().getResources().getDisplayMetrics().heightPixels;
    private static final int mHeight_ = 180;

    public PopupProgressDialog(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initialize(mContext);
    }

    private void initialize(Context context) {
        //mMargin = DisplayUtilities.dip2px(context, 6.0F);
        setPopupWindow();


        mTextView = (TextView) mViewGroup.findViewById(R.id.ui_popupprogressdialog_textview);
    }


    private void setPopupWindow() {

        mViewGroup = (ViewGroup) mLayoutInflater.inflate(R.layout.popupprogressdialog_layout, null);
        mPopupWindow = new PopupWindow(mViewGroup, -2, -2);
        mPopupWindow.setClippingEnabled(true);
        mPopupWindow.setOutsideTouchable(true);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnLongClickListener(View.OnLongClickListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }

    public void show(View view, int y) {
        if (mPopupWindow.isShowing()) return;
        mPopupWindow.setHeight(mHeight_);


        if (mHeight - y > mHeight_)
            mPopupWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, y);
        else
            mPopupWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, y - 185);
        if (mOnLongClickListener != null)
            mTextView.setOnLongClickListener(mOnLongClickListener);

    }

    public void hide() {
        if (mPopupWindow != null)
            mPopupWindow.dismiss();
    }


    public void startProgressing() {
        if (mRotateImageView == null) {
            mRotateImageView = (RotateImageView) mViewGroup.findViewById(R.id.ui_popupprogressdialog_rotateimageview);

            if (mRotateImageView.getVisibility() == View.GONE) {
                mRotateImageView.setVisibility(View.VISIBLE);
            }
        }
        mRotateImageView.startRotateAnimation();
    }

    public void stopProgressing() {
        if (mRotateImageView != null) {
            mRotateImageView.stopRotateAnimation();
            mRotateImageView.setVisibility(View.GONE);
        }
    }

    public void setTextView(String text) {
        this.mTextView.setText(text);
    }
}
