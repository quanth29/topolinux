/* @file DistoXSurveyDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid new survey dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 team attribute
 */
package com.android.DistoX;

// import java.Thread;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

public class DistoXSurveyDialog extends Dialog
                                implements View.OnClickListener
{
  // private static final String TAG = "DistoXSurveyDialog";
  private DistoX mDistoX;

  private EditText mEditText;
  private EditText mEditDate;
  private EditText mEditTeam;
  private EditText mEditComment;
  private Button   mBtnOK;
  private Button   mBtnBack;

  public DistoXSurveyDialog( Context context, DistoX distox )
  {
    super(context);
    mDistoX = distox;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_survey_dialog);
    mEditText    = (EditText) findViewById(R.id.new_survey_name);
    mEditDate    = (EditText) findViewById(R.id.new_survey_date);
    mEditTeam    = (EditText) findViewById(R.id.new_survey_team);
    mEditComment = (EditText) findViewById(R.id.new_survey_comment);
    mEditText.setHint( R.string.survey_name );
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    mEditDate.setText( sdf.format( new Date() ) );
    mEditComment.setHint( R.string.survey_description );
    mEditTeam.setHint( R.string.survey_team );

    mBtnOK = (Button) findViewById(R.id.button_ok_survey_name );
    mBtnOK.setOnClickListener( this );
    mBtnBack = (Button) findViewById(R.id.button_back_survey_name );
    mBtnBack.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      mDistoX.makeNewSurvey( mEditText.getText().toString(),
                             mEditDate.getText().toString(),
                             mEditComment.getText().toString(),
                             mEditText.getText().toString() );
    } else {
      /* nothing */
    }
    dismiss();
  }

}

