/** @file DataDatExport.h
 *
 * @author marco corvi
 * @date april 2010
 *
 * @brief Centerline data export in Compass format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DATA_DAT_EXPORT_H
#define DATA_DAT_EXPORT_H

#include "CenterlineInfo.h"
#include "DataList.h"
#include "Units.h"


/** export the data in Compass format
 * @param data       centerline data
 * @param c_info     additional survey info
 * @param units      user units
 * @param max_len    maximum length of a station name
 * @return true if successful
 */
bool
saveAsCompass( DataList & data,
               const CenterlineInfo & c_info,
               const Units & units,
               int & max_len );

#endif
