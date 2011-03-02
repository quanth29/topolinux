/** @file ThPointType.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot therion point types
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef TH_POINT_TYPE_H
#define TH_POINT_TYPE_H

namespace Therion
{
  /** therion point types
   */
  enum PointType {
    THP_AIR_DRAUGHT = 0, // 0
    THP_BLOCKS,       // 1
    THP_CLAY,         // 2
    THP_CONTINUATION, // 12
    THP_DEBRIS,       // 3
    THP_ENTRANCE,     // 11
    THP_ICE,          // 6
    THP_LABEL,        // 4
    THP_PEBBLES,      //
    THP_SAND,         // 5
    THP_SNOW,
    THP_STALACTITE,   // 7
    THP_STALAGMITE,   // 8
    THP_USER,         // 9
    THP_WATER_FLOW,   // 10
    // station must be last
    THP_STATION,      
    THP_PLACEMARK
  };

  /** whether the point can have orientation
   */
  extern const bool PointHasOrientation[ THP_PLACEMARK ];

  #define THP_SUBTYPE_MAX 5
  extern const char * PointSubtype[ THP_PLACEMARK ][ THP_SUBTYPE_MAX ];
  
  extern const char * PointName[ THP_PLACEMARK ];

  #define THP_ALIGN_MAX 10
  extern const char * PointAlign[ THP_ALIGN_MAX ];

  #define THP_SCALE_MAX 6
  extern const char * PointScale[ THP_SCALE_MAX ];
}

#endif
