package github.hotstu.wxscroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author hglf [hglf](https://github.com/hotstu)
 * @desc
 * @since 10/9/19
 */
public class WXScroller extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {


    public interface ScrollerGroupAdapter {
        int getScrollerSize();

        String getScrollerItem(int index);
    }

    public interface OnScrollerGroupChangeListener {
        void onChange(int index, String item);
    }

    @IntDef({STATE_HIDDEN, STATE_VISIBLE, STATE_DRAGGING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }


    private static final int STATE_HIDDEN = 0;
    // Scroll thumb visible and moving along with the scrollbar
    private static final int STATE_VISIBLE = 1;
    // Scroll thumb being dragged by user
    private static final int STATE_DRAGGING = 2;

    private static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    private static final int[] EMPTY_STATE_SET = new int[]{};



    private final Drawable mVerticalTrackDrawable;
    private final int mVerticalTrackDrawableWidth;
    private final int marginBottom;
    private final int marginTop;

    private View itemView;
    private View popupView;
    private RecyclerView mRecyclerView;

    private int mState = STATE_HIDDEN;
    private ScrollerGroupAdapter mScrollerGroupAdapter = null;
    private OnScrollerGroupChangeListener mOnScrollerGroupChangeListener = null;
    private int mLastSendIndex = -1;


    public WXScroller(Context ctx) {
        this(ctx, R.style.WXScrollerDefault);
    }

    public WXScroller(Context ctx, @StyleRes int style) {
        TypedArray a = ctx.obtainStyledAttributes(style, R.styleable.WXScroller);
        mVerticalTrackDrawable = a.getDrawable(R.styleable.WXScroller_sidebar);
        int dimensionPixelSize = a.getDimensionPixelSize(R.styleable.WXScroller_android_width, 0);
        marginTop = a.getDimensionPixelSize(R.styleable.WXScroller_android_layout_marginTop, 0);
        marginBottom = a.getDimensionPixelSize(R.styleable.WXScroller_android_layout_marginBottom, 0);
        int itemLayoutId = a.getResourceId(R.styleable.WXScroller_item_layout, R.layout.wxscroller_item_layout_default);
        int popupLayoutId = a.getResourceId(R.styleable.WXScroller_popup_layout, R.layout.wxscroller_popup_layout_default);
        a.recycle();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        //we use a ViewGroup as parent to make the inflated view contains a layoutParam from layout xml
        FrameLayout parent = new FrameLayout(ctx);
        //TODO 注意这两个view只相当于一个drawable，参与测量、绘制，并没有事件处理和状态，后续如果有需要可以加入
        itemView = inflater.inflate(itemLayoutId, parent, false);
        popupView = inflater.inflate(popupLayoutId, parent, false);
        assert mVerticalTrackDrawable != null;
        mVerticalTrackDrawableWidth = Math.max(dimensionPixelSize, mVerticalTrackDrawable.getIntrinsicWidth());
    }

    public void setScrollerGroupAdapter(ScrollerGroupAdapter adapter) {
        if (mScrollerGroupAdapter == adapter) {
            return;
        }
        mScrollerGroupAdapter = adapter;
        requestRedraw();
    }

    public void setOnScrollerGroupChangeListener(OnScrollerGroupChangeListener listener) {
        mOnScrollerGroupChangeListener = listener;
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(this);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(this);
    }


    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        //这里的onInterceptTouchEvent和ViewGroup中的onInterceptTouchEvent不同
        //返回true，当前事件并没有带入到onTouchEvent
        //返回false，还能收到后续事件
        final boolean handled;
        if (mState == STATE_VISIBLE) {
            float x = e.getX();
            float y = e.getY();
            if (e.getAction() == MotionEvent.ACTION_DOWN && isPointInsideVerticalThumb(x, y)) {
                handled = true;
            } else {
                handled = false;
            }
        } else if (mState == STATE_DRAGGING) {
            handled = true;
        } else {
            handled = false;
        }
        if (handled) {
            onTouchEvent(mRecyclerView, e);
        }
        return handled;
    }


    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            setState(STATE_DRAGGING);
            verticalScrollTo(e.getY());
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mLastSendIndex = -1;
            setState(STATE_VISIBLE);
        } else if (action == MotionEvent.ACTION_MOVE && mState == STATE_DRAGGING) {
            verticalScrollTo(e.getY());
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mState == STATE_HIDDEN) {
            return;
        }
        int w = mRecyclerView.getWidth();
        int h = mRecyclerView.getHeight();

        int left = w - mVerticalTrackDrawableWidth;
        mVerticalTrackDrawable.setBounds(0, 0, mVerticalTrackDrawableWidth, h);
        canvas.translate(left, 0);
        mVerticalTrackDrawable.draw(canvas);
        drawItems(canvas);
        canvas.translate(-left, 0);
        if (mState == STATE_DRAGGING) {
            drawPopup(canvas);
        }
    }

    private void drawPopup(Canvas canvas) {
        //bind value
        if (mLastSendIndex < 0) {
            return;
        }
        if (popupView.getId() == android.R.id.text1) {
            ((TextView) popupView).setText(mScrollerGroupAdapter.getScrollerItem(mLastSendIndex));
        } else {
            View viewById = popupView.findViewById(android.R.id.text1);
            if (viewById != null) {
                ((TextView) viewById).setText(mScrollerGroupAdapter.getScrollerItem(mLastSendIndex));
            }
        }
        //measure
        int height = mRecyclerView.getHeight();
        int width = mRecyclerView.getWidth() - mVerticalTrackDrawableWidth;
        int specW = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int specH = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
        ViewGroup.LayoutParams layoutParams = popupView.getLayoutParams();

        int childSpecW = ViewGroup.getChildMeasureSpec(specW, 0, layoutParams.width);
        int chidlSpecH = ViewGroup.getChildMeasureSpec(specH, 0, layoutParams.height);

        popupView.measure(childSpecW, chidlSpecH);

        //layout
        int measuredWidth = popupView.getMeasuredWidth();
        int measuredHeight = popupView.getMeasuredHeight();
        popupView.layout(0, 0, measuredWidth, measuredHeight);

        //draw
        canvas.save();
        canvas.translate(width / 2 - measuredWidth / 2, height / 2 - measuredHeight / 2);
        popupView.draw(canvas);
        canvas.restore();
    }

    private void drawItems(Canvas canvas) {
        if (mScrollerGroupAdapter == null) {
            return;
        }
        if (mScrollerGroupAdapter.getScrollerSize() <= 0) {
            return;
        }
        int size = mScrollerGroupAdapter.getScrollerSize();
        int height = mRecyclerView.getHeight() / size;
        int width = mVerticalTrackDrawableWidth;
        int measureSpec1 = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measureSpec2 = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        canvas.save();
        for (int i = 0; i < size; i++) {
            String item = mScrollerGroupAdapter.getScrollerItem(i);
            if (itemView.getId() == android.R.id.text1) {
                ((TextView) itemView).setText(item);
            } else {
                View viewById = itemView.findViewById(android.R.id.text1);
                if (viewById != null) {
                    ((TextView) viewById).setText(item);
                }
            }
            itemView.measure(measureSpec1, measureSpec2);
            int measuredWidth = itemView.getMeasuredWidth();
            int measuredHeight = itemView.getMeasuredHeight();
            itemView.layout(0, 0, measuredWidth, measuredHeight);
            itemView.draw(canvas);
            canvas.translate(0, height);
        }
        canvas.restore();
    }


    private void verticalScrollTo(float y) {
        //这里设计不让滚动事件驱动recyclerview滑动，而是通过事件回调让调用者自己处理
        if (mOnScrollerGroupChangeListener == null) {
            return;
        }
        if (mScrollerGroupAdapter == null) {
            return;
        }
        if (mScrollerGroupAdapter.getScrollerSize() == 0) {
            return;
        }
        final int[] scrollbarRange = new int[]{marginTop, mRecyclerView.getHeight() - marginBottom};
        y = Math.max(scrollbarRange[0], Math.min(scrollbarRange[1], y));

        int groupOffset = (int) ((y - scrollbarRange[0]) / (scrollbarRange[1] - scrollbarRange[0]) * mScrollerGroupAdapter.getScrollerSize());
        if (groupOffset < 0) {
            groupOffset = 0;
        }
        if (groupOffset >= mScrollerGroupAdapter.getScrollerSize()) {
            groupOffset = mScrollerGroupAdapter.getScrollerSize() - 1;
        }
        if (mLastSendIndex != groupOffset) {
            mOnScrollerGroupChangeListener.onChange(groupOffset, mScrollerGroupAdapter.getScrollerItem(groupOffset));
            mLastSendIndex = groupOffset;
            requestRedraw();
        }
    }

    private boolean isPointInsideVerticalThumb(float x, float y) {
        return x >= mRecyclerView.getWidth() - mVerticalTrackDrawableWidth;
    }


    private void setState(@State int state) {
        if (state == STATE_DRAGGING && mState != STATE_DRAGGING) {
            mVerticalTrackDrawable.setState(PRESSED_STATE_SET);
        }
        if (mState == STATE_DRAGGING && state != STATE_DRAGGING) {
            mVerticalTrackDrawable.setState(EMPTY_STATE_SET);
        }
        boolean redraw = (mState != state);
        mState = state;
        if (redraw) {
            requestRedraw();
        }
    }


    public void show() {
        setState(STATE_VISIBLE);
    }

    public void hide() {
        setState(STATE_HIDDEN);
    }


    private void requestRedraw() {
        mRecyclerView.postInvalidate();
    }
}
