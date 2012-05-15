/** @file PlotCanvas.cpp
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief 2D plot implementation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <sstream>

// #include "portability.h"

#include "ArgCheck.h"

#ifdef WARP_PLAQUETTE
  #include "thwarppt.h"
#endif

#include <QFileDialog>
#include <QMessageBox>

#define ZOOM_STEP 1.41  //!< factor to increment/decrement the scale
#define ZOOM_MIN  0.1   //!< minimum value for the scale
#define ZOOM_MAX  10.0  //!< maximum value for the scale

#include "Language.h"
#include "Extend.h"        // extend values
#include "PlotCanvas.h"
#include "PlotCanvasScene.h"
#include "PlotScale.h"
#ifdef HAS_BACKIMAGE
  #include "BackgroundImage.h"
#endif

const char * UndoCommand[] = {
  "POINT",
  "LINE",
  "ENDLINE",
  "CLOSELINE"
};

void dumpUndoList( CanvasUndo * undo )
{
  while ( undo ) {
    fprintf(stderr, "%s ", UndoCommand[ undo->getCommand() ] );
    undo = undo->next();
  }
  fprintf(stderr, "\n");
}


/** canvas mode names
 * @note indices must agree with modes defined in CanvasMode.h
 */
const char * mode_string[] = {
  "plan",
  "extended",
  "x-section",
  "3D"
};


// ============================================================
// PLOT CANVAS VIEW

PlotCanvasView::PlotCanvasView( PlotCanvasScene * c, PlotCanvas * pc )
  : QGraphicsView( c, pc )
  , plot_canvas( pc )
  , scene( c )
  , lexicon( Language::Get() )
{ 
  // FIXME setHScrollBarMode( AlwaysOn );
  // FIXME setVScrollBarMode( AlwaysOn );
}
  
// ==========================================================
// PLOT CANVAS

void 
PlotCanvas::centerOn( double x, double y ) 
{ 
  view->centerOn(x,y);
}

void
PlotCanvas::setCaption() 
{
  switch ( mode ) {
    case MODE_PLAN:
      setWindowTitle( plot_name );
      break;
    case MODE_EXT:
      setWindowTitle( plot_name );
      break;
    case MODE_CROSS:
    case MODE_HCROSS:
      setWindowTitle( lexicon("qtopo_x_select") );
      break;
    case MODE_3D:
      setWindowTitle( lexicon("qtopo_3d") );
      break;
    default:
      setWindowTitle( lexicon("qtopo_select") );
  }
}


PlotCanvas::PlotCanvas( QTshotWidget * my_parent, int the_mode, 
                        const char * pname,
                        const char * sname,
                        DBlock * block, bool reversed )
  : QMainWindow( my_parent )
  , parent( my_parent )
  , lexicon( Language::Get() )
  , icon( IconSet::Get() )
  , plot_name( pname ) 
  , mode( the_mode )
  , units( my_parent->getUnits() )
{
  DBG_CHECK("PlotCanvas::cstr() mode %d name %s %s\n", mode, pname, sname );

  setCaption();

  if ( mode == MODE_3D ) {
    parent->set3DCanvas( this );
    status = new PlotStatus( PLOT_FRAME_3D, pname, sname );
  } else { // MODE_PLAN MODE_EXT MODE_CROSS MODE_HCROSS
    status = new PlotStatus( PLOT_FRAME_GRID, pname, sname );
  }

  #ifdef HAS_LRUD
    plot = new Plot( parent->DoLRUD() );
  #else
    plot = new Plot( );
  #endif

  if ( block == NULL ) {
    // parent->GetList()->evalSplayExtended();
    if ( ! plot->computePlot( parent->getList(), mode ) ) {
      // delete this;
      return;
    }
  } else {
#if 1 // def USER_HORIZONTAL_SECTION
    // force vertical / horizontal to the user choice
    double vertical = ( mode == MODE_CROSS )? 100.0 : -1.0 ;
#else
    Config & config = Config::Get();
    double vertical = atof( config("V_THRESHOLD") );
#endif
    if ( ! plot->computeXSection( parent->getList(), block, reversed, vertical ) ) {
      delete this;
      return;
    }
  }
  createActions();
  createToolBar();

  scene = new PlotCanvasScene( 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, this );
  view  = new PlotCanvasView( scene, this );
  // frame  = new PlotFrame( scene );
  status->setScene( scene );

  // view->setFixedSize( CANVAS_WIDTH, CANVAS_HEIGHT );
  // view->resize( CANVAS_WIDTH, CANVAS_HEIGHT );
  // view->show();
  this->setCentralWidget( view );
  //  this->show();
  scene->setOffset( );
  double ls = units.length_factor;
  status->computeGrid( status->getScale() / ls,
                 (int)(status->getWidth() * ls),
                 (int)(status->getHeight() * ls),
                 units.length_unit );
  scene->showPlot( );
  showGridSpacing();
}


void 
PlotCanvas::setMode( int input_mode, const char * item_name ) 
{
  DBG_CHECK( "PlotCanvas::setMode() %d %s\n", input_mode, item_name );
  switch (input_mode) {
    case INPUT_POINT:
      if ( mode == MODE_PLAN ) {
        setWindowTitle( plot_name +  lexicon("qtopo_p_point") + QString(item_name) );
      } else if ( mode == MODE_EXT ) {
        setWindowTitle( plot_name + lexicon("qtopo_e_point") + QString(item_name) );
      } else if ( mode == MODE_CROSS ) {
        setWindowTitle( QString(lexicon("qtopo_x_point")) + QString(item_name) );
      }
      setCursor( QCursor(Qt::CrossCursor) );
      break;
    case INPUT_LINE:
      if ( mode == MODE_PLAN ) {
        setWindowTitle( plot_name + lexicon("qtopo_p_line") + QString(item_name) );
      } else if ( mode == MODE_EXT ) {
        setWindowTitle( plot_name + lexicon("qtopo_e_line") + QString(item_name) );
      } else if ( mode == MODE_CROSS ) {
        setWindowTitle( QString(lexicon("qtopo_x_line")) + QString(item_name) );
      }
      // setCursor( getCursorPen() );
      setCursor( icon->PenUp() );
      break;
    case INPUT_AREA:
      if ( mode == MODE_PLAN ) {
        setWindowTitle( plot_name + lexicon("qtopo_p_area") + QString(item_name) );
      } else if ( mode == MODE_EXT ) {
        setWindowTitle( plot_name + lexicon("qtopo_e_area") + QString(item_name) );
      } else if ( mode == MODE_CROSS ) {
        setWindowTitle( QString(lexicon("qtopo_x_area")) + QString(item_name) );
      }
      // setCursor( getCursorPen() );
      setCursor( icon->PenUp() );
      break;
    default:
      if ( mode == MODE_PLAN ) {
        setWindowTitle( plot_name + lexicon("qtopo_p_select"));
      } else if ( mode == MODE_EXT ) {
        setWindowTitle( plot_name + lexicon("qtopo_e_select"));
      } else if ( mode == MODE_CROSS ) {
        setWindowTitle(lexicon("qtopo_x_select"));
      } else if ( mode == MODE_3D ) {
        setWindowTitle(lexicon("qtopo_select"));
      }
      setCursor( QCursor(Qt::ArrowCursor) );
  }
}

//----------------------------------------------------------

/** clear list of items of all the plots
 */
void
PlotCanvas::clearTh2PointsAndLines()
{
  status->clear();
  // plan_status.Clear();
  // ext_status.Clear();
  // cross_status.Clear();
}

void
PlotCanvas::onClearTh2()
{
  DBG_CHECK("onClearTh2 \n");

  if ( mode != MODE_3D && status->getScrap()->hasItems() ) {
    CleanScrapWidget clean(this);
  }
}

void
PlotCanvas::doRealClearTh2()
{
  printf("doRealClearTh2() \n");
  status->clear();
  // ClearUndos();
  scene->clearScrap();
  turnButtonsOnOff( status->hasUndo() );
}

void
PlotCanvas::doExtend( DBlock * b, int extend, bool propagate )
{
  ARG_CHECK( b == NULL, );
  DBG_CHECK("doExtend: extend %d \n", extend );

  if ( propagate ) {
    parent->getList()->updateExtend( b /*s->block*/, extend );
    parent->showData();
  }
  if ( mode != MODE_EXT )
    return;

  // parent->GetList()->evalSplayExtended();
  // redrawPlot();
}

void 
PlotCanvas::redrawPlot()
{
  // DBG_CHECK("redrawPlot() mode %d \n",  mode );
  if ( plot->computePlot(parent->getList(), mode, true ) ) {
    scene->setOffset( );
    // FIXME scene->clearPlot();
    // fprintf(stderr, "***** PlotCanvas::redrawPlot() ***** \n");
    scene->makeSurvey();
    // scene->showPlot( );
  }
}

PlotCanvas::~PlotCanvas()
{
  DBG_CHECK("PlotCanvas::dstr\n");

  scene->endLine( false );

  if ( plot ) delete plot;
  // ClearUndos();
  DBG_CHECK("PlotCanvas::dstr done\n");
  if ( mode == MODE_PLAN ) {
    // parent->setPlanCanvas( NULL );
  } else if ( mode == MODE_EXT ) {
    // parent->setExtCanvas( NULL );
  } else if ( mode == MODE_CROSS || mode == MODE_HCROSS ) {
    // parent->setCrossCanvas( NULL );
  } else if ( mode == MODE_3D ) {
    parent->set3DCanvas( NULL );
  }

  // if ( frame ) delete frame;
}

void
PlotCanvas::onGrid()
{
  // frame->switchStatus();
  status->switchStatus();
  showGridSpacing();
  // redrawPlot();
}

void
PlotCanvas::onNumbers()
{
  status->flipNumbers();
  scene->displayNumbers();
}

void
PlotCanvas::onScrap( QAction * action )
{
  if ( scene -> onLine() ) {
    scene->endLine();
  } else if ( scene -> onArea() ) {
    scene->finishArea();
  }
  if ( action->text() == lexicon("new") ) {
    ScrapNewWidget(  this );
    return;
  }
  status->setScrap( action->text().TO_CHAR() );
  show();  // FIXME
}


void 
PlotCanvas::onSelect()
{
  scene->setInputMode( INPUT_COMMAND );
}


void
PlotCanvas::onPoint()
{
  scene->setInputMode( INPUT_POINT );
}

void
PlotCanvas::onLine()
{
  scene->setInputMode( INPUT_LINE );
}

void
PlotCanvas::onArea()
{
  scene->setInputMode( INPUT_AREA );
}

void 
PlotCanvas::onMode()
{
  DBG_CHECK("PlotCanvas::onMode()\n");

  CanvasCommandWidget ccw( this, scene );
}

void
PlotCanvas::onPointMenu( QAction * a )
{
  for ( int k = 0; k < (int)(Therion::THP_STATION); ++k ) {
    if ( a == actPointMenu[k] ) {
      scene->setPointType( k );
      scene->setInputMode( INPUT_POINT );
      break;
    }
  }
}

void
PlotCanvas::onLineMenu( QAction * a )
{
  for ( int k = 0; k < (int)(Therion::THL_PLACEMARK); ++k ) {
    if ( a == actLineMenu[k] ) {
      scene->setLineType( k, true );
      scene->setInputMode( INPUT_LINE );
      break;
    }
  }
}

void
PlotCanvas::onAreaMenu( QAction * a )
{
  for ( int k = 0; k < (int)(Therion::THA_PLACEMARK); ++k ) {
    if ( a == actAreaMenu[k] ) {
      scene->setAreaType( k, true );
      scene->setInputMode( INPUT_AREA );
      break;
    }
  }
}

void 
PlotCanvas::onClose()
{
  DBG_CHECK("PlotCanvas::onClose()\n");
  this->hide();
}

void
PlotCanvas::onQuit()
{
  QuitWidget dialog( this );
}


#ifdef HAS_BACKIMAGE
void
PlotCanvas::onImage()
{
  DBG_CHECK("PlotCanvas::onImage \n");

  doImage( QFileDialog::getOpenFileName( this, lexicon("open_sketch"), 
                     "", // direcgtory
                     "PNG files (*.png)\nJPG files (*.jpg)\nAll (*.*)" ) );
}

void
PlotCanvas::doImage( const QString & name )
{
  DBG_CHECK("PlotCanvas::doImage \"%s\"\n", name.TO_CHAR() );

  if ( ! name.isNull() && ! name.isEmpty() ) {
    bool ok_image = false;
    FILE * fp = fopen( name.TO_CHAR(), "r" );
    if ( fp != NULL ) {
      unsigned char header[16];
      if ( fread( header, 1, 16, fp ) == 16 ) {
        if ( strncmp( (char*)(header+1), "PNG", 3 ) == 0 ) {
          ok_image = true;
        } else if ( strncmp( (char*)(header+6), "JFIF", 4 ) == 0 ) {
          ok_image = true;
        }
      }
      fclose( fp );
    }
    if ( ok_image ) {
      /* BackgroundImage * image = */
        new BackgroundImage( NULL, scene, name.TO_CHAR() );
      // image.show();
    } else {
      QMessageBox::warning(parent, lexicon("qtopo_plot"),
        lexicon("image_open_failed" ) );
    }
  }
}
#endif

void
PlotCanvas::onSave( QAction * action )

{
  DBG_CHECK("PlotCanvas::onSave\n");
  if ( action->text() == lexicon( "therion" ) ) {
    TherionScrap * scrap = status->getScrap();
    // [1] check if there are two stations
    int cnt = 0;
    for (std::vector< ThPoint2D * >::iterator it = scrap->pointsBegin(); 
         it != scrap->pointsEnd();
         ++it ) {
      if ( (*it)->type() == Therion::THP_STATION ) ++cnt;
    }
    if ( cnt < 2 ) {
      ScrapWarnWidget sw( this );
    } else {
      doRealSaveTh2();
    }
  } else if ( action->text() == lexicon("image") ) {
    saveAsImage();
  } else {
    fprintf(stderr, "Error: save as %s\n", action->text().TO_CHAR() );
  }
}


void 
PlotCanvas::doRealSaveTh2( )
{
  QString name( status -> getFileName() );
  doSaveTh2( 
    QFileDialog::getSaveFileName( this, lexicon("save_th2"),
                                  name, // FIXME directory
                                  "Th2 files (*.th2)\nAll (*.*)" )
  );
}

void
PlotCanvas::saveAsImage()
{
  QString name( status -> getImageName() );
  doSaveImage( 
    QFileDialog::getSaveFileName( this, lexicon("save_png"),
                                  name, // FIXME directory
                                  "PNG files (*.png)\nAll (*.*)" )
  );
}

// --------------------------------------------------
// 3D rotations  FIXME

void 
PlotCanvas::onThetaPlus()
{
  if ( status->getTheta() < 90.0 ) {
    status->addTheta( 10.0 );
    plot->setThetaPhi( status->getTheta(), status->getPhi() );
    plot->computePlot(parent->getList(), mode, false );
    scene->makeSurvey();
    showGridSpacing();
  }
}

void 
PlotCanvas::onThetaMinus()
{
  if ( status->getTheta() > -90.0 ) {
    status->addTheta( -10.0 );
    plot->setThetaPhi( status->getTheta(), status->getPhi() );
    plot->computePlot(parent->getList(), mode, false );
    scene->makeSurvey();
    showGridSpacing();
  }
}

void 
PlotCanvas::onPhiPlus()
{
  status->addPhi( 10.0 );
  plot->setThetaPhi( status->getTheta(), status->getPhi() );
  plot->computePlot(parent->getList(), mode, false );
  scene->makeSurvey();
  showGridSpacing();
}

void 
PlotCanvas::onPhiMinus()
{
  status->addPhi( -10.0 );
  plot->setThetaPhi( status->getTheta(), status->getPhi() );
  plot->computePlot(parent->getList(), mode, false );
  scene->makeSurvey();
  showGridSpacing();
}


// =============================================================

// --------------------------------------------------
// UNDO and INSERT

void
PlotCanvas::onUndo()
{
  if ( status->hasUndo() ) {
    TherionScrap * scrap = status->getScrap();
    CanvasUndo * cu = status->popUndo();
    DBG_CHECK("onUndo() command %s \n", UndoCommand[cu->command] );
    // dumpUndoList( cu );
    switch ( cu->getCommand() ) {
      case UNDO_POINT:
        if ( scrap->pointsSize() > 0 ) {
          ThPoint2D * pt = scrap->pointsEraseLast( );
          scene->dropPoint( pt );
          scene->setInputMode( INPUT_POINT );
        }
        break;
      case UNDO_LINEPOINT:
        if ( scrap->linesSize() > 0 ) {
          assert ( scene->onLine() == true && scene->curLine() != NULL );
          if ( scene->curLine()->pointSize() > 1 ) {
            scene->dropLinePoint();
            // scene->curLine()->DropLast();
            // DBG_CHECK("     current line size %d \n", cur_line->Size() );
            scene->setInputMode( INPUT_LINE );
            scene->setOnLine( true );
          } else {
            scene->dropLine();
            scrap->linesEraseLast( );
            // DBG_CHECK("     drop line. lines %d \n", th_lines->size() );
            scene->setInputMode( INPUT_COMMAND );
            scene->setOnLine( false );
          }
          // fprintf(stderr, "UNDO_LINEPOINT cur_line %p \n", scene->curLine() );
        }
        break;
      case UNDO_ENDLINE:
        if ( scrap->linesSize() > 0 ) {
          scene->setCurLine( scrap->linesBack() );
          scene->resetLineType( scene->curLine()->type() );
          scene->setInputMode( INPUT_LINE );
          scene->setOnLine( true );
          // fprintf(stderr, "UNDO_ENDLINE cur_line %p \n", scene->curLine() );
        }
        break;
      case UNDO_CLOSELINE:
        if ( scrap->linesSize() > 0 ) {
          scene->uncloseLine( scrap->linesBack() );
          // fprintf(stderr, "UNDO_CLOSELINE cur_line %p \n", scene->curLine() );
        }
        break;
      case UNDO_AREAPOINT:
        if ( scrap->areasSize() > 0 ) {
          assert ( scene->onArea() == true && scene->curArea() != NULL );
          if ( scene->curArea()->Size() > 1 ) {
            scene->dropAreaPoint();
            // scene->curArea()->DropLast();
            // DBG_CHECK("     current line size %d \n", cur_area->Size() );
            scene->setInputMode( INPUT_AREA );
            scene->setOnArea( true );
          } else {
            scene->dropArea();
            scrap->areasEraseLast( );
            // DBG_CHECK("     drop line. lines %d \n", th_areas->size() );
            scene->setInputMode( INPUT_COMMAND );
            scene->setOnArea( false );
          }
          // fprintf(stderr, "UNDO_AREAPOINT cur_area %p \n", scene->curArea() );
        }
        break;
      case UNDO_FINISHAREA:
        if ( scrap->areasSize() > 0 ) {
          scene->unfinishArea( scrap->areasBack() );
          // fprintf(stderr, "UNDO_FINISHAREA cur_area %p \n", scene->curArea() );
        }
        break;
    }
    delete cu;
  }  
  turnButtonsOnOff( status->hasUndo() );
}

// station points are black crosses
ThPoint2D * 
PlotCanvas::insertPointStation( double x, double y, const char * option )
{
  DBG_CHECK("insertPointStation X %.2f Y %.2f option \"%s\" offset %d %d\n",
     x, y, option,  status->getOffsetX(), status->getOffsetY() );

  x += status->getOffsetX();
  y += status->getOffsetY();
  // x = status->evalToX( x );
  // y = status->evalToY( y );
  ThPoint2D * pt = new ThPoint2D( x, y, Therion::THP_STATION, 0, option );
  status->getScrap()->pointsAdd( pt );
  status->addUndo( UNDO_POINT );
  turnButtonsOnOff( status->hasUndo() );
  // showPoint( x, y, Qt::red );
  // FIX-3 scene->showPlot();
  // double s = status->getScale();
  // view->updateContents( (int)(x * s)-4, (int)(y * s)-4, 9, 9 );
  return pt;
}

bool
PlotCanvas::hasStation( const std::string & option )
{
  TherionScrap * scrap = status->getScrap();
  for ( std::vector< ThPoint2D * >::const_iterator it = scrap->pointsBegin(), 
        end = scrap->pointsEnd(); it != end; ++it ) {
    ThPoint2D * pt = *it;
    if ( pt->type() == Therion::THP_STATION && option == pt->option()  ) return true;
  }
  return false;
}

// ----------------------------------------------------
// ZOOM in/out, SPLAY shots, and SAVE

void 
PlotCanvas::onZoomIn()
{
  scene->zoom( 1 );
  view->scale( ZOOM_STEP, ZOOM_STEP );
}

void
PlotCanvas::onZoomOut()
{
  scene->zoom( -1 );
  view->scale( 1.0/ZOOM_STEP, 1.0/ZOOM_STEP );
}

void 
PlotCanvas::update()
{
  // fprintf(stderr, "PlotCanvas::update() \n");
  scene->showPlot( );
}


void
PlotCanvas::onSplay()
{
  scene->toggleSplay();
}

void 
PlotCanvas::onEval()
{
  DBG_CHECK("onEval\n");
  // ask parent to recompute centerline
  parent->redoNum();
  // TODO recompute plot
  redrawPlot( );
}


// ------------------------------------------------
// export

void
PlotCanvas::doSaveTh2( const QString & file )
{
  if ( file.isNull() ) { // got a file name
    DBG_CHECK("doSaveTh2 filename is empty\n");
    return;
  }
  status -> setFileName( file.TO_CHAR() );
  PlotThExport::exportTherion( mode_string[mode], status, parent->getList() );
}


void
PlotCanvas::doSaveImage( const QString & file )
{
  if ( file.isNull() ) { // got a file name
    DBG_CHECK("doSaveImage filename is empty\n");
    return;
  }
  status -> setImageName( file.TO_CHAR() );
  PlotThExport::exportImage( status, parent->getList() );
}

void 
PlotCanvas::showGridSpacing()
{
  if ( mode == MODE_3D ) {
    QString text;
    text.sprintf("  I: %.0f  A: %.0f", status->getTheta(), status->getPhi() );
    point_of_view->setText( text );
  } else {
    // int gs = frame->GridSpacing();
    int gs = status->getGridSpacing();
    if ( gs > 0 ) {
      QString text;
      text.sprintf("%s: %d %s", lexicon("grid"), gs, units.length_unit );
      point_of_view->setText( text );
    } else {
      point_of_view->setText( "" );
    }
  }
}

//----------------------------------------------------------
void
PlotCanvas::createToolBar()
{
  QToolBar * toolbar = addToolBar( tr("") );
  setToolButtonStyle( Qt::ToolButtonIconOnly );

  if ( actNew ) toolbar->addAction( actNew );
  if ( actSave ) toolbar->addAction( actSave );
  toolbar->addAction( actEval );
  #ifdef HAS_BACKIMAGE
    if ( actImage ) toolbar->addAction( actImage );
  #endif
  // if ( actGrid ) toolbar->addAction( actGrid );
  // toolbar->addAction( actCollapse );
  // toolbar->addAction( actNumbers );
  toolbar->addAction( actView );

  if ( actScrap ) toolbar->addAction( actScrap );
  if ( actSelect ) toolbar->addAction( actSelect );
  if ( actPoint ) toolbar->addAction( actPoint );
  if ( actLine ) toolbar->addAction( actLine );
  if ( actArea ) toolbar->addAction( actArea );

  if ( actUndo ) toolbar->addAction( actUndo );
  toolbar->addAction( actZoomIn );
  toolbar->addAction( actZoomOut );
  if ( actThetaPlus ) toolbar->addAction( actThetaPlus );
  if ( actThetaMinus ) toolbar->addAction( actThetaMinus );
  if ( actPhiPlus ) toolbar->addAction( actPhiPlus );
  if ( actPhiMinus ) toolbar->addAction( actPhiMinus );
  toolbar->addAction( actClose );
  // toolbar->addAction( actQuit );
  // if ( mode == MODE_3D ) 
  point_of_view = new QLabel( "" );
  toolbar->addWidget( point_of_view );
}

void
PlotCanvas::createActions()
{
  if ( mode != MODE_3D ) {
    actSave = new QAction( icon->Save(), lexicon("save"),  this );
    QMenu * save_menu = new QMenu( this );
    save_menu->addAction( lexicon("therion") );
    save_menu->addAction( lexicon("image") );
    actSave->setMenu( save_menu );
    connect( save_menu,SIGNAL(triggered( QAction * )), this, SLOT(onSave( QAction * )) );
    actSave->setVisible( true );
#ifdef HAS_BACKIMAGE
    actImage = new QAction( icon->Image(), lexicon("sketch"), this );
    connect( actImage, SIGNAL(triggered()), this, SLOT(onImage()) );
    actImage->setVisible( true );
#endif
    // actGrid = new QAction( icon->Grid(), lexicon("grid"), this );
    // connect( actGrid, SIGNAL(triggered()), this, SLOT(onGrid()) );
    // actGrid->setVisible( true );
  } else {
    actSave = NULL;
    #ifdef HAS_BACKIMAGE
      actImage = NULL;
    #endif
    actGrid = NULL;
  }

  actEval = new QAction( icon->Eval(), lexicon("eval"), this );
  connect( actEval, SIGNAL(triggered()), this, SLOT(onEval()) );

  actView = new QAction( icon->View(), lexicon("view"), this );
  view_menu = new QMenu( this );
  actCollapse = view_menu->addAction( lexicon("splay") );
  connect( actCollapse, SIGNAL(triggered()), this, SLOT(onSplay()) );
  actCollapse -> setCheckable( true );
  actCollapse -> setChecked( false );
  if ( mode != MODE_3D ) {
    actGrid = view_menu->addAction( lexicon("grid") );
    connect( actGrid, SIGNAL(triggered()), this, SLOT(onGrid()) );
    actGrid -> setCheckable( true );
    actGrid -> setChecked( true );
  }
  actNumbers = view_menu->addAction( lexicon("number") );
  connect( actNumbers, SIGNAL(triggered()), this, SLOT(onNumbers()) );
  actNumbers -> setCheckable( true );
  actNumbers -> setChecked( true );
  actView->setMenu( view_menu );

  actZoomIn   = new QAction( icon->ZoomIn(), lexicon("zoom_in"), this );
  actZoomOut  = new QAction( icon->ZoomOut(), lexicon("zoom_out"), this );
  connect( actZoomIn, SIGNAL(triggered()), this, SLOT(onZoomIn()) );
  connect( actZoomOut, SIGNAL(triggered()), this, SLOT(onZoomOut()) );
  actEval->setVisible( true );
  actCollapse->setVisible( true );
  actNumbers->setVisible( true );
  actZoomIn->setVisible( true );
  actZoomOut->setVisible( true );

  if ( mode != MODE_3D ) {
    actNew = new QAction( icon->NewOff(), lexicon("clear"), this );
    actUndo = new QAction( icon->UndoOff(), lexicon("undo"), this );

    turnButtonsOnOff( status->hasUndo() );

    actScrap = new QAction( icon->Scrap(), lexicon("scrap"), this );
    scrap_menu = new QMenu( this );
    scrap_menu->addAction( lexicon("new") );
    for ( std::vector<TherionScrap *>::const_iterator sit = status->scrapBegin(); 
          sit != status->scrapEnd();
          ++ sit ) {
      scrap_menu->addAction( (*sit)->getScrapName() );
    }
    actScrap->setMenu( scrap_menu );

    actSelect = new QAction( icon->Select(), lexicon("select"), this );
    QMenu * select_menu = new QMenu( this );
    actSelectMenu[0] = select_menu->addAction( lexicon("select") );
    actSelectMenu[1] = select_menu->addAction( lexicon("orientation") );
    actSelect->setMenu( select_menu );

    actPoint  = new QAction( icon->Point(), lexicon("point"), this );
    QMenu * point_menu = new QMenu( this );
    // FIXME in pure-editor mode need to allow also "station" point insertion
    for (int k=0; k<(int)(Therion::THPG_STATION); ++k ) {
      const int * index = Therion::PointGroup[k];
      if ( index[1] < 0 ) {
        actPointMenu[index[0]] = point_menu->addAction( Therion::PointName[ index[0] ] );
      } else {
        QMenu * submenu = point_menu->addMenu( Therion::PointGroupName[ k ] );
        for ( int j=0; index[j] >= 0; ++j ) {
          actPointMenu[index[j]] = submenu->addAction( Therion::PointName[ index[j] ] );
        }
      }
      // actPointMenu[k] = point_menu->addAction( Therion::PointName[ k ] );
    }
    actPoint->setMenu( point_menu );
    // connect( actPoint, SIGNAL(triggered()), point_menu, SLOT( showNormal() ) );
    
    actLine   = new QAction( icon->Line(), lexicon("line"), this );
    QMenu * line_menu = new QMenu( this );
    for (int k=0; k<(int)(Therion::THL_PLACEMARK); ++k ) {
      actLineMenu[k] = line_menu->addAction( Therion::LineName[ k ] );
    }
    actLine->setMenu( line_menu );
    
    actArea   = new QAction( icon->Area(), lexicon("area"), this );
    QMenu * area_menu = new QMenu( this );
    for (int k=0; k<(int)(Therion::THA_PLACEMARK); ++k ) {
      actAreaMenu[k] = area_menu->addAction( Therion::AreaName[ k ] );
    }
    actArea->setMenu( area_menu );

    actThetaPlus  = NULL;
    actThetaMinus = NULL;
    actPhiPlus  = NULL;
    actPhiMinus = NULL;
    connect( actNew, SIGNAL(triggered()), this, SLOT(onClearTh2()) );
    connect( actUndo, SIGNAL(triggered()), this, SLOT(onUndo()) );

    connect( scrap_menu, SIGNAL(triggered(QAction*)), this, SLOT(onScrap(QAction*)) ); 

    connect( actSelect, SIGNAL(triggered()), this, SLOT(onSelect()) );
    connect( actSelectMenu[0], SIGNAL(triggered()), this, SLOT(onSelect()) );
    connect( actSelectMenu[1], SIGNAL(triggered()), this, SLOT(onMode()) );

    connect( actPoint, SIGNAL(triggered()), this, SLOT(onPoint()) );
    connect( actLine, SIGNAL(triggered()), this, SLOT(onLine()) );
    connect( actArea, SIGNAL(triggered()), this, SLOT(onArea()) );
    connect( point_menu, SIGNAL(triggered( QAction * )), this, SLOT( onPointMenu( QAction * ) ) );
    connect( line_menu, SIGNAL(triggered( QAction * )), this, SLOT( onLineMenu( QAction * ) ) );
    connect( area_menu, SIGNAL(triggered( QAction * )), this, SLOT( onAreaMenu( QAction * ) ) );

    actNew->setVisible( true );
    actUndo->setVisible( true );
    actPoint->setVisible( true );
    actLine->setVisible( true );
    actArea->setVisible( true );
  } else {
    actNew  = NULL;
    actUndo = NULL;
    actPoint = NULL;
    actLine = NULL;
    actArea = NULL;
    actThetaPlus = new QAction( icon->ThetaPlus(), lexicon("incl+"), this );
    actThetaMinus = new QAction( icon->ThetaMinus(), lexicon("incl-"), this );
    actPhiPlus = new QAction( icon->PhiPlus(), lexicon("azimuth+"), this );
    actPhiMinus = new QAction( icon->PhiMinus(), lexicon("azimuth-"), this );
    connect( actThetaPlus, SIGNAL(triggered()), this, SLOT(onThetaPlus()) );
    connect( actThetaMinus, SIGNAL(triggered()), this, SLOT(onThetaMinus()) );
    connect( actPhiPlus, SIGNAL(triggered()), this, SLOT(onPhiPlus()) );
    connect( actPhiMinus, SIGNAL(triggered()), this, SLOT(onPhiMinus()) );
    actThetaPlus->setVisible( true );
    actThetaMinus->setVisible( true );
    actPhiPlus->setVisible( true );
    actPhiMinus->setVisible( true );
  }
  actClose = new QAction( icon->Close(), lexicon("close"), this );
  QMenu * close_menu = new QMenu( this );
  QAction * actClose2 = close_menu->addAction( lexicon("close") );
  actQuit = close_menu->addAction( lexicon("exit") );
  actClose->setMenu( close_menu );

  connect( actClose, SIGNAL(triggered()), this, SLOT(onClose()) );
  connect( actClose2, SIGNAL(triggered()), this, SLOT(onClose()) );
  connect( actQuit, SIGNAL(triggered()), this, SLOT(onQuit()) );
  actClose->setVisible( true );
}

// ===========================================================================
// NEW SCRAP WIDGET

ScrapNewWidget::ScrapNewWidget( PlotCanvas * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("scrap_new") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  QWidget * hb;
  QHBoxLayout * hbl;
 
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  hbl->addWidget( new QLabel( lexicon("scrap_name"), hb ) );
  line = new QLineEdit( "", this );
  hbl->addWidget( line );
  vbl->addWidget( hb );

  QPushButton * c0;
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  c0 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c0, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c0 );
  c0 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c0, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c0 );
  vbl->addWidget( hb );

  // show();
  exec();
}

void
ScrapNewWidget::doOK()
{
  hide();
  if ( ! line->text().isEmpty() ) {
    parent->doNewScrap( line->text().TO_CHAR() );
  }
}


// ===========================================================================
// CANVAS COMMAND WIDGET

CanvasCommandWidget::CanvasCommandWidget( QWidget * parent, PlotCanvasScene * c )
  : QDialog( parent )
  , scene( c )
  // , np_type( (int)Therion::THP_PLACEMARK )
  // , nl_type( (int)Therion::THL_PLACEMARK )
  // , ar_type( (int)Therion::THA_PLACEMARK )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("canvas_mode") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  QWidget * hb;
  QHBoxLayout * hbl;
 
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  hbl->addWidget( new QLabel( lexicon("orientation"), hb ) );
  porient = new QDial( hb );
  porient->setMinimum( 0 );
  porient->setMaximum( 360 );
  porient->setWrapping( true );
  porient->setNotchesVisible( true );
  porient->setNotchTarget( ORIENTATION_UNITS );
  // DBG_CHECK("orientation %d \n", scene->getPointOrient() * 45 - 180 );
  porient->setValue( (scene->getPointOrient() * ORIENTATION_UNITS + 180)%360 );
  hbl->addWidget( porient );
  vbl->addWidget( hb );

  QPushButton * c0;
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  // c0 = new QPushButton( tr( lexicon("undo") ), hb );
  // connect( c0, SIGNAL(clicked()), this, SLOT(doUndo()) );
  c0 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c0, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c0 );
  c0 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c0, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c0 );
  vbl->addWidget( hb );

  // show();
  exec();
}


void
CanvasCommandWidget::doOK()
{
  hide();
  int angle = ( ( porient->value() + 180 + ORIENTATION_HALF) / ORIENTATION_UNITS ) % ORIENTATION_MAX;
  scene->setPointOrient( angle );
}


// ===========================================================================
// CLEAN-SCRAP WIDGET
// clear the drawing

CleanScrapWidget::CleanScrapWidget( PlotCanvas * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_clean_scrap") );
  QVBoxLayout* vbl = new QVBoxLayout(this );
  vbl->addWidget( new QLabel( lexicon("clean_scrap"), this ) );

  QWidget * hb;
  QHBoxLayout * hbl;

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  // show();
  exec();
}


// ===========================================================================
// SCRAP WIDGET
// get the scrap name on save (export as therion file)

ScrapWarnWidget::ScrapWarnWidget( PlotCanvas * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_scrap") );
  QVBoxLayout* vbl = new QVBoxLayout(this);
  vbl->addWidget( new QLabel( lexicon("scrap_name"), this ) );
  vbl -> addWidget( new QLabel( parent->getScrapName(), this ) );

  vbl->addWidget( new QLabel( lexicon("warn_scrap"), this ) );
 
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
ScrapWarnWidget::doOK()
{
  hide();
  parent->doRealSaveTh2();
}

// ===========================================================================
// STATION COMMENT WIDGET
// edit the comment to a station point

StationCommentWidget::StationCommentWidget( QWidget * p, PlotCanvas * pc, const char * n )
  : QDialog( p )
  , plot_canvas( pc )
  , name( n )
{
  DBG_CHECK("StationCommentWidget list %p point %s", (void *)plot_canvas->GetList(), name );

  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_station_comment" ) );
  
  QVBoxLayout* vbl = new QVBoxLayout(this);
  // vbl->setAutoAdd(TRUE);

  std::ostringstream oss;
  oss << lexicon("station") << " " << name;
  vbl->addWidget( new QLabel( oss.str().c_str(), this ) );
  if ( plot_canvas->getList()->hasStationComment( name ) ) {
    comment = new QLineEdit( plot_canvas->getList()->getStationComment( name ), this );
  } else {
    comment = new QLineEdit( "", this );
  }
  vbl->addWidget( comment );
  

  QWidget * hb;
  QHBoxLayout * hbl;
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );

  QPushButton * c1;
  c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c1 );
  c1 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c1, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c1 );
  vbl->addWidget( hb );

  // show();
  exec();
}

void
StationCommentWidget::doOK()
{
  // if ( ! comment->text().isEmpty() ) // allow to erase comment
  {
    plot_canvas->getList()->setStationComment( name, comment->text().TO_CHAR() );
    plot_canvas->propagateUpdate();
  }
  hide();
}

  
  
// ===========================================================================
// EXTEND WIDGET
// shot extend flag widget

ExtendWidget::ExtendWidget( QWidget * p, PlotCanvas * pc, DBlock * b )
  : QDialog( p )
  , plot_canvas( pc )
  , block( b )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_extend") );
  
  QVBoxLayout* vbl = new QVBoxLayout(this);
  // vbl->setAutoAdd(TRUE);
  if ( b->hasFromStation() ) {
    std::ostringstream oss;
    oss << lexicon("from") << " " << b->fromStation();
    if ( b->hasToStation() )
      oss << " " << lexicon("to") << " " << b->toStation();
    vbl->addWidget( new QLabel( oss.str().c_str(), this ) );
  } else if ( b->hasToStation() ) {
    std::ostringstream oss;
      oss << lexicon("to") << " " << b->toStation();
    vbl->addWidget( new QLabel( oss.str().c_str(), this ) );
  }
  char value[64];
  sprintf(value, "D. %.2f   N. %.1f   C. %.1f", b->Tape(), b->Compass(), b->Clino() );
  vbl->addWidget( new QLabel( value, this ) );

  comment = new QLineEdit( block->getComment(), this );
  vbl->addWidget( comment );

  if ( plot_canvas->getMode() == MODE_EXT ) {
    QWidget * vb = new QWidget( this );
    QVBoxLayout * vbl1 = new QVBoxLayout( vb );
    extBox[EXTEND_NONE]   = new QRadioButton( lexicon("none"), vb );
    extBox[EXTEND_LEFT]   = new QRadioButton( lexicon("left"), vb );
    extBox[EXTEND_RIGHT]  = new QRadioButton( lexicon("right"), vb );
    extBox[EXTEND_VERT]   = new QRadioButton( lexicon("vertical"), vb );
    extBox[EXTEND_IGNORE] = new QRadioButton( lexicon("ignore"), vb );
    QButtonGroup * m_group = new QButtonGroup( );
    for (int i=0; i<EXTEND_MAX; ++i ) {
      m_group->addButton( extBox[i] );
      vbl1->addWidget( extBox[i] );
    }
    extBox[ block->Extend() ]->setChecked( true );
    vbl->addWidget( vb );
  }
  
  QPushButton * c1;
  QWidget * hb = new QWidget(this);
  QHBoxLayout * hbl = new QHBoxLayout( hb );
  c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c1 );
  c1 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c1, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c1 );
  vbl->addWidget( hb );

  // show();
  exec();
}

void
ExtendWidget::doOK()
{
  hide();
  block->setComment( comment->text().TO_CHAR() );
  if ( plot_canvas->getMode() == MODE_EXT ) {
    for (int i=0; i<EXTEND_MAX; ++i) {
      if ( extBox[i]->isChecked() ) {
        // block->extend = i;
        plot_canvas->doExtend( block, i, true ); // true: propagate update to parent
        break;
      }
    }
  }
  plot_canvas->propagateUpdate();
  // redrawPlot();
}

void 
ExtendWidget::doCancel()
{
  hide();
}


// ============================================================
#if 0 // def HAS_BACKIMAGE
MyFileDialogSketch::MyFileDialogSketch( PlotCanvas * parent,
                                        const char * title, 
                                        int m )
      : QDialog( parent )
      , widget( parent )
      , mode( m )
{
  Language & lexicon = Language::Get();
  setWindowTitle( title );
  // DBG_CKECK("MyFileDialogSketch \n");
  QVBoxLayout* vbl = new QVBoxLayout(this);
  // vbl->setAutoAdd(TRUE);
  
  QWidget * hb;
  QHBoxLayout * hbl;
  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  hbl->addWidget( new QLabel(lexicon("enter_filename"), hb ) );
  vbl->addWidget( hb );

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  line = new QLineEdit( hb );
  hbl->addWidget( line );
  vbl->addWidget( hb );

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  QPushButton * c = new QPushButton( tr(lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr(lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  // show();
  exec();
}
#endif

QuitWidget::QuitWidget( PlotCanvas * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("plot_exit") );
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

 void 
QuitWidget::doOK() 
{ 
  // fprintf(stderr, "QuitWidget::doOK()\n");
  hide();
  parent->doQuit();
}
