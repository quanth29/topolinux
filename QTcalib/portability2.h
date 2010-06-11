/** @file portability2.h
 *
 * @author marco corvi
 * @date 2009
 *
 * @brief more portability issues
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#ifndef EMBEDDED
  #define WIDGET_WIDTH  400
  #define WIDGET_HEIGHT 480
  #define STATION_WIDTH 40
  #define DATA_WIDTH    70
  #define FLAG_WIDTH    40

  #include <qevent.h>
  #include <qnamespace.h>
  #define WFLAGS Qt::WFlags
  #if QT_VERSION >= 0x040000
    #include <q3mainwindow.h>
    #include <q3hbox.h>
    #include <q3table.h>
    #include <qmenu.h>
    #include <q3toolbar.h>
    #include <qtoolbutton.h>
    #define QMAINWINDOW Q3MainWindow
    #define QTABLE Q3Table
    #define QHBOX Q3HBox
    #define QVBOX Q3VBox
    #define QHEADER Q3Header
    #define QTOOLBAR Q3ToolBar
    #define QTOOLBUTTON QToolButton
    // TODO #define QCANVASITEMLIST QCanvasItemList
  #else
    #include <qmainwindow.h>
    #include <qhbox.h>
    #include <qtable.h>
    #include <qtoolbar.h>
    #include <qtoolbutton.h>
    #define QMAINWINDOW QMainWindow
    #define QTABLE QTable
    #define QHBOX QHBox
    #define QVBOX QVBox
    #define QHEADER QHeader
    #define QCANVASITEMLIST QCanvasItemList
    #define QTOOLBAR QToolBar
    #define QTOOLBUTTON QToolButton
  #endif
#else
  #define WIDGET_WIDTH  240
  #define WIDGET_HEIGHT 320
  #define STATION_WIDTH 30
  #define DATA_WIDTH    40
  #define FLAG_WIDTH    30

  #include <qmainwindow.h>
  #include <qhbox.h>
  #include <qtable.h>
  #include <qtoolbar.h>
  #include <qtoolbutton.h>
  #define QMAINWINDOW QMainWindow
  #define WFLAGS WFlags
  #define QTABLE QTable
  #define QHBOX QHBox
  #define QVBOX QVBox
  #define QHEADER QHeader
  #define QCANVASITEMLIST QCanvasItemList
  #define QTOOLBAR QToolBar
  #define QTOOLBUTTON QToolButton
#endif
