/** @file Coverage.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief calibartion 4-PI coverage
 * -------------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 *
 */
#ifndef COVERAGE_H
#define COVERAGE_H

#include "CalibList.h"

#define COVERAGE_WIDTH  180
#define COVERAGE_HEIGHT  90

struct Direction
{
  double compass;
  double clino;
  double value;
};

class Coverage
{
  private:
    int clino_angles[ 19 ];
    int t_size[ 19 ];
    int t_offset[ 19 ];
    int t_dim;
    Direction * angles;
    unsigned char * img;

  public:
    /** cstr
     */
    Coverage();

    /** dstr
     */
    ~Coverage()
    {
      if ( angles ) delete[] angles;
      if ( img ) delete[] img;
    }

    void UpdateDirections( double compass, double clino, int cnt );

    double EvaluateCoverage( CalibList & clist );

    /** fill the image data and optionally write it to a file
     * @param filename name of the file (if NULL do not write the file)
     * @return the image data
     */
    unsigned char * FillImage( const char * filename = NULL);

    /** get the image width
     * @return the image width
     */
    unsigned int Width() const { return COVERAGE_WIDTH; }

    /** get the image height
     * @return the image height
     */
    unsigned int Height() const { return COVERAGE_HEIGHT; }

    /** get the image data
     * @return the image data
     */
    unsigned char * Data() const { return img; }

};

#endif
