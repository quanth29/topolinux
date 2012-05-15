/** @file PlotCanvas.h
 *
 * @author marco corvi
 * @date aug. 2009
 *
 * @brief 2D/3D plot canvas
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef PLOT_CANVAS_H
#define PLOT_CANVAS_H

#include <vector>

#include <QMainWindow>
#include <QImage>
#include <QCursor>
#include <QDial>
#include <QGraphicsItem>
#include <QGraphicsView>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QPushButton>
#include <QButtonGroup>
#include <QToolBar>
#include <QMenu>

#include "shorthands.h"

#include "IconSet.h"
#include "Language.h"
#include "Extend.h"
#include "Units.h"
#include "TherionLine.h"
#include "Plot.h"
#include "CanvasMode.h"
#include "PlotStatus.h"
// #include "PlotFrame.h"
#include "PlotThExport.h"
#include "QTshot.h"
 
#define NP_ORIENT 8

class PlotCanvasView; // forward
class PlotCanvasScene;

/** plot canvas main window
 */
class PlotCanvas : public QMainWindow
{
  Q_OBJECT

  private:
    QTshotWidget * parent;    //!< parent widget
    Language & lexicon;       //!< i18n lexicon
    IconSet * icon;           //!< pointer to the singleton icon set
    QString plot_name;        //!< plot name
    Plot * plot;              //!< plot
    QLabel * point_of_view;   //!< 3D point of view
    PlotCanvasScene * scene;  //!< QT scene object
    PlotCanvasView * view;    //!< viewer of the scene
    // PlotFrame * frame;        //!< plot reference frame
    PlotStatus * status;

    int mode;                 //!< plot mode, 0: plan, 1: extended, 2: 3D (not used)
    const Units & units;      //!< reference to the current units
    // PlotThExport plot_export; //!< plot therion exporter
    
    QAction * actSave;
    QAction * actGrid;
    QAction * actEval;
    QAction * actCollapse;
    QAction * actNumbers;
    QAction * actZoomIn;
    QAction * actZoomOut;
    QAction * actNew;
    QAction * actUndo;
    QAction * actScrap;
    QAction * actSelect;
    QAction * actSelectMenu[ 2 ]; // select | orientation
    QAction * actPoint;
    QAction * actPointMenu[ Therion::THP_PLACEMARK ];
    QAction * actLine;
    QAction * actLineMenu[ Therion::THL_PLACEMARK ];
    QAction * actArea;
    QAction * actAreaMenu[ Therion::THA_PLACEMARK ];

    QAction * actThetaPlus;
    QAction * actThetaMinus;
    QAction * actPhiPlus;
    QAction * actPhiMinus;
    QAction * actClose;
    QAction * actQuit;
    QAction * actView;

  #ifdef HAS_BACKIMAGE        // background sketch
    QAction * actImage;
  #endif

    QMenu * scrap_menu;
    QMenu * view_menu;
  private:
    void setCaption();

  public:
    /** center the view
     * @param x   X coord
     * @param y   Y coord
     */
    void centerOn( double x, double y );

    int getPlotWidth() { return plot->getWidth(); }
    int getPlotHeight() { return plot->getHeight(); }
    int getPlotXOffset() { return plot->getXOffset(); }
    int getPlotYOffset() { return plot->getYOffset(); }

    PlotCanvasScene * getScene() { return scene; }

    Plot * getPlot() { return plot; }

    /** The file_name (std::string) coincides with the plot_name (QString)
     * @return the file_name (as C string)
     */
    const char * getScrapName() const { return status -> getScrap() -> getScrapName(); }
    const char * getFileName() const { return  status -> getFileName(); }
    const char * getBaseFileName() const { return  status -> getBaseFileName(); }

    /** set the scrap name
     * @param name new scrap name
     */
    void setName( const char * name ) 
    {
      plot_name = name;
      status -> getScrap() -> setScrapName( name );
      status -> setFileName( name );
      status -> setImageName( name );
    }

    /** set the name of the file
     * @param name filename
     */
    void setFileName( const char * name ) 
    {
      status -> setFileName( name );
    }

    /** set the name of the image
     * @param name image filename
     */
    void setImageName( const char * name ) 
    {
      status -> setImageName( name );
    }

    /** display grid-string on toolbar
     */
    void showGridSpacing();

   

  public:
    /**
     * @param my_parent   parent QTshotWidget
     * @param mode        scene mode: PLAN EXT CROSS
     * @param pname       plot name
     * @param sname       scrap name
     * @param block       data_block (only for CROSS mode)
     * @param reversed    whether data block cross-section is reversed
     */
    PlotCanvas( QTshotWidget * my_parent,
                int mode, 
                const char * pname, 
                const char * sname,
                DBlock * block = NULL,
                bool reversed = false );

    /**
     * @param my_parent   parent QTshotWidget
     * @param mode        scene mode: PLAN EXT CROSS
     * @param hide        whether to open the plot scene hiidden (default no)
     * 
    PlotCanvas( QTshotWidget * my_parent, int mode, bool hide = false );
     */

    ~PlotCanvas();

    /** get the plot status info
     * @return the plot status
     */
    PlotStatus * getStatus() { return status; }

    /** get the current units
     * @return the units
     */
    const Units & getUnits() const { return units; }

    /** propagate an update to the parent
     */
    void propagateUpdate()
    {
      parent->updateCanvases();
    }

    /** get the pen cursor
     * @return the pen cursor
     *
    QCursor & getCursorPen() { return cursor_pen; }
     */
 
  public:
    /** turn disableable buttons on/off
     * @param on_off whether to turn the buttons on or off
     */
    void turnButtonsOnOff( bool on_off )
    {
      if ( on_off ) {
        actNew->setIcon( icon->New() );
        actUndo->setIcon( icon->Undo() );
      } else {
        actNew->setIcon( icon->NewOff() );
        actUndo->setIcon( icon->UndoOff() );
      }
    }
  
  public:
    /** accessor to the survey data list
     * used by the station comment widget only
     */
    DataList * getList() { return parent->getList(); }
 

    /** clear list of items of this plot
     * @note used by QTshotWidget on "new"
     */
    void clearTh2PointsAndLines();

    /** set the canvas mode
     * @param input_mode  new mode
     * @param name        mode name (window title)
     */
    void setMode( int input_mode, const char * name = NULL );

  private:
    /** creaete the toolbar
     */
    void createToolBar();

    /** create the actions
     */
    void createActions();

  public:
    /** recompute and redraw the plot
     */
    void redrawPlot();


  public:
    /** set the extend status of a segment
     * @param block   centerline block
     * @param extend  new extend status
     * @param propagate  whether to propagate update to parent
     */
    void doExtend( DBlock * block, int extend, bool propagate );

    /** accessor: get the plot mode: 0 plan, 1 extended, 2 cross-section
     * @return the current plot mode
     */
    int getMode() const { return mode; }

    /** insert a point of type station
     * @param x   point X coord.
     * @param y   point Y coord.
     * @param option point option string
     */
    ThPoint2D * insertPointStation( double x, double y, const char * option = NULL );

    /** check if a station has already been inserted
     * @param option  station option (ie, "-name ...")
     * @return true if the station has been already inserted
     */
    bool hasStation( const std::string & option );

    /** save current plot as th2 file
     * @param file filename
     */
    void doSaveTh2( const QString & file );

    /** save as image (PNG)
     * @param file image filename
     */
    void doSaveImage( const QString & file );

    /** really save as therion th2 file
     */
    void doRealSaveTh2( );

    /** really save as image (PNG)
     */
    void saveAsImage();

    /** really clear the scrap
     */
    void doRealClearTh2();

    #ifdef HAS_BACKIMAGE
      /** load a background sketch
       * @param name PNG filename of the sketch
       */
      void doImage( const QString & name );
    #endif

    /** add a new scrap
     * @param name name of the new scrap
     */
    void doNewScrap( const char * name ) 
    { 
      status->addScrap( name ); 
      scrap_menu->addAction( name );
    }

    /** do a real quit
     */
    void doQuit()
    {
      hide();
      parent->removePlotCanvas( this );
    }


  public slots:
    #ifdef HAS_BACKIMAGE
      void onImage();
    #endif
    void onUndo();
    void onSplay();
    void onEval();
    void onZoomIn();
    void onZoomOut();
    void onClearTh2();
    void onSave( QAction * );
    void onMode();
    void onScrap( QAction * );
    void onSelect();
    void onPoint();
    void onLine();
    void onArea();
    void onPointMenu( QAction * a );
    void onLineMenu( QAction * a );
    void onAreaMenu( QAction * a );
    void onGrid();
    void onNumbers();
    void onClose();
    void onQuit();
    void onThetaPlus();
    void onThetaMinus();
    void onPhiPlus();
    void onPhiMinus();

    void update();
};

class PlotCanvasView : public QGraphicsView
{
  // Q_OBJECT

  private:
    PlotCanvas * plot_canvas;
    PlotCanvasScene * scene;
    Language & lexicon;         //!< i18n lexicon


  public:
    /** cstr
     * @param s   QT scene
     * @param c   canvas main window
     */
    PlotCanvasView( PlotCanvasScene * s, PlotCanvas * c );


    PlotCanvasScene * getScene() { return scene; }
/*
  public slots:
    void onExtendNone();
    void onExtendLeft();
    void onExtendRight();
    void onExtendVert();
    void onExtendIgnore();
*/
};

// -------------------------------------------------------------------
// SCRAP NAME

/** dialog to get the name of the new scrap
 */
class ScrapNewWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * parent;
    QLineEdit * line;

  public:
    ScrapNewWidget( PlotCanvas * c );

  public slots:
    void doOK();
    void doCancel() { hide(); }

};

/** dialog to switch the scene mode
 */
class CanvasCommandWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvasScene * scene ;
    QDial * porient;          //!< point orientation dial

  public:
    CanvasCommandWidget( QWidget * parent, PlotCanvasScene * c );

    // ~CanvasCommandWidget();

  public slots:
    void doOK();
    void doCancel() { hide(); }

};


#if 0 // def HAS_BACKIMAGE
/** file dialog to open a sketch file for the background image
 */
class MyFileDialogSketch : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * widget;
    QLineEdit * line;
    int mode;

  public:
    MyFileDialogSketch( PlotCanvas * parent, const char * title, int m );

  public slots:
    void onOK()
    {
      hide();
      if ( mode == 0 ) {
        // widget->onOpenFile( line->text() );
      } else {
        widget->doImage( line->text() );
      }
    }

    void onCancel() { hide(); }
};
#endif

/** dialog to get the extend flag for a shot
 */
class ExtendWidget : public QDialog
{ 
  Q_OBJECT
  private:
    QWidget * perent;
    PlotCanvas * plot_canvas;
    DBlock * block;
    QLineEdit * comment;
    QRadioButton * extBox[EXTEND_MAX];
    QButtonGroup * m_group;

  public:
    ExtendWidget( QWidget * p, PlotCanvas * c,  DBlock * b );

  public slots:
    void doOK();

    void doCancel();
};


/** dialog to confirm the scrap clean
 */
class CleanScrapWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * parent;

  public:
    CleanScrapWidget( PlotCanvas * p );

  public slots:
    void doOK() 
    { 
       parent->doRealClearTh2(); 
       hide();
    }
    void doCancel() 
    {
       hide();
    }
};

/** dialog to warn that the scrap has less than two stations
 */
class ScrapWarnWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * parent;

  public:
    ScrapWarnWidget( PlotCanvas * p );

  public slots:
    void doOK();
    void doCancel() 
    { 
      hide();
    }
};

/** dialog to the the comment, input by the user
 */
class StationCommentWidget : public QDialog
{
  Q_OBJECT
  private:
    // QWidget * parent;
    PlotCanvas * plot_canvas;
    const char * name;
    QLineEdit * comment;

  public:
    StationCommentWidget( QWidget * p, PlotCanvas * pc, const char * n );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};

class QuitWidget : public QDialog
{
  Q_OBJECT
  private:
    // QWidget * parent;
    PlotCanvas * parent;

  public:
    QuitWidget( PlotCanvas * p );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};




#endif

