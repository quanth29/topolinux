/* @file SketchShape.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: surface cross-shape editor
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130308 created
 */
package com.android.DistoX;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.widget.Button;
import android.widget.CheckBox;

import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import android.util.FloatMath;

/**
 */
public class SketchShape extends Dialog 
                         implements View.OnTouchListener
                                  , View.OnClickListener
{
  public static float CENTER_X = 100f;
  public static float CENTER_Y = 120f;

  private static final float SCALE_FIX = 20.0f; // FIXME

  private Button mBtnOk;
  private Button mBtnRedraw;
  private Button mBtnCancel;
  private Button mBtnErase;
  private CheckBox mCBvertical;

  SketchActivity mParent;
  private SketchShapeSurface mSurface;

  private float mSaveX;
  private float mSaveY;
  private float[] mShapeSize;


  // ----------------------------------------------------------

  public SketchShape( Context context, float[] shape_size, SketchActivity parent )
  {
    super( context );
    mShapeSize = shape_size;
    mParent = parent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.sketch_shape);

    mSurface = (SketchShapeSurface) findViewById(R.id.sketchShapeSurface);
    mSurface.setRelativeSize( mShapeSize );
    mSurface.setOnTouchListener(this);

    mBtnOk      = (Button) findViewById(R.id.btn_ok);
    mBtnRedraw  = (Button) findViewById(R.id.btn_redraw);
    mBtnCancel  = (Button) findViewById(R.id.btn_cancel);
    mBtnErase   = (Button) findViewById(R.id.btn_erase);
    mCBvertical = (CheckBox) findViewById( R.id.cb_vertical );

    mBtnOk.setOnClickListener( this );
    mBtnRedraw.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
    mBtnErase.setOnClickListener( this );
  }

  // ifdef USE_4_CORNERS
  // private void doSelectAt( float x, float y ) // (x,y) canvas coords
  // {
  //   float xcenter = mSurface.mXcenter;
  //   float ycenter = mSurface.mYcenter;
  //   x -= xcenter;
  //   y -= ycenter;
  //   if ( Math.abs( Math.abs(x) - Math.abs(y) ) > 30 ) return;
  //   float z = (float)( Math.abs(x) + Math.abs(y) ) / 2;
  //   if ( x < 0 ) {
  //     if ( y < 0 ) {
  //       mSurface.ptTL.set( xcenter-z, ycenter-z );
  //     } else {
  //       mSurface.ptBL.set( xcenter-z, ycenter+z );
  //     }
  //   } else {
  //     if ( y < 0 ) {
  //       mSurface.ptTR.set( xcenter+z, ycenter-z );
  //     } else {
  //       mSurface.ptBR.set( xcenter+z, ycenter+z );
  //     }
  //   }
  // }

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    // WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
    MotionEvent event = rawEvent;
    // dumpEvent( event );

    float x_canvas = event.getX();
    float y_canvas = event.getY();
    
    int action = event.getAction() & MotionEvent.ACTION_MASK;
    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      /* nothing */
    } else if (action == MotionEvent.ACTION_DOWN) {
      mSurface.startPath( x_canvas, y_canvas );
      mSaveX = x_canvas;
      mSaveY = y_canvas;
    } else if (action == MotionEvent.ACTION_MOVE) {
      // ifdef USE_4_CORNERS
      // doSelectAt( x_canvas, y_canvas );
      float x_shift = x_canvas - mSaveX; // compute shift
      float y_shift = y_canvas - mSaveY;
      if ( Math.abs(x_shift) + Math.abs(y_shift) > 20 ) {
        mSurface.addPathPoint( x_canvas, y_canvas );
        mSaveX = x_canvas;
        mSaveY = y_canvas;
      }
    } else if (action == MotionEvent.ACTION_UP) {
      /* nothing */
      mSurface.closePath();
    }
    return true;
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId()){
      case R.id.btn_ok:
        // ifdef USE_4_CORNERS
        // float xcenter = mSurface.mXcenter;
        // float r1 = (float)Math.abs(xcenter - mSurface.ptTR.x)/mSurface.mRadius;
        // float r2 = (float)Math.abs(xcenter - mSurface.ptTL.x)/mSurface.mRadius;
        // float r3 = (float)Math.abs(xcenter - mSurface.ptBL.x)/mSurface.mRadius;
        // float r4 = (float)Math.abs(xcenter - mSurface.ptBR.x)/mSurface.mRadius;
        // // Log.v("DistoX", "Rs " + r1 + " " + r2 + " " + r3 + " " + r4 );
        // mParent.doMakeSurface( r1, r2, r3, r4 );
        if ( mSurface.mPath != null ) {
          hide();
          mParent.doMakeSurface( mSurface.mXcenter, mSurface.mYcenter, mSurface.mRadius, mSurface.mPath,
                                 mCBvertical.isChecked() );
          dismiss();
        }
        break;
      case R.id.btn_erase:
        hide();
        mParent.removeSurface();
        dismiss();
      case R.id.btn_redraw:
        mSurface.resetPath();
        break;
      case R.id.btn_cancel:
        hide();
        dismiss();
        break;
    }
  }

}
