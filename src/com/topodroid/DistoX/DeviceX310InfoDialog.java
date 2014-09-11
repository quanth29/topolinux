/** @file DeviceX310InfoDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.view.View;
import android.widget.TextView;

class DeviceX310InfoDialog extends Dialog
{
  private TextView mTVcode;
  private TextView mTVfirmware;
  private TextView mTVhardware;
  // private TextView mTV;

  DeviceActivity mParent;

  DeviceX310InfoDialog( Context context, DeviceActivity parent )
  {
    super( context );
    mParent = parent;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_x310_info_dialog );

    mTVcode = (TextView) findViewById( R.id.tv_code );
    mTVfirmware = (TextView) findViewById( R.id.tv_firmware );
    mTVhardware = (TextView) findViewById( R.id.tv_hardware );

    setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVcode.setText( mParent.readDistoXCode() );
    mTVfirmware.setText( mParent.readX310firmware() );
    mTVhardware.setText( mParent.readX310hardware() );

  }

}
