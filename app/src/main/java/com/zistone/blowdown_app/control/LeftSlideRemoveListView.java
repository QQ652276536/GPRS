package com.zistone.blowdown_app.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import com.zistone.blowdown_app.R;

public class LeftSlideRemoveListView extends ListView
{
    private final static int SNAP_VELOCITY = 600;
    //滑动类
    private Scroller m_scroller;
    //记录手势
    private VelocityTracker m_velocityTracker;
    private int m_touchSlop;
    private boolean m_isSlide = false;
    private int m_delta = 0;
    private int m_downX;
    private int m_downY;
    private int m_maxDistence;
    private int m_slidePosition = INVALID_POSITION;
    private LeftSlideRemoveAdapter.OnItemRemoveListener m_adapterListener;
    private LeftSlideRemoveAdapter m_removeAdapter;
    private View m_currentContentView, m_currentRemoveView;

    public LeftSlideRemoveListView(Context context)
    {
        this(context, null);
    }

    public LeftSlideRemoveListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        m_scroller = new Scroller(context);
        m_touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        m_maxDistence = context.getResources().getDimensionPixelSize(R.dimen.left_slide_remove_width);
    }

    /**
     * 移除监听
     */
    public LeftSlideRemoveAdapter.OnItemRemoveListener m_itemRemoveListener = new LeftSlideRemoveAdapter.OnItemRemoveListener()
    {
        @Override
        public void onItemRemove(int position)
        {
            if(m_adapterListener != null)
            {
                m_adapterListener.onItemRemove(position);
            }
            Clear();
            m_slidePosition = INVALID_POSITION;
        }
    };

    /**
     * 右滑
     */
    private void ScrollRight()
    {
        final int delta = m_delta;
        m_scroller.startScroll(delta, 0, -delta, 0, Math.abs(delta));
        m_delta = 0;
        postInvalidate();
    }

    /**
     * 左滑
     */
    private void ScrollLeft()
    {
        final int delta = m_maxDistence - m_delta;
        m_scroller.startScroll(m_delta, 0, delta, 0, Math.abs(delta));
        m_delta = m_maxDistence;
        postInvalidate();
    }

    /**
     * 添加手势
     * @param event
     */
    private void AddVelocityTracker(MotionEvent event)
    {
        if(m_velocityTracker == null)
        {
            m_velocityTracker = VelocityTracker.obtain();
        }
        m_velocityTracker.addMovement(event);
    }

    private int GetScrollVelocity()
    {
        m_velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) m_velocityTracker.getXVelocity();
        return velocity;
    }

    private void RecycleVelocityTracker()
    {
        if(m_velocityTracker != null)
        {
            m_velocityTracker.recycle();
            m_velocityTracker = null;
        }
    }

    private void Clear()
    {
        if(m_currentContentView != null)
        {
            m_delta = 0;
            m_currentContentView.scrollTo(0, 0);
            m_currentContentView = null;
            m_currentRemoveView.setVisibility(View.GONE);
            m_currentRemoveView = null;
        }
    }

    public void SetOnItemRemoveListener(LeftSlideRemoveAdapter.OnItemRemoveListener listener)
    {
        m_adapterListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                AddVelocityTracker(motionEvent);
                if(!m_scroller.isFinished())
                {
                    return super.dispatchTouchEvent(motionEvent);
                }
                //起始位置,当前position
                m_downX = (int) motionEvent.getX();
                m_downY = (int) motionEvent.getY();
                int position = pointToPosition(m_downX, m_downY);
                if(position == m_slidePosition)
                    break;
                m_slidePosition = position;

                if(m_slidePosition == INVALID_POSITION)
                {
                    return super.dispatchTouchEvent(motionEvent);
                }
                //恢复状态
                Clear();
                //获取当前界面
                View childView = getChildAt(m_slidePosition - getFirstVisiblePosition());
                m_currentContentView = childView.findViewById(R.id.view_content);
                m_currentRemoveView = childView.findViewById(R.id.text_remove);
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                if(m_currentContentView == null)
                    break;
                if(Math.abs(GetScrollVelocity()) > SNAP_VELOCITY || (Math.abs(motionEvent.getX() - m_downX) > m_touchSlop && Math.abs(motionEvent.getY() - m_downY) < m_touchSlop))
                {
                    //开始滑动
                    m_isSlide = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                if(m_currentContentView == null && m_isSlide)
                    break;

                //如果左滑小于4/5,按钮不显示
                if(m_delta < m_maxDistence * 4 / 5)
                {
                    m_currentRemoveView.setVisibility(View.GONE);
                    ScrollRight();
                }
                else if(m_delta < m_maxDistence)
                {
                    ScrollLeft();
                }
                RecycleVelocityTracker();
                m_isSlide = false;
                break;
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * 滑动事件
     *
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        if(m_isSlide && m_slidePosition != INVALID_POSITION)
        {
            final int action = motionEvent.getAction();
            int x = (int) motionEvent.getX();
            switch(action)
            {
                case MotionEvent.ACTION_MOVE:
                    AddVelocityTracker(motionEvent);
                    int deltaX = m_downX - x;
                    m_downX = x;
                    m_delta += deltaX;
                    if(m_delta < 0)
                    {
                        m_currentContentView.scrollTo(0, 0);
                        m_delta = 0;
                        m_currentRemoveView.setVisibility(View.GONE);
                    }
                    else if(m_delta >= m_maxDistence)
                    {
                        m_delta = m_maxDistence;
                        m_currentContentView.scrollTo(m_maxDistence, 0);
                        m_currentRemoveView.setVisibility(View.VISIBLE);
                        m_currentRemoveView.setTranslationX(0);
                    }
                    else
                    {
                        m_currentContentView.scrollBy(deltaX, 0);
                        m_currentRemoveView.setVisibility(View.VISIBLE);
                        m_currentRemoveView.setTranslationX(m_maxDistence - m_delta);
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public void computeScroll()
    {
        if(m_scroller.computeScrollOffset())
        {
            m_currentContentView.scrollTo(m_scroller.getCurrX(), m_scroller.getCurrY());
            m_currentRemoveView.setTranslationX(m_maxDistence - m_scroller.getCurrX());
            postInvalidate();
            if(m_scroller.isFinished())
            {
                m_currentContentView.scrollTo(m_delta, 0);
                m_currentRemoveView.setTranslationX(0);
            }
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter)
    {
        if(adapter instanceof LeftSlideRemoveAdapter)
        {
            super.setAdapter(adapter);

            m_removeAdapter = (LeftSlideRemoveAdapter) adapter;
            m_removeAdapter.m_listener = m_itemRemoveListener;
        }
        else
        {
            throw new IllegalArgumentException("必须是LeftSlideRemoveAdapter类");
        }
    }

}
