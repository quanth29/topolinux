/** @file Units.cpp
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief units implementation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include "Units.h"

const char * UNIT_METER = "m";
const char * UNIT_FEET  = "ft";
const char * UNIT_DEGREE = "deg";
const char * UNIT_GRAD   = "grad";

Units::Units()
    : length_units( LENGTH_METER )
    , angle_units( ANGLE_DEGREE )
    , length_factor( TO_METER )
    , angle_factor( TO_DEGREE )
    , length_unit( UNIT_METER )
    , angle_unit( UNIT_DEGREE )
{ }

void 
Units::setLength( int units )
{
    length_units = units;
    if ( length_units == LENGTH_FEET ) {
      length_factor = TO_FEET;
      length_unit = UNIT_FEET;
    } else {
      length_factor = TO_METER;
      length_unit = UNIT_METER;
    }
}

void 
Units::setAngle( int units )
{
    angle_units = units;
    if ( angle_units == ANGLE_GRAD ) {
      angle_factor = TO_GRAD;
      angle_unit = UNIT_GRAD;
    } else {
      angle_factor = TO_DEGREE;
      angle_unit = UNIT_DEGREE;
    }
}

