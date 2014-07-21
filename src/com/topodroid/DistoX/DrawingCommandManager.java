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
 * 20130204 using Selection class to speed up item selection
 * 20130627 SelectionException
 * 20130828 shift point path (change position of symbol point)
 * 201311   revised selection management to keep into account new point actions
 * 201312   synch bug fixes 
 * ...
 * 20140117 added date/version to export
 * 20140328 line-legs intersection
 * 20140513 export as cSurvey
 * 20140521 1-point line bug
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
// import android.graphics.Path.Direction;
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

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

// import android.util.Log;

/**
 */
public class DrawingCommandManager 
{
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
  private SelectionSet mSelected;
  private boolean mDisplayPoints;

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
    mSelection = new Selection();
    mSelected  = new SelectionSet();
  }

  List< DrawingPath > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    List< DrawingPath > ret = new ArrayList< DrawingPath >();
    for ( DrawingPath p : mFixedStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        if ( p.intersect( p1.mX, p1.mY, p2.mX, p2.mY ) ) {
          // Log.v( TopoDroidApp.TAG, "intersect " + p.mBlock.toString(false) );
          // if ( ret != null ) return null;
          ret.add( p );
        }
      }
    }
    return ret;
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

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
    clearSelected();
    synchronized( mSelection ) {
      mSelection.clear();
    }
  }

  void clearDrawing()
  {
    mGridStack.clear();
    mFixedStack.clear();
    mStations.clear();
    clearSketchItems();
  }

  void clearSketchItems()
  {
    mCurrentStack.clear();
    mRedoStack.clear();

    mSelection.clear();
    mSelected.clear();
    mDisplayPoints = false;
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
  //   if ( plot_type != PlotInfo.PLOT_PLAN && plot_type != PlotInfo.PLOT_EXTENDED ) return null;
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

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  /** 
   * @return result code:
   *    0  no erasing
   *    1  point erased
   *    2  line complete erase
   *    3  line start erase
   *    4  line end erase 
   *    5  line split
   *    6  area complete erase
   *    7  area point erase
   */
  int eraseAt( float x, float y, float zoom ) 
  {
    SelectionSet sel = new SelectionSet();
    mSelection.selectAt( x, y, zoom, sel, false, false, false );
    int ret = 0;
    if ( sel.size() > 0 ) {
      synchronized( mCurrentStack ) {
        for ( SelectionPoint pt : sel.mPoints ) {
          DrawingPath path = pt.mItem;
          if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath)path;
            ArrayList< LinePoint > points = line.mPoints;
            int size = points.size();
            if ( size <= 2 || ( size == 3 && pt.mPoint == points.get(1) ) ) {
              ret = 2;
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else if ( pt.mPoint == points.get(1) ) {
              ret = 3;
              LinePoint lp = points.get(0);
              doRemoveLinePoint( line, lp, null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              synchronized( mSelection ) {
                mSelection.removeLinePoint( line, lp ); // index = 0
                mSelection.mPoints.remove( pt );        // index = 1
              }
              line.retracePath();
            } else if ( pt.mPoint == points.get(size-2) ) {
              ret = 4;
              LinePoint lp = points.get(size-1);
              doRemoveLinePoint( line, lp, null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              synchronized( mSelection ) {
                mSelection.removeLinePoint( line, lp ); // size -1
                mSelection.mPoints.remove( pt );        // size -2
              }
              line.retracePath();
            } else {
              ret = 5;
              doSplitLine( line, pt.mPoint );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath)path;
            if ( area.mPoints.size() <= 3 ) {
              ret = 6;
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else {
              ret = 7;
              doRemoveLinePoint( area, pt.mPoint, pt );
              area.retracePath();
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            ret = 1;
            // DrawingPointPath point = (DrawingPointPath)path;
            mCurrentStack.remove( path );
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          }
        }
      }
    }
    return ret;
  }

  // called from synchronized( CurrentStack ) context
  // called only by eraseAt
  private void doSplitLine( DrawingLinePath line, LinePoint lp )
  {
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2, true ) ) {
      mCurrentStack.remove( line );
      mCurrentStack.add( line1 );
      mCurrentStack.add( line2 );
      synchronized( mSelection ) {
        mSelection.removePath( line ); 
        mSelection.insertLinePath( line1 );
        mSelection.insertLinePath( line2 );
      }
    }
  }

  void splitLine( DrawingLinePath line, LinePoint lp )
  {
    if ( lp == null ) {
      return;
    }
    if ( lp == line.mPoints.get(0) ) {
      return; // cannot split at first point
    }
    int size = line.mPoints.size();
    if ( size == 2 ) {
      return;
    }
    if ( lp == line.mPoints.get(size-1) ) {
      return; // cannot split at last point
    }
    clearSelected();

    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2, false ) ) {
      synchronized( mCurrentStack ) {
        mCurrentStack.remove( line );
        mCurrentStack.add( line1 );
        mCurrentStack.add( line2 );
      }
      synchronized( mSelection ) {
        mSelection.removePath( line ); 
        mSelection.insertLinePath( line1 );
        mSelection.insertLinePath( line2 );
      }
    }
  }

  // called from synchronized( mCurrentStack )
  private void doRemoveLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  {
    // int size = line.mPoints.size();
    // for ( int k=0; k<size; ++k ) {
    //   LinePoint lp = line.mPoints.get( k );
    //   if ( lp == point ) {
    //     line.mPoints.remove( k );
    //     mSelection.mPoints.remove( sp );
    //     return;
    //   }
    // }
    line.mPoints.remove( point );
    synchronized( mSelection ) {
      mSelection.mPoints.remove( sp );
    }
  }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp )
  {
    if ( point == null ) return false;
    int size = line.mPoints.size();
    if ( size <= 2 ) return false;
    synchronized( mSelection ) {
      clearSelected();
    }
    for ( int k=0; k<size; ++k ) {
      LinePoint lp = line.mPoints.get( k );
      if ( lp == point ) {
        synchronized( mCurrentStack ) {
          line.mPoints.remove( k );
          synchronized( mSelection ) {
            mSelection.mPoints.remove( sp );
          }
          return true;
        }
      }
    }
    return false;
  }

  void deletePath( DrawingPath path )
  {
    synchronized( mCurrentStack ) {
      mCurrentStack.remove( path );
    }
    synchronized( mSelection ) {
      mSelection.removePath( path );
      clearSelected();
    }
  }

  void sharpenLine( DrawingLinePath line, boolean reduce ) 
  {
    if ( reduce ) {
      synchronized( mSelection ) {
        mSelection.removePath( line );
        clearSelected();
      }
    }
    synchronized( mCurrentStack ) {
      line.makeSharp( reduce );
    }
    if ( reduce ) {
      synchronized( mSelection ) {
        mSelection.insertPath( line );
      }
    }
  }

  // ooooooooooooooooooooooooooooooooooooooooooooooooooooo

  public void setDisplayMode( int mode ) { mDisplayMode = mode; }
  public int getDisplayMode( ) { return mDisplayMode; }

  // void setBounds( float x1, float x2, float y1, float y2 )
  // {
  //   mSelection = new Selection();
  // }
  //   try {
  //     mSelection = new Selection( x1, x2, y1, y2, 5.0f );
  //     mSelected  = new SelectionSet();
  //   } catch ( SelectionException e ) {
  //     TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "oversize: unable to select " );
  //     mSelection = null;
  //   }
  // } 

  void resetFixedPaint( Paint paint )
  {
    if( mFixedStack != null ) { 
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          path.setPaint( paint );
        }
      }
    }
  }

  /** add a fixed path
   * @param path       path
   * @param selectable whether the path is selectable
   */
  public void addFixedPath( DrawingPath path, boolean selectable )
  {
    mFixedStack.add( path );
    if ( selectable ) {
      synchronized( mSelection ) {
        mSelection.insertPath( path );
      }
    }
  }

  public void addGrid( DrawingPath path )
  {
    mGridStack.add( path );
  }

  public void addStation( DrawingStationName st, boolean selectable )
  {
    mStations.add( st );
    if ( selectable ) {
      synchronized( mSelection ) {
        mSelection.insertStationName( st );
      }
    }
  }

  void setScaleBar( float x0, float y0 ) 
  {
    if ( mCurrentStack.size() > 0 ) return;
    DrawingLinePath scale_bar = new DrawingLinePath( DrawingBrushPaths.mLineLib.mLineSectionIndex );
    scale_bar.addStartPoint( x0 - 50, y0 );
    scale_bar.addPoint( x0 + 50, y0 );  // 5 meters
    synchronized( mCurrentStack ) {
      mCurrentStack.add( scale_bar );
    }
  }


  void addCommand( DrawingPath path )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand path " + path.toString() );
    mRedoStack.clear();
    
    synchronized( mCurrentStack ) {
      mCurrentStack.add( path );
    }
    synchronized( mSelection ) {
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
          drawingPath.mPath.computeBounds( b, true );
          bounds.union( b );
        }
      }
    }
    if( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.mPath.computeBounds( b, true );
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
      synchronized( mSelection ) {
        mSelection.removePath( undoCommand );
      }
      synchronized( mCurrentStack ) {
        mCurrentStack.remove( length - 1 );
      }
      undoCommand.undo();
      mRedoStack.add( undoCommand );
    }
  }

  public int currentStackLength()
  {
    final int length = mCurrentStack.toArray().length;
    return length;
  }

  public void executeAll( Canvas canvas, float zoom, Handler doneHandler)
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;

    if( mGridStack != null && ( (mDisplayMode & DISPLAY_GRID) != 0 ) ) {
      synchronized( mGridStack ) {
        final Iterator i = mGridStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }

    if ( mFixedStack != null && (legs || splays) ) {
      synchronized( mFixedStack ) {
        final Iterator i = mFixedStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath path = (DrawingPath) i.next();
          if ( legs && path.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            path.draw( canvas, mMatrix );
          } else if ( splays && path.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
            path.draw( canvas, mMatrix );
          }
          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
 
    if ( mStations != null && stations ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) {
          st.draw( canvas, mMatrix );
        }
      }
    }

    if ( mCurrentStack != null ){
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ){
          final DrawingPath drawingPath = (DrawingPath) i.next();
          drawingPath.draw( canvas, mMatrix );

          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
    if ( mDisplayPoints ) {
      synchronized( mSelection ) {
        float radius = 5/zoom;
        for ( SelectionPoint pt : mSelection.mPoints ) {
          float x, y;
          if ( pt.mPoint != null ) { // line-point
            x = pt.mPoint.mX;
            y = pt.mPoint.mY;
          } else {  
            x = pt.mItem.cx;
            y = pt.mItem.cy;
          }
          Path path = new Path();
          path.addCircle( x, y, radius, Path.Direction.CCW );
          path.transform( mMatrix );
          canvas.drawPath( path, DrawingBrushPaths.highlightPaint2 );
        }
      }
      synchronized( mSelected ) {
        if ( mSelected.mPoints.size() > 0 ) {
          float radius = 20/zoom;
          Path path;
          SelectionPoint sp = mSelected.mHotItem;
          if ( sp != null ) {
            float x, y;
            if ( sp.mPoint != null ) { // line-point
              x = sp.mPoint.mX;
              y = sp.mPoint.mY;
            } else {
              x = sp.mItem.cx;
              y = sp.mItem.cy;
            }
            path = new Path();
            path.addCircle( x, y, radius, Path.Direction.CCW );
            path.transform( mMatrix );
            canvas.drawPath( path, DrawingBrushPaths.highlightPaint2 );
          }
          radius = radius/3; // 2/zoom;
          for ( SelectionPoint pt : mSelected.mPoints ) {
            float x, y;
            if ( pt.mPoint != null ) { // line-point
              x = pt.mPoint.mX;
              y = pt.mPoint.mY;
            } else {
              x = pt.mItem.cx;
              y = pt.mItem.cy;
            }
            path = new Path();
            path.addCircle( x, y, radius, Path.Direction.CCW );
            path.transform( mMatrix );
            canvas.drawPath( path, DrawingBrushPaths.highlightPaint );
          }
        }
      } 
    }
  }

  boolean hasStationName( String name )
  {
    synchronized( mCurrentStack ) {
      final Iterator i = mCurrentStack.iterator();
      while ( i.hasNext() ){
        final DrawingPath p = (DrawingPath) i.next();
        if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
          DrawingStationPath sp = (DrawingStationPath)p;
          if ( sp.mName.equals( name ) ) return true;
        }
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
      synchronized( mCurrentStack ) {
        mCurrentStack.add( redoCommand );
      }
      synchronized( mSelection ) {
        mSelection.insertPath( redoCommand );
      }
    }
  }

  public SelectionSet getItemsAt( float x, float y, float zoom )
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;
    synchronized ( mSelected ) {
      mSelected.clear();
      mSelection.selectAt( x, y, zoom, mSelected, legs, splays, stations );
      if ( mSelected.mPoints.size() > 0 ) {
        mSelected.nextHotItem();
      }
    }
    return mSelected;
  }

  void splitHotItem()
  { 
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return;
    if ( sp.type() != DrawingPath.DRAWING_PATH_LINE && sp.type() != DrawingPath.DRAWING_PATH_AREA ) return;
    LinePoint lp = sp.mPoint;
    if ( lp == null ) return;
    float x = lp.mX;
    float y = lp.mY;
    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
    LinePoint p1 = line.insertPointAfter( x, y, lp );
    SelectionPoint sp1 = null;
    synchronized( mSelection ) {
      sp1 = mSelection.insertPathPoint( line, p1 );
    }
    if ( sp1 != null ) {
      synchronized( mSelected ) {
        mSelected.mPoints.add( sp1 );
      }
    }
  }

  private float project( LinePoint q, LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.mX - p0.mX;
    float y01 = p1.mY - p0.mY;
    return ((q.mX-p0.mX)*x01 + (q.mY-p0.mY)*y01) / ( x01*x01 + y01*y01 );
  }
    
  private float distance( LinePoint q, LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.mX - p0.mX;
    float y01 = p1.mY - p0.mY;
    return (float)( Math.abs((q.mX-p0.mX)*y01 - (q.mY-p0.mY)*x01) / Math.sqrt( x01*x01 + y01*y01 ) );
  }
    
      
  boolean moveHotItemToNearestPoint()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return false;
    float x = 0.0f;
    float y = 0.0f;
    if ( sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
      x = sp.mItem.cx;
      y = sp.mItem.cy;
    } else if ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) {
      x = sp.mPoint.mX;
      y = sp.mPoint.mY;
    } else {
      return false;
    }
    float dmin = 10f; // require a minimum distance
    SelectionPoint spmin = null;
    for ( SelectionPoint sp1 : mSelection.mPoints ) {
      if ( sp1 == sp ) continue;
      float d = sp1.distance( x, y );
      if ( d < dmin ) {
        dmin = d;
        spmin = sp1;
      }
    }
    if ( spmin != null ) {
      if ( spmin.type() == DrawingPath.DRAWING_PATH_LINE || spmin.type() == DrawingPath.DRAWING_PATH_AREA ) {
        x = spmin.mPoint.mX - x;
        y = spmin.mPoint.mY - y;
      } else {
        x = spmin.mItem.cx - x;
        y = spmin.mItem.cy - y;
      }
      sp.shiftBy( x, y );
    }
    return true;
  }
  
  boolean snapHotItemToNearestLine()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return false;
    if ( sp.type() != DrawingPath.DRAWING_PATH_AREA ) return false;
    DrawingPath item = sp.mItem;
    DrawingAreaPath area = (DrawingAreaPath)item;
    int k0 = 0;
    LinePoint p0 = sp.mPoint;
    ArrayList< LinePoint > pts0 = area.mPoints;
    int size0 = pts0.size();
    for ( ; k0 < size0; ++k0 ) {
      if ( pts0.get(k0) == p0 ) break;
    }
    if ( k0 == size0 ) return false;
    // area border: ... --> p2 --> p0 --> p1 --> ...
    int k1 = (k0+1)%size0;
    int k2 = (k0+size0-1)%size0;
    LinePoint p1 = area.mPoints.get( k1 ); // next point on the area border
    LinePoint p2 = area.mPoints.get( k2 ); // prev point on the area border

    float x = p0.mX;
    float y = p0.mY;
    float thr = 10f;
    float dmin = thr; // require a minimum distance
    DrawingPointLinePath lmin = null;
    boolean min_is_area = false;
    int kk0 = -1;

    // find drawing path with minimal distance from (x,y)
    for ( DrawingPath p : mCurrentStack ) {
      if ( p == item ) continue;
      if ( p.mType != DrawingPath.DRAWING_PATH_LINE &&
           p.mType != DrawingPath.DRAWING_PATH_AREA ) continue;
      DrawingPointLinePath lp = (DrawingPointLinePath)p;
      ArrayList< LinePoint > pts = lp.mPoints;
      int size = pts.size();
      for ( int k=0; k<size; ++k ) {
        float d = pts.get(k).distance( x, y );
        if ( d < dmin ) {
          dmin = d;
          kk0 = k;
          lmin = lp;
          min_is_area = ( p.mType == DrawingPath.DRAWING_PATH_AREA );
        }
      }
    }
    if ( lmin == null ) return false;

    ArrayList< LinePoint > pts1 = lmin.mPoints;
    LinePoint pp0 = pts1.get( kk0 );
    int size1 = pts1.size();

    // try to follow p1 on the line:
    int kk1 = ( kk0+1 < size1 )? kk0 + 1 : (min_is_area)? 0 : -1; // index of next point
    int kk2 = ( kk0 > 0 )? kk0 - 1 : (min_is_area)? size1-1 : -1; // index of prev point
    int delta1 = 0; 
    int delta2 = 0;
    int kk10 = kk0;
    int kk20 = kk0;

    LinePoint pp10 = null; // current point forward
    LinePoint pp20 = null; // current point backward
    LinePoint pp1  = null; // next point forward
    LinePoint pp2  = null; // prev point backwrad
    LinePoint qq10 = null;
    LinePoint qq20 = null;
    LinePoint qq1 = null;
    LinePoint qq2 = null;
    boolean reverse = false;
    int step = 1;
    if ( kk1 >= 0 ) { // FOLLOW LINE FORWARD
      pp1  = pts1.get( kk1 );
      pp10 = pts1.get( kk0 );
      if ( kk2 >= 0 ) {
        pp2  = pts1.get( kk2 ); 
        pp20 = pts1.get( kk0 ); 
      }
      if ( pp1.distance( p1 ) < pp1.distance( p2 ) ) {
        qq1  = p1; // follow border forward
        qq10 = p0;
        delta1 = 1;
        if ( kk2 >= 0 ) {
          qq2 = p2;
          qq20 = p0;
          delta2 = size0-1;
        }
      } else {
        int k = k1; k1 = k2; k2 = k;
        reverse = true;
        qq1  = p2; // follow border backward
        qq10 = p0;
        delta1 = size0-1;
        if ( kk2 >= 0 ) {
          qq2 = p1;
          qq20 = p0;
          delta2 = 1;
        }
      }
    } else if ( kk2 >= 0 ) {
      pp2  = pts1.get( kk2 ); 
      pp20 = pts1.get( kk0 ); 
      if ( pp2.distance( p2 ) < pp2.distance( p1 ) ) {
        qq2 = p2;
        qq20 = p0;
        delta2 = size0-1;
      } else {
        int k = k1; k1 = k2; k2 = k;
        reverse = true;
        qq2 = p1;
        qq20 = p0;
        delta2 = 1;
      }
    } else {  // nothing to follow
      return false;
    }

    if ( qq1 != null ) {
      // follow line pp10 --> pp1 --> ... using step 1
      // with border qq10 --> qq1 --> ... using step delta1
      for (;;) { // try to move qq1 forward
        float s = project( qq1, pp10, pp1 );
        while ( s > 1.0 ) {
          kk1 = ( kk1+1 < size1 )? kk1 + 1 : (min_is_area)? 0 : -1;
          if ( kk1 < 0 || kk1 == kk0 ) break;
          pp10 = pp1;
          pp1 = pts1.get( kk1 );
          s = project( qq1, pp10, pp1 );
        }
        float d1 = distance( qq1, pp10, pp1 );
        if ( s < 0.0f ) break;
        if ( d1 > thr || d1 < 0.001f ) break; 
        qq10 = qq1;
        k1 = (k1+delta1)%size0;
        if ( k1 == k0 ) break;
        qq1 = pts0.get( k1 );
      }
    }

    if ( qq2 != null ) {
      // follow line pp20 --> pp2 --> ... using step size1-1
      // with border qq20 --> qq2 --> ... using step delta2
      for (;;) { // try to move qq1 forward
        float s = project( qq2, pp20, pp2 );
        while ( s > 1.0 ) {
          kk2 = ( kk2 > 0 )? kk2 - 1 : (min_is_area)? size1-1 : -1;
          if ( kk2 < 0 || kk2 == kk0 ) break;
          pp20 = pp2;
          pp2 = pts1.get( kk2 );
          s = project( qq2, pp20, pp2 );
        }
        float d2 = distance( qq2, pp20, pp2 );
        if ( s < 0.0f ) break;
        if ( d2 > thr || d2 < 0.001f ) break; 
        qq20 = qq2;
        k2 = (k2+delta2)%size0;
        if ( k2 == k0 ) break;
        qq2 = pts0.get( k2 );
      }
    }
    if ( reverse ) { int k=k1; k1=k2; k2=k; }
    // k2 and k1 are kept
    ArrayList< LinePoint > pts2 = new ArrayList< LinePoint >();
    LinePoint prev = null;
    for (int k=k1; k!=k2; k=(k+1)%size0 ) {
      prev = new LinePoint(pts0.get(k), prev);
      pts2.add( prev );
    }
    prev = new LinePoint(pts0.get(k2), prev );
    pts2.add( prev );
    if ( reverse ) {
      for ( int k = (kk1+size1-1)%size1; k != kk2; k = (k+size1-1)%size1 ) {
        prev = new LinePoint( pts1.get(k), prev );
        pts2.add( prev );
      }
    } else {
      for ( int k = (kk2+1)%size1; k != kk1; k=(k+1)%size1 ) {
        prev = new LinePoint( pts1.get(k), prev );
        pts2.add( prev );
      }
    }
    synchronized( mCurrentStack ) {
      synchronized( mSelection ) {
        mSelection.removePath( area );
        area.mPoints = pts2;
        area.retracePath();
        mSelection.insertPath( area );
      }
      clearSelected();
    }
    return true;
  }

  SelectionPoint hotItem()
  {
    return mSelected.mHotItem;
  }

  void shiftHotItem( float dx, float dy )
  {
    mSelected.shiftHotItem( dx, dy );
  }

  SelectionPoint nextHotItem()
  {
    return mSelected.nextHotItem();
  }

    SelectionPoint prevHotItem()
    {
      return mSelected.prevHotItem();
    }

    void clearSelected()
    {
      synchronized( mSelected ) {
        mSelected.clear();
      }
    }

  public void exportTherion( BufferedWriter out, String scrap_name, String proj_name )
  {
    try { 
      out.write("encoding utf-8");
      out.newLine();
      out.newLine();
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v. %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );
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
      synchronized( mCurrentStack ) {
        final Iterator i = mCurrentStack.iterator();
        while ( i.hasNext() ) {
          final DrawingPath p = (DrawingPath) i.next();
          if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)p;
            out.write( pp.toTherion() );
            out.newLine();
          } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
            if ( ! TopoDroidApp.mAutoStations ) {
              DrawingStationPath st = (DrawingStationPath)p;
              // Log.v( TopoDroidApp.TAG, "save station to Therion " + st.mName );
              out.write( st.toTherion() );
              out.newLine();
            }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)p;
            // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion line " + lp.lineType() + "/" + DrawingBrushPaths.mLineLib.mLineWallIndex );
            ArrayList< LinePoint > pts = lp.mPoints;
            if ( pts.size() > 1 ) {
              if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
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
            }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)p;
            // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion area " + ap.areaType() );
            out.write( ap.toTherion() );
            out.newLine();
          }
        }
      }
      out.newLine();

      if ( TopoDroidApp.mAutoStations ) {
        TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion auto-stations: nr. " + mStations.size() );
        TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "bbox " + xmin + ".." + xmax + " " + ymin + ".." + ymax );
        for ( DrawingStationName st : mStations ) {
          // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "stations " + st.cx + " " + st.cy );
          // FIXME if station is in the convex hull of the lines
          if ( xmin > st.cx || xmax < st.cx ) continue;
          if ( ymin > st.cy || ymax < st.cy ) continue;
          float u = st.cx + st.cy;
          float v = st.cx - st.cy;
          if ( umin > u || umax < u ) continue;
          if ( vmin > v || vmax < v ) continue;
          // TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "writing" );
          out.write( st.toTherion() );
          out.newLine();
        }
        out.newLine();
        out.newLine();
      } else {
        TopoDroidApp.Log(  TopoDroidApp.LOG_PLOT, "exportTherion NO auto-stations: nr. " + mStations.size() );
      }

      out.write("endscrap");
      out.newLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  void exportAsCsx( PrintWriter pw )
  {
    synchronized( mCurrentStack ) {
      pw.format("    <layers>\n");

      // LAYER 0: images and sketches
      pw.format("      <layer name=\"Base\" type=\"0\">\n");
      pw.format("         <items>\n");
      pw.format("         </items>\n");
      pw.format("      </layer>\n");

      // LAYER 1: soil areas
      pw.format("      <layer name=\"Soil\" type=\"1\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)p;
          if ( DrawingBrushPaths.getAreaCsxLayer( ap.mAreaType ) != 1 ) continue;
          ap.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 2: 
      pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
          lp.toCsurvey( pw );
        } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)p;
          if ( DrawingBrushPaths.getAreaCsxLayer( ap.mAreaType ) != 2 ) continue;
          ap.toCsurvey( pw );
        } 
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 3
      pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
	if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
	  DrawingLinePath lp = (DrawingLinePath)p;
	  if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
	  lp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 4
      pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 4 ) continue;
          lp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 5:
      pw.format("      <layer name=\"Borders\" type=\"5\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath lp = (DrawingLinePath)p;
          if ( DrawingBrushPaths.getLineCsxLayer( lp.mLineType ) != 5 ) continue;
          lp.toCsurvey( pw );
        }
        // if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
        //   // linetype: 0 line, 1 spline, 2 bezier
        //   pw.format("          <item layer=\"5\" name=\"\" type=\"4\" category=\"1\" linetype=\"0\" mergemode=\"0\">\n");
        //   pw.format("            <pen type=\"1\" />\n");
        //   pw.format("            <points data=\"");
        //   ArrayList< LinePoint > pts = lp.mPoints;
        //   boolean b = true;
        //   for ( LinePoint pt : pts ) {
        //     float x = DrawingActivity.sceneToWorldX( pt.mX );
        //     float y = DrawingActivity.sceneToWorldY( pt.mY );
        //     pw.format("%.2f %.2f ", x, y );
        //     if ( b ) { pw.format("B "); b = false; }
        //   }
        //   pw.format("\" />\n");
        //   pw.format("          </item>\n");
        // }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");

      // LAYER 6: signs and texts
      pw.format("      <layer name=\"Signs\" type=\"6\">\n");
      pw.format("        <items>\n");
      for ( DrawingPath p : mCurrentStack ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath pp = (DrawingPointPath)p;
          if ( DrawingBrushPaths.getPointCsxLayer( pp.mPointType ) != 6 ) continue;
          pp.toCsurvey( pw );
        }
      }
      pw.format("        </items>\n");
      pw.format("      </layer>\n");
      pw.format("    </layers>\n");
    }
  }
}
