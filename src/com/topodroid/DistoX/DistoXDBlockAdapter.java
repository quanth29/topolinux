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
 * 20140515 added parent
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

import android.util.Log;

class DistoXDBlockAdapter extends ArrayAdapter< DistoXDBlock >
{
  private Context mContext;
  private ShotActivity mParent;
  private ArrayList< DistoXDBlock > items;
  private ArrayList< TextView > texts;     // array of textviews (aligned with that of items
  private ArrayList< View > views;     // array of textviews (aligned with that of items
  boolean show_ids;  //!< whether to show data ids

  public DistoXDBlockAdapter( Context ctx, ShotActivity parent, int id, ArrayList< DistoXDBlock > items,
                              ArrayList< TextView > texts, ArrayList< View > views )
  {
    super( ctx, id, items );
    this.mContext = ctx;
    this.mParent  = parent;
    this.items    = items;
    this.texts    = texts;
    this.views    = views;
  }

  public DistoXDBlock get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, null );
    }

    DistoXDBlock b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      if ( pos < texts.size() ) {
        texts.set( pos, tw );
        views.set( pos, v );
      } else {
        while ( texts.size()+1 < pos ) {
          texts.add( null );
          views.add( null );
        }
        texts.add( tw );
        views.add( v );
      }
      setViewText( tw, b );
    }
    return v;
  }

  private void setViewText( TextView tw, DistoXDBlock b ) 
  {
    tw.setText( b.toString( show_ids ) );
    tw.setTextSize( TopoDroidApp.mTextSize );
    tw.setTextColor( b.color() );
    if ( b.isRecent( mParent.secondLastShotId() ) ) {
      tw.setBackgroundColor( 0xff000033 ); // dark-blue
    } else if ( ! b.isAcceptable( ) ) {
      tw.setBackgroundColor( 0xff330000 ); // dark-red
    } else {
      tw.setBackgroundColor( 0xff000000 ); // black
    }
  }

  public int size() 
  {
    return items.size();
  }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  void updateBlockView( DistoXDBlock blk ) 
  {
    for ( int k=0; k<items.size(); ++k ) {
      if ( items.get(k) == blk) {
        // Log.v( "DistoX", "update block view pos " + k + " " + blk.toString(true) );
        if ( k < texts.size() ) {
          TextView tv = texts.get(k);
          if ( tv != null ) setViewText( tv, blk );
          // View v = views.get(k);
          // if ( v != null ) v.invalidate();
        }
        return;
      }
    }
  }

  // void updateBlockViews( int k1, int k2 )
  // {
  //   if ( k1 < 0 ) k1 = 0;
  //   if ( k2 > views.size() ) k2 = views.size();
  //   for ( int k = k1; k < k2; ++k ) {
  //     View v = views.get( k );
  //     if ( v != null ) v.invalidate();
  //   }
  // }

}

