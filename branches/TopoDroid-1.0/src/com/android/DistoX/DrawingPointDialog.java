/** @file DrawingPointDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid scrap point attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;

public class DrawingPointDialog extends Dialog
                               implements View.OnClickListener
{
  // private static final String TAG = "DistoX";

  private DrawingPointPath mPoint;

  private TextView mTVtype;
  private EditText mEToptions;
 
  private Button   mButtonOk;
  private Button   mButtonCancel;
  private Button   mButtonDelete;

  public DrawingPointDialog( Context context, DrawingPointPath point )
  {
    super( context );
    mPoint = point;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_point_dialog);

    mTVtype = (TextView) findViewById( R.id.point_type );
    mEToptions = (EditText) findViewById( R.id.point_options );

    mTVtype.setText( DrawingBrushPaths.pointThName[ mPoint.mPointType ] );
    if ( mPoint.mOptions != null ) {
      mEToptions.setText( mPoint.mOptions );
    }

    mButtonOk = (Button) findViewById( R.id.button_ok );
    mButtonOk.setOnClickListener( this );

    mButtonCancel = (Button) findViewById( R.id.button_cancel );
    mButtonCancel.setOnClickListener( this );

    mButtonDelete = (Button) findViewById( R.id.button_delete );
    mButtonDelete.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    if ( b == mButtonOk ) {
      if ( mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mPoint.mOptions = options;
      }
    } else if ( b == mButtonDelete ) {
      // TODO
    }
    dismiss();
  }

}

