/** @file ImageTransform.h
 * 
 * @author marco corvi
 * @date dec 2009
 *
 * @brief tranforms for the background sketches
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef IMAGE_TRANSFORM_H
#define IMAGE_TRANSFORM_H

#ifdef HAS_BACKIMAGE

#include <string>
#include <vector>

#include <qpixmap.h>
#include "BackgroundImageStation.h"

class PlotPoint; // forward

/** point correspondence between sketch image and plot
 */
struct BackgroundImagePoint
{
  double x;            //!< sketch coordinates (pixel in the image)
  double y;
  double x0;           //!< plot coordinates (point on the canvas)
  double y0;
  PlotPoint * point; //!< pointer to the corresponding canvas point

  BackgroundImagePoint( PlotPoint * pt, const BackgroundImageStation * st )
    : x( st->x )
    , y( st->y )
    , point(pt )
  { }
};

/** interface that uses a sketch to make the background image
 */
class BackgroundImageCallback
{
  public:
    virtual ~BackgroundImageCallback()  { }
  
    /** warp the input sketch using the stations correspondences
     * @param stations   stations correspondences
     * @param sketch     background sketch
     */
    virtual void evalBackground( const std::vector< BackgroundImageStation *> & stations,
                                 QPixmap * sketch ) = 0;
};

class Transform
{
  protected:
    bool init;       //!< whether the transform has been properly initialized

  public:
    /** cstr
     */
    Transform()
      : init( false )
    { }

    /** dstr
     */
    virtual ~Transform() {}

    /** check if this transform is initialized
     * @return true if this transform is initialized
     */
    bool isInitialized() const { return init; }

    /** map a background image point (xs,ys) to a sketch point (xt,yt)
     * @param xs  X coord of the point in the background image
     * @param ys  Y coord
     * @param xt  X coord in the sketch image
     * @param yt  Y coord
     * @return the "distance" of the point in the background image
     */
    virtual double map( double xs, double ys, double & xt, double & yt ) = 0;

    /** compute the "distance" of a point (in the background image)
     * @param xs  X coord of the point in the background image
     * @param ys  Y coord
     * @return the "distance" of the point in the background image
     */
    virtual double distance( double xs, double ys ) = 0;
};

/** transform based on two points
 *    x' =  g x + s y + a
 *    y' = -s x + g y + b
 */
class TransformTwoPt : public Transform
{ 
  private:
    double x1s0, y1s0;
    double x1t0, y1t0;
    double ds0, dt0;
    double scale;      //!< scales
    double n1sx, n1sy; //!< unit vector in source space
    double n1tx, n1ty; //!< unit vector in target space
    // double t1x = n1y, t1y = -n1x //!< normal vector;
 
  public:
    TransformTwoPt( double x1s, double y1s, double x1t, double y1t,
                    double x2s, double y2s, double x2t, double y2t );

    double map( double xs, double ys, double & xt, double & yt );

    double distance( double xs, double ys );
};

bool
imageWarp( std::vector< BackgroundImagePoint > & stations,
           unsigned char * dst, int w1, int h1,
           const unsigned char * src, int w2, int h2 );

#endif // HAS_BACKIMAGE

#endif

