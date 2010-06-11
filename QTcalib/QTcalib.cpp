/** @file QTcalib.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief QTopo calibration for OPIE
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#include <stdio.h>
#include <math.h>
#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>

#include <sstream>

// #include <qtoolbar.h>
#include <qtoolbutton.h>
#include <qpixmap.h>
#include <qmessagebox.h>
#include <qdialog.h>
#include <qfiledialog.h>
#include <qinputdialog.h>
#include <qframe.h>
#include <qimage.h>
#include <qthread.h>
#include <qtimer.h>
// #include <qwhatsthis.h>

// #include "driver.h"

#if defined WIN32
  #define QTOPO_RC "C:\\Program Files\\qtopo\\qtopo.rc"
#elif defined ARM
  #define QTOPO_RC "/opt/QtPalmtop/etc/qtopo.rc"
#else
  #define QTOPO_RC "/usr/share/qtopo/qtopo.rc"
#endif

#include "DistoX.h"
#include "Calibration.h"
#include "QTcalib.h"
#include "Locale.h"
#include "GetDate.h"

// #define QT_NO_FILEDIALOG

#ifndef EMBEDDED
  #include <qapplication.h>
  #define QAPPLICATION QApplication
#else
  #include <qpe/qpeapplication.h>
  #define QAPPLICATION QPEApplication
#endif


bool do_debug = false;



CoeffWidget::CoeffWidget( QTcalibWidget * p, double * c, const QString & comment )
  : QDialog( p, "CoeffWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopoc_coeff") );

  QString coeff[24];
  for ( int k=0; k<24; ++k) coeff[k] = Locale::ToString( c[k] );

  QGridLayout * gl = new QGridLayout( this );
  // gl->setHorizontalSpacing( 20 ); 
  // gl->setverticalSpacing( 10 ); 
  gl->setColSpacing(0, 80);
  gl->setColSpacing(1, 80);
  gl->setColSpacing(2, 80);
  gl->setColSpacing(3, 80);
  gl->addWidget( new QLabel( "BG  ", this ), 0, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[0], this ), 0, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[4], this ), 0, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[8], this ), 0, 3, Qt::AlignRight );
  gl->setRowSpacing(0, 30);
  
  gl->addWidget( new QLabel( "AGx ", this ), 1, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[1], this ), 1, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[2], this ), 1, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[3], this ), 1, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  y ", this ), 2, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[5], this ), 2, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[6], this ), 2, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[7], this ), 2, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  z ", this ), 3, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[ 9], this ), 3, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[10], this ), 3, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[11], this ), 3, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "BM  ", this ), 4, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[12], this ), 4, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[16], this ), 4, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[20], this ), 4, 3, Qt::AlignRight );
  gl->setRowSpacing(4, 30);
  
  gl->addWidget( new QLabel( "AMx ", this ), 5, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[13], this ), 5, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[14], this ), 5, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[15], this ), 5, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  y ", this ), 6, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[17], this ), 6, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[18], this ), 6, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[19], this ), 6, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  z ", this ), 7, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[21], this ), 7, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[22], this ), 7, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[23], this ), 7, 3, Qt::AlignRight );

  if ( ! comment.isEmpty() ) {
    gl->addMultiCellWidget( new QLabel( comment, this ), 8, 9, 0, 4, Qt::AlignLeft );
    QPushButton * c1 = new QPushButton( tr("OK"), this );
    connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
    gl->addWidget( c1, 10, 0 );
  } else {
    QPushButton * c1 = new QPushButton( tr("OK"), this );
    connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
    gl->addWidget( c1, 8, 0 );
  }
  exec();
}


QTcalibWidget::QTcalibWidget( Config & cfg, QWidget * parent, const char * name, WFLAGS fl )
  : QMAINWINDOW( parent, name, fl )
  , config( cfg )
  , lexicon( Language::Get() )
  , icon( IconSet::Get() )
  , fileName( config("DEFAULT_COEFF") )
  , device ( config("DEVICE") )
  , data_table( NULL )
  , guessing( true )
  , guess_on_old( false )
  // , guess_angle( 20 )
{
  setCaption( lexicon("qtopoc_calib") );
  setIcon( icon->QTcalib() );

  const char * geometry = config("GEOMETRY");
  int w = WIDGET_WIDTH;
  int h = WIDGET_HEIGHT;
  if ( sscanf( geometry, "%dx%d", &w, &h ) != 2 ) {
    w = WIDGET_WIDTH;
    h = WIDGET_HEIGHT;
  } 
  resize(w, h);

  guess_angle = atoi( config("CALIB_GUESS") );

  // QMenuBar * menubar = this->menuBar();
  QTOOLBAR * toolbar = new QTOOLBAR( this );

  btnNew =
    new QTOOLBUTTON( icon->NewOff(), lexicon("clear"), QString::null,
                     this, SLOT(doNew()), toolbar, lexicon("clear") );
  // QTOOLBUTTON * _open = 
    new QTOOLBUTTON( icon->Open(), lexicon("open"), QString::null,
                     this, SLOT(doOpen()), toolbar, lexicon("open") );
  btnSave =
    new QTOOLBUTTON( icon->SaveOff(), lexicon("save"), QString::null,
                     this, SLOT(doSave()), toolbar, lexicon("save") );
  btnData = 
    new QTOOLBUTTON( icon->Data(), lexicon("download"), QString::null,
                     this, SLOT(doData()), toolbar, lexicon("download") );
  btnEval =
    new QTOOLBUTTON( icon->EvalOff(), lexicon("eval"), QString::null,
                     this, SLOT(doEval()), toolbar, lexicon("eval") );
  btnCover =
    new QTOOLBUTTON( icon->CoverOff(), lexicon("cover"), QString::null,
                     this, SLOT(doCoverage()), toolbar, lexicon("cover") );
  btnComment =
    new QTOOLBUTTON( icon->CommentOff(), lexicon("description"), QString::null,
                     this, SLOT(doComment()), toolbar, lexicon("description") );
    new QTOOLBUTTON( icon->Toggle(), lexicon("toggle"), QString::null,
                     this, SLOT(doToggle()), toolbar, lexicon("toggle") );
    new QTOOLBUTTON( icon->Read(), lexicon("read"), QString::null,
                     this, SLOT(doRead()), toolbar, lexicon("read") );
    new QTOOLBUTTON( icon->Write(), lexicon("write"), QString::null,
                     this, SLOT(doWrite()), toolbar, lexicon("write") );

    new QTOOLBUTTON( icon->Help(), lexicon("help"), QString::null,
                     this, SLOT(doHelp()), toolbar, lexicon("help") );
    new QTOOLBUTTON( icon->Quit(), lexicon("exit"), QString::null,
                     this, SLOT(doQuit()), toolbar, lexicon("exit") );


  onOffButtons( false );

/*
  (void)QWhatsThis::whatsThisButton( toolbar );
  QWhatsThis::add( _open, "open" );
  QWhatsThis::add( _save, "save" );
  QWhatsThis::add( _data, "data" );
  QWhatsThis::add( _eval, "eval" );
  QWhatsThis::add( _toggle, "toggle" );
  QWhatsThis::add( _read, "read" );
  QWhatsThis::add( _write, "write" );
  QWhatsThis::add( _quit, "quit" );
*/

  // CWidget * cwidget = new CWidget( this );
  // setCentralWidget( cwidget );
}

void
QTcalibWidget::distoxReset()
{
  printf("QTshotWidget::distoxReset()\n");
  btnData->setPixmap( icon->Data3() );
}

void
QTcalibWidget::distoxDownload( size_t nr )
{
  printf("QTshotWidget::distoxDownload() %d\n", nr );
  if ( ( nr % 2 ) == 1 ) {
    btnData->setPixmap( icon->Data4() );
  } else {
    btnData->setPixmap( icon->Data3() );
  }
}

void
QTcalibWidget::distoxDone()
{
  printf("QTshotWidget::distoxDone()\n");
  btnData->setPixmap( icon->Data() );
  onOffButtons( clist.size > 0 );
}

// --------------------------------------------------------------
// calibration table

CalibTable::CalibTable( int rows, int cols, QWidget * parent )
  : QTABLE( rows, cols, parent )
{
}

void
CalibTable::cell_clicked( int r, int c, int button, const QPoint & )
{
  if ( c == 4 ) {
    if ( do_debug ) 
      fprintf(stderr, "cell_clicked %d %d button %d \n", r, c, button );
  
    unsigned int p = 0;
    CBlock * b = NULL;
    if ( sscanf(this->text( r, 5 ).latin1(), "%x", &p) == 1 ) {
      // printf("block %d %08x %s\n", r, p, data_table->text( r, 5 ).latin1() );
      b = (CBlock *)p;
      this->setText( r, 4, (b->ignore = 1 - b->ignore)? "v" : " " );
    }
  }
}

void 
CalibTable::header_clicked( int section )
{
  if ( do_debug ) 
    fprintf(stderr, "header clicked: section %d\n", section );
  if ( section == 0 ) {
    sortColumn( 0, TRUE, TRUE );
  } else if ( section == 3 ) {
    sortColumn( 3, FALSE, TRUE );
  }
}

// ----------------------------------------------------------------------

void
QTcalibWidget::value_changed( int r, int c )
{
  if ( do_debug ) 
    fprintf(stderr, "QTcalibWidget::value_changed row %d col %d \n", r, c );
  unsigned int p = 0;
  CBlock * b = NULL;
  if ( c == 0 ) {
    if ( sscanf(data_table->text( r, 5 ).latin1(), "%x", &p) == 1 ) {
      // printf("block %d %08x %s\n", r, p, data_table->text( r, 5 ).latin1() );
      b = (CBlock *)p;
      b->group = data_table->text(r,c).latin1();
    // } else if ( c == 4 ) {
    //   sscanf(data_table->text( r, 5 ).latin1(), "%x", &p);
    //   // printf("block %d %08x %s\n", r, p, data_table->text( r, 5 ).latin1() );
    //   b = (CBlock *)p;
    //   data_table->setText( r, 4, (b->ignore = 1 - b->ignore)? "v" : " " );
    }
  }
}

void
QTcalibWidget::showData( )
{
  if ( do_debug ) 
    fprintf(stderr, "showData() rows %d \n", clist.size);

  if ( data_table == NULL ) {
    data_table = new CalibTable( clist.size, 6, this);
    // data_table->hideColumn(5);
    QHEADER * header = data_table->horizontalHeader ();
    header->setLabel( 0, lexicon("set"),     STATION_WIDTH );
    header->setLabel( 1, lexicon("azimuth"), DATA_WIDTH );
    header->setLabel( 2, lexicon("clino"),   DATA_WIDTH );
    header->setLabel( 3, lexicon("error"),   DATA_WIDTH );
    header->setLabel( 4, lexicon("skip"),    FLAG_WIDTH );
    header->setClickEnabled( TRUE, 0 );
    header->setClickEnabled( TRUE, 4 );
    data_table->show();
    data_table->hideColumn( 5 );
    setCentralWidget( data_table );
    connect( header, SIGNAL(clicked(int)), data_table, SLOT(header_clicked(int)) );
    connect( data_table, SIGNAL(clicked(int,int,int,const QPoint &)), 
             data_table, SLOT(cell_clicked(int,int,int,const QPoint &)) );
    connect( data_table, SIGNAL(valueChanged(int, int)), this, SLOT(value_changed(int,int)) );
    // data_table->setSorting( TRUE );
  } else {
    data_table->setNumRows( clist.size );
  }

  if ( do_debug )
    fprintf(stderr, "showData() prepared data_table\n");
  int row = 0;
#if 0 // QT_VERSION >= 0x040200
  const char * prev_group = NULL;
  QString color_black( "color: black" );
  QString color_red  ( "color: red" );
  QString color_blue ( "color: blue" );
  QString * color = & color_black;
  int grp = 0;
#endif
  for (CBlock * b = clist.head; b != NULL; b=b->next ) {
    if ( do_debug ) {
      fprintf(stderr, "showData() row %d block %p \n", row, (void*)b );
      b->dump();
    }
    QString c1 = Locale::ToString( b->compass, 2 );
    QString c2 = Locale::ToString( b->clino, 2 );
    QString c3 = Locale::ToString( b->error, 4 );
    QString c4;
#if 0 // QT_VERSION >= 0x040200
    if ( b->group == "-1" ) {
      color = &color_black;
      prev_group = NULL;
    } else if ( ! prev_group || strcmp( prev_group, b->group.c_str() ) != 0 ) {
      prev_group = b->group.c_str();
      grp = 1 - grp;
      if ( grp == 0 ) { color = &color_red; }
      else            { color = &color_blue; }
    }
    data_table->cellWidget( row, 0 )->setStyleSheet( *color );
#endif
    data_table->setText( row, 0, b->group.c_str() );
    data_table->setText( row, 1, c1 );
    data_table->setText( row, 2, c2 );
    data_table->setText( row, 3, c3 );
    data_table->setText( row, 4, b->ignore ? "v" : " " );
    unsigned int p = (unsigned int)((void*)b);
    data_table->setText( row, 5, c4.sprintf("0x%08x", p ) );
    // printf("block %d %08x\n", row, p );
    ++ row;
  }
  if ( do_debug )
    fprintf(stderr, "showData() done\n");
}

// -------------------------------------------------------
// DistoX download dialog

DownloadDialog::DownloadDialog( QTcalibWidget * parent )
  : QDialog( parent, "DownloadDialog", true )
  , widget( parent )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("distox_download" ) );

  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);

  QHBOX * hb = new QHBOX(this);
  new QLabel( lexicon("qtopoc_data"), hb );
  hb = new QHBOX(this);
  do_guess = new QCheckBox( lexicon("qtopoc_guess"), hb );
  do_guess->setChecked( parent->isGuessing() );

  hb = new QHBOX(this);
  do_on_old = new QCheckBox( lexicon("qtopoc_guess2"), hb );
  do_on_old->setChecked( parent->isGuessOnOld() );

  hb = new QHBOX(this);
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

// -------------------------------------------------------
// DistoX Write calibration dialog

WriteDialog::WriteDialog( QTcalibWidget * parent )
  : QDialog( parent, "WriteDialog", true )
  , widget( parent )
{
  Config & config = Config::Get();
  Language & lexicon = Language::Get();
  setCaption( lexicon("distox_write" ) );

  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  QHBOX * hb = new QHBOX(this);
  new QLabel( lexicon("distox_write2" ), hb );

  hb = new QHBOX(this);
  backup = new QCheckBox( lexicon("distox_backup"), hb );
  backup->setChecked( FALSE );

  hb = new QHBOX(this);
  const char * backup_ext = config("BACKUP_FILE");
  struct stat file_stat;
  char filename[128];
  int y,m,d;
  int cnt = 0;
  GetDate( &d, &m, &y );
  int st = 0;
  do {
    ++ cnt;
    sprintf(filename, "%04d%02d%02d-%02d.%s", y, m, d, cnt, backup_ext );
    errno = 0;
    st = stat( filename, &file_stat );
    // printf("stat %s = %d errno %d \n", filename, st, errno );
  } while ( st != -1 && errno != ENOENT );
  backup_file = new QLineEdit( filename, hb );

  hb = new QHBOX(this);
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}


void 
WriteDialog::doOK()
{
  if ( backup->isChecked() ) {
    widget->WriteToDistoX( backup_file->text().latin1() );
  } else {
    widget->WriteToDistoX( NULL );
  }
  delete this;
}  

// -----------------------------------------------------------

MyFileDialog::MyFileDialog( QTcalibWidget * parent, const char * caption, int m )
  : QDialog( parent, "MyFileDialog", true )
  , widget( parent )
  , mode( m )
{
  Language & lexicon = Language::Get();
  setCaption ( lexicon( caption ) );

  QVBoxLayout* vbl = new QVBoxLayout(this, 8);
  vbl->setAutoAdd(TRUE);
  QHBOX * hb = new QHBOX(this);
  new QLabel( lexicon("enter_filename"), hb );

  hb = new QHBOX(this);
  line = new QLineEdit( hb );

  hb = new QHBOX(this);
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}

void
MyFileDialog::doOK()
{
  if ( mode == 0 ) {
    widget->doOpenFile( line->text() );
  } else {
    widget->doSaveFile( line->text() );
  }
  delete this;
}  
  

// ---------------------------------------------------------------
// CLEAR
//
void
QTcalibWidget::doNew()
{
  if ( do_debug ) 
    fprintf(stderr, "doNew clear data_table \n");
  clist.clear();
  onOffButtons( false );
  calibration_description = "";
  showData();
}

// ---------------------------------------------------------------
// OPEN A FILE
//
void
QTcalibWidget::doOpen()
{
  if ( do_debug ) 
    fprintf(stderr, "doOpen file %s \n", fileName.latin1() );
#ifdef QT_NO_FILEDIALOG
  new MyFileDialog( this, 0 );
#else
  doOpenFile( QFileDialog::getOpenFileName( fileName, "Textfiles (*.txt)\nRaw (calib*)", this ) );
#endif
}

/** read calibration data from a file
 * either raw format (only data)
 * or TopoLinux format (coeffs and data)
 */
void
QTcalibWidget::doOpenFile( const QString & file )
{
  if ( do_debug ) 
    fprintf(stderr, "doOpen file %s \n", file.latin1() );
  fileName = file;
  if ( ! fileName.isEmpty() ) {
    calibration_description = "";
    clist.load( fileName, calibration_description );
    onOffButtons( clist.size > 0 );
    showData();
  }
}

// ---------------------------------------------------------------
// SAVE TO FILE
//
void
QTcalibWidget::doSave()
{
  if ( do_debug ) 
    fprintf(stderr, "doSave file %s \n", fileName.latin1() );
  if ( clist.size == 0 ) return;
#ifdef QT_NO_FILEDIALOG
  dialog = new MyFileDialog( this, "save_file", 1);
#else
  doSaveFile( QFileDialog::getSaveFileName( fileName, "Textfiles (*.txt)", this ) );
#endif
}

void
QTcalibWidget::doSaveFile( const QString & file )
{
  if ( do_debug ) 
    fprintf(stderr, "doSaveFile file %s \n", file.latin1() );
  fileName = file;
  if ( ! fileName.isEmpty() ) {
    clist.save(fileName, calibration_description);
  }
}

void
QTcalibWidget::doToggle()
{
  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  DistoX disto( device, log );
  int mode = disto.toggleCalib();
  if ( mode == 0 ) {
    QMessageBox::information(this, lexicon("distox_mode"), lexicon("distox_normal") );
  } else if ( mode == 1 ) {
    QMessageBox::information(this, lexicon("distox_mode"), lexicon("distox_calib") );
  } else {
    QMessageBox::warning(this, lexicon("distox_mode"), lexicon("failed_toggle") );
  }
}

void
QTcalibWidget::doData()
{
  new DownloadDialog( this );
}

class DownloadThread : public QThread
{
  public:
    DistoX * disto;
    int status;

    DownloadThread( DistoX * d )
      : disto( d )
      , status( 0 )
    { }

    int getStatus() const { return status; }

    void run();
};

void
DownloadThread::run()
{
  status = ( disto->download() ) ? 1 : -1;
}


void 
QTcalibWidget::downloadData( bool do_guess, bool use_old )
{
  guessing = do_guess;
  guess_on_old = use_old;
  bool do_use_old = guessing && guess_on_old;

  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  unsigned char old_byte[48];
  CTransform transform;

  DistoX disto( device, log );
  disto.setListener( this );

  DownloadThread t( &disto );
  t.start();
  while ( t.getStatus() == 0 ) {
    QTimer timer(this);
    // timer.changeInterval( 100 );
    connect(&timer, SIGNAL(timeout()), this, SLOT(update()) );
    timer.start( 100 );
    repaint(0,0,-1,-1);
  }
  distoxDone();

  if ( t.getStatus() != 1 ) {
    QMessageBox::warning(this, lexicon("qtopoc_calib"),
      lexicon("data_download_failed" ) );
  } else {
    if ( do_use_old ) {
      if ( ! disto.readCoeffs( old_byte ) ) {
        do_use_old = false;
        QMessageBox::warning(this, lexicon("qtopoc_calib"), 
          lexicon("failed_read_default") );
      } else {
        // FIXME setup the CTransform
        transform.setValue( old_byte );
        transform.dump();
      }
    }  
    // sleep(1);
    unsigned int nr = disto.calibrationSize();
    unsigned int nd = disto.measurementSize();
    if ( nd > 0 ) {
      unsigned int id, ib, ic, ir;
      double xd, xb, xc, xr;
      FILE * fpd = fopen( "data.txt", "w" );
      if ( fpd ) {
        std::ostringstream oss;
        oss << lexicon("read_") << " " << nd << " "
            << lexicon("shots_saving") << " \'data.txt\'";
        QMessageBox::warning(this, lexicon("qtopoc_calib"), oss.str().c_str() );
        while ( disto.nextMeasurement( id, ib, ic, ir, xd, xb, xc, xr ) ) {
          fprintf(fpd, "0x%05x 0x%04x 0x%04x 0x%04x ", id, ib, ic, ir );
          fprintf(fpd, "%.2f %.2f %.2f %.2f\n", xd, xb, xc, xr );
        }
        fclose( fpd );
      } else {
        std::ostringstream oss;
        oss << lexicon("read_") << " " << nd << " "
            << lexicon("shots_saving_failed") << " \'data.txt\'";
        QMessageBox::warning(this, lexicon("qtopoc_calib"), oss.str().c_str() );
      }
    }
    if ( nr == 0 ) {
      QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("zero_data") );
    } else {
      unsigned int cnt = 0;
      CBlock * b0 = NULL;
      int16_t gx, gy, gz, mx, my, mz;
      while ( disto.nextCalibration( gx, gy, gz, mx, my, mz ) ) {
        if ( do_use_old ) {
          b0 = clist.addData( b0, gx, gy, gz, mx, my, mz, transform );
        } else {
          b0 = clist.addData( b0, gx, gy, gz, mx, my, mz );
        }
        ++ cnt;
      }
      if ( guessing ) {
        clist.guessGroups( guess_angle );
      }
      assert( cnt == nr/2 ); // FIXME or cnt == nr
      std::ostringstream oss;
      oss << lexicon("read_") << " " << cnt << " " << lexicon("read_data");
      QMessageBox::information( this, lexicon("qtopoc_calib"), oss.str().c_str() );
      showData();
    }
  }

  onOffButtons( clist.size > 0 );
}



void
QTcalibWidget::doRead()
{
  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  DistoX disto( device, log );
  unsigned char * byte = clist.getCoeff();
  if ( ! disto.readCoeffs( byte ) ) {
    QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("failed_read") );
  } else {
    showCoeff( "" );
  }
}

void
QTcalibWidget::showCoeff( const QString & comment )
{
  double c[24];
  clist.getCoeff( c );
  new CoeffWidget( this, c, comment );
}



void
QTcalibWidget::doWrite()
{
  new WriteDialog( this );
}

void
QTcalibWidget::WriteToDistoX( const char * backup_file )
{
  if ( do_debug )
    fprintf(stderr, "WriteToDistoX() backup_file %s\n", backup_file ? backup_file : "NONE" );
 
  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  DistoX disto( device, log );
  bool ok = true;
  if ( backup_file ) {
    unsigned char byte[48];
    if ( ! disto.readCoeffs( byte ) ) {
      QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("failed_read") );
      ok = false;
    } else {
      FILE * fp = fopen(backup_file, "w"); // FIXME
      if ( fp ) {
        for ( size_t k = 0; k<48; ++k ) {
          if ( fprintf(fp, "0x%02x ", byte[k] ) != 5 ) {
            ok = false;
            break;
          }
          if ( ( k % 8 ) == 7 ) {
            if ( fprintf(fp, "\n" ) != 1 ) {
              ok = false;
              break;
            }
          }
        }
        fclose( fp );
        if ( ok ) {
          QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("coeff_backup") );
        } else {
          QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("failed_backup") );
        }
      } else {
        QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("failed_open") );
        ok = false;
      }
    }
  }
  if ( ok ) {
    unsigned char * byte = clist.getCoeff();
    if ( ! disto.writeCoeffs( byte ) ) {
      QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("failed_write") );
    } else {
      QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("write_ok") );
    }
  } else {
    QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("write_no") );
  }
}

void
QTcalibWidget::doEval()
{
  if ( do_debug ) {
    const char * guess_file = config("TEMP_DATA_GUESS");
    fprintf(stderr, "doEval() writing data to file %s\n", guess_file );
    clist.writeData( guess_file );
  }
  if ( clist.size == 0 ) return;
  if ( clist.size < 16 ) {
    QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("too_few_data") );
    return;
  }

  Calibration calib;
  clist.initCalib( calib );
  calib.PrepareOptimize();
  double delta = 0.5;
  int max_it = 500;
  double error = 0.0;
  calib.Optimize(delta, error, max_it);

  if ( do_debug ) {
    const char * coeff_file = config("TEMP_COEFF");
    calib.PrintCalibrationFile( coeff_file );
  }
  // FIXME set output precision
  QString msg = lexicon("delta");
  msg += " ";
  msg += Locale::ToString( delta, 3 );
  msg += "\n";
  msg += lexicon("error");
  msg += " ";
  msg += Locale::ToString( error, 3 );
  msg += "\n";
  msg += lexicon("iterations");
  msg += " ";
  msg += Locale::ToString( max_it );

  unsigned char * byte = clist.getCoeff();
  clist.getErrors( calib );
  calib.GetCoeff( byte );
  showCoeff( msg );
  showData();
}

void
QTcalibWidget::doComment()
{
  if ( do_debug )
    fprintf(stderr, "QTcalibWidget::doComment() \n");
  if ( clist.size == 0 ) return;
  double c[24];
  clist.getCoeff( c );
  new CommentWidget( this, c );
}

void 
QTcalibWidget::doCoverage()
{
  if ( do_debug )
    fprintf(stderr, "doCoverage: list size %d \n", clist.size );
  if ( clist.size == 0 ) return;
  if ( clist.size < 16 ) { 
    QMessageBox::warning(this, lexicon("qtopoc_calib"), lexicon("too_few_data") );
    return;
  }
  coverage.EvaluateCoverage( clist );
  QImage image( coverage.FillImage(), 180, 90, 32, 0, 0, QImage::IgnoreEndian );
  // QRgb * color_table = (QRgb *)malloc( 256 * sizeof(QRgb) );
  // for (int i =0; i<256; ++i) 
  //   color_table[i] = 0xff000000 | (i << 16);
  QPixmap pixmap;
  if ( ! pixmap.convertFromImage( image, 0 /* QPixmap::ColorOnly */ ) ) {
    printf("failed to convert image to pixmap\n");
  } else {

    // QWidget * widget = new QWidget( this );
    #if QT_VERSION >= 0x040000
      QDialog * widget = new QDialog( this, "Coverage", true );
    #else
      QDialog * widget = new QDialog( this, "Coverage", true, WStyle_Title );
    #endif
    widget->resize( 180, 90 );
    widget->setBackgroundPixmap( pixmap );
    // QPainter painter( widget );
    // painter.begin( widget );
    // painter.drawImage( 10, 10, image );
    // painter.drawPixmap( 10, 10, pixmap );
    // painter.end();
    widget->show();
    widget->exec();
    // printf( "done\n");
  }
}

void
QTcalibWidget::doHelp()
{
  pid_t pid;
  if ( (pid = fork() ) == 0 ) { // child
    char * args[3];
    const char * browser = config("BROWSER");
    if ( browser && strlen(browser) > 0 ) {
      args[0] = const_cast<char *>( browser );
    } else {
      args[0] = const_cast<char *>( "/usr/bin/firefox" );
    }
    char path[256];
    sprintf(path, "file://");
    size_t len = strlen( path );
    if ( getcwd( path+len, 256-len ) != NULL ) {
      sprintf(path+strlen(path), "/help/%s/index.htm",config("LANG") );
      args[1] = const_cast<char *>( path );
      args[2] = (char *)NULL;
      if ( do_debug )
        fprintf(stderr, "execv %s %s \n", args[0], args[1] );
      execv( args[0], args );
    } else {
      if ( do_debug )
        fprintf(stderr, "failed getcwd\n");
      exit(0);
    }
  } else { // parent
    /* nothing to do */
  }
}

void
QTcalibWidget::doQuit()
{
  // this->close();
  new ExitWidget( this );
}

ExitWidget::ExitWidget( QTcalibWidget * p )
  : QDialog( p, "ExitWidget", true )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopo_exit") );
  QVBoxLayout* vb = new QVBoxLayout(this, 8);
  vb->setAutoAdd(TRUE);
  QHBOX *hb;
  hb = new QHBOX(this);
  QString label( lexicon("exit_question") );
  new QLabel( label, hb );

  hb = new QHBOX(this);
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );

  exec();
}



int main( int argc, char ** argv )
{
  QAPPLICATION app( argc, argv );

  Config & config = Config::Get();
  char * qtopo_rc = getenv( "QTOPO_RC" );
  if ( qtopo_rc ) {
    if ( ! config.Load( qtopo_rc ) ) {
      // printf("No system-wide config env(\"QTOPO_RC\") \n");
    }
  }
  const char * locale = config("LOCALE");
  if ( locale ) {
    Locale::SetLocale( locale );
  }

  char * home = getenv( "HOME" );
  if ( home ) {
    char * home_rc = (char*)malloc( strlen(home) + 16 );
    sprintf( home_rc, "%s/.qtopo.rc", home );
    if ( ! config.Load( home_rc ) ) {
      // printf("No user config .qtopo.rc \n");
    }
    free( home_rc );
  }
  if ( ! config.Load( "qtopo.rc" ) ) {
    // printf("No local config qtopo.rc \n");
  }

  if ( strcasecmp(config("DEBUG"), "yes") == 0 ) 
    do_debug = true;

  QTcalibWidget widget( config );
  /*
  QPixmap icon;
  if ( icon.load( config("QTCALIB_ICON") ) ) {
    // printf( "loaded icon\n");
    widget.setIcon( icon );
  }
  */
  app.setMainWidget( &widget );
  widget.show();
  return app.exec();
}

CommentWidget::CommentWidget( QTcalibWidget * parent, double * c )
  : QDialog( parent, "CommentWidget", true )
  , widget( parent )
{
  Language & lexicon = Language::Get();
  setCaption( lexicon("qtopoc_comment") );

  QString coeff[24];
  for ( int k=0; k<24; ++k) coeff[k] = Locale::ToString( c[k], 3 );

  QGridLayout * gl = new QGridLayout( this );
  // gl->setHorizontalSpacing( 20 ); 
  // gl->setverticalSpacing( 10 ); 
  gl->setColSpacing(0, 80);
  gl->setColSpacing(1, 80);
  gl->setColSpacing(2, 80);
  gl->setColSpacing(3, 80);
  gl->addWidget( new QLabel( "BG  ", this ), 0, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[0], this ), 0, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[4], this ), 0, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[8], this ), 0, 3, Qt::AlignRight );
  gl->setRowSpacing(0, 30);
  
  gl->addWidget( new QLabel( "AGx ", this ), 1, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[1], this ), 1, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[2], this ), 1, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[3], this ), 1, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  y ", this ), 2, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[5], this ), 2, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[6], this ), 2, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[7], this ), 2, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  z ", this ), 3, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[ 9], this ), 3, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[10], this ), 3, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[11], this ), 3, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "BM  ", this ), 4, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[12], this ), 4, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[16], this ), 4, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[20], this ), 4, 3, Qt::AlignRight );
  gl->setRowSpacing(4, 30);
  
  gl->addWidget( new QLabel( "AMx ", this ), 5, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[13], this ), 5, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[14], this ), 5, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[15], this ), 5, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  y ", this ), 6, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[17], this ), 6, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[18], this ), 6, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[19], this ), 6, 3, Qt::AlignRight );
  
  gl->addWidget( new QLabel( "  z ", this ), 7, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[21], this ), 7, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[22], this ), 7, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[23], this ), 7, 3, Qt::AlignRight );

  gl->addMultiCellWidget( new QLabel( lexicon("edit_descr"), this ), 8,8, 0,3, Qt::AlignLeft );
  description = new QLineEdit( parent->getDescription(), this );
  gl->addMultiCellWidget( description, 9, 9, 0,3, Qt::AlignLeft );
  //
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this);
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  gl->addWidget( c1, 10, 2, Qt::AlignRight );
  gl->addWidget( c2, 10, 3, Qt::AlignRight );

  exec();
}


