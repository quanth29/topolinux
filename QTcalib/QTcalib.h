/** @file QTcalib.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux calibration for OPIE
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef TL_CALIB_H
#define TL_CALIB_H

#include <string>

#include <QMainWindow>
#include <QLabel>
#include <QAction>
#include <QLineEdit>
#include <QCheckBox>
#include <QWidget>
#include <QString>
#include <QDialog>
#include <QTableWidgetItem>

#include "shorthands.h"

#include "CalibList.h"
#include "Coverage.h"
#include "config.h"
#include "Language.h"
#include "IconSet.h"
#include "DistoX.h"

/**
 * calibation data table
 */
class CalibTable : public QTableWidget
{
  Q_OBJECT
  public:
    CalibTable( int rows, int cols, QWidget * parent );

    void updateData( const CalibList & list );

  public slots:
    void cell_clicked(  QTableWidgetItem * cell );

    void header_clicked( int section );
};

/** 
 * main calibration window
 */
class QTcalibWidget : public QMainWindow
                    , public DistoXListener
{  
  Q_OBJECT
  public:
    QTcalibWidget( Config & config, 
              QWidget * parent = 0, 
              WFLAGS fl = 0 );

    // ~QTcalibWidget()

    void distoxReset();
    void distoxDownload( size_t nr );
    void distoxDone();

  private:
    Config & config;
    Language & lexicon;
    IconSet * icon;
    QString fileName;      //!< in/out filename
    std::string device;
    CalibList clist;           //!< calibration data
    CalibTable * data_table;   //!< data table
    CTransform data_transform; //!< data calibration transform
    Coverage  coverage;  
    bool guessing;         //!< whether to perform the group guess when downloading the data
    bool guess_on_old;     //!< use old DistoX values to guess the calib groups
    int guess_angle;       //!< group guess discrepancy angle
    bool is_append;        //!< whether to append new data to previous
    std::string calibration_description; //!< calibration_description string
    bool data_transformed; //!< whether to use transformed compess/clino/roll
    double calib_delta;
    int calib_max_iter;

    QAction * actNew;
    QAction * actOpen;
    QAction * actSave;
    QAction * actData;
    QAction * actEval;
    QAction * actCover;
    QAction * actComment;
    QAction * actToggle;
    QAction * actRead;
    QAction * actWrite;
    QAction * actOptions;
    QAction * actHelp;
    QAction * actQuit;

  private:
    void showData();

    void showCoeff( const QString & comment );

  public:
    /** set the disto rfcomm device
     * @param dev   new device
     */
    void setDevice( const char * dev ) { device = dev; }

    /** get the disto comm device
     * @return the device
     */
    const char * getDevice() const { return device.c_str(); }

    void setDataTransformed( bool t );
    bool getDataTransformed() const { return data_transformed; }

    void setCalibDelta( double d ) { calib_delta = d; }
    double getCalibDelta() const { return calib_delta; }

    void setCalibMaxIter( int iter ) { calib_max_iter = iter; }
    int getCalibMaxIter() const { return calib_max_iter; }

    void setGuessAngle( int angle ) { 
      if ( angle > 0 ) guess_angle = angle;
    }

    int getGuessAngle() const { return guess_angle; }

    bool isAppend() const { return is_append; }
    void setAppend( bool a ) { is_append = a; }

    /** turn buttons on/off
      * @param on_off   whether tu turn buttons on or off
      */
     void onOffButtons( bool on_off )
     {
       // fprintf(stderr, "onOffButtons() %s \n", on_off ? "true" : "false" );
       if ( on_off ) {
         actNew->setIcon( icon->New() );
         actSave->setIcon( icon->Save() );
         actEval->setIcon( icon->Eval() );
         actCover->setIcon( icon->Cover() );
         actComment->setIcon( icon->Comment() );
       } else {
         actNew->setIcon( icon->NewOff() );
         actSave->setIcon( icon->SaveOff() );
         actEval->setIcon( icon->EvalOff() );
         actCover->setIcon( icon->CoverOff() );
         actComment->setIcon( icon->CommentOff() );
       }

       actNew->setCheckable( on_off );
       actSave->setCheckable( on_off );
       actEval->setCheckable( on_off );
       actCover->setCheckable( on_off );
       actComment->setCheckable( on_off );
    } 

    void doOpenFile( const QString & file );
    void doSaveFile( const QString & file );

    void WriteToDistoX( const char * backup_file = NULL );

    /** set the calibration_description string
     * @param str   new value of the calibration_description string
     */
    void setDescription( const char * str ) { calibration_description = str; }

    const char * getDescription() const { return calibration_description.c_str(); }

    /** set the value of the guessing flag
     * @param value    new value of the flag
     */
    void setGuessing( bool value ) { guessing = value; }
 
    /** set the guess_on_old flag
     * @param value   new value of the guess_on_old flag
     */
    void setGuessOnOld( bool value ) { guess_on_old = value; }

    bool isGuessing() const { return guessing; }

    bool isGuessOnOld() const { return guess_on_old; }

    /** download the data from the DistoX
     * @param do_guess    whether to guess the groups of data
     * @param use_old     whether to use old DistoX coeff to guess the groups
     * @param do_append   whether to append the new data to the previous in memory
     */
    void downloadData( bool do_guess, bool use_old, bool do_append );

  public slots:
    void doHelp();

    void doQuit();

    void doNew();

    void doOpen();

    void doSave();

    void doEval();

    void doData();

    void doToggle();

    void doRead();

    void doWrite();

    void doCoverage();

    void doComment();

    void doOptions();

    void value_changed( QTableWidgetItem * item );

  private:
    void createActions();
    void createToolbar();

};

class OptionsWidget : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * parent;
    QLineEdit * m_device;  //!< DistoX device
    QLineEdit * m_angle;   //!< guess tolerance angle
    QCheckBox * mb_transf; //!< whether to show transformed data
    QLineEdit * m_delta;
    QLineEdit * m_max_iter;

  public:
    OptionsWidget( QTcalibWidget * my_parent );

  public slots:
    void doOK();

    void doCancel() { hide(); }
};

class CommentWidget : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * widget;
    QLineEdit * description;

  public:
    CommentWidget( QTcalibWidget * parent, double * c );

  public slots:
    void doOK()
    {
      hide();
      widget->setDescription( description->text().TO_CHAR() );
    } 

    void doCancel() { hide(); }
};

class DownloadDialog : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * widget;  //!< parent widget
    QCheckBox * do_guess;    //!< guess checkbox
    QCheckBox * do_on_old;   //!< guess on old values
    QCheckBox * do_append;   //!< whether to append to current calib data

  public:
    DownloadDialog( QTcalibWidget * widget );

  public slots:
    void doOK();

    void doCancel() { hide(); }
};

class WriteDialog : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * widget;  //!< parent widget
    QCheckBox * backup;      //!< backup checkbox
    QLineEdit * backup_file; //!< backup filename 

  public:
    WriteDialog( QTcalibWidget * widget );

  public slots:
    void doOK();
  
    void doCancel() { hide(); }
};

#ifdef QT_NO_FILEDIALOG
class MyFileDialog : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * widget;
    QLineEdit * line;
    int mode;

  public:
    MyFileDialog( QTcalibWidget * widget, const char * caption, int m );

  public slots:
    void doOK();
    void doCancel() { hide(); }
};
#endif


class CoeffWidget : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * parent;
  
  public:
    CoeffWidget( QTcalibWidget * p, double * c, const QString & comment );

  public slots:
    void doOK() { hide(); }
};

/** dialog to confirm exit action
 */
class ExitWidget : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * parent;

  public:
    ExitWidget( QTcalibWidget * p);

  public slots:
    void doOK() { hide(); parent->close(); }
    void doCancel() { hide(); }
};

class CoverageWidget : public QDialog
{
 Q_OBJECT
  private:
    Coverage & coverage;

  public:
    CoverageWidget( QTcalibWidget * p, Coverage & c );

  public slots:
    void doOK() { hide(); }
};


#endif // TL_CALIB_H
