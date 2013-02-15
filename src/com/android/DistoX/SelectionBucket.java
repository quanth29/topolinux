/** @file SelectionBucket.java
 *
 * @author marco corvi
 * @date feb 2013
 * -------------------------------------------------
 *  CHANGES
 */
package com.android.DistoX;

import java.util.List;
import java.util.ArrayList;

class SelectionBucket
{
  // could store the bucket bounds
  ArrayList< SelectionPoint > mPoints;
  int mSize;
  boolean mChecked;

  SelectionBucket( )
  {
    mPoints = new ArrayList< SelectionPoint >();
    mSize = 0;
    mChecked = false;
  }

  boolean isEmpty() { return mSize == 0; }

  void reset() { mChecked = (mSize == 0); }

  void addPoint( SelectionPoint point ) 
  {
    mPoints.add( point );
    mSize = mPoints.size();
  }

  // void dump()
  // {
  //   for (SelectionPoint p : mPoints ) {
  //     p.dump();
  //   }
  // }

  void removeReferenceTo( DrawingPath path )
  {
    int k = 0;
    while ( k < mPoints.size() ) {
      if ( mPoints.get(k).item == path ) {
        mPoints.remove( k );
      } else {
        ++k;
      }
    }
    mSize = k; // mPoints.size();
  }

  SelectionPoint closestPoint( float xx, float yy, int type ) 
  {
    SelectionPoint ret = null;
    for ( SelectionPoint point : mPoints ) {
      if ( point.item.mType == type ) {
        point.evalDistance( xx, yy );
        if ( ret == null || point.mDistance < ret.mDistance ) {
          ret = point;
        }
      }
    }
    return ret;
  }
	
}

