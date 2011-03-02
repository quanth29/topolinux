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
// #include <sstream>
// 
// #include <QFileInfo>
// #include <QFile>
  
#include "shorthands.h"
#include "DataTopExport.h"
#include "Extend.h"
#include "Flags.h"
  
bool
saveAsPocketTopo( DataList & data,
                  const CenterlineInfo & c_info,
                  PlotStatus * plan,
                  PlotStatus * extended )
{
  const SurveyInfo & info = c_info.surveyInfo;
  FILE * fp = fopen( info.exportName.TO_CHAR(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", info.exportName.TO_CHAR() );
    return false;
  }

#ifdef HAS_POCKETTOPO 
  #include "../PTopo/PTexportDL.impl"
#else
  plan = plan;
  extended = extended;
#endif

  fclose( fp );

  return true;
}
