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
#include <sstream>

#include <QFileInfo>
#include <QFile>

#include "shorthands.h"
#include "DataDatExport.h"
#include "Factors.h"

#include "Flags.h"
#include "Extend.h"
  
#define M2FT 3.28083 /* meters to feet */
const char char0C = 0x0c;

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
  for ( DBlock * b1 = data.listHead(); b1; b1=b1->next() ) {
    if ( b1->hasFromStation() && b1->hasToStation() ) { // centerline shots
      continue;
    }
    // use only splay shots at "From"
    if ( b1->hasFromStation() && strcmp(b1->fromStation(), b->fromStation())==0 ) {
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
 *    DECLINATION: declination  FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00
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
  const SurveyInfo & info = c_info.surveyInfo;
  QFileInfo fileinfo( info.exportName );

  QFile file( info.exportName );
  if ( ! file.open( QIODevice::WriteOnly ) ) {
    DBG_CHECK("Failed to open file \"%s\"\n", info.exportName.TO_CHAR() );
    return false;
  }

// TODO use RB-tree
  std::map<std::string, int> station_names;
  int survey_nr = 0;
  std::string prefix = info.compassPrefix.TO_CHAR();
  int prefix_len = prefix.length();
  // int day, month, year;
  // GetDate( &day, &month, &year);

  bool in_file = false;
  std::string survey_name = info.title.isEmpty()? info.name.trimmed().TO_CHAR()
                             : info.title.trimmed().TO_CHAR();

  std::ostringstream oss;
  oss.setf( std::ios::fixed );
  oss.precision(2);

  if ( info.compassSingleSurvey ) {
    oss << info.name.TO_CHAR() << "\r\n";
    oss << "SURVEY NAME: " << survey_name <<  "\r\n";
    oss << "SURVEY DATE: " << c_info.month
        << " " << c_info.day
        << " " << c_info.year << "\r\n";
    oss << "SURVEY TEAM:\r\n";
    oss << ( info.team.isEmpty()? "..." : info.team.TO_CHAR() ) << "\r\n";
    oss << "DECLINATION: "
        << ( (info.declination != DECLINATION_UNDEF)? info.declination : 0.0 )
        << "  FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00\r\n";
    oss << "\r\n";
    oss << "FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n";
    oss << "\r\n";
  }

  int extra_cnt = 0;
  bool in_splay = false;
  bool in_surface = false;
  bool in_duplicate = false;
  char flags[16];
  DBlock * b;
  max_len = 0;   // maximum length of a station name
  for ( b = data.listHead(); b; b=b->next() ) {
    if ( ! b->hasFromStation() && ! b->hasToStation() ) { // skip data with neither From nor To 
      continue;
    }
// TODO allow to include splay shots if requested
// just must find how ...
    if ( ! b->hasFromStation() || ! b->hasToStation() ) { // skip splay shots
      continue;
    }
    if ( ! info.compassSingleSurvey ) {
      std::map<std::string, int>::iterator itf = station_names.find(b->fromStation());
      std::map<std::string, int>::iterator itt = station_names.find(b->toStation());
      if ( itf == station_names.end() && itt == station_names.end() ) {
        if ( in_file ) {
          
          oss << char0C << "\r\n";
        } else {
          in_file = true;
        }
      
        station_names.clear();
        if (survey_nr == 0 ) {
          oss << info.name.TO_CHAR() << "\r\n";
          oss << "SURVEY NAME: " << survey_name << "\r\n";
        } else {
          char survey_char = 'A'+survey_nr;
          oss << info.name.TO_CHAR() << "-(" << survey_char << ")\r\n";
          oss << "SURVEY NAME: " << survey_name << " (" << survey_char << ")\r\n";
        }
        ++ survey_nr;
        oss <<  "SURVEY DATE: " << c_info.month 
            << " " << c_info.day
            << " " << c_info.year << "\r\n";
        oss << "SURVEY TEAM:\r\n";
        oss << ( info.team.isEmpty()? "..." : info.team.TO_CHAR() ) << "\r\n";
        oss << "DECLINATION: 0.00  FORMAT: DMMDLRUDLADN  CORRECTIONS:  0.00 0.00 0.00\r\n";
        oss << "\r\n";
        oss << "FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n";
        oss << "\r\n";

        extra_cnt = 0;
        in_splay = false;
        in_surface = false;
        in_duplicate = false;
      }
      if ( b->hasFromStation() && itf == station_names.end() ) {
        station_names[ b->fromStation() ] = 1;
      }
      if ( b->hasToStation() && itt == station_names.end() ) {
        station_names[ b->toStation() ] = 1;
      }
    }

    memset(flags, 0, 16);
    in_surface = ( b->Flag() == FLAG_SURFACE );
    in_duplicate = ( b->Flag() == FLAG_DUPLICATE );
    in_splay = ( ! b->hasToStation() || ! b->hasFromStation() );
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

    oss.fill('0');
    if ( ! b->hasFromStation() ) {
      ++extra_cnt;
      if ( ! b->hasToStation() ) {
        int len = prefix_len + 1 + 4;
        if ( len > max_len ) max_len = len;
        oss << prefix << "F";
        oss.width(4); oss << extra_cnt << " " << prefix << "T";
        oss.width(4); oss << extra_cnt << " ";
      } else {
        int len = prefix_len + 4;
        if ( b->hasToStation() ) ++len;
        if ( len > max_len ) max_len = len;
        oss << prefix << b->toStation();
        oss.width(4); oss << extra_cnt << " ";
        oss << prefix << b->toStation();
      }
    } else {
      if ( ! b->hasToStation() ) {
        ++ extra_cnt;
        int len = prefix_len + 4;
        if ( b->hasFromStation() ) ++len;
        if ( len > max_len ) max_len = len;
        oss << prefix << b->fromStation() << " " << prefix << b->fromStation();
        oss.width(4); oss << extra_cnt << " ";
      } else {
        int len = prefix_len;
        if ( b->hasFromStation() ) ++len;
        if ( len > max_len ) max_len = len;
        len = prefix_len;
        if ( b->hasToStation() ) ++len;
        if ( len > max_len ) max_len = len;
        oss << prefix <<  b->fromStation() << " " << prefix << b->toStation() << " ";
      }
    }
  
    // Compass writes distances in feet
    if ( b->hasFromStation() && b->hasToStation() ) {
      double l= 0.0; 
      double r= 0.0;
      double u= 0.0;
      double d= 0.0;
  
      #ifdef HAS_LRUD
        LRUD * lf = b->getLRUD( 0 );
        LRUD * lt = b->getLRUD( 1 );
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
  
      oss << b->Tape() * M2FT << " "
          << b->Compass() << " "
          << b->Clino() << " "
          << l * M2FT << " "
          << u * M2FT << " "
          << d * M2FT << " "
          << r * M2FT << " ";
    } else {
      oss << b->Tape() * M2FT << " "
          << b->Compass() << " "
          << b->Clino() << " -1.0 -1.0 -1.0 -1.0 ";
    }
    if ( fidx > 4 ) {
      oss << flags;
    }
    if ( b->hasComment() ) {
      oss << b->getComment();
    } 
    oss << "\r\n";
  }
  if ( in_file || info.compassSingleSurvey ) {
    oss << char0C << "\r\n";
  }
  
  file.write( oss.str().c_str() );
  file.close();
  return true;
}
