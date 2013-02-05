/** @file DrawingPointDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch point attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121225 implemented erase
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

// import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.View;

public class DrawingPointDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingPointPath mPoint;
  private DrawingActivity  mParent;

  private TextView mTVtype;
  private EditText mEToptions;
  private RadioButton mBtnScaleXS;
  private RadioButton mBtnScaleS;
  private RadioButton mBtnScaleM;
  private RadioButton mBtnScaleL;
  private RadioButton mBtnScaleXL;
 
  private Button   mBtnOk;
  private Button   mBtnCancel;
  private Button   mBtnErase;

  public DrawingPointDialog( DrawingActivity context, DrawingPointPath point )
  {
    super( context );
    mParent = context;
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

    mTVtype.setText( DrawingBrushPaths.mPointLib.getPointThName( mPoint.mPointType, mPoint.mFlip ) );
    if ( mPoint.mOptions != null ) {
      mEToptions.setText( mPoint.mOptions );
    }

    mBtnScaleXS = (RadioButton) findViewById( R.id.point_scale_xs );
    mBtnScaleS  = (RadioButton) findViewById( R.id.point_scale_s  );
    mBtnScaleM  = (RadioButton) findViewById( R.id.point_scale_m  );
    mBtnScaleL  = (RadioButton) findViewById( R.id.point_scale_l  );
    mBtnScaleXL = (RadioButton) findViewById( R.id.point_scale_xl );
    switch ( mPoint.getScale() ) {
      case DrawingPointPath.SCALE_XS: mBtnScaleXS.setChecked( true ); break;
      case DrawingPointPath.SCALE_S:  mBtnScaleS.setChecked( true ); break;
      case DrawingPointPath.SCALE_M:  mBtnScaleM.setChecked( true ); break;
      case DrawingPointPath.SCALE_L:  mBtnScaleL.setChecked( true ); break;
      case DrawingPointPath.SCALE_XL: mBtnScaleXL.setChecked( true ); break;
    }

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingPointDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mPoint.mOptions = options;
      }
      if ( mBtnScaleXS.isChecked() )      mPoint.setScale( DrawingPointPath.SCALE_XS );
      else if ( mBtnScaleS.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_S  );
      else if ( mBtnScaleM.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_M  );
      else if ( mBtnScaleL.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_L  );
      else if ( mBtnScaleXL.isChecked() ) mPoint.setScale( DrawingPointPath.SCALE_XL );

    } else if ( b == mBtnErase ) {
      mParent.deletePoint( mPoint );
    }
    dismiss();
  }

}
