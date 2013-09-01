/** @file DrawingLineDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121225 implemented erase
 * 20130826 added splitLine
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

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
  private DrawingLinePath mLine;
  private DrawingActivity mParent;

  // private TextView mTVtype;
  private EditText mEToptions;
 
  private RadioButton mBtnOutlineOut;
  private RadioButton mBtnOutlineIn;
  private RadioButton mBtnOutlineNone;

  private CheckBox mReversed;

  private Button   mBtnOk;
  // private Button   mBtnCancel;
  private Button   mBtnSplit;
  private Button   mBtnErase;

  private float mX; // scene X coordinate of the selection point (= split point)
  private float mY; // scene Y coordinate 

  public DrawingLineDialog( DrawingActivity context, DrawingLinePath line, float x, float y )
  {
    super( context );
    mParent = context;
    mLine = line;
    mX = x; 
    mY = y;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_dialog);

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_line ),
              DrawingBrushPaths.getLineThName( mLine.mLineType ) ) );

    // mTVtype = (TextView) findViewById( R.id.line_type );
    mEToptions = (EditText) findViewById( R.id.line_options );

    // mTVtype.setText( DrawingBrushPaths.getLineThName( mLine.mLineType ) );
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

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    mBtnSplit = (Button) findViewById( R.id.button_split );
    mBtnSplit.setOnClickListener( this );

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingLineDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mLine.mOptions = options;
      }
      if ( mBtnOutlineOut.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_OUT;
      else if ( mBtnOutlineIn.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_IN;
      else if ( mBtnOutlineNone.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_NONE;

      mLine.setReversed( mReversed.isChecked() );
    } else if ( b == mBtnSplit   ) {
      mParent.splitLine( mLine, mX, mY );
    } else if ( b == mBtnErase ) {
      mParent.deleteLine( mLine );
    }
    dismiss();
  }

}

