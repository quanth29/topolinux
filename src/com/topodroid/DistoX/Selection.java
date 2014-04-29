/* @file Selection.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief Selection among drawing items
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------
 * CHANGES
 * 20130627 SelectionException
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

class Selection
{
  ArrayList< SelectionPoint > mPoints;

  Selection( )
  {
    mPoints = new ArrayList< SelectionPoint >();
  }

  void clear()
  {
    mPoints.clear();
  }

  void insertStationName( DrawingStationName st )
  {
    insertItem( st, null );
  }

  /** like insertItem, but it returns the inserted SelectionPoint
   * @param path     point-line path
   * @param point    new point on the point-line
   * @return
   */
  SelectionPoint insertPathPoint( DrawingPointLinePath path, LinePoint pt )
  {
    SelectionPoint sp = new SelectionPoint( path, pt );
    mPoints.add( sp );
    return sp;
  }
  
  void insertLinePath( DrawingLinePath path )
  {
    for (int k = 0; k < path.mPoints.size(); ++k ) {
      LinePoint p2 = path.mPoints.get(k);
      // Log.v(TopoDroidApp.TAG, "sel. insert " + p2.mX + " " + p2.mY );
      insertItem( path, p2 );
    }
  }

  void insertPath( DrawingPath path )
  {
    // LinePoint p1;
    LinePoint p2;
    switch ( path.mType ) {
      case DrawingPath.DRAWING_PATH_FIXED:
      case DrawingPath.DRAWING_PATH_SPLAY:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_GRID:
        // nothing
        break;
      case DrawingPath.DRAWING_PATH_STATION:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_POINT:
        insertItem( path, null );
        break;
      case DrawingPath.DRAWING_PATH_LINE:
        DrawingLinePath lp = (DrawingLinePath)path;
        for (int k = 0; k < lp.mPoints.size(); ++k ) {
          p2 = lp.mPoints.get(k);
          insertItem( path, p2 );
        }
        break;
      case DrawingPath.DRAWING_PATH_AREA:
        DrawingAreaPath ap = (DrawingAreaPath)path;
        for (int k = 0; k < ap.mPoints.size(); ++k ) {
          p2 = ap.mPoints.get(k);
          insertItem( path, p2 );
        }
        break;
      default:
    }
  }

  void resetDistances()
  {
    for ( SelectionPoint pt : mPoints ) {
      pt.mDistance = 0.0f;
    }
  }

  private void insertItem( DrawingPath path, LinePoint pt )
  {
    mPoints.add( new SelectionPoint( path, pt ) );
  }

  void removePath( DrawingPath path )
  {
    final Iterator i = mPoints.iterator();
    if ( path.mType == DrawingPath.DRAWING_PATH_LINE || path.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)path;
      for ( LinePoint lp : line.mPoints ) {
        for ( SelectionPoint sp : mPoints ) {
          if ( sp.mPoint == lp ) {
            mPoints.remove( sp );
            break;
          }
        }
      }
    } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {  
      for ( SelectionPoint sp : mPoints ) {
        if ( sp.mItem == path ) {
          mPoints.remove( sp );
          break;
        }
      }
    }
  }

  // void removeLinePath( DrawingLinePath path )
  // {
  //   final Iterator i = mPoints.iterator();
  //   while ( i.hasNext() ) {
  //     final SelectionPoint sp = (SelectionPoint) i.next();
  //     if ( sp.mItem == path ) {
  //       // Log.v(TopoDroidApp.TAG, "sel. remove " + sp.mPoint.mX + " " + sp.mPoint.mY );
  //       mPoints.remove( i ); // FIXME
  //     }
  //   }
  // }

  void selectAt( float x, float y, float zoom, SelectionSet sel, boolean legs, boolean splays, boolean stations )
  {
    float radius = TopoDroidApp.mCloseness / zoom;
    for ( SelectionPoint sp : mPoints ) {
      if ( !legs && sp.type() == DrawingPath.DRAWING_PATH_FIXED ) continue;
      if ( !splays && sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) continue;
      if ( !stations && ( sp.type() == DrawingPath.DRAWING_PATH_STATION || sp.type() == DrawingPath.DRAWING_PATH_NAME ) ) continue;
      sp.mDistance = sp.distance(x, y);
      if ( sp.mDistance < radius ) {
        sel.addPoint( sp );
      }
    }
    // Log.v(TopoDroidApp.TAG, "selectAt " + sel.size() );
  }

}
