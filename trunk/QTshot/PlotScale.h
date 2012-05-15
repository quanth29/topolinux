/** @file PlotScale.h
 *
 * @author marco corvi
 * @date Jan 2010
 *
 * @brief plot scale
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_SCALE_H
#define PLOT_SCALE_H

#define PLOT_SCALE    4
#define BASE_SCALE   10
#define PT_FACTOR     4   // PocketTopo drawings seems to be scaled by 4/10
#define TH_FACTOR   5.0   // therion scrap export factor

#define PT_EPS        4  // epsilon size of a point [pxl]
#define CP_MAX      100  // maximum size of a control point segment [pxl]

#define PT_SCALE ((PT_FACTOR * BASE_SCALE))

#endif

