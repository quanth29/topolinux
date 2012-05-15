/** @file PlotThExport.cpp
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief plot export implementation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <sstream>

// #include "portability.h"

#include <QPainter>

#include "ArgCheck.h"

#define PERCENT_OFFSET 10 /* one tenth */
#define MIN_OFFSET 50 /* pixels */

#include "PlotScale.h"
#include "PlotStatus.h"
#include "DataList.h"
#include "ThPointType.h"
#include "ThLineType.h"
#include "ThAreaType.h"
#include "PlotThExport.h"
#include "ThPointType.h"
#include "ThLineType.h"
#include "ThAreaType.h"
#include "IconSet.h"


/** therion scrap names prefixes
 * @note indices must agree with modes defined in CanvasMode.h
 *
const char scrap_prefix[] = {
  'p', // plan
  's', // extended section
  'x', // cross section
  'n', // not used (3D)
  'h'  // horizontal cross section
};
 */

// int PlotThExport::scrap_count = 0;



void 
PlotThExport::exportTherion( const char * proj, PlotStatus * status, DataList * list )
{
  ARG_CHECK( proj == NULL, );
  ARG_CHECK( status == NULL, );
  ARG_CHECK( list == NULL, );

  const char * th2FileName = status -> getFileName();
  
  if ( strlen( th2FileName ) == 0 ) {
    DBG_CHECK("exportTherion() null filename\n");
    return;
  }
  
  // ------------------------------------------------------------------
  // line/area points are multiplied by ( TH_FACTOR / PLOT_SCALE )
  // points are multiplied by TH_FACTOR only as they are stored divided by PLOT_SCALE
  //
  double factor = TH_FACTOR/status->getScale();
  // const std::vector< ThLine2D * > & lines = status->th_lines;
  // const std::vector< ThPoint2D * > & pts  = status->th_points;
 
  FILE * fp = fopen( th2FileName, "w" );
  if ( fp == NULL ) {
    DBG_CHECK("exportTherion() cannot open file %s\n", th2FileName );
    return;
  }
  // only files with this header cxan be imported (need offsets)
  fprintf(fp, "#QTopo %s offset %d %d \n\n", th2FileName, status->getOffsetX(), status->getOffsetY() );

  fprintf(fp, "encoding  utf-8\n\n");

  std::vector< TherionScrap * >::const_iterator sit = status->scrapBegin();
  std::vector< TherionScrap * >::const_iterator sit_end = status->scrapEnd();
  for ( ; sit != sit_end; ++sit ) {
    TherionScrap * scrap = *sit;
    const char * scrap_name  = scrap->getScrapName();
    if ( strlen( scrap_name ) == 0 ) {
      DBG_CHECK("exportTherion() empty scrap name\n");
      continue;
    }
    fprintf(fp, "scrap %s -projection %s -scale [0 0 1 0 0.0 0.0 %.2f 0.0 m]\n",
      scrap_name, proj, factor );
   
    int cnt = 0;  
    for ( std::vector< ThArea2D * >::const_iterator it = scrap->areasBegin(), end = scrap->areasEnd();
          it != end; ++it ) {
      ThArea2D * ar = *it;
      if ( ! ar->IsFinished() ) continue;
      Therion::AreaType type = ar->Type();
      const char * visible = "";
      if ( ! ar->isBorderVisible() ) {
        visible = "-visibility off";
      }
      fprintf(fp, "\nline border -id %s-area-%02d -close on %s\n", scrap_name, cnt, visible );
      for ( ThArea2D::const_point_iterator pit = ar->Begin(); pit != ar->End(); ++pit ) {
        // FIXME control points
        ThLinePoint * pt = *pit;
        fprintf(fp, "  %.2f %.2f\n", pt->x*factor, -(pt->y*factor) );
      }
      ThLinePoint * pt = *(ar->Begin());
      fprintf(fp, "  %.2f %.2f\n", pt->x*factor, -(pt->y*factor) );
      fprintf(fp, "endline\n");
      fprintf(fp, "\narea %s\n", Therion::AreaName[type] );
      fprintf(fp, "  %s-area-%02d\n", scrap_name, cnt );
      fprintf(fp, "endarea\n");
      ++ cnt;
    }
    for ( std::vector< ThLine2D * >::const_iterator it = scrap->linesBegin(), end = scrap->linesEnd();
          it != end; ++it ) {
      ThLine2D * line = *it;
      // fprintf( stderr, "export line: sz %d %d cl %d rev %d clip %d vis %d \n",
      //    line->pointSize(), line->segmentSize(),
      //    line->isClosed(), line->isReversed(), line->isClipped(),
      //    line->isVisible() );
      // NOTE skip 0,1 point lines: otherwise therion-5.3 hangs up
      if ( line->pointSize() <= 1 ) continue; 
      fprintf(fp, "\nline %s", Therion::LineName[ line->type() ]);
      if ( line->hasSubtype() ) {
        fprintf(fp, ":%s", line->getSubtype() );
      }
      if ( line->isClosed() ) {
        fprintf(fp, " -close on" );
      } 
      if ( line->isReversed() ) {
        fprintf(fp, " -reverse on" );
      }
      if ( line->isClipped() ) {
        fprintf(fp, " -clip on" );
      }
      if ( ! line->isVisible() ) {
        fprintf(fp, " -visibility off" );
      }
      switch ( line->type() ) {
        case Therion::THL_ARROW:
        case Therion::THL_SLOPE:
        case Therion::THL_CONTOUR:
          if ( line->hasSpecialString() ) {
            fprintf(fp, " -%s %s",
              Therion::LineSpecialOptionName[ line->type() ],
              line->getSpecialString() );
          }
          break;
        case Therion::THL_PIT:
        case Therion::THL_WALL:
          if ( line->getSpecialValue() > 0 ) {
            fprintf(fp, " -%s %d",
              Therion::LineSpecialOptionName[ line->type() ],
              line->getSpecialValue() );
          }
          break;
        default:
          break;
      }
      fprintf(fp, "\n");
  
      ThLine2D::const_point_iterator pit = line->pointBegin();
      if ( pit != line->pointEnd() ) { // && size > 0
        ThLinePoint * pt0 = *pit;
        fprintf(fp, "  %.2f %.2f\n", pt0->x*factor, -(pt0->y*factor) );
        for ( ++pit; pit != line->pointEnd(); ++pit ) {
          ThLinePoint * pt = *pit;
          // FIXME control-points
          if ( pt0->has2 && pt->has1 ) {
            fprintf(fp, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
              pt0->x2*factor, -(pt0->y2*factor),
              pt->x1*factor, -(pt->y1*factor),
              pt->x*factor, -(pt->y*factor) );
          } else if ( pt0->has2 ) {
            double dx = pt->x - pt0->x;
            double dy = pt->y - pt0->y;
            double dx2 = pt0->x2 - pt0->x;
            double dy2 = pt0->y2 - pt0->y;
            double c = (dx*dx2 + dy*dy2)/(dx*dx + dy*dy);
            double x1 = pt->x + dx2 - 2 * dx * c;
            double y1 = pt->y + dy2 - 2 * dy * c;
            fprintf(fp, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
              pt0->x2*factor, -(pt0->y2*factor),
              x1*factor, -(y1*factor),
              pt->x*factor, -(pt->y*factor) );
          } else if ( pt->has1 ) {
            double dx = pt0->x - pt->x;
            double dy = pt0->y - pt->y;
            double dx1 = pt->x1 - pt->x;
            double dy1 = pt->y1 - pt->y;
            double c = (dx*dx1 + dy*dy1)/(dx*dx + dy*dy);
            double x2 = pt0->x + dx1 - 2 * dx * c;
            double y2 = pt0->y + dy1 - 2 * dy * c;
            fprintf(fp, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
              x2*factor, -(y2*factor),
              pt->x1*factor, -(pt->y1*factor),
              pt->x*factor, -(pt->y*factor) );
          } else {
            fprintf(fp, "  %.2f %.2f\n", pt->x*factor, -(pt->y*factor) );
          }
          if ( pt->hasLPoption() ) {
            fprintf(fp, "%s\n", pt->LPoption() );
          }
          pt0 = pt;
        }
      }
      fprintf(fp, "endline\n\n");
    }
    for ( std::vector< ThPoint2D * >::const_iterator pit = scrap->pointsBegin(), end = scrap->pointsEnd();
          pit != end; ++pit ) {
      ThPoint2D * pt = *pit;
      Therion::PointType type = pt->type();
      double x = pt->x * TH_FACTOR;
      double y = -( pt->y * TH_FACTOR );
      const char * name = Therion::PointName[ type ];
      fprintf(fp, "point %.2f %.2f %s", x, y, name );
      if ( pt->hasText() ) { // name / text
        if ( type == Therion::THP_STATION ) {
          fprintf(fp, " -name %s", pt->text() ); 
        } else {
          fprintf(fp, " -text \"%s\"", pt->text() ); 
        }
      }
      switch ( type ) { // orientation
        case Therion::THP_WATER_FLOW:
        case Therion::THP_AIR_DRAUGHT:
        case Therion::THP_ENTRANCE:
        case Therion::THP_LABEL:
          fprintf(fp, " -orient %d", pt->orientation() * ORIENTATION_UNITS);
          break;
        default:
          break;
      }
      if ( pt->hasSubtype() ) { // subtype
        fprintf(fp, " -subtype %s", pt->subtype() );
      }
      if ( pt->hasAlign() ) { // align
        fprintf(fp, " -align %s", pt->align() );
      }
      if ( pt->hasScale() ) { // scale
        fprintf(fp, " -scale %s", pt->scale() );
      }
      if ( pt->hasOption() ) { // other options
        fprintf(fp, " %s", pt->option() );
      } 
      if ( type == Therion::THP_STATION ) {
        // const char * name = pt->option() + 6; // skip "-name "
        const char * name = pt->text();
        if ( list->hasStationComment( name ) ) {
          fprintf(fp, "\n# %s", list->getStationComment( name ) ); 
        }
      }
      fprintf(fp, "\n\n" );
    }
    fprintf(fp, "endscrap\n\n");
  }

  fclose( fp );
  DBG_CHECK("exportTherion()doSaveTh2 written file %s\n", th2FileName );
}

// ------------------------------------------------------------------
// EXPORT PNG

void 
PlotThExport::exportImage( PlotStatus * status, DataList * list )
{
  ARG_CHECK( status == NULL, );
  ARG_CHECK( list == NULL, );
  IconSet * icon = IconSet::Get();

  const char * imgFileName = status->getImageName();

  fprintf(stderr, "Saving PNG file <<%s>>\n", imgFileName );
  
  if ( strlen( imgFileName ) == 0 ) {
    DBG_CHECK("exportImage() null filename\n");
    return;
  }

  // ------------------------------------------------------------------
  // double factor = TH_FACTOR/status->getScale();
  // const std::vector< ThLine2D * > & lines = status->th_lines;
  // const std::vector< ThPoint2D * > & pts  = status->th_points;

  int width  = status->getWidth()  * PLOT_SCALE;
  int height = status->getHeight() * PLOT_SCALE;
  // fprintf(stderr, "Image width %d height %d\n", width, height );

  QImage image( width, height, QImage::Format_RGB32 );
  QPainter painter( &image );
  image.fill( 0xffffffff );
  painter.setBackground( QBrush( Qt::gray ) );
  painter.begin( &image );

  std::vector< TherionScrap * >::const_iterator sit = status->scrapBegin();
  std::vector< TherionScrap * >::const_iterator sit_end = status->scrapEnd();
  for ( ; sit != sit_end; ++sit ) {
    TherionScrap * scrap = *sit;
  
    painter.setPen( Qt::black );
    for ( std::vector< ThArea2D * >::const_iterator it = scrap->areasBegin(), end = scrap->areasEnd();
          it != end; ++it ) {
      ThArea2D * ar = *it;
      if ( ! ar->IsFinished() ) continue;
      Therion::AreaType type = ar->Type();
      switch ( type ) {
        case Therion::THA_CLAY:
          painter.setBackground( QBrush( Qt::darkYellow ) );
          break;
        case Therion::THA_DEBRIS:
          painter.setBackground( QBrush( Qt::darkGray ) );
          break;
        case Therion::THA_FLOWSTONE:
          painter.setBackground( QBrush( Qt::darkYellow ) );
          break;
        case Therion::THA_ICE:
          painter.setBackground( QBrush( Qt::lightGray ) );
          break;
        case Therion::THA_PEBBLES:
          painter.setBackground( QBrush( Qt::gray ) );
          break;
        case Therion::THA_SAND:
          painter.setBackground( QBrush( Qt::yellow ) );
          break;
        case Therion::THA_SNOW:
          painter.setBackground( QBrush( Qt::gray ) );
          break;
        case Therion::THA_USER:
          painter.setBackground( QBrush( Qt::yellow ) );
          break;
        case Therion::THA_WATER:
          painter.setBackground( QBrush( Qt::cyan ) );
          break;
        case Therion::THA_PLACEMARK:
          break;
      }
      size_t size = ar->Size();
      if ( size <= 1 ) continue; 
      QPointF pts[ size ];
      int k = 0;
      for ( ThArea2D::const_point_iterator pit = ar->Begin(); pit != ar->End(); ++pit, ++k ) {
        ThLinePoint * pt = *pit;
        double x = status->evalToX( pt->x ) / TH_FACTOR;
        double y = status->evalToY( pt->y ) / TH_FACTOR;
        pts[k] = QPointF( x, y );
        // fprintf(stderr, "draw area at %.2f %.2f\n", x, y );
      }
      painter.drawPolygon( pts, size ); // draw area border
    }

    for ( std::vector< ThLine2D * >::const_iterator it = scrap->linesBegin(), end = scrap->linesEnd();
          it != end; ++it ) {
      // NOTE skip 0,1 point lines: otherwise therion-5.3 hangs up
      size_t size = (*it)->pointSize();
      if ( size <= 1 ) continue; 
      Therion::LineType type = (*it)->type();
      switch ( type ) {
        case Therion::THL_ARROW:
          painter.setPen( Qt::blue );
          break;
        case Therion::THL_BORDER:
          painter.setPen( Qt::gray );
          break;
        case Therion::THL_CHIMNEY:
        case Therion::THL_PIT:
          painter.setPen( Qt::magenta );
          break;
        case Therion::THL_USER:
          painter.setPen( Qt::green );
          break;
        case Therion::THL_WALL:
          painter.setPen( Qt::black );
          break;
        case Therion::THL_ROCK:
          painter.setPen( Qt::gray );
          break;
        case Therion::THL_SLOPE:
          painter.setPen( Qt::black );
          break;
        case Therion::THL_CONTOUR:
          painter.setPen( Qt::black );
          break;
        case Therion::THL_PLACEMARK:
          break;
      }
      ThLine2D::const_point_iterator pit = (*it)->pointBegin();
      ThLinePoint * pt = (*pit);
      double x0 = status->evalToX( pt->x ) / TH_FACTOR;
      double y0 = status->evalToY( pt->y ) / TH_FACTOR;
      for ( ++pit; pit != (*it)->pointEnd(); // && size > 0;
            ++pit /* , --size */ ) {
        pt = *pit;
        double x = status->evalToX( pt->x ) / TH_FACTOR;
        double y = status->evalToY( pt->y ) / TH_FACTOR;
        painter.drawLine( x0, y0, x, y );
        // fprintf(stderr, "draw line at %.2f %.2f %.2f %.2f\n", x0, y0, x, y );
        x0 = x;
        y0 = y;
      }
    }

    for ( std::vector< ThPoint2D * >::const_iterator pit = scrap->pointsBegin(), end = scrap->pointsEnd();
          pit != end; ++pit ) {
      ThPoint2D * pt = *pit;
      Therion::PointType type = pt->type();
      // FIXME why need PLOT_SCALE (4) ?
      double x = status->evalToX( PLOT_SCALE * pt->x ) / TH_FACTOR ;
      double y = status->evalToY( PLOT_SCALE * pt->y ) / TH_FACTOR ;
      // printf(stderr, "draw point at %.2f %.2f\n", x, y );
      switch ( type ) {
        case Therion::THP_AIR_DRAUGHT:
        case Therion::THP_ENTRANCE:
        case Therion::THP_WATER_FLOW:
          // FIXME rotate( pt->orientation() * ORIENTATION_UNITS );
        case Therion::THP_ANCHOR:
        case Therion::THP_BLOCKS:
        case Therion::THP_BREAKDOWN_CHOKE:
        case Therion::THP_CLAY:
        case Therion::THP_CLAY_CHOKE:
        case Therion::THP_CRYSTAL:
        case Therion::THP_CURTAIN:
        case Therion::THP_DEBRIS:
        case Therion::THP_DIG:
        case Therion::THP_FLOWSTONE:
        case Therion::THP_FLOWSTONE_CHOKE:
        case Therion::THP_HELICTITE:
        case Therion::THP_ICE:
        case Therion::THP_NARROW_END:
        case Therion::THP_PEBBLES:
        case Therion::THP_PILLAR:
        case Therion::THP_POPCORN:
        case Therion::THP_SAND:
        case Therion::THP_SNOW:
        case Therion::THP_STALACTITE:
        case Therion::THP_STALAGMITE:
        case Therion::THP_USER:
        case Therion::THP_STATION:
          #ifdef HAS_THP_ICONS
            // 12, 12 is half the pixmaps size
            painter.drawPixmap( x-12, y-12, icon->ThpPixmap( type ) );
          #else
            {
              // QPolygon poly( icon->ThpSymbol1( type ) );
              // poly.translate( x, y );
              // painter.drawPolygon( poly );
              QPainterPath path( icon->ThpSymbol2( type ) );
              painter.translate( x, y );
              if ( type == Therion::THP_AIR_DRAUGHT 
                || type == Therion::THP_ENTRANCE
                || type == Therion::THP_WATER_FLOW ) {
                painter.rotate( pt->orientation() * ORIENTATION_UNITS );
                painter.drawPath( path );
                painter.rotate( -pt->orientation() * ORIENTATION_UNITS );
              } else {
                painter.drawPath( path );
              }
              painter.translate( -x, -y );
            }
          #endif
          break;
        case Therion::THP_LABEL:
          // FIXME rotate( pt->orientation() * ORIENTATION_UNITS );
          painter.drawText( x, y, pt->text() );
          break;
        case Therion::THP_CONTINUATION:
          painter.drawText( x, y, "?" );
          break;
        case Therion::THP_PLACEMARK:
          break;
      }
    }
  }
  painter.end();
  image.save( imgFileName );
  
}
