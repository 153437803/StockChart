package com.lib.stockchart.render;

/**
 * description: 模式管理
 * created by kalu on 2017/11/12 3:47
 */
public class RenderManager {

    // K线图+成交量
    public static int MODEL_KLINE_TURNOVER = 1;
    // 分时图+成交量
    public static int MODEL_TLINE_TURNOVER = 2;
    // K线图+MACD+RSI+KDJ+BOLL
    public static int MODEL_KLLINE_MACD_RSI_KDJ_BOLL = 3;

    // 当前模式
    private int mRenderModel = MODEL_KLINE_TURNOVER;
    // K线
    private KlineRender mKlineRender;
    // 分时
    private TlineRender mTlineRender;

    private RenderManager() {
        mKlineRender = new KlineRender();
        mTlineRender = new TlineRender();
    }

    public static RenderManager getInstance() {
        return RenderManager.SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static RenderManager instance = new RenderManager();
    }

    /*********************************************************************************************/

    public KlineRender getKlineRender() {
        return mKlineRender;
    }

    public TlineRender getTlineRender() {
        return mTlineRender;
    }

    public int getRenderModel() {
        return mRenderModel;
    }

    public void setRenderModel(int mRenderModel) {
        this.mRenderModel = mRenderModel;
    }
}
