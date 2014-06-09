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
 * 20130130 bug fix: export shot-type check for BLOCK_SEC_LEG
 * 20130204 disable lock
 * 20130504 DXF export
 * 20130520 altimetric altitude
 * 20130829 mLineShift pref
 * 20130920 defult CS pref
 * 20121116 DistoX types: A3 X310 ...
 * 20140103 moved math consts in util
 * 20140220 use current units to convert manual input to meters/degrees
 * 20140224 LOG_UNITS
 * 20140303 option: symbol picker mode (list or grid)
 * 20140305 immediate update of point symbols upon drawing unit pref. change
 * 20140328 option mHThreshold (theshold for horizontal cross-sections)
 * 20140329 raw data option
 * 20140401 log preferences selection bug fix
 * 20140408 check that manual values are not illegal (out-of-bounds)
 * 20140409 returned to TAG "DistoX"
 * 20140415 using GPSAveraging optional (default no)
 * 20140415 commented TdSymbol stuff
 * 20140508 removed DISABLE_KEYGUARD
 * 20140515 lastShotId and secondLastShotId
 * 20140520 LOG_PTOPO
 * 20140606 auto-station option: splays before or after the shot
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
import android.content.pm.PackageManager.NameNotFoundException;

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
  static String VERSION = "0.0.0"; 
  static int VERSION_CODE = 0;
  static int MAJOR = 0;
  static int MINOR = 0;
  static int SUB   = 0;
  static final int MAJOR_MIN = 2; // minimum compatible version
  static final int MINOR_MIN = 1;
  static final int SUB_MIN   = 1;
  
  static final String TAG = "DistoX";

  // static boolean mHideHelp = false;

  // ---------------------------------------------------------
  // DEBUG: logcat flags

  static boolean LOG_BEZIER = false;
  static boolean LOG_BT     = false;   // bluetooth
  static boolean LOG_CALIB  = false;
  static boolean LOG_COMM   = false;   // connection
  static boolean LOG_CSURVEY = false;
  static boolean LOG_DATA   = false;   // shot data
  static boolean LOG_DB     = false;   // sqlite database
  static boolean LOG_DEBUG  = false;
  static boolean LOG_DEVICE = false;
  static boolean LOG_DISTOX = false;   // DistoX packets
  static boolean LOG_ERR    = true;
  static boolean LOG_FIXED  = false;
  static boolean LOG_INPUT  = false;   // user input
  static boolean LOG_LOC    = false;   // location manager
  static boolean LOG_NOTE   = false;   // annotation
  static boolean LOG_MAIN   = false;   // main app
  static boolean LOG_NAME   = false;   // names
  static boolean LOG_NUM    = false;  
  static boolean LOG_PATH   = false;
  static boolean LOG_PLOT   = false;
  static boolean LOG_PHOTO        = false;   // photos
  static boolean LOG_PREFS  = false;   // preferences
  static boolean LOG_PROTO        = false;   // protocol
  static boolean LOG_PTOPO  = false;   // PocketTopo
  static boolean LOG_SENSOR       = false;   // sensors and measures
  static boolean LOG_SHOT   = false;   // shot
  static boolean LOG_STATS  = false;
  static boolean LOG_SURVEY = false;
  static boolean LOG_THERION= false;
  static boolean LOG_ZIP    = false;   // archive
  static boolean LOG_UNITS  = false;

  static void Log( boolean flag, String msg )
  {
    if ( flag ) {
      Log.v( TAG, msg );
    }
  }

  // -----------------------------------------------------
  // sleep
  static int mScreenTimeout = 60000; // 60 secs
  static int mTextSize = 12; // list text size

  private SharedPreferences prefs;
  boolean askSymbolUpdate = false; // by default do not ask

  static final int STATUS_NORMAL  = 0;   // item (shot, plot) status
  static final int STATUS_DELETED = 1;  

  static final int COLOR_NORMAL    = 0xffcccccc; // title colors
  static final int COLOR_CONNECTED = 0xffff6666; 
  static final int COLOR_COMPUTE   = 0xffff33cc;

  // -----------------------------------------------------------
  
  // final static int CONN_MODE_BATCH = 0; // DistoX connection mode
  // final static int CONN_MODE_CONTINUOUS = 1;
  // int mConnectionMode = CONN_MODE_BATCH; 

  String[] DistoXConnectionError;
  BluetoothAdapter mBTAdapter = null;     // BT connection
  DistoXComm mComm = null;                // BT communication
  static DataHelper mData = null;                // database 

  SurveyActivity mSurveyActivity = null;
  ShotActivity mShotActivity  = null;
  TopoDroidActivity mActivity = null; 

  // static final int DISTO_NONE = 0;  // supported Disto types
  // static final int DISTO_A3 = 1;
  // static final int DISTO_X310 = 2;
  // static int mDistoType = DISTO_A3;

  long mSID   = -1;   // id of the current survey
  long mCID   = -1;   // id of the current calib
  String mySurvey;   // current survey name
  String myCalib;    // current calib name
  Calibration mCalibration    = null;     // current calibration 
  static boolean mRawData;                // whether to display calibration raw data as well
  long mSecondLastShotId = 0L;

  public long lastShotId( ) { return mData.getLastShotId( mSID ); }
  public long secondLastShotId( ) { return mSecondLastShotId; }

  // ---------------------------------------------------------

  static int mCompassReadings = 4; // number of compass readings to average

  static float mCloseDistance = 1.0f; // FIXME kludge

  // selection_radius = cutoff + closeness / zoom
  static final float mCloseCutoff = 0.01f; // minimum selection radius
  static float mCloseness;                 // selection radius

  // ---------------------------------------------------------
  // PREFERENCES

  boolean mWelcomeScreen;  // whether to show the welcome screen
  // boolean mTdSymbol;       // whether to ask TdSymbol
  // boolean mStartTdSymbol;  // whether to start TdSymbol (result "yes" of TdSymbolDialog)
  static String  mManual;  // manual url

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION

  static final int ALT_WGS84 = 0; // WGS84 altitude
  static final int ALT_ASL = 1;   // altimetric altitude

  static String mCRS;    // default coord ref systen 
  static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss
  static int mAltitude;     // location altitude type
  static boolean mUseGPSAveraging = false;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  // calibration data grouping policies
  public static final int GROUP_BY_DISTANCE = 0;
  public static final int GROUP_BY_FOUR     = 1;
  public static final int GROUP_BY_ONLY_16  = 2;

  float mGroupDistance;
  int mGroupBy;          // how to group calib data
  float mCalibEps;       // calibartion epsilon
  int   mCalibMaxIt;     // calibration max nr of iterations

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  Device mDevice = null;
  int    distoType() { return (mDevice == null)? 0 : mDevice.mType; }
  String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }

  // private boolean mSaveOnDestroy = SAVE_ON_DESTROY;
  // int   mDefaultConnectionMode;
  int mCheckBT;        // BT: 0 disabled, 1 check on start, 2 enabled

  static final int TOPODROID_SOCK_DEFAULT      = 0;    // BT socket type
  static final int TOPODROID_SOCK_INSEC        = 1;
  // static final int TOPODROID_SOCK_INSEC_RECORD = 2;
  // static final int TOPODROID_SOCK_INSEC_INVOKE = 3;
  static int mSockType = TOPODROID_SOCK_DEFAULT; // FIXME static
  static int mCommRetry = 1;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  float mVThreshold;         // verticality threshold (LRUD)
  static float mHThreshold;  // horizontal plot threshold
  boolean mCheckAttached;    // whether to check is there are shots non-attached
  private int     mSurveyStations;   // automatic survey stations: 0 no, 1 forward-after-splay, 2 backward-after-splay
  boolean mShotAfterSplays;  //                                  3 forward-before-splay, 4 backward-before-splay

  static boolean mLoopClosure;  // whether to do loop closure
  static float mStationSize;
  
  // conversion factor from internal units (m) to user units
  public static float mUnitLength = 1.0f;
  public static float mUnitAngle  = 1.0f;
  public static double mExtendThr = 30.0f;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SKETCH

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;
  public static final String EXTEND_THR = "30"; // extend vertically splays in [90-30, 90+30] of the leg

  static final int PICKER_LIST = 0;
  static final int PICKER_GRID = 1;

  public static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  public static final int LINE_STYLE_NONE   = 1;
  public static final int LINE_STYLE_TWO    = 2;
  public static final int LINE_STYLE_THREE  = 3;
  static int mLineStyle;       // line style: BEZIER, NONE, TWO, THREE
  static int mLineType;        // line type:  1       1     2    3
  static int mPickerType = 0;

  static final float LEN_THR    = 20.0f; // corner detection length
  static final float TO_THERION = 5.0f;  // therion export scale-factor

  static float mUnit; // drawing unit

  int   mLineSegment;
  float mLineAccuracy;
  float mLineCorner;            // corner threshold
  // boolean mListRefresh;      // whether to refresh list on edit-dialog ok-return
  static boolean mAutoStations; // whether to add stations automatically to scrap therion files
  static boolean mEnableZip;      // whether zip saving is enabled or must wait (locked by th2. saving thread)

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // 3D
  boolean mSketches;         // whether to use 3D sketches
  static float mDeltaExtrude;
  static boolean mSketchUsesSplays; // whether 3D sketches surfaces use splays
  static float mSketchLineStep;
  static float mSketchBorderStep;
  static float mSketchSectionStep;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  static float mAccelerationThr; // acceleration threshold (shot quality)
  static float mMagneticThr;     // magnetic threshold
  static float mDipThr;          // dip threshold
  static float mAccelerationMean = 0.0f;
  static float mMagneticMean     = 0.0f;
  static float mDipMean          = 0.0f;

  static boolean isBlockAcceptable( float acc, float mag, float dip )
  {
    return true
        && Math.abs( acc - TopoDroidApp.mAccelerationMean ) < TopoDroidApp.mAccelerationThr
        && Math.abs( mag - TopoDroidApp.mMagneticMean ) < TopoDroidApp.mMagneticThr
        && Math.abs( dip - TopoDroidApp.mDipMean ) < TopoDroidApp.mDipThr
    ;
  }

  // ------------------------------------------------------------
  // CONSTS
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

  // ------------------------------------------------------------
  // PATHS

  String  mBasePath = Environment.getExternalStorageDirectory().getAbsolutePath(); // app base path

  private static String APP_BASE_PATH; //  = Environment.getExternalStorageDirectory() + "/TopoDroid/";
  // private static String APP_TLX_PATH ; //  = APP_BASE_PATH + "tlx/";
  private static String APP_DAT_PATH ; //  = APP_BASE_PATH + "dat/";
  private static String APP_SVX_PATH ; //  = APP_BASE_PATH + "svx/";
  private static String APP_CSV_PATH ; //  = APP_BASE_PATH + "csv/";
  private static String APP_CSX_PATH ; //  = APP_BASE_PATH + "csx/";
  private static String APP_TOP_PATH ; //  = APP_BASE_PATH + "top/";
  private static String APP_TH_PATH  ; //  = APP_BASE_PATH + "th/";
  private static String APP_TH2_PATH ; //  = APP_BASE_PATH + "th2/";
  private static String APP_TH3_PATH ; //  = APP_BASE_PATH + "th3/";
  private static String APP_DXF_PATH ; //  = APP_BASE_PATH + "dxf/";
  private static String APP_TRO_PATH ; //  = APP_BASE_PATH + "tro/";
  private static String APP_PNG_PATH; //  = APP_BASE_PATH + "png/";
  private static String APP_NOTE_PATH; //  = APP_BASE_PATH + "note/";
  private static String APP_FOTO_PATH; //  = APP_BASE_PATH + "photo/";
  private static String APP_IMPORT_PATH; //  = APP_BASE_PATH + "import/";
  private static String APP_ZIP_PATH; //  = APP_BASE_PATH + "zip/";
  static String APP_SYMBOL_PATH;
  static String APP_SYMBOL_SAVE_PATH;
  static String APP_POINT_PATH; //  = APP_BASE_PATH + "symbol/point/";
  static String APP_LINE_PATH; 
  static String APP_AREA_PATH;
  static String APP_SAVE_POINT_PATH; //  = APP_BASE_PATH + "symbol/point/";
  static String APP_SAVE_LINE_PATH; 
  static String APP_SAVE_AREA_PATH;

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

    APP_CSV_PATH    = APP_BASE_PATH + "csv/";
    dir = new File( APP_CSV_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_CSX_PATH    = APP_BASE_PATH + "csx/";
    dir = new File( APP_CSX_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TOP_PATH    = APP_BASE_PATH + "top/";
    dir = new File( APP_TOP_PATH );
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

    APP_DXF_PATH    = APP_BASE_PATH + "dxf/";
    dir = new File( APP_DXF_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_TRO_PATH    = APP_BASE_PATH + "tro/";
    dir = new File( APP_TRO_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_PNG_PATH   = APP_BASE_PATH + "png/";
    dir = new File( APP_PNG_PATH );
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

    APP_SYMBOL_PATH  = APP_BASE_PATH + "symbol/";
    dir = new File( APP_SYMBOL_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_SYMBOL_SAVE_PATH  = APP_BASE_PATH + "symbol/save/";
    dir = new File( APP_SYMBOL_SAVE_PATH );
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

    APP_SAVE_POINT_PATH  = APP_BASE_PATH + "symbol/save/point/";
    dir = new File( APP_POINT_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_SAVE_LINE_PATH   = APP_BASE_PATH + "symbol/save/line/";
    dir = new File( APP_LINE_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

    APP_SAVE_AREA_PATH   = APP_BASE_PATH + "symbol/save/area/";
    dir = new File( APP_AREA_PATH );
    if ( ! dir.exists() ) dir.mkdirs( );

  }

  // ---------------------------------------------------------
  // PREFERENCES KEYS

  static final int indexKeyHideHelp   = 0;
  static final int indexKeyDeviceName = 23;

  public static final String[] key = { // prefs keys
    "DISTOX_HIDE_HELP",           //  0 HIDE_HELP indexKeyHideHelp
    "DISTOX_BASE_PATH",           //  1
    "DISTOX_TEXT_SIZE",           //  2
    // ------------------- SURVEY PREFERENCES
    "DISTOX_CLOSE_DISTANCE",      //  3
    "DISTOX_EXTEND_THR2",         //  4
    "DISTOX_VTHRESHOLD",          //  5
    "DISTOX_SURVEY_STATION",      //  6 // DISTOX_SURVEY_STATIONS must not be used
    "DISTOX_UNIT_LENGTH",
    "DISTOX_UNIT_ANGLE",
    "DISTOX_ACCEL_THR",           //  9
    "DISTOX_MAG_THR",
    "DISTOX_DIP_THR",             // 11
    "DISTOX_LOOP_CLOSURE",        // 12
    "DISTOX_CHECK_ATTACHED",      // 13

    "DISTOX_UNIT_LOCATION",       // 14 "DISTOX_CONN_MODE" is no longer used
    "DISTOX_ALTITUDE",            // 15
    "DISTOX_CRS",                 // 16
    "DISTOX_GPS_AVERAGING",       // 17

    "DISTOX_GROUP_BY",            // 18
    "DISTOX_GROUP_DISTANCE",
    "DISTOX_CALIB_EPS",           // 20
    "DISTOX_CALIB_MAX_IT",
    "DISTOX_RAW_DATA",            // 22

    "DISTOX_DEVICE",              // 23 N.B. indexKeyDeviceName
    "DISTOX_BLUETOOTH",           // 24
    "DISTOX_SOCK_TYPE",
    "DISTOX_COMM_RETRY",          // 26

    "DISTOX_AUTO_STATIONS",       // 27 
    "DISTOX_CLOSENESS",           // 28
    "DISTOX_LINE_SEGMENT",
    "DISTOX_LINE_ACCURACY",
    "DISTOX_LINE_CORNER",         // 31
    "DISTOX_LINE_STYLE",          // 32
    "DISTOX_DRAWING_UNIT",        // 33
    "DISTOX_PICKER_TYPE",         // 34
    "DISTOX_HTHRESHOLD",          // 35
    "DISTOX_STATION_SIZE",        // 36

    // "DISTOX_SKETCH_USES_SPLAYS",  // 37
    // "DISTOX_SKETCH_LINE_STEP",
    // "DISTOX_SKETCH_BERDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP", // 40
    // "DISTOX_DELTA_EXTRUDE",       // 41
    // "DISTOX_COMPASS_READINGS",    // 42
  };

  public static final String[] log_key = {
    // --------------- LOG PREFERENCES ----------------------
    "DISTOX_LOG_DEBUG",           // + 0
    "DISTOX_LOG_ERR",
    "DISTOX_LOG_INPUT",           // + 2
    "DISTOX_LOG_BT",              // + 3
    "DISTOX_LOG_COMM",
    "DISTOX_LOG_PROTO",
    "DISTOX_LOG_DISTOX",
    "DISTOX_LOG_DEVICE",          // + 7
    "DISTOX_LOG_DATA",
    "DISTOX_LOG_DB",              // + 9
    "DISTOX_LOG_CALIB",
    "DISTOX_LOG_FIXED",
    "DISTOX_LOG_LOC",             // +12
    "DISTOX_LOG_PHOTO",
    "DISTOX_LOG_SENSOR",          // +14
    "DISTOX_LOG_SHOT",
    "DISTOX_LOG_SURVEY",
    "DISTOX_LOG_NUM",             // +17
    "DISTOX_LOG_THERION",
    "DISTOX_LOG_PLOT",            // +19
    "DISTOX_LOG_BEZIER",
    "DISTOX_LOG_CSURVEY",         // +21
    "DISTOX_LOG_PTOPO",           // +22
    "DISTOX_LOG_ZIP",
    "DISTOX_LOG_UNITS"
  };

  public static final int DISTOX_EXPORT_TH  = 0;
  // public static final int DISTOX_EXPORT_TLX = 1;
  public static final int DISTOX_EXPORT_DAT = 2;
  public static final int DISTOX_EXPORT_SVX = 3;
  public static final int DISTOX_EXPORT_TRO = 4;
  public static final int DISTOX_EXPORT_CSV = 5;
  public static final int DISTOX_EXPORT_DXF = 6;
  public static final int DISTOX_EXPORT_CSX = 7;
  public static final int DISTOX_EXPORT_TOP = 8;
  public static final int DISTOX_EXPORT_MAX = 9;   // placeholder 

  public static final int DISTOX_MIN_ITER   = 50;  // hard limits
  public static final float DISTOX_MAX_EPS  = 0.1f;

  // prefs default values
  public static final  String TEXT_SIZE      = "14";
  public static final  String CLOSE_DISTANCE = "0.05f"; // 50 cm / 1000 cm
  public static final  String V_THRESHOLD    = "80.0f";
  public static final  String LINE_SEGMENT   = "10";    // min pixel-distance between line points
  public static final  String LINE_ACCURACY  = "1.0f";
  public static final  String LINE_CORNER    = "20.0f";
  public static final  String LINE_SHIFT     = "20.0f";
  public static final  String LINE_STYLE     = "0";     // LINE_STYLE_BEZIER
  public static final  String DRAWING_UNIT   = "1.2f";  // UNIT
  public static final  String CLOSENESS      = "16";    // drawing closeness threshold
  public static final  String ALTITUDE       = "1";     // 
  // public static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH
  public static final  String GROUP_DISTANCE = "40.0f";
  public static final  String CALIB_EPS      = "0.0000001f";
  public static final  String CALIB_MAX_ITER = "200";
  public static final  String GROUP_BY       = "0";     // GROUP_BY_DISTANCE
  public static final  String DEVICE_NAME    = "";
  // public static final  boolean SAVE_ON_DESTROY = true;
  public static final  boolean CHECK_BT      = true;
  public static final  boolean CHECK_ATTACHED = false;
  // public static final  boolean LIST_REFRESH  = false;
  public static final  String SURVEY_STATION = "0"; 
  public static final  boolean AUTO_STATIONS = true;
  public static final  boolean LOOP_CLOSURE  = false;
  public static final  String UNIT_LENGTH    = "meters";
  public static final  String UNIT_ANGLE     = "degrees";
  // public static final  String UNIT_ANGLE_GRADS = "grads";
  // public static final  String UNIT_ANGLE_SLOPE = "slope";
  public static final  String UNIT_LOCATION  = "ddmmss";
  public static final  boolean HIDE_HELP     = false;
  public static final  boolean USE_GPSAVERAGING = false;

  // intent extra-names
  public static final String TOPODROID_PLOT_ID     = "topodroid.plot_id";
  public static final String TOPODROID_PLOT_ID2    = "topodroid.plot_id2";
  public static final String TOPODROID_PLOT_NAME   = "topodroid.plot_name";
  public static final String TOPODROID_PLOT_NAME2  = "topodroid.plot_name2";
  public static final String TOPODROID_PLOT_TYPE   = "topodroid.plot_type";
  public static final String TOPODROID_PLOT_FROM   = "topodroid.plot_from";
  public static final String TOPODROID_PLOT_AZIMUTH = "topodroid.plot_azimuth";

  // public static final String TOPODROID_SKETCH_ID   = "topodroid.sketch_id";
  // public static final String TOPODROID_SKETCH_NAME = "topodroid.sketch_name";

  public static final String TOPODROID_SURVEY      = "topodroid.survey";
  public static final String TOPODROID_OLDSID      = "topodroid.old_sid";    // SurveyActivity
  public static final String TOPODROID_OLDID       = "topodroid.old_id";
  public static final String TOPODROID_SURVEY_ID   = "topodroid.survey_id";  // DrawingActivity
  
  public static final String TOPODROID_DEVICE_ACTION = "topodroid.device_action";
  // public static final String TOPODROID_DEVICE_ADDR   = "topodroid.device_addr";
  // public static final String TOPODROID_DEVICE_CNCT   = "topodroid.device_cnct";

  public static final String TOPODROID_SENSOR_TYPE  = "topodroid.sensor_type";
  public static final String TOPODROID_SENSOR_VALUE = "topodroid.sensor_value";
  public static final String TOPODROID_SENSOR_COMMENT = "topodroid.sensor_comment";


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
    Log( LOG_CALIB, "info.device " + ((info == null)? "null" : info.device) );
    Log( LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.mAddress) );
    return ( mDevice == null || ( info != null && info.device.equals( mDevice.mAddress ) ) );
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

  private void setCommRetry( int c )
  {
    mCommRetry = c;
    if ( mCommRetry < 1 ) mCommRetry = 1;
    if ( mCommRetry > 5 ) mCommRetry = 5;
  }

  private void setExtendThr( double e )
  {
    mExtendThr = e;
    if ( mExtendThr < 0.0 ) mExtendThr = 0.0;
    if ( mExtendThr > 90.0 ) mExtendThr = 90.0;
  }

  private void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_BEZIER; // default
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
  // public long getSurveyId() { return mSID; }
  // public long getCalibId()  { return mCID; }
  // public String getSurvey() { return mySurvey; }
  // public String getCalib()  { return myCalib; }


  // ---------------------------------------------------------

  // void clearPreferences()
  // {
  //   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
  //   if ( sp.getBoolean( "update_required", true ) ) {
  //     SharedPreferences.Editor editor = sp.edit();
  //     editor.clear();
  //     // TODO make other updates
  //     editor.putBoolean( "update_required", false );
  //     editor.commit();
  //   }
  // }

  @Override
  public void onCreate()
  {
    super.onCreate();

    try {
      VERSION      = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
      VERSION_CODE = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode;
      int v = VERSION_CODE;
      MAJOR = v / 100000;    
      v -= MAJOR * 100000;
      MINOR = v /   1000;    
      v -= MINOR *   1000;
      SUB = v / 10;
    } catch ( NameNotFoundException e ) {
      // FIXME
      e.printStackTrace();
    }
    // // disable lock
    // KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
    // KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
    // lock.disableKeyguard();

    // try {
    //   mScreenTimeout = System.getInt(getContentResolver(), System.SCREEN_OFF_TIMEOUT );
    // } catch ( SettingNotFoundException e ) {
    // }

    // Log.v(TAG, "onCreate app");
    this.prefs = PreferenceManager.getDefaultSharedPreferences( this );
    this.prefs.registerOnSharedPreferenceChangeListener( this );

    mWelcomeScreen = prefs.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    // mTdSymbol = prefs.getBoolean( "DISTOX_TD_SYMBOL", true );
    // mStartTdSymbol = false;

    mBasePath = prefs.getString( "DISTOX_BASE_PATH", mBasePath );
    setPaths( mBasePath );

    // mHideHelp = prefs.getBoolean( key[indexKeyHideHelp], HIDE_HELP );  // DISTOX_HIDE_HELP
    
    mData = new DataHelper( this );

    mEnableZip = true; // true: can save

    installSymbols( false );

    loadPreferences();

    mDevice = mData.getDevice( prefs.getString( key[indexKeyDeviceName], DEVICE_NAME ) );

    // DrawingBrushPaths.makePaths( getResources() );

    mCalibration = new Calibration( 0, this );
    mConnListener = new ArrayList< Handler >();
    {
      String sketches = mData.getValue("sketch");
      mSketches = sketches != null && sketches.equals("on");
    }

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    if ( mBTAdapter == null ) {
      // Toast.makeText( this, R.string.not_available, Toast.LENGTH_SHORT ).show();
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
    // Log.v( TAG, "display " + mDisplayWidth + " " + mDisplayHeight + " scale " + mScaleFactor );

    loadLogPreferences();

  }

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
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("%s\n", sdf.format( new Date() ) );
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
      String[] ver = line.split("\\.");
      int major = Integer.parseInt( ver[0] );
      int minor = Integer.parseInt( ver[1] );
      int sub   = Integer.parseInt( ver[2] );
      if (    ( major < MAJOR_MIN )
           || ( major == MAJOR_MIN && minor < MINOR_MIN )
           || ( major == MAJOR_MIN && minor == MINOR_MIN && sub < SUB_MIN ) ) {
        Log( LOG_ZIP, "TopDroid version mismatch: found " + line + " expected " + VERSION );
        return -2;
      }
      line = br.readLine().trim();
      int db_version = Integer.parseInt( line );
      if ( ! ( db_version >= DataHelper.DATABASE_VERSION_MIN && db_version <= DataHelper.DATABASE_VERSION ) ) {
        Log( LOG_ZIP, "TopDroid DB version mismatch: found " + line + " expected " + DataHelper.DB_VERSION );
        return -3;
      }
      line = br.readLine().trim();
      if ( ! line.equals( surveyname ) ) return -4;
      fr.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return 0;
  }


  static boolean hasTh2Dir() { return (new File( TopoDroidApp.APP_TH2_PATH )).exists(); }
  static boolean hasTh3Dir() { return (new File( TopoDroidApp.APP_TH3_PATH )).exists(); }
  static boolean hasPngDir() { return (new File( TopoDroidApp.APP_PNG_PATH )).exists(); }

  static String getDirFile( String name )    { return APP_BASE_PATH + name; }
  static String getImportFile( String name ) { return APP_IMPORT_PATH + name; }
  static String getZipFile( String name )    { return APP_ZIP_PATH + name; }
  static String getTh2File( String name )    { return APP_TH2_PATH + name; }
  static String getTh3File( String name )    { return APP_TH3_PATH + name; }

  static String getThFile( String name )     { return APP_TH_PATH + name; }
  static String getDatFile( String name )    { return APP_DAT_PATH + name; }
  static String getDxfFile( String name )    { return APP_DXF_PATH + name; }
  static String getSvxFile( String name )    { return APP_SVX_PATH + name; }
  static String getCsvFile( String name )    { return APP_CSV_PATH + name; }
  static String getCsxFile( String name )    { return APP_CSX_PATH + name; }
  static String getTopFile( String name )    { return APP_TOP_PATH + name; }
  static String getTroFile( String name )    { return APP_TRO_PATH + name; }
  static String getPngFile( String name )    { return APP_PNG_PATH + name; }

  static String getNoteFile( String name )   { return APP_NOTE_PATH + name; }

  static String getJpgDir( String dir ) { return APP_FOTO_PATH + dir; }
  static String getJpgFile( String dir, String name ) { return APP_FOTO_PATH + dir + "/" + name; }

  String getSurveyPlotDxfFile( String name ) { return APP_DXF_PATH + mySurvey + "-" + name + ".dxf"; }
  String getSurveyPlotFile( String name ) { return APP_TH2_PATH + mySurvey + "-" + name + ".th2"; }
  String getSurveySketchFile( String name ) { return APP_TH3_PATH + mySurvey + "-" + name + ".th3"; }
  String getSurveyPngFile( String name )  { return APP_PNG_PATH + mySurvey + "-" + name + ".png"; }


  private static String getFile( String directory, String name, String ext ) 
  {
    File dir = new File( directory );
    if (!dir.exists()) dir.mkdirs();
    return directory + name + "." + ext;
  }

  public static String getSurveyNoteFile( String title ) { return getFile( APP_NOTE_PATH, title, "txt" ); }
  public String getTh2FileWithExt( String name ) { return getFile( APP_TH2_PATH, name, "th2" ); }
  public String getTh3FileWithExt( String name ) { return getFile( APP_TH3_PATH, name, "th3" ); }
  public String getDxfFileWithExt( String name ) { return getFile( APP_DXF_PATH, name, "dxf" ); }
  public String getPngFileWithExt( String name ) { return getFile( APP_PNG_PATH, name, "png" ); }

  public String getSurveyZipFile() { return getFile( APP_ZIP_PATH, mySurvey, "zip" ); }
  public String getSurveyDatFile( ) { return getFile( APP_DAT_PATH, mySurvey, "dat" ); }
  // public String getSurveyTlxFile( ) { return getFile( APP_TLX_PATH, mySurvey, "tlx" ); }
  public String getSurveyThFile( ) { return getFile( APP_TH_PATH, mySurvey, "th" ); }
  public String getSurveyTroFile( ) { return getFile( APP_TRO_PATH, mySurvey, "tro" ); }
  public String getSurveyDxfFile( ) { return getFile( APP_DXF_PATH, mySurvey, "dxf" ); }
  public String getSurveySvxFile( ) { return getFile( APP_SVX_PATH, mySurvey, "svx" ); }
  public String getSurveyCsvFile( ) { return getFile( APP_CSV_PATH, mySurvey, "csv" ); }
  public String getSurveyCsxFile( ) { return getFile( APP_CSX_PATH, mySurvey, "csx" ); }
  public String getSurveyCsxFile( String name ) { return getFile( APP_CSX_PATH, mySurvey + "-" + name, "csx" ); }
  public String getSurveyTopFile( ) { return getFile( APP_TOP_PATH, mySurvey, "top" ); }

  private static File[] getFiles( String dirname, final String ext1, final String ext2 )
  {
    File dir = new File( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            return ( pathname.getName().endsWith( ext1 ) ||  pathname.getName().endsWith( ext2 ) );
          }
        } );
    }
    return null;
  }

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

  public static File[] getImportFiles() { return getFiles( APP_IMPORT_PATH, ".th", ".top" ); }
  public static File[] getZipFiles() { return getFiles( APP_ZIP_PATH, ".zip" ); }
  // public String getSurveyPhotoFile( String name ) { return APP_FOTO_PATH + mySurvey + "/" + name; }

  public String getSurveyPhotoDir( ) { return APP_FOTO_PATH + mySurvey; }

  public String getSurveyJpgFile( String id )
  {
    File imagedir = new File( TopoDroidApp.APP_FOTO_PATH + mySurvey + "/" );
    if ( ! ( imagedir.exists() ) ) {
      imagedir.mkdirs();
    }
    return TopoDroidApp.APP_FOTO_PATH + mySurvey + "/" + id + ".jpg";
  }

  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  public long setSurveyFromName( String survey ) 
  { 
    mSID = -1;       // no survey by default
    mySurvey = null;
    if ( survey != null && mData != null ) {
      mSID = mData.setSurvey( survey );
      // mFixed.clear();
      mySurvey = null;
      if ( mSID > 0 ) {
        mySurvey = survey;
        mSecondLastShotId = lastShotId();
        // restoreFixed();
      }
      return mSID;
    }
    return 0;
  }

  public boolean hasSurveyName( String name ) 
  {
    return ( mData == null ) || mData.hasSurveyName( name );
  }

  public boolean hasCalibName( String name ) 
  {
    return ( mData == null ) || mData.hasCalibName( name );
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
        mSecondLastShotId = lastShotId();
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

  // -----------------------------------------------------------------
  // PREFERENCES

  void loadPreferences()
  {
    // ------------------- SURVEY PREFERENCES
    int k = 3;
    mCloseDistance = Float.parseFloat( prefs.getString( key[k++], CLOSE_DISTANCE ) ); // DISTOX_CLOSE_DISTANCE 3
    setExtendThr( Double.parseDouble( prefs.getString( key[k++], EXTEND_THR ) ) );    // DISTOX_EXTEND_THR2
    mVThreshold    = Float.parseFloat( prefs.getString( key[k++], V_THRESHOLD ) );    // DISTOX_VTHRESHOLD
    mSurveyStations= Integer.parseInt( prefs.getString( key[k++], SURVEY_STATION ) ); // DISTOX_SURVEY_STATION 6
    mShotAfterSplays = ( mSurveyStations <= 2 );
    if ( mSurveyStations > 2 ) mSurveyStations -= 2;

    mUnitLength    = prefs.getString( key[k++], UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : M2FT;
    mUnitAngle     = prefs.getString( key[k++], UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : DEG2GRAD;

    mAccelerationThr  = Float.parseFloat( prefs.getString( key[k++], "300.0f" ) );  // DISTOX_ACCEL_THR 9
    mMagneticThr  = Float.parseFloat( prefs.getString( key[k++], "200.0f" ) );     // DISTOX_MAG_THR
    mDipThr  = Float.parseFloat( prefs.getString( key[k++], "2.0f" ) );            // DISTOX_DIP_THR

    mLoopClosure   = prefs.getBoolean( key[k++], LOOP_CLOSURE );                     // DISTOX_LOOP_CLOSURE 12
    mCheckAttached = prefs.getBoolean( key[k++], CHECK_ATTACHED );                   // DISTOX_CHECK_ATTACHED

    mUnitLocation  = prefs.getString( key[k++], UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
    mAltitude      = Integer.parseInt( prefs.getString( key[k++], ALTITUDE ) );      // DISTOX_ALTITUDE
    mCRS           = prefs.getString( key[k++], "Long-Lat" );                        // DISTOX_CRS
    mUseGPSAveraging = prefs.getBoolean( key[k++], USE_GPSAVERAGING );               // DISTOX_GPS_AVERAGING 17

    Log( LOG_UNITS, "mUnitLength " + mUnitLength );
    Log( LOG_UNITS, "mUnitAngle " + mUnitAngle );
    Log( LOG_UNITS, "mUnitLocation " + mUnitLocation );

    // ------------------- CALIBRATION PREFERENCES
    mGroupBy       = Integer.parseInt( prefs.getString( key[k++], GROUP_BY ) );       // DISTOX_GROUP_BY
    mGroupDistance = Float.parseFloat( prefs.getString( key[k++], GROUP_DISTANCE ) ); // DISTOX_GROUP_DISTANCE
    mCalibEps      = Float.parseFloat( prefs.getString( key[k++], CALIB_EPS ) );      // DISTOX_CALIB_EPS
    mCalibMaxIt    = Integer.parseInt( prefs.getString( key[k++], CALIB_MAX_ITER ) ); // DISTOX_CALIB_MAX_IT
    mRawData       = prefs.getBoolean( key[k++], false );                             // DISTOX_RAW_DATA 22
    
    // ------------------- DEVICE PREFERENCES
    k++; // DISTOX_DEVICE              
    mCheckBT       = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_BLUETOOTH
    mSockType      = Integer.parseInt( prefs.getString( key[k++], "0" ) ); // DISTOX_SOCK_TYPE
    setCommRetry( Integer.parseInt( prefs.getString( key[k++], "1" ) ) ); // DISTOX_COMM_RETRY 26
    // mConnectionMode = Integer.parseInt( prefs.getString( key[], "0" ) ); 

    // -------------------  DRAWING PREFERENCES
    mAutoStations  = prefs.getBoolean( key[k++], AUTO_STATIONS );                    // DISTOX_AUTO_STATIONS
    mCloseness     = Float.parseFloat( prefs.getString( key[k++], CLOSENESS ) );     // DISTOX_CLOSENESS
    mLineSegment   = Integer.parseInt( prefs.getString( key[k++], LINE_SEGMENT ) );  // DISTOX_LINE_SEGMENT
    mLineAccuracy  = Float.parseFloat( prefs.getString( key[k++], LINE_ACCURACY ) ); // DISTOX_LINE_ACCURACY
    mLineCorner    = Float.parseFloat( prefs.getString( key[k++], LINE_CORNER ) );   // DISTOX_LINE_CORNER
    setLineStyleAndType( prefs.getString( key[k++], LINE_STYLE ) );                  // DISTOX_LINE_STYLE
    mUnit          = Float.parseFloat( prefs.getString( key[k++], DRAWING_UNIT ) );  // DISTOX_DRAWING_UNIT
    mPickerType    = Integer.parseInt( prefs.getString( key[k++], "0" ) );           // DISTOX_PICKER_TYPE
    mHThreshold    = Float.parseFloat( prefs.getString( key[k++], "60" ) );          // DISTOX_HTHRESHOLD
    mStationSize   = Float.parseFloat( prefs.getString( key[k++], "14" ) );          // DISTOX_STATION_SIZE 36

    // ------------------- SKETCH PREFERENCES
    // mSketchUsesSplays  = prefs.getBoolean( key[k++], false );
    // mSketchLineStep    = Float.parseFloat( prefs.getString( key[k++], "0.5f") );
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( key[k++], "0.2f") );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( key[k++], "0.5f") );
    // mDeltaExtrude      = Float.parseFloat( prefs.getString( key[k++], "0.3f") );
    // mCompassReadings   = Integer.parseInt( prefs.getString( key[k++], "4" ) );
  }

  void loadLogPreferences() // ---------------------- LOG PREFERENCES
  {
    int lk = 0;
    LOG_DEBUG   = prefs.getBoolean( log_key[lk++], false );
    LOG_ERR     = prefs.getBoolean( log_key[lk++], true );
    LOG_INPUT   = prefs.getBoolean( log_key[lk++], false );
    LOG_BT      = prefs.getBoolean( log_key[lk++], false );
    LOG_COMM    = prefs.getBoolean( log_key[lk++], false );
    LOG_PROTO   = prefs.getBoolean( log_key[lk++], false );
    LOG_DISTOX  = prefs.getBoolean( log_key[lk++], false );
    LOG_DEVICE  = prefs.getBoolean( log_key[lk++], false );
    LOG_DATA    = prefs.getBoolean( log_key[lk++], false );
    LOG_DB      = prefs.getBoolean( log_key[lk++], false );
    LOG_CALIB   = prefs.getBoolean( log_key[lk++], false );
    LOG_FIXED   = prefs.getBoolean( log_key[lk++], false );
    LOG_LOC     = prefs.getBoolean( log_key[lk++], false );
    LOG_PHOTO   = prefs.getBoolean( log_key[lk++], false );
    LOG_SENSOR  = prefs.getBoolean( log_key[lk++], false );
    LOG_SHOT    = prefs.getBoolean( log_key[lk++], false );
    LOG_SURVEY  = prefs.getBoolean( log_key[lk++], false );
    LOG_NUM     = prefs.getBoolean( log_key[lk++], false );
    LOG_THERION = prefs.getBoolean( log_key[lk++], false );
    LOG_PLOT    = prefs.getBoolean( log_key[lk++], false );
    LOG_BEZIER  = prefs.getBoolean( log_key[lk++], false );
    LOG_CSURVEY = prefs.getBoolean( log_key[lk++], false );
    LOG_PTOPO   = prefs.getBoolean( log_key[lk++], false );
    LOG_ZIP     = prefs.getBoolean( log_key[lk++], false );
    LOG_UNITS   = prefs.getBoolean( log_key[lk++], false );
  }

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    int nk = 0; // key index
    int lk = 0; // log_key index
    // Log.v(TopoDroidApp.TAG, "onSharePreferenceChanged " + k );

    if ( k.equals( key[ nk++ ] ) ) { // DISTOX_HIDE_HELP
      // mHideHelp = sp.getBoolean( k, HIDE_HELP );
    } else if ( k.equals( key[ nk++ ] ) ) { // "DISTOX_BASE_PATH" 
      mBasePath = sp.getString( k, mBasePath );
      setPaths( mBasePath );
      // FIXME need to restart the app ?
      mData        = new DataHelper( this );
      mCalibration = new Calibration( 0, this );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mTextSize = Integer.parseInt( sp.getString( k, TEXT_SIZE ) );

    } else if ( k.equals( key[ nk++ ] ) ) {
      mCloseDistance = Float.parseFloat( sp.getString( k, CLOSE_DISTANCE ) );
    } else if ( k.equals( key[ nk++ ] ) ) { 
      setExtendThr( Double.parseDouble( sp.getString( k, EXTEND_THR ) ) );   // DISTOX_EXTEND_THR2 4
    } else if ( k.equals( key[ nk++ ] ) ) {
      mVThreshold = Float.parseFloat( sp.getString( k, V_THRESHOLD ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurveyStations = Integer.parseInt( sp.getString( k, SURVEY_STATION ) ); // DISTOX_SURVEY_STATION 6
      mShotAfterSplays = ( mSurveyStations <= 2 );
      if ( mSurveyStations > 2 ) mSurveyStations -= 2;
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLength    = sp.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : M2FT;
      Log( LOG_UNITS, "mUnitLength changed " + mUnitLength );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitAngle     = sp.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : DEG2GRAD;
      Log( LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
    } else if ( k.equals( key[ nk++ ] ) ) {                        // DISTOX_ACCEL_THR 9
      mAccelerationThr  = Float.parseFloat( prefs.getString( k, "300.0f" ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_MAG_THR
      mMagneticThr  = Float.parseFloat( prefs.getString( k, "200.0f" ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_DIP_THR 11
      mDipThr  = Float.parseFloat( prefs.getString( k, "2.0f" ) );

    } else if ( k.equals( key[ nk++ ] ) ) {
      mLoopClosure = sp.getBoolean( k, LOOP_CLOSURE );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckAttached = sp.getBoolean( k, CHECK_ATTACHED );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLocation  = sp.getString( k, UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
      Log( LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAltitude = Integer.parseInt( sp.getString( k, ALTITUDE ) ); // DISTOX_ALTITUDE 15
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCRS           = prefs.getString( k, "Long-Lat" );     // DISTOX_CRS 16
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUseGPSAveraging = prefs.getBoolean( k, USE_GPSAVERAGING );   // DISTOX_GPS_AVERAGING

    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupBy = Integer.parseInt( sp.getString( k, GROUP_BY ) );  // DISTOX_GROUP_BY 18
    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupDistance = Float.parseFloat( sp.getString( k, GROUP_DISTANCE ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibEps = Float.parseFloat( sp.getString( k, CALIB_EPS ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibMaxIt = Integer.parseInt( sp.getString( k, CALIB_MAX_ITER ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mRawData = sp.getBoolean( k, false );                         // DISTOX_RAW_DATE 22

    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_DEVICE 23
      // mDevice = mData.getDevice( sp.getString( k, DEVICE_NAME ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_CHECK_B
      mCheckBT = Integer.parseInt(sp.getString( k, "1" ) ); 
    } else if ( k.equals( key[ nk++ ] ) ) {                        // "DISTOX_SOCK_TYPE 25
      mSockType = Integer.parseInt( sp.getString( k, "0" ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_COMM_RETRY 26
      setCommRetry( Integer.parseInt( prefs.getString( k, "1" ) ) );

    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoStations = sp.getBoolean( k, AUTO_STATIONS );  // DISTOX_AUTO_STATIONS 27
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCloseness = Float.parseFloat( sp.getString( k, CLOSENESS ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineSegment = Integer.parseInt( sp.getString( k, LINE_SEGMENT ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineAccuracy = Float.parseFloat( sp.getString( k, LINE_ACCURACY ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineCorner   = Float.parseFloat( sp.getString( k, LINE_CORNER ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      setLineStyleAndType( sp.getString( k, LINE_STYLE ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnit = Float.parseFloat( sp.getString( k, DRAWING_UNIT ) );  // DISTOX_DRAWING_UNIT 33
      // DrawingBrushPaths.doMakePaths( ); // no longer user
      DrawingBrushPaths.reloadPointLibrary( getResources() );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mPickerType = Integer.parseInt( prefs.getString( k, "0" ) );   // DISTOX_PICKER_TYPE 34
    } else if ( k.equals( key[ nk++ ] ) ) {
      mHThreshold = Float.parseFloat( prefs.getString( k, "60" ) );  // DISTOX_HTHRESHOLD 35
    } else if ( k.equals( key[ nk++ ] ) ) {
      mStationSize   = Float.parseFloat( prefs.getString( k, "10" ) ); // DISTOX_STATION_SIZE
      DrawingBrushPaths.resetPaintStationSize( );

    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchUsesSplays = sp.getBoolean( k, false );
    // } else if ( k.equals( key[ nk++ ] ) ) { 
    //   mSketchLineStep    = Float.parseFloat( prefs.getString( k, "0.5f") );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchBorderStep  = Float.parseFloat( prefs.getString( k, "0.2f") );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchSectionStep = Float.parseFloat( prefs.getString( k, "0.5f") );
    // } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_DELTA_EXTRUDE
    //   mDeltaExtrude  = Float.parseFloat( prefs.getString( k, "0.3f" ) );
    // } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_COMPASS_READINGS
    //   mCompassReadings = Integer.parseInt( sp.getString( k, "4" ) );

    // ---------------------- LOG PREFERENCES
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DEBUG",
      LOG_DEBUG = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_ERR",
      LOG_ERR = sp.getBoolean( k, true );
    } else if ( k.equals( log_key[ lk++ ] )) { // "DISTOX_LOG_INPUT",        
      LOG_INPUT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_BT",
      LOG_BT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_COMM",
      LOG_COMM = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PROTO",
      LOG_PROTO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DISTOX",
      LOG_DISTOX = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DEVICE",       // 40
      LOG_DEVICE = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DATA",
      LOG_DATA = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DB",
      LOG_DB = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_CALIB",
      LOG_CALIB = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_FIXED",
      LOG_FIXED = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_LOC",          // 45
      LOG_LOC = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PHOTO",
      LOG_PHOTO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SENSOR"        // 47
      LOG_SENSOR = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SHOT"        
      LOG_SHOT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SURVEY"
      LOG_SURVEY = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_NUM"
      LOG_NUM = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_THERION"
      LOG_THERION = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PLOT"
      LOG_PLOT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_BEZIER"
      LOG_BEZIER = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_CSURVEY"
      LOG_CSURVEY = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PTOPO"
      LOG_PTOPO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_ZIP"
      LOG_ZIP = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_UNITS"
      LOG_UNITS = sp.getBoolean( k, false );
    } 
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  void setBooleanPreference( String preference, boolean val )
  {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean( preference, val );
    editor.commit(); // Very important to save the preference
  }

  void setDevice( String address ) 
  { 
    if ( address == null ) {
      mDevice = null;
      address = "";
    } else {
      mDevice = mData.getDevice( address );
    }
    if ( prefs != null ) {
      Editor editor = prefs.edit();
      editor.putString( key[indexKeyDeviceName], address ); 
      editor.commit();
    }
  }

  // -------------------------------------------------------------
  // DATA DOWNLOAD

  public int downloadData()
  {
    mSecondLastShotId = lastShotId();
    // Log.v( TAG, "downloadData() device " + mDevice + " comm " + mComm.toString() );
    if ( mComm != null && mDevice != null ) {
      int ret = mComm.downloadData( mDevice.mAddress );
      // Log.v( TAG, "TopoDroidApp.downloadData() result " + ret );

      if ( ret > 0 && mSurveyStations > 0 ) {
        // assign stations
        List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
        DistoXDBlock prev = null;
        String from = (mSurveyStations == 1 )? "0" : "1";
        String to   = (mSurveyStations == 1 )? "1" : "0";
        String station = from;  // splays station

        boolean atStation = false;
        for ( DistoXDBlock blk : list ) {
          if ( blk.mFrom.length() == 0 ) {
            if ( prev == null ) {
              prev = blk;
              blk.mFrom = station;
              mData.updateShotName( blk.mId, mSID, blk.mFrom, "" );
            } else {
              if ( prev.relativeDistance( blk ) < mCloseDistance ) {
                if ( ! atStation ) {
                  prev.mFrom = from;
                  prev.mTo   = to;
                  mData.updateShotName( prev.mId, mSID, from, to );
                  station = from;
                  if ( mSurveyStations == 1 ) {
                    from = to;
                    to   = DistoXStationName.increment( from );
                  } else {
                    to   = from;
                    from = DistoXStationName.increment( to );
                  }
                  atStation = true;
                } else {
                  /* nothing: centerline extra shot */
                }
              } else {
                atStation = false;
                blk.mFrom = station;
                mData.updateShotName( blk.mId, mSID, blk.mFrom, "" );
                prev = blk;
              }
            }
          } else { // blk.mFrom.length > 0
            if ( blk.mTo.length() > 0 ) {
              station = blk.mFrom;
              if ( mSurveyStations == 1 ) {
                from = blk.mTo;
                to   = DistoXStationName.increment( from );
              } else {
                to   = blk.mFrom;
                from = DistoXStationName.increment( from );
              }
              atStation = true;
            } else {
              atStation = false;
            }
            prev = blk;
          }
        }
      }
      return ret;
    } else {
      Log( LOG_ERR, "Comm or Device is null ");
    }
    return 0;
  }

  // =======================================================================
  // CSUREVY EXPORT cSurvey

  static String[] therion_extend = { "left", "vertical", "right", "ignore" };
  static String   therion_flags_duplicate     = "   flags duplicate\n";
  static String   therion_flags_not_duplicate = "   flags not duplicate\n";
  static String   therion_flags_surface       = "   flags surface\n";
  static String   therion_flags_not_surface   = "   flags not surface\n";


  private void exportEmptyCsxSketch( PrintWriter pw )
  {
     pw.format("    <layers>\n");
     pw.format("      <layer name=\"Base\" type=\"0\">\n");
     pw.format("         <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Soil\" type=\"1\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Borders\" type=\"5\">\n");
     pw.format("        <items>\n");
     pw.format("        </items>\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Signs\" type=\"6\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("    </layers>\n");
     pw.format("    <plot />\n");
  }

  public String exportSurveyAsCsx( DrawingActivity sketch, String origin )
  {
    String filename = ( sketch == null )? getSurveyCsxFile() : getSurveyCsxFile(sketch.mName1);

    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    List< FixedInfo > fixed = mData.selectAllFixed( mSID, STATUS_NORMAL );
    List< PlotInfo > plots  = mData.selectAllPlots( mSID, STATUS_NORMAL );
    try {
      SurveyInfo info = mData.selectSurveyInfo( mSID );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );
      // pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );

      pw.format("<csurvey version=\"1.04\" id=\"\">\n");

// ++++++++++++++++ PROPERTIES
      // FIXME origin = origin of Num
      pw.format("  <properties id=\"\" name=\"\" origin=\"%s\" ", origin );
      // pw.format(      "name=\"\" description=\"\" club=\"\" team=\"\" ");
      pw.format(      "calculatemode=\"1\" calculatetype=\"2\" " );
      pw.format(      "ringcorrectionmode=\"2\" nordcorrectionmode=\"0\" inversionmode=\"1\" ");
      pw.format(      "designwarpingmode=\"1\" bindcrosssection=\"1\">\n");
      

   // ============== SESSIONS
      pw.format("    <sessions>\n");
      pw.format("      <session date=\"%s\" ", info.date); // FIXME yyyy-mm-dd
      pw.format(         "description=\"%s\" ", mySurvey ); // title
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format(" team=\"%s\" ", info.team );
      }
      pw.format("nordtype=\"0\" manualdeclination=\"0\" declination=\"%.4f\" ", info.declination );
      pw.format(">\n");
      pw.format("      </session>\n");
      pw.format("    </sessions>\n");

   // ============== CAVE INFOS and BRANCHES
      pw.format("    <caveinfos>\n");
      pw.format("      <caveinfo name=\"%s\"", mySurvey );
      // pw.format( " color=\"\"");
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format( " comment=\"%s\"\n", info.comment );
      }
      pw.format(" >\n");
      pw.format("        <branches />\n");
      pw.format("      </caveinfo>\n");
      pw.format("    </caveinfos>\n");

   // ============== ORIGIN
      if ( origin != null )  {
        pw.format("    <gps enabled=\"0\" refpointonorigin=\"%s\" geo=\"WGS84\" format=\"\" sendtotherion=\"0\" />\n", origin );
      }

      pw.format("  </properties>\n");

// ++++++++++++++++ SHOTS
      pw.format("  <segments>\n");

      // optional attrs of "segment": id cave branch session

      long extend = 0;      // current extend
      boolean dup = false;  // duplicate
      boolean sur = false;  // surface
      String com = null;    // comment
      String f="", t="";          // from to stations
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;

      DistoXDBlock ref_item = null;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s\"", mySurvey, f, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com );
                com = null;
              }
              n = 0;
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"\" to=\"%s\"", mySurvey, to );
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("<!-- comment=\"%s\" -->\n", item.mComment);
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // ONLY FROM STATION : splay shot
            if ( n > 0 ) { // finish writing previous leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s\"", mySurvey, f, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com );
                com = null;
              }
              n = 0;
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"\"", mySurvey, from );
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("<!-- comment=\"%s\" -->\n", item.mComment);
            }
          } else { // BOTH FROM AND TO STATIONS
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s\" ", mySurvey, f, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com);
                com = null;
              }
              n = 0;
            }
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              dup = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              sur = true;
            }
            f = from;
            t = to;
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            n = 1;
            com = item.mComment;
          }
        }
      }
      if ( n > 0 ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s\" ", mySurvey, f, t );
        if ( extend == -1 ) pw.format(" direction=\"1\"");
        if ( dup || sur ) {
           pw.format(" exclude=\"1\"");
           if ( dup ) { pw.format(" duplicate=\"1\""); /* dup = false; */ }
           if ( sur ) { pw.format(" surface=\"1\"");   /* sur = false; */ }
        }
        pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
        pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
        if ( com != null && com.length() > 0 ) {
          pw.format("<!-- comment=\"%s\" -->\n", com);
          // com = null;
        }
      }
      pw.format("  </segments>\n");

      // ============= TRIG POINTS
      pw.format("  <trigpoints>\n");
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("     <trigpoint name=\"%s\">\n", fix.name );
          pw.format("       <coordinate latv=\"%.7f\" longv=\"%.7f\" altv=\"%.2f\" format=\"dd.ddddddd N\"/>\n", fix.lat, fix.lng, fix.alt );
          // latv="0.0000000" longv="0.0000000" altv="100.00" lat="0° 0' 0,00&quot; N" long="0° 0' 0,00&quot; E" format="dd mm ss.ss N" alt="100"
          pw.format("     </trigpoint>\n");
        }
      }
      pw.format("  </trigpoints>\n");

      // ============= SKETCHES
      if ( sketch != null ) {
        sketch.exportAsCsx( pw );
      } else {
        pw.format("  <plan>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </plan>\n");
        pw.format("  <profile>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </profile>\n");
      }
      pw.format("</csurvey>\n");

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }


  // =======================================================================
  // POCKETTOPO EXPORT PocketTopo

  public String exportSurveyAsTop( DrawingActivity sketch, String origin )
  {
    String filename = getSurveyTopFile();

    PTFile ptfile = new PTFile();
    // TODO add a trip
    // date --> year, month, day --> _time
    // _declination (0)
    // _comment

    // TODO add shots
    // _from, _to
    // _dist, _azimuth, _inclination, _roll
    // extend left --> shot._flags bit-0
    // _trip_index (0)
    // _comment
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String[] vals = info.date.split( "\\." );
    ptfile.addTrip( Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]),
                    info.declination, info.comment );

    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    long extend = 0;  // current extend

    int n = 0;
    DistoXDBlock ref_item = null;
    for ( DistoXDBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      extend = item.mExtend;
      if ( from == null || from.length() == 0 ) {
        from = "";
        if ( to == null || to.length() == 0 ) {
          to = "";
          if ( ref_item != null 
            && ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) ) {
            from = ref_item.mFrom;
            to   = ref_item.mTo;
            extend = ref_item.mExtend;
          } else {
            ref_item = null;
          }
        } else { // only TO station
          ref_item = null;
        }
      } else { // with FROM station
        if ( to == null || to.length() == 0 ) { // splay shot
          to = "";
          ref_item = null;
        } else {
          ref_item = item;
        }
      }
      ptfile.addShot( (short)0, from, to, item.mLength, item.mBearing, item.mClino, item.mRoll, (int)extend, item.mComment );
    }

    if ( sketch != null ) {
      // TODO add sketch
    }

    try {
      FileOutputStream fos = new FileOutputStream( filename );
      ptfile.write( fos );
      fos.close();
    } catch ( IOException e ) {
      // TODO
    }
    return filename;
  }
  // =======================================================================
  // THERION EXPORT Therion

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

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );
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

      pw.format("    declination %.4f\n", info.declination );

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
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
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
              b = TopoDroidUtil.in360( b/n );
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
              b = TopoDroidUtil.in360( b/n );
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
        b = TopoDroidUtil.in360( b/n );
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

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("; %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );

      pw.format("*begin %s\n", mySurvey );
      pw.format("  *units tape meters\n");
      pw.format("  *units compass degrees\n");
      pw.format("  *units clino degrees\n");
      pw.format("  *calibrate declination %.f\n", info.declination );
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
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
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
              b = TopoDroidUtil.in360( b/n );
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
              b = TopoDroidUtil.in360( b/n );
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
        b = TopoDroidUtil.in360( b/n );
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
  /** COMMA-SEPARATED VALUES EXPORT 
   *  NOTE declination exported in comment only in CSV
   *
   */
  public String exportSurveyAsCsv()
  {
    String filename = getSurveyCsvFile();
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    // List< FixedInfo > fixed = mData.selectAllFixed( mSID, STATUS_NORMAL );
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );

      pw.format("# %s\n", mySurvey );
      // if ( fixed.size() > 0 ) {
      //   pw.format("  ; fix stations as lomg-lat alt\n");
      //   for ( FixedInfo fix : fixed ) {
      //     pw.format("  ; *fix %s\n", fix.toString() );
      //   }
      // }
      pw.format("# from to tape compass clino (declination %.4f)\n", info.declination );
      
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
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              n = 0;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "-,%s@%s,%.2f,%.1f,%.1f\n", to, mySurvey, item.mLength, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              n = 0;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "%s@%s,-,%.2f,%.1f,%.1f\n", from, mySurvey, item.mLength, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", item.mComment );
            // }
          } else {
            if ( n > 0 ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f,%.1f,%.1f", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              n = 0;
            }
            if ( splays ) {
              splays = false;
            }
            ref_item = item;
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              duplicate = true;
            }
            pw.format("%s@%s,%s@%s", from, mySurvey, to, mySurvey );
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
        b = TopoDroidUtil.in360( b/n );
        pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
        if ( duplicate ) {
          pw.format(",L");
          // duplicate = false;
        }
        pw.format("\n");
      }
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
  //     SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
  //     pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );
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
  //             b = TopoDroidUtil.in360( b/n );
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
  //             b = TopoDroidUtil.in360( b/n );
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
  //             b = TopoDroidUtil.in360( b/n );
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
  //           if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
  //             float bb = TopoDroidUtil.around( item.mBearing, b0[0] );
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
  //       b = TopoDroidUtil.in360( b/n );
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
    float grad2rad = TopoDroidUtil.GRAD2RAD;
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

  private static void printShotToDat( PrintWriter pw, float l, float b, float c, int n, LRUD lrud,
                               boolean duplicate, String comment )
  {
    b = TopoDroidUtil.in360( b/n );
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
      SurveyInfo info = mData.selectSurveyInfo( mSID );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      // pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );

      pw.format("%s\r\n", info.name ); // export as single survey
      pw.format("SURVEY NAME: %s\r\n", mySurvey );
      String date = mData.getSurveyDate( mSID );
      int y = Integer.parseInt( date.substring(0,4) );
      int m = Integer.parseInt( date.substring(5,7) );
      int d = Integer.parseInt( date.substring(8,10) );
      pw.format("SURVEY DATE: %d %d %d", m, d, y ); // format "MM DD YYYY"
      if ( info.comment != null ) {
        pw.format(" COMMENT %s", info.comment );
      }
      pw.format("\r\n");

      pw.format("SURVEY TEAM:\r\n");
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("%s\r\n", info.team );
      } else {
        pw.format("...\r\n");
      }
      pw.format("DECLINATION: %.4f  ", info.declination );
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
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
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
  // DXF EXPORT 
  // NOTE declination not taken into account in DXF export (only saved in comment)

  public String exportSurveyAsDxf( DistoXNum num )
  {
    String filename = getSurveyDxfFile();
    // Log.v( TAG, "exportSurveyAsDxf " + filename );
    try {
      SurveyInfo info = mData.selectSurveyInfo( mSID );
      FileWriter fw = new FileWriter( filename );
      PrintWriter out = new PrintWriter( fw );
      // TODO
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      out.printf("999\nDXF created by TopoDroid v %s - %s (declination %.4f)\n",
        VERSION, sdf.format( new Date() ), info.declination );
      out.printf("0\nSECTION\n2\nHEADER\n");
      out.printf("9\n$ACADVER\n1\nAC1006\n");
      out.printf("9\n$INSBASE\n");
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
      out.printf("9\n$EXTMIN\n");
      float emin = num.surveyEmin() - 2.0f;
      float nmin = - num.surveySmax() - 2.0f;
      float zmin = - num.surveyVmax() - 2.0f;
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        // num.surveyEmin(), -num.surveySmax(), -num.surveyVmax() );
      out.printf("9\n$EXTMAX\n");
      float emax = num.surveyEmax();
      float nmax = - num.surveySmin();
      float zmax = - num.surveyVmin();
      
      int de = (100f < emax-emin )? 100 : (50f < emax-emin)? 50 : 10;
      int dn = (100f < nmax-nmin )? 100 : (50f < nmax-nmin)? 50 : 10;
      int dz = (100f < zmax-zmin )? 100 : (50f < zmax-zmin)? 50 : 10;

      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emax, nmax, zmax );
        // num.surveyEmax(), -num.surveySmin(), -num.surveyVmin() );
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nTABLES\n");
      {
        out.printf("0\nTABLE\n2\nLTYPE\n70\n1\n");
        // int flag = 64;
        out.printf("0\nLTYPE\n2\nCONTINUOUS\n70\n64\n3\nSolid line\n72\n65\n73\n0\n40\n0.0\n");
        out.printf("0\nENDTAB\n");

        out.printf("0\nTABLE\n2\nLAYER\n70\n6\n");
          // 2 layer name, 70 flag (64), 62 color code, 6 line style
          String style = "CONTINUOUS";
          int flag = 64;
          out.printf("0\nLAYER\n2\nLEG\n70\n%d\n62\n%d\n6\n%s\n",     flag, 1, style );
          out.printf("0\nLAYER\n2\nSPLAY\n70\n%d\n62\n%d\n6\n%s\n",   flag, 2, style );
          out.printf("0\nLAYER\n2\nSTATION\n70\n%d\n62\n%d\n6\n%s\n", flag, 3, style );
          out.printf("0\nLAYER\n2\nREF\n70\n%d\n62\n%d\n6\n%s\n",     flag, 4, style );
        out.printf("0\nENDTAB\n");

        out.printf("0\nTABLE\n2\nSTYLE\n70\n0\n");
        out.printf("0\nENDTAB\n");
      }
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nBLOCKS\n");
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nENTITIES\n");
      {
        emin += 1f;
        nmin += 1f;
        zmin += 1f;
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin+de, nmin, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin+dn, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin, zmin+dz );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (de==100)? "100" : (de==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin+de+1, nmin, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dn==100)? "100" : (dn==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin+de+1, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dz==100)? "100" : (dz==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin, zmin+dz+1 );

        // centerline data
        for ( NumShot sh : num.getShots() ) {
          NumStation f = sh.from;
          NumStation t = sh.to;
          out.printf("0\nLINE\n8\nLEG\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", t.e, -t.s, -t.v );
        }

        for ( NumSplay sh : num.getSplays() ) {
          NumStation f = sh.from;
          out.printf("0\nLINE\n8\nSPLAY\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", sh.e, -sh.s, -sh.v );
        }
   
        for ( NumStation st : num.getStations() ) {
          // FIXME station scale is 0.3
          out.printf("0\nTEXT\n8\nSTATION\n");
          out.printf("1\n%s\n", st.name );
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", st.e, -st.s, -st.v );
        }
      }
      out.printf("0\nENDSEC\n");
      out.printf("0\nEOF\n");

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
  // FIXME not sure declination written in the right place

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
    b = TopoDroidUtil.in360( b/n );
    // Log.v( TAG, "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
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
  
      // FIXME 
      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      // pw.format("; %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), VERSION );

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
      
      pw.format("Param Deca Degd Clino Degd %.4f Dir,Dir,Dir Arr Inc 0,0,0\r\n\r\n", info.declination );

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
            if ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < mCloseDistance ) {
              // Log.v( TAG, "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
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
            // Log.v( TAG, "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
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

  // -------------------------------------------------------------
  // SYMBOLS

  void installSymbols( boolean overwrite )
  {
    boolean install = overwrite;
    askSymbolUpdate = false;
    if ( ! overwrite ) { // check whether to install
      String version = mData.getValue( "version" );
      // Log.v("DistoX", "version <" + version + "> VERSION <" + VERSION + ">" );
      if ( version == null ) {
        install = true;
      } else if ( ! version.equals(VERSION) ) {
        askSymbolUpdate = true;
      } else { // version .equals VERSION
        return;
      }
      mData.setValue( "version", VERSION );
    }
    if ( install ) {
      InputStream is = getResources().openRawResource( R.raw.symbols );
      symbolsUncompress( is, overwrite );
    }
  }

  // final static String symbol_urlstr = "http://sites/google.com/speleoapps/home/tdsymbol/TopoDroid-symbol-1.2.zip";

  private void symbolsCheckDirs()
  {
    File f1;
    f1 = new File( APP_POINT_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
    f1 = new File( APP_LINE_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
    f1 = new File( APP_AREA_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
    f1 = new File( APP_SAVE_POINT_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
    f1 = new File( APP_SAVE_LINE_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
    f1 = new File( APP_SAVE_AREA_PATH );
    if ( ! f1.exists() ) f1.mkdirs( );
  }
  
  /** download symbol zip from internet and store files in save/dirs
   */
  // int symbolsSync()
  // {
  //   int cnt = 0;
  //   try {
  //     URL url = new URL( symbol_urlstr );
  //     URLConnection url_conn = url.openConnection( );
  //     HttpURLConnection http_conn = (HttpURLConnection) url_conn;
  //     int resp_code = http_conn.getResponseCode();
  //     // Log.v( TAG, "resp code " + resp_code );
  //     if ( resp_code == HttpURLConnection.HTTP_OK ) {
  //       InputStream in = http_conn.getInputStream();
  //       cnt = symbolsUncompress( in );
  //     } else {
  //       // Toast.makeText( app, "Engine temporarily not available " + resp_code, Toast.LENGTH_SHORT ).show();
  //     }
  //     http_conn.disconnect();
  //   } catch ( MalformedURLException e ) {
  //     // Toast.makeText( app, "Bad URL " + urlstr, Toast.LENGTH_LONG ).show();
  //   } catch ( IOException e ) {
  //     // Toast.makeText( app, "Failed to get " + urlstr, Toast.LENGTH_LONG ).show();
  //   }
  //   return cnt;
  // }

  private int symbolsUncompress( InputStream fis, boolean overwrite )
  {
    int cnt = 0;
    // Log.v(TAG, "symbol uncompress ...");
    symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        // Log.v( TAG, "ZipEntry " + filepath );
        if ( ! ze.isDirectory() ) {
          if ( filepath.startsWith( "symbol" ) ) {
            filepath = filepath.substring( 7 );
          }
          String pathname =  APP_SYMBOL_PATH + filepath;
          File file = new File( pathname );
          if ( overwrite || ! file.exists() ) {
            ++cnt;
            FileOutputStream fout = new FileOutputStream( pathname );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c); // offset 0 in buffer
            }
            fout.close();
          
            // pathname =  APP_SYMBOL_SAVE_PATH + filepath;
            // file = new File( pathname );
            // if ( ! file.exists() ) {
            //   FileOutputStream fout = new FileOutputStream( pathname );
            //   int c;
            //   while ( ( c = zin.read( buffer ) ) != -1 ) {
            //     fout.write(buffer, 0, c); // offset 0 in buffer
            //   }
            //   fout.close();
            // }
          }
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return cnt;
  }


  /**
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   */
  public DistoXDBlock makeNewShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    mSecondLastShotId = lastShotId();
    DistoXDBlock ret = null;
    long id;
    distance /= mUnitLength;
    bearing  /= mUnitAngle;
    clino    /= mUnitAngle;
    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( bearing < 0.0f || bearing >= 360.0f ) ) {
      Toast.makeText( this, R.string.illegal_data_value, Toast.LENGTH_SHORT ).show();
      return null;
    }

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, mSID, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
      // } else
      {
        // Log( LOG_SHOT, "makeNewShot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > mVThreshold );
        // Log( LOG_SHOT, "makeNewShot SID " + mSID + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left ) / mUnitLength;
          if ( l >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, 270.0f, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, l, 270.0f, 0.0f, 0.0f );
              }
            } else {
              float b = bearing - 90.0f;
              if ( b < 0.0f ) b += 360.0f;
              // b = in360( b );
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, b, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, l, b, 0.0f, 0.0f );
              }
            }
            mData.updateShotName( id, mSID, from, "" );
            if ( at >= 0L ) ++at;
          }
        } 
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right ) / mUnitLength;
          if ( r >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, 90.0f, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, r, 90.0f, 0.0f, 0.0f );
              }
            } else {
              float b = bearing + 90.0f;
              if ( b >= 360.0f ) b -= 360.0f;
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, b, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, r, b, 0.0f, 0.0f );
              }
            }
            mData.updateShotName( id, mSID, from, "" );
            if ( at >= 0L ) ++at;
          }
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up ) / mUnitLength;
          if ( u >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 0.0f, 0.0f );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 90.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 90.0f, 0.0f );
              }
            }
            mData.updateShotName( id, mSID, from, "" );
            if ( at >= 0L ) ++at;
          }
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down ) / mUnitLength;
          if ( d >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 180.0f, 0.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, d, 180.0f, 0.0f, 0.0f );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 0.0f, -90.0f, 0.0f );
              } else {
                id = mData.insertShot( mSID, -1L, d, 0.0f, -90.0f, 0.0f );
              }
            }
            mData.updateShotName( id, mSID, from, "" );
            if ( at >= 0L ) ++at;
          }
        }
        if ( at >= 0L ) {
          id = mData.insertShotAt( mSID, at, distance, bearing, clino, 0.0f );
        } else {
          id = mData.insertShot( mSID, -1L, distance, bearing, clino, 0.0f );
        }
        // String name = from + "-" + to;
        mData.updateShotName( id, mSID, from, to );
        mData.updateShotExtend( id, mSID, extend );
        // FIXME updateDisplay( );

        ret = mData.selectShot( id, mSID );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_SHORT ).show();
    }
    return ret;
  }
}
