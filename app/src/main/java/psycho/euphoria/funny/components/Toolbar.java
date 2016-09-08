package psycho.euphoria.funny.components;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.InstanceProviders;


/**
 * Created by Administrator on 2015/1/25.
 */
public class Toolbar {

    private  PopupWindow mPopupWindow;
    private final LTextView mLTextView;


    public Toolbar(LTextView lTextView) {
        this.mLTextView = lTextView;

    }

    public void show() {
        this.mPopupWindow = new PopupWindow(mLTextView);
        mPopupWindow.setWidth(InstanceProviders.getScreenWidthPixels());
        mPopupWindow.setHeight(InstanceProviders.dip2px(50));
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final View view = InstanceProviders.getLayoutInflater().inflate(R.layout.toolbar, null);

        Button buttonCopy = (Button) view.findViewById(R.id.ui_toolbar_copy);
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mLTextView.copy();
                mLTextView.hideSelectionHandle();
            }
        });

        Button buttonShare = (Button) view.findViewById(R.id.ui_toolbar_share);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLTextView.share();
                mLTextView.hideSelectionHandle();
            }
        });
        Button buttonCancel = (Button) view.findViewById(R.id.ui_toolbar_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mLTextView.hideSelectionHandle();
            }
        });
        mPopupWindow.setContentView(view);
        mPopupWindow.showAtLocation(mLTextView, Gravity.BOTTOM, 0, 0);
    }

    public void hide() {

        mPopupWindow.dismiss();
    }
}
