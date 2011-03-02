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

// #include "portability.h"
#include "ThPointType.h" // THP_PLACEMARK

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
          okIcon, leftIcon, rightIcon, upIcon, downIcon,
          scrapIcon, selectIcon, pointIcon, lineIcon, areaIcon;

      QPixmap thpAirDraught, thpBlocks, thpClay, // thpContinuation,
              thpDebris, thpEntrance,  thpIce, // thpLabel,
              thpPebbles, thpSand, thpSnow,
              thpStalactite, thpStalagmite,
              thpUser, thpWaterFlow,
              thpStation;
      QPixmap whiteIcon, blueIcon, greenIcon, darkBlueIcon;
      QBrush  T_brush, C_brush;
      QBrush  brush_wavy;
      QBrush  brush_star;
      QBrush  brush_cross;
      QBrush  brush_dot;

      QPixmap penUpIcon, penDownIcon;
      QCursor penUpCursor, penDownCursor;

#ifdef OLD_POLYGON
      QPolygon polygon[ Therion::THP_PLACEMARK ];  //!< point-type shapes
      QBrush brush[ Therion::THP_PLACEMARK ];      //!< point brushes
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
      QPen dash_violet;
      QPen dark_red;
      QPen dark_gray;

      QPen pen_pit;
      QPen pen_chimney;

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
    const QPolygon & Symbol(int i) const { return polygon[i]; }
    const QPolygon & Arrow(int i) const { return arrow[i%8]; }
    const QPolygon & FatArrow(int i) const { return fat_arrow[i%8]; }
    const QBrush & Brush(int i) const { return brush[i]; }
#endif
    const QBrush & BrushWavy() const { return brush_wavy; }
    const QBrush & BrushStar() const { return brush_star; }
    const QBrush & BrushCross() const { return brush_cross; }
    const QBrush & BrushDot() const { return brush_dot; }

    const QPen & PenRed() const { return pen_red; }
    const QPen & PenBlack() const { return pen_black; }
    const QPen & PenBlue() const { return pen_blue; }
    const QPen & PenViolet() const { return pen_violet; }
    const QPen & PenYellow() const { return pen_yellow; }
    const QPen & PenGray() const { return pen_gray; }
    const QPen & DashBlack() const { return dash_black; }
    const QPen & DashBlue() const { return dash_blue; }
    const QPen & DashViolet() const { return dash_violet; }
    const QPen & PenGreen() const { return pen_green; }
    // const QPen & PenPit() const { return dash_black /* pit_pen */; }
    const QPen & DarkRed() const { return dark_red; }
    const QPen & DarkGray() const { return dark_gray; }
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

    const QPixmap & Scrap() const { return scrapIcon; }
    const QPixmap & Select() const { return selectIcon; }
    const QPixmap & Point() const { return pointIcon; }
    const QPixmap & Line() const { return lineIcon; }
    const QPixmap & Area() const { return areaIcon; }

    const QPixmap & White() const { return whiteIcon; }
    const QPixmap & Blue() const { return blueIcon; }
    const QPixmap & Green() const { return greenIcon; }
    const QPixmap & DarkBlue() const { return darkBlueIcon; }

    const QPixmap & ThpAirDraught() const { return thpAirDraught; }
    const QPixmap & ThpBlocks() const { return thpBlocks; }
    const QPixmap & ThpClay() const { return thpClay; }
    // const QPixmap & ThpContinuation() const
    const QPixmap & ThpDebris() const { return thpDebris; }
    const QPixmap & ThpEntrance() const { return thpEntrance; }
    const QPixmap & ThpIce() const { return thpIce; }
    // const QPixmap & ThpLabel() const 
    const QPixmap & ThpPebbles() const { return thpPebbles; }
    const QPixmap & ThpSand() const { return thpSand; }
    const QPixmap & ThpSnow() const { return thpSnow; }
    const QPixmap & ThpStalactite() const { return thpStalactite; }
    const QPixmap & ThpStalagmite() const { return thpStalagmite; }
    const QPixmap & ThpUser() const { return thpUser; }
    const QPixmap & ThpWaterFlow() const { return thpWaterFlow; }
    // const QPixmap & Thp() const { return thp; }

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
};


#endif
