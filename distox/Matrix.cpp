/** @file Matrix.cpp
 *
 * @author marco corvi
 * @date dec. 2008
 *
 * @brief 3x3 matrices implementation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#include "Matrix.h"

Matrix 
Matrix::zero( Vector(0.0, 0.0, 0.0),
              Vector(0.0, 0.0, 0.0),
              Vector(0.0, 0.0, 0.0) );

Matrix 
Matrix::one( Vector(1.0, 0.0, 0.0),
             Vector(0.0, 1.0, 0.0),
             Vector(0.0, 0.0, 1.0) );

Matrix 
operator &( const Vector & v1, const Vector & v2 )
{
    return Matrix( v2 * v1.X(), v2 * v1.Y(), v2 * v1.Z() );
}
