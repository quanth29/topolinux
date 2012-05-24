/* @file TopoDroidActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid main class: survey/calib list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created from DistoX.java
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;
import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

import android.graphics.Color;
import android.graphics.PorterDuff;

/*
  Method m = device.getClass().getMethod( "createRfcommSocket", new Class[] (int.class) );
  socket = (BluetoothSocket) m.invoke( device, 2 );
  socket.connect();
*/

public class TopoDroidActivity extends Activity
                               implements OnItemClickListener
                               , View.OnClickListener
{
  private static final String TAG = "DistoX";
  private TopoDroidApp app;

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  // statuses
  private static final int STATUS_NONE   = 0;
  private static final int STATUS_SURVEY = 1;
  private static final int STATUS_CALIB  = 2;

  private int mStatus    = STATUS_SURVEY;
  private int mOldStatus = STATUS_SURVEY;

  private TextView mSplash;
  private ListView mList;
  private Button   mBtnSurveys;
  private Button   mBtnCalibs;

  private boolean mNeedUpdate = true;
  private ArrayAdapter<String> mArrayAdapter;

  private MenuItem mMIdevice = null;
  private MenuItem mMInew;
  private MenuItem mMIimport;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;
  private MenuItem mMIabout;

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_no_calib  = true;
  private boolean say_not_enabled = true;
  boolean do_check_bt = true;

  // -------------------------------------------------------------------
    
  public void updateDisplay( )
  {
    // Log.v( TAG, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    // mArrayAdapter.clear();
    DistoXDataHelper data = app.mData;
    switch ( mStatus ) {
        case STATUS_NONE:
          break;
        case STATUS_SURVEY:
          if ( data != null ) {
            List<String> list = data.selectAllSurveys();
            // list.add( "new_survey" );
            // setTitle( R.string.title_survey );
            mBtnSurveys.getBackground().setColorFilter( Color.parseColor( "#ccccff" ),
                                                        PorterDuff.Mode.LIGHTEN );
            mBtnCalibs.getBackground().setColorFilter( Color.parseColor( "#cccccc" ),
                                                       PorterDuff.Mode.DARKEN );
            updateList( list );
            if ( say_no_survey && list.size() == 0 ) {
              say_no_survey = false;
              Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
            } 
          }
          break;
        case STATUS_CALIB:
          if ( data != null ) {
            List<String> list = data.selectAllCalibs();
            // list.add( "new_calib" );
            // setTitle( R.string.title_calib );
            mBtnSurveys.getBackground().setColorFilter( Color.parseColor( "#cccccc" ),
                                                        PorterDuff.Mode.DARKEN );
            mBtnCalibs.getBackground().setColorFilter( Color.parseColor( "#ccccff" ),
                                                       PorterDuff.Mode.LIGHTEN );
            updateList( list );
            if ( say_no_calib && list.size() == 0 ) {
              say_no_calib = false;
              Toast.makeText( this, R.string.no_calib, Toast.LENGTH_LONG ).show();
            } 
          }
          break;
    }
  }

  private void updateList( List<String> list )
  {
    // Log.v(TAG, "updateList" );
    mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( list.size() > 0 ) {
      for ( String item : list ) {
        mArrayAdapter.add( item );
      }
    }
  }

  // ---------------------------------------------------------------
  // list items click


    public void onClick(View view)
    { 
      int status = mStatus;
      switch (view.getId()){
        case R.id.btn_surveys:
          mStatus = STATUS_SURVEY;
          break;
        case R.id.btn_calibs:
          mStatus = STATUS_CALIB;
          break;
        default:
      }
      // Log.v( TAG, "onClick() status " + mStatus );
      if ( status != mStatus ) {
        updateDisplay( );
      }
    }
            

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    switch ( mStatus ) {
      case STATUS_SURVEY:
        app.setSurveyFromName( value );
        // TODO start SurveyActivity
        Intent surveyIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, SurveyActivity.class );
        startActivity( surveyIntent );
        break;
      case STATUS_CALIB:
        app.setCalibFromName( value );
        // TODO start CalibActivity
        Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
        startActivity( calibIntent );
        break;
    }
  }

  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );
    mNeedUpdate = true;

    mMIdevice  = menu.add( R.string.menu_device );
    mMInew     = menu.add( R.string.menu_new );
    mMIimport  = menu.add( R.string.menu_import );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );
    mMIabout   = menu.add( R.string.menu_about );

    // mSMdevice.setIcon( R.drawable.distox ); 
    mMIdevice.setIcon( R.drawable.distox ); 
    mMInew.setIcon( R.drawable.add );
    mMIimport.setIcon( R.drawable.insert );
    mMIoptions.setIcon( R.drawable.prefs );
    mMIhelp.setIcon( R.drawable.help );
    mMIabout.setIcon( R.drawable.info );

    setBTMenus( app.mBTAdapter.isEnabled() );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // Log.v( TAG, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIabout ) { // ABOUT DIALOG
      TopoDroidAbout.show( this );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      TopoDroidHelp.show( this, R.string.help_topodroid );
    } else if ( item == mMInew ) { // NEW SURVEY/CALIB
      if ( mStatus == STATUS_SURVEY ) {
        app.setSurveyFromName( null );
        Intent surveyIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, SurveyActivity.class );
        startActivity( surveyIntent );
      } else {
        app.setCalibFromName( null );
        Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
        startActivity( calibIntent );
      }
    } else if ( item == mMIimport ) { // IMPORT SURVEY/CALIB
      if ( mStatus == STATUS_SURVEY ) {
        // TODO import survey
      } else {
        // TODO import calib
      }
    } else if ( item == mMIdevice ) { // DEVICE
      if ( app.mBTAdapter.isEnabled() ) {
        mNeedUpdate = true;
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      }
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.td_main);
    app = (TopoDroidApp) getApplication();
    mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );

    mList = (ListView) findViewById(R.id.td_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnSurveys = (Button) findViewById( R.id.btn_surveys );
    mBtnCalibs  = (Button) findViewById( R.id.btn_calibs );

    mBtnSurveys.setOnClickListener( this );
    mBtnCalibs.setOnClickListener( this );

    // if ( savedInstanceState == null) {
    //   Log.v(TAG, "onCreate null savedInstanceState" );
    // } else {
    //   Bundle map = savedInstanceState.getBundle(DISTOX_KEY);
    //   restoreInstanceState( map );
    // }
    // restoreInstanceFromFile();
    restoreInstanceFromData();
    if ( app.getSurveyId() < 0 ) {
      TopoDroidAbout.show( this );
    }
    // setTitleColor( 0x006d6df6 );
  }

  // private void restoreInstanceState(Bundle map )
  // {
  //   if ( map != null ) {
  //     Log.v( TAG, "onRestoreInstanceState non-null bundle");
  //     mStatus        = map.getInt( DISTOX_KEY_STATUS );
  //     mOldStatus     = map.getInt( DISTOX_KEY_OLD_STATUS );
  //     mSplay         = map.getBoolean( DISTOX_KEY_SPLAY );
  //     mLeg           = map.getBoolean( DISTOX_KEY_CENTERLINE );
  //     mBlank         = map.getBoolean( DISTOX_KEY_BLANK );
  //     String survey  = map.getString( DISTOX_KEY_SURVEY );
  //     String calib   = map.getString( DISTOX_KEY_CALIB );
  //     if ( survey != null ) setSurveyFromName( survey );
  //     if ( calib  != null ) setCalibFromName( calib );
  //   } else {
  //     Log.v( TAG, "onRestoreInstanceState null bundle");
  //     // mStatus ??
  //   }
  // }

  private void restoreInstanceFromData()
  { 
    // Log.v( TAG, "restoreInstanceFromData ");
    DistoXDataHelper data = app.mData;
    String status = data.getValue( "DISTOX_STATUS" );
    // Log.v( TAG, "restore STATUS " + status );
    if ( status != null ) {
      String[] vals = status.split( " " );
      // FIXME
    }
     
    String survey = data.getValue( "DISTOX_SURVEY" );
    // Log.v( TAG, "restore SURVEY >" + survey + "<" );
    if ( survey != null && survey.length() > 0 ) {
      app.setSurveyFromName( survey );
    } else {
      app.setSurveyFromName( null );
    }
    String calib = data.getValue( "DISTOX_CALIB" );
    // Log.v( TAG, "restore CALIB >" + calib + "<" );
    if ( calib != null && calib.length() > 0 ) {
      app.setCalibFromName( calib );
    } else {
      app.setCalibFromName( null );
    }
  }
    
  private void saveInstanceToData()
  {
    // Log.v(TAG, "saveInstanceToData");
    DistoXDataHelper data = app.mData;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d ", mStatus );
    // Log.v( TAG, "save STATUS " + sw.getBuffer().toString() );
    // Log.v( TAG, "save SURVEY >" + app.getSurvey() + "<" );
    // Log.v( TAG, "save CALIB >" + app.getCalib() + "<" );
    data.setValue( "DISTOX_STATUS", sw.getBuffer().toString() );
    data.setValue( "DISTOX_SURVEY", (app.getSurvey() == null)? "" : app.getSurvey() );
    data.setValue( "DISTOX_CALIB", (app.getCalib() == null)? "" : app.getCalib() );
  }


  // @Override
  // public void onSaveInstanceState(Bundle outState) 
  // {
  //   Log.v( TAG, "onSaveInstanceState");
  //   // outState.putBundle(DISTOX_KEY, mList.saveState());
  //   outState.putInt(DISTOX_KEY_STATUS, mStatus );
  //   outState.putInt(DISTOX_KEY_OLD_STATUS, mOldStatus );
  //   outState.putBoolean(DISTOX_KEY_SPLAY, mSplay );
  //   outState.putBoolean(DISTOX_KEY_CENTERLINE, mLeg );
  //   outState.putBoolean(DISTOX_KEY_BLANK, mBlank );
  //   outState.putString(DISTOX_KEY_SURVEY, getSurvey() );
  //   outState.putString(DISTOX_KEY_CALIB, getCalib() );
  // }

  // ------------------------------------------------------------------
  // LIFECYCLE
  //
  // onCreate --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause --> onStop | drawing | --> onStart --> onResume
  //          --> onSaveInstanceState --> onPause [ off/on ] --> onResume
  //          --> onPause --> onStop --> onDestroy

  @Override
  public void onStart()
  {
    super.onStart();
    // restoreInstanceFromFile();
    // Log.v( TAG, "onStart check BT " + app.mCheckBT + " enabled " + app.mBTAdapter.isEnabled() );

    if ( do_check_bt ) {
      do_check_bt = false;
      if ( app.mCheckBT && ! app.mBTAdapter.isEnabled() ) {    
        Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
        startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
      } else {
        // nothing to do: scanBTDEvices(); is called by menu CONNECT
      }
      setBTMenus( app.mBTAdapter.isEnabled() );
    }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // Log.v( TAG, "onResume " );
    if ( app.mComm != null ) { app.mComm.resume(); }

    // restoreInstanceFromFile();

    // This is necessary: switching display off/on there is the call sequence
    //    [off] onSaveInstanceState
    //    [on]  onResume
    updateDisplay( );
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // Log.v( TAG, "onPause " );
    if ( app.mComm != null ) { app.mComm.suspend(); }
  }

  @Override
  public synchronized void onStop()
  { 
    super.onStop();
    // Log.v( TAG, "onStop " );
    // mSavedState = new Bundle();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // Log.v( TAG, "onDestroy " );
    // FIXME if ( app.mComm != null ) { app.mComm.interrupt(); }
    saveInstanceToData();
  }

  // ------------------------------------------------------------------

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
  }


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // Log.v( TAG, "onActivityResult() request " + mRequestName[request] + " result: " + result );
    DistoXDataHelper data = app.mData;
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_ENABLE_BT:
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDEvices() is called by menu CONNECT
        } else if ( say_not_enabled ) {
          say_not_enabled = false;
          Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_LONG).show();
          // finish();
        }
        // FIXME app.mBluetooth = ( result == Activity.RESULT_OK );
        setBTMenus( app.mBTAdapter.isEnabled() );
        updateDisplay( );
        break;

    }
    // Log.v( TAG, "onActivityResult() done " );
  }

}
