/** @file TherionPoint.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot therion point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef THERION_POINT_H
#define THERION_POINT_H

#include <string>

#include "ThPointType.h"

/** therion point
 */
struct ThPoint
{
  double x;
  double y;

  ThPoint( double x0 = 0.0, double y0 = 0.0 )
    : x( x0 )
    , y( y0 )
  { }
};

/** plot therion point
 */
struct ThPoint2D : public ThPoint
{
  ThPointType type;   //!< point type
  std::string option; //!< point option string
  int orientation;    //!< point orientation

  ThPoint2D( double x0 = 0.0, double y0 = 0.0, 
             ThPointType t = THP_USER, 
             int orient = 0,
             const char * opt = NULL )
    : ThPoint( x0, y0 )
    , type( t )
    , orientation( 0 )
  {
    if ( type == THP_WATER || type == THP_AIR || type == THP_ENTRANCE )
      orientation = orient;
    if ( opt ) option = opt;
  } 

};

#endif
