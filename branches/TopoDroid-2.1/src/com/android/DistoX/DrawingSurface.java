/* @file DrawingSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120623 handle line attributes in loadTherion
 * 20120705 hadle point attributes in loadTherion
 * 20121113 sink/spring points from Therion
 * 20121122 overloaded point snow/ice, flowstone/moonmilk dig/choke crystal/gypsum
 * 20121206 symbol libraries
 * 20130826 split line path
 * 20130828 shift point path (change position of symbol point)
 * 201311   new editing action forwarded to the commandManager
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

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 */
public class DrawingSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{
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

    private DrawingCommandManager commandManager; // FIXME not private only to export DXF
    DrawingCommandManager mCommandManager1; 
    DrawingCommandManager mCommandManager2; 

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    // private Timer mTimer;
    // private TimerTask mTask;

    boolean isSelectable() { return commandManager.isSelectable(); }

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
      mCommandManager1 = new DrawingCommandManager();
      mCommandManager2 = new DrawingCommandManager();
      commandManager = mCommandManager1;

      // setOnLongClickListener(new View.OnLongClickListener() 
      //   {
      //     public boolean onLongClick(View v)
      //     {
      //       Log.v( TopoDroidApp.TAG, "LONG CLICK!" );
      //       return true;
      //     }
      //   }
      // );
    }

    void setManager( int type ) 
    {
      // Log.v( TopoDroidApp.TAG, " set manager type " + PlotInfo.plotType[type] );
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        commandManager = mCommandManager2;
      } else if ( type == PlotInfo.PLOT_PLAN ) {
        commandManager = mCommandManager1;
      } else { // should never happen
        commandManager = mCommandManager1;
        mCommandManager2 = null;
      }
    }

    void setDisplayPoints( boolean display ) 
    { 
      commandManager.setDisplayPoints( display );
      if ( display ) {
      } else {
        commandManager.clearSelected();
      }
    }

    // @Override
    // public boolean onTouchEvent(MotionEvent event) 
    // {
    //   Log.v( TopoDroidApp.TAG, "TOUCH EVENT!" );
    //   super.onTouchEvent(event);
    //   return true;
    // }

    void setScaleBar() { commandManager.setScaleBar(); }

    // -----------------------------------------------------------


    public void setDisplayMode( int mode ) { commandManager.setDisplayMode(mode); }

    public int getDisplayMode( ) { return commandManager.getDisplayMode(); }

    public void setTransform( float dx, float dy, float s )
    {
      commandManager.setTransform( dx, dy, s );
    }

    void splitLine( DrawingLinePath line, LinePoint lp ) { commandManager.splitLine( line, lp ); }

    boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
    { return commandManager.removeLinePoint(line, point, sp); }

    void deletePath( DrawingPath path ) { commandManager.deletePath( path ); }

    void sharpenLine( DrawingLinePath line, boolean reduce ) { commandManager.sharpenLine( line, reduce ); }

    void eraseAt( float x, float y ) { commandManager.eraseAt( x, y ); }
    
    void clearReferences( int type ) 
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.clearReferences();
      } else {
        mCommandManager1.clearReferences();
      }
    }

    void refresh()
    {
      Canvas canvas = null;
      try {
        canvas = mHolder.lockCanvas();
        if ( mBitmap == null ) {
          mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
        }
        final Canvas c = new Canvas (mBitmap);
        mWidth  = c.getWidth();
        mHeight = c.getHeight();

        c.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        commandManager.executeAll(c,previewDoneHandler);
        if ( previewPath != null ) {
          previewPath.draw(c);
        }
      
        canvas.drawBitmap (mBitmap, 0,  0,null);
      } finally {
        if ( canvas != null ) {
          mHolder.unlockCanvasAndPost( canvas );
        }
      }
    }

    private Handler previewDoneHandler = new Handler()
    {
      @Override
      public void handleMessage(Message msg) {
        isDrawing = false;
      }
    };

    void clearDrawing() { commandManager.clearDrawing(); }

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
        while ( _run ) {
          if ( isDrawing == true ) {
            refresh();
            // Canvas canvas = null;
            // try{
            //   canvas = mSurfaceHolder.lockCanvas(null);
            //   if(mBitmap == null){
            //     mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
            //   }
            //   final Canvas c = new Canvas (mBitmap);
            //   mWidth  = c.getWidth();
            //   mHeight = c.getHeight();

            //   c.drawColor(0, PorterDuff.Mode.CLEAR);
            //   canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            //   commandManager.executeAll(c,previewDoneHandler);
            //   previewPath.draw(c);
            //     
            //   canvas.drawBitmap (mBitmap, 0,  0,null);
            // } finally {
            //   mSurfaceHolder.unlockCanvasAndPost(canvas);
            // }
          }
        }
      }
    }

    public DrawingStationName addStation( String name, float x, float y, boolean duplicate, boolean selectable )
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addStation " + name + " " + x + " " + y );
      // Log.v( TopoDroidApp.TAG, "addStation " + name + " " + x + " " + y );
      DrawingStationName st = new DrawingStationName(name, x, y, duplicate );
      st.setPaint( DrawingBrushPaths.fixedStationPaint );
      commandManager.addStation( st, selectable );
      return st;
    }

    public void addFixedPath( DrawingPath path, boolean selectable )
    {
      commandManager.addFixedPath( path, selectable );
    }

    public void addGridPath( DrawingPath path )
    {
      commandManager.addGrid( path );
    }

    public void addDrawingPath (DrawingPath drawingPath)
    {
      commandManager.addCommand(drawingPath);
    }
    
    // void setBounds( float x1, float x2, float y1, float y2 )
    // {
    //   commandManager.setBounds( x1, x2, y1, y2 );
    // }

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

    public boolean hasStationName( String name )
    {
      return commandManager.hasStationName( name );
    }

    public Bitmap getBitmap( int type )
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        return mCommandManager2.getBitmap();
      }
      return mCommandManager1.getBitmap();
    }

    // ---------------------------------------------------------------------
    // SELECT - EDIT

    // public SelectionPoint getPointAt( float x, float y )
    // {
    //   return commandManager.getPointAt( x, y );
    // }

    // public SelectionPoint getLineAt( float x, float y )
    // {
    //   return commandManager.getLineAt( x, y );
    // }

    // public SelectionPoint getAreaAt( float x, float y )
    // {
    //   return commandManager.getAreaAt( x, y );
    // }

    // public SelectionPoint  getStationAt( float x, float y )
    // {
    //   return commandManager.getStationAt( x, y );
    // }

    // public SelectionPoint getShotAt( float x, float y )
    // {
    //   return commandManager.getShotAt( x, y );
    // }

    SelectionSet getItemsAt( float x, float y )
    {
      return commandManager.getItemsAt( x, y );
    }

    void moveHotItemToNearestPoint() { commandManager.moveHotItemToNearestPoint(); }

    void splitHotItem() { commandManager.splitHotItem(); }
    
    SelectionPoint hotItem() { return commandManager.hotItem(); }

    void shiftHotItem( float dx, float dy ) { commandManager.shiftHotItem( dx, dy ); }

    SelectionPoint nextHotItem() { return commandManager.nextHotItem(); }

    SelectionPoint prevHotItem() { return commandManager.prevHotItem(); }

    void clearSelected() { commandManager.clearSelected(); }

    // ---------------------------------------------------------------------

    public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "surfaceChanged " );
      // TODO Auto-generated method stub
      mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
    }


    public void surfaceCreated(SurfaceHolder mHolder) 
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "surfaceCreated " );
      // TODO Auto-generated method stub
      if (thread == null ) {
        thread = new DrawThread(mHolder);
      }
      thread.setRunning(true);
      thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder mHolder) 
    {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "surfaceDestroyed " );
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

    public void exportTherion( int type, BufferedWriter out, String sketch_name, String plot_name )
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.exportTherion( out, sketch_name, plot_name );
      } else {
        mCommandManager1.exportTherion( out, sketch_name, plot_name );
      }
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

  public boolean loadTherion( String filename1, String filename2, MissingSymbols missingSymbols )
  {
    missingSymbols.resetSymbolLists();
    boolean ret = true;
    if ( filename2 != null ) {
      commandManager = mCommandManager2;
      ret = ret && doLoadTherion( filename2, missingSymbols );
    }
    commandManager = mCommandManager1;
    return ret && doLoadTherion( filename1, missingSymbols );
  }

  public boolean doLoadTherion( String filename, MissingSymbols missingSymbols )
  {
    float x, y, x1, y1, x2, y2;

    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "doLoadTherion file " + filename );
    // DrawingBrushPaths.makePaths( );
    DrawingBrushPaths.resetPointOrientations();
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "after reset 0: " + DrawingBrushPaths.mOrientation[0]
    //                      + " 7: " + DrawingBrushPaths.mOrientation[7] );
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = null;
      while ( (line = readLine(br)) != null ) {
        line.trim();
        int comment = line.indexOf('#');
        if ( comment == 0 ) {
          continue;
        } else if (comment > 0 ) {
          line = line.substring( 0, comment );
        }
        if ( line.length() == 0 /* || line.charAt(0) == '#' */ ) {
          continue;
        }

        // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  line: >>" + line + "<<");
        String[] vals = line.split( " " );
        if ( vals[0].equals( "point" ) ) {
          // ****** THERION POINT **********************************
          int ptType = DrawingBrushPaths.mPointLib.mPointNr;
          boolean has_orientation = false;
          float orientation = 0.0f;
          int scale = DrawingPointPath.SCALE_M;
          String options = null;

          x =   Float.parseFloat( vals[1] ) / TopoDroidApp.TO_THERION;
          y = - Float.parseFloat( vals[2] ) / TopoDroidApp.TO_THERION;
          String type = vals[3];
          String label_text = null;
          int k = 4;
          if ( type.equals( "station" ) ) {
            if ( ! TopoDroidApp.mAutoStations ) {
              if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
                String name = vals[k+1];
                DrawingStationPath station_path = new DrawingStationPath( name, x, y, scale );
                addDrawingPath( station_path );
              }
            }
            continue;
          }
          while ( vals.length > k ) { 
            if ( vals[k].equals( "-orientation" ) ) {
              has_orientation = true;
              orientation = Float.parseFloat( vals[k+1] );
              // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "point orientation " + orientation );
              k += 2;
            } else if ( vals[k].equals( "-scale" ) ) {
              if ( vals[k+1].equals("xs") ) {
                scale = DrawingPointPath.SCALE_XS;
              } else if ( vals[k+1].equals("s") ) {
                scale = DrawingPointPath.SCALE_S;
              } else if ( vals[k+1].equals("l") ) {
                scale = DrawingPointPath.SCALE_L;
              } else if ( vals[k+1].equals("xl") ) {
                scale = DrawingPointPath.SCALE_XL;
              } 
              k += 2;
            } else if ( vals[k].equals( "-text" ) ) {
              label_text = vals[k+1].replace( "\"", "" );
              k += 2;
            } else {
              options = vals[k];
              ++ k;
              while ( vals.length > k ) {
                options += " " + vals[k];
                ++ k;
              }
            }
          }

          // overloaded point types
          // if ( type.equals( "stalagmite" ) ) {
          //   ptType = DrawingBrushPaths.POINT_STAL;
          //   orientation = 180.0f;
          //   has_orientation = true;
          // } else if ( type.equals( "stalactite" ) ) {
          //   ptType = DrawingBrushPaths.POINT_STAL;
          //   has_orientation = false;
          // } else if ( type.equals( "narrow-end" ) ) {
          //   ...
          // } 

          for ( ptType = 0; ptType < DrawingBrushPaths.mPointLib.mPointNr; ++ptType ) {
            if ( type.equals( DrawingBrushPaths.getPointThName( ptType ) ) ) {
              break;
            }
          }

          if ( ptType == DrawingBrushPaths.mPointLib.mPointNr ) {
            missingSymbols.addPoint( type );
            continue;
          }

          if ( has_orientation && DrawingBrushPaths.canRotate(ptType) ) {
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "[2] point " + ptType + " has orientation " + orientation );
            DrawingBrushPaths.rotateGrad( ptType, orientation );
            DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
            addDrawingPath( path );
            DrawingBrushPaths.rotateGrad( ptType, -orientation );
          } else {
            if ( ptType != DrawingBrushPaths.mPointLib.mPointLabelIndex ) {
              DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
              addDrawingPath( path );
            } else {
              if ( label_text.equals( "!" ) ) {    // "danger" point
                DrawingPointPath path = new DrawingPointPath( DrawingBrushPaths.mPointLib.mPointDangerIndex, x, y, scale, options );
                addDrawingPath( path );
              } else {                             // regular label
                DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
                addDrawingPath( path );
              }
            }
          }

        } else if ( vals[0].equals( "line" ) ) {
          // ********* THERION LINES ************************************************************
          if ( vals.length >= 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
            boolean visible = true;
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "area id " + vals[3] );
            if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
              visible = false;
            }
            int arType = DrawingBrushPaths.mAreaLib.mAreaNr;
            DrawingAreaPath path = new DrawingAreaPath( arType, vals[3], visible );

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
                  for ( arType=0; arType < DrawingBrushPaths.mAreaLib.mAreaNr; ++arType ) {
                    if ( vals2[1].equals( DrawingBrushPaths.getAreaThName( arType ) ) ) break;
                  }
                  // TopoDroidApp.Log(TopoDroidApp.LOG_PLOT, "set area type " + arType );
                  if ( arType < DrawingBrushPaths.mAreaLib.mAreaNr ) {
                    path.setAreaType( arType );
                    addDrawingPath( path );
                  } else {
                    missingSymbols.addArea( vals2[1] );
                  }
                  line = readLine( br ); // skip two lines
                  line = readLine( br );
                  break;
                }
                // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  line point: >>" + line + "<<");
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
            // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "line type " + vals[1] );
            boolean closed = false;
            boolean reversed = false;
            int outline = DrawingLinePath.OUTLINE_UNDEF;
            String options = null;
           
            String type = vals[1];
            for (int index = 2; index < vals.length; ++index ) {
              if ( vals[index] == null || vals[index].length() == 0 ) {
                continue;
              }
              if ( vals[index].equals( "-close" ) ) {
                ++ index;
                if ( vals.length > index && vals[index].equals( "on" ) ) {
                  closed = true;
                }
              } else if ( vals[index].equals( "-reversed" ) ) {
                ++ index;
                if ( vals.length > index && vals[index].equals( "on" ) ) {
                  reversed = true;
                }
              } else if ( vals[index].equals( "-outline" ) ) {
                ++ index;
                if ( vals.length > index ) {
                  if ( vals[index].equals( "out" ) ) { outline = DrawingLinePath.OUTLINE_OUT; }
                  else if ( vals[index].equals( "in" ) ) { outline = DrawingLinePath.OUTLINE_IN; }
                  else if ( vals[index].equals( "none" ) ) { outline = DrawingLinePath.OUTLINE_NONE; }
                }
              } else {
                if ( options == null ) {
                  options = vals[index];
                } else {
                  options += " " + vals[index];
                }
              } 
            }
            
            int lnTypeMax = DrawingBrushPaths.mLineLib.mLineNr;
            int lnType = lnTypeMax;
            DrawingLinePath path = null;
            for ( lnType=0; lnType < lnTypeMax; ++lnType ) {
              if ( type.equals( DrawingBrushPaths.getLineThName( lnType ) ) ) break;
            }
            // TODO insert new line-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) { 
              if ( lnType < lnTypeMax ) {
                path = new DrawingLinePath( lnType );
                if ( closed ) path.mClosed = true;
                if ( reversed ) path.mReversed = true;
                if ( outline != DrawingLinePath.OUTLINE_UNDEF ) path.mOutline = outline;
                if ( options != null ) path.mOptions = options;

                // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  line start point: >>" + line + "<<");
                String[] pt = line.split( "\\s+" );
                x =   Float.parseFloat( pt[0] ) / TopoDroidApp.TO_THERION;
                y = - Float.parseFloat( pt[1] ) / TopoDroidApp.TO_THERION;
                path.addStartPoint( x, y );
              } else {
                missingSymbols.addLine( type );
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
                  // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "  line point: >>" + line + "<<");
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
    } catch ( FileNotFoundException e ) {
      // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    // remove repeated names
    return missingSymbols.isOK();
  }

}
