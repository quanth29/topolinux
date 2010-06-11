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

#include <vector>

#include "ThLineType.h"
#include "TherionPoint.h"

/** plot therion line
 */
struct ThLine 
{
  typedef std::vector< ThPoint >::iterator iterator;
  typedef std::vector< ThPoint >::const_iterator const_iterator;
 
  ThLineType type;            //!< line type
  bool closed;                //!< whether the line is closed
  std::vector< ThPoint > pts; //!< line control points

  /** cstr
   * @param t  line type (default THL_USER)
   */
  ThLine( ThLineType t = THL_USER )
    : type( t )
    , closed( false )
  { }

  /** accessor: get the number of control points
   * @return the size of the vector of points
   */
  size_t Size() const { return pts.size(); }

  /** set the line close
   * @param close   value of closed
   */
  void setClosed( bool close ) { closed = close; }

  /** check if the line is closed
   * @return true if the lin eis closed
   */
  bool isClosed() const { return closed; }

  /** append a point
   * @param x   point X coord.
   * @param y   point Y coord.
   */
  void Add( double x, double y ) { pts.push_back( ThPoint(x,y) ); }

  /** erase the last point;
   */
  void DropLast() 
  { 
    iterator it = pts.end();
    --it;
    pts.erase( it );
  }

  iterator Begin() { return pts.begin(); }
  const_iterator Begin() const { return pts.begin(); }

  iterator End() { return pts.end(); }
  const_iterator End() const { return pts.end(); }

  ThPoint & operator[]( int k ) { return pts[k]; }

};

#endif

