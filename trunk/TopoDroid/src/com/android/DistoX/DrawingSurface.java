/* @file DrawingSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;

/**
 */
public class DrawingSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{
    private static final String TAG = "DistoX DrawingSurface";

    private Boolean _run;
    protected DrawThread thread;
    private Bitmap mBitmap;
    public boolean isDrawing = true;
    public DrawingPath previewPath;
    private SurfaceHolder mHolder; // canvas holder
    private Context mContext;
    private AttributeSet mAttrs;
    private int mWidth;            // canvas width
    private int mHeight;           // canvas height

    private DrawingCommandManager commandManager;

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    public DrawingSurface(Context context, AttributeSet attrs) 
    {
      super(context, attrs);
      mWidth = 0;
      mHeight = 0;

      thread = null;
      mContext = context;
      mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      commandManager = new DrawingCommandManager();
    }

    // public void clearHighlight()
    // {
    //   commandManager.clearHighlight();
    // }

    // public DistoXDBlock highlight( int plot_type, float x, float y )
    // {
    //   return commandManager.setHighlight( plot_type, x, y);
    // }

    public void setDisplayMode( int mode ) { commandManager.setDisplayMode(mode); }

    public int getDisplayMode( ) { return commandManager.getDisplayMode(); }

    public void setTransform( float dx, float dy, float s )
    {
      commandManager.setTransform( dx, dy, s );
    }


    private Handler previewDoneHandler = new Handler()
    {
      @Override
      public void handleMessage(Message msg) {
        isDrawing = false;
      }
    };

    class DrawThread extends  Thread
    {
      private SurfaceHolder mSurfaceHolder;

      public DrawThread(SurfaceHolder surfaceHolder)
      {
          mSurfaceHolder = surfaceHolder;
      }

      public void setRunning(boolean run)
      {
        _run = run;
      }

      @Override
      public void run() 
      {
        Canvas canvas = null;
        while ( _run ) {
          if ( isDrawing == true ) {
            try{
              canvas = mSurfaceHolder.lockCanvas(null);
              if(mBitmap == null){
                mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
              }
              final Canvas c = new Canvas (mBitmap);
              mWidth  = c.getWidth();
              mHeight = c.getHeight();

              c.drawColor(0, PorterDuff.Mode.CLEAR);
              canvas.drawColor(0, PorterDuff.Mode.CLEAR);

              commandManager.executeAll(c,previewDoneHandler);
              previewPath.draw(c);
                
              canvas.drawBitmap (mBitmap, 0,  0,null);
            } finally {
              mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
          }
        }
      }
    }

    public void addStation( String name, float x, float y )
    {
      // Log.v( TAG, "addStation " + name + " " + x + " " + y );
      DrawingStation st = new DrawingStation(name, x, y );
      st.setPaint( DrawingBrushPaths.fixedStationPaint );
      commandManager.addStation( st );
    }

    public void addFixedPath( DrawingPath path )
    {
      commandManager.addFixed( path );
    }

    public void addGridPath( DrawingPath path )
    {
      commandManager.addGrid( path );
    }

    public void addDrawingPath (DrawingPath drawingPath)
    {
      commandManager.addCommand(drawingPath);
    }

    public boolean hasMoreRedo()
    {
      return commandManager.hasMoreRedo();
    }

    public void redo()
    {
      isDrawing = true;
      commandManager.redo();
    }

    public void undo()
    {
      isDrawing = true;
      commandManager.undo();
    }

    public boolean hasMoreUndo()
    {
      return commandManager.hasMoreUndo();
    }

    public Bitmap getBitmap()
    {
      return commandManager.getBitmap();
    }


    public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
    {
      // Log.v( TAG, "surfaceChanged " );
      // TODO Auto-generated method stub
      mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
    }


    public void surfaceCreated(SurfaceHolder mHolder) 
    {
      // Log.v( TAG, "surfaceCreated " );
      // TODO Auto-generated method stub
      if (thread == null ) {
        thread = new DrawThread(mHolder);
      }
      thread.setRunning(true);
      thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder mHolder) 
    {
      // Log.v( TAG, "surfaceDestroyed " );
      // TODO Auto-generated method stub
      boolean retry = true;
      thread.setRunning(false);
      while (retry) {
        try {
          thread.join();
          retry = false;
        } catch (InterruptedException e) {
          // we will try it again and again...
        }
      }
      thread = null;
    }

    public void exportTherion( BufferedWriter out, String scrap_name, String plot_name )
    {
      commandManager.exportTherion( out, scrap_name, plot_name );
    }

  private String readLine( BufferedReader br )
  {
    String line = null;
    try {
      line = br.readLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    if ( line != null ) {
      line = line.trim();
      line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  public void loadTherion( String filename )
  {
    float x, y, x1, y1, x2, y2;
    // Log.v( TAG, "loadTherion file " + filename );
    DrawingBrushPaths.makePaths( );
    DrawingBrushPaths.resetPointOrientations();
    // Log.v( TAG, "after reset 0: " + DrawingBrushPaths.mOrientation[0]
    //                      + " 7: " + DrawingBrushPaths.mOrientation[7] );
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = null;
      while ( (line = readLine(br)) != null ) {
        if ( line.length() == 0 || line.charAt(0) == '#' ) {
          continue;
        }
        // Log.v( TAG, "  line: >>" + line + "<<");
        String[] vals = line.split( " " );
        if ( vals[0].equals( "point" ) ) {            // ****** THERION POINT
          int ptType = DrawingBrushPaths.POINT_MAX;
          boolean has_orientation = false;
          float orientation = 0.0f;
          x =   Float.parseFloat( vals[1] ) / TopoDroidApp.TO_THERION;
          y = - Float.parseFloat( vals[2] ) / TopoDroidApp.TO_THERION;
          String type = vals[3];
          String label_text = null;
          if ( type.equals( "station" ) ) continue;
          if ( vals.length > 4 ) {
            if ( vals[4].equals( "-orientation" ) ) {
              has_orientation = true;
              orientation = Float.parseFloat( vals[5] );
              // Log.v(TAG, "point orientation " + orientation );
            } else if ( vals[4].equals( "-text" ) ) {
              label_text = vals[5].replace( "\"", "" );
            }
          }
          if ( type.equals( "stalagmite" ) ) {
            ptType = DrawingBrushPaths.POINT_STAL;
            orientation = 180.0f;
            has_orientation = true;
          } else if ( type.equals( "stalactite" ) ) {
            ptType = DrawingBrushPaths.POINT_STAL;
            has_orientation = false;
          } else {
            for ( ptType = 0; ptType < DrawingBrushPaths.POINT_MAX; ++ptType ) {
              if ( type.equals( DrawingBrushPaths.pointName[ ptType ] ) ) break;
            }
          }
          if ( ptType == DrawingBrushPaths.POINT_MAX ) continue;
          if ( has_orientation ) {
            // Log.v( TAG, "[2] point " + ptType + " has orientation " + orientation );
            DrawingBrushPaths.rotateGrad( ptType, orientation );
            DrawingPointPath path = new DrawingPointPath( ptType, x, y );
            addDrawingPath( path );
            DrawingBrushPaths.rotateGrad( ptType, -orientation );
          } else {
            if ( ptType != DrawingBrushPaths.POINT_LABEL ) {
              DrawingPointPath path = new DrawingPointPath( ptType, x, y );
              addDrawingPath( path );
            } else {
              if ( label_text.equals( "!" ) ) {    // "danger" point
                DrawingPointPath path = new DrawingPointPath( DrawingBrushPaths.POINT_DANGER, x, y );
                addDrawingPath( path );
              } else {                             // regular label
                DrawingLabelPath path = new DrawingLabelPath( label_text, x, y );
                addDrawingPath( path );
              }
            }
          }
        } else if ( vals[0].equals( "line" ) ) {   // ********* THERION LINES
          if ( vals.length == 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
            // Log.v( TAG, "area id " + vals[3] );
            int arType = DrawingBrushPaths.AREA_MAX;
            DrawingAreaPath path = new DrawingAreaPath( arType, vals[3] );

            // TODO insert new area-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) { 
              String[] pt = line.split( "\\s+" );
              x =   Float.parseFloat( pt[0] ) / TopoDroidApp.TO_THERION;
              y = - Float.parseFloat( pt[1] ) / TopoDroidApp.TO_THERION;
              path.addStartPoint( x, y );
              while ( (line = readLine( br )) != null ) {
                if ( line.equals( "endline" ) ) {
                  line = readLine( br ); // area statement
                  String[] vals2 = line.split( " " );
                  for ( arType=0; arType < DrawingBrushPaths.AREA_MAX; ++arType ) {
                    if ( vals2[1].equals( DrawingBrushPaths.areaName[ arType ] ) ) break;
                  }
                  // Log.v(TAG, "set area type " + arType );
                  path.setAreaType( arType );
                  addDrawingPath( path );
                  line = readLine( br ); // skip two lines
                  line = readLine( br );
                  break;
                }
                // Log.v( TAG, "  line point: >>" + line + "<<");
                String[] pt2 = line.split( " " );
                if ( pt.length == 2 ) {
                  x  =   Float.parseFloat( pt2[0] ) / TopoDroidApp.TO_THERION;
                  y  = - Float.parseFloat( pt2[1] ) / TopoDroidApp.TO_THERION;
                  path.addPoint( x, y );
                } else if ( pt.length == 6 ) {
                  x1 =   Float.parseFloat( pt2[0] ) / TopoDroidApp.TO_THERION;
                  y1 = - Float.parseFloat( pt2[1] ) / TopoDroidApp.TO_THERION;
                  x2 =   Float.parseFloat( pt2[2] ) / TopoDroidApp.TO_THERION;
                  y2 = - Float.parseFloat( pt2[3] ) / TopoDroidApp.TO_THERION;
                  x  =   Float.parseFloat( pt2[4] ) / TopoDroidApp.TO_THERION;
                  y  = - Float.parseFloat( pt2[5] ) / TopoDroidApp.TO_THERION;
                  path.addPoint3( x1, y1, x2, y2, x, y );
                }
              }
            }
          } else { // ********* regular lines
            // Log.v( TAG, "line type " + vals[1] );
            boolean closed = false;
            String type = vals[1];
            if ( vals.length > 2 ) {
              if ( vals[2].equals( "-close" ) ) {
                if ( vals.length > 3 && vals[3].equals( "on" ) ) closed = true;
              }
            }
            int lnType = DrawingBrushPaths.LINE_MAX;
            DrawingLinePath path = null;
            for ( lnType=0; lnType < DrawingBrushPaths.LINE_MAX; ++lnType ) {
              if ( type.equals( DrawingBrushPaths.lineName[ lnType ] ) ) break;
            }
            // TODO insert new line-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) { 
              if ( lnType < DrawingBrushPaths.LINE_MAX ) {
                path = new DrawingLinePath( lnType );
                // Log.v( TAG, "  line start point: >>" + line + "<<");
                String[] pt = line.split( "\\s+" );
                x =   Float.parseFloat( pt[0] ) / TopoDroidApp.TO_THERION;
                y = - Float.parseFloat( pt[1] ) / TopoDroidApp.TO_THERION;
                path.addStartPoint( x, y );
              }
              while ( (line = readLine( br )) != null ) {
                if ( line.indexOf( "l-size" ) >= 0 ) continue;
                if ( line.equals( "endline" ) ) {
                  if ( path != null ) {
                    addDrawingPath( path );
                  }
                  break;
                }
                if ( path != null ) {
                  // Log.v( TAG, "  line point: >>" + line + "<<");
                  String[] pt = line.split( " " );
                  if ( pt.length == 2 ) {
                    x  =   Float.parseFloat( pt[0] ) / TopoDroidApp.TO_THERION;
                    y  = - Float.parseFloat( pt[1] ) / TopoDroidApp.TO_THERION;
                    path.addPoint( x, y );
                  } else if ( pt.length == 6 ) {
                    x1 =   Float.parseFloat( pt[0] ) / TopoDroidApp.TO_THERION;
                    y1 = - Float.parseFloat( pt[1] ) / TopoDroidApp.TO_THERION;
                    x2 =   Float.parseFloat( pt[2] ) / TopoDroidApp.TO_THERION;
                    y2 = - Float.parseFloat( pt[3] ) / TopoDroidApp.TO_THERION;
                    x  =   Float.parseFloat( pt[4] ) / TopoDroidApp.TO_THERION;
                    y  = - Float.parseFloat( pt[5] ) / TopoDroidApp.TO_THERION;
                    path.addPoint3( x1, y1, x2, y2, x, y );
                  }
                }
              }
            }
          }
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

}