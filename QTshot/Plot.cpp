/** @file Plot.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux measurements data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <string.h>

#include "ArgCheck.h"

#include "Num.h"
#include "PlotScale.h"
#include "Plot.h"
#include "Extend.h"
#include "CanvasMode.h"
#include "TherionScrap.h"


/** border for the 3D viewer: 30 pixels
 */
#define PLOT_3D_BORDER 30

/** plot magnifying scale 
 */

//  double np = -sin_phi * dn + cos_phi * de;      // phi displacement
//  double nt = (cos_phi * dn + sin_phi * de)*sin_theta - cos_theta * dz; // theta displacement
//  double nd = (cos_phi * dn + sin_phi * de)*cos_theta + sin_theta * dz; // depth displacement
#define NP( dn, de ) ( sin_phi * dn - cos_phi * de )     /* phi displacement */
#define NT( dn, de, dz ) \
  ( (cos_phi * dn + sin_phi * de)*sin_theta - cos_theta * dz ) /* theta displacement */
#define ND( dn, de, dz ) \
  ( (cos_phi * dn + sin_phi * de)*cos_theta + sin_theta * dz ) /* depth displacement */

void
Plot::dump()
{
  fprintf(stderr, "Plot segments %d / %d points %d\n", nr_centerline_segments, nr_segments, nr_centerline_points );
  for ( PlotSegment * s = segment; s; s=s->next ) {
    fprintf(stderr, "Segment \"%s-%s\" %d %d  %d %d (%d)\n", 
      s->p0, s->p1, s->x0, s->y0, s->x1, s->y1, s->cs_extend );
  }
  for ( PlotPoint * p = points; p; p=p->next ) {
    fprintf(stderr, "Point \"%s\" %d %d \n", p->name, p->x0, p->y0 );
  }
  
}

bool
Plot::computeXSection( DataList * list, DBlock * block, bool reversed, double vertical )
{
  ARG_CHECK( list == NULL, false );
  ARG_CHECK( block == NULL, false );
  DBG_CHECK("Plot::computeXSection() %s vertical threshold %.2f\n", reversed ? "reversed" : "", vertical );

  double to_rad = M_PI/180.0;

  // assert( block );
  const char * from = block->fromStation();
  const char * to   = block->toStation();
  double compass = block->Compass() * to_rad;
  double clino   = block->Clino() * to_rad;
  double nz = sin( clino );
  double nh = cos( clino );
  double nn = cos( compass ); // north 
  double ne = sin( compass ); // east
  if ( reversed ) {
    nz = -nz;
    nh = -nh;
    nn = -nn;
    ne = -ne;
  }
  points = new PlotPoint();
  points->name = from;
  points->x0 = 0; // midpoint of the canvas
  points->y0 = 0;
  points->z0 = 0; // depth origin
  x0min = x0max = 0;
  y0min = y0max = 0;
  z0min = z0max = 0;
  points->next = new PlotPoint();
  points->next->name = to;
  points->next->next = NULL;
  nr_centerline_points = 2;
  if ( fabs(block->Clino()) < vertical ) { // vertical section
    // DBG_CHECK("vertical cross section dist: %.2f \n", block->Tape() );
    cos_theta = 1.0;
    sin_theta = 0.0;
    cos_phi = nn / fabs(nh);
    sin_phi = ne / fabs(nh);
    points->next->x0 = 0;
    points->next->y0 = - (int)(block->Tape() * nz) * BASE_SCALE;
  } else { // horizontal section
    cos_theta = 0.0;
    sin_theta = -1.0;
    cos_phi = 1.0;
    sin_phi = 0.0;
    points->next->x0 =   (int)(block->Tape() * ne * nh) * BASE_SCALE;
    points->next->y0 = - (int)(block->Tape() * nn * nh) * BASE_SCALE;
  }
  if ( points->next->x0 < x0min )      x0min = points->next->x0;
  else if ( points->next->x0 > x0max ) x0max = points->next->x0;
  if ( points->next->y0 < y0min )      y0min = points->next->y0;
  else if ( points->next->y0 > y0max ) y0max = points->next->y0;
  if ( points->next->z0 < z0min )      z0min = points->next->z0;
  else if ( points->next->z0 > z0max ) z0max = points->next->z0;

  // centerline shot ("vertical")
  drawVerticalSegment( points, points->next, block );

  #ifdef HAS_LRUD
    if ( with_lrud ) {
      if ( block->getLRUD( 0 ) ) {
        drawLRUD( points, block->getLRUD( 0 ), block, reversed );
      }
      if ( block->getLRUD( 1 ) ) {
        drawLRUD( points->next, block->getLRUD( 1 ), block, reversed );
      }
    }
  #endif

  // splay shots
  for ( DBlock * bl = list->listHead(); bl; bl=bl->next() ) {
    if ( bl->hasNoStation() ) continue;
    if ( ! bl->hasToStation() ) { //
      PlotPoint * pt = NULL;
      if ( bl->hasFromStation( from ) ) { // add to points
        pt = points;
      } else if ( bl->hasFromStation( to ) ) {
        pt = points->next;
      }
      if ( pt ) {
        double d = bl->Tape() * BASE_SCALE;
        clino   = bl->Clino() * to_rad;
        compass = bl->Compass() * to_rad;
        double dz = d * sin( clino );
        double dh = d * cos( clino );
        double dn = dh * cos( compass );
        double de = dh * sin( compass );
        drawFromPoint( pt, dn, de, dh, dz, bl, bl->Extend(),  2 /*mode*/ );
      }
    }
  }
  return true;
}

bool
Plot::computePlot( DataList * list, int mode, bool do_num )
{
  ARG_CHECK( list == NULL, false );

  double to_rad = M_PI / 180.0;
  size_t size = list->listSize();
  DBG_CHECK("Plot::computePlot() mode %d size %d do_num %d\n", mode, size, do_num );
  // list->dump();
  if ( size < 1 ) {
    return false;
  }

  if ( do_num ) {
    // DBG_CHECK("Plot::computePlot() do num \n");
    if ( list->doNum() == 0 ) {
      DBG_CHECK("Warning. Plot::computePlot() num_measures == 0 \n");
      // return false;
    }
  }

  int idx = 0;
  int to_paint=0;
  for ( DBlock * bl = list->listHead(); bl; bl=bl->next(), ++idx ) {
    if ( bl->evalNeedPaint() == 3 ) { // only centerline
      ++ to_paint;
    }
  }
  DBG_CHECK("Plot::computePlot() centerline blocks to paint %d\n", to_paint);

  if ( segment != NULL ) clearSegments();
  if ( points != NULL ) clearPoints();
  bool redo_paint = true;
  int extend_sign = 1; // 1: to the right, -1: to the left
  const Num & num = list->getNum();
  while ( redo_paint ) {
    redo_paint = false;
    idx = 0;
    for ( DBlock * bl = list->listHead(); bl; bl=bl->next(), ++idx ) {
      if ( bl->needPaint() < 3 ) // only centerline shots
        continue;
      const NumPoint * pt1 = num.getPoint( bl->fromStation() );
      const NumPoint * pt2 = num.getPoint( bl->toStation() );
      if ( pt1 == NULL || pt2 == NULL ) { // cannot paint block
        bl->setNeedPaint( 0 );
        continue;
      }
      if (points == NULL) {
        points = new PlotPoint();
        nr_centerline_points ++;
        points->name = pt1->tag;
        points->x0 = 0; // midpoint of the canvas
        points->y0 = 0;
        points->z0 = 0;
        points->n = pt1->N * BASE_SCALE;
        points->e = pt1->E * BASE_SCALE;
        points->z = pt1->V * BASE_SCALE;
        points->h = 0; // pt1->H * BASE_SCALE;
        points->next = NULL;
        // fprintf(stderr, "Base point (0,0,0) %.2f %2f \n", points->n, points->e);
      }
      // double dz = pt2->V - pt1->V;
      double dn = pt2->N - pt1->N;
      double de = pt2->E - pt1->E;
      double dh = sqrt( dn*dn +de*de );
      unsigned char extend_flag = bl->Extend();
      if ( mode != MODE_3D ) {
        bl->setExtended( extend_flag );
        if ( bl->Extend() == EXTEND_NONE ) {
          dh *= extend_sign;
          if ( extend_sign == -1 ) {
            extend_flag = EXTEND_LEFT;
          } else if ( extend_sign == 1 ) {
            extend_flag = EXTEND_RIGHT;
          } else {
            extend_flag = EXTEND_VERT;
          }
          bl->setExtended( extend_flag );
        } else if ( bl->Extend() == EXTEND_LEFT ) {
          dh *= (extend_sign = -1);
        } else if ( bl->Extend() == EXTEND_RIGHT ) {
          dh *= (extend_sign = 1); 
        } else if ( bl->Extend() == EXTEND_VERT ) {
          dh = 0;
        } else if ( bl->Extend() == EXTEND_IGNORE ) {
          continue;
        }
        // DBG_CHECK("%s-%s extend_sign %d %d %d\n",
        //   bl->from.c_str(), bl->to.c_str(), bl->extend, extend_sign, extend_flag );
      }

      PlotPoint * pt10 = points;
      while ( pt10 && strcmp(pt10->name, pt1->tag) != 0 )
        pt10 = pt10->next;
      PlotPoint * pt20 = points;
      while ( pt20 && strcmp(pt20->name, pt2->tag) != 0 )
        pt20 = pt20->next;
      if ( pt20 == NULL && pt10 != NULL ) {
        pt20 =  new PlotPoint();
        nr_centerline_points ++;
        pt20->next = points; // insert at head
        points = pt20;
        pt20->name = pt2->tag;
        pt20->z = pt2->V * BASE_SCALE;
        pt20->h = pt10->h + dh * BASE_SCALE;
        pt20->n = pt2->N * BASE_SCALE;
        pt20->e = pt2->E * BASE_SCALE;
        drawPoint2Point( pt10, pt20, bl, extend_flag, mode );
        redo_paint = true;
        // fprintf(stderr, "PlotPoint (%d,%d) %.2f %.2f \n", 
        //   pt20->x0, pt20->y0, pt20->n, pt20->e );

        #ifdef HAS_LRUD
          if ( with_lrud ) {
            if ( bl->getLRUD( 0 ) ) {
              drawLRUD( pt10, bl->getLRUD( 0 ), bl, dn, de, mode );
            }
            if ( bl->getLRUD( 1 ) ) {
              drawLRUD( pt20, bl->getLRUD( 1 ), bl, dn, de, mode );
            }
          }
        #endif

        bl->setNeedPaint( 0 ); // switch off
      } else if ( pt10 == NULL && pt20 != NULL ) {
        pt10 =  new PlotPoint();
        nr_centerline_points ++;
        pt10->next = points; // insert at head
        points = pt10;
        pt10->name = pt1->tag;
        pt10->z = pt1->V * BASE_SCALE;
        pt10->h = pt20->h - dh * BASE_SCALE;
        pt10->n = pt1->N * BASE_SCALE;
        pt10->e = pt1->E * BASE_SCALE;
        drawPoint2Point( pt20, pt10, bl, extend_flag, mode );
        redo_paint = true;
        // fprintf(stderr, "PlotPoint (%d,%d) %.2f %.2f \n", 
        //   pt10->x0, pt10->y0, pt10->n, pt10->e );

        #ifdef HAS_LRUD
          if ( with_lrud ) {
            if ( bl->getLRUD( 0 ) ) {
              drawLRUD( pt10, bl->getLRUD( 0 ), bl, dn, de, mode );
            }
            if ( bl->getLRUD( 1 ) ) {
              drawLRUD( pt20, bl->getLRUD( 1 ), bl, dn, de, mode );
            }
          }
        #endif

        bl->setNeedPaint( 0 );
      } else if ( pt10 != NULL && pt20 != NULL ) {
        drawPoint2Point( pt20, pt10, bl, extend_flag, mode );

        #ifdef HAS_LRUD
          if ( with_lrud ) {
            if ( bl->getLRUD( 0 ) ) {
              drawLRUD( pt10, bl->getLRUD( 0 ), bl, dn, de, mode );
            }
            if ( bl->getLRUD( 1 ) ) {
              drawLRUD( pt20, bl->getLRUD( 1 ), bl, dn, de, mode );
            }
          }
        #endif
        bl->setNeedPaint( 0 );
      } else {
        /* nothing */
      }
    } // for (bl ...
  }

  /** This block of code for only star-like splay shots surveys
   */
  if (points == NULL ) { // FIXME and num.pointsNr() == 1 
    const NumPoint * pt = NULL;
    for ( DBlock * bl = list->listHead(); bl; bl=bl->next() ) {
      if ( bl->hasFromStation() ) {
        pt = num.getPoint( bl->fromStation() );
        if ( pt != NULL ) break;
      }
      if ( bl->hasToStation() ) {
        pt = num.getPoint( bl->toStation() );
        if ( pt != NULL ) break;
      }
    }
    if (pt != NULL) {
      points = new PlotPoint();
      nr_centerline_points ++;
      points->name = pt->tag;
      points->x0 = 0; // midpoint of the canvas
      points->y0 = 0;
      points->z0 = 0;
      points->n = pt->N * BASE_SCALE;
      points->e = pt->E * BASE_SCALE;
      points->z = pt->V * BASE_SCALE;
      points->h = 0; // pt1->H * BASE_SCALE;
      points->next = NULL;
    }
  }

#if 1
  idx = 0;
  // TODO compute extend array for each PlotPoint
  for ( PlotPoint * cp = points; cp; cp = cp->next ) {
    cp->evalExtends();
  }
  // list->evalSplayExtended();
  for ( DBlock * bl = list->listHead(); bl; bl=bl->next(), ++idx ) {
    if ( bl->hasToStation() /* && bl->hasFromStation() */ ) continue; // skip centerline
    double clino   = 0.0;
    double compass = 0.0;
    PlotPoint * pt = points;
    for ( ; pt; pt=pt->next ) {
      if ( bl->hasFromStation( pt->name ) ) {
        clino   = bl->Clino() * to_rad;
        compass = bl->Compass() * to_rad;
        break;
      }
    }
    // fprintf(stderr, "PlotPoint %s: %p \n", bl->fromStation(), pt );
    if ( pt != NULL ) {
      double dz = bl->Tape() * sin( clino ) * BASE_SCALE;
      double dh = bl->Tape() * cos( clino ) * BASE_SCALE;
      double dn = dh * cos( compass ); // north
      double de = dh * sin( compass ); // east
      // WRONG DrawFromPoint( pt, dn, de, dh, dz, bl, bl->Extended(), mode );
      drawFromPoint( pt, dn, de, dh, dz, bl, bl->Extend(), mode );
    }
  }
#endif

  DBG_CHECK("bounding box X %d %d Y %d %d\n", x0min, x0max, y0min, y0max );
  fprintf(stderr, "bounding box X %d %d Y %d %d\n", x0min, x0max, y0min, y0max );
  DBG_CHECK("depth %d - %d\n", z0min, z0max );

  if ( mode == MODE_3D ) {
    // shift the bounding box [(0,0) .. (...)]
    //
    int dz = z0max - z0min;
    for ( PlotSegment * s = segment; s; s=s->next ) {
      s->x0 += PLOT_3D_BORDER - x0min;
      s->y0 += PLOT_3D_BORDER - y0min;
      s->z0 = ((s->z0-z0min)*100)/dz;
      s->x1 += PLOT_3D_BORDER - x0min;
      s->y1 += PLOT_3D_BORDER - y0min;
      s->z1 = ((s->z1 - z0min)*100)/dz;
    }
    for ( PlotPoint * p = points; p; p=p->next ) {
      p->x0 += PLOT_3D_BORDER - x0min;
      p->y0 += PLOT_3D_BORDER - y0min;
      p->z0 = ((p->z0 - z0min)*100)/dz;
    }
    x0max += PLOT_3D_BORDER - x0min;
    y0max += PLOT_3D_BORDER - y0min;
    x0min = 0;
    y0min = 0;
    z0min = 0;
    z0max = 100;
  }

  // dump();
  return true;
}

void
Plot::drawPoint2Point( PlotPoint * p0, PlotPoint * p1, // int col,
                       DBlock * blk, unsigned char extend_flag, int mode )
{
  ARG_CHECK( p0 == NULL, );
  ARG_CHECK( p1 == NULL, );
  ARG_CHECK( blk == NULL, );
  DBG_CHECK("DrawPoint2Point() %d %d   %d %d \n", p0->x0, p0->y0, p1->x0, p1->y0 );

  double dn = p1->n - p0->n; // north displacement
  double de = p1->e - p0->e; // east displacement
  if ( mode == MODE_PLAN ) { // plan
    p1->x0 = p0->x0 + (int)(de); // east
    p1->y0 = p0->y0 - (int)(dn); // south
    p1->z0 = p0->z0;
  } else if ( mode == MODE_EXT ) { // extended section
    p1->x0 = p0->x0 + (int)((p1->h - p0->h)); // horiz.
    p1->y0 = p0->y0 - (int)((p1->z - p0->z)); // vert down.
    p1->z0 = p0->z0;
  } else if ( mode == MODE_CROSS || mode == MODE_3D ) { // 
    // compute parallel projections on the plane with normal (frame NEZ)
    //     ( ct*cp, ct*sp, st )
    // use the orthogonal unit vectors
    //     longitude = ( -sp,   cp,    0 )
    //     latitude  = ( st*cp, st*sp, -ct )
    double dz = p1->z - p0->z; // vertical displacement
    double np = NP( dn, de);     // phi displacement
    double nt = NT( dn, de, dz); // theta displacement
    double nd = ND( dn, de, dz); // depth displacement
    p1->x0 = p0->x0 + (int)(np);
    p1->y0 = p0->y0 + (int)(nt);
    p1->z0 = p0->z0 + (int)(nd);
  }
  // bounding box
  if ( segment == NULL ) {
    x0min = x0max = p0->x0;
    y0min = y0max = p0->y0;
    z0min = z0max = p0->z0;
  } else {
    if ( p0->x0 < x0min )      x0min = p0->x0;
    else if ( p0->x0 > x0max ) x0max = p0->x0;
    if ( p0->y0 < y0min )      y0min = p0->y0;
    else if ( p0->y0 > y0max ) y0max = p0->y0;
    if ( p0->z0 < z0min )      z0min = p0->z0;
    else if ( p0->z0 > z0max ) z0max = p0->z0;
  }
  if ( p1->x0 < x0min )      x0min = p1->x0;
  else if ( p1->x0 > x0max ) x0max = p1->x0;
  if ( p1->y0 < y0min )      y0min = p1->y0;
  else if ( p1->y0 > y0max ) y0max = p1->y0;
  if ( p1->z0 < z0min )      z0min = p1->z0;
  else if ( p1->z0 > z0max ) z0max = p1->z0;

  PlotSegment * s = new PlotSegment;
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p1->x0;
  s->y1 = p1->y0;
  s->z1 = p1->z0;
  s->p0 = p0->name;         // endpoints names
  s->p1 = p1->name;
  s->block = blk;           // data block
  s->cs_extend = extend_flag;  // use extend_flag call-parameter
  s->cs_type = CS_CENTERLINE;  //
  // horizontal angle with the North
  s->horiz_angle = 180.0/M_PI*( atan2( de, dn ) );
  if ( s->horiz_angle < 0 ) s->horiz_angle += 360;

  p0->segments.push_back( s );
  p1->segments.push_back( s );
  // draw_point_2_point( p0->x0, p0->y0, p1->x0, p1->y0 );
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  ++ nr_centerline_segments;
}

double expand( double i, double istart, double iend, double from, double to )
{
  double n = 3.0 + 0.67*(iend - istart)/M_PI;
  double delta = (iend - istart);
  double d3 = delta/n;
  double d3s = d3;
  double d3e = delta - d3;
  double f = (to-from - delta)/( d3e - d3s );
  double is = i - istart;
  if ( is < d3s )
    return cos( (from + is)*M_PI/180.);
  if ( is < d3e )
    return cos( (from + is + ((is-d3s)*f))*M_PI/180.);
  return cos( (to-delta + is)*M_PI/180.);
}

double
Plot::computeXExtend( PlotPoint * p0, double de, double dn )
{
  ARG_CHECK( p0 == NULL, 0);

  double alpha = 180.0/M_PI*( atan2( de, dn ) ); // angle in horizontal plane with the North
  if ( alpha < 0 ) alpha += 360;
  // DBG_CHECK("Plot::ComputeXExtend() %.2f from %s\n", alpha, p0->name);

  return p0->getExtend( alpha );
  // return (int)( dh * ext );
#if 0
  double h0 = dh;
  double * right = new double[ p0->segments.size() ];
  double * left  = new double[ p0->segments.size() ];
  size_t nr = 0; // number of right
  size_t nl = 0; // number of left
  // compute left's and right's
  for ( std::vector< PlotSegment * >::iterator it = p0->segments.begin();
        it != p0->segments.end();
        ++ it ) {
    if ( (*it)->cs_extend == EXTEND_LEFT ) {
      if ( strcmp( (*it)->p0, p0->name) == 0 ) {
        left[ nl++ ] = (*it)->horiz_angle;
      } else {
        right[ nr++ ] = (*it)->horiz_angle + 180;
      }
    } else if ( (*it)->cs_extend == EXTEND_RIGHT ) {
      if ( strcmp( (*it)->p0, p0->name) == 0 ) {
        right[ nr++ ] = (*it)->horiz_angle;
      } else {
        left[ nl++ ] = (*it)->horiz_angle + 180;
      }
    }
  }
  // normalize in [0, 2*M_PI)
  for (size_t k=0; k<nl; ++k) {
    while ( left[k] > 360 ) left[k] -= 360;
  }
  for (size_t k=0; k<nr; ++k) {
    while ( right[k] > 360 ) right[k] -= 360;
  }
  // sort by increasing value
  if ( nl > 1 ) {
    for (size_t k=0; k<nl-1; ) {
      if ( left[k] > left[k+1] ) {
        double a = left[k]; left[k] = left[k+1]; left[k+1] = a;
        if ( k > 1 ) --k;
      } else {
        ++k;
      }
    }
  }
  if ( nr > 1 ) {
    for (size_t k=0; k<nr-1; ) {
      if ( right[k] > right[k+1] ) {
        double a = right[k]; right[k] = right[k+1]; right[k+1] = a;
        if ( k > 1 ) --k;
      } else {
        ++k;
      }
    }
  }
  // find max delta
  size_t klmax=0, krmax=0;
  double lmax=0, lmax1=0;
  double rmax=0, rmax1=0;
  if ( nl > 1 ) {
    klmax = nl - 1;
    lmax = 360 + left[0] - left[nl-1];
    for (size_t k=0; k<nl-1; ++k) {
      if ( left[k+1] - left[k] > lmax ) {
        klmax = k;
        lmax  = left[k+1] - left[k];
      }
    }
    lmax = left[klmax];
    lmax1 = (klmax < nl-1)? left[klmax+1] : 360 + left[0];
  }
  if ( nr > 1 ) {
    krmax = nr - 1;
    rmax = 360 + right[0] - right[nr-1];
    for (size_t k=0; k<nr-1; ++k) {
      if ( right[k+1] - right[k] > rmax ) {
        krmax = k;
        rmax  = right[k+1] - right[k];
      }
    }
    rmax  = right[krmax];
    rmax1 = (krmax < nr-1)? right[krmax+1] : 360 + right[0];
  }

  // compute extend
  if ( nl == 0 ) {
    if ( nr == 0 ) {
      h0 = 0.0;
    }
  } else if ( nr == 1 ) {
    h0 = dh * cos( alpha - right[0] );
  } else {  // nr > 1
    if ( krmax == nr-1 && alpha < right[0] ) alpha += 360;
      if ( alpha > rmax && alpha < rmax1 ) { // outside RIGHT-sector
        double rmed = ( rmax + rmax1 )/ 2;
        if ( alpha < rmed ) {
          h0 = dh * expand( alpha, rmax, rmed, 0, M_PI );
        } else {
          h0 = dh * expand( alpha, rmed, rmax1, M_PI, 360 );
        }
      } else { // inside RIGHT-sector
        h0 = dh;
      }
    }
  } else if ( nl == 1 ) {
    if ( nr == 0 ) {
      h0 = - dh * cos( alpha - left[0] );
    } else if ( nr == 1 ) {
      if ( right[0] < left[0] ) {
        if ( alpha > right[0] && alpha < left[0] ) {
          h0 = dh * expand( alpha, right[0], left[0], 0, M_PI );
        } else {
          if ( alpha < right[0] ) alpha += 360;
          right[0] += 360;
          h0 = - dh * expand( alpha, left[0], right[0], 0, M_PI );
        }
      } else { // right[0] > left[0] 
        if ( alpha > left[0] && alpha < right[0] ) {
          h0 = - dh * expand( alpha, left[0], right[0], 0, M_PI );
        } else {
          if ( alpha < left[0] ) alpha += 360;
          left[0] += 360;
          h0 = dh * expand( alpha, right[0], left[0], 0, M_PI );
        }
      }
    } else { // nr > 1 
      if ( krmax == nr-1 ) {
        if ( left[0] < right[0] ) left[0] += 360;
        if ( alpha < right[0] ) alpha += 360;
      }
      if ( ! ( left[0] > rmax && left[0] < rmax1 ) ) {
        fprintf(stderr, "%s:%d DrawDataFromPoint():\n", __FILE__, __LINE__ );
        fprintf(stderr, "  strange arrangement left %f right from %f to %f\n",
          left[0], rmax1, rmax );
        fprintf(stderr, "Please, report it including your data.\n");
      } else {
        if ( alpha > rmax && alpha < rmax1 ) { // outside RIGHT-sector
          if ( alpha > left[0] ) {
            h0 = - dh * expand( alpha, left[0], rmax1, 0, M_PI );
          } else {
            h0 = dh * expand( alpha, rmax, left[0], 0, M_PI );
          }
        } else { // inside RIGHT-sector
          h0 = dh;
        }
      }
    }
  } else { // nl > 1
    if ( nr <= 1 ) {
      double lmed = (nr==1) ? right[0] : (lmax + lmax1)/2;
  
      if ( klmax == nl-1 ) {
        if ( alpha < left[0] ) alpha += 360;
        if ( nr == 1 && lmed < left[0] ) lmed += 360;
      }
      if ( alpha > lmax && alpha < lmax1 ) { // outside LEFT-sector
        if ( alpha < lmed ) {
          h0 = - dh * expand( alpha, lmax, lmed, 0, M_PI );
        } else {
          h0 = dh * expand( alpha, lmed, lmax1, 0, M_PI );
        }
      } else { // inside LEFT-sector
        h0 = -dh;
      }
    } else { // nr > 1
      if ( klmax == nl-1 ) {
        if ( alpha < left[0] ) alpha += 360;
      }
      if ( alpha > lmax && alpha < lmax1 ) { // outside LEFT-sector
        if ( alpha < rmax1 ) { // from LEFT to RIGHT
          h0 = - dh * expand( alpha, lmax, rmax1, 0, M_PI );
        } else if ( alpha < rmax ) { // inside RIGHT-sector
          h0 = dh;
        } else {
          h0 = dh * expand( alpha, rmax, lmax1, 0, M_PI );
        }
      } else { // inside LEFT-sector
        h0 = -dh;
      }
    }
  }
  delete[] right;
  delete[] left;
  return (int)h0;
#endif
}

void
Plot::drawFromPoint( PlotPoint * p0,
                     double dn, double de, double dh, double dz,
                     DBlock * blk, unsigned char extend, int mode )
{
  ARG_CHECK( p0 == NULL, );
  DBG_CHECK("DrawFromPoint() %s extend %d mode %d\n", blk->From(), extend, mode );

  int x0 = p0->x0;
  int y0 = p0->y0;
  int z0 = p0->z0; // depth
  if ( mode == MODE_PLAN ) {
    x0 += (int)(de); // east
    y0 -= (int)(dn); // south
  } else if ( mode == MODE_EXT ) {
    switch ( extend ) {
      case EXTEND_LEFT:
        x0 -= (int)(dh); // horiz.
        y0 -= (int)(dz); // vert down.
        break;
      case EXTEND_RIGHT:
        x0 += (int)(dh); // horiz.
        y0 -= (int)(dz); // vert down.
        break;
      case EXTEND_VERT:
        // x0 += 0; // horiz.
        y0 -= (int)(dz); // vert down.
        break;
      case EXTEND_IGNORE:
        return;
      default:
        {
          double ext = computeXExtend( p0, de, dn );
          x0 += (int)(dh * ext);
          y0 -= (int)(dz); // vert down.
          extend = (ext < -0.25)? EXTEND_LEFT 
                                : (ext > 0.25)? EXTEND_RIGHT
                                              : EXTEND_VERT;
          blk->setExtended( extend );
        }
        break;
    }
  } else if ( mode == MODE_CROSS || mode == MODE_3D ) {
    // compute projections
    double np = NP( dn, de);     // phi displacement
    double nt = NT( dn, de, dz); // theta displacement
    double nd = ND( dn, de, dz); // depth displacement
    x0 += (int)(np);
    y0 += (int)(nt);
    z0 += (int)(nd);
  }

  if ( segment == NULL ) {
    x0min = x0max = p0->x0;
    y0min = y0max = p0->y0;
  } else {
    if ( p0->x0 < x0min )      x0min = p0->x0;
    else if ( p0->x0 > x0max ) x0max = p0->x0;
    if ( p0->y0 < y0min )      y0min = p0->y0;
    else if ( p0->y0 > y0max ) y0max = p0->y0;
  }
  if ( x0 < x0min )      x0min = x0;
  else if ( x0 > x0max ) x0max = x0;
  if ( y0 < y0min )      y0min = y0;
  else if ( y0 > y0max ) y0max = y0;

  PlotSegment * s = new PlotSegment;
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = x0;
  s->y1 = y0;
  s->z1 = z0;
  s->p0 = p0->name;       // endpoints names
  s->p1 = NULL;
  s->block = blk;         // data block
  s->cs_extend = extend;  // extend used for this segment (call-parameter)
  s->cs_type = CS_SPLAY;

  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  // draw_point_2_point( p0->x0, p0->y0, x0, y0 );
}

#ifdef HAS_LRUD
void
Plot::drawLRUD( PlotPoint * p0, LRUD * lrud, DBlock * blk, 
                double dn0, double de0, int mode )
{
  ARG_CHECK( p0 == NULL, );
  ARG_CHECK( lrud == NULL, );
  ARG_CHECK( blk == NULL, );

  if ( mode == MODE_PLAN ) {
    double dd = ( dn0*dn0 + de0*de0 );
    if ( dd < 0.00000001 ) return;
    dd = sqrt( dd );
    de0 = de0 * BASE_SCALE / dd;
    dn0 = dn0 * BASE_SCALE / dd;
    PlotSegment * s = new PlotSegment; // RIGHT
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 + (int)(lrud->right * dn0);
    s->y1 = p0->y0 + (int)(lrud->right * de0);
    s->z1 = p0->z0;
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
    s = new PlotSegment; // LEFT
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 - (int)(lrud->left * dn0);
    s->y1 = p0->y0 - (int)(lrud->left * de0);
    s->z1 = p0->z0;
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
  } else if ( mode == MODE_EXT ) {
    PlotSegment * s = new PlotSegment; // UP
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0;
    s->y1 = p0->y0 - (int)(lrud->up * BASE_SCALE);
    s->z1 = p0->z0;
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
    s = new PlotSegment; // DOWN
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0;
    s->y1 = p0->y0 + (int)(lrud->down * BASE_SCALE);
    s->z1 = p0->z0;
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
  } else if ( mode == MODE_3D ) {
    double dd = ( dn0*dn0 + de0*de0 );
    if ( dd < 0.00000001 ) return;
    dd = sqrt( dd );
    de0 = de0 * BASE_SCALE / dd;
    dn0 = dn0 * BASE_SCALE / dd;
    double de = lrud->right * dn0;   // DE
    double dn = - lrud->right * de0; // DN
    double dz = 0.0;
    double np = NP( dn, de);     // phi displacement
    double nt = NT( dn, de, dz); // theta displacement
    double nd = ND( dn, de, dz); // depth displacement
    PlotSegment * s = new PlotSegment; // RIGHT
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 + (int)(np);
    s->y1 = p0->y0 + (int)(nt);
    s->z1 = p0->z0 + (int)(nd);
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
    de = - lrud->left * dn0;   // DE
    dn = lrud->left * de0; // DN: this must be subtracted to Y
    dz = 0.0;
    np = NP( dn, de);     // phi displacement
    nt = NT( dn, de, dz); // theta displacement
    nd = ND( dn, de, dz); // depth displacement
    s = new PlotSegment; // LEFT
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 + (int)(np);
    s->y1 = p0->y0 + (int)(nt);
    s->z1 = p0->z0 + (int)(nd);
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
    dn = 0.0;
    de = 0.0;
    dz = lrud->up * BASE_SCALE;
    np = NP( dn, de);     // phi displacement
    nt = NT( dn, de, dz); // theta displacement
    nd = ND( dn, de, dz); // depth displacement
    s = new PlotSegment; // UP
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 + (int)(np);
    s->y1 = p0->y0 + (int)(nt);
    s->z1 = p0->z0 + (int)(nd);
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
    dn = 0.0;
    de = 0.0;
    dz = -lrud->down * BASE_SCALE;
    np = NP( dn, de);     // phi displacement
    nt = NT( dn, de, dz); // theta displacement
    nd = ND( dn, de, dz); // depth displacement
    s = new PlotSegment; // DOWN
    s->next = NULL;
    s->x0 = p0->x0;
    s->y0 = p0->y0;
    s->z0 = p0->z0;
    s->x1 = p0->x0 + (int)(np);
    s->y1 = p0->y0 + (int)(nt);
    s->z1 = p0->z0 + (int)(nd);
    s->p0 = p0->name;           // endpoints names
    s->p1 = NULL;
    s->block = blk;
    s->cs_extend = EXTEND_NONE;
    s->cs_type = CS_LRUD;
    if ( segment != NULL ) {
      s->next = segment;
    }
    segment = s;
    ++ nr_segments;
  }
}

void
Plot::drawLRUD( PlotPoint * p0, LRUD * lrud, DBlock * blk, bool reversed )
{
  ARG_CHECK( p0 == NULL, );
  ARG_CHECK( blk == NULL, );

  PlotSegment * s = new PlotSegment; // RIGHT
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p0->x0 + (int)((reversed? lrud->right : lrud->left) * BASE_SCALE);
  s->y1 = p0->y0;
  s->z1 = p0->z0;
  s->p0 = p0->name;           // endpoints names
  s->p1 = NULL;
  s->block = blk;
  s->cs_extend = EXTEND_NONE;
  s->cs_type = CS_LRUD;
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  s = new PlotSegment; // LEFT
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p0->x0 - (int)((reversed? lrud->left : lrud->right) * BASE_SCALE);
  s->y1 = p0->y0;
  s->z1 = p0->z0;
  s->p0 = p0->name;           // endpoints names
  s->p1 = NULL;
  s->block = blk;
  s->cs_extend = EXTEND_NONE;
  s->cs_type = CS_LRUD;
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  s = new PlotSegment; // UP
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p0->x0;
  s->y1 = p0->y0 - (int)(lrud->up * BASE_SCALE); 
  s->z1 = p0->z0;
  s->p0 = p0->name;           // endpoints names
  s->p1 = NULL;
  s->block = blk;
  s->cs_extend = EXTEND_NONE;
  s->cs_type = CS_LRUD;
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  s = new PlotSegment; // DOWN
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p0->x0;
  s->y1 = p0->y0 + (int)(lrud->down * BASE_SCALE);
  s->z1 = p0->z0;
  s->p0 = p0->name;           // endpoints names
  s->p1 = NULL;
  s->block = blk;
  s->cs_extend = EXTEND_NONE;
  s->cs_type = CS_LRUD;
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
}

#endif // HAS_LRUD

void
Plot::drawVerticalSegment( PlotPoint * p0, PlotPoint * p1, DBlock * blk )
{
  ARG_CHECK( p0 == NULL, );
  ARG_CHECK( p1 == NULL, );
  ARG_CHECK( blk == NULL, );

  PlotSegment * s = new PlotSegment;
  s->next = NULL;
  s->x0 = p0->x0;
  s->y0 = p0->y0;
  s->z0 = p0->z0;
  s->x1 = p1->x0;
  s->y1 = p1->y0;
  s->z1 = p1->z0;
  s->p0 = p0->name;           // endpoints names
  s->p1 = p1->name;
  s->block = blk;             // data block
  s->cs_extend = EXTEND_VERT; // extend flags
  s->cs_type = CS_CENTERLINE;
  // horizontal angle with the North
  // s->horiz_angle = 180.0/M_PI*( atan2( de, dn ) );
  // if ( s->horiz_angle < 0 ) s->horiz_angle += 360;

  p0->segments.push_back( s );
  p1->segments.push_back( s );
  // draw_point_2_point( p0->x0, p0->y0, p1->x0, p1->y0 );
  if ( segment != NULL ) {
    s->next = segment;
  }
  segment = s;
  ++ nr_segments;
  ++ nr_centerline_segments;
}


#ifdef IMPORT_TH2
/** The bounding box is used to compute the plot width and height
 *      width  = xmax - xmin
 *      height = ymax - ymin
 * The plot size is used to cmpute the offset(s) (@see PlotCanvasScene::setOffset() ), eg,
 *      offset_X = width / PERCENT_OFFSET - xmin
 * so that for any point
 *      X + offset_X >= width / PERCENT_OFFSET
 *
 * The PlotCanvasScene sets the width to
 *      scene_width =  2*offset_X + plot_width
 *     
 * Points coords (in ThPoint2D) are divided by PLOT_SCALE with respect to the
 * scene points. Line-point coords (ThLinePoint in ThLine2D) are the same as in
 * the scene.
 *    @see PlotCanvasScene::insertPoint and PlotCanvasScene::insertLinePoint
 *
 * stations labels:
 *   X_num --> X_scene = PLOT_SCALE * ( X_num + X_offset )
 * station points @see PlotCanvas::insertStationPoint
 *   X_scene --> X_scrap = X_scene + X_offset
 * other points
 *   X_scene --> X_scrap = X_scene / PLOT_SCALE
 *
 */
void
Plot::adjustToScrap( TherionScrap * scrap )
{
  if ( scrap == NULL ) return;
  for ( std::vector< ThPoint2D * >::const_iterator pit = scrap->pointsBegin(), end = scrap->pointsEnd();
          pit != end; ++pit ) {
    ThPoint2D * pt = *pit;
    double x = pt->x;
    double y = pt->y;
    if ( x0min < x ) { x0min = x; }
    if ( x0max > x ) { x0max = x; }
    if ( y0min < y ) { y0min = y; }
    if ( y0max > y ) { y0max = y; }
  }
  for ( std::vector< ThLine2D * >::const_iterator lit = scrap->linesBegin(), end = scrap->linesEnd();
        lit != end;
        ++lit ) {
    for ( std::vector< ThLinePoint * >::const_iterator pit = (*lit)->pointBegin(), pend = (*lit)->pointEnd();
          pit != pend;
          ++pit ) {
      ThLinePoint * pt = *pit;
      double x = pt->x / PLOT_SCALE;
      double y = pt->y / PLOT_SCALE;
      if ( x0min < x ) { x0min = x; }
      if ( x0max > x ) { x0max = x; }
      if ( y0min < y ) { y0min = y; }
      if ( y0max > y ) { y0max = y; }
    }
  }
  // TODO for areas
} 
#endif
