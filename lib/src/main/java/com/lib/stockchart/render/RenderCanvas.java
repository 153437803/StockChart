package com.lib.stockchart.render;

import android.graphics.Canvas;
import android.util.Log;

import com.lib.stockchart.draw.DrawKline;
import com.lib.stockchart.draw.DrawTline;
import com.lib.stockchart.draw.DrawTurnover;
import com.lib.stockchart.draw.IDraw;
import com.lib.stockchart.entry.Entry;
import com.lib.stockchart.entry.EntryManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * description: 当前类描述信息
 * created by kalu on 2017/11/11 15:59
 */
public class RenderCanvas {

    // 默认-1, 不显示高亮
    private float xHighligh = -1f;
    private float yHighligh = -1f;

    private float oldPriceMin, oldPriceMax, oldTurnoverMax;

    public float getxHighligh() {
        return xHighligh;
    }

    public void setxHighligh(float xHighligh) {
        this.xHighligh = xHighligh;
    }

    public float getyHighligh() {
        return yHighligh;
    }

    public void setyHighligh(float yHighligh) {
        this.yHighligh = yHighligh;
    }

    // 股票指标列表
    private final LinkedList<IDraw> mDrawList = new LinkedList<>();
    // 显示区域
    int left, top, right, bottom, width, height;

    public void onSizeChanged(int left, int top, int right, int bottom, float xlabelHeight, float boardPadding) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.width = right - left;
        this.height = bottom - top;
        // Log.e("RenderCanvas", "left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom + ", width = " + width + ", height = " + height);

        for (IDraw drawing : mDrawList) {
            drawing.onDrawInit(this.left, this.top, this.right, this.bottom, this.width, this.height, xlabelHeight, boardPadding);
        }
    }

    /**
     * 计算数据
     *
     * @param indexBegin   起始下标索引
     * @param indexEnd     结束下标索引
     * @param xlabelHeight x坐标轴显示信息区域高度
     * @param boardPadding 内边距
     */
    void calculateData(int indexBegin, int indexEnd, float xlabelHeight, float boardPadding) {

        // TODO: 2017/12/3  
        // todo计算效率, 有BUG当移动时是旧数据
        final float priceMin = EntryManager.getInstance().calculatePriceMin(indexBegin, indexEnd);
        final float priceMax = EntryManager.getInstance().calculatePriceMax(indexBegin, indexEnd);
        final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(indexBegin, indexEnd);
//        if (oldPriceMin == priceMin && oldPriceMax == priceMax && oldTurnoverMax == turnoverMax)
//            return;

        final int weightTop = EntryManager.getInstance().getWeightTop();
        final int weightDown = EntryManager.getInstance().getWeightDown();
        final int weightSum = weightTop + weightDown;

        // 内边框边距
        final float bottomSrc = height / weightSum * weightTop - boardPadding - xlabelHeight;
        final float heightSrc = bottomSrc - top - boardPadding;

        // 下部分, 成交量高度
        final float heightBottom = height / weightSum * weightDown - boardPadding;

        // 点与点间隙
        final float pointSpace = EntryManager.getInstance().getPointSpace();
        // 每个点宽度
        final float pointWidth = (width - 2 * boardPadding - (indexEnd - indexBegin) * pointSpace) / (indexEnd - indexBegin + 1);
        EntryManager.getInstance().setPointWidth((int) pointWidth);

        final List<Entry> entryList = EntryManager.getInstance().getEntryList();

        final float realBegin = left + boardPadding;
        final float realEnd = right - boardPadding - pointWidth;
        float priceSum = priceMax - priceMin;

        final float temp1 = heightSrc / priceSum;
        final float temp2 = priceMin * temp1;

        for (int i = indexBegin; i <= indexEnd; i++) {

            final Entry entry = entryList.get(i);

            // 1.计算真实X轴坐标
            if (i == indexBegin) {
                entry.setxLabelReal(realBegin);
            } else if (i == indexEnd) {
                entry.setxLabelReal(realEnd);
            } else {
                float temp = realBegin + (i - indexBegin) * (pointWidth + pointSpace);
                entry.setxLabelReal(temp);
            }

            // 价格
            entry.setOpenReal(bottomSrc - entry.getOpen() * temp1 + temp2);
            entry.setCloseReal(bottomSrc - entry.getClose() * temp1 + temp2);
            entry.setHighReal(bottomSrc - entry.getHigh() * temp1 + temp2);
            entry.setLowReal(bottomSrc - entry.getLow() * temp1 + temp2);
            entry.setMa5Real(bottomSrc - entry.getMa5() * temp1 + temp2);
            entry.setMa10Real(bottomSrc - entry.getMa10() * temp1 + temp2);
            entry.setMa20Real(bottomSrc - entry.getMa20() * temp1 + temp2);
            // 成交量
            entry.setVolumeReal(bottom - entry.getVolume() * heightBottom / turnoverMax);
            entry.setVolumeMa5Real(bottom - entry.getVolumeMa5() * heightBottom / turnoverMax);
            entry.setVolumeMa10Real(bottom - entry.getVolumeMa10() * heightBottom / turnoverMax);
        }
    }

    /**********************************************************************************************/

    /**
     * @param canvas
     * @param pointMax      最多显示个数
     * @param indexBegin    起始下标
     * @param indexEnd      结束下标
     * @param xoffsetLeft   左侧拖拽位移
     * @param xoffsetRight  右侧拖拽位移
     * @param loadingStr    加载信息
     * @param xlabelHeight  x坐标轴显示信息总高度
     * @param boardPadding  内边距
     */
    public void onCanvas(Canvas canvas, int pointMax, int indexBegin, int indexEnd, float xoffsetLeft, float xoffsetRight, String loadingStr, float xlabelHeight, float boardPadding) {
     //   final List<IDraw> drawList = getDrawList();

        if (indexEnd <= 0) {
          //  Log.e("RenderCanvas", "onCanvas ==> indexCountMax <= 0");
            for (IDraw drawing : mDrawList) {

                if (drawing instanceof DrawTurnover) {
                 //   Log.e("RenderCanvas", "onCanvas ==> onDrawNull - DrawTurnover");
                    drawing.onDrawNull(canvas, loadingStr, xlabelHeight, boardPadding);
                }

                if (drawing instanceof DrawTline && RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
                  //  Log.e("RenderCanvas", "onCanvas ==> onDrawNull - DrawTline");
                    drawing.onDrawNull(canvas, loadingStr, xlabelHeight, boardPadding);
                } else if (drawing instanceof DrawKline && RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {
                //    Log.e("RenderCanvas", "onCanvas ==> onDrawNull - DrawKline");
                    drawing.onDrawNull(canvas, loadingStr, xlabelHeight, boardPadding);
                }
            }
        } else {
           // Log.e("RenderCanvas", "onCanvas ==> indexCountMax > 0");

            if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_TLINE_TURNOVER) {
                final float minPrice = EntryManager.getInstance().calculatePriceMin(indexBegin, indexEnd);
                final float maxPrice = EntryManager.getInstance().calculatePriceMax(indexBegin, indexEnd);
                final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(indexBegin, indexEnd);
                calculateData(0, indexEnd, xlabelHeight, boardPadding);

                for (IDraw temp : mDrawList) {
                    if (temp instanceof DrawTurnover || temp instanceof DrawTline) {
                      //  Log.e("RenderCanvas", "onCanvas ==> onDrawData - Tline");
                        temp.onDrawData(this, canvas, indexEnd, 0, indexEnd, minPrice, maxPrice, turnoverMax, getxHighligh(), getyHighligh(), xoffsetLeft, xoffsetRight, xlabelHeight, boardPadding);
                    }
                }
            } else if (RenderManager.getInstance().getRenderModel() == RenderManager.MODEL_KLINE_TURNOVER) {

                calculateData(indexBegin, indexEnd, xlabelHeight, boardPadding);
                final float minPrice = EntryManager.getInstance().calculatePriceMin(indexBegin, indexEnd);
                final float maxPrice = EntryManager.getInstance().calculatePriceMax(indexBegin, indexEnd);
                final float turnoverMax = EntryManager.getInstance().calculateTurnoverMax(indexBegin, indexEnd);

                for (IDraw temp : mDrawList) {
                    if (temp instanceof DrawTurnover || temp instanceof DrawKline) {
                       // Log.e("RenderCanvas", "onCanvas ==> onDrawData - Kline");
                        temp.onDrawData(this, canvas, pointMax, indexBegin, indexEnd, minPrice, maxPrice, turnoverMax, getxHighligh(), getyHighligh(), xoffsetLeft, xoffsetRight, xlabelHeight, boardPadding);
                    }
                }
            }
        }
    }

    public void clearData() {
        mDrawList.clear();
    }

    public void addData() {
        if (mDrawList.size() != 0) return;
        mDrawList.add(new DrawTurnover());
        mDrawList.add(new DrawTline());
        mDrawList.add(new DrawKline());
    }
}