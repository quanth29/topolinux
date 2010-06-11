/** @file DataSvxExport.h
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Survex format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DATA_SVX_EXPORT_H
#define DATA_SVX_EXPORT_H

#include "CenterlineInfo.h"
#include "DataList.h"
#include "Units.h"


/** export the data in Survex format
 * @param data       centerline data
 * @param c_info     additional survey info
 * @param units      user units
 * @return true if successful
 */
bool
saveAsSurvex( DataList & data,
              const CenterlineInfo & c_info,
              const Units & units );

#endif
