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
  #define QTOPO_RC "C:\\Program Files\\qtopo\\qtopo.rc"
  #define strcasecmp strcmp
#elif defined ARM
  #define QTOPO_RC "/opt/QtPalmtop/etc/qtopo.rc"
#else
  #include <unistd.h>
  #define QTOPO_RC "/usr/share/qtopo/qtopo.rc"
#endif


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
#include "CanvasMode.h"

#include "DataThExport.h"
#include "DataDatExport.h"
#include "DataSvxExport.h"
#include "DataTopExport.h"

// FIXME LANGUAGE
const char * extends[] = { "-", "L", "R", "V", "I" };
const char * flags[]   = { "-", "S", "D" };

#if 0
  #include "Programs.h"
#endif

/** default comment size
 */
#define COMMENT_SIZE 20

// ===========================================================================
// Main Widget


QTshotWidget::QTshotWidget( QWidget * parent, const char * name, WFLAGS fl )
  : QMAINWINDOW( parent, name, fl )
  , config( Config::Get() )
  , lexicon( Language::Get() )
  , table( NULL )
  , planCanvas( NULL )
  , extCanvas( NULL )
  , crossCanvas( NULL )
  , _3DCanvas( NULL )
  , collapse( true )              // default onCollapse action
  , append( true )                // downloaded data are appended
  , smart( true )                 // smart-process downloaded data 
  , splay_at( SPLAY_AT_FROM )                 // splay at station FROM
  , backward( false )             // station numbers are forward
  , comment_size( COMMENT_SIZE )  // comment width displayed in the table 
{
  info.fileName = config("DEFAULT_DATA");
  
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
  setIcon( icon->QTshot() );

  setCaption( lexicon("qtopo_shot") );
  extends[0] = lexicon("N");
  extends[1] = lexicon("L");
  extends[2] = lexicon("R");
  extends[3] = lexicon("V");
  extends[4] = lexicon("I");
  flags[0] = lexicon("N");
  flags[1] = lexicon("S");
  flags[2] = lexicon("D");
  

  const char * geometry = config("GEOMETRY");
  int w = WIDGET_WIDTH;
  int h = WIDGET_HEIGHT;
  if ( sscanf( geometry, "%dx%d", &w, &h ) != 2 ) {
    w = WIDGET_WIDTH;
    h = WIDGET_HEIGHT;
  } 
  resize(w, h);

  // QMenuBar * menubar = this->menuBar();
  QTOOLBAR * toolbar = new QTOOLBAR( this );
  btnNew =
    new QTOOLBUTTON( icon->NewOff(), lexicon("clear"), QString::null,
                     this, SLOT(doNew()), toolbar, lexicon("clear") );
  // QTOOLBUTTON * _open =
    new QTOOLBUTTON( icon->Open(), lexicon("open"), QString::null,
                     this, SLOT(doOpen()), toolbar, lexicon("open") );
  btnSave =
    new QTOOLBUTTON( icon->SaveOff(), lexicon("save"), QString::null,
                     this, SLOT(doSave()), toolbar, lexicon("save") );
  btnData = 
    new QTOOLBUTTON( icon->Data(), lexicon("download"), QString::null,
                     this, SLOT(doData()), toolbar, lexicon("download") );
  btnExport =
    new QTOOLBUTTON( icon->ExportThOff(), lexicon("export"), QString::null,
                     this, SLOT(doExport()), toolbar, lexicon("export") );
  btnCollapse =
    new QTOOLBUTTON( icon->CollapseOff(), lexicon("splay"), QString::null,
                     this, SLOT(doCollapse()), toolbar, lexicon("splay") );
  btnPlan =
    new QTOOLBUTTON( icon->PlanOff(), lexicon("plan"), QString::null,
                     this, SLOT(doPlan()), toolbar, lexicon("plan") );
  btnExtended =
    new QTOOLBUTTON( icon->ExtendedOff(), lexicon("extended"), QString::null,
                     this, SLOT(doExtended()), toolbar, lexicon("extended") );
  btn3D = 
    new QTOOLBUTTON( icon->_3dOff(), lexicon("3d"), QString::null,
                     this, SLOT(do3D()), toolbar, lexicon("3d") );

    new QTOOLBUTTON( icon->Toggle(), lexicon("toggle"), QString::null,
                     this, SLOT(doToggle()), toolbar, lexicon("toggle") );

    new QTOOLBUTTON( icon->Options(), lexicon("options"), QString::null,
                     this, SLOT(doOptions()), toolbar, lexicon("options") );

    new QTOOLBUTTON( icon->Help(), lexicon("help"), QString::null,
                     this, SLOT(doHelp()), toolbar, lexicon("help") );

    new QTOOLBUTTON( icon->Quit(), lexicon("exit"), QString::null,
                     this, SLOT(doQuit()), toolbar, lexicon("exit") );

/*
  (void)QWhatsThis::whatsThisButton( toolbar );
  QWhatsThis::add( _open, "open" );
  QWhatsThis::add( _save, "save" );
  QWhatsThis::add( _data, "data" );
  QWhatsThis::add( _export, "export" );
  QWhatsThis::add( _quit, "quit" );
*/

/*
  QPOPUPMENU * menu  = new QPOPUPMENU( this, "File" );
  menubar->insertItem( "&File", menu );
  menu->insertItem( "&Open", this, SLOT( doOpen() ), 0, 1 );
  menu->insertItem( "&Save", this, SLOT( doSave() ), 0, 1 );
  menu->insertItem( "&Quit", this, SLOT( doQuit() ), 0, 1 );
*/
  // CWidget * cwidget = new CWidget( this );
  // setCentralWidget( cwidget );
  
  if ( (u = config("EXPORT")) != NULL ) {
    if ( strcmp(u, "compass") == 0 ) {
      export_type = ExportCompass;
    } else if ( strcmp(u, "pockettopo") == 0 ) {
      export_type = ExportPocketTopo;
    } else if ( strcmp(u, "survex") == 0 ) {
      export_type = ExportSurvex;
    } else {
      export_type = ExportTherion;
    }
    onOffButtonExport( false );
  }

  show();
  if ( strcmp( config("NEWBY"), "yes") == 0 ) {
    new SplashWidget( this );
  }
}

void
QTshotWidget::distoxReset()
{
  DBG_CHECK("QTshotWidget::distoxReset()\n");
  btnData->setPixmap( icon->Data3() );
  btnData->update();
  // btnData->repaint(0,0, -1,-1);
  // btnData->show();
}

void
QTshotWidget::distoxDownload( size_t nr )
{
  DBG_CHECK("QTshotWidget::distoxDownload() %d\n", nr );
  if ( ( nr % 2 ) == 1 ) {
    btnData->setPixmap( icon->Data4() );
  } else {
    btnData->setPixmap( icon->Data3() );
  }
  // btnData->repaint(0,0, -1,-1);
  // btnData->show();
}

void
QTshotWidget::distoxDone()
{
  DBG_CHECK("QTshotWidget::distoxDone()\n");
  btnData->setPixmap( icon->Data() );
  // repaint(0,0, -1,-1);

  onOffButtons( dlist.Size() > 0 );
}


void
QTshotWidget::value_changed( int r, int c )
{
  DBG_CHECK("value_changed row %d column %d \n", r, c );
  if ( c == 0 || c == 1 || c == 5 || c == 6 ) {
    dlist.updateBlock( r, c, table->text(r,c) );
  /*
  } else if ( c == 4 ) {
    int ignore = dlist.toggleIgnore( r );
    table->setText( r, 4, ignore ? "v" : " " );
  */
  }
}

void
QTshotWidget::SetBaseBlock( DBlock * base )
{
  if ( table != NULL ) {
    int row = 0;
    for (DBlock * b = dlist.Head(); b != NULL; b=b->Next() ) {
      if ( dlist.IsBaseBlock( b ) ) {
        #ifdef HAS_LRUD
          if ( do_lrud ) {
            if ( b->LRUD_From() ) {
              table->setPixmap( row, 0, icon->Blue() );
            } else {
              table->setPixmap( row, 0, icon->White() );
              table->updateCell( row, 0 );
            }
          } else {
            // fprintf(stderr, "paint row %d white\n", row );
            table->setPixmap( row, 0, icon->White() );
            table->updateCell( row, 0 );
          }
        #else
          table->setPixmap( row, 0, icon->White() );
          table->updateCell( row, 0 );
        #endif
        break;
      }
      ++ row;
    }
  }
  dlist.SetBaseBlock( base );
}

void
QTshotWidget::showData( )
{
  DBG_CHECK("showData() table %p \n", (void *)table );
  if ( table == NULL ) {
    table = new QTABLE( dlist.Size(), 8, this);
    QHEADER * header = table->horizontalHeader ();
    header->setLabel( 0, lexicon("from"),    STATION_WIDTH );
    header->setLabel( 1, lexicon("to"),      STATION_WIDTH );
    header->setLabel( 2, lexicon("tape"),    DATA_WIDTH );
    header->setLabel( 3, lexicon("azimuth"), DATA_WIDTH );
    header->setLabel( 4, lexicon("clino"),   DATA_WIDTH );
    header->setLabel( 5, lexicon("ext"),     FLAG_WIDTH );
    header->setLabel( 6, lexicon("flag"),    FLAG_WIDTH );
    header->setLabel( 7, lexicon("comment"), FLAG_WIDTH );
    table->setColumnWidth( 7, 100 );
    // header->setClickEnabled( TRUE, 0 );
    // header->setClickEnabled( TRUE, 1 );
    // connect( table, SIGNAL(clicked(int, int, int, const QPoint &)), 
    //          this, SLOT(clicked( int, int, int, const QPoint & ) ) );
    connect( table, SIGNAL(valueChanged(int, int)), 
             this, SLOT(value_changed(int,int)) );
    connect( table, SIGNAL(doubleClicked(int, int, int, const QPoint & ) ),
             this, SLOT(double_clicked( int, int, int, const QPoint & ) ) );
    table->show();
    setCentralWidget( table );

    // table->setSorting( TRUE );
    DBG_CHECK("showData() created table, rows %d \n", dlist.Size() );
  } else {
    table->setNumRows( dlist.Size() );
    DBG_CHECK("showData() table set rows %d \n", dlist.Size() );
  }

  double ls = units.length_factor;
  double as = units.angle_factor;

#if 0 // QT_VERSION >= 0x40200
  QString color_black( "QTextItem{ color: black }" );
  QString color_red  ( "QTextItem{ color: red }" );
  QString color_blue ( "QTextItem{ color: blue }" );
#endif

  int row = 0;
  for (DBlock * b = dlist.Head(); b != NULL; b=b->Next() ) {
#if 0 // QT_VERSION >= 0x040200
    if ( b->NrBlocklets() <= 1 ) {
      table->cellWidget( row, 0 )->setStyleSheet( color_black );
    } else {
      table->cellWidget( row, 0 )->setStyleSheet( color_red );
    }
#endif

    #ifdef HAS_LRUD
      if ( do_lrud ) {
        if ( dlist.IsBaseBlock( b ) ) { 
          if ( b->LRUD_From() ) {
            table->setPixmap( row, 0, icon->DarkBlue() );
          } else {
            table->setPixmap( row, 0, icon->Green() );
          }
        } else {
          if ( b->LRUD_From() ) {
            table->setPixmap( row, 0, icon->Blue() );
          } else {
            // table->setPixmap( row, 0, icon->White() );
          }
        }
        if ( b->LRUD_To() ) {
          table->setPixmap( row, 1, icon->Blue() );
        } else {
          // table->setPixmap( row, 0, icon->White() );
        }
      } else { // do_lrud == false
        if ( dlist.IsBaseBlock( b ) ) {
          table->setPixmap( row, 0, icon->Green() );
        } else {
          // table->setPixmap( row, 0, icon->White() );
        }
      }
    #else
      if ( dlist.IsBaseBlock( b ) ) {
        table->setPixmap( row, 0, icon->Green() );
      } else {
        // table->setPixmap( row, 0, icon->White() );
      }
    #endif

    table->setText( row, 0, b->From() );
    table->setText( row, 1, b->To() );
    table->setText( row, 2, Locale::ToString(b->Tape() * ls, 2) );
    table->setText( row, 3, Locale::ToString(b->Compass() * as, 1) );
    table->setText( row, 4, Locale::ToString(b->Clino() * as, 1) );
    table->setText( row, 5, extends[ b->Extend() ] );
    table->setText( row, 6, flags[ b->Flag() ] );
    if ( b->hasComment() ) {
      QString c( b->Comment() );
      c.truncate( comment_size ); // FIXME 10 chars only
      table->setText( row, 7, c );
    }
    ++ row;
  }
}

void 
QTshotWidget::double_clicked( int row, int col, int button, const QPoint & )
{
  DBG_CHECK("QTshotWidget::double_clicked %d %d Button %d \n", row, col, button );
  col = col;  // avoid gcc warnings
  button = button;
  // comment input dialog
  DBlock * b = dlist.getBlock( row );
  #ifdef HAS_LRUD
    new CommentWidget( this, b, do_lrud );
  #else
    new CommentWidget( this, b );
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
  if ( dlist.Size() == 0 ) return;
  DBG_CHECK("doCollapse() %s \n", collapse? "yes" : "no" );
  if ( ! table ) return;
  if ( collapse ) {
    for (int row = 0; row <table->numRows(); ++row ) {
      if ( table->text(row, 1).isEmpty() ) {
        table->hideRow( row );
      } else {
        table->setRowHeight(row, 16);
      }
    }
    collapse = false;
  } else {
    for (int row = 0; row <table->numRows(); ++row ) {
      table->showRow( row );
      table->setRowHeight(row, 16);
    }
    collapse = true;
  }
}

void
QTshotWidget::doNew()
{
  DBG_CHECK("doNew \n" );

  if ( dlist.Size() > 0 ) {
    CleanShotsWidget * csw = new CleanShotsWidget( this );
    // csw->show();
    csw->exec();
  }
}
  

void
QTshotWidget::doRealNew()
{
  dlist.clear();
  onOffButtons( dlist.Size() > 0 );
  closePlots();
  showData();
}

// ------------------------------------------------------------------
// PlotDrawer interface

void
QTshotWidget::closePlots()
{
  DBG_CHECK("closePlots() %p %p %p %p\n",
      (void*)planCanvas, (void*)extCanvas, (void*)crossCanvas, (void*)_3DCanvas );

  if ( planCanvas ) {
    planCanvas->hide();
    planCanvas->ClearTh2PointsAndLines();
    delete planCanvas;
    planCanvas = NULL;
  }
  if ( extCanvas ) {
    extCanvas->hide();
    extCanvas->ClearTh2PointsAndLines();
    delete extCanvas;
    extCanvas = NULL;
  }
  if ( crossCanvas ) {
    crossCanvas->hide();
    crossCanvas->ClearTh2PointsAndLines();
    delete crossCanvas;
    crossCanvas = NULL;
  }
  if ( _3DCanvas ) {
    _3DCanvas->hide();
    // _3DCanvas->ClearTh2PointsAndLines();
    delete _3DCanvas;
    _3DCanvas = NULL;
  }
}

void 
QTshotWidget::openPlot( int mode )
{
  if ( mode == MODE_PLAN && planCanvas == NULL ) {
    planCanvas = new PlotCanvas( this, MODE_PLAN /*, true */ );
  } else if ( mode == MODE_EXT && extCanvas == NULL ) {
    extCanvas = new PlotCanvas( this, MODE_EXT /*, true */ );
  }
}


void 
QTshotWidget::insertPoint( int x, int y, ThPointType type, int mode )
{
  if ( mode == MODE_PLAN && planCanvas ) {
    planCanvas->insertPoint( x, y, type, 0, 0, true );
  } else if ( mode == MODE_EXT && extCanvas ) {
    extCanvas->insertPoint( x, y, type, 0, 0, true );
  }
}
 
void 
QTshotWidget::insertLinePoint( int x, int y, ThLineType type, int mode )
{
  if ( mode == MODE_PLAN && planCanvas ) {
    // DBG_CHECK("insertLinePoint %d %d \n", x, y );
    planCanvas->insertLinePoint( x, y, type, true );
  } else if ( mode == MODE_EXT && extCanvas ) {
    extCanvas->insertLinePoint( x, y, type, true );
  }
}

// ---------------------------------------------------------

void
QTshotWidget::doOpen()
{
  DBG_CHECK("doOpen \n" );
#ifdef QT_NO_FILEDIALOG
  // MyFileDialog * dialog = 
    new MyFileDialog( this, lexicon("open_file"), 0 );
  // dialog->show();
  // dialog->exec();
#else
  onOpenFile( QFileDialog::getOpenFileName( info.fileName,
    "Tlx files (*.tlx)\nRaw files (*.txt)\nPocketTopo (*.top)\nAll (*.*)", this ) );
#endif
}

void
QTshotWidget::onOpenFile( const QString & file )
{
  info.fileName = file;
  DBG_CHECK("onOpenFile file \"%s\"\n", info.fileName.latin1() );
  if ( ! info.fileName.isEmpty() ) {
    closePlots();           // close all plots
    SetBaseBlock( NULL );   // erase base block pixmap
    collapse = true;        // reset onCollapse action
    if ( table != NULL ) {  // set all table rows visible
      for (int row = 0; row <table->numRows(); ++row ) {
        table->showRow( row );
      }
    }

    int ret = dlist.loadFile( this, info.fileName, false, &info ); // do not append but replace data
    if ( ret == 0 ) {
      onOffButtons( dlist.Size() > 0 );
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

void
QTshotWidget::updateCanvases()
{
  if ( planCanvas ) planCanvas->redrawPlot();
  if ( extCanvas )  extCanvas->redrawPlot();
  // FIXME
  // if ( _3DCanvas )  _3DCanvas->redrawPlot();
  // if ( crossCanvas ) crossCanvas->redrawPlot();
}

void
QTshotWidget::doSave()
{
  if ( dlist.Size() == 0 ) return;
#ifdef QT_NO_FILEDIALOG
  // MyFileDialog * dialog = 
    new MyFileDialog( this, lexicon("save_file"), 1 );
  // dialog->show();
  // dialog->exec();
#else
  onSaveFile( QFileDialog::getSaveFileName( info.fileName,
    "Tlx files (*.tlx)\nAll (*.*)", this ) );
#endif
}

void
QTshotWidget::onSaveFile( const QString & file )
{
  info.fileName = file;
  DBG_CHECK("onSaveFile file \"%s\"\n", info.fileName.latin1() );
  if ( !info.fileName.isEmpty() ) {                 // got a file name
    
    // TODO get optional comment to write to TLX file
    if ( info.description.size() == 0 ) {
      // info.surveyComment = 
      QString descr = 
        QInputDialog::getText( lexicon("qtopo_comment"), 
                               lexicon("comment"),
                               QLineEdit::Normal, 
                               "" // info.description // info.surveyComment
      );
      info.description = descr.latin1();
    }
    dlist.saveTlx( info );
  }
}

void
QTshotWidget::doInsertBlock( DBlock * block )
{
  new InsertWidget( this, block );
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
    // if ( (d = atof( L.latin1() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( L )) > 0.0 ) {
      b = a + 270; while ( b >= 360.0 ) b-= 360.0;
      c = 0.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! R.isEmpty() ) {
    // if ( (d = atof( R.latin1() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( R )) > 0.0 ) {
      b = a + 90; while ( b >= 360.0 ) b-= 360.0;
      c = 0.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! U.isEmpty() ) {
    // if ( (d = atof( U.latin1() )) > 0.0 ) {
    if ( (d = Locale::ToDouble( U )) > 0.0 ) {
      b = 0.0;
      c = 90.0;
      dlist.insertBlock( block, d, b, c, false );
    }
  }
  if ( ! D.isEmpty() ) {
    // if ( (d = atof( D.latin1() )) > 0.0 ) {
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
QTshotWidget::updateExtCanvas( DBlock * b )
{ 
  DBG_CHECK("updateExtCanvas() block %s %s \n", b->From(), b->To() );

  dlist.resetNum();
  if ( extCanvas ) 
    extCanvas->doExtend( b, b->Extend(), false );
  if ( planCanvas )
    planCanvas->redrawPlot();
  if ( _3DCanvas )
    _3DCanvas->redrawPlot();
}


void
QTshotWidget::doData()
{
  DBG_CHECK("doData begin\n");
  download = false;
  new DataWidget( this );
  if ( download ) {
    distoxReset();
    downloadData( );
    onOffButtons( dlist.Size() > 0 );
    if ( !append && dlist.Size() > 0 ) {
      // TODO ask date and description
      new CenterlineWidget( this );
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
  status = ( disto->download() ) ? 1 : -1;
}

bool
QTshotWidget::GetDistoModes( bool & calib, bool & silent )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.readMode();
  if ( mode < 0 ) return false;
  calib  = (mode & 0x08) != 0;
  silent = (mode & 0x10) != 0;
  return true;
}

bool 
QTshotWidget::SetCalibMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setCalib( on );
  return ( on == ( mode == 1 ) );
}

bool 
QTshotWidget::SetSilentMode( bool on )
{
  const char * disto_log = config("DISTO_LOG");
  bool log = (disto_log[0] == 'y');
  DistoX disto( device, log );
  int mode = disto.setSilent( on );
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
      SetBaseBlock( NULL );   // erase base block pixmap
      collapse = true;        // reset onCollapse action
      if ( table != NULL ) {  // set all table rows visible
        for (int row = 0; row <table->numRows(); ++row ) {
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
  : QDialog( my_parent, "CenterlineWidget", true ) 
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_centerline") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX * hb = new QHBOX(this);
  int y,m,d;
  GetDate( &d, &m, &y );
  char dstr[16];
  // sprintf(dstr, "%04d-%02d-%02d", y, m, d); 
  Locale::ToDate( dstr, y, m, d );
  new QLabel( lexicon("date"), hb );
  date  = new QLineEdit( dstr, hb );
  hb = new QHBOX(this);
  new QLabel( lexicon("description"), hb );
  hb = new QHBOX(this);
  descr = new QLineEdit( "", hb );

  hb = new QHBOX(this);
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), hb );
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vb->addWidget( hb );

  exec();
}

void
CenterlineWidget::doOK()
{
  hide();
  parent->setDateAndDescription( date->text().latin1(), descr->text().latin1() );
  delete this;
}

// ----------------------------------------------------------------------
// Toggle widget: DistoX modes

ToggleWidget::ToggleWidget( QTshotWidget * my_parent )
  : QDialog( my_parent, "ToggleWidget", true ) 
  , parent( my_parent )
  , isCalib( false )
  , isSilent( false )
  , calibBtn( NULL )
  , silentBtn( NULL )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_toggle") );
  if ( ! parent->GetDistoModes( isCalib, isSilent ) ) {
    QVBoxLayout* vb = new QVBoxLayout(this);
    vb->setAutoAdd(TRUE);
    new QLabel( lexicon("toggle_mode"), this );
    new QLabel( lexicon("failed_mode"), this );
    QPushButton * c2 = new QPushButton( tr( lexicon("close") ), this );
    connect( c2, SIGNAL(clicked()), this, SLOT(doClose()) );
  } else {
    QLabel * label;
    QGridLayout * vb = new QGridLayout( this, 2 );
    label = new QLabel( lexicon("toggle_mode"), this );
    vb->addWidget( label,    0, 0 );
    calibBtn = new QCheckBox( lexicon("mode_calib"), this );
    calibBtn->setCheckState ( isCalib ? Qt::Checked : Qt::Unchecked ); 
    vb->addWidget( calibBtn, 1, 0 );
    silentBtn = new QCheckBox( lexicon("mode_silent"), this );
    silentBtn->setCheckState ( isSilent ? Qt::Checked : Qt::Unchecked ); 
    vb->addWidget( silentBtn, 2, 0 );

    connect( calibBtn, SIGNAL(stateChanged(int)), this, SLOT(doCalib(int)) );
    connect( silentBtn, SIGNAL(stateChanged(int)), this, SLOT(doSilent(int)) );
    

    QPushButton * c2 = new QPushButton( tr( lexicon("close") ), this );
    vb->addWidget( c2, 3, 0 );
    connect( c2, SIGNAL(clicked()), this, SLOT(doClose()) );
  }

  exec();
}

void
ToggleWidget::doCalib(int state) 
{
  bool calib = (state == Qt::Checked);
  if ( parent->SetCalibMode( calib ) ) {
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
  if ( parent->SetSilentMode( silent ) ) {
    isSilent = silent;
  } else {
    Language & lexicon = Language::Get();
    QMessageBox::warning(this, lexicon("failed_disto"), lexicon("failed_toggle") );
    silentBtn->setCheckState ( isSilent ? Qt::Checked : Qt::Unchecked );
  }
}

// ----------------------------------------------------------------------
// Option widget

OptionsWidget::OptionsWidget( QTshotWidget * my_parent )
  : QDialog( my_parent, "OptionsWidget", true ) 
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_options") );
  // QVBoxLayout* vb = new QVBoxLayout(this, 8);
  // vb->setAutoAdd(TRUE);
  // QHBOX * hb = new QHBOX(this);
  QGridLayout * vb = new QGridLayout( this, 8 );
  QLabel * label;
  label = new QLabel( lexicon("length_units"), this );
  length_btn[0] = new QRadioButton( lexicon("m"), this );
  length_btn[1] = new QRadioButton( lexicon("ft"), this );
  vb->addWidget( label,    0, 0 );
  vb->addWidget( length_btn[0], 0, 1 );
  vb->addWidget( length_btn[1],  0, 2 );
  QBUTTONGROUP * m_group_length = new QBUTTONGROUP( );
  m_group_length->insert( length_btn[0] );
  m_group_length->insert( length_btn[1] );

  if ( parent->lengthUnits() == LENGTH_METER ) {
    length_btn[0]->setChecked( TRUE );
  } else if ( parent->lengthUnits() == LENGTH_FEET ) {
    length_btn[1]->setChecked( TRUE );
  }

  label = new QLabel( lexicon("angle_units"), this);
  angle_btn[0] = new QRadioButton( lexicon("deg"), this );
  angle_btn[1] = new QRadioButton( lexicon("grad"), this );
  vb->addWidget( label,    1, 0 );
  vb->addWidget( angle_btn[0],   1, 1 );
  vb->addWidget( angle_btn[1],  1, 2 );
  QBUTTONGROUP * m_group_angle = new QBUTTONGROUP( );
  m_group_angle->insert( angle_btn[0] );
  m_group_angle->insert( angle_btn[1] );

  if ( parent->angleUnits() == ANGLE_DEGREE ) {
    angle_btn[0]->setChecked( TRUE );
  } else if ( parent->angleUnits() == ANGLE_GRAD ) {
    angle_btn[1]->setChecked( TRUE );
  }

  label = new QLabel( lexicon("export"), this );
  QBUTTONGROUP * m_group_export = new QBUTTONGROUP( );
  export_btn[0] = new QRadioButton( "th", this);
  export_btn[1] = new QRadioButton( "svx", this);
  export_btn[2] = new QRadioButton( "dat", this);
  export_btn[3] = new QRadioButton( "top", this);
  m_group_export->insert( export_btn[0] );
  m_group_export->insert( export_btn[1] );
  m_group_export->insert( export_btn[2] );
  m_group_export->insert( export_btn[3] );
  switch ( parent->exportType() ) {
    case ExportTherion:    export_btn[0]->setChecked( TRUE ); break;
    case ExportSurvex:     export_btn[1]->setChecked( TRUE ); break;
    case ExportCompass:    export_btn[2]->setChecked( TRUE ); break;
    case ExportPocketTopo: export_btn[3]->setChecked( TRUE ); break;
    default: break;
  }
  vb->addWidget( label,     2, 0 );
  vb->addWidget( export_btn[0], 2, 1 );
  vb->addWidget( export_btn[1], 2, 2 );
  vb->addWidget( export_btn[2], 2, 3 );
  vb->addWidget( export_btn[3], 2, 4 );

  // hb = new QHBOX(this);
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this );
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vb->addWidget( c1, 3, 0 );
  vb->addWidget( c2, 3, 1 );

  exec();
}

void
OptionsWidget::doOK()
{
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

  if ( export_btn[0]->isChecked() ) {
    parent->setExportType( ExportTherion ); 
  } else if ( export_btn[1]->isChecked() ) {
    parent->setExportType( ExportSurvex );
  } else if ( export_btn[2]->isChecked() ) {
    parent->setExportType( ExportCompass );
  } else if ( export_btn[3]->isChecked() ) {
    parent->setExportType( ExportPocketTopo );
  }

  parent->showData();
  delete this;
}

// ----------------------------------------------------------------------
// Comment

#ifdef HAD_LRUD
CommentWidget::CommentWidget( QTshotWidget * my_parent, DBlock * b, bool do_lrud )
#else
CommentWidget::CommentWidget( QTshotWidget * my_parent, DBlock * b )
#endif
  : QDialog( my_parent, "CommentWidget", true ) 
  , parent( my_parent )
  , block( b )
{
  double ls = parent->lengthFactor();
  double as = parent->angleFactor();
  Language & lexicon = Language::Get();
  setCaption( lexicon("shot_comment") );
  std::ostringstream oss;
  int n_stations = 0; // number of stations
  if ( block->hasFrom() ) {
    ++ n_stations;
    oss << " " << lexicon("from") << " " << block->From();
  }
  if ( block->hasTo() ) {
    ++ n_stations;
    oss << " " << lexicon("to") << " " << block->To();
  }

  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX * hb;

  new QLabel( oss.str().c_str(), this );

  if ( block->NrBlocklets() > 1 ) {
    DBlocklet * bl = block->Blocklets();
    while ( bl ) {
      QString str = Locale::ToString( bl->distance * ls, 2 )
          + "   " + Locale::ToString( bl->compass * as, 1 )
          + "   " + Locale::ToString( bl->clino * as, 1 );
      new QLabel( str, this );
      bl = bl->next;
    }
  }

  QButtonGroup * m_group = new QButtonGroup( );
  properties = new QRadioButton( lexicon("properties"), this );
  properties->setChecked( false );
  m_group->insert( properties );
  comment = new QLineEdit( b->Comment(), this );
  connect( comment, SIGNAL(textChanged(const QString &)), this, SLOT(doComment(const QString &)) );

  hb = new QHBOX(this);
  new QLabel( lexicon("ext_box"), hb );
  extBox = new QComboBox( hb );
  connect( extBox, SIGNAL(activated(int)), this, SLOT(doExtend(int)) );
  extBox->insertItem( lexicon("none") );  // addItem( lexicon("none") );
  extBox->insertItem( lexicon("left") );
  extBox->insertItem( lexicon("right") );
  extBox->insertItem( lexicon("vertical") );
  extBox->insertItem( lexicon("ignore") );
  extBox->setCurrentItem( b->Extend() );

  hb = new QHBOX(this);
  new QLabel( lexicon("flag_box"), hb );
  flagBox = new QComboBox( hb );
  connect( flagBox, SIGNAL(activated(int)), this, SLOT(doFlag(int)) );
  flagBox->insertItem( lexicon("none") );
  flagBox->insertItem( lexicon("surface") );
  flagBox->insertItem( lexicon("duplicate") );
  flagBox->setCurrentItem( b->Flag() );

  hb = new QHBOX(this);
  swapBox = new QCheckBox( lexicon("swap_from_to"), hb );
  connect( swapBox, SIGNAL(toggled(bool)), this, SLOT(doSwap(bool)) );

  #ifdef HAS_LRUD
    if ( do_lrud ) {
      hb = new QHBOX(this);
      lrud = new QRadioButton( lexicon("LRUD"), hb );
      lrud->setChecked( false );
      m_group->insert( lrud );
    } else {
      lrud = NULL;
    }
  #endif

  if ( block->hasFrom() ) {
    hb = new QHBOX(this);
    base_station = new QRadioButton( lexicon("base_station"), hb );
    base_station->setChecked( false );
    m_group->insert( base_station );
  } else {
    base_station = NULL;
  }

  hb = new QHBOX(this);
  renumber = new QRadioButton( lexicon("renumber"), hb );
  renumber->setChecked( false );
  m_group->insert( renumber );

  hb = new QHBOX(this);
  tomerge = new QRadioButton( lexicon("merge_next"), hb );
  tomerge->setChecked( false );
  m_group->insert( tomerge );

  if ( block->NrBlocklets() > 1 ) {
    hb = new QHBOX(this);
    tosplit = new QRadioButton( lexicon("split"), hb );
    tosplit->setChecked( false );
    m_group->insert( tosplit );
  } else {
    tosplit = NULL;
  }

  hb = new QHBOX(this);
  toinsert = new QRadioButton( lexicon("insert_shot"), hb );
  toinsert->setChecked( false );
  m_group->insert( toinsert );

  hb = new QHBOX(this);
  todrop = new QRadioButton( lexicon("delete_shot"), hb );
  todrop->setChecked( false );
  m_group->insert( todrop );

  if ( n_stations == 2 ) {
    hb = new QHBOX(this);
    cross_section = new QRadioButton( lexicon("cross_section"), hb );
    cross_section->setChecked( false );
    m_group->insert( cross_section );
#ifdef USER_HORIZONTAL_SECTION
    QVBOX * vb1 = new QVBOX( hb );
    reversed = new QCheckBox( lexicon("reversed"), vb1 );
    horizontal = new QCheckBox( lexicon("horizontal"), vb1 );
#else
    reversed = new QCheckBox( lexicon("reversed"), hb );
#endif
  } else {
    cross_section = NULL;
    reversed = NULL;
#ifdef USER_HORIZONTAL_SECTION
    horizontal = NULL;
#endif
  }

  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
CommentWidget::doComment( const QString & DBG(text) )
{
  DBG_CHECK("CommentWidget::doComment() %s\n", text.latin1() );
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

void
CommentWidget::doSwap( bool /* status */ )
{
  properties->setChecked( true );
}


void
CommentWidget::doOK()
{
  hide();
  bool need_to_show_data = false;
  bool need_to_compute_data = false;
  if ( properties->isChecked() ) {
    need_to_show_data = true;
    block->setComment( comment->text().latin1() );
    if ( block->Extend() != extBox->currentItem() ) {
      need_to_compute_data = true;
      block->setExtend( extBox->currentItem() ); // currentIndex()
    }
    if ( block->Flag() != flagBox->currentItem() ) {
      block->setFlag( flagBox->currentItem() );
    }
    if ( swapBox->isChecked() ) {
      if ( ! block->isSplay() ) { // not necessary because block can swap only if it has both stations
        if ( block->swapStations() ) {
          need_to_compute_data = true;
        }
      }
    }
  } else if ( base_station && base_station->isChecked() ) {
    parent->SetBaseBlock( block );
    need_to_show_data = true;
    need_to_compute_data = true;
#ifdef HAS_HLRUD
  } else if ( lrud && lrud->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    new LRUDWidget( parent, block );
#endif
  } else if ( renumber->isChecked() ) {
    need_to_show_data = true;
    block->renumber();
  } else if ( tomerge->isChecked() ) {
    need_to_show_data = true;
    parent->GetList()->mergeBlock( block );
  } else if ( tosplit && tosplit->isChecked() ) {
    need_to_show_data = true;
    parent->GetList()->splitBlock( block );
  } else if ( toinsert->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    parent->doInsertBlock( block );
    // parent->GetList()->insertBlock( block );
  } else if ( todrop->isChecked() ) {
    need_to_show_data = true;
    need_to_compute_data = true;
    parent->drop( block );
  } else if ( cross_section && cross_section->isChecked() ) {
#ifdef USER_HORIZONTAL_SECTION
    parent->doCrossSection( block, reversed->isChecked(), ! horizontal->isChecked() );
#else
    parent->doCrossSection( block, reversed->isChecked() );
#endif
  }

  if ( need_to_show_data ) {
    if ( need_to_compute_data ) {
      parent->updateExtCanvas( block );
    }
    parent->showData();
  }
  delete this;
}


// ----------------------------------------------------------------
// DistoX Data download dialog

DataWidget::DataWidget( QTshotWidget * my_parent )
  : QDialog( my_parent, "DataWidget", true ) 
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("distox_download") ); // setWindowTitle( lexicon("distox_download") );

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
  vb->addMultiCellWidget( description,  2, 2, 0, 1 );

  label = new QLabel( lexicon("device"), this );
  device = new QLineEdit( parent->getDevice(), this );
  vb->addWidget( label,  3, 0 );
  vb->addWidget( device, 3, 1 );

  label  = new QLabel( lexicon("append_shots"), this );
  append = new QCheckBox( "", this );
  append->setChecked( parent->getAppend() );
  vb->addWidget( label,  4, 0 );
  vb->addWidget( append, 4, 1 );

  label = new QLabel( lexicon("guess_centerline"), this );
  smart = new QCheckBox( "", this );
  smart->setChecked( parent->getSmart() );
  vb->addWidget( label, 5, 0 );
  vb->addWidget( smart, 5, 1 );

  label    = new QLabel( "...   ", this );
  backward = new QCheckBox( lexicon("backward"), this );
  backward->setChecked( parent->getBackward() );
  vb->addWidget( label,    6, 0 );
  vb->addWidget( backward, 6, 1 );

  label  = new QLabel( lexicon("splay_shots"), this );
  splay1 = new QCheckBox( lexicon("from_station"), this );
  splay2 = new QCheckBox( lexicon("to_station"), this );
  splay1->setChecked( parent->getSplay() == SPLAY_AT_FROM );
  splay2->setChecked( parent->getSplay() == SPLAY_AT_TO );
  connect( splay1, SIGNAL(toggled(bool)), this, SLOT(doSplay1(bool)) );
  connect( splay2, SIGNAL(toggled(bool)), this, SLOT(doSplay2(bool)) );
  vb->addWidget( label,  7, 0 );
  vb->addWidget( splay1, 7, 1 );
  label  = new QLabel( "   ", this );
  vb->addWidget( label,  8, 0 );
  vb->addWidget( splay2, 8, 1 );

  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this);
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  vb->addWidget( c1, 9, 0 );
  vb->addWidget( c2, 9, 1 );

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
  // hide();
  parent->setDateAndDescription( date->text().latin1(),
                                 description->text().latin1() );
  parent->setDownload( true,
                       device->text().latin1(),
                       append->isChecked(),
                       smart->isChecked(), 
                       splay1->isChecked(),
                       splay2->isChecked(),
                       backward->isChecked() );
  // parent->downloadData( );
  delete this;
}

// ----------------------------------------------------------------
//

ExitWidget::ExitWidget( QTshotWidget * p )
  : QDialog( p, "ExitWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_exit") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  QString label( lexicon("exit_question") );
  new QLabel( label, hb );

  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  
  exec();
}

SplashWidget::SplashWidget( QTshotWidget * p )
  : QDialog( p, "SplashWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  IconSet * icon = IconSet::Get();
  setCaption( lexicon("qtopo_shot") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  QPushButton * c;
  hb = new QHBOX(this);
  QLabel label( lexicon("what_do"), hb );
  
  hb = new QHBOX(this);
  c = new QPushButton( icon->Open(), lexicon("open_survey"), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOpen()) );

  hb = new QHBOX(this);
  c = new QPushButton( icon->Data(), lexicon("download_shots"), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doData()) );

  hb = new QHBOX(this);
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

// ----------------------------------------------------------------
// Insert Shot Widget

InsertWidget::InsertWidget( QTshotWidget * p, DBlock * blk ) 
  : QDialog( p, "InsertWidget", true ) 
  , parent( p )
  , block( blk )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_insert_shot") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  QString label( lexicon("insert_shot_at") );
  label += blk->hasFrom() ? blk->From() : "..." ;
  label += " - ";
  label += blk->hasTo()? blk->To() : "..." ;
  new QLabel( label, hb );
  hb = new QHBOX(this);
  new QLabel( lexicon("tape"), hb );
  distance = new QLineEdit( "", hb );
  hb = new QHBOX(this);
  new QLabel( lexicon("azimuth"), hb );
  compass = new QLineEdit( "", hb );
  hb = new QHBOX(this);
  new QLabel( lexicon("clino"), hb );
  clino = new QLineEdit( "", hb );
  hb = new QHBOX(this);
  new QLabel( lexicon("insert_before"), hb );
  before = new QCheckBox( "", hb );
  before->setChecked( false );

  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
InsertWidget::doOK()
{
  hide();
  parent->doInsertBlock( block,
                       distance->text().latin1(),
                       compass->text().latin1(),
                       clino->text().latin1(),
                       before->isChecked() );
  delete this;
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
  setCaption( lexicon("qtopo_insert_LRUD") );
  QGridLayout* vb = new QGridLayout(this, 8);
  QLabel label( lexicon("write_LRUD"), this );
  vb->addMultiCellWidget( &label, 0, 0, 0, 5);
  vb->addWidget( new QLabel( lexicon("station"), this), 1, 0 );
  vb->addWidget( new QLabel( lexicon("left"), this), 1, 1 );
  vb->addWidget( new QLabel( lexicon("right"), this), 1, 2 );
  vb->addWidget( new QLabel( lexicon("up"), this), 1, 3 );
  vb->addWidget( new QLabel( lexicon("down"), this), 1, 4 );
  if ( blk->hasFrom() ) {
    ok = true;
    vb->addWidget( new QLabel( blk->From(), this), 2, 0 );
    LRUD * lrud = blk->LRUD_From(); // "From" LRUD
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
  if ( blk->hasTo() ) {
    ok = true;
    vb->addWidget( new QLabel( blk->To(), this), 3, 0 );
    LRUD * lrud = blk->LRUD_To(); // "To" LRUD
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
  if ( blk->hasFrom() ) {
    DBG_CHECK("LRUD From %s %s %s %s\n",
        L1->text().latin1(), R1->text().latin1(), U1->text().latin1(), D1->text().latin1() );
    parent->doInsertLRUD( blk, 
                          L1->text(), R1->text(), U1->text(), D1->text(),
                          true ); // at from
  }
  if ( blk->hasTo() ) {
    DBG_CHECK("LRUD To %s %s %s %s\n", 
        L2->text().latin1(), R2->text().latin1(), U2->text().latin1(), D2->text().latin1() );
    parent->doInsertLRUD( blk, 
                          L2->text(), R2->text(), U2->text(), D2->text(),
                          false ); // at to
  }
  // FIXME this should be here but it segfaults
  // delete this; 
}
#endif // HAS_LRUD

// ----------------------------------------------------------------
// Survey Info

SurveyInfoWidget::SurveyInfoWidget( QTshotWidget * my_parent )
  : QDialog( my_parent, "SurveyInfoWidget", true ) 
  , parent( my_parent )
  , team( NULL )
  , prefix( NULL )
  , single_survey( NULL )
  , centerline( NULL )
  , survey( NULL )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("survey_info") );
  SurveyInfo * survey_info = parent->GetSurveyInfo();
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  // QWidget * label1 = 
    new QLabel( lexicon("name"), hb );
  name = new QLineEdit( survey_info->name, hb );
  hb = new QHBOX(this);
  // QWidget * label2 = 
    new QLabel( lexicon("title"), hb );
  title = new QLineEdit( survey_info->title, hb );
  
  ExportType e = parent->exportType();

  if ( e == ExportTherion || e == ExportSurvex ) { // CENTERLINE COMMANDS --> Therion, Survex
    new QLabel( lexicon("centerline_commands"), this );
    centerline = new QMULTILINEEDIT( this );
    centerline->insertLine( survey_info->centerlineCommand.c_str() );
  } else if ( e == ExportCompass ) { // TEAM --> Compass
    new QLabel( lexicon("team"), this );
    team = new QLineEdit( survey_info->team, this );
  }
  if ( e == ExportTherion ) { // SURVEY COMMANDS --> Therion
    new QLabel( lexicon("survey_commands"), this );
    survey = new QMULTILINEEDIT( this );
    survey->insertLine( survey_info->surveyCommand.c_str() );
  } else if ( e == ExportCompass ) { // STATION PREFIX --> Compass
    new QLabel( lexicon("prefix"), this );
    prefix = new QLineEdit( survey_info->prefix, this );
    single_survey = new QCheckBox( lexicon("single_survey"), this );
    single_survey->setChecked( survey_info->single_survey ); 
  }

  hb = new QHBOX(this);
  double as = parent->angleFactor();
  new QLabel( lexicon("declination"), hb );
  declination = new QLineEdit( Locale::ToString( survey_info->declination * as, 2 ), hb );

  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
SurveyInfoWidget::doOK()
{
  SurveyInfo * survey_info = parent->GetSurveyInfo();
  survey_info->name  = name->text();
  survey_info->title = title->text();
  if ( centerline ) {
    int k0 = centerline->numLines();
    if ( k0 > 0 ) {
      std::ostringstream oss;
      oss << centerline->textLine(0).latin1();
      for ( int k = 1; k < k0; ++k ) {
        oss << "\n" << centerline->textLine(k).latin1();
      }
      survey_info->centerlineCommand = oss.str();
    } else {
      survey_info->centerlineCommand = "";
    }
  } else {
    survey_info->centerlineCommand = "";
  }

  if ( survey ) {
    int k0 = survey->numLines();
    if ( k0 > 0 ) {
      std::ostringstream oss;
      oss << survey->textLine(0).latin1();
      for ( int k = 1; k < k0; ++k ) {
        oss << "\n" << survey->textLine(k).latin1();
      }
      survey_info->surveyCommand = oss.str();
    } else {
      survey_info->surveyCommand = "";
    }
  } else {
    survey_info->surveyCommand = "";
  }

  if ( team ) {
    survey_info->team = team->text();
  } else {
    survey_info->team = "";
  }

  if ( prefix ) {
    survey_info->prefix = prefix->text();
  }
  if ( single_survey ) {
    survey_info->single_survey = single_survey->isChecked();
  }
  if ( declination && ! declination->text().isEmpty() ) {
    double as = parent->angleFactor();
    survey_info->declination = Locale::ToDouble( declination->text() ) / as;
    printf("Set declination %s to %.2f as %.2f\n", 
      declination->text().latin1(), survey_info->declination, as );
  }

  parent->doExportOK();
  delete this;
}


// -------------------------------------------------------------------
// main widget

void
QTshotWidget::doExport()
{
  if ( dlist.Size() == 0 ) return;
#ifdef QT_NO_FILEDIALOG
  if ( export_type == ExportTherion ) { // therion
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("default_th") );
  } else if ( export_type == ExportSurvex ) { // survex
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("default_svx") );
  } else if ( export_type == ExportCompass ) {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("default_dat") );
  } else if ( export_type == ExportPocketTopo ) {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("default_top") );
  } else {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("default_none") );
    return;
  }
#else
  if ( export_type == ExportTherion ) { // therion
    info.exportName = QFileDialog::getSaveFileName( info.exportName,
      "Therion files (*.th)\nAll (*.*)", this );
  } else if ( export_type == ExportSurvex ) { // survex
    info.exportName = QFileDialog::getSaveFileName( info.exportName,
      "Survex files (*.svx)\nAll (*.*)", this );
  } else if ( export_type == ExportCompass ) {
    info.exportName = QFileDialog::getSaveFileName( info.exportName,
      "Compass files (*.dat)\nAll (*.*)", this );
  } else if ( export_type == ExportPocketTopo ) {
    info.exportName = QFileDialog::getSaveFileName( info.exportName,
      "PocketTopo files (*.top)\nAll (*.*)", this );
  } else {
    return;
  }
#endif
  DBG_CHECK("doExport()\n");
  if ( ! info.exportName.isEmpty() ) { // got a file name
    new SurveyInfoWidget( this );
  }
}

/*
void 
QTshotWidget::showCanvas( int mode, DBlock * block, bool reversed )
{
  DBG_CHECK("showCanvas mode %d\n", mode );

  new PlotCanvas( this, mode, block, reversed );
}
*/
    
void
QTshotWidget::doExportOK()
{
  if ( export_type == ExportTherion ) {
    if ( saveAsTherion( dlist, info, units ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_th") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_th") );
    }
  } else if ( export_type == ExportSurvex ) { // survex
    if ( saveAsSurvex( dlist, info, units ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_svx") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_svx") );
    }
  } else if ( export_type == ExportCompass ) { // compass
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
  } else if ( export_type == ExportPocketTopo ) { // PocketTopo
    if ( saveAsPocketTopo( dlist, info,
                           planCanvas ? planCanvas->getStatus() : NULL,
                           extCanvas ? extCanvas->getStatus() : NULL ) ) {
      QMessageBox::information(this, lexicon("qtopo_shot"), lexicon("saved_top") );
    } else {
      QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_saved_top") );
    }
  } else {
    QMessageBox::warning(this, lexicon("qtopo_shot"), lexicon("no_export_type") );
  }
}

void
QTshotWidget::doPlan()
{
  if ( dlist.Size() == 0 ) return;
  if ( planCanvas == NULL ) {
    planCanvas = new PlotCanvas( this, MODE_PLAN );
  }
  planCanvas->show();
  // this->showCanvas( MODE_PLAN );
}

void
QTshotWidget::doExtended()
{
  if ( dlist.Size() == 0 ) return;
  if ( extCanvas == NULL ) {
    extCanvas = new PlotCanvas( this, MODE_EXT );
  }
  extCanvas->show();
  // this->showCanvas( MODE_EXT );
}

void
QTshotWidget::do3D()
{
  if ( dlist.Size() == 0 ) return;
  if ( _3DCanvas == NULL ) {
    _3DCanvas = new PlotCanvas( this, MODE_3D );
  } 
  _3DCanvas->show();
  // this->showCanvas( MODE_3D );
}

void
QTshotWidget::doCrossSection( DBlock * block, bool reversed, bool vertical )
{
  if ( dlist.Size() == 0 ) return;
  assert( block != NULL );
  // if ( block == NULL ) return;

  PlotCanvas * xsectionCanvas;
  if ( vertical ) {
    xsectionCanvas = new PlotCanvas( this, MODE_CROSS, block, reversed );
    // this->showCanvas( MODE_CROSS, block, reversed );
  } else { // horizontal X-section
    xsectionCanvas = new PlotCanvas( this, MODE_HCROSS, block, reversed );
  }
  xsectionCanvas->show();
}

void
QTshotWidget::doToggle()
{
  new ToggleWidget( this );
}

void
QTshotWidget::doOptions()
{
  new OptionsWidget( this );
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
  new ExitWidget( this );
}

void
QTshotWidget::doRealExit()
{
  this->close();
}

CleanShotsWidget::CleanShotsWidget( QTshotWidget * p )
  : QDialog( p, "CleanShotsWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_clean_shots") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  new QLabel( lexicon("clean_shots"), this );

  QHBOX *hb;
  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
}

// ------------------------------------------------------------------------
// main program

#ifdef WIN32
int qtshot_main( int argc, char ** argv )
#else
int main( int argc, char ** argv )
#endif
{
  QAPPLICATION app( argc, argv );

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
  /*
  QPixmap icon;
  if ( icon.load( config("QTDATA_ICON") ) ) {
    // printf( "loaded icon\n");
    widget.setIcon( icon );
  }
  */
  app.setMainWidget( &widget );
  widget.show();
  return app.exec();
}



MyFileDialog::MyFileDialog( QTshotWidget * parent, const char * title, int m )
      : QDialog( parent, title, TRUE )
      , widget( parent )
      , mode( m )
{
  Language & lexicon = Language::Get();
  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  QHBOX * hb = new QHBOX(this);
  new QLabel( lexicon("enter_filename"), hb );
  hb = new QHBOX(this);
  line = new QLineEdit( hb );
  hb = new QHBOX(this);
  QPushButton * c = new QPushButton( tr(lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr(lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}




