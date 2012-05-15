/** @file DataThExport.cpp
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Therion format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <assert.h>
#include <sstream>

#include <QFileInfo>
#include <QFile>
  
#include "shorthands.h"
#include "DataThExport.h"
#include "Extend.h"
#include "Flags.h"

/** Centerline data are exported to Therion format as follow
 *    survey survey_name -title "survey_title"
 *      declination ... degrees 
 *      centerline
 *        date yyyy.mm.dd
 *        units length left right up down feet|metres
 *        units compass clino grad|degrees
 *        (optional centerline commands)
 *        data normal from to length compass clino
 *        ...
 *        # shot comment
 *        data normal from to length compass clino left right up down
 *        ...
 *        export right|left|vertical|ignore
 *        flags surface|duplicate|splay (not ...)
 *      endcenterline
 *      (optional survey commands)
 *    endsurvey
 *
 * Note splay shots are listed all together after the centerline shots.
 */
bool
saveAsTherion( DataList & data,
               const CenterlineInfo & c_info,
               const Units & units )
{
  const SurveyInfo & info = c_info.surveyInfo;
  QFileInfo fileinfo( info.exportName );

  QFile file( info.exportName );
  if ( ! file.open( QIODevice::WriteOnly ) ) {
    DBG_CHECK("Failed to open file \"%s\"\n", info.exportName.TO_CHAR() );
    return false;
  }
  if ( info.therionThconfig ) {
    QString thconfigname = fileinfo.path() + "/thconfig"; 
    // printf("saving thconfig <<%s>>\n", thconfigname.TO_CHAR() );
    QString thname = fileinfo.fileName();
    QFile thconfig( thconfigname );
    if ( thconfig.open( QIODevice::WriteOnly ) ) {
      thconfig.write( "# Therion configuration file\n");
      thconfig.write( "# Edit as needed\n");
      thconfig.write( "\nsource ");
      thconfig.write( thname.TO_CHAR() );
      thconfig.write( "\n\nexport map -proj plan -o cave-p.pdf\n" );
      thconfig.write( "\nexport map -proj extended -o cave-s.pdf\n" );
      thconfig.close();
    } else {
      DBG_CHECK("Failed to open thconfig file \"%s\"\n", thconfigname.TO_CHAR() );
    }

  }
  int day, month, year;
  GetDate( &day, &month, &year);
  char today[12];
  sprintf(today, "%04d-%02d-%02d", year, month, day );

  std::ostringstream oss;
  oss.setf( std::ios::fixed );
  oss.precision(2);

  oss << "survey " << info.name.TO_CHAR();
  if ( ! info.title.isEmpty() ) {
    oss << "\\\n   -title \"" << info.title.TO_CHAR() << "\"";
  }
  if ( info.declination != DECLINATION_UNDEF ) {
    oss << "\\\n   -declination [" << info.declination << " degrees]";
  }
  oss << "\n\n";

  oss << "  centerline\n";
  if ( ! c_info.author.empty() ) {
    oss << "    author " << today << " " << c_info.author << "\n";
  }
  if ( ! c_info.copyright.empty() ) {
    oss << "    copyright " << today << " " << c_info.copyright << "\n";
  }

  oss << "    date ";
  oss.fill( '0' );
  oss.width( 4 ); oss << c_info.year  << ".";
  oss.width( 2 ); oss << c_info.month << ".";
  oss.width( 2 ); oss <<  c_info.day  << "\n";
  oss.fill( ' ');
  // file.write( "%4d.%02d.%02d\n", c_info.year, c_info.month, c_info.day );
  double ls = units.length_factor;
  double as = units.angle_factor;
  if ( units.length_units == LENGTH_FEET ) {
    oss << "    units length left right up down feet\n";
  } else {
    oss << "    units length left right up down metres\n";
  }
  if ( units.angle_units == ANGLE_GRAD ) {
    oss << "    units compass clino grads\n";
  } else {
    oss << "    units compass clino degrees\n";
  }

  if ( info.therionCenterlineCommand.size() > 0 ) {
    oss << info.therionCenterlineCommand.c_str() << "\n";
  }
  oss << "    data normal from to length compass clino\n";

  DBlock * b;
  int extend = EXTEND_RIGHT;
  bool in_surface = false;
  bool in_duplicate = false;
  // FIXME use this when writing splay shots interleaved with centerline shots
  // bool in_splay  = false;
  #ifdef HAS_LRUD
    bool with_lrud = false;
  #endif
  int extra_cnt = 0;

  // first pass centerline shots
  for ( b = data.listHead(); b; b=b->next() ) {
    if ( ! b->hasFromStation() || ! b->hasToStation() ) {
      // skip data without either From or To 
      continue;
    }

    #ifdef HAS_LRUD
      LRUD * lf = b->GetLRUD( 0 );
      LRUD * lt = b->GetLRUD( 1 );
      if ( lf != NULL || lt != NULL ) {
        if ( ! with_lrud ) {
          with_lrud = true;
          oss << "    data normal from to length compass clino left right up down\n";
        }
      } else {
        if ( with_lrud ) {
          with_lrud = false;
          oss << "    data normal from to length compass clino\n";
        }
      }
    #else
      // FIXED 20100715 no need to write this
      // oss << "    data normal from to length compass clino\n";
    #endif
    
    if ( b->Flag() == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        oss << "    flags surface\n";
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        oss << "    flags not surface\n";
        in_surface = false;
      }
    }
    if ( b->Flag() == FLAG_DUPLICATE ) { // duplicate
      if ( ! in_duplicate ) {
        oss << "    flags duplicate\n";
        in_duplicate = true;
      }
    } else {
      if ( in_duplicate ) {
        oss << "    flags not duplicate\n";
        in_duplicate = false;
      }
    }
/*
    if ( b->hasFromStation() && b->hasToStation() ) {
      if ( in_splay ) {
        oss << "    flags not splay\n";
        in_splay = false;
      }
    } else {
      if ( ! in_splay ) {
        oss << "    flags splay\n";
        in_splay = true;
      }
    }
*/

    if ( b->Extend() == EXTEND_NONE ) {
      b->setExtended( extend );
    } else if ( b->Extend() != extend ) {
      b->setExtended( b->Extend() );
      if ( b->Extend() == EXTEND_IGNORE ) { 
        oss << "    extend ignore\n";
      } else if ( b->Extend() == EXTEND_VERT ) {
        oss << "    extend vertical\n";
      } else {
        extend = b->Extend();
        if ( b->Extend() == EXTEND_LEFT ) {
          oss << "    extend left\n";
        } else if ( b->Extend() == EXTEND_RIGHT ) { 
          oss << "    extend right\n";
        }
      }
    }

    oss << "    " << b->fromStation() << " " << b->toStation()
        << " " << ls*b->Tape() 
        << " " << as*b->Compass()
        << " " << as*b->Clino() ;
    
    #ifdef HAS_LRUD
      if ( lf != NULL ) {
        if ( lt != NULL ) {
          oss << " [" <<  lf->left  << " " << lt->left  << "]"
              << " [" <<  lf->right << " " << lt->right << "]"
              << " [" <<  lf->up    << " " << lt->up    << "]"
              << " [" <<  lf->down  << " " << lt->down  << "]";
        } else {
          oss << " " <<  lf->left 
              << " " <<  lf->right
              << " " <<  lf->up
              << " " <<  lf->down;
        }
      } else if ( lt != NULL ) {
        oss << " " <<  lt->left 
            << " " <<  lt->right
            << " " <<  lt->up
            << " " <<  lt->down;
      }
    #endif
    oss << "\n";

    if ( b->Extend() == EXTEND_IGNORE || b->Extend() == EXTEND_VERT ) {
      if ( extend == EXTEND_LEFT ) {
        oss << "    extend left\n";
      } else if ( extend == EXTEND_RIGHT ) {
        oss << "    extend right\n";
      }
    }
    if ( b->hasComment() ) {
      oss << "    # " << b->getComment() << "\n";
    }
  }
  // second pass: splay shots
  // splay shots do not have LRUD
  oss << "    # splay shots\n";
  oss << "    flags splay\n";
  for ( b = data.listHead(); b; b=b->next() ) {
    if ( b->hasFromStation() && b->hasToStation() ) { // centerline shots
      continue;
    }
    if ( ! b->hasFromStation() && ! b->hasToStation() ) { // skip data with neither From nor To 
      continue;
    }
    /* ignore SURFACE FLAG for splay shots
    if ( b->flag == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        oss << "    flags surface\n";
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        oss << "    flags not surface\n";
        in_surface = false;
      }
    }
    */
    // FIXME FIXME FIXME
    // splay shots need the extended flag set
    // this is done when the extended section plot is computed
    // this means that cannot save as therion before computing the extended plot
    // ...
    // evaluation of extends should be done by DataList ...
    // but it cannot be done before num has computed the 3D position of the stations
    // and the segments in the horizontal plane
    //
    // evalSplayExtended( b );

    if ( b->Extended() == EXTEND_IGNORE ) { 
      oss << "    extend ignore\n";
    } else if ( b->Extended() == EXTEND_VERT ) {
      oss << "    extend vertical\n";
    } else if ( b->Extended() == EXTEND_LEFT ) {
      oss << "    extend left \n";
    } else if ( b->Extended() == EXTEND_RIGHT ) { 
      oss << "    extend right \n";
    }
    ++extra_cnt;
    if ( ! b->hasFromStation() ) {
      assert( b->hasToStation() );
      // FIXME Therion uses '-' for wall and '.' for inside features
      oss << "    - " << b->toStation();
    } else { // b->hasFromStation() 
      assert( ! b->hasToStation() );
      oss << "    " << b->fromStation() << " -";
    }

    oss << " " << ls*b->Tape() 
        << " " << as*b->Compass()
        << " " << as*b->Clino() << "\n";
    if ( b->Extend() == EXTEND_IGNORE || b->Extend() == EXTEND_VERT ) {
      if ( extend == EXTEND_LEFT ) {
        file.write( "    extend left\n");
      } else if ( extend == EXTEND_RIGHT ) {
        file.write( "    extend right\n");
      }
    }
    if ( b->hasComment() ) {
      oss << "    # " << b->getComment() << "\n";
    }
  }

  oss << "  endcenterline\n";

  if ( info.therionSurveyCommand.size() > 0 ) {
    oss << info.therionSurveyCommand <<  "\n";
  }
  oss << "endsurvey\n";

  file.write( oss.str().c_str() );
  file.close();
  return true;
}
