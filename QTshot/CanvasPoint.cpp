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
#include "CanvasExtend.h"

bool compareCanvasExtend( const CanvasExtend & ce1, const CanvasExtend & ce2 )
{ return ce1.angle < ce2.angle; }

/*
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
*/

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
      double ha = (*it)->horiz_angle; if ( strcmp((*it)->p0, name) == 0 ) {
        temp.push_back( CanvasExtend( DIR_LEFT, ha ) );
      } else if ( strcmp((*it)->p1, name) == 0 ) {
        ha += 180.0; 
        if ( ha >= 360.0 ) ha -= 360.0;
        temp.push_back( CanvasExtend( DIR_RIGHT, ha ) );
      }
    } else if ( ext == EXTEND_RIGHT ) {
      double ha = (*it)->horiz_angle;
      if ( strcmp((*it)->p0, name) == 0 ) {
        temp.push_back( CanvasExtend( DIR_RIGHT, ha ) );
      } else if ( strcmp((*it)->p1, name) == 0 ) {
        ha += 180.0;
        if ( ha >= 360.0 ) ha -= 360.0;
        temp.push_back( CanvasExtend( DIR_LEFT, ha ) );
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
    extends.push_back( CanvasExtend( temp[0].dir, temp[0].angle ) );
    extends.push_back( CanvasExtend( -temp[0].dir, t ) );
  } else if ( n > 1 ) {
    for ( size_t k=0; k<temp.size(); ++k ) {
      extends.push_back( CanvasExtend( temp[k].dir, temp[k].angle ) );
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
