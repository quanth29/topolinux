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
  FILE * fp = fopen( c_info.exportName.latin1(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", c_info.exportName.latin1() );
    return false;
  }
  const SurveyInfo & info = c_info.surveyInfo;
  // int day, month, year;
  // GetDate( &day, &month, &year);

  fprintf(fp, "survey %s", info.name.latin1() );
  if ( ! info.title.isEmpty() ) {
    fprintf(fp, " -title \"%s\"", info.title.latin1() );
  }
  fprintf(fp, "\n\n");
  fprintf(fp, "  declination %.2f degrees \n\n", info.declination ); 

  fprintf(fp, "  centerline\n");
  fprintf(fp, "    date %4d.%02d.%02d\n", c_info.year, c_info.month, c_info.day );
  double ls = units.length_factor;
  double as = units.angle_factor;
  if ( units.length_units == LENGTH_FEET ) {
    fprintf(fp, "    units length left right up down feet\n");
  } else {
    fprintf(fp, "    units length left right up down metres\n");
  }
  if ( units.angle_units == ANGLE_GRAD ) {
    fprintf(fp, "    units compass clino grads\n");
  } else {
    fprintf(fp, "    units compass clino degrees\n");
  }

  if ( info.centerlineCommand.size() > 0 ) {
    fprintf(fp, "%s\n", info.centerlineCommand.c_str() );
  }
  fprintf(fp, "    data normal from to length compass clino\n");

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
  for ( b = data.Head(); b; b=b->Next() ) {
    if ( ! b->hasFrom() || ! b->hasTo() ) {
      // skip data without either From or To 
      continue;
    }

    #ifdef HAS_LRUD
      LRUD * lf = b->LRUD_From();
      LRUD * lt = b->LRUD_To();
      if ( lf != NULL || lt != NULL ) {
        if ( ! with_lrud ) {
          with_lrud = true;
          fprintf(fp, "    data normal from to length compass clino left right up down\n");
        }
      } else {
        if ( with_lrud ) {
          with_lrud = false;
          fprintf(fp, "    data normal from to length compass clino\n");
        }
      }
    #else
      fprintf(fp, "    data normal from to length compass clino\n");
    #endif
    
    if ( b->Flag() == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        fprintf(fp, "    flags surface\n");
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        fprintf(fp, "    flags not surface\n");
        in_surface = false;
      }
    }
    if ( b->Flag() == FLAG_DUPLICATE ) { // duplicate
      if ( ! in_duplicate ) {
        fprintf(fp, "    flags duplicate\n");
        in_duplicate = true;
      }
    } else {
      if ( in_duplicate ) {
        fprintf(fp, "    flags not duplicate\n");
        in_duplicate = false;
      }
    }
/*
    if ( b->hasFrom() && b->hasTo() ) {
      if ( in_splay ) {
        fprintf(fp, "    flags not splay\n");
        in_splay = false;
      }
    } else {
      if ( ! in_splay ) {
        fprintf(fp, "    flags splay\n");
        in_splay = true;
      }
    }
*/

    if ( b->Extend() == EXTEND_NONE ) {
      b->setExtended( extend );
    } else if ( b->Extend() != extend ) {
      b->setExtended( b->Extend() );
      if ( b->Extend() == EXTEND_IGNORE ) { 
        fprintf(fp, "    extend ignore\n");
      } else if ( b->Extend() == EXTEND_VERT ) {
        fprintf(fp, "    extend vertical\n");
      } else {
        extend = b->Extend();
        if ( b->Extend() == EXTEND_LEFT ) {
          fprintf(fp, "    extend left \n");
        } else if ( b->Extend() == EXTEND_RIGHT ) { 
          fprintf(fp, "    extend right \n");
        }
      }
    }

    fprintf(fp, "    %s %s", b->From(), b->To() );
    fprintf(fp, " %.2f %.2f %.2f ", ls*b->Tape(), as*b->Compass(), as*b->Clino() );
    
    #ifdef HAS_LRUD
      if ( lf != NULL ) {
        if ( lt != NULL ) {
          fprintf(fp, "[%.2f %.2f] [%.2f %.2f] [%.2f %.2f] [%.2f %.2f]",
            lf->left, lt->left, lf->right, lt->right, lf->up, lt->up, lf->down, lt->down );
        } else {
          fprintf(fp, "%.2f %.2f %.2f %.2f", lf->left, lf->right, lf->up, lf->down );
        }
      } else if ( lt != NULL ) {
        fprintf(fp, "%.2f %.2f %.2f %.2f", lt->left, lt->right, lt->up, lt->down );
      }
    #endif

    fprintf(fp, "\n");
    if ( b->Extend() == EXTEND_IGNORE || b->Extend() == EXTEND_VERT ) {
      if ( extend == EXTEND_LEFT ) {
        fprintf(fp, "    extend left\n");
      } else if ( extend == EXTEND_RIGHT ) {
        fprintf(fp, "    extend right\n");
      }
    }
    if ( b->hasComment() ) {
      fprintf(fp, "    # %s\n", b->Comment() );
    }
  }
  // second pass: splay shots
  // splay shots do not have LRUD
  fprintf(fp, "    # splay shots\n");
  fprintf(fp, "    flags splay\n");
  for ( b = data.Head(); b; b=b->Next() ) {
    if ( b->hasFrom() && b->hasTo() ) { // centerline shots
      continue;
    }
    if ( ! b->hasFrom() && ! b->hasTo() ) { // skip data with neither From nor To 
      continue;
    }
    /* ignore SURFACE FLAG for splay shots
    if ( b->flag == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        fprintf(fp, "    flags surface\n");
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        fprintf(fp, "    flags not surface\n");
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
      fprintf(fp, "    extend ignore\n");
    } else if ( b->Extended() == EXTEND_VERT ) {
      fprintf(fp, "    extend vertical\n");
    } else if ( b->Extended() == EXTEND_LEFT ) {
      fprintf(fp, "    extend left \n");
    } else if ( b->Extended() == EXTEND_RIGHT ) { 
      fprintf(fp, "    extend right \n");
    }
    ++extra_cnt;
    if ( ! b->hasFrom() ) {
      assert( b->hasTo() );
      // FIXME Therion uses '-' for wall and '.' for inside features
      fprintf(fp, "    - %s", b->To() );
    } else { // b->hasFrom() 
      assert( ! b->hasTo() );
      fprintf(fp, "    %s -", b->From() );
    }

    fprintf(fp, " %.2f %.2f %.2f \n", 
      ls*b->Tape(), as*b->Compass(), as*b->Clino() );
    if ( b->Extend() == EXTEND_IGNORE || b->Extend() == EXTEND_VERT ) {
      if ( extend == EXTEND_LEFT ) {
        fprintf(fp, "    extend left\n");
      } else if ( extend == EXTEND_RIGHT ) {
        fprintf(fp, "    extend right\n");
      }
    }
    if ( b->hasComment() ) {
      fprintf(fp, "    # %s\n", b->Comment() );
    }
  }

  fprintf(fp, "  endcenterline\n");

  if ( info.surveyCommand.size() > 0 ) {
    fprintf(fp, "%s\n", info.surveyCommand.c_str() );
  }
  fprintf(fp, "endsurvey\n");

  fclose( fp );
  return true;
}
