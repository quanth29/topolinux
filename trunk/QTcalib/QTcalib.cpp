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

#ifdef WIN32
  #include <direct.h>
  #include <stdint.h> // FIXME
  #define chdir _chdir
  #define strcasecmp strcmp
#endif

#include <QThread>
#include <QTimer>

// #include <QDialog>
#include <QMessageBox>
#include <QLabel>
#include <QPushButton>
#include <QFileDialog>
#include <QImage>
#include <QPixmap>
#include <QHeaderView>
#include <QGraphicsPixmapItem>
#include <QGraphicsScene>
#include <QGraphicsView>
#include <QApplication>
#include <QTableWidget>
#include <QToolBar>

#include <QVBoxLayout>
#include <QHBoxLayout>

// #include "driver.h"

#if defined WIN32
  #define QTOPO_RC "C:\\Program Files\\qtopo\\qtopo.rc"
#else
  #define QTOPO_RC "/usr/share/qtopo/qtopo.rc"
#endif

#include "DistoX.h"
#include "Calibration.h"
#include "QTcalib.h"
#include "Locale.h"
#include "GetDate.h"

// #define QT_NO_FILEDIALOG

#define WIDGET_WIDTH   480
#define WIDGET_HEIGHT  600
#define GROUP_WIDTH 40
#define DATA_WIDTH  100
#define FLAG_WIDTH  40
#define ITEM_HEIGHT 20

#define COL_GROUP   0
#define COL_AZIMUTH 1
#define COL_CLINO   2
#define COL_ROLL    3
#define COL_ERROR   4
#define COL_SKIP    5
#define COL_BLOCK   6
#define COL_NR      7

bool do_debug = false;



// -----------------------------------------------------------------

QTcalibWidget::QTcalibWidget( Config & cfg, QWidget * parent, WFLAGS fl )
  : QMainWindow( parent, fl )
  , config( cfg )
  , lexicon( Language::Get() )
  , icon( IconSet::Get() )
  , fileName( config("DEFAULT_COEFF") )
  , device ( config("DEVICE") )
  , data_table( NULL )
  , guessing( true )
  , guess_on_old( false )
  // , guess_angle( 20 )
  // , data_transformed( false )
  // , calib_delta( 0.5 )
  // , calib_max_iter( 500 )
{
  setWindowTitle( lexicon("qtopoc_calib") );
  setWindowIcon( icon->QTcalib() );

  const char * geometry = config("GEOMETRY");
  int w = WIDGET_WIDTH;
  int h = WIDGET_HEIGHT;
  if ( sscanf( geometry, "%dx%d", &w, &h ) != 2 ) {
    w = WIDGET_WIDTH;
    h = WIDGET_HEIGHT;
  } 
  // printf("resize to %d %d \n", w, h );
  resize(w, h);

  guess_angle = atoi( config("CALIB_GUESS") );
  data_transformed = config.isTrue("TRANSFORMED_DATA");
  calib_delta = atof( config("CALIB_DELTA") );
  calib_max_iter = atoi( config("CALIB_MAX_ITER") );

  createActions();
  createToolbar();

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
QTcalibWidget::createToolbar()
{
  QToolBar * toolbar = addToolBar( tr("") );
  setToolButtonStyle( Qt::ToolButtonIconOnly ); // this is the default

  toolbar->addAction( actNew );
  toolbar->addAction( actOpen );
  toolbar->addAction( actSave );
  toolbar->addAction( actData );
  toolbar->addAction( actEval );
  toolbar->addAction( actCover );
  toolbar->addAction( actComment );
  toolbar->addAction( actToggle );
  toolbar->addAction( actRead );
  toolbar->addAction( actWrite );
  toolbar->addAction( actOptions );
  toolbar->addAction( actHelp );
  toolbar->addAction( actQuit );
}

void 
QTcalibWidget::createActions()
{
  actNew      = new QAction( icon->NewOff(), lexicon("clear"), this);
  actOpen     = new QAction( icon->Open(), lexicon("open"), this);
  actSave     = new QAction( icon->SaveOff(), lexicon("save"), this);
  actData     = new QAction( icon->Data(), lexicon("download"), this);
  actEval     = new QAction( icon->EvalOff(), lexicon("eval"), this);
  actCover    = new QAction( icon->CoverOff(), lexicon("cover"), this);
  actComment  = new QAction( icon->CommentOff(), lexicon("description"), this);
  actToggle   = new QAction( icon->Toggle(), lexicon("toggle"), this);
  actRead     = new QAction( icon->Read(), lexicon("read"), this);
  actWrite    = new QAction( icon->Write(), lexicon("write"), this);
  actOptions  = new QAction( icon->Options(), lexicon("options"), this );
  actHelp     = new QAction( icon->Help(), lexicon("help"), this);
  actQuit     = new QAction( icon->Quit(), lexicon("exit"), this);

  connect( actNew,     SIGNAL(triggered()), this, SLOT(doNew()) );
  connect( actOpen,    SIGNAL(triggered()), this, SLOT(doOpen()) );
  connect( actSave,    SIGNAL(triggered()), this, SLOT(doSave()) );
  connect( actData,    SIGNAL(triggered()), this, SLOT(doData()) );
  connect( actEval,    SIGNAL(triggered()), this, SLOT(doEval()) );
  connect( actCover,   SIGNAL(triggered()), this, SLOT(doCoverage()) );
  connect( actComment, SIGNAL(triggered()), this, SLOT(doComment()) );
  connect( actToggle,  SIGNAL(triggered()), this, SLOT(doToggle()) );
  connect( actRead,    SIGNAL(triggered()), this, SLOT(doRead()) );
  connect( actWrite,   SIGNAL(triggered()), this, SLOT(doWrite()) );
  connect( actOptions, SIGNAL(triggered()), this, SLOT(doOptions()) );
  connect( actHelp,    SIGNAL(triggered()), this, SLOT(doHelp()) );
  connect( actQuit,    SIGNAL(triggered()), this, SLOT(doQuit()) );

  actNew->setVisible( true );
  actOpen->setVisible( true );
  actSave->setVisible( true );
  actData->setVisible( true );
  actEval->setVisible( true );
  actCover->setVisible( true );
  actComment->setVisible( true );
  actToggle->setVisible( true );
  actRead->setVisible( true );
  actWrite->setVisible( true );
  actOptions->setVisible( true );
  actHelp->setVisible( true );
  actQuit->setVisible( true );

}
    
void 
QTcalibWidget::setDataTransformed( bool t ) 
{
  if ( data_transformed != t ) {
    data_transformed = t;
    if ( data_transformed ) {
      // data_transform.dump();
    }
    clist.computeData( data_transformed ? &data_transform : NULL );
    data_table->updateData( clist );
  }
}

void
QTcalibWidget::doOptions()
{
  OptionsWidget options( this );
}

// -------------------------------------------------------------
// Disto INTERACTION

void
QTcalibWidget::distoxReset()
{
  printf("QTshotWidget::distoxReset()\n");
  actData->setIcon( icon->Data3() );
}

void
QTcalibWidget::distoxDownload( size_t nr )
{
  printf("QTshotWidget::distoxDownload() %d\n", nr );
  if ( ( nr % 2 ) == 1 ) {
    actData->setIcon( icon->Data4() );
  } else {
    actData->setIcon( icon->Data3() );
  }
}

void
QTcalibWidget::distoxDone()
{
  printf("QTshotWidget::distoxDone()\n");
  actData->setIcon( icon->Data() );
  onOffButtons( clist.size > 0 );
}

// --------------------------------------------------------------
// calibration table

CalibTable::CalibTable( int rows, int cols, QWidget * parent )
  : QTableWidget( rows, cols, parent )
{
}

void
CalibTable::updateData( const CalibList & list )
{
  int row = 0;
  QString c1, c2, c3;
  for ( CBlock * b = list.head; b; b = b->next, ++row ) {
    c1 = Locale::ToString( b->compass, 2 );
    c2 = Locale::ToString( b->clino, 2 );
    c3 = Locale::ToString( b->roll, 0 );
    item(row, COL_AZIMUTH)->setText( c1 );
    item(row, COL_CLINO)->setText( c2 );
    item(row, COL_ROLL)->setText( c3 );
  }
}

void
CalibTable::cell_clicked(  QTableWidgetItem * cell )
{
  int r = cell -> row();
  int c = cell -> column();
  if ( c == COL_SKIP ) {
    if ( do_debug ) 
      fprintf(stderr, "cell_clicked %d %d \n", r, c );
  
    unsigned int p = 0;
    CBlock * b = NULL;
    if ( sscanf( this -> item(r,COL_BLOCK) -> text().TO_CHAR(), "%x", &p) == 1 ) {
      // printf("block %d %08x %s\n", r, p, data_table->text( r, COL_BLOCK ).TO_CHAR() );
      b = (CBlock *)p;
      this -> item( r,COL_SKIP) -> setText( (b->ignore = 1 - b->ignore)? "v" : " " );
    }
  }
}

void 
CalibTable::header_clicked( int section )
{
  if ( do_debug ) 
    fprintf(stderr, "header clicked: section %d\n", section );
  if ( section == COL_GROUP ) {
    sortItems( COL_GROUP, Qt::AscendingOrder );
  } else if ( section == COL_ERROR ) {
    sortItems( COL_ERROR, Qt::DescendingOrder );
  }
}

// ----------------------------------------------------------------------

void
QTcalibWidget::value_changed( QTableWidgetItem * item )
{
  int r = item->row();
  int c = item->column();
  if ( do_debug ) 
    fprintf(stderr, "QTcalibWidget::value_changed row %d col %d --> %s\n",
      r, c, item->text().TO_CHAR() );
  if ( c == COL_GROUP ) {
    unsigned int p = 0;
    QTableWidgetItem * block_item = data_table->item(r,COL_BLOCK);
    if ( block_item != NULL && sscanf(block_item->text().TO_CHAR(), "%x", &p) == 1 ) {
      // printf("block %d %08x %s\n", r, p, item->text().TO_CHAR() );
      CBlock * b = (CBlock *)p;
      // b->group = data_table->item(r,c)->text().TO_CHAR();
      b->SetGroup( item->text().TO_CHAR() );
      // printf("value_changed() set group %s \n", b->Group() );
    // } else if ( c == COL_SKIP ) {
    //   sscanf(data_table->text( r, COL_BLOCK ).TO_CHAR(), "%x", &p);
    //   // printf("block %d %08x %s\n", r, p, data_table->text( r, COL_BLOCK ).TO_CHAR() );
    //   b = (CBlock *)p;
    //   data_table->setText( r, COL_SKIP, (b->ignore = 1 - b->ignore)? "v" : " " );
    }
  }
}

void
QTcalibWidget::showData( )
{
  if ( do_debug ) 
    fprintf(stderr, "showData() rows %d \n", clist.size);

  if ( data_table != NULL ) {
    delete data_table;
    data_table = NULL;
  }
  data_table = new CalibTable( clist.size, COL_NR, this);
  QTableWidgetItem * header0 = new QTableWidgetItem( lexicon("set") );
  QTableWidgetItem * header1 = new QTableWidgetItem( lexicon("azimuth") );
  QTableWidgetItem * header2 = new QTableWidgetItem( lexicon("clino") );
  QTableWidgetItem * header3 = new QTableWidgetItem( lexicon("roll") );
  QTableWidgetItem * header4 = new QTableWidgetItem( lexicon("error") );
  QTableWidgetItem * header5 = new QTableWidgetItem( lexicon("skip") );
  data_table -> setColumnWidth( COL_GROUP, GROUP_WIDTH );
  data_table -> setColumnWidth( COL_AZIMUTH, DATA_WIDTH );
  data_table -> setColumnWidth( COL_CLINO, DATA_WIDTH );
  data_table -> setColumnWidth( COL_ROLL, DATA_WIDTH );
  data_table -> setColumnWidth( COL_ERROR, DATA_WIDTH );
  data_table -> setColumnWidth( COL_SKIP, FLAG_WIDTH );
  data_table -> setHorizontalHeaderItem(COL_GROUP, header0 );
  data_table -> setHorizontalHeaderItem(COL_AZIMUTH, header1 );
  data_table -> setHorizontalHeaderItem(COL_CLINO, header2 );
  data_table -> setHorizontalHeaderItem(COL_ROLL, header3 );
  data_table -> setHorizontalHeaderItem(COL_ERROR, header4 );
  data_table -> setHorizontalHeaderItem(COL_SKIP, header5 );

  for (int r = 0; r<data_table -> rowCount(); ++r ) {
    #if 0
      for (int c = 0; c < COL_NR; ++c ) {
        QTableWidgetItem * prototype = new QTableWidgetItem( "" );
        data_table -> setItem( r, c, prototype );
      }
    #endif
    data_table -> setRowHeight( r, ITEM_HEIGHT );
  }
  // data_table -> setItemPrototype( data_table->item(0,0) );
    
  data_table -> show();
  data_table -> hideColumn( COL_BLOCK );
  setCentralWidget( data_table );

  QHeaderView * header_view = data_table->horizontalHeader();
  connect( header_view, SIGNAL( sectionClicked(int)),
           data_table, SLOT(header_clicked(int)) );
  connect( data_table, SIGNAL(itemClicked( QTableWidgetItem* )), 
           data_table, SLOT(cell_clicked( QTableWidgetItem* )) );
  connect( data_table, SIGNAL(itemChanged( QTableWidgetItem* )), 
           this, SLOT(value_changed(QTableWidgetItem*)) );

  if ( do_debug )
    fprintf(stderr, "showData() prepared data_table\n");
  int row = 0;
 
  const char * prev_group = NULL;
  QBrush * brush = NULL;
  QBrush brushes[3];
  brushes[0] = QBrush( Qt::red );
  brushes[1] = QBrush( Qt::blue );
  brushes[2] = QBrush( Qt::black );
  int grp = 0;

  QString c[COL_NR];

  for (CBlock * b = clist.head; b != NULL; b=b->next ) {
    if ( do_debug ) {
      fprintf(stderr, "showData() row %d block %p \n", row, (void*)b );
      b->dump();
    }
    c[COL_GROUP]   = b->Group();
    c[COL_AZIMUTH] = Locale::ToString( b->compass, 2 );
    c[COL_CLINO]   = Locale::ToString( b->clino, 2 );
    c[COL_ROLL]    = Locale::ToString( b->roll, 0 );
    c[COL_ERROR]   = Locale::ToString( b->error, 4 );
    c[COL_SKIP]    = b->ignore ? "v" : " ";
    unsigned int p = (unsigned int)((void*)b);
    c[COL_BLOCK].sprintf("0x%08x", p );

    for (int col = 0; col < COL_NR; ++col) {
      QTableWidgetItem * item = new QTableWidgetItem( c[col] );
      data_table->setItem(row,col, item );
      // data_table->item(row,col)->setText( c[col] );
    }

    if ( strcmp(b->Group(), "-1") == 0 ) {
      brush = &( brushes[2] );
      prev_group = NULL;
    } else if ( prev_group == NULL || strcmp( prev_group, b->Group() ) != 0 ) {
      prev_group = b->Group();
      grp = 1 - grp;
      brush = &( brushes[grp] );
    }

    data_table->item(row,COL_GROUP)->setForeground( *brush );
    data_table->item(row,COL_AZIMUTH)->setForeground( *brush );
    data_table->item(row,COL_CLINO)->setForeground( *brush );
    data_table->item(row,COL_ROLL)->setForeground( *brush );

    // fprintf(stderr, "row %d  block %08x\n", row, p );
    ++ row;
  }
  if ( do_debug )
    fprintf(stderr, "showData() done\n");
}

// -------------------------------------------------------
// DistoX download dialog

DownloadDialog::DownloadDialog( QTcalibWidget * parent )
  : QDialog( parent )
  , widget( parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("distox_download" ) );

  QVBoxLayout* vbl = new QVBoxLayout(this);

  DEFINE_HB;
  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("qtopoc_data"), hb ) );
  vbl->addWidget( hb );

  CREATE_HB;
  do_guess = new QCheckBox( lexicon("qtopoc_guess"), hb );
  do_guess->setChecked( parent->isGuessing() );
  hbl->addWidget( do_guess );
  vbl->addWidget( hb );

  CREATE_HB;
  do_on_old = new QCheckBox( lexicon("qtopoc_guess2"), hb );
  do_on_old->setChecked( parent->isGuessOnOld() );
  hbl->addWidget( do_on_old );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}

// -------------------------------------------------------
// DistoX Write calibration dialog

WriteDialog::WriteDialog( QTcalibWidget * parent )
  : QDialog( parent )
  , widget( parent )
{
  Config & config = Config::Get();
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("distox_write" ) );

  QVBoxLayout* vbl = new QVBoxLayout(this);

  DEFINE_HB;
  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("distox_write2" ), hb ) );
  vbl->addWidget( hb );

  CREATE_HB;
  backup = new QCheckBox( lexicon("distox_backup"), hb );
  backup->setChecked( FALSE );
  hbl->addWidget( backup );
  vbl->addWidget( hb );

  CREATE_HB;
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
  hbl->addWidget( backup_file );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}


void 
WriteDialog::doOK()
{
  hide();
  if ( backup->isChecked() ) {
    widget->WriteToDistoX( backup_file->text().TO_CHAR() );
  } else {
    widget->WriteToDistoX( NULL );
  }
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

// -----------------------------------------------------------

#ifdef QT_NO_FILEDIALOG
MyFileDialog::MyFileDialog( QTcalibWidget * parent, const char * caption, int m )
  : QDialog( parent )
  , widget( parent )
  , mode( m )
{
  Language & lexicon = Language::Get();
  setWindowTitle ( lexicon( caption ) );

  QVBoxLayout* vbl = new QVBoxLayout(this);
  
  DEFINE_HB;
  CREATE_HB;
  hbl->addWidget( new QLabel( lexicon("enter_filename"), hb ) );
  vbl->addWidget( hb );

  CREATE_HB;
  line = new QLineEdit( hb );
  hbl->addWidget( line );
  vbl->addWidget( hb );

  CREATE_HB;
  QPushButton * c = new QPushButton( tr( lexicon("ok") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("cancel") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

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
}  
#endif  

// ---------------------------------------------------------------
// OPEN A FILE
//
void
QTcalibWidget::doOpen()
{
  if ( do_debug ) 
    fprintf(stderr, "doOpen file %s \n", fileName.TO_CHAR() );
#ifdef QT_NO_FILEDIALOG
  MyFileDialog dialog( this, 0 );
#else
  doOpenFile( QFileDialog::getOpenFileName( this,
              lexicon("open_file"),
              fileName, 
              "Textfiles (*.txt)\nRaw (calib*)" ) );
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
    fprintf(stderr, "doOpenFile file %s \n", file.TO_CHAR() );
  fileName = file;
  if ( ! fileName.isEmpty() ) {
    calibration_description = "";
    clist.load( fileName.TO_CHAR(), calibration_description, guess_angle );
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
    fprintf(stderr, "doSave file %s \n", fileName.TO_CHAR() );
  if ( clist.size == 0 ) return;
#ifdef QT_NO_FILEDIALOG
  MyFileDialog dialog( this, "save_file", 1);
#else
  doSaveFile( QFileDialog::getSaveFileName( this,
              lexicon("save_file"),
              fileName,
              "Textfiles (*.txt)" ) );
#endif
}

void
QTcalibWidget::doSaveFile( const QString & file )
{
  if ( do_debug ) 
    fprintf(stderr, "doSaveFile file %s \n", file.TO_CHAR() );
  fileName = file;
  if ( ! fileName.isEmpty() ) {
    clist.save(fileName.TO_CHAR(), calibration_description);
  }
}

void
QTcalibWidget::doToggle()
{
  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  DistoX disto( device.c_str(), log );
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
  DownloadDialog download( this );
}

class DownloadThread : public QThread
{
  public:
    DistoX * disto;
    int status;

    DownloadThread( DistoX * d )
      : disto( d )
      , status( 0 )
    {
      fprintf(stderr, "DownloadThread started ...\n"); 
    }

    int getStatus() const { return status; }

    void run();
};

void
DownloadThread::run()
{
  // -1: ask the number of data to the distox
  status = ( disto->download( 0) ) ? 1 : -1;
  fprintf(stderr, "DownloadThread finished width status %d\n", status);
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
  // CTransform transform;

  DistoX disto( device.c_str(), log );
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
    data_transform = CTransform();
    if ( do_use_old ) {
      if ( ! disto.readCoeffs( old_byte ) ) {
        do_use_old = false;
        QMessageBox::warning(this, lexicon("qtopoc_calib"), 
          lexicon("failed_read_default") );
      } else {
        // FIXME setup the CTransform
        data_transform.setValue( old_byte );
        // transform.dump();
      }
    }  
    // sleep(1);
    unsigned int nr = disto.calibrationSize();
    unsigned int nd = disto.measurementSize();
    if ( nd > 0 ) {
      unsigned int id, ib, ic, ir;
      double xd, xb, xc, xr;
      const char * data_file = config( "DEFAULT_DATA" );
      FILE * fpd = fopen( data_file, "w" );
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
          b0 = clist.addData( b0, gx, gy, gz, mx, my, mz, data_transform );
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
  DistoX disto( device.c_str(), log );
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
  CoeffWidget dialog( this, c, comment );
}



void
QTcalibWidget::doWrite()
{
  WriteDialog dialog( this );
}

void
QTcalibWidget::WriteToDistoX( const char * backup_file )
{
  if ( do_debug )
    fprintf(stderr, "WriteToDistoX() backup_file %s\n", backup_file ? backup_file : "NONE" );
 
  const char * disto_log = config( "DISTO_LOG" );
  bool log = ( disto_log[0] == 'y' );
  DistoX disto( device.c_str(), log );
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
  double delta = calib_delta;
  int max_iter = calib_max_iter;
  double error = 0.0;
  int iter = calib.Optimize(delta, error, max_iter);

  if ( do_debug ) {
    const char * coeff_file = config("TEMP_COEFF");
    calib.PrintCalibrationFile( coeff_file );
  }
  data_transform.setValue( &calib );
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
  msg += Locale::ToString( iter );
  msg += "\n";
  msg += lexicon("calib_angle");
  msg += " ";
  msg += Locale::ToString( calib.GetDipAngle(), 2 );

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
  CommentWidget comment( this, c );
}

// ===========================================================
// CIOVERAGE 

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
  CoverageWidget( this, coverage );
}

CoverageWidget::CoverageWidget( QTcalibWidget * p, Coverage & cover )
  : QDialog( p )
  , coverage( cover )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_cover") );
  QVBoxLayout* vbl = new QVBoxLayout(this);

  DEFINE_HB;
  CREATE_HB;
  QString label( lexicon("coverage") );
  hbl->addWidget( new QLabel( label, hb ) );
  vbl->addWidget( hb );
  
  unsigned char * img = coverage.FillImage();
  int width =  coverage.Width();
  int height = coverage.Height();
  QImage image( img, width, height, QImage::Format_RGB32 );
  // image.save( "/tmp/calib.jpg" );
  QPixmap pixmap = QPixmap::fromImage( image );

  // widget->resize( width, height );
  // widget->setBackgroundPixmap( pixmap );
  QGraphicsScene scene( this );
  QGraphicsPixmapItem * pix_item = scene.addPixmap( pixmap );
  pix_item->setVisible( true );
  pix_item->setZValue( 1 );
  QGraphicsView view( &scene, this );
  vbl->addWidget( &view );
  double w = scene.width();
  double h = scene.height();
  QGraphicsTextItem * t1 = scene.addText( "N" );
  QGraphicsTextItem * t2 = scene.addText( "E" );
  QGraphicsTextItem * t3 = scene.addText( "W" );
  QGraphicsTextItem * t4 = scene.addText( "+90" );
  QGraphicsTextItem * t5 = scene.addText( "-90" );
  t1->setPos( 0.50*w-10, h/2-10 );
  t2->setPos( 0.75*w-10, h/2-10 );
  t3->setPos( 0.25*w-10, h/2-10 );
  t4->setPos( 0.50*w-10, -20 );
  t5->setPos( 0.50*w-10, h );
  t1->setZValue( 2 );
  t2->setZValue( 2 );
  t3->setZValue( 2 );
  t4->setZValue( 2 );
  t5->setZValue( 2 );

  QPushButton * c = new QPushButton( tr( lexicon("ok") ), this );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  vbl->addWidget( c );
  // show();
  exec();
}

// ===========================================================
// HELP


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

// ===========================================================
// QUIT
  
void
QTcalibWidget::doQuit()
{
  // this->close();
  ExitWidget dialog( this );
}

ExitWidget::ExitWidget( QTcalibWidget * p )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_exit") );
  QVBoxLayout* vbl = new QVBoxLayout(this);

  DEFINE_HB;
  CREATE_HB;
  QString label( lexicon("exit_question") );
  hbl->addWidget( new QLabel( label, hb ) );
  vbl->addWidget( hb );
  

  CREATE_HB;
  QPushButton * c;
  c = new QPushButton( tr( lexicon("yes") ), hb );
  connect( c, SIGNAL(clicked()), this, SLOT(doOK()) );
  hbl->addWidget( c );
  c = new QPushButton( tr( lexicon("no") ), hb);
  connect( c, SIGNAL(clicked()), this, SLOT(doCancel()) );
  hbl->addWidget( c );
  vbl->addWidget( hb );

  exec();
}


// ===========================================================
// MAIN

#ifdef WIN32
int qtcalib_main( int argc, char ** argv )
#else
int main( int argc, char ** argv )
#endif
{
  QApplication app( argc, argv );

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

  if ( strlen( config("ROOT") ) > 0 ) {
    if ( chdir( config("ROOT") ) != 0 ) {
      fprintf(stderr, "Cannot change to root directory %s: %s\n",
        config("ROOT"), strerror( errno ) );
    } else {
      fprintf(stderr, "Changed root to %s\n", config("ROOT") );
    }
  }


  QTcalibWidget widget( config );
  /*
  QPixmap icon;
  if ( icon.load( config("QTCALIB_ICON") ) ) {
    // printf( "loaded icon\n");
    widget.setIcon( icon );
  }
  */
  widget.show();
  return app.exec();
}

// ------------------------------------------------------------------------
// COMMENT

CommentWidget::CommentWidget( QTcalibWidget * parent, double * c )
  : QDialog( parent )
  , widget( parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopoc_comment") );

  QString coeff[24];
  for ( int k=0; k<24; ++k) coeff[k] = Locale::ToString( c[k], 3 );

  QGridLayout * gl = new QGridLayout( this );
  gl->setHorizontalSpacing( 20 ); 
  gl->setVerticalSpacing( 10 ); 
  gl->addWidget( new QLabel( "BG  ", this ), 0, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[0], this ), 0, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[4], this ), 0, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[8], this ), 0, 3, Qt::AlignRight );
  
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

  gl->addWidget( new QLabel( lexicon("edit_descr"), this ), 8, 0, 1, 4, Qt::AlignLeft );
  description = new QLineEdit( parent->getDescription(), this );
  gl->addWidget( description, 9, 0, 1, 4, Qt::AlignLeft );
  //
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this);
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  gl->addWidget( c1, 10, 2, Qt::AlignRight );
  gl->addWidget( c2, 10, 3, Qt::AlignRight );

  exec();
}


CoeffWidget::CoeffWidget( QTcalibWidget * p, double * c, const QString & comment )
  : QDialog( p )
  , parent( p )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopoc_coeff") );

  QString coeff[24];
  for ( int k=0; k<24; ++k) coeff[k] = Locale::ToString( c[k] );

  QGridLayout * gl = new QGridLayout( this );
  gl->setHorizontalSpacing( 20 ); 
  gl->setVerticalSpacing( 10 ); 
  // gl->setColStretch(0, 80);
  // gl->setColStretch(1, 80);
  // gl->setColStretch(2, 80);
  // gl->setColStretch(3, 80);
  int row = 0;
  gl->addWidget( new QLabel( "BG  ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[0], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[4], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[8], this ), row, 3, Qt::AlignRight );
  
  ++row; 
  gl->addWidget( new QLabel( "AGx ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[1], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[2], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[3], this ), row, 3, Qt::AlignRight );
  
  ++row; 
  gl->addWidget( new QLabel( "  y ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[5], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[6], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[7], this ), row, 3, Qt::AlignRight );
  
  ++row; 
  gl->addWidget( new QLabel( "  z ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[ 9], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[10], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[11], this ), row, 3, Qt::AlignRight );
  
  ++row; 
  gl->addWidget( new QLabel( "BM  ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[12], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[16], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[20], this ), row, 3, Qt::AlignRight );
  ++row; 
  // gl->setRowSpacing(4, 30);
  
  ++row; 
  gl->addWidget( new QLabel( "AMx ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[13], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[14], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[15], this ), row, 3, Qt::AlignRight );
  
  ++row; 
  gl->addWidget( new QLabel( "  y ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[17], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[18], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[19], this ), row, 3, Qt::AlignRight );

  ++row; 
  gl->addWidget( new QLabel( "  z ", this ), row, 0, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[21], this ), row, 1, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[22], this ), row, 2, Qt::AlignRight );
  gl->addWidget( new QLabel( coeff[23], this ), row, 3, Qt::AlignRight );

  ++row; 
  if ( ! comment.isEmpty() ) {
    gl->addWidget( new QLabel( comment, this ), row, 0, 2, 4, Qt::AlignLeft );
    row += 2;
    QPushButton * c1 = new QPushButton( tr("OK"), this );
    connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
    gl->addWidget( c1, row, 0 );
  } else {
    QPushButton * c1 = new QPushButton( tr("OK"), this );
    connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
    gl->addWidget( c1, row, 0 );
  }
  exec();
}

// -------------------------------------------------------------
// Option widget

OptionsWidget::OptionsWidget( QTcalibWidget * my_parent )
  : QDialog( my_parent )
  , parent( my_parent )
{
  Language & lexicon = Language::Get();
  setWindowTitle( lexicon("qtopo_options") );
  // QVBoxLayout* vb = new QVBoxLayout(this, 8);
  // vb->setAutoAdd(TRUE);
  // QHBOX * hb = new QHBOX(this);

  QGridLayout * gbl = new QGridLayout( this );
  m_device = new QLineEdit( parent->getDevice(), this ); 
  gbl->addWidget( new QLabel( lexicon("distox_device"), this ), 0, 0 );
  gbl->addWidget( m_device, 0, 1 );

  QString guess;
  guess.sprintf( "%d",  parent->getGuessAngle() );
  m_angle = new QLineEdit( guess, this ); 
  gbl->addWidget( new QLabel( lexicon("guess_angle"), this ), 1, 0 );
  gbl->addWidget( m_angle, 1, 1 );

  mb_transf = new QCheckBox( );
  mb_transf->setChecked(  parent->getDataTransformed() );
  gbl->addWidget( new QLabel( lexicon("data_transformed") ), 2, 0 );
  gbl->addWidget( mb_transf, 2, 1 );

  QString delta;
  delta.sprintf( "%.6f", parent->getCalibDelta() );
  m_delta = new QLineEdit( delta, this );
  gbl->addWidget( new QLabel( lexicon("calib_delta"), this ), 3, 0 );
  gbl->addWidget( m_delta, 3, 1 );

  QString max_iter;
  max_iter.sprintf( "%d", parent->getCalibMaxIter() );
  m_max_iter = new QLineEdit( max_iter, this );
  gbl->addWidget( new QLabel( lexicon("calib_max_iter"), this ), 4, 0 );
  gbl->addWidget( m_max_iter, 4, 1 );

  
  // hb = new QHBOX(this);
  QPushButton * c1 = new QPushButton( tr( lexicon("ok") ), this );
  connect( c1, SIGNAL(clicked()), this, SLOT(doOK()) );
  QPushButton * c2 = new QPushButton( tr( lexicon("cancel") ), this );
  connect( c2, SIGNAL(clicked()), this, SLOT(doCancel()) );
  gbl->addWidget( c1, 5, 0 );
  gbl->addWidget( c2, 5, 1 );

  exec();
}

void
OptionsWidget::doOK()
{
  hide();
  if ( ! m_device->text().isEmpty() ) {
    parent->setDevice( m_device->text().TO_CHAR() );
  }
  if ( ! m_angle->text().isEmpty() ) {
    parent->setGuessAngle( atoi( m_angle->text().TO_CHAR() ) );
  }
  parent->setDataTransformed( mb_transf->isChecked() ) ;
  if ( ! m_delta->text().isEmpty() ) {
    parent->setCalibDelta( atof( m_delta->text().TO_CHAR() ) );
  }
  if ( ! m_max_iter->text().isEmpty() ) {
    parent->setCalibMaxIter( atof( m_max_iter->text().TO_CHAR() ) );
  }
}
