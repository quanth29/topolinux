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
  int extra_cnt = 0;
  FILE * fp = fopen( c_info.exportName.latin1(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", c_info.exportName.latin1() );
    return false;
  }

  const SurveyInfo & info = c_info.surveyInfo;
  // int day, month, year;
  // GetDate( &day, &month, &year);

  fprintf(fp, "*begin %s\n\n", info.name.latin1() );
  if ( ! info.title.isEmpty() ) {
    fprintf(fp, " *title \"%s\"", info.title.latin1() );
  }
  // TODO survey title ect.
  double ls = units.length_factor;
  double as = units.angle_factor;
  if ( units.length_units == LENGTH_FEET ) {
    fprintf(fp, "    *units tape feet\n");
  } else {
    fprintf(fp, "    *units tape metres\n");
  }
  if ( units.angle_units == ANGLE_GRAD ) {
    fprintf(fp, "    *units compass clino grads\n");
  } else {
    fprintf(fp, "    *units compass clino degrees\n");
  }

  fprintf(fp, "    *calibrate declination %.2f \n\n", info.declination ); 

  fprintf(fp, "    *date %4d.%02d.%02d\n", c_info.year, c_info.month, c_info.day );
  if ( info.centerlineCommand.size() > 0 ) {
    fprintf(fp, "%s\n", info.centerlineCommand.c_str() );
  }
  fprintf(fp, "    *data normal from to tape compass clino\n");
  DBlock * b;
  bool in_splay = false;
  bool in_surface = false;
  bool in_duplicate = false;
  for ( b = data.Head(); b; b=b->Next() ) {
    if ( ! b->hasFrom() && ! b->hasTo() ) {
      // skip data with neither From nor To 
      continue;
    }
    if ( b->Flag() == FLAG_SURFACE ) { // surface
      if ( ! in_surface ) {
        fprintf(fp, "    *flags surface\n");
        in_surface = true;
      }
    } else {
      if ( in_surface ) {
        fprintf(fp, "    *flags not surface\n");
        in_surface = false;
      }
    }
    if ( b->Flag() == FLAG_DUPLICATE || ! b->hasTo() ) { // duplicate
      if ( ! in_surface ) {
        fprintf(fp, "    *flags duplicate\n");
        in_duplicate = true;
      }
    } else {
      if ( in_duplicate ) {
        fprintf(fp, "    *flags not duplicate\n");
        in_duplicate = false;
      }
    }

    if ( ! b->hasFrom() ) {
      if ( ! in_splay ) {
        fprintf(fp, "    *flags splay\n");
        in_splay = true;
      }
      ++extra_cnt;
      if ( b->hasTo() ) {
        fprintf(fp, "    %s_%04d %s", b->To(), extra_cnt, b->To() );
      } else {
        fprintf(fp, "    from_%04d to_%04d", extra_cnt, extra_cnt);
      }
    } else {
      if ( ! b->hasTo() ) {
        if ( ! in_splay ) {
          fprintf(fp, "    *flags splay\n");
          in_splay = true;
        }
        fprintf(fp, "    %s %s_%04d", b->From(), b->From(), ++extra_cnt);
      } else {
        if ( in_splay ) {
          fprintf(fp, "    *flags not splay\n");
          in_splay = false;
        }
        fprintf(fp, "    %s %s", b->From(), b->To() );
      }
    }

    fprintf(fp, " %.2f %.2f %.2f \n", ls*b->Tape(), as*b->Compass(), as*b->Clino() );
    if ( b->hasComment() ) {
      fprintf(fp, "    ; %s\n", b->Comment() );
    }
  }

  if ( info.surveyCommand.size() > 0 ) {
    fprintf(fp, "%s\n", info.surveyCommand.c_str() );
  }
  fprintf(fp, "*end\n");
  fclose( fp );
  return true;
}
