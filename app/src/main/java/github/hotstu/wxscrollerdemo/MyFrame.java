package github.hotstu.wxscrollerdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 10/9/19
 */
public class MyFrame extends FrameLayout {
    public MyFrame(@NonNull Context context) {
        super(context);
    }

    public MyFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        PrintAction.print(ev.getAction());
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PrintAction.print(event.getAction());
        return true;
    }
}
