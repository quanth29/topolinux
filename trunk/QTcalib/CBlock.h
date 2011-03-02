/** @file CBlock.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux calibration data 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef C_BLOCK_H
#define C_BLOCK_H

#include <stdio.h>

#include <string>
#include <sstream>

#include "Factors.h"
#include "CTransform.h"

struct CBlock 
{
  private:
    std::string group;
  
  public:
  int gx, gy, gz, mx, my, mz;
  double compass;
  double clino;
  double roll;
  double error;
  bool ignore;
  CBlock * next;

  CBlock( int _gx, int _gy, int _gz, int _mx, int _my, int _mz, 
          const char * grp = "", int ign = 0, double err = -1.0 )
    : group( grp )
    , gx( _gx )
    , gy( _gy )
    , gz( _gz )
    , mx( _mx )
    , my( _my )
    , mz( _mz )
    , error( err ) 
    , ignore( ign != 0 )
    , next( NULL )
  {
    Vector g0( gx/FV, gy/FV, gz/FV );
    Vector m0( mx/FV, my/FV, mz/FV );
    CTransform::DefaultCompassAndClino( g0, m0, compass, clino, roll );
  }

  CBlock( int _gx, int _gy, int _gz, int _mx, int _my, int _mz, const CTransform & t )
    : group( "" )
    , gx( _gx )
    , gy( _gy )
    , gz( _gz )
    , mx( _mx )
    , my( _my )
    , mz( _mz )
    , error( -1.0 )
    , ignore( false )
    , next( NULL )
  {
    computeCompassAndClino( t );
  }

  /** compute compass and clino values 
   *
  void ComputeCompassAndClino();
   */

  /** compute compass and clino values using a calibration transform
   * @param t the calibration transform
   */
  void computeCompassAndClino( const CTransform & t )
  {
    Vector g0( gx/FV, gy/FV, gz/FV );
    Vector m0( mx/FV, my/FV, mz/FV );
    t.ComputeCompassAndClino( g0, m0, compass, clino, roll );
  }

  /** debug 
   */
  void dump()
  {
    fprintf(stderr, "Block %s: %d %d %d  %d %d %d I %d B %.2f C %.2f E %.2f\n",
      group.c_str(), gx, gy, gz, mx, my, mz, ignore?1:0, 
      compass, clino, error);
  }

  void SetGroup( int g ) 
  {
    std::ostringstream oss;
    oss << g;
    group = oss.str();
  }

  void SetGroup( const char * g ) 
  {
    group = g;
  }

  const char * Group() const { return group.c_str(); }

};

#endif

