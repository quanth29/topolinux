/* @file Selection.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief Selection among drawing items
 */
package com.android.DistoX;

import android.util.Log;

class Selection
{
  float x0, y0, x1, y1;
  float delta;
  float delta2;
  int N, M;
  SelectionBucket[] mBucket;

  Selection( float x00, float x10, float y00, float y10, float delta0 )
  {
    x0 = x00;
    y0 = y00;
    x1 = x10;
    y1 = y10;
    delta = delta0 * 2;
    N = M = 1;
    while ( N*M < 100 && delta > 1.0f ) {
      delta /= 2;
      N = 1+(int)((x1-x0)/delta);
      M = 1+(int)((y1-y0)/delta);
    }
    delta2 = delta/2;
    float d = ((N*delta) - (x1-x0))/2;
    x1 -= d;
    x0 += d;
    d = ((M*delta) - (y1-y0))/2;
    y1 -= d;
    y0 += d;
    mBucket = new SelectionBucket[N*M];
    for (int k=0; k<N*M; ++k ) mBucket[k] = new SelectionBucket( );
  }

  private void resetBuckets()
  {
    for (int k=0; k<N*M; ++k ) {
      mBucket[k].reset();
    }
  }

  void insertStationName( DrawingStationName st )
  {
    insertItem( st.mX, st.mY, st );
  }

  void insertPath( DrawingPath path )
  {
    LinePoint p1, p2;
    switch ( path.mType ) {
      case DrawingPath.DRAWING_PATH_FIXED:
      case DrawingPath.DRAWING_PATH_SPLAY:
        insertItem( path.cx, path.cy, path );
        break;
      case DrawingPath.DRAWING_PATH_GRID:
        // nothing
        break;
      case DrawingPath.DRAWING_PATH_STATION:
        DrawingStationPath sp = (DrawingStationPath)path;
        insertItem( sp.mXpos, sp.mYpos, path );
        break;
      case DrawingPath.DRAWING_PATH_POINT:
        DrawingPointPath pp = (DrawingPointPath)path;
        insertItem( pp.mXpos, pp.mYpos, path );
        break;
      case DrawingPath.DRAWING_PATH_LINE:
        DrawingLinePath lp = (DrawingLinePath)path;
        p1 = lp.points.get(0);
        for (int k = 1; k < lp.points.size(); ++k ) {
          p2 = lp.points.get(k);
          insertItem( (p1.mX+p2.mX)/2, (p1.mY+p2.mY)/2, path );
          p1 = p2;
        }
        break;
      case DrawingPath.DRAWING_PATH_AREA:
        DrawingAreaPath ap = (DrawingAreaPath)path;
        p1 = ap.points.get(0);
        for (int k = 1; k < ap.points.size(); ++k ) {
          p2 = ap.points.get(k);
          insertItem( (p1.mX+p2.mX)/2, (p1.mY+p2.mY)/2, path );
          p1 = p2;
        }
        break;
      default:
    }
  }

  private void insertItem( float xx, float yy, DrawingPath path )
  {
    SelectionPoint point = new SelectionPoint( xx, yy, path );
    int n = (int)( (xx - x0)/delta);
    int m = (int)( (yy - y0)/delta);
    if ( n < 0 || n >= N ) return;
    if ( m < 0 || m >= M ) return;
    mBucket[ m*N + n].addPoint( point );
  }

  void removeReferencesTo( DrawingPath path )
  {
    // FIXME use path to call only relevant buckets
    for (int k=0; k<N*M; ++k ) mBucket[k].removeReferenceTo( path );
  }

  private int n1, n2, m1, m2; // search index bounds
  private int max_iter;

  private boolean prepareSearchBounds( float xx, float yy, float max_distance )
  {
    max_iter = 1 + (int)(max_distance/delta);
    if ( xx < x0 || yy < y0 ) return false;
    int nn = (int)((xx-x0)/delta);
    int mm = (int)((yy-y0)/delta);
    if ( nn >= N || mm >= M ) return false;

    if ( xx - nn*delta < delta2 ) {
      n1 = (nn==0)? 0 : nn-1;
      n2 = nn+1;
    } else {
      n1 = nn;
      n2 = nn+2; if (n2>N) n2=N;
    }
    if ( yy - mm*delta < delta2 ) {
      m1 = (mm==0)? 0 : mm-1;
      m2 = mm+1;
    } else {
      m1 = mm;
      m2 = mm+2; if (m2>M) m2=M;
    }
    resetBuckets();
    return true;
  }

  private void incrementSearchBounds() 
  {
    if ( n1 > 0 ) n1--;
    if ( m1 > 0 ) m1--;
    if ( n2 < N ) n2++;
    if ( m2 < M ) m2++;
  }

  // void dump()
  // {
  //   for ( int n0=0; n0<N; ++n0 ) {
  //     for ( int m0=0; m0<M; ++m0 ) {
  //       if ( ! mBucket[m0*N+n0].isEmpty() ) {
  //         mBucket[m0*N+n0].dump();
  //       }
  //     }
  //   }
  // }

  SelectionPoint getClosestItem( float xx, float yy, float max_distance, boolean legs, boolean splays )
  {
    if ( ! prepareSearchBounds( xx, yy, max_distance ) ) return null;

    SelectionPoint ret = null;
    for ( int iter = 0; iter < max_iter; ++iter ) {
      for ( int n0=n1; n0<n2; ++n0 ) {
        for ( int m0=m1; m0<m2; ++m0 ) {
	  if ( mBucket[m0*N+n0].mChecked == false ) {
            if ( legs ) {
	      SelectionPoint leg_point = mBucket[m0*N+n0].closestPoint( xx, yy, DrawingPath.DRAWING_PATH_FIXED );
              if ( leg_point != null && ( ret == null || leg_point.mDistance < ret.mDistance ) ) {
	        ret = leg_point;
	      }
            }
            if ( splays ) {
              SelectionPoint splay_point = mBucket[m0*N+n0].closestPoint( xx, yy, DrawingPath.DRAWING_PATH_SPLAY );
	      if ( splay_point != null && ( ret == null || splay_point.mDistance < ret.mDistance ) ) {
	        ret = splay_point;
	      }
            }
	  }
	}
      }
      if ( ret != null ) break;
      incrementSearchBounds();
    }
    return ret;
  }

  SelectionPoint getClosestItem( float xx, float yy, float max_distance, int type )
  { 
    if ( ! prepareSearchBounds( xx, yy, max_distance ) ) return null;

    SelectionPoint ret = null;
    for ( int iter = 0; iter < max_iter; ++iter ) {
      for ( int n0=n1; n0<n2; ++n0 ) {
        for ( int m0=m1; m0<m2; ++m0 ) {
	  if ( mBucket[m0*N+n0].mChecked == false ) {
	    SelectionPoint point = mBucket[m0*N+n0].closestPoint( xx, yy, type );
	    if ( point != null && ( ret == null || point.mDistance < ret.mDistance ) ) {
	      ret = point;
	    }
	  }
	}
      }
      if ( ret != null ) break;
      incrementSearchBounds();
    }
    return ret;
  }

}
