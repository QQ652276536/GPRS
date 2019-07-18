package com.zistone.blowdown_app.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration
{

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable m_divider;

    private int m_orientation;

    public DividerItemDecoration(Context context, int orientation)
    {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        m_divider = a.getDrawable(0);
        a.recycle();
        SetOrientation(orientation);
    }

    public void SetOrientation(int orientation)
    {
        if(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)
        {
            throw new IllegalArgumentException("invalid orientation");
        }
        m_orientation = orientation;
    }

    /**
     * 绘制竖线
     *
     * @param c
     * @param parent
     */
    public void DrawVertical(Canvas c, RecyclerView parent)
    {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++)
        {
            final View child = parent.getChildAt(i);
            RecyclerView v = new RecyclerView(parent.getContext());
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + m_divider.getIntrinsicHeight();
            m_divider.setBounds(left, top, right, bottom);
            m_divider.draw(c);
        }
    }

    /**
     * 绘制横线
     *
     * @param c
     * @param parent
     */
    public void DrawHorizontal(Canvas c, RecyclerView parent)
    {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++)
        {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + m_divider.getIntrinsicHeight();
            m_divider.setBounds(left, top, right, bottom);
            m_divider.draw(c);
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent)
    {
        if(m_orientation == VERTICAL_LIST)
        {
            DrawVertical(c, parent);
        }
        else
        {
            DrawHorizontal(c, parent);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
    {
        if(m_orientation == VERTICAL_LIST)
        {
            outRect.set(0, 0, 0, m_divider.getIntrinsicHeight());
        }
        else
        {
            outRect.set(0, 0, m_divider.getIntrinsicWidth(), 0);
        }
    }
}
