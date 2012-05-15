/** @file IconSet.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief holder of icons
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef ICON_SET_H
#define ICON_SET_H

#include <stdlib.h>

#include <QPixmap>
#include <QCursor>
#include <QPen>
#include <QBrush>
#include <QPainterPath>

// #include "portability.h"
#include "ThPointType.h" // THP_PLACEMARK
#include "ThLineType.h"  // 
#include "ThAreaType.h"

class IconSet
{
  private:
      static IconSet * the_icon_set;

      QPixmap qtshotIcon, qtcalibIcon;

      QPixmap newIcon, newOffIcon, 
          openIcon, saveIcon, saveOffIcon, closeIcon,
          exportThIcon, exportThOffIcon,
          exportDatIcon, exportDatOffIcon,
          exportSvxIcon, exportSvxOffIcon,
          exportTopIcon, exportTopOffIcon,
          helpIcon, quitIcon,
          collapseIcon, collapseOffIcon, optionsIcon,
          planIcon, planOffIcon, 
          extendedIcon, extendedOffIcon,
          _3dIcon, _3dOffIcon, gridIcon,
          dataIcon, data3Icon, data4Icon,
          zoomInIcon, zoomOutIcon,
          mode1Icon, mode2Icon, mode3Icon, mode4Icon,
          thetaPlusIcon, thetaMinusIcon,
          phiPlusIcon, phiMinusIcon,
          undoIcon, undoOffIcon, imageIcon,
          evalIcon, evalOffIcon,
          readIcon, writeIcon, toggleIcon, 
          coverIcon, coverOffIcon,
          commentIcon, commentOffIcon, numberIcon,
          okIcon, leftIcon, rightIcon, upIcon, downIcon, viewIcon,
          scrapIcon, selectIcon, pointIcon, lineIcon, areaIcon;

#ifdef HAS_THP_ICONS
      QPixmap thpAirDraught, thpBlocks, thpClay, // thpContinuation,
              thpCrystal, thpDebris, thpEntrance, 
              thpFlowstone, thpHelictite, thpIce, // thpLabel,
              thpPebbles, thpSand, thpSnow,
              thpStalactite, thpStalagmite,
              thpUser, thpWaterFlow,
              thpStation;
#else
      QPolygon polygon[ Therion::THP_PLACEMARK ];  //!< point-type shapes
      QPainterPath path[ Therion::THP_PLACEMARK ]; //!< point-type shapes
      QBrush brush[ Therion::THP_PLACEMARK ];      //!< point brushes
      QPen thp_pen[ Therion::THP_PLACEMARK ];      //!< point pen
#endif

      QPixmap whiteIcon, blueIcon, greenIcon, darkBlueIcon;

      QBrush  T_brush,
              C_brush;

      QBrush  brush_wavy_black;  // THA_CLAY
      QBrush  brush_debris;
      QBrush  brush_dash_black;  // THA_FLOWSTONE
      QBrush  brush_cross_black; // THA_ICE
      QBrush  brush_pebbles;
      QBrush  brush_dot_yellow;  // THA_SAND
      QBrush  brush_star_black;  // THA_SNOW
      QBrush  brush_dot_green;   // THA_USER
      QBrush  brush_wavy_blue;   // THA_WATER 

      QPixmap penUpIcon, penDownIcon;
      QCursor penUpCursor, penDownCursor;

#ifdef OLD_POLYGON
      QPolygon arrow[8];     //!< orientation arrows
      QPolygon fat_arrow[8]; //!< orientation fat arrows
#endif
      QPen pen_red;          //!< pens
      QPen pen_black;
      QPen pen_blue;
      QPen pen_violet;
      QPen pen_yellow;
      QPen pen_green;
      QPen pen_gray;
      QPen dash_black;
      QPen dash_blue;
      QPen dash_gray;
      QPen dash_violet;
      QPen dash_yellow;
      QPen dark_red;
      QPen dark_gray;

      QPen pen_pit;      // magenta with ticks
      QPen pen_chimney;  // dash magenta with ticks

      // QPixmap pit_pixmap;
      // QBrush pit_brush;
      // QPen pit_pen;

      QPolygon arrow_end[8];

  private:
    IconSet();

  public:
    static void Release()
    {
      if ( the_icon_set ) delete the_icon_set;
    }

  public:
    /* const */ int Min() const { return -10; }
    /* const */ int Max() const { return  10; }
    /* const */ int Size() const { return 21; }

#ifdef OLF_POLYGON
    const QPolygon & Arrow(int i) const { return arrow[i%8]; }
    const QPolygon & FatArrow(int i) const { return fat_arrow[i%8]; }
#endif

    const QBrush & BrushWavyBlue() const { return brush_wavy_blue; }
    const QBrush & BrushWavyBlack() const { return brush_wavy_black; }
    const QBrush & BrushStarBlack() const { return brush_star_black; }
    const QBrush & BrushCrossBlack() const { return brush_cross_black; }
    const QBrush & BrushDotGreen() const { return brush_dot_green; }
    const QBrush & BrushDotYellow() const { return brush_dot_yellow; }
    const QBrush & BrushDashBlack() const { return brush_dash_black; }
    const QBrush & BrushDebris() const { return brush_debris; }
    const QBrush & BrushPebbles() const { return brush_pebbles; }

    const QPen & PenRed() const { return pen_red; }
    const QPen & PenBlack() const { return pen_black; }
    const QPen & PenBlue() const { return pen_blue; }
    const QPen & PenViolet() const { return pen_violet; }
    const QPen & PenYellow() const { return pen_yellow; }
    const QPen & PenGray() const { return pen_gray; }
    const QPen & PenGreen() const { return pen_green; }

    const QPen & PenDashBlack() const { return dash_black; }
    const QPen & PenDashBlue() const { return dash_blue; }
    const QPen & PenDashGray() const { return dash_gray; }
    const QPen & PenDashViolet() const { return dash_violet; }
    const QPen & PenDashYellow() const { return dash_yellow; }

    const QPen & PenDarkRed() const { return dark_red; }
    const QPen & PenDarkGray() const { return dark_gray; }

    const QPen & PenPit() const { return pen_pit; }
    const QPen & PenChimney() const { return pen_chimney; }

    const QPixmap & QTshot() const { return qtshotIcon; }
    const QPixmap & QTcalib() const { return qtcalibIcon; }

    const QPixmap & New() const { return newIcon; }
    const QPixmap & NewOff() const { return newOffIcon; }
    const QPixmap & Open() const { return openIcon; }
    const QPixmap & Close() const { return closeIcon; }
    const QPixmap & Save() const { return saveIcon; }
    const QPixmap & SaveOff() const { return saveOffIcon; }
    const QPixmap & ExportTh() const { return exportThIcon; }
    const QPixmap & ExportThOff() const { return exportThOffIcon; }
    const QPixmap & ExportDat() const { return exportDatIcon; }
    const QPixmap & ExportDatOff() const { return exportDatOffIcon; }
    const QPixmap & ExportSvx() const { return exportSvxIcon; }
    const QPixmap & ExportSvxOff() const { return exportSvxOffIcon; }
    const QPixmap & ExportTop() const { return exportTopIcon; }
    const QPixmap & ExportTopOff() const { return exportTopOffIcon; }
    const QPixmap & Help() const { return helpIcon; }
    const QPixmap & Quit() const { return quitIcon; }
    const QPixmap & Collapse() const { return collapseIcon; }
    const QPixmap & CollapseOff() const { return collapseOffIcon; }
    const QPixmap & Options() const { return optionsIcon; }
    const QPixmap & Plan() const { return planIcon; }
    const QPixmap & PlanOff() const { return planOffIcon; }
    const QPixmap & Extended() const { return extendedIcon; }
    const QPixmap & ExtendedOff() const { return extendedOffIcon; }
    const QPixmap & _3d() const { return _3dIcon; }
    const QPixmap & _3dOff() const { return _3dOffIcon; }
    const QPixmap & Data() const { return dataIcon; }
    const QPixmap & Data3() const { return data3Icon; }
    const QPixmap & Data4() const { return data4Icon; }
    const QPixmap & ZoomIn() const { return zoomInIcon; }
    const QPixmap & ZoomOut() const { return zoomOutIcon; }
    const QPixmap & Mode1() const { return mode1Icon; }
    const QPixmap & Mode2() const { return mode2Icon; }
    const QPixmap & Mode3() const { return mode3Icon; }
    const QPixmap & Mode4() const { return mode4Icon; }
    const QPixmap & Grid() const { return gridIcon; }
    const QPixmap & Number() const { return numberIcon; }
    const QPixmap & Undo() const { return undoIcon; }
    const QPixmap & UndoOff() const { return undoOffIcon; }
    const QPixmap & Image() const { return imageIcon; }
    const QPixmap & ThetaPlus() const { return thetaPlusIcon; }
    const QPixmap & ThetaMinus() const { return thetaMinusIcon; }
    const QPixmap & PhiPlus() const { return phiPlusIcon; }
    const QPixmap & PhiMinus() const { return phiMinusIcon; }
    const QPixmap & Eval() const { return evalIcon; }
    const QPixmap & EvalOff() const { return evalOffIcon; }
    const QPixmap & Read() const { return readIcon; }
    const QPixmap & Write() const { return writeIcon; }
    const QPixmap & Toggle() const { return toggleIcon; }
    const QPixmap & Cover() const { return coverIcon; }
    const QPixmap & CoverOff() const { return coverOffIcon; }
    const QPixmap & Comment() const { return commentIcon; }
    const QPixmap & CommentOff() const { return commentOffIcon; }
    const QPixmap & Left() const { return leftIcon; }
    const QPixmap & Right() const { return rightIcon; }
    const QPixmap & Up() const { return upIcon; }
    const QPixmap & Down() const { return downIcon; }
    const QPixmap & Ok() const { return okIcon; }
    const QPixmap & View() const { return viewIcon; }

    const QPixmap & Scrap() const { return scrapIcon; }
    const QPixmap & Select() const { return selectIcon; }
    const QPixmap & Point() const { return pointIcon; }
    const QPixmap & Line() const { return lineIcon; }
    const QPixmap & Area() const { return areaIcon; }

    const QPixmap & White() const { return whiteIcon; }
    const QPixmap & Blue() const { return blueIcon; }
    const QPixmap & Green() const { return greenIcon; }
    const QPixmap & DarkBlue() const { return darkBlueIcon; }

#ifdef HAS_THP_ICONS
    const QPixmap & ThpAirDraught() const { return thpAirDraught; }
    const QPixmap & ThpBlocks() const { return thpBlocks; }
    const QPixmap & ThpClay() const { return thpClay; }
    // const QPixmap & ThpContinuation() const
    const QPixmap & ThpCrystal() const { return thpCrystal; }
    const QPixmap & ThpDebris() const { return thpDebris; }
    const QPixmap & ThpEntrance() const { return thpEntrance; }
    const QPixmap & ThpFlowstone() const { return thpFlowstone; }
    const QPixmap & ThpHelictite() const { return thpHelictite; }
    const QPixmap & ThpIce() const { return thpIce; }
    // const QPixmap & ThpLabel() const 
    const QPixmap & ThpPebbles() const { return thpPebbles; }
    const QPixmap & ThpSand() const { return thpSand; }
    const QPixmap & ThpSnow() const { return thpSnow; }
    const QPixmap & ThpStalactite() const { return thpStalactite; }
    const QPixmap & ThpStalagmite() const { return thpStalagmite; }
    const QPixmap & ThpUser() const { return thpUser; }
    const QPixmap & ThpWaterFlow() const { return thpWaterFlow; }
    const QPixmap & ThpStation() const { return thpStation; }
    // const QPixmap & Thp() const { return thp; }
    const QPixmap & ThpPixmap( Therion::PointType type ) const;
#else
    const QPolygon &     ThpSymbol1(int i) const { return polygon[i]; }
    const QPainterPath & ThpSymbol2(int i) const { return path[i]; }
    const QBrush & ThpBrush(int i) const { return brush[i]; }
    const QPen & ThpPen( int i ) const { return thp_pen[i]; }
    void ThpStyle1( Therion::PointType t, QPolygon & poly, QBrush & brush ) const;
    void ThpStyle2( Therion::PointType t, QPainterPath & path, QBrush & brush, QPen & pen ) const;
#endif

    const QCursor & PenUp() const { return penUpCursor; }
    const QCursor & PenDown() const { return penDownCursor; }

    const QPolygon & ArrowEnd( int i ) const { return arrow_end[i]; }

    static IconSet * Get() 
    {
      if ( the_icon_set == NULL ) {
        the_icon_set = new IconSet();
        atexit( IconSet::Release );
      }
      return the_icon_set;
    }
  
    void AreaStyle( Therion::AreaType t, QColor & color, QPen & pen, QBrush & brush );
  
  
    QColor LinePointColor( Therion::LineType type );

    void LineStyle( Therion::LineType t, QPen & pen, QColor & color );
};


#endif
