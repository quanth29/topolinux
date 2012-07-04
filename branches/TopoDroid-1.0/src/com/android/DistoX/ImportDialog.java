/** @file ImportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid import file list dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * 20120605 created
 */
package com.android.DistoX;

import java.util.Set;
import java.io.File;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;

// import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.content.IntentFilter;
import android.content.Context;


public class ImportDialog extends Dialog
                          implements OnItemClickListener
{ 
  // private static final String TAG = "DistoX";
  
  private Context mContext;
  private TopoDroidApp app;
  private TopoDroidActivity mParent;
  private File[] files;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;

  public ImportDialog( Context context, TopoDroidActivity parent, TopoDroidApp _app )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    app = _app;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.file_chooser);

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // setTitleColor( 0x006d6df6 );

    boolean added = false;
    files = TopoDroidApp.getImportFiles();
    if ( files != null ) {
      for ( File f : files ) {
        mArrayAdapter.add( f.getName() );
        added = true;
      }
    }
    files = TopoDroidApp.getZipFiles();
    if ( files != null ) {
      for ( File f : files ) {
        mArrayAdapter.add( f.getName() );
        added = true;
      }
    }
    if ( added ) {
      mList.setAdapter( mArrayAdapter );
    } else {
      Toast.makeText( mContext, R.string.file_parse_none, Toast.LENGTH_LONG ).show();
      dismiss();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    mParent.importFile( item );
    dismiss();
  }

}

