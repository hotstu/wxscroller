package github.hotstu.wxscrollerdemo;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 9/29/19
 */
public class PrintAction {
    static final String TAG = "PrintAction";

    public static void print(int action) {
        print(TAG, action);
    }

    public static void print(String tag, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(tag, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e(tag, "ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_UP:
                Log.e(tag, "ACTION_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(tag, "ACTION_MOVE");
                break;
            default:
                Log.e(tag, "OTHER:" + action);
                break;
        }
    }
}
