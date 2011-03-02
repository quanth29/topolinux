/** @file config.cpp
 *
 * @author marco corvi
 * @date june 2009
 *
 * @brief stripped down configuration strings manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>

#include "config.h"

Config * 
Config::the_config = NULL;

Config::Config( )
{
  LoadDefaults();
}

void
Config::LoadDefaults()
{
  m_param["DEBUG"] = "no";
  m_param["NEWBY"] = "no";
  m_param["DISTO_LOG"] = "no";
  m_param["LRUD"] = "no";
  m_param["GEOMETRY"] = "480x640";
  m_param["Y_OFFSET"] = "30";
  m_param["V_THRESHOLD"] = "80.0";
  m_param["COMMENT_SIZE"] = "20";
  m_param["LENGTH_UNITS"] = "meter";
  m_param["ANGLE_UNITS"] = "degree";
  m_param["EXPORT"] = "therion";
  // m_param["LANGUAGE"] = "i18n/language-it.txt";
  // m_param["LOCALE"] = "it";
  m_param["LANG"] = "en";
  m_param["CALIB_GUESS"] = "20";
  m_param["BACKUP_FILE"] = "backup";
  m_param["PT_LINES"] = "0 1 4 3 5 6 2";
  m_param["PT_POINTS"] = "0 1 2 3 5 6 8";

  #if defined WIN32
    m_param["DEVICE" ] = "com1";
    m_param["ROOT" ] = "/home/projects/Disto/DATA";
    m_param["BROWSER" ] = "/usr/bin/iceweasel";
    m_param["DEFAULT_DATA"] = "/home/projects/Disto/DATA/data.tlx";

    m_param["TMP_DIR"] =        "C:\\Program Files\\qtopo\\tmp";
    m_param["QTCALIB_ICON"] =   "C:\\Program Files\\qtopo\\icons\\logo6c-64.xpm";
    m_param["QTDATA_ICON"] =    "C:\\Program Files\\qtopo\\icons\\logo6-64.xpm";
    m_param["TEMP_DATA"] =       "C:\\Program Files\\qtopo\\tmp\\data.txt";
    m_param["TEMP_DATA_GUESS"] = "C:\\Program Files\\qtopo\\tmp\\data-guess.txt";
    m_param["TEMP_COEFF"] =      "C:\\Program Files\\qtopo\\tmp\\coeff.txt";
    m_param["DEFAULT_COEFF"] =   "C:\\Program Files\\qtopo\\tmp\\coeff.txt";
    m_param["COVER_XPM"] =       "C:\\Program Files\\qtopo\\tmp\\cover.xpm";
    m_param["TEMP_TLXDATA"] =    "C:\\Program Files\\qtopo\\tmp\\data.tlx";
    m_param["DEFAULT_DATA"] =    "C:\\Program Files\\qtopo\\tmp\\data.txt";
  #elif defined ARM /* ARM Linux */   
    m_param["GEOMETRY"] = "240x320";
    m_param["DEVICE" ] = "/dev/rfcomm0";

    m_param["TMP_DIR"] =      "/tmp";
    m_param["DEFAULT_DATA"] = "/opt/QtPalmtop/data.tlx";
    m_param["QTCALIB_ICON"] = "/opt/QtPalmtop/pics/qtcalib-64.xpm";
    m_param["QTDATA_ICON"] =  "/opt/QtPalmtop/pics/qtdata-64.xpm";
    m_param["TEMP_DATA"] =    "/tmp/data.txt";
    m_param["TEMP_DATA_GUESS"] = "/tmp/data-guess.txt";
    m_param["TEMP_COEFF"] =      "/tmp/coeff.txt";
    m_param["DEFAULT_COEFF"] =   "/tmp/coeff.txt";
    m_param["COVER_XPM"] =       "/tmp/cover.xpm";
    m_param["TEMP_TLXDATA"] =    "/tmp/data.tlx";
    m_param["DEFAULT_DATA"] =    "/tmp/data.txt";
  #else /* X86 Linux */
    m_param["DEVICE" ] = "/dev/rfcomm0";

    m_param["TMP_DIR"] =      "/tmp";
    m_param["DEFAULT_DATA"] = "/home/projects/Disto/DATA/data.tlx";
    m_param["QTCALIB_ICON"] = "/usr/local/share/icons/logo6c-64.xpm";
    m_param["QTDATA_ICON"] =  "/usr/local/share/icons/logo6-64.xpm";
    m_param["TEMP_DATA"] =       "/tmp/data.txt";
    m_param["TEMP_DATA_GUESS"] = "/tmp/data-guess.txt";
    m_param["TEMP_COEFF"] =      "/tmp/coeff.txt";
    m_param["DEFAULT_COEFF"] =   "/tmp/coeff.txt";
    m_param["COVER_XPM"] =       "/tmp/cover.xpm";
    m_param["TEMP_TLXDATA"] =    "/tmp/data.tlx";
    m_param["DEFAULT_DATA"] =    "/tmp/data.txt";
  #endif
}

bool
Config::Load( const char * filename )
{
  if ( ! filename ) return false;
  FILE * fp = fopen( filename, "r" );
  if ( ! fp ) return false;;

  char line[256];
  while ( fgets(line, 255, fp ) ) {
    char * ch = line;
    // skip heading white spaces
    while ( *ch && isspace(*ch) ) ++ch;
    if ( *ch == 0 || *ch == '#' ) continue;
    // ch points to the KEY
    char * ch1 = ch;  
    while ( *ch1 && ! isspace(*ch1) ) ++ch1;
    if ( *ch1 == 0 ) continue;
    *ch1 = 0; // KEY terminator
    ++ch1;
    while ( *ch1 && isspace(*ch1) ) ++ch1;
    if ( *ch1 == 0 ) continue; // no VALUE
    char * ch2 = ch1;
    if ( *ch2 == '"' ) { // quoted string
      ++ch1;
      ++ch2;
      while ( *ch2 && (*ch2 != '"' || *ch2 != '\n') ) ++ch2;
      *ch2 = 0;
    } else {
      while ( *ch2 && ! isspace(*ch2) ) ++ch2;
      *ch2 = 0;
    }
    m_param[ch] = ch1;
  }

  fclose( fp );
  return true;
}
