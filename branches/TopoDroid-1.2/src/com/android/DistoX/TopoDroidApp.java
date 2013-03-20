/** @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120516 length and angle units
 * 20120517 survey-info and fix station export
 * 20120518 app base directory
 * 20120523 connection mode (batch, continuous)
 * 20120524 welcome screen and base-path pref. 
 * 20120524 moved fixed station management to SurveyActivity
 * 20120525 conn-mode pref.
 * 20120529 connection colors
 * 20120531 STATUS const's
 * 20120602 fixed-info exports
 * 20120611 made filename compositions private
 * 20120705 symbols screen units
 * 20120706 screen scale factor (for drawing on the canvas)
 * 20120715 forced no_spaces in names (survey, station, sketch, calib)
 * 20120720 added manifest
 * 20120725 centralized log
 * 20120803 removed connection-mode preference
 * 20121001 splay extend in therion export
 * 20121114 added LOG_DEBUG (true)
 * 20121120 added LOG_INPUT (false) log user inputs
 * 20121121 added log preferences
 * 20121124 added checkCalibrationDeviceMatch() 
 * 20121129 added mExtendThr and its pref 
 * 20121201 APP_POINT_PATH APP_LINE_PATH APP_AREA_PATH
 * 20121205 location units
 * 20121217 dropped TLX export, added mkdirs() for dir paths
 * 20121217 bug-fix comment in tro export
 * 20121218 pref whether to show TdSymbol 
 * 20130108 therion export surface flags (TODO better flags management)
 * 20130108 auto_stations option
 * 20130130 bug fix: export shot-type check for BLOCK_LEG
 * 20130204 disable lock
 */
package com.android.DistoX;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Activity;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;

import android.view.WindowManager;
import android.view.Display;
import android.graphics.Point;

import android.util.Log;
import android.util.DisplayMetrics;

import android.bluetooth.BluetoothAdapter;

import android.widget.Toast;

public class TopoDroidApp extends Application
                          implements OnSharedPreferenceChangeListener
{
  static final String TAG = "DistoX";

  static final boolean LOG_BEZIER = false;
  static boolean LOG_BT     = false;   // bluetooth
  static boolean LOG_CALIB  = false;
  static boolean LOG_COMM   = false;   // connection
  static boolean LOG_DATA   = false;   // shot data
  static boolean LOG_DB     = false;   // sqlite database
  static boolean LOG_DEBUG  = false;
  static boolean LOG_DEVICE = false;
  static boolean LOG_DISTOX = false;   // DistoX packets
  static boolean LOG_ERR    = true;
  static boolean LOG_FIXED  = false;
  static boolean LOG_INPUT  = false;   // user input
  static boolean LOG_LOC    = false;   // location manager
  static final boolean LOG_NOTE   = false;   // annotation
  static final boolean LOG_MAIN   = false;   // main app
  static final boolean LOG_NAME   = false;   // names
  static final boolean LOG_NUM    = false;  
  static final boolean LOG_PATH   = false;
  static final boolean LOG_PLOT   = false;
  static boolean LOG_PHOTO        = false;   // photos
  static final boolean LOG_PREFS  = false;   // preferences
  static boolean LOG_PROTO        = false;   // protocol
  static boolean LOG_SENSOR       = false;   // sensors and measures
  static final boolean LOG_SHOT   = false;   // shot
  static final boolean LOG_STATS  = false;
  static final boolean LOG_SURVEY = false;
  static final boolean LOG_THERION= true;
  static final boolean LOG_ZIP    = false;   // archive

  static int mScreenTimeout = 60000; // 60 secs

  static void Log( boolean flag, String msg )
  {
    if ( flag ) {
      Log.v( TAG, msg );
    }
  }


  static final String VERSION = "1.1.0"; // must agree with AndroidManifest.xml

  private SharedPreferences prefs;

  static final int STATUS_NORMAL  = 0;   // item (shot, plot) status
  static final int STATUS_DELETED = 1;  

  static final int COLOR_NORMAL    = 0xffcccccc; // title color
  static final int COLOR_CONNECTED = 0xffff6666;

  BluetoothAdapter mBTAdapter = null;
  DistoXComm mComm = null;
  DataHelper mData = null;
  Calibration mCalibration = null;

  // final static int CONN_MODE_BATCH = 0; // DistoX connection mode
  // final static int CONN_MODE_CONTINUOUS = 1;
  // int mConnectionMode = CONN_MODE_BATCH; 

  long mSID   = -1;   // id of the current survey
  long mCID   = -1;   // id of the current calib
  String mySurvey;   // current survey name
  String myCalib;    // current calib name

  // preferences
  boolean mWelcomeScreen;  // whether to show the welcome screen
  boolean mTdSymbol;       // whether to ask TdSymbol
  boolean mStartTdSymbol;  // whether to start TdSymbol (result "yes" of TdSymbolDialog)
  static String  mManual;  // manual url

  String  mBasePath = Environment.getExternalStorageDirectory().getAbsolutePath(); // app base path

  float mCloseDistance;
  // int   mExportType;
  float mGroupDistance;
  float mCalibEps;
  int   mCalibMaxIt;
  String mDevice = DEVICE_NAME;
  // private boolean mSaveOnDestroy = SAVE_ON_DESTROY;
  // int   mDefaultConnectionMode;

  int   mLineSegment;
  float mVThreshold;    // verticality threshold (LRUD)
  float mLineAccuracy;
  float mLineCorner;    // corner threshold
  static float mCloseness;
  boolean mCheckBT;     // check BT on start
  boolean mCheckAttached; 
  boolean mListRefresh;  // whether to refresh list on edit-dialog ok-return
  static boolean mAutoStations; // whether to add stations automatically to scrap therion files
  static boolean mLoopClosure;  // whether to do loop closure
  int mGroupBy;          // how to group calib data

  // create socket type
  static final int TOPODROID_SOCK_DEFAULT      = 0;
  static final int TOPODROID_SOCK_INSEC        = 1;
  // static final int TOPODROID_SOCK_INSEC_RECORD = 2;
  // static final int TOPODROID_SOCK_INSEC_INVOKE = 3;
  static int mSockType = TOPODROID_SOCK_DEFAULT; // FIXME static
  static int mCommRetry = 1;

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;

  // consts
  public static final float M_PI  = 3.1415926536f; // Math.PI;
  public static final float M_2PI = 6.283185307f;  // 2*Math.PI;
  public static final float RAD2GRAD_FACTOR = (180.0f/M_PI);
  public static final float GRAD2RAD_FACTOR = (M_PI/180.0f);
  public static final long ZERO = 32768;
  public static final long NEG  = 65536;
  public static final float FV = 24000.0f;
  public static final float FM = 16384.0f;

  static final float M2FT = 3.28083f; // meters to feet 
  static final float FT2M = 1/M2FT;
  static final float IN2M = 0.0254f;
  static final float YD2M = 0.914f;
  static final float DEG2GRAD = 400.0f/360.0f;
  static final float GRAD2DEG = 360.0f/400.0f;
  static final int DDMMSS = 0;
  static final int DEGREE = 1;
  // private static final byte char0C = 0x0c;
  public static final String EXTEND_THR = "30"; // extend vertically splays in [90-30, 90+30] of the leg

  private static String APP_BASE_PATH; //  = Environment.getExternalStorageDirectory() + "/TopoDroid/";
  // private static String APP_TLX_PATH ; //  = APP_BASE_PATH + "tlx/";
  private static String APP_DAT_PATH ; //  = APP_BASE_PATH + "dat/";
  private static String APP_SVX_PATH ; //  = APP_BASE_PATH + "svx/";
  private static String APP_TH_PATH  ; //  = APP_BASE_PATH + "th/";
  private static String APP_TH2_PATH ; //  = APP_BASE_PATH + "th2/";
  private static String APP_TH3_PATH ; //  = APP_BASE_PATH + "th3/";
  private static String APP_TRO_PATH ; //  = APP_BASE_PATH + "tro/";
  private static String APP_MAPS_PATH; //  = APP_BASE_PATH + "png/";
  private static String APP_NOTE_PATH; //  = APP_BASE_PATH + "note/";
  private static String APP_FOTO_PATH; //  = APP_BASE_PATH + "photo/";
  private static String APP_IMPORT_PATH; //  = APP_BASE_PATH + "import/";
  private static String APP_ZIP_PATH; //  = APP_BASE_PATH + "zip/";
  static String APP_POINT_PATH; //  = APP_BASE_PATH + "symbol/point/";
  static String APP_LINE_PATH; 
  static String APP_AREA_PATH;

  private void setPaths( String path )
  {
    mManual = getResources().getString( R.string.topodroid_man );
    File dir = null;

    APP_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TopoDroid/";
    // APP_BASE_PATH = Environment.getExternalStorageDirectory() + "/TopoDroid/";
    if ( path != null ) {
      dir = new File( path );
      if ( dir.exists() && dir.isDirectory() && dir.canWrite() ) {
        APP_BASE_PATH = path + "/TopoDroid/";
      }
    } 
    // Log.v(TAG, "Base Path \"" + APP_BASE_PATH + "\"" );

    // APP_TLX_PATH    = APP_BASE_PATH + "tlx/";
    // dir = new File( APP_TLX_PATH );
    // if ( ! dir.exists() ) dir.mkdirs( );

    APP_DAT_PATH    = APP_BASE_PATH + "dat/";
    dir = new File( APP_DAT_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_SVX_PATH    = APP_BASE_PATH + "svx/";
    dir = new File( APP_SVX_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TH_PATH     = APP_BASE_PATH + "th/";
    dir = new File( APP_TH_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TH2_PATH    = APP_BASE_PATH + "th2/";
    dir = new File( APP_TH2_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TH3_PATH    = APP_BASE_PATH + "th3/";
    dir = new File( APP_TH3_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TRO_PATH    = APP_BASE_PATH + "tro/";
    dir = new File( APP_TRO_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_MAPS_PATH   = APP_BASE_PATH + "png/";
    dir = new File( APP_MAPS_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_NOTE_PATH   = APP_BASE_PATH + "note/";
    dir = new File( APP_NOTE_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_FOTO_PATH   = APP_BASE_PATH + "photo/";
    dir = new File( APP_FOTO_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_IMPORT_PATH = APP_BASE_PATH + "import/";
    dir = new File( APP_IMPORT_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_ZIP_PATH    = APP_BASE_PATH + "zip/";
    dir = new File( APP_ZIP_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_POINT_PATH  = APP_BASE_PATH + "symbol/point/";
    dir = new File( APP_POINT_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_LINE_PATH   = APP_BASE_PATH + "symbol/line/";
    dir = new File( APP_LINE_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_AREA_PATH   = APP_BASE_PATH + "symbol/area/";
    dir = new File( APP_AREA_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

  }

  // sketch types
  public static final long PLOT_V_SECTION = 0;
  public static final long PLOT_PLAN      = 1;
  public static final long PLOT_EXTENDED  = 2;
  public static final long PLOT_H_SECTION = 3;
  public static final long PLOT_SKETCH_3D = 4;

  // drawing line styles
  public static final int LINE_STYLE_BEZIER = 0;
  public static final int LINE_STYLE_NONE   = 1;
  public static final int LINE_STYLE_TWO    = 2;
  public static final int LINE_STYLE_THREE  = 3;
  int mLineStyle;       // line style: BEZIER, NONE, TWO, THREE
  int mLineType;        // line type:  1       1     2    3

  // calibration data grouping policies
  public static final int GROUP_BY_DISTANCE = 0;
  public static final int GROUP_BY_FOUR     = 1;
  public static final int GROUP_BY_ONLY_16  = 2;


  public static final String[] projName = { // therion projection names
    "none", "plan", "extended", "none", "sketch_3d"
  };

  public static final float LEN_THR    = 20.0f; // corner detection length
  public static final float TO_THERION = 5.0f;

  public static float mUnit; // drawing unit
  // conversion factor from internal units (m) to user units
  public static float mUnitLength;
  public static float mUnitAngle;
  public static int mUnitLocation; // 0 dec-degree, 1 ddmmss
  public static double mExtendThr;

  public static final String[] key = { // prefs keys
    "DISTOX_CLOSE_DISTANCE",  // 0
    "DISTOX_EXPORT_TYPE",     // 1 NOT USED
    "DISTOX_GROUP_DISTANCE",
    "DISTOX_CALIB_EPS",
    "DISTOX_CALIB_MAX_IT",
    "DISTOX_DEVICE",          // 5
    "DISTOX_SAVE_ON_DESTROY",
    "DISTOX_LINE_SEGMENT",
    "DISTOX_VTHRESHOLD",
    "DISTOX_LINE_ACCURACY",
    "DISTOX_LINE_CORNER",     // 10
    "DISTOX_LINE_STYLE",
    "DISTOX_CHECK_BT",
    "DISTOX_DRAWING_UNIT",
    "DISTOX_GROUP_BY",
    "DISTOX_LIST_REFRESH",    // 15 
    "DISTOX_AUTO_STATIONS",   // 16 
    "DISTOX_UNIT_LENGTH",
    "DISTOX_UNIT_ANGLE",
    "DISTOX_UNIT_LOCATION",   // 19 "DISTOX_CONN_MODE" is no longer used
    "DISTOX_BASE_PATH",
    "DISTOX_CHECK_ATTACHED",   // 21
    "DISTOX_SOCK_TYPE",
    "DISTOX_COMM_RETRY",       // 23
    "DISTOX_EXTEND_THR2",      // 24
    "DISTOX_LOOP_CLOSURE",     // 25
    "DISTOX_CLOSENESS",        // 26
    // --------------- LOG PREFERENCES ----------------------
    "DISTOX_LOG_DEBUG",
    "DISTOX_LOG_ERR",
    "DISTOX_LOG_INPUT",        // 29
    "DISTOX_LOG_BT",           // 30
    "DISTOX_LOG_COMM",
    "DISTOX_LOG_PROTO",
    "DISTOX_LOG_DISTOX",
    "DISTOX_LOG_DEVICE",       // 34
    "DISTOX_LOG_DATA",
    "DISTOX_LOG_DB",           // 36
    "DISTOX_LOG_CALIB",
    "DISTOX_LOG_FIXED",
    "DISTOX_LOG_LOC",          // 39
    "DISTOX_LOG_PHOTO",
    "DISTOX_LOG_SENSOR"        // 41
    // "DISTOX_LOG_SHOT",
    // "DISTOX_LOG_SURVEY",
    // "DISTOX_LOG_NUM",          // 44
    // "DISTOX_LOG_THERION",
    // "DISTOX_LOG_PLOT",
    // "DISTOX_LOG_BEZIER"
  };

  public static final int DISTOX_EXPORT_TH  = 0;
  // public static final int DISTOX_EXPORT_TLX = 1;
  public static final int DISTOX_EXPORT_DAT = 2;
  public static final int DISTOX_EXPORT_SVX = 3;
  public static final int DISTOX_EXPORT_TRO = 4;
  public static final int DISTOX_EXPORT_MAX = 5;   // placeholder 

  public static final int DISTOX_MIN_ITER   = 50;  // hard limits
  public static final float DISTOX_MAX_EPS  = 0.1f;

  // prefs default values
  public static final  String CLOSE_DISTANCE = "0.05f"; // 50 cm / 1000 cm
  public static final  String V_THRESHOLD    = "80.0f";
  public static final  String LINE_SEGMENT   = "3";
  public static final  String LINE_ACCURACY  = "1.0f";
  public static final  String LINE_CORNER    = "20.0f";
  public static final  String LINE_STYLE     = "2";     // LINE_STYLE_TWO
  public static final  String DRAWING_UNIT   = "1.2f";  // UNIT
  public static final  String CLOSENESS      = "16";    // drawing closeness threshold
  // public static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH
  public static final  String GROUP_DISTANCE = "40.0f";
  public static final  String CALIB_EPS      = "0.0000001f";
  public static final  String CALIB_MAX_ITER = "200";
  public static final  String GROUP_BY       = "0";     // GROUP_BY_DISTANCE
  public static final  String DEVICE_NAME    = "";
  // public static final  boolean SAVE_ON_DESTROY = true;
  public static final  boolean CHECK_BT      = true;
  public static final  boolean CHECK_ATTACHED = false;
  public static final  boolean LIST_REFRESH  = false;
  public static final  boolean AUTO_STATIONS = true;
  public static final  boolean LOOP_CLOSURE  = false;
  public static final  String UNIT_LENGTH    = "meters";
  public static final  String UNIT_ANGLE     = "degrees";
  public static final  String UNIT_LOCATION  = "ddmmss";

  // intent names
  public static final String TOPODROID_PLOT_ID     = "topodroid.plot_id";
  public static final String TOPODROID_PLOT_NAME   = "topodroid.plot_name";

  public static final String TOPODROID_SKETCH_ID   = "topodroid.sketch_id";
  public static final String TOPODROID_SKETCH_NAME = "topodroid.sketch_name";

  public static final String TOPODROID_SURVEY      = "topodroid.survey";
  public static final String TOPODROID_SURVEY_ID   = "topodroid.survey_id";
  
  public static final String TOPODROID_DEVICE_ACTION = "topodroid.device_action";
  // public static final String TOPODROID_DEVICE_ADDR   = "topodroid.device_addr";
  // public static final String TOPODROID_DEVICE_CNCT   = "topodroid.device_cnct";

  public static final String TOPODROID_SENSOR_TYPE  = "topodroid.sensor_type";
  public static final String TOPODROID_SENSOR_VALUE = "topodroid.sensor_value";
  public static final String TOPODROID_SENSOR_COMMENT = "topodroid.sensor_comment";

  public String[] DistoXConnectionError;

  // ---------------------------------------------------------------
  // ConnListener
  ArrayList< Handler > mConnListener;

  void registerConnListener( Handler hdl )
  {
    if ( hdl != null ) {
      mConnListener.add( hdl );
      try {
        new Messenger( hdl ).send( new Message() );
      } catch ( RemoteException e ) { }
    }
  }

  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null ) {
      try {
        new Messenger( hdl ).send( new Message() );
      } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  public void notifyConnState( )
  {
    // Log.v( TAG, "notify conn state" );
    for ( Handler hdl : mConnListener ) {
      try {
        new Messenger( hdl ).send( new Message() );
      } catch ( RemoteException e ) { }
    }
  }
  
  // ---------------------------------------------------------------
  // survey/calib info
  //

  boolean checkCalibrationDeviceMatch() 
  {
    CalibInfo info = mData.selectCalibInfo( mCID  );
    return ( info != null && info.device.equals( mDevice ) );
  }

  static String noSpaces( String s )
  {
    return ( s == null )? null : s.trim().replaceAll("\\s+", "_");
  }

  public SurveyInfo getSurveyInfo()
  {
    if ( mSID <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( mSID );
  }

  public CalibInfo getCalibInfo()
  {
    if ( mCID <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectCalibInfo( mCID );
  }

  // ----------------------------------------------------------------

  @Override
  public void onTerminate()
  {
    super.onTerminate();
    // Log.v(TAG, "onTerminate app");
  }

  // private void setExportType( String type )
  // {
  //   mExportType = DISTOX_EXPORT_TH;
  //   if ( type.equals("th") ) {
  //     mExportType = DISTOX_EXPORT_TH;
  //   // } else if ( type.equals("tlx") ) { 
  //   //   mExportType = DISTOX_EXPORT_TLX;
  //   } else if ( type.equals("dat") ) { 
  //     mExportType = DISTOX_EXPORT_DAT;
  //   } else if ( type.equals("svx") ) { 
  //     mExportType = DISTOX_EXPORT_SVX;
  //   } else if ( type.equals("tro") ) { 
  //     mExportType = DISTOX_EXPORT_TRO;
  //   }
  // }


  private void setCommRetry( SharedPreferences sp )
  {
    mCommRetry = Integer.parseInt( sp.getString( key[23], "1" ) );
    if ( mCommRetry < 1 ) mCommRetry = 1;
    if ( mCommRetry > 5 ) mCommRetry = 5;
  }

  private void setExtendThr( SharedPreferences sp )
  {
    mExtendThr = Double.parseDouble( sp.getString( key[24], EXTEND_THR ) );
    if ( mExtendThr < 0.0 ) mExtendThr = 0.0;
    if ( mExtendThr > 90.0 ) mExtendThr = 90.0;
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    // disable lock
    KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
    KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
    lock.disableKeyguard();

    try {
      mScreenTimeout = System.getInt(getContentResolver(), System.SCREEN_OFF_TIMEOUT );
    } catch ( SettingNotFoundException e ) {
    }

    // Log.v(TAG, "onCreate app");
    this.prefs = PreferenceManager.getDefaultSharedPreferences( this );
    this.prefs.registerOnSharedPreferenceChangeListener( this );

    mWelcomeScreen = prefs.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    mTdSymbol = prefs.getBoolean( "DISTOX_TD_SYMBOL", true );
    mStartTdSymbol = false;

    mBasePath = prefs.getString( "DISTOX_BASE_PATH", mBasePath );
    setPaths( mBasePath );

    // ------------------- SURVEY PREFERENCES
    mCloseDistance = Float.parseFloat( prefs.getString( key[0], CLOSE_DISTANCE ) );
    mVThreshold    = Float.parseFloat( prefs.getString( key[8], V_THRESHOLD ) ); // DISTOX_VTHRESHOLD
    // setExportType( prefs.getString( key[1], EXPORT_TYPE ) );
    mLoopClosure   = prefs.getBoolean( key[25], LOOP_CLOSURE );

    // -------------------  DRAWING PREFERENCES
    mLineSegment   = Integer.parseInt( prefs.getString( key[7], LINE_SEGMENT ) ); // DISTOX_LINE_SEGMENT
    mLineAccuracy  = Float.parseFloat( prefs.getString( key[9], LINE_ACCURACY ) ); // DISTOX_LINE_ACCURACY
    mLineCorner    = Float.parseFloat( prefs.getString( key[10], LINE_CORNER ) );  // DISTOX_LINE_CORNER
    setLineStyleAndType( prefs.getString( key[11], LINE_STYLE ) );                 // DISTOX_LINE_STYLE
    mUnit          = Float.parseFloat( prefs.getString( key[13], DRAWING_UNIT ) );
    mCloseness     = Float.parseFloat( prefs.getString( key[26], CLOSENESS ) );

    // ------------------- CALIBRATION PREFERENCES
    mGroupBy       = Integer.parseInt( prefs.getString( key[14], GROUP_BY ) );
    mGroupDistance = Float.parseFloat( prefs.getString( key[2], GROUP_DISTANCE ) );
    mCalibEps      = Float.parseFloat( prefs.getString( key[3], CALIB_EPS ) );
    mCalibMaxIt    = Integer.parseInt( prefs.getString( key[4], CALIB_MAX_ITER ) );
    
    mCheckBT       = prefs.getBoolean( key[12], CHECK_BT );        // DISTOX_CHECK_BT
    mListRefresh   = prefs.getBoolean( key[15], LIST_REFRESH );
    mAutoStations  = prefs.getBoolean( key[16], AUTO_STATIONS );
    mCheckAttached = prefs.getBoolean( key[21], CHECK_ATTACHED );

    // ------------------- DISPLAY UNITS
    mUnitLength    = prefs.getString( key[17], UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : M2FT;
    mUnitAngle     = prefs.getString( key[18], UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : DEG2GRAD;
    mUnitLocation  = prefs.getString( key[19], UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
    setExtendThr( prefs );

    // ------------------- DEVICE PREFERENCES
    mDevice        = prefs.getString( key[5], DEVICE_NAME );
    // mConnectionMode = Integer.parseInt( prefs.getString( key[], "0" ) );
    mSockType      = Integer.parseInt( prefs.getString( key[22], "0" ) );
    setCommRetry( prefs );

    mData = new DataHelper( this );
    mCalibration = new Calibration( 0, this );
    mConnListener = new ArrayList< Handler >();

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    if ( mBTAdapter == null ) {
      // Toast.makeText( this, R.string.not_available, Toast.LENGTH_LONG ).show();
      // finish(); // FIXME
      // return;
    }

    mComm = new DistoXComm( this );

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );
    
    // WindowManager wm = (WindowManager)getSystemService( Context.WINDOW_SERVICE );
    // Display d = wm.getDefaultDisplay();
    // Point s = new Point();
    // d.getSize( s );
    // Log.v( TAG, "display " + d.getWidth() + " " + d.getHeight() );
    // mDisplayWidth  = d.getWidth();
    // mDisplayHeight = d.getHeight();
    DisplayMetrics dm = getResources().getDisplayMetrics();
    float density  = dm.density;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    Log.v( TAG, "display " + mDisplayWidth + " " + mDisplayHeight + " scale " + mScaleFactor );
  }

  private void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_TWO; // default
    mLineType  = 1;
    if ( style.equals( "0" ) ) {
      mLineStyle = LINE_STYLE_BEZIER;
      mLineType  = 1;
    } else if ( style.equals( "1" ) ) {
      mLineStyle = LINE_STYLE_NONE;
      mLineType  = 1;
    } else if ( style.equals( "2" ) ) {
      mLineStyle = LINE_STYLE_TWO;
      mLineType  = 2;
    } else if ( style.equals( "3" ) ) {
      mLineStyle = LINE_STYLE_THREE;
      mLineType  = 3;
    }
  }

  // public BluetoothAdapter getBTAdapter() { return mBTAdapter; }
  // public DistoXComm getComm() { return mComm; }
  // public DataHelper getData() { return mData; }
  // public Calibration getCalibration() { return mCalibration; }

  public void resumeComm()
  {
    if ( mComm != null ) { mComm.resume(); }
  }

  public void resetComm() 
  { 
    if ( mComm != null && mComm.mBTConnected ) {
      mComm.disconnectRemoteDevice( );
    }
    mComm = null;
    mComm = new DistoXComm( this );
  }

  public boolean isConnected()
  {
    // return mComm != null && mComm.mBTConnected;
    return mComm != null && mComm.mBTConnected && mComm.mRfcommThread != null;
  }

  // FIXME to disappear ...
  public long getSurveyId() { return mSID; }
  public long getCalibId()  { return mCID; }
  public String getSurvey() { return mySurvey; }
  public String getCalib()  { return myCalib; }

  // ------------------------------------------------------------------
  // FILE NAMES

  public static String getSqlFile()
  {
    return APP_BASE_PATH + "survey.sql";
  }

  public static String getManifestFile()
  {
    return APP_BASE_PATH + "manifest";
  }

  public void writeManifestFile()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    try {
      FileWriter fw = new FileWriter( getManifestFile() );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s\n", VERSION );
      pw.format( "%s\n", DataHelper.DB_VERSION );
      pw.format( "%s\n", info.name );
      fw.flush();
      fw.close();
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch ( IOException e ) {
      // FIXME
    }
  }

  public int checkManifestFile( String filename, String surveyname )
  {
    String line;
    if ( mData.hasSurveyName( surveyname ) ) {
      return -1;
    }
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      // first line is version
      line = br.readLine().trim();
      if ( ! line.equals( VERSION ) ) return -2;
      line = br.readLine().trim();
      if ( ! line.equals( DataHelper.DB_VERSION ) ) return -2;
      line = br.readLine().trim();
      if ( ! line.equals( surveyname ) ) return -2;
      fr.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return 0;
  }

  public static String getSurveyNoteFile( String title ) 
  {
    File dir = new File( APP_NOTE_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_NOTE_PATH + title + ".txt";
  }

  public static boolean hasTh2Dir()
  {
    File dir = new File( TopoDroidApp.APP_TH2_PATH );
    return dir.exists();
  }

  public static boolean hasTh3Dir()
  {
    File dir = new File( TopoDroidApp.APP_TH3_PATH );
    return dir.exists();
  }

  public static boolean hasPngDir()
  {
    File dir = new File( TopoDroidApp.APP_MAPS_PATH );
    return dir.exists();
  }

  public static String getDirFile( String name )    { return APP_BASE_PATH + name; }
  public static String getImportFile( String name ) { return APP_IMPORT_PATH + name; }
  public static String getZipFile( String name )    { return APP_ZIP_PATH + name; }
  public static String getTh2File( String name )    { return APP_TH2_PATH + name; }
  public static String getTh3File( String name )    { return APP_TH3_PATH + name; }
  public static String getNoteFile( String name )   { return APP_NOTE_PATH + name; }

  public static String getJpgDir( String dir ) { return APP_FOTO_PATH + dir; }
  public static String getJpgFile( String dir, String name ) { return APP_FOTO_PATH + dir + "/" + name; }

  public String getSurveyPlotFile( String name ) { return APP_TH2_PATH + mySurvey + "-" + name + ".th2"; }
  public String getSurveySketchFile( String name ) { return APP_TH3_PATH + mySurvey + "-" + name + ".th3"; }
  public String getSurveyPngFile( String name )  { return APP_MAPS_PATH + mySurvey + "-" + name + ".png"; }

  public String getTh2FileWithExt( String name ) 
  {
    File dir = new File( APP_TH2_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_TH2_PATH + name + ".th2";
  }

  public String getTh3FileWithExt( String name ) 
  {
    File dir = new File( APP_TH3_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_TH3_PATH + name + ".th3";
  }
 
  public String getPngFileWithExt( String name ) 
  {
    File dir = new File( APP_MAPS_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_MAPS_PATH + name + ".png";
  }

  public static File[] getImportFiles() { return getFiles( APP_IMPORT_PATH, ".th" ); }
  public static File[] getZipFiles() { return getFiles( APP_ZIP_PATH, ".zip" ); }

  private static File[] getFiles( String dirname, final String extension )
  {
    File dir = new File( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { return pathname.getName().endsWith( extension ); }
        } );
    }
    return null;
  }


  // public String getSurveyPhotoFile( String name ) { return APP_FOTO_PATH + mySurvey + "/" + name; }

  public String getSurveyPhotoDir( ) { return APP_FOTO_PATH + mySurvey; }

  public String getSurveyJpgFile( long id )
  {
    File imagedir = new File( TopoDroidApp.APP_FOTO_PATH + mySurvey + "/" );
    if ( ! ( imagedir.exists() ) ) {
      imagedir.mkdirs();
    }
    return TopoDroidApp.APP_FOTO_PATH + mySurvey + "/" + id + ".jpg";
  }

  public String getSurveyZipFile()
  {
    File dir = new File( APP_ZIP_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_ZIP_PATH + mySurvey + ".zip";
  }

  public String getSurveyDatFile( )
  {
    File dir = new File( APP_DAT_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_DAT_PATH + mySurvey + ".dat";
  }

  // public String getSurveyTlxFile( )
  // {
  //   File dir = new File( APP_TLX_PATH );
  //   if (!dir.exists()) dir.mkdirs();
  //   return APP_TLX_PATH + mySurvey + ".tlx";
  // }

  public String getSurveyThFile( )
  {
    File dir = new File( APP_TH_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_TH_PATH + mySurvey + ".th";
  }

  public String getSurveyTroFile( )
  {
    File dir = new File( APP_TRO_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_TRO_PATH + mySurvey + ".tro";
  }

  public String getSurveySvxFile( )
  {
    File dir = new File( APP_SVX_PATH );
    if (!dir.exists()) dir.mkdirs();
    return APP_SVX_PATH + mySurvey + ".svx";
  }

  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  public long setSurveyFromName( String survey ) 
  { 
    mSID = -1;
    mySurvey = null;
    if ( survey != null && mData != null ) {
      mSID = mData.setSurvey( survey );
      // mFixed.clear();
      mySurvey = null;
      if ( mSID > 0 ) {
        mySurvey = survey;
        // restoreFixed();
      }
      return mSID;
    }
    return 0;
  }

  public boolean hasSurveyName( String name ) 
  {
    if ( mData != null ) {
      return mData.hasSurveyName( name );
    }
    return true;
  }

  public boolean hasCalibName( String name ) 
  {
    if ( mData != null ) {
      return mData.hasCalibName( name );
    }
    return true;
  }

  public long setCalibFromName( String calib ) 
  {
    mCID = -1;
    myCalib = null;
    if ( calib != null && mData != null ) {
      mCID = mData.setCalib( calib );
      myCalib = (mCID > 0)? calib : null;
      return mCID;
    }
    return 0;
  }

  public void setSurveyFromId( long id )
  {
    if ( mData != null ) {
      mySurvey = mData.getSurveyFromId( id );
      mSID = 0;
      // mFixed.clear();
      if ( mySurvey != null ) {
        mSID = id;
        // restoreFixed();
      }
    }
  }

  public void setCalibFromId( long id )
  {
    if ( mData != null ) {
      myCalib = mData.getCalibFromId( id );
      mCID = ( myCalib == null )? 0 : id;
    }
  }

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    if ( k.equals( key[0] ) ) {
      mCloseDistance = Float.parseFloat( sp.getString( k, CLOSE_DISTANCE ) );
    // } else if ( k.equals( key[1] ) ) {
    //   setExportType( sp.getString( key[1], EXPORT_TYPE ) );
    } else if ( k.equals( key[2] ) ) {
      mGroupDistance = Float.parseFloat( sp.getString( k, GROUP_DISTANCE ) );
    } else if ( k.equals( key[3] ) ) {
      mCalibEps = Float.parseFloat( sp.getString( k, CALIB_EPS ) );
    } else if ( k.equals( key[4] ) ) {
      mCalibMaxIt = Integer.parseInt( sp.getString( k, CALIB_MAX_ITER ) );
    } else if ( k.equals( key[5] ) ) {
      mDevice = sp.getString( k, DEVICE_NAME );
    } else if ( k.equals( key[7] ) ) {
      mLineSegment = Integer.parseInt( sp.getString( k, LINE_SEGMENT ) );
    } else if ( k.equals( key[8] ) ) {
      mVThreshold = Float.parseFloat( sp.getString( k, V_THRESHOLD ) );
    } else if ( k.equals( key[9] ) ) {
      mLineAccuracy = Float.parseFloat( sp.getString( k, LINE_ACCURACY ) );
    } else if ( k.equals( key[10] ) ) {
      mLineCorner   = Float.parseFloat( sp.getString( k, LINE_CORNER ) );
    } else if ( k.equals( key[11] ) ) {
      setLineStyleAndType( sp.getString( k, LINE_STYLE ) );
    } else if ( k.equals( key[12] ) ) {
      mCheckBT     = sp.getBoolean( k, CHECK_BT );
    } else if ( k.equals( key[13] ) ) {
      mUnit = Float.parseFloat( sp.getString( k, DRAWING_UNIT ) );
      DrawingBrushPaths.doMakePaths( );
    } else if ( k.equals( key[26] ) ) {
      mCloseness = Float.parseFloat( sp.getString( k, CLOSENESS ) );
    } else if ( k.equals( key[14] ) ) {
      mGroupBy = Integer.parseInt( sp.getString( k, GROUP_BY ) );
    } else if ( k.equals( key[15] ) ) {
      mListRefresh = sp.getBoolean( k, LIST_REFRESH );
    } else if ( k.equals( key[16] ) ) {
      mAutoStations = sp.getBoolean( k, AUTO_STATIONS );
    } else if ( k.equals( key[25] ) ) {
      mLoopClosure = sp.getBoolean( k, LOOP_CLOSURE );
    } else if ( k.equals( key[17] ) ) {
      mUnitLength    = sp.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : M2FT;
    } else if ( k.equals( key[18] ) ) {
      mUnitAngle     = sp.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : DEG2GRAD;
    } else if ( k.equals( key[19] ) ) {
      mUnitLocation  = sp.getString( k, UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
    // } else if ( k.equals( key[??] ) ) { // "DISTOX_CONN_MODE" 
    //   mConnectionMode = Integer.parseInt( sp.getString( key[??], "0" ) );
    } else if ( k.equals( key[20] ) ) { // "DISTOX_BASE_PATH" 
      mBasePath = sp.getString( k, mBasePath );
      setPaths( mBasePath );
      // FIXME need to restart the app ?
      mData        = new DataHelper( this );
      mCalibration = new Calibration( 0, this );
    } else if ( k.equals( key[21] ) ) {
      mCheckAttached = sp.getBoolean( k, CHECK_ATTACHED );
    } else if ( k.equals( key[22] ) ) { // "DISTOX_SOCK_TYPE
      mSockType = Integer.parseInt( sp.getString( k, "0" ) );
    } else if ( k.equals( key[23] ) ) { // "DISTOX_COMM_RETRY
      setCommRetry( sp );
    } else if ( k.equals( key[24] ) ) { 
      setExtendThr( sp );

    // ---------------------- LOG PREFERENCES
    } else if ( k.equals( key[27] ) ) { // "DISTOX_LOG_DEBUG",
      LOG_DEBUG = sp.getBoolean( k, false );
    } else if ( k.equals( key[28] ) ) { // "DISTOX_LOG_ERR",
      LOG_ERR = sp.getBoolean( k, true );
    } else if ( k.equals( key[29] ) ) { // "DISTOX_LOG_INPUT",        // 28
      LOG_INPUT = sp.getBoolean( k, false );
    } else if ( k.equals( key[30] ) ) { // "DISTOX_LOG_BT",
      LOG_BT = sp.getBoolean( k, false );
    } else if ( k.equals( key[31] ) ) { // "DISTOX_LOG_COMM",
      LOG_COMM = sp.getBoolean( k, false );
    } else if ( k.equals( key[32] ) ) { // "DISTOX_LOG_PROTO",
      LOG_PROTO = sp.getBoolean( k, false );
    } else if ( k.equals( key[33] ) ) { // "DISTOX_LOG_DISTOX",
      LOG_DISTOX = sp.getBoolean( k, false );
    } else if ( k.equals( key[34] ) ) { // "DISTOX_LOG_DEVICE",       // 33
      LOG_DEVICE = sp.getBoolean( k, false );
    } else if ( k.equals( key[35] ) ) { // "DISTOX_LOG_DATA",
      LOG_DATA = sp.getBoolean( k, false );
    } else if ( k.equals( key[36] ) ) { // "DISTOX_LOG_DB",
      LOG_DB = sp.getBoolean( k, false );
    } else if ( k.equals( key[37] ) ) { // "DISTOX_LOG_CALIB",
      LOG_CALIB = sp.getBoolean( k, false );
    } else if ( k.equals( key[38] ) ) { // "DISTOX_LOG_FIXED",
      LOG_FIXED = sp.getBoolean( k, false );
    } else if ( k.equals( key[39] ) ) { // "DISTOX_LOG_LOC",          // 38
      LOG_LOC = sp.getBoolean( k, false );
    } else if ( k.equals( key[40] ) ) { // "DISTOX_LOG_PHOTO",
      LOG_PHOTO = sp.getBoolean( k, false );
    } else if ( k.equals( key[41] ) ) { // "DISTOX_LOG_SENSOR"        // 40
      LOG_SENSOR = sp.getBoolean( k, false );
    // } else if ( k.equals( key[42] ) ) { // "DISTOX_LOG_SHOT"        
    //   LOG_SHOT = sp.getBoolean( k, false );
    } 
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  void setBooleanPreference( String preference, boolean val )
  {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean( preference, val );
    editor.commit(); // Very important to save the preference
  }

  void setDevice( String device ) 
  { 
    mDevice = device;
    if ( prefs != null ) {
      Editor editor = prefs.edit();
      editor.putString( key[5], mDevice ); 
      editor.commit();
    }
  }

  public int downloadData()
  {
    // Log.v( TAG, "downloadData() device " + mDevice );
    if ( mComm != null && mDevice != null ) {
      return mComm.downloadData( mDevice );
    }
    return 0;
  }

  // =======================================================================
  // THERION EXPORT Therion

  static String[] therion_extend = { "left", "vertical", "right", "ignore" };
  static String   therion_flags_duplicate     = "   flags duplicate\n";
  static String   therion_flags_not_duplicate = "   flags not duplicate\n";
  static String   therion_flags_surface       = "   flags surface\n";
  static String   therion_flags_not_surface   = "   flags not surface\n";

  public String exportSurveyAsTh()
  {
    String filename = getSurveyThFile();
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    List< FixedInfo > fixed = mData.selectAllFixed( mSID, STATUS_NORMAL );
    List< PlotInfo > plots  = mData.selectAllPlots( mSID, STATUS_NORMAL );
    try {
      SurveyInfo info = mData.selectSurveyInfo( mSID );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("survey %s -title \"%s\"\n", mySurvey, mySurvey );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");
      pw.format("  centerline\n");

      if ( fixed.size() > 0 ) {
        pw.format("    cs long-lat\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("    # fix %s m\n", fix.toString() );
        }
      }
      pw.format("    date %s \n", info.date );
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("    # team %s \n", info.team );
      }

      pw.format("    data normal from to length compass clino\n");

      long extend = 0;  // current extend
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;

      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.mType == DistoXDBlock.BLOCK_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            pw.format("    - %s ", to );
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // finish writing previous leg shot
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            pw.format("    %s - ", from ); // write splay shot
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          } else {
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              n = 0;
            }
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              pw.format(therion_flags_duplicate);
              duplicate = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              pw.format(therion_flags_surface);
              surface = true;
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            pw.format("    %s %s ", from, to );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            n = 1;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 ) {
        b = in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
        if ( duplicate ) {
          pw.format(therion_flags_not_duplicate);
          // duplicate = false;
        }
      }
      pw.format("  endcenterline\n");

      for ( PlotInfo plt : plots ) {
        pw.format("  # input \"%s-%s.th2\"\n", mySurvey, plt.name );
      }

      pw.format("endsurvey\n");
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

  // -----------------------------------------------------------------------
  /** SURVEX EXPORT 
   *
   * The following format is used to export the centerline data in survex
   *
   *    *begin survey_name
   *      *units tape feet|metres
   *      *units compass clino grad|degrees
   *      *calibrate declination ...
   *      *date yyyy.mm.dd
   *      ; *fix station long lat alt
   *      ; *team "teams"
   *      *data normal from to tape compass clino
   *      ...
   *      *flags surface|not surface
   *      *flags duplicate|not duplicate
   *      *flags splay|not splay
   *      ...
   *      ; shot_comment
   *      ...
   *      (optional survey commands)
   *    *end survey_name
   */
  static String   survex_flags_duplicate     = "   *flags duplicate\n";
  static String   survex_flags_not_duplicate = "   *flags not duplicate\n";
  static String   survex_flags_surface       = "   *flags surface\n";
  static String   survex_flags_not_surface   = "   *flags not surface\n";

  public String exportSurveyAsSvx()
  {
    String filename = getSurveySvxFile();
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    List< FixedInfo > fixed = mData.selectAllFixed( mSID, STATUS_NORMAL );
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("*begin %s\n", mySurvey );
      pw.format("  *units tape meters\n");
      pw.format("  *units compass degrees\n");
      pw.format("  *units clino degrees\n");
      pw.format("; %s \n\n", mData.getSurveyComment( mSID ) );
      pw.format("  *date %s \n", mData.getSurveyDate( mSID ) );
      pw.format("  ; *team \"%s\" \n", info.team );
      if ( fixed.size() > 0 ) {
        pw.format("  ; fix stations as lomg-lat alt\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("  ; *fix %s\n", fix.toString() );
        }
      }
      pw.format("  *data normal from to tape compass clino\n");
      
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean splays = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.mType == DistoXDBlock.BLOCK_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "  %.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", ref_item.mComment );
            }

            if ( ! splays ) {
              pw.format("  *flags splay\n" );
              splays = true;
            }
            pw.format(Locale.ENGLISH, "  - %s %.2f %.1f %.1f\n", to, item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", item.mComment );
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "  %.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", ref_item.mComment );
            }

            if ( ! splays ) {
              pw.format("  *flags splay\n" );
              splays = true;
            }
            pw.format(Locale.ENGLISH, "  %s - %.2f %.1f %.1f\n", from, item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", item.mComment );
            }
          } else {
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              n = 0;
            }
            if ( splays ) {
              pw.format("  *flags not splay\n");
              splays = false;
            }
            ref_item = item;
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              pw.format(survex_flags_duplicate);
              duplicate = true;
            }
            pw.format("    %s %s ", from, to );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            n = 1;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 ) {
        b = in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
        if ( duplicate ) {
          pw.format(survex_flags_not_duplicate);
          // duplicate = false;
        }
      }
      pw.format("*end %s\n", mySurvey );
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

  // -----------------------------------------------------------------------
  // TOPOLINUX EXPORT 

  // public String exportSurveyAsTlx()
  // {
  //   File dir = new File( TopoDroidApp.APP_TLX_PATH );
  //   if (!dir.exists()) {
  //     dir.mkdirs();
  //   }
  //   String filename = TopoDroidApp.APP_TLX_PATH + mySurvey + ".tlx";
  //   List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
  //   try {
  //     FileWriter fw = new FileWriter( filename );
  //     PrintWriter pw = new PrintWriter( fw );
  //     pw.format("tlx2\n");
  //     pw.format("# date %s \n", mData.getSurveyDate( mSID ) );
  //     pw.format("# %s \n", mData.getSurveyComment( mSID ) );
  //     int n = 0;
  //     float l=0.0f, b=0.0f, c=0.0f;
  //     float l0[] = new float[10];
  //     float b0[] = new float[10];
  //     float c0[] = new float[10];
  //     float r0[] = new float[10];
  //     DistoXDBlock ref_item = null;
  //     int extend = 0;
  //     int flag   = DistoXDBlock.BLOCK_SURVEY;

  //     for ( DistoXDBlock item : list ) {
  //       String from = item.mFrom;
  //       String to   = item.mTo;
  //       if ( from != null && from.length() > 0 ) {
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 ) {
  //             b = in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           ref_item = item;
  //           // item.Comment()
  //           pw.format("    \"%s\" \"%s\" ", from, to );
  //           l = item.mLength;
  //           b = item.mBearing;
  //           c = item.mClino;
  //           extend = (int) item.mExtend;
  //           flag   = (int) item.mFlag;
  //           l0[0] = item.mLength;
  //           b0[0] = item.mBearing;
  //           c0[0] = item.mClino;
  //           r0[0] = item.mRoll;
  //           n = 1;
  //         } else { // to.isEmpty()
  //           if ( n > 0 ) {
  //             b = in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1fi %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"%s\" \"\" ", from );
  //           pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         }
  //       } else { // from.isEmpty()
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 ) {
  //             b = in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"\" \"%s\" ", to );
  //           pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         } else {
  //           // not exported
  //           if ( item.mType == DistoXDBlock.BLOCK_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
  //             float bb = around( item.mBearing, b0[0] );
  //             l += item.mLength;
  //             b += bb;
  //             c += item.mClino;
  //             l0[n] = item.mLength;
  //             b0[n] = item.mBearing;
  //             c0[n] = item.mClino;
  //             r0[n] = item.mRoll;
  //             ++n;
  //           }
  //         }
  //       }
  //     }
  //     if ( n > 0 ) {
  //       b = in360( b/n );
  //       pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //       while ( n > 0 ) {
  //         -- n;
  //         pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //       }
  //       // extend = 0;
  //       // flag   = DistoXDBlock.BLOCK_SURVEY;
  //     }
  //     // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d %d\n", 
  //     //   item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, 0, 1 );
  //     // item.mComment
  //     fw.flush();
  //     fw.close();
  //     return filename;
  //   } catch ( IOException e ) {
  //     return null;
  //   }
  // }

  // -----------------------------------------------------------------------
  // COMPASS EXPORT 

  private float in360( float b ) { return (b<0.0f)? b+360.0f : b; }

  private float around( float bb, float b0 ) 
  {
    while ( ( bb - b0 ) > 180.0f ) bb -= 360.0f;
    while ( ( b0 - bb ) > 180.0f ) bb += 360.0f;
    return bb;
  }

  private class LRUD 
  {
    float l, r, u, d;

    LRUD() 
    {
      l = 0.0f;
      r = 0.0f;
      u = 0.0f;
      d = 0.0f;
    }
  }

  private LRUD computeLRUD( DistoXDBlock b, List<DistoXDBlock> list, boolean at_from )
  {
    LRUD lrud = new LRUD();
    float grad2rad = TopoDroidApp.GRAD2RAD_FACTOR;
    float n0 = (float)Math.cos( b.mBearing * grad2rad );
    float e0 = (float)Math.sin( b.mBearing * grad2rad );
    float cc0 = (float)Math.cos( b.mClino * grad2rad );
    float sc0 = (float)Math.sin( b.mClino * grad2rad );
    float cb0 = n0;
    float sb0 = e0;
    float sc02 = sc0 * sc0;
    float cc02 = 1.0f - sc02;
    String station = ( at_from ) ? b.mFrom : b.mTo;
    
    for ( DistoXDBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      if ( from == null || from.length() == 0 ) { // skip blank
        // if ( to == null || to.length() == 0 ) continue;
        continue;
      } else { // skip leg
        if ( to != null && to.length() > 0 ) continue;
      }
      if ( station.equals( from ) ) {
        float cb = (float)Math.cos( item.mBearing * grad2rad );
        float sb = (float)Math.sin( item.mBearing * grad2rad );
        float cc = (float)Math.cos( item.mClino * grad2rad );
        float sc = (float)Math.sin( item.mClino * grad2rad );
        float len = item.mLength;
        // float z1 = sc02 * sc;      // first point: horizontal projection [times sc02]
        // float n1 = sc02 * cc * cb;
        // float e1 = sc02 * cc * sb;
        float cbb0 = sb*sb0 + cb*cb0;
        // len * ( second_point - first_point )
        float z1 = len * ( sc * cc02 - cc * cc0 * sc0 * cbb0 + sc02 * sc);
        float n1 = len * cc * ( cc02 * ( cb - cb0 * cbb0 )   + sc02 * cb);
        float e1 = len * cc * ( cc02 * ( sb - sb0 * cbb0 )   + sc02 * sb);
        if ( z1 > 0.0 ) { if ( z1 > lrud.u ) lrud.u = z1; }
        else            { if ( -z1 > lrud.d ) lrud.d = -z1; }
        float rl = e1 * n0 - n1 * e0;
        if ( rl > 0.0 ) { if ( rl > lrud.r ) lrud.r = rl; }
        else            { if ( -rl > lrud.l ) lrud.l = -rl; }
      }
    }
    return lrud;
  }

  /** Centerline data are exported in Compass format as follows
   *    SURVEY NAME: survey_name
   *    SURVEY DATE: mm dd yyyy
   *    SURVEY TEAM:
   *    team_line
   *    DECLINATION: declination  FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00
   *    FROM TO LENGTH BEARING INC FLAGS COMMENTS
   *    ...
   *    0x0c
   *
   * Notes.
   * Names must limited to 14 characters: this include the "prefix" and the station FROM and TO names.
   * Distances are in feet.
   * The flags string is composed as "#|...#", Flags characters: L (duplicate) P (no plot) X (surface).
   * Splay shots are not exported, they may be used to find transversal dimensions, if LRUD are not provided  
   * Multisurvey file is possible.
   */

  private void printShotToDat( PrintWriter pw, float l, float b, float c, int n, LRUD lrud,
                               boolean duplicate, String comment )
  {
    b = in360( b/n );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", (l/n)*M2FT, b, c/n, 
      lrud.l*M2FT, lrud.u*M2FT, lrud.d*M2FT, lrud.r*M2FT );
    if ( duplicate ) {
      pw.format(" #|L#");
    }
    if ( comment != null && comment.length() > 0 ) {
      pw.format(" %s", comment );
    }
    pw.format( "\r\n" );
  }

  public String exportSurveyAsDat()
  {
    String filename = getSurveyDatFile();
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("%s\r\n", mySurvey ); // export as single survey
      pw.format("SURVEY NAME: %s\r\n", mySurvey );
      String date = mData.getSurveyDate( mSID );
      int y = Integer.parseInt( date.substring(0,4) );
      int m = Integer.parseInt( date.substring(5,7) );
      int d = Integer.parseInt( date.substring(8,10) );
      pw.format("SURVEY DATE: %d %d %d\r\n", m, d, y ); // format "MM DD YYYY"

      pw.format("SURVEY TEAM:\r\n...\r\n" );
      pw.format("DECLINATION: 0.0  " );
      pw.format("FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00\r\n" );
      pw.format("\r\n" );
      pw.format("FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n" );
      pw.format( "\r\n" );

      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f; // shot average values
      int n = 0;

      DistoXDBlock ref_item = null;
      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.mType == DistoXDBlock.BLOCK_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", mySurvey, ref_item.mFrom, mySurvey, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              LRUD lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", mySurvey, ref_item.mFrom, mySurvey, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", mySurvey, ref_item.mFrom, mySurvey, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
            }
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            n = 1;
          }
        }
      }
      if ( n > 0 ) {
        LRUD lrud = computeLRUD( ref_item, list, true );
        pw.format("%s-%s %s-%s ", mySurvey, ref_item.mFrom, mySurvey, ref_item.mTo );
        printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\f\r\n" );

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }


  // -----------------------------------------------------------------------
  // VISUALTOPO EXPORT 
  // FIXME photos

  private void printStartShotToTro( PrintWriter pw, DistoXDBlock item, List< DistoXDBlock > list )
  {
    LRUD lrud = computeLRUD( item, list, true );
    pw.format(Locale.ENGLISH, "%s %s 0.00 0.00 0.00 ", item.mFrom, item.mFrom );
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  private void printShotToTro( PrintWriter pw, DistoXDBlock item, float l, float b, float c, int n, LRUD lrud )
  {
    b = in360( b/n );
    pw.format("%s %s ", item.mFrom, item.mTo );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f ", (l/n), b, c/n );
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    // if ( duplicate ) {
    //   // pw.format(" #|L#");
    // }
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  public String exportSurveyAsTro()
  {
    String filename = getSurveyTroFile();
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    List< FixedInfo > fixed = mData.selectAllFixed( mSID, STATUS_NORMAL );
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("Version 5.02\r\n\r\n");
      if ( fixed.size() > 0 ) {
        // pw.format("Trou %s,%.2f,%.2f,LT2E\r\n", mySurvey, fix.lat, fix.lng );
        for ( FixedInfo fix : fixed ) {
          // pw.format("Entree %s\r\n", fix.name );
          break;
        }
      } else {
        // pw.format("Trou %s\r\n", mySurvey );
      }
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("Club %s\r\n", info.team );
      }
      pw.format("Couleur 0,0,0\r\n\r\n");
      
      pw.format("Param Deca Degd Clino Degd 0.0000 Dir,Dir,Dir Arr Inc 0,0,0\r\n\r\n");

      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f; // shot average values
      int n = 0;

      DistoXDBlock ref_item = null;
      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      boolean start = true;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.mType == DistoXDBlock.BLOCK_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              LRUD lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              LRUD lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 ) {
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              LRUD lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
            }
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            n = 1;
          }
        }
      }
      if ( n > 0 ) {
        if ( start ) {
          printStartShotToTro( pw, ref_item, list );
          start = false;
        }
        LRUD lrud = computeLRUD( ref_item, list, false );
        printShotToTro( pw, ref_item, l, b, c, n, lrud );
      }

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

}
