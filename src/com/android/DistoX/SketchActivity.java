/* @file SketchActivity.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid main 3d sketch activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130216 created
 * 20130307 made Annotations into a dialog
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
import android.content.pm.ActivityInfo;

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
public class SketchActivity extends Activity 
                            implements View.OnTouchListener
                                     , View.OnClickListener
                                     , View.OnLongClickListener
                                     , DrawingPointPickerDialog.OnPointSelectedListener
                                     , DrawingLinePickerDialog.OnLineSelectedListener
                                     , DrawingAreaPickerDialog.OnAreaSelectedListener
                                     , OnZoomListener
                                     , ILabelAdder
                                     , IExtruder
                                     , ILister
{
  static final String TAG = "DistoX";

  private TopoDroidApp app;
  private Sketch3dInfo mInfo;
  private SketchPainter mPainter;

  private SketchDrawingSurface mSketchSurface;

  private DrawingLinePath mCurrentLinePath;
  // private DrawingAreaPath mCurrentAreaPath;

  private int mCurrentPoint;  // current point type
  private int mCurrentLine;   // current line type
  private int mCurrentArea;   // current area type
  private static final long mType = TopoDroidApp.PLOT_SKETCH_3D;

  // private boolean canRedo;
  private DistoXNum mNum;
  private int mPointCnt; // counter of points in the currently drawing line

  private boolean mIsNotMultitouch;
  private boolean mAllSymbols; // whether the library has all the symbols of the plot
  // private boolean mDoMakeSurface; // whether a surface can be safely made

  private Button modeBtn;
  // private Button redoBtn;
  private Button undoBtn;
  private Button viewBtn;
  private Button symbolBtn;
  // private Button pointBtn;
  // private Button lineBtn;
  // private Button areaBtn;
  private Button editBtn;


  private DrawingBrush mCurrentBrush;
  // private Path  mCurrentPath;

  private String mFullName;
    
  private MenuItem mMIsave;
  private MenuItem mMIoptions;
  // private MenuItem mMIzoom;
  private MenuItem mMInotes;
  // private MenuItem mMIedit;
  private MenuItem mMIdownload;
  private MenuItem mMIredo;
  private MenuItem mMIone;
  private MenuItem mMIdisplay; // 3D display mode
  private MenuItem mMIstats;
  private MenuItem mMIdelete;
  private MenuItem mMIsurface;
  private SubMenu  mSMmore;

  ZoomButtonsController mZoomBtnsCtrl;
  View mZoomView;
  ZoomControls mZoomCtrl;
  private float oldDist;  // zoom pointer-spacing
  // private float oldDir;   // zoom pointer direction
  // private float oldPos;   // zoom pointer position

  public int mSymbol = SketchDef.SYMBOL_LINE; // default
  public int mMode   = SketchDef.MODE_MOVE;
  public int mView   = SketchDef.VIEW_TOP;
  // private int mSaveView  = SketchDef.VIEW_3D;
  private int mTouchMode = SketchDef.TOUCH_MOVE;
  private int mEdit = SketchDef.EDIT_NONE;

  private float mSaveX;
  private float mSaveY;
  // private float mStartX;
  // private float mStartY;

  private DataHelper mData;
  private long mSid; // survey id
  private long mPid; // sketch id

  private SketchModel mModel;

  // ---------------------------------------------------------------------------
  // helper private methods 

  private void setSymbolButton()
  {
      Resources res = getResources();
      switch ( mSymbol ) {
      case SketchDef.SYMBOL_POINT:
        symbolBtn.setText( res.getString(R.string.btn_point ) );
        break;
      case SketchDef.SYMBOL_AREA:
        symbolBtn.setText( res.getString(R.string.btn_area ) );
        break;
      // case SketchDef.SYMBOL_LINE:
      default:
        symbolBtn.setText( res.getString(R.string.btn_line ) );
        break;
      }
  }

  private void setTheTitle()
  {
    Resources res = getResources();
    // String dir = mInfo.getDirectionString();
    String symbol_name =
        ( mSymbol == SketchDef.SYMBOL_POINT )? 
        (res.getString(R.string.btn_point) + DrawingBrushPaths.mPointLib.getPointName( mCurrentPoint ))
      : ( mSymbol == SketchDef.SYMBOL_LINE )? res.getString(R.string.btn_line)
      : (res.getString(R.string.btn_area) + DrawingBrushPaths.mAreaLib.getAreaName( mCurrentArea ) );

    setTitle( String.format( res.getString( R.string.title_sketch), 
        ( mMode == SketchDef.MODE_DRAW )? "DRAW" 
      : ( mMode == SketchDef.MODE_MOVE )? "MOVE"
      : ( mMode == SketchDef.MODE_STEP )? "STEP"
      : "TURN",
      symbol_name,
        ( mView == SketchDef.VIEW_TOP )? "TOP"
      : ( mView == SketchDef.VIEW_SIDE )? "SIDE"
      : ( mView == SketchDef.VIEW_3D )? mInfo.getDirectionString()
      : "CROSS",
      mInfo.st1,
      mInfo.st2
    ) );

    switch ( mMode ) {
      case SketchDef.MODE_DRAW:
        modeBtn.setText( res.getString(R.string.btn_draw ) );
        // modeBtn.setBackgroundResource( R.drawable.note2 );
        modeBtn.setBackgroundColor( 0xff9999ff );
        break;
      case SketchDef.MODE_MOVE:
        modeBtn.setText( res.getString(R.string.btn_move ) );
        modeBtn.setBackgroundColor( 0xff999999 );
        break;
      case SketchDef.MODE_STEP:
        modeBtn.setText( res.getString(R.string.btn_step ) );
        modeBtn.setBackgroundColor( 0xffff9999 );
        break;
      case SketchDef.MODE_SHOT:
        modeBtn.setText( res.getString(R.string.btn_shot ) );
        modeBtn.setBackgroundColor( 0xffff3333 );
        break;
      // case SketchDef.MODE_ROTATE:
      //   modeBtn.setText( res.getString(R.string.btn_turn ) );
      //   modeBtn.setBackgroundColor( 0xff99ff99 );
      //   break;
    }

    switch ( mView ) {
      case SketchDef.VIEW_TOP:
        viewBtn.setText( res.getString( R.string.btn_top ) );
        mSymbol = SketchDef.SYMBOL_LINE;
        symbolBtn.setText( res.getString( R.string.btn_line ) );
        break;
      case SketchDef.VIEW_SIDE:
        viewBtn.setText( res.getString( R.string.btn_side ) );
        mSymbol = SketchDef.SYMBOL_LINE;
        symbolBtn.setText( res.getString( R.string.btn_line ) );
        break;
      case SketchDef.VIEW_3D:
        viewBtn.setText( res.getString( R.string.btn_3d ) );
        break;
      default:
        viewBtn.setText( res.getString( R.string.btn_view ) );
    }
  }

  private void alertMakeSurface( )
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.make_surface ) );
  
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doMakeSurfaceDialog( );
        }
      } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
          public void onClick( DialogInterface dialog, int btn ) { }
      } );
    alert.show();
  }

  private void alertMissingSymbols()
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

  // -------------------------------------------------------------------------
  // SYMBOL-CHOICE methods
  
  public void areaSelected( int k ) 
  {
    if ( k >= 0 && k < DrawingBrushPaths.mAreaLib.mAreaNr ) {
      mSymbol = SketchDef.SYMBOL_AREA;
      mCurrentArea = k;
    }
    setTheTitle();
  }

  public void lineSelected( int k ) 
  {
    if ( k >= 0 && k < DrawingBrushPaths.mLineLib.mLineNr ) {
      mSymbol = SketchDef.SYMBOL_LINE;
      mCurrentLine = k;
    }
    setTheTitle();
  }

  public void pointSelected( int p )
  {
    if ( p >= 0 && p < DrawingBrushPaths.mPointLib.mPointNr ) {
      mSymbol = SketchDef.SYMBOL_POINT;
      mCurrentPoint = p;
    }
    setTheTitle();
  }

  // ---------------------------------------------------------------
  // ZOOM button controls - and methods 

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    mZoomBtnsCtrl.setVisible( visible );
  }

  // private void rotateInfoBy( float angle ) 
  // { mInfo.azimuth += angle; }
  // private void tiltInfoBy( float clino )
  // {
  //   clino += mInfo.clino;
  //   if ( clino > 90 ) clino = 90;
  //   if ( clino < -90 ) clino = -90;
  //   mInfo.clino = clino;
  // }

  private void changeZoom( float f ) 
  {
    switch ( mView ) {
      case SketchDef.VIEW_TOP:
        mInfo.changeZoomtop( f ); // , mDisplayCenter );
        mModel.setTransform( mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top );
        break;
      case SketchDef.VIEW_SIDE:
        mInfo.changeZoomside( f ); // , mDisplayCenter );
        mModel.setTransform( mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side );
        break;
      case SketchDef.VIEW_3D:
        mInfo.changeZoom3d( f ); // , mDisplayCenter );
        mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
        break;
    }
  }

  /** set info offsets and zoom
   * and set the model transform (matrix) accordingly
   */
  private void resetZoom() 
  {
    switch ( mView ) {
      case SketchDef.VIEW_TOP:
        mInfo.resetZoomtop( app.mDisplayWidth/(10*app.mScaleFactor),
                       app.mDisplayHeight/(10*app.mScaleFactor),
                       10 * app.mScaleFactor );
        mModel.setTransform( mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top );
      case SketchDef.VIEW_SIDE:
        mInfo.resetZoomside( app.mDisplayWidth/(10*app.mScaleFactor),
                       app.mDisplayHeight/(10*app.mScaleFactor),
                       10 * app.mScaleFactor );
        mModel.setTransform( mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side );
      case SketchDef.VIEW_3D:
        mInfo.resetZoom3d( app.mDisplayWidth/(10*app.mScaleFactor),
                       app.mDisplayHeight/(10*app.mScaleFactor),
                       10 * app.mScaleFactor );
        mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
        break;
    }
  }

  public void zoomIn()  { changeZoom( SketchDef.ZOOM_INC ); }
  public void zoomOut() { changeZoom( SketchDef.ZOOM_DEC ); }
  public void zoomOne() { resetZoom( ); }

  @Override
  public void onZoom( boolean zoomin )
  {
    if ( zoomin ) {
      zoomIn();
    } else {
      zoomOut();
    }
  }

  // -----------------------------------------------------------------------------------
  // OUTPUT

  // void savePng()
  // {
  //   final Activity currentActivity  = this;
  //   Handler saveHandler = new Handler(){
  //        @Override
  //        public void handleMessage(Message msg) {
  //   //         final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
  //   //         alertDialog.setTitle("Saving sketch");
  //   //         alertDialog.setMessage("File: " + mFullName );
  //   //         alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
  //   //             public void onClick(DialogInterface dialog, int which) {
  //   //                 return;
  //   //             }
  //   //         });
  //   //         alertDialog.show();
  //        }
  //   } ;
  //   Bitmap bitmap = mSketchSurface.getBitmap();
  //   if ( bitmap == null ) {
  //     Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_LONG ).show();
  //   } else {
  //     new ExportBitmapToFile(this, saveHandler, mSketchSurface.getBitmap(), mFullName ).execute();
  //     Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".png", Toast.LENGTH_LONG ).show();
  //   }
  // }

  // private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> 
  // {
  //   private Context mContext;
  //   private Handler mHandler;
  //   private Bitmap mBitmap;
  //   private String mFullName;

  //   public ExportBitmapToFile( Context context, Handler handler, Bitmap bitmap, String name )
  //   {
  //      mContext  = context;
  //      mBitmap   = bitmap;
  //      mHandler  = handler;
  //      mFullName = name;
  //      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "ExportBitmapToFile " + mFullName );
  //   }

  //   @Override
  //   protected Boolean doInBackground(Intent... arg0)
  //   {
  //     try {
  //       String filename = app.getPngFileWithExt( mFullName );
  //       final FileOutputStream out = new FileOutputStream( filename );
  //       mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
  //       out.flush();
  //       out.close();
  //       return true;
  //     } catch (Exception e) {
  //       e.printStackTrace();
  //     }
  //     //mHandler.post(completeRunnable);
  //     return false;
  //   }


  //   @Override
  //   protected void onPostExecute(Boolean bool) {
  //       super.onPostExecute(bool);
  //       if ( bool ){
  //           mHandler.sendEmptyMessage(1);
  //       }
  //   }
  // }

  void saveTh3()
  {
    if ( doSaveTh3( ! mAllSymbols ) ) {
      Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_LONG ).show();
    }
  }

  private class SaveTh3File extends AsyncTask<Intent,Void,Boolean>
  {
    private Context mContext;
    private Handler mHandler;
    private SketchModel mModel;
    private String mFullName;

    public SaveTh3File( Context context, Handler handler, SketchModel model, String name )
    {
       mContext  = context;
       mHandler  = handler;
       mModel    = model;
       mFullName = name;
       // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "SaveTh3File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Intent... arg0)
    {
      try {
        String filename = app.getTh3FileWithExt( mFullName );
        FileWriter writer = new FileWriter( filename );
        BufferedWriter out = new BufferedWriter( writer );
        mModel.exportTherion( out, mFullName, app.projName[ (int)mType ] );
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

  boolean doSaveTh3( boolean not_all_symbols )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, " savingTh3 " + mFullName );
    if ( mFullName != null && mSketchSurface != null ) {
      if ( not_all_symbols ) {
        alertMissingSymbols();
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
        new SaveTh3File(this, saveHandler, mModel, mFullName ).execute();
      }
    }
    return false;
  }

  private class SaveDxfFile extends AsyncTask<Intent,Void,Boolean>
  {
      private Context mContext;
      private Handler mHandler;
      private SketchModel mModel;
      private String mFullName;

      public SaveDxfFile( Context context, Handler handler, SketchModel model, String name )
      {
         mContext  = context;
         mHandler  = handler;
         mModel    = model;
         mFullName = name;
         // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "SaveDxfFile " + mFullName );
      }

      @Override
      protected Boolean doInBackground(Intent... arg0)
      {
        try {
          String filename = app.getDxfFileWithExt( mFullName );
          FileWriter writer = new FileWriter( filename );
          PrintWriter out = new PrintWriter( writer );
          SketchDxf.write( out, mFullName, mModel );
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

  void doSaveDxf()
  {
    if ( mFullName != null && mSketchSurface != null ) {
      // if ( not_all_symbols ) {
      //   alertMissingSymbols();
      // }
      // if ( mAllSymbols ) {
        Handler saveHandler = new Handler(){
             @Override
             public void handleMessage(Message msg) { }
        } ;
        new SaveDxfFile(this, saveHandler, mModel, mFullName ).execute();
      // }
    }
  }

  // -----------------------------------------------------------------------------------
  // LIFECYCLE of the activity


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
    mPainter = new SketchPainter();

    setContentView(R.layout.sketch_activity);
    app = (TopoDroidApp)getApplication();
    // mInfo.zoom = app.mScaleFactor;    // canvas zoom

    // setCurrentPaint();
    mCurrentBrush = new DrawingPenBrush();
    mCurrentPoint = 0; // DrawingBrushPaths.POINT_LABEL;
    mCurrentLine  = 0; // DrawingBrushPaths.mLineLib.mLineWallIndex;
    mCurrentArea  = 0; // DrawingBrushPaths.AREA_WATER;

    mSketchSurface = (SketchDrawingSurface) findViewById(R.id.sketchSurface);
    mSketchSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
    mSketchSurface.previewPath.path = new Path();
    mSketchSurface.previewPath.setPaint( mPainter.previewPaint );
    mSketchSurface.setOnTouchListener(this);
    // mSketchSurface.setOnLongClickListener(this);
    // mSketchSurface.setBuiltInZoomControls(true);

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

    modeBtn = (Button) findViewById(R.id.modeBtn);
    // redoBtn = (Button) findViewById(R.id.redoBtn);
    undoBtn = (Button) findViewById(R.id.undoBtn);
    // zoomBtn = (Button) findViewById(R.id.zoomBtn);
    viewBtn    = (Button) findViewById(R.id.viewBtn);
    symbolBtn  = (Button) findViewById(R.id.symbolBtn);
    editBtn  = (Button) findViewById(R.id.editBtn);
    // stretchBtn  = (Button) findViewById(R.id.stretchBtn);
    // extrudeBtn  = (Button) findViewById(R.id.extrudeBtn);

    modeBtn.setOnLongClickListener(this);
    viewBtn.setOnLongClickListener(this);
    symbolBtn.setOnLongClickListener(this);

    // undoBtn.setAlpha( 0.5f );
    // zoomBtn.setAlpha( 0.5f );
    // modeBtn.setAlpha( 0.5f );

    // redoBtn.setEnabled(false);
    // undoBtn.setEnabled(false); // let undo always be there

    DrawingBrushPaths.makePaths( getResources() );

    mData        = app.mData; // new DataHelper( this ); 
    Bundle extras = getIntent().getExtras();
    mSid         = extras.getLong(   app.TOPODROID_SURVEY_ID );
    String name  = extras.getString( app.TOPODROID_SKETCH_NAME );
    mFullName    = app.getSurvey() + "-" + name;

    mInfo      = app.mData.getSketch3dInfo( mSid, name );
    mPid         = mInfo.id;
    mInfo.xcenter = app.mDisplayWidth/2;
    mInfo.ycenter = app.mDisplayHeight/2;

    List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_LONG ).show();
      finish();
    } else {
      prepareReferences( list );
      computeReferenceFrame( false );
      mAllSymbols  = true; // by default there are all the symbols

      // now try to load drawings from therion file
      String filename = app.getTh3FileWithExt( mFullName );
      MissingSymbols missingSymbols = new MissingSymbols();
      // mAllSymbols = mSketchSurface.loadTh3( filename, missingSymbols );
      mAllSymbols = mModel.loadTh3( filename, missingSymbols, mPainter );
      if ( ! mAllSymbols ) {
        String msg = missingSymbols.getMessage( getResources() );
        (new MissingDialog( this, msg )).show();
      }

      setSurfaceTransform( 0, 0 );
      // mSketchSurface.refresh();
    }
    setTheTitle();
    setSymbolButton();
  }

  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    mSketchSurface.isDrawing = true;
  }


  @Override
  protected synchronized void onPause() 
  { 
    if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
    mSketchSurface.isDrawing = false;
    mData.updateSketch( mPid, mSid, mInfo.st1, mInfo.st2, 
                        mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top, 
                        mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side, 
                        mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d, 
                        mInfo.east, mInfo.south, mInfo.vert,
                        mInfo.azimuth, mInfo.clino );
    // Toast.makeText( this, R.string.saving_wait, Toast.LENGTH_LONG ).show();
    if ( mAllSymbols ) {
      doSaveTh3( false ); // do not alert-dialog on mAllSymbols: in case do not save 
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

  // -------------------------------------------------------------------------------------
  //  ADD ITEMS

  @Override
  public void addLabel( String label, float x, float y )
  {
    if ( label != null && label.length() > 0 ) {
      mModel.setLabelToLastPoint( label );
    }
  }


  private void addStationName( NumStation st )
  {
    // mSketchSurface.addStation( st.name, st.e, st.s, st.v ); 
    SketchStationName stn = new SketchStationName( st.name, st.e, st.s, st.v );
    stn.mPaint = DrawingBrushPaths.fixedStationPaint;
    mModel.addFixedStation( stn );
  }

  // convert coordinates relative to base point (e,s,v) of the sketch info
  // x east rightward
  // y south downward
  // z vertical downward
  private void addFixed( float x1, float y1, float z1, 
                         float x2, float y2, float z2, 
                         DistoXDBlock blk, boolean splay, boolean is_reference )
  {
    x1 = Sketch3dInfo.mXScale * (x1 - mInfo.east);
    y1 = Sketch3dInfo.mXScale * (y1 - mInfo.south);
    x2 = Sketch3dInfo.mXScale * (x2 - mInfo.east);
    y2 = Sketch3dInfo.mXScale * (y2 - mInfo.south);
    z1 = Sketch3dInfo.mXScale * (z1 - mInfo.vert);
    z2 = Sketch3dInfo.mXScale * (z2 - mInfo.vert);
   
    SketchFixedPath path = null;
    if ( splay ) {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_SPLAY, blk, DrawingBrushPaths.fixedSplayPaint );
    } else {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_FIXED, blk,
                             is_reference? DrawingBrushPaths.highlightPaint : DrawingBrushPaths.fixedShotPaint );
    }
    if ( path != null ) {
      // path.setEndPoints( x1, y1, x2, y2 ); // scene coords
      path.set3dMidpoint( (x1+x2)/2, (y1+y2)/2, (z1+z2)/2 );
      path.addPoint( x1, y1, z1 );
      path.addPoint( x2, y2, z2 );
      path.compute2dMidpoint( mInfo, mView );
      mModel.addFixedPath( path );
    }
  }

  // ------------------------------------------------------------------------------------
  // FRAME and REFERENCE

  private void computeReferenceFrame( boolean set_origin )
  {
    // get the two stations
    // clear Reference Frame
    mSketchSurface.clearReferences();
    mModel.mView = mView;
    // mModel.mExtrudeLine = null;

    NumStation station1 = mNum.getStation( mInfo.st1 );
    NumStation station2 = mNum.getStation( mInfo.st2 );
    // NumShot    shot = mNum.getShot( mInfo.st1, mInfo.st2 );
    mInfo.setStations( station1, station2, set_origin, mView );
    // resetZoom();

    List<NumShot>  shots  = mNum.getShots();
    List<NumSplay> splays = mNum.getSplays();

    // float de = station2.e - station1.e;
    // float ds = station2.s - station1.s;
    // float dv = station2.v - station1.v;
    // float d  = FloatMath.sqrt( ds*ds + de*de );
    // mInfo.sin_alpha = de / d;
    // mInfo.cos_alpha = ds / d;
    // float d3 = FloatMath.sqrt( d*d + dv*dv );
    // mInfo.sin_gamma = dv / d3;
    // mInfo.cos_gamma = d / d3;
    // int extend = 1; // FIXME
    // mInfo.cos_alpha *= extend;
    // mInfo.sin_alpha *= extend;

    addStationName( station1 );
    addStationName( station2 );
    // List<NumStation> stations = mNum.getStations();
    // for ( NumStation station : stations ) {
    //   addStationName( station );
    // }

    for ( NumSplay splay : splays ) {
      if ( station1.equals(splay.from) ) {
        addFixed( station1.e, station1.s, station1.v, splay.e, splay.s, splay.v, splay.block, true, false );
      } else if ( station2.equals(splay.from) ) {
        addFixed( station2.e, station2.s, station2.v, splay.e, splay.s, splay.v, splay.block, true, false );
      }
    }
    for ( NumShot sh : shots ) {
      // addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, false );
      if ( sh.from.equals(station1) ) {
        if ( ! sh.to.equals(station2) ) {
          addStationName( sh.to );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, true );
        }
      } else if ( sh.from.equals(station2) ) {
        if ( ! sh.to.equals(station1) ) {
          addStationName( sh.to );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, true );
        }
      } else if ( sh.to.equals(station1) ) {
        if ( ! sh.from.equals(station2) ) {
          addStationName( sh.from );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, true );
        }
      } else if ( sh.to.equals(station2) ) {
        if ( ! sh.from.equals(station1) ) {
          addStationName( sh.from );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.block, false, true );
        }
      }
    }
  }

  private void prepareReferences( List<DistoXDBlock> list )
  {
    // mSketchSurface.clearReferences();
    mNum = new DistoXNum( list, mInfo.start );
    if ( (! mNum.surveyAttached) && app.mCheckAttached ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_LONG ).show();
    }
    mModel = new SketchModel( mInfo, mNum, mPainter );
    mSketchSurface.setModel( mModel );
  }

  void recreateNum( List<DistoXDBlock> list )
  {
    mNum = new DistoXNum( list, mInfo.start );
    mModel.setNum( mNum );
  }

  void updateNum( ArrayList<DistoXDBlock> list )
  {
    mNum.addData( list );
    computeReferenceFrame( false );
  }

  private void setSurfaceTransform( float x_shift, float y_shift )
  {
    switch ( mView ) {
      case SketchDef.VIEW_TOP:
        mInfo.shiftOffsettop( x_shift, y_shift );
        mModel.setTransform( mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top );
        break;
      case SketchDef.VIEW_SIDE:
        mInfo.shiftOffsetside( x_shift, y_shift );
        mModel.setTransform( mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side );
        break;
      case SketchDef.VIEW_3D:
        mInfo.shiftOffset3d( x_shift, y_shift );
        mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
        break;
    }
  }

  // -------------------------------------------------------------------------
  // SELECT methods
  
  // private void doSelectAt( float x_scene, float y_scene )
  // {
  //   // Log.v( "DistoX", "doSelectAt at " + x_scene + " " + y_scene );
  //   float d0 = TopoDroidApp.mCloseness;

  //   // SketchPointPath point = mSketchSurface.getPointAt( x_scene, y_scene );
  //   // if ( point != null ) {
  //   //   // new DrawingPointDialog( this, point ).show();
  //   //   return;
  //   // } 

  //   // SketchLinePath line = mSketchSurface.getLineAt( x_scene, y_scene );
  //   // if ( line != null ) {
  //   //   // new DrawingLineDialog( this, line ).show();
  //   //   return;
  //   // }
  //   // 
  //   // SketchAreaPath area = mSketchSurface.getAreaAt( x_scene, y_scene );
  //   // if ( area != null ) {
  //   //   // new DrawingAreaDialog( this, area ).show();
  //   //   return;
  //   // }
  // }

  private SketchFixedPath doSelectShotAt( float x_scene, float y_scene )
  {
    return mModel.selectShotAt( x_scene, y_scene );
  }

  private SketchStationName doSelectStationAt( float x_scene, float y_scene )
  {
    return mModel.selectStationAt( x_scene, y_scene );
  }

  private SketchLinePath doSelectPathAt( float x_scene, float y_scene )
  {
    LinePoint pt = new LinePoint( x_scene, y_scene );
    Vector v = ( mView == SketchDef.VIEW_TOP )? mInfo.topTo3d( pt )
             : ( mView == SketchDef.VIEW_SIDE )? mInfo.sideTo3d( pt )
             : null;
    return mModel.selectLineAt( v.x, v.y, v.z, mView );
  }


  private SketchTriangle doSelectTriangleAt( float x_scene, float y_scene, SketchTriangle tri )
  {
    return mModel.selectTriangleAt( x_scene, y_scene, tri );
  }
    
  // --------------------------------------------------------------------------
  // DELETE

  // void deletePoint( DrawingPointPath point ) 
  // {
  //   mSketchSurface.deletePath( point );
  // }

  // void deleteLine( DrawingLinePath line ) 
  // {
  //   mSketchSurface.deletePath( line );
  // }

  // void deleteArea( DrawingAreaPath area ) 
  // {
  //   mSketchSurface.deletePath( area );
  // }

  // void refreshSurface()
  // {
  //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "refresh surface");
  //   mSketchSurface.refresh();
  // }

    
  // --------------------------------------------------------------------------
  // TOUCH

  // private void dumpEvent( WrapMotionEvent ev )
  // {
  //   String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
  //   StringBuilder sb = new StringBuilder();
  //   int action = ev.getAction();
  //   int actionCode = action & MotionEvent.ACTION_MASK;
  //   sb.append( "Event action_").append( name[actionCode] );
  //   if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
  //     sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
  //   }
  //   sb.append( " [" );
  //   for (int i=0; i<ev.getPointerCount(); ++i ) {
  //     sb.append( "#" ).append( i );
  //     sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
  //     if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
  //   }
  //   sb.append( "]" );
  //   // Log.v( TopoDroidApp.TAG, sb.toString() );
  // }
  

  private float spacing( WrapMotionEvent ev )
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return FloatMath.sqrt(x*x + y*y);
  }

  // private float direction( WrapMotionEvent ev )
  // {
  //   int np = ev.getPointerCount();
  //   if ( np < 2 ) return 0.0f;
  //   float x = ev.getX(1) - ev.getX(0);
  //   float y = ev.getY(1) - ev.getY(0);
  //   return (float)Math.atan2( y, x );
  // }

  // private float position( WrapMotionEvent ev ) // vertical position
  // {
  //   int np = ev.getPointerCount();
  //   if ( np == 0 ) return 0.0f;
  //   if ( np == 1 ) return ev.getY(0);
  //   return ( ev.getY(1) + ev.getY(0) ) / 2;
  // }

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "SketchActivity onTouch() " );
    // dumpEvent( event );

    float x_canvas = event.getX();
    float y_canvas = event.getY();
    if ( mIsNotMultitouch && y_canvas > app.mDisplayHeight-30 ) {
      mZoomBtnsCtrl.setVisible( true );
    }

    float x_scene = mInfo.canvasToSceneX( x_canvas, mView );
    float y_scene = mInfo.canvasToSceneY( y_canvas, mView );

    // Log.v("DistoX", "canvas pt " + x_canvas + " " + y_canvas + " scene " + x_scene + " " + y_scene );
    int action = event.getAction() & MotionEvent.ACTION_MASK;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      // if ( mMode != SketchDef.MODE_ROTATE ) 
      {
        mTouchMode = SketchDef.TOUCH_ZOOM;
        oldDist = spacing( event );
        // oldDir  = direction( event );
        // oldPos  = position( event );
      }
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      mTouchMode = SketchDef.TOUCH_MOVE;
      /* nothing */
    } else if (action == MotionEvent.ACTION_DOWN) {
      if ( mMode == SketchDef.MODE_DRAW ) {
        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
        mPointCnt = 0;
        if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
          mSketchSurface.isDrawing = true;
          mCurrentLinePath = new DrawingLinePath( mCurrentLine );
          mCurrentLinePath.addStartPoint( x_scene, y_scene );
          mCurrentBrush.mouseDown( mSketchSurface.previewPath.path, x_canvas, y_canvas );
        } else { // SketchDef.SYMBOL_POINT
          mSaveX = x_canvas;
          mSaveY = y_canvas;
        }
      } else if ( mMode == SketchDef.MODE_STEP ) {
        SketchFixedPath path = null;
        SketchLinePath path2 = null;
        if ( mView == SketchDef.VIEW_TOP || mView == SketchDef.VIEW_SIDE ) {
          path2 = doSelectPathAt( x_scene, y_scene );
        }
        if ( path2 != null ) { // ask delete dialog
          path2.mPaint = mPainter.bluePaint;
          askDeleteLine( path2 );
          path2.mPaint = ( mView == SketchDef.VIEW_TOP )? mPainter.topLinePaint
                                                        : mPainter.sideLinePaint;
        } else { // try to find leg shot 
          path = doSelectShotAt( x_scene, y_scene ); 
          if ( path == null ) {
            Toast.makeText( this, R.string.no_shot_found, Toast.LENGTH_SHORT ).show();
          } else {
            DistoXDBlock blk = path.mBlock;
            if ( blk != null ) {
              // float a = mInfo.azimuth;
              // float c = mInfo.clino;
              // float x = mInfo.xoffset_3d;
              // float y = mInfo.yoffset_3d;
              // float z = mInfo.zoom_3d;

              mInfo.st1 = blk.mFrom;
              mInfo.st2 = blk.mTo;
              computeReferenceFrame( false );

              mMode = SketchDef.MODE_MOVE;
              setTheTitle();

              // mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
              // resetZoom();
              // mInfo.shiftOffset3d( x, y );
              // mInfo.resetZoom3d( x, y, z );
              // mInfo.rotateBy3d( a, c );
            }
          }
        }
      } else if ( mMode == SketchDef.MODE_SHOT ) {
        SketchStationName st = doSelectStationAt( x_scene, y_scene ); 
        if ( st == null ) {
          Toast.makeText( this, R.string.no_station_found, Toast.LENGTH_SHORT ).show();
        } else {
          new SketchNewShotDialog( this, this, app, st.mName ).show();
        }
      } else if ( mMode == SketchDef.MODE_MOVE /* || mMode == SketchDef.MODE_ROTATE */ ) {
        // setTitle( R.string.title_move );
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        // mStartX = x_canvas;
        // mStartY = y_canvas;
        return false;
      }
    } else if (action == MotionEvent.ACTION_MOVE) {
      if ( mTouchMode == SketchDef.TOUCH_MOVE) {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( mMode == SketchDef.MODE_DRAW ) {
          if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
            if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > app.mLineSegment ) {
              mSketchSurface.isDrawing = true;
              if ( ++mPointCnt % app.mLineType == 0 ) {
                mCurrentLinePath.addPoint( x_scene, y_scene );
              }
              mCurrentBrush.mouseMove( mSketchSurface.previewPath.path, x_canvas, y_canvas );
            }
          }
        } else if ( mMode == SketchDef.MODE_MOVE ) {
          if ( mView == SketchDef.VIEW_TOP || mView == SketchDef.VIEW_SIDE ) {
            if ( mIsNotMultitouch || (Math.abs(x_shift) + Math.abs(y_shift)) < 200 ) { 
              setSurfaceTransform( x_shift, y_shift );
            } else {
              x_canvas = mSaveX;
              y_canvas = mSaveY;
            }
          } else if ( mView == SketchDef.VIEW_3D ) { // mMode == SketchDef.MODE_ROTATE
            if ( Math.abs( x_shift ) < 20 && Math.abs( y_shift ) < 20 ) {
              mInfo.rotateBy3d( x_shift/4, y_shift/4 );
              setTheTitle();
              mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
            }
          }
        } else { // mMode == SketchDef.MODE_STEP
          // nothing
        }
        mSaveX = x_canvas;                 // reset start
        mSaveY = y_canvas;
      } else { // mTouchMode == SketchDef.TOUCH_ZOOM
        float newDist = spacing( event );
        if ( newDist > 16.0f && oldDist > 16.0f ) {
          float factor = newDist/oldDist;
          // float newDir = direction( event );
          // float newPos = position( event );
          if ( factor > 0.05f && factor < 4.0f ) {
            changeZoom( factor );
            oldDist = newDist;
          } 
          if ( Math.abs(factor-1.0f) <= 0.05f ) { // move
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
            if ( (Math.abs(x_shift) + Math.abs(y_shift)) < 200 ) { 
              setSurfaceTransform( x_shift, y_shift );
              mSaveX = x_canvas;
              mSaveY = y_canvas;
            }
          }
          // if ( mView == SketchDef.VIEW_3D ) {
          //   float angle = newDir - oldDir;
          //   float clino = newPos - oldPos;
          //   if ( angle > 0.05f || angle < 0.05f || clino > 0.05f || clino < 0.05f ) {
          //     mInfo.rotateBy( angle, clino );
          //     oldDir = newDir;
          //     oldPos = newPos;
          //   }
          // }
        }
      }
    } else if (action == MotionEvent.ACTION_UP) {
      if ( mTouchMode == SketchDef.TOUCH_ZOOM ) {
        mTouchMode = SketchDef.TOUCH_MOVE;
      } else {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( mMode == SketchDef.MODE_DRAW ) {
          if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
            mCurrentBrush.mouseUp( mSketchSurface.previewPath.path, x_canvas, y_canvas );
            mSketchSurface.previewPath.path = new Path();
            if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > app.mLineSegment || (mPointCnt % app.mLineType) > 0 ) {
              // Log.v("DistoX", "add last line point " + x_scene + " " + y_scene );
              mCurrentLinePath.addPoint( x_scene, y_scene );
            }
            if ( mPointCnt > app.mLineType ) {
              SketchLinePath line = null;
              if ( mSymbol == SketchDef.SYMBOL_LINE ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, mCurrentLine, mView, mInfo.st1, mInfo.st2, mPainter );
              } else if ( mSymbol == SketchDef.SYMBOL_AREA ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_AREA, mCurrentArea, mView, mInfo.st1, mInfo.st2, mPainter );
              }
              ArrayList< LinePoint > pts = mCurrentLinePath.points; 
              SketchTriangle tri = null;
              int np = pts.size();
              LinePoint p1 = pts.get(0);
              LinePoint p2 = pts.get(np-1);
              if ( mView == SketchDef.VIEW_TOP ) { // project on the reference segment and get its Z FIXME_TOP
                if ( mInfo.projTop( p1 ) > mInfo.projTop( p2 ) ) {
                  for ( ; np>0; ) {
                    --np;
                    Vector v = mInfo.topTo3d( pts.get(np) );
                    line.addLinePoint( v.x, v.y, v.z );
                  }
                } else {
                  for (LinePoint p : pts ) {
                    Vector v = mInfo.topTo3d( p );
                    line.addLinePoint( v.x, v.y, v.z );
                  }
                }
              } else if ( mView == SketchDef.VIEW_SIDE ) {
                if ( p1.mX > p2.mX ) {
                  for ( ; np>0; ) {
                    --np;
                    Vector v = mInfo.sideTo3d( pts.get(np) );
                    line.addLinePoint( v.x, v.y, v.z );
                  }
                } else {
                  for (LinePoint p : pts ) {
                    Vector v = mInfo.sideTo3d( p );
                    line.addLinePoint( v.x, v.y, v.z );
                  }
                }
              } else if ( mView == SketchDef.VIEW_3D ) {
                if ( mEdit == SketchDef.EDIT_NONE ) {
                  for (LinePoint p : pts ) {
                    // find point on the triangulated surface and add it to the line
                    tri = doSelectTriangleAt( p.mX, p.mY, tri );
                    if ( tri != null /* && mInfo.isForward( tri ) */ ) {
                      Vector q1 = tri.get3dPoint( p.mX, p.mY );
                      line.addLinePoint( mInfo.east + q1.x, mInfo.south + q1.y, mInfo.vert + q1.z );
                    }
                  }                  
                } else if ( mEdit == SketchDef.EDIT_EXTRUDE ) {
                  Vector v1 = mInfo.projTo3d( pts.get(0) );
                  for (LinePoint p : pts ) {
                    Vector v = mInfo.projTo3d( p );
                    line.addLinePoint( v.x-v1.x, v.y-v1.y, v.z-v1.z );
                  }
                }
              } else {
                  // FIXME
              }

              if ( mView == SketchDef.VIEW_TOP ) { 
              } else if ( mView == SketchDef.VIEW_SIDE ) {
              } else if ( mView == SketchDef.VIEW_3D && mEdit == SketchDef.EDIT_NONE ) {
                if ( mSymbol == SketchDef.SYMBOL_LINE ) {
                  p1 = pts.get(0);
                  p2 = pts.get(pts.size()-1);
                  float len = FloatMath.sqrt( (p2.mX-p1.mX)*(p2.mX-p1.mX) + (p2.mY-p1.mY)*(p2.mY-p1.mY) );
                  if ( len < SketchDef.CLOSE_GAP ) {
                    line.close();
                  }
                } else if ( mSymbol == SketchDef.SYMBOL_AREA ) {
                  // Log.v("DistoX", "add area type " + mCurrentArea );
                  line.close();
                }
              }

              if ( line.mViewType == SketchDef.VIEW_TOP ) {
                line.make3dPoints( 1.0f, false, null, null ); // 1.1f/(mInfo.cos_gamma+0.1f) ); // FIXME VERTICAL
              } else if ( line.mViewType == SketchDef.VIEW_SIDE ) {
                line.make3dPoints( 1.0f, false, null, null );
              }

              if ( mEdit == SketchDef.EDIT_NONE ) { // line is a path to add to the model
                mModel.addSketchPath( line );
              } else if ( mEdit == SketchDef.EDIT_CUT ) { // line is the path along which to cut the new surface
                //
              } else if ( mEdit == SketchDef.EDIT_EXTRUDE ) { // line is the path along which to extrude the new surface
                // if ( mView == SketchDef.VIEW_TOP || mView == SketchDef.VIEW_SIDE ) 
                if ( mView == SketchDef.VIEW_3D ) {
                  mModel.extrudeSketchPath( line );
                }
                mView = SketchDef.VIEW_3D; // mSaveView;
              } else if ( mEdit == SketchDef.EDIT_STRETCH ) {
                if ( mView == SketchDef.VIEW_3D ) {
                  mModel.stretchSketchPath( pts );
                }
                mView = SketchDef.VIEW_3D; // mSaveView;
              }
              mEdit = SketchDef.EDIT_NONE;
              // undoBtn.setEnabled(true);
              // redoBtn.setEnabled(false);
              // canRedo = false;
            }
          } else { // SketchDef.SYMBOL_POINT
            if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
              // Log.v("DistoX", "point get triangle at " + x_scene + " " + y_scene );
              SketchTriangle tri = doSelectTriangleAt( x_scene, y_scene, null );
              if ( tri != null && mInfo.isForward( tri ) ) {
                Vector p = tri.get3dPoint( x_scene, y_scene );
                // Log.v("DistoX", "new point " + mCurrentPoint + " at " + p.x + " " + p.y + " " + p.z );
                SketchPointPath path = new SketchPointPath( mCurrentPoint, mInfo.st1, mInfo.st2, p.x, p.y, p.z );
                SymbolPointLibrary point_lib = DrawingBrushPaths.mPointLib;
                if ( point_lib.canRotate(mCurrentPoint) ) {
                  float angle = (float)( point_lib.getPointOrientation( mCurrentPoint ) );
                  // Log.v("DistoX", "point " + mCurrentPoint + " angle " + angle );
                  angle *= (float)(Math.PI/180.0);
                  // angles 0:upward 90;rightward 180:downward 270:leftward
                  // scene: x is rightward, y downward
                  // p1 is the 3D point of the orientation (from p to p1)
                  Vector p1 = tri.get3dPoint( x_scene + 0.1f * FloatMath.sin(angle),
                                              y_scene - 0.1f * FloatMath.cos(angle) );
                  path.setOrientation( p1, mInfo );
                }
                mModel.addPoint( path );
                if ( point_lib.pointHasText( mCurrentPoint ) ) {
                  // TODO text dialog
                  new DrawingLabelDialog( this, this, x_scene, y_scene ).show();
                }
              }
            }
          }
        } else { // SketchDef.MODE_MOVE 
          // if ( Math.abs( x_canvas - mStartX ) < 16 && Math.abs( y_canvas - mStartY ) < 16 ) {
          //   return false; // long click
          // }
          /* nothing */
        }
      }
    }
    return true;
  }


  private void askDeleteLine( final SketchLinePath line )
  {
    Resources res = getResources();
    // SketchLinePath mLine = line;
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( res.getString( R.string.line_delete ) + " ?" );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDeleteLine( line );
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { 
        }
    } );
    alert.show();
  }

  void doDeleteLine( SketchLinePath line )
  {
    mModel.deleteLine( line );
  }

  // -----------------------------------------------------------------------------
  // CLICK

    public void onClick(View view)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "SketchActivity onClick() " + view.toString() );
      switch (view.getId()){
        case R.id.undoBtn:
          mModel.undo();
          // if( mSketchSurface.hasMoreUndo() == false ){
          //   undoBtn.setEnabled( false );
          // }
          // redoBtn.setEnabled( true );
          // canRedo = true;
          break;

        // case R.id.zoomBtn:
        //   zoomView( );
        //   break;
        case R.id.viewBtn:
          if ( mView == SketchDef.VIEW_TOP ) {
            mView = SketchDef.VIEW_SIDE;
          } else { // SketchDef.VIEW_SIDE SketchDef.VIEW_3D SketchDef.VIEW_CROSS
            mView = SketchDef.VIEW_TOP;
          }
          mSymbol = SketchDef.SYMBOL_LINE; // 2d can draw only wall-lines
          mCurrentLine = 0;
          mMode = SketchDef.MODE_MOVE;
          mTouchMode = SketchDef.TOUCH_MOVE;
          setTheTitle();
          computeReferenceFrame( false );
          setSurfaceTransform( 0, 0 );
          break;
        case R.id.symbolBtn:
          if ( mSymbol == SketchDef.SYMBOL_POINT ) {
            new DrawingPointPickerDialog(this, this, mCurrentPoint).show();
          } else if (  mSymbol == SketchDef.SYMBOL_AREA ) {
            new DrawingAreaPickerDialog(this, this, mCurrentArea).show();
          } else if (  mSymbol == SketchDef.SYMBOL_LINE ) {
            mCurrentLine = 0; // line wall
          }
          setTheTitle();
          break;
        case R.id.editBtn:
          if ( mView == SketchDef.VIEW_3D ) {
            new SketchEditDialog( this, this ).show();
          }
          break;
        case R.id.modeBtn:
          if ( mMode == SketchDef.MODE_DRAW ||  mMode == SketchDef.MODE_STEP ) { 
            mMode = SketchDef.MODE_MOVE;
          } else {
            mMode = SketchDef.MODE_DRAW;
          }
          // mSketchSurface.clearHighlight();
          setTheTitle();
          break;
      }
    }

    private void switchToView3D()
    {
      mView = SketchDef.VIEW_3D;
      mMode = SketchDef.MODE_MOVE;
      mTouchMode = SketchDef.TOUCH_MOVE;
      computeReferenceFrame( false );
      mInfo.rotateBy3d( 180, 0 );
      // mSketchSurface.refresh();
    }

    public boolean onLongClick( View view )
    {
      if ( (Button)view == modeBtn ) {
        if ( mMode == SketchDef.MODE_MOVE ) {
          mMode = SketchDef.MODE_STEP;
        } else if ( mMode == SketchDef.MODE_STEP ) {
          mMode = SketchDef.MODE_SHOT;
        }
      } else if ( (Button)view == viewBtn ) {
        switchToView3D();
      } else if ( (Button)view == symbolBtn ) {
        if ( mSymbol == SketchDef.SYMBOL_POINT ) {
          mSymbol = SketchDef.SYMBOL_AREA;
        } else if (  mSymbol == SketchDef.SYMBOL_AREA ) {
          mSymbol = SketchDef.SYMBOL_LINE;
        } else if (  mSymbol == SketchDef.SYMBOL_LINE ) {
          mSymbol = SketchDef.SYMBOL_POINT;
        }
        setSymbolButton();
      }
      setTheTitle();
      return true;
    }

    // ----------------------------------------------
    // options

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      // mMIedit    = menu.add( R.string.menu_edit );
      mMIone     = menu.add( R.string.menu_one );
      mMIredo    = menu.add( R.string.menu_redo );
      mMIsurface = menu.add( R.string.menu_surface );
      mMIdownload = menu.add( R.string.menu_download );
      mMInotes   = menu.add( R.string.menu_notes );
      mSMmore    = menu.addSubMenu( R.string.menu_more );
      // mMIone     = mSMmore.add( R.string.menu_one );
      mMIdisplay = mSMmore.add( R.string.menu_display );
      mMIstats   = mSMmore.add( R.string.menu_stats );
      mMIsave    = mSMmore.add( R.string.menu_save_th2 );
      mMIdelete  = mSMmore.add( R.string.menu_delete );
      // mMIzoom    = mSMmore.add( R.string.menu_zoom );
      // mMIsymbol  = mSMmore.add( R.string.menu_symbol );
      mMIoptions = mSMmore.add( R.string.menu_options );
      // mMIhelp    = mSMmore.add( R.string.menu_help );

      mMIone.setIcon( R.drawable.location );
      // mMIedit.setIcon( R.drawable.save );
      mMIdownload.setIcon( R.drawable.download );
      mMIredo.setIcon( R.drawable.redo );
      // mMIsave.setIcon( R.drawable.save );
      // mMIone.setIcon( R.drawable.zoomone );
      mMIsurface.setIcon( R.drawable.zoomone );
      // mMIstats.setIcon( R.drawable.info );
      mMInotes.setIcon( R.drawable.compose );
      mSMmore.setIcon( R.drawable.more );
      // mMIdisplay.setIcon( R.drawable.display );
      // mMIdelete.setIcon( R.drawable.delete );
      // mMIoptions.setIcon( R.drawable.prefs );
      // mMIhelp.setIcon( R.drawable.help );

      if ( mNum == null ) mMIstats.setEnabled( false );
      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "SketchActivity onOptionsItemSelected() " + item.toString() );

      if ( item == mMIredo ) {
        Toast.makeText( this, "Redo not implemented", Toast.LENGTH_SHORT ).show();
        // FIXME TODO
        // if ( mSketchSurface.hasMoreRedo() ) {
        //   mSketchSurface.redo();
        // }
      } else if ( item == mMInotes ) {
        (new DistoXAnnotations( this, mData.getSurveyFromId(mSid) )).show();
        // String survey = mData.getSurveyFromId(mSid);
        // Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        // notesIntent.putExtra( app.TOPODROID_SURVEY, survey );
        // startActivity( notesIntent );
      } else if (item == mMIstats && mNum != null ) {
        new DistoXStatDialog( mSketchSurface.getContext(), mNum ).show();
      } else if (item == mMIone ) {
        mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
        resetZoom();
      } else if (item == mMIdisplay ) {
        mModel.mDisplayMode ++;
        if ( mModel.mDisplayMode == SketchDef.DISPLAY_MAX ) mModel.mDisplayMode = 0; // SketchDef.DISPLAY_NGBH;
      // } else if (item == mMIzoom ) {
        // new DrawingZoomDialog( mSketchSurface.getContext(), this ).show();
      } else if (item == mMIsurface ) {
        SketchSurface surface = mModel.getSurface();
        if ( surface != null ) {
          alertMakeSurface( );
        } else {
          doMakeSurfaceDialog( );
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
      } else if ( item == mMIdownload ) {
        if ( app.mDevice != null && app.mDevice.length() > 0 ) {
          // TODO if there is an empty shot use it, else try to download the data
          //      with the Asynch task that download the data.
          //      if there is an empty shot assign it
          setTitleColor( TopoDroidApp.COLOR_CONNECTED );
          new DistoXRefresh( app, this ).execute();
        } else {
          Toast.makeText( this, R.string.device_none, Toast.LENGTH_LONG ).show();
        }
      // } else if (item == mMIedit ) {
      //   if ( mView == SketchDef.VIEW_3D ) {
      //     new SketchEditDialog( this, this ).show();
      //   }
      } else if (item == mMIsave ) {
        new SketchSaveDialog( this, this ).show();
        // saveTh3( );
      }
      return true;
    }

  // ------------------------------------------------------------------------
  // HIGHLIGHT - EXTRUSION

  void cutRegion()
  {
    mModel.cutLineRegion( mCurrentLinePath );
  }

  void stretchRegion()
  {
    mModel.stretchRegion( this, mCurrentLinePath );
  }

  void highlightRegion()
  {
    mModel.highlightLineRegion( );
  }

  void extrudeRegion()
  {
    mModel.extrudeLineRegion( this, mCurrentLinePath );
  }

  // extrusion callback from SketchModel
  public void doExtrudeLineRegion( )
  {
    // mSaveView = mView; // mSaveView == VIEW_3D
    mView   = SketchDef.VIEW_3D;
    mEdit   = SketchDef.EDIT_EXTRUDE;
    mSymbol = SketchDef.SYMBOL_LINE; // 2d can draw only wall-lines
    mCurrentLine = 0;
    mMode = SketchDef.MODE_MOVE;
    mTouchMode = SketchDef.TOUCH_MOVE;
    setTheTitle();
    computeReferenceFrame( false );
    setSurfaceTransform( 0, 0 );
  }

  public void doStretchLineRegion( int view )
  {
    // Log.v("DistoX", "SketchActivity::doStretchLineRegion" );
    // mSaveView = mView; // mSaveView == VIEW_3D
    mView   = view;
    mEdit   = SketchDef.EDIT_STRETCH;
    mSymbol = SketchDef.SYMBOL_LINE; // 2d can draw only wall-lines
    mCurrentLine = 0;
    mView = SketchDef.VIEW_3D;
    mMode = SketchDef.MODE_DRAW;
    mTouchMode = SketchDef.TOUCH_MOVE;
    setTheTitle();
    // computeReferenceFrame( false );
    // setSurfaceTransform( 0, 0 );
  }

  public void doCutLineRegion( int view )
  {
    // mSaveView = mView; // mSaveView == VIEW_3D
    mView   = view;
    mEdit   = SketchDef.EDIT_CUT;
    mSymbol = SketchDef.SYMBOL_LINE; // 2d can draw only wall-lines
    mCurrentLine = 0;
    mMode = SketchDef.MODE_DRAW;
    mTouchMode = SketchDef.TOUCH_MOVE;
    setTheTitle();
    // computeReferenceFrame( false );
    // setSurfaceTransform( 0, 0 );
  }
 
  

  // ------------------------------------------------------------------------------
  // MAKE SURFACE
  
  /** (x,y) quadrant ( 0:TL 1:BL 2:BR 3:TR )
   *         0  |  3
   *        ----+----
   *         1  |  2
   */
  int getQuad( float x, float y )
  {
    if ( x < 0 ) {
      return ( y < 0 )? 0 : 1;
    } 
    return ( y > 0 )? 2 : 3;
  }

  /** quadrants start point
   * @param q    quadrant
   * @param xc   center X coord
   * @param yc   center Y coord
   * @param r    radius
   */
  PointF getQuadStart( int q, float xc, float yc, float r )
  {
    switch ( q ) {
      case 0: return new PointF( xc,   yc-r ); // Top Left
      case 1: return new PointF( xc-r, yc   ); // Bottom Left
      case 2: return new PointF( xc,   yc+r ); // Bottom Right
      case 3: return new PointF( xc+r, yc   ); // Top Right
    }
    return null;
  }
  
  /**
   * @param xc   center X coord
   * @param yc   center Y coord
   * @param r    radius
   * @param path shape path
   * @param vertical whether the shot is vertical
   */
  public boolean doMakeSurface( float xc, float yc, float r, SketchShapePath path, boolean vertical )
  {
    boolean ret = true;
    SketchShapePoints[] pts = new SketchShapePoints[4];
    for (int k=0; k<4; ++k ) pts[k] = new SketchShapePoints();
    float r_sqrt2 = r * FloatMath.sqrt( 2.0f ); // half-diagonal

    PointF p0 = path.mPts.get(0);
    float x = (p0.x - xc);
    float y = (p0.y - yc);
    int q0 = getQuad( x, y );
    p0 = getQuadStart( q0, xc, yc, r ); // first quadrant start-point
    for ( int k = 0; k < path.mPts.size(); ++ k ) {
      PointF p1 = path.mPts.get( k );
      x = (p1.x - xc)/(r_sqrt2);
      y = (p1.y - yc)/(r_sqrt2);
      int q1 = getQuad( x, y );
      if ( q1 == (q0+1)%4 ) { // next quadrant
        q0 = q1;
        p0 = getQuadStart( q0, xc, yc, r );
      } else if ( q1 == q0 ) {
        float x1 = p1.x - p0.x;
        float y1 = p1.y - p0.y;
        if ( FloatMath.sqrt( x1*x1 + y1*y1 ) > SketchDef.SHAPE_STEP ) { 
          float a=0;
          switch ( q1 ) {
            case 0: // top-left
              a = 1 - (-x) / ( - x - y );
              break;
            case 1: // bottom-left
              a = 1 - y / ( - x + y );
              break;
            case 2: // bottom-right
              a = 1 - x / ( x + y );
              break;
            case 3: // top-right
              a = 1 - (-y) / ( x - y );
              break;
          }
          if ( a > 0.1f && a < 0.9f ) {
            float b = Math.abs( 0.5f - a );
            float rr = FloatMath.sqrt( x * x + y * y ); 
            rr = (b + rr * (1 - b)) * ( 1.25f - (a-0.5f)*(a-0.5f));
            pts[(q1+1)%4].add( new PointF(a,rr) );
          }
          p0 = p1;
        }
      } else {
        ret = false;
        break;
      }
    }
    if ( ret ) {
      mModel.makeSurface( pts, vertical );
      switchToView3D();
    }
    return ret;
  }

  public void removeSurface()
  {
    mModel.removeSurface();
  }

  public void doMakeSurfaceDialog( )
  {
    // new SketchSurfaceDialog( mSketchSurface.getContext(), this ).show();
    mModel.computeShapeSize();
    new SketchShape( mSketchSurface.getContext(), mModel.mShapeSize, this ).show();
  }

  // -----------------------------------------------------------------------------

  @Override
  public void refreshDisplay( int nr ) 
  {
    setTitleColor( TopoDroidApp.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        recreateNum( list );
      }
      Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_LONG ).show();
    } else if ( nr < 0 ) {
      Toast.makeText( this, app.DistoXConnectionError[ -nr ], Toast.LENGTH_LONG ).show();
    }
  }
   
}
