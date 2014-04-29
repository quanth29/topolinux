/** @file DistoXDBlockAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20131118 background color for non-acceptable shots 
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

class DistoXDBlockAdapter extends ArrayAdapter< DistoXDBlock >
{
  private ArrayList< DistoXDBlock > items;
  private Context context;
  boolean show_ids;  //!< whether to show data ids

  public DistoXDBlockAdapter( Context ctx, int id, ArrayList< DistoXDBlock > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  public DistoXDBlock get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, null );
    }

    DistoXDBlock b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString( show_ids ) );
      tw.setTextSize( TopoDroidApp.mTextSize );
      tw.setTextColor( b.color() );
      if ( b.isRecent() ) {
        tw.setBackgroundColor( 0xff000033 ); // dark-blue
      } else if ( ! b.isAcceptable( ) ) {
        tw.setBackgroundColor( 0xff330000 ); // dark-red
      } else {
        tw.setBackgroundColor( 0xff000000 ); // black
      }
    }
    return v;
  }

  public int size() 
  {
    return items.size();
  }

}

