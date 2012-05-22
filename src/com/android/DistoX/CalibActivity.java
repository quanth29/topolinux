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
 */
package com.android.DistoX;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

import android.util.Log;

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
// import android.view.View;
// import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;


public class CalibActivity extends Activity
                            // implements OnItemClickListener, ILister
                            // implements View.OnClickListener
{
  private static final String TAG = "DistoX";

  private EditText mEditName;
  private EditText mEditDate;
  private EditText mEditComment;

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
    mEditComment = (EditText) findViewById(R.id.calib_comment);

    app     = (TopoDroidApp)getApplication();
    // Log.v( TAG, "app mCID " + app.mCID );
    isSaved = ( app.mCID >= 0 );
    if ( isSaved ) {
      info = app.getCalibInfo();
      mEditName.setText( info.name );
      setNameNotEditable();
      // mEditName.setEditable( false );
      mEditDate.setText( info.date );
      if ( info.comment != null && info.comment.length() > 0 ) {
        mEditComment.setText( info.comment );
      } else {
        mEditComment.setHint( R.string.description );
      }
    } else {
      mEditName.setHint( R.string.name );
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mEditDate.setText( sdf.format( new Date() ) );
      mEditComment.setHint( R.string.description );
    }
  }


  // --------- menus ----------
  private MenuItem mMIsave;
  private MenuItem mMIopen;
  private MenuItem mMIexport;
  private MenuItem mMInotes;
  private MenuItem mMIdelete;

  private SubMenu  mSMmore;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsave    = menu.add( R.string.menu_save );
    mMIopen    = menu.add( R.string.menu_open );
    mMIdelete  = menu.add( R.string.menu_delete );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help );

    mMIsave    .setIcon( R.drawable.save );
    mMIopen    .setIcon( R.drawable.open );
    mMIdelete  .setIcon( R.drawable.delete );
    mMIoptions .setIcon( R.drawable.prefs );
    mMIhelp    .setIcon( R.drawable.help );

    setMenus( );
    return true;
  }

  private void setMenus( )
  { 
    if ( mMIopen != null )   mMIopen.setEnabled( isSaved );
    if ( mMIdelete != null ) mMIdelete.setEnabled( isSaved );
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // Log.v( TAG, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIsave ) {
      saveCalib( );
      setMenus();
    } else if ( item == mMIopen ) {
      Intent openIntent = new Intent( this, GMActivity.class );
      startActivity( openIntent );
    } else if ( item == mMIdelete ) {
      // TODO ...
      // deleteCalib();
      Toast.makeText( this, R.string.calib_delete, Toast.LENGTH_LONG ).show();
    } else if ( item == mMIoptions ) {  
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIhelp ) {  
      TopoDroidHelp.show( this, R.string.help_calib );
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  // ---------------------------------------------------------------

  private void saveCalib( )
  {
    String name = mEditName.getText().toString();
    String date = mEditDate.getText().toString();
    String comment = mEditComment.getText().toString();

    if ( isSaved ) { // calib already saved
      // Log.v( TAG, "INSERT calib id " + id + " date " + date + " name " + name + " comment " + comment );
      app.mData.updateCalibDayAndComment( app.mCID, date, comment );
    } else { // new calib
      if ( app.hasCalibName( name ) ) { // name already exists
        Toast.makeText( this, R.string.calib_exists, Toast.LENGTH_LONG ).show();
      } else {
        app.setCalibFromName( name );
        app.mData.updateCalibDayAndComment( app.mCID, date, comment );
        isSaved = true;
        setNameNotEditable();
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

  public void deleteCalib()
  {
    if ( app.mCID < 0 ) return;
    app.mData.doDeleteCalib( app.mCID );
    app.setCalibFromName( null );
    finish();
  }
}

