/** @file DataThExport.h
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Therion format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DATA_TH_EXPORT_H
#define DATA_TH_EXPORT_H

#include "CenterlineInfo.h"
#include "DataList.h"
#include "Units.h"


/** export the data in Therion format
 * @param data       centerline data list
 * @param c_info     additional survey info
 * @param units      user units
 * @return true if successful
 */
bool
saveAsTherion( DataList & data,
               const CenterlineInfo & c_info,
               const Units & units );

#endif
