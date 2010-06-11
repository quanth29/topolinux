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

#include <qpixmap.h>
#include <qcursor.h>
#include <qpen.h>
#include <qbrush.h>

#include "portability.h"

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
          mode1Icon, mode2Icon, mode3Icon,
          thetaPlusIcon, thetaMinusIcon,
          phiPlusIcon, phiMinusIcon,
          undoIcon, undoOffIcon, imageIcon,
          evalIcon, evalOffIcon,
          readIcon, writeIcon, toggleIcon, 
          coverIcon, coverOffIcon,
          commentIcon, commentOffIcon, numberIcon,
          okIcon, leftIcon, rightIcon, upIcon, downIcon;
      QPixmap whiteIcon, blueIcon, greenIcon, darkBlueIcon;

      QPixmap penUpIcon, penDownIcon;
      QCursor penUpCursor, penDownCursor;

      QPOINTARRAY polygon[12];  //!< point-type shapes
      QBrush brush[12];         //!< point brushes
      QPOINTARRAY arrow[8];     //!< orientation arrows
      QPOINTARRAY fat_arrow[8]; //!< orientation fat arrows
      QPen pen_red;          //!< pens
      QPen pen_black;
      QPen pen_blue;
      QPen pen_violet;
      QPen pen_green;
      QPen pen_gray;
      QPen dash_black;
      QPen dash_blue;
      QPen dash_violet;
      QPen dark_red;
      QPen dark_gray;

      // QPixmap pit_pixmap;
      // QBrush pit_brush;
      // QPen pit_pen;

      QPOINTARRAY arrow_end[8];

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

    const QPOINTARRAY & Symbol(int i) const { return polygon[i]; }
    const QPOINTARRAY & Arrow(int i) const { return arrow[i%8]; }
    const QPOINTARRAY & FatArrow(int i) const { return fat_arrow[i%8]; }
    const QBrush & Brush(int i) const { return brush[i]; }

    const QPen & PenRed() const { return pen_red; }
    const QPen & PenBlack() const { return pen_black; }
    const QPen & PenBlue() const { return pen_blue; }
    const QPen & PenViolet() const { return pen_violet; }
    const QPen & PenGray() const { return pen_gray; }
    const QPen & DashBlack() const { return dash_black; }
    const QPen & DashBlue() const { return dash_blue; }
    const QPen & DashViolet() const { return dash_violet; }
    const QPen & PenGreen() const { return pen_green; }
    const QPen & PenPit() const { return dash_black /* pit_pen */; }
    const QPen & DarkRed() const { return dark_red; }
    const QPen & DarkGray() const { return dark_gray; }

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

    const QPixmap & White() const { return whiteIcon; }
    const QPixmap & Blue() const { return blueIcon; }
    const QPixmap & Green() const { return greenIcon; }
    const QPixmap & DarkBlue() const { return darkBlueIcon; }

    const QCursor & PenUp() const { return penUpCursor; }
    const QCursor & PenDown() const { return penDownCursor; }

    const QPOINTARRAY & ArrowEnd( int i ) const { return arrow_end[i]; }

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
