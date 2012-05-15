/** @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import android.util.Log;
import android.app.Application;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;

import android.bluetooth.BluetoothAdapter;

public class TopoDroidApp extends Application
                          implements OnSharedPreferenceChangeListener
{
  private static final String TAG = "DistoX App";

  private SharedPreferences prefs;
  BluetoothAdapter mBTAdapter = null;
  DistoXComm mComm = null;
  DistoXDataHelper mData = null;
  Calibration mCalibration = null;

  private long mSID   = -1;   // id of the current survey
  // private long mSIDid = -1; // id of the shot
  private long mCID   = -1;   // id of the current calib
  // private long mCIDid = -1;
  // private long mPID   = -1;   // id of the plot
  private String mySurvey;   // current survey name
  private String myCalib;    // current calib name

  // preferences
  float mCloseDistance;
  int   mExportType;
  float mGroupDistance;
  float mCalibEps;
  int   mCalibMaxIt;
  String mDevice        = DEVICE_NAME;
  // private boolean mSaveOnDestroy = SAVE_ON_DESTROY;

  int   mLineSegment;
  float mVThreshold;    // verticality threshold (LRUD)
  float mLineAccuracy;
  float mLineCorner;    // corner threshold
  int mLineStyle;       // line style: BEZIER, NONE, TWO, THREE
  int mLineType;        // line type:  1       1     2    3
  boolean mCheckBT;     // check BT on start
  boolean mListRefresh; // whether to refresh list on edit-dialog ok-return
  int mGroupBy;         // how to group calib data

  // consts
  public static final float M_PI = 3.1415926536f;
  public static final float M_2PI = 6.283185307f;
  // public static final float M_PI = 3.14159265358979323846;
  public static final float RAD2GRAD_FACTOR = (180.0f/M_PI);
  public static final float GRAD2RAD_FACTOR = (M_PI/180.0f);
  public static final long ZERO = 32768;
  public static final long NEG  = 65536;
  public static final float FV = 24000.0f;
  public static final float FM = 16384.0f;

  public static final String APP_BASE_PATH = "/mnt/sdcard/TopoDroid/";
  public static final String APP_TLX_PATH  = APP_BASE_PATH + "tlx/";
  public static final String APP_DAT_PATH  = APP_BASE_PATH + "dat/";
  public static final String APP_SVX_PATH  = APP_BASE_PATH + "svx/";
  public static final String APP_TH_PATH   = APP_BASE_PATH + "th/";
  public static final String APP_TH2_PATH  = APP_BASE_PATH + "th2/";
  public static final String APP_TRO_PATH  = APP_BASE_PATH + "tro/";
  public static final String APP_MAPS_PATH = APP_BASE_PATH + "maps/";
  public static final String APP_NOTE_PATH = APP_BASE_PATH + "note/";

  public static final long PLOT_V_SECTION = 0;
  public static final long PLOT_PLAN      = 1;
  public static final long PLOT_EXTENDED  = 2;
  public static final long PLOT_H_SECTION = 3;

  public static final int LINE_STYLE_BEZIER = 0;
  public static final int LINE_STYLE_NONE   = 1;
  public static final int LINE_STYLE_TWO    = 2;
  public static final int LINE_STYLE_THREE  = 3;

  public static final int GROUP_BY_DISTANCE = 0;
  public static final int GROUP_BY_FOUR     = 1;
  public static final int GROUP_BY_ONLY_16  = 2;

  public static final String[] projName = { // therion projection names
    "none", "plan", "extended", "none"
  };

  public static final float LEN_THR    = 20.0f; // corner detection length
  public static final float TO_THERION = 5.0f;

  public static float mUnit;

  public static final String[] key = { // prefs keys
    "DISTOX_CLOSE_DISTANCE",  // 0
    "DISTOX_EXPORT_TYPE",
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
    "DISTOX_LIST_REFRESH"     // 15
  };

  public static final int DISTOX_EXPORT_TH  = 0;
  public static final int DISTOX_EXPORT_TLX = 1;
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
  public static final  String LINE_STYLE     = "0";     // BEZIER
  public static final  String DRAWING_UNIT   = "1.2f";  // UNIT
  public static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH
  public static final  String GROUP_DISTANCE = "40.0f";
  public static final  String CALIB_EPS      = "0.0000001f";
  public static final  String CALIB_MAX_ITER = "200";
  public static final  String GROUP_BY       = "0";     // GROUP_BY_DISTANCE
  public static final  String DEVICE_NAME    = "";
  // public static final  boolean SAVE_ON_DESTROY = true;
  public static final  boolean CHECK_BT        = true;
  public static final  boolean LIST_REFRESH    = false;

  // intent names
  public static final String TOPODROID_CALIB       = "topodroid.calib";

  public static final String TOPODROID_GM_NAME     = "topodroid.gm_name";
  public static final String TOPODROID_GM_DATA     = "topodroid.gm_data";

  public static final String TOPODROID_SHOT_FROM   = "topodroid.shot_from";
  public static final String TOPODROID_SHOT_TO     = "topodroid.shot_to";
  public static final String TOPODROID_SHOT_EXT    = "topodroid.shot_ext";

  public static final String TOPODROID_PLOT_ID     = "topodroid.plot_id";
  public static final String TOPODROID_PLOT_TYPE   = "topodroid.plot_type";
  public static final String TOPODROID_PLOT_STRT   = "topodroid.plot_strt";
  public static final String TOPODROID_PLOT_VIEW   = "topodroid.plot_view";
  public static final String TOPODROID_PLOT_FILE   = "topodroid.plot_file";

  public static final String TOPODROID_LINE_SGM    = "topodroid.line_sgm";
  public static final String TOPODROID_LINE_ACC    = "topodroid.line_acc";
  public static final String TOPODROID_LINE_CRNR   = "topodroid.line_crnr";

  public static final String TOPODROID_SURVEY      = "topodroid.survey";
  public static final String TOPODROID_SURVEY_ID   = "topodroid.survey_id";
  
  public static final String TOPODROID_DEVICE_ACTION = "topodroid.device_action";
  public static final String TOPODROID_DEVICE_ADDR   = "topodroid.device_addr";
  public static final String TOPODROID_DEVICE_CNCT   = "topodroid.device_cnct";

  public String[] DistoXConnectionError;

  private ArrayList< DistoXFix > mFixed;

  public void addFixed( String station, double latitude, double longitude, double altitude )
  {
    mFixed.add( new DistoXFix( station, latitude, longitude, altitude ) );
  }

  // ----------------------------------------------------------------

  @Override
  public void onTerminate()
  {
    super.onTerminate();
    // Log.v(TAG, "onTerminate app");
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    // Log.v(TAG, "onCreate app");
    this.prefs = PreferenceManager.getDefaultSharedPreferences( this );
    this.prefs.registerOnSharedPreferenceChangeListener( this );

    mCloseDistance = Float.parseFloat( prefs.getString( key[0], CLOSE_DISTANCE ) );
    String type = prefs.getString( key[1], EXPORT_TYPE );
    mExportType = DISTOX_EXPORT_TH;
    if ( type.equals("th") ) {
      mExportType = DISTOX_EXPORT_TH;
    } else if ( type.equals("tlx") ) { 
      mExportType = DISTOX_EXPORT_TLX;
    } else if ( type.equals("dat") ) { 
      mExportType = DISTOX_EXPORT_DAT;
    } else if ( type.equals("svx") ) { 
      mExportType = DISTOX_EXPORT_SVX;
    } else if ( type.equals("tro") ) { 
      mExportType = DISTOX_EXPORT_TRO;
    }
    mGroupDistance = Float.parseFloat( prefs.getString( key[2], GROUP_DISTANCE ) );
    mCalibEps      = Float.parseFloat( prefs.getString( key[3], CALIB_EPS ) );
    mCalibMaxIt    = Integer.parseInt( prefs.getString( key[4], CALIB_MAX_ITER ) );
    mDevice        = prefs.getString( key[5], DEVICE_NAME );
    mLineSegment   = Integer.parseInt( prefs.getString( key[7], LINE_SEGMENT ) );
    mVThreshold    = Float.parseFloat( prefs.getString( key[8], V_THRESHOLD ) );
    mLineAccuracy  = Float.parseFloat( prefs.getString( key[9], LINE_ACCURACY ) );
    mLineCorner    = Float.parseFloat( prefs.getString( key[10], LINE_CORNER ) );
    setLineStyleAndType( prefs.getString( key[11], LINE_STYLE ) );
    mUnit          = Float.parseFloat( prefs.getString( key[13], DRAWING_UNIT ) );
    mGroupBy       = Integer.parseInt( prefs.getString( key[14], GROUP_BY ) );
    
    mCheckBT       = prefs.getBoolean( key[12], CHECK_BT );
    mListRefresh   = prefs.getBoolean( key[15], LIST_REFRESH );

    DrawingBrushPaths.doMakePaths( );

    mData = new DistoXDataHelper( this );
    mCalibration = new Calibration( 0, this );
    mFixed = new ArrayList< DistoXFix >();

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
    
  }

  private void setLineStyleAndType( String style )
  {
    mLineStyle     = LINE_STYLE_BEZIER;
    mLineType      = 1;
    if ( style.equals( "none" ) ) {
      mLineStyle = LINE_STYLE_NONE;
      mLineType  = 1;
    } else if ( style.equals( "two" ) ) {
      mLineStyle = LINE_STYLE_TWO;
      mLineType  = 2;
    } else if ( style.equals( "three" ) ) {
      mLineStyle = LINE_STYLE_THREE;
      mLineType  = 3;
    }
  }

  // public BluetoothAdapter getBTAdapter() { return mBTAdapter; }
  // public DistoXComm getComm() { return mComm; }
  // public DistoXDataHelper getData() { return mData; }
  // public Calibration getCalibration() { return mCalibration; }

  public void resumeComm()
  {
    if ( mComm != null ) { mComm.resume(); }
  }

  public void resetComm() 
  { 
    mComm = null;
    mComm = new DistoXComm( this );
  }

  public boolean isConnected()
  {
    return mComm != null && mComm.mBTConnected;
  }

  public long getSurveyId() { return mSID; }
  public long getCalibId()  { return mCID; }
  public String getSurvey() { return mySurvey; }
  public String getCalib()  { return myCalib; }

  public long setSurveyFromName( String survey ) 
  {
    if ( mData != null ) {
      mSID = mData.setSurvey( survey );
      mySurvey = (mSID > 0)? survey : null;
      return mSID;
    }
    return 0;
  }

  public long setCalibFromName( String calib ) 
  {
    if ( mData != null ) {
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
      mSID = ( mySurvey == null )? 0 : id;
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
    } else if ( k.equals( key[1] ) ) {
      String type = sp.getString( k, EXPORT_TYPE );
      mExportType = DISTOX_EXPORT_TH;
      if ( type.equals("th") ) {
        mExportType = DISTOX_EXPORT_TH;
      } else if ( type.equals("tlx") ) { 
        mExportType = DISTOX_EXPORT_TLX;
      } else if ( type.equals("dat") ) { 
        mExportType = DISTOX_EXPORT_DAT;
      } else if ( type.equals("svx") ) { 
        mExportType = DISTOX_EXPORT_SVX;
      } else if ( type.equals("tro") ) { 
        mExportType = DISTOX_EXPORT_TRO;
      }
      // Log.v( TAG, "exportType " +  mExportType + " type " + type);
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
      setLineStyleAndType( prefs.getString( key[11], LINE_STYLE ) );
    } else if ( k.equals( key[12] ) ) {
      mCheckBT     = sp.getBoolean( k, CHECK_BT );
    } else if ( k.equals( key[13] ) ) {
      mUnit = Float.parseFloat( prefs.getString( key[13], DRAWING_UNIT ) );
      DrawingBrushPaths.doMakePaths( );
    } else if ( k.equals( key[14] ) ) {
      mGroupBy = Integer.parseInt( prefs.getString( key[14], GROUP_BY ) );
    } else if ( k.equals( key[15] ) ) {
      mListRefresh = sp.getBoolean( k, LIST_REFRESH );
    }
  }

  public void setDevice( String device ) 
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

  public String exportSurveyAsTh()
  {
    File dir = new File( TopoDroidApp.APP_TH_PATH );
    if (!dir.exists()) {
      dir.mkdirs();
    }
    String filename = TopoDroidApp.APP_TH_PATH + mySurvey + ".th";
    List<DistoXDBlock> list = mData.selectAllShots( mSID, 0 );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("survey %s -title \"%s\"\n", mySurvey, mySurvey );
      pw.format("# %s \n\n", mData.getSurveyComment( mSID ) );
      if ( mFixed.size() > 0 ) {
        pw.format("  cs latlon\n");
        for ( DistoXFix fix : mFixed ) {
          pw.format("  fix %s %.2f %.2f %.2f m\n", fix.name, fix.lat, fix.lng, fix.alt );
        }
      }
      pw.format("  centerline\n");
      pw.format("    date %s \n", mData.getSurveyDate( mSID ) );
      pw.format("    data normal from to length compass clino\n");
      long extend = 0;
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( item.relativeDistance( ref_item ) < mCloseDistance ) {
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
                pw.format("  flags not duplicate\n");
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            pw.format("    - %s ", to );
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format("  flags not duplicate\n");
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            pw.format("    %s - ", from ); // write splay shot
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          } else {
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format("  flags not duplicate\n");
                duplicate = false;
              }
              n = 0;
            }
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              if ( extend == -1 ) {
                pw.format("    extend left\n");
              } else if ( extend == 1 ) {
                pw.format("    extend right\n");
              } else if ( extend == 0 ) {
                pw.format("    extend vertical\n");
              }
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              pw.format("  flags duplicate\n");
              duplicate = true;
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
          pw.format("  flags not duplicate\n");
          // duplicate = false;
        }
      }
      pw.format("  endcenterline\n");
      pw.format("endsurvey\n");
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

  /** Survex export
   * The following format is used to export the centerline data in survex
   *
   *    *begin survey_name
   *      *units tape feet|metres
   *      *units compass clino grad|degrees
   *      *calibrate declination ...
   *      *date yyyy.mm.dd
   *      *data normal from to tape compass clino
   *      ...
   *      *flags surface|not surface
   *      *flags duplicate|not duplicate
   *      *flags splay|not splay
   *      ...
   *      ; shot_comment
   *      ...
   *      (optional survey commands)
   *    *end
   */
  public String exportSurveyAsSvx()
  {
    File dir = new File( TopoDroidApp.APP_SVX_PATH );
    if (!dir.exists()) {
      dir.mkdirs();
    }
    String filename = TopoDroidApp.APP_SVX_PATH + mySurvey + ".svx";
    List<DistoXDBlock> list = mData.selectAllShots( mSID, 0 );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("*begin %s\n", mySurvey );
      pw.format("  *units tape meters\n");
      pw.format("  *units compass degrees\n");
      pw.format("  *units clino degrees\n");
      pw.format("; %s \n\n", mData.getSurveyComment( mSID ) );
      pw.format("  *date %s \n", mData.getSurveyDate( mSID ) );
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
            if ( item.relativeDistance( ref_item ) < mCloseDistance ) {
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
                pw.format("  *flags not duplicate\n");
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
                pw.format("  *flags not duplicate\n");
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
                pw.format("  *flags not duplicate\n");
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
              pw.format("  *flags duplicate\n");
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
          pw.format("  flags not duplicate\n");
          // duplicate = false;
        }
      }
      pw.format("*end\n");
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

  public String exportSurveyAsTlx()
  {
    File dir = new File( TopoDroidApp.APP_TLX_PATH );
    if (!dir.exists()) {
      dir.mkdirs();
    }
    String filename = TopoDroidApp.APP_TLX_PATH + mySurvey + ".tlx";
    List<DistoXDBlock> list = mData.selectAllShots( mSID, 0 );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("tlx2\n");
      pw.format("# date %s \n", mData.getSurveyDate( mSID ) );
      pw.format("# %s \n", mData.getSurveyComment( mSID ) );
      int n = 0;
      float l=0.0f, b=0.0f, c=0.0f;
      float l0[] = new float[10];
      float b0[] = new float[10];
      float c0[] = new float[10];
      float r0[] = new float[10];
      DistoXDBlock ref_item = null;
      int extend = 0;
      int flag   = DistoXDBlock.BLOCK_SURVEY;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from != null && from.length() > 0 ) {
          if ( to != null && to.length() > 0 ) {
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
              while ( n > 0 ) {
                -- n;
                pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
              }
              extend = 0;
              flag   = DistoXDBlock.BLOCK_SURVEY;
            }
            ref_item = item;
            // item.Comment()
            pw.format("    \"%s\" \"%s\" ", from, to );
            l = item.mLength;
            b = item.mBearing;
            c = item.mClino;
            extend = (int) item.mExtend;
            flag   = (int) item.mFlag;
            l0[0] = item.mLength;
            b0[0] = item.mBearing;
            c0[0] = item.mClino;
            r0[0] = item.mRoll;
            n = 1;
          } else { // to.isEmpty()
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
              while ( n > 0 ) {
                -- n;
                pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1fi %.1f\n", l0[n], b0[n], c0[n], r0[n] );
              }
              ref_item = null;
              extend = 0;
              flag   = DistoXDBlock.BLOCK_SURVEY;
            }
            // item.Comment()
            pw.format("    \"%s\" \"\" ", from );
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
              item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
          }
        } else { // from.isEmpty()
          if ( to != null && to.length() > 0 ) {
            if ( n > 0 ) {
              b = in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
              while ( n > 0 ) {
                -- n;
                pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
              }
              ref_item = null;
              extend = 0;
              flag   = DistoXDBlock.BLOCK_SURVEY;
            }
            // item.Comment()
            pw.format("    \"\" \"%s\" ", to );
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
              item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
          } else {
            // not exported
            if ( item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0[0] );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              l0[n] = item.mLength;
              b0[n] = item.mBearing;
              c0[n] = item.mClino;
              r0[n] = item.mRoll;
              ++n;
            }
          }
        }
      }
      if ( n > 0 ) {
        b = in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
        while ( n > 0 ) {
          -- n;
          pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
        }
        // extend = 0;
        // flag   = DistoXDBlock.BLOCK_SURVEY;
      }
      // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d %d\n", 
      //   item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, 0, 1 );
      // item.mComment
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

  private float in360( float b ) { return (b<0.0f)? b+360.0f : b; }

  private float around( float bb, float b0 ) 
  {
    while ( ( bb - b0 ) > 180.0f ) bb -= 360.0f;
    while ( ( b0 - bb ) > 180.0f ) bb += 360.0f;
    return bb;
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

  private static final float M2FT = 3.28083f; // meters to feet 
  // private static final byte char0C = 0x0c;

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

  private void printShotToTro( PrintWriter pw, DistoXDBlock item, float l, float b, float c, int n,
                               LRUD lrud, boolean start )
  {
    b = in360( b/n );
    if ( start ) {
      pw.format(Locale.ENGLISH, "%s %s 0.00 0.00 0.00 ", item.mFrom, item.mFrom );
    }
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I\r\n", lrud.l, lrud.r, lrud.u, lrud.d );
    pw.format("%s %s ", item.mFrom, item.mTo );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f ", (l/n), b, c/n );
    // if ( duplicate ) {
    //   // pw.format(" #|L#");
    // }
    // if ( comment != null && comment.length() > 0 ) {
    //   // pw.format(" %s", comment );
    // }
  }


  private LRUD computeLRUD( DistoXDBlock b, List<DistoXDBlock> list )
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
    String station = b.mFrom;
    
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

  public String exportSurveyAsDat()
  {
    File dir = new File( TopoDroidApp.APP_DAT_PATH );
    if (!dir.exists()) {
      dir.mkdirs();
    }
    String filename = TopoDroidApp.APP_DAT_PATH + mySurvey + ".dat";
    List<DistoXDBlock> list = mData.selectAllShots( mSID, 0 );
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
            if ( item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list );
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
              LRUD lrud = computeLRUD( ref_item, list );
              pw.format("%s-%s %s-%s ", mySurvey, ref_item.mFrom, mySurvey, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list );
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
        LRUD lrud = computeLRUD( ref_item, list );
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


  /* export as VisualTopo
   */
  public String exportSurveyAsTro()
  {
    File dir = new File( TopoDroidApp.APP_TRO_PATH );
    if (!dir.exists()) {
      dir.mkdirs();
    }
    String filename = TopoDroidApp.APP_TRO_PATH + mySurvey + ".tro";
    List<DistoXDBlock> list = mData.selectAllShots( mSID, 0 );
    try {
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("Version 5.02\r\n\r\n");
      if ( mFixed.size() > 0 ) {
        for ( DistoXFix fix : mFixed ) {
          // pw.format("Trou %s,%.2f,%.2f,%.2f\r\n", mySurvey, fix.lat, fix.lng, fix.alt );
          // pw.format("Entree %s\r\n", fix.name );
          break;
        }
      } else {
        // pw.format("Trou %s\r\n", mySurvey );
      }
      // pw.format("Club ...\r\n");
      pw.format("Couleur 0,0,0\r\n\r\n");
      
      pw.format("Param Deca Deg Clino Deg 0.0000 Dir,Dir,Dir Inc 0,0,0\r\n\r\n");

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
            if ( item.relativeDistance( ref_item ) < mCloseDistance ) {
              float bb = around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list );
              printShotToTro( pw, ref_item, l, b, c, n, lrud, start );
              start = false;
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 ) { // write pervious leg shot
              LRUD lrud = computeLRUD( ref_item, list );
              printShotToTro( pw, ref_item, l, b, c, n, lrud, start );
              start = false;
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 ) {
              LRUD lrud = computeLRUD( ref_item, list );
              printShotToTro( pw, ref_item, l, b, c, n, lrud, start );
              start = false;
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
        LRUD lrud = computeLRUD( ref_item, list );
        printShotToTro( pw, ref_item, l, b, c, n, lrud, start );
      }
      pw.format(" * * * * N I\r\n");

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      return null;
    }
  }

}
