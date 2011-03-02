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
      if ( type == Therion::THA_USER ) {
        visible = "-visibility off";
      }
      fprintf(fp, "\nline border -id %s-area-%02d -close on %s\n", scrap_name, cnt, visible );
      for ( ThArea2D::const_point_iterator pit = ar->Begin(); pit != ar->End(); ++pit ) {
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
      // size_t size = (*it)->Size();
      // NOTE skip 0,1 point lines: otherwise therion-5.3 hangs up
      if ( (*it)->pointSize() <= 1 ) continue; 
      if ( (*it)->isClosed() ) {
        fprintf(fp, "\nline %s -close on\n", Therion::LineName[ (*it)->type() ]);
      } else {
        fprintf(fp, "\nline %s\n", Therion::LineName[ (*it)->type() ]);
      }
  
      for ( ThLine2D::const_point_iterator pit = (*it)->pointBegin();
            pit != (*it)->pointEnd(); // && size > 0;
            ++pit /* , --size */ ) {
        ThLinePoint * pt = *pit;
        fprintf(fp, "  %.2f %.2f\n", pt->x*factor, -(pt->y*factor) );
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

#ifdef IMPORT_TH2
/*
 * On input from th2 file
 */
bool
importTh2( const char * filename )
{
  TherionScrap * scrap = NULL;
  TherionPoint * point;
  TherionLine  * line;
  TherionLinePoint * lp;
  // TherionArea  * area;
  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) return false;
  size_t n = 256;
  char * line = (char *)malloc( n );
  while ( getline( &line, &n, fp ) >= 0 ) {
    Therion::parser( line, fp );
    const char * item = parser.get();
    if ( strcmp( item, "scrap" ) == 0 ) {
      scrap = new TherionScrap();
      // TODO insert scrap
    } else if ( strcmp( item, "point" ) == 0 ) {
      point = new ThPoint2D( ... );
      // TODO add point to scrap
    } else if ( strcmp( item, "line" ) == 0 ) {
      line = new ThLine2D( ... );
      // TODO add line to scrap
    } else if ( strcmp( item, "endline" ) == 0 ) {
      line = NULL;
    } else if ( strcmp( item, "endarea" ) == 0 ) {
      area = NULL;
    } else if ( strcmp( item, "endscrap" ) == 0 ) {
      scrap = NULL;
    } else if ( line != NULL ) {
      lp = new ThLinePoint( ... );
      // TODO insert point in line
    } else if ( area != NULL ) {
      lp = new ThLinePoint( ... );
      // TODO insert point in area-line
    }
  }
  free( line );
 * having read
 *  - point type
 *  - x, y
 *  - options
 *
   int i=0;
   for ( ; i<Therion::THP_PLACEMARK; ++i) {
     if ( type == Therion::PointType[i] ) break;
   }
   if ( i == Therion::THP_PLACEMARK ) i = Therion::THP_USER;
   ThPoint2D * pt = new ThPoint2D( x / TH_FACTOR, y / TH_FACTOR, (Therion::PointType)(i) );
   if ( align ) {
     pt -> setAlign( ... );
   }
   if ( scale ) {
     pt -> setScale( ... );
   }
   ...

 * having read: 
 *   - line type
     ThLine2D * line = new TherionLine( (Therion::LineType)(i) );
     if ( closed ) {
       line->setClosed( true );
 * insert line point
     ThLinePoint * lp1 = new ThLinePoint( x * PLOT_SCALE/TH_FACTOR, ... );
     line->points.push_back( lp1 );
 * while not "endline"
 *   insert line point
       ThLinePoint * lp2 = new ThLinePoint( x * PLOT_SCALE/TH_FACTOR, ... );
       line->points.push_back( lp1 );
       ThLineSegment * sgm = new ThLineSegment( lp1, lp2 );
       line->segments.push_bach( sgm );
       lp1 = lp2;
#endif


void 
PlotThExport::exportImage( PlotStatus * status, DataList * list )
{
  ARG_CHECK( status == NULL, );
  ARG_CHECK( list == NULL, );

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
        case Therion::THA_WATER:
          painter.setBackground( QBrush( Qt::cyan ) );
          break;
        case Therion::THA_SNOW:
        case Therion::THA_ICE:
          painter.setBackground( QBrush( Qt::lightGray ) );
          break;
        case Therion::THA_USER:
          painter.setBackground( QBrush( Qt::yellow ) );
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
      painter.drawPolygon( pts, size );
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
      double x = status->evalToX( PLOT_SCALE * pt->x ) / TH_FACTOR;
      double y = status->evalToY( PLOT_SCALE * pt->y ) / TH_FACTOR;
      // printf(stderr, "draw point at %.2f %.2f\n", x, y );
      painter.setPen( Qt::black );
      switch ( type ) {
        case Therion::THP_AIR_DRAUGHT:
          painter.setPen( Qt::cyan );
          break;
        case Therion::THP_BLOCKS:
          painter.setPen( Qt::black );
          break;
        case Therion::THP_CLAY:
          painter.setPen( Qt::yellow );
          break;
        case Therion::THP_DEBRIS:
          painter.setPen( Qt::darkYellow );
          break;
        case Therion::THP_PEBBLES:
          painter.setPen( Qt::gray );
          break;
        case Therion::THP_LABEL:
          painter.setPen( Qt::gray );
          break;
        case Therion::THP_SAND:
          painter.setPen( Qt::gray );
          break;
        case Therion::THP_SNOW:
          painter.setPen( Qt::lightGray );
          break;
        case Therion::THP_ICE:
          painter.setPen( Qt::lightGray );
          break;
        case Therion::THP_STALACTITE:
        case Therion::THP_STALAGMITE:
          painter.setPen( Qt::black );
          break;
        case Therion::THP_USER:
          painter.setPen( Qt::green );
          break;
        case Therion::THP_WATER_FLOW:
          painter.setPen( Qt::blue );
          break;
        case Therion::THP_ENTRANCE:
          painter.setPen( Qt::black );
          break;
        case Therion::THP_CONTINUATION:
          painter.setPen( Qt::black );
          break;
        case Therion::THP_STATION:
          painter.setPen( Qt::black );
          break;
        case Therion::THP_PLACEMARK:
          break;
      }
      if ( type == Therion::THP_WATER_FLOW 
        || type == Therion::THP_AIR_DRAUGHT
        // || type == Therion::THP_LABEL
        || type == Therion::THP_ENTRANCE ) {
        double a = pt->orientation() * ORIENTATION_UNITS; 
        double x1 = x + sin( a ) * 5;
        double y1 = y - cos( a ) * 5;
        painter.drawLine( x,y, x1, y1 );
        painter.drawEllipse( x, y, 2, 2 );
      } else if ( type == Therion::THP_STATION ) {
        painter.drawLine( x-4, y-4, x+4, y+4 );
        painter.drawLine( x-4, y+4, x+4, y-4 );
      } else {
        painter.drawEllipse( x, y, 3, 3 );
      }
    }
  }
  painter.end();
  image.save( imgFileName );
  
}
