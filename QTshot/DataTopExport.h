/** @file DataTopExport.h
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Survex format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DATA_TOP_EXPORT_H
#define DATA_TOP_EXPORT_H

#include "CenterlineInfo.h"
#include "DataList.h"
#include "Units.h"


/** export the data in PocketTopo format
 * @param data centerline data
 * @param info centerline info
 * @param plan plan plot status (or NULL)
 * @param extended extended section plot status (or NULL)
 * @return true if ok
 */
bool
saveAsPocketTopo( DataList & data,
                  const CenterlineInfo & c_info,
                  PlotStatus * plan,
                  PlotStatus * extended );

#endif
