/** @file EnableSymbol.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid enabled symbol(s)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.content.Context;
// import android.app.Dialog;

import android.widget.CheckBox;
// import android.widget.Button;
import android.widget.TextView;

import android.view.LayoutInflater;
// import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnTouchListener;
// import android.text.Layout;
import android.widget.LinearLayout;

import android.util.Log;

class EnableSymbol implements View.OnClickListener
{
  int mType;   // symbol type POINT (0) LINE (1) AREA (2)
  // int mIndex;  // symbol index
  boolean mMustSave;
  CheckBox     mCheckBox = null;
  // ItemButton   mButton   = null;
  TextView     mTextView = null;
  LinearLayout mView = null;
  Symbol mSymbol;
  float sx = 1.0f;
  float sy = 1.0f;

  // private Context mContext;

  @Override
  public void onClick( View v ) 
  {
    mMustSave = true;
  }

  String getName()  { return mSymbol.mThName; }
  // Paint  getPaint() { return null; }
  // Path   getPath()  { return null; }
  // boolean isOrientable() { return false; }
  // void rotate( float angle ) { } 

  boolean getEnabled() { return mSymbol.mEnabled; }
  // void setEnabled( boolean enabled ) { mSymbol.mEnabled = enabled; }

  public EnableSymbol( Context context, SymbolEnableDialog dialog, int type, int index, Symbol symbol )
  {  
    mType  = type;
    // mIndex = index;
    mMustSave = false;
    mSymbol = symbol;

    if ( mType == DrawingActivity.SYMBOL_POINT ) {
      sx = 2.0f;
      sy = 2.0f;
    } else if ( mType == DrawingActivity.SYMBOL_AREA ) {
      sx = 2.5f;
      sy = 1.7f;
    }

    // Log.v( TopoDroidApp.TAG, "Item " + mType + "/" + mIndex + " " + mSymbol.getName() );

    // mCheckBox = new CheckBox( context );
    // mButton   = new ItemButton( context, mSymbol.getPaint(), mSymbol.getPath(), sx, sy );
    // mTextView = new TextView( context );
    // mCheckBox.setBackgroundColor( Color.BLACK );
    // mTextView.setBackgroundColor( Color.BLACK );

    // mCheckBox.setChecked( mSymbol.mEnabled );
    // mTextView.setText( mSymbol.mThName );

    // LinearLayout ll = new LinearLayout( context );
    // // ll.setOrientation( LinearLayout.HORIZONTAL );
    // int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    // int lh = LinearLayout.LayoutParams.WRAP_CONTENT;

    // ll.addView( mCheckBox, new LinearLayout.LayoutParams(lh,lw) );
    // // ll.addView( mButton,   new LinearLayout.LayoutParams(lh,lw) );
    // ll.addView( mTextView, new LinearLayout.LayoutParams(lh,lw) );
    // mView = ll;

  }

}


