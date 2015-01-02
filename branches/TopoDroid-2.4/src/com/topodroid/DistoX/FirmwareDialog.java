/** @file FirmwareDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device firmware dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201312   created to distinguish from A3 memory dialog
 * 20140416 setError for required EditText inputs
 * 20140719 save dump to file
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
// import java.io.StringWriter;
// import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Dialog;

// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
// import android.widget.TextView;
import android.widget.Toast;

import android.text.method.KeyListener;

import android.util.Log;

class FirmwareDialog extends Dialog
                             implements View.OnClickListener
{
  private RadioButton mBtnDump;
  private RadioButton mBtnUpload;
  private Button mBtnOK;
  // private Button mBtnClose;

  private EditText mETfile;

  Context mContext;
  DeviceActivity mParent;
  TopoDroidApp   mApp;
  KeyListener    mETkeyListener;

  FirmwareDialog( Context context, DeviceActivity parent, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mApp     = app;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.firmware_dialog );

    mETfile  = (EditText) findViewById( R.id.firmware_file );

    mBtnUpload = (RadioButton) findViewById(R.id.firmware_upload );
    mBtnDump   = (RadioButton) findViewById(R.id.firmware_dump );
    mBtnOK = (Button) findViewById(R.id.firmware_ok);
    // mBtnClose = (Button) findViewById(R.id.firmware_close);

    mETkeyListener = mETfile.getKeyListener();
    mETfile.setOnClickListener( this );
    // mETfile.setEnabled( false );
    mETfile.setFocusable( false );
    mETfile.setFocusableInTouchMode( false );
    // mETfile.setClickable( true );
    mETfile.setKeyListener( null );

    mBtnUpload.setOnClickListener( this );
    mBtnDump.setOnClickListener( this );
    mBtnOK.setOnClickListener( this );
    // mBtnClose.setOnClickListener( this );
    
    setTitle( mParent.getResources().getString( R.string.firmware_title ) );
  }

  void setFile( String filename )
  {
    mETfile.setText( filename );
  }

  @Override
  public void onClick( View view )
  {
    switch ( view.getId() ) {
      case R.id.firmware_file:
        if ( mBtnUpload.isChecked() ) {
          (new FirmwareFileDialog( mContext, this, mApp)).show(); // select file from bin directory
        }
        break;
      case R.id.firmware_upload:
        // mETfile.setEnabled( false );
        mETfile.setFocusable( false );
        mETfile.setFocusableInTouchMode( false );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( null );
        break;
      case R.id.firmware_dump:
        // mETfile.setEnabled( true );
        mETfile.setFocusable( true );
        mETfile.setFocusableInTouchMode( true );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( mETkeyListener );
        break;
      case R.id.firmware_ok:
        String filename = null;
        if ( mETfile.getText() != null ) { 
          filename = mETfile.getText().toString();
          if ( filename != null ) {
            filename = filename.trim();
            if ( filename.length() == 0 ) filename = null;
          }
        }
        if ( filename == null ) {
          Toast.makeText( mParent, mParent.getResources().getString(R.string.firmware_file_missing), Toast.LENGTH_SHORT).show();
          return;
        }
        if ( mBtnDump.isChecked() ) {
          File fp = new File( mApp.getBinFile( filename ) );
          if ( fp.exists() ) {
            Toast.makeText( mParent, mParent.getResources().getString(R.string.firmware_file_exists), Toast.LENGTH_SHORT).show();
            return;    
          }
          int ret = mApp.dumpFirmware( filename );
          Toast.makeText( mParent, 
            String.format( mParent.getResources().getString(R.string.firmware_file_dumped), filename, ret ),
            Toast.LENGTH_SHORT).show();
        } else if ( mBtnUpload.isChecked() ) {
          File fp = new File( mApp.getBinFile( filename ) );
          if ( ! fp.exists() ) {
            TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "inexistent upload firmware file " + filename );
            return;    
          }
          int fw = readFirmwareFirmware( fp );
          int hw = mApp.readFirmwareHardware();
          // Log.v( "DistoX", "HW " + hw + " FW " + fw );
          // Toast.makeText( mParent, "HARDWARE " + hw, Toast.LENGTH_LONG ).show();

          askUpload( filename, areCompatible(hw,fw) );
        }
        break;
    }
  }

  static final byte[] signature = {
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x03, (byte)0xf0, (byte)0x34, (byte)0xf8,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xf5, (byte)0x08, (byte)0x00, (byte)0x08,
    (byte)0x40, (byte)0x0c, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
    (byte)0x01, (byte)0x23, (byte)0x00, (byte)0x22, (byte)0xc0, (byte)0x46, (byte)0xf0, (byte)0xb5,
    (byte)0xdb, (byte)0x07, (byte)0x27, (byte)0x4e, (byte)0x00, (byte)0xf0, (byte)0x3b, (byte)0xf8,
    (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b, (byte)0x25, (byte)0x4e, (byte)0x00, (byte)0xf0,
    (byte)0x35, (byte)0xf8, (byte)0x00, (byte)0xf0, (byte)0x34, (byte)0xf8, (byte)0x24, (byte)0x4e,
    (byte)0x00, (byte)0xf0, (byte)0x30, (byte)0xf8, (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b
  };
  //                                    2.1    2.2    2.3
  // signatures differ in bytes 6-7    f834   f83a   f990
  //                           16-17   0c40   0c40   0c50

  private int readFirmwareFirmware( File fp )
  {
    try {
      FileInputStream fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      if ( dis.skipBytes( 2048 ) != 2048 ) {
        // Log.v("DistoX", "failed skip");
        return 0; // skip 8 bootloader blocks
      }
      byte[] buf = new byte[64];
      if ( dis.read( buf, 0, 64 ) != 64 ) {
        // Log.v("DistoX", "failed read");
        return 0;
      }
      for ( int k=0; k<64; ++k ) {
        // Log.v("DistoX", "byte " + k + " " + buf[k] + " sign " + signature[k] );
        if ( k==6 || k==7 || k==16 || k==17 ) continue;
        if ( buf[k] != signature[k] ) return 0;
      }
      if ( buf[7] == (byte)0xf8 ) {
        if ( buf[6] == (byte)0x34 ) {
          return 21;
        } else if ( buf[6] == (byte)0x3a ) {
          return 22;
        }
      } else if ( buf[7] == (byte)0xf9 ) {
        if ( buf[6] == (byte)0x90 ) {
          return 23;
        }
      }
    } catch ( IOException e ) {
    }
    return 0;
  }

  private boolean areCompatible( int hw, int fw )
  {
    switch ( hw ) {
      case 10:
        return fw == 21 || fw == 22 || fw == 23;
    }
    // default:
    return false;
  }
    

  void askUpload( final String filename, final boolean compatible )
  {
    String title = mParent.getResources().getString( compatible? R.string.ask_upload : R.string.ask_upload_not_compatible );
    new TopoDroidAlertDialog( mContext, mParent.getResources(), title,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          int ret = mApp.uploadFirmware( filename );
          Toast.makeText( mParent, 
            String.format( mParent.getResources().getString(R.string.firmware_file_uploaded), filename, ret ),
            Toast.LENGTH_SHORT).show();
          // finish(); 
        }
      }
    );
  }

}
