/** @file BackgroundImage.cpp
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief background image
 *
// FIXME there must be an undo, 
//       a way to cancel added stations
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifdef HAS_BACKIMAGE

#include <qapplication.h>

// #include <q3vboxlayout.h>
#include <QLabel>
#include <QCursor>
#include <QPushButton>
#include <QCheckBox>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QImage>
#include <QToolBar>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "ArgCheck.h"

#include "Language.h"
#include "config.h"
#include "IconSet.h"
#include "TherionPoint.h"
#include "BackgroundImage.h"

/** Default Y offset
 * The Y offset is fixed at runtime later
 */
#define OFFSET_Y 30

#include <QGraphicsSceneMouseEvent>


BackgroundImageScene::BackgroundImageScene( BackgroundImage * my_image )
      : image( my_image )
      , pixmap( NULL )
      , orig_pix( NULL )
      , do_add_station( false )
{ 
}

BackgroundImageScene::~BackgroundImageScene()
{
}

void
BackgroundImageScene::loadImage( const char * name )
{
  ARG_CHECK( name == NULL, );

  if ( orig_pix != NULL ) delete orig_pix;
  orig_pix = new QPixmap( name );
  {
    DBG_CHECK("BackgroundImageShow::LoadImage %s %d x %d\n", 
      name, orig_pix->width(), orig_pix->height() ); 
    if ( pixmap ) {
      // ig_pixmap -> removeFromGroup( pixmap );
      this -> removeItem( pixmap );
      delete pixmap;
    }
    pixmap = new QGraphicsPixmapItem( *orig_pix );
    if ( pixmap ) {
      pixmap->setVisible( true );
      pixmap->setZValue( 128 );
      pixmap->show();
      // ig_pixmap -> addToGroup( pixmap );
      this -> addItem( pixmap );
    } else {
      printf("BackgroundImageScene failed to add pixmap \n");
    }
  }
}

// MOUSE EVENTS

void 
BackgroundImageScene::mouseReleaseEvent( QGraphicsSceneMouseEvent * e0 )
{
  QPointF p = e0->scenePos();
  // fprintf(stderr, "mouseReleaseEvent %.2f %.2f \n", p.x(), p.y() );
}

void 
BackgroundImageScene::mousePressEvent( QGraphicsSceneMouseEvent * e0 )
{
  QPointF p = e0->scenePos();
  double x = p.x();
  double y = p.y();
  // fprintf(stderr, "mousePressEvent %.2f %.2f \n", p.x(), p.y() );

  // FIXME use list of graphics item
  BackgroundImageStation * st = stations.getStationAt( x, y );
  if ( st == NULL ) {
    // TODO show the cross
    // DBG_CHECK("station at %d %d\n", x, y );
    do_add_station = false;
    QGraphicsEllipseItem * point = addEllipse( x-2, y-2, 5, 5, IconSet::Get()->PenRed() );
    point -> setZValue( 255 );
    point -> setVisible( true );
    BackgroundImageStationDialog( image, this );
    if ( do_add_station ) {
      printf("addStation %s at %.2f %.2f \n", station_name.TO_CHAR(), x, y );
      stations.addStation( station_name, x, y, point );
    } else {
      // printf("do not addStation\n");
      delete point;
    }
  } else {
    do_remove_station = false;
    BackgroundImageEditStationDialog( image, this, st );
    if ( do_remove_station ) {
      printf("removeStation %s \n", st->name.TO_CHAR() );
      this -> removeItem( st->point );
      stations.removeStation( st );
      // FIXME check if need to call dstr
    }
  }
}


// ----------------------------------------------------------------------

void
BackgroundImage::createToolBar()
{
  QToolBar * toolbar = addToolBar( tr("") );
  setToolButtonStyle( Qt::ToolButtonIconOnly ); // this is the default

  toolbar->addAction( actOk );
  toolbar->addAction( actZoomIn );
  toolbar->addAction( actZoomOut );
  toolbar->addAction( actQuit );
}

void
BackgroundImage::createActions()
{
  IconSet * icon = IconSet::Get();
  actOk       = new QAction( icon->Ok(), lexicon("insert"), this); // doSave
  actZoomIn   = new QAction( icon->ZoomIn(), lexicon("zoom_in"), this );
  actZoomOut  = new QAction( icon->ZoomOut(), lexicon("zoom_out"), this );
  actQuit     = new QAction( icon->Close(), lexicon("exit"), this); // doQuit
  connect( actOk,      SIGNAL(triggered()),  this, SLOT(doSave()) );
  connect( actZoomIn, SIGNAL(triggered()), this, SLOT(onZoomIn()) );
  connect( actZoomOut, SIGNAL(triggered()), this, SLOT(onZoomOut()) );
  connect( actQuit,    SIGNAL(triggered()),  this, SLOT(doQuit()) );
  actOk->setVisible( true );
  actZoomIn->setVisible( true );
  actZoomOut->setVisible( true );
  actQuit->setVisible( true );
}

BackgroundImage::BackgroundImage( QWidget * my_parent,
                                  BackgroundImageCallback * cb, const char *name )
  : QMainWindow( my_parent )
  , parent( my_parent )
  , lexicon( Language::Get() )
  , offset_y( OFFSET_Y )
  , callback( cb )
{
  printf("BackgroundImage() image <<%s>>\n", name );
  setWindowTitle( lexicon("qtopo_sketch") );
  setCursor( QCursor(Qt::CrossCursor) );
  resize( 640, 480 );

/*
  QVBoxLayout* vb = new QVBoxLayout(this);
  // vb->setAutoAdd(TRUE);
  vb->setSpacing( 0 );
  vb->setMargin( 0 );
*/

  createActions();
  createToolBar();

  // QVBOX * vbox = new QVBOX( this );
  scene = new BackgroundImageScene( this );
  if ( name ) {
    scene->loadImage( name );
  }

  view  = new BackgroundImageView( this, scene );
  this->setCentralWidget( view );
  this->show();

  // QPoint p = scene->pos();
  // offset_y = p.y();
}

BackgroundImage::~BackgroundImage()
{
  // DBG_CHECK("BackgroundImage dstr \n");
  if ( scene ) delete scene; // FIXME ok ?
}

BackgroundImageView::BackgroundImageView( BackgroundImage * my_parent,
                                          BackgroundImageScene * my_scene )
  : QGraphicsView( my_scene, my_parent )
  , image( my_parent )
  , scene( my_scene )
{ }

void
BackgroundImage::doSave()
{
  const std::vector< BackgroundImageStation * > & stations = scene->getStations();
  DBG_CHECK("BackgroundImage::doSave() stations %d \n", stations.size() );
  printf("BackgroundImage::doSave() stations %d \n", stations.size() );
  callback->evalBackground( stations, scene->getImage() );
  this->close();
}

void
BackgroundImage::doQuit()
{
  // DBG_CHECK("BackgroundImage::doQuit() \n");
  this->close();
}
  
// ---------------------------------------------------------------------

BackgroundImageStationDialog::BackgroundImageStationDialog( BackgroundImage * my_parent,
                                                            BackgroundImageScene * my_scene )
  : QDialog( my_parent )
  , scene( my_scene )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_station") );
  QVBoxLayout* vbl = new QVBoxLayout(this);

  QWidget * hb = new QWidget( this );
  QHBoxLayout * hbl = new QHBoxLayout( hb );
  hbl->addWidget( new QLabel( lexicon("station"), hb ) );
  station = new QLineEdit( "", hb );
  hbl->addWidget( station );
  vbl->addWidget( hb );

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );
  show();
  exec();
}

void
BackgroundImageStationDialog::doOK()
{
  hide();
  QString name( station->text() );
  scene->setStationName( name );
  // delete this;
}

BackgroundImageEditStationDialog::BackgroundImageEditStationDialog(
    BackgroundImage * my_parent, 
    BackgroundImageScene * my_scene,
    BackgroundImageStation * st )
  : QDialog( my_parent )
  , scene( my_scene )
  , station( st )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_edit_station") );
  QVBoxLayout* vbl = new QVBoxLayout(this);

  QWidget * hb = new QWidget( this );
  QHBoxLayout * hbl = new QHBoxLayout( hb );
  hbl->addWidget( new QLabel( lexicon("station"), hb ) );
  st_name = new QLineEdit( st->name.TO_CHAR(), hb );
  hbl->addWidget( st_name );
  vbl->addWidget( hb ) ;

  remove = new QCheckBox( lexicon("remove_station"), hb );
  vbl->addWidget( remove );

  hb = new QWidget(this);
  hbl = new QHBoxLayout( hb );
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  show();
  exec();
}

void
BackgroundImageEditStationDialog::doOK()
{
  hide();
  if ( remove->isChecked() ) {
    scene->setRemoveStation( true );
  } else if ( station->name != st_name->text() ) {
    scene->changeStation( station, st_name->text() );
  }
}


#ifdef TEST

int main( int argc, char **argv )
{
    QApplication a( argc, argv );

    BackgroundImage * img = new BackgroundImage( 0, argv[1] );

    a.setMainWidget( img );
    img->setWindowTitle("Qt Example - Image");
    img->show();
    return a.exec();
}

#endif // TEST

#endif // HAS_BACKIMAGE

/*
void BackgroundImageShow::paintEvent( QPaintEvent * )
{
  showIt();
}
*/

/*
void BackgroundImageShow::doZoom( int in_out )
{
  zoom += in_out;
  if ( zoom < -4) zoom = -4;
  if ( zoom > 4)  zoom = 4;
  if ( orig_pix == NULL ) return;
  // DBG_CHECK("BackgroundImageShow::doZoom() zoom %d \n", zoom );
  if ( zoom > 0 ) {
    QImage img = orig_pix->toImage();
    QImage img2 = img.scaled( orig_pix->width() * (1<<zoom),  orig_pix->height() * (1<<zoom) );
    pix.fromImage( img2 );
  } else if ( zoom < 0 ) {
    QImage img = orig_pix->toImage();
    QImage img2 = img.scaled( orig_pix->width() / (1<<(-zoom)), orig_pix->height() / (1<<(-zoom)) );
    pix.fromImage( img2 );
  } else {
    pix = *orig_pix;
  }
  DBG_CHECK("BackgroundImageShow::doZoom() pixmap %d %d\n", pix.width(), pix.height() );

  #if QT_VERSION >= 0x040000
    image->repaint();
  #else
    showIt();
  #endif
}

void BackgroundImageShow::doMove( int dx, int dy )
{
  xpos += dx * (pix.width()/10);
  ypos += dy * (pix.height()/10);
  #if QT_VERSION >= 0x040000
    image->repaint();
  #else
    showIt();
  #endif
  // repaint(0, 0, width(), height() );
}

void BackgroundImageShow::showIt()
{
  DBG_CHECK("BackgroundImageShow::showIt %d x %d zoom %d\n",
    pix.width(), pix.height(), zoom );
 
  QPainter p;

  resize( pix.width(), pix.height() );
  QPixmap pm( pix.width(), pix.height() );
  pm.fill( p.background().color() ); // FIXME
  if ( ! p.begin( &pm ) )
    return;

  int x=0, y=0, sx=0, sy=0;
  if ( xpos > 0 ) { sx += xpos; }
  else            { x  -= xpos; }
  if ( ypos > 0 ) { sy += ypos; }
  else            { y  -= ypos; }
  // DBG_CHECK("Pos %d %d X %d %d SX %d %d\n", xpos, ypos, x, y, sx, sy );
   
  // p.drawPixmap( x, y, pix, sx, sy, -1, -1 );
  p.drawPixmap( 0, 0, pix );

  if ( zoom < 0 ) {
    int s = 1 << (-zoom);
    // tentative station cross
    if ( xs >= 0 ) {
      int x = xs / s;
      int y = ys / s;
      p.drawLine( x-4, y, x+4, y );
      p.drawLine( x, y-4, x, y+4 );
    }
    const std::vector< BackgroundImageStation > & stations = image->getStations();
    for ( std::vector< BackgroundImageStation >::const_iterator it = stations.begin();
          it != stations.end();
          ++it ) {
      int x = it->x / s;
      int y = it->y / s;
      p.drawLine( x-4, y, x+4, y );
      p.drawLine( x, y-4, x, y+4 );
      p.drawText( x+2, y-2, it->name.c_str() );
    }
  } else {
    int s = 1 << zoom;
    if ( xs >= 0 ) {
      int x = xs * s;
      int y = ys * s;
      p.drawLine( x-4, y, x+4, y );
      p.drawLine( x, y-4, x, y+4 );
    }
    const std::vector< BackgroundImageStation > & stations = image->getStations();
    for ( std::vector< BackgroundImageStation >::const_iterator it = stations.begin();
          it != stations.end();
          ++it ) {
      int x = it->x * s;
      int y = it->y * s;
      p.drawLine( x-4, y, x+4, y );
      p.drawLine( x, y-4, x, y+4 );
      p.drawText( x+2, y-2, it->name.c_str() );
    }
  }

  p.end();
  // copy pixmap to widget
  // bitBlt( this, xpos, ypos, &pm, 0, 0, -1, -1 );
  // bitBlt( this, x, y, &pm, sx, sy, -1, -1 );
  printf("FIXME do display with GraphicsScene\n");
}
*/
