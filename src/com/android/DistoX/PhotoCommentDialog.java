/* @file PhotoCommentDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo comment dialog (to enter the comment of the photo)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

public class PhotoCommentDialog extends Dialog
                         implements View.OnClickListener
{
  private ShotActivity mParent;

  private EditText mETcomment;     // photo comment
  private Button   mButtonOK;
  private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoCommentDialog( Context context, ShotActivity parent )
  {
    super( context );
    mParent = parent;
    // TopoDroidApp.Log( TopoDroidApp.LOG_PHOTO, "PhotoCommentDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log(  TopoDroidApp.LOG_PHOTO, "PhotoCommentDialog onCreate" );
    setContentView(R.layout.distox_photo_comment_dialog);

    mETcomment    = (EditText) findViewById(R.id.photo_comment_comment);
    mButtonOK     = (Button) findViewById(R.id.photo_comment_ok );
    mButtonCancel = (Button) findViewById(R.id.photo_comment_cancel );

    setTitle( R.string.title_photo_comment );

    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "PhotoCommentDialog onClick() " + b.getText().toString() );

    if ( b == mButtonOK && mETcomment.getText() != null ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PHOTO, "set photo comment " + mETcomment.getText().toString() );
      mParent.insertPhoto( mETcomment.getText().toString() );
    }
    dismiss();
  }

}

