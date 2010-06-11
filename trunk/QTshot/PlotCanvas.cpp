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

#include "portability.h"

#include "ArgCheck.h"

#ifdef WARP_PLAQUETTE
  #include "thwarppt.h"
#endif

#include <qfiledialog.h>
#include <qbitmap.h>
#include <qmessagebox.h>

// only QT v.4
// #include <qlineargradient.h>

/** size of the ticks of the pit lines (pixels ?)
 */
#define PIT_TICK 4

#define PERCENT_OFFSET 10 /* one tenth */
#define MIN_OFFSET 50 /* pixels */

#define ZOOM_STEP 1.41  //!< factor to increment/decrement the scale
#define ZOOM_MIN  0.1   //!< minimum value for the scale
#define ZOOM_MAX  10.0  //!< maximum value for the scale

#include "Language.h"
#include "Extend.h"        // extend values
#include "PlotCanvas.h"
#include "PlotScale.h"
#ifdef HAS_BACKIMAGE
  #include "BackgroundImage.h"
#endif


/** border pixels in the plots
 */
#define BORDER 0 // 20

const char * UndoCommand[] = {
  "POINT",
  "LINE",
  "ENDLINE",
  "CLOSELINE"
};

void dumpUndoList( CanvasUndo * undo )
{
  while ( undo ) {
    fprintf(stderr, "%s ", UndoCommand[undo->command] );
    undo = undo->next;
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


PlotStatus plan_status;
PlotStatus ext_status;
PlotStatus cross_status;
PlotStatus _3D_status;



// ============================================================
// PLOT CANVAS VIEW

PlotCanvasView::PlotCanvasView( QCANVAS * c, PlotCanvas * pc )
  : QCANVASVIEW( c, pc )
  , plot_canvas( pc )
  , lexicon( Language::Get() )
  , the_line( NULL )
  , input_mode( INPUT_COMMAND )
  , point_type( THP_USER )
  , point_orient( 0 )
  , line_type( THL_WALL )
{ 
  setHScrollBarMode( AlwaysOn );
  setVScrollBarMode( AlwaysOn );

  switch ( plot_canvas->getMode() ) {
    case MODE_PLAN:
      plot_canvas->setCaption( lexicon("qtopo_p_select") );
      break;
    case MODE_EXT:
      plot_canvas->setCaption( lexicon("qtopo_e_select") );
      break;
    case MODE_CROSS:
    case MODE_HCROSS:
      plot_canvas->setCaption( lexicon("qtopo_x_select") );
      break;
    case MODE_3D:
      plot_canvas->setCaption( lexicon("qtopo_3d") );
      break;
    default:
      plot_canvas->setCaption( lexicon("qtopo_select") );
  }
}

void 
PlotCanvasView::contentsMousePressEvent(QMouseEvent* e)
{
  ARG_CHECK( e == NULL, );

  // DBG_CHECK("contentsMousePressEvent X %d Y %d \n", e->x(), e->y() );
  if ( plot_canvas->getMode() == MODE_3D )
    return;

  cur_pt.setX( e->x() );
  cur_pt.setY( e->y() );
  if ( input_mode == INPUT_LINE ) {
    plot_canvas->insertLinePoint( e->x(), e->y(), line_type );
  } else if ( input_mode == INPUT_POINT ) {
    if ( point_type == THP_LABEL || point_type == THP_CONTINUATION ) {
      label_text = "";
      const char * caption = "";
      if ( point_type == THP_LABEL ) { caption = lexicon("qtopo_label"); }
      else if ( point_type == THP_CONTINUATION ) { caption = lexicon("qtopo_continuation"); }
      LabelWidget * text = new LabelWidget( this, caption );
      text->exec();
      if ( label_text.size() > 0 ) {
        std::string option("-text \"");
        option += label_text;
        option += "\"";
        plot_canvas->insertPoint( e->x(), e->y(), point_type, point_orient, option.c_str() );
      }
    } else {
      plot_canvas->insertPoint( e->x(), e->y(), point_type, point_orient );
    }
  } else { // INPUT_COMMAND
    QCANVASITEMLIST list = canvas()->collisions(e->pos());
    // int mode = plot_canvas->getMode();
    if ( list.isEmpty() /* || mode == MODE_CROSS || mode == MODE_HCROSS */ ) {
      /*
      if ( plot_canvas->getMode() != MODE_CROSS )
        zoom->insertItem("Toggle Splay", plot_canvas, SLOT(onSplay()) );
      zoom->insertItem("Save", this, SLOT(onSaveTh2()) );
      zoom->insertItem("Clear", plot_canvas, SLOT(onClearTh2()) ); // TODO
      zoom->show();
      zoom->exec();
      */
    } else { // if ( mode != MODE_CROSS && mode != MODE_HCROSS ) {
      QCANVASITEM * item = list.first();
      if ( item != NULL ) {
        if ( item->rtti() == 3 ) {
          QCANVASTEXT * text = (QCANVASTEXT *)( item );
          CanvasPoint* p = plot_canvas->getTextPoint( text );
          if ( p ) {
            std::ostringstream oss;
            oss << "-name " << text->text().latin1();
            if ( ! plot_canvas->hasStation( oss.str() ) ) {
              plot_canvas->insertStationPoint( p->x0, p->y0, oss.str().c_str() );
            } else {
              new StationCommentWidget( this, plot_canvas, text->text().latin1() );
            }
          }
        } else if ( /* plot_canvas->getMode() == MODE_EXT && */ item->rtti() == 7 ) {
          the_line = (QCANVASLINE *)( item );
          CanvasSegment * s = plot_canvas->getSegmentFromLine( the_line );
          if ( s && s->block ) {
            // ExtendWidget * ew = 
            new ExtendWidget( this, plot_canvas, s->block );
            // ew->show();
            // ew->exec();
          }
        }
      }
    }
  }
}


void
PlotCanvasView::setInputMode( InputMode mode )
{
  DBG_CHECK("setInputMode() %d \n", (int)(mode) );

  input_mode = mode;
  if ( mode == INPUT_POINT ) {
    plot_canvas->setMode( mode, PlotThExport::ThPointName[(int)point_type] );
  } else if ( mode == INPUT_LINE ) {
    plot_canvas->setMode( mode, PlotThExport::ThLineName[(int)line_type] );;
  } else {
    plot_canvas->setMode( mode );
  }
}


// ==========================================================
// PLOT CANVAS


PlotCanvas::PlotCanvas( QTshotWidget * my_parent, int the_mode, DBlock * block, bool reversed )
  : QMAINWINDOW( my_parent )
  , parent( my_parent )
  , lexicon( Language::Get() )
  , icon( IconSet::Get() )
  , mode( the_mode )
  , cur_line( NULL )
  , units( my_parent->GetUnits() )
  , do_splay( false )
  , on_line( false )
  #ifdef HAS_BACKIMAGE
    , sketch( NULL )
    , backPixmap( NULL )
  #endif
{
  DBG_CHECK("PlotCanvas::cstr() mode %d \n", mode );

  if ( mode == MODE_PLAN ) {
    plot_export.setScrapname( lexicon("scrap_prefix_p") );
    parent->setPlanCanvas( this );
    status = &plan_status;
  } else if ( mode == MODE_EXT ) {
    plot_export.setScrapname( lexicon("scrap_prefix_e") );
    parent->setExtCanvas( this );
    status = &ext_status;
  } else if ( mode == MODE_CROSS || mode == MODE_HCROSS ) {
    if ( mode == MODE_CROSS ) {
      plot_export.setScrapname( lexicon("scrap_prefix_x") );
    } else if ( mode == MODE_HCROSS ) {
      plot_export.setScrapname( lexicon("scrap_prefix_h") );
    }
    parent->setCrossCanvas( this );
    status = &cross_status;
    status->Clear();
  } else { // if ( mode == MODE_3D ) {
    parent->set3DCanvas( this );
    status = &_3D_status;
  }

  #ifdef HAS_LRUD
    plot = new Plot( parent->DoLRUD() );
  #else
    plot = new Plot( );
  #endif

  if ( block == NULL ) {
    // parent->GetList()->evalSplayExtended();
    if ( ! plot->computePlot( parent->GetList(), mode ) ) {
      delete this;
      return;
    }
  } else {
#ifdef USER_HORIZONTAL_SECTION
    // force vertical / horizontal to the user choice
    double vertical = ( mode == MODE_CROSS )? 100.0 : -1.0 ;
#else
    Config & config = Config::Get();
    double vertical = atof( config("V_THRESHOLD") );
#endif
    if ( ! plot->computeXSection( parent->GetList(), block, reversed, vertical ) ) {
      delete this;
      return;
    }
  }

  QTOOLBAR * toolbar = new QTOOLBAR( this );
  if ( mode != MODE_3D ) {
    new QTOOLBUTTON( icon->Save(), lexicon("save"), QString::null,
                     this, SLOT(onSaveTh2()), toolbar, lexicon("save") );
#ifdef HAS_BACKIMAGE
    new QTOOLBUTTON( icon->Image(), lexicon("sketch"), QString::null,
                     this, SLOT(onImage()), toolbar, lexicon("sketch") );
#endif
    new QTOOLBUTTON( icon->Grid(), lexicon("grid"), QString::null,
                     this, SLOT(onGrid()), toolbar, lexicon("grid") );
  }
    new QTOOLBUTTON( icon->Collapse(), lexicon("splay"), QString::null,
                     this, SLOT(onSplay()), toolbar, lexicon("splay") );
    new QTOOLBUTTON( icon->Number(), lexicon("number"), QString::null,
                     this, SLOT(onNumbers()), toolbar, lexicon("number") );
    new QTOOLBUTTON( icon->ZoomIn(), lexicon("zoom_in"), QString::null,
                     this, SLOT(onZoomIn()), toolbar, lexicon("zoom_in") );
    new QTOOLBUTTON( icon->ZoomOut(), lexicon("zoom_out"), QString::null,
                     this, SLOT(onZoomOut()), toolbar, lexicon("zoom_out") );
  if ( mode != MODE_3D ) {
    btnNew = 
      new QTOOLBUTTON( icon->NewOff(), lexicon("clear"), QString::null,
                       this, SLOT(onClearTh2()), toolbar, lexicon("clear") );
    btnUndo = 
      new QTOOLBUTTON( icon->UndoOff(), lexicon("undo"), QString::null,
                       this, SLOT(onUndo()), toolbar, lexicon("undo") );

    OnOffButtons( status->HasUndo() );

    btnMode = 
      new QTOOLBUTTON( icon->Mode1(), lexicon("mode"), QString::null,
                       this, SLOT(onMode()), toolbar, lexicon("mode") );
  } else {
    new QTOOLBUTTON( icon->ThetaPlus(), lexicon("incl+"), QString::null,
                     this, SLOT(onThetaPlus()), toolbar, lexicon("incl+") );
    new QTOOLBUTTON( icon->ThetaMinus(), lexicon("incl-"), QString::null,
                     this, SLOT(onThetaMinus()), toolbar, lexicon("incl-") );
    new QTOOLBUTTON( icon->PhiPlus(), lexicon("azimuth+"), QString::null,
                     this, SLOT(onPhiPlus()), toolbar, lexicon("azimuth+") );
    new QTOOLBUTTON( icon->PhiMinus(), lexicon("azimuth-"), QString::null,
                     this, SLOT(onPhiMinus()), toolbar, lexicon("azimuth-") );
  }
    new QTOOLBUTTON( icon->Close(), lexicon("exit"), QString::null,
                     this, SLOT(onQuit()), toolbar, lexicon("exit") );

  // if ( mode == MODE_3D ) 
  point_of_view = new QLabel( "", toolbar );

  canvas = new QCANVAS( CANVAS_WIDTH, CANVAS_HEIGHT );
  view = new PlotCanvasView( canvas, this );

  // view->setFixedSize( CANVAS_WIDTH, CANVAS_HEIGHT );
  // view->resize( CANVAS_WIDTH, CANVAS_HEIGHT );
  // view->show();
  this->setCentralWidget( view );
  //  this->show();
  setOffset( );
  showPlot( );
  showGridSpacing();
}

/** clear list of items of all the plots
 */
void
PlotCanvas::ClearTh2PointsAndLines()
{
  plan_status.Clear();
  ext_status.Clear();
  cross_status.Clear();
}

void
PlotCanvas::onClearTh2()
{
  DBG_CHECK("onClearTh2 \n");

  if ( mode != MODE_3D && status->hasItems() ) {
    CleanScrapWidget * clean = new CleanScrapWidget(this);
    clean->exec();
  }
}

void
PlotCanvas::doRealClearTh2()
{
  status->Clear();
  // ClearUndos();
  showPlot();
  OnOffButtons( status->HasUndo() );
}

CanvasPoint* 
PlotCanvas::getTextPoint( QCANVASTEXT * text )
{
  ARG_CHECK( text == NULL, NULL );
  DBG_CHECK("getTextPoint() \n");

  for ( std::vector< std::pair<QCANVASTEXT*, CanvasPoint*> >::iterator it = text_items.begin();
        it != text_items.end();
        ++it ) {
    if ( text == it->first ) {
      return it->second;
    }
  }
  return NULL;
}

CanvasSegment *
PlotCanvas::getSegmentFromLine( QCANVASLINE * line )
{
  ARG_CHECK( line == NULL, NULL );
  DBG_CHECK("getSegmentFromLine() \n");

  CanvasSegment * s = NULL;
  std::vector< std::pair<QCANVASLINE *, CanvasSegment*> >::iterator end = line_items.end();
  for ( std::vector< std::pair<QCANVASLINE *, CanvasSegment*> >::iterator it = line_items.begin();
        it != end; ++it ) {
    if ( it->first == line ) {
      s = it->second;
      break;
    }
  }
  return s;
}

void
PlotCanvas::doExtend( DBlock * b, int extend, bool propagate )
{
  ARG_CHECK( b == NULL, );
  DBG_CHECK("doExtend: extend %d \n", extend );

  if ( propagate ) {
    parent->GetList()->updateExtend( b /*s->block*/, extend );
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
  if ( plot->computePlot(parent->GetList(), mode, true ) ) {
    setOffset( );
    showPlot( );
  }
}

void
PlotCanvas::setOffset()
{
  int offx = MIN_OFFSET;
  int offy = MIN_OFFSET;
  
  if ( mode == MODE_CROSS || mode == MODE_HCROSS ) {
    status->SetOffsetX( offx );
    status->SetOffsetY( offy );
  } else {
    int dx = - status->OffsetX(); // previous offset
    int dy = - status->OffsetY();
    int offx = plot->getWidth()/PERCENT_OFFSET;
    if ( offx < MIN_OFFSET ) offx = MIN_OFFSET;
    offx -= plot->xOffset();
    int offy = plot->getHeight()/PERCENT_OFFSET;
    if ( offy < MIN_OFFSET ) offy = MIN_OFFSET;
    offy -= plot->yOffset();
    status->SetOffsetX( offx );
    status->SetOffsetY( offy );
    // shift drawing by the offset difference
    dx += offx;
    dy += offy;
    status->ShiftItems( dx, dy );
  }
  status->SetWidth(  2*offx + plot->getWidth() );
  status->SetHeight( 2*offy + plot->getHeight() );
  DBG_CHECK("PlotCanvas::setOffset() to %d %d \n", offx, offy );
}

PlotCanvas::~PlotCanvas()
{
  DBG_CHECK("PlotCanvas::dstr\n");

  if ( cur_line ) {
    endLine();
  }
  if ( plot ) delete plot;
  #ifdef HAS_BACKIMAGE
    if ( sketch ) delete sketch;
    if ( backPixmap ) delete backPixmap;
  #endif
  // ClearUndos();
  DBG_CHECK("PlotCanvas::dstr done\n");
  if ( mode == MODE_PLAN ) {
    parent->setPlanCanvas( NULL );
  } else if ( mode == MODE_EXT ) {
    parent->setExtCanvas( NULL );
  } else if ( mode == MODE_CROSS || mode == MODE_HCROSS ) {
    parent->setCrossCanvas( NULL );
  } else if ( mode == MODE_3D ) {
    parent->set3DCanvas( NULL );
  }
}

void
PlotCanvas::onGrid()
{
  double ls = units.length_factor;
  frame.switchStatus();
  frame.Update( canvas,
                status->Scale() / ls,
                (int)(status->Width() * ls),
                (int)(status->Height() * ls),
                units.length_unit );
  showGridSpacing();
  redrawPlot();
}

void
PlotCanvas::onNumbers()
{
  status->FlipNumbers();
  redrawPlot();
}


void 
PlotCanvas::onMode()
{
  DBG_CHECK("PlotCanvas::onMode()\n");

  CanvasCommandWidget * ccw = new CanvasCommandWidget( view );
  ccw->show();
  ccw->exec();
}

void 
PlotCanvas::onQuit()
{
  DBG_CHECK("PlotCanvas::onQuit()\n");

  this->hide();
  #ifndef EMBEDDED
    // delete this;
  #endif
}

#ifdef HAS_BACKIMAGE
void
PlotCanvas::onImage()
{
  DBG_CHECK("PlotCanvas::onImage \n");

  #ifdef QT_NO_FILEDIALOG
    MyFileDialogSketch * dialog = new MyFileDialogSketch( this,
                     lexicon("open_sketch"), 1 );
    dialog->show();
    dialog->exec();
  #else
    doImage( QFileDialog::getOpenFileName( "",
                     "PNG files (*.png)\nJPG files (*.jpg)\nAll (*.*)",
                     this ) );
  #endif
}

void
PlotCanvas::doImage( const QString & name )
{
  DBG_CHECK("PlotCanvas::doImage \"%s\"\n", name.latin1() );

  if ( ! name.isNull() && ! name.isEmpty() ) {
    bool ok_image = false;
    FILE * fp = fopen( name.latin1(), "r" );
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
        new BackgroundImage( NULL, this, name.latin1() );
      // image.show();
    } else {
      QMessageBox::warning(this, lexicon("qtopo_plot"),
        lexicon("image_open_failed" ) );
    }
  }
}
#endif

void
PlotCanvas::onSaveTh2()
{
  DBG_CHECK("PlotCanvas::onSaveTh2 \n");

  // [1] check if there are two stations
  int cnt = 0;
  for (std::vector< ThPoint2D >::iterator it = status->pts.begin(); 
       it != status->pts.end();
       ++it ) {
    if ( it->type == THP_STATION ) ++cnt;
  }
  ScrapWidget * sw = new ScrapWidget( this, cnt );
  sw->exec();
}

void 
PlotCanvas::doRealSaveTh2( )
{
#ifdef QT_NO_FILEDIALOG
  MyFileDialogCV * dialog = new MyFileDialogCV( this, lexicon("save_th2"), 1 );
  dialog->show();
  dialog->exec();
#else
  QString name( plot_export.getFilename() );
  doSaveTh2( 
    QFileDialog::getSaveFileName( name, "Th2 files (*.th2)\nAll (*.*)", this )
  );
#endif
}

// --------------------------------------------------
// 3D rotations

void 
PlotCanvas::onThetaPlus()
{
  if ( status->Theta() < 90.0 ) {
    status->AddTheta( 10.0 );
    plot->setThetaPhi( status->Theta(), status->Phi() );
    plot->computePlot(parent->GetList(), mode, false );
    showPlot();
  }
}

void 
PlotCanvas::onThetaMinus()
{
  if ( status->Theta() > -90.0 ) {
    status->AddTheta( -10.0 );
    plot->setThetaPhi( status->Theta(), status->Phi() );
    plot->computePlot(parent->GetList(), mode, false );
    showPlot();
  }
}

void 
PlotCanvas::onPhiPlus()
{
  status->AddPhi( 10.0 );
  plot->setThetaPhi( status->Theta(), status->Phi() );
  plot->computePlot(parent->GetList(), mode, false );
  showPlot();
}

void 
PlotCanvas::onPhiMinus()
{
  status->AddPhi( -10.0 );
  plot->setThetaPhi( status->Theta(), status->Phi() );
  plot->computePlot(parent->GetList(), mode, false );
  showPlot();
}

// --------------------------------------------------
// UNDO and INSERT

void
PlotCanvas::onUndo()
{
  if ( status->HasUndo() ) {
    CanvasUndo * cu = status->PopUndo();
    DBG_CHECK("onUndo() command %s \n", UndoCommand[cu->command] );
    // dumpUndoList( cu );
    switch ( cu->command ) {
      case UNDO_POINT:
        if ( status->pts.size() > 0 ) {
          std::vector< ThPoint2D >::iterator it = status->pts.end();
          --it;
          status->pts.erase( it );
          showPlot();
        }
        break;
      case UNDO_LINEPOINT:
        if ( status->lines.size() > 0 ) {
          assert ( on_line == true && cur_line != NULL );
          if ( cur_line->Size() > 1 ) {
            cur_line->DropLast();
            // DBG_CHECK("     current line size %d \n", cur_line->Size() );
          } else {
            std::vector< ThLine * >::iterator it = status->lines.end();
            --it;
            status->lines.erase( it );
            // DBG_CHECK("     drop line. lines %d \n", lines->size() );
            setOnLine( false );
            cur_line = NULL;
          }
          showPlot();
        }
        break;
      case UNDO_ENDLINE:
        if ( status->lines.size() > 0 ) {
          cur_line = status->lines.back();
          view->resetLineType( cur_line->type );
          setOnLine( true );
          showPlot();
        }
        break;
      case UNDO_CLOSELINE:
        if ( status->lines.size() > 0 ) {
          cur_line = status->lines.back();

          cur_line->setClosed( false );
          cur_line->DropLast();

          view->resetLineType( cur_line->type );
          setOnLine( true );
          showPlot();
        }
        break;
    }
    delete cu;
  }  
  OnOffButtons( status->HasUndo() );
}

// station points are black crosses
void
PlotCanvas::insertStationPoint( int x, int y, const char * option )
{
  DBG_CHECK("insertStationPoint X %d Y %d option %s\n", x, y, option );

  x += status->OffsetX();
  y += status->OffsetY();
  status->pts.push_back( ThPoint2D( x, y, THP_STATION, 0, option ) );
  status->AddUndo( UNDO_POINT );
  OnOffButtons( status->HasUndo() );
  // showPoint( x, y, Qt::red );
  showPlot();
  // double s = status->Scale();
  // view->updateContents( (int)(x * s)-4, (int)(y * s)-4, 9, 9 );
}

bool
PlotCanvas::hasStation( const std::string & option )
{
  for ( std::vector< ThPoint2D >::const_iterator it = status->pts.begin(), 
        end = status->pts.end(); it != end; ++it ) {
    if ( it->type == THP_STATION && it->option == option ) return true;
  }
  return false;
}

void
PlotCanvas::insertPoint( int x, int y, ThPointType type, int orient, const char * option, bool add_offset )
{
  DBG_CHECK("insertPoint X %d Y %d type %d option %s\n", x, y, type, option );

  double s = status->Scale();
  if ( add_offset ) {
    x += status->OffsetX() * PT_FACTOR;
    y += status->OffsetY() * PT_FACTOR;
  }
  double x0 = x / s;
  double y0 = y / s;
  // status->orientation = orient; // save orientation

  status->pts.push_back( ThPoint2D( x0, y0, type, orient, option ) );
  status->AddUndo( UNDO_POINT );
  OnOffButtons( status->HasUndo() );
  if ( type == THP_LABEL ) {
    showPointLabel( x0, y0, option );
    int len = (option)? strlen(option) : 4;
    view->updateContents( x-4*len, y+2*icon->Min(), 8*len, 4*icon->Size() );
  } else if ( type == THP_CONTINUATION ) {
    showPointLabel( x0, y0, "      \"?\"" ); // six spaces followed by "?"
    view->updateContents( x-4, y+2*icon->Min(), 8, 4*icon->Size() );
  } else {
    showPoint( x0, y0, type, orient );
    view->updateContents( x+icon->Min(), y+icon->Min(), icon->Size(), icon->Size() );
  }
}


void
PlotCanvas::insertLinePoint( int x, int y, ThLineType type, bool add_offset )
{
  DBG_CHECK("insertLinePoint X %d Y %d Line type %s on-line %d\n", x, y, PlotExport::ThLineName[(int)type], (int)on_line );

  double s = status->Scale();
  if ( add_offset ) {
    x += status->OffsetX() * PT_FACTOR;
    y += status->OffsetY() * PT_FACTOR;
  }
  double x0 = x/s;
  double y0 = y/s;

  // if ( cur_line == NULL ) {
  if ( on_line == false ) {
    cur_line = new ThLine( type );
    status->lines.push_back( cur_line );
    setOnLine( true );
  }
  size_t size = cur_line->Size();
  if ( size > 0 ) {
    ThPoint & pt =  cur_line->operator[](0);
    if ( fabs( pt.x*s - x ) < 4 && fabs( pt.y*s - y ) < 4 ) {
      DBG_CHECK("insertLinePoint end close line\n" );
      x0 = pt.x;
      y0 = pt.y;
      x = (int)(x0*s);
      y = (int)(y0*s);
      cur_line->Add( x0, y0 );
      showLineSegment( x, y, x0, y0, type, size );
      cur_line->setClosed( true );
      setOnLine( false );
      cur_line = NULL;
      status->AddUndo( UNDO_CLOSELINE );
      OnOffButtons( status->HasUndo() );
      return;
    } else {
      ThPoint & pt =  cur_line->operator[](size-1);
      if ( fabs( pt.x*s - x ) < 4 && fabs( pt.y*s - y ) < 4 ) {
        DBG_CHECK("insertLinePoint end open line\n" );
        endLine();
        // dumpUndoList( status->undo );
        return;
      }
    }
  } 
  DBG_CHECK("insertLinepoint() line size before insert %d\n", size);

  cur_line->Add( x0, y0 );
  status->AddUndo( UNDO_LINEPOINT );
  OnOffButtons( status->HasUndo() );
  // dumpUndoList( status->undo );
  showLineSegment( x, y, x0, y0, type, size );
}

void
PlotCanvas::showLineSegment( int x, int y, double x0, double y0,
                             ThLineType type, size_t size )
{
  DBG_CHECK("showLineSegment() %d %d %.2f %.2f\n", x, y, x0, y0 );

  if ( cur_line == NULL ) return;
  double s = status->Scale();

  // end-point
  if ( type == THL_WALL ) {
    showPoint( x0, y0, Qt::blue );
  } else if ( type == THL_ARROW ) {
    if ( size == 0 ) {
      showPoint( x0, y0, Qt::black );
    } else {
      int k = 0;
      ThPoint & prev = cur_line->operator[]( size - 1 );
      double dx = x0 - prev.x;
      double dy = y0 - prev.y;
      if ( fabs(dx) > 2*fabs(dy) ) {
        k = (dx > 0)? 2 : 6;
      } else if ( fabs(dy) > 2*fabs(dx) ) { 
        k = (dy > 0)? 4 : 0;
      } else if ( dx > 0 ) {
        k = (dy > 0)? 3 : 1;
      } else {
        k = (dy > 0)? 5 : 7;
      }
      showPoint( x0, y0, icon->ArrowEnd(k) );
    }
  } else if ( type == THL_ROCK ) {
    showPoint( x0, y0, Qt::gray );
  } else {
    showPoint( x0, y0, Qt::magenta );
  }

  // line-segment
  int x2 = x;
  int y2 = y;
  if ( size > 0 ) {
    ThPoint & prev = cur_line->operator[]( size - 1 );
    QColor color;
    QPen pen;
    selectLineStyle( type, pen, color );
    showLine( prev.x, prev.y, x0, y0, pen );
    if ( type == THL_PIT || type == THL_CHIMNEY ) {
      double dx = y0 - prev.y;
      double dy = x0 - prev.x;
      double d = ( dx*dx + dy*dy);
      if ( d > 0.0 ) {
        d = sqrt( d );
        if ( type == THL_CHIMNEY ) {
          d = -d; // ticks point into the cave
          pen = icon->PenViolet();
        }
        dx /= d;
        dy /= d;
        showLine( x0, y0, x0+(int)(PIT_TICK*dx), y0-(int)(PIT_TICK*dy), pen );
      }
    }
    if ( prev.x * s < x ) { x = (int)(prev.x * s); }
    else { x2 = (int)(prev.x * s); }
    if ( prev.y * s < y ) { y = (int)(prev.y * s); }
    else { y2 = (int)(prev.y * s); }
  }
  view->updateContents( x-10, y-10, 21+x2-x, 21+y2-y  );
}

// ----------------------------------------------------
// ZOOM in/out, SPLAY shots, and SAVE

void 
PlotCanvas::onZoomIn()
{
  double s = status->Scale() * ZOOM_STEP;
  // if ( s > ZOOM_MAX ) return;
  double x, y;
  getCanvasScrollPosition( x, y );
  status->SetScale( s );
  DBG_CHECK("PlotCanvas::onZoomIn %.2f Pos %.2f %.2f ", s, x, y );
  #ifdef HAS_BACKIMAGE
    setBackground();
  #endif
  showPlot( );
  setCanvasScrollPosition( x, y );
}

void
PlotCanvas::onZoomOut()
{
  double s = status->Scale() / ZOOM_STEP;
  if ( s < ZOOM_MIN ) return;
  double x, y;
  getCanvasScrollPosition( x, y );
  status->SetScale( s );
  DBG_CHECK("PlotCanvas::onZoomOut %.2f Pos. %.2f %.2f ", s, x, y );
  #ifdef HAS_BACKIMAGE
    setBackground();
  #endif
  showPlot( );
  setCanvasScrollPosition( x, y );
}

void 
PlotCanvas::getCanvasScrollPosition( double & x, double & y )
{
  x = (double)(view->contentsX() + view->visibleWidth()/2)/(double)(view->contentsWidth());
  y = (double)(view->contentsY() + view->visibleHeight()/2)/(double)(view->contentsHeight());
  // DBG_CHECK("Get Pos. X %d %.2f %d (%d) Y %d %.2f %d (%d)\n",
  //     view->contentsX(), x, view->contentsWidth(), view->visibleWidth(),
  //     view->contentsY(), y, view->contentsHeight(), view->visibleHeight() );
}

void 
PlotCanvas::setCanvasScrollPosition( double x, double y )
{
  if ( x > 1.0 ) x = 1.0;
  int x1 = (int)(x * view->contentsWidth() - view->visibleWidth()/2);
  if ( x1 < 0 ) x1 = 0;
  if ( y > 1.0 ) y = 1.0;
  int y1 = (int)(y * view->contentsHeight() - view->visibleHeight()/2);
  if ( y1 < 0 ) y1 = 0;
  // DBG_CHECK("Set Pos. X %d %.2f Y %d %.2f\n", x1, x, y1, y );
  view->setContentsPos( x1, y1 );
  /*
  view->center( status->width / 2, status->height / 2 );
  */
}

void 
PlotCanvas::update()
{
  // DBG_CHECK("PlotCanvas::update() \n");
  #ifdef HAS_BACKIMAGE
    setBackground();
  #endif
  showPlot( );
}

void
PlotCanvas::onSplay()
{
  // DBG_CHECK("onSplay %d \n", do_splay );
  do_splay = ! do_splay;
  showPlot( );
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
  plot_export.setFilename( file );

  // char name[32];
  // name[0] = scrap_prefix[mode];
  // sprintf(name+1, "-%03d", scrap_nr );
  plot_export.exportTherion( mode_string[mode], status, parent->GetList() );
}

#ifdef HAS_BACKIMAGE
// ------------------------------------------------
// BACKGROUND

void 
PlotCanvas::evalBackground( std::vector< BackgroundImageStation > & stations,
                            QPixmap * back_pixmap )
{
  ARG_CHECK( back_pixmap == NULL, );
  DBG_CHECK("PlotCanvas::evalBackground() stations %d \n", stations.size() );

  if ( stations.size() < 2 ) { // caller should invoke this only 
    QMessageBox::warning(this, lexicon("qtopo_plot"), lexicon("one_station") );
    return;                    // when stations are at least 2
  }

  backStations.clear();
  for ( std::vector< BackgroundImageStation >::iterator it = stations.begin();
        it != stations.end();
        ++it ) {
    CanvasPoint * pt = plot->getPoints();
    while ( pt && ( pt->name != it->name ) ) pt=pt->next;
    if ( pt ) {
      backStations.push_back( BackgroundImagePoint( pt, *it ) );
      // it->x0 = 4 * (status->offx + pt->x0);
      // it->y0 = 4 * (status->offy + pt->y0);
    }
  }
  if ( backStations.size() < 2 ) {
    QMessageBox::warning(this, lexicon("qtopo_plot"), lexicon("one_station") );
    return;
  }

  if ( backPixmap ) {
    delete backPixmap;
  }
  backPixmap = back_pixmap;
  setBackgroundInit();
}

void
PlotCanvas::setBackgroundInit()
{
  if ( backPixmap == NULL ) 
    return;

  int w1 = (int)(width() * status->Scale() );
  int h1 = (int)(height() * status->Scale() );
  int w2 = backPixmap->width();
  int h2 = backPixmap->height();
  DBG_CHECK("make background %d %d for pixmap %d %d \n", w1, h1, w2, h2 );
  QImage src_image = backPixmap->convertToImage();
  const unsigned char * src = src_image.bits();
  if ( sketch == NULL || w1 != sketch->width() || h1 != sketch->height() ) {
    if ( sketch ) delete sketch;
    sketch = new QImage( w1, h1, 32 );
  }
  unsigned char * dst = sketch->bits();
  memset( dst, 0xff, w1*h1*4 ); // make background white

#ifdef WARP_PLAQUETTE
  therion::warp::morph_type morph_type = therion::warp::THMORPH_STATION;
  therion::warp::plaquette_algo algo;
#endif

  for ( std::vector< BackgroundImagePoint >::iterator it=backStations.begin();
        it != backStations.end();
        ++it ) {
    it->x0 = 4 * (status->OffsetX() + it->point->x0);
    it->y0 = 4 * (status->OffsetY() + it->point->y0);
#ifdef WARP_PLAQUETTE
    std::string name( it->point->name );
    algo.insert_point( morph_type, name, thvec2(it->x,it->y), thvec2(it->x0,it->y0) );
#endif
  }

#ifdef WARP_PLAQUETTE
  for ( std::vector< BackgroundImagePoint >::iterator it=backStations.begin();
        it != backStations.end();
        ++it ) {
    std::string name( it->point->name );
    std::vector< BackgroundImagePoint >::iterator it2 = it;
    for ( ++it2; it2 != backStations.end(); ++it2 ) {
      std::string name2( it2->point->name );
      if ( parent->GetList()->hasBlock( it->point->name, it2->point->name ) ) {
        algo.insert_line( morph_type, name, name2 );
        break;
      } 
    }
  }

  algo.initialize( therion::warp::THWARP_PLAN );
  thvec2 origin( w1/2.0, h1/2.0 );
  double unit = algo.to_unit() * 1.0;
  algo.map_image( src, w2, h2, dst, w1, h1, origin, unit, 4, therion::warp::THWARP_PLAN );
  if ( true ) {
#else
  if ( imageWarp( backStations, dst, w1, h1, src, w2, h2 ) ) {
#endif
    QImage img2 = sketch->scaleWidth( (int)(sketch->width() * (status->Scale())/4) );
    sketch_pix.convertFromImage( img2 );
    canvas->setBackgroundPixmap( sketch_pix );
  } else {
    // TODO tell the user that warping failed
    delete sketch;
    sketch = NULL; 
  }
}

void
PlotCanvas::setBackground()
{
  if ( sketch ) {
    QImage img2 = sketch->scaleWidth( (int)(sketch->width() * (status->Scale())/4) );
    sketch_pix.convertFromImage( img2 );
    canvas->setBackgroundPixmap( sketch_pix );
  }
}

#endif // HAS_BACKIMAGE

// ------------------------------------------------
// DISPLAY and CLEAR

void
PlotCanvas::selectLineStyle( ThLineType t, QPen & pen, QColor & color )
{
  color = Qt::magenta;
  pen = icon->PenViolet();
  switch ( t ) {
    case THL_ARROW:
      color = Qt::black;
      pen = icon->PenBlack();
      break;
    case THL_BORDER:
      // pen violet
      break;
    case THL_CHIMNEY:
      pen = icon->DashViolet(); // with ticks
      break;
    case THL_PIT:
      // pen violet with ticks
      break;
    case THL_USER:
      color = Qt::green;
      pen = icon->PenGreen();
      break;
    case THL_WALL:
      color = Qt::blue;
      pen = icon->PenBlue();
      break;
    case THL_ROCK:
      color = Qt::gray;
      pen = icon->DarkGray();
    case THL_PLACEMARK:
      /* nothing */
      break;
  }
}

void
PlotCanvas::showPlot( )
{
  DBG_CHECK("showPlot() mode %d theta %.2f phi %.2f\n", mode, status->theta, status->phi );

  double s = status->Scale();
  int ox = status->OffsetX();
  int oy = status->OffsetY();
  
  // int w2 = (int)((plot->x0max + ox*2)*s) + BORDER;
  // int h2 = (int)((plot->y0max + oy*2)*s) + BORDER;
  // status->width  = w2;
  // status->height = h2;
  int w2 = (int)(s * status->Width() ) + BORDER;
  int h2 = (int)(s * status->Height() ) + BORDER;
  canvas->resize( w2, h2 );

  clear();
  if ( mode != MODE_3D ) {
    double ls = units.length_factor;
    frame.Update( canvas,
                  s/ls,
                  (int)(status->Width() * ls),
                  (int)(status->Height() * ls),
                  units.length_unit );
  }
  showGridSpacing();

  for ( CanvasSegment * cs = plot->getSegments(); cs; cs=cs->next ) {
    if ( ! do_splay && cs->cs_type != CS_CENTERLINE ) continue;
    QCANVASLINE * line = new QCANVASLINE( canvas );
    line->setPoints( (int)((cs->x0 + ox)*s), (int)((cs->y0 + oy)*s),
                     (int)((cs->x1 + ox)*s), (int)((cs->y1 + oy)*s) );
    if ( mode == MODE_3D && cs->cs_type == CS_CENTERLINE ) {
      QPen p( icon->PenRed() );
      p.setWidth( (cs->z0 + cs->z1)/30 );
      line->setPen( p );
      line->setZ( 128 );
      line->show();
    } else {
      QPen p( (cs->cs_type == CS_CENTERLINE )? icon->PenRed() : icon->DarkGray()  );
      if ( cs->block->hasComment() ) { // lines with comments are thick
        // p = (cs->cs_type == CS_CENTERLINE )? icon->DarkRed() : icon->PenBlack();
        p.setWidth( 2 );
      }
      line->setPen( p );
      line->setZ( 128 );
      line->show();
    }
    line_items.push_back( std::pair<QCANVASLINE *, CanvasSegment*>(line, cs) );
  }
  
  if ( status->IsNumbers() ) {
    for ( CanvasPoint * pt = plot->getPoints(); pt; pt=pt->next ) {
      QCANVASTEXT * text = new QCANVASTEXT( QString(pt->name), canvas );
      text->setColor( parent->GetList()->hasStationComment(pt->name)? Qt::blue : Qt::darkCyan );
      // text->setColor( parent->GetList()->hasStationComment(pt->name)? Qt::darkBlue : Qt::blue );
      text->setX( (int)((pt->x0 + ox)*s) );
      text->setY( (int)((pt->y0 + oy)*s) );
      text->setZ( 255 );
      text->show();
      text_items.push_back( std::pair<QCANVASTEXT*, CanvasPoint*>(text,pt) );
    }
  }
  if ( mode != MODE_3D ) {
    for ( std::vector< ThLine * >::iterator lit = status->lines.begin(), lend = status->lines.end();
          lit != lend;
          ++lit ) {
      ThLine::iterator it = (*lit)->Begin();
      ThLine::iterator end = (*lit)->End();
      QColor color;
      QPen pen;
      selectLineStyle( (*lit)->type, pen, color );
      double x = it->x;
      double y = it->y;
      showPoint( it->x, it->y, color );
      for ( ++it; it != end; ++it ) {
        if ( (*lit)->type == THL_ARROW ) {
          int k = 0;
          double dx = it->x - x;
          double dy = it->y - y;
          if ( fabs(dx) > 2*fabs(dy) ) {
            k = (dx > 0)? 2 : 6;
          } else if ( fabs(dy) > 2*fabs(dx) ) { 
            k = (dy > 0)? 4 : 0;
          } else if ( dx > 0 ) {
            k = (dy > 0)? 3 : 1;
          } else {
            k = (dy > 0)? 5 : 7;
          }
          showPoint( it->x, it->y, icon->ArrowEnd(k) );
        } else {
          showPoint( it->x, it->y, color );
        }
        showLine( x, y, it->x, it->y, pen );
        if ( (*lit)->type == THL_PIT || (*lit)->type == THL_CHIMNEY ) {
          double dx = it->y - y;
          double dy = it->x - x; 
          double d = ( dx*dx + dy*dy);
          if ( d > 0.0 ) {
            d = sqrt( d );
            if ( (*lit)->type == THL_CHIMNEY ) {
              d = -d; // ticks point into the cave
              pen = icon->PenViolet();
            }
            dx /= d;
            dy /= d;
            showLine( it->x, it->y, it->x+(int)(PIT_TICK*dx), it->y-(int)(PIT_TICK*dy), pen );
          }
        }
        x = it->x;
        y = it->y;
      }
    }
    for ( std::vector< ThPoint2D >::iterator it = status->pts.begin(), 
          end = status->pts.end(); it != end; ++it ) {
      if ( it->type == THP_LABEL ) {
        showPointLabel( it->x, it->y, it->option.c_str() );
      } else if ( it->type == THP_CONTINUATION ) {
        showPointLabel( it->x, it->y, "?" ); // six spaces followed by '?'
      } else {
        showPoint( it->x, it->y, it->type, it->orientation );
      }
    }
  }
  view->updateContents( view->contentsX(), view->contentsY(),
                        view->contentsWidth(), view->contentsHeight() );
  view->show();
}

void
PlotCanvas::showLine( double x1, double y1, double x2, double y2, const QPen & pen )
{
  double s = status->Scale();
  // DBG_CHECK("showLine %.2f %.2f - %.2f %.2f\n", x1, y1, x2, y2 );
  QCANVASLINE * line = new QCANVASLINE( canvas );
  line->setPoints( (int)(x1*s), (int)(y1*s), (int)(x2*s), (int)(y2*s) );
  line->setPen( pen );
  line->setZ( 128 );
  line->show();
  line_items.push_back( std::pair<QCANVASLINE *, CanvasSegment*>(line, NULL) );
}

void 
PlotCanvas::showPoint( double x, double y, const QColor & color )
{
  double s = status->Scale();
  // DBG_CHECK("showPoint %.2f %.2f\n", x, y );
  QCANVASELLIPSE * ell = new QCANVASELLIPSE( 4, 4, canvas );
  // ell->setPen( icon->PenBlue() );
  ell->setBrush( QBrush( color ) );
  ell->setX( (int)( x * s ) );
  ell->setY( (int)( y * s ) );
  ell->setZ( 255 );
  ell->show();
  point_items.push_back( ell );
}

void
PlotCanvas::showPoint( double x, double y, const QPOINTARRAY & points )
{
  double s = status->Scale();
  QCANVASPOLYGON * poly = new QCANVASPOLYGON( canvas );
  poly->setPoints( points );
  poly->setBrush( QBrush( Qt::black ) );
  poly->setX( (int)( x * s ) );
  poly->setY( (int)( y * s ) );
  poly->setZ( 255 );
  poly->show();
  point_items.push_back( poly );
}

void
PlotCanvas::showPoint( double x, double y, ThPointType type, int orient )
{
  double s = status->Scale();
  // DBG_CHECK("showPoint x %.2f y %.2f type %d orient %d \n", x, y, type, orient );
  QCANVASPOLYGON * poly = new QCANVASPOLYGON( canvas );
  if ( type == THP_WATER || type == THP_AIR ) {
    poly->setPoints( icon->Arrow(orient) );
  } else if ( type == THP_ENTRANCE ) {
    poly->setPoints( icon->FatArrow(orient) );
  } else {
    poly->setPoints( icon->Symbol(type) );
  }
  poly->setBrush( icon->Brush(type) );
  poly->setX( (int)( x * s ) );
  poly->setY( (int)( y * s ) );
  poly->setZ( 255 );
  poly->show();
  point_items.push_back( poly );
}

void
PlotCanvas::showPointLabel( double x, double y, const char * option )
{
  double s = status->Scale();
  QCANVASTEXT * text = new QCANVASTEXT( canvas );
  QString t("ABC");
  if ( option ) {
    const char * ch1 = strchr( option, '"' );
    const char * ch2 = strrchr( option, '"' );
    if ( ch1 && ch2 && ch1 != ch2 ) {
      t = ch1+1;
      t.truncate( ch2 - ch1 - 1 );
    }
  }
  text->setText( t );
  text->setX( (int)( x * s ) );
  text->setY( (int)( y * s ) );
  text->setColor( Qt::black );
  text->show();
  point_items.push_back( text );
  // view->updateContents( (int)(x * s)-30, (int)(y * s)-10, 60, 20 );
}
  

void
PlotCanvas::clear()
{
  // DBG_CHECK("PlotCanvas::clear() \n");
  std::vector< std::pair<QCANVASLINE *,CanvasSegment*> >::iterator end = line_items.end();
  for ( std::vector< std::pair<QCANVASLINE *,CanvasSegment*> >::iterator it = line_items.begin();
        it != end; ++it ) {
    it->first->hide();
    canvas->removeItem( it->first );
    delete (it->first);
  }
  line_items.clear();

  std::vector< std::pair<QCANVASTEXT *, CanvasPoint*> >::iterator end2 = text_items.end();
  for ( std::vector< std::pair<QCANVASTEXT *, CanvasPoint*> >::iterator it = text_items.begin();
        it != end2; ++it ) {
    it->first->hide();
    canvas->removeItem( it->first );
    delete (it->first);
  }
  text_items.clear();

  std::vector< QCANVASITEM * >::iterator end3 = point_items.end();
  for ( std::vector< QCANVASITEM * >::iterator it = point_items.begin();
        it != end3; ++it ) {
    (*it)->hide();
    canvas->removeItem( *it );
    delete (*it);
  }
  point_items.clear();

}

// ----------------------------------------------------------------------
// CANVAS COMMAND WIDGET

CanvasCommandWidget::CanvasCommandWidget( PlotCanvasView * my_parent )
  : QDialog( my_parent, "CanvasCommandWidget", true ) 
  , view( my_parent )
  , np_type( (int)THP_PLACEMARK )
  , nl_type( (int)THL_PLACEMARK )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("canvas_mode") );
  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  // comment = new QLineEdit( b->getComment(), this );

  QBUTTONGROUP * m_group = new QBUTTONGROUP( );
  QHBOX * hb = new QHBOX(this);
  mode[0] = new QRadioButton( lexicon("select"), hb);
  m_group->insert( mode[0] );

  hb = new QHBOX(this);
  mode[1] = new QRadioButton( lexicon("point"), hb);
  m_group->insert( mode[1] );
  ptsBox = new QComboBox( hb );
  connect( ptsBox, SIGNAL(activated(int)), this, SLOT(doPoint(int)) );
  for (int i=0; i<np_type-1; ++i) { // -1: do not include station
    ptsBox->insertItem( PlotThExport::ThPointName[i] );
  }
  ptsBox->setCurrentItem( (int)view->getPointType() );

  hb = new QHBOX(this);
  mode[2] = new QRadioButton( lexicon("line"), hb);
  m_group->insert( mode[2] );
  lnsBox = new QComboBox( hb );
  connect( lnsBox, SIGNAL(activated(int)), this, SLOT(doLine(int)) );
  for (int i=0; i<nl_type; ++i) {
    lnsBox->insertItem( PlotThExport::ThLineName[i] );
  }
  lnsBox->setCurrentItem( (int)view->getLineType() );

  hb = new QHBOX(this);
  new QLabel( lexicon("orientation"), hb );
  porient = new QDial( hb );
  porient->setMinValue( 0 );
  porient->setMaxValue( 360 );
  porient->setWrapping( true );
  porient->setNotchesVisible( true );
  porient->setNotchTarget( 45 );
  // DBG_CHECK("orientation %d \n", view->getPointOrient() * 45 - 180 );
  porient->setValue( view->getPointOrient() * 45 - 180 );

  switch ( view->getInputMode() ) {
    case INPUT_COMMAND:
      mode[0]->setChecked( TRUE );
      break;
    case INPUT_POINT:
      mode[1]->setChecked( TRUE );
      break;
    case INPUT_LINE:
      mode[2]->setChecked( TRUE );
      break;
  }
  
  QPushButton * c;
  hb = new QHBOX(this);
  // c = new QPushButton( tr( lexicon("undo") ), hb );
  // connect( c, SIGNAL(clicked()), this, SLOT(doUndo()) );
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
}

// void
// CanvasCommandWidget::doUndo()
// {
//   view->onUndo();
//   delete this;
// }

void
CanvasCommandWidget::doPoint(int 
#ifdef CHECK_DBG
  i
#endif
)
{
  DBG_CHECK("CanvasCommandWidget::doPoint() %d\n", i);
  mode[1]->setChecked( TRUE );
}

void
CanvasCommandWidget::doLine(int 
#ifdef CHECK_DBG
  i
#endif
)
{
  DBG_CHECK("CanvasCommandWidget::doLine() %d\n", i);
  mode[2]->setChecked( TRUE );
}
  

void
CanvasCommandWidget::doOK()
{
  if ( mode[0]->isChecked() ) {
    view->setInputMode( INPUT_COMMAND );
  } else if ( mode[1]->isChecked() ) {
    view->setInputMode( INPUT_POINT );
    view->setPointType( ptsBox->currentItem() ); // currentIndex()
    int angle = ( ( porient->value() + 180 + 22) / 45 ) % 8;
    view->setPointOrient( angle );
  } else if ( mode[2]->isChecked() ) {
    view->setInputMode( INPUT_LINE );
    view->setLineType( lnsBox->currentItem(), mode[2]->isChecked() );
  }  

  delete this;
}

void
CanvasCommandWidget::doCancel()
{
  delete this;
}

// ------------------------------------------------------
// CLEAN-SCRAP WIDGET
// clear the drawing

CleanScrapWidget::CleanScrapWidget( PlotCanvas * p )
  : QDialog( p, "CleanScrapWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_clean_scrap") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  new QLabel( lexicon("clean_scrap"), this );

  QHBOX *hb;
  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
}


// ==============================================================
// SCRAP WIDGET
// get the scrap name on save (export as therion file)

ScrapWidget::ScrapWidget( PlotCanvas * p, int cnt )
  : QDialog( p, "ScrapWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_scrap") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  new QLabel( lexicon("scrap_name"), this );
  name = new QLineEdit( parent->getScrapName(), this );

  if ( cnt < 2 ) {
    new QLabel( lexicon("warn_scrap"), this );
  }
 
  QHBOX *hb;
  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
}

void
ScrapWidget::doOK()
{
  hide();
  if ( ! name->text().isEmpty() ) {
    parent->setScrapName( name->text().latin1() );
  }
  parent->doRealSaveTh2();
  delete this;
}

// --------------------------------------------------
// LABEL WIDGET
// "label" and "continuation" points text

LabelWidget::LabelWidget( PlotCanvasView * p, const char * caption )
  : QDialog( p, "LabelWidget", true )
  , parent( p )
{
  setCaption( caption );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  text = new QLineEdit( "", this );
  connect( text, SIGNAL(returnPressed()), this, SLOT(doOK()) );
  // connect( text, SIGNAL(editingFinished()), this, SLOT(doOK()) );
}
  

// ============================================================
// STATION COMMENT WIDGET
// edit the comment to a station point

StationCommentWidget::StationCommentWidget( QWidget * p, PlotCanvas * pc, const char * n )
  : QDialog( p, "StationCommentWidget", true )
  , plot_canvas( pc )
  , name( n )
{
  DBG_CHECK("StationCommentWidget list %p point %s", (void *)plot_canvas->GetList(), name );

  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_station_comment" ) );
  
  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  std::ostringstream oss;
  oss << lexicon("station") << " " << name;
  new QLabel( oss.str().c_str(), this );
  if ( plot_canvas->GetList()->hasStationComment( name ) ) {
    comment = new QLineEdit( plot_canvas->GetList()->getStationComment( name ), this );
  } else {
    comment = new QLineEdit( "", this );
  }

  QPushButton * c1;
  QHBOX * hb = new QHBOX(this);
  c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  c1 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c1, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
StationCommentWidget::doOK()
{
  // if ( ! comment->text().isEmpty() ) // allow to erase comment
  {
    plot_canvas->GetList()->setStationComment( name, comment->text().latin1() );
    plot_canvas->propagateUpdate();
  }
  delete this;
}

  
  
// ============================================================
// EXTEND WIDGET
// shot extend flag widget

ExtendWidget::ExtendWidget( QWidget * p, PlotCanvas * pc, DBlock * b )
  : QDialog( p, "ExtendWidget", true )
  , plot_canvas( pc )
  , block( b )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_extend") );
  
  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  if ( b->hasFrom() ) {
    std::ostringstream oss;
    oss << lexicon("from") << " " << b->From();
    if ( b->hasTo() )
      oss << " " << lexicon("to") << " " << b->To();
    new QLabel( oss.str().c_str(), this );
  } else if ( b->hasTo() ) {
    std::ostringstream oss;
      oss << lexicon("to") << " " << b->To();
    new QLabel( oss.str().c_str(), this );
  }

  comment = new QLineEdit( block->Comment(), this );

  if ( plot_canvas->getMode() == MODE_EXT ) {
    QVBOX * vb = new QVBOX( this );
    extBox[EXTEND_NONE]   = new QRadioButton( lexicon("none"), vb );
    extBox[EXTEND_LEFT]   = new QRadioButton( lexicon("left"), vb );
    extBox[EXTEND_RIGHT]  = new QRadioButton( lexicon("right"), vb );
    extBox[EXTEND_VERT]   = new QRadioButton( lexicon("vertical"), vb );
    extBox[EXTEND_IGNORE] = new QRadioButton( lexicon("ignore"), vb );
    QBUTTONGROUP * m_group = new QBUTTONGROUP( );
    for (int i=0; i<EXTEND_MAX; ++i ) {
      m_group->insert( extBox[i] );
    }
    extBox[ block->Extend() ]->setChecked( true );
  }
  
  QPushButton * c1;
  QHBOX * hb = new QHBOX(this);
  c1 = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  c1 = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c1, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
ExtendWidget::doOK()
{
  hide();
  block->setComment( comment->text().latin1() );
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
  // plot_canvas->redrawPlot();
  // delete this;
}

void 
ExtendWidget::doCancel()
{
  delete this;
}


#ifdef HAS_BACKIMAGE
MyFileDialogSketch::MyFileDialogSketch( PlotCanvas * parent,
                                        const char * title, 
                                        int m )
      : QDialog( parent, title, TRUE )
      , widget( parent )
      , mode( m )
{
  Language & lexicon = Language::Get();
  // DBG_CKECK("MyFileDialogSketch \n");
  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  QHBOX * hb = new QHBOX(this);
  new QLabel(lexicon("enter_filename"), hb );
  hb = new QHBOX(this);
  line = new QLineEdit( hb );
  hb = new QHBOX(this);
  QPushButton * c = new QPushButton( tr(lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr(lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
}
#endif
