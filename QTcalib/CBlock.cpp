/** @file CBlock.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief OpenTopo calibration data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <math.h>

#include "Factors.h"
#include "Vector.h"
#include "CBlock.h"

#define RAD2GRAD_FACTOR (180.0/M_PI)

typedef unsigned short uint16_t;

// ================================================================

#if 0
void
CBlock::ComputeCompassAndClino()
{
  Vector g( gx, gy, gz );
  Vector m( mx, my, mz );
  Vector e( 1.0, 0.0, 0.0 );
  Vector m0 = g % (m % g);
  Vector e0 = g % (e % g);
  clino = acos( g.X() / g.length() ) - M_PI/2;
  // double sc = sin( clino );
  // double cc = cos( clino );
  Vector em0 = e0 % m0;
  double sb = em0.length() * ( ( em0*g > 0 ) ? -1.0 : 1.0 );
  double cb = e0 * m0;
  compass = atan2( sb, cb );
  if ( compass < 0.0 ) compass += 2*M_PI;
  double sr = gy;
  double cr = gz;
  roll = atan2( sr, cr);
  if ( roll < 0.0 ) roll += 2*M_PI;
  clino   *= RAD2GRAD_FACTOR;
  compass *= RAD2GRAD_FACTOR;
  roll    *= RAD2GRAD_FACTOR;
}
#endif

#if 0
void
CBlock::ComputeCompassAndClino( const CTransform & t )
{
  Vector g;
  Vector m;
  g.X() = t.G[0][0] + t.G[0][1] * gx + t.G[0][2] * gy + t.G[0][3] * gz;
  g.Y() = t.G[1][0] + t.G[1][1] * gx + t.G[1][2] * gy + t.G[1][3] * gz;
  g.Z() = t.G[2][0] + t.G[2][1] * gx + t.G[2][2] * gy + t.G[2][3] * gz;
  m.X() = t.M[0][0] + t.M[0][1] * mx + t.M[0][2] * my + t.M[0][3] * mz;
  m.Y() = t.M[1][0] + t.M[1][1] * mx + t.M[1][2] * my + t.M[1][3] * mz;
  m.Z() = t.M[2][0] + t.M[2][1] * mx + t.M[2][2] * my + t.M[2][3] * mz;
  g *= 1.0/g.length();
  m *= 1.0/m.length();
  Vector e( 1.0, 0.0, 0.0 );
  Vector m0 = g % (m % g);
  Vector e0 = g % (e % g);
  clino = acos( g.X() / g.length() ) - M_PI/2;
  // double sc = sin( clino );
  // double cc = cos( clino );
  Vector em0 = e0 % m0;
  double sb = em0.length() * ( ( em0*g > 0 ) ? -1.0 : 1.0 );
  double cb = e0 * m0;
  compass = atan2( sb, cb );
  if ( compass < 0.0 ) compass += 2*M_PI;
  double sr = gy;
  double cr = gz;
  roll = atan2( sr, cr);
  if ( roll < 0.0 ) roll += 2*M_PI;
  clino *= RAD2GRAD_FACTOR;
  compass *= RAD2GRAD_FACTOR;
  roll *= RAD2GRAD_FACTOR;
}
#endif



