/** @file DistoXClosureAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

class DistoXClosureAdapter extends ArrayAdapter< DistoXNum.Closure >
{
  private ArrayList< DistoXNum.Closure > items;
  private Context context;

  public DistoXClosureAdapter( Context ctx, int id, ArrayList< DistoXNum.Closure > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  public DistoXNum.Closure get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, null );
    }

    DistoXNum.Closure b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      // set color RED if error is too big 
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}


