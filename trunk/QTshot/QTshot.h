/* @file QTshot.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux data for OPIE
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef QT_DATA_H
#define QT_DATA_H
#include <assert.h>

#include <string>
#include <sstream>

// #include <qglobal.h>
#include <QMainWindow>
// #include <qpushbutton.h>
#include <QLabel>
#include <QThread>

#include <QAction>

// #include <qmenubar.h>
// #include <qlcdnumber.h>
// #include <qlayout.h>
#include <QLineEdit>
#include <QCheckBox>
#include <QComboBox>
// #include <qabstractlayout.h>
#include <QWidget>
#include <QPoint>
#include <QDialog>
#include <QRadioButton>
#include <QTableWidget>
#include <QTextEdit>
#include <QMenu>
#include <QMessageBox>
#include <QBrush>

#include "shorthands.h"


#ifndef EMBEDDED
  #define WIDGET_WIDTH  400
  #define WIDGET_HEIGHT 480
  #define STATION_WIDTH 40
  #define DATA_WIDTH    60
  #define FLAG_WIDTH    30

  #define CANVAS_WIDTH  400
  #define CANVAS_HEIGHT 400
#else
  #define WIDGET_WIDTH  240
  #define WIDGET_HEIGHT 320
  #define STATION_WIDTH 20
  #define DATA_WIDTH    35
  #define FLAG_WIDTH    15

  #define CANVAS_WIDTH  240
  #define CANVAS_HEIGHT 300
#endif


#include "DistoX.h"
#include "config.h"
#include "Language.h"
#include "IconSet.h"
#include "Locale.h"
#include "SplayAt.h"

#include "CenterlineInfo.h"
#include "DataList.h"
#include "ExportType.h"
#include "Units.h"

#include "PlotDrawer.h"
#include "PTcolors.h"
#include "PlotList.h"

class PlotCanvas;
class QTshotWidget;

class QTshotWidget : public QMainWindow
                   , public DistoXListener
                   , public PlotDrawer
{  
  Q_OBJECT

  public:
    /** cstr
     * @param parent      parent widget
     * @param name        widget name
     * @param fl          flags
     */
    QTshotWidget( QWidget * parent = 0, const char * name = 0, WFLAGS fl = 0 );

    // ~QTshotWidget()

   private:
     Config & config;       //!< application configuration
     Language & lexicon;    //!< lexicon
     IconSet * icon;        //!< pointer to the singleton IconSet
     CenterlineInfo info;
     #ifdef HAS_LRUD
       bool do_lrud;        //!< whether to show the LRUD checkbox
     #endif
     DataList dlist;        //!< survey data
     QTableWidget * table;  //!< widget data table
     QBrush brushes[4];     //!< red blue black gray
     QAction * actNew;
     QAction * actOpen;
     QAction * actSave;
     QAction * actData;
     // QAction * actExport;
     QAction * actCollapse;
     QAction * actPlan;
     QAction * actExtended;
     QAction * act3D;
     // QAction * actToggle;
     QAction * actInfo;
     // QAction * actOptions;
     // QAction * actHelp;
     QAction * actQuit;
     QMenu * plan_menu;
     QMenu * ext_menu;
     int plan_cnt;
     int ext_cnt;
     int xsect_cnt;
     int hsect_cnt;
     QString temp_plan_name;  //!< temporary plot scrap name (for ScrapNameWidget)
     QString temp_ext_name;  //!< temporary plot scrap name (for ScrapNameWidget)
     // PlotCanvas * planCanvas;  //!< plot canvases
     PlotList  planCanvases;
     // PlotCanvas * extCanvas;
     PlotList  extCanvases;
     // PlotCanvas * crossCanvas;
     PlotList  crossCanvases;


     PlotCanvas * _3DCanvas;

     Units units;
     ExportType export_type;      //!< 0: th,  1: svx  2: dat
     PTcolors   pt_colors;        //!< pocket topo colors to therion lines and points

     char device[32];      //!< current DistoX device
     bool collapse;        //!< onCollapse action [true: do collapse, false: do un-collapse]
     bool append;          //!< whether download data is appended
     bool smart;           //!< whether data download is "smart"
     int  splay_at;        //!< station of splay shots (1: FROM, 2: TO)
     bool backward;        //!< whether shots are backwards
     bool download;        //!< whether to download the data

     int comment_size;     //!< width of comment displayed in the table

   private:
     void createActions();
     void createToolBar();
     void createTable();

     /** turn button "export" on/off and set the icon
      * @param on_off   whether to turn button on or off
      *
     void onOffButtonExport( bool on_off )
     {
       if ( on_off ) {
         switch ( export_type ) {
         case EXPORT_THERION: 
           actExport->setIcon( icon->ExportTh() );
           break;
         case EXPORT_COMPASS:
           actExport->setIcon( icon->ExportDat() );
           break;
         case EXPORT_SURVEX:
           actExport->setIcon( icon->ExportSvx() );
           break;
         case EXPORT_POCKETTOPO:
           actExport->setIcon( icon->ExportTop() );
           break;
         case EXPORT_UNKNOWN:
           break;
         }
       } else {
         switch ( export_type ) {
         case EXPORT_THERION: 
           actExport->setIcon( icon->ExportThOff() );
           break;
         case EXPORT_COMPASS:
           actExport->setIcon( icon->ExportDatOff() );
           break;
         case EXPORT_SURVEX:
           actExport->setIcon( icon->ExportSvxOff() );
           break;
         case EXPORT_POCKETTOPO:
           actExport->setIcon( icon->ExportTopOff() );
           break;
         case EXPORT_UNKNOWN:
           break;
         }
       }
       actExport->setEnabled( on_off );
     }
      */

     /** turn buttons on/off
      * @param on_off   whether tu turn buttons on or off
      */
     void onOffButtons( bool on_off )
     {
       // fprintf(stderr, "onOffButtons() %s \n", on_off ? "true" : "false" );
       if ( on_off ) {
         actNew->setIcon( icon->New() );
         actSave->setIcon( icon->Save() );
         actCollapse->setIcon( icon->Collapse() );
         actPlan->setIcon( icon->Plan() );
         actExtended->setIcon( icon->Extended() );
         act3D->setIcon( icon->_3d() );
       } else {
         actNew->setIcon( icon->NewOff() );
         actSave->setIcon( icon->SaveOff() );
         actCollapse->setIcon( icon->CollapseOff() );
         actPlan->setIcon( icon->PlanOff() );
         actExtended->setIcon( icon->ExtendedOff() );
         act3D->setIcon( icon->_3dOff() );
       }
       actNew->setEnabled( on_off );
       actSave->setEnabled( on_off ); // setCheckable( on_off )
       actCollapse->setEnabled( on_off );
       actPlan->setEnabled( on_off );
       actExtended->setEnabled( on_off );
       act3D->setEnabled( on_off );

       // onOffButtonExport( on_off );
     }

   public:
     void getSurveyText( std::ostringstream & oss );

     /*
     void setPlotName( const char * name, int mode ) 
     {
       if ( mode == MODE_PLAN ) {
         temp_plan_name = name;
       } else if ( mode == MODE_EXT ) {
         temp_ext_name = name;
       }
     }
     */

     const char * getPlotName( int mode ) const 
     { 
       if ( mode == MODE_PLAN ) {
         return temp_plan_name.TO_CHAR();
       } else if ( mode == MODE_EXT ) {
         return temp_ext_name.TO_CHAR();
       } else if ( mode == MODE_3D ) {
         return "3D plot";
       }
       return "Cross section";
     }

     /** close all plots
      */
     void closePlots();
    
     void insertPoint( int x, int y, Therion::PointType type, PlotCanvas * canvas );
     void insertLinePoint( int x, int y, Therion::LineType type, PlotCanvas * canvas );

     /** callback: PlotDrawer callback to open a new plot+scrap
      * @param mode   plot mode
      * @param pname  plot name
      * @param sname  scrap name
      */
     PlotCanvas * openPlot( int mode, const char * pname, const char * sname );

     const PTcolors & getColors() const { return pt_colors; }

     // ---------------------------------------------------

     /** set the pointer to the extended section plot
      * @param canvas  extended section plot
      */
     // void setPlanCanvas( PlotCanvas * canvas = NULL ) { planCanvas = canvas; }
     // void setExtCanvas( PlotCanvas * canvas = NULL ) { extCanvas = canvas; }
     // void addPlanCanvas( const char * name, PlotCanvas * c ) 
     // { 
     //   planCanvases.addPlot( name, c ); 
     // }

     void addExtCanvas( const char * name, PlotCanvas * c ) 
     {
       extCanvases.addPlot( name, c ); 
     }

     void removePlotCanvas( PlotCanvas * c );


     // void setCrossCanvas( PlotCanvas * canvas = NULL ) { crossCanvas = canvas; }
     void set3DCanvas( PlotCanvas * canvas = NULL ) { _3DCanvas = canvas; }

     /** update extended plot when the extend of a shot is changed
      * @param b    shot (block)
      */
     void updateExtCanvases( DBlock * b );

     void updateCanvases();

     void showData();

#ifdef HAS_LRUD
     /** check whether to do LRUD or not
      * @return the LRUD flag
      */
     bool DoLRUD() const { return do_lrud; }
#endif

     /*
     void showCanvas( int mode, DBlock * block = NULL, bool reversed = false );
      */

     /** distox callbacks
      */
     void distoxReset();
     void distoxDownload( size_t nr );
     void distoxDone();

    /** accessor: get the survey info
     * @return pointer to the survey info 
     */
    SurveyInfo * getSurveyInfo() { return &(info.surveyInfo); }

    /** fill the date string
     * @param date   date string (output)
     */
    void getDate( char * date ) {
      Locale::ToDate( date, info.year, info.month, info.day );
    }

    /** get the centerline description
     * @return centerline description
     */
    const char * getDescription() { return info.description.c_str(); }
 
    /** set centerline date and description
     * @param date         date
     * @param description  description
     * @return true if ok, false if date is illegal
     */
    bool setDateAndDescription( const char * date, 
                                const char * description,
                                const char * author = NULL,
                                const char * copyright = NULL )
    {
      int maxd[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
      int y,m,d;
      Locale::FromDate( date, y, m, d );
      bool leap = ((y%4)==0) && ( ((y%100)!=0) || ((y%400)==0) );
      if ( m >= 1 && m <= 12 && d>=1 && (d<=maxd[m] || (leap && m==2 && d<=29)) ) {
        info.year  = y;
        info.month = m;
        info.day   = d;
        info.description = description;
        if ( author ) info.author = author;
        if ( copyright ) info.copyright = copyright;
      } else {
        std::ostringstream oss;
        oss << lexicon( "illegal_year" ) << " " << y << " " 
            << lexicon( "month" ) << " " << m << " "
            << lexicon( "day" ) << " " << d;
        QMessageBox::warning(this, lexicon("illegal_date"), oss.str().c_str() );
        return false;
      }
      return true;
    }
      

    /** accessor: get the survey data
     * @return pointer to the survey data
     */
    DataList * getList() { return &dlist; }

    /** ask the data-list to recompute the centerline
     */
    void redoNum() { dlist.doNum( true ); }

    /** drop a block from the list
     * @param block block to drop
     */
    void drop( DBlock * block ) { dlist.dropBlock( block ); }

// ------------------------------------------------------------
// UNITS

    // for Plot
    const Units & getUnits() { return units; }

    int lengthUnits() const { return units.length_units; }
    int angleUnits() const { return units.angle_units; }

    double lengthFactor() const { return units.length_factor; }
    double angleFactor() const { return units.angle_factor; }

    void setLengthUnits( int u ) { units.setLength( u ); }
    void setAngleUnits( int u ) { units.setAngle( u ); }

// -----------------------------------------------------------

    /** get the type of the export
     * @return the export type
     */
    ExportType exportType() const { return export_type; }

    /** set the type of the export
     * @param t  the export type
     */
    void setExportType( ExportType t ) 
    { 
      if ( export_type != t ) {
        export_type = t;
        if ( export_type == EXPORT_THERION ) { // therion
          info.surveyInfo.exportName = "cave.th";
        } else if ( export_type == EXPORT_SURVEX ) { // survex
          info.surveyInfo.exportName = "cave.svx";
        } else if ( export_type == EXPORT_COMPASS ) { // compass
          info.surveyInfo.exportName = "cave.dat";
        } else if ( export_type == EXPORT_POCKETTOPO ) { // compass
          info.surveyInfo.exportName = "cave.top";
        }
        // onOffButtonExport( dlist.listSize() > 0 );
      }
    }

    void downloadData( );

    /** get the DistoX device
     * @return the DistoX device
     */
    const char * getDevice() const { return device; }
 
    /** set the DistoX device
     * @param d  new DistoX device
     */
    void setDevice( const char * d )
    { 
      if ( d ) {
        strncpy( device, d, 32 );
        device[32-1] = 0;
      }
    }

    bool getAppend() const { return append; }
    bool getSmart() const { return smart; }
    bool getBackward() const { return backward; }

    int getSplay() const { return splay_at; }

    /** accessor: set the download device and mode
     * @param f   whether to do download
     * @param d   device
     * @param a   append mode
     * @param s   smart mode
     * @param s1  splay at station "from"
     * @param s2  splay at station "to"
     * @param b   backward shots
     */
    void setDownload( bool f,
                      const char * d, bool a, bool s, bool s1, bool s2, bool b )
    {
      download = f;
      assert( s1 == ! s2 );
      setDevice( d );
      append = a;
      smart = s;
      splay_at = (s1) ? SPLAY_AFTER_SHOT : SPLAY_BEFORE_SHOT ;
      backward = b;
    }

    /** set the base block (start block for plots)
     * @param b new base block
     */
    void setBaseBlock( DBlock * b );

    /** get distoX modes
     * @param calib  whether distox is in calibration mode
     * @param silent whether distox is in silent mode
     * @param grad   whether distox is in grad (as opposed to degrees)
     * @param compass whether distox is in compass/clino mode
     * return true if ok, false if failed to get distox modes
     */
    bool getDistoModes( bool & calib, bool & silent,
                        bool & grad, bool & compass );

    bool setCalibMode( bool on );

    bool setSilentMode( bool on );

    bool setGradMode( bool on );

    bool setCompassMode( bool on );

  public:
    /** callback: on File->Open
     * @param file   filename
     */
    void onOpenFile( const QString & file );

    /** callback: on File->Save
     * @param file   filename
     */
    void onSaveFile( const QString & file );

    /** start the Insert Block dialog
     * @param block block after which to insert the new block
     */
    void doInsertBlock( DBlock * block );
   
    /** do the actual block insertion
     * @param block block after which to insert the new block
     * @param d    distance
     * @param b    compass
     * @param c    clino
     * @param before if true insert before the block
     */
    void doInsertBlock( DBlock * block, const char * d, const char * b, const char * c, bool before);

#ifdef HAS_LRUD
    void doInsertLRUD( DBlock * block, 
                       const QString & L, const QString & R, 
                       const QString & U, const QString & D,
                       bool at_from );
#endif
       
    /** exit for real
     */
    void doRealExit();

    /** clear centerline data and table for real
     */
    void doRealNew();

    /** create a new plot
     * @param pname  plot name
     * @param sname  scrap name
     * @param mode   plot mode (MODE_PLAN or MODE_EXT)
     */
    void doNewPlot( QString pname, QString sname, int mode );

  public slots:
    void doHelp();

    void doQuit();

    /** load data from file
     */
    void doOpen();

    /** clear data list
     */
    void doNew();

    /** save data to file
     */
    void doSave();

    /** get the data from distox
     */
    void doData();

    /** export data as Therion/Survex/Compass
     */
    void doExport( QAction * action);

    /** export data 
     */
    void doExportOK();

    /** show survey info's
     */
    void doInfo();

    /** show / modify run-time options
     */
    void doOptions();

    /** show / modify distox modes
     */
    void doToggle();

    /** collapse splay rows
     */
    void doCollapse();

    /** show plot of the plan
     */
    void doPlanScrap();
    void doPlan( QAction * );

    /** show plot of the extended section
     */
    void doExtendedScrap();
    void doExtended( QAction * );

    /** show 3D view
     */
    void do3D();

    /** show plot of a cross-section
     * @param block    shot where the cross-section is taken
     * @param name     cross section name
     * @param reversed whether the cross-section is taken reversed
     * @param vertical whether the cross-section is taken vertically
     */
    void doCrossSection( DBlock * block, QString name, bool reversed, bool vertical = true );

    /** update datas
     * @param row    row index
     * @param col    column index
     */
    void value_changed( int row, int col );

    /** table double clicked
     */
    void double_clicked( int row, int col );

    /** table clicked
     *
    void clicked( int row, int col, int btn, const QPoint & mousePos ); 
     */

    void updateActData( int c );

  signals:
    void signalActData( int c );

};

// --------------------------------------------------------------------
// small widgets


/** initial splash dialog: help new users to do one of the two:
 * - load a survey from file
 * - download the survey from the distox
 */
class SplashWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;

  public:
    SplashWidget( QTshotWidget * my_parent );

  public slots:
    void doOpen()
    {
      hide();
      parent->doOpen();
    }

    void doData() 
    {
      hide();
      parent->doData();
    }

    void doCancel() 
    { 
      hide();
    }
};

#ifdef HAS_LRUD
class LRUDWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    DBlock * blk;
    QLineEdit * L1;
    QLineEdit * R1;
    QLineEdit * U1;
    QLineEdit * D1;
    QLineEdit * L2;
    QLineEdit * R2;
    QLineEdit * U2;
    QLineEdit * D2;

  public:
    LRUDWidget( QTshotWidget * p, DBlock * b );

  public slots:
    void doOK();
    void doCancel() 
    {
      hide();
   }
};
#endif

/** dialog for distoX modes
 */
class ToggleWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    bool isCalib;
    bool isSilent;
    bool isGrad;
    bool isCompass;
    QCheckBox * calibBtn;
    QCheckBox * silentBtn;
    QCheckBox * gradBtn;
    QCheckBox * compassBtn;

  public:
    ToggleWidget( QTshotWidget * parent );

  public slots:
    void doCalib(int state);
    void doSilent(int state);
    void doGrad(int state);
    void doCompass(int state);

    void doClose()
    {
      hide();
    }

};

/** dialog for the run-time options
 */
class OptionsWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QRadioButton * length_btn[2];
    QRadioButton * angle_btn[2];
    // QRadioButton * export_btn[4];
    QLineEdit    * m_device;

  public:
    OptionsWidget( QTshotWidget * my_parent );

    void SetValues();

  public slots:
    void doOK();
    void doCancel() { hide(); }
};

/** dialog to insert a new block
 */
class InsertWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    DBlock * block;
    QLineEdit * distance;
    QLineEdit * compass;
    QLineEdit * clino;
    QCheckBox * before;

  public:
    InsertWidget( QTshotWidget * p, DBlock * blk );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};

/** dialog to confirm exit action
 */
class ExitWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;

  public:
    ExitWidget( QTshotWidget * p);

  public slots:
    void doOK() { hide(); parent->doRealExit(); }
    void doCancel() { hide(); }
};

/** dialog for the survey info
 */
class InfoWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;

  public:
    InfoWidget( QTshotWidget * my_parent );

  public slots:
    void doOK() { hide(); }
};


/** dialog for the data download
 */
class DataWidget : public QDialog
{ 
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * date;
    QLineEdit * description;
    QLineEdit * device;
    QCheckBox * append;
    QCheckBox * smart;
    QCheckBox * splay1;
    QCheckBox * splay2;
    QCheckBox * backward;

  public:
    DataWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { hide(); }
    void doSplay1( bool );
    void doSplay2( bool );
};

/** dialog for a shot properties
 */
class CommentWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * comment;
    QRadioButton * properties;
    #ifdef HAS_LRUD
      QRadioButton * lrud;         //!< LRUD option
    #endif
    QRadioButton * renumber;
    QRadioButton * tomerge;
    QRadioButton * base_station;
    QRadioButton * tosplit;
    QRadioButton * toinsert;
    QRadioButton * todrop;
    QRadioButton * cross_section;
    QLineEdit * section_name;
    QCheckBox * reversed;
    QCheckBox * horizontal;
    // QCheckBox * swapBox;
    QComboBox * extBox;
    QComboBox * flagBox;
    DBlock * block;

  public:
    /** 
     * @param do_lrud whether to show the LRUD checkbox
     */ 
    #ifdef HAS_LRUD
      CommentWidget( QTshotWidget * my_parent, DBlock * b, bool do_lrud );
    #else
      CommentWidget( QTshotWidget * my_parent, DBlock * b );
    #endif

    void SetValues(); // TODO

  public slots:
    void doOK();
    void doCancel() { hide(); }

    /** handle comment text changes
     * @param text commenttext
     */
    void doComment( const QString & text );

    /** handle extend selection
     * @param extend  value of the selection
     */
    void doExtend( int extend );

    /** handle flag selection
     * @param extend  value of the selection
     */
    void doFlag( int flag );

    // void doSwap( bool );

};

/** dialog for the survey infos
 */
class SurveyInfoWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * edit_name;            //!< survey name
    QLineEdit * edit_title;           //!< survey title
    QLineEdit * edit_team;            //!< team(s) string
    QLineEdit * edit_prefix;          //!< compass station prefix
    QLineEdit * edit_declination;     //!< magnetic declination
    QCheckBox * box_single_survey;    //!< single survey in compass
    QTextEdit * centerline;
    QTextEdit * survey;
    QCheckBox * box_thconfig;         //!< write thconfig

  public:
    SurveyInfoWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};

/** dialog for the survey clean
 */
class CleanShotsWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;

  public:
    CleanShotsWidget( QTshotWidget * my_parent );

  public slots:
    void doOK() { hide(); parent->doRealNew(); }
    void doCancel() { hide(); }
};

/** dialog for centerline info
 */
class CenterlineWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * date;
    QLineEdit * author;
    QLineEdit * copyright;
    QLineEdit * descr;

  public:
    CenterlineWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};

/** dialog to get the scrap name, input by the user
 */
class ScrapNameWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * pname;      //!< plot name
    QLineEdit * sname;      //!< scrap name
    int plot_type;          //!< plot/scrap type

  public:
    ScrapNameWidget( QTshotWidget * p, QString & name, int plot_type );

  public slots:
    void doOK();
    void doCancel() 
    { 
      hide();
    }
};

#endif // TL_DATA_H
