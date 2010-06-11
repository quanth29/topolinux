/** @file CanvasSegment.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief segment on the plot canvas
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CANVAS_SEGMENT_H
#define CANVAS_SEGMENT_H

#include "DBlock.h"

/** Canvassegment types
 */
#define CS_CENTERLINE 0
#define CS_SPLAY      1
#define CS_LRUD       2

/** Survey segment on the plot, either centerline shot or splay shot
 */
struct CanvasSegment 
{
  int x0, y0, x1, y1;  //!< plot coordinates
  int z0, z1;          //!< plot depth coords
  const char * p0;     //!< endpoint name
  const char * p1;     //!< endpoint name
  double horiz_angle;  //!< horizontal-plane angle
  DBlock * block;      //!< data block
  unsigned char cs_extend; //!< extend: 0 none, 1 left, 2 right, 3 vert., 4 ignore
  unsigned char cs_type;   //!< type: 0 centerline, 1 splay, 2 LRUD
  CanvasSegment * next;
};

#endif
