/** @file DataTopExport.cpp
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

#include "DataTopExport.h"

#include "Flags.h"
#include "Extend.h"
  
bool
saveAsPocketTopo( DataList & data,
                  const CenterlineInfo & c_info,
                  PlotStatus * plan,
                  PlotStatus * extended )
{
  FILE * fp = fopen( c_info.exportName.latin1(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", c_info.exportName.latin1() );
    return false;
  }

#ifdef HAS_POCKETTOPO 
  const SurveyInfo & info = c_info.surveyInfo;
  #include "../PTopo/PTexportDL.impl"
#else
  plan = plan;
  extended = extended;
#endif

  fclose( fp );

  return true;
}
