/* @file CalibDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new-calibration dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class CalibDialog extends Dialog
                         implements View.OnClickListener
{
  // private static final String TAG = "DistoX CalibDialog";
  private CalibActivity mCalib;
  private String calibName;

  private EditText mEditText;
  private EditText mEditDate;
  private EditText mEditComment;
  private Button   mButtonOK;
 
  public CalibDialog( Context context, CalibActivity calib, String name )
  {
    super( context );
    mCalib = calib;
    calibName = name;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_calib_dialog);
    mEditText = (EditText) findViewById(R.id.edit_calib_name);
    mEditDate = (EditText) findViewById(R.id.edit_calib_date);
    mEditComment = (EditText) findViewById(R.id.edit_calib_comment);

    mEditText.setHint( calibName );
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    mEditDate.setText( sdf.format( new Date() ) );
    mEditComment.setHint( R.string.calib_comment );

    mButtonOK   = (Button) findViewById(R.id.button_ok_calib_name );
    Button back = (Button) findViewById(R.id.button_back_calib_name );
    mButtonOK.setOnClickListener( this );
    back.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    if ( ((Button)v) == mButtonOK ) {
      mCalib.makeNewCalib( mEditText.getText().toString(),
                           mEditDate.getText().toString(),
                           mEditComment.getText().toString() );
    }
    dismiss();
  }

}

