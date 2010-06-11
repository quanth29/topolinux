/** @file QTshot.h
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

#include <string>

#include <qglobal.h>
#include <qmainwindow.h>
#include <qpushbutton.h>
#include <qlabel.h>
#include <qthread.h>

#include <qaction.h>

#include <qmenubar.h>
#include <qlcdnumber.h>
#include <qlayout.h>
#include <qlineedit.h>
#include <qcheckbox.h>
#include <qcombobox.h>
// #include <qabstractlayout.h>
#include <qwidget.h>
#include <qpoint.h>
// #include <qradiobutton.h>
#include <qcheckbox.h>
#include <qdialog.h>
#include <qradiobutton.h>

#include "portability.h"


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

class PlotCanvas;
class QTshotWidget;

class QTshotWidget : public QMAINWINDOW
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
     QTABLE * table;        //!< widget data table
     QTOOLBUTTON * btnNew;
     QTOOLBUTTON * btnData;
     QTOOLBUTTON * btnSave;
     QTOOLBUTTON * btnExport;
     QTOOLBUTTON * btnCollapse;
     QTOOLBUTTON * btnPlan;
     QTOOLBUTTON * btnExtended;
     QTOOLBUTTON * btn3D;
     PlotCanvas * planCanvas;  //!< plot canvas
     PlotCanvas * extCanvas;
     PlotCanvas * crossCanvas;
     PlotCanvas * _3DCanvas;


     Units units;
     ExportType export_type;      //!< 0: th,  1: svx  2: dat
     PTcolors   pt_colors;        //!< pocket topo colors to therion lines and points

     char device[32];
     bool collapse;        //!< onCollapse action [true: do collapse, false: do un-collapse]
     bool append;          //!< whether download data is appended
     bool smart;           //!< whether data download is "smart"
     int  splay_at;        //!< station of splay shots (1: FROM, 2: TO)
     bool backward;        //!< whether shots are backwards
     bool download;        //!< whether to download the data

     int comment_size;     //!< width of comment displayed in the table

   private:
     /** turn button "export" on/off and set the icon
      * @param on_off   whether to turn button on or off
      */
     void onOffButtonExport( bool on_off )
     {
       if ( on_off ) {
         switch ( export_type ) {
         case ExportTherion: 
           btnExport->setPixmap( icon->ExportTh() );
           break;
         case ExportCompass:
           btnExport->setPixmap( icon->ExportDat() );
           break;
         case ExportSurvex:
           btnExport->setPixmap( icon->ExportSvx() );
           break;
         case ExportPocketTopo:
           btnExport->setPixmap( icon->ExportTop() );
           break;
         case ExportUnknown:
           break;
         }
       } else {
         switch ( export_type ) {
         case ExportTherion: 
           btnExport->setPixmap( icon->ExportThOff() );
           break;
         case ExportCompass:
           btnExport->setPixmap( icon->ExportDatOff() );
           break;
         case ExportSurvex:
           btnExport->setPixmap( icon->ExportSvxOff() );
           break;
         case ExportPocketTopo:
           btnExport->setPixmap( icon->ExportTopOff() );
           break;
         case ExportUnknown:
           break;
         }
       }
       btnExport->setToggleButton( on_off );
       // btnExport->repaint(0,0, -1,-1);
       // btnExport->show();
     }

     /** turn buttons on/off
      * @param on_off   whether tu turn buttons on or off
      */
     void onOffButtons( bool on_off )
     {
       // fprintf(stderr, "onOffButtons() %s \n", on_off ? "true" : "false" );
       if ( on_off ) {
         btnNew->setPixmap( icon->New() );
         btnSave->setPixmap( icon->Save() );
         btnCollapse->setPixmap( icon->Collapse() );
         btnPlan->setPixmap( icon->Plan() );
         btnExtended->setPixmap( icon->Extended() );
         btn3D->setPixmap( icon->_3d() );
       } else {
         btnNew->setPixmap( icon->NewOff() );
         btnSave->setPixmap( icon->SaveOff() );
         btnCollapse->setPixmap( icon->CollapseOff() );
         btnPlan->setPixmap( icon->PlanOff() );
         btnExtended->setPixmap( icon->ExtendedOff() );
         btn3D->setPixmap( icon->_3dOff() );
       }
       btnNew->setToggleButton( on_off );
       btnSave->setToggleButton( on_off ); // setCheckable( on_off )
       btnCollapse->setToggleButton( on_off );
       btnPlan->setToggleButton( on_off );
       btnExtended->setToggleButton( on_off );
       btn3D->setToggleButton( on_off );

       onOffButtonExport( on_off );
/*
       btnSave->repaint(0,0, -1,-1);
       btnCollapse->repaint(0,0, -1,-1);
       btnPlan->repaint(0,0, -1,-1);
       btnExtended->repaint(0,0, -1,-1);
       btn3D->repaint(0,0, -1,-1);
       btnSave->show();
       btnCollapse->show();
       btnPlan->show();
       btnExtended->show();
       btn3D->show();
*/
     }

   public:
     /** close all plots
      */
     void closePlots();
    
     void insertPoint( int x, int y, ThPointType type, int mode );
     void insertLinePoint( int x, int y, ThLineType type, int mode );
     void openPlot( int mode );

     const PTcolors & colors() const { return pt_colors; }

     // ---------------------------------------------------

     /** set the pointer to the extended section plot
      * @param canvas  extended section plot
      */
     void setPlanCanvas( PlotCanvas * canvas = NULL ) { planCanvas = canvas; }
     void setExtCanvas( PlotCanvas * canvas = NULL ) { extCanvas = canvas; }
     void setCrossCanvas( PlotCanvas * canvas = NULL ) { crossCanvas = canvas; }
     void set3DCanvas( PlotCanvas * canvas = NULL ) { _3DCanvas = canvas; }

     /** update extended plot when the extend of a shot is changed
      * @param b    shot (block)
      */
     void updateExtCanvas( DBlock * b );

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
    SurveyInfo * GetSurveyInfo() { return &(info.surveyInfo); }

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
    bool setDateAndDescription( const char * date, const char * description )
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
      } else {
        printf("illegal date: year %04d month %02d day %02d\n", y, m, d);
        return false;
      }
      return true;
    }
      

    /** accessor: get the survey data
     * @return pointer to the survey data
     */
    DataList * GetList() { return &dlist; }

    /** drop a block from the list
     * @param block block to drop
     */
    void drop( DBlock * block ) { dlist.dropBlock( block ); }

// ------------------------------------------------------------
// UNITS

    // for Plot
    const Units & GetUnits() { return units; }

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
        if ( export_type == ExportTherion ) { // therion
          info.exportName = "/tmp/cave.th";
        } else if ( export_type == ExportSurvex ) { // survex
          info.exportName = "/tmp/cave.svx";
        } else if ( export_type == ExportCompass ) { // compass
          info.exportName = "/tmp/cave.dat";
        }
        onOffButtonExport( dlist.Size() > 0 );
      }
    }

    void downloadData( );

    const char * getDevice() const { return device; }
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
      if ( d ) strncpy(device, d, 31);
      append = a;
      smart = s;
      splay_at = (s1) ? SPLAY_AT_FROM : SPLAY_AT_TO;
      backward = b;
    }

    /** set the base block (start block for plots)
     * @param b new base block
     */
    void SetBaseBlock( DBlock * b );

    /** get distoX modes
     * @param calib whether distox is in calibration mode
     * @param silent whether distox is in silent mode
     * return true if ok, false if failed to get distox modes
     */
    bool GetDistoModes( bool & calib, bool & silent );

    bool SetCalibMode( bool on );

    bool SetSilentMode( bool on );

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
    void doExport();

    /** export data 
     */
    void doExportOK();

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
    void doPlan();

    /** show plot of the extended section
     */
    void doExtended();

    /** show 3D view
     */
    void do3D();

    /** show plot of a cross-section
     * @param block    shot where the cross-section is taken
     * @param reversed whether the cross-section is taken reversed
     * @param vertical whether the cross-section is taken vertically
     */
    void doCrossSection( DBlock * block, bool reversed, bool vertical = true );

    /** update datas
     * @param row    row index
     * @param col    column index
     */
    void value_changed( int row, int col );

    /** table double clicked
     */
    void double_clicked( int row, int col, int btn, const QPoint & mousePos ); 

    /** table clicked
     *
    void clicked( int row, int col, int btn, const QPoint & mousePos ); 
     */

};

// --------------------------------------------------------------------
// small widgets

/** dialog for a filename
 */
class MyFileDialog : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * widget;
    QLineEdit * line;
    int mode;

  public:
    MyFileDialog( QTshotWidget * parent, const char * title, int m );

  public slots:
    void onOK()
    {
      if ( mode == 0 ) {
        widget->onOpenFile( line->text() );
      } else {
        widget->onSaveFile( line->text() );
      }
      delete this;
    }

    void onCancel() { delete this; }

};

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
      // delete this;
    }

    void doData() 
    {
      hide();
      parent->doData();
      // delete this;
    }

    void doCancel() 
    { 
      hide();
      // FIXME this should be here but it segfaults
      // delete this;
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
      // FIXME this should be here but it segfaults
      // delete this;
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
    QCheckBox * calibBtn;
    QCheckBox * silentBtn;

  public:
    ToggleWidget( QTshotWidget * parent );

  public slots:
    void doCalib(int state);

    void doSilent(int state);

    void doClose()
    {
      hide();
      // delete this;
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
    QRadioButton * export_btn[4];

  public:
    OptionsWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { delete this; }
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
    void doCancel() { delete this; }
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
    void doOK() { hide(); parent->doRealExit(); /* delete this; */ }
    void doCancel() { delete this; }
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
    void doCancel() { delete this; }
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
    QCheckBox * reversed;
    QCheckBox * horizontal;
    QCheckBox * swapBox;
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

  public slots:
    void doOK();
    void doCancel() { delete this; }

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

    void doSwap( bool );

};

/** dialog for the survey infos
 */
class SurveyInfoWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * name;            //!< survey name
    QLineEdit * title;           //!< survey title
    QLineEdit * team;            //!< team(s) string
    QLineEdit * prefix;          //!< compass station prefix
    QLineEdit * declination;     //!< magnetic declination
    QCheckBox * single_survey;   //!< single survey in compass
    QMULTILINEEDIT * centerline;
    QMULTILINEEDIT * survey;

  public:
    SurveyInfoWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { delete this; }
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
    void doOK() { parent->doRealNew(); delete this; }
    void doCancel() { delete this; }
};

/** dialog for centerline info
 */
class CenterlineWidget : public QDialog
{
  Q_OBJECT
  private:
    QTshotWidget * parent;
    QLineEdit * date;
    QLineEdit * descr;

  public:
    CenterlineWidget( QTshotWidget * my_parent );

  public slots:
    void doOK();
    void doCancel() { delete this; }
};

#endif // TL_DATA_H
