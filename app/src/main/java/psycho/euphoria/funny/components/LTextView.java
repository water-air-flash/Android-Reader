package psycho.euphoria.funny.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import psycho.euphoria.funny.provider.DatabaseProvider;
import psycho.euphoria.funny.utils.*;

import java.io.*;
import java.net.*;
import java.text.BreakIterator;

import static psycho.euphoria.funny.utils.Constants.DATABASE_FILENAME;
import static psycho.euphoria.funny.utils.Constants.YOUDAO_API;

/**
 * Created by Administrator on 2015/1/23.
 */
public class LTextView extends TextView implements View.OnTouchListener {


    private int mGravity = Gravity.TOP | Gravity.START;
    private int mMinTouchOffset, mMaxTouchOffset;
    public LEditor mLEditor;
    private LSelectionController mLSelectionController;
    private boolean mIsTouchscreenMultiTouchDistinct;
    private WordIterator mWordIterator;

    private int mLength;
    private Layout mLayout;

    private Path mHighLightPath;

    private long blockTime = 0L;
    private Toolbar mToolbar;
    private final Paint mPaintHighLight = new Paint();


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isLayoutModeOptical(ViewGroup viewGroup) {
        return viewGroup.getLayoutMode() == ViewGroup.LAYOUT_MODE_OPTICAL_BOUNDS;
    }

    public static boolean isLayoutModeOptical(Object o) {
        return o instanceof ViewGroup && isLayoutModeOptical((ViewGroup) o);
    }

    public float convertToLocalHorizontalCoordinate(float x) {
        x -= getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        x = Math.max(0.0f, x);
        x = Math.min(getWidth() - getTotalPaddingRight() - 1, x);
        x += getScrollX();
        return x;
    }

    int viewportToContentVerticalOffset() {
        int offset = getExtendedPaddingTop() - getScrollY();
        if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
            offset += getVerticalOffset(false);
        }
        return offset;
    }

    int getVerticalOffset(boolean forceNormal) {
        int voffset = 0;
        final int gravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

        Layout l = this.getLayout();


        if (gravity != Gravity.TOP) {
            int boxht = getBoxHeight(l);
            int textht = l.getHeight();

            if (textht < boxht) {
                if (gravity == Gravity.BOTTOM)
                    voffset = boxht - textht;
                else // (gravity == Gravity.CENTER_VERTICAL)
                    voffset = (boxht - textht) >> 1;
            }
        }
        return voffset;
    }

    private int getBoxHeight(Layout l) {

        int padding =
                getExtendedPaddingTop() + getExtendedPaddingBottom();
        return getMeasuredHeight() - padding;
    }

    public int getLineAtCoordinate(float y) {
        y -= getTotalPaddingTop();
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
        y += getScrollY();
        return getLayout().getLineForVertical((int) y);
    }

    public LTextView(Context context) {
        super(context);
        initialize();
    }

    public LTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }


    public LTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {


        /*setContentText(getText(), true);*/
        mPaintHighLight.setColor(0X50ff5722);
        mPaintHighLight.setStyle(Paint.Style.FILL);
        this.setOnEditorActionListener(null);
        mLSelectionController = new LSelectionController(this);
        mLEditor = new LEditor(this, mLSelectionController);
        this.setOnTouchListener(this);
        mWordIterator = new WordIterator();
        mWordIterator.setCharSequence(getText(), 0, getText().length());
        mIsTouchscreenMultiTouchDistinct = InstanceProviders.isTouchscreenMultiTouchDistinct();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return null;
    }


    public void pauseTouchEventMillis(long time) {
        blockTime = System.currentTimeMillis() + time;
    }

    public void setContentText(CharSequence text, boolean isInitialize) {

        mLength = text.length();


        if (!isInitialize) {
            setText(text);
        }
        mLayout = getLayout();

    }

    public void setContentTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        mLayout = getLayout();

    }
    public void updateLayout(){
        mLayout = getLayout();

    }

    private boolean isPositionOnText(float x, float y) {
        Layout layout = getLayout();
        if (layout == null) return false;
        final int line = getLineAtCoordinate(y);
        x = convertToLocalHorizontalCoordinate(x);
        if (x < layout.getLineLeft(line)) return false;
        if (x > layout.getLineRight(line)) return false;
        return true;
    }

    public int getPreciseOffset(int x, int y) {
        Layout layout = getLayout();
        if (layout != null) {
            int topVisibleLine = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(topVisibleLine, x);
            int offset_x = (int) layout.getPrimaryHorizontal(offset);
            if (offset_x > x) {
                return layout.getOffsetToLeftOf(offset);
            }
        }
        return getOffset(x, y);
    }

    public int getOffset(int x, int y) {
        Layout layout = getLayout();
        int offset = -1;
        if (layout != null) {
            int topVisibleLine = layout.getLineForVertical(y);
            offset = layout.getOffsetForHorizontal(topVisibleLine, x);
        }
        return offset;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                final float x = event.getX();
                final float y = event.getY();
                mMinTouchOffset = mMaxTouchOffset = getOffsetForPosition(x, y);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mIsTouchscreenMultiTouchDistinct) {
                    updateMinAndMaxOffsets(event);
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mLEditor.getSelectionController() != null) {
                    if (mLEditor.getSelectionController().mIsShowing) {
                        hideSelectionHandle();
                        return true;
                    }
                }
                if (selectCurrentWord()) {
                    mLEditor.prepareCursorControllers();
                    mLEditor.getSelectionController().show();
                    if (mToolbar == null)
                        mToolbar = new Toolbar(this);
                    mToolbar.show();
                    new QueryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLSelectionController.getSelectionText());
                }
                break;

        }


    /*    final float x = event.getX();
        final float y = event.getY();


        if (isPositionOnText(x, y)) {
            final int offset = getPreciseOffset((int) x, (int) y);

            mLSelectionController.SelectionStart = mWordIterator.getBeginning(offset);
            mLSelectionController.SelectionEnd = mWordIterator.getEnd(offset);

            InstanceProviders.logToPush(mLSelectionController.SelectionStart, "mLSelectionController.SelectionStart");
            mLEditor.prepareCursorControllers();
            mLEditor.getSelectionController().show();
        }*/

        return true;
    }

    private long getLastTouchOffsets() {

        return InstanceProviders.packRangeInLong(mMinTouchOffset, mMaxTouchOffset);
    }

    private void updateMinAndMaxOffsets(MotionEvent event) {
        if (System.currentTimeMillis() > blockTime) {
            int pointerCount = event.getPointerCount();
            for (int index = 0; index < pointerCount; index++) {
                int offset = getOffsetForPosition(event.getX(index), event.getY(index));
                if (offset < mMinTouchOffset) mMinTouchOffset = offset;
                if (offset > mMaxTouchOffset) mMaxTouchOffset = offset;
            }
        }
    }


    private int findOffsetForPosition(float x, float y) {
        final Layout layout = getLayout();
        int line = layout.getLineForVertical((int) y);
        return layout.getOffsetForHorizontal(line, x);
    }

    private boolean selectCurrentWord() {

        long lastTouchOffsets = getLastTouchOffsets();
        final int minOffset = InstanceProviders.unpackRangeStartFromLong(lastTouchOffsets);
        final int maxOffset = InstanceProviders.unpackRangeEndFromLong(lastTouchOffsets);
        if (minOffset < 0 || minOffset >= mLength) return false;
        if (maxOffset < 0 || maxOffset >= mLength) return false;
        int selectionStart, selectionEnd;

        mWordIterator.setCharSequence(getText(), minOffset, maxOffset);
        selectionStart = mWordIterator.getBeginning(minOffset);
        selectionEnd = mWordIterator.getEnd(maxOffset);
        if (selectionStart == BreakIterator.DONE || selectionEnd == BreakIterator.DONE ||
                selectionStart == selectionEnd) {
            long range = getCharRange(minOffset);
            selectionStart = InstanceProviders.unpackRangeStartFromLong(range);
            selectionEnd = InstanceProviders.unpackRangeEndFromLong(range);
        }
        mLSelectionController.setSelection(selectionStart, selectionEnd);

        return !TextUtilities.isEmpty(mLSelectionController.getSelectionText());
    }

    private long getCharRange(int offset) {
        if (offset + 1 < mLength) {
            final char currentChar = getText().charAt(offset);
            final char nextChar = getText().charAt(offset + 1);
            if (Character.isSurrogatePair(currentChar, nextChar)) {
                return InstanceProviders.packRangeInLong(offset, offset + 2);
            }
        }
        if (offset < mLength) {
            return InstanceProviders.packRangeInLong(offset, offset + 1);
        }
        if (offset - 2 >= 0) {
            final char previousChar = getText().charAt(offset - 1);
            final char previousPreviousChar = getText().charAt(offset - 2);
            if (Character.isSurrogatePair(previousPreviousChar, previousChar)) {
                return InstanceProviders.packRangeInLong(offset - 2, offset);
            }
        }
        if (offset - 1 >= 0) {
            return InstanceProviders.packRangeInLong(offset - 1, offset);
        }
        return InstanceProviders.packRangeInLong(offset, offset);
    }

    @Override
    public void onAttachedToWindow() {
        mLEditor.onAttachedToWindow();
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {

        mLEditor.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    public void share() {

        InstanceProviders.getContext().startActivity(InstanceProviders.shareNormalPlainText(mLSelectionController.getSelectionText()));
    }

    public void hideSelectionHandle() {

        if (mToolbar != null)
            mToolbar.hide();
        mLEditor.hideControllers();
        mLSelectionController.SelectionStart = mLSelectionController.SelectionEnd = 0;
        invalidate();
    }

    public void copy() {

        String content = mLSelectionController.getSelectionText();
        InstanceProviders.pushStringToClipboard(content);
        String fullName = DATABASE_FILENAME + "/english.txt";
        File file = new File(fullName);

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(content.trim() + "\n\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InstanceProviders.showToast("文本已复制到剪切板.", true);
    }

    public void setContentText(Spanned spanned, boolean isInitialize, int offset) {

        setContentText(spanned, isInitialize);
        final int line = getLayout().getLineForOffset(offset);
        final int y = (int) ((line + 0.5) * getLineHeight());
        LogUtilities.logToPush(line, y);
        post(new Runnable() {
            @Override
            public void run() {

                final ScrollView scrollView = ((ScrollView) LTextView.this.getParent());
                scrollView.smoothScrollTo(0, y - scrollView.getHeight() / 2);
                LogUtilities.logToPush(line, y, y - scrollView.getHeight() / 2);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*;*/
        if (mLSelectionController.SelectionEnd > mLSelectionController.SelectionStart && mLSelectionController.SelectionStart >= 0 && !TextUtilities.isEmpty(mLSelectionController.getSelectionText()))
            drawHighlight(mLSelectionController.SelectionStart, mLSelectionController.SelectionEnd, canvas);
        else
            super.onDraw(canvas);
    }

    private void drawHighlight(int start, int end, Canvas canvas) {

        if (mHighLightPath == null)
            mHighLightPath = new Path();
        mHighLightPath.reset();
        if (mLayout == null)
            mLayout = getLayout();
        mLayout.getSelectionPath(start, end, mHighLightPath);
        mLayout.draw(canvas, mHighLightPath, mPaintHighLight, 0);


    }

    private class QueryTask extends AsyncTask<String, Void, Void> {

        private String nResult;

        private final Handler sHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                final int w = message.what;
                if (w == 0) {
                    InstanceProviders.showToast("网络未连接", false);
                } else {
                    mLEditor.setActionContentText(nResult);
                  /*  LogUtilities.logToPush("mLEditor.setActionContentText(nResult);", nResult);*/
                }
            }
        };

        private QueryTask() {

        }

        @Override
        protected void onPreExecute() {
            LTextView.this.mLEditor.showActionProgressBar();
        }

        @Override
        protected Void doInBackground(String... strings) {
            if (strings == null) return null;

            String word = strings[0].toLowerCase();
            nResult = DatabaseProvider.getInstance().query(word);
           /* LogUtilities.logToPush("DatabaseProvider.getInstance().query(word", nResult);*/
            if (TextUtilities.isEmpty(nResult)) {
                if (NetworkUtilities.isInternetConnected(InstanceProviders.getContext())) {

                    nResult = getExplain(getJson(word));
                    if (!TextUtilities.isEmpty(word) && !TextUtilities.isEmpty(nResult)) {
                        DatabaseProvider.getInstance().insert(word, nResult);
                    }
                } else {


                    sHandler.sendEmptyMessage(0);

                }
            }
            if (TextUtilities.isEmpty(nResult))
                nResult += " 没有找到相关词组。";
            nResult = word + " " + nResult;
            sHandler.sendEmptyMessage(1);
            return null;
        }

        private String getJson(String word) {
            word = word.replaceAll(" ", "%20");
            //InputStream inputStream = null;
            String result = null;
            try {

                final URL url = new URL(YOUDAO_API + URLEncoder.encode(word, "UTF-8"));
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.addRequestProperty("Content-Type", "application/json");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);

                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        return sb.toString();
                }


                // if (responseCode == HttpURLConnection.HTTP_OK) { // success
//                inputStream =c.getInputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line + "\n");
//                }
//                result = sb.toString();

                //   inputStream.close();


                //  } else {
                //   System.out.println("GET request not worked");
                // }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return result;
        }


        private String getExplain(String jsonString) {
            String result = "";
            try {
                final JSONObject jsonObject = new JSONObject(jsonString);


                try {
                    result = jsonObject.getJSONObject("basic").getString("phonetic");
                    if (!TextUtilities.isEmpty(result)) {
                        result = " /" + result + "/ ";
                    }
                } catch (Exception exception) {

                }

                try {
                /*抽取基本解释*/
                    result = jsonObject.getJSONObject("basic").getString("explains") + "\n";
                } catch (Exception exception) {
                }
             /*抽取网络释义*/
                JSONArray jsonArray = jsonObject.getJSONArray("web");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        result += jsonObject1.getString("key") + " : " + jsonObject1.getString("value") + "\n";
                    }
                }
            } catch (Exception exception) {
            }
        /*用正则表达式去除一些多余的字符*/
            return result.replaceAll("[\\[\\]\"]+", "");
        }
    }
}


