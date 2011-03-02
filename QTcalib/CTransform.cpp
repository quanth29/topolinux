/** @file CTransform.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief OpenTopo calibration data transform
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <math.h>

#include <QMessageBox>

#include "Factors.h"
#include "Vector.h"
#include "CTransform.h"
#include "Calibration.h"

#define RAD2GRAD_FACTOR (180.0/M_PI)

typedef unsigned short uint16_t;

// ================================================================

CTransform::CTransform()
{
  G[0][0] = 0.0; G[0][1] = 1.0; G[0][2] = 0.0; G[0][3] = 0.0;
  G[1][0] = 0.0; G[1][1] = 0.0; G[1][2] = 1.0; G[1][3] = 0.0;
  G[2][0] = 0.0; G[2][1] = 0.0; G[2][2] = 0.0; G[2][3] = 1.0;
  M[0][0] = 0.0; M[0][1] = 1.0; M[0][2] = 0.0; M[0][3] = 0.0;
  M[1][0] = 0.0; M[1][1] = 0.0; M[1][2] = 1.0; M[1][3] = 0.0;
  M[2][0] = 0.0; M[2][1] = 0.0; M[2][2] = 0.0; M[2][3] = 1.0;
}

void 
CTransform::setValue( const unsigned char * byte )
{
    G[0][0] = GetCoeff( byte + 0 ) / FV;
    G[0][1] = GetCoeff( byte + 2 ) / FM;
    G[0][2] = GetCoeff( byte + 4 ) / FM;
    G[0][3] = GetCoeff( byte + 6 ) / FM;
    G[1][0] = GetCoeff( byte + 8 ) / FV;
    G[1][1] = GetCoeff( byte +10 ) / FM;
    G[1][2] = GetCoeff( byte +12 ) / FM;
    G[1][3] = GetCoeff( byte +14 ) / FM;
    G[2][0] = GetCoeff( byte +16 ) / FV;
    G[2][1] = GetCoeff( byte +18 ) / FM;
    G[2][2] = GetCoeff( byte +20 ) / FM;
    G[2][3] = GetCoeff( byte +22 ) / FM;

    M[0][0] = GetCoeff( byte +24 ) / FV;
    M[0][1] = GetCoeff( byte +26 ) / FM;
    M[0][2] = GetCoeff( byte +28 ) / FM;
    M[0][3] = GetCoeff( byte +30 ) / FM;
    M[1][0] = GetCoeff( byte +32 ) / FV;
    M[1][1] = GetCoeff( byte +34 ) / FM;
    M[1][2] = GetCoeff( byte +36 ) / FM;
    M[1][3] = GetCoeff( byte +38 ) / FM;
    M[2][0] = GetCoeff( byte +40 ) / FV;
    M[2][1] = GetCoeff( byte +42 ) / FM;
    M[2][2] = GetCoeff( byte +44 ) / FM;
    M[2][3] = GetCoeff( byte +46 ) / FM;
}

void
CTransform::setValue( const Calibration * calib )
{
  const Vector & bG = calib->GetBG();
  const Matrix & aG = calib->GetAG();
  const Vector & bM = calib->GetBM();
  const Matrix & aM = calib->GetAM();

  G[0][0] = bG.X();
  G[0][1] = aG.X().X();
  G[0][2] = aG.X().Y();
  G[0][3] = aG.X().Z();

  G[1][0] = bG.Y();
  G[1][1] = aG.Y().X();
  G[1][2] = aG.Y().Y();
  G[1][3] = aG.Y().Z();

  G[2][0] = bG.Z();
  G[2][1] = aG.Z().X();
  G[2][2] = aG.Z().Y();
  G[2][3] = aG.Z().Z();

  M[0][0] = bM.X();
  M[0][1] = aM.X().X();
  M[0][2] = aM.X().Y();
  M[0][3] = aM.X().Z();

  M[1][0] = bM.Y();
  M[1][1] = aM.Y().X();
  M[1][2] = aM.Y().Y();
  M[1][3] = aM.Y().Z();

  M[2][0] = bM.Z();
  M[2][1] = aM.Z().X();
  M[2][2] = aM.Z().Y();
  M[2][3] = aM.Z().Z();
}

double 
CTransform::GetCoeff( const unsigned char * data )
{
  short ival = (short)( ( ((unsigned short)(data[1])) << 8 ) | (unsigned short)(data[0]) );
  return (double)(ival);
}

void 
CTransform::ComputeCompassAndClino( const Vector & g0, const Vector & m0, 
                    double & compass, double & clino, double & roll ) const
{
  Vector g;
  Vector m;

  g.X() = G[0][0] + G[0][1] * g0.X() + G[0][2] * g0.Y() + G[0][3] * g0.Z();
  g.Y() = G[1][0] + G[1][1] * g0.X() + G[1][2] * g0.Y() + G[1][3] * g0.Z();
  g.Z() = G[2][0] + G[2][1] * g0.X() + G[2][2] * g0.Y() + G[2][3] * g0.Z();

  m.X() = M[0][0] + M[0][1] * m0.X() + M[0][2] * m0.Y() + M[0][3] * m0.Z();
  m.Y() = M[1][0] + M[1][1] * m0.X() + M[1][2] * m0.Y() + M[1][3] * m0.Z();
  m.Z() = M[2][0] + M[2][1] * m0.X() + M[2][2] * m0.Y() + M[2][3] * m0.Z();
  g.normalize();
  m.normalize();
  Vector e( 1.0, 0.0, 0.0 );  // Disto axis
  Vector y = m % g;           // neg. Earth Y axis
  Vector x = g % (m % g);     // Earth X axis
                              // Earth Z axis pointing down as "g"
#if 0
  Vector e1 = g % (e % g);
  clino = acos( g.X() / g.length() ) - M_PI/2;
  // double sc = sin( clino );
  // double cc = cos( clino );
  Vector em1 = e1 % x;
  double sb = em1.length() * ( ( em1*g > 0 ) ? -1.0 : 1.0 );
  double cb = e1 * x;
  compass = atan2( sb, cb );
#else
  y.normalize();
  x.normalize();
  double ex = e*x;
  double ey = e*y;
  double ez = e*g;

  compass = atan2( -ey, ex );
  clino   = atan2( ez, sqrt(ex*ex+ey*ey) );
#endif
  roll    = atan2( g.Y(), g.Z() );
  if ( compass < 0.0 ) compass += 2*M_PI;
  if ( roll < 0.0 ) roll += 2*M_PI;
  clino   *= RAD2GRAD_FACTOR;
  compass *= RAD2GRAD_FACTOR;
  roll    *= RAD2GRAD_FACTOR;
}

void
CTransform::DefaultCompassAndClino( const Vector & g0, const Vector & m0, 
                    double & compass, double & clino, double & roll )
{
  Vector g( g0 );
  Vector m( m0 );
  g.normalize();
  m.normalize();
  Vector e( 1.0, 0.0, 0.0 );
  Vector m1 = g % (m % g);
  Vector e0 = g % (e % g);
  clino = acos( g.X() / g.length() ) - M_PI/2;
  // double sc = sin( clino );
  // double cc = cos( clino );
  Vector em1 = e0 % m1;
  double sb = em1.length() * ( ( em1*g > 0 ) ? -1.0 : 1.0 );
  double cb = e0 * m1;
  compass = atan2( sb, cb );
  if ( compass < 0.0 ) compass += 2*M_PI;
  double sr = g.Y();
  double cr = g.Z();
  roll = atan2( sr, cr);
  if ( roll < 0.0 ) roll += 2*M_PI;
  clino *= RAD2GRAD_FACTOR;
  compass *= RAD2GRAD_FACTOR;
  roll *= RAD2GRAD_FACTOR;
}

