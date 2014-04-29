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
 * 20130910 populate survey with old-survey data (oldSid and oldId)
 * 20130921 handling return from Proj4 request (coord. conversion only on-demand for now)
 * 20130921 bug-fix long/lat swapped in add FixedInfo
 * 20131201 button bar new interface. reorganized actions
 * 20140221 if geodetic height fails, altimetric height is negative
 */
package com.topodroid.DistoX;

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
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import android.app.Application;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

import android.net.Uri;

public class SurveyActivity extends Activity
                            // implements OnItemClickListener, ILister
                            // extends Dialog
                            implements View.OnClickListener
{
  private static int icons[] = { // R.drawable.ic_save,
                        R.drawable.ic_export,
                        R.drawable.ic_note,
                        R.drawable.ic_gps,
                        R.drawable.ic_details,
                        R.drawable.ic_camera,
                        R.drawable.ic_3d,
                        R.drawable.ic_close,
                        R.drawable.ic_pref,
                        R.drawable.ic_help
                     };
  private static int help_texts[] = { // R.string.help_save,
                        R.string.help_export_survey,
                        R.string.help_note,
                        R.string.help_loc,
                        R.string.help_info_shot,
                        R.string.help_photo,
                        R.string.help_3d,
                        R.string.help_delete_survey,
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // private ShotActivity mParent;
  private Context mContext;

  private TextView mTextName;
  private EditText mEditDate;
  private EditText mEditTeam;
  private EditText mEditComment;

  // private Button mButtonHelp;
  private Button[] mButton1;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;

  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  // private Button mBTNopen;
  private Button mBTNexport;
  private Button mBTN3d;
  private Button mBTNdelete;
  private Button mBTNnotes;
  private Button mBTNlocation;
  private Button mBTNinfo;
  private Button mBTNphoto;

  private TopoDroidApp app;
  private SurveyInfo info;
  // private DistoX mDistoX;
  private boolean mustOpen;
  private long oldSid;   // old survey id
  private long oldId;    // old shot id

  // private ArrayList< FixedInfo > mFixed; // fixed stations
  // private ArrayList< PhotoInfo > mPhoto; // photoes

// -------------------------------------------------------------------
  // public SurveyActivity( Context context, ShotActivity parent )
  // {
  //   super( context );
  //   mContext = context;
  //   mParent  = parent;
  //   app = (TopoDroidApp)mParent.getApplication();
  // }

  private final static int LOCATION_REQUEST = 1;
  private static int CRS_CONVERSION_REQUEST = 2; // not final ?
  private DistoXLocation mLocation;
  private FixedDialog mFixedDialog;

  void tryProj4( FixedDialog dialog, String cs_to, FixedInfo fxd )
  {
    if ( cs_to == null ) return;
    try {
      Intent intent = new Intent( "Proj4.intent.action.Launch" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, "com.topodroid.Proj4.intent.action.Launch" );
      intent.putExtra( "version", "1.1" );      // Proj4 version
      intent.putExtra( "cs_from", "Long-Lat" ); // NOTE MUST USE SAME NAME AS Proj4
      intent.putExtra( "cs_to", cs_to ); 
      intent.putExtra( "longitude", fxd.lng );
      intent.putExtra( "latitude",  fxd.lat );
      intent.putExtra( "altitude",  fxd.alt );

      mFixedDialog = dialog;
      TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "CONV. REQUEST " + fxd.lng + " " + fxd.lat + " " + fxd.alt );
      startActivityForResult( intent, SurveyActivity.CRS_CONVERSION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      mFixedDialog = null;
      Toast.makeText( this, R.string.no_proj4, Toast.LENGTH_SHORT).show();
    }
  }

  boolean tryGPSAveraging( DistoXLocation loc )
  {
    mLocation = null;
    try {
      mLocation = loc;
      Intent intent = new Intent( "cz.destil.gpsaveraging.AVERAGED_LOCATION" );
      // Intent intent = new Intent( Intent.ACTION_DEFAULT, Uri.parse("cz.destil.gpsaveraging.AVERAGED_LOCATION") );
      startActivityForResult( intent, LOCATION_REQUEST );
    } catch ( ActivityNotFoundException e ) {
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "ActivityNotFound " + e.toString() );
      mLocation = null;
      return false;
    }
    return true;
  }

  public void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    if ( resCode == RESULT_OK ) {
      if ( reqCode == LOCATION_REQUEST ) {
        if ( mLocation != null ) {
          Bundle bundle = intent.getExtras();
          mLocation.setPosition( 
            bundle.getDouble( "longitude" ),
            bundle.getDouble( "latitude" ),
            bundle.getDouble( "altitude" ) );
          // accuracy = bundle.getDouble( "accuracy" );
          // name = bundle.getStriung( "name" ); waypoint name

          mLocation = null;
        }
      } else if ( reqCode == CRS_CONVERSION_REQUEST ) {
        if ( mFixedDialog != null ) {
          Bundle bundle = intent.getExtras();
          String cs = bundle.getString( "cs_to" );
          StringWriter sw = new StringWriter();
          PrintWriter pw  = new PrintWriter( sw );
          pw.format( "%.2f %.2f %.2f",
             bundle.getDouble( "longitude"),
             bundle.getDouble( "latitude"),
             bundle.getDouble( "altitude") );
          TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "CONV. RESULT " + sw.getBuffer().toString() );
          mFixedDialog.setTitle( sw.getBuffer().toString() );
          mFixedDialog.setCSto( cs );
          mFixedDialog = null;
        }
      }
    }
  }

  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    app = (TopoDroidApp)getApplication();
    app.mSurveyActivity = this;
    mFixedDialog = null;

    mContext = this;
    mustOpen = false;
    oldSid = -1L;
    oldId  = -1L;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      if ( extras.getInt( TopoDroidApp.TOPODROID_SURVEY ) == 1 ) mustOpen = true;
      oldSid = extras.getLong( TopoDroidApp.TOPODROID_OLDSID );
      oldId  = extras.getLong( TopoDroidApp.TOPODROID_OLDID );
    }

    setContentView(R.layout.survey_activity);
    setTitle( R.string.title_survey );
    mTextName    = (TextView) findViewById(R.id.survey_name);
    mEditDate    = (EditText) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "app mSID " + app.mSID );
    if ( app.mSID >= 0 ) {
      info = app.getSurveyInfo();
      mTextName.setText( info.name );

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
      TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "opening non-existent survey" );
      setResult( RESULT_CANCELED );
      finish();
      // mTextName.setHint( R.string.name );
      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      // mEditDate.setText( sdf.format( new Date() ) );
      // mEditTeam.setHint( R.string.team );
      // mEditTeam.setHint( R.string.team );
      // mEditComment.setHint( R.string.description );
    }

    // mFixed = new ArrayList< FixedInfo >();
    // mPhoto = new ArrayList< PhotoInfo >();

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }

    int nr_button1 = 7;
    mButton1 = new Button[ nr_button1 ];
    for ( int k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );
  }

  // @Override
  // public synchronized void onResume() 
  // {
  //   super.onResume();
  //   if ( mustOpen ) {
  //     mustOpen = false;
  //     doOpen();
  //   }
  // }

  // ------------------------------------------
   
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    int k = 0;
    // if ( b == mButton1[k++] ) {  // save
    //   doSave();
    // } else
    if ( b == mButton1[k++] ) {  // export
      // doExport( true );
      new SurveyExportDialog( this, this ).show();
    } else if ( b == mButton1[k++] ) {  // note
      doNotes();
    } else if ( b == mButton1[k++] ) {  // gps
      doLocation();
    } else if ( b == mButton1[k++] ) {  // details
      (new SurveyStatDialog( this, app.mData.getSurveyStat( app.mSID ) )).show();
    } else if ( b == mButton1[k++] ) {  // photo camera
      Intent photoIntent = new Intent( this, PhotoActivity.class );
      startActivity( photoIntent );
    } else if ( b == mButton1[k++] ) {  // 3D
      do3D();
    // } else if ( b == mButton1[k++] ) {  // back
    //   setResult( RESULT_CANCELED );
    //   finish();
    } else if ( b == mButton1[k++] ) {  // delete
      askDelete();
    // } else if ( b == mButtonHelp ) {  // help
    //   (new HelpDialog(this, icons, help_texts ) ).show();
    }
  }

  @Override
  public void onStop()
  {
    doSave();
    super.onStop();
  }

  void doArchive()
  {
    doExport( TopoDroidApp.DISTOX_EXPORT_TH, false );
    Archiver archiver = new Archiver( app );
    if ( archiver.archive( ) ) {
      String msg = getResources().getString( R.string.zip_saved ) + " " + archiver.zipname;
      Toast.makeText( this, msg, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( this, R.string.zip_failed, Toast.LENGTH_SHORT).show();
    }
  }

  private void askDelete()
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.survey_delete ) + " " + app.mySurvey + " ?" );
    
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

  // ===============================================================

  private void do3D()
  {
    app.exportSurveyAsTh(); // make sure to have survey exported as therion
    try {
      Intent intent = new Intent( "Cave3D.intent.action.Launch" );
      intent.putExtra( "survey", app.getSurveyThFile() );
      startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT).show();
    }
  }

  private void doSave()
  {
    saveSurvey( );
    // setMenus();
  }

  // private void doOpen()
  // {
  //   // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "do OPEN " );
  //   // dismiss();
  //   Intent openIntent = new Intent( mContext, ShotActivity.class );
  //   mContext.startActivity( openIntent );
  // }

  private void doLocation()
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_DEBUG, "doLocation" );
    LocationManager lm = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
    new DistoXLocation( mContext, this, app, lm ).show();
  }

  private void doNotes()
  {
    if ( app.mySurvey != null ) {
      (new DistoXAnnotations( this, app.mySurvey )).show();
    } else { // SHOULD NEVER HAPPEN
      Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT).show();
    }
  }


  // ---------------------------------------------------------------

  private void saveSurvey( )
  {
    // String name = mTextName.getText().toString();
    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();

    // FIXME FORCE NAMES WITHOUT SPACES
    // name = TopoDroidApp.noSpaces( name );
    if ( date != null ) { date = date.trim(); }
    if ( team != null ) { team = team.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    // TopoDroidApp.Log( TopoDroidApp.LOG_SURVEY, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
    app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
    if ( team != null ) {
     app.mData.updateSurveyTeam( app.mSID, team );
    } 
  }
  
  void doExport( int exportType, boolean warn )
  {
    if ( app.mSID < 0 ) {
      if ( warn ) {
        Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT).show();
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
        case TopoDroidApp.DISTOX_EXPORT_CSV:
          filename = app.exportSurveyAsCsv();
          break;
        case TopoDroidApp.DISTOX_EXPORT_DXF:
          List<DistoXDBlock> list = app.mData.selectAllShots( app.mSID, TopoDroidApp.STATUS_NORMAL );
          DistoXDBlock blk = list.get( 0 );
          if ( blk != null ) {
            Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            DistoXNum num = new DistoXNum( list, blk.mFrom );
            filename = app.exportSurveyAsDxf( num );
          }
          break;
        case TopoDroidApp.DISTOX_EXPORT_CSX:
          filename = app.exportSurveyAsCsx();
          break;
        case TopoDroidApp.DISTOX_EXPORT_TOP:
          filename = app.exportSurveyAsTop();
          break;
      }
      if ( warn ) { 
        if ( filename != null ) {
          Toast.makeText( mContext, mContext.getString(R.string.saving_) + filename, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText( mContext, R.string.saving_file_failed, Toast.LENGTH_SHORT).show();
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
    setResult( RESULT_OK, new Intent() );
    finish();
    // dismiss();
  }
 
  public FixedInfo addLocation( String station, double longitude, double latitude, double altitude, double altimetric )
  {
    // app.addFixed( station, longitude, latitude, altitude );
    // addFixed( station, longitude, latitude, altitude );
    long id = app.mData.insertFixed( app.mSID, -1L, station, longitude, latitude, altitude, altimetric, "", 0L ); // FIXME comment
    // TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "addLocation mSID " + app.mSID + " id " + id );

    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    // DistoXAnnotations.append( app.mySurvey, sw.getBuffer().toString() );

    return new FixedInfo( id, station, latitude, longitude, altitude, altimetric, "" ); // FIXME comment
  }

  public void updateFixed( FixedInfo fxd, String station )
  {
    app.mData.updateFixedStation( fxd.id, app.mSID, station );
  }

  public void dropFixed( FixedInfo fxd )
  {
    app.mData.updateFixedStatus( fxd.id, app.mSID, TopoDroidApp.STATUS_DELETED );
  }

  // ---------------------------------------------------------
  // MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIhelp.setIcon( R.drawable.ic_help );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    Intent intent;
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( intent );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

}
