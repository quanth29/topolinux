/* @file CalibActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 * 20120524 added device in CalibInfo
 * 20120531 activated doDelete with askDelete first
 * 20120725 TopoDroidApp log
 * 20121124 calibration-device consistency check
 */
package com.android.DistoX;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.app.Activity;
// import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import android.app.Application;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;


public class CalibActivity extends Activity
                           // implements OnItemClickListener, ILister
                           implements View.OnClickListener
{
  private EditText mEditName;
  private EditText mEditDate;
  private EditText mEditDevice;
  private EditText mEditComment;

  private Button mBTsave;
  private Button mBTNopen;
  // private Button mBTNexport;
  private Button mBTNdelete;

  private TopoDroidApp app;
  private CalibInfo info;
  private boolean isSaved;

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_calib_dialog);
    mEditName    = (EditText) findViewById(R.id.calib_name);
    mEditDate    = (EditText) findViewById(R.id.calib_date);
    mEditDevice  = (EditText) findViewById(R.id.calib_device);
    mEditComment = (EditText) findViewById(R.id.calib_comment);

    mBTsave      = (Button) findViewById( R.id.calibSave );
    mBTNopen     = (Button) findViewById( R.id.calibOpen );
    // mBTNexport   = (Button) findViewById( R.id.calibExport );
    mBTNdelete   = (Button) findViewById( R.id.calibDelete );

    app     = (TopoDroidApp)getApplication();
    // TopoDroidApp.Log( TopoDroidApp.LOG_CALIB, "app mCID " + app.mCID );
    isSaved = ( app.mCID >= 0 );
    if ( isSaved ) {
      info = app.getCalibInfo();
      mEditName.setText( info.name );
      setNameNotEditable();
      // mEditName.setEditable( false );
      mEditDate.setText( info.date );
      if ( info.device != null && info.device.length() > 0 ) {
        mEditDevice.setText( info.device );
      } else {
        mEditDevice.setText( app.mDevice );
      }
      if ( info.comment != null && info.comment.length() > 0 ) {
        mEditComment.setText( info.comment );
      } else {
        mEditComment.setHint( R.string.description );
      }
    } else {
      mEditName.setHint( R.string.name );
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mEditDate.setText( sdf.format( new Date() ) );
      mEditDevice.setText( app.mDevice );
      mEditComment.setHint( R.string.description );
    }
  }

  // --------- menus ----------
  // private MenuItem mMIsave;
  // private MenuItem mMIopen;
  // private MenuItem mMIexport;
  // private MenuItem mMInotes;
  // private MenuItem mMIdelete;

  // private SubMenu  mSMmore;
  // private MenuItem mMIoptions;
  // private MenuItem mMIhelp;

  // @Override
  // public boolean onCreateOptionsMenu(Menu menu) 
  // {
  //   super.onCreateOptionsMenu( menu );

  //   mMIsave    = menu.add( R.string.menu_save );
  //   mMIopen    = menu.add( R.string.menu_open );
  //   mMIdelete  = menu.add( R.string.menu_delete );
  //   mMIoptions = menu.add( R.string.menu_options );
  //   mMIhelp    = menu.add( R.string.menu_help );

  //   mMIsave    .setIcon( R.drawable.save );
  //   mMIopen    .setIcon( R.drawable.open );
  //   mMIdelete  .setIcon( R.drawable.delete );
  //   mMIoptions .setIcon( R.drawable.prefs );
  //   mMIhelp    .setIcon( R.drawable.help );

  //   setMenus( );
  //   return true;
  // }

  // private void setMenus( )
  // { 
  //   if ( mMIopen != null )   mMIopen.setEnabled( isSaved );
  //   if ( mMIdelete != null ) mMIdelete.setEnabled( isSaved );
  // }

  // @Override
  // public boolean onOptionsItemSelected(MenuItem item) 
  // {
  //   // Handle item selection
  //   if ( item == mMIsave ) {
  //     doSave( );
  //     setMenus();
  //   } else if ( item == mMIopen ) {
  //     Intent openIntent = new Intent( this, GMActivity.class );
  //     startActivity( openIntent );
  //   } else if ( item == mMIdelete ) {
  //     // TODO ...
  //     // doDelete();
  //     Toast.makeText( this, R.string.calib_deleted, Toast.LENGTH_LONG ).show();
  //   } else if ( item == mMIoptions ) {  
  //     doOpen();
  //   } else if ( item == mMIhelp ) {  
  //     TopoDroidHelp.show( this, R.string.help_calib );
  //   } else {
  //     return super.onOptionsItemSelected(item);
  //   }
  //   return true;
  // }
  // ---------------------------------------------------------------

  @Override
  public void onClick(View view)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "onClick(View) " + view.toString() );
    switch (view.getId()){
      case R.id.calibSave:
        doSave();
        break;
      case R.id.calibOpen:
        if ( ! app.checkCalibrationDeviceMatch() ) {
          // FIXME use alert dialog
          Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
        }
        doOpen();
        break;
      // case R.id.calibExport:
      //   doExport();
      //   break;
      case R.id.calibDelete:
        askDelete();
        break;
    }
  }

  private void askDelete()
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.calib_delete ) + " " + app.getCalib() + " ?" );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "calib delite" );
          doDelete();
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
  }

  private void doOpen()
  {
    Intent openIntent = new Intent( this, GMActivity.class );
    startActivity( openIntent );
  }

  private void doSave( )
  {
    String name = mEditName.getText().toString();
    String date = mEditDate.getText().toString();
    String device = mEditDevice.getText().toString();
    String comment = mEditComment.getText().toString();
    if ( date    != null ) { date    = date.trim(); }
    if ( device  != null ) { device  = device.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    if ( isSaved ) { // calib already saved
      app.mData.updateCalibInfo( app.mCID, date, device, comment );
    } else { // new calib
      name = TopoDroidApp.noSpaces( name );
      if ( name != null && name.length() > 0 ) {
        if ( app.hasCalibName( name ) ) { // name already exists
          Toast.makeText( this, R.string.calib_exists, Toast.LENGTH_LONG ).show();
        } else {
          app.setCalibFromName( name );
          app.mData.updateCalibInfo( app.mCID, date, device, comment );
          isSaved = true;
          setNameNotEditable();
        }
      }
    }
  }
  
  private void setNameNotEditable()
  {
    if ( isSaved ) {
      mEditName.setFocusable( false );
      mEditName.setClickable( false );
      mEditName.setKeyListener( null );
    }
  }

  public void doDelete()
  {
    if ( app.mCID < 0 ) return;
    app.mData.doDeleteCalib( app.mCID );
    app.setCalibFromName( null );
    finish();
  }
}

