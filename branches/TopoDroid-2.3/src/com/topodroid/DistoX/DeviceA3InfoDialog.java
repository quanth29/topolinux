/** @file DeviceA3InfoDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX A3 device info dialog
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

class DeviceA3InfoDialog extends Dialog
{
  private TextView mTVstatus;
  private TextView mTVserial;

  DeviceActivity mParent;

  DeviceA3InfoDialog( Context context, DeviceActivity parent )
  {
    super( context );
    mParent = parent;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_a3_info_dialog );

    mTVserial = (TextView) findViewById( R.id.tv_serial );
    mTVstatus = (TextView) findViewById( R.id.tv_status );

    setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVserial.setText( mParent.readDistoXCode() );
    mTVstatus.setText( mParent.readA3status() );
  }

}
