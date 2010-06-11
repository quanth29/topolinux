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

#include <qimage.h>
#include <qcursor.h>
#include <qdial.h>

#include "IconSet.h"
#include "Language.h"
#include "Extend.h"
#include "Units.h"
#include "TherionLine.h"
#include "Plot.h"
#include "CanvasMode.h"
#include "PlotStatus.h"
#include "PlotFrame.h"
#include "PlotThExport.h"
#include "BackgroundImage.h"
#include "ImageTransform.h"
#include "QTshot.h"
 
#define NP_ORIENT 8

class PlotCanvasView; // forward


/** plot canvas main window
 */
class PlotCanvas : public QMAINWINDOW
                 #ifdef HAS_BACKIMAGE
                 , BackgroundImageCallback
                 #endif
{
  Q_OBJECT

  private:
    QTshotWidget * parent;    //!< parent widget
    Language & lexicon;       //!< i18n lexicon
    IconSet * icon;           //!< pointer to the singleton icon set
    Plot * plot;              //!< plot
    QCANVAS * canvas;         //!< QT canvas object
    PlotCanvasView * view;    //!< viewer of the canvas
    QLabel * point_of_view;   //!< 3D point of view

    std::vector< std::pair<QCANVASLINE *, CanvasSegment*> > line_items;
    std::vector< std::pair<QCANVASTEXT *, CanvasPoint*> > text_items;
    std::vector< QCANVASITEM * > point_items;

    int mode;                 //!< plot mode, 0: plan, 1: extended, 2: 3D (not used)
    PlotThExport plot_export; //!< plot therion exporter

    PlotStatus * status;      //!< non-volatile status of the plot
    ThLine * cur_line;        //!< current line
    PlotFrame frame;          //!< plot reference frame
    const Units & units;      //!< reference to the current units
    bool do_splay;            //!< whether to show splay shots
    bool on_line;             //!< whether it is drawing a line
    
    // variable buttons:
    QTOOLBUTTON * btnNew;     //!< clear scrap
    QTOOLBUTTON * btnUndo;    //!< undo
    QTOOLBUTTON * btnMode;    //!< input mode

  #ifdef HAS_BACKIMAGE        // background sketch
    QImage * sketch;          //!< background warped image (sketch)
    QPixmap sketch_pix;       //!< background warped pixmap
    std::vector< BackgroundImagePoint > backStations;
    const QPixmap * backPixmap; //!< reference to the original background pixmap
  #endif

  public:
    /**
     * @param my_parent   parent QTshotWidget
     * @param mode        canvas mode: PLAN EXT CROSS
     * @param block       data_block (only for CROSS mode)
     * @param reversed    whether data block cross-section is reversed
     */
    PlotCanvas( QTshotWidget * my_parent,
                int mode, 
                DBlock * block = NULL,
                bool reversed = false );

    /**
     * @param my_parent   parent QTshotWidget
     * @param mode        canvas mode: PLAN EXT CROSS
     * @param hide        whether to open the plot canvas hiidden (default no)
     * 
    PlotCanvas( QTshotWidget * my_parent, int mode, bool hide = false );
     */

    ~PlotCanvas();

    PlotStatus * getStatus() { return status; }

    void selectLineStyle( ThLineType t, QPen & pen, QColor & color );

    void propagateUpdate()
    {
      parent->updateCanvases();
    }

    /** get the pen cursor
     * @return the pen cursor
     *
    QCursor & getCursorPen() { return cursor_pen; }
     */

    /** set the scrap name
     * @param name new scrap name
     */
    void setScrapName( const char * name ) { plot_export.setScrapname( name ); }
  
    /** get the scrap name
     * @return the scrap name
     */
    const char * getScrapName() const { return plot_export.getScrapname(); }

    void OnOffButtons( bool on_off )
    {
      if ( on_off ) {
        btnNew->setPixmap( icon->New() );
        btnUndo->setPixmap( icon->Undo() );
      } else {
        btnNew->setPixmap( icon->NewOff() );
        btnUndo->setPixmap( icon->UndoOff() );
      }
    }

    /** accessor to the survey data list
     * used by the station comment widget only
     */
    DataList * GetList() { return parent->GetList(); }
 

    /** clear list of items of all the plots
     * @note used by QTshotWidget on "new"
     */
    void ClearTh2PointsAndLines();

    void setMode( int input_mode, const char * name = NULL ) 
    {
      switch (input_mode) {
        case INPUT_POINT:
          if ( mode == MODE_PLAN ) {
            setCaption( QString(lexicon("qtopo_p_point")) + QString(name) );
          } else if ( mode == MODE_EXT ) {
            setCaption( QString(lexicon("qtopo_e_point")) + QString(name) );
          } else if ( mode == MODE_CROSS ) {
            setCaption( QString(lexicon("qtopo_x_point")) + QString(name) );
          }
          setCursor( QCursor(Qt::CrossCursor) );
          btnMode->setPixmap( icon->Mode3() );
          break;
        case INPUT_LINE:
          if ( mode == MODE_PLAN ) {
            setCaption( QString(lexicon("qtopo_p_line")) + QString(name) );
          } else if ( mode == MODE_EXT ) {
            setCaption( QString(lexicon("qtopo_e_line")) + QString(name) );
          } else if ( mode == MODE_CROSS ) {
            setCaption( QString(lexicon("qtopo_x_line")) + QString(name) );
          }
          // setCursor( getCursorPen() );
          setCursor( icon->PenUp() );
          btnMode->setPixmap( icon->Mode2() );
          break;
        default:
          if ( mode == MODE_PLAN ) {
            setCaption(lexicon("qtopo_p_select"));
          } else if ( mode == MODE_EXT ) {
            setCaption(lexicon("qtopo_e_select"));
          } else if ( mode == MODE_CROSS ) {
            setCaption(lexicon("qtopo_x_select"));
          } else if ( mode == MODE_3D ) {
            setCaption(lexicon("qtopo_select"));
          }
          setCursor( QCursor(Qt::ArrowCursor) );
          btnMode->setPixmap( icon->Mode1() );
      }
    }

    /** close the current line if it is open
     */
    void endLine() 
    {
      if ( cur_line ) {
        setOnLine( false );
        cur_line = NULL;
        status->AddUndo( UNDO_ENDLINE );
      } else {
        assert( on_line == false );
      }
    }

    /** recompute and redraw the plot
     */
    void redrawPlot();

    #ifndef QT_NO_SCROLLVIEW
      /** get the canvas scrollling position
       */
      void getCanvasScrollPosition( double & x, double & y );

      /** get the canvas scrollling position
       * @param x   horizontal position (between 0 and 1)
       * @param y   vertical position (between 0 and 1)
       */
      void setCanvasScrollPosition( double x, double y );
    #else
      void getCanvasScrollPosition( double & x, double & y ) { }
      void setCanvasScrollPosition( double x, double y ) { }
    #endif

  private:
    /** display the plot
     */
    void showPlot( );
  
    /** set whether is drawing a line or not
     * @param yes_no
     */
    void setOnLine( bool yes_no ) 
    {
      on_line = yes_no;
      if ( on_line ) {
        setCursor( icon->PenDown() );
      } else {
        setCursor( icon->PenUp() );
      }
    }

    #ifdef HAS_BACKIMAGE
      /** set the background image
       */
      void setBackground();

      void setBackgroundInit();
    #endif

    /** set the picture offset on the canvas
     */
    void setOffset();

    /** display a point as a disk in color
     * @param x      X coord. of the center of the disk
     * @param y      Y coord.
     * @param color  disk color
     */
    void showPoint( double x, double y, const QColor & color );

    /** display a point with orientation
     * @param x   X coord
     * @param y   Y coord
     * @param type   point type
     * @param orient point orientation (0..7, units 45 degrees)
     */
    void showPoint( double x, double y, ThPointType type, int orient );

    /** show arrow head
     * @param x   X coord
     * @param y   Y coord
     * @param points arrows head shape
     */
    void showPoint( double x, double y, const QPOINTARRAY & points );

    /** show a label point
     * @param x   X coord
     * @param y   Y coord
     * @param option label option "-text ..."
     */
    void showPointLabel( double x, double y, const char * option);

    /**
     * @param type   line type
     * @param size   lien size before inserting the point
     */
    void showLineSegment( int x, int y, double x0, double y0,
                          ThLineType type, size_t size );

    /** display a line segment
     * @param x1  first endpoint X coord.
     * @param y1  first endpoint Y coord.
     * @param x2  second endpoint X coord.
     * @param y2  second endpoint Y coord.
     * @param pen drawing pen
     */
    void showLine( double x1, double y1, double x2, double y2, const QPen & pen );

    /** clear the drawing
     */
    void clear();

    /** clear list of items (lines and points) of the plot
     * @param lines   vector of lines
     * @param pts     vector of points
     *
    void ClearTh2( std::vector< ThLine * > & lines, std::vector< ThPoint2D > & pts );
     */

  public:
    /** set the extend status of a segment
     * @param block   centerline block
     * @param extend  new extend status
     * @param propagate  whether to propagate update to parent
     */
    void doExtend( DBlock * block, int extend, bool propagate );

    /** get the segment associated with a Canvas line
     * @param line   canvas line
     * @return the associated segment (or NULL)
     */
    CanvasSegment * getSegmentFromLine( QCANVASLINE * line );

    /** accessor: get the plot mode: 0 plan, 1 extended, 2 cross-section
     * @return the current plot mode
     */
    int getMode() const { return mode; }

    /** insert a point of type station
     * @param x   point X coord.
     * @param y   point Y coord.
     * @param option point option string
     */
    void insertStationPoint( int x, int y, const char * option = NULL );

    /** check if a station has already been inserted
     * @param option  station option (ie, "-name ...")
     * @return true if the station has been already inserted
     */
    bool hasStation( const std::string & option );

    /** insert a point on the list of therion 2D points
     * @param x       X coordinate
     * @param y       Y coordinate
     * @param type    point type
     * @param orient  point orientation (WATER and AIR only)
     * @param option  point option string
     * @param add_offset whether to add the offset (default no)
     */
    void insertPoint( int x, int y, 
                      ThPointType type, int orient = 0, 
                      const char * option = NULL,
                      bool add_offset = false );

    /** insert a line control point
     * @param x       X coordinate
     * @param y       Y coordinate
     * @param type    line type (used only when a new line begins)
     * @param add_offset whether to add the offset (default no)
     */
    void insertLinePoint( int x, int y, ThLineType type, bool add_offset = false );

    /** save current plot as th2 file
     * @param file filename
     */
    void doSaveTh2( const QString & file );

    void doRealSaveTh2( );

    /** accessor:
     * @param text
     * @return pointer to the canvas point
     */ 
    CanvasPoint * getTextPoint( QCANVASTEXT * text );

    /** really clear the scrap
     */
    void doRealClearTh2();

    #ifdef HAS_BACKIMAGE
      /** load a background sketch
       * @param name PNG filename of the sketch
       */
      void doImage( const QString & name );

      /** make the background image warping the sketch
       * @param stations vector of station correspondences
       * @param back_pixmap sketch background pixmap
       */
      void evalBackground( std::vector< BackgroundImageStation > & stations, QPixmap * back_pixmap );
    #endif

  private:
    /** display grid-string on toolbar
     */
    void showGridSpacing()
    {
      if ( mode == MODE_3D ) {
        QString text;
        text.sprintf("  I: %.0f  A: %.0f", status->Theta(), status->Phi() );
        point_of_view->setText( text );
      } else {
        int gs = frame.GridSpacing();
        if ( gs > 0 ) {
          QString text;
          text.sprintf("%s: %d %s", lexicon("grid"), gs, units.length_unit );
          point_of_view->setText( text );
        } else {
          point_of_view->setText( "" );
        }
      }
    }


  public slots:
    #ifdef HAS_BACKIMAGE
      void onImage();
    #endif
    void onUndo();
    void onSplay();
    void onZoomIn();
    void onZoomOut();
    void onClearTh2();
    void onSaveTh2();
    void onMode();
    void onGrid();
    void onNumbers();
    void onQuit();
    void onThetaPlus();
    void onThetaMinus();
    void onPhiPlus();
    void onPhiMinus();

    void update();
};

class PlotCanvasView : public QCANVASVIEW
{
  // Q_OBJECT

  private:
    PlotCanvas * plot_canvas;
    Language & lexicon;         //!< i18n lexicon
    QCANVASLINE * the_line;     //!< current canvas line
    QPoint cur_pt;              //!< current canvas point
    enum InputMode input_mode;  //!< current canvas input mode
    ThPointType point_type;     //!< current point type
    int         point_orient;   //!< current point orientation
    ThLineType  line_type;      //!< current line type
    std::string label_text;     //!< text of the current label point

  public:
    /** cstr
     * @param c    QT canvas
     * @param mc   canvas main window
     */
    PlotCanvasView( QCANVAS * c, PlotCanvas * mc );

    /** handle mouse press events
     * @param e  mouse press event (need X and Y)
     */
    void contentsMousePressEvent(QMouseEvent* e);

    /** accessor: get the input mode
     * @return the current input mode
     */
    InputMode getInputMode() const { return input_mode; }

    /** accessor to the point type
     * @return the current point type
     */
    ThPointType getPointType() const { return point_type; }

    int getPointOrient() const { return point_orient; }

    ThLineType getLineType() const { return line_type; }

    void setPointOrient( int orient ) { point_orient = orient; }

    void setPointType(int t ) { point_type = (ThPointType)t; }

    void setLabelText( const char * text ) { label_text = text; }

    /** set the type of line
     * @param t      new line type
     * @param is_line_mode whether it is in line mode
     */
    void setLineType(int t, bool is_line_mode ) { 
      if ( line_type != (ThLineType)t ) {
        line_type = (ThLineType)t;
        if ( is_line_mode ) {
          plot_canvas->endLine( );
        }
      } else if ( !is_line_mode ) {
        plot_canvas->endLine( );
      }
    }

    /** reset line type (called by the UNDO)
     * @param t      new line type
     */
    void resetLineType( int t ) { line_type = (ThLineType)t; }


    void setInputMode( InputMode mode );

/*
  public slots:
    void onExtendNone();
    void onExtendLeft();
    void onExtendRight();
    void onExtendVert();
    void onExtendIgnore();
*/
};

/** dialog to switch the canvas mode
 */
class CanvasCommandWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvasView * view;
    QRadioButton * mode[3];   //!< mode: command point line
    QDial * porient;          //!< point orientation dial
    QComboBox * ptsBox;       //!< point type selection
    QComboBox * lnsBox;       //!< line type selection
    int np_type;              //!< number of point types
    int nl_type;              //!< number of line types

  public:
    CanvasCommandWidget( PlotCanvasView * parent );

    // ~CanvasCommandWidget();

  public slots:
    void doOK();
    void doCancel();

    /** swicth to Point mode active
     * @param i   point type index
     */
    void doPoint(int i);

    /** swicth to Line mode active
     *@param i  line type index
     */
    void doLine(int i);

    // void doUndo();
};

/** file dialog to save the plot in therion th2 format
 */
class MyFileDialogCV : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * widget;
    QLineEdit * line;
    int mode;

  public:
    MyFileDialogCV( PlotCanvas * parent, const char * title, int m )
      : QDialog( parent, title, TRUE )
      , widget( parent )
      , mode( m )
    {
      Language & lexicon = Language::Get();
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

  public slots:
    void onOK()
    {
      if ( mode == 0 ) {
        // widget->onOpenFile( line->text() );
      } else {
        widget->doSaveTh2( line->text() );
      }
      delete this;
    }

    void onCancel()
    {
      delete this;
    }

};

#ifdef HAS_BACKIMAGE
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
      if ( mode == 0 ) {
        // widget->onOpenFile( line->text() );
      } else {
        widget->doImage( line->text() );
      }
      delete this;
    }

    void onCancel()
    {
      delete this;
    }
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
    QBUTTONGROUP * m_group;

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
    void doOK() { parent->doRealClearTh2(); delete this; }
    void doCancel() { delete this; }
};

/** dialog to get the scrap name, input by the user
 */
class ScrapWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvas * parent;
    QLineEdit * name;

  public:
    ScrapWidget( PlotCanvas * p, int cnt );

  public slots:
    void doOK();
    void doCancel() { delete this; }
};

/** dialog to get the label text, input by the user
 */
class LabelWidget : public QDialog
{
  Q_OBJECT
  private:
    PlotCanvasView * parent;
    QLineEdit * text;

  public:
    LabelWidget( PlotCanvasView * p, const char * caption );

  public slots:
    void doOK() 
    { 
      parent->setLabelText( text->text().latin1() );
      delete this;
    }

    void doCancel() { delete this; }
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
    void doCancel() { delete this; }
};



#endif

