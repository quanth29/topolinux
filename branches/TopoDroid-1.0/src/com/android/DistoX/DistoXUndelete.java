/* @file DistoXUndelete.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid undelete survey item activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 cstr signature
 */
package com.android.DistoX;

import java.util.List;
import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.view.View;
import android.view.View.OnClickListener;

public class DistoXUndelete extends Dialog
                            implements OnItemClickListener, View.OnClickListener
{
  public long mSID;
  DistoXDataHelper mData;
  ShotActivity mParent;

  private Button btnCancel;
  ArrayAdapter< String >  mArrayAdapter;
  ListView mList;

  public DistoXUndelete( Context context, ShotActivity parent, DistoXDataHelper data, long sid )
  {
    super( context );
    mParent = parent;
    mData   = data;
    mSID    = sid;
  }

  public void onClick(View v) 
  {
    dismiss();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String[] value = item.toString().split( " " );
    
    if ( value.length >= 2 ) {
      if ( value[0].equals( "shot" ) ) {
        mData.undeleteShot( Long.parseLong( value[1] ), mSID );
      } else {
        mData.undeletePlot( Long.parseLong( value[1] ), mSID );
      }
    }
    dismiss();
  }

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.undelete);
    mArrayAdapter = new ArrayAdapter<String>( mParent, R.layout.message );
    mList = (ListView) findViewById(R.id.list_undelete);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // TODO fill the list
    List< DistoXDBlock > shots = mData.selectAllShots( mSID, 1 );
    List< DistoXPlot > plots   = mData.selectAllPlots( mSID, 1 );
    if ( shots.size() == 0 && plots.size() == 0 ) {
      Toast.makeText( mParent, "No item to undelete", Toast.LENGTH_LONG ).show();
      dismiss();
      // finish();
    }
    for ( DistoXDBlock b : shots ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("shot %d <%s> %.2f %.1f %.1f", b.mId, b.Name(), b.mLength, b.mBearing, b.mClino );
      String result = sw.getBuffer().toString();
      mArrayAdapter.add( result );
    }
    for ( DistoXPlot p : plots ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("plot %d <%s> %s", p.id, p.name, p.getTypeString() );
      String result = sw.getBuffer().toString();
      mArrayAdapter.add( result );
    }
    btnCancel = (Button) findViewById(R.id.button_undelete );
    btnCancel.setOnClickListener( this );
  }
}
