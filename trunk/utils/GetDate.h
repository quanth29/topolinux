/** @file GetDate.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief date
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef GET_DATE_H
#define GET_DATE_H


/** get the date
 * @param day    day of the month
 * @param month  month (1 to 12)
 * @param year   year
 */
void 
GetDate( int * day, int * month, int * year );

void 
GetDate( int * day, int * month, int * year, int * hour, int * min, int * sec );

/** get a new filename in the directory "./tmp"
 * @param directory  name of the temporary directory
 * @param filename   output filename (must be preallocated by the caller)
 * @param ext        filename extension
 */
void getFilename( const char * directory, char * filename, const char * ext );

#endif
