/** @file DeviceA3MemoryDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX A3 device memory dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201312   created to distinguish from X310 memory dialog
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


class DeviceA3MemoryDialog extends Dialog
                           implements View.OnClickListener
{
  private Button mBtnStore;
  private Button mBtnReset;
  private Button mBtnDump;
  private Button mBtnRead;
  // private Button mBtnClose;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETdumpfrom;
  private EditText mETdumpto;
  private TextView mTVshead;
  private TextView mTVstail;
  private TextView mTVrhead;
  private TextView mTVrtail;

  DeviceActivity mParent;

  DeviceA3MemoryDialog( Context context, DeviceActivity parent )
  {
    super( context );
    mParent = parent;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_a3_memory_dialog );

    mTVshead = (TextView) findViewById( R.id.tv_stored_head );
    mTVstail = (TextView) findViewById( R.id.tv_stored_tail );
    mTVrhead = (TextView) findViewById( R.id.tv_read_head );
    mTVrtail = (TextView) findViewById( R.id.tv_read_tail );
    mETfrom  = (EditText) findViewById( R.id.et_from );
    mETto    = (EditText) findViewById( R.id.et_to );
    mETdumpfrom  = (EditText) findViewById( R.id.et_dumpfrom );
    mETdumpto    = (EditText) findViewById( R.id.et_dumpto );
    mBtnStore = (Button) findViewById(R.id.button_store);
    mBtnRead  = (Button) findViewById(R.id.button_read );
    mBtnDump  = (Button) findViewById(R.id.button_dump );
    mBtnReset = (Button) findViewById(R.id.button_reset);
    // mBtnClose = (Button) findViewById(R.id.button_close);
    mBtnStore.setOnClickListener( this );
    mBtnReset.setOnClickListener( this );
    mBtnRead.setOnClickListener( this );
    mBtnDump.setOnClickListener( this );
    // mBtnClose.setOnClickListener( this );
    
    setTitle("DistoX A3 Memory");

    int[] ht = new int[2];
    mParent.retrieveDeviceHeadTail( ht );
    setText( mTVshead, mTVstail, ht );
  }

  private void setText( TextView h, TextView t, int[] ht )
  {
    StringWriter swh = new StringWriter();
    PrintWriter  pwh = new PrintWriter( swh );
    StringWriter swt = new StringWriter();
    PrintWriter  pwt = new PrintWriter( swt );
    pwh.printf("%04d", ht[0] / 8 );
    pwt.printf("%04d", ht[1] / 8);
    h.setText( swh.getBuffer().toString() );
    t.setText( swt.getBuffer().toString() );
  }

  @Override
  public void onClick( View view )
  {
    int[] ht = new int[2];
    switch ( view.getId() ) {
      case R.id.button_store:
        ht[0] = Integer.parseInt( mTVrhead.getText().toString() ) * 8;
        ht[1] = Integer.parseInt( mTVrtail.getText().toString() ) * 8;
        mParent.storeDeviceHeadTail( ht );
        mTVshead.setText( mTVrhead.getText() );
        mTVstail.setText( mTVrtail.getText() );
        break;
      case R.id.button_dump:
        ht[0] = Integer.parseInt( mETdumpfrom.getText().toString() ) * 8;
        ht[1] = Integer.parseInt( mETdumpto.getText().toString() ) * 8;
        mParent.readA3Memory( ht );
        break;
      case R.id.button_read:
        mParent.readDeviceHeadTail( ht );
        setText( mTVrhead, mTVrtail, ht );
        mETfrom.setText( mTVstail.getText() );
        mETto.setText( mTVrtail.getText() );
        break;
      case R.id.button_reset:
        ht[0] = Integer.parseInt( mETfrom.getText().toString() ) * 8;
        ht[1] = Integer.parseInt( mETto.getText().toString() ) * 8;
        askReset( ht );
        // mParent.resetA3DeviceHeadTail( ht );
        break;
      // case R.id.button_close:
      //   dismiss();
      //   break;
    }
  }

  void askReset( final int ht[] )
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( mParent );
    // alert.setTitle( R.string.delete );
    alert.setMessage( mParent.getResources().getString( R.string.ask_reset ) );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          mParent.resetA3DeviceHeadTail( ht );
          // finish(); 
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
  }


}
