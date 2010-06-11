/** @file PlotFrame.cpp
 *
 * @author marco corvi
 * @date jan 2010
 *
 * @brief plot reference frame
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#include "ArgCheck.h"

#include "PlotFrame.h"
#include "PlotScale.h"
#include "IconSet.h"

/** number of cells in the window
 */
#define FRAME_NUMBER 40


void
PlotFrame::clearBar()
{
  if ( scale_bar ) delete scale_bar;
  scale_bar = NULL;
  if ( scale_text ) delete scale_text;
  scale_text = NULL;
}

void 
PlotFrame::clearGrid()
{
  if ( grid ) {
    for (size_t k=0; k<gx+gy; ++k ) delete grid[k];
    delete[] grid;
  }
  grid = NULL;
}

void
PlotFrame::Update( QCANVAS * canvas, double s, int dx, int dy, const char * units )
{
  ARG_CHECK( canvas == NULL, );
  ARG_CHECK( units == NULL, );
  
  clearBar();   
  clearGrid();

  IconSet * icon = IconSet::Get();
  QPen p( icon->PenGray() );
  dx = (int)(dx*s);
  dy = (int)(dy*s);
  s *= SCALE;
  grid_spacing = 0;
  if ( status == PLOT_FRAME_GRID ) {
    int k = 0; // index: 1 units (meters or feet)
    int value[] = { 1, 5, 10, 50, 100, 500 }; // grid spacing
    int d = dx / FRAME_NUMBER;
    if ( dy/FRAME_NUMBER < d ) d = dy / FRAME_NUMBER; // use the smallest
    while ( value[k]*s < d && k < 6 ) ++k;

    grid_spacing = value[k];
    int d0 = (int)(value[k]*s);
    gx = dx/d0;
    gy = dy/d0;
    grid = new QCANVASLINE* [ gx+gy ];
    for (size_t k=0; k<gx; ++k ) {
      int x = d0/2+k*d0;
      grid[k] = new QCANVASLINE( canvas );
      grid[k]->setPen( p );
      grid[k]->setZ( 128 );
      grid[k]->setPoints( x, 0, x, dy );
      grid[k]->show();
    }
    for (size_t k=gx; k<gx+gy; ++k ) {
      int y = d0/2+(k-gx)*d0;
      grid[k] = new QCANVASLINE( canvas );
      grid[k]->setPen( p );
      grid[k]->setZ( 128 );
      grid[k]->setPoints( 0, y, dx, y );
      grid[k]->show();
    }
    // printf("grid sacing %d %s \n", grid_spacing, units );
  } else {
    int k = 3; // index: 10 units (meters or feet)
    int value[] = { 1, 2, 5, 10, 20, 50, 100, 200 };
    while ( value[k]*s < 50 && k < 8 ) ++k;
    while ( value[k]*s > 150 && k > 0 ) --k;
 
    scale_bar = new QCANVASLINE( canvas );
    QPen p1( icon->PenBlack() );
    p1.setWidth( 3 );
    scale_bar->setPen( p1 );
    scale_bar->setZ( 128 );
    scale_bar->setPoints( (int)(s), (int)(s), (int)(s + value[k]*s), (int)(s) );
    scale_bar->show();
    char scale[10];
    sprintf(scale, "%d %s", value[k], units );
    scale_text = new QCANVASTEXT( QString(scale), canvas );
    scale_text->setColor( Qt::black );
    scale_text->setX( (int)(s + value[k]*s) );
    scale_text->setY( (int)(s) );
    scale_text->setZ( 255 );
    scale_text->show();
  }
}
