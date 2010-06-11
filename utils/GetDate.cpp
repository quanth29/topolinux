/** @file GetDate.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief date
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <time.h>

#include "GetDate.h"

void GetDate( int * day, int * month, int * year )
{
  time_t t;
  struct tm *tp;
  time( &t );
  tp = localtime( &t );
  *day   = tp->tm_mday;
  *month = tp->tm_mon + 1;
  *year  = tp->tm_year + 1900;
}

void GetDate( int * day, int * month, int * year, int * hour, int * min, int * sec )
{
  time_t t;
  struct tm *tp;
  time( &t );
  tp = localtime( &t );
  *day   = tp->tm_mday;
  *month = tp->tm_mon + 1;
  *year  = tp->tm_year + 1900;
  *hour  = tp->tm_hour;
  *min   = tp->tm_min;
  *sec   = tp->tm_sec;
}


void getFilename( const char * directory, char * filename, const char * ext )
{
  int day, mon, year;
  GetDate( &day, &mon, &year );
  sprintf(filename, "%s/%04d%02d%02d.%s", directory, year, mon, day, ext );
  struct stat sb;
  if ( stat( filename, &sb ) == 0 ) {
    int cnt = 0;
    do {
      sprintf(filename+12, "-%02d.%s", cnt, ext );
      ++ cnt;
    } while ( stat( filename, &sb ) == 0 );
  }
}

