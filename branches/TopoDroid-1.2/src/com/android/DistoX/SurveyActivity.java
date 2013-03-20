/* @file SurveyActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created from DistoX.java
 * 20120524 fixed station management
 * 20120524 changing to dialog / menu buttons
 * 20120531 implementing survey delete
 * 20120603 fixed-info update/delete methods
 * 20120607 added 3D button / rearranged buttons layout
 * 20120610 archive (zip) button
 * 20120619 handle "mustOpen" (immediate) request
 * 20130213 unified export and zip (export dialog)
 * 20130307 made Annotations into a dialog
 */
package com.android.DistoX;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Activity;
// import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import android.app.Application;
// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import android.view.View;
import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;


public class SurveyActivity extends Activity
                            // implements OnItemClickListener, ILister
                            // extends Dialog
                            implements View.OnClickListener
{
  // private TopoDroidActivity mParent;
  private Context mContext;

  private EditText mEditName;
  private EditText mEditDate;
  private EditText mEditTeam;
  private EditText mEditComment;

  private Button mBTsave;
  private Button mBTNopen;
  private Button mBTNexport;
  private Button mBTN3d;
  private Button mBTNdelete;
  private Button mBTNnotes;
  private Button mBTNlocation;
  // private Button mBTNarchive;
  private Button mBTNinfo;
  private Button mBTNphoto;

  private TopoDroidApp app;
  private SurveyInfo info;
  // private DistoX mDistoX;
  private boolean isSaved;
  private boolean mustOpen;

  // private ArrayList< FixedInfo > mFixed; // fixed stations
  // private ArrayList< PhotoInfo > mPhoto; // photoes

// -------------------------------------------------------------------
  // public SurveyActivity( Context context, TopoDroidActivity parent )
  // {
  //   super( context );
  //   mContext = context;
  //   mParent  = parent;
  //   app = (TopoDroidApp)mParent.getApplication();
  // }

  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    app = (TopoDroidApp)getApplication();
    mContext = this;
    mustOpen = false;
    Bundle extras = getIntent().getExtras();
    if ( extras != null && extras.getInt( TopoDroidApp.TOPODROID_SURVEY ) == 1 ) {
      mustOpen = true;
    }

    setContentView(R.layout.distox_survey_dialog);
    setTitle( R.string.title_survey );
    mEditName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (EditText) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "app mSID " + app.mSID );
    isSaved = ( app.mSID >= 0 );
    if ( isSaved ) {
      info = app.getSurveyInfo();
      mEditName.setText( info.name );
      setNameNotEditable();

      mEditDate.setText( info.date );
      if ( info.comment != null && info.comment.length() > 0 ) {
        mEditComment.setText( info.comment );
      } else {
        mEditComment.setHint( R.string.description );
      }
      if ( info.team != null && info.team.length() > 0 ) {
        mEditTeam.setText( info.team );
      } else {
        mEditTeam.setHint( R.string.team );
      }
    } else {
      mEditName.setHint( R.string.name );
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mEditDate.setText( sdf.format( new Date() ) );
      mEditTeam.setHint( R.string.team );
      mEditTeam.setHint( R.string.team );
      mEditComment.setHint( R.string.description );
    }

    // mFixed = new ArrayList< FixedInfo >();
    // mPhoto = new ArrayList< PhotoInfo >();

    mBTsave      = (Button) findViewById( R.id.surveySave );
    mBTNopen     = (Button) findViewById( R.id.surveyOpen );
    mBTNexport   = (Button) findViewById( R.id.surveyExport );
    mBTN3d       = (Button) findViewById( R.id.survey3D );
    mBTNdelete   = (Button) findViewById( R.id.surveyDelete );
    mBTNnotes    = (Button) findViewById( R.id.surveyNotes );
    mBTNlocation = (Button) findViewById( R.id.surveyLocation );
    // mBTNarchive  = (Button) findViewById( R.id.surveyArchive );
    mBTNinfo     = (Button) findViewById( R.id.surveyInfo );
    mBTNphoto    = (Button) findViewById( R.id.surveyPhoto );

    setButtons();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    if ( mustOpen ) {
      mustOpen = false;
      doOpen();
    }
  }

  // --------- menus ----------
  // private MenuItem mMIsave;
  // private MenuItem mMIopen;
  // private MenuItem mMIexport;
  // private MenuItem mMInotes;
  // private MenuItem mMIlocation;

  // private SubMenu  mSMmore;
  // private MenuItem mMIdelete;
  // private MenuItem mMIoptions;
  // private MenuItem mMIhelp;

  // @Override
  // public boolean onCreateOptionsMenu(Menu menu) 
  // {
  //   super.onCreateOptionsMenu( menu );

  //   mMIsave    = menu.add( R.string.menu_save );
  //   mMIopen    = menu.add( R.string.menu_open );
  //   mMIexport  = menu.add( R.string.menu_export );
  //   mMInotes   = menu.add( R.string.menu_notes );
  //   mMIlocation = menu.add( R.string.menu_location );
  //   mSMmore    = menu.addSubMenu( R.string.menu_more );
  //   mMIdelete  = mSMmore.add( R.string.menu_delete );
  //   mMIoptions = mSMmore.add( R.string.menu_options );
  //   mMIhelp    = mSMmore.add( R.string.menu_help );

  //   mMIsave    .setIcon( R.drawable.save );
  //   mMIopen    .setIcon( R.drawable.open );
  //   mMIexport  .setIcon( R.drawable.export );
  //   mMInotes   .setIcon( R.drawable.compose );
  //   mMIlocation.setIcon( R.drawable.location );
  //   mSMmore.setIcon( R.drawable.more );
  //   // mMIdelete  .setIcon( R.drawable.delete );
  //   // mMIoptions .setIcon( R.drawable.prefs );
  //   // mMIhelp    .setIcon( R.drawable.help );


  //   setMenus( );
  //   return true;
  // }

  // private void setMenus( )
  // { 
  //   // mEditName.setEnabled( ! isSaved );
  //   mMIopen.setEnabled( isSaved );
  //   mMIexport.setEnabled( isSaved );
  //   mMIdelete.setEnabled( isSaved );
  //   mMInotes.setEnabled( isSaved );
  //   mMIlocation.setEnabled( isSaved );
  // }
   
  private void setButtons( )
  { 
    mBTNopen.setEnabled( isSaved );
    mBTNexport.setEnabled( isSaved );
    mBTN3d.setEnabled( isSaved );
    mBTNdelete.setEnabled( isSaved );
    mBTNnotes.setEnabled( isSaved );
    mBTNlocation.setEnabled( isSaved );
    // mBTNarchive.setEnabled( isSaved );
    mBTNinfo.setEnabled( isSaved );
    mBTNphoto.setEnabled( isSaved );
  }

  private void setNameNotEditable()
  {
    if ( isSaved ) {
      mEditName.setFocusable( false );
      mEditName.setClickable( false );
      mEditName.setKeyListener( null );
    }
  }


  @Override
  public void onClick(View view)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "SurveyActivity onClick() " + item.toString() );
    switch (view.getId()){
      case R.id.surveySave:
        doSave();
        break;
      case R.id.surveyOpen:
        doOpen();
        break;
      case R.id.surveyExport:
        // doExport( true );
        new SurveyExportDialog( this, this ).show();
        break;
      case R.id.survey3D:
        do3D();
        break;
      case R.id.surveyNotes:
        doNotes();
        break;
      case R.id.surveyLocation:
        doLocation();
        break;
      case R.id.surveyDelete:
        askDelete();
        break;
      // case R.id.surveyArchive:
      //   doArchive();
      //   break;
      case R.id.surveyInfo:
        (new SurveyStatDialog( this, app.mData.getSurveyStat( app.mSID ) )).show();
        break;
      case R.id.surveyPhoto:
        Intent photoIntent = new Intent( this, PhotoActivity.class );
        startActivity( photoIntent );
        break;
    }
  }

  void doArchive()
  {
    doExport( TopoDroidApp.DISTOX_EXPORT_TH, false );
    Archiver archiver = new Archiver( app );
    if ( archiver.archive( ) ) {
      String msg = getResources().getString( R.string.zip_saved ) + " " + archiver.zipname;
      Toast.makeText( this, msg, Toast.LENGTH_LONG ).show();
    } else {
      Toast.makeText( this, R.string.zip_failed, Toast.LENGTH_LONG ).show();
    }
  }

  private void askDelete()
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.survey_delete ) + " " + app.getSurvey() + " ?" );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDelete();
        }
    } );

    alert.setNegativeButton( R.string.button_cancel, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) { }
    } );
    alert.show();
  }

  // @Override
  // public boolean onOptionsItemSelected(MenuItem item) 
  // {
  //   // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "onOptionsItemSelected() " + StatusName() );
  //   // Handle item selection
  //   if ( item == mMIsave ) {
  //     doSave();
  //   } else if ( item == mMIopen ) {
  //     doOpen();
  //   } else if ( item == mMIexport ) {
  //     doExport( true );
  //   } else if ( item == mMInotes ) {
  //     doNotes();
  //   } else if ( item == mMIlocation ) {  
  //     doLocation();
  //   } else if ( item == mMIdelete ) {  
  //     // TODO ask confirm
  //     // doDelete();
  //   } else if ( item == mMIoptions ) {  
  //     Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
  //     startActivity( optionsIntent );
  //   } else if ( item == mMIhelp ) {  
  //     TopoDroidHelp.show( this, R.string.help_survey );
  //   } else {
  //     return super.onOptionsItemSelected(item);
  //   }
  //   return true;
  // }

  // ===============================================================

  private void do3D()
  {
    app.exportSurveyAsTh(); // make sure to have survey exported as therion
    Intent intent = new Intent( "Cave3D.intent.action.Launch" );
    intent.putExtra( "survey", app.getSurveyThFile() );
    try {
      startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_LONG ).show();
    }
  }

  private void doSave()
  {
    saveSurvey( );
    // setMenus();
    setButtons();
  }

  private void doOpen()
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "do OPEN " );
    // dismiss();
    Intent openIntent = new Intent( mContext, ShotActivity.class );
    mContext.startActivity( openIntent );
  }

  private void doLocation()
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_DEBUG, "doLocation" );
    LocationManager lm = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
    new DistoXLocation( mContext, this, app, lm ).show();
  }

  private void doNotes()
  {
    if ( app.getSurvey() != null ) {
      (new DistoXAnnotations( this, app.getSurvey() )).show();
      // Intent notesIntent = new Intent( mContext, DistoXAnnotations.class );
      // notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
      // mContext.startActivity( notesIntent );
    } else { // SHOULD NEVER HAPPEN
      Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_LONG ).show();
    }
  }


  // ---------------------------------------------------------------
  // fixed stations
  //

  // public void addFixed( String station, double latitude, double longitude, double altitude )
  // {
  //   // mFixed.add( new FixedInfo( station, latitude, longitude, altitude ) );
  //   // NOTE info.id == app.mSID
  //   app.mData.insertFixed( station, info.id, longitude, latitude, altitude, "" ); // FIXME comment
  // }

  // populate fixed from the DB
  // this is not ok must re-populate whenever the survey changes
  // public void restoreFixed()
  // {
  //   mFixed.clear(); // just to make sure ...
  //   List< FixedInfo > fixed = app.mData.selectAllFixed( mSID );
  //   for ( FixedInfo fix : fixed ) {
  //     mFixed.add( fix );
  //   }
  // }

  // ---------------------------------------------------------------

  private void saveSurvey( )
  {
    String name = mEditName.getText().toString();
    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();

    // FIXME FORCE NAMES WITHOUT SPACES
    name = TopoDroidApp.noSpaces( name );
    if ( date != null ) { date = date.trim(); }
    if ( team != null ) { team = team.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    if ( isSaved ) { // survey already saved
      // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
      app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
      if ( team != null ) {
        app.mData.updateSurveyTeam( app.mSID, team );
      } 
    } else { // new survey
     
      if ( app.hasSurveyName( name ) ) { // name already exists
        Toast.makeText( mContext, R.string.survey_exists, Toast.LENGTH_LONG ).show();
      } else {
        app.setSurveyFromName( name );
        app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
        if ( team != null ) {
          app.mData.updateSurveyTeam( app.mSID, team );
        } 
        isSaved = true;
        setNameNotEditable();
      }
    }
  }
  
  void doExport( int exportType, boolean warn )
  {
    if ( app.getSurveyId() < 0 ) {
      if ( warn ) {
        Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        // case TopoDroidApp.DISTOX_EXPORT_TLX:
        //   filename = app.exportSurveyAsTlx();
        //   break;
        case TopoDroidApp.DISTOX_EXPORT_TH:
          filename = app.exportSurveyAsTh();
          break;
        case TopoDroidApp.DISTOX_EXPORT_DAT:
          filename = app.exportSurveyAsDat();
          break;
        case TopoDroidApp.DISTOX_EXPORT_SVX:
          filename = app.exportSurveyAsSvx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TRO:
          filename = app.exportSurveyAsTro();
          break;
      }
      if ( warn ) { 
        if ( filename != null ) {
          Toast.makeText( mContext, mContext.getString(R.string.saving_) + filename, Toast.LENGTH_LONG ).show();
        } else {
          Toast.makeText( mContext, R.string.saving_file_failed, Toast.LENGTH_LONG ).show();
        }
      }
    }
  }

  private void doDelete()
  {
    if ( app.mSID < 0 ) return;
    File imagedir = new File( app.getSurveyPhotoDir() );
    if ( imagedir.exists() ) {
      File[] fs = imagedir.listFiles();
      for ( File f : fs ) f.delete();
      imagedir.delete();
    }

    File t = new File( TopoDroidApp.getSurveyNoteFile( app.mySurvey ) );
    if ( t.exists() ) t.delete();
    
    // t = new File( app.getSurveyTlxFile() );
    // if ( t.exists() ) t.delete();
    
    t = new File( app.getSurveyThFile() );
    if ( t.exists() ) t.delete();

    t = new File( app.getSurveySvxFile() );
    if ( t.exists() ) t.delete();
    
    t = new File( app.getSurveyDatFile() );
    if ( t.exists() ) t.delete();
    
    t = new File( app.getSurveyTroFile() );
    if ( t.exists() ) t.delete();
    
    List< PlotInfo > plots = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_NORMAL );
    if ( TopoDroidApp.hasTh2Dir() ) {
      for ( PlotInfo p : plots ) {
        t = new File( app.getSurveyPlotFile( p.name ) );
        if ( t.exists() ) t.delete();
      }
    }
    if ( TopoDroidApp.hasPngDir() ) {
      for ( PlotInfo p : plots ) {
        t = new File( app.getSurveyPngFile( p.name ) );
        if ( t.exists() ) t.delete();
      }
    }

    plots = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_DELETED );
    if ( TopoDroidApp.hasTh2Dir() ) {
      for ( PlotInfo p : plots ) {
        t = new File( app.getSurveyPlotFile( p.name ) );
        if ( t.exists() ) t.delete();
      }
    }
    if ( TopoDroidApp.hasPngDir() ) {
      for ( PlotInfo p : plots ) {
        t = new File( app.getSurveyPngFile( p.name ) );
        if ( t.exists() ) t.delete();
      }
    }

    app.mData.doDeleteSurvey( app.mSID );
    app.setSurveyFromName( null );
    finish();
    // dismiss();
  }
 
  public FixedInfo addLocation( String station, double latitude, double longitude, double altitude )
  {
    
    // app.addFixed( station, latitude, longitude, altitude );
    // addFixed( station, latitude, longitude, altitude );
    long id = app.mData.insertFixed( app.mSID, -1L, station, longitude, latitude, altitude, "", 0L ); // FIXME comment
    // TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "addLocation mSID " + app.mSID + " id " + id );

    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    // DistoXAnnotations.append( app.getSurvey(), sw.getBuffer().toString() );

    return new FixedInfo( id, station, latitude, longitude, altitude, "" ); // FIXME comment
  }

  public void updateFixed( FixedInfo fxd, String station )
  {
    app.mData.updateFixedStation( fxd.id, app.mSID, station );
  }

  public void dropFixed( FixedInfo fxd )
  {
    app.mData.updateFixedStatus( fxd.id, app.mSID, TopoDroidApp.STATUS_DELETED );
  }

}
