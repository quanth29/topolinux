/* @file DrawingLinePicketDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line-type pick dialog
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

public class DrawingLinePickerDialog extends Dialog 
                               implements View.OnClickListener
{
    private Button[] mBtnLine;
    private Button mBtnOK;
    private Button mBtnCancel;

    private int mIndex;
    // private TextView mText;

    private Context mContext;
    private DrawingActivity mActivity;

    public DrawingLinePickerDialog( Context context, DrawingActivity activity, int index )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mBtnLine = new Button[ DrawingBrushPaths.LINE_MAX ];
      for (int k=0; k<DrawingBrushPaths.LINE_MAX; ++k ) {
        mBtnLine[k] = new Button( context );
      }
      mIndex = index;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_item_dialog);

        // TopDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingLinePickerDialog::onCreate index " + mIndex );
        TableLayout layout = (TableLayout) findViewById( R.id.layout_items );

        int k0 = 0;
        TableRow row = null;
        for (int k=0; k<DrawingBrushPaths.LINE_MAX; ++k ) {
          k0 = k0 % 3;
          if ( k0 == 0 ) {
            row = new TableRow( mContext );
            layout.addView( row );
          }
          ++ k0; 
          mBtnLine[k].setText( DrawingBrushPaths.lineLocalName[k] );
          mBtnLine[k].setOnClickListener( this );
          row.addView( mBtnLine[k] );
        }

        mBtnOK     = (Button) findViewById(R.id.button_ok);
        mBtnCancel = (Button) findViewById(R.id.button_cancel);
        mBtnOK.setOnClickListener( this );
        mBtnCancel.setOnClickListener( this );

        setTitle( String.format( mContext.getResources().getString( R.string.title_draw_line ),
                                 DrawingBrushPaths.lineLocalName[ mIndex ] ) );
    }

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingLinePickerDialog::onClick" );
      switch (view.getId()) {
        case R.id.button_ok:
          mActivity.lineSelected( mIndex );
          dismiss();
          break;
        case R.id.button_cancel:
          dismiss();
          break;
        default:
          Button btn = (Button)view;
          for (int k=0; k<DrawingBrushPaths.LINE_MAX; ++k ) {
            if ( btn == mBtnLine[k] ) {
              mIndex = k;
              setTitle("LINE " + DrawingBrushPaths.lineLocalName[ mIndex ] );
              // mText.setText( DrawingBrushPaths.lineLocalName[ mIndex ] );
              break;
            }
          }
      }
    }
}
        

