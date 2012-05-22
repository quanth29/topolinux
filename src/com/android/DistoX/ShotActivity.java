/* @file ShotActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created from DistoX.java
 */
package com.android.DistoX;

import java.io.File;
import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import android.app.Application;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;
import android.os.Bundle;

import android.util.Log;

// import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

/*
  Method m = device.getClass().getMethod( "createRfcommSocket", new Class[] (int.class) );
  socket = (BluetoothSocket) m.invoke( device, 2 );
  socket.connect();
*/

public class ShotActivity extends Activity
                          implements OnItemClickListener, ILister, INewPlot
{
  private static final String TAG = "DistoX";
  private TopoDroidApp app;

  // private static final int REQUEST_DEVICE    = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  private boolean mSplay = true; //!< whether to show splay shots
  private boolean mLeg   = true; //!< whether to hide leg extra shots
  private boolean mBlank = true; //!< whether to hide blank shots
  // private Bundle mSavedState = null;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DistoXDBlockAdapter   mDataAdapter;
  private long mSIDid = -1;    // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  // private int      mSaveTextPos  = 0;
  private DistoXDBlock mSaveBlock = null;

  private MenuItem mMIdevice = null;
  private SubMenu  mSMsurvey;
  private MenuItem mMIsplay;
  private MenuItem mMIleg;
  private MenuItem mMIblank;
  private MenuItem mMInumber;
  private MenuItem mMIplotnew;
  private MenuItem mMIshotnew;
  private MenuItem mMIundelete;
  // private MenuItem mMIlocation;
  private MenuItem mMIphoto;
  private MenuItem mMIplot;
  private MenuItem mMInotes;
  private SubMenu  mSMmore;
  private MenuItem mMIrefresh;
  private MenuItem mMIdownload = null;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;


  // -------------------------------------------------------------------

  private boolean numberSplays()
  { 
    long sid = app.mSID;
    if ( sid < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return false;
    } else {
      List<DistoXDBlock> list = app.mData.selectAllShots( sid, 0 ); // status = 0
      int size = list.size();
      int from = 0;
      int k;
      DistoXDBlock prev = null;
      for ( k=0; k<size; ++k ) {
        DistoXDBlock item = list.get( k );
        int t = item.type();
        if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          from = k+1;
        } else if ( t == DistoXDBlock.BLOCK_CENTERLINE ) {
          prev = item;
          if ( from < k ) { // assign splay name
            String name = item.mFrom;
            if ( name != null ) {
              // Log.v( TAG, "update splays from " + from + " to " + k + " with name: <" + name + ">" );
              for ( ; from < k; ++from ) {
                DistoXDBlock cur = list.get( from );
                cur.setName( name, "" );
                app.mData.updateShotName( cur.mId, sid, name, "" );
              }
            }
          }
        } else if ( t == DistoXDBlock.BLOCK_BLANK ) {
          if ( item.relativeDistance( prev ) < app.mCloseDistance ) {
            from = k+1;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void refreshDisplay( int nr ) 
  {
    if ( nr >= 0 ) {
      if ( nr > 0 ) updateDisplay( true );
      Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_LONG ).show();
    } else if ( nr < 0 ) {
      // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_LONG ).show();
      Toast.makeText( this, app.DistoXConnectionError[ -nr ], Toast.LENGTH_LONG ).show();
    }
  }
    
  public void updateDisplay( boolean force_update )
  {
    // Log.v( TAG, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( force_update ) {
      DistoXDataHelper data = app.mData;
      if ( data != null && app.mSID >= 0 ) {
        List<DistoXDBlock> list = data.selectAllShots( app.mSID, 0 );
        // Log.v( TAG, "update shot list size " + list.size() );
        updateShotList( list );
        setTitle( app.getSurvey() );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    }
  }

  private void updateShotList( List<DistoXDBlock> list )
  {
    // Log.v(TAG, "updateShotList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.no_shots, Toast.LENGTH_LONG ).show();
      return;
    }
    DistoXDBlock prev = null;
    boolean prev_is_leg = false;
    for ( DistoXDBlock item : list ) {
      DistoXDBlock cur = item;
      // Log.v(TAG, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      int t = cur.type();
      if ( cur.relativeDistance( prev ) < app.mCloseDistance ) {
        if ( mLeg ) { // hide leg extra shots
          // Log.v(TAG, "close distance");
          if ( mBlank && prev.type() == DistoXDBlock.BLOCK_BLANK ) {
            // prev was skipped: draw it now
            if ( ! prev_is_leg ) {
              cur = prev;
              prev_is_leg = true;
            } else {
              continue;
            }
          } else {
            continue;
          }
        } else {
          /* nothing */
        }
      } else {
        // Log.v(TAG, "not close distance");
        prev_is_leg = false;
        if ( t == DistoXDBlock.BLOCK_BLANK ) {
          prev = cur;
          if ( mBlank ) continue;
        } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          prev = null;
          if ( mSplay ) continue;
        } else { // t == DistoXDBlock.BLOCK_CENTERLINE
          prev = cur;
        }
      }
      // Log.v(TAG, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    if ( value.equals( getResources().getString( R.string.back_to_survey ) ) ) {
      updateDisplay( true );
      return;
    }
    // setListPos( position  );
    startShotDialog( (TextView)view, position );
  }

  public void startShotDialog( TextView tv, int pos )
  {
     mSaveBlock = mDataAdapter.get(pos);
     mSIDid = mSaveBlock.mId;

     String msg = tv.getText().toString();
     String[] st = msg.split( " ", 6 );
     mSaveTextView = tv;
     mSaveData = st[2] + " " + st[3] + " " + st[4];
     
     DistoXDBlock prevBlock = null;
     if ( mSaveBlock.type() == DistoXDBlock.BLOCK_BLANK ) {
       prevBlock = app.mData.selectPreviousLegShot( mSIDid, app.mSID );
       // Log.v(TAG, "prev leg " + prevBlock.mFrom + " " + prevBlock.mTo );
     }

     (new ShotDialog( this, this, mSaveBlock, mSaveData, prevBlock )).show();
  }


  // ---------------------------------------------------------------
  // OPTIONS MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.option_menu_none, menu);

    mMIdownload = menu.add( R.string.menu_download );
    mSMsurvey = menu.addSubMenu( R.string.menu_survey );
      mMIsplay  = mSMsurvey.add( R.string.menu_splay );
        mMIsplay.setCheckable( true );
        mMIsplay.setChecked( mSplay );
      mMIleg = mSMsurvey.add( R.string.menu_leg );
        mMIleg.setCheckable( true );
        mMIleg.setChecked( mLeg );
      mMIblank = mSMsurvey.add( R.string.menu_blank );
        mMIblank.setCheckable( true );
        mMIblank.setChecked( mBlank );
      mMInumber  = mSMsurvey.add( R.string.menu_number );
      mMIrefresh = mSMsurvey.add( R.string.menu_refresh );
    mMInotes    = menu.add( R.string.menu_notes );

    mMIplot     = menu.add( R.string.menu_plot );
    mMIplotnew  = menu.add( R.string.menu_plot_new );

    mSMmore = menu.addSubMenu( R.string.menu_more );
      mMIdevice   = mSMmore.add( R.string.menu_device );
      mMIshotnew  = mSMmore.add( R.string.menu_shot_new );
      mMIundelete = mSMmore.add( R.string.menu_undelete );
      // mMIlocation = mSMmore.add( R.string.menu_location );
      mMIphoto    = mSMmore.add( R.string.menu_photo );
      mMIoptions  = mSMmore.add( R.string.menu_options );
      mMIhelp     = mSMmore.add( R.string.menu_help  );

    // Log.v( TAG, "menu size " + menu.size() );
    // menu has size 7

    // mMIdevice.setIcon( R.drawable.distox ); 
    mMIdownload.setIcon( R.drawable.distox );
    mSMsurvey.setIcon( R.drawable.survey );
    mMInotes.setIcon( R.drawable.compose );

    mMIplot.setIcon( R.drawable.scrap );
    mMIplotnew.setIcon( R.drawable.scrapnew );
    mSMmore.setIcon( R.drawable.more );

    setBTMenus( app.mBTAdapter.isEnabled() );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // Log.v( TAG, "onOptionsItemSelected() " + StatusName() );
    // Handle item selection
    if ( item == mMIrefresh ) {
      updateDisplay( true );
    } else if ( item == mMIdownload ) {
      new DistoXRefresh( app, this ).execute();
      // updateDisplay( true );
    } else if ( item == mMInotes ) { // ANNOTATIONS DIALOG
      if ( app.getSurvey() != null ) {
        Intent notesIntent = new Intent( this, DistoXAnnotations.class );
        notesIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, app.getSurvey() );
        startActivity( notesIntent );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
      startActivity( optionsIntent );
    } else if ( item == mMIhelp ) { // HELP DIALOG
      TopoDroidHelp.show( this, R.string.help_shot );
    } else if ( item == mMIundelete ) { // UNDELETE SURVEY ITEM
      if ( app.mData != null && app.mSID >= 0 ) {
        (new DistoXUndelete(this, this, app.mData, app.mSID ) ).show();
        updateDisplay( true );
      } else {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      }
    // } else if ( item == mMIlocation ) {
    //   LocationManager lm = (LocationManager) getSystemService( LOCATION_SERVICE );
    //   DistoXLocation loc = new DistoXLocation( this, this, lm );
    //   loc.show();
    } else if ( item == mMIsplay ) {     // toggle splay shots
      mSplay = ! mMIsplay.isChecked();
      mMIsplay.setChecked( mSplay );
      updateDisplay( true );
    } else if ( item == mMIleg ) { // toggle leg extra shots
      mLeg =  ! mMIleg.isChecked();
      mMIleg.setChecked( mLeg );
      updateDisplay( true );
    } else if ( item == mMIblank ) {     // toggle blank shots
      mBlank = ! mMIblank.isChecked();
      mMIblank.setChecked( mBlank );
      updateDisplay( true );
    } else if ( item == mMInumber ) {    // autonumber splays
      // TODO number splay shots
      if ( numberSplays() ) {
        updateDisplay( true );
      }
    // // ---------------------- DEVICES
    } else if ( item == mMIdevice ) {
      if ( app.mBTAdapter.isEnabled() ) {
        Intent deviceIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
        startActivity( deviceIntent );
      }
    // ---------------------- SHOTS
    } else if ( item == mMIshotnew ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        (new ShotNewDialog( this, this )).show();
        updateDisplay( true );
      }
    // ---------------------- PLOTS
    } else if ( item == mMIplotnew ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        (new DistoXPlotDialog( this, this )).show();
        // FIXME start Drawing activity
        // updateDisplay( true );
      }
    } else if ( item == mMIplot ) {
      if ( app.mSID < 0 ) {
        Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      } else {
        new PlotDialog( this, this, app ).show();
      }
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }
  // ---------------------------------------------------------------

  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main);
    app = (TopoDroidApp) getApplication();
    mDataAdapter = new DistoXDBlockAdapter( this, R.layout.row, new ArrayList<DistoXDBlock>() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // setTitleColor( 0x006d6df6 );
    restoreInstanceFromData();
    updateDisplay( true );
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    saveInstanceToData();
  }

  private void restoreInstanceFromData()
  { 
    String shots = app.mData.getValue( "DISTOX_SHOTS" );
    // Log.v( TAG, "DISTOX_SHOTS " + shots );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      mSplay  = vals[0].equals("1");
      mLeg    = vals[1].equals("1");
      mBlank  = vals[2].equals("1");
    }
  }
    
  private void saveInstanceToData()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d %d %d", mSplay?1:0, mLeg?1:0, mBlank?1:0 );
    app.mData.setValue( "DISTOX_SHOTS", sw.getBuffer().toString() );
  }


  // ------------------------------------------------------------------

  private void setBTMenus( boolean enabled )
  {
    if ( mMIdevice != null )   mMIdevice.setEnabled( enabled );
    if ( mMIdownload != null ) mMIdownload.setEnabled( enabled );
  }

  public void makeNewPlot( String name, long type, String start, String view )
  {
    long mPID = app.mData.insertPlot( name, app.mSID, type, start, view );
    // Log.v( TAG, "insertPLot return " + mPID + " " + name + " start " + start + " view " + view );
    if ( mPID >= 0 ) {
      startDrawingActivity( start, name, type, view );
    }
    updateDisplay( true );
  }

  public void startPlot( String plot_name, String type )
  {
    DistoXDataHelper data = app.mData;
    long mPID = data.getPlotId( app.getSurveyId(), plot_name );
    if ( mPID >= 0 ) {
      long id = app.mSID;
      // long plot_type = data.getPlotFieldAsLongType( id, mPID, "type" );
      String plot_start = data.getPlotFieldAsString( id, mPID, "start" );
      // String plot_view = data.getPlotFieldAsString( id, mPID, "view" );
      long plot_type = TopoDroidApp.PLOT_PLAN;
      if ( type.equals("V-SECTION") ) { plot_type = TopoDroidApp.PLOT_V_SECTION; }
      else if ( type.equals("PLAN") ) { plot_type = TopoDroidApp.PLOT_PLAN; }
      else if ( type.equals("EXTENDED") ) { plot_type = TopoDroidApp.PLOT_EXTENDED; }
      else if ( type.equals("H-SECTION") ) { plot_type = TopoDroidApp.PLOT_H_SECTION; }
      startDrawingActivity( plot_start, plot_name, plot_type, null );
      updateDisplay( true );
    } else {
      Toast.makeText(getApplicationContext(), R.string.plot_not_found, Toast.LENGTH_LONG).show();
    }
  }

  private void startDrawingActivity( String start, String plot_name, long plot_type, String view )
  {
    if ( app.mSID < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_LONG ).show();
      return;
    }
    // FIXME what if plot_name already exists ? 
    long mPID = app.mData.getPlotId( app.mSID, plot_name );
    String filename   = app.getSurvey() + "-" + plot_name;
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, app.getSurveyId() );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID,   mPID );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_STRT, start );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_VIEW, view );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, plot_type );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FILE, filename );
    // Log.v( TAG, "startDrawing plot " + plot_name + " SID " + app.getSurveyId() + " PID " + mPID + " start at " + start );
    startActivity( drawIntent );
  }

  public void makeNewShot( String from, String to, float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down )
  {
    long id;
    long sid = app.mSID;
    DistoXDataHelper data = app.mData;
    if ( from != null && to != null && from.length() > 0 && to.length() > 0 ) {
      // if ( data.makesCycle( -1L, sid, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_LONG ).show();
      // } else
      {
        // Log.v( TAG, "Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > app.mVThreshold );
        // Log.v( TAG, "SID " + sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = Float.parseFloat( left );
          if ( horizontal ) {
            id = data.insertShot( sid, l, 270.0f, 0.0f, 0.0f );
          } else {
            float b = bearing - 90.0f;
            if ( b < 0.0f ) b += 360.0f;
            // b = in360( b );
            id = data.insertShot( sid, l, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( right != null && right.length() > 0 ) {
          float r = Float.parseFloat( right );
          if ( horizontal ) {
            id = data.insertShot( sid, r, 90.0f, 0.0f, 0.0f );
          } else {
            float b = bearing + 90.0f;
            if ( b >= 360.0f ) b -= 360.0f;
            id = data.insertShot( sid, r, b, 0.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( up != null && up.length() > 0 ) {
          float u = Float.parseFloat( up );
          if ( horizontal ) {
            id = data.insertShot( sid, u, 0.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, u, 0.0f, 90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        if ( down != null && down.length() > 0 ) {
          float d = Float.parseFloat( down );
          if ( horizontal ) {
            id = data.insertShot( sid, d, 180.0f, 0.0f, 0.0f );
          } else {
            id = data.insertShot( sid, d, 0.0f, -90.0f, 0.0f );
          }
          data.updateShotName( id, sid, from, "" );
        }
        id = data.insertShot( sid, distance, bearing, clino, 0.0f );
        // String name = from + "-" + to;
        data.updateShotName( id, sid, from, to );
        // data.updateShotExtend( id, sid, extend );
        updateDisplay( true );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_LONG ).show();
    }
  }

  public void dropShot()
  {
    app.mData.deleteShot( mSIDid, app.mSID );
    updateDisplay( true ); // FIXME
  }

  public void updateShot( String from, String to, long extend, long flag, String comment )
  {
    // Log.v( TAG, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    int ret = app.mData.updateShot( mSIDid, app.mSID, from, to, extend, flag, comment );
    if ( ret == -1 ) {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_LONG ).show();
    // } else if ( ret == -2 ) {
    //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_LONG ).show();
    } else {
      if ( app.mListRefresh ) {
        // This works but it refreshes the whole list
        mDataAdapter.notifyDataSetChanged();
        // mList.smoothScrollToPosition( mSaveTextPos );
        // mSaveTextView.requestLayout();
        // mSaveTextView.requestFocus();
      } else {
        mSaveTextView.setText( mSaveBlock.toString() );
        mSaveTextView.setTextColor( mSaveBlock.color() );
      }
    }
  }
}
