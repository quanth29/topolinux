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
 */
package com.android.DistoX;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
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
                             implements View.OnTouchListener,
                                        DrawingPointPickerDialog.OnPointSelectedListener
{
    private static final String TAG = "DistoX DrawingActivity";
    private TopoDroidApp app;

    private static final String TITLE_DRAW_POINT = "Draw point ";
    private static final String TITLE_DRAW_LINE  = "Draw line ";
    private static final String TITLE_DRAW_AREA  = "Draw area ";
    private static final String TITLE_MOVE       = "TopoDroid - Move";
    // private static final String TITLE_EDIT       = "TopoDroid - View";

    // 0: no bezier, plain path
    // 1: bezier interpolator

    public static final float CENTER_X = 100f; // FIXME
    public static final float CENTER_Y = 120f;

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
    private PointF mOffset = new PointF( 0f, 0f );
    private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );
    private static float mZoom = 1.0f;                     // canvas zoom
    private DistoXDataHelper mData;
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
      mZoom = 1.0f;
      mOffset.x = 0.0f;
      mOffset.y = 0.0f;
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    public void zoomOne() { resetZoom( ); }

    public void zoomView( )
    {
      // Log.v( TAG, "zoomView ");
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
      setTitle();
    }

    public void lineSelected( int k ) 
    {
      if ( k >= 0 && k < DrawingBrushPaths.LINE_MAX ) {
        mSymbol = SYMBOL_LINE;
        mCurrentLine = k;
      }
      setTitle();
    }

    public void pointSelected( int p )
    {
      if ( p >= 0 && p < DrawingBrushPaths.POINT_MAX ) {
        mSymbol = SYMBOL_POINT;
        // pointBtn.setText("Point");
        mCurrentPoint = p;
      }
      setTitle();
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
      x1 = mCenter.x + x1 * SCALE_FIX;
      y1 = mCenter.y + y1 * SCALE_FIX;
      x2 = mCenter.x + x2 * SCALE_FIX;
      y2 = mCenter.y + y2 * SCALE_FIX;
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
      float x1 = (float)(mCenter.x + xmin * SCALE_FIX - mOffset.x);
      float x2 = (float)(mCenter.x + xmax * SCALE_FIX - mOffset.x);
      float y1 = (float)(mCenter.y + ymin * SCALE_FIX - mOffset.y);
      float y2 = (float)(mCenter.y + ymax * SCALE_FIX - mOffset.y);
      for ( int x = (int)Math.round(xmin); x < xmax; x += 1 ) {
        float x0 = (float)(mCenter.x + x * SCALE_FIX - mOffset.x);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.path  = new Path();
        mFixedDrawingPath.path.moveTo( x0, y1 );
        mFixedDrawingPath.path.lineTo( x0, y2 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
      for ( int y = (int)Math.round(ymin); y < ymax; y += 1 ) {
        float y0 = (float)(mCenter.y + y * SCALE_FIX - mOffset.y);
        mFixedDrawingPath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        mFixedDrawingPath.setPaint( DrawingBrushPaths.fixedGridPaint );
        mFixedDrawingPath.path  = new Path();
        mFixedDrawingPath.path.moveTo( x1, y0 );
        mFixedDrawingPath.path.lineTo( x2, y0 );
        mDrawingSurface.addGridPath( mFixedDrawingPath );
      }
    }

    private void setTitle()
    {
      if ( mMode == MODE_DRAW ) { 
        modeBtn.setText("Draw");
        // modeBtn.setBackgroundResource( R.drawable.draw );
        if ( mSymbol == SYMBOL_POINT ) {
          setTitle(TITLE_DRAW_POINT + DrawingBrushPaths.pointName[mCurrentPoint] );
        } else if ( mSymbol == SYMBOL_LINE ) {
          setTitle(TITLE_DRAW_LINE + DrawingBrushPaths.lineName[mCurrentLine] );
        } else  {  // if ( mSymbol == SYMBOL_LINE ) 
          setTitle(TITLE_DRAW_AREA + DrawingBrushPaths.areaName[mCurrentArea] );
        }
      } else if ( mMode == MODE_MOVE ) {
        modeBtn.setText("Move");
        // modeBtn.setBackgroundResource( R.drawable.move );
        setTitle( TITLE_MOVE );
      // } else if ( mMode == MODE_EDIT ) {
      //   modeBtn.setText("View");
      //   // modeBtn.setBackgroundResource( R.drawable.move );
      //   setTitle( TITLE_EDIT );
      }
    }

    private boolean saveTh2()
    {
      // Log.v( TAG, "saving th2 file\n");
      if ( mFullName != null && mDrawingSurface != null ) {
        try {
          File dir = new File( app.APP_TH2_PATH );
          if (!dir.exists()) dir.mkdirs();
          FileWriter writer = new FileWriter(new File(app.APP_TH2_PATH + mFullName + ".th2" ));
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

      setTitle();

      mData        = new DistoXDataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mFullName    = extras.getString( app.TOPODROID_PLOT_FILE );
      mSid         = extras.getLong(   app.TOPODROID_SURVEY_ID );
      mPid         = extras.getLong(   app.TOPODROID_PLOT_ID );
      String start = extras.getString( app.TOPODROID_PLOT_STRT );
      mType        = (int)( extras.getLong( app.TOPODROID_PLOT_TYPE ) );

      // mLineSegment = app.mLineSegment;
      // mLineAcc     = app.mLineAccuracy;
      // mLineCorner  = app.mLineCorner;
     
      if ( mType == TopoDroidApp.PLOT_PLAN || mType == TopoDroidApp.PLOT_EXTENDED ) {
        List<DistoXDBlock> list = mData.selectAllShots( mSid, 0 );
        // Log.v( TAG, "onCreate SID " + mSid + " start at " + start + " shots " + list.size() );
        if ( list.size() == 0 ) {
           Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
           finish();
        } else {
          mNum = new DistoXNum( list, start );
          List< DistoXNum.Station > stations = mNum.getStations();
          List< DistoXNum.Shot > shots = mNum.getShots();
          List< DistoXNum.Splay > splays = mNum.getSplays();
          // Log.v( TAG, "stations " + stations.size() + " legs " + shots.size() );
          for ( DistoXNum.Shot sh : shots ) {
            DistoXNum.Station st1 = sh.from;
            DistoXNum.Station st2 = sh.to;
            if ( mType == TopoDroidApp.PLOT_PLAN ) {
              addFixedLine( sh.block, (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), false );
              // Log.v( TAG, "add line " + (float)(st1.e) + " " + (float)(st1.s) + " "
              //        + (float)(st2.e) + " " + (float)(st2.s) );
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
              mDrawingSurface.addStation( st.name, st.e * SCALE_FIX, st.s * SCALE_FIX );
            } else if ( mType == TopoDroidApp.PLOT_EXTENDED ) {
              mDrawingSurface.addStation( st.name, st.h * SCALE_FIX, st.v * SCALE_FIX );
            }
          }
          if ( mType == TopoDroidApp.PLOT_PLAN ) {
            addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax() );
          } else {
            addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax() );
          }
          // now try to load drawings from therion file
          String filename = app.APP_TH2_PATH + mFullName + ".th2";
          mDrawingSurface.loadTherion( filename );
        }
      } else { // ( mType == TopoDroidApp.PLOT_V_SECTION || mType == TopoDroidApp.PLOT_H_SECTION ) {
        long extend0 = 1L;
        mNum = null;
        float xmin = 0.0f;
        float xmax = 0.0f;
        float ymin = 0.0f;
        float ymax = 0.0f;
        String station = start;
        String view_station = null;
        int idx = start.indexOf('-');
        if ( idx < 0 ) idx = start.indexOf(' ');
        if ( idx > 0 ) {
          station = start.substring(0, idx);
          view_station = start.substring( idx+1 );
        }
        // Log.v( TAG, "station " + station + " view-station " + view_station );
        List<DistoXDBlock> list = mData.selectAllShotsAtStation( mSid, station );
        if ( list.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
          finish();
        } else if ( mType == TopoDroidApp.PLOT_H_SECTION ) {
          mDrawingSurface.addStation( station, 0.0f * SCALE_FIX, 0.0f * SCALE_FIX );
          for ( DistoXDBlock bl : list ) {
            float b = bl.mBearing;
            float d = bl.mLength * (float)Math.cos( bl.mClino * app.GRAD2RAD_FACTOR );
            float s = - d * (float)Math.cos( b * app.GRAD2RAD_FACTOR );
            float e =   d * (float)Math.sin( b * app.GRAD2RAD_FACTOR );
            if ( bl.mTo != null ) {
              mDrawingSurface.addStation( view_station, e * SCALE_FIX, s * SCALE_FIX );
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
          mDrawingSurface.addStation( station, 0.0f * SCALE_FIX, 0.0f * SCALE_FIX );
          float bearing = 0.0f;
          DistoXDBlock bl0 = null;
          for ( DistoXDBlock bl : list ) {
            String to = bl.mTo;
            if ( to == null ) continue;
            if ( to.equals( view_station ) ) {
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
            if ( bl.mTo != null ) {
              mDrawingSurface.addStation( view_station, e * SCALE_FIX, s * SCALE_FIX );
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


    public boolean onTouch(View view, MotionEvent motionEvent)
    {
      float x_canvas = motionEvent.getX();
      float y_canvas = motionEvent.getY();
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        if ( mMode == MODE_DRAW ) {
          // Log.v( TAG, "onTouch ACTION_DOWN symbol " + mSymbol );
          mPointCnt = 0;
          if ( mSymbol == SYMBOL_LINE ) {
            mDrawingSurface.isDrawing = true;
            mCurrentLinePath = new DrawingLinePath( mCurrentLine );
            mCurrentLinePath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
          } else if ( mSymbol == SYMBOL_AREA ) {
            // Log.v( TAG, "onTouch ACTION_DOWN area type " + mCurrentArea );
            mDrawingSurface.isDrawing = true;
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, null );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.path, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          }
        } else if ( mMode == MODE_MOVE ) { // MODE_EDIT
          // Log.v(TAG, "highlight scene " + x_scene + " " + y_scene );
          // Log.v(TAG, "  center " + mCenter.x + " " + mCenter.y );
          // Log.v(TAG, "  offset " + mOffset.x + " " + mOffset.y );
          // setTitle( TITLE_EDIT );
          setTitle( TITLE_MOVE );

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
            mOffset.x += x_shift;                // add shift to offset
            mOffset.y += y_shift; 
            // Log.v( TAG, "shift: offset " + mOffset.x + " " + mOffset.y );
            mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
          }
        }
      } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
        if ( mMode == MODE_DRAW ) {
          if ( mSymbol == SYMBOL_LINE || mSymbol == SYMBOL_AREA ) {
            // Log.v( TAG, "onTouch ACTION_UP line style " + app.mLineStyle );
            // Log.v( TAG, "  path size " + ((mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size()) );
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
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
                  // Log.v( TAG, " Bezier size " + k0 );
                  if ( k0 > 0 ) {
                    BezierCurve c = curves.get(0);
                    BezierPoint p0 = c.getPoint(0);
                    if ( mSymbol == SYMBOL_LINE ) {
                      DrawingLinePath bezier_path = new DrawingLinePath( mCurrentLine );
                      bezier_path.addStartPoint( p0.mX, p0.mY );
                      if ( mCurrentLine ==  DrawingBrushPaths.LINE_PIT 
                        || mCurrentLine ==  DrawingBrushPaths.LINE_CHIMNEY
                        || mCurrentLine ==  DrawingBrushPaths.LINE_SLOPE ) {
                        BezierPoint p1 = c.getPoint(1);
                        float dx = p1.mX - p0.mX;
                        float dy = p1.mY - p0.mY;
                        float d = dx*dx + dy*dy;
                        if ( d > 0.0f ) {
                          d = 5.0f / (float)Math.sqrt( d );
                          dx *= d;
                          dy *= d;
                          if ( mCurrentLine ==  DrawingBrushPaths.LINE_PIT ) {
                            bezier_path.addTick( p0.mX, p0.mY, dy, -dx );
                          } else if ( mCurrentLine ==  DrawingBrushPaths.LINE_SLOPE ) {
                            bezier_path.addTick( p0.mX, p0.mY, 3*dy, -3*dx );
                          } else {
                            bezier_path.addTick( p0.mX, p0.mY, -dy, dx );
                          }
                        }
                      }
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
            if ( Math.abs( x_canvas - mSaveX ) < 16 && Math.abs( y_canvas - mSaveY ) < 16 ) {
              if ( mCurrentPoint == DrawingBrushPaths.POINT_LABEL ) {
                DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                label.show();
              } else {
                DrawingPointPath path = new DrawingPointPath( mCurrentPoint, x_scene, y_scene );
                mDrawingSurface.addDrawingPath( path );

                undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                canRedo = false;
              }
            }
          }
        } else { // MODE_MOVE 
          /* nothing */
        }
      }
      return true;
    }

    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y );
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
        // Log.v( TAG, "onClick() point " + mCurrentPoint + " symbol " + mSymbol );
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
              setTitle();
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
            // Log.v( TAG, "Export as file " + mFullName );
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            File dir = new File( app.APP_MAPS_PATH );
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final FileOutputStream out = new FileOutputStream(new File(app.APP_MAPS_PATH + mFullName + ".png" ));
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
      // Log.v( TAG, "onCreateOptionsMenu()");
      mMIsaveTH2 = menu.add( R.string.menu_save_th2 );
      mMIredo    = menu.add( R.string.menu_redo );
      mMIdisplay = menu.add( R.string.menu_display );
      mMIstats   = menu.add( R.string.menu_stats );
      mMInotes   = menu.add( R.string.menu_notes );
      mSMmore    = menu.addSubMenu( R.string.menu_more );
      mMIdelete  = mSMmore.add( R.string.menu_delete );
      mMIsavePNG = mSMmore.add( R.string.menu_save_png );
      mMIoptions = mSMmore.add( R.string.menu_options );

      mMIsaveTH2.setIcon( R.drawable.save );
      mMIredo.setIcon( R.drawable.redo );
      mMIdisplay.setIcon( R.drawable.display );
      mMIstats.setIcon( R.drawable.help );
      mMInotes.setIcon( R.drawable.compose );
      mSMmore.setIcon( R.drawable.more );
      // mMIdelete.setIcon( R.drawable.delete );
      // mMIsavePNG.setIcon( R.drawable.gallery );
      // mMIoptions.setIcon( R.drawable.prefs );

      if ( mNum == null ) mMIstats.setEnabled( false );
      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      // Log.v( TAG, "onOptionsItemSelected()");
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
        startActivity( optionsIntent );
      } else if (item == mMIsaveTH2 ) {
        if ( saveTh2() ) {
          Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_LONG ).show();
        }
      }
      return true;
    }

}
