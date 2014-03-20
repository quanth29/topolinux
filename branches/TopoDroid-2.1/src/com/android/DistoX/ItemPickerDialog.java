/** @file ItemPickerDialog.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140303 symbol picker mode, list or grid
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.view.Window;

import android.graphics.*;
import android.view.View;
import android.widget.Button;

import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

// import android.widget.TextView;
import android.widget.ListView;
import android.widget.GridView;

// import android.util.Log;

class ItemPickerDialog extends Dialog
                       implements View.OnClickListener, IItemPicker
{
  private int mItemType; // items type
  private int mPlotType;

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  private  Button mBTleft;
  private  Button mBTright;
  // private  Button mBTcancel;
  // private  Button mBTok;

  private Context mContext;
  private DrawingActivity mParent;

  private ListView    mList = null;
  private GridView    mGrid = null;
  private ItemAdapter mPointAdapter;
  private ItemAdapter mLineAdapter;
  private ItemAdapter mAreaAdapter;
  private boolean mUseText = false;

  // static int mPointPos;
  // static int mLinePos;
  // static int mAreaPos;

  /**
   * @param context   context
   * @param parent    DrawingActivity parent
   * @param type      drawing type
   */
  ItemPickerDialog( Context context, DrawingActivity parent, int type )
  {
    super( context );
    mContext = context;
    mParent  = parent;

    mPlotType = type;
    mItemType = mParent.mSymbol;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    
    if ( TopoDroidApp.mPickerType == TopoDroidApp.PICKER_LIST ) {
      mUseText = true;
      setContentView(R.layout.item_picker_dialog);
      mList = (ListView) findViewById(R.id.item_list);
      // mList.setOnItemClickListener( this );
      mList.setDividerHeight( 2 );
      mGrid = null;
    } else {
      mUseText = false;
      setContentView(R.layout.item_picker2_dialog);
      mGrid = (GridView) findViewById(R.id.item_grid);
      // mGrid.setOnItemClickListener( this );
      // mGrid.setDividerHeight( 2 );
      mList = null;
    }
    
    mBTpoint = (Button) findViewById(R.id.item_point);
    mBTline  = (Button) findViewById(R.id.item_line );
    mBTarea  = (Button) findViewById(R.id.item_area );
    mBTleft  = (Button) findViewById(R.id.item_left );
    mBTright = (Button) findViewById(R.id.item_right );
    // mBTcancel  = (Button) findViewById(R.id.item_cancel );
    // mBTok    = (Button) findViewById(R.id.item_ok   );

    mBTpoint.setOnClickListener( this );
    mBTline.setOnClickListener( this );
    mBTarea.setOnClickListener( this );
    mBTleft.setOnClickListener( this );
    mBTright.setOnClickListener( this );
    // mBTcancel.setOnClickListener( this );
    // mBTok.setOnClickListener( this );

    // requestWindowFeature( Window.FEATURE_NO_TITLE );

    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... createAdapters" );
    createAdapters();
    updateList();
  }

  void createAdapters()
  {
    mPointAdapter = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
    mLineAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );
    mAreaAdapter  = new ItemAdapter( mContext, this, R.layout.item, new ArrayList<ItemSymbol>() );

    SymbolPointLibrary point_lib = DrawingBrushPaths.mPointLib;
    SymbolLineLibrary line_lib = DrawingBrushPaths.mLineLib;
    SymbolAreaLibrary area_lib = DrawingBrushPaths.mAreaLib;
    int np = point_lib.mPointNr;
    int nl = line_lib.mLineNr;
    int na = area_lib.mAreaNr;
    for ( int i=0; i<np; ++i ) {
      mPointAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_POINT, i, point_lib.getPoint( i ), mUseText ) );
    }
    for ( int j=0; j<nl; ++j ) {
      mLineAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_LINE, j, line_lib.getLine( j ), mUseText ) );
    }
    for ( int k=0; k<na; ++k ) {
      mAreaAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_AREA, k, area_lib.getArea( k ), mUseText ) );
    }

    mPointAdapter.setSelectedItem( mParent.mCurrentPoint );
    mLineAdapter.setSelectedItem( mParent.mCurrentLine ); 
    mAreaAdapter.setSelectedItem( mParent.mCurrentArea );
  }

  void updateList()
  {
    ItemAdapter adapter = null;
    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... updateList type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        adapter = mPointAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_LINE:
        adapter = mLineAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_AREA:
        adapter = mAreaAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        break;
    }
    if ( adapter != null ) {
      if ( mList != null ) {
        mList.setAdapter( adapter );
        mList.invalidate();
      } else if ( mGrid != null ) {
        mGrid.setAdapter( adapter );
        mGrid.invalidate();
      }
    }
  }

  public void setTypeAndItem( int pos )
  {
    // Log.v( TopoDroidApp.TAG, "setTypeAndItem type " + mItemType  + " item " + pos );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // mPointPos = pos;
        mParent.mCurrentPoint = pos;
        mParent.pointSelected( pos ); // mPointAdapter.getSelectedItem() );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // mLinePos = pos;
        if ( mPlotType != (int)PlotInfo.PLOT_SECTION || pos != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
          mParent.mCurrentLine = pos;
          mParent.lineSelected( pos ); // mLineAdapter.getSelectedItem() );
        } else {
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // mAreaPos = pos;
        mParent.mCurrentArea = pos;
        mParent.areaSelected( pos ); // mAreaAdapter.getSelectedItem() );
        break;
    }
    // cancel();
  }

  void rotatePoint( int angle )
  {
    if ( mItemType == DrawingActivity.SYMBOL_POINT ) {
      mPointAdapter.rotatePoint( mParent.mCurrentPoint, angle );
    }
  }

  @Override
  public void onBackPressed ()
  {
    // Log.v( TopoDroidApp.TAG, "onBackPressed type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // mParent.pointSelected( mPointPos );
        mParent.pointSelected( mParent.mCurrentPoint );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // mParent.lineSelected( mLinePos ); 
        mParent.lineSelected( mParent.mCurrentLine ); 
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // mParent.areaSelected( mAreaPos );
        mParent.areaSelected( mParent.mCurrentArea );
        break;
    }
    cancel();
  }

  @Override
  public void onClick(View view)
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingLinePickerDialog::onClick" );
    switch (view.getId()) {
      case R.id.item_point:
        mItemType = DrawingActivity.SYMBOL_POINT;
        break;
      case R.id.item_line:
        mItemType = DrawingActivity.SYMBOL_LINE;
        break;
      case R.id.item_area:
        mItemType = DrawingActivity.SYMBOL_AREA;
        break;
      case R.id.item_left:
        rotatePoint( -10 );
        break;
      case R.id.item_right:
        rotatePoint( 10 );
        break;
      // case R.id.item_cancel:
      //   dismiss();
      //   break;
      // case R.id.item_ok:
      //   setTypeAndItem();
      //   break;
      default:
        break;
    }
    updateList();
    // dismiss();
  }
}
