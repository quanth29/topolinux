/** @file calib.cpp
 * 
 * @author marco corvi
 * @date march 2009
 * 
 * @brief execute the Calibration algorithm
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "Factors.h"
#include "Calibration.h"

void usage()
{
  static bool printed_usage = false;
  if ( ! printed_usage ) {
    fprintf(stderr, "Usage: calib [-i max_iter] [-p] [-h] <input_file> [output_file]\n");
    fprintf(stderr, "Options: \n");
    fprintf(stderr, "  -i max_iter   max nr. iterations [default 2000]\n");
#ifdef EXPERIMENTAL
    fprintf(stderr, "  -m mode       mode: 1 PocketTopo, 2 Gradient, 3 Delta, 4 PocketTopo w. GUI, 5 Delta w. GUI\n");
    fprintf(stderr, "  -d delta      required delta (Delta mode only, default 0.5)\n");
#endif
    fprintf(stderr, "  -p            print input data\n");
    fprintf(stderr, "  -v            verbose\n");
    fprintf(stderr, "  -h            print usage\n");
    fprintf(stderr, "The input file contains the calibration data\n");
    fprintf(stderr, "one record per line, in the format: \n");
    fprintf(stderr, "   Gx Gy Gz Mx My Mz group ignore\n");
    fprintf(stderr, "where the G and M are hex, group and ignore are decimal\n");
#ifdef EXPERIMENTAL
    fprintf(stderr, "The mode is one of:\n");
    fprintf(stderr, "  1 PocketTopo algorithm with angular error instead of rms error\n");
    fprintf(stderr, "  2 Gradient algorithm \n");
    fprintf(stderr, "  3 PocketTopo algorithm iterated until the error is below delta\n");
    fprintf(stderr, "  4 mode 1 with graphical display of set planes\n");
    fprintf(stderr, "  5 mode 3 with graphical display of set planes\n");
#endif
    fprintf(stderr, "The calibration coefficients are written to the output_file\n");
    fprintf(stderr, "if specified.\n");
  }
  printed_usage = true;
}

/** 
 * syntax:
 *    calib <input_file>
 * where
 */
#include <time.h>

int main( int argc, char ** argv )
{
  char * in_file = NULL;
  char * out_file = NULL;

  unsigned int max_it = 2000; // Max nr. of iterations
  bool print_input = false;
  bool verbose = false;
  int mode = 1;  // 1: PocketTopo Optimize
                 // 2: Optimize2
                 // 3: OptimizeBest
                 // 4: PocketTopo Optimize with optimize_axis
                 // 5: OptimizeBest with optimize_axis
                 // 6: Optimize M at fixed G 
  double delta = 0.5; // required delta for OptimizeBest
  double error;
  unsigned int iter = 0;

  int ac = 1;
  while ( ac < argc ) {
    if ( strncmp(argv[ac],"-i",2) == 0 ) {
      max_it = atoi( argv[ac+1]);
      if ( max_it < 200 ) max_it = 200;
      ac += 2;
#ifdef EXPERIMENTAL
    } else if ( strncmp(argv[ac],"-m", 2) == 0 ) {
      mode = atoi( argv[ac+1] );
      if ( mode < 1 || mode > 7 ) mode = 1;
      ac += 2;
    } else if ( strncmp(argv[ac],"-d", 2) == 0 ) {
      delta = atof( argv[ac+1] );
      if ( delta < 0.001 ) delta = 0.5;
      ac += 2;
#endif
    } else if ( strncmp(argv[ac],"-p", 2) == 0 ) {
      print_input = true;
      ac ++;
    } else if ( strncmp(argv[ac],"-v", 2) == 0 ) {
      verbose = true;
      ac ++;
    } else if ( strncmp(argv[ac],"-h", 2) == 0 ) {
      usage();
      ac ++;
    } else {
      break;
    }
  }

  if ( ac >= argc ) {
    usage();
    return 0;
  }
  in_file = argv[ac];
  ac ++;
  if ( argc > ac ) {
    out_file = argv[ac];
  }
  
  if ( verbose ) {
    fprintf(stderr, "Calibration data file \"%s\"\n", in_file );
    if ( out_file ) {
      fprintf(stderr, "Calibration coeff file \"%s\"\n", out_file );
    }
    fprintf(stderr, "Max nr. iterations %d \n", max_it );
  }

  FILE * fp = fopen( in_file, "r" );
  if ( fp == NULL ) {
    fprintf(stderr, "ERROR: cannot open input file \"%s\"\n", in_file);
    return 0;
  }

  Calibration calib;
  unsigned int gx0, gy0, gz0, mx0, my0, mz0;
  int16_t gx, gy, gz, mx, my, mz;
  int grp;
  int ignore;
  int cnt = 0;
  char line[256];
  while ( fgets(line, 255, fp ) ) {
    if ( line[0] == '#' ) continue;
    sscanf( line, "%x %x %x %x %x %x %d %d",
          &gx0, &gy0, &gz0, &mx0, &my0, &mz0, &grp, &ignore);
    gx = (int16_t)( gx0 & 0xffff );
    gy = (int16_t)( gy0 & 0xffff );
    gz = (int16_t)( gz0 & 0xffff );
    mx = (int16_t)( mx0 & 0xffff );
    my = (int16_t)( my0 & 0xffff );
    mz = (int16_t)( mz0 & 0xffff );
    calib.AddValues( gx, gy, gz, mx, my, mz, cnt, grp, ignore );
    // printf(" G: %d %d %d  M: %d %d %d  [%d]\n",
    //        gx, gy, gz, mx, my, mz, grp );
    ++cnt;
  }
  fclose( fp );


  if ( print_input ) {
    calib.PrintValues();
    // { int i; scanf("%d", &i); }
    // calib.CheckInput();
    // { int i; scanf("%d", &i); }
    calib.PrintGroups();
  }

  calib.PrepareOptimize();
  
  if ( mode == 1 ) {
    iter = calib.Optimize( delta, error, max_it );
#ifdef EXPERIMENTAL
  } else if ( mode == 2 ) {
    iter = calib.Optimize2( delta, error, max_it );
  } else if ( mode == 3 ) {
    iter = calib.OptimizeBest( delta, error, max_it );
  } else if ( mode == 4 ) {
    iter = calib.Optimize( delta, error, max_it, true );
  } else if ( mode == 5 ) {
    iter = calib.OptimizeBest( delta, error, max_it, true );
  } else if ( mode == 6 ) {
    // TODO set the G coeffs
    Vector b( 0.0037, -0.0725, -0.0300);
    Vector ax( 1.7678, -0.0013, 0.0119);
    Vector ay( -0.0023, 1.7657, -0.0016);
    Vector az(-0.0112, -0.0016, 1.7660);
    Matrix a( ax, ay, az );
    iter = calib.Optimize( delta, error, max_it );
    fprintf(stdout, "Iterations %d\n",  iter);
    fprintf(stdout, "Delta      %.4f\n", delta );
    fprintf(stdout, "Max error  %.4f\n", error );
    calib.PrintCoeffs();
    delta = 0.5;
    iter = 0;
    error = 0.0;
    calib.SetGCoeffs( a, b );
    iter = calib.OptimizeM( delta, error, max_it );
  } else if ( mode == 7 ) {
    /*
    Vector b( 0.0037, -0.0725, -0.0300);
    Vector ax( 1.7678, -0.0013, 0.0119);
    Vector ay( -0.0023, 1.7657, -0.0016);
    Vector az(-0.0112, -0.0016, 1.7660);
    Matrix a( ax, ay, az );
    calib.SetGCoeffs( a, b );
    */
    iter = calib.OptimizeIterative( delta, error, max_it );
#endif // EXPERIMENTAL
  }

  fprintf(stdout, "Iterations %d\n",  iter);
  fprintf(stdout, "Delta      %.4f\n", delta );
  fprintf(stdout, "Max error  %.4f\n", error );
  if ( verbose ) {
    calib.PrintCoeffs();
    // calib.CheckInput();
  }

#if 0
  unsigned char data[48];
  calib.GetCoeff( data );
  for (int k=0; k<48; ++k) {
    printf("0x%02x ", data[k] );
    if ( ( k % 4 ) == 3 ) printf("\n");
  }
#endif

  if ( out_file != NULL ) {
    calib.PrintCalibrationFile( out_file );
  }

  return 0;
}
