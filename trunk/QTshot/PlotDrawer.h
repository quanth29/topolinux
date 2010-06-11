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

class PlotDrawer
{
  public:
    virtual ~PlotDrawer() { }

    /** close all plots
     */
    virtual void closePlots() = 0;

    virtual void insertPoint( int x, int y, ThPointType type, int mode ) = 0;
  
    virtual void insertLinePoint( int x, int y, ThLineType type, int mode ) = 0;

    virtual void openPlot( int mode ) = 0;

    virtual const PTcolors & colors() const = 0;

};

#endif

