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

#include <qmainwindow.h>
#include <qpushbutton.h>
#include <qlabel.h>
// #include <qaction.h>
#include <qmenubar.h>
#include <qlcdnumber.h>
#include <qlayout.h>
#include <qlineedit.h>
#include <qcheckbox.h>
// #include <qabstractlayout.h>
#include <qwidget.h>
#include <qstring.h>
#include <qdialog.h>

#include "portability2.h"
#include "portability.h"

#include "CalibList.h"
#include "Coverage.h"
#include "config.h"
#include "Language.h"
#include "IconSet.h"
#include "DistoX.h"

/**
 * calibation data table
 */
class CalibTable : public QTABLE
{
  Q_OBJECT
  public:
    CalibTable( int rows, int cols, QWidget * parent );

  public slots:
    void cell_clicked( int row, int col, int button, const QPoint & pos );

    void header_clicked( int section );
};

/** 
 * main calibration window
 */
class QTcalibWidget : public QMAINWINDOW
                    , public DistoXListener
{  
  Q_OBJECT
  public:
    QTcalibWidget( Config & config, 
              QWidget * parent = 0, 
              const char * name = 0,
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
    const char * device;
    CalibList clist;       //!< calibration data
    CalibTable * data_table;  //!< data table
    Coverage  coverage;  
    bool guessing;         //!< whether to perform the group guess when downloading the data
    bool guess_on_old;     //!< use old DistoX values to guess the calib groups
    int guess_angle;       //!< group guess discrepancy angle
    std::string calibration_description; //!< calibration_description string

    QTOOLBUTTON * btnNew;
    QTOOLBUTTON * btnSave;
    QTOOLBUTTON * btnData;
    QTOOLBUTTON * btnEval;
    QTOOLBUTTON * btnCover;
    QTOOLBUTTON * btnComment;

  private:
    void showData();

    void showCoeff( const QString & comment );

  public:
    /** turn buttons on/off
      * @param on_off   whether tu turn buttons on or off
      */
     void onOffButtons( bool on_off )
     {
       // fprintf(stderr, "onOffButtons() %s \n", on_off ? "true" : "false" );
       if ( on_off ) {
         btnNew->setPixmap( icon->New() );
         btnSave->setPixmap( icon->Save() );
         btnEval->setPixmap( icon->Eval() );
         btnCover->setPixmap( icon->Cover() );
         btnComment->setPixmap( icon->Comment() );
       } else {
         btnNew->setPixmap( icon->NewOff() );
         btnSave->setPixmap( icon->SaveOff() );
         btnEval->setPixmap( icon->EvalOff() );
         btnCover->setPixmap( icon->CoverOff() );
         btnComment->setPixmap( icon->CommentOff() );
       }

       btnNew->setToggleButton( on_off );
       btnSave->setToggleButton( on_off );
       btnEval->setToggleButton( on_off );
       btnCover->setToggleButton( on_off );
       btnComment->setToggleButton( on_off );
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
     */
    void downloadData( bool do_guess, bool use_old );

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

    void value_changed( int row, int col );

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
      widget->setDescription( description->text().latin1() );
      delete this;
    } 

    void doCancel() { delete this; }
};

class DownloadDialog : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * widget;     //!< parent widget
    QCheckBox * do_guess;  //!< guess checkbox
    QCheckBox * do_on_old; //!< guess on old values

  public:
    DownloadDialog( QTcalibWidget * widget );

  public slots:
    void doOK()
    {
      widget->downloadData( do_guess->isChecked(), do_on_old->isChecked() );
      delete this;
    }

    void doCancel() { delete this; }
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
  
    void doCancel() { delete this; }
};

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
    void doCancel() { delete this; }
};


class CoeffWidget : public QDialog
{
  Q_OBJECT
  private:
    QTcalibWidget * parent;
  
  public:
    CoeffWidget( QTcalibWidget * p, double * c, const QString & comment );

  public slots:
    void doOK() { delete this; }
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
    void doOK() { hide(); parent->close(); /* delete this; */ }
    void doCancel() { delete this; }
};



#endif // TL_CALIB_H
