/* @file DrawingCommandManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: commands manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120621 item editoing: getPointAt getLineAt
 * 20120726 TopoDroidApp log
 * 20121225 getAreaAt and deletePath
 * 20130108 getStationAt getShotAt
 * 20130204 using Selection class to spped up item selection
 * 20130627 SelectionException
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileReader;
// import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;

import android.util.Log;

/**
 */
public class DrawingCommandManager 
{
  // private static final String TAG = "DistoX CM";
  private static final int BORDER = 20;

  private static final float mCloseness = TopoDroidApp.mCloseness;


  static final int DISPLAY_NONE    = 0;
  static final int DISPLAY_LEG     = 0x01;
  static final int DISPLAY_SPLAY   = 0x02;
  static final int DISPLAY_STATION = 0x04;
  static final int DISPLAY_GRID    = 0x08;
  static final int DISPLAY_ALL     = 0x0f;
  // private static final int DISPLAY_MAX     = 4;
  static int mDisplayMode = DISPLAY_ALL;

  private List<DrawingPath>    mGridStack;
  List<DrawingPath>    mFixedStack;
  List<DrawingPath>    mCurrentStack;
  private List<DrawingPath>    mRedoStack;
  // private List<DrawingPath>    mHighlight;  // highlighted path
  private List<DrawingStationName> mStations;

  private Selection mSelection;

  private Matrix mMatrix;

  public DrawingCommandManager()
  {
    mGridStack    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mFixedStack   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mCurrentStack = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mRedoStack    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    // mHighlight    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mStations     = Collections.synchronizedList(new ArrayList<DrawingStationName>());
    mMatrix = new Matrix(); // identity
    mSelection = null;
  }

  boolean isSelectable() { return mSelection != null; }

  void clearReferences()
  {
    synchronized( mGridStack ) {
      mGridStack.clear();
    }
    synchronized( mFixedStack ) {
      mFixedStack.clear();
    }
    synchronized( mStations ) {
      mStations.clear();
    }
    mSelection = null;
  }

  // public void clearHighlight()
  // {
  //   for ( DrawingPath p : mHighlight ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
  //       p.mPaint = DrawingBrushPaths.fixedShotPaint;
  //     } else {
  //       p.mPaint = DrawingBrushPaths.fixedSplayPaint;
  //     }
  //   }
  //   mHighlight.clear();
  // }

  // public DistoXDBlock setHighlight( int plot_type, float x, float y )
  // {
  //   clearHighlight();
  //   if ( plot_type != TopoDroidApp.PLOT_PLAN && plot_type != TopoDroidApp.PLOT_EXTENDED ) return null;
  //   boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
  //   boolean splays = (mDisplayMode & DISPLAY_SPLAY) != 0;
  //   for ( DrawingPath p : mFixedStack ) {
  //     if (    ( p.mType == DrawingPath.DRAWING_PATH_FIXED && legs )
  //          || ( p.mType == DrawingPath.DRAWING_PATH_SPLAY && splays ) ) {
  //       if ( p.isCloseTo( x, y ) ) {
  //         p.mPaint = DrawingBrushPaths.highlightPaint;
  //         mHighlight.add( p );
  //       }
  //     }
  //   }
  //   if ( mHighlight.size() == 1 ) {
  //     return mHighlight.get(0).mBlock;
  //   }
  //   return null;
  // }

  public void setTransform( float dx, float dy, float s )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
  }

  void deletePath( DrawingPath path )
  {
    mCurrentStack.remove( path );
    if ( mSelection != null ) {
      mSelection.removeReferencesTo( path );
    }
  }

  public void setDisplayMode( int mode ) { mDisplayMode = mode; }
  public int getDisplayMode( ) { return mDisplayMode; }

  void setBounds( float x1, float x2, float y1, float y2 )
  {
    try {
      mSelection = new Selection( x1, x2, y1, y2, 5.0f );
    } catch ( SelectionException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "oversize: unable to select " );
      mSelection = null;
    }
  }

  public void addFixedPath( DrawingPath path, boolean selectable )
  {
    mFixedStack.add( path );
    if ( selectable && mSelection != null ) {
      mSelection.insertPath( path );
    }
  }

  public void addGrid( DrawingPath path )
  {
    mGridStack.add( path );
  }

  public void addStation( DrawingStationName st, boolean selectable )
  {
    mStations.add( st );
    if ( selectable && mSelection != null ) {
      mSelection.insertStationName( st );
    }
  }

  public void addCommand( DrawingPath path )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand path " + path.toString() );
    mRedoStack.clear();
    mCurrentStack.add( path );
    if ( mSelection != null ) {
      mSelection.insertPath( path );
    }
  }

  public Bitmap getBitmap()
  {

    RectF bounds = new RectF();
    RectF b = new RectF();
    if( mFixedStack != null ) { 
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.path.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.path.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "getBitmap Bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    int width  = (int)(bounds.right - bounds.left + 2 * BORDER);
    int height = (int)(bounds.bottom - bounds.top + 2 * BORDER);

    Bitmap bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
    // TODO
    Canvas c = new Canvas (bitmap);
    c.drawColor(0, PorterDuff.Mode.CLEAR);
    // commandManager.executeAll(c,previewDoneHandler);
    // previewPath.draw(c);
    c.drawBitmap (bitmap, 0, 0, null);

    Matrix mat = new Matrix();
    mat.postTranslate( BORDER - bounds.left, BORDER - bounds.top );
    if ( mGridStack != null ) {
      synchronized( mGridStack ) {
        final Iterator i = mGridStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
      }
    }
    if ( mFixedStack != null ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
      }
    }
    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( c, mat );
        }
      }
    }
    return bitmap;
  }

  public void undo ()
  {
    final int length = currentStackLength();
    if ( length > 0) {
      final DrawingPath undoCommand = mCurrentStack.get(  length - 1  );
      mCurrentStack.remove( length - 1 );
      undoCommand.undo();
      // FIXME_SEL
      mRedoStack.add( undoCommand );
    }
  }

  public int currentStackLength()
  {
    final int length = mCurrentStack.toArray().length;
    return length;
  }

  public void executeAll( Canvas canvas, Handler doneHandler)
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;

    synchronized( mGridStack ) {
      if( mGridStack != null && ( (mDisplayMode & DISPLAY_GRID) != 0 ) ) {
        final Iterator i = mGridStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }

    synchronized( mFixedStack ) {
      if ( mFixedStack != null && (legs || splays) ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          if ( ( legs && drawingPath.mType == DrawingPath.DRAWING_PATH_FIXED ) 
            || ( splays && drawingPath.mType == DrawingPath.DRAWING_PATH_SPLAY ) ) {
            drawingPath.draw( canvas, mMatrix );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
 
    synchronized( mStations ) {
      if ( mStations != null && stations ) {  
        for ( DrawingStationName st : mStations ) {
          st.draw( canvas, mMatrix );
        }
      }
    }

    synchronized( mCurrentStack ) {
      if ( mCurrentStack != null ){
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );

          // FIXME DEBUG area contour points
          // if ( drawingPath.mType == DrawingPath.DRAWING_PATH_AREA ) {
          //   ArrayList< LinePoint > pts = ((DrawingAreaPath)drawingPath).getPoints();
          //   for ( LinePoint pt : pts ) {
          //     // public float mX;
          //     // public float mY;
          //     Path p = new Path( DrawingBrushPaths.crossPath );
          //     p.offset( pt.mX, pt.mY );
          //     p.transform( mMatrix );
          //     canvas.drawPath( p, DrawingBrushPaths.debugRed );
          //     if ( pt.has_cp ) {
          //       // float mX1; // first control point
          //       // float mY1;
          //       p = new Path( DrawingBrushPaths.crossPath );
          //       p.offset( pt.mX1, pt.mY1 );
          //       p.transform( mMatrix );
          //       canvas.drawPath( p, DrawingBrushPaths.debugGreen );
          //       // float mX2; // second control point
          //       // float mY2;
          //       p = new Path( DrawingBrushPaths.crossPath );
          //       p.offset( pt.mX2, pt.mY2 );
          //       p.transform( mMatrix );
          //       canvas.drawPath( p, DrawingBrushPaths.debugBlue );
          //     }
          //   }
          // }
          // end DEBUG

          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
  }

  boolean hasStationName( String name )
  {
    for ( DrawingPath p : mCurrentStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
        DrawingStationPath sp = (DrawingStationPath)p;
        if ( sp.mName.equals( name ) ) return true;
      }
    }
    return false;
  }

  public boolean hasMoreRedo()
  {
    return  mRedoStack.toArray().length > 0;
  }

  public boolean hasMoreUndo()
  {
    return  mCurrentStack.toArray().length > 0;
  }

  public void redo()
  {
    final int length = mRedoStack.toArray().length;
    if ( length > 0) {
      final DrawingPath redoCommand = mRedoStack.get(  length - 1  );
      mRedoStack.remove( length - 1 );
      mCurrentStack.add( redoCommand );
    }
  }

  public DrawingPointPath getPointAt( float x, float y )
  {
    // FIXME_SEL
    if ( mSelection == null ) return null;
    SelectionPoint sp = mSelection.getClosestItem(x, y, mCloseness, DrawingPath.DRAWING_PATH_POINT );
    return ( sp != null )? (DrawingPointPath)sp.item : null;

    // float min_dist = 1000.0f;
    // DrawingPointPath ret = null;
    // for ( DrawingPath p : mCurrentStack ) {
    //   if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
    //     DrawingPointPath pp = (DrawingPointPath)p;
    //     float d = Math.abs( pp.mXpos - x ) + Math.abs( pp.mYpos - y );
    //     if ( d < mCloseness && d < min_dist ) {
    //       min_dist = d;
    //       ret = pp;
    //     }
    //   }
    // }
    // return ret;
  }

  // get station is different it checks drawing-stations
  public DrawingStationName getStationAt( float x, float y )
  {
    if ( (mDisplayMode & DISPLAY_STATION) == 0 ) {
      return null;
    }
    if ( mSelection == null ) return null;

    // FIXME_SEL
    // Log.v("DistoX", "Closeness " + mCloseness );
    // mSelection.dump();
    SelectionPoint sp = mSelection.getClosestItem(x, y, mCloseness, DrawingPath.DRAWING_PATH_NAME );
    return ( sp != null )? (DrawingStationName)sp.item : null;

    // float min_dist = 1000.0f;
    // DrawingStationName ret = null;
    // for ( DrawingStationName st : mStations ) {
    //   float d = st.distance( x, y );
    //   if ( d < mCloseness && d < min_dist ) {
    //     min_dist = d;
    //     ret = st;
    //   }
    // }
    // return ret;
  }

  public DrawingPath getShotAt( float x, float y )
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG)   != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY) != 0;
    if ( ! ( legs || splays ) ) return null;

    // FIXME_SEL
    if ( mSelection == null ) return null;
    SelectionPoint sp = mSelection.getClosestItem(x, y, mCloseness, legs, splays );
    return ( sp != null )? (DrawingPath)sp.item : null;

    // float min_dist = 1000.0f;
    // DrawingPath ret = null;
    // for ( DrawingPath p : mFixedStack ) {
    //   if (    ( legs && p.mType == DrawingPath.DRAWING_PATH_FIXED )
    //        || ( splays && p.mType == DrawingPath.DRAWING_PATH_SPLAY ) ) {
    //    float d = p.distance( x, y );
    //    if ( d < mCloseness && d < min_dist ) { 
    //      min_dist = d;
    //      ret = p;
    //    }
    //   }
    // }
    // return ret;
  }

  public DrawingLinePath getLineAt( float x, float y )
  {
    // FIXME_SEL
    if ( mSelection == null ) return null;
    SelectionPoint sp = mSelection.getClosestItem(x, y, mCloseness, DrawingPath.DRAWING_PATH_LINE );
    return ( sp != null )? (DrawingLinePath)sp.item : null;

    // DrawingLinePath ret = null;
    // float min_dist = 1000.0f;
    // for ( DrawingPath p : mCurrentStack ) {
    //   if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
    //     DrawingLinePath pp = (DrawingLinePath)p;
    //     float d = pp.distance( x, y );
    //     if ( d < mCloseness && d < min_dist ) {
    //       min_dist = d;
    //       ret = pp;
    //     }
    //   }
    // }
    // return ret;
  }

  public DrawingAreaPath getAreaAt( float x, float y )
  { 
    // FIXME_SEL
    if ( mSelection == null ) return null;
    SelectionPoint sp = mSelection.getClosestItem(x, y, mCloseness, DrawingPath.DRAWING_PATH_AREA );
    return ( sp != null )? (DrawingAreaPath)sp.item : null;

    // DrawingAreaPath ret = null;
    // float min_dist = 1000.0f;
    // for ( DrawingPath p : mCurrentStack ) {
    //   if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
    //     DrawingAreaPath pp = (DrawingAreaPath)p;
    //     float d = pp.distance( x, y );
    //     if ( d < mCloseness && d < min_dist ) {
    //       min_dist = d;
    //       ret = pp;
    //     }
    //   }
    // }
    // return ret;
  }


  public void exportTherion( BufferedWriter out, String scrap_name, String proj_name )
  {
    try { 
      out.write("encoding utf-8");
      out.newLine();
      out.newLine();
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("scrap %s -projection %s -scale [0 0 1 0 0.0 0.0 1 0.0 m]", scrap_name, proj_name );
      out.write( sw.getBuffer().toString() );
      out.newLine();
      // out.newLine();
      // for ( DrawingStationName st : mStations ) {
      //   out.write( st.toTherion() );
      //   out.newLine();
      // }
      out.newLine();
      float xmin=10000f, xmax=-10000f, 
            ymin=10000f, ymax=-10000f,
            umin=10000f, umax=-10000f,
            vmin=10000f, vmax=-10000f;
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath pp = (DrawingPointPath)p;
          out.write( pp.toTherion() );
          out.newLine();
        } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
          if ( ! TopoDroidApp.mAutoStations ) {
            DrawingStationPath st = (DrawingStationPath)p;
            // Log.v("DistoX", "save station to Therion " + st.mName );
            out.write( st.toTherion() );
            out.newLine();
          }
        } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion line " + lp.lineType() );
          if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
            ArrayList< LinePoint > pts = lp.points;
            for ( LinePoint pt : pts ) {
              if ( pt.mX < xmin ) xmin = pt.mX;
              if ( pt.mX > xmax ) xmax = pt.mX;
              if ( pt.mY < ymin ) ymin = pt.mY;
              if ( pt.mY > ymax ) ymax = pt.mY;
              float u = pt.mX + pt.mY;
              float v = pt.mX - pt.mY;
              if ( u < umin ) umin = u;
              if ( u > umax ) umax = u;
              if ( v < vmin ) vmin = v;
              if ( v > vmax ) vmax = v;
            }
          }
          out.write( lp.toTherion() );
          out.newLine();
        } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)p;
          // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion area " + ap.areaType() );
          out.write( ap.toTherion() );
          out.newLine();
        }
      }
      out.newLine();

      if ( TopoDroidApp.mAutoStations ) {
        for ( DrawingStationName st : mStations ) {
          // FIXME if station is in the convex hull of the lines
          if ( xmin > st.mX || xmax < st.mX ) continue;
          if ( ymin > st.mY || ymax < st.mY ) continue;
          float u = st.mX + st.mY;
          float v = st.mX - st.mY;
          if ( umin > u || umax < u ) continue;
          if ( vmin > v || vmax < v ) continue;
          out.write( st.toTherion() );
          out.newLine();
        }
        out.newLine();
        out.newLine();
      }

      out.write("endscrap");
      out.newLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

}
