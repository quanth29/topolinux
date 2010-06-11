/** @file PlotFrame.h
 *
 * @author marco corvi
 * @date jan 2010
 *
 * @brief plot reference frame
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_FRAME_H
#define PLOT_FRAME_H

#include "portability.h"

#define PLOT_FRAME_GRID 0
#define PLOT_FRAME_BAR  1


class PlotFrame
{
  private:
    int status;               //!< frame status
    size_t gx;                //!< number of vertical grid-lines
    size_t gy;                //!< number of horizontal grid-lines
    QCANVASLINE ** grid;
    QCANVASLINE * scale_bar;  //!< scale bar (10 m)
    QCANVASTEXT * scale_text; //!< scale value 
    int grid_spacing;         //!< grid spacing


  public:
    PlotFrame()
      : status( PLOT_FRAME_GRID )
      , gx( 0 )
      , gy( 0 ) 
      , grid( NULL )
      , scale_bar( NULL )
      , scale_text( NULL )
      , grid_spacing( 0 )
    { }

    ~PlotFrame()
    {
      clearGrid();
      clearBar();
    }

    int Status() const { return status; }

    QCANVASLINE * ScaleBar() { return scale_bar; }

    QCANVASTEXT * ScaleText() { return scale_text; }

    int GridSpacing() const { return grid_spacing; }

    /** set the status
     * @param s   new status
     */
    void setStatus( int s )
    {
      if ( status != s ) {
        if ( status == PLOT_FRAME_GRID ) {
          clearGrid(); 
        } else if ( status == PLOT_FRAME_BAR ) {
          clearBar();
        }
        status = s;
      }
    }

    void switchStatus()
    {
      if ( status == PLOT_FRAME_GRID ) {
        clearGrid();
        status = PLOT_FRAME_BAR;
      } else if ( status == PLOT_FRAME_BAR ) {
        clearBar();
        status = PLOT_FRAME_GRID;
      }
    }

    /** update the frame grid
     * @param canvas canvas
     * @param s      plot scale
     * @param dx     plot width
     * @param dy     plot height
     * @param units  name of the units
     */
    void Update( QCANVAS * canvas, double s, int dx, int dy, const char * units );

  private:
    void clearGrid();

    void clearBar();

};

#endif
