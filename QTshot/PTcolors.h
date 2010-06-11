/** @file PTcolors.h
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief match between PocketTopo colors and therion lines and points
 *
 */
#ifndef PT_COLORS_H
#define PT_COLORS_H

#include <ctype.h>
#include <stdlib.h>

/** number of PocketTopo colors
 */
#define PT_COLORS 7

#include "ThPointType.h"
#include "ThLineType.h"

class PTcolors
{
  private:
    int point[ PT_COLORS ];
    int line[ PT_COLORS ];

  public:
    PTcolors()
    {
      for (int i=0; i<PT_COLORS; ++i ) {
        point[i] = i;
        line[i] = i;
      }
    }
  
    /** set the map colors-therion_point_types
     * @param points    string listing the therion_points indices (@see ThPointType.h)
     */ 
    void setPoints( const char * points )
    {
      if ( points == NULL ) return;
      const char * ch = points;
      for (int i=0; i<PT_COLORS; ++i ) {
        while( *ch && isspace(*ch) ) ++ch;
        if ( *ch == 0 || ! isdigit(*ch) ) break;
        point[i] = '0' + *ch;
        ++ch;
        while ( *ch != 0 && isdigit(*ch) ) {
          point[i] = 10*point[i] + ('0' + *ch);
        }
      }
    }

  
    /** set the map colors-therion_line_types
     * @param points    string listing the therion_lines indices (@see ThLineType.h)
     */ 
    void setLines( const char * lines )
    {
      if ( lines == NULL ) return;
      const char * ch = lines;
      for (int i=0; i<PT_COLORS; ++i ) {
        while( *ch && isspace(*ch) ) ++ch;
        if ( *ch == 0 || ! isdigit(*ch) ) break;
        line[i] = '0' + *ch;
        ++ch;
        while ( *ch != 0 && isdigit(*ch) ) {
          line[i] = 10*line[i] + ('0' + *ch);
        }
      }
    }


    /** get the therion-point index for a given color
     * @param k    color index (as in PocketTopo)
     * @return the therion point index
     */
    ThPointType thPoint( size_t k ) const
    {
      if ( k >= PT_COLORS ) return THP_PLACEMARK;
      return (ThPointType)point[k];
    }

    /** get the therion-line index for a given color
     * @param k    color index (as in PocketTopo)
     * @return the therion line index
     */
    ThLineType thLine( size_t k ) const
    {
      if ( k >= PT_COLORS ) return THL_PLACEMARK;
      return (ThLineType)line[k];
    }

};

#endif

