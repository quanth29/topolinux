/* @file PhotoSensorsDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
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

// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class PhotoSensorsDialog extends Dialog
                                implements View.OnClickListener
{
  private ShotActivity mParent;

  private Button   mButtonPhoto;
  private Button   mButtonSensor;
  private Button   mButtonExternal;

  private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoSensorsDialog( Context context, ShotActivity parent )
  {
    super( context );
    mParent = parent;
    // TopoDroidApp.Log( TopoDroidApp.LOG_PHOTO, "PhotoSensorDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log(  TopoDroidApp.LOG_PHOTO, "PhotoSensorDialog onCreate" );
    setContentView(R.layout.distox_photo_sensor_dialog);

    mButtonPhoto    = (Button) findViewById(R.id.photo_photo );
    mButtonSensor   = (Button) findViewById(R.id.photo_sensor );
    mButtonExternal = (Button) findViewById(R.id.photo_external );

    mButtonCancel = (Button) findViewById(R.id.photo_cancel );

    setTitle( R.string.title_photo );

    mButtonPhoto.setOnClickListener( this );
    mButtonSensor.setOnClickListener( this );
    mButtonExternal.setOnClickListener( this );

    mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log(  TopoDroidApp.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );

    if ( b == mButtonPhoto ) {
      mParent.askPhoto( );
    } else if ( b == mButtonSensor ) {
      mParent.askSensor( );
    } else if ( b == mButtonExternal ) {
      mParent.askExternal( );
    }
    dismiss();
  }

}

