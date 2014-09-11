/* @file CalibActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 * 20120524 added device in CalibInfo
 * 20120531 activated doDelete with askDelete first
 * 20120725 TopoDroidApp log
 * 20121124 calibration-device consistency check
 * 20131201 button bar new interface. reorganized actions
 * 20140416 setError for required EditText inputs
 */
package com.topodroid.DistoX;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.app.Activity;
// import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;

import android.app.Application;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;

import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;


public class CalibActivity extends Activity
                           // implements OnItemClickListener
                           implements View.OnClickListener
{
  private static int icons[] = { R.drawable.ic_save,
                        R.drawable.ic_open,
                        R.drawable.ic_export,
                        R.drawable.ic_read,
                        R.drawable.ic_close,
                        R.drawable.ic_pref,
                        R.drawable.ic_help
                     };
  private static int help_texts[] = { R.string.help_save,
                        R.string.help_open_calib,
                        R.string.help_export_calib,
                        R.string.help_coeff,
                        R.string.help_delete_calib,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private EditText mEditName;
  private EditText mEditDate;
  private EditText mEditDevice;
  private EditText mEditComment;

  private RadioButton mCBAlgoAuto;
  private RadioButton mCBAlgoLinear;
  private RadioButton mCBAlgoNonLinear;

  private MenuItem mMIdelete;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

  private TopoDroidApp mApp;
  private boolean isSaved;

  private void setButtons( )
  {
    mButton1[1].setEnabled( isSaved );   // open
    mButton1[2].setEnabled( isSaved );   // export
    mButton1[3].setEnabled( isSaved );   // coeff
    // mButton1[4].setEnabled( isSaved );   // delete
    if ( isSaved ) {
      mButton1[1].setBackgroundResource(  icons[1] );
      mButton1[2].setBackgroundResource(  icons[2] );
      mButton1[3].setBackgroundResource(  icons[3] );
      // mButton1[4].setBackgroundResource(  icons[4] );
    } else {
      mButton1[1].setBackgroundResource(  R.drawable.ic_open_no );
      mButton1[2].setBackgroundResource(  R.drawable.ic_export_no );
      // mButton1[3].setBackgroundResource(  R.drawable.ic_coeff_no );
      // mButton1[4].setBackgroundResource(  R.drawable.ic_close_no );
    }
  }

// -------------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  // private Button[] mButton2;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calib_activity);
    mEditName    = (EditText) findViewById(R.id.calib_name);
    mEditDate    = (EditText) findViewById(R.id.calib_date);
    mEditDevice  = (EditText) findViewById(R.id.calib_device);
    mEditComment = (EditText) findViewById(R.id.calib_comment);

    mCBAlgoAuto      = (RadioButton) findViewById( R.id.calib_algo_auto );
    mCBAlgoLinear    = (RadioButton) findViewById( R.id.calib_algo_linear );
    mCBAlgoNonLinear = (RadioButton) findViewById( R.id.calib_algo_non_linear );

    // mButtonHelp = (Button)findViewById( R.id.help );
    // mButtonHelp.setOnClickListener( this );
    // if ( TopoDroidApp.mHideHelp ) {
    //   mButtonHelp.setVisibility( View.GONE );
    // } else {
    //   mButtonHelp.setVisibility( View.VISIBLE );
    // }

    int nr_button1 = 4;
    mButton1 = new Button[ nr_button1 ];
    for ( int k=0; k<nr_button1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      mButton1[k].setBackgroundResource(  icons[k] );
    }

    mButtonView1 = new HorizontalButtonView( mButton1 );
    // mButtonView2 = new HorizontalButtonView( mButton2 );
    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setAdapter( mButtonView1.mAdapter );

    mApp     = (TopoDroidApp)getApplication();
    // TopoDroidApp.Log( TopoDroidApp.LOG_CALIB, "app mCID " + mApp.mCID );

    setNameEditable( mApp.mCID >= 0 );
    if ( isSaved ) {
      CalibInfo info = mApp.getCalibInfo();
      mEditName.setText( info.name );
      // mEditName.setEditable( false );
      mEditDate.setText( info.date );
      if ( info.device != null && info.device.length() > 0 ) {
        mEditDevice.setText( info.device );
      } else if ( mApp.distoAddress() != null ) {
        mEditDevice.setText( mApp.distoAddress() );
      }
      if ( info.comment != null && info.comment.length() > 0 ) {
        mEditComment.setText( info.comment );
      } else {
        mEditComment.setHint( R.string.description );
      }
      switch ( info.algo ) {
        case 0: mCBAlgoAuto.setChecked( true ); break;
        case 1: mCBAlgoLinear.setChecked( true ); break;
        case 2: mCBAlgoNonLinear.setChecked( true ); break;
        default: mCBAlgoAuto.setChecked( true ); break;
      }
    } else {
      mEditName.setHint( R.string.name );
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mEditDate.setText( sdf.format( new Date() ) );
      mEditDevice.setText( mApp.distoAddress() );
      mEditComment.setHint( R.string.description );
      mCBAlgoAuto.setChecked( true );
    }

    setButtons();
  }

  // ---------------------------------------------------------------

  @Override
  public void onClick(View view)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "onClick(View) " + view.toString() );
    Button b = (Button)view;

    // if ( b == mButton1[0] ) {
    //   finish();
    // } else
    if ( b == mButton1[0] ) {
      doSave();
    } else if ( b == mButton1[1] ) {
      if ( ! mApp.checkCalibrationDeviceMatch() ) {
        // FIXME use alert dialog
        Toast.makeText( this, R.string.calib_device_mismatch, Toast.LENGTH_LONG ).show();
      }
      doOpen();
    } else if ( b == mButton1[2] ) { // export
      new CalibExportDialog( this, this ).show();
    } else if ( b == mButton1[3] ) {
      showCoeffs();
    // } else if ( b == mButton1[4] ) {
    //   askDelete();
    // } else if ( b == mButton1[5] ) {
    //   doExport();
    // } else if ( b == mButton1[*] ) {
    //   Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
    //   optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
    //   startActivity( optionsIntent );
    // } else if ( b == mButtonHelp ) {
    //   (new HelpDialog(this, icons, help_texts ) ).show();
    }
  }

  private void showCoeffs()
  {
    byte[] coeff = Calibration.stringToCoeff( mApp.mData.selectCalibCoeff( mApp.mCID ) );
    Matrix mG = new Matrix();
    Matrix mM = new Matrix();
    Vector vG = new Vector();
    Vector vM = new Vector();
    Vector nL = new Vector();
    Calibration.coeffToG( coeff, vG, mG );
    Calibration.coeffToM( coeff, vM, mM );
    Calibration.coeffToNL( coeff, nL );
   
    CalibResult res = new CalibResult();
    mApp.mData.selectCalibError( mApp.mCID, res );
    (new CalibCoeffDialog( this, mApp, vG, mG, vM, mM, nL, res.error, res.max_error, res.iterations, coeff )).show();
  }

  private void askDelete()
  {
    AlertDialog.Builder alert = new AlertDialog.Builder( this );
    // alert.setTitle( R.string.delete );
    alert.setMessage( getResources().getString( R.string.calib_delete ) + " " + mApp.myCalib + " ?" );
    
    alert.setPositiveButton( R.string.button_ok, 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "calib delite" );
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

  private void doOpen()
  {
    Intent openIntent = new Intent( this, GMActivity.class );
    startActivity( openIntent );
  }

  private void doSave( )
  {
    String name = mEditName.getText().toString().trim();
    if ( name == null || name.length() == 0 ) {
      String error = getResources().getString( R.string.error_name_required );
      mEditName.setError( error );
      return;
    }

    String date = mEditDate.getText().toString();
    String device = mEditDevice.getText().toString();
    String comment = mEditComment.getText().toString();
    if ( date    != null ) { date    = date.trim(); }
    if ( device  != null ) { device  = device.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    if ( isSaved ) { // calib already saved
      mApp.mData.updateCalibInfo( mApp.mCID, date, device, comment );
      Toast.makeText( this, R.string.calib_updated, Toast.LENGTH_SHORT ).show();
    } else { // new calib
      name = TopoDroidApp.noSpaces( name );
      if ( name != null && name.length() > 0 ) {
        if ( mApp.hasCalibName( name ) ) { // name already exists
          // Toast.makeText( this, R.string.calib_exists, Toast.LENGTH_SHORT ).show();
          String error = getResources().getString( R.string.calib_exists );
          mEditName.setError( error );
        } else {
          mApp.setCalibFromName( name );
          mApp.mData.updateCalibInfo( mApp.mCID, date, device, comment );
          setNameEditable( true );
          Toast.makeText( this, R.string.calib_saved, Toast.LENGTH_SHORT ).show();
        }
      } else {
        Toast.makeText( this, R.string.calib_no_name, Toast.LENGTH_SHORT ).show();
      }
    }
    int algo = 0;
    if ( mCBAlgoLinear.isChecked() ) algo = 1;
    else if ( mCBAlgoNonLinear.isChecked() ) algo = 2;
    mApp.mData.updateCalibAlgo( mApp.mCID, algo );

    setButtons();
  }
  
  private void setNameEditable( boolean saved )
  {
    isSaved = saved;
    if ( isSaved ) {
      mEditName.setFocusable( false );
      mEditName.setClickable( false );
      mEditName.setKeyListener( null );
      mEditDevice.setFocusable( false );
      mEditDevice.setClickable( false );
      mEditDevice.setKeyListener( null );
    }
  }

  public void doDelete()
  {
    if ( mApp.mCID < 0 ) return;
    mApp.mData.doDeleteCalib( mApp.mCID );
    mApp.setCalibFromName( null );
    finish();
  }

  // ---------------------------------------------------------
  // MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIdelete  = menu.add( R.string.menu_delete );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIdelete.setIcon( R.drawable.ic_close );
    mMIoptions.setIcon( R.drawable.ic_pref );
    mMIhelp.setIcon( R.drawable.ic_help );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_CALIB );
      startActivity( intent );
    } else if ( item == mMIdelete  ) { // DELETE DIALOG
      askDelete();
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      (new HelpDialog(this, icons, help_texts ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  void doExport( int exportType, boolean warn )
  {
    if ( mApp.mCID < 0 ) {
      if ( warn ) {
        Toast.makeText( this, R.string.no_calibration, Toast.LENGTH_SHORT).show();
      }
    } else {
      String filename = null;
      switch ( exportType ) {
        case TopoDroidApp.DISTOX_EXPORT_CSV:
          filename = mApp.exportCalibAsCsv();
      }
      if ( warn ) { 
        if ( filename != null ) {
          Toast.makeText( this, getString(R.string.saving_) + filename, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText( this, R.string.saving_file_failed, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }
}
