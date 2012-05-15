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

class PlotLineItem; // forward

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
  int lsize;       //!< l-size (only slope and gradient)
  const char * adjust;

  ThLinePoint( double x, double y, QGraphicsItem * it )
    : ThPoint( x, y, it )
    , x1( x )
    , y1( y )
    , x2( x )
    , y2( y )
    , has1( false )
    , has2( false )
    , lsize( 0 )
    , adjust( NULL )
  { }

  void shift( double x0, double y0 );

  void setControl1( double x10, double y10 );

  void setControl2( double x20, double y20 );

  void setControls( double x20, double y20 );

  void clearControl1() { has1 = false; }

  void clearControl2() { has2 = false; }


  bool hasLPoption() const { return ! line_point_option.empty(); }
  const char * LPoption() const { return line_point_option.c_str(); }
  void setLPOption( const char * st ) { line_point_option = st; }


  int getLSize() const { return lsize; }
  void setLSize( int s ) { lsize = s; }

  const char * getAdjust() const { return adjust; }
  void setAdjust( const char * a ) { adjust = a; }

};

struct ThLineSegment
{
  ThLinePoint * pt1;     //!< start point
  ThLinePoint * pt2;     //!< end point
  PlotLineItem * item;  //!< line curve

  ThLineSegment( ThLinePoint * p1, ThLinePoint * p2, PlotLineItem * it = NULL )
    : pt1( p1 )
    , pt2( p2 )
    , item( it )
  { }

  void shift( double x0, double y0 );

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
    Therion::LineType line_type;   //!< line type
    const char * subtype;          //!< line subtype
    const char * outline;          //!< line scrap outline
    bool closed;                   //!< whether the line is closed
    bool reversed;                 //!< whether the line direction is reversed
    bool clipped;
    bool visible;
    const char * special_string;
    int special_value;
    std::vector< ThLinePoint * > pts;    //!< line control points
    std::vector< ThLineSegment * > sgms; //!< line segments
  
  public:
    /** cstr
     * @param t  line type (default THL_USER)
     */
    ThLine2D( Therion::LineType type = Therion::THL_USER )
      : line_type( type )
      , subtype( NULL )
      , outline( NULL )
      , closed( false )
      , reversed( false )
      , clipped( false )
      , visible( true )
      , special_string( NULL )
      , special_value( 0 )
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

    void shift( double dx, double dy );
  
  
    bool hasSubtype() const { return subtype != NULL; }
    const char * getSubtype() const { return subtype; }
    void setSubtype( const char * st ) { subtype = st; }
  
    /** accessor: get the number of control points
     * @return the size of the vector of points
     */
    size_t pointSize() const { return pts.size(); }
    size_t segmentSize() const { return sgms.size(); }

    ThLinePoint * lastPoint() { return pts.back(); }
    ThLineSegment * lastSegment() { return sgms.back(); }

    /** unclose the line
     */
    bool unclose();
  
    bool closeLine( PlotLineItem * sgm_item );
  
    /** check if the line is closed
     * @return true if the lin eis closed
     */
    bool isClosed() const { return closed; }
    void setClosed( bool c ) { closed = c; }

    bool isReversed() const { return reversed; }
    void setReversed( bool r ) { reversed = r; }

    bool isClipped() const { return clipped; }
    void setClipped( bool c ) { clipped = c; }

    bool isVisible() const { return visible; }
    void setVisible( bool v ) { visible = v; }

    bool hasSpecialString() const { return special_string != NULL; }
    const char * getSpecialString() const { return special_string; }
    void setSpecialString( const char * s ) { special_string = s; }

    bool hasSpecialValue() const { return special_value != 0; }
    int getSpecialValue() const { return special_value; }
    void setSpecialValue( int v ) { special_value = v; }

    void setOutline( const char * ot ) { outline = ot; }
    const char * getOutline() const { return outline; }

    /** append a point
     * @param x   point X coord.
     * @param y   point Y coord.
     * @return pointer to the last inserted point
     */
    void addLinePoint( ThLinePoint * p2, QGraphicsItem * pt_item, PlotLineItem * sgm_item );

    /** erase the last point and segment
     */
    void dropLast() 
    { 
      dropLastPoint();
      dropLastSegment();
    }

  private:
    void dropLastPoint();
    
    void dropLastSegment();

    void insertSegment( size_t s1, size_t s2, PlotLineItem * sgm_item );
  
  public:
    segment_iterator segmentBegin() { return sgms.begin(); }
    segment_iterator segmentEnd() { return sgms.end(); }

    point_iterator pointBegin() { return pts.begin(); }
    const_point_iterator pointBegin() const { return pts.begin(); }

    point_iterator pointEnd() { return pts.end(); }
    const_point_iterator pointEnd() const { return pts.end(); }

    ThLinePoint * operator[]( int k ) { return pts[k]; }

  public:
    static void computeCP( ThLinePoint * from, ThLinePoint * to,
                        double & x1, double & y1, double & x1c, double & y1c,
                        double & x2, double & y2, double & x2c, double & y2c );

};

#endif

