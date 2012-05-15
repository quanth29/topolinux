/** @file PlotStatus.cpp
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief plot status
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include "PlotStatus.h"
#include "PlotScale.h"
#include "PlotCanvasScene.h"

/** number of cells in the window
 */
#define FRAME_NUMBER 40


void 
PlotStatus::setActiveScrap( TherionScrap * scrap )
{
  if ( th_scrap ) {
    if ( canvas ) canvas->greyOut( th_scrap );
  }
  th_scrap = scrap;
  if ( th_scrap ) {
    if ( canvas ) canvas->greyIn( th_scrap );
  }
}

#ifdef HAS_POCKETTOPO
void
PlotStatus::exportPTfile( PTdrawing & drawing )
{
   if ( th_scrap ) th_scrap->exportPocketTopoFile( drawing, offx, offy );
}
#endif



void
PlotStatus::setStatus( int s )
{
  if ( status != s ) {
    status = s;
    if ( canvas ) canvas->displayGrid( status );
  }
}

void 
PlotStatus::switchStatus()
{
  if ( status == PLOT_FRAME_GRID ) {
    status = PLOT_FRAME_BAR;
  } else if ( status == PLOT_FRAME_BAR ) {
    status = PLOT_FRAME_GRID;
  }
  if ( canvas ) canvas->displayGrid( status );
}

void
PlotStatus::computeGrid( double s, int dx, int dy, const char * units )
{
  ARG_CHECK( units == NULL, );
  if ( status != PLOT_FRAME_3D ) {
    dx = (int)(dx * s);
    dy = (int)(dy * s);
    s *= BASE_SCALE;   // SCALE = 10
    grid_spacing = 0;
    {
      int k = 0; // index: 1 units (meters or feet)
      int value[] = { 1, 5, 10, 50, 100, 500 }; // grid spacing
/*
      int d = dx / FRAME_NUMBER;
      if ( dy/FRAME_NUMBER < d ) d = dy / FRAME_NUMBER; // use the smallest
      while ( value[k]*s < d && k < 6 ) ++k;
*/  
      grid_spacing = value[k];
      int d0 = (int)(value[k]*s);
      int gx = dx/d0;
      int gy = dy/d0;
      if ( canvas ) canvas->makeGrid( d0, gx, gy, dx, dy );
    }
    // printf("grid spacing %d %s \n", grid_spacing, units );
    {
      int k = 3; // index: 10 units (meters or feet)
      int value[] = { 1, 2, 5, 10, 20, 50, 100, 200 };
      while ( value[k]*s < 50 && k < 8 ) ++k;
      while ( value[k]*s > 150 && k > 0 ) --k;
      char scale[10];
      sprintf(scale, "%d %s", value[k], units );
      if ( canvas ) canvas->makeScaleBar( s+3, s+value[k]*s-2, s, scale );
    }
  }
}


