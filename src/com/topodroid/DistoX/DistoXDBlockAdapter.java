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
import android.view.View.OnClickListener;

import java.util.List;
import java.util.ArrayList;

import android.util.Log;

class DistoXDBlockAdapter extends ArrayAdapter< DistoXDBlock >
                          implements OnClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  ArrayList< DistoXDBlock > mItems;
  boolean show_ids;  //!< whether to show data ids
  private LayoutInflater mLayoutInflater;

  public DistoXDBlockAdapter( Context ctx, ShotActivity parent, int id, ArrayList< DistoXDBlock > items )
  {
    super( ctx, id, items );
    mContext = ctx;
    mParent  = parent;
    mItems   = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  void addBlock( DistoXDBlock blk ) 
  {
    mItems.add( blk );
  }

  // called by ShotActivity::updateShotlist
  //  
  void reviseBlockWithPhotos( List< PhotoInfo > photos )
  {
    for ( DistoXDBlock b : mItems ) b.mWithPhoto = false; 
    for ( PhotoInfo p : photos ) {
      // mark block with p.shotid
      for ( DistoXDBlock b : mItems ) {
        if ( b.mId == p.shotid ) { 
          b.mWithPhoto = true;
          break;
        }
      }
    }
  }

  public DistoXDBlock get( int pos ) { return mItems.get(pos); }
 
  private class ViewHolder
  { 
    TextView tvId;
    TextView tvFrom;
    TextView tvTo;
    TextView tvLength;
    TextView tvCompass;
    TextView tvClino;
    TextView tvNote;
    TextView textView;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.dblock_row, null );
      holder = new ViewHolder();
      holder.tvId      = (TextView) convertView.findViewById( R.id.id );
      holder.tvFrom    = (TextView) convertView.findViewById( R.id.from );
      holder.tvTo      = (TextView) convertView.findViewById( R.id.to );
      holder.tvLength  = (TextView) convertView.findViewById( R.id.length );
      holder.tvCompass = (TextView) convertView.findViewById( R.id.compass );
      holder.tvClino   = (TextView) convertView.findViewById( R.id.clino );
      holder.tvNote    = (TextView) convertView.findViewById( R.id.note );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    DistoXDBlock b = mItems.get( pos );
    setViewText( holder, b );
    b.mView = convertView;
    convertView.setVisibility( b.mVisible );
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  public int size() { return mItems.size(); }

  private void setViewText( ViewHolder holder, DistoXDBlock b ) 
  {

    holder.tvId.setText( String.format( "%1$d", b.mId ) );
    holder.tvFrom.setText( b.mFrom );
    holder.tvTo.setText( b.mTo );
    holder.tvLength.setText( String.format("%1$.2f", b.mLength * TopoDroidApp.mUnitLength ) );
    holder.tvCompass.setText( String.format("%1$.1f", b.mBearing * TopoDroidApp.mUnitAngle ) );
    holder.tvClino.setText( String.format("%1$.1f", b.mClino * TopoDroidApp.mUnitAngle ) );
    holder.tvNote.setText( b.toNote() );

    holder.tvFrom.setOnClickListener( this );
    holder.tvTo.setOnClickListener( this );

    if ( holder.tvFrom.getTextSize() != TopoDroidApp.mTextSize ) {
      holder.tvId.setTextSize( TopoDroidApp.mTextSize );
      holder.tvFrom.setTextSize( TopoDroidApp.mTextSize );
      holder.tvTo.setTextSize( TopoDroidApp.mTextSize );
      holder.tvLength.setTextSize( TopoDroidApp.mTextSize );
      holder.tvCompass.setTextSize( TopoDroidApp.mTextSize );
      holder.tvClino.setTextSize( TopoDroidApp.mTextSize );
      holder.tvNote.setTextSize( TopoDroidApp.mTextSize );
    }

    if ( show_ids ) {
      holder.tvId.setVisibility( View.VISIBLE );
      holder.tvId.setTextColor( 0xff6666cc ); // light-blue
    } else {
      holder.tvId.setVisibility( View.GONE );
    }
    holder.tvFrom.setTextColor( b.color() );
    holder.tvTo.setTextColor( b.color() );
    holder.tvLength.setTextColor( b.color() );
    holder.tvCompass.setTextColor( b.color() );
    holder.tvClino.setTextColor( b.color() );
    holder.tvNote.setTextColor( b.color() );

    if ( b.isRecent( mParent.secondLastShotId() ) ) {
      holder.tvFrom.setBackgroundColor( 0xff000033 ); // dark-blue
      holder.tvTo.setBackgroundColor( 0xff000033 ); // dark-blue
    } 
    if ( ! b.isAcceptable( ) ) {
      holder.tvLength.setBackgroundColor( 0xff330000 ); // dark-red
      holder.tvCompass.setBackgroundColor( 0xff330000 ); // dark-red
      holder.tvClino.setBackgroundColor( 0xff330000 ); // dark-red
    } else {
      holder.tvLength.setBackgroundColor( 0xff000000 ); // black
      holder.tvCompass.setBackgroundColor( 0xff000000 ); // black
      holder.tvClino.setBackgroundColor( 0xff000000 ); // black
    }
  }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
  // called by ShotActivity::updateShot()
  //
  void updateBlockView( DistoXDBlock blk ) 
  {
    for ( int k=0; k<mItems.size(); ++k ) {
      if ( mItems.get(k) == blk) {
        View v = blk.mView;
        if ( v != null ) {
          ViewHolder holder = (ViewHolder) v.getTag();
          if ( holder != null ) setViewText( holder, blk );
          v.setVisibility( blk.mVisible );
        }
        return;
      }
    }
  }


  public void onClick(View view)
  {
    TextView tv = (TextView) view;
    if ( tv != null ) {
      String st = tv.getText().toString();
      mParent.recomputeItems( st );
    }
  }

}

