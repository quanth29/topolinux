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
 * 20130910 startSurvey takes old sid/id to populate new survey
 * 20131201 button bar new interface. reorganized actions
 * 20140415 commented TdSymbol stuff
 * 20140416 menus: palette options help logs about
 * 20140526 removed oldSID and oldID from startSurvey 
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.FileInputStream;
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
// import android.content.res.ColorStateList;
import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.os.Parcelable;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

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

import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Color;
import android.graphics.PorterDuff;

// import android.util.Log;

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
  private TopoDroidApp mApp;

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

  private MenuItem mMIsymbol;
  private MenuItem mMIoptions;
  private MenuItem mMIlogs;
  private MenuItem mMImanual;
  private MenuItem mMIhelp;
  private MenuItem mMIabout;

  private ArrayAdapter<String> mArrayAdapter;

  private Button[] mButton1;
  private Button[] mButton2;
  private static int icons[] = { R.drawable.ic_disto,
                          R.drawable.ic_add,
                          R.drawable.ic_import,
                          R.drawable.ic_more,
                          R.drawable.ic_less,
                          R.drawable.ic_therion,
                          R.drawable.ic_database,
                          R.drawable.ic_symbol,
                          R.drawable.ic_pref,
                          R.drawable.ic_logs,
                          R.drawable.ic_info,
                          R.drawable.ic_help };
  private static int help_texts[] = { R.string.help_device,
                          R.string.help_add_topodroid,
                          R.string.help_import,
                          R.string.help_more,
                          R.string.help_less,
                          R.string.help_therion,
                          R.string.help_database,
                          R.string.help_symbol,
                          R.string.help_prefs,
                          R.string.help_log,
                          R.string.help_info_topodroid,
                          R.string.help_help };

  // -------------------------------------------------------------
  private boolean say_no_survey = true;
  private boolean say_no_calib  = true;
  private boolean say_not_enabled = true; // whether to say that BT is not enabled
  boolean do_check_bt = true;             // one-time bluetooth check sentinel

  // -------------------------------------------------------------------

  @Override
  public void onBackPressed () // askClose
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.ask_close ) );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          finish(); // doClose()
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
  }


    
  public void updateDisplay( )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    // mArrayAdapter.clear();
    DataHelper data = mApp.mData;
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
              Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
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
              Toast.makeText( this, R.string.no_calib, Toast.LENGTH_SHORT ).show();
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
      Intent intent;
      int status = mStatus;
      Button b = (Button)view;
      int k1 = 0;
      int k2 = 0;

      if ( b == mBtnSurveys ) {
        mStatus = STATUS_SURVEY;
      } else if ( b == mBtnCalibs ) {
        mStatus = STATUS_CALIB;
      } else if ( b == mButton1[k1++] ) { // mBtnDevice
        if ( mApp.mBTAdapter.isEnabled() ) {
          intent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
          startActivity( intent );
        }
      } else if ( b == mButton1[k1++] ) {  // mBtnNew
        if ( mStatus == STATUS_SURVEY ) {
          // startSurvey( null, 0, -1, -1 );
          mApp.setSurveyFromName( null );
          (new SurveyNewDialog( this, this, -1, -1 )).show(); // NO SPLIT
        } else {
          if ( mApp.mDevice != null ) {
            startCalib( null, 0 );
          } else {
            Toast.makeText( this, R.string.device_none, Toast.LENGTH_SHORT ).show();
          }
        }
      } else if ( b == mButton1[k1++] ) {  // mBtnImport
        if ( mStatus == STATUS_SURVEY ) {
          (new ImportDialog( this, this, mApp )).show();
        } else {
          // TODO import calib
          Toast.makeText( this, R.string.not_implemented, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[k1++] ) {  // more
        mListView.setAdapter( mButtonView2.mAdapter );
        mListView.invalidate();
      } else if ( b == mButton2[k2++] ) {  // less
        mListView.setAdapter( mButtonView1.mAdapter );
        mListView.invalidate();
      } else if ( b == mButton2[k2++] ) {  // mBtnThConfig
        try {
          intent = new Intent( "ThManager.intent.action.Launch" );
          // intent.putExtra( "survey", mApp.getSurveyThFile() );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_thmanager, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton2[k2++] ) {  // database
        try {
          intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + DataHelper.DATABASE_NAME ) );
          intent.addCategory("com.kokufu.intent.category.APP_DB_VIEWER");
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, "DB_viewer mApp not found", Toast.LENGTH_SHORT ).show();
        }
      // } else if ( b == mButton2[k2++] ) {  // mBtnSymbol
      //   DrawingBrushPaths.makePaths( getResources() );
      //   (new SymbolEnableDialog( this, this )).show();
      //   // intent = new Intent( "TdSymbol.intent.action.Launch" );
      //   // try {
      //   //   startActivity( intent );
      //   //   DrawingBrushPaths.mReloadSymbols = true;
      //   // } catch ( ActivityNotFoundException e ) {
      //   //   Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_SHORT ).show();
      //   // }
      // } else if ( b == mButton2[k2++] ) {  // mBtnOptions
      //   intent = new Intent( this, TopoDroidPreferences.class );
      //   intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
      //   startActivity( intent );
      // } else if ( b == mButton2[k2++] ) {  // mBtnLogs
      //   intent = new Intent( this, TopoDroidPreferences.class );
      //   intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
      //   startActivity( intent );
      // } else if ( b == mButton2[k2++] ) {  // mBtnAbout
      //   (new TopoDroidAbout( this )).show();
      // } else if ( b == mButtonHelp ) {  // TODO HELP
      //   (new HelpDialog(this, icons, help_texts ) ).show();
      }
      // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onClick() status " + mStatus );
      if ( status != mStatus ) {
        updateDisplay( );
      }
    }

  // splitSurvey invokes this method with args: null, 0, old_sid, old_id
  //
  void startSurvey( String value, int mustOpen ) // , long old_sid, long old_id )
  {
    mApp.setSurveyFromName( value );
    Intent surveyIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, SurveyActivity.class );
    surveyIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen );
    // surveyIntent.putExtra( TopoDroidApp.TOPODROID_OLDSID, old_sid );
    // surveyIntent.putExtra( TopoDroidApp.TOPODROID_OLDID,  old_id );
    startActivity( surveyIntent );
  }

  void startSplitSurvey( long old_sid, long old_id )
  {
    mApp.setSurveyFromName( null );
    (new SurveyNewDialog( this, this, old_sid, old_id )).show(); // WITH SPLIT
  }

  private void startCalib( String value, int mustOpen )
  {
    mApp.setCalibFromName( value );
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
        startSurvey( item.toString(), 0 ); // , -1, -1 );
        return true;
      case STATUS_CALIB:
        startCalib( item.toString(), 0 );
        return true;
    }
    return false;
  }

  void doOpenSurvey( String name )
  {
    mApp.setSurveyFromName( name );
    Intent openIntent = new Intent( this, ShotActivity.class );
    startActivity( openIntent );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onItemClick() " + item.toString() );
    switch ( mStatus ) {
      case STATUS_SURVEY:
        // startSurvey( item.toString(), 1); // , -1, -1 ); // start survey and open it
        mApp.setSurveyFromName( item.toString() );
        Intent openIntent = new Intent( this, ShotActivity.class );
        startActivity( openIntent );
        break;
      case STATUS_CALIB:
        startCalib( item.toString(), 1 );
        break;
    }
  }

  // void handleSurveyActivity( int result )
  // {
  // }

  
  private class ImportTherionTask extends AsyncTask<String , Integer, Long >
  {
    @Override
    protected Long doInBackground( String... str )
    {
      long sid = 0;
      try {
        TherionParser parser = new TherionParser( str[0], true ); // apply_declination = true
        ArrayList< ParserShot > shots  = parser.getShots();
        ArrayList< ParserShot > splays = parser.getSplays();

        sid = mApp.setSurveyFromName( str[1] );
        mApp.mData.updateSurveyDayAndComment( sid, parser.mDate, parser.mTitle );
        mApp.mData.updateSurveyDeclination( sid, parser.mDeclination );

        long id = mApp.mData.insertShots( sid, 1, shots ); // start id = 1
      } catch ( ParserException e ) {
        // Toast.makeText(this, R.string.file_parse_fail, Toast.LENGTH_SHORT).show();
      }
      return sid;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      setTitle( R.string.app_name );
      setTitleColor( TopoDroidApp.COLOR_NORMAL );
      updateDisplay( );
    }
  }
  
  private class ImportZipTask extends AsyncTask<String , Integer, Long >
  {
    TopoDroidActivity activity;

    ImportZipTask( TopoDroidActivity act ) { activity = act; }

    @Override
    protected Long doInBackground( String... str )
    {
      String filename = str[0];
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "ImportZipTask doInBackground " + filename );
      Archiver archiver = new Archiver( mApp );
      int ret = archiver.unArchive( TopoDroidApp.getZipFile( filename ), filename.replace(".zip", ""));
      return (long)ret;
    }

    // @Override
    // protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute(Long result) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "ImportZipTask onPostExecute " + result );
      activity.setTitle( R.string.app_name );
      activity.setTitleColor( TopoDroidApp.COLOR_NORMAL );
      activity.updateDisplay( );
      if ( result == -2 ) {
        Toast.makeText( activity, R.string.unzip_fail, Toast.LENGTH_SHORT).show();
      } else if ( result == -1 ) {
        Toast.makeText( activity, R.string.import_already, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText( activity, R.string.import_zip_ok, Toast.LENGTH_SHORT).show();
      }
    }
  }

  void importFile( String filename )
  {
    int PT_SCALE = 40;
    setTitle( R.string.import_title );
    setTitleColor( TopoDroidApp.COLOR_CONNECTED );
    if ( filename.endsWith(".th") ) {
      String filepath = TopoDroidApp.getImportFile( filename );
      String name = filename.replace(".th", "" );
      if ( mApp.mData.hasSurveyName( name ) ) {
        Toast.makeText(this, R.string.import_already, Toast.LENGTH_SHORT).show();
        return;
      }
      // Toast.makeText(this, R.string.import_wait, Toast.LENGTH_SHORT).show();
      new ImportTherionTask() .execute( filepath, name );
    } else if ( filename.endsWith(".top") ) {
      // import PocketTopo (only data for the first trip)
      PTFile ptfile = new PTFile();
      String filepath = TopoDroidApp.getImportFile( filename );
      try {
        // Log.v("DistoX", "PT read file " + filename );
        FileInputStream fs = new FileInputStream( filepath );
        ptfile.read( fs );
        fs.close();
      } catch ( FileNotFoundException e ) {
        // FIXME
        return;
      } catch ( IOException e ) { // on close
        return;
      }
      int nr_trip = ptfile.tripCount();
      if ( nr_trip > 0 ) { // use only the first trip
        PTTrip trip = ptfile.getTrip(0);
        String name = filename.replace( ".top", "" );
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.format( "%04d-%02d-%02d", trip._year, trip._month, trip._day );
        String date = sw.getBuffer().toString();
        String comment = "";
        if ( trip.hasComment() ) comment = trip.comment();
        // trip.declination(); NOT USED
        // TODO create a survey
        String team = "";

        long sid = mApp.setSurveyFromName( name );
        mApp.mData.updateSurveyInfo( mApp.mSID, date, team, 0.0, comment ); // FIXME declination
        
        ArrayList< ParserShot > shots = new ArrayList< ParserShot >();

        int shot_count = ptfile.shotCount();
        int extend = DistoXDBlock.EXTEND_NONE;
        int ext_flag = extend;
        DistoXDBlock b     = null;  // temporary block pointer
        DistoXDBlock start = null;  // first block inserted
        DistoXDBlock last  = null;  // last block on the list

        String from_prev = "";
        String to_prev   = "";
        for ( int s=0; s < shot_count; ++s ) {
          PTShot shot = ptfile.getShot(s);
          String from = shot.from().toString();
          String to   = shot.to().toString();
          if ( from.equals("-") ) from = "";
          if ( to.equals("-") )   to = "";
          if ( from.equals( from_prev ) && to.equals( to_prev ) ) {
            from = "";
            to   = "";
          } else {
            from_prev = from;
            to_prev   = to;
          }
          float da = shot.distance();
          float ba = shot.azimuth();
          float ca = shot.inclination();
          float ra = shot.roll();
          if ( shot.isFlipped() ) {
            if ( extend != DistoXDBlock.EXTEND_LEFT ) {
              extend = DistoXDBlock.EXTEND_LEFT;
              ext_flag = extend;
            } else {
              ext_flag = DistoXDBlock.EXTEND_NONE;
            }
          } else {
            if ( extend != DistoXDBlock.EXTEND_RIGHT ) {
              extend = DistoXDBlock.EXTEND_RIGHT;
              ext_flag = extend;
            } else {
              ext_flag = DistoXDBlock.EXTEND_NONE;
            }
          }
          comment = "";
          if ( shot.hasComment() ) comment = shot.comment();
          shots.add( new ParserShot( from, to,  da, ba, ca, ra, extend, false, false, comment ) );
        }
        mApp.mData.insertShots( sid, 1, shots ); // start with id = 1

        // FIXME drawings are not imported yet
        PTDrawing outline = ptfile.getOutline();
        PTMapping mapping = outline.mapping();
        int x0 = 0; // mapping.origin().x();
        int y0 = 0; // mapping.origin().y();
        // int scale = mapping.scale();
        int elem_count = outline.elementNumber();
        if ( elem_count > 0 ) {
          for (int h=0; h<elem_count; ++h ) {
            try {
              PTPolygonElement elem = (PTPolygonElement)outline.getElement(h);
              int point_count = elem.pointCount();
              int col = elem.color();
              if ( point_count > 1 ) {
                PTPoint point = elem.point(0);
                // FIXME Therion::LineType type = colors.thLine( col );
                // add a line to the plotCanvas
                int k=0;
                int x1 = (int)( PT_SCALE*(point.x() - x0)/1000.0 );
                int y1 = (int)( PT_SCALE*(point.y() - y0)/1000.0 );
                // FIXME drawer->insertLinePoint( x1, y1, type, canvas );
                for (++k; k<point_count; ++k ) {
                  point = elem.point(k);
                  int x = (int)( PT_SCALE*(point.x() - x0)/1000.0 );
                  int y = (int)( PT_SCALE*(point.y() - y0)/1000.0 );
                  if ( Math.abs(x - x1) >= 4 || Math.abs(y - y1) >= 4 ) {
                    x1 = x;
                    y1 = y;
                    // FIXME drawer->insertLinePoint( x, y, type, canvas );
                  }
                }
                // FIXME drawer->insertLinePoint( x1, y1, type, canvas ); // close the line
              } else if ( point_count == 1 ) {
                PTPoint point = elem.point(0);
                // FIXME Therion::PointType type = colors.thPoint( col );
                int x = (int)( PT_SCALE*(point.x() - x0)/1000.0 );
                int y = (int)( PT_SCALE*(point.y() - y0)/1000.0 );
                // FIXME drawer->insertPoint(x, y, type, canvas );
              }
            } catch( ClassCastException e ) {
            }
          }
        }
        PTDrawing sideview = ptfile.getSideview();
        mapping = sideview.mapping();
        // TODO do the same for the section

        updateDisplay();
      }
      setTitleColor( TopoDroidApp.COLOR_NORMAL );
    } else if ( filename.endsWith(".zip") ) {
      Toast.makeText(this, R.string.import_zip_wait, Toast.LENGTH_LONG).show();
      new ImportZipTask( this ) .execute( filename );
    }
  }
  
  // ---------------------------------------------------------------
  // private Button mButtonHelp;

  TopoDroidAbout mTopoDroidAbout = null;
  // TdSymbolDialog mTdSymbolDialog = null;
 
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.topodroid_activity);
    mApp = (TopoDroidApp) getApplication();
    mApp.mActivity = this;
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

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }

    int nr_button1 = 4;
    int nr_button2 = 3;
    mButton1 = new Button[nr_button1];
    mButton2 = new Button[nr_button2];
    int k;
    for (k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }
    for (k=0; k<nr_button2; ++k ) {
      mButton2[k] = new Button( this );
      mButton2[k].setPadding(0,0,0,0);
      mButton2[k].setOnClickListener( this );
      mButton2[k].setBackgroundResource(  icons[k+nr_button1] );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    // if ( savedInstanceState == null) {
    //   TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "onCreate null savedInstanceState" );
    // } else {
    //   Bundle map = savedInstanceState.getBundle(DISTOX_KEY);
    //   restoreInstanceState( map );
    // }
    // restoreInstanceFromFile();
    restoreInstanceFromData();
    if ( mApp.mWelcomeScreen ) {
      mApp.setBooleanPreference( "DISTOX_WELCOME_SCREEN", false );
      mApp.mWelcomeScreen = false;
      mTopoDroidAbout = new TopoDroidAbout( this );
      mTopoDroidAbout.setOnCancelListener( this );
      mTopoDroidAbout.setOnDismissListener( this );
      mTopoDroidAbout.show();
    // } else if ( mApp.mTdSymbol ) {
    //   startTdSymbolDialog();
    }
    if ( mApp.askSymbolUpdate ) {
      (new TopoDroidVersionDialog(this, mApp)).show();
    }

    DrawingBrushPaths.doMakePaths( );

    // setTitleColor( 0x006d6df6 );
  }

  @Override
  public void onDismiss( DialogInterface d )
  { 
    // if ( d == (Dialog)mTdSymbolDialog ) {
    //   if ( mApp.mStartTdSymbol ) {
    //     Intent intent = new Intent( "TdSymbol.intent.action.Launch" );
    //     try {
    //       startActivity( intent );
    //       DrawingBrushPaths.mReloadSymbols = true;
    //     } catch ( ActivityNotFoundException e ) {
    //       Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_SHORT ).show();
    //     }
    //   }
    //   mTdSymbolDialog = null;
    // } else
    if ( d == (Dialog)mTopoDroidAbout ) {
      // startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  @Override
  public void onCancel( DialogInterface d )
  {
    if ( d == (Dialog)mTopoDroidAbout ) {
      // startTdSymbolDialog();
      mTopoDroidAbout = null;
    }
  }

  // private void startTdSymbolDialog()
  // {
  //   mTdSymbolDialog = new TdSymbolDialog( this, mApp );
  //   // mTdSymbolDialog.setOnCancelListener( this );
  //   mTdSymbolDialog.setOnDismissListener( this );
  //   mTdSymbolDialog.show();
  // }

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
    DataHelper data = mApp.mData;
    String status = data.getValue( "DISTOX_STATUS" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore STATUS " + status );
    if ( status != null ) {
      String[] vals = status.split( " " );
      // FIXME
    }
     
    String survey = data.getValue( "DISTOX_SURVEY" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore SURVEY >" + survey + "<" );
    if ( survey != null && survey.length() > 0 ) {
      mApp.setSurveyFromName( survey );
    } else {
      mApp.setSurveyFromName( null );
    }
    String calib = data.getValue( "DISTOX_CALIB" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "restore CALIB >" + calib + "<" );
    if ( calib != null && calib.length() > 0 ) {
      mApp.setCalibFromName( calib );
    } else {
      mApp.setCalibFromName( null );
    }
  }
    
  private void saveInstanceToData()
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_MAIN, "saveInstanceToData");
    DataHelper data = mApp.mData;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d ", mStatus );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save STATUS " + sw.getBuffer().toString() );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save SURVEY >" + mApp.mySurvey + "<" );
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "save CALIB >" + mApp.getCalib() + "<" );
    data.setValue( "DISTOX_STATUS", sw.getBuffer().toString() );
    data.setValue( "DISTOX_SURVEY", (mApp.mySurvey == null)? "" : mApp.mySurvey );
    data.setValue( "DISTOX_CALIB", (mApp.myCalib == null)? "" : mApp.myCalib );
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
  //   outState.putString(DISTOX_KEY_SURVEY, mySurvey );
  //   outState.putString(DISTOX_KEY_CALIB, myCalib );
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onStart check BT " + mApp.mCheckBT + " enabled " + mApp.mBTAdapter.isEnabled() );

    if ( do_check_bt ) {
      do_check_bt = false;
      if ( mApp.mCheckBT == 1 && ! mApp.mBTAdapter.isEnabled() ) {    
        Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
        startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
      } else {
        // nothing to do: scanBTDEvices(); is called by menu CONNECT
      }
      // FIXME_BT
      // setBTMenus( mApp.mBTAdapter.isEnabled() );
    }
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onResume " );
    if ( mApp.mComm != null ) { mApp.mComm.resume(); }

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
    if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
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
    // FIXME if ( mApp.mComm != null ) { mApp.mComm.interrupt(); }
    saveInstanceToData();
  }

  // ------------------------------------------------------------------


  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onActivityResult() request " + mRequestName[request] + " result: " + result );
    DataHelper data = mApp.mData;
    Bundle extras = (intent != null )? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_ENABLE_BT:
        if ( result == Activity.RESULT_OK ) {
          // nothing to do: scanBTDEvices() is called by menu CONNECT
        } else if ( say_not_enabled ) {
          say_not_enabled = false;
          Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_SHORT).show();
          // finish();
        }
        // FIXME_BT
        // FIXME mApp.mBluetooth = ( result == Activity.RESULT_OK );
        // setBTMenus( mApp.mBTAdapter.isEnabled() );
        updateDisplay( );
        break;

    }
    // TopoDroidApp.Log( TopoDroidApp.LOG_MAIN, "onActivityResult() done " );
  }

  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsymbol  = menu.add( R.string.menu_palette );
    mMIoptions = menu.add( R.string.menu_options );
    mMIlogs    = menu.add( R.string.menu_logs );
    mMImanual  = menu.add( R.string.menu_manual  );
    mMIhelp    = menu.add( R.string.menu_help  );
    mMIabout   = menu.add( R.string.menu_about );

    mMIsymbol.setIcon( R.drawable.ic_symbol );
    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIlogs.setIcon( R.drawable.ic_logs );
    mMImanual.setIcon( R.drawable.ic_manual );
    mMIhelp.setIcon( R.drawable.ic_help );
    mMIabout.setIcon( R.drawable.ic_info );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    Intent intent;
    if ( item == mMIsymbol ) { 
      DrawingBrushPaths.makePaths( getResources() );
      (new SymbolEnableDialog( this, this )).show();
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_ALL );
      startActivity( intent );
    } else if ( item == mMIlogs ) { // LOG OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_LOG );
      startActivity( intent );
    } else if ( item == mMImanual ) { // ABOUT DIALOG
      try {
        // TopoDroidHelp.show( this, R.string.help_topodroid );
        Intent pdf = new Intent( Intent.ACTION_VIEW, Uri.parse( TopoDroidApp.mManual ) );
        startActivity( pdf );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, "No pdf viewer mApp", Toast.LENGTH_SHORT ).show();
      }
    } else if ( item == mMIabout ) { // ABOUT DIALOG
      (new TopoDroidAbout( this )).show();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}
