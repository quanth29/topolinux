/** @file TdSymbolDialog.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid option list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121217 created
 */
package com.android.DistoX;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;

// import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;

// import android.widget.Toast;

public class TdSymbolDialog extends Dialog
                            implements OnClickListener
{
  TopoDroidApp mApp;
  CheckBox mCBask;
  Button mBTyes;
  Button mBTno;

  TdSymbolDialog( Context context, TopoDroidApp app )
  {
    super( context );
    mApp = app;
    
    setContentView(R.layout.td_symbol_dialog);
    mCBask = (CheckBox) findViewById( R.id.td_symbol_ask );
    mBTyes = (Button) findViewById( R.id.td_symbol_yes );
    mBTno  = (Button) findViewById( R.id.td_symbol_no  );
    // setTitle(R.string.welcome_title);

    mBTyes.setOnClickListener( this );
    mBTno.setOnClickListener( this );
  }

  @Override
  public void onClick( View v )
  {
    if ( mCBask.isChecked() ) {
      mApp.setBooleanPreference( "DISTOX_TD_SYMBOL", false );
      mApp.mStartTdSymbol = false;
    }
    mApp.mStartTdSymbol = ( (Button)v == mBTyes );
    dismiss();
  }
}
