package com.zistone.blowdown_app.control;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.blowdown_app.R;

public class LeftSlideRemoveView extends RecyclerView
{
    //滑动最大、最小距离
    private int m_touchMax, m_touchMin;
    //当前按下的坐标、移动距离
    private int m_xDown, m_yDown, m_xMove, m_yMove;
    //当前选中的item索引(这个很重要)
    private int m_currentSelectPosition;
    //滑动
    private Scroller m_scroller;
    private LinearLayout m_currentItemLayout, m_lastItemLayout;
    //隐藏部分
    private LinearLayout m_linearHidden;
    //隐藏部分长度
    private int m_hiddenWidth;
    //记录连续移动的长度
    private int m_moveWidth = 0;
    //是否第一次触摸
    private boolean m_isFirstTouch = true;
    private Context m_context;
    //点击隐藏的"删除"事件
    public OnRightClickListener m_rightListener;

    public LeftSlideRemoveView(Context context)
    {
        this(context, null);
    }

    public LeftSlideRemoveView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LeftSlideRemoveView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        m_context = context;
        //滑动到最小距离
        m_touchMin = ViewConfiguration.get(context).getScaledTouchSlop();
        //滑动的最大距离
        m_touchMax = ((int) (100 * context.getResources().getDisplayMetrics().density + 0.5f));
        //初始化Scroller
        m_scroller = new Scroller(context, new LinearInterpolator(context, null));
    }

    /**
     * 触摸事件
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch(e.getAction())
        {
            //按下
            case MotionEvent.ACTION_DOWN:
            {
                //记录当前按下的坐标
                m_xDown = x;
                m_yDown = y;
                //获取第一个可见Item的位置
                int firstPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                Rect itemRect = new Rect();
                final int count = getChildCount();
                for(int i = 0; i < count; i++)
                {
                    final View view = getChildAt(i);
                    if(view.getVisibility() == View.VISIBLE)
                    {
                        //获取子View可点击的矩形左、上、右、下边界相对于父View左顶点的距离(偏移量)
                        view.getHitRect(itemRect);
                        //判断按下的坐标是否处于子View
                        if(itemRect.contains(x, y))
                        {
                            m_currentSelectPosition = firstPosition + i;
                            break;
                        }
                    }
                }
                //第一次时触摸,不用重置上一次的item
                if(m_isFirstTouch)
                {
                    m_isFirstTouch = false;
                }
                //屏幕再次接收到触摸时,恢复上一次Item的状态
                else
                {
                    //找到最后一个Item且有Item已经移动过
                    if(m_lastItemLayout != null && m_moveWidth > 0)
                    {
                        //将Item右移,恢复原位
                        ScrollRight(m_lastItemLayout, (0 - m_moveWidth));
                        //清空移动距离
                        m_hiddenWidth = 0;
                        m_moveWidth = 0;
                    }
                }
                //取到当前选中的Item,赋给m_currentItemLayout,以便对其进行左移
                View item = getChildAt(m_currentSelectPosition - firstPosition);
                if(item != null)
                {
                    //获取当前选中的Item
                    LeftSlideRemoveAdapter.ViewHolder viewHolder = (LeftSlideRemoveAdapter.ViewHolder) getChildViewHolder(item);
                    m_currentItemLayout = viewHolder.m_linear_item;
                    //找到具体元素
                    m_linearHidden = m_currentItemLayout.findViewById(R.id.linear_hidden);
                    //给隐藏部分(删除)注册点击事件
                    m_linearHidden.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if(m_rightListener != null)
                            {
                                m_rightListener.OnRightClick(m_currentSelectPosition, "");
                            }
                        }
                    });
                    //这里将删除按钮的宽度设为可以移动的距离
                    m_hiddenWidth = m_linearHidden.getWidth();
                }
                break;
            }
            //移动
            case MotionEvent.ACTION_MOVE:
            {
                //记录当前移动距离
                m_xMove = x;
                m_yMove = y;
                //为负时:手指向左滑动;为正时:手指向右滑动.这与Android的屏幕坐标定义有关
                int dx = m_xMove - m_xDown;
                int dy = m_yMove - m_yDown;
                //左滑
                if(dx < 0 && Math.abs(dx) > m_touchMin && Math.abs(dy) < m_touchMin)
                {
                    int newScrollX = Math.abs(dx);
                    //超过了,不能再移动了
                    if(m_moveWidth >= m_hiddenWidth)
                    {
                        newScrollX = 0;
                    }
                    //这次要超了
                    else if(m_moveWidth + newScrollX > m_hiddenWidth)
                    {
                        newScrollX = m_hiddenWidth - m_moveWidth;
                    }
                    //左滑,每次滑动手指移动的距离
                    ScrollLeft(m_currentItemLayout, newScrollX);
                    //对移动的距离叠加
                    m_moveWidth = m_moveWidth + newScrollX;
                }
                //右滑
                else if(dx > 0)
                {
                    //执行右滑,这里没有做跟随,瞬间恢复
                    ScrollRight(m_currentItemLayout, 0 - m_moveWidth);
                    m_moveWidth = 0;
                }
                break;
            }
            //抬起
            case MotionEvent.ACTION_UP:
            {
                int scrollX = m_currentItemLayout.getScrollX();
                //连续移动距离不超过隐藏部分长度时,判断是否显示
                if(m_hiddenWidth > m_moveWidth)
                {
                    int toX = (m_hiddenWidth - m_moveWidth);
                    //超过一半长度时松开,则自动滑到左侧
                    if(scrollX > m_hiddenWidth / 2)
                    {
                        ScrollLeft(m_currentItemLayout, toX);
                        m_moveWidth = m_hiddenWidth;
                    }
                    //不到一半时松开,则恢复原状
                    else
                    {
                        ScrollRight(m_currentItemLayout, 0 - m_moveWidth);
                        m_moveWidth = 0;
                    }
                }
                m_lastItemLayout = m_currentItemLayout;
                break;
            }
        }
        return super.onTouchEvent(e);
    }


    @Override
    public void computeScroll()
    {
        if(m_scroller.computeScrollOffset())
        {
            m_currentItemLayout.scrollBy(m_scroller.getCurrX(), 0);
            invalidate();
        }
    }

    /**
     * 向左滑动
     */
    private void ScrollLeft(View item, int scorllX)
    {
        item.scrollBy(scorllX, 0);
    }

    /**
     * 向右滑动
     */
    private void ScrollRight(View item, int scorllX)
    {
        item.scrollBy(scorllX, 0);
    }

    public interface OnRightClickListener
    {
        void OnRightClick(int position, String id);
    }
}