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

#include "CTransform.h"

struct CBlock 
{
  std::string group;
  int gx, gy, gz, mx, my, mz;
  double compass;
  double clino;
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
    , ignore( ign )
    , next( NULL )
  {
    ComputeCompassAndClino();
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
    , ignore( 0 )
    , next( NULL )
  {
    ComputeCompassAndClino( t );
  }

  /** compute compass and clino values 
   */
  void ComputeCompassAndClino();

  /** compute compass and clino values using a calibration transform
   * @param t the calibration transform
   */
  void ComputeCompassAndClino( const CTransform & t );

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

};

#endif

