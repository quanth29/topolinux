/* @file DrawingActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main drawing activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 point names in the point dialog
 * 20120621 begin long-click (item options editing)
 * 20120706 display screen scale factor
 * 20120715 per-category preferences
 * 20120725 TopoDroidApp log
 * 20121206 using symbol libraries
 * 20121220 handle missing symbols with Toast
 * 20121220 plot with mising symbols are not saved by default onPause
 * 20121220 if missing symbol saveTh menu has confirm dialog
 * 20121220 menu to invoke TdSymbol --> force DrawingBrushPaths reload symbols
 * 20121221 bug-fix isDrawing false onPause
 * 20121221 zoom button controls; replaced zoom button with "display mode" button
 * 20121221 undo button always enabled; removed canRedo variable
 * 20121224 bug-fix offset adjustment on zoom change
 * 20121225 point/line/area delete
 * 20130131 fixed multitouch
 * 20130213 save dialog (therion + PNG )
 * 20130307 made Annotations into a dialog
 * 20130825 added updateBlockName
 * 20130826 added splitLine
 * 20131201 button bar new interface. reorganized actions
 * ... interface restyling: buttons and all that
 * 20140115 sharpen line
 * 20140303 symbol picker mode, list or grid
 * 20140328 section plot with splays and leg(s) of the station the section line looks to
 * 20140401 cross-section bug fix
 * 20140513 export as cSurvey
 * 20140515 blus paint for latest shots
 * 20140527 selection radius cutoff (minimum value)
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.util.TypedValue;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
// import android.view.SubMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.ViewGroup;
import android.view.Display;
import android.util.DisplayMetrics;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ZoomControls;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.FloatMath;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

// import android.view.MenuInflater;
// import android.support.v7.app.ActionBar;
// import android.support.v7.app.ActionBarActivity;

/**
 */
public class DrawingActivity extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      // , View.OnLongClickListener
                                      , OnItemClickListener
                                      , OnItemSelectedListener
                                      , OnZoomListener
                                      , ILabelAdder
                                      , ILister
{
  private static int PLOT_BUTTON = 6; // index of the plot button (mButton1)
  private static int DOWNLOAD_BUTTON = 4; // index of the download utton (mButton1)
  // nr. 8 6 7 3
  private static int icons00[];
  private static int icons00ok[];

  private static int iconsok[] = { 
                        R.drawable.ic_edit_ok, // 0
                        R.drawable.ic_eraser_ok,
                        R.drawable.ic_select_ok };
  private static int ixonsok[] = { 
                        R.drawable.ix_edit_ok, // 0
                        R.drawable.ix_eraser_ok,
                        R.drawable.ix_select_ok };
                      
  private static int icons[] = { 
                        R.drawable.ic_edit,          // 0
                        R.drawable.ic_eraser,
                        R.drawable.ic_select,
                        R.drawable.ic_refs,          // 3
                        R.drawable.ic_download,      // 
                        R.drawable.ic_note,
                        R.drawable.ic_plan,
                        R.drawable.ic_info,          // 7 ic_details,
                        R.drawable.ic_undo,
                        R.drawable.ic_redo,
                        R.drawable.ic_list,          // 10
                        R.drawable.ic_back,          // 11
                        R.drawable.ic_forw,
                        R.drawable.ic_join,
                        R.drawable.ic_note,          // 14
                        0,
                        R.drawable.ic_menu,          // 16
                        R.drawable.ic_extended,
                        R.drawable.ic_join_no,
                      };
  private static int ixons[] = { 
                        R.drawable.ix_edit, // 0
                        R.drawable.ix_eraser,
                        R.drawable.ix_select,
                        R.drawable.ix_refs,          // 3
                        R.drawable.ix_download,      // 4 <-- updateBlockList
                        R.drawable.ix_note,
                        R.drawable.ix_plan,          // 6 <-- switchPlotType
                        R.drawable.ix_info,          // 7 ic_details,
                        R.drawable.ix_undo,
                        R.drawable.ix_redo,
                        R.drawable.ix_list,          // 10
                        R.drawable.ix_back,
                        R.drawable.ix_forw,
                        R.drawable.ix_join,          // 13 <-- setButton3
                        R.drawable.ix_note,          // 14
                        0,
                        R.drawable.ix_menu,          // 16
                        R.drawable.ix_extended,
                        R.drawable.ix_join_no,
                      };
  private static int menus[] = {
                        R.string.menu_export,
                        R.string.menu_reload,
                        R.string.menu_delete,
                        R.string.menu_palette,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = { R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_refs,
                        R.string.help_download,
                        R.string.help_note,
                        R.string.help_toggle_plot,
                        R.string.help_stats,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_symbol_plot,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_line_point, // R.string.help_to_point,
                        R.string.help_note_plot
                      };
  private static int help_menus[] = {
                        R.string.help_save_plot,
                        R.string.help_recover,
                        R.string.help_trash,
                        R.string.help_symbol,
                        R.string.help_prefs,
                        R.string.help_help
                      };
    private TopoDroidApp mApp;
    private DataDownloader mDataDownloader;

    private PlotInfo mPlot1;
    private PlotInfo mPlot2;

    long getSID() { return mApp.mSID; }
    String getSurvey() { return mApp.mySurvey; }

    // 0: no bezier, plain path
    // 1: bezier interpolator

    public static final float CENTER_X = 100f;
    public static final float CENTER_Y = 120f;

    static final float SCALE_FIX = 20.0f; // FIXME
    private String mSectionName;

    private BezierInterpolator mBezierInterpolator;
    private DrawingSurface  mDrawingSurface;
    private DrawingLinePath mCurrentLinePath;
    private DrawingAreaPath mCurrentAreaPath;
    private DrawingPath mFixedDrawingPath;
    // private Paint mCurrentPaint;
    LinearLayout popup_layout = null;
    PopupWindow popup_window = null;

    // private MenuItem mMIrefs;
    private MenuItem mMIsymbol;
    private MenuItem mMIsave;
    private MenuItem mMIdelete;
    private MenuItem mMIoptions;
    private MenuItem mMIrecover;
    private MenuItem mMIhelp;

    // private boolean canRedo;
    private DistoXNum mNum;
    private int mPointCnt; // counter of points in the currently drawing line

    private boolean mIsNotMultitouch;

    private DrawingBrush mCurrentBrush;
    private Path  mCurrentPath;

    private String mName;
    String mName1;  // first name (PLAN)
    private String mName2;  // second name (EXTENDED)
    private String mFullName1;
    private String mFullName2;

    private boolean mEditMove;    // whether moving the selected point
    private boolean mShiftMove;   // whether to move the canvas in point-shift mode
    boolean mShiftDrawing;        // whether to shift the drawing

    ZoomButtonsController mZoomBtnsCtrl;
    View mZoomView;
    ZoomControls mZoomCtrl;
    // ZoomButton mZoomOut;
    // ZoomButton mZoomIn;
    private float oldDist;  // zoom pointer-sapcing

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public static final int MODE_DRAW  = 1;
    public static final int MODE_MOVE  = 2;
    public static final int MODE_EDIT  = 3;
    public static final int MODE_ZOOM = 4;  // used only for touchMode
    public static final int MODE_SHIFT = 5; // change point symbol position
    public static final int MODE_ERASE = 6;

    public int mMode   = MODE_MOVE;
    private int mTouchMode = MODE_MOVE;
    private float mSaveX;
    private float mSaveY;
    private float mSave0X;
    private float mSave0Y;
    private float mSave1X;
    private float mSave1Y;
    private float mStartX; // line shift scene start point
    private float mStartY;
    private PointF mOffset  = new PointF( 0f, 0f );
    private PointF mOffset0 = new PointF( 0f, 0f );
    private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );
    private PointF mDisplayCenter;
    private float mZoom  = 1.0f;

    private DataHelper mData;
    private long mSid;  // survey id
    private long mPid1; // plot id
    private long mPid2; // plot id
    private long mPid;  // current plot id
    private long mType;  // current plot type
    private String mFrom;
    private float mAzimuth = 0.0f;

    private boolean mAllSymbols; // whether the library has all the symbols of the plot

    // private Timer mTimer;
    // private TimerTask mTask;

    @Override
    public void onVisibilityChanged(boolean visible)
    {
      mZoomBtnsCtrl.setVisible( visible );
    }

    @Override
    public void onZoom( boolean zoomin )
    {
      if ( zoomin ) changeZoom( ZOOM_INC );
      else changeZoom( ZOOM_DEC );
    }

    private void changeZoom( float f ) 
    {
      float zoom = mZoom;
      mZoom     *= f;
      // Log.v( TopoDroidApp.TAG, "zoom " + mZoom );
      mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
      mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
      // mZoomCtrl.hide();
      // mZoomBtnsCtrl.setVisible( false );
    }

    // private void resetZoom() 
    // {
    //   int w = mDrawingSurface.width();
    //   int h = mDrawingSurface.height();
    //   mOffset.x = w/4;
    //   mOffset.y = h/4;
    //   mZoom = mApp.mScaleFactor;
    //   // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "zoom one " + mZoom + " off " + mOffset.x + " " + mOffset.y );
    //   if ( mType == PlotInfo.PLOT_PLAN ) {
    //     float zx = w/(mNum.surveyEmax() - mNum.surveyEmin());
    //     float zy = h/(mNum.surveySmax() - mNum.surveySmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
    //     float zx = w/(mNum.surveyHmax() - mNum.surveyHmin());
    //     float zy = h/(mNum.surveyVmax() - mNum.surveyVmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else {
    //     mZoom = mApp.mScaleFactor;
    //     mOffset.x = 0.0f;
    //     mOffset.y = 0.0f;
    //   }
    //     
    //   // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "zoom one to " + mZoom );
    //     
    //   mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    //   // mDrawingSurface.refresh();
    // }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    // public void zoomOne() { resetZoom( ); }

    // public void zoomView( )
    // {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "zoomView ");
    //   DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
    //   zoom.show();
    // }


    // void setType( int type )
    // {
    //   mSymbol = SYMBOL_POINT;
    //   setTheTitle();
    // }

    private static float toSceneX( float x ) { return mCenter.x + x * SCALE_FIX; }
    private static float toSceneY( float y ) { return mCenter.y + y * SCALE_FIX; }

    static float sceneToWorldX( float x ) { return (x - mCenter.x)/SCALE_FIX; }
    static float sceneToWorldY( float y ) { return (y - mCenter.y)/SCALE_FIX; }

    private void resetFixedPaint( )
    {
      mDrawingSurface.resetFixedPaint( DrawingBrushPaths.fixedShotPaint );
    }

    private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, 
                               boolean splay, boolean selectable )
    {
      if ( splay ) {
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedSplayPaint );
      } else {
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
        mFixedDrawingPath.setPaint( ( blk.isRecent( mApp.mSecondLastShotId ) )? DrawingBrushPaths.fixedBluePaint 
                                    : DrawingBrushPaths.fixedShotPaint );
      }
      mFixedDrawingPath.mPath  = new Path();
      x1 = toSceneX( x1 );
      y1 = toSceneY( y1 );
      x2 = toSceneX( x2 );
      y2 = toSceneY( y2 );
      mFixedDrawingPath.setEndPoints( x1, y1, x2, y2 ); // this sets the midpoint only
      mFixedDrawingPath.mPath.moveTo( x1 - xoff, y1 - yoff );
      mFixedDrawingPath.mPath.lineTo( x2 - xoff, y2 - yoff );
      mDrawingSurface.addFixedPath( mFixedDrawingPath, selectable );
    }

    public void addGrid( float xmin, float xmax, float ymin, float ymax, float xoff, float yoff )
    {
      xmin -= 10.0f;
      xmax += 10.0f;
      ymin -= 10.0f;
      ymax += 10.0f;
      float x1 = (float)(toSceneX( xmin ) - xoff);
      float x2 = (float)(toSceneX( xmax ) - xoff);
      float y1 = (float)(toSceneY( ymin ) - yoff);
      float y2 = (float)(toSceneY( ymax ) - yoff);
      // mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );

      for ( int x = (int)Math.round(xmin); x < xmax; x += 1 ) {
        float x0 = (float)(toSceneX( x ) - xoff);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint(
          (Math.abs(x%10)==5)? DrawingBrushPaths.fixedGrid10Paint : DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.mPath  = new Path();
        mFixedDrawingPath.mPath.moveTo( x0, y1 );
        mFixedDrawingPath.mPath.lineTo( x0, y2 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
      for ( int y = (int)Math.round(ymin); y < ymax; y += 1 ) {
        float y0 = (float)(toSceneY( y ) - yoff);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint(
          (Math.abs(y%10)==5)? DrawingBrushPaths.fixedGrid10Paint : DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.mPath  = new Path();
        mFixedDrawingPath.mPath.moveTo( x1, y0 );
        mFixedDrawingPath.mPath.lineTo( x2, y0 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
    }

    private void setTheTitle()
    {
      String s1 = mApp.getConnectionStateTitleStr();
      Resources res = getResources();
      if ( mMode == MODE_DRAW ) { 
        if ( mSymbol == SYMBOL_POINT ) {
          setTitle( s1 + String.format( res.getString(R.string.title_draw_point), 
                                   DrawingBrushPaths.mPointLib.getAnyPointName(mCurrentPoint) ) );
        } else if ( mSymbol == SYMBOL_LINE ) {
          setTitle( s1 + String.format( res.getString(R.string.title_draw_line),
                                   DrawingBrushPaths.getLineName(mCurrentLine) ) );
        } else  {  // if ( mSymbol == SYMBOL_LINE ) 
          setTitle( s1 + String.format( res.getString(R.string.title_draw_area),
                                   DrawingBrushPaths.mAreaLib.getAreaName(mCurrentArea) ) );
        }
      } else if ( mMode == MODE_MOVE ) {
        setTitle( s1 + res.getString( R.string.title_move ) );
      } else if ( mMode == MODE_EDIT ) {
        setTitle( s1 + res.getString( R.string.title_edit ) );
      } else if ( mMode == MODE_SHIFT ) {
        setTitle( s1 + res.getString( R.string.title_shift ) );
      } else if ( mMode == MODE_ERASE ) {
        setTitle( s1 + res.getString( R.string.title_erase ) );
      }
      if ( ! mDrawingSurface.isSelectable() ) {
        setTitle( s1 + getTitle() + " [!s]" );


      }
    }

    private void AlertMissingSymbols()
    {
      new TopoDroidAlertDialog( this, getResources(),
                        getResources().getString( R.string.missing_symbols ),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mAllSymbols = true;
          }
        }
      );
    }

    private void immediateSaveTh2( )
    {
      // Log.v( TopoDroidApp.TAG, " savingTh2 " + mFullName1 + " " + mFullName2 + " do save ");
      Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
          mApp.mShotActivity.enableSketchButton( true );
        }
      };
      mApp.mShotActivity.enableSketchButton( false );
      (new SaveTh2File(this, handler, mApp, mDrawingSurface, mFullName1, mFullName2 )).execute();
    }

    private void doSaveTh2( boolean not_all_symbols )
    {
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT,
                        " doSaveTh2 " + mFullName1 + " " + mFullName2 + " not all symbols " + not_all_symbols );
      if ( mFullName1 != null && mDrawingSurface != null ) {
        if ( not_all_symbols ) {
          AlertMissingSymbols();
        }
        if ( mAllSymbols ) {
          String p1 = mApp.getTh2FileWithExt( mFullName1 );
          File f1 = new File( p1 );
          File b1 = new File( p1 + ".bck" );
          f1.renameTo( b1 );
          if ( mFullName2 != null ) {
            String p2 = mApp.getTh2FileWithExt( mFullName2 );
            File f2 = new File( p2 );
            File b2 = new File( p2 + ".bck" );
            f2.renameTo( b2 );
          }

          // Toast.makeText( this, R.string.sketch_saving, Toast.LENGTH_SHORT ).show();

          final boolean alert = not_all_symbols;
          final Activity currentActivity  = this;
          Handler saveHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
              mApp.mShotActivity.enableSketchButton( true );
              if ( alert ) {
                if (msg.what == 1 ) { // FIXME mFullName
                  Toast.makeText( currentActivity, 
                        getString(R.string.saved_file_) + " " + mFullName1 + ".th2", Toast.LENGTH_SHORT ).show();
                } else {
                  Toast.makeText( currentActivity, 
                        getString(R.string.saving_file_failed), Toast.LENGTH_SHORT ).show();
                }
              }
            }
          } ;

          mApp.mShotActivity.enableSketchButton( false );
          (new SaveTh2File(this, saveHandler, mApp, mDrawingSurface, mFullName1, mFullName2 )).execute();
        } else { // mAllSymbols is false
          // FIXME what to do ?
         Toast.makeText( this,
           "NOT SAVING " + mFullName1 + " " + mFullName2, Toast.LENGTH_LONG ).show();
        }
      }
    }

  private void computeReferences( int type, float xoff, float yoff, float zoom )
  {
    if ( type != PlotInfo.PLOT_PLAN && type != PlotInfo.PLOT_EXTENDED ) return;

    mDrawingSurface.clearReferences( type );
    mDrawingSurface.setManager( type );

    if ( type == PlotInfo.PLOT_PLAN ) {
      addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(), xoff, yoff );
    } else {
      addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(), xoff, yoff );
    }

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots = mNum.getShots();
    List< NumSplay > splays = mNum.getSplays();
    // Log.v( TopoDroidApp.TAG, "stations " + stations.size() + " legs " + shots.size() );
    // Log.v( TopoDroidApp.TAG, "compute refs. offs " + xoff + " " + yoff + " zoom " + zoom );
    if ( type == PlotInfo.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        addFixedLine( sh.getFirstBlock(), (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), 
                      xoff, yoff, false, true );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, 
        //   "add line " + (float)(st1.e) + " " + (float)(st1.s) + " " + (float)(st2.e) + " " + (float)(st2.s) );
      }
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.getBlock(), (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), 
                      xoff, yoff, true, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addStation( st.name, toSceneX(st.e) - xoff, toSceneY(st.s) - yoff, st.mDuplicate, true );
      }
    } else { // if ( type == PlotInfo.PLOT_EXTENDED && 
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          addFixedLine( sh.getFirstBlock(), (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v), 
                      xoff, yoff, false, true );
          // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "line " + toSceneX(st1.h) + " " + toSceneY(st1.v) + " - " + toSceneX(st2.h) + " " + toSceneY(st2.v) );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.getBlock(), (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), 
                      xoff, yoff, true, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addStation( st.name, toSceneX(st.h) - xoff, toSceneY(st.v) - yoff, st.mDuplicate, true );
      }
    }

    if ( (! mNum.surveyAttached) && mApp.mCheckAttached ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_SHORT ).show();
    }

  }
    

  // private Button mButtonHelp;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private int mNrButton1 = 8;          // main-primary
  private int mNrButton2 = 6;          // draw
  private int mNrButton3 = 7;          // edit
  private int mNrButton5 = 3;          // erase
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  HorizontalButtonView mButtonView3;
  // HorizontalButtonView mButtonView4;
  HorizontalButtonView mButtonView5;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  List<DistoXDBlock> mList = null;

  int mHotItemType = -1;
  private boolean inLinePoint = false;

  float zoom() { return mZoom; }

  // set the button3 by the type of the hot-item
  void setButton3( int type )
  {
    mHotItemType = type;
    if (    type == DrawingPath.DRAWING_PATH_POINT 
         || type == DrawingPath.DRAWING_PATH_LINE 
         || type == DrawingPath.DRAWING_PATH_AREA 
         || type == DrawingPath.DRAWING_PATH_STATION ) {
      inLinePoint = true;
      mButton3[5].setBackgroundResource( icons00[13] );
    } else {
      inLinePoint = false;
      mButton3[5].setBackgroundResource( icons00[18] );
    }
  }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      // Display display = getWindowManager().getDefaultDisplay();
      // DisplayMetrics dm = new DisplayMetrics();
      // display.getMetrics( dm );
      // int width = dm widthPixels;
      int width = getResources().getDisplayMetrics().widthPixels;

      mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
      mSectionName = null;
      mShiftDrawing = false;

      setContentView(R.layout.drawing_activity);
      mApp = (TopoDroidApp)getApplication();
      mDataDownloader = new DataDownloader( this, mApp, this );
      mZoom = mApp.mScaleFactor;    // canvas zoom

      mDisplayCenter = new PointF(mApp.mDisplayWidth  / 2, mApp.mDisplayHeight / 2);

      // setCurrentPaint();
      mCurrentBrush = new DrawingPenBrush();

      mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mDrawingSurface.setActivity( this );
      mDrawingSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
      mDrawingSurface.previewPath.mPath = new Path();
      mDrawingSurface.previewPath.setPaint( getPreviewPaint() );
      mDrawingSurface.setOnTouchListener(this);
      // mDrawingSurface.setOnLongClickListener(this);
      // mDrawingSurface.setBuiltInZoomControls(true);


      if ( mIsNotMultitouch ) {
        mZoomView = (View) findViewById(R.id.zoomView );
        mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
        // ViewGroup vg = mZoomBtnsCtrl.getContainer();
      }

      mListView = (HorizontalListView) findViewById(R.id.listview);
      mApp.setListViewHeight( mListView );
      icons00   = ( TopoDroidApp.mSizeButtons == 2 )? ixons : icons;
      icons00ok = ( TopoDroidApp.mSizeButtons == 2 )? ixonsok : iconsok;

      mButton1 = new Button[ mNrButton1 ];
      for ( int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = new Button( this );
        mButton1[k].setPadding(0,0,0,0);
        mButton1[k].setOnClickListener( this );
        mButton1[k].setBackgroundResource( icons00[k] );
      }
      mDataDownloader.mButtonDownload = mButton1[4]; // download-index

      mButton2 = new Button[ mNrButton2 ];
      for ( int k=0; k<mNrButton2; ++k ) {
        mButton2[k] = new Button( this );
        mButton2[k].setPadding(0,0,0,0);
        mButton2[k].setOnClickListener( this );
        if ( k == 0 ) {
          mButton2[k].setBackgroundResource( icons00ok[k] );
        } else if ( k < 3 ) {
          mButton2[k].setBackgroundResource( icons00[k] );
        } else {
          mButton2[k].setBackgroundResource( icons00[8-3+k] );
        }
      }

      mButton3 = new Button[ mNrButton3 ];
      for ( int k=0; k<mNrButton3; ++k ) {
        mButton3[k] = new Button( this );
        mButton3[k].setPadding(0,0,0,0);
        mButton3[k].setOnClickListener( this );
        if ( k == 2 ) {
          mButton3[k].setBackgroundResource( icons00ok[k] );
        } else if ( k < 3 ) {
          mButton3[k].setBackgroundResource( icons00[k] );
        } else {
          mButton3[k].setBackgroundResource( icons00[11-3+k] );
        }
      }

      mButton5 = new Button[ mNrButton5 ];
      for ( int k=0; k<mNrButton5; ++k ) {
        mButton5[k] = new Button( this );
        mButton5[k].setPadding(0,0,0,0);
        mButton5[k].setOnClickListener( this );
        if ( k == 1 ) {
          mButton5[k].setBackgroundResource( icons00ok[k] );
        } else if ( k < 3 ) {
          mButton5[k].setBackgroundResource( icons00[k] );
        } else {
          mButton5[k].setBackgroundResource( icons00[8-3+k] );
        }
      }
      if ( TopoDroidApp.mActivityLevel < 1 ) {
        mButton1[2].setVisibility( View.GONE );
        mButton2[2].setVisibility( View.GONE );
        mButton3[2].setVisibility( View.GONE );
        mButton5[2].setVisibility( View.GONE );
      }

      if ( mApp.mDevice == null ) {
        mButton1[DOWNLOAD_BUTTON].setBackgroundResource( 
          (TopoDroidApp.mSizeButtons == 2 )? R.drawable.ix_add : R.drawable.ic_add );
      }

      mButtonView1 = new HorizontalButtonView( mButton1 );
      mButtonView2 = new HorizontalButtonView( mButton2 );
      mButtonView3 = new HorizontalButtonView( mButton3 );
      mButtonView5 = new HorizontalButtonView( mButton5 );
      mListView.setAdapter( mButtonView1.mAdapter );

      // redoBtn.setEnabled(false);
      // undoBtn.setEnabled(false); // let undo always be there

      DrawingBrushPaths.makePaths( getResources() );
      setTheTitle();

      // mTimer = null;
      // mTask  = null;

      mData        = mApp.mData; // new DataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mSid         = extras.getLong(   mApp.TOPODROID_SURVEY_ID );
      mName1       = extras.getString( mApp.TOPODROID_PLOT_NAME );
      mName2       = extras.getString( mApp.TOPODROID_PLOT_NAME2 );
      mFrom        = extras.getString( mApp.TOPODROID_PLOT_FROM );
      mAzimuth = 0.0f;
      mFullName1   = mApp.mySurvey + "-" + mName1;
      if ( mName2 != null && mName2.length() > 0 ) {
        mFullName2   = mApp.mySurvey + "-" + mName2;
      } else {
        mName2 = null;
        mFullName2 = null;
      }
      mName = mName1;

      mType = (int)extras.getLong( mApp.TOPODROID_PLOT_TYPE );
      if ( ( mType != PlotInfo.PLOT_SECTION && mType != PlotInfo.PLOT_H_SECTION ) ) {
      } else {
        if ( mType == PlotInfo.PLOT_SECTION ) {
          mAzimuth = (float)extras.getLong( mApp.TOPODROID_PLOT_AZIMUTH );
          TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "azimuth " + mAzimuth );
        }
      }

      mBezierInterpolator = new BezierInterpolator( );

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      mImage.setBackgroundResource( icons00[16] );
      mMenu = (ListView) findViewById( R.id.menu );
      setMenuAdapter();
      closeMenu();
      mMenu.setOnItemClickListener( this );

      doStart();
    }

    @Override
    protected synchronized void onResume()
    {
      super.onResume();
      doResume();
    }

    @Override
    protected synchronized void onPause() 
    { 
      doPause();
      super.onPause();
    }

    @Override
    protected synchronized void onStart()
    {
      super.onStart();
    }

    @Override
    protected synchronized void onStop()
    {
      super.onStop();
    }

    private void doResume()
    {
      PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
      mOffset.x = info.xoffset;
      mOffset.y = info.yoffset;
      mZoom     = info.zoom;
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "doResume " + mName + " offset " +  mOffset.x + " " + mOffset.y + " zoom " + mZoom );
      mDrawingSurface.isDrawing = true;
    }

    private void doPause()
    {
      if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
      mDrawingSurface.isDrawing = false;
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "doPause " + mName + " offset " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
      if ( mPid >= 0 ) mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
      doSaveTh2( false ); // do not alert-dialog on mAllSymbols
    }

    private void doStart()
    {
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "doStart " + mName1 + " " + mName2 );

      mCurrentPoint = 1; // DrawingBrushPaths.POINT_LABEL;
      mCurrentLine  = 1; // DrawingBrushPaths.mLineLib.mLineWallIndex;
      mCurrentArea  = 1; // DrawingBrushPaths.AREA_WATER;

      if ( ( mType != PlotInfo.PLOT_SECTION && mType != PlotInfo.PLOT_H_SECTION ) ) {
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      } else {
        mList = mData.selectAllShotsAtStation( mSid, mFrom );
        TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "nr shots at " + mFrom + " = " + mList.size() );
      }

      loadFiles( mType );

      if ( mType == PlotInfo.PLOT_SECTION || mType == PlotInfo.PLOT_H_SECTION ) {
        addGrid( -10, 10, -10, 10, 0.0f, 0.0f );
        mDrawingSurface.setScaleBar( mCenter.x, mCenter.y ); // (90,160) center of the drawing
        // Log.v( TopoDroidApp.TAG, "drawing " + mName1 + " type "+ mType + " start " + mFrom + " azimuth " + mAzimuth );
        for ( DistoXDBlock b : mList ) {
          float d = b.mLength;
          boolean splay = b.mType == DistoXDBlock.BLOCK_SPLAY;
          if ( mType == PlotInfo.PLOT_SECTION ) {
            float y = - d * (float)Math.sin( b.mClino * TopoDroidUtil.GRAD2RAD );
            float x =   d * (float)Math.cos( b.mClino * TopoDroidUtil.GRAD2RAD )
                          * (float)Math.sin( ( b.mBearing - mAzimuth ) * TopoDroidUtil.GRAD2RAD );
            TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "section blk " + d + " " + b.mBearing + " " + b.mClino + " X " + x + " Y " + y );
            addFixedLine( b, 0, 0, x, y, 0, 0, splay, false );
          } else {
            d *= (float)Math.cos( b.mClino * TopoDroidUtil.GRAD2RAD );
            float y = - d * (float)Math.cos( b.mBearing * TopoDroidUtil.GRAD2RAD );
            float x =   d * (float)Math.sin( b.mBearing * TopoDroidUtil.GRAD2RAD );
            TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "h-section blk " + d + " " + b.mBearing + " " + b.mClino + " X " + x + " Y " + y );
            addFixedLine( b, 0, 0, x, y, 0, 0, splay, false );
          }
        }
      }
    }

    private void loadFiles( long type )
    {
      // Log.v( TopoDroidApp.TAG, "load " + mName1 + " " + mName2 );
      mPlot1 = mApp.mData.getPlotInfo( mSid, mName1 );
      mPid1        = mPlot1.id;
      if ( mName2 != null ) {
        mPlot2 = mApp.mData.getPlotInfo( mSid, mName2 );
        mPid2        = mPlot2.id;
      } else {
        mPlot2 = null;
        mPid2 = -1;
      }
      mPid = mPid1;
      // Log.v( TopoDroidApp.TAG, "loadFiles pid " + mName1 + " " + mName2 );

      String start = mPlot1.start;
      String view  = mPlot1.view;
      mType        = mPlot1.type;
      // Log.v( TopoDroidApp.TAG, "loadFiles start <" + start + "> view <" + view + ">" );

      mAllSymbols  = true; // by default there are all the symbols

      if ( mType != PlotInfo.PLOT_SECTION && mType != PlotInfo.PLOT_H_SECTION ) {
        if ( mList.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          if ( mPid1 >= 0 ) mApp.mData.dropPlot( mPid1, mSid );
          if ( mPid2 >= 0 ) mApp.mData.dropPlot( mPid2, mSid );
          finish();
        } else {
          mNum = new DistoXNum( mList, start, view );
          computeReferences( (int)PlotInfo.PLOT_PLAN, mOffset.x, mOffset.y, mZoom );
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, mOffset.x, mOffset.y, mZoom );
        }
      }
      // now try to load drawings from therion file
      // Log.v( TopoDroidApp.TAG, "load th2 file " + mFullName1 + " " + mFullName2 );

      String filename1 = mApp.getTh2FileWithExt( mFullName1 );
      String filename2 = null;
      if ( mFullName2 != null ) {
        filename2 = mApp.getTh2FileWithExt( mFullName2 );
      }

      // Toast.makeText( this, R.string.sketch_loading, Toast.LENGTH_SHORT ).show();
      SymbolsPalette missingSymbols = new SymbolsPalette(); 
      //
      // missingSymbols = palette of missing symbols
      // if there are missing symbols mAllSymbols is false and the MissingDialog is shown
      //    (the dialog just warns the user about missing symbols, maybe a Toast would be enough)
      // when the sketch is saved, mAllSymbols is checked ( see doSaveTh2 )
      // if there are not all symbols the user is asked if he/she wants to save anyways
      //
      mAllSymbols = mDrawingSurface.loadTherion( filename1, filename2, missingSymbols );

      if ( ! mAllSymbols ) {
        String msg = missingSymbols.getMessage( getResources() );
        TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "Missing " + msg );
        Toast.makeText( this, "Missing symbols \n" + msg, Toast.LENGTH_LONG ).show();
        // (new MissingDialog( this, this, msg )).show();
        // finish();
      }

      // resetZoom();
      resetReference( mPlot1 );

      if ( type == PlotInfo.PLOT_EXTENDED ) {
        switchPlotType();
      }
   }

   private void saveReference( PlotInfo plot, long pid )
   {
     // Log.v("DistoX", "save ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     plot.xoffset = mOffset.x;
     plot.yoffset = mOffset.y;
     plot.zoom    = mZoom;
     mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
   }

   private void resetReference( PlotInfo plot )
   {
     mOffset.x = plot.xoffset; 
     mOffset.y = plot.yoffset; 
     mZoom     = plot.zoom;    
     // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
     // mDrawingSurface.refresh();
   }

    // private void setCurrentPaint()
    // {
    //   mCurrentPaint = new Paint();
    //   mCurrentPaint.setDither(true);
    //   mCurrentPaint.setColor(0xFFFFFF00);
    //   mCurrentPaint.setStyle(Paint.Style.STROKE);
    //   mCurrentPaint.setStrokeJoin(Paint.Join.ROUND);
    //   mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);
    //   mCurrentPaint.setStrokeWidth( STROKE_WIDTH_CURRENT );
    // }

    private Paint getPreviewPaint()
    {
      final Paint previewPaint = new Paint();
      previewPaint.setColor(0xFFC1C1C1);
      previewPaint.setStyle(Paint.Style.STROKE);
      previewPaint.setStrokeJoin(Paint.Join.ROUND);
      previewPaint.setStrokeCap(Paint.Cap.ROUND);
      previewPaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
      return previewPaint;
    }

    private void doSelectAt( float x_scene, float y_scene )
    {
      if ( mMode == MODE_EDIT ) {
        // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mCloseness / mZoom;
        SelectionSet selection = mDrawingSurface.getItemsAt( x_scene, y_scene, mZoom );
        // Log.v( TopoDroidApp.TAG, "selection at " + x_scene + " " + y_scene + " items " + selection.size() );
        // Log.v( TopoDroidApp.TAG, " zoom " + mZoom + " radius " + d0 );
        if ( selection.mPoints.size() > 0 ) {
          mMode = MODE_SHIFT;
          setButton3( selection.mHotItem.type() );
        }
      }
    }

    private void doEraseAt( float x_scene, float y_scene )
    {
      int ret = mDrawingSurface.eraseAt( x_scene, y_scene, mZoom );
      // if ( ret > 0 ) {
      //   Log.v( TopoDroidApp.TAG, "erase at " + x_scene + " " + y_scene + " = " + ret );
      // }
    }

    void updateBlockName( DistoXDBlock block, String from, String to )
    {
      if ( mFullName2 == null ) return; // nothing for PLOT_SECTION or PLOT_H_SECTION
      if ( ( ( block.mFrom == null && from == null ) || block.mFrom.equals(from) ) && 
           ( ( block.mTo == null && to == null ) || block.mTo.equals(to) ) ) return;

      block.mFrom = from;
      block.mTo   = to;
      mData.updateShotName( block.mId, mSid, from, to, true );
      // float x = mOffset.x; 
      // float y = mOffset.y; 
      // float z = mZoom;    
      // mOffset.x = 0.0f;
      // mOffset.y = 0.0f;
      // mZoom = mApp.mScaleFactor;    // canvas zoom
      mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
      computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
      computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      // mOffset.x = x; 
      // mOffset.y = y; 
      // mZoom = z;    
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
    }
 
    // void updateBlockComment( DistoXDBlock block, String comment ) 
    // {
    //   mBlock.mComment = comment;
    //   mData.updateBlockComment( block.mId, mSid, comment );
    // }
    
    void updateBlockFlag( DistoXDBlock block, long flag )
    {
      if ( block.mFlag == flag ) return;
      block.mFlag = flag;
      mData.updateShotFlag( block.mId, mSid, flag, true );
    }

    // called only be DrawingShotDialog
    void updateBlockExtend( DistoXDBlock block, long extend )
    {
      if ( block.mExtend == extend ) return;
      block.mExtend = extend;
      mData.updateShotExtend( block.mId, mSid, extend, true );
      if ( mType == PlotInfo.PLOT_EXTENDED ) {
        // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "updateBlockExtend off " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
        // float x = mOffset.x; 
        // float y = mOffset.y; 
        // float z = mZoom;    
        // mOffset.x = 0.0f;
        // mOffset.y = 0.0f;
        // mZoom = mApp.mScaleFactor;    // canvas zoom
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
        // mOffset.x = x; 
        // mOffset.y = y; 
        // mZoom = z;    
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
    }

    void deletePoint( DrawingPointPath point ) { mDrawingSurface.deletePath( point ); }

    void splitLine( DrawingLinePath line, LinePoint point ) { mDrawingSurface.splitLine( line, point ); }

    boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
    { return mDrawingSurface.removeLinePoint(line, point, sp); }

    void deleteLine( DrawingLinePath line ) { mDrawingSurface.deletePath( line ); }

    void sharpenLine( DrawingLinePath line, boolean reduce ) { mDrawingSurface.sharpenLine( line, reduce ); }

    void deleteArea( DrawingAreaPath area ) { mDrawingSurface.deletePath( area ); }

    // void refreshSurface()
    // {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "refresh surface");
    //   mDrawingSurface.refresh();
    // }

    
    private void dumpEvent( WrapMotionEvent ev )
    {
      String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = ev.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append( "Event action_").append( name[actionCode] );
      if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
        sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
      }
      sb.append( " [" );
      for (int i=0; i<ev.getPointerCount(); ++i ) {
        sb.append( "#" ).append( i );
        sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
        if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
      }
      sb.append( "]" );
      // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, sb.toString() );
    }
    

    float spacing( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return FloatMath.sqrt(x*x + y*y);
    }

    void saveEventPoint( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        mSave0X = ev.getX(0);
        mSave0Y = ev.getY(0);
        if ( np >= 2 ) {
          mSave1X = ev.getX(1);
          mSave1Y = ev.getY(1);
        } else {
          mSave1X = mSave0X;
          mSave1Y = mSave0Y;
        } 
      }
    }

    
    void shiftByEvent( WrapMotionEvent ev )
    {
      float x0 = 0.0f;
      float y0 = 0.0f;
      float x1 = 0.0f;
      float y1 = 0.0f;
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        x0 = ev.getX(0);
        y0 = ev.getY(0);
        if ( np >= 2 ) {
          x1 = ev.getX(1);
          y1 = ev.getY(1);
        } else {
          x1 = x0;
          y1 = y0;
        } 
      }
      float x_shift = ( x0 - mSave0X + x1 - mSave1X ) / 2;
      float y_shift = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
      mSave0X = x0;
      mSave0Y = y0;
      mSave1X = x1;
      mSave1Y = y1;
    
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }
    }

    private void moveCanvas( float x_shift, float y_shift )
    {
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
    }


    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      dismissPopup();

      float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mCloseness / mZoom;

      WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingActivity onTouch() " );
      // dumpEvent( event );

      float x_canvas = event.getX();
      float y_canvas = event.getY();
      if ( mIsNotMultitouch && y_canvas > CENTER_Y*2-20 ) {
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      }
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;
      // Log.v("DistoX", "onTouch " + x_scene + " " + y_scene );

      int action = event.getAction() & MotionEvent.ACTION_MASK;

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        mTouchMode = MODE_MOVE;
        /* nothing */

      // ---------------------------------------- DOWN

      } else if (action == MotionEvent.ACTION_DOWN) {

        if ( mMode == MODE_DRAW ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
          mPointCnt = 0;
          if ( mSymbol == SYMBOL_LINE ) {
            mCurrentLinePath = new DrawingLinePath( mCurrentLine );
            mCurrentLinePath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else if ( mSymbol == SYMBOL_AREA ) {
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), true );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            // mSaveX = x_canvas; // FIXME-000
            // mSaveY = y_canvas;
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
        } else if ( mMode == MODE_EDIT ) {
          mStartX = x_canvas;
          mStartY = y_canvas;
          mEditMove = true;
          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            mEditMove = ( pt.distance( x_scene, y_scene ) < d0 );
          } 
          // doSelectAt( x_scene, y_scene );
          mSaveX = x_canvas;
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_SHIFT ) {
          mShiftMove = true; // whether to move canvas in point-shift mode
          mStartX = x_canvas;
          mStartY = y_canvas;

          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            if ( pt.distance( x_scene, y_scene ) < d0 ) {
              mShiftMove = false;
              mStartX = x_scene;  // save start position
              mStartY = y_scene;
            }
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_ERASE ) {
          doEraseAt(  x_scene, y_scene );
        } else if ( mMode == MODE_MOVE ) {
          setTheTitle( );
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          return false;
        }

      // ---------------------------------------- MOVE

      } else if ( action == MotionEvent.ACTION_MOVE ) {
        // Log.v(  TopoDroidApp.TAG, "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
        if ( mTouchMode == MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          boolean save = true; // FIXME-000
          // mSaveX = x_canvas; 
          // mSaveY = y_canvas;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE ) {
              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > mApp.mLineSegment ) {
                if ( ++mPointCnt % mApp.mLineType == 0 ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            } else if ( mSymbol == SYMBOL_AREA ) {
              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > mApp.mLineSegment ) {
                if ( ++mPointCnt % mApp.mLineType == 0 ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            }
          } else if (  mMode == MODE_MOVE 
                   || (mMode == MODE_EDIT && mEditMove ) 
                   || (mMode == MODE_SHIFT && mShiftMove) ) {
            moveCanvas( x_shift, y_shift );
          } else if ( mMode == MODE_SHIFT ) {
            mDrawingSurface.shiftHotItem( x_scene - mStartX, y_scene - mStartY );
            mStartX = x_scene;
            mStartY = y_scene;
          } else if ( mMode == MODE_ERASE ) {
            doEraseAt( x_scene, y_scene );
          }
          if ( save ) { // FIXME-000
            mSaveX = x_canvas; 
            mSaveY = y_canvas;
          }
        } else { // mTouchMode == MODE_ZOOM
          float newDist = spacing( event );
          if ( newDist > 16.0f && oldDist > 16.0f ) {
            float factor = newDist/oldDist;
            if ( factor > 0.05f && factor < 4.0f ) {
              changeZoom( factor );
              oldDist = newDist;
            }
          }
          if ( mMode == MODE_MOVE && mShiftDrawing ) {
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
            if ( TopoDroidApp.mLevelOverNormal ) {
              if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
                mDrawingSurface.shiftDrawing( x_shift/mZoom, y_shift/mZoom );
              }
            // } else {
            //   moveCanvas( x_shift, y_shift );
            }
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          } else {
            shiftByEvent( event );
          }
        }

      // ---------------------------------------- UP

      } else if (action == MotionEvent.ACTION_UP) {
        if ( onMenu ) {
          closeMenu();
          return true;
        }

        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        } else {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE || mSymbol == SYMBOL_AREA ) {
              // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_UP line style " + mApp.mLineStyle );
              // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, 
              //   "  path size " + ((mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size()) );

              mCurrentBrush.mouseUp( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              mDrawingSurface.previewPath.mPath = new Path();

              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > mApp.mLineSegment || (mPointCnt % mApp.mLineType) > 0 ) {
                if ( mSymbol == SYMBOL_LINE ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                } else if ( mSymbol == SYMBOL_AREA ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
              }
              if ( mPointCnt > mApp.mLineType ) {
                // Log.v( TopoDroidApp.TAG, "Line type " + mCurrentLinePath.mLineType );
                if ( ! ( mSymbol == SYMBOL_LINE && mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) 
                     &&  ( mApp.mLineStyle == mApp.LINE_STYLE_BEZIER ) ) {
                  int nPts = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size() ;
                  if ( nPts > 1 ) {
                    ArrayList< BezierPoint > pts = new ArrayList< BezierPoint >(); // [ nPts ];
                    // ArrayList< LinePoint > lp = 
                    //   (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mPoints : mCurrentAreaPath.mPoints ;
                    // for (int k=0; k<nPts; ++k ) {
                    //   pts.add( new BezierPoint( lp.get(k).mX, lp.get(k).mY ) );
                    // }
                    LinePoint lp = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                    for ( ; lp != null; lp = lp.mNext ) {
                      pts.add( new BezierPoint( lp.mX, lp.mY ) );
                    }

                    mBezierInterpolator.fitCurve( pts, nPts, mApp.mLineAccuracy, mApp.mLineCorner );
                    ArrayList< BezierCurve > curves = mBezierInterpolator.getCurves();
                    int k0 = curves.size();
                    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, " Bezier size " + k0 );
                    if ( k0 > 0 ) {
                      BezierCurve c = curves.get(0);
                      BezierPoint p0 = c.getPoint(0);
                      if ( mSymbol == SYMBOL_LINE ) {
                        DrawingLinePath bezier_path = new DrawingLinePath( mCurrentLine );
                        bezier_path.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          bezier_path.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        mDrawingSurface.addDrawingPath( bezier_path );
                      } else { //  mSymbol == SYMBOL_AREA
                        DrawingAreaPath bezier_path = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), true ); 
                        bezier_path.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          bezier_path.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        bezier_path.close();
                        mDrawingSurface.addDrawingPath( bezier_path );
                      }
                    }
                  }
                } else {
                  if ( mSymbol == SYMBOL_LINE ) {
                    // N.B.
                    // section direction is in the direction of the tick
                    // and splay reference are taken from the station the section looks towards
                    //
                    if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                      mCurrentLinePath.makeStraight( true ); // true = with arrow
                      // TODO get shot
                      // LinePoint l1 = mCurrentLinePath.mPoints.get(0);
                      // LinePoint l2 = mCurrentLinePath.mPoints.get(1);
                      LinePoint l1 = mCurrentLinePath.mFirst;
                      LinePoint l2 = l1.mNext;
                      List< DrawingPath > paths = mDrawingSurface.getIntersectionShot( l1, l2 );
                      if ( paths.size() > 0 ) {
                        mDrawingSurface.addDrawingPath( mCurrentLinePath );

                        String from = "-1";
                        String to   = "-1";
                        boolean vert = true;
                        float azimuth = 90.0f + (float)(Math.atan2( l2.mX - l1.mX, - l2.mY + l1.mY ) * TopoDroidUtil.RAD2GRAD );
                        if ( azimuth >= 360.0f ) azimuth -= 360;

                        DistoXDBlock blk = null;
                        if ( paths.size() > 1 ) {
                          Toast.makeText( this, R.string.too_many_leg_intersection, Toast.LENGTH_SHORT ).show();
                        } else {
                          blk = paths.get(0).mBlock;
                        }

                        if ( blk != null ) {
                          if ( mType == PlotInfo.PLOT_EXTENDED ) vert = ( Math.abs( blk.mClino ) < TopoDroidApp.mHThreshold );
                          from = blk.mFrom;
                          to   = blk.mTo;
                          if ( mType == PlotInfo.PLOT_EXTENDED ) {
                            if ( vert ) azimuth = blk.mBearing;
                          }
                          if ( Math.abs( azimuth - blk.mBearing ) < 90 ) {
                            from = blk.mTo;
                            to   = blk.mFrom;
                            azimuth += 180;
                            if ( azimuth >= 360 ) azimuth -= 360;
                          }
                        } else { // null block
                          azimuth = 90.0f + (float)(Math.atan2( l2.mX - l1.mX, - l2.mY + l1.mY ) * TopoDroidUtil.RAD2GRAD );
                          if ( azimuth >= 360.0f ) azimuth -= 360;
                        }
                        new DrawingLineSectionDialog( this, mApp, mCurrentLinePath, from, to, vert, azimuth ).show();
                      } else { // empty path list
                        Toast.makeText( this, R.string.no_leg_intersection, Toast.LENGTH_SHORT ).show(); 
                      }
                    } else {
                      mDrawingSurface.addDrawingPath( mCurrentLinePath );
                    }
                  } else { //  mSymbol == SYMBOL_AREA
                    mCurrentAreaPath.close();
                    mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                  }
                }
                // undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                // canRedo = false;
              }
              // if ( mSymbol == SYMBOL_LINE ) {
              //   // Log.v( TopoDroidApp.TAG, "line type " + mCurrentLinePath.mLineType );
              //   if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
              //     // keep only first and last point
              //     // remove line points are put the new ones: FIXME delete and add it again
              //     mDrawingSurface.addDrawingPath( mCurrentLinePath );
              //   }
              // }
            } else { // SYMBOL_POINT
              if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
                if ( DrawingBrushPaths.mPointLib.pointHasText(mCurrentPoint) ) {
                  DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                  label.show();
                } else {
                  mDrawingSurface.addDrawingPath( 
                    new DrawingPointPath( mCurrentPoint, x_scene, y_scene, DrawingPointPath.SCALE_M, null ) );

                  // undoBtn.setEnabled(true);
                  // redoBtn.setEnabled(false);
                  // canRedo = false;
                }
              }
            }
          } else if ( mMode == MODE_EDIT ) {
            if ( Math.abs(mStartX - x_canvas) < 10 && Math.abs(mStartY - y_canvas) < 10 ) {
              doSelectAt( x_scene, y_scene );
            }
            mEditMove = false;
          } else if ( mMode == MODE_SHIFT ) {
            if ( mShiftMove ) {
              if ( Math.abs(mStartX - x_canvas) < 10 && Math.abs(mStartY - y_canvas) < 10 ) {
                // mEditMove = false;
                mMode = MODE_EDIT;
                mDrawingSurface.clearSelected();
                setButton3( -1 );
              }
            }
            mShiftMove = false;
          } else { // MODE_MOVE 
            //   return false; // long click
            // }
            /* nothing */
          }
        }
      }
      return true;
    }



    // add a therion label point (ILabelAdder)
    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, DrawingPointPath.SCALE_M, null );
        mDrawingSurface.addDrawingPath( label_path );
      } 
    }

    void setCurrentStationName( String name ) { mApp.setCurrentStationName( name ); }

    public void toggleStationBarrier( String name, boolean is_barrier ) 
    {
      String view = mPlot1.view;
      String new_view = "";
      boolean add = false;
      boolean drop = false;
      if ( view == null ) {
        add = true;
        drop = false;
      } else {
        String[] barrier = view.split( " " );
        int k = 0;
        for (; k < barrier.length; ++k ) {
          if ( barrier[k].equals( name ) ) {
            drop = true;
          } else {
            new_view = new_view + " " + barrier[k];
          }
        }
        new_view.trim();
        add = ! drop;
      }
      if ( add && ! is_barrier ) {
        if ( view == null || view.length() == 0 ) {
          view = name;
        } else {
          view = view + " " + name;
        }
        // Log.v( TopoDroidApp.TAG, "addStationBarrier " + name + " view <" + view + ">" );
        mData.updatePlotView( mPid1, mSid, view );
        mData.updatePlotView( mPid2, mSid, view );
        mPlot1.view = view;
        mPlot2.view = view;
        // FIXME recompute num
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      } else if ( drop && is_barrier ) {
        mData.updatePlotView( mPid1, mSid, new_view );
        mData.updatePlotView( mPid2, mSid, new_view );
        mPlot1.view = new_view;
        mPlot2.view = new_view;
        // FIXME recompute num
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      }
    }
   
    // add a therion station point
    public void addStationPoint( DrawingStationName st )
    {
      mDrawingSurface.addDrawingPath( new DrawingStationPath( st, DrawingPointPath.SCALE_M ) );
    }

    void doDelete()
    {
      mData.deletePlot( mPid1, mSid );
      mData.deletePlot( mPid2, mSid );
      finish();
    }

    private void askDelete()
    {
      new TopoDroidAlertDialog( this, getResources(),
                        getResources().getString( R.string.plot_delete ),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doDelete();
          }
        }
      );
    }

    private Button makeButton( String text )
    {
      Button myTextView = new Button( this );
      myTextView.setHeight( 42 );

      myTextView.setText( text );
      myTextView.setTextColor( 0xffffffff );
      myTextView.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 16 );
      myTextView.setBackgroundColor( 0xff333333 );
      myTextView.setSingleLine( true );
      myTextView.setGravity( 0x03 ); // left
      myTextView.setPadding( 4, 4, 4, 4 );
      // Log.v(TopoDroidApp.TAG, "makeButton " + text );
      return myTextView;
    }

    public void onBackPressed()
    {
      if ( popup_window != null ) {
        dismissPopup();
        return;
      } 
      // finish();
      super.onBackPressed();
    }

    /** line/area editing
     * @param b button
     */
    private void makePopup( View b )
    {
        final Context context = this;
        popup_layout = new LinearLayout(this);
        popup_layout.setOrientation(LinearLayout.VERTICAL);
        int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
        int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

        // ----- MOVE POINT TO THE NEAREST CLOSE POINT
        //
        String text = getString(R.string.popup_join_pt);
        int len = text.length();
        Button myTextView0 = makeButton( text );
        popup_layout.addView(myTextView0, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView0.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_POINT ||
                 mHotItemType == DrawingPath.DRAWING_PATH_LINE ||
                 mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // move to nearest point POINT/LINE/AREA
              if ( ! mDrawingSurface.moveHotItemToNearestPoint() ) {
                Toast.makeText( context, R.string.failed_snap_to_point, Toast.LENGTH_SHORT ).show();
              }
            }
            dismissPopup();
          }
        } );
  
        // ----- SNAP AREA BORDER TO CLOSE LINE
        //
        text = getString(R.string.popup_snap_ln);
        if ( len < text.length() ) len = text.length();
        Button myTextView1 = makeButton( text );
        popup_layout.addView(myTextView1, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView1.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // snap to nearest line
              switch ( mDrawingSurface.snapHotItemToNearestLine() ) {
                case 1:  // single point copy
                case 0:  // normal
                case -1: // no hot point
                case -2: // not snapping area border
                  break;
                case -3: // no line close enough
                  Toast.makeText( context, R.string.failed_snap_to_line, Toast.LENGTH_SHORT ).show();
                  break;
                default:
                  break;
              }
            }
            dismissPopup();
          }
        } );
  
        // ----- SPLIT LINE/AREA POINT IN TWO
        //
        text = getString(R.string.popup_split_pt);
        if ( len > text.length() ) len = text.length();
        Button myTextView2 = makeButton( text );
        popup_layout.addView(myTextView2, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView2.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // split point LINE/AREA
              mDrawingSurface.splitHotItem();
            }
            dismissPopup();
          }
        } );

        // ----- CUT LINE AT SELECTED POINT AND SPLIT IT IN TWO LINES
        //
        text = getString(R.string.popup_split_ln);
        if ( len < text.length() ) len = text.length();
        Button myTextView3 = makeButton( text );
        popup_layout.addView(myTextView3, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView3.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) { // split-line LINE
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_LINE ) {
                splitLine( (DrawingLinePath)(sp.mItem), sp.mPoint );
              }
            }
            dismissPopup();
          }
        } );

        // ----- MAKE LINE SEGMENT STRAIGHT
        //
        text = getString(R.string.popup_sharp_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView4 = makeButton( text );
        popup_layout.addView(myTextView4, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView4.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment straight LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                sp.mPoint.has_cp = false;
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                line.retracePath();
                // mDrawingSurface.refresh();
              }
            }
            dismissPopup();
          }
        } );

        // ----- MAKE LINE SEGMENT SMOOTH (CURVED, WITH CONTROL POINTS)
        //
        text = getString(R.string.popup_curve_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView5 = makeButton( text );
        popup_layout.addView(myTextView5, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView5.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment curved LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                LinePoint lp0 = sp.mPoint;
                LinePoint lp2 = lp0.mPrev; 
                if ( ! lp0.has_cp && lp2 != null ) {
                  float dx = (lp0.mX - lp2.mX)/3;
                  float dy = (lp0.mY - lp2.mY)/3;
                  if ( Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01 ) {
                    lp0.mX1 = lp2.mX + dx;
                    lp0.mY1 = lp2.mY + dy;
                    lp0.mX2 = lp0.mX - dx;
                    lp0.mY2 = lp0.mY - dy;
                    lp0.has_cp = true;
                    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                    line.retracePath();
                  }
                }
                // mDrawingSurface.refresh();
              }
            }
            dismissPopup();
          }
        } );

        // ----- REMOVE LINE/AREA POINT
        //
        text = getString(R.string.popup_remove_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView6 = makeButton( text );
        popup_layout.addView(myTextView6, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView6.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // remove pt
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                // if ( line.mPoints.size() > 2 ) 
                if ( line.size() > 2 ) 
                {
                  removeLinePoint( line, sp.mPoint, sp );
                  line.retracePath();
                }
              }
            }
            dismissPopup();
          }
        } );

        FontMetrics fm = myTextView0.getPaint().getFontMetrics();
        int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
        int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.30);
        myTextView0.setWidth( w );
        myTextView1.setWidth( w );
        myTextView2.setWidth( w );
        myTextView3.setWidth( w );
        myTextView4.setWidth( w );
        myTextView5.setWidth( w );
        myTextView6.setWidth( w );
        // Log.v( TopoDroidApp.TAG, "popup width " + w );
        popup_window = new PopupWindow( popup_layout, w, h ); // popup_layout.getHeight(), popup_layout.getWidth() );
        popup_window.showAsDropDown(b); 
    }

    private void dismissPopup()
    {
      if ( popup_window != null ) {
        popup_window.dismiss();
        popup_window = null;
      }
    }

    private void switchPlotType()
    {
      if ( mType == PlotInfo.PLOT_PLAN ) {
        saveReference( mPlot1, mPid1 );
        mPid  = mPid2;
        mName = mName2;
        mType = (int)PlotInfo.PLOT_EXTENDED;
        mButton1[ PLOT_BUTTON ].setBackgroundResource( icons00[17] );
        mDrawingSurface.setManager( mType );
        resetReference( mPlot2 );
      } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
        saveReference( mPlot2, mPid2 );
        mPid  = mPid1;
        mName = mName1;
        mType = (int)PlotInfo.PLOT_PLAN;
        mButton1[ PLOT_BUTTON ].setBackgroundResource(  icons00[6] );
        mDrawingSurface.setManager( mType );
        resetReference( mPlot1 );
      }
    }
  
    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingActivity onClick() " + view.toString() );
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingActivity onClick() point " + mCurrentPoint + " symbol " + mSymbol );
      dismissPopup();

      Button b = (Button)view;
      if ( b == mImage ) {
        if ( mMenu.getVisibility() == View.VISIBLE ) {
          mMenu.setVisibility( View.GONE );
          onMenu = false;
        } else {
          mMenu.setVisibility( View.VISIBLE );
          onMenu = true;
        }
        return;
      }
      if ( ( b == mButton2[0] && mMode == MODE_DRAW ) || 
           ( b == mButton5[1] && mMode == MODE_ERASE ) || 
           ( b == mButton3[2] && ( mMode == MODE_EDIT || mMode == MODE_SHIFT ) ) ) { 
        mMode = MODE_MOVE;
        setTheTitle();
        mDrawingSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView1.mAdapter );
        mListView.invalidate();
      } else if ( b == mButton1[0] || b == mButton3[0] || b == mButton5[0] ) { // 0 --> DRAW
        mMode = MODE_DRAW;
        setTheTitle();
        mDrawingSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView2.mAdapter );
        mListView.invalidate();
      } else if ( b == mButton1[1] || b == mButton2[1] || b == mButton3[1] ) { // 1--> ERASE
        mMode = MODE_ERASE;
        setTheTitle();
        mDrawingSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView5.mAdapter );
        mListView.invalidate();
      } else if ( b == mButton1[2] || b == mButton2[2] || b == mButton5[2] ) { // 2 --> EDIT
        if ( TopoDroidApp.mLevelOverBasic ) {
          mMode = MODE_EDIT;
          setTheTitle();
          mDrawingSurface.setDisplayPoints( true );
          mListView.setAdapter( mButtonView3.mAdapter );
          mListView.invalidate();
        }
      } else if ( b == mButton1[3] ) { // display mode 
        new DrawingModeDialog( this, this, mDrawingSurface ).show();
      } else if ( b == mButton1[4] ) { // download
        resetFixedPaint();
        if ( mType == (int)PlotInfo.PLOT_PLAN ) {
          saveReference( mPlot1, mPid1 );
        } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
          saveReference( mPlot2, mPid2 );
        }
        mDataDownloader.downloadData();
      } else if ( b == mButton1[5] ) { //  noteBtn
        (new DistoXAnnotations( this, mData.getSurveyFromId(mSid) )).show();
      } else if ( b == mButton1[6] ) { // toggle
        if ( mType != PlotInfo.PLOT_SECTION && mType != PlotInfo.PLOT_H_SECTION ) {
          immediateSaveTh2( ); 
          // mDrawingSurface.clearDrawing();
          switchPlotType();
        }
      } else if ( b == mButton1[7] ) { // more --> info
        // mListView.setAdapter( mButtonView4.mAdapter );
        // mListView.invalidate();
        if ( mNum != null ) {
          new DistoXStatDialog( mDrawingSurface.getContext(), mNum ).show();
        }

      } else if ( b == mButton2[3] ) { // undoBtn
        mDrawingSurface.undo();
        if ( mDrawingSurface.hasMoreUndo() == false ) {
          // undoBtn.setEnabled( false );
        }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
      } else if ( b == mButton2[4] ) { // redoBtn
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
        }
      } else if ( b == mButton2[5] ) { // pointBtn
        new ItemPickerDialog(this, this, mType ).show();

      } else if ( b == mButton3[3] ) { // prev
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.prevHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[4] ) { // next
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.nextHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[5] ) { // item/point editing: move, split, remove, etc.
        // Log.v( TopoDroidApp.TAG, "Button3[5] inLinePoint " + inLinePoint );
        if ( inLinePoint ) {
          makePopup( b );
        } else {
          // SelectionPoint sp = mDrawingSurface.hotItem();
          // if ( sp != null && sp.mItem.mType == DrawingPath.DRAWING_PATH_NAME ) {
          //   DrawingStationName sn = (DrawingStationName)(sp.mItem);
          //   new DrawingBarrierDialog( this, this, sn.mName, mNum.isBarrier( sn.mName ) ).show();
          // }
        }
      } else if ( b == mButton3[6] ) { // edit item properties
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          switch ( sp.type() ) {
            case DrawingPath.DRAWING_PATH_NAME:
              DrawingStationName sn = (DrawingStationName)(sp.mItem);
              new DrawingStationDialog( this, this, sn, mNum.isBarrier( sn.mName ) ).show();
              break;
            case DrawingPath.DRAWING_PATH_POINT:
              new DrawingPointDialog( this, (DrawingPointPath)(sp.mItem) ).show();
              break;
            case DrawingPath.DRAWING_PATH_LINE:
              DrawingLinePath line = (DrawingLinePath)(sp.mItem);
              if ( line.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                new DrawingLineSectionDialog( this, mApp, line, null, null, true, 0.0f ).show();
              } else {
                new DrawingLineDialog( this, line, sp.mPoint ).show();
              }
              break;
            case DrawingPath.DRAWING_PATH_AREA:
              new DrawingAreaDialog( this, (DrawingAreaPath)(sp.mItem) ).show();
              break;
            case DrawingPath.DRAWING_PATH_FIXED:
            case DrawingPath.DRAWING_PATH_SPLAY:
              new DrawingShotDialog( this, this, (DrawingPath)(sp.mItem) ).show();
              break;
          }
        }
        mDrawingSurface.clearSelected();
        mMode = MODE_EDIT;

      // } else if ( b == mButtonHelp ) { // help
      //   (new HelpDialog(this, icons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
      }

    }



    void makeSectionPhoto( DrawingLinePath line, String id )
    {
      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      // imageFile := PHOTO_DIR / surveyId / photoId .jpg
      File imagefile = new File( mApp.getSurveyJpgFile( id ) );
      // Log.v( TopoDroidApp.TAG, "section " + mSectionName + " file " + imagefile.toString() );
      try {
        Uri outfileuri = Uri.fromFile( imagefile );
        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
        intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
        startActivityForResult( intent, ShotActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, R.string.no_capture_app, Toast.LENGTH_SHORT ).show();
      }
    }

    void makeSectionDraw( DrawingLinePath line, String id, String from, String to, boolean vert, float azimuth )
    {
      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      // Log.v( TopoDroidApp.TAG, "makeSectionDraw name " + id );
      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );
      long type = vert? PlotInfo.PLOT_SECTION : PlotInfo.PLOT_H_SECTION;
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "make section draw: azimuth " + azimuth + " vert " + vert );
      if ( pid < 0 ) { 
        // pid = mApp.mData.insertPlot( mApp.mSID, -1L, mSectionName, type, 0L,
        //                              from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, false ); // forward or not ?
        pid = mApp.insert2dSection( mApp.mSID, mSectionName, type, from, to, azimuth );
        TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "insert plot " + mSectionName + " from " + from + " azimuth " + azimuth + " vert " + vert  );
      }
      // Log.v( TopoDroidApp.TAG, "makeSectionDraw pid " + pid );
      if ( pid >= 0 ) {
        Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
        drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, mApp.mSID );
        drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME, mSectionName );
        drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, type );
        drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FROM, from );
        drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_AZIMUTH, (long)azimuth );
        startActivity( drawIntent );
      }
    }

    @Override
    protected void onActivityResult( int reqCode, int resCode, Intent data )
    {
      switch ( reqCode ) {
        case ShotActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
          if ( resCode == Activity.RESULT_OK ) {
            // TODO store a photo - section in the plots ?

            // long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );
            // if ( pid < 0 ) {
            //   Log.v( TopoDroidApp.TAG, "section " + mSectionName + " insert photo plot pid " + pid );
            //   mApp.mData.insertPlot( mApp.mSID, -1L, mSectionName, PlotInfo.PLOT_PHOTO, 0L, "-1", "-1", 0, 0, TopoDroidApp.mScaleFactor );
            // } else {
            //   Log.v( TopoDroidApp.TAG, "section " + mSectionName + " photo plot pid " + pid + " already exists" );
            // }
          }
          break;
      }
    }


    private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> 
    {
        private Context mContext;
        private Handler mHandler;
        private Bitmap mBitmap;
        private String mFullName;

        public ExportBitmapToFile( Context context, Handler handler, Bitmap bitmap, String name )
        {
           mContext  = context;
           mBitmap   = bitmap;
           mHandler  = handler;
           mFullName = name;
           // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "ExportBitmapToFile " + mFullName );
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            String filename = mApp.getPngFileWithExt( mFullName );
            final FileOutputStream out = new FileOutputStream( filename );
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            return true;
          } catch (Exception e) {
            e.printStackTrace();
          }
          //mHandler.post(completeRunnable);
          return false;
        }


        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            mHandler.sendEmptyMessage( bool? 1 : 0 );
        }
    }


    private class ExportToFile extends AsyncTask<Intent,Void,Boolean> 
    {
        private Context mContext;
        private DrawingCommandManager mCommand;
        private DistoXNum mNum;
        private long mType;
        private Handler mHandler;
        private String mFullName;
        private String mExt; // extension

        public ExportToFile( Context context, Handler handler, DrawingCommandManager command,
                                DistoXNum num, long type, String name, String ext )
        {
           mContext  = context;
           mCommand  = command;
           mNum = num;
           mType = type;
           mHandler  = handler;
           mFullName = name;
           mExt = ext;
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            String filename = null;
            if ( mExt.equals("dxf") ) {
              filename = mApp.getDxfFileWithExt( mFullName );
            } else if ( mExt.equals("svg") ) {
              filename = mApp.getSvgFileWithExt( mFullName );
            }
            if ( filename != null ) {
              // final FileOutputStream out = new FileOutputStream( filename );
              final FileWriter fw = new FileWriter( filename );
              BufferedWriter bw = new BufferedWriter( fw );
              if ( mExt.equals("dxf") ) {
                DrawingDxf.write( bw, mNum, mCommand, mType );
              } else if ( mExt.equals("svg") ) {
                DrawingSvg.write( bw, mNum, mCommand, mType );
              }
              fw.flush();
              fw.close();
              return true;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          //mHandler.post(completeRunnable);
          return false;
        }


        @Override
        protected void onPostExecute(Boolean bool) 
        {
          super.onPostExecute(bool);
          mHandler.sendEmptyMessage( bool? 1 : 0 );
        }
    }

    // --------------------------------------------------------

    void savePng()
    {
      if ( mType == PlotInfo.PLOT_SECTION || mType == PlotInfo.PLOT_H_SECTION ) {
        doSavePng( mType, mFullName1 ); // FIXME
      } else {
        doSavePng( (int)PlotInfo.PLOT_PLAN, mFullName1 ); // FIXME
        doSavePng( (int)PlotInfo.PLOT_EXTENDED, mFullName2 );
      }
    }

    void doSavePng( long type, final String filename )
    {
      final Activity currentActivity  = this;
      Handler saveHandler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
             if (msg.what == 1 ) {
               Toast.makeText( currentActivity, 
                     getString(R.string.saved_file_) + " " + filename + ".png", Toast.LENGTH_SHORT ).show();
             } else {
               Toast.makeText( currentActivity, R.string.saving_file_failed, Toast.LENGTH_SHORT ).show();
             }
           }
      } ;
      Bitmap bitmap = mDrawingSurface.getBitmap( type );
      if ( bitmap == null ) {
        Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_SHORT ).show();
      } else {
        new ExportBitmapToFile(this, saveHandler, bitmap, filename ).execute();
      }
    }

    void saveCsx()
    {
      mApp.exportSurveyAsCsx( this, mPlot1.start );
    }

    void saveWithExt( String ext )
    {
      if ( mType == PlotInfo.PLOT_SECTION || mType == PlotInfo.PLOT_H_SECTION ) {
        doSaveWithExt( mType, mFullName1, ext ); // FIXME
      } else {
        doSaveWithExt( PlotInfo.PLOT_PLAN, mFullName1, ext ); // FIXME
        doSaveWithExt( PlotInfo.PLOT_EXTENDED, mFullName2, ext );
      }
    }

    // ext file extension (--> saving class)
    void doSaveWithExt( long type, final String filename, final String ext )
    {
      final Activity currentActivity  = this;
      Handler saveHandler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
             if (msg.what == 1 ) {
               Toast.makeText( currentActivity, 
                 getString(R.string.saved_file_) + " " + filename + "." + ext, Toast.LENGTH_SHORT ).show();
             } else {
               Toast.makeText( currentActivity, 
                 getString(R.string.saving_file_failed), Toast.LENGTH_SHORT ).show();
             }
           }
      } ;
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        new ExportToFile(this, saveHandler, mDrawingSurface.mCommandManager2, mNum, type, filename, ext ).execute();
      } else {
        new ExportToFile(this, saveHandler, mDrawingSurface.mCommandManager1, mNum, type, filename, ext ).execute();
      }
    }

    // called by PlotSaveDialog: save as th2 even if there are missing symbols
    void saveTh2()
    {
      TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "saveTh2 back up " + mFullName1 + " " + mFullName2 );
      String filename1 = mApp.getTh2FileWithExt( mFullName1 ) + ".bck";
      String filename2 = null;
      if ( mFullName2 != null ) {
        filename2 = mApp.getTh2FileWithExt( mFullName2 ) + ".bck";
      }
      doSaveTh2( ! mAllSymbols );
    }

  
  // @Override
  // public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo info )
  // {
  //   super.onCreateContextMenu( menu, v, info );
  //   getMenuInflater().inflate( R.menu.popup, menu );
  //   menu.setHeaderTitle( "Context Menu" );
  //   Log.v( TopoDroidApp.TAG, "onCreateContextMenu view " + v.toString()  );
  // }

  // @Override
  // public boolean onContextItemSelected( MenuItem item )
  // {
  //   switch ( item.getItemId() ) {
  //     // case ...:
  //     //   break;
  //     default:
  //       break;
  //   }
  //   return super.onOptionsItemSelected( item );
  // }

  public void refreshDisplay( int nr, boolean toast )
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        if ( mType == (int)PlotInfo.PLOT_PLAN ) {
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
          computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
          resetReference( mPlot1 );
        } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
          computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
          resetReference( mPlot2 );
        }
      }
      if ( toast ) {
        Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }

  public void updateDisplay( boolean compute )
  {
    // Log.v( TopoDroidApp.TAG, "update display: list " + mList.size() );
    if ( compute ) {
      mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
      computeReferences( (int)mType, 0.0f, 0.0f, mApp.mScaleFactor );
    }
    if ( mType == (int)PlotInfo.PLOT_PLAN ) {
      resetReference( mPlot1 );
    } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
      resetReference( mPlot2 );
    }
  }

  @Override
  public void updateBlockList( DistoXDBlock blk )
  {
    mApp.mShotActivity.updateBlockList( blk );
    mButton1[4].setBackgroundResource( icons00[4] );
    updateDisplay( true );
  }

  // ---------------------------------------------------------
  /* MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsave    = menu.add( R.string.menu_export );
    mMIsave.setIcon(    icons[15] );

    if ( TopoDroidApp.mLevelOverNormal ) {
      mMIrecover   = menu.add( R.string.menu_reload );
      mMIrecover.setIcon( icons[16] );

      mMIdelete  = menu.add( R.string.menu_delete );
      mMIdelete.setIcon(  icons[17] );

      mMIsymbol  = menu.add( R.string.menu_palette );
      mMIsymbol.setIcon(  icons[18] );
    }

    mMIoptions = menu.add( R.string.menu_options );
    mMIoptions.setIcon( icons[19] );

    mMIhelp    = menu.add( R.string.menu_help  );
    mMIhelp.setIcon(    icons[20] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    // if ( item == mMIrefs ) { // REFERENCES DIALOG
    //   new DrawingModeDialog( this, mDrawingSurface ).show();
    if ( item == mMIsymbol ) { // PALETTE
      DrawingBrushPaths.makePaths( getResources() );
      (new SymbolEnableDialog( this, this )).show();
    } else if ( item == mMIsave ) { // SAVE DIALOG
      new PlotSaveDialog( this, this ).show();
    } else if ( item == mMIdelete ) { // DELETE DIALOG
      askDelete();
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
      startActivity( intent );
    } else if ( item == mMIrecover ) { // RECOVER FROM BACKUP
      askRecover();
    } else if ( item == mMIhelp ) { // HELP DIALOG
      int nn = mNrButton1 + mNrButton2 - 3 + mNrButton5 - 3 + ( TopoDroidApp.mLevelOverBasic? mNrButton3 - 3: 0 );
      (new HelpDialog(this, icons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    if ( TopoDroidApp.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        new PlotSaveDialog( this, this ).show();
      } else if ( p++ == pos ) { // RECOVER
        askRecover();
      } else if ( TopoDroidApp.mLevelOverBasic && p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // PALETTE
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this )).show();
      } else if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        int nn = mNrButton1 + mNrButton2 - 3 + mNrButton5 - 3 + ( TopoDroidApp.mLevelOverBasic? mNrButton3 - 3: 0 );
        (new HelpDialog(this, icons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
      }
    }
  }

  private void askRecover()
  {
    new TopoDroidAlertDialog( this, getResources(), getResources().getString( R.string.plot_recover ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doRecover();
        }
      }
    );
  }

  private void doRecover()
  {
    String filename1 = mApp.getTh2FileWithExt( mFullName1 ) + ".bck";
    String filename2 = null;
    if ( mFullName2 != null ) {
      filename2 = mApp.getTh2FileWithExt( mFullName2 ) + ".bck";
    }
    SymbolsPalette missingSymbols = new SymbolsPalette();
    mDrawingSurface.loadTherion( filename1, filename2, missingSymbols );
  }

  void exportAsCsx( PrintWriter pw )
  {
    pw.format("  <plan>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_PLAN );
    pw.format("    <plot />\n");
    pw.format("  </plan>\n");
    pw.format("  <profile>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_EXTENDED );
    pw.format("    <plot />\n");
    pw.format("  </profile>\n");
  }

}
