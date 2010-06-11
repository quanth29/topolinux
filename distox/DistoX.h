/** @file DistoX.h
 *
 * @author marco corvi
 * @date jan 2009
 *
 * @brief get the data from the DistoX
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DISTO_X_H
#define DISTO_X_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifdef WIN32
  #define int16_t  signed short
  #define uint16_t unsigned short
#else
  #include <stdint.h>
#endif

#include "Protocol.h"
#include "Factors.h"

#define N_COEFF 48

#define C_2_D( c, k ) ((int16_t)(((uint16_t)(c[k])) | (uint16_t)(c[k+1])<< 8))

#define COEFF2BGX( c ) (  C_2_D( c,  0) / FV )
#define COEFF2AGXX( c ) ( C_2_D( c,  2) / FM )
#define COEFF2AGXY( c ) ( C_2_D( c,  4) / FM )
#define COEFF2AGXZ( c ) ( C_2_D( c,  6) / FM )
#define COEFF2BGY( c )  ( C_2_D( c,  8) / FV )
#define COEFF2AGYX( c ) ( C_2_D( c, 10) / FM )
#define COEFF2AGYY( c ) ( C_2_D( c, 12) / FM )
#define COEFF2AGYZ( c ) ( C_2_D( c, 14) / FM )
#define COEFF2BGZ( c )  ( C_2_D( c, 16) / FV )
#define COEFF2AGZX( c ) ( C_2_D( c, 18) / FM )
#define COEFF2AGZY( c ) ( C_2_D( c, 20) / FM )
#define COEFF2AGZZ( c ) ( C_2_D( c, 22) / FM )

#define COEFF2BMX( c )  ( C_2_D( c, 24) / FV )
#define COEFF2AMXX( c ) ( C_2_D( c, 26) / FM )
#define COEFF2AMXY( c ) ( C_2_D( c, 28) / FM )
#define COEFF2AMXZ( c ) ( C_2_D( c, 30) / FM )
#define COEFF2BMY( c )  ( C_2_D( c, 32) / FV )
#define COEFF2AMYX( c ) ( C_2_D( c, 34) / FM )
#define COEFF2AMYY( c ) ( C_2_D( c, 36) / FM )
#define COEFF2AMYZ( c ) ( C_2_D( c, 38) / FM )
#define COEFF2BMZ( c )  ( C_2_D( c, 40) / FV )
#define COEFF2AMZX( c ) ( C_2_D( c, 42) / FM )
#define COEFF2AMZY( c ) ( C_2_D( c, 44) / FM )
#define COEFF2AMZZ( c ) ( C_2_D( c, 46) / FM )

class DistoXListener
{
  public:
    /** dstr
     * for the virtual table
     */
    virtual ~DistoXListener() {}

    /** reset callback
     */
    virtual void distoxReset() = 0;

    /** data received callback
     * @param nr number od disto data downloaded
     */
    virtual void distoxDownload( size_t nr ) = 0;

    /** "done" callback
     */
    virtual void distoxDone() = 0;
};

class DistoX
{
  private:
    Protocol    mProto;          //!< DistoX communication protocol
    DistoXListener * mListener;  //!< listener for notification

  public:
    /** cstr
     * @param device   DistoX device
     * @param log      whether to do log or not [default: false= no log]
     */
    DistoX( const char * device, bool log = false )
      : mProto( device, log )
      , mListener( NULL )
    { }

    /** set the listener
     * @param listener  distoX listener
     */
    void setListener( DistoXListener * listener )
    {
      mListener = listener;
    }

    /** download the data
     * @param number   number of data to download [0: infinity]
     */
    bool download( int number = 0 )
    {
      if ( ! mProto.Open() ) {
        // fprintf(stderr, "ERROR: failed to open protocol \n");
        return false;
      }

      ProtoError err = PROTO_OK;
      size_t cnt = 0;
      for ( size_t retry=0; retry<1; ++retry ) {
        while ( ( err = mProto.ReadData() ) == PROTO_OK ) {
          cnt ++;
          number --;
          if ( mListener ) {
            mListener->distoxDownload( cnt );
          }
          if ( number == 0 ) break;
        }
        if ( err == PROTO_TIMEOUT ) {
          // fprintf(stderr, "timeout: retry n. %d\n", retry );
          continue;
        }
        if ( err != PROTO_OK ) {
          // fprintf(stderr, "ERROR: Read failed: %s\n", ProtoErrorStr(err) );
        }
      }
      if ( mListener ) {
        mListener->distoxDone();
      }

      // close the connection with the device
      mProto.Close();
      return true;
    }


    /** accessor: get the number of calibration data
     * @return the number of calibration data
     */
    unsigned int calibrationSize() const { return mProto.CalibSize(); }
    
    /** accessor: get the number of measurement data
     * @return the number of measurement data
     */
    unsigned int measurementSize() const { return mProto.DataSize(); }

    /** get the next measurement data
     * @param ...
     * @return true if successful
     */
    bool nextMeasurement( unsigned int & id, unsigned int & ib,
                          unsigned int & ic, unsigned int & ir,
                          double & dist, double & compass, 
                          double & clino, double & roll )
    { 
      unsigned char b[8];
      if ( ! mProto.NextData( b ) ) return false;
      id = DATA_2_DISTANCE( b );
      ib = DATA_2_COMPASS( b );
      ic = DATA_2_CLINO( b );
      ir = DATA_2_ROLL( b );
      dist    = DISTANCE_METERS( id ),
      compass = COMPASS_DEGREES( ib ),
      clino   = CLINO_DEGREES( ic );
      roll    = ROLL_DEGREES( ir );
      return true;
    }

    /** get the next calibration data
     * @param ...
     * @return true if successful
     */
    bool nextCalibration( int16_t & gx, int16_t & gy, int16_t & gz,
                          int16_t & mx, int16_t & my, int16_t & mz )
    { 
      unsigned char b[8];
      if ( ! mProto.NextCalib( b ) ) return false;
      gx = CALIB_2_X( b );
      gy = CALIB_2_Y( b );
      gz = CALIB_2_Z( b );
      if ( ! mProto.NextCalib( b ) ) return false;
      mx = CALIB_2_X( b );
      my = CALIB_2_Y( b );
      mz = CALIB_2_Z( b );
      return true;
    }

    /** read DistoX user mode
     * @return neg. if failed, otherwise the mode
     */
    int readMode( )
    {
      int ret = -1;
      if ( ! mProto.Open() ) return ret;
      unsigned char mode = 0x00;
      for (int k = 0; k<3; ++k ) {
        if ( mProto.Read8000( &mode ) ) {
          ret = mode;
        }
      }
      mProto.Close();
      return ret;
    }

    /** toggle Disto X calibration mode
     * @return 0 normal mode, 1 calib mode, -1 error
     */
    int toggleCalib()
    {
      int ret = -1;
      if ( ! mProto.Open() ) return ret;
      unsigned char mode = 0x00;
      for (int k = 0; k<3; ++k ) {
        if ( mProto.Read8000( &mode ) ) {
          break;
        }
      }
      if ( mode != 0x00 ) {
        unsigned char mode1 = 0x00;
        unsigned char mode2 = mode;
        if ( mode2 & 0x08 ) {
          mode2 &= 0xf7;
        } else {
          mode2 |= 0x08;
        }
        for (int k = 0; k<3; ++k ) {
          if ( mode & 0x08 ) { // calib on: switch off
            mProto.SendCommandByte( 0x30 );
          } else {
            mProto.SendCommandByte( 0x31 );
          }
          if ( mProto.Read8000( &mode1 ) && mode1 != mode ) {
            break;
          }
        }
        if ( mode1 == mode2 ) {
          if ( mode & 0x08 ) {
            ret = 0;
            // fprintf(stdout, "DistoX in normal mode\n");
          } else {
            ret = 1;
            // fprintf(stdout, "DistoX in calibration mode\n");
          }
        } else {
          ret = -1;
          // fprintf(stdout, "Failed to switch DistoX mode\n");
        }
      }
      mProto.Close();
      return ret;
    }

    /** toggle Disto X calib mode
     * @param on   whether to turn calib mode on or off
     * @return 0 normal mode, 1 calib mode, -1 error
     */
    int setCalib( bool on )
    {
      int ret = -1;
      if ( ! mProto.Open() ) return ret;
      unsigned char mode = 0x00;
      // for (int k = 0; k<3; ++k ) 
      {
        if ( mProto.Read8000( &mode ) ) {
          bool calib = ( mode & 0x08 ) != 0;
          if ( calib != on) {
            unsigned char mode1 = 0x00;
            unsigned char mode2 = mode ^ 0x08; // expected mode: toggle calib bit
            for (int k = 0; k<3; ++k ) {
              mProto.SendCommandByte( on ? 0x31 : 0x30 ); // start|stop calib
              if ( mProto.Read8000( &mode1 ) && mode1 == mode2 ) {
                ret = ( ( mode1 & 0x08 ) != 0 )? 1 : 0;
                break;
              }
            }
          } else {
            ret = calib ? 1 : 0;
          }
        }
      }
      mProto.Close();
      return ret;
    }

    /** toggle Disto X silent mode
     * @param on   whether to turn silent mode on or off
     * @return 0 normal mode, 1 silent mode, -1 error
     */
    int setSilent( bool on )
    {
      int ret = -1;
      if ( ! mProto.Open() ) return ret;
      unsigned char mode = 0x00;
      // for (int k = 0; k<3; ++k )
      {
        if ( mProto.Read8000( &mode ) ) {
          fprintf(stderr, "mode %02x \n", mode );
          bool silent = ( mode & 0x10 ) == 0; 
          if ( silent != on) {
            unsigned char mode1 = 0x00;
            unsigned char mode2 = mode ^ 0x10;
            for (int k = 0; k<3; ++k ) {
              mProto.SendCommandByte( on ? 0x33 : 0x32 ); // start|stop silent
              if ( mProto.Read8000( &mode1 ) && mode1 == mode2 ) {
                ret = ( ( mode1 & 0x10 ) != 0 )? 1 : 0;
                break;
              }
            }
          } else {
            ret = silent ? 1 : 0;
          }
        }
      }
      mProto.Close();
      return ret;
    }


    /** read the calibration coeffs
     * @param byte eight-byte array to write the coeffs
     *
     * @return true if memory has been changed, false otherwise
     */
    bool readCoeffs( unsigned char * byte )
    {
      if ( ! mProto.Open() ) return false;
      bool ret = ( mProto.ReadCalibration( byte ) == PROTO_OK );
      mProto.Close();
      return ret;
    }


    /** write the calibration coeffs
     * @param byte eight-byte array to write the coeffs
     *
     * @return true if memory has been changed, false otherwise
     */
    bool writeCoeffs( unsigned char * byte )
    {
      if ( ! mProto.Open() ) return false;
      bool ret = ( mProto.WriteCalibration( byte ) == PROTO_OK );
      mProto.Close();
      return ret;
    }

    /** print the calibration coeffs
     * @param byte coeff array
     */
    void printCoeffs( unsigned char * buf )
    {
      fprintf(stdout, "Calibration coefficients.\n");
      fprintf(stdout, "BG:  %7.4f %7.4f %7.4f\n", 
        COEFF2BGX( buf ), COEFF2BGY( buf ), COEFF2BGZ( buf ) ); 
      fprintf(stdout, "AGx: %7.4f %7.4f %7.4f\n", 
        COEFF2AGXX( buf ), COEFF2AGXY( buf ), COEFF2AGXZ( buf ) ); 
      fprintf(stdout, "AGy: %7.4f %7.4f %7.4f\n", 
        COEFF2AGYX( buf ), COEFF2AGYY( buf ), COEFF2AGYZ( buf ) ); 
      fprintf(stdout, "AGz: %7.4f %7.4f %7.4f\n", 
        COEFF2AGZX( buf ), COEFF2AGZY( buf ), COEFF2AGZZ( buf ) ); 
      fprintf(stdout, "BM:  %7.4f %7.4f %7.4f\n", 
        COEFF2BMX( buf ), COEFF2BMY( buf ), COEFF2BMZ( buf ) ); 
      fprintf(stdout, "AMx: %7.4f %7.4f %7.4f\n", 
        COEFF2AMXX( buf ), COEFF2AMXY( buf ), COEFF2AMXZ( buf ) ); 
      fprintf(stdout, "AMy: %7.4f %7.4f %7.4f\n", 
        COEFF2AMYX( buf ), COEFF2AMYY( buf ), COEFF2AMYZ( buf ) ); 
      fprintf(stdout, "AMz: %7.4f %7.4f %7.4f\n", 
        COEFF2AMZX( buf ), COEFF2AMZY( buf ), COEFF2AMZZ( buf ) ); 
    }

};

#endif
 
