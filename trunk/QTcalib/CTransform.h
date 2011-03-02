/** @file CTransform.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief topolinux calibration data transform
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef C_TRANSFORM_H
#define C_TRANSFORM_H

#include <stdio.h>

class Calibration;

struct CTransform
{
  double G[3][4]; // bGx aGx[x] aGx[y] aGx[z]
  double M[3][4];

  /** default cstr
   */
  CTransform();

  /** cstr
   * @param byte   byte array of the transform coeffs
   */
  CTransform( const unsigned char * byte )
  {
    setValue( byte );
  }

  CTransform( const Calibration * calib )
  {
    setValue( calib );
  }

  void setValue( const Calibration * calib );

  void setValue( const unsigned char * byte );

  void dump()
  {
    for (int i=0; i<3; ++i) {
      printf("G[%1d]: %8.2f  %7.4f %7.4f %7.4f\n", 
        i+1, G[i][0], G[i][1], G[i][2], G[i][3] );
    }
    for (int i=0; i<3; ++i) {
      printf("M[%1d]: %8.2f  %7.4f %7.4f %7.4f\n", 
        i+1, M[i][0], M[i][1], M[i][2], M[i][3] );
    }
  }

  void ComputeCompassAndClino( const Vector & g0, const Vector & m0, 
             double & compass, double & clino, double & roll ) const;
       
  /** Compute the default (T= identity) compass/clino/roll
   */
  static void DefaultCompassAndClino( const Vector & g0, const Vector & m0, 
             double & compass, double & clino, double & roll );

  private:
    double GetCoeff( const unsigned char * byte );
};


#endif
