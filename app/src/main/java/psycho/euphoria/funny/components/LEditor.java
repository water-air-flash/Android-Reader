package psycho.euphoria.funny.components;


import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.Layout;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import psycho.euphoria.funny.R;
import psycho.euphoria.funny.utils.InstanceProviders;


public class LEditor {
    private static final String TAG = "LEditor";
    private static final float[] TEMP_POSITION = new float[2];

    private boolean mIsTouchscreenMultiTouchDistinct;
    private static final int sDrawableCursor = R.drawable.cursor;
    SelectionModifierCursorController mSelectionModifierCursorController;
    boolean mSelectionControllerEnabled;
    boolean mDiscardNextActionUp;
    boolean mPreserveDetachedSelection;
    boolean mTemporaryDetach;
    private Drawable mSelectHandleLeft;
    private Drawable mSelectHandleRight;
    private PositionListener mPositionListener;

    private static final int sScaledDoubleTapSlopSquare = InstanceProviders.getScaledDoubleTapSlop() * InstanceProviders.getScaledDoubleTapSlop();

    private final LTextView mTextView;

    private final LSelectionController mLSelectionController;


    LEditor(LTextView textView, LSelectionController mLSelectionController) {
        mTextView = textView;
        this.mLSelectionController = mLSelectionController;
        mIsTouchscreenMultiTouchDistinct = InstanceProviders.isTouchscreenMultiTouchDistinct();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void onAttachedToWindow() {
        mTemporaryDetach = false;
        final ViewTreeObserver observer = mTextView.getViewTreeObserver();
        if (mSelectionModifierCursorController != null) {
            mSelectionModifierCursorController.resetTouchOffsets();
            observer.addOnTouchModeChangeListener(mSelectionModifierCursorController);
        }
        if (mTextView.hasTransientState()) {
            if (mLSelectionController != null) {
                if (mLSelectionController.SelectionStart != mLSelectionController.SelectionEnd) {
                    mTextView.setHasTransientState(false);
                }
            }
        }
    }

    void onDetachedFromWindow() {
        if (mSelectionModifierCursorController != null) {
            mSelectionModifierCursorController.onDetached();
        }
        mPreserveDetachedSelection = true;
        hideControllers();
        mPreserveDetachedSelection = false;
        mTemporaryDetach = false;
    }

    void prepareCursorControllers() {
        mSelectionControllerEnabled = mTextView.getLayout() != null;
        if (!mSelectionControllerEnabled) {
            if (mSelectionModifierCursorController != null) {
                mSelectionModifierCursorController.onDetached();
                mSelectionModifierCursorController = null;
            }
        }
    }

    void hideControllers() {
        if (mSelectionModifierCursorController != null)
            mSelectionModifierCursorController.hide();

    }


    private boolean touchPositionIsInSelection() {
        int selectionStart = mLSelectionController.SelectionStart;
        int selectionEnd = mLSelectionController.SelectionEnd;
        if (selectionStart == selectionEnd) {
            return false;
        }
        if (selectionStart > selectionEnd) {
            int tmp = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = tmp;
            mLSelectionController.setSelection(selectionStart, selectionEnd);
        }
        SelectionModifierCursorController selectionController = getSelectionController();
        int minOffset = selectionController.getMinTouchOffset();
        int maxOffset = selectionController.getMaxTouchOffset();
        return ((minOffset >= selectionStart) && (maxOffset < selectionEnd));
    }

    private PositionListener getPositionListener() {
        if (mPositionListener == null) {
            mPositionListener = new PositionListener();
        }
        return mPositionListener;
    }

    private interface TextViewPositionListener {
        public void updatePosition(int parentPositionX, int parentPositionY,
                                   boolean parentPositionChanged, boolean parentScrolled);
    }

    private boolean isPositionVisible(final float positionX, final float positionY) {
        synchronized (TEMP_POSITION) {
            final float[] position = TEMP_POSITION;
            position[0] = positionX;
            position[1] = positionY;
            View view = mTextView;
            while (view != null) {
                if (view != mTextView) {
                    position[0] -= view.getScrollX();
                    position[1] -= view.getScrollY();
                }
                if (position[0] < 0 || position[1] < 0 ||
                        position[0] > view.getWidth() || position[1] > view.getHeight()) {
                    return false;
                }
                if (!view.getMatrix().isIdentity()) {
                    view.getMatrix().mapPoints(position);
                }
                position[0] += view.getLeft();
                position[1] += view.getTop();
                final ViewParent parent = view.getParent();
                if (parent instanceof View) {
                    view = (View) parent;
                } else {
                    view = null;
                }
            }
        }
        return true;
    }

    private boolean isOffsetVisible(int offset) {
        Layout layout = mTextView.getLayout();
        if (layout == null) return false;
        final int line = layout.getLineForOffset(offset);
        final int lineBottom = layout.getLineBottom(line);
        final int primaryHorizontal = (int) layout.getPrimaryHorizontal(offset);
        return isPositionVisible(primaryHorizontal,
                lineBottom + mTextView.viewportToContentVerticalOffset());
    }

    private boolean isPositionOnText(float x, float y) {
        Layout layout = mTextView.getLayout();
        if (layout == null) return false;
        final int line = mTextView.getLineAtCoordinate(y);
        x = mTextView.convertToLocalHorizontalCoordinate(x);
        if (x < layout.getLineLeft(line)) return false;
        if (x > layout.getLineRight(line)) return false;
        return true;
    }

    public void setActionContentText(String text) {

        mSelectionModifierCursorController.setActionContentText(text);
    }

    public void showActionProgressBar() {
        mSelectionModifierCursorController.showActionProgressBar();
    }


    private int getLastTapPosition() {
        if (mSelectionModifierCursorController != null) {
            int lastTapPosition = mSelectionModifierCursorController.getMinTouchOffset();
            if (lastTapPosition >= 0) {
                if (lastTapPosition > mTextView.getText().length()) {
                    lastTapPosition = mTextView.getText().length();
                }
                return lastTapPosition;
            }
        }
        return -1;
    }

    void onWindowFocusChanged(boolean hasWindowFocus) {
    }


    SelectionModifierCursorController getSelectionController() {
        if (!mSelectionControllerEnabled) {
            return null;
        }
        if (mSelectionModifierCursorController == null) {
            mSelectionModifierCursorController = new SelectionModifierCursorController();
            final ViewTreeObserver observer = mTextView.getViewTreeObserver();
            observer.addOnTouchModeChangeListener(mSelectionModifierCursorController);
        }
        return mSelectionModifierCursorController;
    }

    void onScrollChanged() {
        if (mPositionListener != null) {
            mPositionListener.onScrollChanged();
        }
    }


    private class PositionListener implements ViewTreeObserver.OnPreDrawListener {
        private final int MAXIMUM_NUMBER_OF_LISTENERS = 3;
        private TextViewPositionListener[] mPositionListeners =
                new TextViewPositionListener[MAXIMUM_NUMBER_OF_LISTENERS];
        private boolean mCanMove[] = new boolean[MAXIMUM_NUMBER_OF_LISTENERS];
        private boolean mPositionHasChanged = true;
        private int mPositionX, mPositionY;
        private int mNumberOfListeners;
        private boolean mScrollHasChanged;
        final int[] mTempCoords = new int[2];

        public void addSubscriber(TextViewPositionListener positionListener, boolean canMove) {
            if (mNumberOfListeners == 0) {
                updatePosition();
                ViewTreeObserver vto = mTextView.getViewTreeObserver();
                vto.addOnPreDrawListener(this);
            }
            int emptySlotIndex = -1;
            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                TextViewPositionListener listener = mPositionListeners[i];
                if (listener == positionListener) {
                    return;
                } else if (emptySlotIndex < 0 && listener == null) {
                    emptySlotIndex = i;
                }
            }
            mPositionListeners[emptySlotIndex] = positionListener;
            mCanMove[emptySlotIndex] = canMove;
            mNumberOfListeners++;
        }

        public void removeSubscriber(TextViewPositionListener positionListener) {
            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                if (mPositionListeners[i] == positionListener) {
                    mPositionListeners[i] = null;
                    mNumberOfListeners--;
                    break;
                }
            }
            if (mNumberOfListeners == 0) {
                ViewTreeObserver vto = mTextView.getViewTreeObserver();
                vto.removeOnPreDrawListener(this);
            }
        }

        public int getPositionX() {
            return mPositionX;
        }

        public int getPositionY() {
            return mPositionY;
        }

        @Override
        public boolean onPreDraw() {
            updatePosition();
            for (int i = 0; i < MAXIMUM_NUMBER_OF_LISTENERS; i++) {
                if (mPositionHasChanged || mScrollHasChanged || mCanMove[i]) {
                    TextViewPositionListener positionListener = mPositionListeners[i];
                    if (positionListener != null) {
                        positionListener.updatePosition(mPositionX, mPositionY,
                                mPositionHasChanged, mScrollHasChanged);
                    }
                }
            }
            mScrollHasChanged = false;
            return true;
        }

        private void updatePosition() {
            mTextView.getLocationInWindow(mTempCoords);
            mPositionHasChanged = mTempCoords[0] != mPositionX || mTempCoords[1] != mPositionY;
            mPositionX = mTempCoords[0];
            mPositionY = mTempCoords[1];
        }

        public void onScrollChanged() {
            mScrollHasChanged = true;
        }
    }

    private abstract class PinnedPopupWindow implements TextViewPositionListener {
        protected PopupWindow mPopupWindow;
        protected ViewGroup mContentView;
        int mPositionY;

        protected abstract void createPopupWindow();

        protected abstract void initContentView();

        protected abstract int getTextOffset();

        protected abstract int getVerticalLocalPosition(int line);

        protected abstract int clipVertically(int positionY);

        public PinnedPopupWindow() {
            createPopupWindow();
            mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
            mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            initContentView();

            mPopupWindow.setContentView(mContentView);
        }

        public void show() {
            getPositionListener().addSubscriber(this, false);
            computeLocalPosition();
            final PositionListener positionListener = getPositionListener();
            updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
        }

        protected void measureContent() {

            mContentView.measure(0,
                    View.MeasureSpec.makeMeasureSpec(InstanceProviders.getScreenHeightPixels(),
                            View.MeasureSpec.AT_MOST));
        }

        private void computeLocalPosition() {
            measureContent();

            final int offset = getTextOffset();

            final int line = mTextView.getLayout().getLineForOffset(offset);
            mPositionY = getVerticalLocalPosition(line);
            mPositionY += mTextView.viewportToContentVerticalOffset();
        }

        private void updatePosition(int parentPositionX, int parentPositionY) {

            int positionY = parentPositionY + mPositionY;
            positionY = clipVertically(positionY);


            if (isShowing()) {
                mPopupWindow.update(0, positionY-20, -1, -1);
            } else {
                mPopupWindow.showAtLocation(mTextView, Gravity.NO_GRAVITY,
                        0, positionY);
            }
        }

        public void hide() {
            mPopupWindow.dismiss();
            getPositionListener().removeSubscriber(this);
        }

        @Override
        public void updatePosition(int parentPositionX, int parentPositionY,
                                   boolean parentPositionChanged, boolean parentScrolled) {
            if (isShowing() && isOffsetVisible(getTextOffset())) {
                if (parentScrolled) computeLocalPosition();
                updatePosition(parentPositionX, parentPositionY);
            } else {
                hide();
            }
        }

        public boolean isShowing() {
            return mPopupWindow.isShowing();
        }
    }

    private class ActionPopupWindow extends PinnedPopupWindow {
        private static final int POPUP_TEXT_LAYOUT = R.layout.ltextview_action_mode;

        private ProgressBar nProgressBar;
        private TextView nTextView;

        @Override
        protected void createPopupWindow() {
            mPopupWindow = new PopupWindow(InstanceProviders.getContext(), null,
                    android.R.attr.textSelectHandleWindowStyle);
            mPopupWindow.setClippingEnabled(true);
        }

        @Override
        protected void initContentView() {


            mContentView = (ViewGroup) InstanceProviders.getLayoutInflater().inflate(POPUP_TEXT_LAYOUT, null);
            nProgressBar = (ProgressBar) mContentView.findViewById(R.id.ui_ltextview_button_progressbar);
            nTextView = (TextView) mContentView.findViewById(R.id.ui_ltextview_button_content);
        }

        @Override
        public void show() {

            nTextView.setText("");
            nProgressBar.setVisibility(View.VISIBLE);
            super.show();
        }


        public void showProgressBar() {

            nTextView.setText("");

        }


        public void setContentText(String text) {
            nTextView.setText(text);
            nProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected int getTextOffset() {
            return (mLSelectionController.SelectionEnd + mLSelectionController.SelectionStart) / 2;
        }

        @Override
        protected int getVerticalLocalPosition(int line) {
            return mTextView.getLayout().getLineTop(line) - mContentView.getMeasuredHeight();
        }

        @Override
        protected int clipVertically(int positionY) {
            if (positionY < 0) {
                final int offset = getTextOffset();
                final Layout layout = mTextView.getLayout();
                final int line = layout.getLineForOffset(offset);
                positionY += layout.getLineBottom(line) - layout.getLineTop(line);
                positionY += mContentView.getMeasuredHeight();
                final Drawable handle = InstanceProviders.getContext().getResources().getDrawable(
                        sDrawableCursor);
                positionY += handle.getIntrinsicHeight();
            }
            return positionY;
        }
    }

    private abstract class HandleView extends View implements TextViewPositionListener {
        protected Drawable mDrawable;
        protected Drawable mDrawableLtr;
        protected Drawable mDrawableRtl;
        private final PopupWindow mContainer;
        private int mPositionX, mPositionY;
        private boolean mIsDragging;
        private float mTouchToWindowOffsetX, mTouchToWindowOffsetY;
        protected int mHotSpotX;
        protected int mHorizontalGravity;
        private float mTouchOffsetY;
        private float mIdealVerticalOffset;
        private int mLastParentX, mLastParentY;
        protected ActionPopupWindow mActionPopupWindow;
        private int mPreviousOffset = -1;
        private boolean mPositionHasChanged = true;
        /*private Runnable mActionPopupShower;*/
        private int mMinSize;

        public HandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(InstanceProviders.getContext());
            mContainer = new PopupWindow(InstanceProviders.getContext(), null,
                    android.R.attr.textSelectHandleWindowStyle);
            mContainer.setSplitTouchEnabled(true);
            mContainer.setClippingEnabled(false);
            mContainer.setContentView(this);
            mDrawableLtr = drawableLtr;
            mDrawableRtl = drawableRtl;
            mMinSize = InstanceProviders.getContext().getResources().getDimensionPixelSize(
                    R.dimen.text_handle_min_size);
            updateDrawable();
            final int handleHeight = getPreferredHeight();
            mTouchOffsetY = -0.3f * handleHeight;
            mIdealVerticalOffset = 0.7f * handleHeight;
        }

        protected void updateDrawable() {
            final int offset = getCurrentCursorOffset();
            final boolean isRtlCharAtOffset = mTextView.getLayout().isRtlCharAt(offset);
            mDrawable = isRtlCharAtOffset ? mDrawableRtl : mDrawableLtr;
            mHotSpotX = getHotSpotX(mDrawable, isRtlCharAtOffset);
            mHorizontalGravity = getHorizontalGravity(isRtlCharAtOffset);
        }

        protected abstract int getHotSpotX(Drawable drawable, boolean isRtlRun);

        protected abstract int getHorizontalGravity(boolean isRtlRun);

        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        private final long[] mPreviousOffsetsTimes = new long[HISTORY_SIZE];
        private final int[] mPreviousOffsets = new int[HISTORY_SIZE];
        private int mPreviousOffsetIndex = 0;
        private int mNumberPreviousOffsets = 0;

        private void startTouchUpFilter(int offset) {
            mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            mPreviousOffsetIndex = (mPreviousOffsetIndex + 1) % HISTORY_SIZE;
            mPreviousOffsets[mPreviousOffsetIndex] = offset;
            mPreviousOffsetsTimes[mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp() {
            final long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = mPreviousOffsetIndex;
            final int iMax = Math.min(mNumberPreviousOffsets, HISTORY_SIZE);
            while (i < iMax && (now - mPreviousOffsetsTimes[index]) < TOUCH_UP_FILTER_DELAY_AFTER) {
                i++;
                index = (mPreviousOffsetIndex - i + HISTORY_SIZE) % HISTORY_SIZE;
            }
            if (i > 0 && i < iMax &&
                    (now - mPreviousOffsetsTimes[index]) > TOUCH_UP_FILTER_DELAY_BEFORE) {
                positionAtCursorOffset(mPreviousOffsets[index], false);
            }
        }

        public boolean offsetHasBeenChanged() {
            return mNumberPreviousOffsets > 1;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(getPreferredWidth(), getPreferredHeight());
        }

        private int getPreferredWidth() {
            return Math.max(mDrawable.getIntrinsicWidth(), mMinSize);
        }

        private int getPreferredHeight() {
            return Math.max(mDrawable.getIntrinsicHeight(), mMinSize);
        }

        public void show() {
            if (isShowing()) return;
            getPositionListener().addSubscriber(this, true);
            mPreviousOffset = -1;
            positionAtCursorOffset(getCurrentCursorOffset(), false);
            hideActionPopupWindow();
        }

        protected void dismiss() {
            mIsDragging = false;
            mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            getPositionListener().removeSubscriber(this);
        }

        void showActionPopupWindow() {
            if (mActionPopupWindow == null) {
                mActionPopupWindow = new ActionPopupWindow();
            }
            mActionPopupWindow.show();
           /* if (mActionPopupShower == null) {
                mActionPopupShower = new Runnable() {
                    public void run() {
                        mActionPopupWindow.show();
                    }
                };
            } else {
                mTextView.removeCallbacks(mActionPopupShower);
            }
            mTextView.postDelayed(mActionPopupShower, delay);*/
        }

        protected void hideActionPopupWindow() {
            /*if (mActionPopupShower != null) {
                mTextView.removeCallbacks(mActionPopupShower);
            }*/
            if (mActionPopupWindow != null) {
                if (mActionPopupWindow.isShowing()) {
                    mActionPopupWindow.hide();

                }
            }

        }

        public boolean isShowing() {
            return mContainer.isShowing();
        }

        private boolean isVisible() {
            if (mIsDragging) {
                return true;
            }
            return isPositionVisible(mPositionX + mHotSpotX, mPositionY);
        }

        public abstract int getCurrentCursorOffset();

        protected abstract void updateSelection(int offset);

        public abstract void updatePosition(float x, float y);

        protected void positionAtCursorOffset(int offset, boolean parentScrolled) {
            Layout layout = mTextView.getLayout();
            if (layout == null) {
                prepareCursorControllers();
                return;
            }
            boolean offsetChanged = offset != mPreviousOffset;
            if (offsetChanged || parentScrolled) {
                if (offsetChanged) {
                    updateSelection(offset);
                    addPositionToTouchUpFilter(offset);
                }
                final int line = layout.getLineForOffset(offset);
                mPositionX = (int) (layout.getPrimaryHorizontal(offset) - 0.5f - mHotSpotX -
                        getHorizontalOffset() + getCursorOffset());
                mPositionY = layout.getLineBottom(line);
                mPositionY += mTextView.viewportToContentVerticalOffset();
                mPreviousOffset = offset;
                mPositionHasChanged = true;
            }
        }

        public void updatePosition(int parentPositionX, int parentPositionY,
                                   boolean parentPositionChanged, boolean parentScrolled) {
            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled);
            if (parentPositionChanged || mPositionHasChanged) {
                if (mIsDragging) {
                    if (parentPositionX != mLastParentX || parentPositionY != mLastParentY) {
                        mTouchToWindowOffsetX += parentPositionX - mLastParentX;
                        mTouchToWindowOffsetY += parentPositionY - mLastParentY;
                        mLastParentX = parentPositionX;
                        mLastParentY = parentPositionY;
                    }
                    onHandleMoved();
                }
                if (isVisible()) {
                    final int positionX = parentPositionX + mPositionX;
                    final int positionY = parentPositionY + mPositionY;
                    if (isShowing()) {
                        mContainer.update(positionX, positionY, -1, -1);
                    } else {
                        mContainer.showAtLocation(mTextView, Gravity.NO_GRAVITY,
                                positionX, positionY);
                    }
                } else {
                    if (isShowing()) {
                        dismiss();
                    }
                }
                mPositionHasChanged = false;
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            final int drawWidth = mDrawable.getIntrinsicWidth();
            final int left = getHorizontalOffset();
            mDrawable.setBounds(left, 0, left + drawWidth, mDrawable.getIntrinsicHeight());
            mDrawable.draw(c);
        }

        private int getHorizontalOffset() {
            final int width = getPreferredWidth();
            final int drawWidth = mDrawable.getIntrinsicWidth();
            final int left;
            switch (mHorizontalGravity) {
                case Gravity.LEFT:
                    left = 0;
                    break;
                default:
                case Gravity.CENTER:
                    left = (width - drawWidth) / 2;
                    break;
                case Gravity.RIGHT:
                    left = width - drawWidth;
                    break;
            }
            return left;
        }

        protected int getCursorOffset() {
            return 0;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    startTouchUpFilter(getCurrentCursorOffset());
                    mTouchToWindowOffsetX = ev.getRawX() - mPositionX;
                    mTouchToWindowOffsetY = ev.getRawY() - mPositionY;
                    final PositionListener positionListener = getPositionListener();
                    mLastParentX = positionListener.getPositionX();
                    mLastParentY = positionListener.getPositionY();
                    mIsDragging = true;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float rawX = ev.getRawX();
                    final float rawY = ev.getRawY();
                    final float previousVerticalOffset = mTouchToWindowOffsetY - mLastParentY;
                    final float currentVerticalOffset = rawY - mPositionY - mLastParentY;
                    float newVerticalOffset;
                    if (previousVerticalOffset < mIdealVerticalOffset) {
                        newVerticalOffset = Math.min(currentVerticalOffset, mIdealVerticalOffset);
                        newVerticalOffset = Math.max(newVerticalOffset, previousVerticalOffset);
                    } else {
                        newVerticalOffset = Math.max(currentVerticalOffset, mIdealVerticalOffset);
                        newVerticalOffset = Math.min(newVerticalOffset, previousVerticalOffset);
                    }
                    mTouchToWindowOffsetY = newVerticalOffset + mLastParentY;
                    final float newPosX = rawX - mTouchToWindowOffsetX + mHotSpotX;
                    final float newPosY = rawY - mTouchToWindowOffsetY + mTouchOffsetY;
                    updatePosition(newPosX, newPosY);
                    break;
                }
                case MotionEvent.ACTION_UP:
                    filterOnTouchUp();
                    mIsDragging = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mIsDragging = false;
                    break;
            }
            return true;
        }

        public boolean isDragging() {
            return mIsDragging;
        }

        void onHandleMoved() {
            hideActionPopupWindow();
        }

        public void onDetached() {
            hideActionPopupWindow();
        }
    }

    private class SelectionStartHandleView extends HandleView {
        public SelectionStartHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        @Override
        protected int getHotSpotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return drawable.getIntrinsicWidth() / 4;
            } else {
                return (drawable.getIntrinsicWidth() * 3) / 4;
            }
        }

        @Override
        protected int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun ? Gravity.RIGHT : Gravity.LEFT;
        }

        @Override
        public int getCurrentCursorOffset() {
            return mLSelectionController.SelectionStart;
        }

        @Override
        public void updateSelection(int offset) {
            mLSelectionController.setSelection(offset,
                    mLSelectionController.SelectionEnd);
            InstanceProviders.logToPush(TAG, "SelectionStartHandleView updateSelection", offset);
            updateDrawable();
        }

        @Override
        public void updatePosition(float x, float y) {
            int offset = mTextView.getPreciseOffset((int) x, (int) y);
            final int selectionEnd = mLSelectionController.SelectionEnd;
            if (offset >= selectionEnd) offset = Math.max(0, selectionEnd - 1);
            InstanceProviders.logToPush(TAG, "updatePosition(float x, float y)", offset);

            positionAtCursorOffset(offset, false);
        }

        public ActionPopupWindow getActionPopupWindow() {
            return mActionPopupWindow;
        }
    }

    private class SelectionEndHandleView extends HandleView {
        public SelectionEndHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        @Override
        protected int getHotSpotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return (drawable.getIntrinsicWidth() * 3) / 4;
            } else {
                return drawable.getIntrinsicWidth() / 4;
            }
        }

        @Override
        protected int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun ? Gravity.LEFT : Gravity.RIGHT;
        }

        @Override
        public int getCurrentCursorOffset() {
            return mLSelectionController.SelectionEnd;

        }

        @Override
        public void updateSelection(int offset) {
            mLSelectionController.setSelection(mLSelectionController.SelectionStart, offset);
            InstanceProviders.logToPush(TAG, "SelectionEndHandleView mLSelectionController.SelectionStart", mLSelectionController.SelectionStart);
            InstanceProviders.logToPush(TAG, "SelectionEndHandleView updateSelection", offset);
            updateDrawable();
        }

        @Override
        public void updatePosition(float x, float y) {
            int offset = mTextView.getOffsetForPosition(x, y);
            final int selectionStart = mLSelectionController.SelectionStart;
            if (offset <= selectionStart) {
                offset = Math.min(selectionStart + 1, mTextView.getText().length());
            }
            positionAtCursorOffset(offset, false);
        }

        public void setActionPopupWindow(ActionPopupWindow actionPopupWindow) {
            mActionPopupWindow = actionPopupWindow;
        }
    }

    private interface CursorController extends ViewTreeObserver.OnTouchModeChangeListener {
        public void show();

        public void hide();

        public void onDetached();
    }

    class SelectionModifierCursorController implements CursorController {
        /*private static final int DELAY_BEFORE_REPLACE_ACTION = 200;*/
        private SelectionStartHandleView mStartHandle;
        private SelectionEndHandleView mEndHandle;
        private int mMinTouchOffset, mMaxTouchOffset;
        private long mPreviousTapUpTime = 0;
        private float mDownPositionX, mDownPositionY;
        private boolean mGestureStayedInTapRegion;

        public boolean mIsShowing = false;

        SelectionModifierCursorController() {
            resetTouchOffsets();
        }

        public void show() {
            initDrawables();
            initHandles();
            mIsShowing = true;
        }

        private void initDrawables() {
            if (mSelectHandleLeft == null) {
                mSelectHandleLeft = InstanceProviders.getContext().getResources().getDrawable(
                        sDrawableCursor);
            }
            if (mSelectHandleRight == null) {
                mSelectHandleRight = InstanceProviders.getContext().getResources().getDrawable(
                        sDrawableCursor);
            }
        }

        private void initHandles() {
            if (mStartHandle == null) {
                mStartHandle = new SelectionStartHandleView(mSelectHandleLeft, mSelectHandleRight);
            }
            if (mEndHandle == null) {
                mEndHandle = new SelectionEndHandleView(mSelectHandleRight, mSelectHandleLeft);
            }
            mStartHandle.show();
            mEndHandle.show();
            mStartHandle.showActionPopupWindow();
            mEndHandle.setActionPopupWindow(mStartHandle.getActionPopupWindow());
        }

        public void hide() {
            if (mStartHandle != null) mStartHandle.hide();
            if (mEndHandle != null) mEndHandle.hide();
            mIsShowing = false;
        }

        public void onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    final float x = event.getX();
                    final float y = event.getY();
                    mMinTouchOffset = mMaxTouchOffset = mTextView.getOffsetForPosition(x, y);
                    if (mGestureStayedInTapRegion) {
                        long duration = SystemClock.uptimeMillis() - mPreviousTapUpTime;
                        if (duration <= ViewConfiguration.getDoubleTapTimeout()) {
                            final float deltaX = x - mDownPositionX;
                            final float deltaY = y - mDownPositionY;
                            final float distanceSquared = deltaX * deltaX + deltaY * deltaY;

                            boolean stayedInArea = distanceSquared < sScaledDoubleTapSlopSquare;
                            if (stayedInArea && isPositionOnText(x, y)) {
                                mDiscardNextActionUp = true;
                            }
                        }
                    }
                    mDownPositionX = x;
                    mDownPositionY = y;
                    mGestureStayedInTapRegion = true;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    if (mIsTouchscreenMultiTouchDistinct) {
                        updateMinAndMaxOffsets(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mGestureStayedInTapRegion) {
                        final float deltaX = event.getX() - mDownPositionX;
                        final float deltaY = event.getY() - mDownPositionY;
                        final float distanceSquared = deltaX * deltaX + deltaY * deltaY;
                        if (distanceSquared > sScaledDoubleTapSlopSquare) {
                            mGestureStayedInTapRegion = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mPreviousTapUpTime = SystemClock.uptimeMillis();
                    break;
            }
        }

        private void updateMinAndMaxOffsets(MotionEvent event) {
            int pointerCount = event.getPointerCount();
            for (int index = 0; index < pointerCount; index++) {
                int offset = mTextView.getOffsetForPosition(event.getX(index), event.getY(index));
                if (offset < mMinTouchOffset) mMinTouchOffset = offset;
                if (offset > mMaxTouchOffset) mMaxTouchOffset = offset;
            }
        }

        public int getMinTouchOffset() {
            return mMinTouchOffset;
        }

        public int getMaxTouchOffset() {
            return mMaxTouchOffset;
        }

        public void resetTouchOffsets() {
            mMinTouchOffset = mMaxTouchOffset = -1;
        }

        public boolean isSelectionStartDragged() {
            return mStartHandle != null && mStartHandle.isDragging();
        }

        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        @Override
        public void onDetached() {
            final ViewTreeObserver observer = mTextView.getViewTreeObserver();
            observer.removeOnTouchModeChangeListener(this);
            if (mStartHandle != null) mStartHandle.onDetached();
            if (mEndHandle != null) mEndHandle.onDetached();
        }


        public void showActionProgressBar() {

            if (mStartHandle.getActionPopupWindow() != null) {
                mStartHandle.getActionPopupWindow().show();
            }
        }

        public void setActionContentText(String text) {
            if (mStartHandle.getActionPopupWindow() != null) {
                mStartHandle.getActionPopupWindow().setContentText(text);
            }
        }
    }
}
