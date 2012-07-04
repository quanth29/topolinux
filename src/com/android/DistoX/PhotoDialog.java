/* @file PhotoDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid pohoto dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;

// import android.util.Log;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class PhotoDialog extends Dialog
                         implements View.OnClickListener
{
  // private static final String TAG = "DistoX";
  private ShotActivity mParent;

  private EditText mETname;     // station name / photo name
  private EditText mETcomment;  // photo comment
  private Button   mButtonOK;
  private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoDialog( Context context, ShotActivity parent )
  {
    super( context );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // Log.v( TAG, "onCreate" );
    setContentView(R.layout.distox_photo_dialog);
    mETname       = (EditText) findViewById(R.id.photo_name);
    mETcomment    = (EditText) findViewById(R.id.photo_comment);
    mButtonOK     = (Button) findViewById(R.id.photo_ok );
    mButtonCancel = (Button) findViewById(R.id.photo_cancel );

    setTitle( R.string.title_photo );

    mButtonOK.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // Log.v(TAG, "text " + mETname.getText().toString() );
    if ( b == mButtonOK && mETname.getText() != null ) {
      if ( mETcomment.getText() == null ) {
        mParent.takePhoto( mETname.getText().toString(), "" );
      } else {
        mParent.takePhoto( mETname.getText().toString(), mETcomment.getText().toString() );
      }
    }
    dismiss();
  }

}

