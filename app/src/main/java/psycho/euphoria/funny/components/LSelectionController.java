package psycho.euphoria.funny.components;

/**
 * Created by Administrator on 2015/1/25.
 */
public class LSelectionController {
    public int SelectionEnd=0;
    public int SelectionStart=0;
    public final LTextView mLTextView;


    public LSelectionController(LTextView textView) {
        mLTextView = textView;
    }

    public void setSelection(int start, int end) {
        SelectionStart = Math.min(start, end);
        SelectionEnd = Math.max(start, end);
        if (SelectionEnd > SelectionStart && SelectionStart >= 0) {
            mLTextView.invalidate();
        }
    }

    public String getSelectionText() {

        return mLTextView.getText().subSequence(SelectionStart, SelectionEnd).toString();
    }
}
