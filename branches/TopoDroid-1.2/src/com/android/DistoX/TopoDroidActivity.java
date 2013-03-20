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
 * 20120606 import survey (therion format)
 * 20120610 import zip (unarchive)
 * 20120619 added "long-press" for immediate survey opening
 * 20121211 thconfig-manager and symbol-manager menus
 * 20121212 AsyncTask to import therion files
 * 20130307 made Annotations into a dialog
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

import android.os.AsyncTask;

// import java.lang.Long;
// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;
import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
                               , OnItemLongClickListener
                               , View.OnClickListener
                               , OnCancelListener
                               , OnDismissListener
{
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

  private ArrayAdapter<String> mArrayAdapter;

  private MenuItem mMIdevice = null;
  private MenuItem mMInew;
  private MenuItem mMIimport;
  // private MenuItem mMIopen;
  private SubMenu  mSMmore;
  // private MenuItem mMIsymbol;
  private MenuItem mMIthconfig;
  private MenuItem mMIoptions;
  private MenuItem mMIlogs;
  private MenuItem mMIhelp;
  private MenuItem mMIabout;

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_no_calib  = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  boolean do_check_bt = true;             // one-time bluetooth check sentinel

  // -------------------------------------------------------------------
    
  public void updateDisplay( )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    // mArrayAdapter.clear();
    DataHelper data = app.mData;
    switch ( mStatus ) {
        case STATUS_NONE:
          break;
        case STATUS_SURVEY:
          if ( data != null ) {
            List<String> list = data.selectAllSurveys();
            // list.add( "new_survey" );
            // setTitle( R.string.title_survey );
            setButtonsBackgroud( mBtnCalibs, mBtnSurveys );
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
            setButtonsBackgroud( mBtnSurveys, mBtnCalibs );
            updateList( list );
            if ( say_no_calib && list.size() == 0 ) {
              say_no_calib = false;
              Toast.makeText( this, R.string.no_calib, Toast.LENGTH_LONG ).show();
            } 
          }
          break;
    }
  }

  private void setButtonsBackgroud( Button grey, Button blue )
  {
    grey.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
    blue.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
  }

  private void updateList( List<String> list )
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "updateList" );
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
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onClick() " + view.toString() );
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
      // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onClick() status " + mStatus );
      if ( status != mStatus ) {
        updateDisplay( );
      }
    }

    private void startSurvey( String value, int mustOpen )
    {
      app.setSurveyFromName( value );
      Intent surveyIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, SurveyActivity.class );
      surveyIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen );
      startActivity( surveyIntent );
    }

    private void startCalib( String value, int mustOpen )
    {
      app.setCalibFromName( value );
      Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
      calibIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen ); // FIXME not handled yet
      startActivity( calibIntent );
    }
            
  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onItemLongClick() " + item.toString() );
    switch ( mStatus ) {
      case STATUS_SURVEY:
        startSurvey( item.toString(), 1 );
        return true;
      case STATUS_CALIB:
        startCalib( item.toString(), 1 );
        return true;
    }
    return false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onItemClick() " + item.toString() );
    switch ( mStatus ) {
      case STATUS_SURVEY:
        startSurvey( item.toString(), 0 );
        break;
      case STATUS_CALIB:
        startCalib( item.toString(), 0 );
        break;
    }
  }

  // void handleSurveyActivity( int result )
  // {
  // }

  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIdevice  = menu.add( R.string.menu_device );
    mMInew     = menu.add( R.string.menu_new );
    // mMIopen    = menu.add( R.string.menu_open);
    mMIimport  = menu.add( R.string.menu_import );
    mSMmore    = menu.addSubMenu( R.string.menu_more );
      mMIthconfig = mSMmore.add( R.string.menu_thconfig );
      // mMIsymbol   = mSMmore.add( R.string.menu_symbol );
      mMIoptions = mSMmore.add( R.string.menu_options );
      mMIlogs    = mSMmore.add( R.string.menu_logs );
      mMIhelp    = mSMmore.add( R.string.menu_help  );
      mMIabout   = mSMmore.add( R.string.menu_about );

    // mSMdevice.setIcon( R.drawable.distox ); 
    mMIdevice.setIcon( R.drawable.distox ); 
    mMInew.setIcon( R.drawable.add );
    mMIimport.setIcon( R.drawable.insert );
    // mMIopen.setIcon( R.drawable.open );
    mSMmore.setIcon( R.drawable.more );
    // mMIthconfig.setIcon( R.drawable.therion );
    // mMIsymbol.setIcon( R.drawable.symbol );
    // mMIoptions.setIcon( R.drawable.prefs );
    // mMIlogs.setIcon( R.drawable.prefs );
    // mMIhelp.setIcon( R.drawable.help );
    // mMIabout.setIcon( R.drawable.info );

    setBTMenus( app.mBTAdapter.isEnabled() );
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIthconfig ) { 
      Intent intent = new Intent( "ThManager.intent.action.Launch" );
      // intent.putExtra( "survey", app.getSurveyThFile() );
      try {
        startActivity( intent );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, R.string.no_thmanager, Toast.LENGTH_LONG ).show();
      }
    // } else if ( item == mMIsymbol ) { 
    //   Intent intent = new Intent( "TdSymbol.intent.action.Launch" );
    //   try {
    //     startActivity( intent );
    //     DrawingBrushPaths.reloadSymbols(); // force reloading paths
    //   } catch ( ActivityNotFoundException e ) {
    //     Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_LONG ).show();
    //   }
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
      startActivity( optionsIntent );
    } else if ( item == mMIlogs ) { // LOG OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
      startActivity( optionsIntent );
    } else if ( item == mMIabout ) { // ABOUT DIALOG
      (new TopoDroidAbout( this )).show();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      // TopoDroidHelp.show( this, R.string.help_topodroid );
      Intent pdf = new Intent( Intent.ACTION_VIEW, Uri.parse( TopoDroidApp.mManual ) );
      startActivity( pdf );
    } else if ( item == mMInew ) { // NEW SURVEY/CALIB
      if ( mStatus == STATUS_SURVEY ) {
        startSurvey( null, 0 );
      } else {
        startCalib( null, 0 );
      }
    // } else if ( item == mMIopen ) { // OPEN LAST SURVEY/CALIB
    //   if ( mStatus == STATUS_SURVEY ) {
    //     startSurvey( ?, 1 );
    //   } else {
    //     // TODO
    //   }
    } else if ( item == mMIimport ) { // IMPORT SURVEY/CALIB
      if ( mStatus == STATUS_SURVEY ) {
        (new ImportDialog( this, this, app )).show();
      } else {
        // TODO import calib
        Toast.makeText( this, R.string.not_implemented, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIdevice ) { // DEVICE
      if ( app.mBTAdapter.isEnabled() ) {
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      }
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private class ImportTask extends AsyncTask<String , Integer, Long >
  {
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        TherionParser parser = new TherionParser( str[0] );
        ArrayList< TherionParser.Shot > shots  = parser.getShots();
        ArrayList< TherionParser.Shot > splays = parser.getSplays();

        sid = app.setSurveyFromName( str[1] );
        String date = parser.mDate;
        String title = parser.mTitle;
        app.mData.updateSurveyDayAndComment( sid, date, title );
        long id = app.mData.insertShots( sid, 1, shots ); // start id = 1
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    protected void onProgressUpdate(Integer... progress) { }

    protected void onPostExecute(Long result) {
      updateDisplay( );
    }
  }

  void importFile( String filename )
  {
    if ( filename.endsWith(".th") ) {
      String filepath = TopoDroidApp.getImportFile( filename );
      String name = filename.replace(".th", "" );
      if ( app.mData.hasSurveyName( name ) ) {
        Toast.makeText(this, R.string.file_parse_already, Toast.LENGTH_SHORT).show();
        return;
      }
      Toast.makeText(this, R.string.file_import, Toast.LENGTH_LONG).show();
      new ImportTask() .execute( filepath, name );
      // try {
      //   TherionParser parser = new TherionParser( filepath );
      //   ArrayList< TherionParser.Shot > shots  = parser.getShots();
      //   ArrayList< TherionParser.Shot > splays = parser.getSplays();

      //   long sid = app.setSurveyFromName( name );
      //   String date = parser.mDate;
      //   app.mData.updateSurveyDayAndComment( sid, date, "" );
      //   long id = app.mData.insertShots( sid, 1, shots ); // start id = 1
      //   updateDisplay( );
      //   Toast.makeText(this, R.string.file_parse_ok, Toast.LENGTH_SHORT).show();
      // } catch ( ParserException e ) {
      //   Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      // }
    } else if ( filename.endsWith(".zip") ) {
      Archiver archiver = new Archiver( app );
      int ret = archiver.unArchive( TopoDroidApp.getZipFile( filename ), filename.replace(".zip", ""));
      if ( ret == -2 ) {
        Toast.makeText(this, R.string.unzip_fail, Toast.LENGTH_SHORT).show();
      } else if ( ret == -1 ) {
        Toast.makeText(this, R.string.file_parse_already, Toast.LENGTH_SHORT).show();
      } else {
        updateDisplay( );
      }
    }
  }
  
  // ---------------------------------------------------------------

  TopoDroidAbout mTopoDroidAbout = null;
  TdSymbolDialog mTdSymbolDialog = null;
  
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
    mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnSurveys = (Button) findViewById( R.id.btn_surveys );
    mBtnCalibs  = (Button) findViewById( R.id.btn_calibs );

    mBtnSurveys.setOnClickListener( this );
    mBtnCalibs.setOnClickListener( this );

    // if ( savedInstanceState == null) {
    //   TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "onCreate null savedInstanceState" );
    // } else {
    //   Bundle map = savedInstanceState.getBundle(DISTOX_KEY);
    //   restoreInstanceState( map );
    // }
    // restoreInstanceFromFile();
    restoreInstanceFromData();
    if ( app.mWelcomeScreen ) {
      app.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
      app.mWelcomeScreen = false;
      mTopoDroidAbout = new TopoDroidAbout( this );
      mTopoDroidAbout.setOnCancelListener( this );
      mTopoDroidAbout.setOnDismissListener( this );
      mTopoDroidAbout.show();
    } else if ( app.mTdSymbol ) {
      startTdSymbolDialog();
    } else {
    }
    DrawingBrushPaths.doMakePaths( );

    // setTitleColor( 0x006d6df6 );
  }

  @Override
  public void onDismiss( DialogInterface d )
  { 
    if ( d == (Dialog)mTdSymbolDialog ) {
      if ( app.mStartTdSymbol ) {
        Intent intent = new Intent( "TdSymbol.intent.action.Launch" );
        try {
          startActivity( intent );
          // DrawingBrushPaths.reloadSymbols(); // force reloading paths
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_LONG ).show();
        }
      }
      mTdSymbolDialog = null;
    } else if ( d == (Dialog)mTopoDroidAbout ) {
      startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  @Override
  public void onCancel( DialogInterface d )
  {
    if ( d == (Dialog)mTopoDroidAbout ) {
      startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  private void startTdSymbolDialog()
  {
    mTdSymbolDialog = new TdSymbolDialog( this, app );
    // mTdSymbolDialog.setOnCancelListener( this );
    mTdSymbolDialog.setOnDismissListener( this );
    mTdSymbolDialog.show();
  }

  // private void restoreInstanceState(Bundle map )
  // {
  //   if ( map != null ) {
  //     TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onRestoreInstanceState non-null bundle");
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
  //     TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onRestoreInstanceState null bundle");
  //     // mStatus ??
  //   }
  // }

  private void restoreInstanceFromData()
  { 
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restoreInstanceFromData ");
    DataHelper data = app.mData;
    String status = data.getValue( "DISTOX_STATUS" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore STATUS " + status );
    if ( status != null ) {
      String[] vals = status.split( " " );
      // FIXME
    }
     
    String survey = data.getValue( "DISTOX_SURVEY" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore SURVEY >" + survey + "<" );
    if ( survey != null && survey.length() > 0 ) {
      app.setSurveyFromName( survey );
    } else {
      app.setSurveyFromName( null );
    }
    String calib = data.getValue( "DISTOX_CALIB" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore CALIB >" + calib + "<" );
    if ( calib != null && calib.length() > 0 ) {
      app.setCalibFromName( calib );
    } else {
      app.setCalibFromName( null );
    }
  }
    
  private void saveInstanceToData()
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "saveInstanceToData");
    DataHelper data = app.mData;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d ", mStatus );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save STATUS " + sw.getBuffer().toString() );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save SURVEY >" + app.getSurvey() + "<" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save CALIB >" + app.getCalib() + "<" );
    data.setValue( "DISTOX_STATUS", sw.getBuffer().toString() );
    data.setValue( "DISTOX_SURVEY", (app.getSurvey() == null)? "" : app.getSurvey() );
    data.setValue( "DISTOX_CALIB", (app.getCalib() == null)? "" : app.getCalib() );
  }


  // @Override
  // public void onSaveInstanceState(Bundle outState) 
  // {
  //   TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onSaveInstanceState");
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onStart check BT " + app.mCheckBT + " enabled " + app.mBTAdapter.isEnabled() );

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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onResume " );
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onPause " );
    if ( app.mComm != null ) { app.mComm.suspend(); }
  }

  @Override
  public synchronized void onStop()
  { 
    super.onStop();
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onStop " );
    // mSavedState = new Bundle();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onDestroy " );
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onActivityResult() request " + mRequestName[request] + " result: " + result );
    DataHelper data = app.mData;
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onActivityResult() done " );
  }


  // =====================================================================
/*
  private void doSaveSurvey()
  {
    saveSurvey( );
    // setMenus();
    setButtons();
  }

  private void doOpenSurvey()
  {
    dismiss();
    Intent openIntent = new Intent( mContext, ShotActivity.class );
    mContext.startActivity( openIntent );
  }

  private void doLocation()
  {
    LocationManager lm = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
    DistoXLocation loc = new DistoXLocation( mContext, this, lm );
    loc.show();
  }

  private void doNotes()
  {
    if ( app.getSurvey() != null ) {
      (new DistoXAnnotations( this, app.getSurvey() )).show();
      // Intent notesIntent = new Intent( mContext, DistoXAnnotations.class );
      // notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
      // mContext.startActivity( notesIntent );
    } else { // SHOULD NEVER HAPPEN
      Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_LONG ).show();
    }
  }


  // ---------------------------------------------------------------
  // fixed stations
  //

  public void addFixed( String station, double latitude, double longitude, double altitude )
  {
    // mFixed.add( new FixedInfo( station, latitude, longitude, altitude ) );
    // NOTE info.id == app.mSID
    app.mData.insertFixed( station, info.id, longitude, latitude, altitude, "" ); // FIXME comment
  }

  // populate fixed from the DB
  // this is not ok must re-populate whenever the survey changes
  // public void restoreFixed()
  // {
  //   mFixed.clear(); // just to make sure ...
  //   List< FixedInfo > fixed = app.mData.selectAllFixed( mSID );
  //   for ( FixedInfo fix : fixed ) {
  //     mFixed.add( fix );
  //   }
  // }

  // ---------------------------------------------------------------

  private void saveSurvey( )
  {
    String name = mEditName.getText().toString();
    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();

    if ( isSaved ) { // survey already saved
      // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
      app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
      if ( team != null ) {
        app.mData.updateSurveyTeam( app.mSID, team );
      } 
    } else { // new survey
      if ( app.hasSurveyName( name ) ) { // name already exists
        Toast.makeText( mContext, R.string.survey_exists, Toast.LENGTH_LONG ).show();
      } else {
        app.setSurveyFromName( name );
        app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
        if ( team != null ) {
          app.mData.updateSurveyTeam( app.mSID, team );
        } 
        isSaved = true;
        setNameNotEditable();
      }
    }
  }
  
  private void doExport()
  {
    if ( app.getSurveyId() < 0 ) {
      Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_LONG ).show();
    } else {
      String filename = null;
      switch ( app.mExportType ) {
        case TopoDroidApp.DISTOX_EXPORT_TLX:
          filename = app.exportSurveyAsTlx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TH:
          filename = app.exportSurveyAsTh();
          break;
        case TopoDroidApp.DISTOX_EXPORT_DAT:
          filename = app.exportSurveyAsDat();
          break;
        case TopoDroidApp.DISTOX_EXPORT_SVX:
          filename = app.exportSurveyAsSvx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TRO:
          filename = app.exportSurveyAsTro();
          break;
      }
      if ( filename != null ) {
        Toast.makeText( mContext, mContext.getString(R.string.saving_) + filename, Toast.LENGTH_LONG ).show();
      } else {
        Toast.makeText( mContext, R.string.saving_file_failed, Toast.LENGTH_LONG ).show();
      }
    }
  }

  public void deleteSurvey()
  {
    if ( app.mSID < 0 ) return;
    app.mData.doDeleteSurvey( app.mSID );
    app.setSurveyFromName( null );
    // finish();
    dismiss();
  }
 
  public void addLocation( String station, double latitude, double longitude, double altitude )
  {
    // app.addFixed( station, latitude, longitude, altitude );
    addFixed( station, latitude, longitude, altitude );

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    DistoXAnnotations.append( app.getSurvey(), sw.getBuffer().toString() );
  }

*/

}
