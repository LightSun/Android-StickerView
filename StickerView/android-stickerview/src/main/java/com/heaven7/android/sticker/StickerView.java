package com.heaven7.android.sticker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * the sticker view
 *  .width and height should be match_parent, match_parent.
 * @author heaven7
 */
public class StickerView extends View {

    private static final String TAG = "StickerView";
    //drag directions
    private static final int DRAG_DIRECTION_LEFT_TOP     = 1;
    private static final int DRAG_DIRECTION_LEFT_BOTTOM  = 2;
    private static final int DRAG_DIRECTION_RIGHT_BOTTOM = 3;
    private static final int DRAG_DIRECTION_RIGHT_TOP    = 4;

    private static final int FIX_WIDTH  = 1;
    private static final int FIX_HEIGHT = 2;

    private Params mParams = new Params();
    private final Rect mRect = new Rect();
    private final RectF mRectF = new RectF();

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTextArea = new RectF();

    private final GestureDetectorFactory.Delegate mGestureDetector;

    private PathEffect mEffect;
    private Bitmap mSticker;
    private Callback mCallback;

    private List<Decoration> mDecorations;
    private float mRotateDegree; //in clockwise

    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context,  AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StickerView);
        try {
            mParams.init(ta);
        }finally {
            ta.recycle();
        }
        mGestureDetector = GestureDetectorFactory.getDelegate(context, new Gesture0());
    }

    /**
     * get the result bitmap
     * @return the bitmap
     * @since 1.0.6
     */
    public Bitmap getResultBitmap(){
        final Rect mRect = new Rect();
        final RectF mRectF = new RectF();

        mRect.set(0, 0, mSticker.getWidth(), mSticker.getHeight());
        mRectF.set(0, 0, getStickerWidth(), getStickerHeight());

        Bitmap bg = Bitmap.createBitmap(getStickerWidth(), getStickerHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        canvas.drawBitmap(mSticker, mRect, mRectF, null);
        return bg;
    }

    /**
     * after you change some params from {@linkplain #getParams()}. you may should call this.
     * @param resetWH true to reset sticker width and height
     */
    public void reset(boolean resetWH){
        if(resetWH){
            mParams.resetStickerWidthHeight();
        }
        mRotateDegree = 0;
        mEffect = new DashPathEffect(new float[]{mParams.linePathInterval, mParams.linePathInterval}, mParams.linePathPhase);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                adjustMargin();
                return true;
            }
        });
    }
    /**
     * set the callback
     * @param cb the Callback
     */
    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    /**
     * add decoration
     * @param decoration the decoration
     */
    public void addDecoration(Decoration decoration){
        if(mDecorations == null){
            mDecorations = new ArrayList<>(5);
        }
        mDecorations.add(decoration);
    }
    /**
     * get the params
     * @return the params
     */
    public Params getParams(){
        return mParams;
    }

    /*
     * call this that if you confirm changed some of params.
     */
    /*public void applyParams(){

    }*/
    /**
     * get the margin start
     * @return the margin start
     */
    public int getMarginStart() {
        return mParams.marginStart;
    }
    /**
     * set margin start
     * @param mMarginStart the margin start.
     */
    public void setMarginStart(int mMarginStart) {
        mParams.marginStart = mMarginStart;
        invalidate();
    }
    /**
     * get margin top
     * @return the margin top
     */
    public int getMarginTop() {
        return mParams.marginTop;
    }
    /**
     * set margin top for content
     * @param mMarginTop the margin top
     */
    public void setMarginTop(int mMarginTop) {
        mParams.marginTop = mMarginTop;
        invalidate();
    }
    /**
     * get the sticker
     * @return the sticker
     */
    public Bitmap getSticker(){
        return mSticker;
    }
    /**
     * set sticker by drawable id
     * @param drawableId the drawable id
     */
    public void setSticker(int drawableId){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        if(bitmap == null){
            throw new IllegalArgumentException();
        }
        setSticker(bitmap);
    }
    /**
     * set sticker bitmap
     * @param bitmap the sticker
     */
    public void setSticker(Bitmap bitmap){
        mSticker = bitmap;
        mParams.setStickerWidthHeight(bitmap, true);
        //zoom-eq. we need reset sticker width and height. if not fix width and height
        if(mParams.fixOrientation == 0){
            fitZoomEqual(false);
        }
        reset(false);
        invalidate();
    }

    /**
     * rotate 90 degree for the sticker as clockwise.
     * @param degree the rotate degree.
     * @throws UnsupportedOperationException if degree is not times of 90.
     */
    public void rotateSticker(float degree){
        if(degree % 90 != 0){
            throw new UnsupportedOperationException("currently only support rotate 90 degree.");
        }
        if(mSticker != null){
            rotateStickerInternal(degree);
            invalidate();
        }
    }
    /**
     * get the sticker rotate
     * @return the sticker rotate
     */
    public float getStickerRotate(){
        return mRotateDegree;
    }
    //-------------------------------------------------------------

    /**
     * get the sticker width . this should call after {@linkplain #setSticker(Bitmap)}.
     * @return the sticker width
     */
    public int getStickerWidth(){
        return mParams.stickerWidth <=0 ? mSticker.getWidth() : mParams.stickerWidth;
    }
    /**
     * get the sticker height . this should call after {@linkplain #setSticker(Bitmap)}.
     * @return the sticker height
     */
    public int getStickerHeight(){
        return mParams.stickerHeight <=0 ? mSticker.getHeight() : mParams.stickerHeight;
    }

    /**
     * get the scale x for sticker
     * @return the sticker scale x
     */
    public float getStickerScaleX(){
        return mParams.stickerWidth * 1f / mParams.rawStickerWidth;
    }
    /**
     * get the scale y for sticker
     * @return the sticker scale y
     */
    public float getStickerScaleY(){
        return mParams.stickerHeight * 1f / mParams.rawStickerHeight;
    }
    //-------------------------------------------------------------
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mParams);
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mParams = ss.params;
        reset(false);
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);
        if(event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            onTouchRelease();
        }
        return result || super.onTouchEvent(event);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if(mSticker == null){
            return;
        }
        int paddingStart = getPaddingStart();
        int paddingTop = getPaddingTop();
        //int paddingEnd = getPaddingEnd();
        //int paddingBottom = getPaddingBottom();
        final int stickerWidth = getStickerWidth();
        final int stickerHeight = getStickerHeight();

        canvas.save();
        canvas.translate(paddingStart + mParams.marginStart, paddingTop + mParams.marginTop);
        //draw sticker
        mRect.set(0, 0, mSticker.getWidth(), mSticker.getHeight());
        mRectF.set(0, 0, stickerWidth, stickerHeight);
        canvas.drawBitmap(mSticker, mRect, mRectF, null);
        //line range
        mLinePaint.setPathEffect(mEffect);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setColor(mParams.lineColor);
        canvas.drawLine(0, 0, stickerWidth, 0, mLinePaint);
        canvas.drawLine(stickerWidth, 0, stickerWidth, stickerHeight, mLinePaint);
        canvas.drawLine(0, stickerHeight, stickerWidth, stickerHeight, mLinePaint);
        canvas.drawLine(0, 0, 0 ,stickerHeight, mLinePaint);
        //four dot
        //(0, 0), (stickerWidth, 0), (stickerWidth, stickerHeight), (0, stickerHeight)
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setPathEffect(null);
        mLinePaint.setColor(mParams.dotColor);
        canvas.drawCircle(0, 0, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(stickerWidth, 0, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(stickerWidth, stickerHeight, mParams.dotRadius, mLinePaint);
        canvas.drawCircle(0, stickerHeight, mParams.dotRadius, mLinePaint);
        //---------- texts' --------------
        if(mParams.textEnabled && mParams.text != null){
            //text prepare
            measureText0();
            final int textWidth = mRect.width();
            final int textHeight = mRect.height();
            int left = stickerWidth + mParams.textMarginStart;
            int top = (stickerHeight - textHeight - mParams.textPaddingTop - mParams.textPaddingBottom) / 2;
            int right = left + textWidth + mParams.textPaddingStart + mParams.textPaddingEnd;
            int bottom = top + textHeight + mParams.textPaddingTop + mParams.textPaddingBottom;
            //text bg
            mTextPaint.setStyle(Paint.Style.FILL);
            mTextPaint.setColor(mParams.textBgColor);
            mRectF.set(left, top, right, bottom);
            mTextArea.set(mRectF);
            canvas.drawRoundRect(mRectF, mParams.textBgRoundSize, mParams.textBgRoundSize, mTextPaint);
            //text
            mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint.setColor(mParams.textColor);

            mRect.set(left + mParams.textPaddingStart,
                    top + mParams.textPaddingTop,
                    left + mParams.textPaddingStart + textWidth,
                    top + mParams.textPaddingTop + textHeight);
            Utils.computeTextDrawingCoordinate(mParams.text, mTextPaint, mRect, mRectF);
            canvas.drawText(mParams.text, mRectF.left, mRectF.top - mTextPaint.ascent(), mTextPaint);
        }
        if(mDecorations != null){
            for (Decoration decoration :mDecorations){
                decoration.onDraw(this, stickerWidth, stickerHeight);
            }
        }
        canvas.restore();
    }

    private void fitZoomEqual(boolean minOrMax){
        if(mParams.proportionalZoom && mParams.stickerWidth > 0 && mParams.stickerHeight > 0){
            float scaleX = mParams.stickerWidth * 1f / mSticker.getWidth();
            float scaleY = mParams.stickerHeight * 1f / mSticker.getHeight();
            float scale = minOrMax ? Math.min(scaleX, scaleY) : Math.max(scaleX, scaleY);
            mParams.stickerWidth = (int) (mSticker.getWidth() * scale);
            mParams.stickerHeight = (int) (mSticker.getHeight() * scale);
        }
    }
    protected void onTouchRelease() {
       // Logger.d(TAG, "onTouchRelease");
    }

    /**
     * indicate the target x, y is in target rect or not .you should note the growX and growY.
     *
     * @param target                  the target rect
     * @param x                       the x position
     * @param y                       the y position
     * @param touchPadding            the touch padding as slop
     * @return true if contains in rect.
     */
    private boolean containsInRect(RectF target, float x, float y, int touchPadding) {
        mRectF.set(target);
        mRectF.offset(getPaddingStart() + mParams.marginStart, getPaddingTop() + mParams.marginTop);
        mRectF.set(mRectF.left - touchPadding,
                mRectF.top - touchPadding,
                mRectF.right + touchPadding,
                mRectF.bottom + touchPadding);
        return mRectF.contains(x, y);
    }
    private int getDragDirection(MotionEvent e){
        if(containsXY(0, 0, e)){
            return DRAG_DIRECTION_LEFT_TOP;
        }
        final int stickerWidth = getStickerWidth();
        final int stickerHeight = getStickerHeight();
        if(containsXY(stickerWidth, 0, e)){
            return DRAG_DIRECTION_RIGHT_TOP;
        }
        if(containsXY(stickerWidth, stickerHeight, e)){
            return DRAG_DIRECTION_RIGHT_BOTTOM;
        }
        if(containsXY(0, stickerHeight, e)){
            return DRAG_DIRECTION_LEFT_BOTTOM;
        }
        return -1;
    }
    private boolean containsXY(int expectX, int expectY, MotionEvent e){
        mRectF.set(expectX - mParams.dotRadius,
                expectY - mParams.dotRadius,
                expectX + mParams.dotRadius,
                expectY + mParams.dotRadius
        );
        return containsInRect(mRectF, e.getX(), e.getY(), mParams.touchPadding * 2);
    }
    private void measureText0(){
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextSize(mParams.textSize);
        mTextPaint.getTextBounds(mParams.text, 0, mParams.text.length(), mRect);
    }

    /**
     * get the whole content width
     * @return the content width
     */
    public int getContentWidth(){
        final int stickerWidth = getStickerWidth();
        if(!mParams.textEnabled){
            return stickerWidth;
        }
        measureText0();
        final int textWidth = mRect.width();
        return stickerWidth + mParams.textMarginStart + textWidth + mParams.textPaddingStart + mParams.textPaddingEnd;
    }
    private void adjustMargin(){
        if(mParams.stickerInCenter){
            mParams.marginStart = (getWidth() - getPaddingStart() - getPaddingEnd() - mParams.stickerWidth ) / 2;
            mParams.marginTop = (getHeight() - getPaddingTop() - getPaddingBottom() - mParams.stickerHeight ) / 2;
        }else {
            //getStickerHeight as content height
            if(mParams.marginStart < 0){
                mParams.marginStart = getWidth() - getContentWidth() - getPaddingStart()
                        - getPaddingEnd() - Math.abs(mParams.marginStart);
            }
            if(mParams.marginTop < 0){
                mParams.marginTop = getHeight() - getStickerHeight() - getPaddingTop()
                        - getPaddingBottom() - Math.abs(mParams.marginTop);
            }
        }
    }
    private boolean reachScaleBound(){
        float sw = mParams.stickerWidth * 1f / mParams.rawStickerWidth;
        float sh = mParams.stickerHeight * 1f / mParams.rawStickerHeight;
        if(sw < mParams.minScale || sh < mParams.minScale){
            return true;
        }
        if(sw > mParams.maxScale || sh > mParams.maxScale){
            return true;
        }
        return false;
    }
    private void setStickerInternal(Bitmap bitmap){
        mSticker = bitmap;
        mParams.setStickerWidthHeight(bitmap, false);
        //zoom-eq. we need reset sticker width and height.
        fitZoomEqual(false);
    }
    //TODO currently, only support 90*n
    private void rotateStickerInternal(float degree){
        degree = degree % 360;
        mRotateDegree = (mRotateDegree + degree) % 360;
        if(degree == 90 || degree == 270){
            mParams.swapStickerWidthHeight();
        }
        setStickerInternal(Utils.rotate(mSticker, degree)); //Utils.rotate: only work 90.
    }
    public static class SavedState extends BaseSavedState{

        final Params params;

        public SavedState(Parcelable superState, Params params) {
            super(superState);
            this.params = params;
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            params.writeToParcel(out, flags);
        }
        private SavedState(Parcel in) {
            super(in);
            params = new Params(in);
        }

        @Override
        public String toString() {
            String str = "StickerView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + params.toString();
            return str + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
    private class Gesture0 implements GestureDetector.OnGestureListener{
        private int mDragDirection;
        private int mTmpStickerWidth;
        private int mTmpStickerHeight;

        private int mTmpMarginTop;
        private int mTmpMarginStart;
        private Decoration mTouchDecoration;

        private void moveInternal(int dx, int dy, boolean dragScale){
            mParams.marginStart = mTmpMarginStart + dx;
            mParams.marginTop = mTmpMarginTop + dy;
            handleReachEdge(dragScale);
            invalidate();
        }
        //out of screen not permit
        private void handleReachEdge(boolean scale){
            boolean shouldFitZoom = false;
            if(mParams.marginStart < 0){
                mParams.marginStart = 0;
            }else {
                int val = mParams.marginStart + getPaddingStart() + getPaddingEnd() + mParams.stickerWidth;
                if(val > getWidth()){
                    if(scale){
                        mParams.stickerWidth -= val - getWidth();
                        shouldFitZoom = true;
                    }else {
                        mParams.marginStart -= val - getWidth();
                    }
                }
            }
            if(mParams.marginTop < 0){
                mParams.marginTop = 0;
            }else {
                int val = mParams.marginTop + getPaddingTop() + getPaddingBottom() + mParams.stickerHeight;
                if(val > getHeight()){
                    if(scale){
                        mParams.stickerHeight -= val - getHeight();
                        shouldFitZoom = true;
                    }else {
                        mParams.marginTop -= val - getHeight();
                    }
                }
            }
            if(shouldFitZoom){
                fitZoomEqual(true);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mTouchDecoration = null;
            mDragDirection = getDragDirection(e);
            if(mDragDirection > 0){
                mTmpStickerWidth = mParams.stickerWidth > 0 ? mParams.stickerWidth : mSticker.getWidth();
                mTmpStickerHeight = mParams.stickerHeight > 0 ? mParams.stickerHeight : mSticker.getHeight();
            }else {
                mRectF.set(0, 0, getContentWidth(), getStickerHeight());
                boolean shouldHandle = false;
                //may be drag . so need * 2
                if(containsInRect(mRectF, e.getX(), e.getY(), mParams.touchPadding * 2)){
                    shouldHandle = true;
                }else if(mDecorations != null){
                    for (Decoration decor: mDecorations){
                        mRect.set(0, 0,0 ,0);
                        decor.getRangeRect(mRect);
                        //if valid
                        if(mRect.right > mRect.left && mRect.bottom > mRect.top){
                            mRectF.set(mRect);
                            if(containsInRect(mRectF, e.getX(), e.getY(), mParams.touchPadding)){
                                mTouchDecoration = decor;
                                shouldHandle = true;
                                break;
                            }
                        }
                    }
                }
                //out of range .ignore
                if(!shouldHandle){
                    return false;
                }
            }
            mTmpMarginStart = mParams.marginStart;
            mTmpMarginTop = mParams.marginTop;
            return true;
        }
        @Override
        public void onShowPress(MotionEvent e) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mRectF.set(0, 0, getStickerWidth(), getStickerHeight());
            if(containsInRect(mRectF, e.getX(), e.getY(), mParams.touchPadding)){
                if(mCallback != null){
                    mCallback.onClickSticker(StickerView.this);
                    return true;
                }
            }
            if(mParams.textEnabled && containsInRect(mTextArea, e.getX(), e.getY(), mParams.touchPadding)){
                if(mCallback != null){
                    mCallback.onClickTextArea(StickerView.this);
                    return true;
                }
            }
            if(mTouchDecoration != null){
                mTouchDecoration.onClick(StickerView.this);
                return true;
            }
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx = (int) (e2.getX() - e1.getX());
            int dy = (int) (e2.getY() - e1.getY());
            final int oldWidth = mParams.stickerWidth;
            final int oldHeight = mParams.stickerHeight;

            switch (mDragDirection){
                case DRAG_DIRECTION_LEFT_BOTTOM:{
                    int oldRight = mParams.marginStart + getPaddingStart() + mParams.stickerWidth;
                    //w, h, mx ---  fix- right-top
                    mParams.stickerWidth = mTmpStickerWidth - dx;
                    mParams.stickerHeight = mTmpStickerHeight + dy;
                    //for better performance
                    fitZoomEqual(true);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }

                    int newRight = mTmpMarginStart + dx + getPaddingStart() + mParams.stickerWidth;
                    if(newRight != oldRight){
                        dx -= newRight - oldRight;
                    }
                    moveInternal(dx, 0, true);
                }break;

                case DRAG_DIRECTION_LEFT_TOP:{
                    int oldRight = mParams.marginStart + getPaddingStart() + mParams.stickerWidth;
                    int oldBottom = mParams.marginTop + getPaddingTop() + mParams.stickerHeight;
                    //w, h. mx, my ------ fix=right-bottom
                    mParams.stickerWidth = mTmpStickerWidth - dx;
                    mParams.stickerHeight = mTmpStickerHeight - dy;
                    fitZoomEqual(false);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }

                    int newRight = mTmpMarginStart + dx + getPaddingStart() + mParams.stickerWidth;
                    int newBottom = mTmpMarginTop + dy + getPaddingTop() + mParams.stickerHeight;
                    if(newRight != oldRight){
                        dx -= newRight - oldRight;
                    }
                    if(newBottom != oldBottom){
                        dy -= newBottom - oldBottom;
                    }
                    moveInternal(dx, dy, true);
                }break;

                case DRAG_DIRECTION_RIGHT_BOTTOM:{
                    //w,h
                    mParams.stickerWidth = mTmpStickerWidth + dx;
                    mParams.stickerHeight = mTmpStickerHeight + dy;
                    fitZoomEqual(false);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }
                    handleReachEdge(true);
                    invalidate();
                }break;

                case DRAG_DIRECTION_RIGHT_TOP:{
                    int oldBottom = mParams.marginTop + getPaddingTop() + mParams.stickerHeight;
                    //w, h, my ---- fix= left-bottom
                    mParams.stickerWidth = mTmpStickerWidth + dx;
                    mParams.stickerHeight = mTmpStickerHeight - dy;
                    fitZoomEqual(true);
                    //check scale bounds
                    if(reachScaleBound()){
                        mParams.stickerWidth = oldWidth;
                        mParams.stickerHeight = oldHeight;
                        return true;
                    }
                    int newBottom = mTmpMarginTop + dy + getPaddingTop() + mParams.stickerHeight;
                    if(newBottom != oldBottom){
                        dy -= newBottom - oldBottom;
                    }
                    moveInternal(0, dy, true);
                }break;

                default:
                    //not drag
                    moveInternal(dx, dy, false);
            }
            return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    public static class Params implements Parcelable {
        //can't change
        int _rawStickerWidth;
        int _rawStickerHeight;

        int rawStickerWidth;
        int rawStickerHeight;

        public int stickerWidth;
        public int stickerHeight;
        float stickerScaleRatio;
        int fixOrientation;       //init fix width or height
        boolean stickerInCenter;  //init in center
        //line
        int lineColor;
        float linePathInterval;
        float linePathPhase;
        //dot
        float dotRadius;
        int dotColor;
        //text area
        public int textBgColor;
        public int textBgRoundSize;
        public int textColor;
        public float textSize;
        public int textMarginStart;
        public int textPaddingStart;
        public int textPaddingTop;
        public int textPaddingEnd;
        public int textPaddingBottom;
        public String text;
        public boolean textEnabled;

        boolean proportionalZoom; //zoom -equal or not

        //content margin top and bottom
        public int marginStart = 0;
        public int marginTop = 0 ;
        float minScale = 0.675f;
        float maxScale = 1000000;

        int touchPadding;

        public Params(){}
        public void init(TypedArray ta){
            rawStickerWidth = stickerWidth = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_init_width, 0);
            rawStickerHeight = stickerHeight = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_sticker_init_height, 0);
            stickerScaleRatio = ta.getFloat(R.styleable.StickerView_stv_sticker_init_scale_ratio, 0);
            _rawStickerWidth = rawStickerWidth;
            _rawStickerHeight = rawStickerHeight;

            lineColor = ta.getColor(R.styleable.StickerView_stv_line_color, Color.BLACK);
            linePathInterval = ta.getFloat(R.styleable.StickerView_stv_line_pe_interval, 0);
            linePathPhase = ta.getFloat(R.styleable.StickerView_stv_line_pe_phase, 0);

            dotRadius = ta.getDimensionPixelSize(R.styleable.StickerView_stv_dotRadius, 0);
            dotColor = ta.getColor(R.styleable.StickerView_stv_dotColor, Color.BLACK);

            text = ta.getString(R.styleable.StickerView_stv_text);
            textEnabled = ta.getBoolean(R.styleable.StickerView_stv_text_enable, true);
            textBgColor = ta.getColor(R.styleable.StickerView_stv_text_bg_color, Color.BLACK);
            textBgRoundSize = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_bg_round, 20);

            textColor = ta.getColor(R.styleable.StickerView_stv_text_color, Color.WHITE);
            textSize = ta.getDimension(R.styleable.StickerView_stv_text_size, 15);
            textMarginStart = ta.getDimensionPixelSize(R.styleable.StickerView_stv_text_marginStart, 30);

            textPaddingStart = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_start, 0);
            textPaddingTop = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_top, 0);
            textPaddingEnd = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_end, 0);
            textPaddingBottom = ta.getDimensionPixelOffset(R.styleable.StickerView_stv_text_padding_bottom, 0);

            proportionalZoom = ta.getBoolean(R.styleable.StickerView_stv_proportional_zoom, true);
            marginStart = ta.getDimensionPixelSize(R.styleable.StickerView_stv_content_margin_start, 0);
            marginTop = ta.getDimensionPixelSize(R.styleable.StickerView_stv_content_margin_top, 0);
            minScale = ta.getFloat(R.styleable.StickerView_stv_min_scale, minScale);
            maxScale = ta.getFloat(R.styleable.StickerView_stv_max_scale, maxScale);

            touchPadding = ta.getDimensionPixelSize(R.styleable.StickerView_stv_touch_padding, 10);
            fixOrientation = ta.getInt(R.styleable.StickerView_stv_sticker_init_fix_orientation, 0);
            stickerInCenter = ta.getBoolean(R.styleable.StickerView_stv_sticker_init_center, true);
        }
        void resetStickerWidthHeight() {
            stickerWidth = rawStickerWidth = _rawStickerWidth;
            stickerHeight = rawStickerHeight = _rawStickerHeight;
        }
        //effect raw
        void setStickerWidth0(int width){
            rawStickerWidth = stickerWidth = width;
        }
        void setStickerHeight0(int height){
            rawStickerHeight = stickerHeight = height;
        }
        void setStickerWidthHeight(Bitmap sticker, boolean checkFixOrientation){
            if(stickerScaleRatio > 0){
                setStickerWidth0((int) (sticker.getWidth() * stickerScaleRatio));
                setStickerHeight0((int) (sticker.getHeight() * stickerScaleRatio));
            }else {
                //fix height. compute width
                if(checkFixOrientation && fixOrientation == FIX_HEIGHT){
                    if(stickerHeight == 0){
                        throw new IllegalStateException();
                    }
                    //sw / sh = w / stickerHeight
                    setStickerWidth0(sticker.getWidth() * stickerHeight / sticker.getHeight());
                }else {
                    if(stickerWidth <= 0){
                         setStickerWidth0(sticker.getWidth());
                    }
                }
                //fix width .compute height
                if(checkFixOrientation && fixOrientation == FIX_WIDTH){
                    if(stickerWidth == 0){
                        throw new IllegalStateException();
                    }
                    setStickerHeight0(sticker.getHeight() * stickerWidth / sticker.getWidth());
                }else {
                    if(stickerHeight <= 0){
                        setStickerHeight0(sticker.getHeight());
                    }
                }
            }
        }
        public void swapStickerWidthHeight(){
            int rw = rawStickerWidth;
            rawStickerWidth = rawStickerHeight;
            rawStickerHeight = rw;

            int w = stickerWidth;
            stickerWidth = stickerHeight;
            stickerHeight = w;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this._rawStickerWidth);
            dest.writeInt(this._rawStickerHeight);
            dest.writeInt(this.rawStickerWidth);
            dest.writeInt(this.rawStickerHeight);
            dest.writeInt(this.stickerWidth);
            dest.writeInt(this.stickerHeight);
            dest.writeFloat(this.stickerScaleRatio);
            dest.writeInt(this.lineColor);
            dest.writeFloat(this.linePathInterval);
            dest.writeFloat(this.linePathPhase);
            dest.writeFloat(this.dotRadius);
            dest.writeInt(this.dotColor);
            dest.writeInt(this.textBgColor);
            dest.writeInt(this.textBgRoundSize);
            dest.writeInt(this.textColor);
            dest.writeFloat(this.textSize);
            dest.writeInt(this.textMarginStart);
            dest.writeInt(this.textPaddingStart);
            dest.writeInt(this.textPaddingTop);
            dest.writeInt(this.textPaddingEnd);
            dest.writeInt(this.textPaddingBottom);
            dest.writeString(this.text);
            dest.writeByte(this.textEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.proportionalZoom ? (byte) 1 : (byte) 0);
            dest.writeInt(this.marginStart);
            dest.writeInt(this.marginTop);
            dest.writeFloat(this.minScale);
            dest.writeFloat(this.maxScale);
            dest.writeInt(this.touchPadding);
            dest.writeInt(this.fixOrientation);
            dest.writeByte(this.stickerInCenter ? (byte) 1 : (byte) 0);
        }

        protected Params(Parcel in) {
            setFromParcel(in);
        }
        public void setFromParcel(Parcel in){
            this._rawStickerWidth = in.readInt();
            this._rawStickerHeight = in.readInt();
            this.rawStickerWidth = in.readInt();
            this.rawStickerHeight = in.readInt();
            this.stickerWidth = in.readInt();
            this.stickerHeight = in.readInt();
            this.stickerScaleRatio = in.readFloat();
            this.lineColor = in.readInt();
            this.linePathInterval = in.readFloat();
            this.linePathPhase = in.readFloat();
            this.dotRadius = in.readFloat();
            this.dotColor = in.readInt();
            this.textBgColor = in.readInt();
            this.textBgRoundSize = in.readInt();
            this.textColor = in.readInt();
            this.textSize = in.readFloat();
            this.textMarginStart = in.readInt();
            this.textPaddingStart = in.readInt();
            this.textPaddingTop = in.readInt();
            this.textPaddingEnd = in.readInt();
            this.textPaddingBottom = in.readInt();
            this.text = in.readString();
            this.textEnabled = in.readByte() != 0;
            this.proportionalZoom = in.readByte() != 0;
            this.marginStart = in.readInt();
            this.marginTop = in.readInt();
            this.minScale = in.readFloat();
            this.maxScale = in.readFloat();
            this.touchPadding = in.readInt();
            this.fixOrientation = in.readInt();
            this.stickerInCenter = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Params> CREATOR = new Parcelable.Creator<Params>() {
            @Override
            public Params createFromParcel(Parcel source) {
                return new Params(source);
            }

            @Override
            public Params[] newArray(int size) {
                return new Params[size];
            }
        };

        @Override
        public String toString() {
            return "Params{" +
                    "rawStickerWidth=" + rawStickerWidth +
                    ", rawStickerHeight=" + rawStickerHeight +
                    ", stickerWidth=" + stickerWidth +
                    ", stickerHeight=" + stickerHeight +
                    ", stickerScaleRatio=" + stickerScaleRatio +
                    ", fixOrientation=" + fixOrientation +
                    ", lineColor=" + lineColor +
                    ", linePathInterval=" + linePathInterval +
                    ", linePathPhase=" + linePathPhase +
                    ", dotRadius=" + dotRadius +
                    ", dotColor=" + dotColor +
                    ", textBgColor=" + textBgColor +
                    ", textBgRoundSize=" + textBgRoundSize +
                    ", textColor=" + textColor +
                    ", textSize=" + textSize +
                    ", textMarginStart=" + textMarginStart +
                    ", textPaddingStart=" + textPaddingStart +
                    ", textPaddingTop=" + textPaddingTop +
                    ", textPaddingEnd=" + textPaddingEnd +
                    ", textPaddingBottom=" + textPaddingBottom +
                    ", text='" + text + '\'' +
                    ", textEnabled=" + textEnabled +
                    ", proportionalZoom=" + proportionalZoom +
                    ", marginStart=" + marginStart +
                    ", marginTop=" + marginTop +
                    ", minScale=" + minScale +
                    ", maxScale=" + maxScale +
                    ", touchPadding=" + touchPadding +
                    '}';
        }
    }

    /**
     * the click listener for sticker-view
      */
    public interface Callback{
        /**
         * called on click text area
         * @param view the view
         */
        void onClickTextArea(StickerView view);

        /**
         * called on click sticker
         * @param view the sticker view
         */
        void onClickSticker(StickerView view);
    }

    /**
     * the decoration
     */
    public abstract static class Decoration{
        /**
         * called on draw this.
         * @param view the sticker view
         * @param stickerWidth the sticker width
         * @param stickerHeight the sticker height
         */
        public abstract void onDraw(StickerView view, int stickerWidth, int stickerHeight);

        /**
         * get range rect. which used for check motion event.
         * @param rect the out range rect.exclude padding, {@linkplain StickerView#getMarginStart()} and {@linkplain StickerView#getMarginTop()}.
         */
        public abstract void getRangeRect(Rect rect);

        /**
         * called on click this decoration
         * @param view the sticker view
         */
        public abstract void onClick(StickerView view);
    }
}
