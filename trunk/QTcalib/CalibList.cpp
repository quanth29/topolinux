/** @file CalibList.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief OpenTopo calibration data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <math.h>
#include <sstream>

#include <QWidget>
#include <QMessageBox>

#include "Factors.h"
#include "Vector.h"
#include "CalibList.h"


typedef unsigned short uint16_t;

// ================================================================

void
CalibList::computeData( const CTransform * t )
{
  if ( t ) {
    for ( CBlock * b = head; b != NULL; b=b->next ) {
      b->computeCompassAndClino( *t );
    }
  } else {
    CTransform t;
    for ( CBlock * b = head; b != NULL; b=b->next ) {
      b->computeCompassAndClino( t );
    }
  }
}

void 
CalibList::clear()
{
  CBlock * next;
  CBlock * b = head;
  while ( b != NULL ) {
    next = b->next;
    delete b;
    b = next;
  }
  head = NULL;
  size = 0;
  // default vales
  memset( coeff, 0x00, 48 );
  coeff[0x03] = 0x40;
  coeff[0x0d] = 0x40;
  coeff[0x17] = 0x40;
  coeff[0x1b] = 0x40;
  coeff[0x25] = 0x40;
  coeff[0x2f] = 0x40;
}

bool 
CalibList::save( const char * filename, std::string & comment )
{
    FILE * fp = fopen( filename, "w" );
    if ( fp == NULL ) {
      return false;
    }
    for (int i=0; i<48; ++i ) {
      fprintf(fp, "0x%02x ", coeff[i] );
      if ( ( i % 8 ) == 7 ) fprintf(fp, "\n");
    }
    fprintf(fp, "Calibration input data.\n");
    for ( CBlock * b = head; b != NULL; b=b->next ) {
      fprintf(fp, "G: %d %d %d M: %d %d %d Set %s %d %.4f\n",
        b->gx, b->gy, b->gz, b->mx, b->my, b->mz, b->Group(),
        (b->ignore)? 1 : 0, b->error );
    }
    if ( comment.size() > 0 ) {
      fprintf(fp, "# ---------------------\n");
      fprintf(fp, "Description:\n");
      fprintf(fp, comment.c_str() );
      fprintf(fp, "\n" );
    }
    fclose( fp );
    return true;
}

bool 
CalibList::load( const char * filename, std::string & comment, int angle )
{
  // if ( do_debug )
  //   fprintf(stderr, "CalibList::load() %s\n", filename );
  clear();
  char line[256];
  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    return false;
  }
  if ( fgets(line, 256, fp) == NULL ) { // empty file
    fclose( fp );
    return false;
  }
  if ( line[0] != '0' || line[1] != 'x' ) { // wrong format
    fprintf(stderr, "CalibList::load() wrong file format. Line %s", line );
    fclose( fp );
    return false;
  }
  rewind( fp );
  if ( line[4] == ' ' ) { 
    // fprintf(stderr, "CalibList::load() coeff file %s\n", filename );
    return loadCoeff( fp, comment );
  } 
  // fprintf(stderr, "CalibList::load() data file %s\n", filename );
  size_t ret = loadData( fp, angle );
  fclose( fp );
  return ret > 0;
}


size_t
CalibList::loadData( FILE * fp, int angle )
{
  CBlock * b0 = NULL;
  char line[256];

  unsigned int gx0, gy0, gz0, mx0, my0, mz0;
  char group[32];
  int ignore;
  size_t ret = 0; // number of read data 
  bool auto_guess = true;
  while ( fgets( line, 256, fp ) ) {
    // if ( line[0] == '#' ) break;
    if ( sscanf( line, "%x %x %x %x %x %x %s %d", 
            &gx0, &gy0, &gz0, &mx0, &my0, &mz0, group, &ignore) != 8 ) {
      break;
    }
    int16_t gx = (int16_t)(gx0 & 0xffff);
    int16_t gy = (int16_t)(gy0 & 0xffff);
    int16_t gz = (int16_t)(gz0 & 0xffff);
    int16_t mx = (int16_t)(mx0 & 0xffff);
    int16_t my = (int16_t)(my0 & 0xffff);
    int16_t mz = (int16_t)(mz0 & 0xffff);
    if ( group[0] != '-' || group[1] != '1' ) auto_guess = false;

    b0 = addData( b0, gx, gy, gz, mx, my, mz, group, ignore );
    ++ ret;
/*
    CBlock * b = new CBlock( gx, gy, gz, mx, my, mz, group, ignore, 0.0 );
    if ( head == NULL ) {
      head = b0 = b;
    } else {
      b0->next = b;
      b0 = b;
    }
    ++ size;
*/
  }
  if ( auto_guess ) {
    guessGroups( angle );
  }

  return ret;
}


CBlock *
CalibList::addData( CBlock * b0, 
   int16_t gx, int16_t gy, int16_t gz, int16_t mx, int16_t my, int16_t mz,
   const char * group, int ignore )
{
  CBlock * b = new CBlock( gx, gy, gz, mx, my, mz, group, ignore, 0.0 );
  if ( b0 == NULL ) {
    if  ( head != NULL ) clear();
    head = b;
  } else {
    b0->next = b;
  }
  ++ size;
  return b;
}

CBlock *
CalibList::addData( CBlock * b0,
   int16_t gx, int16_t gy, int16_t gz, int16_t mx, int16_t my, int16_t mz, 
   const CTransform & t )
{
  CBlock * b = new CBlock( gx, gy, gz, mx, my, mz, t );
  if ( b0 == NULL ) {
    if  ( head != NULL ) clear();
    head = b;
  } else {
    b0->next = b;
  }
  ++ size;
  return b;
}

double 
Angle( double compass1, double clino1, double compass2, double clino2 )
{
  double c1 = cos( clino1 );
  double z1 = sin( clino1 );
  double x1 = c1 * cos( compass1 );
  double y1 = c1 * sin( compass1 );
  double c2 = cos( clino2 );
  double z2 = sin( clino2 );
  double x2 = c2 * cos( compass2 );
  double y2 = c2 * sin( compass2 );
  return acos( x1*x2 + y1*y2 + z1*z2 ) * 180.0 / M_PI;
}

void 
CalibList::guessGroups( int guess_angle )
{
  int guess = 0;
  double compass2 = 0.0, clino2 = 0.0;
  CBlock * b0 = head;
  while ( b0 ) {
    double compass1 = b0->compass * M_PI/180.0;
    double clino1   = b0->clino   * M_PI/180.0;
    if ( guess == 0 ) {
      compass2 = compass1;
      clino2   = clino1;
      guess = 1;
    } else {
      if ( Angle( compass1, clino1, compass2, clino2 ) > guess_angle ) {
        compass2 = compass1;
        clino2   = clino1;
        ++ guess;
      }
    }
    // printf("Block %8.2f %8.2f Group %2d\n", b0->compass, b0->clino, guess );
    b0->SetGroup( guess );
    b0 = b0->next;
  }
}

bool 
CalibList::loadCoeff( FILE * fp, std::string & comment )
{
  CBlock * b0 = head;
  char line[256];
  for (int i=0; i<48; ++i ) {
    unsigned int u;
    if ( fscanf(fp, "0x%x ", &u ) != 1 ) {
      fclose( fp );
      return false;
    }
    coeff[i] = (unsigned char)u;
  }
  int cnt = 0;
  if ( fgets(line, 256, fp) != NULL ) { // skip a line
    // read calibration data
    while ( fgets(line, 256, fp) != NULL ) {
      // printf(line);
      if ( line[0] == '#' ) break;
      if (strncmp(line, "G:", 2 ) == 0 ) {
        ++ cnt;
        int gx, gy, gz, mx, my, mz, ignore;
        double error;
        char rem[32];
        char group[32];
        if ( sscanf(line+3, "%d %d %d %s %d %d %d %s %s %d %lf",
                    &gx, &gy, &gz, rem, &mx, &my, &mz, rem, group, &ignore, &error )
             != 11 )
          break;
        CBlock * b = new CBlock( gx, gy, gz, mx, my, mz, group, ignore, error );
        if ( head == NULL ) {
          head = b0 = b;
        } else {
          b0->next = b;
          b0 = b;
        }
        ++ size;
      }
    }
  } else {
    // printf("read %d data: cannot skip a line \n", cnt );
    fclose( fp );
    return false;
  }
  // printf("read %d data \n", cnt );
  // loadDescription
  if ( fgets(line, 256, fp) != NULL ) { // empty file
    printf(line);
    if ( strncmp(line, "Description", 11) == 0 ) {
      if ( fgets(line, 256, fp) != NULL ) {
        printf(line);
        comment = line;
      }
    }
  }
  // ignore rest of the file
  fclose( fp );
  return true;
}

void
CalibList::readCoeff( QWidget * widget, const char * filename )
{
  FILE * fp = fopen( filename, "r" );
  if ( fp != NULL ) {
    for ( int k=0; k<48; ++k ) {
      unsigned int c;
      if ( fscanf( fp, "%x", &c ) != 1 ) {
        fclose( fp ); // FIXME coeff are messed up
        return;
      }
      coeff[k] = (unsigned char)(c & 0xff);
    }
    char line[256];
    while ( fgets(line, 256, fp ) ) {
      if ( strncmp(line, "Calibration coeff", 17) == 0 ) {
        std::ostringstream ost;
        ost << line;
        while ( fgets(line, 256, fp ) ) ost << line;
        QMessageBox::information( widget, "Coefficients", ost.str().c_str() );
      }
    }
    fclose( fp );
  }
}

void
CalibList::getCoeff( double * c ) 
{
  for (int k=0; k<24; ++k ) {
    c[k] = (double)( (int16_t)( (uint16_t)(coeff[2*k]) + (((uint16_t)(coeff[2*k+1]))<<8) ) )
      /( (k%4)? FM : FV ) ;
  }
}

void
CalibList::setCoeff( const unsigned char * byte )
{
  memcpy( coeff, byte, 48 );
}

bool
CalibList::writeData( const char * filename )
{
  FILE * fp = fopen( filename, "w" );
  if ( fp == NULL ) 
    return false;

  CBlock * b = head;
  for ( ; b; b=b->next ) {
    uint16_t gx0, gy0, gz0, mx0, my0, mz0;
    gx0 = (uint16_t)(b->gx);
    gy0 = (uint16_t)(b->gy);
    gz0 = (uint16_t)(b->gz);
    mx0 = (uint16_t)(b->mx);
    my0 = (uint16_t)(b->my);
    mz0 = (uint16_t)(b->mz);
    fprintf(fp, "0x%04x 0x%04x 0x%04x 0x%04x 0x%04x 0x%04x %s %d\n",
      gx0, gy0, gz0, mx0, my0, mz0, b->Group(), b->ignore );
  }
  fclose( fp );
  return true;
}

void
CalibList::readData( const char * filename, int angle )
{
  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    return;
  }
  loadData( fp, angle );
  fclose( fp );
}

void
CalibList::initCalib( Calibration & calib ) 
{
  CBlock * b = head;
  int cnt = 0;
  for ( ; b; b=b->next ) {
    int grp = atoi(  b->Group() );
    calib.AddValues( b->gx, b->gy, b->gz, b->mx, b->my, b->mz, 
      cnt, grp, b->ignore );
    cnt ++;
  }
}

void 
CalibList::getErrors( Calibration & calib ) 
{
  CBlock * b = head;
  size_t cnt = 0;
  for ( ; b; b=b->next ) {
    b->error = calib.GetError( cnt );
    ++ cnt;
  }
}


int 
CalibList::toggleIgnore( int r )
{
  int ret = 0;
  CBlock * b = head;
  while ( b && r > 0 ) { --r; b=b->next; }
  if ( b ) {
    ret = b->ignore = 1 - b->ignore;
  }
  return ret;
}
  
/*
void
CalibList::updateGroup( int r, const char * txt )
{
  CBlock * b = head;
  while ( b && r > 0 ) { --r; b=b->next; }
  if ( b ) {
    b->group = txt;
  }
}
*/


