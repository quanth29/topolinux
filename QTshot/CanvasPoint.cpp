/** @file CanvasPoint.cpp
 *
 * @author marco corvi
 * @date jan 2010
 * 
 * @brief implements CanvasPoint methods
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <assert.h>

#include <algorithm>

#include "ArgCheck.h"

#include "CanvasPoint.h"
#include "Extend.h"


double bisector( double a1, double a2 )
{
  double a = (a1+a2)/2.0;
  if ( a1 > a2 ) a += 180.0;
  if ( a > 360 ) a -= 360.0;
  return a;
}

double bisector( double a1, const CanvasExtend & a2 )
{ return bisector( a1, a2.angle ); }

double bisector( const CanvasExtend & a1, double a2 )
{ return bisector( a1.angle, a2 ); }

double bisector( const CanvasExtend & a1, const CanvasExtend & a2 )
{ return bisector( a1.angle, a2.angle ); }


double difference( double a1, double a2 )
{
  if ( a1 > a2 ) 
    return a1 - a2;
  return 360 + a1 - a2;
}

bool compareCanvasExtend( const CanvasExtend & ce1, const CanvasExtend & ce2 )
{ return ce1.angle < ce2.angle; }


void
CanvasPoint::evalExtends()
{
  DBG_CHECK("point %s evalExtends() segments %d \n", name, segments.size() );

  extends.clear();
  std::vector< CanvasExtend > temp;
  for ( std::vector< CanvasSegment * >::iterator it = segments.begin();
        it != segments.end();
        ++ it ) {
    unsigned char ext = (*it)->cs_extend;
    // DBG_CHECK("segment %s %s extend %d\n", (*it)->p0, (*it)->p1, ext );
    if ( ext == EXTEND_LEFT ) {
      double ha = (*it)->horiz_angle;
      if ( strcmp((*it)->p0, name) == 0 ) {
        temp.push_back( CanvasExtend( -1, ha ) );
      } else if ( strcmp((*it)->p1, name) == 0 ) {
        ha += 180.0; 
        if ( ha >= 360.0 ) ha -= 360.0;
        temp.push_back( CanvasExtend( +1, ha ) );
      }
    } else if ( ext == EXTEND_RIGHT ) {
      double ha = (*it)->horiz_angle;
      if ( strcmp((*it)->p0, name) == 0 ) {
        temp.push_back( CanvasExtend( +1, ha ) );
      } else if ( strcmp((*it)->p1, name) == 0 ) {
        ha += 180.0;
        if ( ha >= 360.0 ) ha -= 360.0;
        temp.push_back( CanvasExtend( -1, ha ) );
      }
    }
  }
  std::sort( temp.begin(), temp.end(), compareCanvasExtend );
  size_t n = temp.size();
  if ( n == 0 ) {
    DBG_CHECK("WARNING no temp extend for point %s\n", name );
  } else if ( n == 1 ) {
    //              opposite
    //               |0
    //          a2 \ | / a1
    //        -dir  \|/
    //    t ---------+------> angle
    //              / \ +dir
    //             / 0 \  ...
    //
    double t = temp[0].angle + 180; // opposite angle
    if ( t >= 360 ) t -= 360;
    double opposite = bisector( temp[0].angle, t );
    double a1 = bisector( temp[0].angle, opposite );
    double a2 = bisector( opposite, t );
    extends.push_back( CanvasExtend(0, a1) );
    extends.push_back( CanvasExtend(-temp[0].dir, a2) );
    a1 += 180; if ( a1 >= 360 ) a1 -= 360;
    a2 += 180; if ( a2 >= 360 ) a2 -= 360;
    extends.push_back( CanvasExtend(0, a1) );
    extends.push_back( CanvasExtend(temp[0].dir, a2) );
  } else if ( n > 1 ) {
    size_t start = 0;
    int dir = temp[start].dir;
    while ( start < n && temp[start].dir == dir ) ++start;
    // start is the first with dir != temp[0].dir
    if ( start == n ) { // only one extend
      double max = temp[0] - temp[n-1];
      size_t kmax = 0;
      for (size_t k=1; k<n; ++k ) {
        double d = temp[k] - temp[k-1];
        if ( d > max ) { max = d; kmax = k; }
      }
      size_t kmax1 = (kmax+n-1)%n;
      // printf("Point %s max %.2f %.2f\n", name, temp[kmax1].angle, temp[kmax].angle);
      // order:   kmax1 ------ opposite ------ kmax
      //                  a1              a2 
      //        dir ...... | .... -dir ... | ... dir ...
      // TODO put vertical in between
      double opposite = bisector( temp[kmax1], temp[kmax] );
      double a2 = bisector( opposite, temp[kmax] );
      double a1 = bisector( temp[kmax1], opposite );
      extends.push_back( CanvasExtend( -dir, a1 ) );
      extends.push_back( CanvasExtend( dir, a2 ) );
    } else { // start != n
      assert( start > 0 );
      // temp[start].dir != temp[start-1].dir = ... = temp[0].dir
      size_t current = start;
      int dir = temp[start].dir;
      for (size_t k=0; k<=n; ++k ) {
        size_t k1 = (start+k)%n;
        if ( temp[k1].dir != dir ) {
          dir = temp[k1].dir;
          current = k1;
          size_t k2 = (start+k+n-1)%n;
          double opposite = bisector( temp[k2].angle, temp[k1].angle );
          double a2 = bisector( temp[k2].angle, opposite );
          double a1 = bisector( opposite, temp[k1].angle );
          extends.push_back( CanvasExtend( 0, a2 ) );
          extends.push_back( CanvasExtend( dir, a1 ) );
        }
      }
      if ( start != n-1 && temp[n-1].dir != temp[0].dir ) {
        size_t k2 = n-1;
        size_t k1 = 0;
        double opposite = bisector( temp[k2].angle, temp[k1].angle );
        double a2 = bisector( temp[k2].angle, opposite );
        double a1 = bisector( opposite, temp[k1].angle );
        extends.push_back( CanvasExtend( 0, a2 ) );
        extends.push_back( CanvasExtend( temp[k1].dir, a1 ) );
      }
    }
  }
  std::sort( extends.begin(), extends.end(), compareCanvasExtend );
  /*
  n = extends.size();
  printf("Point %s extends %d: ", name, n );
  for ( size_t k=0; k<n; ++k ) printf("%2d %6.2f ", extends[k].dir, extends[k].angle );
  printf("\n");
  */
}
