/** @file TherionPoint.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot therion point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef THERION_POINT_H
#define THERION_POINT_H

#include <string>
#include <sstream>

#include <QGraphicsItem>

#include "ThPointType.h"

#define ORIENTATION_UNITS 10 // 45
#define ORIENTATION_HALF   5 // 22
#define ORIENTATION_MAX   36 //  8 /** 360 / ORIENTATION_UNITS */

/** therion point
 */
struct ThPoint
{
  double x;              //!< x plot coord
  double y;              //!< y plot coord
  QGraphicsItem * item;  //!< graphics item displaying this point

  ThPoint( double x0 = 0.0, double y0 = 0.0, QGraphicsItem * it = NULL )
    : x( x0 )
    , y( y0 )
    , item( it )
  { }

  /** shift the point coordiantes 
   * @param x0   X shift
   * @param y0   Y shift
   */
  void shift( double x0, double y0 )
  {
    x += x0;
    y += y0;
    item->moveBy( x0*4, y0*4 );
  }

  /** get the point graphics item
   * @return the point graphics item
   */
  QGraphicsItem * getItem() { return item; }

};

/** plot therion point
 */
class ThPoint2D : public ThPoint
{
  private:
    Therion::PointType point_type;  //!< point type
    int point_orientation;          //!< point orientation
    std::string point_option;       //!< point option string
    std::string point_text;         //!< point text / name
    const char * point_subtype;     //!< point subtype
    const char * point_align;       //!< point align
    const char * point_scale;       //!< point scale

  public:
    ThPoint2D( double x0 = 0.0, double y0 = 0.0, 
               Therion::PointType type = Therion::THP_USER, 
               int orient = 0,
               const char * opt = NULL )
      : ThPoint( x0, y0 )
      , point_type( type )
      , point_orientation( 0 ) // default 0, only certain points have orient.
      , point_subtype( NULL )
      , point_align( NULL )
      , point_scale( NULL )
    {
      switch (type) {
        case Therion::THP_AIR_DRAUGHT:
        case Therion::THP_WATER_FLOW:
        case Therion::THP_ENTRANCE:
        case Therion::THP_LABEL:
          point_orientation = orient;
          break;
        default:
          break;
      }
      if ( opt ) point_option = opt;
    } 

    /** get the point type
     * @return the point type 
     */
    Therion::PointType type() const { return point_type; }

    /** get the point orientation
     * @return the point orientation (as int)
     */
    int orientation() const { return point_orientation; }
    void setOrientation( int orient ) { point_orientation = orient; }

    /** get the point text/name
     * @return the point text (or name) string
     */
    const char * text() const { return point_text.c_str(); }
    void setText( const char * text ) { point_text = text; }
    bool hasText() const { return point_text.size() > 0; }

    /** get the point options
     * @return the point option string
     */
    const char * option() const { return point_option.c_str(); }
    void setOption( const char * option ) { point_option = option; }
    bool hasOption() const { return point_option.size() > 0; }

    /** append a key-value pair to the option string
     */
    void appendOption( const char * key, const char * value )
    {
      std::ostringstream oss;
      if ( ! point_option.empty() ) {
        oss << point_option << " ";
      }
      oss << key << " " << value;
      point_option = oss.str();
    }

    /** get the point subtype
     * @return the point subtype string
     */
    const char * subtype() const { return point_subtype; }
    void setSubtype( const char * st ) { point_subtype = st; }
    bool hasSubtype() const { return point_subtype != NULL; }

    /** get the point align
     * @return the point align string
     */
    const char * align() const { return point_align; }
    void setAlign( const char * al ) { point_align = al; }
    bool hasAlign() const { return point_align != NULL; }

    /** get the point scale
     * @return the point scale string
     */
    const char * scale() const { return point_scale; }
    void setScale( const char * sc ) { point_scale = sc; }
    bool hasScale() const { return point_scale != NULL; }

};

#endif
