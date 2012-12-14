/** @file DistoXFixedAdapter.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid adapter for fixed station info
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20120603 created
 */
package com.android.DistoX;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;

// import android.util.Log;

class DistoXFixedAdapter extends ArrayAdapter< FixedInfo >
{
  // static final String TAG = "DistoX";

  private List< FixedInfo > items;
  private Context context;

  public DistoXFixedAdapter( Context ctx, int id, List< FixedInfo > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  public FixedInfo get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, null );
    }

    FixedInfo b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      // Log.v( TAG, "FixedInfo " + b.toString() );
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}

