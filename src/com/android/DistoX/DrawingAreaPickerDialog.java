/* @file DrawingAreaPicketDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: area-type pick dialog
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
// import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;

// import android.util.Log;

public class DrawingAreaPickerDialog extends Dialog 
                               implements View.OnClickListener
{
    // private static final String TAG = "DistoX DrawingAreaPicker";

    private Button[] mBtnArea;
    private Button mBtnOK;
    private Button mBtnCancel;

    private int mIndex;
    // private TextView mText;

    private Context mContext;
    private DrawingActivity mActivity;

    public DrawingAreaPickerDialog( Context context, DrawingActivity activity, int index )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mBtnArea = new Button[ DrawingBrushPaths.AREA_MAX ];
      for (int k=0; k<DrawingBrushPaths.AREA_MAX; ++k ) {
        mBtnArea[k] = new Button( context );
      }
      mIndex = index;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_item_dialog);

        TableLayout layout = (TableLayout) findViewById( R.id.layout_items );

        int k0 = 0;
        TableRow row = null;
        for (int k=0; k<DrawingBrushPaths.AREA_MAX; ++k ) {
          k0 = k0 % 3;
          if ( k0 == 0 ) {
            row = new TableRow( mContext );
            layout.addView( row );
          }
          ++ k0; 
          mBtnArea[k].setText( DrawingBrushPaths.areaLocalName[k] );
          mBtnArea[k].setOnClickListener( this );
          row.addView( mBtnArea[k] );
        }

        mBtnOK     = (Button) findViewById(R.id.button_ok);
        mBtnCancel = (Button) findViewById(R.id.button_cancel);
        mBtnOK.setOnClickListener( this );
        mBtnCancel.setOnClickListener( this );

        setTitle("AREA " + DrawingBrushPaths.areaLocalName[ mIndex ] );
    }

    public void onClick(View view)
    {
      // Log.v( TAG, "onClick()" );
      switch (view.getId()) {
        case R.id.button_ok:
          mActivity.areaSelected( mIndex );
          dismiss();
          break;
        case R.id.button_cancel:
          dismiss();
          break;
        default:
          Button btn = (Button)view;
          for (int k=0; k<DrawingBrushPaths.AREA_MAX; ++k ) {
            if ( btn == mBtnArea[k] ) {
              mIndex = k;
              setTitle("AREA " + DrawingBrushPaths.areaLocalName[ mIndex ] );
              // mText.setText( DrawingBrushPaths.areaLocalName[ mIndex ] );
              break;
            }
          }
      }
    }
}
        

