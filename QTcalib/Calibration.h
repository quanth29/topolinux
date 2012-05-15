/** @file Calibration.h
 *
 * @author marco corvi
 * @date dec 2008
 * 
 * @brief Beat Heeb calibration algorithm
 *
 * @note after the class CalibAlgorithm.cs by B. Heeb
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef CALIBRATION_H
#define CALIBRATION_H

#include <vector>

#include "Vector.h"
#include "Matrix.h"

#define  MAX_IT 2000 /** default maximum number of iterations */

#define NOT_USED (-2)
#define NO_GROUP (-1)


// namespace TopoLinux
// {
  /** a calibration measure data
   */
  struct MeasureData
  {
    int G[3]; //!< G values
    int M[3]; //!< M values
    int idx;  //!< index
    int grp;  //!< group index
    int ignore;   //!< data is ignored
    double error; //!< calibration error

    /** default cstr
     */
    MeasureData()
      : idx( -1 )
      , grp( -1 )
      , ignore( 0 )
      , error( -1.0 )
    {
      G[0] = G[1] = G[2] = 0;
      M[0] = M[1] = M[2] = 0;
    }

    /** cstr
     */
    MeasureData( int gx, int gy, int gz, int mx, int my, int mz, int i, int g, int ign)
      : idx( i )
      , grp( g )
      , ignore( ign )
      , error( -1.0 )
    {
      G[0] = gx;
      G[1] = gy;
      G[2] = gz;
      M[0] = mx;
      M[1] = my;
      M[2] = mz;
    }
  };

  /** group of calibration measures
   *
   * a set of measures form a group when they have the same
   * bearing and inclination, but differ by the roll
   */
  struct Group 
  {
    std::vector< size_t > idx; //!< indices of the measures in the group
    
    /** add an index to the group
     * @param i  index to add
     * @return true if the index has been added to the group
     */
    bool Add( size_t i ) 
    {
      for (size_t k=0; k<idx.size(); ++k) {
        if ( idx[k] == i ) return false;
      }
      idx.push_back( i );
      return true;
    }

    /** the number of indices in the group
     * @return the size of the array of indices
     */
    size_t Size() const { return idx.size(); }

    /** get the i-th index
     * @param i number of the index
     * @return the i-th index
     */
    size_t Index( size_t i ) { return idx[i]; }
  };

  class Calibration
  {
    private:
      // static const unsigned int MAX_IT;
      std::vector< MeasureData > vD; //!< array of input measure data

      std::vector< Vector > vG; //!< array of G vectors
      std::vector< Vector > vM; //!< array of M vectors
      std::vector< int > vIgnore;    //!< ignore in calibration
      std::vector< double > vError;  //!< calibration error
      std::vector< int > vGroup; //!< array of the groups
      std::vector< double > vCompass;
      std::vector< double > vClino;
      unsigned int num;         //!< size of the vectors
      double optimize_eps;
      double dip_angle;         //!< M dip angle

      /** calibration coefficients
       */
      Matrix aG;  //!< G calibration matrix
      Matrix aM;  //!< M calibration matrix
      Vector bG;  //!< G calibration offset
      Vector bM;  //!< M calibration offset

      std::vector< Group > vGroups; //!< set of groups of indices

    public:
      /** cstr
       */
      Calibration()
        : num( 0 )
        #ifdef EXPERIMENTAL
        , show_gui( false )
        #endif
      { }

      void SetGCoeffs( Matrix & a, Vector & b )
      {
        aG = a;
        bG = b;
      }

      const Matrix & GetAG() const { return aG; }
      const Vector & GetBG() const { return bG; }
      const Matrix & GetAM() const { return aM; }
      const Vector & GetBM() const { return bM; }

      /** clear the vectors of G and M
       */
      void Clear();

      /** get the calibration coefficients
       * @param coeff  output array with the calibration coefficients
       */
      void GetCoeff( unsigned char coeff[ 48 ] );

      /** set the calibration coefficients
       * @param coeff  input array with the calibration coefficients
       */
      void SetCoeff( const unsigned char coeff[ 48 ] );

      double GetError( size_t k ) const { return vError[k]; }

      int GetGroup( size_t k ) const { return vGroup[k]; }

      int GetIgnore( size_t k ) const { return vIgnore[k]; }

      double GetDipAngle() const { return dip_angle; }

      /** copy a coefficient value in a pair of bytes
       * @param data array of two bytes
       * @param value value to write in the array of two bytes
       */
      void PutCoeff( unsigned char * data, double value );

      /** copy a pair of bytes into a coefficient value
       * @param data array of two bytes
       * @param value value to write from the array of two bytes
       */
      void GetCoeff( const unsigned char * data, double & value );

      /** insert a pair of vectors into the arrays
       * @param gx   X component of vector G
       * @param gy   Y component of vector G
       * @param gz   Z component of vector G
       * @param mx   X component of vector M
       * @param my   Y component of vector M
       * @param mz   Z component of vector M
       * @param idx  index
       * @param group group of the measure [-i for no group]
       * @param compass  compass value of the measure [debug]
       * @param clino    clino value of the measure [debug]
       */
      void AddValues( int gx, int gy, int gz, int mx, int my, int mz, 
                      unsigned int idx, 
                      int group = NO_GROUP,
                      int ignore = 0,
                      double compass = 0.0,double clino = 0.0 );

      /** print the input data
       */
      void PrintValues();

      /** print the calibration coeffs
       */
      void PrintCoeffs();

      /** print the calibration groups
       */
      void PrintGroups();

      /** print calibration to a file
       * @param name output filename
       */
      void PrintCalibrationFile( const char * name );

      /** print check 
       * @param print whether to print [default true]
       * @return the average difference [rads]
       */
      double CheckInput( bool print = true );
 
      /** check that the groups indices are aligned
       * @return fals eif there is a mismatch
       */
      bool CheckGroups();


      /** initialize calibration coeffs
       */
      void PrepareOptimize();

      /** carry out a measure
       * @param g   G vector
       * @param m   M vector
       * @param compass (output) compass value
       * @param clino   (output) clino value
       */
      void Measure( const Vector & g, const Vector & m,
                    double & compass, double & clino );

      /** carry out the calibration optmazation
       * @param delta (output) average angle error (degrees)
       *              used to be percent L2 error between input G and M's
       * @param error (output) final error
       * @param max_it maximum number of iterations
       * @param optimize_axis  whether to optimize the rotation axis
       * @return number of iterations
       */
      /**
       *
       * FIXME ...
       */
      unsigned int Optimize( double & delta, double & error, unsigned int max_it = MAX_IT );

#ifdef EXPERIMENTAL
    private:
      bool show_gui;
      #include "../experimental/ExperimentalCalibration.h"
#endif

    private:
      /** core of the optimization algo
       * @param gr work vectors for G
       * @param mr work vectors for M
       * @param gx second work vectors for G
       * @param mx second work vectors for M
       * @param max_it max number of iterations
       * @param sin_alpha  angle between G and M (output)
       * @param cos_alpha
       * @return number of iterations
       */
      int OptimizeCore( Vector * gr, Vector * mr, Vector * gx, Vector * mx,
                        unsigned int max_it, double * sin_alpha, double * cos_alpha );

      /**
       * @param gr  G vector
       * @param mr  M vector
       * @param alpha rotation angle
       * @param gx  output G vector
       * @param mx  output M vector
       *
       *         <-------+    (No normal to the page, incoming)
       *         Mr     /|\   (Mr^No vertical, downwards)
       *              /  |  x Gr
       *  rot_a(Mr) x   |     (No^Gx leftwards)
       *                |
       *                v Gx = rot_a(Mr) + Gr
       */
      void OptVectors( const Vector & gr, const Vector & mr,
                       double sin_alpha, double cos_alpha, 
                       Vector & gx, Vector & mx );

      /**
       * @param gxp  input G vector
       * @param mxp  input M vector
       * @param gr   reference G vector
       * @param mr   reference M vector
       * @param gx   (output) rotated G vector
       * @param mx   (output) rotated M vector
       * @return the rotation angle [degrees]
       */
      double TurnVectors( const Vector & gxp, const Vector & mxp,
                    const Vector & gr,  const Vector & mr,
                    Vector & gx, Vector & mx, bool print = false );

      /** compute the euclidean distance between the stored (G,M) pair array
       *  and a given array of pairs
       * @param gx   array of G vectors
       * @param mx   array of M vectors
       * @param error max error
       * @param jmax  index of max error
       * @param alpha expected angle between G and M
       * @param print whether to print out info (verbose)
       * @return the L2 distance between (gx,mx) and (gr,mr) (percent)
       */
      double ComputeDelta( // Vector * gx, Vector * mx, 
                           double & error, int & jmax, 
                           double sin_alpha, double cos_alpha, 
                           bool print=false );

    private:
      /** set ignore 
       * @param j index
       */
      void SetIgnore( int j ) 
      {
        // vIgnore.at(j) = 1;
        vIgnore[j] = 1;
      }
    };


// } // namespace TopoLinux

#endif // CALIBRATION_H

