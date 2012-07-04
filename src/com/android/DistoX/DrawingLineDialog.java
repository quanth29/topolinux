/** @file DrawingLineDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid scrap line attributes editing dialog
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
import android.widget.CheckBox;
import android.view.View;

public class DrawingLineDialog extends Dialog
                               implements View.OnClickListener
{
  // private static final String TAG = "DistoX";

  private DrawingLinePath mLine;
  private DrawingActivity mActivity;

  private TextView mTVtype;
  private EditText mEToptions;
 
  private RadioButton mBtnOutlineOut;
  private RadioButton mBtnOutlineIn;
  private RadioButton mBtnOutlineNone;

  private CheckBox mReversed;

  private Button   mButtonOk;
  private Button   mButtonCancel;
  private Button   mButtonDelete;

  public DrawingLineDialog( DrawingActivity context, DrawingLinePath line )
  {
    super( context );
    mActivity = context;
    mLine = line;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_dialog);

    mTVtype = (TextView) findViewById( R.id.line_type );
    mEToptions = (EditText) findViewById( R.id.line_options );

    mTVtype.setText( DrawingBrushPaths.lineThName[ mLine.mLineType ] );
    if ( mLine.mOptions != null ) {
      mEToptions.setText( mLine.mOptions );
    }

    mBtnOutlineOut  = (RadioButton) findViewById( R.id.line_outline_out );
    mBtnOutlineIn   = (RadioButton) findViewById( R.id.line_outline_in );
    mBtnOutlineNone = (RadioButton) findViewById( R.id.line_outline_none );

    if ( mLine.mOutline == DrawingLinePath.OUTLINE_OUT ) {
      mBtnOutlineOut.setChecked( true );
    } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_IN ) {
      mBtnOutlineIn.setChecked( true );
    } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_NONE ) {
      mBtnOutlineNone.setChecked( true );
    }

    mReversed = (CheckBox) findViewById( R.id.line_reversed );
    mReversed.setChecked( mLine.mReversed );

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
        if ( options.length() > 0 ) mLine.mOptions = options;
      }
      if ( mBtnOutlineOut.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_OUT;
      else if ( mBtnOutlineIn.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_IN;
      else if ( mBtnOutlineNone.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_NONE;

      mLine.setReversed( mReversed.isChecked() );
    } else if ( b == mButtonDelete ) {
      // TODO
    }
    dismiss();
  }

}

