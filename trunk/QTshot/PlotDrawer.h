/** @file PlotDrawer.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief Plot drawer interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_DRAWER_H
#define PLOT_DRAWER_H

#include "TherionPoint.h"
#include "TherionLine.h"
#include "CanvasMode.h"
#include "PTcolors.h"

class PlotCanvas;

class PlotDrawer
{
  public:
    virtual ~PlotDrawer() { }

    /** close all plots
     */
    virtual void closePlots() = 0;

    virtual void insertPoint( int x, int y, Therion::PointType type, PlotCanvas * canvas ) = 0;
  
    virtual void insertLinePoint( int x, int y, Therion::LineType type, PlotCanvas * canvas ) = 0;

    virtual PlotCanvas *  openPlot( int mode, const char * pname, const char * sname ) = 0;

    virtual const PTcolors & getColors() const = 0;

};

#endif

