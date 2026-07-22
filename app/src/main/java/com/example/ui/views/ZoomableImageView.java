package com.example.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView implements View.OnTouchListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private final PointF start = new PointF();
    private final PointF mid = new PointF();

    private float minScale = 1.0f;
    private float maxScale = 5.0f;
    private float saveScale = 1.0f;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private float origWidth = 0f;
    private float origHeight = 0f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public ZoomableImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
        setOnTouchListener(this);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            origWidth = bm.getWidth();
            origHeight = bm.getHeight();
            fitToScreen();
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null) {
            origWidth = drawable.getIntrinsicWidth();
            origHeight = drawable.getIntrinsicHeight();
            fitToScreen();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int newWidth = MeasureSpec.getSize(widthMeasureSpec);
        int newHeight = MeasureSpec.getSize(heightMeasureSpec);

        if ((viewWidth != newWidth || viewHeight != newHeight) && newWidth > 0 && newHeight > 0) {
            viewWidth = newWidth;
            viewHeight = newHeight;
            fitToScreen();
        }
    }

    public void resetZoom() {
        saveScale = 1.0f;
        fitToScreen();
    }

    public boolean isZoomed() {
        return saveScale > 1.05f;
    }

    public void fitToScreen() {
        if (origWidth == 0 || origHeight == 0 || viewWidth == 0 || viewHeight == 0) {
            return;
        }

        float scale;
        float scaleX = (float) viewWidth / origWidth;
        float scaleY = (float) viewHeight / origHeight;
        scale = Math.min(scaleX, scaleY);

        matrix.setScale(scale, scale);

        // Center the image
        float redundantYSpace = (float) viewHeight - (scale * origHeight);
        float redundantXSpace = (float) viewWidth - (scale * origWidth);
        redundantYSpace /= 2;
        redundantXSpace /= 2;

        matrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = origWidth * scale;
        origHeight = origHeight * scale;
        saveScale = 1.0f;
        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(curr);
                mode = DRAG;
                if (isZoomed()) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                start.set(curr);
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG && saveScale > minScale) {
                    float deltaX = curr.x - start.x;
                    float deltaY = curr.y - start.y;

                    matrix.set(savedMatrix);
                    matrix.postTranslate(deltaX, deltaY);
                    fixTranslation();

                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }

        setImageMatrix(matrix);
        return true;
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        float mScaleFactor = detector.getScaleFactor();
        float origScale = saveScale;
        saveScale *= mScaleFactor;

        if (saveScale > maxScale) {
            saveScale = maxScale;
            mScaleFactor = maxScale / origScale;
        } else if (saveScale < minScale) {
            saveScale = minScale;
            mScaleFactor = minScale / origScale;
        }

        if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
            matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2f, viewHeight / 2f);
        } else {
            matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
        }

        fixTranslation();
        return true;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        mode = ZOOM;
        return true;
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
    }

    private void fixTranslation() {
        matrix.getValues(matrixValues);
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0) {
            matrix.postTranslate(fixTransX, fixTransY);
        }
    }

    private float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        }
        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0;
    }

    // Gesture Listener callbacks
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {}

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    // Double Tap Listener
    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        return performClick();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        if (saveScale > minScale) {
            resetZoom();
        } else {
            float targetScale = 2.5f;
            matrix.postScale(targetScale, targetScale, e.getX(), e.getY());
            saveScale = targetScale;
            fixTranslation();
            setImageMatrix(matrix);
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return false;
    }
}
