/* @file SketchShapeSurface.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: surface cross-shape 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130308 created
 */
package com.android.DistoX;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.util.AttributeSet;

import android.util.Log;
import android.util.FloatMath;

/**
 */
public class SketchShapeSurface extends SurfaceView
                                implements SurfaceHolder.Callback
{
  private Boolean _run;
  protected SketchShapeSurfaceThread thread;
  private Bitmap mBitmap;
  public boolean isDrawing = true;
  private SurfaceHolder mHolder; // canvas holder
  private Context mContext;
  private AttributeSet mAttrs;

  int mWidth;            // canvas width
  int mHeight;           // canvas height
  int mXcenter;
  int mYcenter;
  float mRadius;

  Paint mPaintWhite;
  Paint mPaintGrey;
  Paint mPaintBlue;
  PointF ptC, ptT, ptB, ptR, ptL;
  // ifdef USE_4_CORNERS
  // PointF ptTL, ptTR, ptBL, ptBR;
  SketchShapePath mPath;
  float[] mRelativeSize;

  boolean mNeedPoints = false;

  public SketchShapeSurface( Context context, AttributeSet attrs )
  {
    super(context, attrs);

    thread = null;
    mContext = context;
    mAttrs   = attrs;
    mHolder = getHolder();
    mHolder.addCallback(this);

    mPaintWhite = new Paint(); // almost white
    mPaintWhite.setColor(0xFFeeeeee);
    mPaintWhite.setStyle(Paint.Style.STROKE);
    mPaintWhite.setStrokeJoin(Paint.Join.ROUND);
    mPaintWhite.setStrokeCap(Paint.Cap.ROUND);
    mPaintWhite.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );

    mPaintGrey= new Paint(); 
    mPaintGrey.setColor(0xFFcccccc);
    mPaintGrey.setStyle(Paint.Style.FILL_AND_STROKE);
    mPaintGrey.setStrokeJoin(Paint.Join.ROUND);
    mPaintGrey.setStrokeCap(Paint.Cap.ROUND);
    mPaintGrey.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );

    mPaintBlue = new Paint(); 
    mPaintBlue.setColor(0xFF3399ff);
    mPaintBlue.setStyle(Paint.Style.STROKE);
    mPaintBlue.setStrokeJoin(Paint.Join.ROUND);
    mPaintBlue.setStrokeCap(Paint.Cap.ROUND);
    mPaintBlue.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );

    mRelativeSize = new float[4];
    for ( int k=0; k<4; ++k ) mRelativeSize[k] = 1.0f;
    mNeedPoints = true;
    mPath = null;
  }

  /** relative sizes:
   *    right bottom left top
   */
  void setRelativeSize( float[] size )
  {
    for ( int k=0; k<4; ++k ) mRelativeSize[k] = size[k];
  }
  
  void makePoints( Canvas canvas )
  {
    if ( mNeedPoints ) {
      mWidth  = 400; // canvas.getWidth(); 
      mHeight = 640; // canvas.getHeight();
      mXcenter = mWidth / 2;
      mYcenter = mHeight / 2;
      float r = mWidth/3;
      mRadius = r/2;
      // Log.v( "DistoX", "surface " + mWidth + " " + mHeight + " R " + mRadius );

      ptC  = new PointF( mXcenter,   mYcenter   );
      ptT  = new PointF( mXcenter,   mYcenter-r*mRelativeSize[3] );
      ptB  = new PointF( mXcenter,   mYcenter+r*mRelativeSize[1] );
      ptR  = new PointF( mXcenter+r*mRelativeSize[0], mYcenter   );
      ptL  = new PointF( mXcenter-r*mRelativeSize[2], mYcenter   );
      // ifdef USE_4_CORNERS
      // ptTL = new PointF( mXcenter-mRadius, mYcenter-mRadius );
      // ptTR = new PointF( mXcenter+mRadius, mYcenter-mRadius );
      // ptBR = new PointF( mXcenter+mRadius, mYcenter+mRadius );
      // ptBL = new PointF( mXcenter-mRadius, mYcenter+mRadius );
      mNeedPoints = false;
    }
  }

  void refresh()
  {
    Canvas canvas = null;
    try {
      canvas = mHolder.lockCanvas();
      if ( mBitmap == null ) {
        mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
      }
      final Canvas c = new Canvas (mBitmap);
      makePoints( c );

      c.drawColor(0, PorterDuff.Mode.CLEAR);
      canvas.drawColor(0, PorterDuff.Mode.CLEAR);

      executeAll(c, previewDoneHandler);
      canvas.drawBitmap (mBitmap, 0,  0,null);
    } finally {
      if ( canvas != null ) {
        mHolder.unlockCanvasAndPost( canvas );
      }
    }
  }

  private Handler previewDoneHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg) {
      isDrawing = false;
    }
  };

  void resetPath()
  {
    mPath = null;
  }

  void startPath( float x, float y )
  {
    mPath = new SketchShapePath( );
    mPath.addPoint( x, y );
  }

  void addPathPoint( float x, float y )
  {
    if ( mPath != null ) {
      mPath.addPoint( x, y );
    }
  }

  void closePath()
  {
    if ( mPath != null ) {
      mPath.closePath();
    }
  }

  void drawPointAt( Canvas canvas, PointF p, Paint paint )
  {
    Path path = new Path();
    path.addCircle( p.x, p.y, 10, Path.Direction.CCW );
    canvas.drawPath( path, paint );
  }

  void drawLine( Canvas canvas, PointF p1, PointF p2, Paint paint)
  {
    Path path = new Path();
    path.moveTo( p1.x, p1.y );
    path.lineTo( p2.x, p2.y );
    canvas.drawPath( path, paint );
  }

  public void executeAll( Canvas c, Handler h )
  {
    drawLine( c, ptB, ptT, mPaintWhite );
    drawLine( c, ptL, ptR, mPaintWhite );
    // ifdef USE_4_CORNERS
    // drawLine( c, ptTL, ptT, mPaintWhite );
    // drawLine( c, ptTR, ptT, mPaintWhite );
    // drawLine( c, ptTR, ptR, mPaintWhite );
    // drawLine( c, ptBR, ptR, mPaintWhite );
    // drawLine( c, ptBR, ptB, mPaintWhite );
    // drawLine( c, ptBL, ptB, mPaintWhite );
    // drawLine( c, ptBL, ptL, mPaintWhite );
    // drawLine( c, ptTL, ptL, mPaintWhite );
    drawPointAt( c, ptC, mPaintGrey );
    drawPointAt( c, ptT, mPaintGrey );
    drawPointAt( c, ptB, mPaintGrey );
    drawPointAt( c, ptR, mPaintGrey );
    drawPointAt( c, ptL, mPaintGrey );
    // ifdef USE_4_CORNERS
    // drawPointAt( c, ptTL, mPaintBlue );
    // drawPointAt( c, ptTR, mPaintBlue );
    // drawPointAt( c, ptBL, mPaintBlue );
    // drawPointAt( c, ptBR, mPaintBlue );
    if ( mPath != null ) {
      c.drawPath( mPath.mPath, mPaintBlue );
    }
  }

  private class SketchShapeSurfaceThread extends  Thread
  {
    private SurfaceHolder mSurfaceHolder;

    public SketchShapeSurfaceThread(SurfaceHolder surfaceHolder)
    {
        mSurfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run)
    {
      _run = run;
    }

    @Override
    public void run() 
    {
      while ( _run ) {
        if ( isDrawing == true ) {
          refresh();
        }
      }
    }
  }

  public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
  {
    mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
  }


  public void surfaceCreated(SurfaceHolder mHolder) 
  {
    if (thread == null ) {
      thread = new SketchShapeSurfaceThread(mHolder);
    }
    thread.setRunning(true);
    thread.start();
  }

  public void surfaceDestroyed(SurfaceHolder mHolder) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "surfaceDestroyed " );
    // TODO Auto-generated method stub
    boolean retry = true;
    thread.setRunning(false);
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
        // we will try it again and again...
      }
    }
    thread = null;
  }
}

