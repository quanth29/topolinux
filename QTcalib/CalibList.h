/** @file CalibList.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux calibration data list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CALIB_LIST_H
#define CALIB_LIST_H

#include <stdio.h>

#include <string>
#include <sstream>

#include "Calibration.h"
#include "CTransform.h"
#include "CBlock.h"

#ifdef WIN32
  #include "stdint.h"
#endif

class QWidget; // forward


struct CalibList
{
  int size;                //!< number of blocks
  CBlock * head;           //!< head of the list of blocks
  unsigned char coeff[48]; //!< calibration coeffs

  CalibList()
    : size( 0 )
    , head( NULL )
  { }

  ~CalibList()
  {
    clear();
  }

  /** compute data compass/clino/roll
   * @param transformed whether to use the calibration transformation to compute
   */
  void computeData( const CTransform * t );

  /** clear the list of calibration data
   */
  void clear();

  /** write the calibration coeff and data to a file (TopoLinux format)
   * @param filename name of the file
   * @param comment  description string (in)
   * @return true if OK
   */
  bool save( const char * filename, std::string & comment );

  /** read the calibration coeff and data from a file (TopoLinux format)
   * @param filename name of the file
   * @param comment  description string (out)
   * @param angle guess tolerance angle
   * @return true if OK
   */
  bool load( const char * filename, std::string & comment, int angle );

    /** write the calibration data to a file (raw format)
     * @param filename name of the file
     */
    bool writeData( const char * filename );

    /** read the calibration data from a file (raw format)
     * @param filename name of the file
     * @param angle guess tolerance angle
     */
    void readData(  const char * filename, int angle );

    /** add a calibration data
     * @param b0    previous block
     * @param gx    X component of G 
     * @param gy    ...
     * ...
     * @param group group of the data
     * @param ignore whether to ignore this data
     */
    CBlock * addData( CBlock * b0, 
      int16_t gx, int16_t gy, int16_t gz, int16_t mx, int16_t my, int16_t mz,
      const char * group = "", int ignore = 0);

    /** add a calibration data
     * @param b0    previous block
     * @param gx    X component of G 
     * @param gy    ...
     * ...
     * @param t     calibration transform (used to guess the groups)
     */
    CBlock * addData( CBlock * b0,
      int16_t gx, int16_t gy, int16_t gz, int16_t mx, int16_t my, int16_t mz,
      const CTransform & t );

    void readCoeff( QWidget * widget, const char * filename );

    int toggleIgnore( int r );

    // void updateGroup( int r, const char * txt );

    void initCalib( Calibration & calib );

    void getErrors( Calibration & calib );

    unsigned char * getCoeff( ) { return coeff; }

    /** get the coefficients 
     * @param array of 24 real numbers where the coeffs are written [output]
     */
    void getCoeff( double * c ); 

    /** guess the groups
     * @param guess_angle group guess discrepancy angle
     */
    void guessGroups( int guess_angle = 20 );

    /** set the calibration coefficients
     * @param byte   new array of calibration coeff (48 bytes)
     */
    void setCoeff( const unsigned char * byte );

  private:
    /** load a calibration data file 
     * @param fp    open file pointer
     * @param angle guess tolerance angle
     * @return number of read data
     */
    size_t loadData( FILE * fp, int angle );

    /** load a calibration coeff file
     * @param fp   open file pointer
     * @param comment  calibration description (out)
     * @return  true if successful
     */
    bool loadCoeff( FILE * fp, std::string & comment );

};

#endif // 
