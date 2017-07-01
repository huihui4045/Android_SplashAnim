package com.huihui.splashanim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by gavin
 * Time 2017/6/30  15:36
 * Email:molu_clown@163.com
 */

public class SplashView extends View {

    private  ValueAnimator mAnimator;
    // 小圆圈的颜色列表，在initialize方法里面初始化
    private int[] mCircleColors;
    private Paint mPaint=new Paint();
    private Paint mPaintBackground=new Paint();

    // 大圆(里面包含很多小圆的)的半径
    private float mRotationRadius = 90;

    // 整体的背景颜色
    private int mSplashBgColor = Color.WHITE;

    //当前大圆旋转角度(弧度)
    private float mCurrentRotationAngle = 0F;



    // 大圆和小圆旋转的时间
    private long mRotationDuration = 1200; //ms

    //空心圆初始半径
    private float mHoleRadius = 0F;


    //屏幕对角线一半
    private float mDiagonalDist;

    // 屏幕正中心点坐标
    private float mCenterX;
    private float mCenterY;


    // 每一个小圆的半径
    private float mCircleRadius = 18;

    //当前大圆的半径
    private float mCurrentRotationRadius = mRotationRadius;


    public SplashView(Context context) {
        super(context);

        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w/2f;
        mCenterY = h/2f;
        mDiagonalDist = (float) Math.sqrt((w*w+h*h))/2f;//勾股定律
    }

    private void initView(Context context) {

        mCircleColors = context.getResources().getIntArray(R.array.splash_circle_colors);
        //画笔初始化
        //消除锯齿
        mPaint.setAntiAlias(true);
        mPaintBackground.setAntiAlias(true);
        //设置样式---边框样式--描边
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setColor(mSplashBgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mState==null){
          //开启第一个旋转动画
            mState=new RotateState();
        }

        mState.drawState(canvas);

    }

    /****
     * 开启聚合动画
     */
    public void splashDisappear() {

        if (mState!=null && mState instanceof RotateState){

            ((RotateState) mState).cancel();

            post(new Runnable() {
                @Override
                public void run() {

                    mState=new MergingState();
                }
            });
        }
    }

    /*****
     * 旋转动画
     */
    private class RotateState extends SplashState{

        public RotateState() {

            //1.动画的初始工作；2.开启动画
            //花1200ms，计算某个时刻当前的角度是多少？ 0~2π
            mAnimator = ValueAnimator.ofFloat(0f,(float)Math.PI*2);

            mAnimator.setInterpolator(new LinearInterpolator());

            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentRotationAngle= (float) animation.getAnimatedValue();

                    invalidate();
                }
            });

            mAnimator.setDuration(mRotationDuration);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.start();
        }

        public void cancel(){
            mAnimator.cancel();
        }

        @Override
        public void drawState(Canvas canvas) {

//1.背景--擦黑板，涂成白色
            drawBackground(canvas);
            //2.绘制小圆

            drawCircles(canvas);
        }
    }


    /*****
     * 聚合动画
     */
    private class MergingState extends SplashState{

        public MergingState() {
            //
            mAnimator=ValueAnimator.ofFloat(mRotationRadius,0);

            mAnimator.setDuration(mRotationDuration);
            mAnimator.setInterpolator(new OvershootInterpolator(20f));
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    mCurrentRotationRadius= (float) animation.getAnimatedValue();

                    invalidate();
                }
            });

            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    mState=new ExpandState();
                }
            });


            mAnimator.start();
        }

        @Override
        public void drawState(Canvas canvas) {

            drawBackground(canvas);

            drawCircles(canvas);


            //mAnimator.reverse();
        }
    }

    /****
     * 扩散动画
     * 画一个空心圆  设置画笔粗细
     * 空心圆变化范围
     */
    private class ExpandState extends SplashState {

        public ExpandState() {
            //计算某个时刻大圆的半径是多少
            mAnimator=ValueAnimator.ofFloat(0,mDiagonalDist);
            mAnimator.setDuration(mRotationDuration);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //当前空心圆的半径
                mHoleRadius= (float) animation.getAnimatedValue();

                    invalidate();
                }
            });

            mAnimator.start();
        }

        @Override
        public void drawState(Canvas canvas) {

            drawBackground(canvas);
        }
    }



    private void drawCircles(Canvas canvas) {

        //每个小圆之间的间隔角度 = 2π/小圆的个数
        float rotationAngle = (float) (2*Math.PI/mCircleColors.length);

        for (int i=0; i < mCircleColors.length; i++){
            /**
             * x = r*cos(a) +centerX
             y=  r*sin(a) + centerY
             每个小圆i*间隔角度 + 旋转的角度 = 当前小圆的真是角度
             */
            double angle = i*rotationAngle + mCurrentRotationAngle;
            float cx = (float) (mCurrentRotationRadius*Math.cos(angle) + mCenterX);
            float cy = (float) (mCurrentRotationRadius*Math.sin(angle) + mCenterY);
            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cx,cy,mCircleRadius,mPaint);
        }
    }

    private void drawBackground(Canvas canvas) {


        if(mHoleRadius>0f){//空心圆初始半径
            //得到画笔的宽度 = 对角线/2 - 空心圆的半径
            float strokeWidth = mDiagonalDist - mHoleRadius;
            mPaintBackground.setStrokeWidth(strokeWidth);
            //画圆的半径 = 空心圆的半径 + 画笔的宽度/2
            float radius = mHoleRadius + strokeWidth/2;
            canvas.drawCircle(mCenterX,mCenterY,radius,mPaintBackground);
        }else {

            canvas.drawColor(mSplashBgColor);
        }
    }

    private SplashState mState = null;

    //策略模式:State---三种动画状态
    private abstract  class SplashState{
        public abstract  void drawState(Canvas canvas);
    }



}
