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
#include <stdio.h>
#include <string.h>
#include <math.h>

// #include "Programs.h"
#include "Coverage.h"

#ifndef M_PI
  #define M_PI 3.14159265358979323846
#endif

#define BYTES 4 /* RGB need 32 bits */
#define AZIMUTH_BIT 16

Coverage::Coverage()
  : t_dim( 0 )
{
  for ( int i=0; i<19; ++i ) { // clino angles: from +90 to -90
    clino_angles[i] = 90 - 10*i;
  }
  t_size[ 0 ] = t_size[18] = 1;
  for ( int i=1; i<9; ++i ) {
    t_size[i] = t_size[18-i] = AZIMUTH_BIT * i;
  }
  t_size[ 9 ] = AZIMUTH_BIT * 9; // max azimuth steps 54 at clino 0

  t_offset[0] = 0;
  for (int k=1; k<19; ++k ) {
    t_offset[k] = t_offset[k-1] + t_size[k-1];
  }
  t_dim = t_offset[18] + t_size[18];
  angles = new Direction [ t_dim ];
  for (int k = 0; k<19; ++k ){
    for (int j=t_offset[k]; j<t_offset[k]+t_size[k]; ++j ) {
      angles[j].clino   = clino_angles[k] * M_PI / 180.0;
      angles[j].compass = M_PI + ( 2.0 * M_PI * (j - t_offset[k]) ) / t_size[k];
    }
  }
  img = new unsigned char [ COVERAGE_WIDTH * COVERAGE_HEIGHT * BYTES ]; // 0xff-RGB
}

double Cosine( double compass1, double clino1, double compass2, double clino2 )
{
  double h1 = cos( clino1 );
  double z1 = sin( clino1 );
  double x1 = h1 * cos( compass1 );
  double y1 = h1 * sin( compass1 );
  double h2 = cos( clino2 );
  double z2 = sin( clino2 );
  double x2 = h2 * cos( compass2 );
  double y2 = h2 * sin( compass2 );
  return x1*x2 + y1*y2 + z1*z2; // cosine of the angle
}


void 
Coverage::UpdateDirections( double compass, double clino, int cnt )
{
  for (int j=0; j<t_dim; ++j ) {
    double c = Cosine( compass, clino, angles[j].compass, angles[j].clino );
    if ( c > 0.0 ) {
      c = c * c;
      angles[j].value -= (cnt >= 4)? c*c : c*c*cnt*0.25;
      if ( angles[j].value < 0.0 ) angles[j].value = 0.0;
    }
  }
}

double
Coverage::EvaluateCoverage( CalibList & clist )
{
  for (int j=0; j<t_dim; ++j ) angles[j].value   = 1.0;
  CTransform t( clist.getCoeff() );

  const char * old_grp = NULL;
  double compass_avg = 0.0;
  double clino_avg   = 0.0;
  int cnt_avg = 0;
  for (CBlock * b = clist.head; b != NULL; b=b->next ) {
    if ( b->ignore != 0 ) continue;
    Vector g( b->gx, b->gy, b->gz );
    Vector m( b->mx, b->my, b->mz );
    double compass, clino, roll;
    t.ComputeCompassAndClino( g, m, compass, clino, roll );
    compass *= M_PI/180.0; // GRAD2RAD_FACTOR;
    clino   *= M_PI/180.0; // GRAD2RAD_FACTOR;
    if ( old_grp && strcmp( b->Group(), old_grp ) == 0 ) {
      if ( cnt_avg > 0 && fabs( compass - compass_avg / cnt_avg ) > 1.5*M_PI ) {
        if ( compass > M_PI ) {
          compass -= 2.0 * M_PI; // average around 0
        } else {
          compass += 2.0 * M_PI; // average around 360
        }
      }
      // printf("cnt %2d add  clino %8.2f compass %8.2f\n", cnt_avg, clino, compass ); 
      clino_avg   += clino;
      compass_avg += compass;
      cnt_avg     ++;
    } else {
      if ( cnt_avg > 0 ) {
        compass_avg /= cnt_avg;
        clino_avg   /= cnt_avg;
        UpdateDirections( compass_avg, clino_avg, cnt_avg );
      }
      clino_avg   = clino;
      compass_avg = compass;
      cnt_avg     = 1;
      // printf("cnt %2d init clino %8.2f compass %8.2f\n", cnt_avg, clino, compass ); 
      old_grp = b->Group();
    }
  }
  if ( cnt_avg > 0 ) {
    compass_avg /= cnt_avg;
    clino_avg   /= cnt_avg;
    UpdateDirections( compass_avg, clino_avg, cnt_avg );
  }

  double coverage = 0.0;
  for (int j=0; j<t_dim; ++j ) {
    coverage += angles[j].value;
  }
  
  coverage = 1.0 - coverage/t_dim;
  // fprintf(stderr, "4-pi Coverage %.2f \%\n", coverage );
  return 100.0*coverage;
}

// image is 90 * 180 * 4
unsigned char * 
Coverage::FillImage( const char * filename )
{
  memset( img, 0xcc, COVERAGE_WIDTH * COVERAGE_HEIGHT * BYTES ); // grey
  for (int j0=0; j0<COVERAGE_HEIGHT; ++j0) {
    int j = 2 * j0;
    double clino = j - 90.0;
    int j1 = j/10;
    int j2 = j1 + 1;
    // assert( j2 < 19 );
    double d = (j%10)/10.0;
    int j1off = t_offset[j1];
    int j2off = t_offset[j2];
    double amax = 180.0 * sqrt( 1.0 - (clino/90.0)*(clino/90.0) );
    // if ( amax < 1.0 ) amax = 1.0;
    int ioff = (180 - (int)(amax)) / 2;
    if (ioff < 0 ) ioff = 0;
    int ixold = -1;
    for (int i0=0; i0<COVERAGE_WIDTH; ++i0) {
      int i = 2 * i0;
      double compass = (i + 180)%360; // N middle, W left, E right
      int ix = (int)(compass/180.0*amax);  // from 0 to 2*amax
      ix /= 2;
      if ( ix == ixold ) continue;
      ixold = ix;
      double c1 = compass/360.0*t_size[j1];
      double c2 = compass/360.0*t_size[j2];
      int i11 = (int)(c1); // index in [0, t_size)
      int i21 = (int)(c2);
      int i12 = (i11 + 1)%t_size[j1];
      double d1 = c1 - i11;
      int i22 = (i21 + 1)%t_size[j2];
      double d2 = c2 - i21;
      double v1 = angles[j1off+i11].value * (1-d1) + angles[j1off+i12].value * d1;
      double v2 = angles[j2off+i21].value * (1-d2) + angles[j2off+i22].value * d2;
      double v = v1 * (1-d) + v2 * d;
      int off = (j0*180 + (ioff + ix))*BYTES;
      unsigned char col = ( v > 254.0 )? 254 : (unsigned char)(254*v);
      // if ( col > 200 ) {
      //   printf("J0 %d j %d j1 %d j2 %d I0 %d i %d i11 %d i21 %d i12 %d i22 %d col %d\n",
      //     j0, j, j1, j2, i0, i, i11, i21, i12, i22, col );
      // }
      img[off+0] = 0;
      img[off+1] = 0xff-col;
      img[off+2] = col;
      img[off+3] = 0xff;
      // // img[off+3] = alpha; 
      
    }
  }
  if ( filename ) {
    FILE * fp = fopen( filename, "w" );
    if ( ! fp ) {
      fprintf(stderr, "Cannot open XPM file %s\n", filename);
      // return NULL;
    } else {
      // fprintf(stderr, "writing image to \"%s\"\n", filename);
      // PGM
      // fprintf(fp, "P5\n%d\n%d\n255\n", COVERAGE_WIDTH, COVERAGE_HEIGHT );
      // fwrite(img, 1, COVERAGE_WIDTH*COVERAGE_HEIGHT, fp);
      //
      fprintf(fp, "/* XPM */\n");
      fprintf(fp, "static char * cover_xpm[] = {\n");
      fprintf(fp, "\"%d %d 256 2\",\n", COVERAGE_WIDTH, COVERAGE_HEIGHT );
      for (int k=0; k<255; ++k) {
        fprintf(fp, "\"%02x\tc #%02x0000\",\n", k, k );
      }
      fprintf(fp, "\"ff\tc #808080\",\n");
      for ( int j=0; j<COVERAGE_HEIGHT; ++j) {
        fprintf(fp, "\"");
        for (int i=0; i<COVERAGE_WIDTH; ++i) {
          fprintf(fp, "%02x", img[0+(j*COVERAGE_WIDTH+i)*BYTES] );
        }
        fprintf(fp, (j<COVERAGE_HEIGHT-1) ? "\",\n" : "\"\n");
      }
      fprintf(fp, "};\n");
      fclose( fp );
    }
  }
  return img;
}

