/** @file PlotStatus.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief plot status
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_STATUS_H
#define PLOT_STATUS_H

#include <stdlib.h>

#include <string>

// #include "TherionPoint.h"
// #include "TherionLine.h"
// #include "TherionArea.h"
#include "TherionScrap.h"

#include "CanvasUndo.h"
#include "PlotScale.h"

#ifdef HAS_POCKETTOPO
  #include "PTfile.h"
#endif

#define PLOT_FRAME_GRID 0
#define PLOT_FRAME_BAR  1
#define PLOT_FRAME_3D   2


class PlotCanvasScene; // forward

struct PlotStatus
{
  private:
    std::vector< TherionScrap * > scraps; //!< therion scraps
    TherionScrap * th_scrap;    //!< current scrap
    // std::vector< ThPoint2D * > th_points;
    // std::vector< ThLine2D * >  th_lines;
    // std::vector< ThArea2D * >  th_areas;
    int    status;         //!< frame status: grid or scalebar
    // double scale;
    int    offx;
    int    offy;
    int    width;          //!< canvas width
    int    height;         //!< canvas height
    // int    orientation;    //!< orientation 0..7 [units of 45 degrees]
    double theta, phi;     //!< viewpoint (for 3D)
    CanvasUndo * undo;     //!< list of undo's
    bool do_numbers;       //!< whether to display station numbers
    int  grid_spacing;     //!< grid spacing
    std::string file_name;  //!< file name
    std::string image_name;  //!< file name
    // std::string scrap_name; //!< scrap name
    PlotCanvasScene * canvas;

  public:
    PlotStatus( int st, const char * fname, const char * sname  )
      : status( st )
      // , scale( 4.0 )
      , offx( 0 )
      , offy( 0 )
      , width( 1 )
      , height( 1 )
      // , orientation( 0 )
      , theta( 0.0 )  // from north (0 azymuth) on the horizontal plane
      , phi( 0.0 )
      , undo( NULL )
      , do_numbers( true )
      , grid_spacing( 1 ) // 1 meter
      , canvas( NULL )
    { 
      setFileName( fname );
      addScrap( sname );
      // setScrapName( name );
    }

    ~PlotStatus()
    {
      clear();
    }
 

    void setScene( PlotCanvasScene * scene ) { canvas = scene; }

    const char * getFileName() const { return file_name.c_str(); }
    const char * getImageName() const { return image_name.c_str(); }
    // const char * getScrapName() const { return scrap_name.c_str(); }

    /** get the current (active) scrap
     * @return the current therion scrap
     */
    TherionScrap * getScrap() const { return th_scrap; }

    /** set the active scrap by the name
     * @param name   name of the new active scrap
     */
    bool setScrap( const char * name )
    {
      for ( std::vector< TherionScrap * >::const_iterator sit = scraps.begin();
            sit != scraps.end();
            ++ sit ) {
        if ( (*sit)->hasName( name ) ) {
          setActiveScrap( *sit );
          return true;
        }
      }
      return false;
    }

    /** get the number of scraps
     * @return the number of scraps
     */
    size_t getScrapNr() const { return scraps.size(); }

    std::vector< TherionScrap * >::const_iterator scrapBegin() const { return scraps.begin(); }
    std::vector< TherionScrap * >::const_iterator scrapEnd() const { return scraps.end(); }

    /** add a new scrap
     * @param name scrap name
     */
    void addScrap( const char * name )
    {
      TherionScrap * scrap = new TherionScrap( this, name );
      scraps.push_back( scrap );
      setActiveScrap( scrap );
    }

    /** set the active scrap
     * @param scrap    new active scrap
     */
    void setActiveScrap( TherionScrap * scrap );

    /** set the plot th2 filename
     * @param name   filename
     */
    void setFileName( const char * name ) 
    { 
      file_name = name;
      // enforce .th2
      size_t len = file_name.size();
      if ( len > 4 ) len -= 4; 
      if ( strncmp(file_name.c_str() + len, ".th2", 4) != 0 ) file_name += ".th2";
    }

    /** set the plot PNG filename
     * @param name   filename
     */
    void setImageName( const char * name ) 
    { 
      image_name = name;
      // enforce .th2
      size_t len = image_name.size();
      if ( len > 4 ) len -= 4; 
      if ( strncmp(image_name.c_str() + len, ".png", 4) != 0 ) image_name += ".png";
    }

    /** set the status
     * @param s   new status
     */
    void setStatus( int s );
    void switchStatus();
    int getStatus() const { return status; }

    /** update the frame grid
     * @param s      plot scale (plot scale divided by the lenght factor)
     * @param dx     plot width
     * @param dy     plot height
     * @param units  name of the units
     */
    void computeGrid( double s, int dx, int dy, const char * units );
    int getGridSpacing() const { return grid_spacing; }

    /** check if there are drawing items
     * @return true if there are drawing items
     */
    // bool hasItems()
    // {
    //   return th_points.size() > 0 || th_lines.size() > 0 || th_areas.size() > 0 ;
    // }

    void reset()
    {
      // scale = 4.0;
      offx = 0;
      offy = 0;
      do_numbers = true;
      clear();
    }


    void clear()
    {
      // size_t k0 = th_points.size();
      // for ( size_t k=0; k<k0; ++k) delete th_points[k];
      // th_points.clear();
      // k0 = th_lines.size();
      // for ( size_t k=0; k<k0; ++k) delete th_lines[k];
      // th_lines.clear();
      // k0 = th_areas.size();
      // for ( size_t k=0; k<k0; ++k) delete th_areas[k];
      // th_areas.clear();
      clearUndos();
      for ( std::vector< TherionScrap *>::iterator sit = scraps.begin(); sit != scraps.end(); ++sit ) {
        delete *sit;
      }
      scraps.clear();
      th_scrap = NULL;
    }

    /** clear list of undo's
     */
    void clearUndos()
    {
      while ( this->undo ) {
        CanvasUndo * next_undo = undo->next();
        delete this->undo;
        this->undo = next_undo;
      }
    }

    /** append an undo to the undo list
     * @param cmd undo command
     */
    void addUndo( int cmd )
    {
      this->undo = new CanvasUndo( cmd, this->undo );
    }

    CanvasUndo * popUndo()
    {
      CanvasUndo * ret = undo;
      if ( undo ) undo = undo->next();
      return ret;
    }

    /** check if there are undos
     * @return true is the undo list is not empty
     */
    bool hasUndo() const { return undo != NULL; }

    // void shiftItems( int dx, int dy )
    // {
    //   std::vector< ThPoint2D * >::iterator pend = th_points.end();
    //   for ( std::vector< ThPoint2D * >::iterator it = th_points.begin(); it != pend; ++it ) {
    //     (*it)->shift( dx, dy );
    //   }
    //   for ( std::vector< ThLine2D * >::iterator lit = th_lines.begin(), lend = th_lines.end(); lit != lend; ++lit ) {
    //     for ( ThLine2D::point_iterator it = (*lit)->pointBegin(), end = (*lit)->pointEnd(); it != end; ++it ) {
    //       (*it)->shift( dx, dy );
    //     }
    //   }
    //   for ( std::vector< ThArea2D * >::iterator lit = th_areas.begin(), lend = th_areas.end(); lit != lend; ++lit ) {
    //     for ( ThArea2D::point_iterator it = (*lit)->Begin(), end = (*lit)->End(); it != end; ++it ) {
    //       (*it)->shift( dx, dy );
    //     }
    //   }
    // }

    /** get theta (azimuth)
     * @return theta
     */
    double getTheta() const { return theta; }

    /** get phi 
     * @return phi
     */
    double getPhi() const { return phi; }

    void addTheta( double t ) 
    { 
      theta += t;
      if ( theta > 90.0 ) theta = 90.0;
      if ( theta < -90.0 ) theta = -90.0;
    }

    void addPhi( double p ) 
    {
      phi += p; 
      if ( phi >= 360.0 ) phi -= 360.0;
      if ( phi < 0.0 ) phi += 360.0;
    }

    /** get the X offset of the plot
     * @return the plot X-offset
     *
     * @note The offset is added to the point coordinates so that the plot points
     *       can have only positive values. The transformation from world frame to 
     *       plot frame has also a multiplicative factor:
     *
     *         X_plot = scale * ( X_offset + X_world )
     *         Y_plot = scale * ( Y_offset + Y_world )
     */
    int getOffsetX() const { return offx; }
    int getOffsetY() const { return offy; }

    /** set the plot X-offset
     * @param x   plot X offset
     * @note called by PlotCanvasScene
     */
    void setOffsetX( int x ) { offx = x; }
    void setOffsetY( int y ) { offy = y; }

    /** transform from world X to plot X
     * @param x   X_world value
     * @return X_plot value
     *
     * @note PLOT_SCALE = 4
     */
    double evalToX( double x ) { return PLOT_SCALE*(offx + x); }
    double evalToY( double y ) { return PLOT_SCALE*(offy + y); }

    /** transform from plot X to world X
     * @param x   X_plot value
     * @return X_world value
     */
    double evalFromX( double x ) { return x/PLOT_SCALE - offx; }
    double evalFromY( double y ) { return y/PLOT_SCALE - offy; }

    /** get the width of the plot
     * @return the plot width
     */
    int getWidth() const { return width; }
    int getHeight() const { return height; }
    
    void setWidth( int w ) { width = w; }
    void setHeight( int h ) { height = h; }

    double getScale() const { return PLOT_SCALE; }
    // void setScale( double s ) { scale = s; }

    void flipNumbers() { do_numbers = ! do_numbers; }
    bool isNumbers() const { return do_numbers; }

  #ifdef HAS_POCKETTOPO
    /** export drawing to a pockettopo file
     * @param drawing  PocketTopo drawing in the pockettopo file
     */
    void exportPTfile( PTdrawing & drawing );
  #endif

    // size_t pointsSize() const { return th_points.size(); }
    // std::vector< ThPoint2D * >::iterator pointsBegin() { return th_points.begin(); }
    // std::vector< ThPoint2D * >::iterator pointsEnd()   { return th_points.end(); }
    // void pointsAdd( ThPoint2D * pt ) { th_points.push_back( pt ); }
    // void pointsErase( std::vector< ThPoint2D * >::iterator it ) { th_points.erase( it ); }
    // ThPoint2D * pointsEraseLast()
    // {
    //   ThPoint2D * pt = ( th_points.back() );
    //   th_points.pop_back();
    //   return pt;
    // }
    // ThPoint2D * pointsBack() { return th_points.back(); }

    // size_t linesSize() const { return th_lines.size(); }
    // std::vector< ThLine2D * >::iterator linesBegin() { return th_lines.begin(); }
    // std::vector< ThLine2D * >::iterator linesEnd()   { return th_lines.end(); }
    // void linesAdd( ThLine2D * line ) { th_lines.push_back( line ); }
    // void linesErase( std::vector< ThLine2D * >::iterator it ) { th_lines.erase( it ); }
    // ThLine2D * linesEraseLast()
    // {
    //   ThLine2D * line = ( th_lines.back() );
    //   th_lines.pop_back();
    //   return line;
    // }
    // ThLine2D * linesBack() { return th_lines.back(); }


    // size_t areasSize() const { return th_areas.size(); }
    // std::vector< ThArea2D * >::iterator areasBegin() { return th_areas.begin(); }
    // std::vector< ThArea2D * >::iterator areasEnd()   { return th_areas.end(); }
    // void areasAdd( ThArea2D * area ) { th_areas.push_back( area ); }
    // void areasErase( std::vector< ThArea2D * >::iterator it ) { th_areas.erase( it ); }
    // ThArea2D * areasEraseLast()
    // {
    //   ThArea2D * area = ( th_areas.back() );
    //   th_areas.pop_back();
    //   return area;
    // }
    // ThArea2D * areasBack() { return th_areas.back(); }
};

#endif

