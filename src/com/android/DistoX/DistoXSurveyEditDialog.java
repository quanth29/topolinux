/* @file DistoXSurveyEditDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey edit dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
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
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

public class DistoXSurveyEditDialog extends Dialog
                                    implements View.OnClickListener
{
  // private static final String TAG = "DistoX";
  private DistoX mDistoX;

  private TextView mEditName;
  private EditText mEditDate;
  private EditText mEditComment;
  private EditText mEditTeam;

  private Button   mBtnOK;
  private Button   mBtnBack;

  private DistoXSurveyInfo info;

  public DistoXSurveyEditDialog( Context context, DistoX distox, DistoXSurveyInfo i )
  {
    super(context);
    mDistoX = distox;
    info    = i;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_survey_edit_dialog);
    mEditName    = (TextView) findViewById(R.id.edit_survey_name);
    mEditDate    = (EditText) findViewById(R.id.edit_survey_date);
    mEditComment = (EditText) findViewById(R.id.edit_survey_comment);
    mEditTeam    = (EditText) findViewById(R.id.edit_survey_team);
    mEditName.setText( info.name );
    // mEditName.setEditable( false );
    mEditDate.setText( info.date );
    if ( info.comment != null && info.comment.length() > 0 ) {
      mEditComment.setText( info.comment );
    } else {
      mEditComment.setHint( R.string.survey_description );
    }
    if ( info.team != null && info.team.length() > 0 ) {
      mEditTeam.setText( info.team );
    } else {
      mEditTeam.setHint( R.string.survey_team );
    }

    mBtnOK = (Button) findViewById(R.id.button_ok_survey_name );
    mBtnOK.setOnClickListener( this );
    mBtnBack = (Button) findViewById(R.id.button_back_survey_name );
    mBtnBack.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      mDistoX.editSurvey( info.id,
                          mEditName.getText().toString(),
                          mEditDate.getText().toString(),
                          mEditComment.getText().toString(),
                          mEditTeam.getText().toString() );
    } else {
      /* nothing */
    }
    dismiss();
  }

}

