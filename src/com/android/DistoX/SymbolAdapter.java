/** @file SymbolAdapter.java
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
package com.android.DistoX;

import java.util.ArrayList;

import android.app.Activity;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.LinearLayout;

// import android.util.Log;

class SymbolAdapter extends ArrayAdapter< EnableSymbol >
{
  private ArrayList< EnableSymbol > mItems;
  // private Context mContext;
  private Activity mActivity;

  SymbolAdapter( Activity ctx, int id, ArrayList< EnableSymbol > items )
  {
    super( ctx, id, items );
    // mContext = ctx;
    mActivity = ctx;
    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< EnableSymbol >();
    }
  }

  // ArrayList< EnableSymbol > getItems() 
  // {
  //   return mItems;
  // }

  public EnableSymbol get( int pos ) { return mItems.get(pos); }

  public EnableSymbol get( String th_name ) 
  {
    for (int k = 0; k < mItems.size(); ++k ) {
      EnableSymbol sym = mItems.get( k );
      if ( sym.getName().equals( th_name ) ) return sym;
    }
    return null;
  }

  public void add( EnableSymbol sym ) 
  {
    mItems.add( sym );
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    EnableSymbol b = mItems.get( pos );
    View v = convertView;
    if ( b == null ) return v;
    if ( b.mCheckBox == null ) {
      LinearLayout ll = new LinearLayout( mActivity );
      // ll.setOrientation( LinearLayout.HORIZONTAL );
      int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
      int lh = LinearLayout.LayoutParams.WRAP_CONTENT;

      b.mCheckBox = new CheckBox( mActivity );
      b.mTextView = new TextView( mActivity );
      ll.addView( b.mCheckBox, new LinearLayout.LayoutParams(lh,lw) );
      ll.addView( b.mTextView, new LinearLayout.LayoutParams(lh,lw) );
      b.mTextView.setText( b.getName() );

      // v = new View( mActivity );
      // v.setContentView( ll );
      b.mView = ll;

      b.mCheckBox.setChecked( b.getEnabled() );
      b.mCheckBox.setOnClickListener( b );
    }

    // b.mTextView = (TextView) v.findViewById( R.id.text );
    // b.mTextView.setText( b.mThName );
    // b.mTextView.setTextColor( b.color() );

    return b.mView;
  }

  // void resetChecked( CheckBox cb )
  // {
  //   for ( int k = 0; k < mItems.size(); ++k ) {
  //     if ( cb == mItems.get(k).mCheckBox ) {
  //       mItems.get(k).setEnabled( cb.isChecked() );
  //       break;
  //     }
  //   }
  // }

  public int size() { return mItems.size(); }

  void updateSymbols( String prefix )
  {
    for ( EnableSymbol symbol : mItems ) {
      if ( symbol.mMustSave ) {
        symbol.mSymbol.mEnabled = symbol.mCheckBox.isChecked();
        TopoDroidApp.mData.setSymbolEnabled( prefix + symbol.mSymbol.mThName, symbol.mSymbol.mEnabled );
      }
    }
  }

}

