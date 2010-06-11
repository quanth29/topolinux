/** @file CanvasPoint.h
 *
 * @author marco corvi
 * @date aug 2009
 *
 * @brief point on the plot canvas
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CANVAS_POINT_H
#define CANVAS_POINT_H

#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include <string>
#include <utility>
#include <vector>

#include "ArgCheck.h"
#include "Extend.h"
#include "CanvasSegment.h"


/** difference between two angles in [0, 360)
 * @param a1 first angle
 * @param a2 second angle
 * @return the difference angle: a1 - a2
 */
double difference( double a1, double a2 );

/** A canvas extend is a value of "extend" associated to an interval
 * of angular directions. As the CanvasExtends are used only in arrays,
 * ordered around the circle, it is enough to record only the start angle
 * of the interval, the end angle being te start angle of the next
 * CanvasExtend in the array.
 */
struct CanvasExtend
{
  int dir;      //!< direction: -1 left, 0 vertical, +1 right
  double angle; //!< angle where this extend begins

  CanvasExtend( int d, double a )
    : dir( d )
    , angle( a )
  { }

  /** operator less
   * @param ce   the other CanvasExtend
   * @return true if this angle is less than the other CanvasExtend angle
   */
  bool operator < ( const CanvasExtend & ce ) { return angle < ce.angle; }

  double operator-( const CanvasExtend & ce ) { return difference( angle, ce.angle ); }

  double operator-( double a ) { return difference( angle, a ); }

};


/** bisector of two angles in [0, 360)
 * @param a1 first angle
 * @param a2 second angle
 * @return the bisector angle (between a1 and a2)
 */
double bisector( double a1, double a2 );

double bisector( double a1, const CanvasExtend & a2 );
double bisector( const CanvasExtend & a1, double a2 );
double bisector( const CanvasExtend & a1, const CanvasExtend & a2 );

/** Canvas point on the plots.
 *
 * A canvas point has the following attributes
 *    - a name: in case it correspond to a station, this is the station name
 *    - "world" coords, east, north, altitude (z), and horizontal displacement (h)
 *    - plot coordinates, x0, y0 (and z0, depth, for the 3D
 *
 * It has also a pointer to the next point in the list, a list of pointers to the
 * canvas segment that have the point as endpoint, and an array of angular interval
 * to describe the "extend" for the directions around the point.
 */
class CanvasPoint
{
public:
    const char * name;   //!< point name if any (station name)
    double n, e, z, h;   //!< north, east, vertical, horizontal
    int x0;              //!< plot X coord.: east / horizontal (to the right)
    int y0;              //!< plot Y coord.: north / vertical
    int z0;              //!< plot depth coorinate (3D only)
    CanvasPoint * next;
    std::vector< CanvasSegment * > segments; //!< segment at the point
    std::vector< CanvasExtend > extends;     //!< arrays of extends interval around 360

  public:
    /** default cstr
     */
    CanvasPoint()
      : name( NULL )
      , n( 0.0 )
      , e( 0.0 )
      , z( 0.0 )
      , h( 0.0 )
      , x0( 0 )
      , y0( 0 )
      , z0( 0 )
      , next( NULL )
    { }

    /** check if among the segments of the point pt there is one with a given name
     * @param n   name of the other point
     * @return true if the point has a segment with the other endpoint of the given name
     */
    bool HasSegmentTo( const char * n )
    {
      if ( n == NULL || strlen(n) == 0 ) return false;
      for ( std::vector< CanvasSegment * >::iterator it = segments.begin();
            it != segments.end();
            ++ it ) {
        if (    strcmp( (*it)->p0, n ) == 0 
             && strcmp( (*it)->p1, this->name ) == 0 ) 
          return true;
        if (    strcmp( (*it)->p1, n ) == 0
             && strcmp( (*it)->p0, this->name ) == 0 ) 
          return true;
      }
      return false;
    }

    /** check if the point has a certain segment
     * @param sgm  segment (poiner)
     * @return true if the point has the given segment
     */
    bool HasSegment( CanvasSegment * sgm )
    {
      if ( sgm == NULL ) return false;
      for ( std::vector< CanvasSegment * >::iterator it = segments.begin();
            it != segments.end();
            ++ it ) {
        if ( *it == sgm ) return true;
      }
      return false;
    }


    /** compute the array of extend intervals
     */
    void evalExtends();

    /** get the extend for a given angle
     * @param a   angle [degrees]
     */
    int getExtend( double a ) const
    {
      size_t n = extends.size();
      DBG_CHECK("point %s getExtend() for %.2f (size %d)\n", name, a, n );

      if ( n == 0 ) 
        return EXTEND_RIGHT;

      if ( a < extends[0].angle ) {
        return extends[n-1].dir;
      }
      size_t k=0;
      while ( k<n && a > extends[k].angle ) ++k;
      --k;
      // extends[k].angle < a
      return extends[k].dir;
    }

};

#endif
