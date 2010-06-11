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

#include "TherionPoint.h"
#include "TherionLine.h"

#ifdef HAS_POCKETTOPO
  #include "PTfile.h"
#endif

#define UNDO_POINT     0
#define UNDO_LINEPOINT 1
#define UNDO_ENDLINE   2
#define UNDO_CLOSELINE 3

/** undo struct
 * LIFO list of undo-commands
 * the undo are inserted at the head of the list and removed from the head
 */
struct CanvasUndo
{
  int command;              //!< commandtype: 0 point, 1 line-point, 2 end-line
  struct CanvasUndo * next; //!< link
 
  public:
    /** cstr
     * @param cmd   command type
     * @param n     next canvas-undo
     *
     * @note the cstr puts the new CanvasUndo at the head of th list of undo's
     */
    CanvasUndo( int cmd, CanvasUndo * n )
      : command( cmd )
      , next( n )
    { }

    /** get the next undo
     * @return the next undo
     */
   CanvasUndo * Next() { return next; }

  
};

struct PlotStatus
{
    std::vector< ThPoint2D > pts;
    std::vector< ThLine * >  lines;
  private:
    double scale;
    int    offx;
    int    offy;
    int    width;          //!< canvas width
    int    height;         //!< canvas height
    // int    orientation;    //!< orientation 0..7 [units of 45 degrees]
    double theta, phi;     //!< viewpoint (for 3D)
    CanvasUndo * undo;     //!< list of undo's
    bool numbers;          //!< whether to display station numbers
    int  grid_spacing;     //!< grid spacing

  public:
    PlotStatus()
      : scale( 4.0 )
      , offx( 0 )
      , offy( 0 )
      , width( 1 )
      , height( 1 )
      // , orientation( 0 )
      , theta( 0.0 )  // from north (0 azymuth) on the horizontal plane
      , phi( 0.0 )
      , undo( NULL )
      , numbers( true )
      , grid_spacing( 1 ) // 1 meter
    { }

    /** check if there are drawing items
     * @return true if there are drawing items
     */
    bool hasItems()
    {
      return pts.size() > 0 || lines.size() > 0;
    }

    void Reset()
    {
      scale = 4.0;
      offx = 0;
      offy = 0;
      numbers = true;
      Clear();
    }

    void Clear() 
    {
      pts.clear();
      std::vector< ThLine * >::iterator end = lines.end();
      for ( std::vector< ThLine * >::iterator it = lines.begin();
            it != end;
            ++it ) {
        delete (*it);
      }
      lines.clear();
      ClearUndos();
    }

    /** clear list of undo's
     */
    void ClearUndos()
    {
      while ( this->undo ) {
        CanvasUndo * next = undo->Next();
        delete this->undo;
        this->undo = next;
      }
    }

    /** append an undo to the undo list
     * @param cmd undo command
     */
    void AddUndo( int cmd )
    {
      this->undo = new CanvasUndo( cmd, this->undo );
    }

    CanvasUndo * PopUndo()
    {
      CanvasUndo * ret = undo;
      if ( undo ) undo = undo->Next();
      return ret;
    }

    /** check if there are undos
     * @return true is the undo list is not empty
     */
    bool HasUndo() const { return undo != NULL; }

    void ShiftItems( int dx, int dy )
    {
      // if ( do_debug ) 
      // fprintf(stderr, "ShiftItems: lines %d points %d DX %d DY %d\n",
      //   lines.size(), pts.size(), dx, dy );

      for ( std::vector< ThPoint2D >::iterator it = pts.begin(), end = pts.end(); it != end; ++it ) {
        it->x += dx;
        it->y += dy;
      }
      for ( std::vector< ThLine * >::iterator lit = lines.begin(), lend = lines.end(); lit != lend; ++lit ) {
        for ( ThLine::iterator it = (*lit)->Begin(), end = (*lit)->End(); it != end; ++it ) {
          it->x += dx;
          it->y += dy;
        }
      }
    }

    /** get theta (azimuth)
     * @return theta
     */
    double Theta() const { return theta; }

    /** get phi 
     * @return phi
     */
    double Phi() const { return phi; }

    void AddTheta( double t ) 
    { 
      theta += t;
      if ( theta > 90.0 ) theta = 90.0;
      if ( theta < -90.0 ) theta = -90.0;
    }

    void AddPhi( double p ) 
    {
      phi += p; 
      if ( phi >= 360.0 ) phi -= 360.0;
      if ( phi < 0.0 ) phi += 360.0;
    }

    int OffsetX() const { return offx; }
    int OffsetY() const { return offy; }

    void SetOffsetX( int x ) { offx = x; }
    void SetOffsetY( int y ) { offy = y; }

    int Width() const { return width; }
    int Height() const { return height; }
    
    void SetWidth( int w ) { width = w; }
    void SetHeight( int h ) { height = h; }

    double Scale() const { return scale; }
    void SetScale( double s ) { scale = s; }

    void FlipNumbers() { numbers = ! numbers; }
    bool IsNumbers() const { return numbers; }

  #ifdef HAS_POCKETTOPO
    /** export drawing to a pockettopo file
     * @param drawing  PocketTopo drawing in the pockettopo file
     */
    void exportPTfile( PTdrawing & drawing );
  #endif

};

#endif

