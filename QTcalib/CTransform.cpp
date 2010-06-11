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

#include <qmessagebox.h>

#include "Factors.h"
#include "Vector.h"
#include "CTransform.h"

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

double 
CTransform::GetCoeff( const unsigned char * data )
{
  short ival = (short)( ( ((unsigned short)(data[1])) << 8 ) | (unsigned short)(data[0]) );
  return (double)(ival);
}
