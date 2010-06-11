/* @file ImageTransform.cpp
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief implementation of transforms
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifdef HAS_BACKIMAGE

#include <assert.h>
#include <math.h>

#include "ArgCheck.h"
#include "ImageTransform.h"


TransformTwoPt::TransformTwoPt( double x1s, double y1s, double x1t, double y1t,
                                double x2s, double y2s, double x2t, double y2t )
  : x1s0( x1s )
  , y1s0( y1s )
  , x1t0( x1t )
  , y1t0( y1t )
{
  double dxs = x2s - x1s; //!< source vector
  double dys = y2s - y1s;
  double dxt = x2t - x1t; //!< target vector
  double dyt = y2t - y1t;
  ds0 = ( dxs*dxs + dys*dys );
  dt0 = ( dxt*dxt + dyt*dyt );
  if ( ds0 > 0.005 && dt0 > 0.005 ) { 
    ds0 = sqrt( ds0 );
    dt0 = sqrt( dt0 );
    scale = dt0 / ds0;
    n1sx = dxs / ds0; // unit source vector
    n1sy = dys / ds0;
    n1tx = dxt / dt0; // unit target vector
    n1ty = dyt / dt0;
    assert( fabs( n1sx*n1sx + n1sy*n1sy - 1.0 ) < 0.005 );
    assert( fabs( n1tx*n1tx + n1ty*n1ty - 1.0 ) < 0.005 );
    init = true;
  }
  // DBG_CHECK("Transform scale %.2f base pt. %.2f %.2f --> %.2f %.2f init %s\n",
  //         scale, x1s0, y1s0, x1t0, y1t0, init ? "true" : "false" );
}

double
TransformTwoPt::map( double xs, double ys, double & xt, double & yt )
{
  double dx = xs - x1s0;
  double dy = ys - y1s0;
  double n = dx*n1sx + dy*n1sy; //!< source proj. along N
  double t = dx*n1sy - dy*n1sx; //!< source proj. orthogonal to N

  // vector
  //    base pt.  scale    along N   orthog. to N
  xt = x1t0    + scale * ( n * n1tx + t * n1ty );
  yt = y1t0    + scale * ( n * n1ty - t * n1tx );

  if ( n >= 0.0 && n <= ds0 ) {
    return fabs( t );
  } else if ( n > ds0 ) {
    n -= ds0;
  }
  return sqrt( n*n + t*t );
}

double 
TransformTwoPt::distance( double x, double y )
{
  double dx = x - x1s0;
  double dy = y - y1s0;
  double n = dx*n1sx + dy*n1sy;
  double t = dx*n1sy - dy*n1sx;
  if ( n >= 0.0 && n <= ds0 ) {
    return fabs( t );
  } else if ( n > ds0 ) {
    n -= ds0;
  }
  return sqrt( n*n + t*t );
}

/*
void 
BackgroundImageCallback::morphImage( std::vector< BackgroundImageStation > &, const QPixmap & )
{ 
  fprintf(stderr, "BackgroundImageCallback::morphImage() \n");
}
*/

bool
imageWarp( std::vector< BackgroundImagePoint > & stations,
           unsigned char * dst, int wd, int hd,
           const unsigned char * src, int ws, int hs )
{
  ARG_CHECK( dst == NULL, false );

  // DBG_CHECK("imageWarp() base on %d stations %d %d <-- %d %d\n",
  //   stations.size(), wd, hd, ws, hs );
  
  std::vector< Transform * > transforms;
  std::vector< BackgroundImagePoint >::iterator it1 = stations.begin();
  std::vector< BackgroundImagePoint >::iterator it2 = it1;
  for ( ++it2; it2 != stations.end(); ++it2 ) {
    Transform * t = new TransformTwoPt ( it1->x0, it1->y0, it1->x, it1->y, 
                                         it2->x0, it2->y0, it2->x, it2->y );
    if ( t->isInitialized() ) {
      transforms.push_back( t );
    } else {
      delete t;
    }
    it1 = it2;
  }
  // DBG_CHECK("imageWarp() base on %d station-pairs\n", transforms.size() );
  if ( transforms.size() == 0 ) return false;

  for (int i=0; i<wd; ++i ) {
    for (int j=0; j<hd; ++j ) {
      double x0 = i;
      double y0 = j;
      double x, y;
      double w=0.0, wx=0.0, wy=0.0;
      for ( std::vector< Transform * >::iterator it = transforms.begin();
            it != transforms.end();
            ++it ) {
        double d = (*it)->map( x0, y0, x, y );
        // double d = it->distance( x0, y0 );
        if ( d > 1.0 ) {
          d = 1.0/(d*d*d*d);
        } else {
          d = 1.0;
        }
        w += d;
        wx += x*d;
        wy += y*d;
      }
      if ( w > 0.0 ) {
        int i1 = (int)(wx/w);
        if ( i1 >= 0 && i1 < ws ) {
          int j1 = (int)(wy/w);  
          if (j1 >= 0 && j1 < hs ) {
            memcpy( dst+(j*wd+i)*4, src+(j1*ws+i1)*4, 4 ); // 4 bytes per pixel
          }
        }
      }
    }
  }
  // DBG_CHECK("imageWarp() done\n");
  for ( std::vector< Transform * >::iterator it = transforms.begin();
        it != transforms.end();
        ++it ) {
    delete *it;
  }
  return true;
}

#endif // HAS_BACKIMAGE

#if 0
  double error = 0.0;
  double Ex = 0.0;
  double Ey = 0.0;
  double Ex0 = 0.0;
  double Ey0 = 0.0;
  int n = 0;
  for ( std::vector< BackgroundImageStation >::iterator it = stations.begin();
        it != stations.end();
        ++it ) {
    if ( ! it->use ) continue;
    ++ n;
    Ex += it->x;
    Ey += it->y;
    Ex0 += it->x0;
    Ey0 += it->y0;
    error += (it->x0 - it->x)*(it->x0 - it->x)
           + (it->y0 - it->y)*(it->y0 - it->y);
  }
  printf("error [0] %.2f \n", error );
  if ( n == 0 ) return;
  Ex /= n;
  Ey /= n;
  Ex0 /= n;
  Ey0 /= n;

  double dx = Ex0 - Ex;
  double dy = Ey0 - Ey;

  double Exx = 0.0;
  double Exx0 = 0.0;
  double Eyx0 = 0.0;
  double Eyy = 0.0;
  double Eyy0 = 0.0;
  double Exy0 = 0.0;
  error = 0.0;
  for ( std::vector< BackgroundImageStation >::iterator it = stations.begin();
        it != stations.end();
        ++it ) {
    if ( ! it->use ) continue;
    double x = it->x - Ex;
    double y = it->y - Ey;
    double x0 = it->x0 - Ex0;
    double y0 = it->y0 - Ey0;
    error += (x-x0)*(x-x0) + (y-y0)*(y-y0);
    Exx += x*x;
    Eyy += y*y;
    Exx0 += x * x0;
    Eyx0 += y * x0;
    Eyy0 += y * y0;
    Exy0 += x * y0;
  }
  printf("error [1] %.2f \n", error );
  double c = ( Exx0 + Eyy0 )/( Exx + Eyy );
  double s = ( Eyx0 - Exy0 )/( Exx + Eyy );

  double Fx = 0.0;
  double Fx0 = 0.0;
  double Fxx = 0.0;
  double Fxx0 = 0.0;
  double Fy = 0.0;
  double Fy0 = 0.0;
  double Fyy = 0.0;
  double Fyy0 = 0.0;
  error = 0.0;
  for ( std::vector< BackgroundImageStation >::iterator it = stations.begin();
        it != stations.end();
        ++it ) {
    if ( ! it->use ) continue;
    double x =  c*(it->x-Ex) + s*(it->y-Ey);
    double y = -s*(it->x-Ex) + c*(it->y-Ey);
    double x0 = it->x0 - Ex0;
    double y0 = it->y0 - Ey0;
    error += (x-x0)*(x-x0) + (y-y0)*(y-y0);
    Fx += x;
    Fx0 += x0;
    Fxx = x*x;
    Fxx0 += x*x0;
    Fy += y;
    Fy0 += y0;
    Fyy = y*y;
    Fyy0 += y*y0;
  }
  printf("error [2] %.2f \n", error );
  Fx /= n;
  Fx0 /= n;
  Fxx /= n;
  Fxx0 /= n;
  Fy /= n;
  Fy0 /= n;
  Fyy /= n;
  Fyy0 /= n;
  double D = ( Fxx0 + Fyy0 - Fx0*Fx - Fy0*Fy)/( Fxx + Fyy - Fx*Fx - Fy*Fy);
  double A = Fx0 - D * Fx;
  double B = Fy0 - D * Fy;

  error = 0.0;
  for ( std::vector< BackgroundImageStation >::iterator it = stations.begin();
        it != stations.end();
        ++it ) {
    if ( ! it->use ) continue;
    it->xt = Ex0 + A + D*( c*(it->x-Ex) + s*(it->y-Ey) );
    it->yt = Ey0 + B + D*(-s*(it->x-Ex) + c*(it->y-Ey) );
    error += (it->x0 - it->xt)*(it->x0 - it->xt)
           + (it->y0 - it->yt)*(it->y0 - it->yt);
    DBG_CHECK("station %s sketch %d, %d plot %d %d T %.2f %.2f\n",
      it->name.c_str(), it->x, it->y, it->x0, it->y0, it->xt, it->yt );
  } 
  error = sqrt( error );
  printf("map error %.2f \n", error );
#endif

