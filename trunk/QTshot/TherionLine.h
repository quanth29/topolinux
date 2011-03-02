/** @file TherionLine.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot therion line
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef THERION_LINE_H
#define THERION_LINE_H

#include <math.h> // fabs
#include <vector>

#include "ThLineType.h"
#include "TherionPoint.h"

/** A line point.
 * A line point vcan have two control points, Point-1 before it and Point-2
 * after it. When saving the therion file the control points should be used
 * as follow:
 *   - the first point has no control points, so write
 *     x0 y0
 *   - if the previous point or the current point have control points write
 *     x2_prev y2_prev x1 y1 x0, y0
 *     (in case one of the two points does not have control points, use the
 *     point coords for the control point coords)
 *   - if neither the previous point nor the current point have control points write
 *     x0 y0
 * FIXME i have to find out how to write for closed lines at start/end
 */
struct ThLinePoint : public ThPoint
{
  double x1, y1;   //!< first control point
  double x2, y2;   //!< second control point
  bool has1, has2; //!< whether this point has control points
  std::string line_point_option; //!< line-point subtype

  ThLinePoint( double x, double y, QGraphicsItem * it )
    : ThPoint( x, y, it )
    , has1( false )
    , has2( false )
  { }

  void shift( double x0, double y0 )
  {
    x  += x0;
    y  += y0;
    x1 += x0;
    y1 += y0;
    x2 += x0;
    y2 += y0;
  }

  void setControl1( double x10, double y10 )
  {
    if ( fabs(x10-x) > 2.0 || fabs(y10-y) > 2.0 ) {
      x1 = x10;
      y1 = y10;
      has1 = true;
    }
  }

  void setControl2( double x20, double y20 )
  {
    if ( fabs(x20-x) > 2.0 || fabs(y20-y) > 2.0 ) {
      x2 = x20;
      y2 = y20;
      has2 = true;
    }
  }

  void setControls( double x20, double y20 )
  {
    if ( fabs(x20-x) > 2.0 || fabs(y20-y) > 2.0 ) {
      x1 = 2*x - x20;
      y1 = 2*y - y20;
      x2 = x20;
      y2 = y20;
      has1 = has2 = true;
      printf("LP %.2f %.2f %.2f %.2f %.2f %.2f\n", x, y, x1, y1, x2, y2 );
    }
  }

  void clearControl1() { has1 = false; }

  void clearControl2() { has2 = false; }

  const char * LPoption() const { return line_point_option.c_str(); }

  void setLPOption( const char * st ) { line_point_option = st; }

};

struct ThLineSegment
{
  ThLinePoint * pt1;  //!< start point
  ThLinePoint * pt2;  //!< end point
  QGraphicsLineItem * item; 

  ThLineSegment( ThLinePoint * p1, ThLinePoint * p2, QGraphicsLineItem * it = NULL )
    : pt1( p1 )
    , pt2( p2 )
    , item( it )
  { }

};

/** plot therion line
 */
struct ThLine2D
{
  public:
    typedef std::vector< ThLineSegment * >::iterator segment_iterator;

    typedef std::vector< ThLinePoint * >::iterator point_iterator;
    typedef std::vector< ThLinePoint * >::const_iterator const_point_iterator;
 
  private:
    Therion::LineType line_type;            //!< line type
    bool closed;                //!< whether the line is closed
    std::vector< ThLinePoint * > pts;    //!< line control points
    std::vector< ThLineSegment * > sgms; //!< line segments
  
  public:
    /** cstr
     * @param t  line type (default THL_USER)
     */
    ThLine2D( Therion::LineType type = Therion::THL_USER )
      : line_type( type )
      , closed( false )
    { }
  
    ~ThLine2D()
    {
      size_t k0 = pts.size();
      for ( size_t k=0; k<k0; ++k) delete pts[k];
      pts.clear();
      k0 = sgms.size();
      for ( size_t k=0; k<k0; ++k) delete sgms[k];
      sgms.clear();
    }

    Therion::LineType type() const { return line_type; }
  
    /** accessor: get the number of control points
     * @return the size of the vector of points
     */
    size_t pointSize() const { return pts.size(); }
    size_t segmentSize() const { return sgms.size(); }

    ThLinePoint * lastPoint() { return pts.back(); }
    ThLineSegment * lastSegment() { return sgms.back(); }

    /** unclose the line
     */
    bool unclose()
    {
      if ( closed ) { // drop last segment
        dropLastSegment(); // FIX-2
        closed = false;
      }
      return closed;
    }
  
    bool close( QGraphicsLineItem * sgm_item )
    {
      if ( ! closed ) {
        size_t s = pts.size();
        if ( s > 0 ) {
          insertSegment(s-1, 0, sgm_item);
          closed = true;
        }
      }
      return closed;
    }
  
    /** check if the line is closed
     * @return true if the lin eis closed
     */
    bool isClosed() const { return closed; }

    /** append a point
     * @param x   point X coord.
     * @param y   point Y coord.
     * @return pointer to the last inserted point
     */
    ThLinePoint * addLinePoint( double x, double y, QGraphicsItem * pt_item, QGraphicsLineItem * sgm_item )
    { 
      ThLinePoint * p2 = new ThLinePoint( x, y, pt_item );
      pts.push_back( p2 );
      if ( sgm_item ) {
        size_t s = pts.size();
        insertSegment(s-2, s-1, sgm_item);
      }
      return p2;
    }

    /** erase the last point and segment
     */
    void dropLast() 
    { 
      dropLastPoint();
      dropLastSegment();
    }

  private:
    void dropLastPoint()
    {
      if ( pts.size() == 0 ) return;
      ThLinePoint * pt = pts.back();
      delete pt;
      pts.pop_back();
    }
    
    void dropLastSegment()
    {
      if ( sgms.size() == 0 ) return;
      ThLineSegment * sgm = sgms.back();
      delete sgm;
      sgms.pop_back(); // erase  from vector
    }

    void insertSegment( size_t s1, size_t s2, QGraphicsLineItem * sgm_item )
    {
      ThLinePoint * p2 = pts[s1];
      ThLinePoint * p1 = pts[s2];
      ThLineSegment * sgm = new ThLineSegment( p1, p2, sgm_item );
      sgms.push_back( sgm );
    }

  
  public:
    segment_iterator segmentBegin() { return sgms.begin(); }
    segment_iterator segmentEnd() { return sgms.end(); }

    point_iterator pointBegin() { return pts.begin(); }
    const_point_iterator pointBegin() const { return pts.begin(); }

    point_iterator pointEnd() { return pts.end(); }
    const_point_iterator pointEnd() const { return pts.end(); }

    ThLinePoint * operator[]( int k ) { return pts[k]; }

};

#endif

