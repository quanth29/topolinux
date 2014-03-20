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

import android.util.Log;

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
    mCurrentStack.clear();
    mRedoStack.clear();
    mStations.clear();

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

  void eraseAt( float x, float y ) 
  {
    SelectionSet sel = new SelectionSet();
    mSelection.selectAt( x, y, sel, false, false, false );
    if ( sel.size() > 0 ) {
      synchronized( mCurrentStack ) {
        for ( SelectionPoint pt : sel.mPoints ) {
          DrawingPath path = pt.mItem;
          if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath)path;
            int size = line.mPoints.size();
            if ( size <= 2 || ( size == 3 && pt.mPoint == line.mPoints.get(1) ) ) {
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else if ( pt.mPoint == line.mPoints.get(1) ) {
              doRemoveLinePoint( line, line.mPoints.get(0), null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              line.retracePath();
            } else if ( pt.mPoint == line.mPoints.get(size-2) ) {
              doRemoveLinePoint( line, line.mPoints.get(size-1), null );
              doRemoveLinePoint( line, pt.mPoint, pt );
              line.retracePath();
            } else {
              doSplitLine( line, pt.mPoint );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath)path;
            if ( area.mPoints.size() <= 3 ) {
              mCurrentStack.remove( path );
              synchronized( mSelection ) {
                mSelection.removePath( path );
              }
            } else {
              doRemoveLinePoint( area, pt.mPoint, pt );
              area.retracePath();
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // DrawingPointPath point = (DrawingPointPath)path;
            mCurrentStack.remove( path );
            synchronized( mSelection ) {
              mSelection.removePath( path );
            }
          }
        }
      }
    }
  }

  // called from synchronized( CurrentStack ) context
  private void doSplitLine( DrawingLinePath line, LinePoint lp )
  {
    DrawingLinePath line1 = new DrawingLinePath( line.mLineType );
    DrawingLinePath line2 = new DrawingLinePath( line.mLineType );
    if ( line.splitAt( lp, line1, line2 ) ) {
      mCurrentStack.remove( line );
      mCurrentStack.add( line1 );
      mCurrentStack.add( line2 );
      mSelection.removePath( line ); 
      mSelection.insertLinePath( line1 );
      mSelection.insertLinePath( line2 );
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
    if ( line.splitAt( lp, line1, line2 ) ) {
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

  void setScaleBar() 
  {
    if ( mCurrentStack.size() > 0 ) return;
    DrawingLinePath scale_bar = new DrawingLinePath( DrawingBrushPaths.mLineLib.mLineSectionIndex );
    scale_bar.addStartPoint( 40, 160 );
    scale_bar.addPoint( 140, 160 );  // 5 meters
    mCurrentStack.add( scale_bar );
  }


  void addCommand( DrawingPath path )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand stack size  " + mCurrentStack.size() );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "addCommand path " + path.toString() );
    mRedoStack.clear();
    mCurrentStack.add( path );
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

          //doneHandler.sendEmptyMessage(1);
        }
      }
    }
    if ( mDisplayPoints ) {
      synchronized( mSelection ) {
        float radius = 5/DrawingActivity.mZoom;
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
          float radius = 20/DrawingActivity.mZoom;
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
          radius = radius/3; // 2/DrawingActivity.mZoom;
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
      synchronized( mCurrentStack ) {
        mCurrentStack.add( redoCommand );
      }
      synchronized( mSelection ) {
        mSelection.insertPath( redoCommand );
      }
    }
  }

  public SelectionSet getItemsAt( float x, float y )
  {
    boolean legs   = (mDisplayMode & DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DISPLAY_SPLAY ) != 0;
    boolean stations = (mDisplayMode & DISPLAY_STATION ) != 0;
    synchronized ( mSelected ) {
      mSelected.clear();
      mSelection.selectAt( x, y, mSelected, legs, splays, stations );
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
  
  void moveHotItemToNearestPoint()
  {
    SelectionPoint sp = mSelected.mHotItem;
    if ( sp == null ) return;
    float x = 0.0f;
    float y = 0.0f;
    if ( sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
      x = sp.mItem.cx;
      y = sp.mItem.cy;
    } else if ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) {
      x = sp.mPoint.mX;
      y = sp.mPoint.mY;
    } else {
      return;
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
      for ( DrawingPath p : mCurrentStack ) {
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
          if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
            ArrayList< LinePoint > pts = lp.mPoints;
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

}
