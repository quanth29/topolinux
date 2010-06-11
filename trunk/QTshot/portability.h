/** @file portability.h
 *
 * @brief compile & portability issues
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PORTABILITY_H
#define PORTABILITY_H

#ifndef EMBEDDED
  #include <qevent.h>
  #include <qnamespace.h>
  #define WFLAGS Qt::WFlags
  #if QT_VERSION >= 0x040000
    #include <q3mainwindow.h>
    #include <q3toolbar.h>
    #include <qtoolbutton.h>
    #include <q3hbox.h>
    #include <q3vbox.h>
    #include <q3button.h>
    #include <q3buttongroup.h>
    #include <q3table.h>
    #include <q3canvas.h>
    #include <q3multilineedit.h>
    #include <qmenu.h>
    #include <q3popupmenu.h>
    // FIXME #include <qvbuttongroup.h>
    #define QMAINWINDOW Q3MainWindow
    #define QTOOLBAR Q3ToolBar
    #define QTOOLBUTTON QToolButton
    #define QPOPUPMENU Q3PopupMenu
    #define QHEADER Q3Header
    #define QHBOX Q3HBox
    #define QVBOX Q3VBox
    #define QBUTTON Q3Button
    #define QVBUTTONGROUP QVButtonGroup  /* FIXME */
    #define QBUTTONGROUP QButtonGroup  /* FIXME */
    #define QTABLE Q3Table
    #define QMULTILINEEDIT Q3MultiLineEdit
    #define QCANVAS Q3Canvas
    #define QCANVASITEM Q3CanvasItem
    #define QCANVASITEMLIST Q3CanvasItemList
    #define QCANVASLINE Q3CanvasLine
    #define QCANVASTEXT Q3CanvasText
    #define QCANVASELLIPSE Q3CanvasEllipse
    #define QCANVASPOLYGON Q3CanvasPolygon
    #define QCANVASVIEW Q3CanvasView
    #define QPOINTARRAY Q3PointArray
  #else
    #include <qmainwindow.h>
    #include <qtoolbar.h>
    #include <qtoolbutton.h>
    #include <qhbox.h>
    #include <qtable.h>
    #include <qcanvas.h>
    #include <qmultilineedit.h>
    #include <qpopupmenu.h>
    #include <qvbox.h>
    #include <qvbuttongroup.h>
    #define QMAINWINDOW QMainWindow
    #define QTOOLBAR QToolBar
    #define QTOOLBUTTON QToolButton
    #define QPOPUPMENU QPopupMenu
    #define QHEADER QHeader
    #define QHBOX QHBox
    #define QVBOX QVBox
    #define QBUTTON QButton
    #define QVBUTTONGROUP QVButtonGroup 
    #define QBUTTONGROUP QButtonGroup 
    #define QTABLE QTable
    #define QMULTILINEEDIT QMultiLineEdit
    #define QCANVAS QCanvas
    #define QCANVASITEM QCanvasItem
    #define QCANVASITEMLIST QCanvasItemList
    #define QCANVASLINE QCanvasLine
    #define QCANVASTEXT QCanvasText
    #define QCANVASELLIPSE QCanvasEllipse
    #define QCANVASPOLYGON QCanvasPolygon
    #define QCANVASVIEW QCanvasView
    #define QPOINTARRAY QPointArray
  #endif
#else /* EMBEDDED */
  #include <qmainwindow.h>
  #include <qtoolbar.h>
  #include <qtoolbutton.h>
  #include <qhbox.h>
  #include <qvbox.h>
  #include <qtable.h>
  #include <qcanvas.h>
  #include <qmultilineedit.h>
  #include <qpopupmenu.h>
  #include <qvbuttongroup.h>
  #define QMAINWINDOW QMainWindow
  #define QTOOLBAR QToolBar
  #define QTOOLBUTTON QToolButton
  #define QPOPUPMENU QPopupMenu
  #define QHEADER QHeader
  #define QHBOX QHBox
  #define QVBOX QVBox
  #define QBUTTON QButton
  #define QVBUTTONGROUP QVButtonGroup 
  #define QBUTTONGROUP QButtonGroup 
  #define QTABLE QTable
  #define WFLAGS WFlags
  #define QMULTILINEEDIT QMultiLineEdit
  #define QCANVAS QCanvas
  #define QCANVASITEM QCanvasItem
  #define QCANVASITEMLIST QCanvasItemList
  #define QCANVASLINE QCanvasLine
  #define QCANVASTEXT QCanvasText
  #define QCANVASELLIPSE QCanvasEllipse
  #define QCANVASPOLYGON QCanvasPolygon
  #define QCANVASVIEW QCanvasView
  #define QPOINTARRAY QPointArray
#endif

#endif
