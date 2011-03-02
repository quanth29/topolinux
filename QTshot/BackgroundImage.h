/** @file BackgroundImage.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief background image
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef BACKGROUND_IMAGE_H
#define BACKGROUND_IMAGE_H

#ifdef HAS_BACKIMAGE

#include <math.h>

#include <string>
#include <vector>

// #include "portability.h"
#include "shorthands.h"


// #include <qvbox.h>
// #include <qhbox.h>
// #define QHBOX QHBox

// #include <QPainter>
#include <QPixmap>
// #include <QPicture>
#include <QDialog>
#include <QLineEdit>
#include <QCheckBox>
#include <QGraphicsView>
#include <QGraphicsScene>
#include <QGraphicsPixmapItem>
#include <QMainWindow>
#include <QAction>

#include "ImageTransform.h"
#include "Language.h"

#include "BackgroundImageStationSet.h"

#define ZOOM_STEP 1.41

class BackgroundImage;

class BackgroundImageScene: public QGraphicsScene
{
  private:
    BackgroundImageStationSet stations;
    BackgroundImage * image;
    QGraphicsPixmapItem * pixmap;
    QPixmap * orig_pix; //!< original pixmap (N.B. caller must manage it FIXME)
    bool do_add_station;
    bool do_remove_station;
    QString station_name;
    
  public:
    BackgroundImageScene( BackgroundImage * my_image );

    ~BackgroundImageScene();

    /** accessor for the image
     * @return the pointer to the background image
     * FIXME
     */
    QPixmap * getImage() { return orig_pix; }
    // const QPixmap & getPixmap() const { return pixmap->pixmap(); }


    // callback for StationDialog
    void setStationName( QString & name )
    {
      do_add_station = ! name.isEmpty();
      station_name = name;
    }
    
    void setRemoveStation( bool remove ) { do_remove_station = remove; }

    const std::vector< BackgroundImageStation * > & getStations() 
    {
      return stations.getStations();
    }

    void mouseReleaseEvent( QGraphicsSceneMouseEvent * e0 );
    void mousePressEvent( QGraphicsSceneMouseEvent * e0 );

    /** load a PNG image
     * @param name image filename
     */
    void loadImage( const char * name );

    void changeStation( BackgroundImageStation * st, QString name )
    {
      st->setName( name );
    }
};

/** sketch image display
 */
class BackgroundImageView : public QGraphicsView
{
    // Q_OBJECT
  private:
    BackgroundImage * image;      //!< main window
    BackgroundImageScene * scene; //!< scene

  public:
    /** cstr
     * @param parent   image main window
     * @param scene    image scene
     */
    BackgroundImageView( BackgroundImage * parent, BackgroundImageScene * scene );

    /** dstr
     * @note the original pixmap is not destroyed
     *       must be passed to the caller (or upper) before dstr
     *       and the caller must manage it
     */
    ~BackgroundImageView() { }

};



/** sketch image main window
 */
class BackgroundImage : public QMainWindow
{
  Q_OBJECT
  private:
    QWidget * parent;                     //!< parent widget
    Language & lexicon;
    int offset_y;                         //!< Y offset of the image on the screen
    BackgroundImageCallback * callback;   //!<
    BackgroundImageView     * view;        //!< sketch image display
    BackgroundImageScene    * scene;

    QAction * actOk;
    QAction * actQuit;
    QAction * actZoomIn;
    QAction * actZoomOut;

  public:
    /** cstr
     * @param my_parent   parent widget
     * @param cb          callback object to set the background image
     * @paramn name       image filename
     */
    BackgroundImage( QWidget * my_parent, 
                     BackgroundImageCallback * cb,
                     const char * name );

    /** dstr
     */
    virtual ~BackgroundImage();

  public slots:
    /** set the image as canvas background 
     */
    void doSave();

    /** quit without setting the background image
     */
    void doQuit();

    void onZoomIn() { view->scale( ZOOM_STEP, ZOOM_STEP ); }

    void onZoomOut() { view->scale( 1.0/ZOOM_STEP, 1.0/ZOOM_STEP ); }

  private:
    void createToolBar();

    void createActions();
};


/** dialog to enter the station names
 */
class BackgroundImageStationDialog : public QDialog
{
  Q_OBJECT
  private:
    BackgroundImageScene * scene;
    QLineEdit * station;

  public:
    BackgroundImageStationDialog( BackgroundImage * my_parent, 
                                  BackgroundImageScene * scene );

  public slots:
    void doOK();
    
    void doCancel() 
    { 
      hide();
      QString empty; // actually null
      scene->setStationName( empty );
    }
};

/** dialog to edit the station names
 */
class BackgroundImageEditStationDialog : public QDialog
{
  Q_OBJECT
  private:
    BackgroundImage * parent;
    BackgroundImageScene * scene;
    BackgroundImageStation * station;
    QLineEdit * st_name;
    QCheckBox * remove;

  public:
    BackgroundImageEditStationDialog( BackgroundImage * my_parent,
                                      BackgroundImageScene * my_scene,
                                      BackgroundImageStation * st );

  public slots:
    void doOK();
    
    void doCancel() 
    {
      hide();
    }
};

#endif

#endif

