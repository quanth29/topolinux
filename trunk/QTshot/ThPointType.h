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

/** therion point types
 */
enum ThPointType {
  THP_AIR = 0,      // 0
  THP_BLOCK,        // 1
  THP_CLAY,         // 2
  THP_DEBRIS,       // 3
  THP_LABEL,        // 4
  THP_SAND,         // 5
  THP_STALACTITE,   // 6
  THP_USER,         // 7
  THP_WATER,        // 8
  THP_ENTRANCE,     // 9
  THP_CONTINUATION, // 10
  THP_STATION,      // station must be last
  THP_PLACEMARK
};

#endif
