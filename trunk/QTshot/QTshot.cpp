/** @file QTshot.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief QTopo DistoX data management for OPIE
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#include <stdio.h>
#include <math.h>
#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>

#include <sstream>

#if defined WIN32
  #include <direct.h>
  #define QTOPO_RC "C:\\Program Files\\qtopo\\qtopo.rc"
  #define strcasecmp strcmp
  #define chdir _chdir
#elif defined ARM
  #define QTOPO_RC "/opt/QtPalmtop/etc/qtopo.rc"
#else
  #include <unistd.h>
  #define QTOPO_RC "/usr/share/qtopo/qtopo.rc"
#endif


#include <QMenu>
#include <QRegExp>
#include <qlayout.h>
// #include <qtoolbar.h>
#include <qmenubar.h>
// #include <qtoolbutton.h>
#include <qpixmap.h>
#include <qmessagebox.h>
#include <qdialog.h>
#include <qinputdialog.h>
#include <qfiledialog.h>
#include <qframe.h>
// #include <qwhatsthis.h>
#include <qbuttongroup.h>

#include <qtimer.h>

bool do_debug = false; // enable with -d command option

#ifndef EMBEDDED
  #include <qapplication.h>
  #define QAPPLICATION QApplication
#else
  #include <qpe/qpeapplication.h>
  #define QAPPLICATION QPEApplication
#endif

#include "DistoX.h"

#include "ExportType.h"
#include "Units.h"

#include "GetDate.h"
#include "Locale.h"
#include "QTshot.h"
#include "PlotCanvas.h"
#include "PlotCanvasScene.h"
#include "CanvasMode.h"

#include "DataThExport.h"
#include "DataDatExport.h"
#include "DataSvxExport.h"
#include "DataTopExport.h"

#include "PlotTh2Import.h"

#define TABLE_ROW_HEIGHT 16

// FIXME LANGUAGE
const char * extends[] = { "-", "L", "R", "V", "I" };
const char * flags[]   = { "-", "S", "D" };


/** default comment size
 */
#define COMMENT_SIZE 20

// ===========================================================================
// Main Widget


QTshotWidget::QTshotWidget( QWidget * parent, const char * /* name */, WFLAGS fl )
  : QMainWindow( parent,  fl )
  , config( Config::Get() )
  , lexicon( Language::Get() )
  , table( NULL )
  , plan_cnt( 1 )
  , ext_cnt( 1 )
  , xsect_cnt( 1 )
  , hsect_cnt( 1 )
  // , planCanvas( NULL )
  // , extCanvas( NULL )
  // , crossCanvas( NULL )
  , _3DCanvas( NULL )
  , collapse( true )              // default onCollapse action
  , append( true )                // downloaded data are appended
  , smart( true )                 // smart-process downloaded data 
  , splay_at( SPLAY_BEFORE_SHOT ) // splay at station TO
  , backward( false )             // station numbers are forward
  , comment_size( COMMENT_SIZE )  // comment width displayed in the table 
{
  connect( this, SIGNAL( signalActData(int) ), this, SLOT( updateActData(int) ) );
  brushes[0] = QBrush( Qt::red );
  brushes[1] = QBrush( Qt::blue );
  brushes[2] = QBrush( Qt::black );
  brushes[3] = QBrush( Qt::gray );

  info.fileSaveName = config("DEFAULT_DATA");
  // info.fileExportName = "";
  
  const char * u = config("LENGTH_UNITS");
  if ( u ) {
    if ( u[0] == 'm' ) {
      units.setLength( LENGTH_METER );
    } else if ( u[0] == 'f' ) {
      units.setLength( LENGTH_FEET );
    }
  }
  u = config("ANGLE_UNITS");
  if ( u ) {
    if ( u[0] == 'd' ) {
      units.setAngle( ANGLE_DEGREE );
    } else if ( u[0] == 'g' ) {
      units.setAngle( ANGLE_GRAD );
    }
  }
  // TODO setIconSize( size );

  const char * colors = config("PT_POINTS");
  pt_colors.setPoints( colors );
  colors = config("PT_LINES");
  pt_colors.setLines( colors );

  memset( device, 0, 32 );
  strncpy(device, config("DEVICE" ), 31 );
  #ifdef HAS_LRUD
    do_lrud = strncmp( config("LRUD"), "yes", 3) == 0;
  #endif
  int cs = atoi( config("COMMENT_SIZE" ) );
  if ( cs > 0 && cs <= 80 ) { // max COMMENT_SIZE set to 80 chars
    comment_size = cs;
  }

  icon = IconSet::Get();
  setWindowIcon( icon->QTshot() );

  setWindowTitle( lexicon("qtopo_shot") );
  extends[0] = lexicon("N");
  extends[1] = lexicon("L");
  extends[2] = lexicon("R");
  extends[3] = lexicon("V");
  extends[4] = lexicon("I");
  flags[0] = lexicon("N");
  flags[1] = lexicon("S");
  flags[2] = lexicon("D");
  
  createActions();
  createToolBar();

  const char * geometry = config("GEOMETRY");
  int w = WIDGET_WIDTH;
  int h = WIDGET_HEIGHT;
  if ( sscanf( geometry, "%dx%d", &w, &h ) != 2 ) {
    w = WIDGET_WIDTH;
    h = WIDGET_HEIGHT;
  } 
  resize(w, h);
  
  /*
  if ( (u = config("EXPORT")) != NULL ) {
    if ( strcmp(u, "compass") == 0 ) {
      export_type = EXPORT_COMPASS;
    } else if ( strcmp(u, "pockettopo") == 0 ) {
      export_type = EXPORT_POCKETTOPO;
    } else if ( strcmp(u, "survex") == 0 ) {
      export_type = EXPORT_SURVEX;
    } else {
      export_type = EXPORT_THERION;
    }
    onOffButtonExport( false );
  }
  */

  show();
  if ( strcmp( config("NEWBY"), "yes") == 0 ) {
    SplashWidget( this );
  }
}

// -----------------------------------------------------
void
QTshotWidget::createToolBar()
{
  QToolBar * toolbar = addToolBar( tr("") );
  setToolButtonStyle( Qt::ToolButtonIconOnly ); // this is the default

  toolbar->addAction( actNew );
  toolbar->addAction( actOpen );
  toolbar->addAction( actSave );
  toolbar->addAction( actData );
  // toolbar->addAction( actExport );
  toolbar->addAction( actCollapse );
  toolbar->addAction( actPlan );
  toolbar->addAction( actExtended );
  toolbar->addAction( act3D );
  // toolbar->addAction( actToggle );
  toolbar->addAction( actInfo );
  // toolbar->addAction( actOptions );
  // toolbar->addAction( actHelp );
  toolbar->addAction( actQuit );
}

// -----------------------------------------------------
void
QTshotWidget::createActions()
{
  actNew      = new QAction( icon->NewOff(), lexicon("clear"), this);
  actOpen     = new QAction( icon->Open(), lexicon("open"), this);
  actSave     = new QAction( icon->SaveOff(), lexicon("save"), this );
  QMenu * save_menu = new QMenu( this );
  save_menu->addAction( lexicon("topolinux") );
  save_menu->addAction( lexicon("therion") );
  save_menu->addAction( lexicon("compass") );
  save_menu->addAction( lexicon("survex") );
  save_menu->addAction( lexicon("pockettopo") );
  connect( save_menu, SIGNAL(triggered(QAction*)), this, SLOT(doExport(QAction*)) ); 
  actSave->setMenu( save_menu );

  actData     = new QAction( icon->Data(), lexicon("download"), this );
  QMenu * disto_menu = new QMenu( this );
  QAction * act_download = disto_menu->addAction( lexicon("download") );
  QAction * act_toggle   = disto_menu->addAction( lexicon("toggle") );
  connect( act_download, SIGNAL(triggered()), this, SLOT(doData()) );
  connect( act_toggle, SIGNAL(triggered()), this, SLOT(doToggle()) );
  actData->setMenu( disto_menu );


  // actExport   = new QAction( icon->ExportThOff(), lexicon("export"), this );
  actCollapse = new QAction( icon->CollapseOff(), lexicon("splay"), this );
  actPlan     = new QAction( icon->PlanOff(), lexicon("plan"), this );
  plan_menu = new QMenu( this );
  plan_menu->addAction( lexicon("new") );
  plan_menu->addAction( lexicon("open") );
  for ( size_t k = 0; k < planCanvases.plotsSize(); ++k ) {
    plan_menu->addAction( planCanvases.getName(k) );
  }
  connect( plan_menu, SIGNAL(triggered(QAction*)), this, SLOT(doPlan(QAction*)) ); 
  actPlan->setMenu( plan_menu );

  actExtended = new QAction( icon->ExtendedOff(), lexicon("extended"), this );
  ext_menu = new QMenu( this );
  ext_menu->addAction( lexicon("new") );
  ext_menu->addAction( lexicon("open") );
  for ( size_t k = 0; k < extCanvases.plotsSize(); ++k ) {
    plan_menu->addAction( extCanvases.getName(k) );
  }
  connect( ext_menu, SIGNAL(triggered(QAction*)), this, SLOT(doExtended(QAction*)) ); 
  actExtended->setMenu( ext_menu );

  act3D       = new QAction( icon->_3dOff(), lexicon("3d"), this );
  // actToggle   = new QAction( icon->Toggle(), lexicon("toggle"), this );
  actInfo     = new QAction( icon->View(), lexicon("info"), this );
  // actOptions  = new QAction( icon->Options(), lexicon("options"), this );
  // actHelp     = new QAction( icon->Help(), lexicon("help"), this );
  QMenu * info_menu = new QMenu( this );
  QAction * act_info    = info_menu->addAction( lexicon("info") );
  QAction * act_options = info_menu->addAction( lexicon("options") );
  QAction * act_help    = info_menu->addAction( lexicon("help") );
  connect( act_info, SIGNAL(triggered()), this, SLOT(doInfo()) );
  connect( act_options, SIGNAL(triggered()), this, SLOT(doOptions()) );
  connect( act_help, SIGNAL(triggered()), this, SLOT(doHelp()) );
  actInfo->setMenu( info_menu );

  actQuit     = new QAction( icon->Quit(), lexicon("exit"), this );
  // actQuit->setShortcuts( QKeySequence::Quit );
  // actQuit->setStatusTip( tr("...") );

  connect( actNew,     SIGNAL(triggered()),  this, SLOT(doNew()) );
  connect( actOpen,    SIGNAL(triggered()), this, SLOT(doOpen()) );
  connect( actSave,    SIGNAL(triggered()), this, SLOT(doSave()) );
  connect( actData,    SIGNAL(triggered()), this, SLOT(doData()) );
  // connect( actExport,  SIGNAL(triggered()), this, SLOT(doExport()) );
  connect( actCollapse, SIGNAL(triggered()), this, SLOT(doCollapse()) );
  connect( actPlan,    SIGNAL(triggered()), this, SLOT(doPlanScrap()) );
  connect( actExtended, SIGNAL(triggered()), this, SLOT(doExtendedScrap()) );
  connect( act3D,      SIGNAL(triggered()), this, SLOT(do3D()) );
  // connect( actToggle,  SIGNAL(triggered()), this, SLOT(doToggle()) );
  connect( actInfo,    SIGNAL(triggered()), this, SLOT(doInfo()) );
  // connect( actOptions, SIGNAL(triggered()), this, SLOT(doOptions()) );
  // connect( actHelp,    SIGNAL(triggered()), this, SLOT(doHelp()) );
  connect( actQuit,    SIGNAL(triggered()), this, SLOT(doQuit()) );

  actNew->setVisible( true );
  actOpen->setVisible( true );
  actSave->setVisible( true );
  actData->setVisible( true );
  // actExport->setVisible( true );
  actCollapse->setVisible( true );
  actPlan->setVisible( true );
  actExtended->setVisible( true );
  act3D->setVisible( true );
  // actToggle->setVisible( true );
  actInfo->setVisible( true );
  // actOptions->setVisible( true );
  // actHelp->setVisible( true );
  actQuit->setVisible( true );
}

// -----------------------------------------------------
void
QTshotWidget::updateActData( int c )
{
  switch ( c ) {
    case 3:
      actData->setIcon( icon->Data3() );
      break;
    case 4:
      actData->setIcon( icon->Data4() );
      break;
    default:
      actData->setIcon( icon->Data() );
  }
}
      
  
void
QTshotWidget::distoxReset()
{
  DBG_CHECK("QTshotWidget::distoxReset()\n");
  signalActData( 3 );
}

void
QTshotWidget::distoxDownload( size_t nr )
{
  DBG_CHECK("QTshotWidget::distoxDownload() %d\n", nr );
  if ( ( nr % 2 ) == 1 ) {
    signalActData( 4 );
  } else {
    signalActData( 3 );
  }
}

void
QTshotWidget::distoxDone()
{
  DBG_CHECK("QTshotWidget::distoxDone()\n");
  signalActData( 0 );
  onOffButtons( dlist.listSize() > 0 );
}


void
QTshotWidget::value_changed( int r, int c )
{
  DBG_CHECK("value_changed row %d column %d \n", r, c );
  if ( c == 0 || c == 1 || c == 5 || c == 6 ) {
    dlist.updateBlock( r, c, table->item(r,c)->text().TO_CHAR() );
  /*
  } else if ( c == 4 ) {
    int ignore = dlist.toggleIgnore( r );
    table->setText( r, 4, ignore ? "v" : " " );
  */
  }
}

void
QTshotWidget::setBaseBlock( DBlock * base )
{
  if ( table != NULL ) {
    int row = 0;
    for (DBlock * b = dlist.listHead(); b != NULL; b=b->next() ) {
      if ( dlist.isBaseBlock( b ) ) {
        #ifdef HAS_LRUD
          if ( do_lrud ) {
            if ( b->getLRUD( 0 ) ) {
              table->item( row, 0 )->setIcon( icon->Blue() );
            } else {
              table->item( row, 0 )->setIcon( icon->White() );
              // FIXME table->updateCell( row, 0 );
            }
          } else {
            // fprintf(stderr, "paint row %d white\n", row );
            table->item( row, 0 )->setIcon( icon->White() );
            // FIXME table->updateCell( row, 0 );
          }
        #else
          table->item( row, 0)->setIcon( icon->White() );
          // FIXME table->updateCell( row, 0 );
        #endif
        break;
      }
      ++ row;
    }
  }
  dlist.setBaseBlock( base );
}


void
QTshotWidget::showData( )
{
  DBG_CHECK("showData() table %p \n", (void *)table );
  createTable();

  double ls = units.length_factor;
  double as = units.angle_factor;

  QTableWidgetItem * item;
  int row = 0;
  for (DBlock * b = dlist.listHead(); b != NULL; b=b->next() ) {
    table->setRowHeight( row, TABLE_ROW_HEIGHT );
    QBrush * brush = &( brushes[3] );
    if ( b->hasFromStation() && b->hasToStation() ) {
      switch ( b->nrBlocklets() ) {
        case 1:
          brush = &( brushes[0] );
          break;
        case 2:
          brush = &( brushes[1] );
          break;
        default:
          brush = &( brushes[2] );
      }
    }

    item = new QTableWidgetItem( b->fromStation() );
    // item->setForeground( *brush );
    table->setItem( row, 0, item);
    item = new QTableWidgetItem( b->toStation() );
    table->setItem( row, 1, item);
    item = new QTableWidgetItem( Locale::ToString(b->Tape() * ls, 2) );
    item->setForeground( *brush );
    table->setItem( row, 2, item);
    item = new QTableWidgetItem( Locale::ToString(b->Compass() * as, 1) );
    item->setForeground( *brush );
    table->setItem( row, 3, item);
    item = new QTableWidgetItem( Locale::ToString(b->Clino() * as, 1) );
    item->setForeground( *brush );
    table->setItem( row, 4, item);
    item = new QTableWidgetItem( tr( extends[ b->Extend() ] ) );
    table->setItem( row, 5, item);
    item = new QTableWidgetItem( tr( flags[ b->Flag() ] ) );
    table->setItem( row, 6, item);
    if ( b->hasComment() ) {
      QString c( b->getComment() );
      c.truncate( comment_size ); 
      item = new QTableWidgetItem( c );
      table->setItem( row, 7, item );
    }

    #ifdef HAS_LRUD
      if ( do_lrud ) {
        if ( dlist.IsBaseBlock( b ) ) { 
          if ( b->getLRUD( 0 ) ) {
            table->item( row, 0)->setIcon( icon->DarkBlue() );
          } else {
            table->item( row, 0)->setIcon( icon->Green() );
          }
        } else {
          if ( b->getLRUD( 0 ) ) {
            table->item( row, 0)->setIcon( icon->Blue() );
          } else {
            // table->item( row, 0)->setIcon( icon->White() );
          }
        }
        if ( b->getLRUD( 1 ) ) {
          table->item( row, 1)->setIcon( icon->Blue() );
        } else {
          // table->item( row, 0)->setIcon( icon->White() );
        }
      } else { // do_lrud == false
        if ( dlist.IsBaseBlock( b ) ) {
          table->item( row, 0)->setIcon( icon->Green() );
        } else {
          // table->item( row, 0)->setIcon( icon->White() );
        }
      }
    #else
      if ( dlist.isBaseBlock( b ) ) {
        table->item( row, 0 )->setIcon( icon->Green() );
      } else {
        // table->setPixmap( row, 0, icon->White() );
      }
    #endif

    ++ row;
  }
}


void
QTshotWidget::createTable()
{
  int rows = dlist.listSize();
  int cols = 8;
  if ( table == NULL ) {
    
    table = new QTableWidget( rows, cols, this);
    table->setHorizontalHeaderItem( 0, new QTableWidgetItem( lexicon("from") ) ); // ,    STATION_WIDTH );
    table->setHorizontalHeaderItem( 1, new QTableWidgetItem( lexicon("to") ) ); // ,      STATION_WIDTH );
    table->setHorizontalHeaderItem( 2, new QTableWidgetItem( lexicon("tape") ) ); // ,    DATA_WIDTH );
    table->setHorizontalHeaderItem( 3, new QTableWidgetItem( lexicon("azimuth") ) ); // , DATA_WIDTH );
    table->setHorizontalHeaderItem( 4, new QTableWidgetItem( lexicon("clino") ) ); // ,   DATA_WIDTH );
    table->setHorizontalHeaderItem( 5, new QTableWidgetItem( lexicon("ext") ) ); // ,     FLAG_WIDTH );
    table->setHorizontalHeaderItem( 6, new QTableWidgetItem( lexicon("flag") ) ); // ,    FLAG_WIDTH );
    table->setHorizontalHeaderItem( 7, new QTableWidgetItem( lexicon("comment") ) ); // , FLAG_WIDTH );
    table->setColumnWidth( 0, STATION_WIDTH );
    table->setColumnWidth( 1, STATION_WIDTH );
    table->setColumnWidth( 2, DATA_WIDTH );
    table->setColumnWidth( 3, DATA_WIDTH );
    table->setColumnWidth( 4, DATA_WIDTH );
    table->setColumnWidth( 5, FLAG_WIDTH );
    table->setColumnWidth( 6, FLAG_WIDTH );
    table->setColumnWidth( 7, 100 );
    table->setRowHeight( 0, TABLE_ROW_HEIGHT );
    // header->setClickEnabled( TRUE, 0 );
    // header->setClickEnabled( TRUE, 1 );
    // connect( table, SIGNAL(clicked(int, int, int, const QPoint &)), 
    //          this, SLOT(clicked( int, int, int, const QPoint & ) ) );
    table->setItemPrototype( table->item( 0, 0 ) );
    connect( table, SIGNAL(cellChanged(int, int)), this, SLOT(value_changed(int,int)) );
    connect( table, SIGNAL(cellDoubleClicked(int, int ) ), this, SLOT(double_clicked( int, int ) ) );
    table->show();
    setCentralWidget( table );

    // table->setSorting( TRUE );
    DBG_CHECK("showData() created table, rows %d \n", dlist.Size() );
  } else {
    table->setRowCount( dlist.listSize() );
    DBG_CHECK("showData() table set rows %d \n", dlist.Size() );
  }
}

void 
QTshotWidget::double_clicked( int row, int col )
{
  DBG_CHECK("QTshotWidget::double_clicked %d %d \n", row, col );
  col = col;  // avoid gcc warnings
  // comment input dialog
  DBlock * b = dlist.getBlock( row );
  #ifdef HAS_LRUD
    CommentWidget dialog( this, b, do_lrud );
  #else
    CommentWidget dialog( this, b );
  #endif
}

/*
void
QTshotWidget::clicked( int row, int col, int button, const QPoint & )
{
  DBG_CHECK("QTshotWidget::clicked %d %d Button %d \n", row, col, button );
}
*/

void 
QTshotWidget::doCollapse()
{
  if ( dlist.listSize() == 0 ) return;
  DBG_CHECK("doCollapse() %s \n", collapse? "yes" : "no" );
  if ( ! table ) return;
  if ( collapse ) {
    for (int row = 0; row <table->rowCount(); ++row ) {
      if ( table->item(row, 1)->text().isEmpty() ) {
        table->hideRow( row );
      } else {
        table->setRowHeight(row, TABLE_ROW_HEIGHT);
      }
    }
    collapse = false;
  } else {
    for (int row = 0; row <table->rowCount(); ++row ) {
      table->showRow( row );
      table->setRowHeight(row, TABLE_ROW_HEIGHT);
    }
    collapse = true;
  }
}

void
QTshotWidget::doNew()
{
  DBG_CHECK("doNew \n" );

  if ( dlist.listSize() > 0 ) {
    CleanShotsWidget dialog( this );
  }
}
  

void
QTshotWidget::doRealNew()
{
  dlist.clear();
  onOffButtons( dlist.listSize() > 0 );
  closePlots();
  showData();
}

// ------------------------------------------------------------------
// PlotDrawer interface

void 
QTshotWidget::removePlotCanvas( PlotCanvas * c )
{
  int mode = c->getMode();
  QString name;
  bool ok = false;
  if ( mode == 0 ) { // plan
    ok = planCanvases.removePlot( c, name );
    if ( ok ) {
      // fprintf(stderr, "removePlotCanvas %s \n", name.TO_CHAR() );
      QList<QAction *> actions = plan_menu->actions();
      for ( QList<QAction *>::iterator it = actions.begin(); it != actions.end(); ++it ) {
        if ( (*it)->text() == name ) {
          plan_menu->removeAction( *it );
          break;
        }
      }
    }
  } else if ( mode == 1 ) { // extended
    ok = extCanvases.removePlot( c, name );
    if ( ok ) {
      QList<QAction *> actions = ext_menu->actions();
      for ( QList<QAction *>::iterator it = actions.begin(); it != actions.end(); ++it ) {
        if ( (*it)->text() == name ) {
          ext_menu->removeAction( *it );
          break;
        }
      }
    }
  } else if ( mode == 2 ) {
    ok = crossCanvases.removePlot( c, name );
    // FIXME ??
  }
  // delete c;
}

void
QTshotWidget::closePlots()
{
  // DBG_CHECK("closePlots() %p %p %p %p\n",
  //     (void*)planCanvas, (void*)extCanvas, (void*)crossCanvas, (void*)_3DCanvas );

  // if ( planCanvas ) {
  //   planCanvas->hide();
  //   planCanvas->ClearTh2PointsAndLines();
  //   delete planCanvas;
  //   planCanvas = NULL;
  // }
  planCanvases.clear();
  plan_menu->clear();
  plan_menu->addAction( lexicon("new") );
  plan_menu->addAction( lexicon("open") );

  // if ( extCanvas ) {
  //   extCanvas->hide();
  //   extCanvas->ClearTh2PointsAndLines();
  //   delete extCanvas;
  //   extCanvas = NULL;
  // }
  extCanvases.clear();
  ext_menu->clear();
  ext_menu->addAction( lexicon("new") );
  ext_menu->addAction( lexicon("open") );

  // if ( crossCanvas ) {
  //   crossCanvas->hide();
  //   crossCanvas->ClearTh2PointsAndLines();
  //   delete crossCanvas;
  //   crossCanvas = NULL;
  // }
  crossCanvases.clear();

  if ( _3DCanvas ) {
    _3DCanvas->hide();
    // _3DCanvas->ClearTh2PointsAndLines();
    delete _3DCanvas;
    _3DCanvas = NULL;
  }
}

PlotCanvas *
QTshotWidget::openPlot( int mode, const char * pname, const char * sname )
{
  if ( dlist.listSize() == 0 ) return NULL;
  // fprintf( stderr, "QTshotWidget::openPlot() mode %d name %s %s\n", mode, pname, sname ); // FIXME
  PlotCanvas * canvas = NULL;
  if ( mode == MODE_PLAN ) {
    canvas = new PlotCanvas( this, MODE_PLAN, pname, sname /*, true */ );
    planCanvases.addPlot( pname, canvas );
    plan_menu->addAction( pname );
  } else if ( mode == MODE_EXT ) {
    canvas = new PlotCanvas( this, MODE_EXT, pname, sname /*, true */ );
    extCanvases.addPlot( pname, canvas ); 
    ext_menu->addAction( pname );
  } else {
    fprintf(stderr, "QTshotWidget::openPlot() not supported mode %d name %s %s\n",
      mode, pname, sname );
  } 
  return canvas;
}


void 
QTshotWidget::insertPoint( int x, int y, Therion::PointType type, PlotCanvas * canvas )
{
  canvas->getScene()->insertPoint( x, y, type, 0, 0, true );
}
 
void 
QTshotWidget::insertLinePoint( int x, int y, Therion::LineType type, PlotCanvas * canvas )
{
  canvas->getScene()->insertLinePoint( x, y, x, y, type, true );
}

void
QTshotWidget::updateCanvases()
{
  // if ( planCanvas ) planCanvas->redrawPlot();
  // if ( extCanvas )  extCanvas->redrawPlot();
  for ( size_t k=0; k<planCanvases.plotsSize(); ++k ) {
    planCanvases[k] ->redrawPlot();
  }
  for ( size_t k=0; k < extCanvases.plotsSize(); ++k ) {
    extCanvases[k] -> redrawPlot();
  } 
  // FIXME
  // if ( _3DCanvas )  _3DCanvas->redrawPlot();
  // if ( crossCanvas ) crossCanvas->redrawPlot();
}


// ---------------------------------------------------------
// FILE -> OPEN

void
QTshotWidget::doOpen()
{
  DBG_CHECK("doOpen \n" );
  onOpenFile( QFileDialog::getOpenFileName( this,
      lexicon("open_file"),
      info.fileSaveName,
      "Tlx files (*.tlx)\nRaw files (*.txt)\nPocketTopo (*.top)\nAll (*.*)"
      ) );
}

void
QTshotWidget::onOpenFile( const QString & file )
{
  info.fileSaveName = file;
  DBG_CHECK("onOpenFile file \"%s\"\n", info.fileSaveName.TO_CHAR() );
  if ( ! info.fileSaveName.isEmpty() ) {
    closePlots();           // close all plots
    setBaseBlock( NULL );   // erase base block pixmap
    collapse = true;        // reset onCollapse action
    if ( table != NULL ) {  // set all table rows visible
      for (int row = 0; row <table->rowCount(); ++row ) {
        table->showRow( row );
      }
    }

    int ret = dlist.loadFile( this, info.fileSaveName.TO_CHAR(), false, &info ); // do not append but replace data
    if ( ret == 0 ) {
      onOffButtons( dlist.listSize() > 0 );
      showData();
    } else {
      switch (ret) {
      case 1:
        QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("file_open_failed") );
        break;
      case 2:
        QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("raw_read_failed") );
        break;
      case 3:
        QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("tlx_read_failed") );
        break;
      case 4:
        QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("bad_file_format") );
        break;
      }
    }
  }
}

// ---------------------------------------------------------
// FILE -> SAVE

void
QTshotWidget::doSave()
{
  if ( dlist.listSize() == 0 ) return;
  onSaveFile( QFileDialog::getSaveFileName( this,
    lexicon("save_file"),
    info.fileSaveName,
    "Tlx files (*.tlx)\nAll (*.*)" ) );
}

void
QTshotWidget::onSaveFile( const QString & file )
{
  info.fileSaveName = file;
  DBG_CHECK("onSaveFile file \"%s\"\n", info.fileSaveName.TO_CHAR() );
  if ( !info.fileSaveName.isEmpty() ) {                 // got a file name
    
    // TODO get optional comment to write to TLX file
    if ( info.description.size() == 0 ) {
      // info.surveyComment = 
      QString descr = 
        QInputDialog::getText( this,
                               lexicon("qtopo_comment"), 
                               lexicon("comment"),
                               QLineEdit::Normal, 
                               "" // info.description // info.surveyComment
      );
      info.description = descr.TO_CHAR();
    }
    dlist.saveTlx( info );
  }
}

void 
QTshotWidget::getSurveyText( std::ostringstream & oss ) 
{
  for ( size_t k=0; k<planCanvases.plotsSize(); ++k ) {
    PlotCanvas * canvas = planCanvases[k];
    oss << " input " << canvas->getBaseFileName() << "\n";
  }
  for ( size_t k=0; k<extCanvases.plotsSize(); ++k ) {
    PlotCanvas * canvas = extCanvases[k];
    oss << " input " << canvas->getBaseFileName() << "\n";
  }
  for ( size_t k=0; k<crossCanvases.plotsSize(); ++k ) {
    PlotCanvas * canvas = crossCanvases[k];
    oss << " input " << canvas->getBaseFileName() << "\n";
  }
}

void
QTshotWidget::doInsertBlock( DBlock * block )
{
  InsertWidget dialog( this, block );
}

void 
QTshotWidget::doInsertBlock( DBlock * block, const char * d0, const char * b0, const char * c0, bool before )
{
  // double d = atof( d0 );
  // double b = atof( b0 );
  // double c = atof( c0 );
  double d = Locale::ToDouble( d0 );
  double b = Locale::ToDouble( b0 );
  double c = Locale::ToDouble( c0 );
  double r = 0.0; // FIXME
  if (    d >= 0.0 && d < 1000.0 
       && b >= 0.0 && b < 360.0 
       && c >= -90.0 && c <= 90.0 ) {
    dlist.insertBlock( block, d, b, c, r, before );
  }
  showData();
}

#ifdef HAS_LRUD
void 
QTshotWidget::doInsertLRUD( DBlock * block, 
                       const QString & L, const QString & R, 
                       const QString & U, const QString & D,
                       bool at_from )
{
#if 0
  double a = block->Compass();
  if ( ! at_from ) a += 180.0;
  double d, b, c;
  if ( ! L.isEmpty() ) {
    // if ( (d = atof( L.TO_CHAR() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( L )) > 0.0 ) {
      b = a + 270; while ( b >= 360.0 ) b-= 360.0;
      c = 0.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! R.isEmpty() ) {
    // if ( (d = atof( R.TO_CHAR() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( R )) > 0.0 ) {
      b = a + 90; while ( b >= 360.0 ) b-= 360.0;
      c = 0.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! U.isEmpty() ) {
    // if ( (d = atof( U.TO_CHAR() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( U )) > 0.0 ) {
      b = 0.0;
      c = 90.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! D.isEmpty() ) {
    // if ( (d = atof( D.TO_CHAR() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( D )) > 0.0 ) {
      b = 0.0;
      c = -90.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
#else
  double ls = units.length_factor;
  bool insert = false;
  double l=0.0, r=0.0, u=0.0, d=0.0;
  if ( ! L.isEmpty() ) {
    l = Locale::ToDouble( L ) / ls; // store distances internally in meters
    if ( l < 0.0 ) { l = 0.0; } else { insert = true; }
  }
  if ( ! R.isEmpty() ) {
    r = Locale::ToDouble( R ) / ls;
    if ( r < 0.0 ) { r = 0.0; } else { insert = true; }
  }
  if ( ! U.isEmpty() ) {
    u = Locale::ToDouble( U ) / ls;
    if ( u < 0.0 ) { u = 0.0; } else { insert = true; }
  }
  if ( ! D.isEmpty() ) {
    d = Locale::ToDouble( D ) / ls;
    if ( d < 0.0 ) { d = 0.0; } else { insert = true; }
  }
  if ( insert ) {
    DBG_CHECK("Insert %s LRUD %.2f %.2f %.2f %.2f [m]\n", 
        at_from? "from": "to", l, r, u, d );
    block->SetLRUD( l, r, u, d, at_from );
  } else {
    DBG_CHECK("Insert %s LRUD none\n", at_from? "from": "to" );
  }
#endif
}
#endif // HAS_LRUD

void
QTshotWidget::updateExtCanvases( DBlock * b )
{ 
  DBG_CHECK("updateExtCanvas() block %s %s \n", b->From(), b->To() );

  dlist.resetNum();
  for ( size_t k=0; k<extCanvases.plotsSize(); ++k ) 
    extCanvases[k] -> doExtend( b, b->Extend(), false );
  for ( size_t k=0; k<planCanvases.plotsSize(); ++k ) 
    planCanvases[k] -> redrawPlot();
  if ( _3DCanvas )
    _3DCanvas->redrawPlot();
}


void
QTshotWidget::doData()
{
  DBG_CHECK("doData begin\n");
  download = false;
  DataWidget data_dialog( this );
  if ( download ) {
    distoxReset();
    downloadData( );
    onOffButtons( dlist.listSize() > 0 );
    if ( !append && dlist.listSize() > 0 ) {
      // TODO ask date and description
      CenterlineWidget dialog( this );
    }
  }
}

class DownloadThread : public QThread
{
  public:
    DistoX * disto;
    int status;

    DownloadThread( DistoX * d )
      : disto( d )
      , status( 0 )
    { }

    int getStatus() const { return status; }

    void run();
};

void
DownloadThread::run()
{
  // -1: ask the number of data to the distox
  status = ( disto->download( -1 ) ) ? 1 : -1;
}

bool
QTshotWidget::getDistoModes( bool & calib, bool & silent,
                             bool & grad, bool & compass )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.readMode();
  if ( mode < 0 ) return false;
  // TODO use other distox status info
  // bt = IS_STATUS_BT(mode);
  grad   = IS_STATUS_GRAD(mode);
  compass = IS_STATUS_COMPASS(mode);
  calib  = IS_STATUS_CALIB(mode);
  silent = IS_STATUS_SILENT(mode);
  return true;
}

bool 
QTshotWidget::setCalibMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setCalib( on );
  return ( on == ( mode == 1 ) );
}

bool 
QTshotWidget::setSilentMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setSilent( on );
  return ( on == ( mode == 1 ) );
}

bool 
QTshotWidget::setGradMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setGrad( on );
  return ( on == ( mode == 1 ) );
}


bool 
QTshotWidget::setCompassMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setCompass( on );
  return ( on == ( mode == 1 ) );
}

void 
QTshotWidget::downloadData( )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');

  DistoX disto( device, log );
  disto.setListener( this );

  DownloadThread t( &disto );
  t.start();
  while ( t.getStatus() == 0 ) {
    QTimer timer(this);
    // timer.changeInterval( 100 );
    connect(&timer, SIGNAL(timeout()), this, SLOT(update()) );
    timer.start( 100 );
    repaint(0,0,-1,-1);
  }
  distoxDone();

  // if ( ! disto.download() ) {
  if ( t.getStatus() != 1 ) {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("shot_download_failed" ) );
  } else {
    unsigned int nc = disto.calibrationSize();
    if ( nc > 0 ) {
      int day, month, year;
      GetDate( &day, &month, &year );
      char filename[32];
      sprintf(filename, "calib-%04d%02d%02d.txt", year, month, day );
      FILE * fpc = fopen( filename, "w" );
      if ( fpc ) {
        std::ostringstream oss;
        oss << lexicon("read_") << nc << " "
            << lexicon("calibration_data_save")
            << filename;
        QMessageBox::warning(this, lexicon("qtopo_shot"), oss.str().c_str() );
        int16_t gx, gy, gz, mx, my, mz;
        while ( disto.nextCalibration( gx, gy, gz, mx, my, mz ) ) {
          fprintf(fpc, "0x%04x 0x%04x 0x%04x ", gx, gy, gz );
          fprintf(fpc, "0x%04x 0x%04x 0x%04x ", mx, my, mz );
          fprintf(fpc,"-1 0\n"); 
        }
        fclose( fpc );
      } else {
        std::ostringstream oss;
        oss << lexicon("read_") << nc << " "
            << lexicon("calibration_data_no_save") 
            << filename;
        QMessageBox::warning(this, lexicon("qtopo_shot"), oss.str().c_str() );
      }
    }
    // append, smart, backward
    if ( !append ) {
      closePlots();           // close all plots
      setBaseBlock( NULL );   // erase base block pixmap
      collapse = true;        // reset onCollapse action
      if ( table != NULL ) {  // set all table rows visible
        for (int row = 0; row <table->rowCount(); ++row ) {
          table->showRow( row );
        }
      }
    }
    dlist.loadDisto( disto, append, smart, splay_at, backward );
    showData();
  }
}
// ----------------------------------------------------------------------
// Centerline info  widget

CenterlineWidget::CenterlineWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_centerline") );
  QVBoxLayout* vbl = new QVBoxLayout();
  setLayout( vbl );
  // vb->setAutoAdd(TRUE);
  DEFINE_HB;

  CREATE_HB;
  int y,m,d;
  GetDate( &d, &m, &y );
  char dstr[16];
  // sprintf(dstr, "%04d-%02d-%02d", y, m, d); 
  Locale::ToDate( dstr, y, m, d );
  hbl->addWidget( new QLabel( lexicon("date"), hb ) );
  date  = new QLineEdit( dstr, hb );
  hbl->addWidget( date );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("author"), hb ) );
  author = new QLineEdit( "", hb );
  hbl->addWidget( author );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("copyright"), hb ) );
  copyright = new QLineEdit( "", hb );
  hbl->addWidget( copyright );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("description"), hb ) );
  descr = new QLineEdit( "", hb );
  hbl->addWidget( descr );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c1 );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), hb );
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c2 );
  vbl->addWidget( hb );

  exec();
}

void
CenterlineWidget::doOK()
{
  hide();
  parent->setDateAndDescription( date->text().TO_CHAR(),
                                 descr->text().TO_CHAR(),
                                 author->text().TO_CHAR(),
                                 copyright->text().TO_CHAR() );
}

// ----------------------------------------------------------------------
// Toggle widget: DistoX modes

ToggleWidget::ToggleWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
  , isCalib( false )
  , isSilent( false )
  , isGrad( false )
  , isCompass( true )
  , calibBtn( NULL )
  , silentBtn( NULL )
  , gradBtn( NULL )
  , compassBtn( NULL )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_toggle") );
  if ( ! parent->getDistoModes( isCalib, isSilent, isGrad, isCompass ) ) {
    QVBoxLayout* vbl = new QVBoxLayout(this);
    // vb->setAutoAdd(TRUE);
    vbl->addWidget( new QLabel( lexicon("status_mode"), this ) );
    vbl->addWidget( new QLabel( lexicon("failed_mode"), this ) );
    QPushButton * c2 = new QPushButton( tr( lexicon("close") ), this );
    connect( c2, SIGNAL(clicked()), this, SLOT(doClose()) );
    vbl->addWidget( c2 );
  } else {
    QLabel * label;
    QGridLayout * gbl = new QGridLayout( this );
    label = new QLabel( lexicon("status_mode"), this );
    gbl->addWidget( label,    0, 0 );
    calibBtn = new QCheckBox( lexicon("mode_calib"), this );
    calibBtn->setCheckState ( isCalib ? Qt::Checked : Qt::Unchecked ); 
    gbl->addWidget( calibBtn, 1, 0 );
    silentBtn = new QCheckBox( lexicon("mode_silent"), this );
    silentBtn->setCheckState ( isSilent ? Qt::Checked : Qt::Unchecked ); 
    gbl->addWidget( silentBtn, 2, 0 );
    gradBtn = new QCheckBox( lexicon("mode_grad"), this );
    gradBtn->setCheckState ( isGrad ? Qt::Checked : Qt::Unchecked ); 
    gbl->addWidget( gradBtn, 3, 0 );
    compassBtn = new QCheckBox( lexicon("mode_compass"), this );
    compassBtn->setCheckState ( isCompass ? Qt::Checked : Qt::Unchecked ); 
    gbl->addWidget( compassBtn, 4, 0 );

    connect( calibBtn, SIGNAL(stateChanged(int)), this, SLOT(doCalib(int)) );
    connect( silentBtn, SIGNAL(stateChanged(int)), this, SLOT(doSilent(int)) );
    // TODO
    // connect( gradBtn, SIGNAL(stateChanged(int)), this, SLOT(doGrad(int)) );
    // connect( compassBtn, SIGNAL(stateChanged(int)), this, SLOT(doCompass(int)) );
   

    QPushButton * c2 = new QPushButton( tr( lexicon("close") ), this );
    gbl->addWidget( c2, 5, 0 );
    connect( c2, SIGNAL(clicked()), this, SLOT(doClose()) );
  }

  exec();
}

void
ToggleWidget::doCalib(int state) 
{
  bool calib = (state == Qt::Checked);
  if ( parent->setCalibMode( calib ) ) {
    isCalib = calib;
  } else {
    Language & lexicon = Language::Get();
    QMessageBox::warning(this, lexicon("failed_disto"), lexicon("failed_toggle") );
    calibBtn->setCheckState ( isCalib ? Qt::Checked : Qt::Unchecked );
  }
}

void
ToggleWidget::doSilent(int state) 
{
  bool silent = (state == Qt::Checked);
  if ( parent->setSilentMode( silent ) ) {
    isSilent = silent;
  } else {
    Language & lexicon = Language::Get();
    QMessageBox::warning(this, lexicon("failed_disto"), lexicon("failed_toggle") );
    silentBtn->setCheckState ( isSilent ? Qt::Checked : Qt::Unchecked );
  }
}

void
ToggleWidget::doGrad(int state) 
{
#if 0
  bool grad = (state == Qt::Checked);
  if ( parent->SetGradMode( grad ) ) {
    isGrad = grad;
  } else {
    Language & lexicon = Language::Get();
    QMessageBox::warning(this, lexicon("failed_disto"), lexicon("failed_toggle") );
    gradBtn->setCheckState ( isGrad ? Qt::Checked : Qt::Unchecked );
  }
#else
  state = state;
  gradBtn->setCheckState ( isGrad ? Qt::Checked : Qt::Unchecked );
#endif
}

void
ToggleWidget::doCompass(int state) 
{
#if 0
  bool compass = (state == Qt::Checked);
  if ( parent->SetCompassMode( silent ) ) {
    isCompass = compass;
  } else {
    Language & lexicon = Language::Get();
    QMessageBox::warning(this, lexicon("failed_disto"), lexicon("failed_toggle") );
    compassBtn->setCheckState ( isCompass ? Qt::Checked : Qt::Unchecked );
  }
#else
  state = state;
  compassBtn->setCheckState ( isCompass ? Qt::Checked : Qt::Unchecked );
#endif
}

// ----------------------------------------------------------------------
// Info widget

InfoWidget::InfoWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_info") );

  QVBoxLayout* vbl = new QVBoxLayout(this);
  vbl->setSpacing( 0 );
  // vb->setAutoAdd(TRUE);

  DataList * dlist = parent->getList();
  if ( dlist->centerlineSize() > 0 ) dlist->doNum();
  const NumStats & stats = dlist->getNum().getStats();
  QString n_stations, n_shots, n_loops;
  n_stations.setNum( stats.n_stations );
  n_shots.setNum( stats.n_shots );
  n_loops.setNum( stats.n_loops );
  std::ostringstream ossz;
  ossz.precision( 1 );
  ossz.setf( std::ios::fixed, std::ios::floatfield );
  ossz << stats.delta_z() << " (" << stats.z_max << " " << stats.z_min << ")";
  std::ostringstream ossn;
  ossn.precision( 1 );
  ossn.setf( std::ios::fixed, std::ios::floatfield );
  ossn << stats.delta_north() << " (" << stats.n_max << " " << stats.n_min << ")";
  std::ostringstream osse;
  osse.precision( 1 );
  osse.setf( std::ios::fixed, std::ios::floatfield );
  osse << stats.delta_east() << " (" << stats.e_max << " " << stats.e_min << ")";
  
  DEFINE_HB;

  vbl->addWidget( new QLabel( lexicon("survey_data"), this ) );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("stations"), hb) );
  hbl->addWidget( new QLabel( n_stations, hb) );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("shots"), hb) );
  hbl->addWidget( new QLabel( n_shots, hb) );
  vbl->addWidget( hb );
 
  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("loops"), hb) );
  hbl->addWidget( new QLabel( n_loops, hb) );
  vbl->addWidget( hb );
 
  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("delta_z"), hb) );
  hbl->addWidget( new QLabel( ossz.str().c_str(), hb) );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("delta_north"), hb) );
  hbl->addWidget( new QLabel( ossn.str().c_str(), hb) );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("delta_east"), hb) );
  hbl->addWidget( new QLabel( osse.str().c_str(), hb) );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
};


// ----------------------------------------------------------------------
// Option widget

OptionsWidget::OptionsWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_options") );
  // QVBoxLayout* vb = new QVBoxLayout(this, 8);
  // vb->setAutoAdd(TRUE);
  // QHBOX * hb = new QHBOX(this);
  QGridLayout * gbl = new QGridLayout( this );
  QLabel * label;
  label = new QLabel( lexicon("length_units"), this );
  length_btn[0] = new QRadioButton( lexicon("m"), this );
  length_btn[1] = new QRadioButton( lexicon("ft"), this );
  gbl->addWidget( label,    0, 0 );
  gbl->addWidget( length_btn[0], 0, 1 );
  gbl->addWidget( length_btn[1],  0, 2 );
  QButtonGroup * m_group_length = new QButtonGroup( );
  m_group_length->addButton( length_btn[0] );
  m_group_length->addButton( length_btn[1] );

  label = new QLabel( lexicon("angle_units"), this);
  angle_btn[0] = new QRadioButton( lexicon("deg"), this );
  angle_btn[1] = new QRadioButton( lexicon("grad"), this );
  gbl->addWidget( label,    1, 0 );
  gbl->addWidget( angle_btn[0],   1, 1 );
  gbl->addWidget( angle_btn[1],  1, 2 );
  QButtonGroup * m_group_angle = new QButtonGroup( );
  m_group_angle->addButton( angle_btn[0] );
  m_group_angle->addButton( angle_btn[1] );
 
/*  
  label = new QLabel( lexicon("export"), this );
  QButtonGroup * m_group_export = new QButtonGroup( );
  export_btn[0] = new QRadioButton( "th", this);
  export_btn[1] = new QRadioButton( "svx", this);
  export_btn[2] = new QRadioButton( "dat", this);
  export_btn[3] = new QRadioButton( "top", this);
  m_group_export->addButton( export_btn[0] );
  m_group_export->addButton( export_btn[1] );
  m_group_export->addButton( export_btn[2] );
  m_group_export->addButton( export_btn[3] );

  gbl->addWidget( label,     2, 0 );
  gbl->addWidget( export_btn[0], 2, 1 );
  gbl->addWidget( export_btn[1], 2, 2 );
  gbl->addWidget( export_btn[2], 2, 3 );
  gbl->addWidget( export_btn[3], 2, 4 );
*/

  label = new QLabel( lexicon("distox_device"), this );
  m_device = new QLineEdit( parent->getDevice(), this ); 
  gbl->addWidget( label, 3, 0 );
  gbl->addWidget( m_device, 3, 1, 1, 4 );

  // hb = new QHBOX(this);
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this );
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  gbl->addWidget( c1, 4, 0 );
  gbl->addWidget( c2, 4, 1 );

  SetValues();

}

void
OptionsWidget::SetValues()
{
  if ( parent->lengthUnits() == LENGTH_METER ) {
    length_btn[0]->setChecked( TRUE );
  } else if ( parent->lengthUnits() == LENGTH_FEET ) {
    length_btn[1]->setChecked( TRUE );
  }

  if ( parent->angleUnits() == ANGLE_DEGREE ) {
    angle_btn[0]->setChecked( TRUE );
  } else if ( parent->angleUnits() == ANGLE_GRAD ) {
    angle_btn[1]->setChecked( TRUE );
  }

/*
  switch ( parent->exportType() ) {
    case EXPORT_THERION:    export_btn[0]->setChecked( TRUE ); break;
    case EXPORT_SURVEX:     export_btn[1]->setChecked( TRUE ); break;
    case EXPORT_COMPASS:    export_btn[2]->setChecked( TRUE ); break;
    case EXPORT_POCKETTOPO: export_btn[3]->setChecked( TRUE ); break;
    default: break;
  }
*/

  exec();
}

void
OptionsWidget::doOK()
{
  hide();
  if ( length_btn[0]->isChecked() ) {
    parent->setLengthUnits( LENGTH_METER );
  } else if ( length_btn[1]->isChecked() ) {
    parent->setLengthUnits( LENGTH_FEET );
  }

  if ( angle_btn[0]->isChecked() ) {
    parent->setAngleUnits( ANGLE_DEGREE );
  } else if ( angle_btn[1]->isChecked() ) {
    parent->setAngleUnits( ANGLE_GRAD );
  }

/*
  if ( export_btn[0]->isChecked() ) {
    parent->setExportType( EXPORT_THERION ); 
  } else if ( export_btn[1]->isChecked() ) {
    parent->setExportType( EXPORT_SURVEX );
  } else if ( export_btn[2]->isChecked() ) {
    parent->setExportType( EXPORT_COMPASS );
  } else if ( export_btn[3]->isChecked() ) {
    parent->setExportType( EXPORT_POCKETTOPO );
  }
*/

  if ( ! m_device->text().isEmpty() ) {
    parent->setDevice( m_device->text().TO_CHAR() );
  }

  parent->showData();
}

// ----------------------------------------------------------------------
// Comment

#define COMMENT_ITEM_HEIGHT 30

#ifdef HAS_LRUD
CommentWidget::CommentWidget( QTshotWidget * my_parent, DBlock * b, bool do_lrud )
#else
CommentWidget::CommentWidget( QTshotWidget * my_parent, DBlock * b )
#endif
  : QDialog( my_parent )
  , parent( my_parent )
  , block( b )
{
  double ls = parent->lengthFactor();
  double as = parent->angleFactor();
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("shot_comment") );

  std::ostringstream oss;
  int n_stations = 0; // number of stations
  if ( block->hasFromStation() ) {
    ++ n_stations;
    oss << " " << lexicon("from") << " " << block->fromStation();
  }
  if ( block->hasToStation() ) {
    ++ n_stations;
    oss << " " << lexicon("to") << " " << block->toStation();
  }

  QVBoxLayout* vbl = new QVBoxLayout(this);
  vbl->setSpacing( 0 );
  // vb->setAutoAdd(TRUE);
  
  DEFINE_HB;

  vbl->addWidget( new QLabel( oss.str().c_str(), this ) );

  if ( block->nrBlocklets() > 1 ) {
    QWidget * vb1 = new QWidget( this );
    QVBoxLayout * vbl1 = new QVBoxLayout( vb1 );
    vbl1->setSpacing( 0 );
    DBlocklet * bl = block->getBlocklets();
    while ( bl ) {
      QString str = Locale::ToString( bl->distance * ls, 2 )
          + "   " + Locale::ToString( bl->compass * as, 1 )
          + "   " + Locale::ToString( bl->clino * as, 1 );
      vbl1->addWidget( new QLabel( str, vb1 ) );
      bl = bl->next();
    }
    vbl->addWidget( vb1 );
  }

  QButtonGroup * m_group = new QButtonGroup( );
  properties = new QRadioButton( lexicon("properties"), this );
  vbl->addWidget( properties );
  properties->setChecked( false );
  m_group->addButton( properties );
  comment = new QLineEdit( b->getComment(), this );
  connect( comment, SIGNAL(textChanged(const QString &)), this, SLOT(doComment(const QString &)) );
  vbl->addWidget( comment );

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  hbl->addWidget( new QLabel( lexicon("ext_box"), hb ) );
  extBox = new QComboBox( hb );
  connect( extBox, SIGNAL(activated(int)), this, SLOT(doExtend(int)) );
  extBox->addItem( lexicon("none") );  // addItem( lexicon("none") );
  extBox->addItem( lexicon("left") );
  extBox->addItem( lexicon("right") );
  extBox->addItem( lexicon("vertical") );
  extBox->addItem( lexicon("ignore") );
  extBox->setCurrentIndex( b->Extend() );
  hbl->addWidget( extBox );
  vbl->addWidget( hb );

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  hbl->addWidget( new QLabel( lexicon("flag_box"), hb ) );
  flagBox = new QComboBox( hb );
  connect( flagBox, SIGNAL(activated(int)), this, SLOT(doFlag(int)) );
  flagBox->addItem( lexicon("none") );
  flagBox->addItem( lexicon("surface") );
  flagBox->addItem( lexicon("duplicate") );
  flagBox->setCurrentIndex( b->Flag() );
  hbl->addWidget( flagBox );
  vbl->addWidget( hb );

  // CREATE_HB;
  // swapBox = new QCheckBox( lexicon("swap_from_to"), hb );
  // connect( swapBox, SIGNAL(toggled(bool)), this, SLOT(doSwap(bool)) );
  // hbl->addWidget( swapBox );
  // vbl->addWidget( hb );

  #ifdef HAS_LRUD
    if ( do_lrud ) {
      CREATE_HB;
      hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
      lrud = new QRadioButton( lexicon("LRUD"), hb );
      lrud->setChecked( false );
      m_group->addButton( lrud );
      hbl->addWidget( lrud );
      vbl->addWidget( hb );
    } else {
      lrud = NULL;
    }
  #endif

  if ( block->hasFromStation() ) {
    CREATE_HB;
    hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
    base_station = new QRadioButton( lexicon("base_station"), hb );
    base_station->setChecked( false );
    m_group->addButton( base_station );
    hbl->addWidget( base_station );
    vbl->addWidget( hb );
  } else {
    base_station = NULL;
  }

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  renumber = new QRadioButton( lexicon("renumber"), hb );
  renumber->setChecked( false );
  m_group->addButton( renumber );
  hbl->addWidget( renumber );
  vbl->addWidget( hb );

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  tomerge = new QRadioButton( lexicon("merge_next"), hb );
  tomerge->setChecked( false );
  m_group->addButton( tomerge );
  hbl->addWidget( tomerge );
  vbl->addWidget( hb );

  if ( block->nrBlocklets() > 1 ) {
    CREATE_HB;
    hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
    tosplit = new QRadioButton( lexicon("split"), hb );
    tosplit->setChecked( false );
    m_group->addButton( tosplit );
    hbl->addWidget( tosplit );
    vbl->addWidget( hb );
  } else {
    tosplit = NULL;
  }

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  toinsert = new QRadioButton( lexicon("insert_shot"), hb );
  toinsert->setChecked( false );
  m_group->addButton( toinsert );
  hbl->addWidget( toinsert );
  vbl->addWidget( hb );

  CREATE_HB;
  hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  todrop = new QRadioButton( lexicon("delete_shot"), hb );
  todrop->setChecked( false );
  m_group->addButton( todrop );
  hbl->addWidget( todrop );
  vbl->addWidget( hb );

  if ( n_stations == 2 ) {
    CREATE_HB;
    hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
    cross_section = new QRadioButton( lexicon("cross_section"), hb );
    cross_section->setChecked( false );
    section_name = new QLineEdit( "xsect", hb );
    m_group->addButton( cross_section );
    hbl->addWidget( cross_section );
    hbl->addWidget( section_name );
#if 0 // not defined  USER_HORIZONTAL_SECTION
    reversed = new QCheckBox( lexicon("reversed"), hb );
    hbl->addWidget( reversed );
#else
    vbl->addWidget( hb );
    CREATE_HB;
    reversed = new QCheckBox( lexicon("reversed"), hb );
    horizontal = new QCheckBox( lexicon("horizontal"), hb );
    hbl->addWidget( reversed );
    hbl->addWidget( horizontal );
#endif
    vbl->addWidget( hb );
  } else {
    cross_section = NULL;
    reversed = NULL;
#if 0 // not defined  USER_HORIZONTAL_SECTION
#else
    horizontal = NULL;
#endif
  }

  CREATE_HB;
  // hb->setMaximumHeight( COMMENT_ITEM_HEIGHT );
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}

void
CommentWidget::SetValues()
{
}

void
CommentWidget::doComment( const QString & DBG(text) )
{
  DBG_CHECK("CommentWidget::doComment() %s\n", text.TO_CHAR() );
  properties->setChecked( true );
}

void
CommentWidget::doExtend( int DBG(extend) )
{
  DBG_CHECK("CommentWidget::doExtend %d\n", extend );
  properties->setChecked( true );
}

void
CommentWidget::doFlag( int DBG(flag) )
{
  DBG_CHECK("CommentWidget::doFlag %d\n", flag );
  properties->setChecked( true );
}

// void
// CommentWidget::doSwap( bool /* status */ )
// {
//   properties->setChecked( true );
// }


void
CommentWidget::doOK()
{
  hide();
  bool need_to_show_data = false;
  bool need_to_compute_data = false;
  if ( properties->isChecked() ) {
    need_to_show_data = true;
    block->setComment( comment->text().TO_CHAR() );
    if ( block->Extend() != extBox->currentIndex() ) {
      need_to_compute_data = true;
      block->setExtend( extBox->currentIndex() ); // currentIndex()
    }
    if ( block->Flag() != flagBox->currentIndex() ) {
      block->setFlag( flagBox->currentIndex() );
    }
    // if ( swapBox->isChecked() ) {
    //   if ( ! block->isSplay() ) { // not necessary because block can swap only if it has both stations
    //     if ( block->swapStations() ) {
    //       need_to_compute_data = true;
    //     }
    //   }
    // }
  } else if ( base_station && base_station->isChecked() ) {
    parent->setBaseBlock( block );
    need_to_show_data = true;
    need_to_compute_data = true;
#ifdef HAS_HLRUD
  } else if ( lrud && lrud->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    LRUDWidget lrud_dialog( parent, block );
#endif
  } else if ( renumber->isChecked() ) {
    need_to_show_data = true;
    block->renumber( parent->getSplay() == SPLAY_BEFORE_SHOT );
  } else if ( tomerge->isChecked() ) {
    need_to_show_data = true;
    parent->getList()->mergeBlock( block );
  } else if ( tosplit && tosplit->isChecked() ) {
    need_to_show_data = true;
    parent->getList()->splitBlock( block );
  } else if ( toinsert->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    parent->doInsertBlock( block );
    // parent->GetList()->insertBlock( block );
  } else if ( todrop->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    parent->drop( block );
  } else if ( cross_section && cross_section->isChecked() && ! section_name->text().isEmpty()) {
#if 0 // not defined  USER_HORIZONTAL_SECTION
    parent->doCrossSection( block, reversed->isChecked() );
#else
    parent->doCrossSection( block, section_name->text(),
                             reversed->isChecked(), ! horizontal->isChecked() );
#endif
  }

  if ( need_to_show_data ) {
    if ( need_to_compute_data ) {
      parent->updateExtCanvases( block );
    }
    parent->showData();
  }
}


// ----------------------------------------------------------------
// DistoX Data download dialog

DataWidget::DataWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("distox_download") ); // setWindowTitle( lexicon("distox_download") );

  QGridLayout* vb = new QGridLayout( this );
  QLabel * label;

  label = new QLabel( lexicon("date"), this );
  char date_str[16];
  parent->getDate( date_str );
  date = new QLineEdit( date_str, this );
  vb->addWidget( label,  0, 0 );
  vb->addWidget( date,  0, 1 );

  label = new QLabel( lexicon("description"), this );
  description = new QLineEdit( parent->getDescription(), this );
  vb->addWidget( label,  1, 0 );
  vb->addWidget( description,  1, 1 );

  label = new QLabel( lexicon("device"), this );
  device = new QLineEdit( parent->getDevice(), this );
  vb->addWidget( label,  2, 0 );
  vb->addWidget( device, 2, 1 );

  label  = new QLabel( lexicon("append_shots"), this );
  append = new QCheckBox( "", this );
  append->setChecked( parent->getAppend() );
  vb->addWidget( label,  3, 0 );
  vb->addWidget( append, 3, 1 );

  label = new QLabel( lexicon("guess_centerline"), this );
  smart = new QCheckBox( "", this );
  smart->setChecked( parent->getSmart() );
  vb->addWidget( label, 4, 0 );
  vb->addWidget( smart, 4, 1 );

  label    = new QLabel( "...   ", this );
  backward = new QCheckBox( lexicon("backward"), this );
  backward->setChecked( parent->getBackward() );
  vb->addWidget( label,    5, 0 );
  vb->addWidget( backward, 5, 1 );

  label  = new QLabel( lexicon("splay_shots"), this );
  splay1 = new QCheckBox( lexicon("from_station"), this );
  splay2 = new QCheckBox( lexicon("to_station"), this );
  splay1->setChecked( parent->getSplay() == SPLAY_AFTER_SHOT );
  splay2->setChecked( parent->getSplay() == SPLAY_BEFORE_SHOT );
  connect( splay1, SIGNAL(toggled(bool)), this, SLOT(doSplay1(bool)) );
  connect( splay2, SIGNAL(toggled(bool)), this, SLOT(doSplay2(bool)) );
  vb->addWidget( label,  6, 0 );
  vb->addWidget( splay1, 6, 1 );
  label  = new QLabel( "   ", this );
  vb->addWidget( label,  7, 0 );
  vb->addWidget( splay2, 7, 1 );

  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this);
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vb->addWidget( c1, 8, 0 );
  vb->addWidget( c2, 8, 1 );

  exec();
}

void
DataWidget::doSplay1( bool ok )
{
  if ( ok == splay1->isChecked() ) splay2->setChecked( ! ok );
}
  
void
DataWidget::doSplay2( bool ok )
{
  if ( ok == splay2->isChecked() ) splay1->setChecked( ! ok );
}
  

void
DataWidget::doOK()
{
  hide();
  parent->setDateAndDescription( date->text().TO_CHAR(),
                                 description->text().TO_CHAR() );
  parent->setDownload( true,
                       device->text().TO_CHAR(),
                       append->isChecked(),
                       smart->isChecked(), 
                       splay1->isChecked(),
                       splay2->isChecked(),
                       backward->isChecked() );
  // parent->downloadData( );
}

// ----------------------------------------------------------------
//

ExitWidget::ExitWidget( QTshotWidget * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_exit") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  DEFINE_HB;
  vbl->addWidget( new QLabel( lexicon("exit_question"), this ) );

  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );
  
  exec();
}

SplashWidget::SplashWidget( QTshotWidget * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  IconSet * icon = IconSet::Get();
  setWindowTitle( lexicon("qtopo_shot") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  
  vbl->addWidget( new QLabel( lexicon("what_do"), this ) );
  
  QPushButton * c;
  c = new QPushButton( icon->Open(), lexicon("open_survey"), this );
  connect( c, SIGNAL(clicked()), this, SLOT(doOpen()) );
  vbl->addWidget( c );

  c = new QPushButton( icon->Data(), lexicon("download_shots"), this );
  connect( c, SIGNAL(clicked()), this, SLOT(doData()) );
  vbl->addWidget( c );

  c = new QPushButton( tr( lexicon("cancel") ), this);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vbl->addWidget( c );

  exec();
}

// ----------------------------------------------------------------
// Insert Shot Widget

InsertWidget::InsertWidget( QTshotWidget * p, DBlock * blk ) 
  : QDialog( p )
  , parent( p )
  , block( blk )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_insert_shot") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  
  QString label( lexicon("insert_shot_at") );
  label += blk->hasFromStation() ? blk->fromStation() : "..." ;
  label += " - ";
  label += blk->hasToStation()? blk->toStation() : "..." ;
  vbl->addWidget( new QLabel( label, this ) );
  
  DEFINE_HB;

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("tape"), hb ) );
  distance = new QLineEdit( "", hb );
  hbl->addWidget( distance );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("azimuth"), hb ) );
  compass = new QLineEdit( "", hb );
  hbl->addWidget( compass );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("clino"), hb ) );
  clino = new QLineEdit( "", hb );
  hbl->addWidget( clino );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("insert_before"), hb ) );
  before = new QCheckBox( "", hb );
  before->setChecked( false );
  hbl->addWidget( before );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}

void
InsertWidget::doOK()
{
  hide();
  parent->doInsertBlock( block,
                       distance->text().TO_CHAR(),
                       compass->text().TO_CHAR(),
                       clino->text().TO_CHAR(),
                       before->isChecked() );
}

#ifdef HAS_LRUD
LRUDWidget::LRUDWidget( QTshotWidget * p, DBlock * b )
  : QDialog( p )
  , parent( p )
  , blk( b )
{ 
  double ls = parent->lengthFactor();
  bool ok = false;
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_insert_LRUD") );
  QGridLayout* vb = new QGridLayout(this, 8);
  QLabel label( lexicon("write_LRUD"), this );
  vb->addMultiCellWidget( &label, 0, 0, 0, 5);
  vb->addWidget( new QLabel( lexicon("station"), this), 1, 0 );
  vb->addWidget( new QLabel( lexicon("left"), this), 1, 1 );
  vb->addWidget( new QLabel( lexicon("right"), this), 1, 2 );
  vb->addWidget( new QLabel( lexicon("up"), this), 1, 3 );
  vb->addWidget( new QLabel( lexicon("down"), this), 1, 4 );
  if ( blk->hasFromStation() ) {
    ok = true;
    vb->addWidget( new QLabel( blk->From(), this), 2, 0 );
    LRUD * lrud = blk->getLRUD( 0 ); // "From" LRUD
    if ( lrud == NULL ) {
      L1 = new QLineEdit("", this );
      R1 = new QLineEdit("", this );
      U1 = new QLineEdit("", this );
      D1 = new QLineEdit("", this );
    } else {
      // display distances with user units (and locale)
      // internally they are stored in meters
      L1 = new QLineEdit( Locale::ToString( lrud->left * ls, 2 ), this );
      R1 = new QLineEdit( Locale::ToString( lrud->right * ls, 2 ), this );
      U1 = new QLineEdit( Locale::ToString( lrud->up * ls, 2 ), this );
      D1 = new QLineEdit( Locale::ToString( lrud->down * ls, 2 ), this );
    }
    vb->addWidget( L1, 2, 1 );
    vb->addWidget( R1, 2, 2 );
    vb->addWidget( U1, 2, 3 );
    vb->addWidget( D1, 2, 4 );
  } else {
    L1 = R1 = U1 = D1 = NULL;
  }
  if ( blk->hasToStation() ) {
    ok = true;
    vb->addWidget( new QLabel( blk->To(), this), 3, 0 );
    LRUD * lrud = blk->getLRUD( 1 ); // "To" LRUD
    if ( lrud == NULL ) {
      L2 = new QLineEdit("", this );
      R2 = new QLineEdit("", this );
      U2 = new QLineEdit("", this );
      D2 = new QLineEdit("", this );
    } else {
      L2 = new QLineEdit( Locale::ToString( lrud->left * ls, 2 ), this );
      R2 = new QLineEdit( Locale::ToString( lrud->right * ls, 2 ), this );
      U2 = new QLineEdit( Locale::ToString( lrud->up * ls, 2 ), this );
      D2 = new QLineEdit( Locale::ToString( lrud->down * ls, 2 ), this );
    }
    vb->addWidget( L2, 3, 1 );
    vb->addWidget( R2, 3, 2 );
    vb->addWidget( U2, 3, 3 );
    vb->addWidget( D2, 3, 4 );
  } else {
    L2 = R2 = U2 = D2 = NULL;
  }
    
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), this );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  vb->addWidget( c, 4, 3 );
  c = new QPushButton( tr( lexicon("cancel") ), this );
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vb->addWidget( c, 4, 4 );

  exec();
}

void
LRUDWidget::doOK()
{
  hide();
  if ( blk->hasFromStation() ) {
    DBG_CHECK("LRUD From %s %s %s %s\n",
        L1->text().TO_CHAR(), R1->text().TO_CHAR(), U1->text().TO_CHAR(), D1->text().TO_CHAR() );
    parent->doInsertLRUD( blk, 
                          L1->text(), R1->text(), U1->text(), D1->text(),
                          true ); // at from
  }
  if ( blk->hasToStation() ) {
    DBG_CHECK("LRUD To %s %s %s %s\n", 
        L2->text().TO_CHAR(), R2->text().TO_CHAR(), U2->text().TO_CHAR(), D2->text().TO_CHAR() );
    parent->doInsertLRUD( blk, 
                          L2->text(), R2->text(), U2->text(), D2->text(),
                          false ); // at to
  }
}
#endif // HAS_LRUD

// ----------------------------------------------------------------
// Survey Info

SurveyInfoWidget::SurveyInfoWidget( QTshotWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
  , edit_team( NULL )
  , edit_prefix( NULL )
  , box_single_survey( NULL )
  , centerline( NULL )
  , survey( NULL )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("survey_info") );
  SurveyInfo * survey_info = parent->getSurveyInfo();
  
  QVBoxLayout* vbl = new QVBoxLayout(this);
  DEFINE_HB;

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("name"), hb ) );
  edit_name = new QLineEdit( survey_info->name, hb );
  hbl->addWidget( edit_name );
  vbl->addWidget( hb );

  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("title"), hb ) );
  edit_title = new QLineEdit( survey_info->title, hb );
  hbl->addWidget( edit_title );
  vbl->addWidget( hb );
  
  ExportType e = parent->exportType();

  if ( e == EXPORT_THERION || e == EXPORT_SURVEX ) { // CENTERLINE COMMANDS --> Therion, Survex
    vbl->addWidget( new QLabel( lexicon("centerline_commands"), this ) );
    centerline = new QTextEdit( this );
    centerline->append( survey_info->therionCenterlineCommand.c_str() ); // FIXME
    vbl->addWidget( centerline );
  } else if ( e == EXPORT_COMPASS ) { // TEAM --> Compass
    vbl->addWidget( new QLabel( lexicon("team"), this ) );
    edit_team = new QLineEdit( survey_info->team, this );
    vbl->addWidget( edit_team );
  }
  if ( e == EXPORT_THERION ) { // SURVEY COMMANDS --> Therion
    vbl->addWidget( new QLabel( lexicon("survey_commands"), this ) );
    survey = new QTextEdit( this );
    if ( survey_info->therionSurveyCommand.empty() ) {
      std::ostringstream oss;
      parent->getSurveyText( oss );
      survey_info->therionSurveyCommand = oss.str();
    }
    survey->append( survey_info->therionSurveyCommand.c_str() );
    vbl->addWidget( survey );
  } else if ( e == EXPORT_COMPASS ) { // STATION PREFIX --> Compass
    vbl->addWidget( new QLabel( lexicon("prefix"), this ) );
    edit_prefix = new QLineEdit( survey_info->compassPrefix, this );
    box_single_survey = new QCheckBox( lexicon("single_survey"), this );
    box_single_survey->setChecked( survey_info->compassSingleSurvey ); 
    vbl->addWidget( edit_prefix );
    vbl->addWidget( box_single_survey );
  }

  CREATE_HB;
  double as = parent->angleFactor();
  hbl->addWidget( new QLabel( lexicon("declination"), hb ) );
  if ( survey_info->declination != DECLINATION_UNDEF ) {
    edit_declination = new QLineEdit( Locale::ToString( survey_info->declination * as, 2 ), hb );
  } else {
    edit_declination = new QLineEdit( "", hb );
  }
  hbl->addWidget( edit_declination );
  vbl->addWidget( hb );

  if (  e == EXPORT_THERION ) { // export THCONFIG
    CREATE_HB;
    box_thconfig =  new QCheckBox( lexicon("thconfig"), hb ); 
    box_thconfig->setChecked( survey_info->therionThconfig );
    vbl->addWidget( box_thconfig );
  }

  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}

void
SurveyInfoWidget::doOK()
{
  hide();
  SurveyInfo * survey_info = parent->getSurveyInfo();
  survey_info->name  = edit_name->text();
  survey_info->title = edit_title->text();
  if ( centerline ) {
    QString txt = centerline->toPlainText();
    if ( ! txt.isEmpty() ) {
      survey_info->therionCenterlineCommand = txt.TO_CHAR();
    } else {
      survey_info->therionCenterlineCommand = "";
    }
  } else {
    survey_info->therionCenterlineCommand = "";
  }

  if ( survey ) {
    QString txt = survey->toPlainText();
    if ( ! txt.isEmpty() ) {
      survey_info->therionSurveyCommand = txt.TO_CHAR();
    } else {
      survey_info->therionSurveyCommand = "";
    }
  } else {
    survey_info->therionSurveyCommand = "";
  }

  if ( edit_team ) {
    survey_info->team = edit_team->text();
  } else {
    survey_info->team = "";
  }

  if ( edit_prefix ) {
    survey_info->compassPrefix = edit_prefix->text().trimmed();
  }
  if ( box_single_survey ) {
    survey_info->compassSingleSurvey = box_single_survey->isChecked();
  }
  if ( edit_declination ) {
    if ( edit_declination->text().isEmpty() ) {
      survey_info->declination = DECLINATION_UNDEF;
    } else {
      double as = parent->angleFactor();
      survey_info->declination = Locale::ToDouble( edit_declination->text().TO_CHAR() ) / as;
      printf("Set declination %s to %.2f as %.2f\n", 
        edit_declination->text().TO_CHAR(), survey_info->declination, as );
    }
  }
  if ( box_thconfig ) {
    survey_info->therionThconfig = box_thconfig->isChecked();
  }

  parent->doExportOK();
}


// -------------------------------------------------------------------
// main widget

void
QTshotWidget::doExport( QAction * action )
{
  DBG_CHECK("doExport() list size %d\n", dlist.listSize() );
  if ( dlist.listSize() == 0 ) return;
  SurveyInfo & survey_info = info.surveyInfo;
  if ( action->text() == lexicon("topolinux") ) {
    doSave();
    return;
  } else if ( action->text() == lexicon("therion") ) {
    export_type = EXPORT_THERION;
    survey_info.exportName = QFileDialog::getSaveFileName( this,
      lexicon("default_th"),
      survey_info.exportName,
      "Therion files (*.th)\nAll (*.*)" );
  } else if ( action->text() == lexicon("compass") ) {
    export_type = EXPORT_COMPASS;
    survey_info.exportName = QFileDialog::getSaveFileName( this,
      lexicon("default_dat"),
      survey_info.exportName,
      "Compass files (*.dat)\nAll (*.*)" );
  } else if ( action->text() == lexicon("survex") ) {
    export_type = EXPORT_SURVEX;
    survey_info.exportName = QFileDialog::getSaveFileName( this,
      lexicon("default_svx"),
      survey_info.exportName,
      "Survex files (*.svx)\nAll (*.*)" );
  } else if ( action->text() == lexicon("pockettopo") ) {
    export_type = EXPORT_POCKETTOPO;
    survey_info.exportName = QFileDialog::getSaveFileName( this,
      lexicon("default_top"),
      survey_info.exportName,
      "PocketTopo files (*.top)\nAll (*.*)" );
  } else {
    fprintf(stderr, "ERROR unknown export type %s\n", action->text().TO_CHAR() );
    return;
  }
  
  if ( ! survey_info.exportName.isEmpty() ) { // got a file name
    SurveyInfoWidget dialog( this );
  }
}

void
QTshotWidget::doExportOK()
{
  if ( export_type == EXPORT_THERION ) {
    if ( saveAsTherion( dlist, info, units ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_th") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_th") );
    }
  } else if ( export_type == EXPORT_SURVEX ) { // survex
    if ( saveAsSurvex( dlist, info, units ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_svx") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_svx") );
    }
  } else if ( export_type == EXPORT_COMPASS ) { // compass
    int max_len;
    if ( saveAsCompass( dlist, info, units, max_len ) ) {
      if ( max_len > 12 ) {
        QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("prefix_too_long") );
      } else {
        QMessageBox::information(this, lexicon("qtopo_shot"),  lexicon("saved_dat") );
      }
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_dat") );
    }
  } else if ( export_type == EXPORT_POCKETTOPO ) { // PocketTopo
    if ( saveAsPocketTopo( dlist, info,  // FIXME
                           planCanvases.plotsSize() > 0 ? planCanvases[0]->getStatus() : NULL,
                           extCanvases.plotsSize() > 0 ? extCanvases[0]->getStatus() : NULL ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_top") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_top") );
    }
  } else {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_export_type") );
  }
}



void 
QTshotWidget::doPlanScrap()
{
  if ( dlist.listSize() == 0 ) return;
  if ( temp_plan_name.isEmpty() ) {
    temp_plan_name.sprintf( "plan-%d", plan_cnt );
    ++plan_cnt;
  } else {
    temp_plan_name = temp_plan_name.simplified();
    temp_plan_name.replace(' ', '_' );
    int last = temp_plan_name.lastIndexOf( QRegExp("[0-9]") );
    QString suffix;
    if ( last >= 0 ) {
      QString prefix = temp_plan_name.left( last );
      suffix = temp_plan_name.right( temp_plan_name.length() - last );
      plan_cnt = suffix.toInt() + 1;
      temp_plan_name = prefix + suffix.sprintf( "%d", plan_cnt );
    } else {
      ++plan_cnt;
      temp_plan_name = temp_plan_name  + suffix.sprintf( "%d", plan_cnt );
    }
  }
  ScrapNameWidget dialog( this, temp_plan_name, MODE_PLAN );
}

void 
QTshotWidget::doExtendedScrap()
{
  if ( dlist.listSize() == 0 ) return;
  if ( temp_ext_name.isEmpty() ) {
    temp_ext_name.sprintf( "ext-%d", ext_cnt );
    ++ext_cnt;
  } else {
    temp_ext_name = temp_ext_name.simplified();
    temp_ext_name.replace(' ', '_' );
    int last = temp_ext_name.lastIndexOf( QRegExp("[0-9]") );
    QString suffix;
    if ( last >= 0 ) {
      QString prefix = temp_ext_name.left( last );
      suffix = temp_ext_name.right( temp_ext_name.length() - last );
      ext_cnt = suffix.toInt() + 1;
      temp_ext_name = prefix + suffix.sprintf( "%d", ext_cnt );
    } else {
      ++ext_cnt;
      temp_ext_name = temp_ext_name  + suffix.sprintf( "%d", ext_cnt );
    }
  }
  ScrapNameWidget dialog( this, temp_ext_name, MODE_EXT );
}



void
QTshotWidget::doNewPlot( QString pname, QString sname, int mode )
{
  if ( dlist.listSize() == 0 ) return;
  PlotCanvas * canvas;
  if ( mode == MODE_PLAN ) {
    temp_plan_name = pname;
    canvas = new PlotCanvas( this, mode, temp_plan_name.TO_CHAR(), sname.TO_CHAR() );
    planCanvases.addPlot( temp_plan_name, canvas ); 
    plan_menu->addAction( temp_plan_name );
    canvas->show();
  } else if ( mode == MODE_EXT ) {
    temp_ext_name = pname;
    canvas = new PlotCanvas( this, mode, temp_ext_name.TO_CHAR(), sname.TO_CHAR() );
    extCanvases.addPlot( temp_ext_name, canvas ); 
    ext_menu->addAction( temp_ext_name );
    canvas->show();
  }
}

void
QTshotWidget::doPlan( QAction * action )
{
  PlotCanvas * canvas = NULL;
  if ( action->text() == lexicon("new") ) {
    doPlanScrap();
    return;
  }
  if ( action->text() == lexicon("open") ) {
    QString filename = QFileDialog::getOpenFileName( this, lexicon("open_file"), "",
                                                     "Th2 files (*.th2)\nAll (*.*)" );
    if ( ! filename.isEmpty() ) {
      PlotTh2Import import( this );
      const char * pname = filename.TO_CHAR();
      canvas = import.loadTh2File( pname );
      if ( canvas ) {
        canvas -> redrawPlot();
        canvas->show();
      }
    }
  } else {
    canvas = planCanvases.getPlot( action->text().TO_CHAR() );
    canvas->show();
  }
}


void
QTshotWidget::doExtended( QAction * action )
{
  PlotCanvas * canvas = NULL;
  if ( action->text() == lexicon("new") ) {
    doExtendedScrap();
    return;
  }

  if ( action->text() == lexicon("open") ) {
    QString filename = QFileDialog::getOpenFileName( this, lexicon("open_file"), "",
                                                     "Th2 files (*.th2)\nAll (*.*)" );
    if ( ! filename.isEmpty() ) {
      PlotTh2Import import( this );
      const char * pname = filename.TO_CHAR();
      canvas = import.loadTh2File( pname );
      if ( canvas ) {
        canvas -> redrawPlot();
        canvas->show();
      }
    }
  } else {
    canvas = extCanvases.getPlot( action->text().TO_CHAR() );
    canvas->show();
  }
}

void
QTshotWidget::do3D()
{
  if ( dlist.listSize() == 0 ) return;
  if ( _3DCanvas == NULL ) {
    _3DCanvas = new PlotCanvas( this, MODE_3D, "3d", "3d" );
  } 
  _3DCanvas->show();
  // this->showCanvas( MODE_3D );
}

void
QTshotWidget::doCrossSection( DBlock * block, QString name, bool reversed, bool vertical )
{
  if ( dlist.listSize() == 0 ) return;
  // assert( block != NULL );
  if ( block == NULL ) {
    QMessageBox::warning( this, lexicon("warning"), lexicon("warn_null_block") );
    return;
  }
  PlotCanvas * crossCanvas;
  int mode = ( vertical )? MODE_CROSS : MODE_HCROSS;
  crossCanvas = new PlotCanvas( this, mode, name.TO_CHAR(), name.TO_CHAR(), block, reversed );
  crossCanvases.addPlot( name, crossCanvas ); // FIXME used by POCKETTOPO
  crossCanvas->show();
}

void
QTshotWidget::doToggle()
{
  ToggleWidget dialog( this );
}

void
QTshotWidget::doInfo()
{
  InfoWidget dialog( this );
}

void
QTshotWidget::doOptions()
{
  OptionsWidget dialog( this );
}

void 
QTshotWidget::doHelp()
{
#ifdef WIN32
  QMessageBox::warning(this, lexicon("qtopo_help"), lexicon("help_index") );
#else
  pid_t pid;
  if ( (pid = fork() ) == 0 ) { // child
    char * args[3];
    const char * browser = config("BROWSER");
    if ( browser && strlen(browser) > 0 ) {
      args[0] = const_cast<char *>( browser );
    } else {
      args[0] = const_cast<char *>( "/usr/bin/firefox" );
    }
    char path[256];
    sprintf(path, "file://");
    size_t len = strlen( path );
    if ( getcwd( path+len, 256-len ) != NULL ) {
      sprintf(path+strlen(path), "/help/%s/index.htm",config("LANG") );
      args[1] = const_cast<char *>( path );
      args[2] = (char *)NULL;
      DBG_CHECK("execv %s %s \n", args[0], args[1] );
      execv( args[0], args );
    } else {
      DBG_CHECK("failed getcwd\n");
      exit(0);
    }
  } else { // parent
    /* nothing to do */
  }
#endif
}

void
QTshotWidget::doQuit()
{
  ExitWidget dialog( this );
}

void
QTshotWidget::doRealExit()
{
  this->close();
}

CleanShotsWidget::CleanShotsWidget( QTshotWidget * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_clean_shots") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  vbl->addWidget( new QLabel( lexicon("clean_shots"), this ) );

  DEFINE_HB;
  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  show();
  exec();
}

// ------------------------------------------------------------------------
// main program

#ifdef WIN32
int qtshot_main( int argc, char ** argv )
#else
int main( int argc, char ** argv )
#endif
{
  QApplication app( argc, argv );

/*
  int ac = 1;
  while ( ac < argc ) {
    if ( argv[ac][0] == '-' && argv[ac][1] == 'd' ) {
      do_debug = true;
    }
    ac ++;
  }
*/
  // Locale::SetLocale( "it" );

  Config & config = Config::Get();
  char * qtopo_rc = getenv( "QTOPO_RC" );
  if ( qtopo_rc ) {
    if ( ! config.Load( qtopo_rc ) ) {
      // printf("No system-wide config env(\"QTOPO_RC\") \n");
    }
  }
  char * home = getenv( "HOME" );
  if ( home ) {
    char * home_rc = (char*)malloc( strlen(home) + 16 );
    sprintf( home_rc, "%s/.qtopo.rc", home );
    if ( ! config.Load( home_rc ) ) {
      // printf("No user config .qtopo.rc \n");
    }
    free( home_rc );
  }
  if ( ! config.Load( "qtopo.rc" ) ) {
    // printf("No local config qtopo.rc \n");
  }

  if ( strlen( config("ROOT") ) > 0 ) {
    if ( chdir( config("ROOT") ) != 0 ) {
      fprintf(stderr, "Cannot change to root directory %s: %s\n",
        config("ROOT"), strerror( errno ) );
    } else {
      fprintf(stderr, "Changed root to %s\n", config("ROOT") );
    }
  }

  if ( strcasecmp(config("DEBUG"), "yes") == 0 ) {
    fprintf(stderr, "Debug is enabled\n");
    do_debug = true;
  }

  if ( strlen( config("LANGUAGE") ) > 0 ) {
    Language & lexicon = Language::Get();
    lexicon.init( config("LANGUAGE") );
  }

  QTshotWidget widget;
  QPixmap icon;
  if ( icon.load( config("QTDATA_ICON") ) ) {
    // printf( "loaded icon\n");
    widget.setWindowIcon( icon );
  }
  // app.setMainWidget( &widget );
  widget.show();
  return app.exec();
}



// ===========================================================================
// SCRAP NAME WIDGET
// get the scrap name on save (export as therion file)

ScrapNameWidget::ScrapNameWidget( QTshotWidget * p, QString & name, int type )
  : QDialog( p )
  , parent( p )
  , plot_type( type )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_scrap") );
  QVBoxLayout* vbl = new QVBoxLayout(this);

  vbl->addWidget( new QLabel( lexicon("plot_name"), this ) );
  pname = new QLineEdit( name, this );
  vbl->addWidget( pname );

  vbl->addWidget( new QLabel( lexicon("scrap_name"), this ) );
  sname = new QLineEdit( name, this );
  vbl->addWidget( sname );

  QWidget * hb;
  QHBoxLayout * hbl;

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );

  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  // show();
  exec();
}

void
ScrapNameWidget::doOK()
{
  hide();
  if ( pname->text().isEmpty() || sname->text().isEmpty() ) return;
  switch ( plot_type ) {
    case MODE_PLAN:
      parent->doNewPlot( pname->text(), sname->text(), MODE_PLAN );
      break;
    case MODE_EXT:
      parent->doNewPlot( pname->text(), sname->text(), MODE_EXT );
      break;
    case MODE_3D:
    case MODE_CROSS:
      /* nothing */
      break;
  }
}


