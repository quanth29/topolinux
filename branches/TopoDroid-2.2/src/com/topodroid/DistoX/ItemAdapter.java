/** @file ItemAdapter.java
 *
 * @author marco corvi
 * @date
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
// import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
// import android.widget.RadioGroup;

import android.view.View.OnClickListener;

import android.widget.LinearLayout;

// import android.util.Log;

class ItemAdapter extends ArrayAdapter< ItemSymbol >
                  implements OnClickListener
{
  private ArrayList< ItemSymbol > mItems;
  // private Context mContext;
  private IItemPicker mParent;
  private int mPos;    

  public ItemAdapter( Context ctx, IItemPicker parent, int id, ArrayList< ItemSymbol > items )
  {
    super( ctx, id, items );
    mParent = parent;
    mPos    = -1;

    if ( items != null ) {
      mItems = items;
      for ( ItemSymbol item : items ) {
        item.setOnClickListener( this );
      }
    } else {
      mItems = new ArrayList< ItemSymbol >();
    }
  }

  void rotatePoint( int pos, int angle )
  {
    ItemSymbol b = mItems.get( pos );
    b.rotate( angle );
  }

  // ItemSymbol get( int pos ) { return mItems.get(pos); }
  // int getSelectedItem() { return mPos; }
  // public int size() { return mItems.size(); }

  void setSelectedItem( int pos )
  {
    mPos = pos;
    for ( ItemSymbol item : mItems ) {
      item.setChecked( mPos == item.mIndex );
    }
  }

  // public ItemSymbol get( String name ) 
  // {
  //   for (int k = 0; k < mItems.size(); ++k ) {
  //     ItemSymbol sym = mItems.get( k );
  //     if ( sym.mName.equals( name ) ) return sym;
  //   }
  //   return null;
  // }

  public void add( ItemSymbol item ) 
  {
    mItems.add( item );
    item.setOnClickListener( this );
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    ItemSymbol b = mItems.get( pos );
    if ( b == null ) return convertView;
    return b.mView;
  }

  @Override
  public void onClick( View v )
  {
    // Log.v("DistoX", "ItemAdapter onClick()");
    doClick( v );
  }

  public void doClick( View v )
  {
    // Log.v("DistoX", "--> ItemAdapter doClick()");
    try {
      CheckBox cb = (CheckBox)v;
      if ( cb != null ) {
        for ( ItemSymbol item : mItems ) {
          if ( cb == item.mCheckBox ) {
            mPos = item.mIndex;
            mParent.setTypeAndItem( mPos );
            item.setChecked( true );
          } else {
            item.setChecked( false );
          }
        }
      }
    } catch ( ClassCastException e ) {
      ItemButton ib = (ItemButton)v;
      for ( ItemSymbol item : mItems ) {
        if ( ib == item.mButton ) {
          mPos = item.mIndex;
          mParent.setTypeAndItem( mPos );
          item.setChecked( true );
        } else {
          item.setChecked( false );
        }
      }
    }
  }

}

