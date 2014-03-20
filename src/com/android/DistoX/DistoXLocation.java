/* @file DistoXLocation.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid GPS-location for fixed stations
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 comments
 * 20120521 parented with SurveyActivity
 * 20120603 fixed-info list
 * 20120726 TopoDroid log
 * 20121114 manual fixed station
 * 20121205 location units
 * 20130910 using GPSAveraging
 */
package com.android.DistoX;

// import java.Thread;
import java.util.Iterator;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import android.view.View;
import android.widget.ListView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;


// import android.util.Log;

public class DistoXLocation extends Dialog
                            implements View.OnClickListener
                                     , AdapterView.OnItemClickListener
                                     , TextView.OnEditorActionListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  // private boolean  mLocated;
  private LocationManager locManager;
  private Context mContext;
  private TopoDroidApp app;

  private TextView mTVlat;
  private TextView mTVlong;
  private TextView mTValt;
  private EditText mETstation;
  private Button   mBtnLoc;
  private Button   mBtnAdd;
  private Button   mBtnMan;
  private Button   mBtnStatus;
  // private Button   mBtnCancel;
  private ListView mList;
  private FixedAdapter mFixedAdapter;
  // private TextView  mSaveTextView;
  private int mSavePos;
  private FixedInfo mSaveFixed;

  double mLatitude;   // decimal degrees
  double mLongitude;  // decimal degrees
  double mAltitude;   // meters
  private double mAltimetric; // altimetric altitude
  private SurveyActivity mParent;
  private GpsStatus mStatus;
  private boolean mLocating; // whether is locating

  public DistoXLocation( Context context, SurveyActivity parent, TopoDroidApp _app, LocationManager lm )
  {
    super(context);
    mContext = context;
    mParent = parent;
    app = _app;
    locManager = lm;
    mStatus = locManager.getGpsStatus( null );
    mLocating = false;

    // Log.v(  TopoDroidApp.TAG, "UnitLocation " + TopoDroidApp.mUnitLocation + " ddmmss " + TopoDroidApp.DDMMSS );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "Location onCreate" );
    setContentView(R.layout.distox_location);
    mTVlong = (TextView) findViewById(R.id.longitude );
    mTVlat  = (TextView) findViewById(R.id.latitude  );
    mTValt  = (TextView) findViewById(R.id.altitude  );
    mETstation = (EditText) findViewById( R.id.station );
    mBtnStatus = (Button) findViewById( R.id.status );

    mETstation.setOnEditorActionListener( this );

    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );

    mBtnLoc = (Button) findViewById( R.id.button_loc );
    mBtnAdd = (Button) findViewById(R.id.button_add );
    mBtnMan = (Button) findViewById(R.id.button_manual );
    mBtnLoc.setOnClickListener( this );
    mBtnAdd.setOnClickListener( this );
    mBtnMan.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.btn_cancel );
    // mBtnCancel.setOnClickListener( this );

    mBtnLoc.setEnabled( false );
    mBtnAdd.setEnabled( false );
    mBtnMan.setEnabled( false );
    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    // mBtnLoc.setText( getResources.getString( R.string.button_gps_start ) );
    
    // mLocated = false;
    // locManager = (LocationManager) getSystemService( LOCATION_SERVICE );
    mLocating = false;
    setTitle( R.string.title_location );

    refreshList();
  }

  public void refreshList()
  {
    List< FixedInfo > fxds = app.mData.selectAllFixed( app.mSID, TopoDroidApp.STATUS_NORMAL );
    // TopoDroidApp.Log( TopoDroidApp.LOG_DEBUG, "Location::refreshList size " + fxds.size() );
    mFixedAdapter = new FixedAdapter( mContext, R.layout.message, fxds );
    mList.setAdapter( mFixedAdapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_LOC, "Location::onItemClick pos " + pos );
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // // setListPos( position  );
    // mSaveTextView = (TextView) view;
    mSaveFixed = mFixedAdapter.get(pos);
    mSavePos   = pos;
    (new FixedDialog( mContext, mParent, this, mSaveFixed )).show();
  }

  public void updatePos( )
  {
    mFixedAdapter.insert( mSaveFixed, mSavePos );
    mList.invalidate();
  }

  public void addFixedPoint( double lng, // decimal degrees
                             double lat,
                             double alt,  // meters
                             double asl
                           )
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_DEBUG, "addFixedPoint " + lng + " " + lat + " " + alt );
    // FIXME TODO try to get altimetric altitude
    if ( mETstation.getText() != null ) {
      String name = mETstation.getText().toString();
      if ( name.length() > 0 ) {
        FixedInfo f = mParent.addLocation( name, lng, lat, alt, asl );
        // no need to update the adatper: fixeds are not many and can just request
        // the list to the database 
        // mFixedAdapter.add( f );
        refreshList();
      }
      mBtnAdd.setEnabled( false );
    }
  }

  private void setGPSoff()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_start ) );
    locManager.removeUpdates( this );
    locManager.removeGpsStatusListener( this );
    mLocating = false;
    setTitle( R.string.title_location );
  }

  private void setGPSon()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_stop ) );
    mBtnAdd.setEnabled( false );
    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    locManager.addGpsStatusListener( this );
    locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
    mLocating = true;
    setTitle( R.string.title_location_gps );
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "Location onEditorAction " + actionId );
    // if ( actionId == 6 )
    {
      EditText et = (EditText)v;
      if ( et == mETstation ) {
        CharSequence item = v.getText();
        if ( item != null ) {
          String str = item.toString();
          mBtnLoc.setEnabled( str != null && str.length() > 0 );
          mBtnMan.setEnabled( str != null && str.length() > 0 );
        }
      }
    }
    return false;
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "Location onClick button " + b.getText().toString() );
    if ( b == mBtnAdd ) {
      double geo_height = GeodeticHeight.geodeticHeight( mLatitude, mLongitude );
      if ( geo_height > -999 ) {
        mAltimetric = mAltitude - geo_height;
        addFixedPoint( mLongitude, mLatitude, mAltitude, mAltimetric );
      } else {
        addFixedPoint( mLongitude, mLatitude, mAltitude, geo_height/1000 );
        
      }
    } else if ( b == mBtnMan ) {
      // stop GPS location and start dialog for lat/long/alt data
      if ( mLocating ) {
        setGPSoff();
      }
      new LongLatAltDialog( mContext, this ).show();
    } else if ( b == mBtnLoc ) {
      if ( mLocating ) {
        setGPSoff();
      } else {
        if ( ! mParent.tryGPSAveraging( this ) ) {
          TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "Location: failed GPSAveraging" );
          setGPSon();
        }
      }
    // } else if ( b == mBtnCancel ) {
    //   dismiss();
    }
    // refreshList();
  }


  @Override
  public void onBackPressed()
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_INPUT, "Location onBackPressed");
    if ( mLocating ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
      mLocating = false;
    }
    dismiss();
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    displayLocation( loc );
    // mLocated = true;
  }

  // location is stored in decimal degrees but displayed as deg:min:sec
  private void displayLocation( Location loc )
  {
    if ( loc != null ) {
      mLatitude  = loc.getLatitude();
      mLongitude = loc.getLongitude();
      mAltitude  = loc.getAltitude();
      showLocation();
    } else {
      TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "displayLocation null");
    }
  }

  void showLocation()
  {
    if ( TopoDroidApp.mUnitLocation == TopoDroidApp.DDMMSS ) {
      mTVlong.setText( mContext.getResources().getString( R.string.longitude ) + " " + FixedInfo.double2ddmmss( mLongitude ) );
      mTVlat.setText( mContext.getResources().getString( R.string.latitude ) + " " + FixedInfo.double2ddmmss( mLatitude ) );
    } else {
      mTVlong.setText( mContext.getResources().getString( R.string.longitude ) + " " + FixedInfo.double2degree( mLongitude ) );
      mTVlat.setText( mContext.getResources().getString( R.string.latitude ) + " " + FixedInfo.double2degree( mLatitude ) );
    }
    mTValt.setText( mContext.getResources().getString( R.string.altitude ) + " " + Integer.toString( (int)(mAltitude) ) );
  }

  public void onProviderDisabled( String provider )
  {
  }

  public void onProviderEnabled( String provider )
  {
  }

  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    // TopoDroidApp.Log(TopoDroidApp.LOG_LOC, "onStatusChanged status " + status );
  }

  public void onGpsStatusChanged( int event ) 
  {
    if ( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS ) {
      locManager.getGpsStatus( mStatus );
      Iterator< GpsSatellite > sats = mStatus.getSatellites().iterator();
      int  nr = 0;
      while( sats.hasNext() ) {
        GpsSatellite sat = sats.next();
        if ( sat.usedInFix() ) ++nr;
      }
      // TopoDroidApp.Log(TopoDroidApp.LOG_LOC, "onGpsStatusChanged nr satellites used in fix " + nr );
      mBtnStatus.setText( Integer.toString( nr ) );
      switch ( nr ) {
        case 0: mBtnStatus.setBackgroundColor( 0x80ff0000 );
                break;
        case 1: mBtnStatus.setBackgroundColor( 0x80993333 );
                break;
        case 2: mBtnStatus.setBackgroundColor( 0x80663333 );
                break;
        case 3: mBtnStatus.setBackgroundColor( 0x80339933 );
                break;
        default: mBtnStatus.setBackgroundColor( 0x8000ff00 );
                break;
      }
      mBtnAdd.setEnabled( nr >= 4 );

      try {
        Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        displayLocation( loc );
      } catch ( IllegalArgumentException e ) {
        TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "onGpsStatusChanged IllegalArgumentException " );
      } catch ( SecurityException e ) {
        TopoDroidApp.Log(TopoDroidApp.LOG_ERR, "onGpsStatusChanged SecurityException " );
      }
    }
  }

  void setPosition( double lng, double lat, double alt )
  {
    mLongitude = lng;
    mLatitude  = lat;
    mAltitude  = alt;
    showLocation();
    mBtnAdd.setEnabled( true );
  }
}

