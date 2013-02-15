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
 */
package com.android.DistoX;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ZoomControls;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.Toast;

import android.util.FloatMath;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;

import android.util.Log;

/**
 */
public class DrawingActivity extends Activity 
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , View.OnLongClickListener
                                      , DrawingPointPickerDialog.OnPointSelectedListener
                                      , OnZoomListener
{
  // static final String TAG = "DistoX";

    private TopoDroidApp app;
    private PlotInfo mPlot;

    // 0: no bezier, plain path
    // 1: bezier interpolator

    public static float CENTER_X = 100f;
    public static float CENTER_Y = 120f;

    private static final float SCALE_FIX = 20.0f; // FIXME

    private BezierInterpolator mBezierInterpolator;
    private DrawingSurface mDrawingSurface;
    private DrawingLinePath mCurrentLinePath;
    private DrawingAreaPath mCurrentAreaPath;
    private DrawingPath mFixedDrawingPath;
    // private Paint mCurrentPaint;
    private int mCurrentPoint;
    private int mCurrentLine;
    private int mCurrentArea;
    private int mType;
    // private boolean canRedo;
    private DistoXNum mNum;
    private int mPointCnt; // counter of points in the currently drawing line

    private boolean mIsNotMultitouch;

    // private Button redoBtn;
    private Button undoBtn;
    // private Button zoomBtn;
    private Button displayBtn;
    private Button pointBtn;
    private Button lineBtn;
    private Button areaBtn;
    private Button modeBtn;

    private DrawingBrush mCurrentBrush;
    private Path  mCurrentPath;

    private String mFullName;
    
    private MenuItem mMIsave;
    // private MenuItem mMIsavePNG;
    // private MenuItem mMIsymbol;
    private MenuItem mMIoptions;
    private MenuItem mMIzoom;
    // private MenuItem mMIhelp;
    private MenuItem mMInotes;
    private MenuItem mMIredo;
    private MenuItem mMIone;
    private MenuItem mMIdisplay;
    private MenuItem mMIstats;
    private MenuItem mMIdelete;
    private SubMenu  mSMmore;

    ZoomButtonsController mZoomBtnsCtrl;
    View mZoomView;
    ZoomControls mZoomCtrl;
    // ZoomButton mZoomOut;
    // ZoomButton mZoomIn;
    private float oldDist;  // zoom pointer-sapcing

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public static final int SYMBOL_POINT = 1;
    public static final int SYMBOL_LINE  = 2;
    public static final int SYMBOL_AREA  = 3;
    public static final int MODE_DRAW  = 1;
    public static final int MODE_MOVE  = 2;
    public static final int MODE_EDIT  = 3;
    public static final int MODE_ZOOM = 4;
    public int mSymbol = SYMBOL_LINE; // default
    public int mMode   = MODE_MOVE;
    private int mTouchMode = MODE_MOVE;
    private float mSaveX;
    private float mSaveY;
    private float mStartX;
    private float mStartY;
    private PointF mOffset  = new PointF( 0f, 0f );
    private PointF mOffset0 = new PointF( 0f, 0f );
    private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );
    private static PointF mDisplayCenter;
    private static float mZoom  = 1.0f;
    private DataHelper mData;
    private long mSid; // survey id
    private long mPid; // plot id

    // private float mLineSegment;
    // private float mLineAcc;
    // private float mLineCorner;

    // public PointF Offset() { return mOffset; }
    // public PointF Center() { return mCenter; }
    // public float  Zoom()   { return mZoom; }

    private boolean mAllSymbols; // whether the library has all the symbols of the plot

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
      mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
      mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
      // mZoomCtrl.hide();
      // mZoomBtnsCtrl.setVisible( false );
    }

    private void resetZoom() 
    {
      int w = mDrawingSurface.width();
      int h = mDrawingSurface.height();
      mOffset.x = w/4;
      mOffset.y = h/4;
      mZoom = app.mScaleFactor;
      // Log.v( TopoDroidApp.TAG, "zoom one " + mZoom + " off " + mOffset.x + " " + mOffset.y );
      if ( mType == TopoDroidApp.PLOT_PLAN ) {
        float zx = w/(mNum.surveyEmax() - mNum.surveyEmin());
        float zy = h/(mNum.surveySmax() - mNum.surveySmin());
        mZoom = (( zx < zy )? zx : zy)/40;
      } else if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
        float zx = w/(mNum.surveyHmax() - mNum.surveyHmin());
        float zy = h/(mNum.surveyVmax() - mNum.surveyVmin());
        mZoom = (( zx < zy )? zx : zy)/40;
      } else {
        mZoom = app.mScaleFactor;
        mOffset.x = 0.0f;
        mOffset.y = 0.0f;
      }
        
      // Log.v( TopoDroidApp.TAG, "zoom one to " + mZoom );
        
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
    }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    public void zoomOne() { resetZoom( ); }

    // public void zoomView( )
    // {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "zoomView ");
    //   DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
    //   zoom.show();
    // }

    public void colorChanged( int color )
    {
      // if ( mCurrentPaint != null ) {
      //   mCurrentPaint.setColor( color );
      // }
    }

    public void areaSelected( int k ) 
    {
      if ( k >= 0 && k < DrawingBrushPaths.mAreaLib.mAreaNr ) {
        mSymbol = SYMBOL_AREA;
        mCurrentArea = k;
      }
      setTheTitle();
    }

    public void lineSelected( int k ) 
    {
      if ( k >= 0 && k < DrawingBrushPaths.mLineLib.mLineNr ) {
        mSymbol = SYMBOL_LINE;
        mCurrentLine = k;
      }
      setTheTitle();
    }

    public void pointSelected( int p )
    {
      if ( p >= 0 && p < DrawingBrushPaths.mPointLib.mPointNr ) {
        mSymbol = SYMBOL_POINT;
        // pointBtn.setText("Point");
        mCurrentPoint = p;
      }
      setTheTitle();
    }

    private float toSceneX( float x )
    {
      return mCenter.x + x * SCALE_FIX;
    }
    private float toSceneY( float y )
    {
      return mCenter.y + y * SCALE_FIX;
    }


    private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, boolean splay, boolean selectable )
    {
      if ( splay ) {
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedSplayPaint );
      } else {
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedShotPaint );
      }
      mFixedDrawingPath.path  = new Path();
      x1 = toSceneX( x1 );
      y1 = toSceneY( y1 );
      x2 = toSceneX( x2 );
      y2 = toSceneY( y2 );
      mFixedDrawingPath.setEndPoints( x1, y1, x2, y2 );
      mFixedDrawingPath.path.moveTo( x1 - mOffset.x, y1 - mOffset.y );
      mFixedDrawingPath.path.lineTo( x2 - mOffset.x, y2 - mOffset.y );
      mDrawingSurface.addFixedPath( mFixedDrawingPath, selectable );
    }

    public void addGrid( float xmin, float xmax, float ymin, float ymax )
    {
      xmin -= 10.0f;
      xmax += 10.0f;
      ymin -= 10.0f;
      ymax += 10.0f;
      float x1 = (float)(toSceneX( xmin ) - mOffset.x);
      float x2 = (float)(toSceneX( xmax ) - mOffset.x);
      float y1 = (float)(toSceneY( ymin ) - mOffset.y);
      float y2 = (float)(toSceneY( ymax ) - mOffset.y);
      mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );

      for ( int x = (int)Math.round(xmin); x < xmax; x += 1 ) {
        float x0 = (float)(toSceneX( x ) - mOffset.x);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.path  = new Path();
        mFixedDrawingPath.path.moveTo( x0, y1 );
        mFixedDrawingPath.path.lineTo( x0, y2 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
      for ( int y = (int)Math.round(ymin); y < ymax; y += 1 ) {
        float y0 = (float)(toSceneY( y ) - mOffset.y);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.path  = new Path();
        mFixedDrawingPath.path.moveTo( x1, y0 );
        mFixedDrawingPath.path.lineTo( x2, y0 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
    }

    private void setTheTitle()
    {
      Resources res = getResources();
      if ( mMode == MODE_DRAW ) { 
        modeBtn.setText( res.getString(R.string.btn_draw ) );
        // modeBtn.setBackgroundResource( R.drawable.note2 );
        modeBtn.setBackgroundColor( 0xff9999ff );
        if ( mSymbol == SYMBOL_POINT ) {
          setTitle( String.format( res.getString(R.string.title_draw_point), 
                                   DrawingBrushPaths.mPointLib.getPointName(mCurrentPoint) ) );
        } else if ( mSymbol == SYMBOL_LINE ) {
          setTitle( String.format( res.getString(R.string.title_draw_line),
                                   DrawingBrushPaths.getLineName(mCurrentLine) ) );
        } else  {  // if ( mSymbol == SYMBOL_LINE ) 
          setTitle( String.format( res.getString(R.string.title_draw_area),
                                   DrawingBrushPaths.mAreaLib.getAreaName(mCurrentArea) ) );
        }
      } else if ( mMode == MODE_MOVE ) {
        modeBtn.setText( res.getString(R.string.btn_move ) );
        // modeBtn.setBackgroundResource( R.drawable.hand2 );
        modeBtn.setBackgroundColor( 0xff999999 );
        setTitle( R.string.title_move );
      } else if ( mMode == MODE_EDIT ) {
        modeBtn.setText( res.getString(R.string.btn_edit ) );
        modeBtn.setBackgroundColor( 0xffff9999 );
        setTitle( R.string.btn_edit );
      }
    }

    private void AlertMissingSymbols()
    {
      AlertDialog.Builder alert = new AlertDialog.Builder( this );
      // alert.setTitle( R.string.delete );
      alert.setMessage( getResources().getString( R.string.missing_symbols ) );
    
      alert.setPositiveButton( R.string.button_ok, 
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mAllSymbols = true;
          }
        } );

      alert.setNegativeButton( R.string.button_cancel, 
        new DialogInterface.OnClickListener() {
          @Override
            public void onClick( DialogInterface dialog, int btn ) { }
        } );
      alert.show();
    }

    private boolean doSaveTh2( boolean not_all_symbols )
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, " savingTh2 ");
      if ( mFullName != null && mDrawingSurface != null ) {
        if ( not_all_symbols ) {
          AlertMissingSymbols();
        }
        if ( mAllSymbols ) {
          Handler saveHandler = new Handler(){
               @Override
               public void handleMessage(Message msg) {
          //         final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
          //         alertDialog.setTitle("Saving sketch");
          //         alertDialog.setMessage("File: " + mFullName );
          //         alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
          //             public void onClick(DialogInterface dialog, int which) {
          //                 return;
          //             }
          //         });
          //         alertDialog.show();
               }
          } ;
          new SaveTherionFile(this, saveHandler, mDrawingSurface, mFullName ).execute();
        }
      }
      return false;
    }

    @Override
    protected synchronized void onPause() 
    { 
      if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
      mDrawingSurface.isDrawing = false;
      mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
      // Toast.makeText( this, R.string.saving_wait, Toast.LENGTH_LONG ).show();
      if ( mAllSymbols ) {
        doSaveTh2( false ); // do not alert-dialog on mAllSymbols: in case do not save 
      // } else {
      //   Toast.makeText( this, R.string.missing_save, Toast.LENGTH_LONG ).show();
      }
      super.onPause();
    }

    // @Override
    // protected synchronized void onStop() 
    // {   
    //   super.onStop();
    // }


  private void computeReferences( List<DistoXDBlock> list, String start )
  {
    if ( mType != TopoDroidApp.PLOT_PLAN && mType != TopoDroidApp.PLOT_EXTENDED ) return;

    mDrawingSurface.clearReferences();
    mNum = new DistoXNum( list, start );

    if ( mType == TopoDroidApp.PLOT_PLAN ) {
      addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax() );
    } else {
      addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax() );
    }

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots = mNum.getShots();
    List< NumSplay > splays = mNum.getSplays();
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "stations " + stations.size() + " legs " + shots.size() );
    // Log.v( TopoDroidApp.TAG, "compute refs. offs " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
    if ( mType == TopoDroidApp.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        addFixedLine( sh.block, (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), false, false );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, 
        //   "add line " + (float)(st1.e) + " " + (float)(st1.s) + " " + (float)(st2.e) + " " + (float)(st2.s) );
      }
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.block, (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), true, false );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addStation( st.name, toSceneX(st.e) - mOffset.x, toSceneY(st.s) - mOffset.y, st.mDuplicate, true );
      }
    } else { // if ( mType == TopoDroidApp.PLOT_EXTENDED && 
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          addFixedLine( sh.block, (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v), false, true );
          // Log.v( TopoDroidApp.TAG, "line " + toSceneX(st1.h) + " " + toSceneY(st1.v) + " - " + toSceneX(st2.h) + " " + toSceneY(st2.v) );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.block, (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), true, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addStation( st.name, toSceneX(st.h) - mOffset.x, toSceneY(st.v) - mOffset.y, st.mDuplicate, true );
      }
    }

    if ( (! mNum.surveyAttached) && app.mCheckAttached ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_LONG ).show();
    }

  }
    


    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

      setContentView(R.layout.drawing_activity);
      app = (TopoDroidApp)getApplication();
      mZoom = app.mScaleFactor;    // canvas zoom

      mDisplayCenter = new PointF(app.mDisplayWidth  / 2, app.mDisplayHeight / 2);

      // setCurrentPaint();
      mCurrentBrush = new DrawingPenBrush();
      mCurrentPoint = 0; // DrawingBrushPaths.POINT_LABEL;
      mCurrentLine  = 0; // DrawingBrushPaths.mLineLib.mLineWallIndex;
      mCurrentArea  = 0; // DrawingBrushPaths.AREA_WATER;

      mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mDrawingSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
      mDrawingSurface.previewPath.path = new Path();
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

      // redoBtn = (Button) findViewById(R.id.redoBtn);
      undoBtn = (Button) findViewById(R.id.undoBtn);
      // zoomBtn = (Button) findViewById(R.id.zoomBtn);
      displayBtn = (Button) findViewById(R.id.displayBtn);
      pointBtn = (Button) findViewById(R.id.pointBtn);
      lineBtn = (Button) findViewById(R.id.lineBtn);
      areaBtn = (Button) findViewById(R.id.areaBtn);
      modeBtn = (Button) findViewById(R.id.modeBtn);

      modeBtn.setOnLongClickListener(this);

      // undoBtn.setAlpha( 0.5f );
      // zoomBtn.setAlpha( 0.5f );
      // modeBtn.setAlpha( 0.5f );
      // lineBtn.setAlpha( 0.5f );
      // areaBtn.setAlpha( 0.5f );
      // pointBtn.setAlpha( 0.5f );

      // redoBtn.setEnabled(false);
      // undoBtn.setEnabled(false); // let undo always be there

      DrawingBrushPaths.makePaths( getResources() );
      setTheTitle();

      mData        = app.mData; // new DataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mSid         = extras.getLong(   app.TOPODROID_SURVEY_ID );
      String name  = extras.getString( app.TOPODROID_PLOT_NAME );
      mFullName    = app.getSurvey() + "-" + name;

      mPlot = app.mData.getPlotInfo( mSid, name );
      mPid         = mPlot.id;
      String start = mPlot.start;
      mType        = mPlot.type;

      mAllSymbols  = true; // by default there are all the symbols

      // mLineSegment = app.mLineSegment;
      // mLineAcc     = app.mLineAccuracy;
      // mLineCorner  = app.mLineCorner;
     
      if ( mType == TopoDroidApp.PLOT_PLAN || mType == TopoDroidApp.PLOT_EXTENDED ) {
        List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT,
        //   "DrawingActivity::onCreate SID " + mSid + " start at " + start + " shots " + list.size() );
        if ( list.size() == 0 ) {
           Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
           finish();
        } else {
          computeReferences( list, start );
        }
      } else { // ( mType == TopoDroidApp.PLOT_V_SECTION || mType == TopoDroidApp.PLOT_H_SECTION ) {
        long extend0 = 1L;
        mNum = null;
        float xmin = 0.0f;
        float xmax = 0.0f;
        float ymin = 0.0f;
        float ymax = 0.0f;
        String viewed  = mPlot.view; // extras.getString( app.TOPODROID_PLOT_VIEW );
        List<DistoXDBlock> list = mData.selectAllShotsAtStation( mSid, start );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "start " + start + " viewed " + viewed + " shots " + list.size() );
        if ( list.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
          finish();
        } else if ( mType == TopoDroidApp.PLOT_H_SECTION ) {
          // find bounds
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing;
            float d = bl.mLength * FloatMath.cos( bl.mClino * app.GRAD2RAD_FACTOR );
            float s = - d * FloatMath.cos( b * app.GRAD2RAD_FACTOR );
            float e =   d * FloatMath.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            } else {
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            }
          }
          addGrid( xmin, xmax, ymin, ymax );

          mDrawingSurface.addStation( start, toSceneX(0.0f) - mOffset.x, toSceneY(0.0f) - mOffset.y, false, false );
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing;
            float d = bl.mLength * FloatMath.cos( bl.mClino * app.GRAD2RAD_FACTOR );
            float s = - d * FloatMath.cos( b * app.GRAD2RAD_FACTOR );
            float e =   d * FloatMath.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              mDrawingSurface.addStation( viewed, toSceneX(e) - mOffset.x, toSceneY(s) - mOffset.y, false, false );
              addFixedLine( bl, 0.0f, 0.0f, e, s, false, false );
            } else {
              addFixedLine( bl, 0.0f, 0.0f, e, s, true, false );
            }
          }
        } else { // ( mType == TopoDroidApp.PLOT_V_SECTION )
          float bearing = 0.0f;
          DistoXDBlock bl0 = null;
          for ( DistoXDBlock bl : list ) {
            String to = bl.mTo;
            if ( to == null ) continue;
            if ( to.equals( viewed ) ) {
              bearing = bl.mBearing;
              extend0 = bl.mExtend;
              // float s = - bl.mLength * FloatMath.sin( bl.mClino * app.GRAD2RAD_FACTOR );
              bl0 = bl;
              break;
            }
          }
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing - bearing;
            float s = - bl.mLength * FloatMath.sin( bl.mClino * app.GRAD2RAD_FACTOR );
            float e = bl.mLength * FloatMath.cos( bl.mClino * app.GRAD2RAD_FACTOR ) 
                                 * FloatMath.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mExtend != extend0 ) {
              e = -e;
            }
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            } else {
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            }
          }
          addGrid( xmin, xmax, ymin, ymax );

          mDrawingSurface.addStation( start, toSceneX(0.0f) - mOffset.x, toSceneY(0.0f) - mOffset.y, false, false );
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing - bearing;
            float s = - bl.mLength * FloatMath.sin( bl.mClino * app.GRAD2RAD_FACTOR );
            float e = bl.mLength * FloatMath.cos( bl.mClino * app.GRAD2RAD_FACTOR ) 
                                 * FloatMath.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mExtend != extend0 ) {
              e = -e;
            }
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              mDrawingSurface.addStation( viewed, toSceneX(e) - mOffset.x, toSceneY(s) - mOffset.y, false, false );
              addFixedLine( bl, 0.0f, 0.0f, e, s, false, false );
            } else {
              addFixedLine( bl, 0.0f, 0.0f, e, s, true, false );
            }
          }
        }
      }
      // now try to load drawings from therion file
      String filename = app.getTh2FileWithExt( mFullName );
      mAllSymbols = mDrawingSurface.loadTherion( filename );
      if ( ! mAllSymbols ) {
        // Toast.makeText( this, "Missing symbols", Toast.LENGTH_LONG ).show();
        
        String prev = "";
        Resources res = getResources();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.format( "%s\n",  res.getString( R.string.missing_warning ) );
        if ( mDrawingSurface.mMissingPoint.size() > 0 ) {
          pw.format( "%s:", res.getString( R.string.missing_point ) );
          for ( String p : mDrawingSurface.mMissingPoint ) {
            if ( ! p.equals(prev) ) pw.format( " %s", p );
          }
          pw.format( "\n");
        }
        if ( mDrawingSurface.mMissingLine.size() > 0 ) {
          pw.format( "%s:", res.getString( R.string.missing_line ) );
          prev = "";
          for ( String p : mDrawingSurface.mMissingLine ) {
            if ( ! p.equals(prev) ) pw.format( " %s", p );
          }
          pw.format( "\n");
        }
        if ( mDrawingSurface.mMissingArea.size() > 0 ) {
          pw.format( "%s:", res.getString( R.string.missing_area ) );
          prev = "";
          for ( String p : mDrawingSurface.mMissingArea ) {
            if ( ! p.equals(prev) ) pw.format( " %s", p );
          }
          pw.format( "\n");
        }
        pw.format( "%s\n",  res.getString( R.string.missing_hint ) );
        (new MissingDialog( this, sw.getBuffer().toString() )).show();
      }
      

      mBezierInterpolator = new BezierInterpolator( );
      // resetZoom();
      mOffset.x = mPlot.xoffset; 
      mOffset.y = mPlot.yoffset; 
      mZoom     = mPlot.zoom;    
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

    public boolean onLongClick( View view )
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingActivity onLongClick() " );

      // if ( Math.abs( mSaveX - mStartX ) < 16 && Math.abs( mSaveY - mStartY ) < 16 ) {

      //   float x_scene = mStartX/mZoom - mOffset.x;
      //   float y_scene = mStartY/mZoom - mOffset.y;
      //   // Log.v( "DistoX", "DrawingActivity onLongClick() " + x_scene + " " + y_scene );

      //   doSelectAt( x_scene, y_scene );
      // }
      if ( mMode == MODE_MOVE && (Button)view == modeBtn ) {
        mMode = MODE_EDIT;
        setTheTitle();
      }
      return true;
    }

    private void doSelectAt( float x_scene, float y_scene )
    {
      // Log.v( "DistoX", "doSelectAt at " + x_scene + " " + y_scene );
      float d0 = TopoDroidApp.mCloseness;
      if ( ! TopoDroidApp.mAutoStations && ( mType == TopoDroidApp.PLOT_PLAN || mType == TopoDroidApp.PLOT_EXTENDED ) ) {
        DrawingStationName station = mDrawingSurface.getStationAt( x_scene, y_scene );
        if ( station != null ) {
          // this check should prevent to insert a station twice
          if ( mDrawingSurface.hasStationName( station.mName ) ) {
            // Log.v("DistoX", "station " + station.mName + " already inserted" );
            // Toast
          } else { // start dialog to set the station
            new DrawingStationDialog( this, this, station ).show();
            return;
          }
        }
      } 

      if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
        //SLE   shot = sp.item;
        DrawingPath shot = mDrawingSurface.getShotAt( x_scene, y_scene );
        if ( shot != null ) {
          new DrawingShotDialog( this, this, shot ).show();
          return;
        }
      }

      DrawingPointPath point = mDrawingSurface.getPointAt( x_scene, y_scene );
      if ( point != null ) {
        new DrawingPointDialog( this, point ).show();
        return;
      } 

      DrawingLinePath line = mDrawingSurface.getLineAt( x_scene, y_scene );
      if ( line != null ) {
        new DrawingLineDialog( this, line ).show();
        return;
      }
      
      DrawingAreaPath area = mDrawingSurface.getAreaAt( x_scene, y_scene );
      if ( area != null ) {
        new DrawingAreaDialog( this, area ).show();
        return;
      }
    }
    
    void updateBlockExtend( DistoXDBlock block, long extend )
    {
      if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
        block.mExtend = extend;
        mData.updateShotExtend( block.mId, mSid, extend );
        // Log.v(TopoDroidApp.TAG, "updateBlockExtend off " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
        float x = mOffset.x; 
        float y = mOffset.y; 
        float z = mZoom;    
        mOffset.x = 0.0f;
        mOffset.y = 0.0f;
        mZoom = app.mScaleFactor;    // canvas zoom
        List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        computeReferences( list, mPlot.start );
        mOffset.x = x; 
        mOffset.y = y; 
        mZoom = z;    
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
    }

    void deletePoint( DrawingPointPath point ) 
    {
      mDrawingSurface.deletePath( point );
    }

    void deleteLine( DrawingLinePath line ) 
    {
      mDrawingSurface.deletePath( line );
    }

    void deleteArea( DrawingAreaPath area ) 
    {
      mDrawingSurface.deletePath( area );
    }

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
      Log.v( TopoDroidApp.TAG, sb.toString() );
    }
    

    float spacing( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return FloatMath.sqrt(x*x + y*y);
    }


    public boolean onTouch( View view, MotionEvent rawEvent )
    {
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

      int action = event.getAction() & MotionEvent.ACTION_MASK;
      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        mTouchMode = MODE_MOVE;
        /* nothing */
      } else if (action == MotionEvent.ACTION_DOWN) {
        if ( mMode == MODE_DRAW ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
          mPointCnt = 0;
          if ( mSymbol == SYMBOL_LINE ) {
            mDrawingSurface.isDrawing = true;
            mCurrentLinePath = new DrawingLinePath( mCurrentLine );
            mCurrentLinePath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
          } else if ( mSymbol == SYMBOL_AREA ) {
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
            mDrawingSurface.isDrawing = true;
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, null, true );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          }
        } else if ( mMode == MODE_EDIT ) {
          // setTitle( R.string.title_edit );
          doSelectAt( x_scene, y_scene );
        } else if ( mMode == MODE_MOVE ) {
          setTitle( R.string.title_move );
          mSaveX = x_canvas;
          mSaveY = y_canvas;
          mStartX = x_canvas;
          mStartY = y_canvas;
          return false;
        }
      } else if (action == MotionEvent.ACTION_MOVE) {
        Log.v( "DistoX", "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
        if ( mTouchMode == MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          mSaveX = x_canvas;                 // reset start
          mSaveY = y_canvas;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE ) {
              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > app.mLineSegment ) {
                mDrawingSurface.isDrawing = true;
                if ( ++mPointCnt % app.mLineType == 0 ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
              }
            } else if ( mSymbol == SYMBOL_AREA ) {
              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > app.mLineSegment ) {
                mDrawingSurface.isDrawing = true;
                if ( ++mPointCnt % app.mLineType == 0 ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
              }
            }
          } else if ( mMode == MODE_MOVE ) {
            mOffset.x += x_shift / mZoom;                // add shift to offset
            mOffset.y += y_shift / mZoom; 
            mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
            // mDrawingSurface.refresh();
          } else { // mMode == MODE_EDIT
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
        }
      } else if (action == MotionEvent.ACTION_UP) {
        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        } else {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE || mSymbol == SYMBOL_AREA ) {
              // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_UP line style " + app.mLineStyle );
              // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, 
              //   "  path size " + ((mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size()) );
              mCurrentBrush.mouseUp( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
              mDrawingSurface.previewPath.path = new Path();

              if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > app.mLineSegment || (mPointCnt % app.mLineType) > 0 ) {
                if ( mSymbol == SYMBOL_LINE ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                } else if ( mSymbol == SYMBOL_AREA ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
              }
              if ( mPointCnt > app.mLineType ) {
                if ( app.mLineStyle == app.LINE_STYLE_BEZIER ) {
                  int nPts = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.points.size() : mCurrentAreaPath.points.size() ;
                  if ( nPts > 1 ) {
                    ArrayList< BezierPoint > pts = new ArrayList< BezierPoint >(); // [ nPts ];
                    ArrayList< LinePoint > lp = 
                      (mSymbol == SYMBOL_LINE )? mCurrentLinePath.points : mCurrentAreaPath.points ;
                    for (int k=0; k<nPts; ++k ) {
                      pts.add( new BezierPoint( lp.get(k).mX, lp.get(k).mY ) );
                    }
                    mBezierInterpolator.fitCurve( pts, nPts, app.mLineAccuracy, app.mLineCorner );
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
                        DrawingAreaPath bezier_path = new DrawingAreaPath( mCurrentArea, null, true ); 
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
                    mDrawingSurface.addDrawingPath( mCurrentLinePath );
                  } else { //  mSymbol == SYMBOL_AREA
                    mCurrentAreaPath.close();
                    mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                  }
                }
                // undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                // canRedo = false;
              }
            } else { // SYMBOL_POINT
              if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
                if ( DrawingBrushPaths.mPointLib.pointHasText(mCurrentPoint) ) {
                  DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                  label.show();
                } else {
                  DrawingPointPath path = new DrawingPointPath( mCurrentPoint, x_scene, y_scene, DrawingPointPath.SCALE_M, null );
                  mDrawingSurface.addDrawingPath( path );

                  // undoBtn.setEnabled(true);
                  // redoBtn.setEnabled(false);
                  // canRedo = false;
                }
              }
            }
          } else { // MODE_MOVE 
            // if ( Math.abs( x_canvas - mStartX ) < 16 && Math.abs( y_canvas - mStartY ) < 16 ) {
            //   return false; // long click
            // }
            /* nothing */
          }
        }
      }
      return true;
    }

    // add a therion label point
    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, DrawingPointPath.SCALE_M, null );
        mDrawingSurface.addDrawingPath( label_path );
      } 
    }

    // add a therion station point
    public void addStationPoint( DrawingStationName st )
    {
      DrawingStationPath path = new DrawingStationPath( st, DrawingPointPath.SCALE_M );
      mDrawingSurface.addDrawingPath( path );
    }


    public void onClick(View view)
    {
        // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingActivity onClick() " + view.toString() );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingActivity onClick() point " + mCurrentPoint + " symbol " + mSymbol );
        switch (view.getId()){
            case R.id.undoBtn:
              mDrawingSurface.undo();
              if( mDrawingSurface.hasMoreUndo() == false ){
                  // undoBtn.setEnabled( false );
              }
              // redoBtn.setEnabled( true );
              // canRedo = true;
              break;

            // case R.id.zoomBtn:
            //   zoomView( );
            //   break;
            case R.id.displayBtn:
              new DrawingModeDialog( this, mDrawingSurface ).show();
              break;
            case R.id.pointBtn:
              new DrawingPointPickerDialog(this, this, mCurrentPoint).show();
              break;
            case R.id.lineBtn:
              new DrawingLinePickerDialog(this, this, mCurrentLine).show();
              break;
            case R.id.areaBtn:
              new DrawingAreaPickerDialog(this, this, mCurrentArea).show();
              break;
            case R.id.modeBtn:
              if ( mMode == MODE_DRAW ||  mMode == MODE_EDIT ) { 
                mMode = MODE_MOVE;
              } else if ( mMode == MODE_MOVE ) {
                mMode = MODE_DRAW;
              }
              // mDrawingSurface.clearHighlight();
              setTheTitle();
              break;
        }
    }

    private class SaveTherionFile extends AsyncTask<Intent,Void,Boolean>
    {
        private Context mContext;
        private Handler mHandler;
        private DrawingSurface mSurface;
        private String mFullName;

        public SaveTherionFile( Context context, Handler handler, DrawingSurface surface, String name )
        {
           mContext  = context;
           mSurface  = surface;
           mHandler  = handler;
           mFullName = name;
           // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "SaveTherionFile " + mFullName );
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            String filename = app.getTh2FileWithExt( mFullName );
            FileWriter writer = new FileWriter( filename );
            BufferedWriter out = new BufferedWriter( writer );
            mSurface.exportTherion( out, mFullName, app.projName[ mType ] );
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
            if ( bool ){
                mHandler.sendEmptyMessage(1);
            }
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
            String filename = app.getPngFileWithExt( mFullName );
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
            if ( bool ){
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    // ----------------------------------------------
    // options

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      mMIsave    = menu.add( R.string.menu_save_th2 );
      mMIredo    = menu.add( R.string.menu_redo );
      mMIone     = menu.add( R.string.menu_one );
      // mMIdisplay = menu.add( R.string.menu_display );
      mMIstats   = menu.add( R.string.menu_stats );
      mMInotes   = menu.add( R.string.menu_notes );
      mSMmore    = menu.addSubMenu( R.string.menu_more );
      mMIdelete  = mSMmore.add( R.string.menu_delete );
      // mMIsavePNG = mSMmore.add( R.string.menu_save_png );
      mMIzoom    = mSMmore.add( R.string.menu_zoom );
      // mMIsymbol  = mSMmore.add( R.string.menu_symbol );
      mMIoptions = mSMmore.add( R.string.menu_options );
      // mMIhelp    = mSMmore.add( R.string.menu_help );

      mMIsave.setIcon( R.drawable.save );
      mMIredo.setIcon( R.drawable.redo );
      mMIone.setIcon( R.drawable.zoomone );
      // mMIdisplay.setIcon( R.drawable.display );
      mMIstats.setIcon( R.drawable.info );
      mMInotes.setIcon( R.drawable.compose );
      mSMmore.setIcon( R.drawable.more );
      // mMIdelete.setIcon( R.drawable.delete );
      // mMIsavePNG.setIcon( R.drawable.gallery );
      // mMIoptions.setIcon( R.drawable.prefs );
      // mMIhelp.setIcon( R.drawable.help );

      if ( mNum == null ) mMIstats.setEnabled( false );
      return true;
    }

    void savePng()
    {
      final Activity currentActivity  = this;
      Handler saveHandler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
      //         final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
      //         alertDialog.setTitle("Saving sketch");
      //         alertDialog.setMessage("File: " + mFullName );
      //         alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
      //             public void onClick(DialogInterface dialog, int which) {
      //                 return;
      //             }
      //         });
      //         alertDialog.show();
           }
      } ;
      Bitmap bitmap = mDrawingSurface.getBitmap();
      if ( bitmap == null ) {
        Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_LONG ).show();
      } else {
        new ExportBitmapToFile(this, saveHandler, mDrawingSurface.getBitmap(), mFullName ).execute();
        Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".png", Toast.LENGTH_LONG ).show();
      }
    }

    void saveTh2()
    {
      if ( doSaveTh2( ! mAllSymbols ) ) {
        Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_LONG ).show();
      }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "DrawingActivity onOptionsItemSelected() " + item.toString() );

      if ( item == mMIredo ) {
        // if ( canRedo ) {
        //   mDrawingSurface.redo();
        //   if ( mDrawingSurface.hasMoreRedo() == false ) {
        //     // redoBtn.setEnabled( false );
        //     canRedo = false;
        //   }
        //   // undoBtn.setEnabled( true );
        // }
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
        }
      } else if ( item == mMInotes ) {
        String survey = mData.getSurveyFromId(mSid);
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( app.TOPODROID_SURVEY, survey );
        startActivity( notesIntent );
      } else if (item == mMIstats && mNum != null ) {
        new DistoXStatDialog( mDrawingSurface.getContext(), mNum ).show();
      } else if (item == mMIone ) {
        resetZoom();
      } else if (item == mMIzoom ) {
        new DrawingZoomDialog( mDrawingSurface.getContext(), this ).show();
      // } else if (item == mMIdisplay ) {
      //   // mDrawingSurface.toggleDisplayMode();
      //   new DrawingModeDialog( this, mDrawingSurface ).show();
      // } else if (item == mMIsavePNG ) {
      //   savePng();
      } else if (item == mMIdelete ) {
        // TODO ask for confirmation: however file th2 is not deleted
        // if ( mType == TopoDroidApp.PLOT_PLAN || mType == TopoDroidApp.PLOT_EXTENDED )
          mData.deletePlot( mPid, mSid );
        finish();
      // } else if ( item == mMIsymbol ) { // SYMBOL APP
      //   Intent intent = new Intent( "TdSymbol.intent.action.Launch" );
      //   try {
      //     startActivity( intent );
      //     DrawingBrushPaths.reloadSymbols();
      //   } catch ( ActivityNotFoundException e ) {
      //     Toast.makeText( this, R.string.no_tdsymbol, Toast.LENGTH_LONG ).show();
      //   }
      } else if ( item == mMIoptions ) { // OPTIONS DIALOG
        Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
        optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( optionsIntent );
      // } else if ( item == mMIhelp ) { // HELP
      //   TopoDroidHelp.show( this, R.string.help_drawing );
      } else if (item == mMIsave ) {
        new PlotSaveDialog( this, this ).show();
        // if ( doSaveTh2( ! mAllSymbols ) ) {
        //   Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_LONG ).show();
        // }
      }
      return true;
    }

}
