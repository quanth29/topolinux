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
 * 20140719 dump directory
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
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
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;

import android.view.WindowManager;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Point;

import android.util.Log;
import android.util.DisplayMetrics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.widget.Toast;

public class TopoDroidApp extends Application
                          implements OnSharedPreferenceChangeListener
{
  static String SYMBOL_VERSION = "05"; 
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
  static boolean VERSION30 = true;

  // ---------------------------------------------------------
  // DEBUG: logcat flags

  static int mLogStream = 0;    // log stream
  static PrintWriter mLog = null;
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
  static boolean LOG_PHOTO  = false;   // photos
  static boolean LOG_PREFS  = false;   // preferences
  static boolean LOG_PROTO  = false;   // protocol
  static boolean LOG_PTOPO  = false;   // PocketTopo
  static boolean LOG_SENSOR = false;   // sensors and measures
  static boolean LOG_SHOT   = false;   // shot
  static boolean LOG_STATS  = false;
  static boolean LOG_SURVEY = false;
  static boolean LOG_THERION= false;
  static boolean LOG_ZIP    = false;   // archive
  static boolean LOG_UNITS  = false;
  static boolean LOG_SYNC   = false;

  static void Log( boolean flag, String msg )
  {
    if ( flag ) {
      if ( mLogStream == 0 ) {
        Log.v( TAG, msg );
      } else {
        mLog.format( "%s\n", msg );
        // mLog.flush(); // autoflush ?
      }
    }
  }

  // ----------------------------------------------------------------------
  // DataListener
  private ArrayList< DataListener > mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener )
  {
    for ( DataListener l : mDataListeners ) {
      if ( l == listener ) return;
    }
    mDataListeners.add( listener );
  }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener )
  {
    mDataListeners.remove( listener );
  }

  // -----------------------------------------------------
  // sleep
  // static int mScreenTimeout = 60000; // 60 secs
  static int mTimerCount = 10; // Acc/Mag timer countdown (secs)
  static int mBeepVolume = 50; // beep volume

  private SharedPreferences prefs;
  boolean askSymbolUpdate = false; // by default do not ask

  static final int STATUS_NORMAL  = 0;   // item (shot, plot, fixed) status
  static final int STATUS_DELETED = 1;  

  static final int COLOR_NORMAL    = 0xffcccccc; // title colors
  static final int COLOR_NORMAL2   = 0xffffcc99; // title color nr. 2
  static final int COLOR_CONNECTED = 0xffff6666; 
  static final int COLOR_COMPUTE   = 0xffff33cc;

  // -----------------------------------------------------------
  
  final static int CONN_MODE_BATCH = 0; // DistoX connection mode
  final static int CONN_MODE_CONTINUOUS = 1;
  int mConnectionMode = CONN_MODE_BATCH; 

  String[] DistoXConnectionError;
  BluetoothAdapter mBTAdapter = null;     // BT connection
  private DistoXComm mComm = null;        // BT communication
  static DataHelper mData = null;         // database 

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
  static long mSecondLastShotId = 0L;

  public long lastShotId( ) { return mData.getLastShotId( mSID ); }
  public long secondLastShotId( ) { return mSecondLastShotId; }

  // ---------------------------------------------------------

  static int mCompassReadings = 4; // number of compass readings to average

  static float mCloseDistance = 1.0f; // FIXME kludge
  static int   mMinNrLegShots = 2;

  // selection_radius = cutoff + closeness / zoom
  static final float mCloseCutoff = 0.01f; // minimum selection radius
  static float mCloseness;                 // selection radius

  // ---------------------------------------------------------
  // PREFERENCES

  boolean mWelcomeScreen;  // whether to show the welcome screen
  static String  mManual;  // manual url
  static int mActivityLevel = 1;
  static boolean mLevelOverBasic        = true;
  static boolean mLevelOverNormal       = false;
  static boolean mLevelOverAdvanced     = false;
  static boolean mLevelOverExperimental = false;
  static int mSizeButtons = 1;   // action bar buttons scale (either 1 or 2)
  static int mTextSize = 14;     // list text size TEXT_SIZE

  static final int LEVEL_BASIC    = 0;
  static final int LEVEL_NORMAL   = 1;
  static final int LEVEL_ADVANCED = 2;
  static final int LEVEL_EXPERIMENTAL = 3;
  static final int LEVEL_COMPLETE     = 4;


  int setListViewHeight( HorizontalListView listView )
  {
    final float scale = getResources().getSystem().getDisplayMetrics().density;
    LayoutParams params = listView.getLayoutParams();
    int size = (int)( 42 * TopoDroidApp.mSizeButtons * scale );
    params.height = size + 10;
    listView.setLayoutParams( params );
    return size;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION

  static final int ALT_WGS84 = 0; // WGS84 altitude
  static final int ALT_ASL = 1;   // altimetric altitude

  static String mCRS;    // default coord ref systen 
  static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss
  static int mAltitude;     // location altitude type
  static boolean mUseGPSAveraging = false;
  static boolean mAltimetricLookup = false; // whether to lookup altimetric atitude
  static String  mDefaultTeam = "";

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
  static boolean mRawData;   // whether to display calibration raw data as well
  static int   mCalibAlgo;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  Device mDevice = null;
  int    distoType() { return (mDevice == null)? 0 : mDevice.mType; }
  String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }
  static boolean mBootloader;  // whether to show bootloader menu

  // private boolean mSaveOnDestroy = SAVE_ON_DESTROY;
  // int   mDefaultConnectionMode;
  int mCheckBT;        // BT: 0 disabled, 1 check on start, 2 enabled

  static final int TOPODROID_SOCK_DEFAULT      = 0;    // BT socket type
  static final int TOPODROID_SOCK_INSEC        = 1;
  // static final int TOPODROID_SOCK_INSEC_RECORD = 2;
  // static final int TOPODROID_SOCK_INSEC_INVOKE = 3;
  static int mSockType = TOPODROID_SOCK_DEFAULT; // FIXME static
  static int mCommRetry = 1;
  static int mCommType  = 0; // 0: on-demand, 1: continuous

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  float mVThreshold;         // verticality threshold (LRUD)
  static float mHThreshold;  // horizontal plot threshold
  boolean mCheckAttached;    // whether to check is there are shots non-attached
  private int     mSurveyStations;   // automatic survey stations: 0 no, 1 forward-after-splay, 2 backward-after-splay
  boolean mShotAfterSplays;  //                                          3 forward-before-splay, 4 backward-before-splay

  boolean isSurveyForward() { return (mSurveyStations%2) == 1; }
  boolean isSurveyBackward() { return mSurveyStations>0 && (mSurveyStations%2) == 0; }

  static boolean mLoopClosure;  // whether to do loop closure
  static float mStationSize;
  static float mLabelSize;
  static boolean mSplayExtend = true;  // whether to extend splays or not
  
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
  static float mLineThickness = 1;

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
  boolean mSketches = false;        // whether to use 3D models
  static float mSketchSideSize;
  static float mDeltaExtrude;
  // static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // static float mSketchBorderStep;
  // static float mSketchSectionStep;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CO-SURVEYING
  static boolean mCosurvey = false;
  boolean mCoSurveyServer = false;

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
  static final int DDMMSS = 0;
  static final int DEGREE = 1;
  // private static final byte char0C = 0x0c;

  // ------------------------------------------------------------
  // PATHS

  String  mBasePath = Environment.getExternalStorageDirectory().getAbsolutePath(); // app base path

  private static String APP_BASE_PATH; //  = Environment.getExternalStorageDirectory() + "/TopoDroid/";
  // private static String APP_TLX_PATH ; //  = APP_BASE_PATH + "tlx/";

  private static String APP_BIN_PATH ; //  = APP_BASE_PATH + "bin/";   // Firmwares  
  private static String APP_CSV_PATH ; //  = APP_BASE_PATH + "csv/";   // CSV text
  private static String APP_CSX_PATH ; //  = APP_BASE_PATH + "csx/";   // cSurvey
  private static String APP_DAT_PATH ; //  = APP_BASE_PATH + "dat/";   // Compass
  private static String APP_DUMP_PATH ; //  = APP_BASE_PATH + "dump/"; // DistoX memopry dumps
  private static String APP_DXF_PATH ; //  = APP_BASE_PATH + "dxf/";
  private static String APP_FOTO_PATH; //  = APP_BASE_PATH + "photo/";
  private static String APP_IMPORT_PATH; //  = APP_BASE_PATH + "import/";
  private static String APP_MAN_PATH;    //  = APP_BASE_PATH + "man/";
  private static String APP_NOTE_PATH;   //  = APP_BASE_PATH + "note/";
  private static String APP_PNG_PATH;    //  = APP_BASE_PATH + "png/";
  private static String APP_SRV_PATH ;   //  = APP_BASE_PATH + "svg/";
  private static String APP_SVG_PATH ;   //  = APP_BASE_PATH + "svg/";
  private static String APP_SVX_PATH ;   //  = APP_BASE_PATH + "svx/";
  private static String APP_TH_PATH  ; //  = APP_BASE_PATH + "th/";
  private static String APP_TH2_PATH ; //  = APP_BASE_PATH + "th2/";
  private static String APP_TH3_PATH ; //  = APP_BASE_PATH + "th3/";
  private static String APP_TOP_PATH ; //  = APP_BASE_PATH + "top/";
  private static String APP_TRO_PATH ; //  = APP_BASE_PATH + "tro/";
  private static String APP_ZIP_PATH; //  = APP_BASE_PATH + "zip/";

  static String APP_SYMBOL_PATH;
  static String APP_SYMBOL_SAVE_PATH;
  static String APP_POINT_PATH; //  = APP_BASE_PATH + "symbol/point/";
  static String APP_LINE_PATH; 
  static String APP_AREA_PATH;
  static String APP_SAVE_POINT_PATH; //  = APP_BASE_PATH + "symbol/point/";
  static String APP_SAVE_LINE_PATH; 
  static String APP_SAVE_AREA_PATH;


  // FIXME BASEPATH 
  // remove comments when ready to swicth to new Android app path system
  //
  private void setPaths( String path )
  {
    mManual = getResources().getString( R.string.topodroid_man );
    File dir = null;
    // String old_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TopoDroid/";
    // APP_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.topodroid.DistoX/";
    APP_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TopoDroid/";
    if ( path != null ) {
      dir = new File( path );
      if ( dir.exists() && dir.isDirectory() && dir.canWrite() ) {
        // old_path = path + "/TopoDroid/";
        // APP_BASE_PATH = path + "/Android/data/com.topodroid.DistoX/";
        APP_BASE_PATH = path + "/TopoDroid/";
      }
    }
    dir = new File( APP_BASE_PATH );
    // File old_dir = new File( old_path );
    // if ( old_dir.exists() && ! dir.exists() ) {
    //   if ( ! old_dir.renameTo( dir ) ) {
    //     Log.e(TAG, "failed renameTo " + APP_BASE_PATH );
    //   }
    // }
    if ( ! dir.exists() ) {
      if ( ! dir.mkdir() ) {
        Log.e(TAG, "failed mkdir " + APP_BASE_PATH );
      }
    }
    // Log.v(TAG, "Base Path \"" + APP_BASE_PATH + "\"" );

    APP_BIN_PATH    = APP_BASE_PATH + "bin/";
    checkDirs( APP_BIN_PATH );

    APP_MAN_PATH    = APP_BASE_PATH + "man/";
    checkDirs( APP_MAN_PATH );

    // APP_TLX_PATH    = APP_BASE_PATH + "tlx/";
    // checkDirs( APP_TLX_PATH );

    APP_DAT_PATH    = APP_BASE_PATH + "dat/";
    checkDirs( APP_DAT_PATH );

    APP_SRV_PATH    = APP_BASE_PATH + "srv/";
    checkDirs( APP_SRV_PATH );

    APP_SVX_PATH    = APP_BASE_PATH + "svx/";
    checkDirs( APP_SVX_PATH );

    APP_CSV_PATH    = APP_BASE_PATH + "csv/";
    checkDirs( APP_CSV_PATH );

    APP_CSX_PATH    = APP_BASE_PATH + "csx/";
    checkDirs( APP_CSX_PATH );

    APP_DUMP_PATH    = APP_BASE_PATH + "dump/";
    checkDirs( APP_DUMP_PATH );

    APP_TOP_PATH    = APP_BASE_PATH + "top/";
    checkDirs( APP_TOP_PATH );

    APP_TH_PATH     = APP_BASE_PATH + "th/";
    checkDirs( APP_TH_PATH );

    APP_TH2_PATH    = APP_BASE_PATH + "th2/";
    checkDirs( APP_TH2_PATH );

    APP_TH3_PATH    = APP_BASE_PATH + "th3/";
    checkDirs( APP_TH3_PATH );

    APP_DXF_PATH    = APP_BASE_PATH + "dxf/";
    checkDirs( APP_DXF_PATH );

    APP_SVG_PATH    = APP_BASE_PATH + "svg/";
    checkDirs( APP_SVG_PATH );

    APP_TRO_PATH    = APP_BASE_PATH + "tro/";
    checkDirs( APP_TRO_PATH );

    APP_PNG_PATH   = APP_BASE_PATH + "png/";
    checkDirs( APP_PNG_PATH );

    APP_NOTE_PATH   = APP_BASE_PATH + "note/";
    checkDirs( APP_NOTE_PATH );

    APP_FOTO_PATH   = APP_BASE_PATH + "photo/";
    checkDirs( APP_FOTO_PATH );

    APP_IMPORT_PATH = APP_BASE_PATH + "import/";
    checkDirs( APP_IMPORT_PATH );

    APP_ZIP_PATH    = APP_BASE_PATH + "zip/";
    checkDirs( APP_ZIP_PATH );

    APP_SYMBOL_PATH  = APP_BASE_PATH + "symbol/";
    checkDirs( APP_SYMBOL_PATH );

    APP_SYMBOL_SAVE_PATH  = APP_BASE_PATH + "symbol/save/";
    checkDirs( APP_SYMBOL_SAVE_PATH );

    APP_POINT_PATH  = APP_BASE_PATH + "symbol/point/";
    checkDirs( APP_POINT_PATH );

    APP_LINE_PATH   = APP_BASE_PATH + "symbol/line/";
    checkDirs( APP_LINE_PATH );

    APP_AREA_PATH   = APP_BASE_PATH + "symbol/area/";
    checkDirs( APP_AREA_PATH );

    APP_SAVE_POINT_PATH  = APP_BASE_PATH + "symbol/save/point/";
    checkDirs( APP_POINT_PATH );

    APP_SAVE_LINE_PATH   = APP_BASE_PATH + "symbol/save/line/";
    checkDirs( APP_LINE_PATH );

    APP_SAVE_AREA_PATH   = APP_BASE_PATH + "symbol/save/area/";
    checkDirs( APP_AREA_PATH );

    Log.v(TAG, "set paths done" );
  }

  // ---------------------------------------------------------
  // PREFERENCES KEYS

  static final int indexKeyHideHelp   = 0;
  static final int indexKeyDeviceName = 24;

  public static final String[] key = { // prefs keys
    "DISTOX_EXTRA_BUTTONS",       //  0 TODO move to general options
    "DISTOX_SIZE_BUTTONS",        //  1
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

    "DISTOX_UNIT_LOCATION",       // 14 
    "DISTOX_ALTITUDE",            // 15
    "DISTOX_CRS",                 // 16
    "DISTOX_GPS_AVERAGING",       // 17

    "DISTOX_GROUP_BY",            // 18
    "DISTOX_GROUP_DISTANCE",
    "DISTOX_CALIB_EPS",           // 20
    "DISTOX_CALIB_MAX_IT",
    "DISTOX_RAW_DATA",            // 22
    "DISTOX_CALIB_ALGO",          // 23

    "DISTOX_DEVICE",              // 24 N.B. indexKeyDeviceName
    "DISTOX_BLUETOOTH",           // 25
    "DISTOX_SOCK_TYPE",
    "DISTOX_COMM_RETRY",          // 27
    "DISTOX_BOOTLOADER",          // 28
    "DISTOX_CONN_MODE",           // 29

    "DISTOX_AUTO_STATIONS",       // 30 
    "DISTOX_CLOSENESS",           // 31
    "DISTOX_LINE_SEGMENT",
    "DISTOX_LINE_ACCURACY",
    "DISTOX_LINE_CORNER",         // 34
    "DISTOX_LINE_STYLE",          // 35
    "DISTOX_DRAWING_UNIT",        // 36
    "DISTOX_PICKER_TYPE",         // 37
    "DISTOX_HTHRESHOLD",          // 38
    "DISTOX_STATION_SIZE",        // 39
    "DISTOX_LABEL_SIZE",          // 40
    "DISTOX_LINE_THICKNESS",      // 41

    "DISTOX_TEAM",                   // 42
    "DISTOX_ALTIMETRIC",             // 43
    "DISTOX_SHOT_TIMER",             // 44
    "DISTOX_BEEP_VOLUME",            // 45
    "DISTOX_LEG_SHOTS",              // 46
    "DISTOX_COSURVEY",

    "DISTOX_SKETCH_LINE_STEP",       // 48
    "DISTOX_DELTA_EXTRUDE",          // 49
    "DISTOX_COMPASS_READINGS",       // 50

    "DISTOX_SPLAY_EXTEND",           // 51

    // "DISTOX_SKETCH_USES_SPLAYS",  // 
    // "DISTOX_SKETCH_BERDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP", // 


    // "DISTOX_HIDE_HELP",           // 
    // "DISTOX_BASE_PATH",           //

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
    "DISTOX_LOG_UNITS",
    "DISTOX_LOG_SYNC"
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
  public static final int DISTOX_EXPORT_SRV = 9;
  public static final int DISTOX_EXPORT_MAX = 10;   // placeholder 

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
  public static final  String GROUP_BY       = "2";     // GROUP_BY_ONLY_16
  public static final  String DEVICE_NAME    = "";
  // public static final  boolean SAVE_ON_DESTROY = true;
  public static final  boolean CHECK_BT      = true;
  public static final  boolean CHECK_ATTACHED = false;
  // public static final  boolean LIST_REFRESH  = false;
  public static final  String SURVEY_STATION = "1"; 
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

  // FIXME_SKETCH_3D
  public static final String TOPODROID_SKETCH_ID   = "topodroid.sketch_id";
  public static final String TOPODROID_SKETCH_NAME = "topodroid.sketch_name";
  // END_SKETCH_3D

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
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
    }
  }

  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null ) {
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  private void notifyConnState( int w )
  {
    // Log.v( TAG, "notify conn state" );
    for ( Handler hdl : mConnListener ) {
      try {
        Message msg = Message.obtain();
        msg.what = w;
        new Messenger( hdl ).send( msg );
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
    if ( c < 1 ) c = 1; else if ( c > 5 ) c = 5;
    mCommRetry = c;
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

  void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == mDevice ) {
      if ( device.mType != model ) {
        if ( model == Device.DISTO_A3 ) {
          mData.updateDeviceModel( device.mAddress, "DistoX" );
          device.mType = model;
        } else if ( model == Device.DISTO_X310 ) {
          mData.updateDeviceModel( device.mAddress, "DistoX-0000" );
          device.mType = model;
        }
      }
    }
  }

  // called by DeviceActivity::onResume()
  public void resumeComm()
  {
    if ( mComm != null ) mComm.resume();
  }

  public void suspendComm()
  {
    if ( mComm != null ) mComm.suspend();
  }

  public void resetComm() 
  { 
    mComm.disconnectRemoteDevice( );
    mComm = null;
    mComm = new DistoXComm( this );
  }

  // called by DeviceActivity::setState()
  //           ShotActivity::onResume()
  public boolean isCommConnected()
  {
    // return mComm != null && mComm.mBTConnected;
    return mComm != null && mComm.mBTConnected && mComm.mRfcommThread != null;
  }

  void disconnectRemoteDevice()
  {
    if ( mComm != null && mComm.mBTConnected ) mComm.disconnectRemoteDevice( );
  }

  void connectRemoteDevice( String address, ILister lister )
  {
    if ( mComm != null ) mComm.connectRemoteDevice( address, lister );
  }

  // FIXME_COMM
  public boolean connect( String address, ILister lister ) 
  {
    return mComm != null && mComm.connect( address, lister );
  }

  public void disconnect()
  {
    if ( mComm != null ) mComm.disconnect();
  }
  // end FIXME_COMM


  String readHeadTail( String address, int[] head_tail )
  {
    return mComm.readHeadTail( address, head_tail );
  }

  int swapHotBit( String address, int from, int to ) 
  {
    return mComm.swapHotBit( address, from, to );
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

    // mBasePath = prefs.getString( "DISTOX_BASE_PATH", mBasePath );
    setPaths( mBasePath );

    mWelcomeScreen = prefs.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true

    mEnableZip = true; // true: can save
    
    mDataListeners = new ArrayList<DataListener>();
    mData = new DataHelper( this, mDataListeners );

    String version = mData.getValue( "version" );
    if ( version == null || ( ! version.equals(VERSION) ) ) {
      mData.setValue( "version", VERSION );
      installManual( );  // must come before installSymbols
      installSymbols( false ); // this updates symbol_version in the database
      installFirmware( false );
    }

    {
      String value = mData.getValue("sketch");
      mSketches =  value != null 
                && value.equals("on")
                && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

      value = mData.getValue("cosurvey");
      mCosurvey =  value != null && value.equals("on");
    }

    mSyncConn = new ConnectionHandler( this );

    loadPreferences();

    mDevice = mData.getDevice( prefs.getString( key[indexKeyDeviceName], DEVICE_NAME ) );

    // DrawingBrushPaths.makePaths( getResources() );

    mCalibration = new Calibration( 0, this, false );

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    // if ( mBTAdapter == null ) {
    //   // Toast.makeText( this, R.string.not_available, Toast.LENGTH_SHORT ).show();
    //   // finish(); // FIXME
    //   // return;
    // }
    mConnListener = new ArrayList< Handler >();

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


    if ( mLog == null ) {
      try {
        File log_file = new File( APP_BASE_PATH + "/log.txt" );
        FileWriter fw = new FileWriter( log_file );
        mLog = new PrintWriter( fw, true ); // true = autoflush
      } catch ( IOException e ) {
        Log.e("DistoX", "cannot create log file" );
      }
    }
    loadLogPreferences();


  }

// -----------------------------------------------------------------

  // called by GMActivity and by CalibCoeffDialog 
  void uploadCalibCoeff( Context context, byte[] coeff )
  {
    if ( mComm == null || mDevice == null ) {
      Toast.makeText( context, R.string.no_device_address, Toast.LENGTH_SHORT ).show();
    } else if ( ! checkCalibrationDeviceMatch() ) {
      Toast.makeText( context, R.string.calib_device_mismatch, Toast.LENGTH_SHORT ).show();
    } else if ( ! mComm.writeCoeff( distoAddress(), coeff ) ) {
      Toast.makeText( context, R.string.write_failed, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( context, R.string.write_ok, Toast.LENGTH_SHORT).show();
    }
  }

  boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || mDevice == null ) return false;
    mComm.readCoeff( mDevice.mAddress, coeff );
    return true;
  }

  boolean toggleCalibMode( )
  {
    if ( mComm == null || mDevice == null ) return false;
    return mComm.toggleCalibMode( mDevice.mAddress, mDevice.mType );
  }

  byte[] readMemory( String address, int addr )
  {
    if ( mComm == null || isCommConnected() ) return null;
    return mComm.readMemory( address, addr );
  }

  int readX310Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    return mComm.readX310Memory( address, h0, h1, memory );
  }

  int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    return mComm.readA3Memory( address, h0, h1, memory );
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
      int major = 0;
      int minor = 0;
      try {
        major = Integer.parseInt( ver[0] );
        minor = Integer.parseInt( ver[1] );
      } catch ( NumberFormatException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "parse error: major/minor " + ver[0] + " " + ver[1] );
      }
      int sub   = 0;
      int k = 0;
      while ( k < ver[2].length() ) {
        char ch = ver[2].charAt(k);
        if ( ch < '0' || ch > '9' ) break;
        sub = 10 * sub + (int)(ch - '0');
        ++k;
      }
      // Log.v( "DistoX", "Version " + major + " " + minor + " " + sub );
      if (    ( major < MAJOR_MIN )
           || ( major == MAJOR_MIN && minor < MINOR_MIN )
           || ( major == MAJOR_MIN && minor == MINOR_MIN && sub < SUB_MIN ) ) {
        Log( LOG_ZIP, "TopDroid version mismatch: found " + line + " expected " + VERSION );
        return -2;
      }
      line = br.readLine().trim();
      int db_version = 0;
      try {
        db_version = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "parse error: db_version " + line );
      }
      
      if ( ! ( db_version >= DataHelper.DATABASE_VERSION_MIN && db_version <= DataHelper.DATABASE_VERSION ) ) {
        Log( LOG_ZIP, "TopDroid DB version mismatch: found " + line + " expected " + DataHelper.DB_VERSION );
        return -3;
      }
      line = br.readLine().trim();
      if ( ! line.equals( surveyname ) ) return -4;
      fr.close();
    } catch ( NumberFormatException e ) {
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
  static String getSrvFile( String name )    { return APP_SRV_PATH + name; }
  static String getSvgFile( String name )    { return APP_SVG_PATH + name; }
  static String getSvxFile( String name )    { return APP_SVX_PATH + name; }
  static String getCsvFile( String name )    { return APP_CSV_PATH + name; }
  static String getCsxFile( String name )    { return APP_CSX_PATH + name; }
  static String getDumpFile( String name )   { return APP_DUMP_PATH + name; }
  static String getTopFile( String name )    { return APP_TOP_PATH + name; }
  static String getTroFile( String name )    { return APP_TRO_PATH + name; }
  static String getPngFile( String name )    { return APP_PNG_PATH + name; }

  static String getBinFile( String name )    { return APP_BIN_PATH + name; }
  static String getManFile( String name )    { return APP_MAN_PATH + name; }

  static String getNoteFile( String name )   { return APP_NOTE_PATH + name; }

  static String getJpgDir( String dir ) { return APP_FOTO_PATH + dir; }
  static String getJpgFile( String dir, String name ) { return APP_FOTO_PATH + dir + "/" + name; }

  String getSurveyPlotDxfFile( String name ) { return APP_DXF_PATH + mySurvey + "-" + name + ".dxf"; }
  String getSurveyPlotFile( String name ) { return APP_TH2_PATH + mySurvey + "-" + name + ".th2"; }
  String getSurveySketchFile( String name ) { return APP_TH3_PATH + mySurvey + "-" + name + ".th3"; }
  String getSurveyPngFile( String name )  { return APP_PNG_PATH + mySurvey + "-" + name + ".png"; }


  private static String getFile( String directory, String name, String ext ) 
  {
    checkDirs( directory );
    return directory + name + "." + ext;
  }

  public static String getSurveyNoteFile( String title ) { return getFile( APP_NOTE_PATH, title, "txt" ); }
  public String getTh2FileWithExt( String name ) { return getFile( APP_TH2_PATH, name, "th2" ); }
  public String getTh3FileWithExt( String name ) { return getFile( APP_TH3_PATH, name, "th3" ); }
  public String getDxfFileWithExt( String name ) { return getFile( APP_DXF_PATH, name, "dxf" ); }
  public String getSvgFileWithExt( String name ) { return getFile( APP_SVG_PATH, name, "svg" ); }
  public String getPngFileWithExt( String name ) { return getFile( APP_PNG_PATH, name, "png" ); }

  public String getSurveyZipFile() { return getFile( APP_ZIP_PATH, mySurvey, "zip" ); }
  public String getSurveyDatFile( ) { return getFile( APP_DAT_PATH, mySurvey, "dat" ); }
  // public String getSurveyTlxFile( ) { return getFile( APP_TLX_PATH, mySurvey, "tlx" ); }
  public String getSurveyThFile( ) { return getFile( APP_TH_PATH, mySurvey, "th" ); }
  public String getSurveyTroFile( ) { return getFile( APP_TRO_PATH, mySurvey, "tro" ); }
  public String getSurveyDxfFile( ) { return getFile( APP_DXF_PATH, mySurvey, "dxf" ); }
  public String getSurveySrvFile( ) { return getFile( APP_SRV_PATH, mySurvey, "srv" ); }
  public String getSurveySvxFile( ) { return getFile( APP_SVX_PATH, mySurvey, "svx" ); }
  public String getSurveyCsvFile( ) { return getFile( APP_CSV_PATH, mySurvey, "csv" ); }
  public String getSurveyCsxFile( ) { return getFile( APP_CSX_PATH, mySurvey, "csx" ); }
  public String getSurveyCsxFile( String name ) { return getFile( APP_CSX_PATH, mySurvey + "-" + name, "csx" ); }
  public String getSurveyTopFile( ) { return getFile( APP_TOP_PATH, mySurvey, "top" ); }

  private static File[] getFiles( String dirname, final String[] ext )
  {
    File dir = new File( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            int ne = ext.length;
            if ( pathname.isDirectory() ) return false;
            if ( ne == 0 ) return true;
            for ( int n = 0; n < ne; ++n ) {
              if ( pathname.getName().endsWith( ext[n] ) ) return true;
            }
            return false;
          }
        } );
    }
    return null;
  }

  // private static File[] getFiles( String dirname, final String extension )
  // {
  //   File dir = new File( dirname );
  //   if ( dir.exists() ) {
  //     return dir.listFiles( new FileFilter() {
  //         public boolean accept( File pathname ) { return pathname.getName().endsWith( extension ); }
  //       } );
  //   }
  //   return null;
  // }

  public static File[] getImportFiles() { return getFiles( APP_IMPORT_PATH, new String[] {".th", ".top", ".dat"} ); }
  public static File[] getZipFiles() { return getFiles( APP_ZIP_PATH, new String[] {".zip"} ); }
  public static File[] getBinFiles() { return getFiles( APP_BIN_PATH, new String[] { } ); }

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

  public long setSurveyFromName( String survey, boolean forward ) 
  { 
    mSID = -1;       // no survey by default
    mySurvey = null;
    if ( survey != null && mData != null ) {
      // Log.v( "DistoX", "setSurveyFromName <" + survey + "> forward " + forward );

      mSID = mData.setSurvey( survey, forward );
      // mFixed.clear();
      mySurvey = null;
      if ( mSID > 0 ) {
        mySurvey = survey;
        mSecondLastShotId = lastShotId();
        // restoreFixed();
        if ( mShotActivity  != null) {
          mShotActivity.setTheTitle();
          mShotActivity.updateDisplay();
        }
        if ( mSurveyActivity != null ) {
          mSurveyActivity.setTheTitle();
          mSurveyActivity.updateDisplay();
        }
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


  private void parseSurveyStations( String str ) 
  {
    try {
      mSurveyStations = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      mSurveyStations = Integer.parseInt( SURVEY_STATION );
    }
    mShotAfterSplays = ( mSurveyStations <= 2 );
    if ( mSurveyStations > 2 ) mSurveyStations -= 2;
    // Log.v("DistoX", "mSurveyStations " + mSurveyStations + " mShotAfterSplays " + mShotAfterSplays );
  }

  void loadPreferences()
  {
    // ------------------- GENERAL PREFERENCES
    int k = 0;

    mActivityLevel = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_EXTRA_BUTTONS
    mLevelOverBasic        = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_BASIC;
    mLevelOverNormal       = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_NORMAL;
    mLevelOverAdvanced     = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_ADVANCED;
    mLevelOverExperimental = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_EXPERIMENTAL;

    try {
      mSizeButtons  = Integer.parseInt( prefs.getString( key[k++], "1" ) );
    } catch ( NumberFormatException e ) { mSizeButtons = 1; }
    try {
      mTextSize = Integer.parseInt( prefs.getString( key[k++], TEXT_SIZE ) );
    } catch ( NumberFormatException e ) { mTextSize = Integer.parseInt( TEXT_SIZE ); }

    // ------------------- SURVEY PREFERENCES
    try {
      mCloseDistance = Float.parseFloat( prefs.getString( key[k++], CLOSE_DISTANCE ) ); // DISTOX_CLOSE_DISTANCE 3
    } catch ( NumberFormatException e ) {
      mCloseDistance = Float.parseFloat( CLOSE_DISTANCE );
    }

    try {
      setExtendThr( Double.parseDouble( prefs.getString( key[k++], EXTEND_THR ) ) );    // DISTOX_EXTEND_THR2
    } catch ( NumberFormatException e ) {
      setExtendThr( Double.parseDouble( EXTEND_THR ) );
    }

    try {
      mVThreshold    = Float.parseFloat( prefs.getString( key[k++], V_THRESHOLD ) );    // DISTOX_VTHRESHOLD
    } catch ( NumberFormatException e ) {
      mVThreshold    = Float.parseFloat( V_THRESHOLD );
    }

    parseSurveyStations( prefs.getString( key[k++], SURVEY_STATION ) ); // DISTOX_SURVEY_STATIONS 6

    mUnitLength    = prefs.getString( key[k++], UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
    mUnitAngle     = prefs.getString( key[k++], UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
  
    try {
      mAccelerationThr  = Float.parseFloat( prefs.getString( key[k++], "300.0f" ) );  // DISTOX_ACCEL_THR 9
    } catch ( NumberFormatException e ) {
      mAccelerationThr = 300.0f;
    }

    try {
      mMagneticThr  = Float.parseFloat( prefs.getString( key[k++], "200.0f" ) );     // DISTOX_MAG_THR
    } catch ( NumberFormatException e ) {
      mMagneticThr = 200.0f;
    }

    try {
      mDipThr  = Float.parseFloat( prefs.getString( key[k++], "2.0f" ) );            // DISTOX_DIP_THR
    } catch ( NumberFormatException e ) {
      mDipThr = 2.0f;
    }


    mLoopClosure   = prefs.getBoolean( key[k++], LOOP_CLOSURE );                     // DISTOX_LOOP_CLOSURE 12
    mCheckAttached = prefs.getBoolean( key[k++], CHECK_ATTACHED );                   // DISTOX_CHECK_ATTACHED

    mUnitLocation  = prefs.getString( key[k++], UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
    try {
      mAltitude      = Integer.parseInt( prefs.getString( key[k++], ALTITUDE ) );      // DISTOX_ALTITUDE
    } catch ( NumberFormatException e ) {
      mAltitude      = Integer.parseInt( ALTITUDE );
    }
    mCRS           = prefs.getString( key[k++], "Long-Lat" );                        // DISTOX_CRS
    mUseGPSAveraging = prefs.getBoolean( key[k++], USE_GPSAVERAGING );               // DISTOX_GPS_AVERAGING 17

    Log( LOG_UNITS, "mUnitLength " + mUnitLength );
    Log( LOG_UNITS, "mUnitAngle " + mUnitAngle );
    Log( LOG_UNITS, "mUnitLocation " + mUnitLocation );

    // ------------------- CALIBRATION PREFERENCES
    try {
      mGroupBy       = Integer.parseInt( prefs.getString( key[k++], GROUP_BY ) );       // DISTOX_GROUP_BY
    } catch ( NumberFormatException e ) {
      mGroupBy       = Integer.parseInt( GROUP_BY );
    }

    try {
      mGroupDistance = Float.parseFloat( prefs.getString( key[k++], GROUP_DISTANCE ) ); // DISTOX_GROUP_DISTANCE
    } catch ( NumberFormatException e ) {
      mGroupDistance = Float.parseFloat( GROUP_DISTANCE );
    }

    try {
      mCalibEps      = Float.parseFloat( prefs.getString( key[k++], CALIB_EPS ) );      // DISTOX_CALIB_EPS
    } catch ( NumberFormatException e ) {
      mCalibEps      = Float.parseFloat( CALIB_EPS );
    }

    try {
      mCalibMaxIt    = Integer.parseInt( prefs.getString( key[k++], CALIB_MAX_ITER ) ); // DISTOX_CALIB_MAX_IT
    } catch ( NumberFormatException e ) {
      mCalibMaxIt  = Integer.parseInt( CALIB_MAX_ITER );
    }

    mRawData       = prefs.getBoolean( key[k++], false );                             // DISTOX_RAW_DATA 22
      
    try {
      mCalibAlgo    = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_CALIB_MAX_IT
    } catch ( NumberFormatException e ) {
      mCalibAlgo = 1;
    }

    // ------------------- DEVICE PREFERENCES
    k++; // DISTOX_DEVICE  24

    try {
      mCheckBT = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_BLUETOOTH 25
    } catch ( NumberFormatException e ) {
      mCheckBT = 1;
    }

    try {
      mSockType = Integer.parseInt( prefs.getString( key[k++], "0" ) ); // DISTOX_SOCK_TYPE 26
    } catch ( NumberFormatException e ) {
      mSockType = 0;
    }
      
    try {
      setCommRetry( Integer.parseInt( prefs.getString( key[k++], "1" ) ) ); // DISTOX_COMM_RETRY 27
    } catch ( NumberFormatException e ) {
      setCommRetry( 1 );
    }

    mBootloader = prefs.getBoolean( key[k++], false );                      // DISTOX_BOOTLOADER 28

    mConnectionMode = Integer.parseInt( prefs.getString( key[k++], "0" ) ); // DISTOX_CONN_MODE 29

    // -------------------  DRAWING PREFERENCES
    mAutoStations  = prefs.getBoolean( key[k++], AUTO_STATIONS );                // DISTOX_AUTO_STATIONS 30
    
    try {
      mCloseness = Float.parseFloat( prefs.getString( key[k++], CLOSENESS ) );   // DISTOX_CLOSENESS
    } catch ( NumberFormatException e ) {
      mCloseness = Float.parseFloat( CLOSENESS );
    }

    try {
      mLineSegment = Integer.parseInt( prefs.getString( key[k++], LINE_SEGMENT ) );  // DISTOX_LINE_SEGMENT
    } catch ( NumberFormatException e ) {
      mLineSegment = Integer.parseInt( LINE_SEGMENT );
    }

    try {
      mLineAccuracy  = Float.parseFloat( prefs.getString( key[k++], LINE_ACCURACY ) ); // DISTOX_LINE_ACCURACY
    } catch ( NumberFormatException e ) {
      mLineAccuracy  = Float.parseFloat( LINE_ACCURACY );
    }

    try {
      mLineCorner = Float.parseFloat( prefs.getString( key[k++], LINE_CORNER ) );   // DISTOX_LINE_CORNER
    } catch ( NumberFormatException e ) {
      mLineCorner = Float.parseFloat( LINE_CORNER );
    }

    setLineStyleAndType( prefs.getString( key[k++], LINE_STYLE ) );              // DISTOX_LINE_STYLE

    try {
      mUnit = Float.parseFloat( prefs.getString( key[k++], DRAWING_UNIT ) );  // DISTOX_DRAWING_UNIT
    } catch ( NumberFormatException e ) {
      mUnit = Float.parseFloat( DRAWING_UNIT );
    }

    try {
      mPickerType = Integer.parseInt( prefs.getString( key[k++], "0" ) );        // DISTOX_PICKER_TYPE
    } catch ( NumberFormatException e ) {
      mPickerType = 0;
    }

    try {
      mHThreshold = Float.parseFloat( prefs.getString( key[k++], "60.0f" ) );    // DISTOX_HTHRESHOLD
    } catch ( NumberFormatException e ) {
      mHThreshold = 60.0f;
    }

    try {
      mStationSize = Float.parseFloat( prefs.getString( key[k++], "24.0f" ) );   // DISTOX_STATION_SIZE 39
    } catch ( NumberFormatException e ) {
      mStationSize = 24.0f;
    }

    try {
      mLabelSize = Float.parseFloat( prefs.getString( key[k++], "24.0f" ) );     // DISTOX_LABEL_SIZE 40
    } catch ( NumberFormatException e ) {
      mLabelSize = 24.0f;
    }

    try {
      float lt = Float.parseFloat( prefs.getString( key[k++], "1.0f" ) );  // DISTOX_LINE_THICKNESS 41
      if ( lt > 0.0f ) mLineThickness = lt;
    } catch ( NumberFormatException e ) {
    }

    mDefaultTeam = prefs.getString( key[k++], "" );                      // DISTOX_TEAM
    mAltimetricLookup = prefs.getBoolean( key[k++], false );               // DISTOX_ALTIMETRIC
    try {
      int t = Integer.parseInt( prefs.getString( key[k++], "10") );  // DISTOX_SHOT_TIMER
      if ( t > 0 ) mTimerCount = t;
    } catch ( NumberFormatException e ) { }
    try {
      int t = Integer.parseInt( prefs.getString( key[k++], "10") );  // DISTOX_BEEP_VOLUME
      if ( t > 0 ) mBeepVolume = (t<10)? 10 : (t>100)? 100 : t;
    } catch ( NumberFormatException e ) { }

    try {
      mMinNrLegShots = Integer.parseInt( prefs.getString( key[k++], "2") );  // DISTOX_LEG_SHOTS
    } catch ( NumberFormatException e ) { }

    boolean co_survey = prefs.getBoolean( key[k++], false );        // DISTOX_COSURVEY
    if ( co_survey ) {                                  // force false at start
      setBooleanPreference( "DISTOX_COSURVEY", false );
    }
    setCoSurvey( false );

    // ------------------- SKETCH PREFERENCES
    mSketchSideSize    = Float.parseFloat( prefs.getString( key[k++], "0.5f") );
    mDeltaExtrude      = Float.parseFloat( prefs.getString( key[k++], "50f") );
    // mSketchUsesSplays  = prefs.getBoolean( key[k++], false );
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( key[k++], "0.2f") );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( key[k++], "0.5f") );
    mCompassReadings   = Integer.parseInt( prefs.getString( key[k++], "4" ) );
    
    mSplayExtend = prefs.getBoolean( key[k++], true ); 
  }

  void loadLogPreferences() // ---------------------- LOG PREFERENCES
  {
    int lk = 0;
    
    mLogStream  = Integer.parseInt( prefs.getString("DISTOX_LOG_STREAM", "0") );
    
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
    LOG_SYNC    = prefs.getBoolean( log_key[lk++], false );
  }

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    int nk = 0; // key index
    int lk = 0; // log_key index
    int i;
    float f;
    // Log.v(TopoDroidApp.TAG, "onSharePreferenceChanged " + k );

      if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_EXTRA_BUTTONS
        int level = Integer.parseInt( prefs.getString( k, "1" ) );
        if ( level != mActivityLevel ) {
          mActivityLevel = level;
          if ( mActivity != null ) {
            mLevelOverBasic        = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_BASIC;
            mLevelOverNormal       = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_NORMAL;
            mLevelOverAdvanced     = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_ADVANCED;
            mLevelOverExperimental = TopoDroidApp.mActivityLevel > TopoDroidApp.LEVEL_EXPERIMENTAL;
            mActivity.resetButtonBar();
            mActivity.setMenuAdapter();
          }  
        }
      } else if ( k.equals( key[ nk++ ] ) ) {
        mSizeButtons  = Integer.parseInt( prefs.getString( k, "1" ) );
        if ( mActivity != null ) mActivity.resetButtonBar();
      } else if ( k.equals( key[ nk++ ] ) ) {   // DISTOX_TEXT_SIZE
        try {
          i = Integer.parseInt( sp.getString( k, TEXT_SIZE ) );
          if ( i > 0 ) mTextSize = i;
        } catch ( NumberFormatException e ) { }
  
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, CLOSE_DISTANCE ) );
          if ( f > 0.0f ) mCloseDistance = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) { 
        try {
          setExtendThr( Double.parseDouble( sp.getString( k, EXTEND_THR ) ) );   // DISTOX_EXTEND_THR2 4
        } catch ( NumberFormatException e ) { setExtendThr( Double.parseDouble( EXTEND_THR ) ); }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, V_THRESHOLD ) );
          if ( f > 0.0f ) mVThreshold = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        parseSurveyStations( sp.getString( k, SURVEY_STATION ) ); // DISTOX_SURVEY_STATION 6
      } else if ( k.equals( key[ nk++ ] ) ) {
        mUnitLength = sp.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
        // Log( LOG_UNITS, "mUnitLength changed " + mUnitLength );
      } else if ( k.equals( key[ nk++ ] ) ) {
        mUnitAngle  = sp.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
        // Log( LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
      } else if ( k.equals( key[ nk++ ] ) ) {                        // DISTOX_ACCEL_THR 9
        try {
          f = Float.parseFloat( prefs.getString( k, "300.0f" ) );
          if ( f > 0.0f ) mAccelerationThr = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_MAG_THR
        try {
          f = Float.parseFloat( prefs.getString( k, "200.0f" ) );
          if ( f > 0.0f ) mMagneticThr = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_DIP_THR 11
        try {
          f = Float.parseFloat( prefs.getString( k, "2.0f" ) );
          if ( f > 0.0f ) mDipThr = f;
        } catch ( NumberFormatException e ) { }
  
      } else if ( k.equals( key[ nk++ ] ) ) {
        mLoopClosure = sp.getBoolean( k, LOOP_CLOSURE );
      } else if ( k.equals( key[ nk++ ] ) ) {
        mCheckAttached = sp.getBoolean( k, CHECK_ATTACHED );
      } else if ( k.equals( key[ nk++ ] ) ) {
        mUnitLocation  = sp.getString( k, UNIT_LOCATION ).equals(UNIT_LOCATION) ? DDMMSS : DEGREE;
        // Log( LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          mAltitude = Integer.parseInt( sp.getString( k, ALTITUDE ) ); // DISTOX_ALTITUDE 15
        } catch ( NumberFormatException e ) { mAltitude = Integer.parseInt( ALTITUDE ); }
      } else if ( k.equals( key[ nk++ ] ) ) {
        mCRS = prefs.getString( k, "Long-Lat" );     // DISTOX_CRS 16
      } else if ( k.equals( key[ nk++ ] ) ) {
        mUseGPSAveraging = prefs.getBoolean( k, USE_GPSAVERAGING );   // DISTOX_GPS_AVERAGING
  
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          mGroupBy = Integer.parseInt( sp.getString( k, GROUP_BY ) );  // DISTOX_GROUP_BY 18
        } catch ( NumberFormatException e ) { mGroupBy = Integer.parseInt( GROUP_BY ); }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, GROUP_DISTANCE ) );
          if ( f > 0.0f ) mGroupDistance = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, CALIB_EPS ) );
          if ( f > 0.0f ) mCalibEps = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          i = Integer.parseInt( sp.getString( k, CALIB_MAX_ITER ) );
          if ( i > 0 ) mCalibMaxIt = i;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        mRawData = sp.getBoolean( k, false );     // DISTOX_RAW_DATA 22
      } else if ( k.equals( key[ nk++ ] ) ) {     // DISTOX_CALIB_ALGO 23
        try {
          mCalibAlgo = Integer.parseInt( prefs.getString( k, "1" ) ); // DISTOX_CALIB_MAX_IT
        } catch ( NumberFormatException e ) { mCalibAlgo = 1; }

      } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_DEVICE 24
        // mDevice = mData.getDevice( sp.getString( k, DEVICE_NAME ) );
      } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_CHECK_B
        try {
          mCheckBT = Integer.parseInt(sp.getString( k, "1" ) ); 
        } catch ( NumberFormatException e ) { mCheckBT = 1; }
      } else if ( k.equals( key[ nk++ ] ) ) {                        // "DISTOX_SOCK_TYPE 26
        try {
          mSockType = Integer.parseInt( sp.getString( k, "0" ) );
        } catch ( NumberFormatException e ) { mSockType = 0; }
      } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_COMM_RETRY 27
        try {
          setCommRetry( Integer.parseInt( prefs.getString( k, "1" ) ) );
        } catch ( NumberFormatException e ) { setCommRetry( 1 ); }
      } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_BOOTLOADER 28
        mBootloader = sp.getBoolean( k, false );     
      } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_CONN_MODE
        mConnectionMode = Integer.parseInt( prefs.getString( k, "0" ) ); 
  
      } else if ( k.equals( key[ nk++ ] ) ) {
        mAutoStations = sp.getBoolean( k, AUTO_STATIONS );  // DISTOX_AUTO_STATIONS 30
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, CLOSENESS ) );
          if ( f < 0.0f ) mCloseness = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          i = Integer.parseInt( sp.getString( k, LINE_SEGMENT ) );
          if ( i > 0 ) mLineSegment = i;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, LINE_ACCURACY ) );
          if ( f > 0.0f ) mLineAccuracy = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( sp.getString( k, LINE_CORNER ) );
          if ( f > 0.0f ) mLineCorner = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {                           // STYLE 35
        setLineStyleAndType( sp.getString( k, LINE_STYLE ) );
      } else if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_DRAWING_UNIT 36
        try {
          f = Float.parseFloat( sp.getString( k, DRAWING_UNIT ) );
          if ( f > 0.0f ) mUnit = f;
          // DrawingBrushPaths.doMakePaths( ); // no longer user
        } catch ( NumberFormatException e ) { }
        DrawingBrushPaths.reloadPointLibrary( getResources() );
      } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_PICKER_TYPE 37
        try {
          mPickerType = Integer.parseInt( prefs.getString( k, "0" ) );
        } catch ( NumberFormatException e ) { mPickerType = 0; }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( prefs.getString( k, "60" ) );  // DISTOX_HTHRESHOLD 38
          if ( f >= 0.0f && f < 90.0f ) mHThreshold = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( prefs.getString( k, "24" ) ); // DISTOX_STATION_SIZE 39
          if ( f > 0.0f && f != mStationSize ) {
            mStationSize = f;
            DrawingBrushPaths.resetPaintStationSize( );
          }
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( prefs.getString( k, "24" ) ); // DISTOX_LABEL_SIZE 40
          if ( f > 0.0f && f != mLabelSize ) {
            mLabelSize = f;
            DrawingBrushPaths.resetPaintLabelSize( );
          }
        } catch ( NumberFormatException e ) { }
        // FIXME changing label size affects only new labels
        //       not existing labels (until they are edited)
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          f = Float.parseFloat( prefs.getString( k, "1" ) );  // DISTOX_LINE_THICKNESS 41
          if ( f != mLineThickness && f > 0.0f ) {
            mLineThickness = f;
            DrawingBrushPaths.doMakePaths();
          }
        } catch ( NumberFormatException e ) { }

      } else if ( k.equals( key[ nk++ ] ) ) {
        mDefaultTeam = prefs.getString( k, "" );              // DISTOX_TEAM
      } else if ( k.equals( key[ nk++ ] ) ) {
        mAltimetricLookup = prefs.getBoolean( k, false );     // DISTOX_ALTIMETRIC
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          i = Integer.parseInt( prefs.getString( k, "10") );  // DISTOX_SHOT_TIMER
          if ( i > 0 ) mTimerCount = i;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          int t = Integer.parseInt( prefs.getString( k, "10") );  // DISTOX_BEEP_VOLUME
          if ( t > 0 ) mBeepVolume = (t<10)? 10 : (t>100)? 100 : t;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        try {
          mMinNrLegShots = Integer.parseInt( prefs.getString( k, "2") );  // DISTOX_LEG_SHOTS
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) {
        boolean co_survey = prefs.getBoolean( k, false );               // DISTOX_COSURVEY
        if ( co_survey != mCoSurveyServer ) {
          setCoSurvey( co_survey ); // set flag and start/stop server
        }

      } else if ( k.equals( key[ nk++ ] ) ) {    // DISTOX_SKETCH_LINE_STEP
        try {
          f = Float.parseFloat( prefs.getString( k, "0.5f") );  // 0.5 meter
          if ( f > 0.0f ) mSketchSideSize = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_DELTA_EXTRUDE
        try {
          f = Float.parseFloat( prefs.getString( k, "50f" ) );
          if ( f > 0.0f ) mDeltaExtrude = f;
        } catch ( NumberFormatException e ) { }
      } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_COMPASS_READINGS
        try {
          i = Integer.parseInt( sp.getString( k, "4" ) );
          if ( i > 0 ) mCompassReadings = i;
        } catch ( NumberFormatException e ) { }

      } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_SPLAY_EXTEND
        mSplayExtend = prefs.getBoolean( k, true ); 

      // } else if ( k.equals( key[ nk++ ] ) ) {
      //   mSketchUsesSplays = sp.getBoolean( k, false );
      // } else if ( k.equals( key[ nk++ ] ) ) {
      //   mSketchBorderStep  = Float.parseFloat( prefs.getString( k, "0.2f") );
      // } else if ( k.equals( key[ nk++ ] ) ) {
      //   mSketchSectionStep = Float.parseFloat( prefs.getString( k, "0.5f") );

      // UNUSED *****
      // } else if ( k.equals( key[ nk++ ] ) ) {        // DISTOX_HIDE_HELP
      //   mHideHelp = sp.getBoolean( k, HIDE_HELP );
      // } else if ( k.equals( key[ nk++ ] ) ) { // "DISTOX_BASE_PATH" 
      //   mBasePath = sp.getString( k, mBasePath );
      //   setPaths( mBasePath );
      //   // FIXME need to restart the app ?
      //   mData        = new DataHelper( this, mDataListeners );
      //   mCalibration = new Calibration( 0, this, false );

    // ---------------------- LOG PREFERENCES
    } else if ( k.equals( "DISTOX_LOG_STREAM" ) ) { // "DISTOX_LOG_STREAM",
      mLogStream = Integer.parseInt( sp.getString(k, "0") );

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
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SYNC"
      LOG_SYNC = sp.getBoolean( k, false );
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

  public int downloadData( ILister lister )
  {
    mSecondLastShotId = lastShotId();
    Log( LOG_DATA, "downloadData() device " + mDevice + " comm " + mComm.toString() );
    int ret = 0;
    if ( mComm != null && mDevice != null ) {
      ret = mComm.downloadData( mDevice.mAddress, lister );
      // Log.v( TAG, "TopoDroidApp.downloadData() result " + ret );
      if ( ret > 0 && mSurveyStations > 0 ) {
        // FIXME TODO select only shots after the last leg shots
        List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
        assignStations( list );
      }
    } else {
      Log( LOG_ERR, "Comm or Device is null ");
    }
    return ret;
  }

  private String mCurrentStationName = null;

  void setCurrentStationName( String name ) 
  { 
    if ( name.equals(mCurrentStationName) ) {
      mCurrentStationName = null; // clear
    } else {
      mCurrentStationName = name;
    }
  }

  String getCurrentStationName() { return mCurrentStationName; }

  boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  // called also by ShotActivity::updataBlockList
  public void assignStations( List<DistoXDBlock> list )
  { 
    if ( mSurveyStations <= 0 ) return;
    // Log( LOG_DATA, "assignStations() policy " + mSurveyStations + "/" + mShotAfterSplays + " nr. shots " + list.size() );
    // Log.v( "DistoX", "assignStations() policy " + mSurveyStations + "/" + mShotAfterSplays + " nr. shots " + list.size() );
    // assign stations
    DistoXDBlock prev = null;
    String from = (mSurveyStations == 1 )? "0" : "1";
    String to   = (mSurveyStations == 1 )? "1" : "0";
    String station = mShotAfterSplays? from : "";  // splays station
    // Log.v("DistoX", "station [0] " + station );

    int atStation = 0;
    for ( DistoXDBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) {
        // Log.v( "DistoX", "Id " + blk.mId + " FROM is empty ");

        if ( prev == null ) {
          prev = blk;
          blk.mFrom = station;
          // Log.v( "DistoX", "Id " + blk.mId + " null prev. FROM " + blk.mFrom );
          mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
        } else {
          if ( prev.relativeDistance( blk ) < mCloseDistance ) {
            if ( atStation == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                if ( mSurveyStations == 1 ) { // forward-shot
                  from = mCurrentStationName;
                } else if ( mSurveyStations == 2 ) {
                  to = mCurrentStationName;
                }
                mCurrentStationName = null;
              }
              atStation = 2;
            } else { /* centerline extra shot */
              atStation ++;
            }
            if ( atStation == TopoDroidApp.mMinNrLegShots ) {
              prev.mFrom = from;                             // forward-shot from--to
              prev.mTo   = to;
              // Log.v( "DistoX", "Id " + prev.mId + " setting prev. FROM " + from + " TO " + to );
              mData.updateShotName( prev.mId, mSID, from, to, true ); // LEG
              if ( ! TopoDroidApp.mSplayExtend ) {
                long extend = ( prev.mBearing < 180.0 )? 1L : -1L;
                mData.updateShotExtend( prev.mId, mSID, extend, true );
              }
              if ( mSurveyStations == 1 ) {                  // forward-shot
                station = mShotAfterSplays ? to : from;      // splay-station = this-shot-to if splays before shot
                // Log.v("DistoX", "station [1] " + station + " from " + from + " to " + to );
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                
                do {
                  to   = DistoXStationName.increment( to );  // next-shot-to   = increment next-shot-from
                } while ( DistoXStationName.listHasName( list, to ) );
              } else {                                       // backward-shot 
                to   = from;                                 // next-shot-to   = this-shot-from
                do {
                  from = DistoXStationName.increment( from );    // next-shot-from = increment this-shot-from
                } while ( DistoXStationName.listHasName( list, from ) );
                station = mShotAfterSplays ? from : to;      // splay-station  = next-shot-from if splay before shot
                // Log.v("DistoX", "station [2] " + station + " from " + from + " to " + to );
              }                                              //                = thsi-shot-from if splay after shot
            }
          } else {
            atStation = 0;
            blk.mFrom = station;
            // Log.v( "DistoX", "Id " + blk.mId + " " + blk.mFrom );
            mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true ); // SPLAY
            prev = blk;
          }
        }
      } else { // blk.mFrom.length > 0
        // Log.v("DistoX", " FROM is not empty: " + blk.mFrom );

        if ( blk.mTo.length() > 0 ) {
          // Log.v("DistoX", " TO is not empty: " + blk.mTo );
          if ( mSurveyStations == 1 ) { // forward shot
            from = blk.mTo;
            to   = from;
            do {
              to   = DistoXStationName.increment( to );
            } while ( DistoXStationName.listHasName( list, to ) );
            station = mShotAfterSplays ? blk.mTo    // blk.mFrom-blk.mTo blk.mTo, ..., blk.mTo-to
                                                    // 1-2, 2, 2, ..., 2-3, 3, 
                                       : blk.mFrom; // blk.mFrom-blk.mTo blk.mFrom ... blk.mTo-to, blk.mTo, ...
                                                    // 1-2, 1, 1, ..., 2-3, 2, 2, ...
            // Log.v("DistoX", "station [3] " + station + " from " + from + " to " + to );
          } else { // backward shot
            to   = blk.mFrom;
            from = to;
            do {
              from = DistoXStationName.increment( from ); // FIXME it was from
            } while ( DistoXStationName.listHasName( list, from ) );
            station = mShotAfterSplays ? from       // blk.mFrom-blk.mTo from ... from-blk.mFrom
                                                    // 2-1, 3, 3, ..., 3-2, 4, ...
                                       : blk.mFrom; // blk.mFrom-blk.mTo ... blk.mFrom from-blk.mFrom, from ...
                                                    // 2-1, 2, 2, ..., 3-2, 3, 3, ...
            // Log.v("DistoX", "station [4] " + station + " from " + from + " to " + to );
          }
          atStation = TopoDroidApp.mMinNrLegShots;
        } else {
          // Log.v("DistoX", " TO is empty " );
          atStation = 0;
        }
        prev = blk;
      }
    }
  }

  // ----------------------------------------------
  // EXPORTS

  public String exportSurveyAsCsx( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = ( sketch == null )? getSurveyCsxFile() : getSurveyCsxFile(sketch.mName1);
    return TopoDroidExporter.exportSurveyAsCsx( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTop( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyTopFile();
    return TopoDroidExporter.exportSurveyAsTop( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTh( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyThFile();
    return TopoDroidExporter.exportSurveyAsTh( mSID, mData, info, filename );
  }

  public String exportSurveyAsSvx()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveySvxFile();
    return TopoDroidExporter.exportSurveyAsSvx( mSID, mData, info, filename );
  }

  public String exportSurveyAsTro()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyTroFile();
    return TopoDroidExporter.exportSurveyAsTro( mSID, mData, info, filename );
  }

  public String exportSurveyAsCsv( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyCsvFile();
    return TopoDroidExporter.exportSurveyAsCsv( mSID, mData, info, filename );
  }

  public String exportSurveyAsSrv()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveySrvFile();
    return TopoDroidExporter.exportSurveyAsSrv( mSID, mData, info, filename );
  }

  public String exportSurveyAsDxf( DistoXNum num )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyDxfFile();
    return TopoDroidExporter.exportSurveyAsDxf( mSID, mData, info, num, filename );
  }

  public String exportSurveyAsDat()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = getSurveyDatFile();
    return TopoDroidExporter.exportSurveyAsDat( mSID, mData, info, filename );
  }



  public String exportCalibAsCsv( )
  {
    if ( mCID < 0 ) return null;
    CalibInfo ci = mData.selectCalibInfo( mCID );
    if ( ci == null ) return null;
    String filename = getCsvFile( ci.name );
    return TopoDroidExporter.exportCalibAsCsv( mCID, mData, ci, filename );
  }

  // ----------------------------------------------
  // FIRMWARE

  private void installFirmware( boolean overwrite )
  {
    InputStream is = getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
  }

  private void installManual( )
  {
    // Log.v( TopoDroidApp.TAG, "Uncompress Manual ");
    InputStream is = getResources().openRawResource( R.raw.manual );
    manualUncompress( is );
  }

  // -------------------------------------------------------------
  // SYMBOLS

  void installSymbols( boolean overwrite )
  {
    boolean install = overwrite;
    askSymbolUpdate = false;
    if ( ! overwrite ) { // check whether to install
      String version = mData.getValue( "symbol_version" );
      // Log.v("DistoX", "symbol version <" + version + "> SYMBOL_VERSION <" + SYMBOL_VERSION + ">" );
      if ( version == null ) {
        install = true;
      } else if ( ! version.equals(SYMBOL_VERSION) ) {
        askSymbolUpdate = true;
      } else { // version .equals SYMBOL_VERSION
        return;
      }
      mData.setValue( "symbol_version", SYMBOL_VERSION );
    }
    if ( install ) {
      InputStream is = getResources().openRawResource( R.raw.symbols );
      symbolsUncompress( is, overwrite );
    }
  }

  // final static String symbol_urlstr = "http://sites/google.com/speleoapps/home/tdsymbol/TopoDroid-symbol-1.2.zip";

  private static void checkDirs( String path )
  {
    File f1 = new File( path );
    if ( ! f1.exists() ) f1.mkdirs( );
  }

  private static void symbolsCheckDirs()
  {
    checkDirs( APP_POINT_PATH );
    checkDirs( APP_LINE_PATH );
    checkDirs( APP_AREA_PATH );
    checkDirs( APP_SAVE_POINT_PATH );
    checkDirs( APP_SAVE_LINE_PATH );
    checkDirs( APP_SAVE_AREA_PATH );
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

  private int firmwareUncompress( InputStream fis, boolean overwrite )
  {
    int cnt = 0;
    // Log.v(TAG, "firmware uncompress ...");
    checkDirs( APP_BIN_PATH );
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        if ( ! filepath.endsWith("bin") ) continue;
        String pathname =  APP_BIN_PATH + filepath;
        File file = new File( pathname );
        if ( overwrite || ! file.exists() ) {
          ++cnt;
          FileOutputStream fout = new FileOutputStream( pathname );
          int c;
          while ( ( c = zin.read( buffer ) ) != -1 ) {
            fout.write(buffer, 0, c); // offset 0 in buffer
          }
          fout.close();
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return cnt;
  }

  private void manualUncompress( InputStream fis )
  {
    // Log.v(TAG, "manual uncompress ...");
    checkDirs( APP_MAN_PATH );
    try {
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        String pathname =  APP_MAN_PATH + filepath;
        
        File file = new File( pathname );
        FileOutputStream fout = new FileOutputStream( pathname );
        int c;
        while ( ( c = zin.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c); // offset 0 in buffer
        }
        fout.close();
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  /**
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   */
  public DistoXDBlock makeNewShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down,
                           String splay_station )
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
          float l = -1.0f;
          try {
            l = Float.parseFloat( left ) / mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "makeNewShot parse error: left " + left );
          }
          if ( l >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, 270.0f, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, l, 270.0f, 0.0f, 0.0f, true );
              }
            } else {
              float b = bearing - 90.0f;
              if ( b < 0.0f ) b += 360.0f;
              // b = in360( b );
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, b, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, l, b, 0.0f, 0.0f, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        } 
        if ( right != null && right.length() > 0 ) {
          float r = -1.0f;
          try {
            r = Float.parseFloat( right ) / mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "makeNewShot parse error: right " + right );
          }
          if ( r >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, 90.0f, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, r, 90.0f, 0.0f, 0.0f, true );
              }
            } else {
              float b = bearing + 90.0f;
              if ( b >= 360.0f ) b -= 360.0f;
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, b, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, r, b, 0.0f, 0.0f, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( up != null && up.length() > 0 ) {
          float u = -1.0f;
          try {
            u = Float.parseFloat( up ) / mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "makeNewShot parse error: up " + up );
          }
          if ( u >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 0.0f, 0.0f, true );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 90.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 90.0f, 0.0f, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( down != null && down.length() > 0 ) {
          float d = -1.0f;
          try {
            d = Float.parseFloat( down ) / mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "makeNewShot parse error: down " + down );
          }
          if ( d >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 180.0f, 0.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, d, 180.0f, 0.0f, 0.0f, true );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 0.0f, -90.0f, 0.0f, true );
              } else {
                id = mData.insertShot( mSID, -1L, d, 0.0f, -90.0f, 0.0f, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( at >= 0L ) {
          id = mData.insertShotAt( mSID, at, distance, bearing, clino, 0.0f, true );
        } else {
          id = mData.insertShot( mSID, -1L, distance, bearing, clino, 0.0f, true );
        }
        // String name = from + "-" + to;
        mData.updateShotName( id, mSID, from, to, true );
        mData.updateShotExtend( id, mSID, extend, true );
        // FIXME updateDisplay( );

        ret = mData.selectShot( id, mSID );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_SHORT ).show();
    }
    return ret;
  }

  int getCalibAlgoFromDB()
  {
    return mData.selectCalibAlgo( mCID );
  }

  void updateCalibAlgo( int algo ) 
  {
    mData.updateCalibAlgo( mCID, algo );
  }
  
  int getCalibAlgoFromDevice()
  {
    if ( mDevice == null ) return 1;                  // default: CALIB_ALGO_LINEAR
    if ( mDevice.mType == Device.DISTO_A3 ) return 1; // A3: CALIB_ALGO_LINEAR
    if ( mDevice.mType == Device.DISTO_X310 ) {
      // if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( mDevice.mAddress, 0xe000 );
      if ( ret == null ) return -1; // should not happen
      if ( ret[0] >= 2 && ret[1] >= 3 ) return 2; // CALIB_ALGO_NON_LINEAR
      return 1; // CALIB_ALGO_LINEAR
    }
    return 1; // CALIB_ALGO_LINEAR
  }  

  void setX310Laser( int what ) // 0: off, 1: on, 2: measure
  {
    mComm.setX310Laser( mDevice.mAddress, what );
  }

  int readFirmwareHardware()
  {
    return mComm.readFirmwareHardware( mDevice.mAddress );
  }

  int dumpFirmware( String filename )
  {
    // Log.v( "DistoX", "dump firmware " + filename );
    return mComm.dumpFirmware( mDevice.mAddress, getBinFile(filename) );
  }

  int uploadFirmware( String filename )
  {
    // Log.v( "DistoX", "upload firmware " + filename );
    return mComm.uploadFirmware( mDevice.mAddress, getBinFile(filename) );
  }

  long insert2dPlot( long sid , String name, String start )
  {
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, mScaleFactor, 0.0f, true );
    long pid_s = mData.insertPlot( sid, -1L, name+"s",
                 PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, mScaleFactor, 0.0f, true );
    return pid_p;
  }
  
  long insert2dSection( long sid, String name, long type, String from, String to, float azimuth )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, false );
  }

  // ---------------------------------------------------------------------
  // SYNC

  ConnectionHandler mSyncConn = null;

  private void setCoSurvey( boolean co_survey )
  {
    if ( ! mCosurvey ) {
      mCoSurveyServer = false;
      return;
    } 
    mCoSurveyServer = co_survey;
    if ( mCoSurveyServer ) { // start server
      startRemoteTopoDroid( );
    } else { // stop server
      stopRemoteTopoDroid( );
    }
  }


  int getConnectionType() 
  {
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getType();
  }

  int getAcceptState()
  {
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getAcceptState();
  }

  int getConnectState()
  {
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getConnectState();
  }

  String getConnectionStateStr()
  {
    if ( mSyncConn == null ) return "NONE";
    return mSyncConn.getConnectStateStr();
  }

  String getConnectedDeviceName()
  {
    if ( mSyncConn == null ) return null;
    return mSyncConn.getConnectedDeviceName();
  }

  String getConnectionStateTitleStr()
  {
    if ( mSyncConn == null ) return "";
    return mSyncConn.getConnectionStateTitleStr();
  }

  void connStateChanged()
  {
    // Log.v( "DistoX", "connStateChanged()" );
    if ( mSurveyActivity != null ) mSurveyActivity.setTheTitle();
    if ( mShotActivity  != null) mShotActivity.setTheTitle();
    if ( mActivity != null ) mActivity.setTheTitle();
  }

  void refreshUI()
  {
    if ( mSurveyActivity != null ) mSurveyActivity.updateDisplay();
    if ( mShotActivity  != null) mShotActivity.updateDisplay();
    if ( mActivity != null ) mActivity.updateDisplay();
  }

  void connectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) {
      mSyncConn.connect( device );
    }
  }

  void disconnectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.disconnect( device );
    }
  }


  void syncRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) {
      mSyncConn.syncDevice( device );
    }
  }

  void startRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) {
      mSyncConn.start( );
    }
  }

  void stopRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.stop( );
    }
  }

  void syncConnectionFailed()
  {
    Toast.makeText( this, "Sync connection failed", Toast.LENGTH_SHORT ).show();
  }

  void syncConnectedDevice( String name )
  {
    Toast.makeText( this, "Sync connected " + name, Toast.LENGTH_SHORT ).show();
    if ( mSyncConn != null ) {
      registerDataListener( mSyncConn );
    }
  }

}
