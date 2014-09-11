/** @file HelpDialog.java
 *
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

import android.view.Window;

// import android.graphics.*;
import android.view.View;
import android.widget.Button;

import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

// import android.widget.TextView;
import android.widget.ListView;

// import android.util.Log;

class HelpDialog extends Dialog
{
  private Context mContext;
  private ListView    mList;
  private HelpAdapter mAdapter;

  private int mIcons[];
  private int mTexts[];

  // TODO list of help entries
  HelpDialog( Context context, int icons[], int texts[] )
  {
    super( context );
    mContext = context;
    mIcons = icons;
    mTexts = texts;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.help_dialog);
    setTitle( mContext.getResources().getString( R.string.HELP ) );


    mList = (ListView) findViewById(R.id.help_list);
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // Log.v( TopoDroidApp.TAG, "HelpDialog ... createAdapters" );
    createAdapter();
    mList.setAdapter( mAdapter );
    mList.invalidate();
  }

  void createAdapter()
  {
    mAdapter = new HelpAdapter( mContext, this, R.layout.item, new ArrayList<HelpEntry>() );
    int np = mIcons.length;
    for ( int i=0; i<np; ++i ) {
      mAdapter.add( new HelpEntry( mContext, mIcons[i], mTexts[i] ) );
    }
  }

}

