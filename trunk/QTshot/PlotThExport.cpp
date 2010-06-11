/** @file PlotThExport.cpp
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot export implementation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <sstream>

#include "portability.h"

#include "ArgCheck.h"

#define PERCENT_OFFSET 10 /* one tenth */
#define MIN_OFFSET 50 /* pixels */

#include "PlotScale.h"
#include "PlotStatus.h"
#include "DataList.h"
#include "PlotThExport.h"


/** therion scrap names prefixes
 * @note indices must agree with modes defined in CanvasMode.h
 *
const char scrap_prefix[] = {
  'p', // plan
  's', // extended section
  'x', // cross section
  'n', // not used (3D)
  'h'  // horizontal cross section
};
 */

int PlotThExport::scrap_count = 0;

/** therion points names
 * @note must agree with defines in TherionPoint.h
 */
const char * 
PlotThExport::ThPointName[] = {
  "air-draught", 
  "blocks",
  "clay",
  "debris",
  "label",
  "sand",
  "stalactite",
  "user",
  "water-flow", 
  "entrance",
  "continuation",
  "station", // station must be last
};

/** therion line names
 * @note must agree with defines in TherionLine.h
 */
const char * 
PlotThExport::ThLineName[] = {
  "arrow",
  "border",
  "chimney",
  "pit",
  "user",
  "wall",
  "rock-border",
};


void 
PlotThExport::exportTherion( const char * proj, PlotStatus * status, DataList * list )
{
  ARG_CHECK( proj == NULL, );
  ARG_CHECK( status == NULL, );
  ARG_CHECK( list == NULL, );
  
  if ( th2FileName.size() == 0 ) {
    DBG_CHECK("exportTherion() null filename\n");
    return;
  }
  if ( scrap_name.size() == 0 ) {
    DBG_CHECK("exportTherion() empty scrap name\n");
    return;
  }

  // ------------------------------------------------------------------
  double scale = status->Scale();
  const std::vector< ThLine * > & lines = status->lines;
  const std::vector< ThPoint2D > & pts  = status->pts;

  FILE * fp = fopen( th2FileName.c_str(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("exportTherion() cannot open file %s\n", th2FileName.c_str() );
    return;
  }
  fprintf(fp, "scrap %s%d -projection %s -scale [0 0 1 0 0.0 0.0 %.2f 0.0 m]\n",
    scrap_name.c_str(), scrap_nr, proj, TH_FACTOR/scale );
  
  for ( std::vector< ThLine * >::const_iterator it = lines.begin(); 
        it != lines.end(); 
        ++it ) {
    // size_t size = (*it)->Size();
    if ( (*it)->isClosed() ) {
      fprintf(fp, "\nline %s -close on\n", ThLineName[ (*it)->type ]);
    } else {
      fprintf(fp, "\nline %s\n", ThLineName[ (*it)->type ]);
    }

    for ( ThLine::const_iterator pit = (*it)->Begin();
          pit != (*it)->End(); // && size > 0;
          ++pit /* , --size */ ) {
      fprintf(fp, "  %.2f %.2f\n", pit->x*TH_FACTOR, -(pit->y*TH_FACTOR) );
    }
    fprintf(fp, "endline\n\n");
  }
  for ( std::vector< ThPoint2D >::const_iterator pit = pts.begin(); pit != pts.end(); ++pit ) {
    if ( pit->type == THP_WATER || pit->type == THP_AIR || pit->type == THP_ENTRANCE ) {
      fprintf(fp, "point %.2f %.2f %s -orient %d %s\n\n", 
        pit->x*TH_FACTOR, -(pit->y*TH_FACTOR), ThPointName[pit->type],
        pit->orientation*45, // orientations are in steps of 45 degrees
        pit->option.c_str() );
    } else if ( pit->type == THP_STATION ) {
      const char * name = pit->option.c_str() + 6; // skip "-name "
      fprintf(fp, "point %.2f %.2f %s %s\n", 
        pit->x*TH_FACTOR, -(pit->y*TH_FACTOR), ThPointName[pit->type], pit->option.c_str() );
      if ( list->hasStationComment( name ) ) {
        fprintf(fp, "# %s\n", list->getStationComment( name ) ); 
      }
      fprintf(fp, "\n");
    } else {
      fprintf(fp, "point %.2f %.2f %s %s\n\n", 
        pit->x*TH_FACTOR, -(pit->y*TH_FACTOR), ThPointName[pit->type], pit->option.c_str() );
    }
  }
  fprintf(fp, "endscrap\n");
  fclose( fp );
  DBG_CHECK("exportTherion()doSaveTh2 written file %s\n", th2FileName.c_str() );
}

