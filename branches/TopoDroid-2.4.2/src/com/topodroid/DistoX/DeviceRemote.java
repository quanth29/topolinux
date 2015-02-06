/** @file DeviceRemote.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device remote control
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
// import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.TextView;
import android.widget.Button;

class DeviceRemote extends Dialog
                   implements View.OnClickListener
{
  private Button mBTlaserOn;
  private Button mBTlaserOff;
  private Button mBTmeasure;

  // private Button mBTback;

  DeviceActivity mParent;
  TopoDroidApp   mApp;

  DeviceRemote( Context context, DeviceActivity parent, TopoDroidApp app )
  {
    super( context );
    mParent = parent;
    mApp    = app;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_remote );

    setTitle( mParent.getResources().getString( R.string.remote_title ) );

    mBTlaserOn  = (Button) findViewById( R.id.remote_on );
    mBTlaserOff = (Button) findViewById( R.id.remote_off );
    mBTmeasure  = (Button) findViewById( R.id.remote_measure );
    // mBTback     = (Button) findViewById( R.id.button_back );

    mBTlaserOn.setOnClickListener( this );
    mBTlaserOff.setOnClickListener( this );
    mBTmeasure.setOnClickListener( this );
    // mBTback.setOnClickListener( this );
  }

  @Override
  public void onClick( View view )
  {
    switch ( view.getId() ) {
      case R.id.remote_on:
        mApp.setX310Laser( 1 );
        break;
      case R.id.remote_off:
        mApp.setX310Laser( 0 );
        break;
      case R.id.remote_measure:
        mApp.setX310Laser( 2 );
        break;
      // case R.id.button_back:
      //   dismiss();
      //   break;
    }
  }

}
