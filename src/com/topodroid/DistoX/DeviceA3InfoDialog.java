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
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.Button;

class DeviceA3InfoDialog extends Dialog
                         implements View.OnClickListener
{
  private TextView mTVstatus;
  private TextView mTVserial;
  private RadioButton mRBa3;
  private RadioButton mRBx310;
  private Button   mBTok;
  // private Button   mBTcancel;

  DeviceActivity mParent;
  Device mDevice;

  DeviceA3InfoDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context );
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_a3_info_dialog );

    mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    mRBa3.setChecked( true );
    // mRBx310.setChecked( false );

    mTVstatus = (TextView) findViewById( R.id.tv_status );
    mTVserial = (TextView) findViewById( R.id.tv_serial );

    setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVserial.setText( mParent.readDistoXCode() );
    mTVstatus.setText( mParent.readA3status() );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBTok.setOnClickListener( this );
    // mBTcancel = (Button) findViewById( R.id.button_cancel );
    // mBTcancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      // TODO ask confirm
      new TopoDroidAlertDialog( mParent, mParent.getResources(),
                                mParent.getResources().getString( R.string.device_model_set ) + " ?",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doSetModel( );
          }
        }
      );
    // } else if ( b == mBTcancel ) {
    //   dismiss();
    }
  }

  void doSetModel()
  {
    if ( mRBa3.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_A3 );
    } else if ( mRBx310.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_X310 );
    }
  }

}
