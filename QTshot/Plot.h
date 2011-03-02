/** @file Plot.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief plot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_H
#define PLOT_H

#include <stdio.h>
#include <ctype.h>
#include <math.h>

#include <string>
#include <utility>
#include <vector>

#include "ArgCheck.h"
#include "DataList.h"
#include "PlotPoint.h"
#include "Factors.h"

// #include <qwidget.h>

class TherionScrap; // forward

class Plot 
{
  private:
    PlotSegment * segment;  //!< list of plotting segments
    PlotPoint * points;       //!< list of plotting points
    int x0min, x0max, y0min, y0max; //!< plot bounding box
    int z0min, z0max;
    // double scale;

    /** sin/cos of viewing direction (for 3D plot)
     */
    double sin_phi, cos_phi, sin_theta, cos_theta;
    int nr_centerline_segments;    //!< number of centerline segments
    int nr_segments;    //!< total number of segments
    int nr_centerline_points;    //!< number of centerline points
    #ifdef HAS_LRUD
      bool with_lrud; //!< whether to add LRUD shots
    #endif

  public:
    #ifdef HAS_LRUD
      Plot( bool lrud, double theta = 0.0, double phi = 0.0 )
    #else
      Plot( double theta = 0.0, double phi = 0.0 )
    #endif
      : segment( NULL )
      , points( NULL )
      // , scale( 1.0 )
      , x0min( 0 )
      , x0max( 0 )
      , y0min( 0 )
      , y0max( 0 )
      , nr_centerline_segments( 0 )
      , nr_segments( 0 ) 
      , nr_centerline_points( 0 )
      #ifdef HAS_LRUD
        , with_lrud( lrud )
      #endif
    {
      DBG_CHECK( "Plot::cstr theta %.2f phi %.2f \n", theta, phi ); 
      setThetaPhi( theta, phi );
    }

    ~Plot()
    {
      DBG_CHECK( "Plot::dstr \n"); 
      reset();
    }

    /** get the plot X size
     * @return the plot width
     */
    int getWidth() const { return x0max - x0min; }

    /** get the plot Y size
     * @return the plot height
     */
    int getHeight() const { return y0max - y0min; }

    /** get the plot X offset
     * @return the plot minimum X
     * @note called only in PlotCanvas::getPlotXOffset()
     */
    int getXOffset() const { return x0min; }

    /** get the plot Y offset
     * @return the plot minimum Y
     * @note called only in PlotCanvas::getPlotYOffset()
     */
    int getYOffset() const { return y0min; }

    /** set theta and phi (viewpoint for the 3D)
     * @param theta   theta [degrees] = 90 - inclination
     * @param phi     phi [degrees] = azimuth
     */
    void setThetaPhi( double theta, double phi )
    {
      sin_theta = sin( M_PI * theta / 180.0 );
      cos_theta = cos( M_PI * theta / 180.0 );
      sin_phi = sin( M_PI * phi / 180.0 );
      cos_phi = cos( M_PI * phi / 180.0 );
    }

    /** get the head of the list of canvas points
     * @return the root point pointer
     */
    PlotPoint * getPoints() { return points; }

    /** get the head of the list of canvas segments
     * @return the root segment pointer
     */
    PlotSegment * getSegments() { return segment; }

    void dump();
 
#ifdef IMPORT_TH2
    /** adjust plot size to scrap (point and line-points)
     * @param scrap therion scrap
     */
    void adjustToScrap( TherionScrap * scrap );
#endif

    /** compute the plot for plan/extended section/3D
     * @param list   data list
     * @param mode   plot mode (see CanvasMode.h)
     * @param do_num if true compute the centerline
     * @return true if successful
     */
    bool computePlot( DataList * list, int mode, bool do_num = true );

    /** compute the plot for a cross section
     * @param list  data list
     * @param block centerline segment
     * @param reversed whether the cros-section is reversed
     * @param vertical vertical threshold (with clino above it the x-section is horizontal)
     */
    bool computeXSection( DataList * list, DBlock * block, bool reversed, double vertical = 80.0 );

#ifdef HAS_LRUD
    /** compute LRUD segments for cross sections
     * @param p0    base point (on the canvas)
     * @param lrud  LRUD struct
     * @param blk   data block (centerline shot)
     * @param reverse whether the shot is plotted reversed (swap Left/Right)
     */
    void drawLRUD( PlotPoint * p0, LRUD * lrud, DBlock * blk, bool reversed );

    /** compute LRUD segments for plan/extended/3D
     * @param p0    base point (on the canvas)
     * @param lrud  LRUD struct
     * @param blk   data block (centerline shot)
     * @param dn    Delta-N between the canvas points of the shot
     * @param de    Delta-E between the canvas points of the shot
     * @param mode  plotting mode
     */
    void drawLRUD( PlotPoint * p0, LRUD * lrud, DBlock * blk, 
                   double dn, double de, int mode );
#endif

/*
    void shift( int x, int y ) 
    {
      for ( PlotPoint * p = points; p; p=p->next ) {
        p->x0 += x;
        p->y0 += y;
      }
      for ( PlotSegment * s = segment; s; s=s->next ) {
        s->x0 += x;
        s->x1 += x;
        s->y0 += y;
        s->y1 += y;
      }
    }

    void zoom( int k ) 
    {
      if ( k > 0 ) scale *= 2;
      else if ( k < 0 ) scale /= 2;
    }
*/

    void reset()
    {
      clearPoints();
      clearSegments();
    }
 
    void clearSegments()
    {
      DBG_CHECK( "Plot::ClearSegments() \n");
      while ( segment ) {
        PlotSegment * next = segment->next;
        delete segment;
        segment = next;
      }
      nr_centerline_segments = 0;
      nr_segments = 0;
    }

    void clearPoints()
    {
      DBG_CHECK( "Plot::ClearPoints() \n");
      while ( points ) {
        PlotPoint * npt = points->next;
        delete points;
        points = npt;
      }
      nr_centerline_points = 0;
    }

    void drawPoint2Point( PlotPoint * p0, PlotPoint * p1, // int col,
                          DBlock * blk, unsigned char extend, int mode );

    void drawFromPoint( PlotPoint * p0,
                     double dn, double de, double dh, double dz,
                     DBlock * blk, unsigned char extend, int mode );

    /** draw a vertical segment (for the cross-section)
     * @param p0 first endpoint
     * @param p1 second endpoint
     * @param blk block
     */
    void drawVerticalSegment( PlotPoint * p0, PlotPoint * p1, DBlock * blk );

    /** compute the amount of horizontal displacement for the extended section
     *  when no extend has been specified
     * @param p0   base point from which to compute DX
     * @param de   East displacement
     * @param dn   North displacement
     * @return fraction of right (+1) or left (-1) extend
     */
    double computeXExtend( PlotPoint * p0, double de, double dn );
};

#endif // PLOT_H
