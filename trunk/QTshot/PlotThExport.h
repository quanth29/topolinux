/** @file PlotThExport.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief plot therion export
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_TH_EXPORT_H
#define PLOT_TH_EXPORT_H

#include <string>

// #include <qstring.h>

class Datalist; // forward
struct PlotStatus;


namespace PlotThExport
{
    /** export the plot data as therion th2 file
     */
    void exportTherion( const char * proj, PlotStatus * status, DataList * list );

    void exportImage( PlotStatus * status, DataList * list );

};

#endif
