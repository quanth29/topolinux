/* @file DrawingPointPickerDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: point-type pick dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 point names in the point dialog
 * 20120618 more room for the center
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.MotionEvent;

public class DrawingPointPickerDialog extends Dialog 
{
    private static final int CENTER_RADIUS = 30;
    private static final int MIDDLE_RADIUS = 60;
    private static final int DIAM = 260; 
    private static final int DIAN = 320; 
    private static int mCenterX;
    private static int mCenterY;
    private static final int EXTRA_Y = 40;

    public interface OnPointSelectedListener 
    {
      void pointSelected( int point );
    }

    private OnPointSelectedListener mListener;
    private int mIdx;

    private static class DrawingPointPickerView extends View 
    {
      private static final int mNN = 6;

        private Paint mPaint;
        private Paint mCenterPaint;
        private Path[] mPoints;
        private OnPointSelectedListener mListen;

        private double mAngle;
        private int mIndex = -1;
        private double mSaveA;

        private int mXoffset = 0;
        private int mYoffset = 0;

        DrawingPointPickerView(Context c, OnPointSelectedListener l, int point ) 
        {
            super(c);
            mIndex  = point;
            mListen = l;
            mPoints = DrawingBrushPaths.getPaths();
            mAngle  = 2*Math.PI / ( mPoints.length );
           
            // Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            // mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor( 0xffffffff );
            mPaint.setStrokeWidth(2);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor( 0xffff0000 );
            mCenterPaint.setStyle(Paint.Style.STROKE);
            mCenterPaint.setStrokeWidth(1);
            mCenterPaint.setTextSize( 14.0f );
        }

        private boolean mTrackingCenter;
        private boolean mTrackingMiddle;
        // private boolean mHighlightCenter;
        private Canvas mCanvas;
        private int mSize = 30;

        private void setCenterPath( )
        {
          if ( mIndex >= 0 && mIndex < DrawingBrushPaths.POINT_MAX ) {
            Path path = new Path( mPoints[mIndex] );
            path.offset( 0, EXTRA_Y );
            mCanvas.drawPath( path, mCenterPaint);
            // mCanvas.drawPath( mPoints[ mIndex ], mCenterPaint);
            mCanvas.drawText( DrawingBrushPaths.pointLocalName[ mIndex ], 10, 20, mCenterPaint ); // FIXME point name pos.
          }
        }

        @Override
        protected void onDraw(Canvas canvas) 
        {
            mCanvas = canvas;
            mSize = mCanvas.getWidth() / (mNN+1); // width / 7
            mCenterX = mCanvas.getWidth() / 2;
            mCenterY = mCanvas.getHeight() / 2;
            mXoffset = mCenterX - mSize / 2;
            mYoffset = mCenterY - mSize / 2;
            mCanvas.translate( mCanvas.getWidth()/2, mCanvas.getHeight()/2 );
            int k;
            for ( k=0; k<mPoints.length; ++k ) {
              float x = (float)( mSize * ( k % mNN )) - mXoffset;
              float y = (float)( mSize * ( k / mNN )) - mYoffset;
              Path path = new Path( mPoints[k] );
              path.offset( x, y );
              mCanvas.drawPath( path, mPaint );
            }

            setCenterPath( );
            if (mTrackingCenter) {
                // int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                mCenterPaint.setColor(0xffff0000);
                mCenterPaint.setAlpha(0xFF);
                // if (mHighlightCenter) {
                //    mCenterPaint.setAlpha(0xFF);
                // } else {
                //   mCenterPaint.setAlpha(0x80);
                // }
            } else {

           }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(DIAM, DIAN);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) 
        {
            int x0 = (int)( (event.getX() - mSize/2 ) / mSize );
            int y0 = (int)( (event.getY() + mSize/2 - 30) / mSize );
            int idx = x0 + mNN * y0;
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Event X " + x0 + " Y " + y0 + " index " + idx );
        
            float x = event.getX() - mCenterX; // mCanvas.getWidth();
            float y = event.getY() - mCenterY - EXTRA_Y; // mCanvas.getHeight();
            float d = (float)( Math.sqrt(x*x + y*y) );
            boolean inCenter = d <= CENTER_RADIUS;
            boolean inMiddle = ( d > CENTER_RADIUS && d <= MIDDLE_RADIUS );
            // if ( d > CENTER ) return true;
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Event OK: X " + x + " Y " + y + " center " + inCenter + " tracking " + mTrackingCenter );

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ( idx >= 0 && idx < DrawingBrushPaths.POINT_MAX ) {
                      mIndex = idx;
                      // mCenterPaint.setColor(interpColor(mColors, unit));
                      setCenterPath();
                      invalidate();
                    } else {
                      mTrackingCenter = inCenter;
                      mTrackingMiddle = inMiddle;
                      if (inCenter) {
                        // mHighlightCenter = true;
                        // invalidate();
                        // break;
                        return true;
                      } else if ( inMiddle ) {
                        double a = Math.atan2(y/d, x/d);
                        if ( a < 0 ) a += 2*Math.PI;
                        mSaveA = a;
                      }
                    }
                case MotionEvent.ACTION_MOVE:
                    // if ( idx >= 0 && idx < DrawingBrushPaths.POINT_MAX ) {
                    //   mIndex = idx;
                    //   // mCenterPaint.setColor(interpColor(mColors, unit));
                    //   setCenterPath();
                    //   invalidate();
                    // } else 
                    if (mTrackingCenter) {
                      // if (mHighlightCenter != inCenter) {
                      //   mHighlightCenter = inCenter;
                      //   invalidate();
                      // }
                    } else if ( mTrackingMiddle ) {
                      if ( DrawingBrushPaths.canRotate( mIndex ) ) {
                        double a = Math.atan2(y/d, x/d);
                        if ( a < 0 ) a += 2*Math.PI;
                        DrawingBrushPaths.rotateRad( mIndex, a - mSaveA );
                        mSaveA = a;
                        setCenterPath();
                        invalidate();
                      }
                    } else {
                      return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                      if (inCenter) {
                        // mListen.colorChanged(mCenterPaint.getColor());
                        if ( mIndex >= 0 ) {
                          if ( mIndex < DrawingBrushPaths.POINT_MAX ) {
                            mListen.pointSelected( mIndex );
                          }
                        }
                      }
                      mTrackingCenter = false;    // so we draw w/o halo
                      invalidate();
                    } else if ( mTrackingMiddle ) {
                      mTrackingMiddle = false;
                      mSaveA = 0.0;
                    } else {
                      setCenterPath();
                      invalidate();
                    }
                    break;
            }
            return true;
        }
    }

    public DrawingPointPickerDialog( Context context,
                              OnPointSelectedListener listener,
                              int initialPoint) 
    {
        super(context);
        mListener = listener;
        mIdx = initialPoint;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        OnPointSelectedListener l = new OnPointSelectedListener() {
            public void pointSelected( int p ) {
                mListener.pointSelected( p );
                dismiss();
            }
        };

        setContentView(new DrawingPointPickerView(getContext(), l, mIdx ));
        // setTitle("Pick a Symbol");
    }
}
        

