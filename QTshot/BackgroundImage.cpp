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
#include "portability.h"
#include <qlabel.h>
#include <qcursor.h>
#include <qpushbutton.h>
#include <qcheckbox.h>
#include <qlayout.h>

// #include <qcanvas.h>
#include <qimage.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "ArgCheck.h"

#include "BackgroundImage.h"
#include "Language.h"
#include "config.h"
#include "IconSet.h"
#include "TherionPoint.h"

/** Default Y offset
 * The Y offset is fixed at runtime later
 */
#define OFFSET_Y 30


BackgroundImageShow::BackgroundImageShow( BackgroundImage * my_image )
  // : QWidget( parent, "Image", WResizeNoErase )
  : QWidget ( my_image )
  , image( my_image )
  , orig_pix( NULL )
  , xs( -1 )
  , ys( -1 )
  , zoom ( 0 )
  , xpos( 0 )
  , ypos( 0 )
{
}

void BackgroundImageShow::paintEvent( QPaintEvent * )
{
  showIt();
}



void BackgroundImageShow::doZoom( int in_out )
{
  zoom += in_out;
  if ( zoom < -4) zoom = -4;
  if ( zoom > 4)  zoom = 4;
  if ( orig_pix == NULL ) return;
  // DBG_CHECK("BackgroundImageShow::doZoom() zoom %d \n", zoom );
  if ( zoom > 0 ) {
    QImage img = orig_pix->convertToImage();
    QImage img2 = img.scaleWidth( orig_pix->width() * (1<<zoom) );
    pix.convertFromImage( img2 );
  } else if ( zoom < 0 ) {
    QImage img = orig_pix->convertToImage();
    QImage img2 = img.scaleWidth( orig_pix->width() / (1<<(-zoom)) );
    pix.convertFromImage( img2 );
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
  pm.fill( foregroundColor() );
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
  bitBlt( this, x, y, &pm, sx, sy, -1, -1 );
}


void
BackgroundImageShow::LoadImage( const char * name )
{
  ARG_CHECK( name == NULL, );

  if ( orig_pix != NULL ) delete orig_pix;
  orig_pix = new QPixmap();
  orig_pix->load( name );
  doZoom( 0 );
  DBG_CHECK("BackgroundImageShow::LoadImage %s %d x %d\n", 
      name, orig_pix->width(), orig_pix->height() ); 
}


BackgroundImage::BackgroundImage( QWidget * my_parent,
                                  BackgroundImageCallback * cb, const char *name )
  : QMAINWINDOW( my_parent )
  , parent( my_parent )
  , lexicon( Language::Get() )
  , offset_y( OFFSET_Y )
  , callback( cb )
{
  setCaption( lexicon("qtopo_sketch") );
  setCursor( QCursor(Qt::CrossCursor) );
  resize( 640, 480 );

  IconSet * icon = IconSet::Get();

/*
  QVBoxLayout* vb = new QVBoxLayout(this);
  // vb->setAutoAdd(TRUE);
  vb->setSpacing( 0 );
  vb->setMargin( 0 );
*/

  QTOOLBAR * toolbar = new QTOOLBAR( this );
  toolbar->resize( width(), offset_y );

  new QTOOLBUTTON( icon->Ok(), lexicon("insert"), QString::null, 
                   this, SLOT(doSave()), toolbar, lexicon("insert") );
  new QTOOLBUTTON( icon->ZoomIn(), lexicon("zoom_in"), QString::null, 
                   this, SLOT(doZoomIn()), toolbar, lexicon("zoom_in") );
  new QTOOLBUTTON( icon->ZoomOut(), lexicon("zoom_out"), QString::null,
                   this, SLOT(doZoomOut()), toolbar, lexicon("zoom_out") );
  new QTOOLBUTTON( icon->Left(), lexicon("left"), QString::null,
                   this, SLOT(doLeft()), toolbar, lexicon("left") );
  new QTOOLBUTTON( icon->Up(), lexicon("up"), QString::null,
                   this, SLOT(doUp()), toolbar, lexicon("up") );
  new QTOOLBUTTON( icon->Down(), lexicon("down"), QString::null,
                   this, SLOT(doDown()), toolbar, lexicon("down") );
  new QTOOLBUTTON( icon->Right(), lexicon("right"), QString::null,
                   this, SLOT(doRight()), toolbar, lexicon("right") );
  new QTOOLBUTTON( icon->Close(), lexicon("exit"), QString::null,
                   this, SLOT(doQuit()), toolbar, lexicon("exit") );

  // QVBOX * vbox = new QVBOX( this );
  mis = new BackgroundImageShow( this );
  this->setCentralWidget( mis );
  this->show();

  QPoint p = mis->pos();
  offset_y = p.y();

  if ( name ) {
    mis->LoadImage( name );
  }

  // vb->insertWidget(0, toolbar, 0 );
  // vb->insertWidget(1, vbox, 1 );

  // TODO
  // connect( emitter, SIGNAL(), receiver, SLOT() );
  // DBG_CHECK("BackgroundImage cstr done\n");
  // sleep( 5 );
}

BackgroundImage::~BackgroundImage()
{
  // DBG_CHECK("BackgroundImage dstr \n");
  if ( mis ) delete mis;
}

void
BackgroundImage::doSave()
{
  DBG_CHECK("BackgroundImage::doSave() stations %d \n", stations.size() );
  callback->evalBackground( stations, mis->GetImage() );
  this->close();
}

void
BackgroundImage::doQuit()
{
  // DBG_CHECK("BackgroundImage::doQuit() \n");
  this->close();
}
  
  
void 
BackgroundImage::mousePressEvent ( QMouseEvent * e )
{
  int z = mis->Zoom();
  int x = e->x() + mis->Xpos();
  int y = e->y() + mis->Ypos() - offset_y;
  if ( z > 0 ) { x /= (1<<z); y /= (1<<z); }
  else if ( z < 0 ) { x *= (1<<(-z)); y *= (1<<(-z)); }
  // DBG_CHECK("BackgroundImage::mousePressEvent at %d %d --> %d %d \n",
  //           e->x(), e->y(), x, y );
  // 
  BackgroundImageStation * st = getStationAt( x, y );
  if ( st == NULL ) {
    // TODO show the cross
    // DBG_CHECK("station at %d %d\n", x, y );
    mis->setStation( x, y );
    // mis->update();
    new BackgroundImageStationDialog( this, x, y );
    mis->setStation( -1, -1 );
  } else {
    new BackgroundImageEditStationDialog( this, st );
  }
}


BackgroundImageStationDialog::BackgroundImageStationDialog( BackgroundImage * my_parent, int x0, int y0 )
  : QDialog( my_parent, "BackgroundImageStationDialog", true )
  , parent( my_parent )
  , x( x0 )
  , y( y0 )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_station") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  new QLabel( lexicon("station"), hb );
  station = new QLineEdit( "", hb );
  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  show();
  exec();
}

void
BackgroundImageStationDialog::doOK()
{
  if ( ! station->text().isEmpty() ) {
    parent->addStation( station->text(), x, y );
  }
  delete this;
}

BackgroundImageEditStationDialog::BackgroundImageEditStationDialog(
    BackgroundImage * my_parent, 
    BackgroundImageStation * st )
  : QDialog( my_parent, "BackgroundImageEditStationDialog", true)
  , parent( my_parent )
  , station( st )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_edit_station") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  new QLabel( lexicon("station"), hb );
  st_name = new QLineEdit( st->name.c_str(), hb );
  hb = new QHBOX(this);
  remove = new QCheckBox( lexicon("remove_station"), hb );
  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("ok")), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel")), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  show();
  exec();
}

void
BackgroundImageEditStationDialog::doOK()
{
  // hide();
  if ( remove->isChecked() ) {
    parent->removeStation( station );
  } else if ( station->name != st_name->text().latin1() ) {
    parent->changeStation( station, st_name->text().latin1() );
  }
  delete this;
}

#endif // HAS_BACKIMAGE

#ifdef TEST

int main( int argc, char **argv )
{
    QApplication a( argc, argv );

    BackgroundImage * img = new BackgroundImage( 0, argv[1] );

    a.setMainWidget( img );
    img->setCaption("Qt Example - Image");
    img->show();
    return a.exec();
}

#endif

