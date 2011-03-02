/** @file ThLineType.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot therion line types
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef TH_LINE_TYPE_H
#define TH_LINE_TYPE_H

namespace Therion
{
  /** therion line types
   */
  enum LineType {
    THL_ARROW = 0,
    THL_BORDER,
    THL_CHIMNEY,
    THL_PIT,
    THL_USER,
    THL_WALL,
    THL_ROCK,   // rock-border / rock-edge
    THL_PLACEMARK
  };
  
  extern const char * LineName[ THL_PLACEMARK ];

}

#endif
