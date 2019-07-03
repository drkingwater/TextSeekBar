package com.pxq.myapplication.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pxq.myapplication.R;


public class TextSeekBar extends View {

    private static final String TAG = "TextSeekBar";

    private static final int MAX_LEVEL = 10000;
    private static final int DEF_TEXT_SIZE = 16;
    private static final int DEF_MAX_PROGRESS = 100;

    private Paint mTextPaint, mThumbPaint;
    private int mWidth, mHeight;

    private int mProgress = 0, mSecondaryProgress = 0;
    private int mMaxProgress;

    private boolean mIsDragging;
    //背景图
    private Drawable mBackgroundDrawable;
    private float mBackgroundHeight;
    private float mBackgroundWidth;
    private float mBackgroundLeft, mBackgroundTop, mBackgroundRight, mBackgroundBottom;
    //progress
    private ClipDrawable mProgressDrawable;
    //secondDrawable
    private ClipDrawable mSecondaryDrawable;

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    //thumb
    private Drawable mThumb;
    private int mThumbLeft, mThumbTop, mThumbBottom, mThumbRight;
    private int mThumbWith = 0, mThumbHeight = 0;
    //文字
    private Drawable mTextBgDrawable;
    private int mTextBgWidth = 0, mTextBgHeight = 0;
    private int mTextColor;
    private float mTextSize = DEF_TEXT_SIZE;
    private int mTextLength = 0;
    private int mTextWidth, mTextHeight;
    private int mTextPaddingTop;
    private String mText = "";

    //padding
    private float mPaddingLeft;

    public TextSeekBar(Context context) {
        super(context);
        init();
    }

    public TextSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextSeekBar);
            if (typedArray.hasValue(R.styleable.TextSeekBar_thumb)) {
                mThumb = typedArray.getDrawable(R.styleable.TextSeekBar_thumb);
                if (mThumb != null) {
                    mThumbWith = mThumb.getIntrinsicWidth();
                    mThumbHeight = mThumb.getIntrinsicHeight();
                }
            }
            mThumbWith = (int) typedArray.getDimension(R.styleable.TextSeekBar_thumbWidth, mThumbWith);
            mThumbHeight = (int) typedArray.getDimension(R.styleable.TextSeekBar_thumbHeight, mThumbHeight);
            //progress
            if (typedArray.hasValue(R.styleable.TextSeekBar_progressDrawable)) {
                Drawable progressLayer = typedArray.getDrawable(R.styleable.TextSeekBar_progressDrawable);
                if (progressLayer instanceof LayerDrawable) {
                    //取背景图
                    mBackgroundDrawable = ((LayerDrawable) progressLayer).findDrawableByLayerId(android.R.id.background);
                    //取secondProgress
                    Drawable secondDrawable = ((LayerDrawable) progressLayer).findDrawableByLayerId(android.R.id.secondaryProgress);
                    if (secondDrawable instanceof ClipDrawable) {
                        mSecondaryDrawable = (ClipDrawable) secondDrawable;
                    }
                    //取progress图
                    Drawable progressDrawable = ((LayerDrawable) progressLayer).findDrawableByLayerId(android.R.id.progress);
                    if (progressDrawable instanceof ClipDrawable) {
                        mProgressDrawable = (ClipDrawable) progressDrawable;
                        Log.d(TAG, "TextSeekBar: mProgressDrawable instanceof Clip");
                    }
                }
            }
            //text
            mTextSize = typedArray.getDimension(R.styleable.TextSeekBar_textSize, DEF_TEXT_SIZE);
            mTextPaddingTop = (int) typedArray.getDimension(R.styleable.TextSeekBar_textPaddingTop, 0);
            mTextColor = typedArray.getColor(R.styleable.TextSeekBar_textColor, Color.BLACK);
            //text bg
            if (typedArray.hasValue(R.styleable.TextSeekBar_textBackground)) {
                mTextBgDrawable = typedArray.getDrawable(R.styleable.TextSeekBar_textBackground);
                if (mTextBgDrawable != null) {
                    mTextBgHeight = mTextBgDrawable.getIntrinsicHeight();
                    mTextBgWidth = mTextBgDrawable.getIntrinsicWidth();
                }
            }
            mTextBgWidth = (int) typedArray.getDimension(R.styleable.TextSeekBar_textBgWidth, mTextBgWidth);
            mTextBgHeight = (int) typedArray.getDimension(R.styleable.TextSeekBar_textBgHeight, mTextBgHeight);

            typedArray.recycle();
            Log.d(TAG, "TextSeekBar: mTextBgDrawable " + (mTextBgDrawable == null));
            Log.d(TAG, "TextSeekBar: " + mThumbWith + " " + mThumbHeight + " textPaddingTop " + mTextPaddingTop + " textSize" + mTextSize);
        }
        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setColor(Color.BLACK);

        mMaxProgress = DEF_MAX_PROGRESS;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        Log.d(TAG, "onMeasure:  " + width + " h " + height);
        //强制seekBar背景高度不能大于Thumb高度
        mBackgroundHeight = mThumb == null ? height : Math.min(height, mThumbHeight);

        //重设布局宽高
        height = Math.max(height + mTextBgHeight, mThumbHeight + mTextBgHeight);
        width = Math.max(width + mThumbWith, width + mTextBgWidth);

        Log.d(TAG, "onMeasure: width " + width + " height " + height + " backHeight " + mBackgroundHeight);
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        //计算背景宽度
        mBackgroundWidth = Math.min(mWidth - mThumbWith, mWidth - mTextBgWidth);
        //计算背景margin
        mBackgroundLeft = Math.max(mThumbWith / 2, mTextBgWidth / 2);
        mBackgroundRight = mBackgroundLeft + mBackgroundWidth;
        mBackgroundTop = (mThumbHeight - mBackgroundHeight) / 2 + mTextBgHeight;
        mBackgroundBottom = mBackgroundTop + mBackgroundHeight;
        //mThumbLeft, mThumbTop, mThumbBottom, mThumbRight
        mThumbLeft = Math.max(mTextBgWidth / 2 - mThumbWith / 2, 0);
        mThumbTop = mTextBgHeight;
        mThumbRight = mThumbLeft + mThumbWith;
        mThumbBottom = mThumbTop + mThumbHeight;

        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds((int) mBackgroundLeft, (int) mBackgroundTop, (int) mBackgroundRight, (int) mBackgroundBottom);
        }
        if (mProgressDrawable != null) {
            mProgressDrawable.setBounds((int) mBackgroundLeft, (int) mBackgroundTop, (int) mBackgroundRight, (int) mBackgroundBottom);
        }
        if (mSecondaryDrawable != null) {
            mSecondaryDrawable.setBounds((int) mBackgroundLeft, (int) mBackgroundTop, (int) mBackgroundRight, (int) mBackgroundBottom);
        }

        Log.d(TAG, "onSizeChanged: " + mWidth + " height " + mHeight + " mBackHeight " + mBackgroundHeight + " mTop " + mBackgroundTop + " thumbWidth " + mThumbWith + " " + mBackgroundLeft + " " + mBackgroundBottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawRect(0, 0, mWidth, mHeight, mThumbPaint);
        //绘制文本进度
        drawText(canvas);
        //绘制背景图
        drawBackground(canvas);
        //绘制progress
        drawSecondary(canvas);
        drawProgress(canvas);
        //绘制thumb
        drawThumb(canvas);
    }

    private void drawText(Canvas canvas) {
        if (mTextBgDrawable != null) {
            mTextBgDrawable.setBounds((int) (getFraction(mProgress) * mBackgroundWidth), 0, (int) (getFraction(mProgress) * mBackgroundWidth + mTextBgWidth), mTextBgHeight);
            mTextBgDrawable.draw(canvas);
            if (!TextUtils.isEmpty(mText)) {
                //重新计算文字宽度
                if (mText.length() != mTextLength) {
                    mTextLength = mText.length();
                    mTextWidth = getTextWidth(mText, mTextPaint);
                    mTextHeight = getTextHeight(mText, mTextPaint);
                }
                canvas.drawText(mText, (int) (getFraction(mProgress) * mBackgroundWidth) + mBackgroundLeft - mTextWidth / 2, (mTextBgHeight - mTextHeight) / 2 + mTextPaddingTop, mTextPaint);

            }
        }
    }

    private void drawBackground(Canvas canvas) {
        //secondaryDrawable完全覆盖background则不绘制背景
        if (mSecondaryProgress == mMaxProgress && mSecondaryDrawable != null) {
            return;
        }
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.draw(canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        if (mProgressDrawable != null) {
            mProgressDrawable.setLevel((int) (getFraction(mProgress) * MAX_LEVEL));
            mProgressDrawable.draw(canvas);
        }
    }

    private void drawSecondary(Canvas canvas) {
        if (mSecondaryDrawable != null) {
            mSecondaryDrawable.setLevel((int) (getFraction(mSecondaryProgress) * MAX_LEVEL));
            mSecondaryDrawable.draw(canvas);
        }
    }

    private void drawThumb(Canvas canvas) {
        if (mThumb != null) {
            mThumb.setBounds((int) (getFraction(mProgress) * mBackgroundWidth) + mThumbLeft, mThumbTop, (int) (getFraction(mProgress) * mBackgroundWidth + mThumbRight), mThumbBottom);
            mThumb.draw(canvas);
        } else {
//                        canvas.drawCircle(getFraction(mProgress) * mBackgroundWidth, mHeight / 2, mThumbWith, mThumbPaint);
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    /**
     * 获取进度百分比
     *
     * @param progress
     * @return
     */
    private float getFraction(int progress) {
        return (float) (progress * 1.0 / mMaxProgress);
    }

    public void setProgress(int progress) {
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        invalidate();
        onProgressRefresh();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (mSecondaryProgress == secondaryProgress) {
            return;
        }
        mSecondaryProgress = secondaryProgress;
        invalidate();
    }

    /**
     * 设置文字
     *
     * @param text
     */
    public void setText(String text) {
        mText = text;
    }

    /**
     * 设置文字颜色
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public void setMax(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    /**
     * 设置Thumb
     *
     * @param thumb
     */
    public void setThumbImage(Drawable thumb) {
        mThumb = thumb;
    }

    /**
     * 设置Thumb宽度
     *
     * @param thumbWith
     */
    public void setThumbWith(int thumbWith) {
        mThumbWith = thumbWith;
    }

    /**
     * 设置Thumb高度
     *
     * @param thumbHeight
     */
    public void setThumbHeight(int thumbHeight) {
        mThumbHeight = thumbHeight;
    }

    public void setTextBgDrawable(Drawable textBgDrawable) {
        mTextBgDrawable = textBgDrawable;
    }

    public void setTextBgWidth(int textBgWidth) {
        mTextBgWidth = textBgWidth;
    }

    public void setTextBgHeight(int textBgHeight) {
        mTextBgHeight = textBgHeight;
    }

    public void setTextPaddingTop(int textPaddingTop) {
        mTextPaddingTop = textPaddingTop;
    }

    public int getMax() {
        return mMaxProgress;
    }

    private int getTextWidth(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    private int getTextHeight(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private void onProgressRefresh() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, mProgress, mIsDragging);
        }
    }

    /**
     * 判断落点是否在thumb范围内
     *
     * @param event
     * @return
     */
    private boolean inThumbBound(MotionEvent event) {
        //(当前进度+bgLeft) +- thumbWidth / 2
        float center = mBackgroundLeft + getFraction(mProgress) * mBackgroundWidth;
        float x = event.getX();
        return x >= center - mThumbWith / 2 && x <= center + mThumbWith / 2;
    }

    /**
     * 判断滑动是否在SeekBar范围内
     *
     * @param event
     * @return
     */
    private boolean checkProgressBound(MotionEvent event) {
        float x = event.getX();
        return x >= (mBackgroundLeft - mThumbLeft) && x <= mBackgroundLeft + mBackgroundWidth + mThumbRight;
    }

    private int getProgress(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float availableWidth = mBackgroundWidth;
        float fraction = (x - mBackgroundLeft) / availableWidth;
        fraction = Math.max(0, fraction);
        fraction = Math.min(fraction, 1);
        return (int) (fraction * getMax());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (inThumbBound(event)) {
                    mIsDragging = true;
                    if (mOnSeekBarChangeListener != null) {
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsDragging && checkProgressBound(event)) {
                    setProgress(getProgress(event));
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mOnSeekBarChangeListener != null && mIsDragging) {
                    mOnSeekBarChangeListener.onStopTrackingTouch(this);
                }
                mIsDragging = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    public interface OnSeekBarChangeListener {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(TextSeekBar seekBar, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStartTrackingTouch(TextSeekBar seekBar);

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStopTrackingTouch(TextSeekBar seekBar);
    }

}
