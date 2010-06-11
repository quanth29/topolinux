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

#include "portability.h"


// #include <qvbox.h>
// #include <qhbox.h>
// #define QHBOX QHBox

#include <qpainter.h>
#include <qpixmap.h>
#include <qpicture.h>
#include <qdialog.h>
#include <qlineedit.h>

#include "ImageTransform.h"
#include "Language.h"

class BackgroundImage;

/** sketch image display
 */
class BackgroundImageShow : public QWidget
{
    // Q_OBJECT
  private:
    BackgroundImage * image;  //!< main window
    QPixmap * orig_pix; //!< original pixmap (N.B. caller must manage it FIXME)
    QPixmap pix;        //!< current pixmap
    int xs, ys;         //!< temporary station coordinates
    int zoom;         //!< zoom value
    int xpos;         //!< X offset
    int ypos;         //!< Y offset

  public:
    /** cstr
     * @param my_image   image main window
     */
    BackgroundImageShow( BackgroundImage * my_image );

    /** dstr
     * @note the original pixmap is not destroyed
     *       must be passed to the caller (or upper) before dstr
     *       and the caller must manage it
     */
    ~BackgroundImageShow() { }

    /** set the temporary station coordinates
     * @param x  X
     * @param y  Y
     */
    void setStation( int x = -1, int y = -1 ) 
    {
      xs = x;
      ys = y;
      if ( xs >= 0 ) showIt();
    }
  
    /** load a PNG image
     * @param name image filename
     */
    void LoadImage( const char * name );

    /** apply the zoom and display the sketch
     * @param in_out  zoom variation (pos. increase; neg. decrease)
     */
    void doZoom( int in_out );

    /** mode the sketch
     * @param dx  X displacement
     * @param dy  Y displacement
     */
    void doMove( int dx, int dy );

    /** accessor for the zoom value
     * @return the value of the zoom
     */
    int Zoom() const { return zoom; }

    /** accessor for the X position
     * @return the value of the X position
     */
    int Xpos() const { return xpos; }

    /** accessor for the Y position
     * @return the value of the Y position
     */
    int Ypos() const { return ypos; }

    /** accessor for the image
     * @return the pointer to the background image
     */
    QPixmap * GetImage() { return orig_pix; }

  private:
    /** display
     */
    void showIt();

    /** handle paint event
     * @param e   paint event
     */
    void paintEvent( QPaintEvent * e );

};

/** sketch image main window
 */
class BackgroundImage : public QMAINWINDOW
{
  Q_OBJECT
  private:
    QWidget * parent;                     //!< parent widget
    Language & lexicon;
    int offset_y;                         //!< Y offset of the image on the screen
    BackgroundImageCallback * callback;   //!<
    BackgroundImageShow    * mis;         //!< sketch image display
    std::vector< BackgroundImageStation > stations; //!< stations correspondences

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

    /** add a new station point (correspondence)
     * @param name   station name
     * @param x      X coord of the point on the pixmap
     * @param y      Y coord of the point on the pixmap
     */
    void addStation( const QString & name, int x, int y )
    { 
      stations.push_back( BackgroundImageStation( name.latin1(), x, y) );
      // update();
    }

    /** get the station at a point (actually close to)
     * @param x   x coord
     * @param y   y coord
     * @return station or NULL
     */
    BackgroundImageStation * getStationAt( int x, int y )
    { 
      for ( std::vector< BackgroundImageStation >::iterator sit = stations.begin(),
            end = stations.end();
            sit != end;
            ++sit ) {
        if ( abs(x - sit->x) < 4 && abs(y - sit->y) < 4 ) 
          return &(*sit);
      }
      return NULL;
    }

    void removeStation( BackgroundImageStation * st )
    {
      for ( std::vector< BackgroundImageStation >::iterator sit = stations.begin(),
             end = stations.end();
             sit != end;
             ++sit ) {
        if ( &(*sit) == st ) {
           stations.erase( sit );
           break;
        }
      }
      // TODO update();
    }

    void changeStation( BackgroundImageStation * st, const char * name )
    {
      st->name = name;
      // TODO update();
    }
    


    /** get the vector of stations
     * @return a ref. to the vector of stations
     */
    const std::vector< BackgroundImageStation > & getStations() const { return stations; }

  private:
    void mousePressEvent ( QMouseEvent * e );

  public slots:
    /** set the image as canvas background 
     */
    void doSave();

    /** quit without setting the background image
     */
    void doQuit();

    /** zoom in and out
     */
    void doZoomIn() { mis->doZoom( 1 ); }
    void doZoomOut() { mis->doZoom( -1 ); }

    /** move the image left/right and up/down
     */
    void doDown() { mis->doMove( 0, -1 ); }
    void doUp() { mis->doMove( 0, 1 ); }
    void doRight() { mis->doMove( -1, 0 ); }
    void doLeft() { mis->doMove( 1, 0 ); }
};


/** dialog to enter the station names
 */
class BackgroundImageStationDialog : public QDialog
{
  Q_OBJECT
  private:
    BackgroundImage * parent;
    QLineEdit * station;
    int x;
    int y;

  public:
    BackgroundImageStationDialog( BackgroundImage * my_parent, int x0, int y0 );

  public slots:
    void doOK();
    
    void doCancel() { delete this; }
};

/** dialog to edit the station names
 */
class BackgroundImageEditStationDialog : public QDialog
{
  Q_OBJECT
  private:
    BackgroundImage * parent;
    BackgroundImageStation * station;
    QLineEdit * st_name;
    QCheckBox * remove;

  public:
    BackgroundImageEditStationDialog( BackgroundImage * my_parent, BackgroundImageStation * st );

  public slots:
    void doOK();
    
    void doCancel() { delete this; }
};

#endif

#endif

