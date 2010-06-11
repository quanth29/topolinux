/** @file Units.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief units
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef UNITS_H
#define UNITS_H

#define LENGTH_METER  0
#define LENGTH_FEET   1

#define ANGLE_DEGREE  0
#define ANGLE_GRAD    1

#define TO_METER 1.0
#define TO_FEET  3.28083

#define TO_DEGREE 1.0
#define TO_GRAD   1.1111111 // 400.0/360.0

struct Units
{
  int length_units;     //!< 0: m,   1: ft
  int angle_units;      //!< 0: deg, 1: grad
  double length_factor;
  double angle_factor;
  const char * length_unit;
  const char * angle_unit;

  Units();

  void setLength( int units );

  void setAngle( int units );

};

#endif
