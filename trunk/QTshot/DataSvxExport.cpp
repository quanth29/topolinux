/** @file DataSvxExport.cpp
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Survex format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <sstream>

#include <QFileInfo>
#include <QFile>
  
#include "shorthands.h"
#include "DataSvxExport.h"

#include "Flags.h"

/** Survex export
 * The following format is used to export the centerline data in survex
 *
 *    *begin survey_name
 *      *units tape feet|metres
 *      *units compass clino grad|degrees
 *      *calibrate declination ...
 *      *date yyyy.mm.dd
 *      *data normal from to tape compass clino
 *      ...
 *      *flags surface|not surface
 *      *flags duplicate|not duplicate
 *      *flags splay|not splay
 *      ...
 *      ; shot_comment
 *      ...
 *      (optional survey commands)
 *    *end
 */
bool
saveAsSurvex( DataList & data,
              const CenterlineInfo & c_info,
              const Units & units )
{
  const SurveyInfo & info = c_info.surveyInfo;
  int extra_cnt = 0;

  QFile file( info.exportName );
  if ( ! file.open( QIODevice::WriteOnly ) ) {
    DBG_CHECK("Failed to open file \"%s\"\n", info.exportName.TO_CHAR() );
    return false;
  }

  // int day, month, year;
  // GetDate( &day, &month, &year);
  
  std::ostringstream oss;
  oss.setf( std::ios::fixed );
  oss.precision(2);

  oss << "*begin " << info.name.TO_CHAR() << "\n\n";
  if ( ! info.title.isEmpty() ) {
    oss << "  *title \"" << info.title.TO_CHAR() << "\"\n";
  }
  // TODO survey title ect.
  double ls = units.length_factor;
  double as = units.angle_factor;
  if ( units.length_units == LENGTH_FEET ) {
    oss << "    *units tape feet\n";
  } else {
    oss << "    *units tape metres\n";
  }
  if ( units.angle_units == ANGLE_GRAD ) {
    oss << "    *units compass clino grads\n";
  } else {
    oss << "    *units compass clino degrees\n";
  }

  if ( info.declination != DECLINATION_UNDEF ) {
    oss << "    *calibrate declination "  <<info.declination << "\n\n";
  }

  oss << "    *date ";
  oss.fill( '0' );
  oss.width( 4 ); oss << c_info.year  << ".";
  oss.width( 2 ); oss << c_info.month << ".";
  oss.width( 2 ); oss <<  c_info.day  << "\n";
  oss.fill( ' ');
  if ( info.therionCenterlineCommand.size() > 0 ) {
    oss << info.therionCenterlineCommand << "\n";
  }
  oss << "    *data normal from to tape compass clino\n";
  DBlock * b;
  bool in_splay = false;
  bool in_surface = false;
  bool in_duplicate = false;
  for ( b = data.listHead(); b; b=b->next() ) {
    if ( ! b->hasFromStation() && ! b->hasToStation() ) {
      // skip data with neither From nor To 
      continue;
    }
    if ( b->Flag() == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        oss << "    *flags surface\n";
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        oss << "    *flags not surface\n";
        in_surface = false;
      }
    }
    if ( b->Flag() == FLAG_DUPLICATE || ! b->hasToStation() ) { // duplicate
      if ( ! in_surface ) {
        oss << "    *flags duplicate\n";
        in_duplicate = true;
      }
    } else {
      if ( in_duplicate ) {
        oss << "    *flags not duplicate\n";
        in_duplicate = false;
      }
    }


    oss.fill( '0' );
    if ( ! b->hasFromStation() ) {
      if ( ! in_splay ) {
        oss << "    *flags splay\n";
        in_splay = true;
      }
      ++extra_cnt;
      if ( b->hasToStation() ) {
        oss << "    " << b->toStation() << "_";
        oss.width( 4 ); oss << extra_cnt << " ";
        oss << b->toStation();
      } else {
        oss << "    f_";
        oss.width( 4 ); oss << extra_cnt << " t_";
        oss.width( 4 ); oss << extra_cnt;
      }
    } else {
      if ( ! b->hasToStation() ) {
        if ( ! in_splay ) {
          oss << "    *flags splay\n";
          in_splay = true;
        }
        ++extra_cnt;
        oss << "    " << b->fromStation() << " " << b->fromStation() << "_";
        oss.width( 4 ); oss << extra_cnt;
      } else {
        if ( in_splay ) {
          oss << "    *flags not splay\n";
          in_splay = false;
        }
        oss << "    " << b->fromStation() << " " <<  b->toStation();
      }
    }
    oss.fill( ' ' );

    oss << " " << ls*b->Tape()
        << " " << as*b->Compass()
        << " " << as*b->Clino() << "\n";
    if ( b->hasComment() ) {
      oss << "    ; " << b->getComment() << "\n";
    }
  }

  if ( info.therionSurveyCommand.size() > 0 ) {
    oss << info.therionSurveyCommand << "\n";
  }
  oss << "*end\n";

  file.write( oss.str().c_str() );
  file.close();
  return true;
}
