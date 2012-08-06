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
 */
package com.android.DistoX;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

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
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;


/**
 */
public class DrawingActivity extends Activity 
                             implements View.OnTouchListener
                                      , View.OnLongClickListener
                                      , DrawingPointPickerDialog.OnPointSelectedListener
{
    private TopoDroidApp app;

    // private static final String TITLE_DRAW_POINT = "Draw point ";
    // private static final String TITLE_DRAW_LINE  = "Draw line ";
    // private static final String TITLE_DRAW_AREA  = "Draw area ";
    // private static final String TITLE_MOVE       = "TopoDroid - Move";
    // private static final String TITLE_EDIT       = "TopoDroid - View";

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
    private boolean canRedo;
    private DistoXNum mNum;
    private int mPointCnt; // counter of points in the currently drawing line

    // private Button redoBtn;
    private Button undoBtn;
    private Button zoomBtn;
    private Button pointBtn;
    private Button lineBtn;
    private Button areaBtn;
    private Button modeBtn;

    private DrawingBrush mCurrentBrush;
    private Path  mCurrentPath;

    private String mFullName;
    
    private MenuItem mMIsaveTH2;
    private MenuItem mMIsavePNG;
    private MenuItem mMIoptions;
    private MenuItem mMIhelp;
    private MenuItem mMInotes;
    private MenuItem mMIredo;
    private MenuItem mMIdisplay;
    private MenuItem mMIstats;
    private MenuItem mMIdelete;
    private SubMenu  mSMmore;

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public static final int SYMBOL_POINT = 1;
    public static final int SYMBOL_LINE  = 2;
    public static final int SYMBOL_AREA  = 3;
    public static final int MODE_DRAW  = 1;
    public static final int MODE_MOVE  = 2;
    // public static final int MODE_EDIT  = 3;
    public int mSymbol = SYMBOL_LINE; // default
    public int mMode   = MODE_MOVE;
    private float mSaveX;
    private float mSaveY;
    private float mStartX;
    private float mStartY;
    private PointF mOffset = new PointF( 0f, 0f );
    private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );
    private static float mZoom = 1.0f;
    private DataHelper mData;
    private long mSid; // survey id
    private long mPid; // plot id

    // private float mLineSegment;
    // private float mLineAcc;
    // private float mLineCorner;

    // public PointF Offset() { return mOffset; }
    // public PointF Center() { return mCenter; }
    // public float  Zoom()   { return mZoom; }

    private void changeZoom( float f ) 
    {
      mZoom     *= f;
      mOffset.x = mOffset.x/f;
      mOffset.y = mOffset.y/f;
      if ( f > 1.0f ) {
        mOffset.x -= mCenter.x/2f;
        mOffset.y -= mCenter.y/2f;
      } else if ( f < 1.0f ) {
        mOffset.x += mCenter.x/2f;
        mOffset.y += mCenter.y/2f;
      }
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    }

    private void resetZoom() 
    {
      mZoom = app.mScaleFactor;
      mOffset.x = 0.0f;
      mOffset.y = 0.0f;
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    public void zoomOne() { resetZoom( ); }

    public void zoomView( )
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "zoomView ");
      DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
      zoom.show();
    }


    public void colorChanged( int color )
    {
      // if ( mCurrentPaint != null ) {
      //   mCurrentPaint.setColor( color );
      // }
    }

    public void areaSelected( int k ) 
    {
      if ( k >= 0 && k < DrawingBrushPaths.AREA_MAX ) {
        mSymbol = SYMBOL_AREA;
        mCurrentArea = k;
      }
      setTheTitle();
    }

    public void lineSelected( int k ) 
    {
      if ( k >= 0 && k < DrawingBrushPaths.LINE_MAX ) {
        mSymbol = SYMBOL_LINE;
        mCurrentLine = k;
      }
      setTheTitle();
    }

    public void pointSelected( int p )
    {
      if ( p >= 0 && p < DrawingBrushPaths.POINT_MAX ) {
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


    private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, boolean splay )
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
      mDrawingSurface.addFixedPath( mFixedDrawingPath );
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
        // modeBtn.setBackgroundResource( R.drawable.draw );
        if ( mSymbol == SYMBOL_POINT ) {
          setTitle( String.format( res.getString(R.string.title_draw_point), 
                                   DrawingBrushPaths.pointLocalName[mCurrentPoint] ) );
        } else if ( mSymbol == SYMBOL_LINE ) {
          setTitle( String.format( res.getString(R.string.title_draw_line),
                                   DrawingBrushPaths.lineLocalName[mCurrentLine] ) );
        } else  {  // if ( mSymbol == SYMBOL_LINE ) 
          setTitle( String.format( res.getString(R.string.title_draw_area),
                                   DrawingBrushPaths.areaLocalName[mCurrentArea] ) );
        }
      } else if ( mMode == MODE_MOVE ) {
        modeBtn.setText( res.getString(R.string.btn_move ) );
        // modeBtn.setBackgroundResource( R.drawable.move );
        setTitle( R.string.title_move );
      // } else if ( mMode == MODE_EDIT ) {
      //   modeBtn.setText( res.getString(R.string.btn_view ) );
      //   // modeBtn.setBackgroundResource( R.drawable.move );
      //   setTitle( R.string.title_edit );
      }
    }

    private boolean saveTh2()
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, " savingTh2 ");
      if ( mFullName != null && mDrawingSurface != null ) {
        try {
          String filename = app.getTh2FileWithExt( mFullName );
          FileWriter writer = new FileWriter( filename );
          BufferedWriter out = new BufferedWriter( writer );
          mDrawingSurface.exportTherion( out, mFullName, app.projName[ mType ] );
          out.flush();
          out.close();
          return true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return false;
    }

    @Override
    protected synchronized void onStop() 
    {   
      super.onStop();
      saveTh2();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.drawing_activity);
      app = (TopoDroidApp)getApplication();

      CENTER_X = app.mDisplayWidth  / 2;
      CENTER_Y = app.mDisplayHeight / 2;
      mZoom    = app.mScaleFactor;    // canvas zoom

      // setCurrentPaint();
      mCurrentBrush = new DrawingPenBrush();
      mCurrentPoint = DrawingBrushPaths.POINT_BLOCK;
      mCurrentLine  = DrawingBrushPaths.LINE_WALL;
      mCurrentArea  = DrawingBrushPaths.AREA_WATER;

      mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mDrawingSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
      mDrawingSurface.previewPath.path = new Path();
      mDrawingSurface.previewPath.setPaint( getPreviewPaint() );
      mDrawingSurface.setOnTouchListener(this);
      mDrawingSurface.setOnLongClickListener(this);
      // mDrawingSurface.setBuiltInZoomControls(true);

      // redoBtn = (Button) findViewById(R.id.redoBtn);
      undoBtn = (Button) findViewById(R.id.undoBtn);
      zoomBtn = (Button) findViewById(R.id.zoomBtn);
      pointBtn = (Button) findViewById(R.id.pointBtn);
      lineBtn = (Button) findViewById(R.id.lineBtn);
      areaBtn = (Button) findViewById(R.id.areaBtn);
      modeBtn = (Button) findViewById(R.id.modeBtn);

      // undoBtn.setAlpha( 0.5f );
      // zoomBtn.setAlpha( 0.5f );
      // modeBtn.setAlpha( 0.5f );
      // lineBtn.setAlpha( 0.5f );
      // areaBtn.setAlpha( 0.5f );
      // pointBtn.setAlpha( 0.5f );

      // redoBtn.setEnabled(false);
      undoBtn.setEnabled(false);
      zoomBtn.setEnabled(true);

      setTheTitle();

      mData        = app.mData; // new DataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mFullName    = extras.getString( app.TOPODROID_PLOT_FILE ); // without ".th2"
      mSid         = extras.getLong(   app.TOPODROID_SURVEY_ID );
      mPid         = extras.getLong(   app.TOPODROID_PLOT_ID );
      String start = extras.getString( app.TOPODROID_PLOT_STRT );
      mType        = (int)( extras.getLong( app.TOPODROID_PLOT_TYPE ) );

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
          mNum = new DistoXNum( list, start );
          List< DistoXNum.Station > stations = mNum.getStations();
          List< DistoXNum.Shot > shots = mNum.getShots();
          List< DistoXNum.Splay > splays = mNum.getSplays();
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "stations " + stations.size() + " legs " + shots.size() );
          for ( DistoXNum.Shot sh : shots ) {
            DistoXNum.Station st1 = sh.from;
            DistoXNum.Station st2 = sh.to;
            if ( mType == TopoDroidApp.PLOT_PLAN ) {
              addFixedLine( sh.block, (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), false );
              // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, 
              //   "add line " + (float)(st1.e) + " " + (float)(st1.s) + " " + (float)(st2.e) + " " + (float)(st2.s) );
            } else if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
              addFixedLine( sh.block, (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v), false );
            } 
          }
          for ( DistoXNum.Splay sp : splays ) {
            DistoXNum.Station st = sp.from;
            if ( mType == TopoDroidApp.PLOT_PLAN ) {
              addFixedLine( sp.block, (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), true );
            } else if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
              addFixedLine( sp.block, (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), true );
            }
          }
          for ( DistoXNum.Station st : stations ) {
            if ( mType == TopoDroidApp.PLOT_PLAN ) {
              mDrawingSurface.addStation( st.name, toSceneX(st.e) - mOffset.x,
                                                   toSceneY(st.s) - mOffset.y );
            } else if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
              mDrawingSurface.addStation( st.name, toSceneX(st.h) - mOffset.x,
                                                   toSceneY(st.v) - mOffset.y );
            }
          }
          if ( mType == TopoDroidApp.PLOT_PLAN ) {
            addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax() );
          } else {
            addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax() );
          }
          if ( (! mNum.surveyAttached) && app.mCheckAttached ) {
            Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_LONG ).show();
          }
          // now try to load drawings from therion file
          String filename = app.getTh2FileWithExt( mFullName );
          mDrawingSurface.loadTherion( filename );
        }
      } else { // ( mType == TopoDroidApp.PLOT_V_SECTION || mType == TopoDroidApp.PLOT_H_SECTION ) {
        long extend0 = 1L;
        mNum = null;
        float xmin = 0.0f;
        float xmax = 0.0f;
        float ymin = 0.0f;
        float ymax = 0.0f;
        String viewed  = extras.getString( app.TOPODROID_PLOT_VIEW );
        List<DistoXDBlock> list = mData.selectAllShotsAtStation( mSid, start );
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "start " + start + " viewed " + viewed + " shots " + list.size() );
        if ( list.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
          finish();
        } else if ( mType == TopoDroidApp.PLOT_H_SECTION ) {
          mDrawingSurface.addStation( start, toSceneX(0.0f) - mOffset.x, toSceneY(0.0f) - mOffset.y );
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing;
            float d = bl.mLength * (float)Math.cos( bl.mClino * app.GRAD2RAD_FACTOR );
            float s = - d * (float)Math.cos( b * app.GRAD2RAD_FACTOR );
            float e =   d * (float)Math.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              mDrawingSurface.addStation( viewed, toSceneX(e) - mOffset.x, toSceneY(s) - mOffset.y );
              addFixedLine( bl, 0.0f, 0.0f, e, s, false );
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            } else {
              addFixedLine( bl, 0.0f, 0.0f, e, s, true );
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            }
          }
        } else { // ( mType == TopoDroidApp.PLOT_V_SECTION )
          mDrawingSurface.addStation( start, toSceneX(0.0f) - mOffset.x, toSceneY(0.0f) - mOffset.y );
          float bearing = 0.0f;
          DistoXDBlock bl0 = null;
          for ( DistoXDBlock bl : list ) {
            String to = bl.mTo;
            if ( to == null ) continue;
            if ( to.equals( viewed ) ) {
              bearing = bl.mBearing;
              extend0 = bl.mExtend;
              // float s = - bl.mLength * (float)Math.sin( bl.mClino * app.GRAD2RAD_FACTOR );
              bl0 = bl;
              break;
            }
          }
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing - bearing;
            float s = - bl.mLength * (float)Math.sin( bl.mClino * app.GRAD2RAD_FACTOR );
            float e = bl.mLength * (float)Math.cos( bl.mClino * app.GRAD2RAD_FACTOR ) 
                                  * (float)Math.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mExtend != extend0 ) {
              e = -e;
            }
            if ( bl.mTo != null && bl.mTo.equals( viewed ) ) {
              mDrawingSurface.addStation( viewed, toSceneX(e) - mOffset.x, toSceneY(s) - mOffset.y );
              addFixedLine( bl, 0.0f, 0.0f, e, s, false );
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            } else {
              addFixedLine( bl, 0.0f, 0.0f, e, s, true );
              if ( e < xmin ) { xmin = e; } else if ( e > xmax ) { xmax = e; }
              if ( s < ymin ) { ymin = s; } else if ( s > ymax ) { ymax = s; }
            }
          }
          addGrid( xmin, xmax, ymin, ymax );
        }
      }

      mBezierInterpolator = new BezierInterpolator( );
      resetZoom();
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
      if ( Math.abs( mSaveX - mStartX ) < 16 && Math.abs( mSaveY - mStartY ) < 16 ) {
        float x_scene = mStartX/mZoom - mOffset.x;
        float y_scene = mStartY/mZoom - mOffset.y;
        // Toast.makeText( this, "LONG CLICK", Toast.LENGTH_SHORT ).show();
        DrawingPointPath point = mDrawingSurface.getPointAt( x_scene, y_scene );
        if ( point != null ) {
          // StringWriter sw = new StringWriter();
          // PrintWriter pw = new PrintWriter( sw );
          // pw.format("Point %s at %.2f %.2f",
          //   DrawingBrushPaths.pointThName[ point.mPointType ],
          //   point.mXpos,
          //   point.mYpos );
          // Toast.makeText( this, sw.getBuffer().toString(), Toast.LENGTH_SHORT ).show();
          (new DrawingPointDialog( this, point )).show();
        } else {
          DrawingLinePath line = mDrawingSurface.getLineAt( x_scene, y_scene );
          if ( line != null ) {
            (new DrawingLineDialog( this, line )).show();
          }
        }
      }
      return true;
    }

    // void refreshSurface()
    // {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "refresh surface");
    //   mDrawingSurface.refresh();
    // }

    public boolean onTouch( View view, MotionEvent motionEvent )
    {
      float x_canvas = motionEvent.getX();
      float y_canvas = motionEvent.getY();
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
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
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, null );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          }
        } else if ( mMode == MODE_MOVE ) { // MODE_EDIT
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "highlight scene " + x_scene + " " + y_scene );
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  center " + mCenter.x + " " + mCenter.y );
          // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  offset " + mOffset.x + " " + mOffset.y );
          // setTitle( R.string.title_edit );
          setTitle( R.string.title_move );

          // DistoXDBlock b = mDrawingSurface.highlight( mType, x_scene, y_scene );
          // if ( b != null ) {
          //   StringWriter sw = new StringWriter();
          //   PrintWriter  pw = new PrintWriter( sw );
          //   pw.format("%s-%s %.2f %.1f %.1f", b.mFrom, (b.mTo==null)? "" : b.mTo, b.mLength, b.mBearing, b.mClino );
          //   setTitle(  sw.getBuffer().toString() );
          // }

          // MODE_MOVE
          mSaveX = x_canvas;
          mSaveY = y_canvas;
          mStartX = x_canvas;
          mStartY = y_canvas;
          return false;
        }
      } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
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
        } else { // MODE_MOVE
          if ( mMode == MODE_MOVE ) {
            mOffset.x += x_shift / mZoom;                // add shift to offset
            mOffset.y += y_shift / mZoom; 
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "shift offset " + mOffset.x + " " + mOffset.y );
            mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
          }
        }
      } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
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
                int nPts = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size() ;
                if ( nPts > 1 ) {
                  ArrayList< BezierPoint > pts = new ArrayList< BezierPoint >(); // [ nPts ];
                  ArrayList< LinePoint > lp = 
                    (mSymbol == SYMBOL_LINE )? mCurrentLinePath.getPoints() : mCurrentAreaPath.getPoints() ;
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
                      // if ( mCurrentLine ==  DrawingBrushPaths.LINE_PIT 
                      //   || mCurrentLine ==  DrawingBrushPaths.LINE_CHIMNEY
                      //   || mCurrentLine ==  DrawingBrushPaths.LINE_SLOPE ) {
                      //   BezierPoint p1 = c.getPoint(1);
                      //   float dx = p1.mX - p0.mX;
                      //   float dy = p1.mY - p0.mY;
                      //   float d = dx*dx + dy*dy;
                      //   if ( d > 0.0f ) {
                      //     d = 5.0f / (float)Math.sqrt( d );
                      //     dx *= d;
                      //     dy *= d;
                      //     if ( mCurrentLine ==  DrawingBrushPaths.LINE_PIT ) {
                      //       bezier_path.addTick( p0.mX, p0.mY, dy, -dx );
                      //     } else if ( mCurrentLine ==  DrawingBrushPaths.LINE_SLOPE ) {
                      //       bezier_path.addTick( p0.mX, p0.mY, 3*dy, -3*dx );
                      //     } else {
                      //       bezier_path.addTick( p0.mX, p0.mY, -dy, dx );
                      //     }
                      //   }
                      // }
                      for (int k=0; k<k0; ++k) {
                        c = curves.get(k);
                        BezierPoint p1 = c.getPoint(1);
                        BezierPoint p2 = c.getPoint(2);
                        BezierPoint p3 = c.getPoint(3);
                        bezier_path.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                      }
                      mDrawingSurface.addDrawingPath( bezier_path );
                    } else { //  mSymbol == SYMBOL_AREA
                      DrawingAreaPath bezier_path = new DrawingAreaPath( mCurrentArea, null ); 
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
              undoBtn.setEnabled(true);
              // redoBtn.setEnabled(false);
              canRedo = false;
            }
          } else { // SYMBOL_POINT
            if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
              if ( mCurrentPoint == DrawingBrushPaths.POINT_LABEL ) {
                DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                label.show();
              } else {
                DrawingPointPath path = new DrawingPointPath( mCurrentPoint, x_scene, y_scene, DrawingPointPath.SCALE_M, null );
                mDrawingSurface.addDrawingPath( path );

                undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                canRedo = false;
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
      return true;
    }

    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, DrawingPointPath.SCALE_M, null );
        // label_path.setPaint( mCurrentPaint );
        // label_path.path.offset( x, y );
        mDrawingSurface.addDrawingPath( label_path );

        undoBtn.setEnabled(true);
        // redoBtn.setEnabled(false);
        canRedo = false;
      } 
    }


    public void onClick(View view)
    {
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingActivity onClick() point " + mCurrentPoint + " symbol " + mSymbol );
        switch (view.getId()){
            case R.id.undoBtn:
                mDrawingSurface.undo();
                if( mDrawingSurface.hasMoreUndo() == false ){
                    undoBtn.setEnabled( false );
                }
                // redoBtn.setEnabled( true );
                canRedo = true;
            break;

            case R.id.zoomBtn:
              zoomView( );
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
              if ( mMode == MODE_DRAW ) { 
                mMode = MODE_MOVE;
              } else if ( mMode == MODE_MOVE ) {
              //   mMode = MODE_EDIT;
              // } else if ( mMode == MODE_EDIT ) {
                mMode = MODE_DRAW;
              }
              // mDrawingSurface.clearHighlight();
              setTheTitle();
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
      mMIsaveTH2 = menu.add( R.string.menu_save_th2 );
      mMIredo    = menu.add( R.string.menu_redo );
      mMIdisplay = menu.add( R.string.menu_display );
      mMIstats   = menu.add( R.string.menu_stats );
      mMInotes   = menu.add( R.string.menu_notes );
      mSMmore    = menu.addSubMenu( R.string.menu_more );
      mMIdelete  = mSMmore.add( R.string.menu_delete );
      mMIsavePNG = mSMmore.add( R.string.menu_save_png );
      mMIoptions = mSMmore.add( R.string.menu_options );
      mMIhelp    = mSMmore.add( R.string.menu_help );

      mMIsaveTH2.setIcon( R.drawable.save );
      mMIredo.setIcon( R.drawable.redo );
      mMIdisplay.setIcon( R.drawable.display );
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      if ( item == mMIredo ) {
        if ( canRedo ) {
          mDrawingSurface.redo();
          if ( mDrawingSurface.hasMoreRedo() == false ) {
            // redoBtn.setEnabled( false );
            canRedo = false;
          }
          undoBtn.setEnabled( true );
        }
      } else if ( item == mMInotes ) {
        String survey = mData.getSurveyFromId(mSid);
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( app.TOPODROID_SURVEY, survey );
        startActivity( notesIntent );
      } else if (item == mMIstats && mNum != null ) {
        new DistoXStatDialog( mDrawingSurface.getContext(), mNum ).show();
      } else if (item == mMIdisplay ) {
        // mDrawingSurface.toggleDisplayMode();
        new DrawingModeDialog( this, mDrawingSurface ).show();
      } else if (item == mMIsavePNG ) {
        final Activity currentActivity  = this;
        Handler saveHandler = new Handler(){
             @Override
             public void handleMessage(Message msg) {
        //         final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
        //         alertDialog.setTitle("Saving scrap");
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
      } else if (item == mMIdelete ) {
        // TODO ask for confirmation: however file th2 is not deleted
        // if ( mType == TopoDroidApp.PLOT_PLAN || mType == TopoDroidApp.PLOT_EXTENDED )
          mData.deletePlot( mPid, mSid );
        finish();
      } else if ( item == mMIoptions ) { // OPTIONS DIALOG
        Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
        optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( optionsIntent );
      } else if ( item == mMIhelp ) { // HELP
        TopoDroidHelp.show( this, R.string.help_drawing );
      } else if (item == mMIsaveTH2 ) {
        if ( saveTh2() ) {
          Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_LONG ).show();
        }
      }
      return true;
    }

}
