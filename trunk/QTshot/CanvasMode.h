/** @file CanvasMode.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief 2D plot canvas modes
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CANVAS_MODE_H
#define CANVAS_MODE_H

#define MODE_PLAN   0
#define MODE_EXT    1
#define MODE_CROSS  2
#define MODE_3D     3
#define MODE_HCROSS 4

/** canvas input modes
 */
enum InputMode {
  INPUT_COMMAND,
  INPUT_POINT,
  INPUT_LINE,
  INPUT_AREA
};

#endif

