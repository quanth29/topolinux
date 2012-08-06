/* @file DrawingZoomDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: zoom in/out dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.widget.Button;

public class DrawingZoomDialog extends Dialog 
                               implements View.OnClickListener
{
    // private static final int ZOOM_NONE  = 0;
    // private static final int ZOOM_MINUS = 1;
    // private static final int ZOOM_ONE   = 2;
    // private static final int ZOOM_PLUS  = 3;

    private Button mBtnPlus;
    private Button mBtnOne;
    private Button mBtnMinus;
    private Button mBtnCancel;

    private DrawingActivity mActivity;

    public DrawingZoomDialog( Context context, DrawingActivity activity )
    {
      super(context);
      mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_zoom_dialog);

        mBtnPlus   = (Button) findViewById(R.id.button_zoom_plus);
        mBtnOne    = (Button) findViewById(R.id.button_zoom_one);
        mBtnMinus  = (Button) findViewById(R.id.button_zoom_minus);
        mBtnCancel = (Button) findViewById(R.id.button_zoom_cancel);

        mBtnPlus.setOnClickListener( this );
        mBtnOne.setOnClickListener( this );
        mBtnMinus.setOnClickListener( this );
        mBtnCancel.setOnClickListener( this );

        setTitle( R.string.title_zoom );
    }

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "ZoomDialog onClick()" );
      switch (view.getId()){
        case R.id.button_zoom_plus:
          mActivity.zoomIn();
          break;
        case R.id.button_zoom_one:
          mActivity.zoomOne();
          break;
        case R.id.button_zoom_minus:
          mActivity.zoomOut();
          break;
      }
      dismiss();
    }
}
        

