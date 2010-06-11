/** @file ArgCheck.cpp
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief arguments checks
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef ARG_CHECK_H
#define ARG_CHECK_H

#include <stdio.h>

extern bool do_debug;

#define CHECK_ARGS 1

#ifdef CHECK_ARGS
  #define ARG_CHECK( test, ret ) \
    if ( test ) { \
      if ( do_debug ) fprintf(stderr, "%s:%d error " #test "\n", __FILE__, __LINE__ ); \
      return ret; \
    }
#else
  #define ARG_CHECK( ... ) /* nothing */
#endif

#ifdef CHECK_DBG
  #define DBG(x) x
  #define DBG_CHECK if ( do_debug ) printf
#else
  #define DBG(x) /* nothing */
  #define DBG_CHECK( ... ) /* nothing */
#endif 


#endif
