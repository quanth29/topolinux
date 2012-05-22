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
 */
package com.android.DistoX;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

import android.util.Log;

import android.content.Context;
// import android.content.Intent;

import android.location.LocationManager;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

import android.app.Application;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;
// import android.view.View;
// import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;


public class SurveyActivity extends Activity
                            // implements OnItemClickListener, ILister
                            // implements View.OnClickListener
{
  private static final String TAG = "DistoX";

  private EditText mEditName;
  private EditText mEditDate;
  private EditText mEditTeam;
  private EditText mEditComment;

  private TopoDroidApp app;
  private SurveyInfo info;
  // private DistoX mDistoX;
  private boolean isSaved;

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.distox_survey_dialog);
    mEditName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (EditText) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    app     = (TopoDroidApp)getApplication();
    Log.v( TAG, "app mSID " + app.mSID );
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
  }


  // --------- menus ----------
  private MenuItem mMIsave;
  private MenuItem mMIopen;
  private MenuItem mMIexport;
  private MenuItem mMInotes;
  private MenuItem mMIlocation;

  private SubMenu  mSMmore;
  private MenuItem mMIdelete;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIsave    = menu.add( R.string.menu_save );
    mMIopen    = menu.add( R.string.menu_open );
    mMIexport  = menu.add( R.string.menu_export );
    mMInotes   = menu.add( R.string.menu_notes );
    mMIlocation = menu.add( R.string.menu_location );
    mSMmore    = menu.addSubMenu( R.string.menu_more );
    mMIdelete  = mSMmore.add( R.string.menu_delete );
    mMIoptions = mSMmore.add( R.string.menu_options );
    mMIhelp    = mSMmore.add( R.string.menu_help );

    mMIsave    .setIcon( R.drawable.save );
    mMIopen    .setIcon( R.drawable.open );
    mMIexport  .setIcon( R.drawable.export );
    mMInotes   .setIcon( R.drawable.compose );
    mMIlocation.setIcon( R.drawable.location );
    mSMmore.setIcon( R.drawable.more );
    // mMIdelete  .setIcon( R.drawable.delete );
    // mMIoptions .setIcon( R.drawable.prefs );
    // mMIhelp    .setIcon( R.drawable.help );

    setMenus( );
    return true;
  }

  private void setMenus( )
  { 
    // mEditName.setEnabled( ! isSaved );
    if ( mMIopen != null )   mMIopen.setEnabled( isSaved );
    if ( mMIexport != null ) mMIexport.setEnabled( isSaved );
    if ( mMIdelete != null ) mMIdelete.setEnabled( isSaved );
    if ( mMInotes  != null ) mMInotes.setEnabled( isSaved );
    if ( mMIlocation != null ) mMIlocation.setEnabled( isSaved );
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // Log.v( TAG, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIsave ) {
      saveSurvey( );
      setMenus();
    } else if ( item == mMIopen ) {
      Intent openIntent = new Intent( this, ShotActivity.class );
      startActivity( openIntent );
    } else if ( item == mMIexport ) {
      exportSurvey();
    } else if ( item == mMInotes ) {
      if ( app.getSurvey() != null ) {
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
        startActivity( notesIntent );
      } else { // SHOULD NEVER HAPPEN
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIlocation ) {  
      // TODO
      LocationManager lm = (LocationManager) getSystemService( LOCATION_SERVICE );
      DistoXLocation loc = new DistoXLocation( this, this, lm );
      loc.show();
    } else if ( item == mMIdelete ) {  
      // TODO ask confirm
      // deleteSurvey();
    } else if ( item == mMIoptions ) {  
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIhelp ) {  
      TopoDroidHelp.show( this, R.string.help_survey );
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  // ---------------------------------------------------------------

  private void saveSurvey( )
  {
    String name = mEditName.getText().toString();
    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();

    if ( isSaved ) { // survey already saved
      // Log.v( TAG, "INSERT survey id " + id + " date " + date + " name " + name + " comment " + comment );
      app.mData.updateSurveyDayAndComment( app.mSID, date, comment );
      if ( team != null ) {
        app.mData.updateSurveyTeam( app.mSID, team );
      } 
    } else { // new survey
      if ( app.hasSurveyName( name ) ) { // name already exists
        Toast.makeText( this, R.string.survey_exists, Toast.LENGTH_LONG ).show();
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
  
  private void setNameNotEditable()
  {
    if ( isSaved ) {
      mEditName.setFocusable( false );
      mEditName.setClickable( false );
      mEditName.setKeyListener( null );
    }
  }

  private void exportSurvey()
  {
    if ( app.getSurveyId() < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
    } else {
      String filename = null;
      switch ( app.mExportType ) {
        case TopoDroidApp.DISTOX_EXPORT_TLX:
          filename = app.exportSurveyAsTlx();
          break;
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
      if ( filename != null ) {
        Toast.makeText( this, getString(R.string.saving_) + filename, Toast.LENGTH_LONG ).show();
      } else {
        Toast.makeText( this, R.string.saving_file_failed, Toast.LENGTH_LONG ).show();
      }
    }
  }

  public void deleteSurvey()
  {
    if ( app.mSID < 0 ) return;
    app.mData.doDeleteSurvey( app.mSID );
    app.setSurveyFromName( null );
    finish();
  }
 
  public void addLocation( String station, double latitude, double longitude, double altitude )
  {
    app.addFixed( station, latitude, longitude, altitude );

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("\nfix %s %f %f %f m\n", station, latitude, longitude, altitude );
    DistoXAnnotations.append( app.getSurvey(), sw.getBuffer().toString() );
  }
}
