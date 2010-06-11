/** @file DataDatExport.cpp
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Compass format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <string.h>

#include "DataDatExport.h"

#include "Flags.h"
#include "Extend.h"
  
#define M2FT 3.28083

/** compute LRUD from splay shots
 * @param b shot block
 * @param data data list
 * @param l left
 * @param r right
 * @param u up
 * @param d down
 */
void
computeLRUD( DBlock * b, DataList & data, 
             double & l, double & r, double & u, double & d )
{
  double n0 = cos( b->Compass() * M_PI/180.0 );
  double e0 = sin( b->Compass() * M_PI/180.0 );
  for ( DBlock * b1 = data.Head(); b1; b1=b1->Next() ) {
    if ( b1->hasFrom() && b1->hasTo() ) { // centerline shots
      continue;
    }
    // use only splay shots at "From"
    if ( b1->hasFrom() && strcmp(b1->From(), b->From())==0 ) {
      double z1 = b1->Tape() * sin( b1->Clino() * M_PI/180.0 );
      double h1 = b1->Tape() * cos( b1->Clino() * M_PI/180.0 );
      double n1 = h1 * cos( b1->Compass() * M_PI/180.0 );
      double e1 = h1 * sin( b1->Compass() * M_PI/180.0 );
      if ( z1 > 0.0 ) { if ( z1 > u ) u = z1; }
      else            { if ( -z1 > d ) d = -z1; }
      double rl = e1 * n0 - n1 * e0;
      if ( rl > 0.0 ) { if ( rl > r ) r = rl; }
      else            { if ( -rl > l ) l = -rl; }
    }
  } 
}

/** Centerline data are exported in Compass format as follows
 *    SURVEY NAME: survey_name
 *    SURVEY DATE: mm dd yyyy
 *    SURVEY TEAM:
 *    team_line
 *    DECLINATION: declination  FORMAT: DMMDLRUDLADN  CORRECTIONS:  0.00 0.00 0.00
 *    FROM TO LENGTH BEARING INC FLAGS COMMENTS
 *    ...
 *    0x0c
 *
 * Notes.
 * Names must limited to 14 characters: this include the "prefix" and the station FROM and TO names.
 * Distances are in feet.
 * The flags string is composed as "#|...#", Flags characters: L (duplicate) P (no plot) X (surface).
 * Splay shots are not exported, they may be used to find transversal dimensions, if LRUD are not provided  
 * Multisurvey file is possible.
 */
bool
saveAsCompass( DataList & data,
               const CenterlineInfo & c_info,
               const Units & /* units */,
               int & max_len )
{
  FILE * fp = fopen( c_info.exportName.latin1(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", c_info.exportName.latin1() );
    return false;
  }
// TODO use RB-tree
  std::map<std::string, int> station_names;
  int survey_nr = 0;
  const SurveyInfo & info = c_info.surveyInfo;
  const char * prefix = info.prefix.latin1();
  int prefix_len = strlen( prefix );
  // int day, month, year;
  // GetDate( &day, &month, &year);

  bool in_file = false;

  if ( info.single_survey ) {
    fprintf(fp, "%s\r\n", info.name.latin1() );
    fprintf(fp, "SURVEY NAME: %s\r\n",
      info.title.isEmpty()? info.name.latin1() : info.title.latin1() );
    fprintf(fp, "SURVEY DATE: %d %d %d\r\n", c_info.month, c_info.day, c_info.year);
    fprintf(fp, "SURVEY TEAM:\r\n");
    fprintf(fp, "%s\r\n", info.team.isEmpty()? "..." : info.team.latin1() );
    fprintf(fp, "DECLINATION: %.2f  FORMAT: DMMDLRUDLADN  CORRECTIONS:  0.00 0.00 0.00\r\n", info.declination );
    fprintf(fp, "\r\n");
    fprintf(fp, "FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n");
    fprintf(fp, "\r\n");
  }

  int extra_cnt = 0;
  bool in_splay = false;
  bool in_surface = false;
  bool in_duplicate = false;
  char flags[16];
  DBlock * b;
  max_len = 0;   // maximum length of a station name
  for ( b = data.Head(); b; b=b->Next() ) {
    if ( ! b->hasFrom() && ! b->hasTo() ) { // skip data with neither From nor To 
      continue;
    }
// TODO allow to include splay shots if requested
// just must find how ...
    if ( ! b->hasFrom() || ! b->hasTo() ) { // skip splay shots
      continue;
    }
    if ( ! info.single_survey ) {
      std::map<std::string, int>::iterator itf = station_names.find(b->From());
      std::map<std::string, int>::iterator itt = station_names.find(b->To());
      if ( itf == station_names.end() && itt == station_names.end() ) {
        if ( in_file ) {
          fprintf(fp, "%c\r\n", 0x0c);
        } else {
          in_file = true;
        }
      
        station_names.clear();
        if (survey_nr == 0 ) {
          fprintf(fp, "%s\r\n", info.name.latin1() );
          fprintf(fp, "SURVEY NAME: %s\r\n",
            info.title.isEmpty()? info.name.latin1() : info.title.latin1() );
        } else {
          fprintf(fp, "%s-(%c)\r\n", info.name.latin1(), 'A'+survey_nr );
          fprintf(fp, "SURVEY NAME: %s (%c)\r\n",
            info.title.isEmpty()? info.name.latin1() : info.title.latin1(), 'A'+survey_nr );
        }
        ++ survey_nr;
        fprintf(fp, "SURVEY DATE: %d %d %d\r\n", c_info.month, c_info.day, c_info.year);
        fprintf(fp, "SURVEY TEAM:\r\n");
        fprintf(fp, "%s\r\n", info.team.isEmpty()? "..." : info.team.latin1() );
        fprintf(fp, "DECLINATION: 0.00  FORMAT: DMMDLRUDLADN  CORRECTIONS:  0.00 0.00 0.00\r\n");
        fprintf(fp, "\r\n");
        fprintf(fp, "FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n");
        fprintf(fp, "\r\n");

        extra_cnt = 0;
        in_splay = false;
        in_surface = false;
        in_duplicate = false;
      }
      if ( b->hasFrom() && itf == station_names.end() ) {
        station_names[ b->From() ] = 1;
      }
      if ( b->hasTo() && itt == station_names.end() ) {
        station_names[ b->To() ] = 1;
      }
    }

    memset(flags, 0, 16);
    in_surface = ( b->Flag() == FLAG_SURFACE );
    in_duplicate = ( b->Flag() == FLAG_DUPLICATE );
    in_splay = ( ! b->hasTo() || ! b->hasFrom() );
    flags[0] = '#';
    flags[1] = '|';
    int fidx = 2;
    if ( in_surface || in_duplicate || in_splay ) {
      if ( in_surface ) { flags[fidx++] = 'X'; }    // exclude from processing
      if ( in_duplicate  ) { flags[fidx++] = 'L'; } // exclude from length
      if ( in_splay  ) { 
        flags[fidx++] = 'L'; // exclude from length
        flags[fidx++] = 'P'; // exclude from plot
      }
    }
    flags[fidx++] = '#';
    flags[fidx++] = 0;

    if ( ! b->hasFrom() ) {
      ++extra_cnt;
      if ( ! b->hasTo() ) {
        int len = prefix_len + 1 + 4;
        if ( len > max_len ) max_len = len;
        fprintf(fp, "%sF%04d %sT%04d ", prefix, extra_cnt, prefix, extra_cnt);
      } else {
        int len = prefix_len + 4;
        if ( b->hasTo() ) ++len;
        if ( len > max_len ) max_len = len;
        fprintf(fp, "%s%s%04d %s%s ", prefix, b->To(), extra_cnt, prefix, b->To() );
      }
    } else {
      if ( ! b->hasTo() ) {
        int len = prefix_len + 4;
        if ( b->hasFrom() ) ++len;
        if ( len > max_len ) max_len = len;
        fprintf(fp, "%s%s %s%s%04d ", prefix, b->From(), prefix, b->From(), ++extra_cnt);
      } else {
        int len = prefix_len;
        if ( b->hasFrom() ) ++len;
        if ( len > max_len ) max_len = len;
        len = prefix_len;
        if ( b->hasTo() ) ++len;
        if ( len > max_len ) max_len = len;
        fprintf(fp, "%s%s %s%s ", prefix, b->From(), prefix, b->To() );
      }
    }
  
    // Compass writes distances in feet
    if ( b->hasFrom() && b->hasTo() ) {
      double l= 0.0; 
      double r= 0.0;
      double u= 0.0;
      double d= 0.0;
  
      #ifdef HAS_LRUD
        LRUD * lf = b->LRUD_From();
        LRUD * lt = b->LRUD_To();
        if ( lf ) {
          if ( lt ) {
            l = (lf->left + lt->left)/2.0;
            r = (lf->right + lt->right)/2.0;
            u = (lf->up + lt->up)/2.0;
            d = (lf->down + lt->down)/2.0;
          } else {
            l = lf->left;
            r = lf->right;
            u = lf->up;
            d = lf->down;
          }
        } else if ( lt ) {
          l = lt->left;
          r = lt->right;
          u = lt->up;
          d = lt->down;
        } else {
          computeLRUD( b, data, l, r, u, d );
        }
      #else
        computeLRUD( b, data, l, r, u, d );
      #endif
  
      fprintf(fp, "%.2f %.2f %.2f %.2f %.2f %.2f %.2f ",
        b->Tape() * M2FT, b->Compass(), b->Clino(),
        l * M2FT, r * M2FT, u * M2FT, d * M2FT );
    } else {
      fprintf(fp, "%.2f %.2f %.2f -1.0 -1.0 -1.0 -1.0 ",
        b->Tape() * M2FT, b->Compass(), b->Clino() );
    }
    if ( fidx > 4 ) {
      fprintf(fp, "%s", flags );
    }
    if ( b->hasComment() ) {
      fprintf(fp, " %s", b->Comment() );
    } 
    fprintf(fp, "\r\n");
  }
  if ( in_file || info.single_survey ) {
    fprintf(fp, "%c\r\n", 0x0c);
  }
  fclose( fp );
  return true;
}
