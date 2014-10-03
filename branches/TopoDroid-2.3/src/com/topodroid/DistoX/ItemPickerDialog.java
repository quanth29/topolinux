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
 * 20140417 bug-fix: onClick not reported timely. replaced List with Grid
 */
package com.topodroid.DistoX;

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

import android.util.Log;

class ItemPickerDialog extends Dialog
                       implements View.OnClickListener, IItemPicker
                       // , AdapterView.OnItemClickListener
{
  private int mItemType; // items type
  private long mPlotType;

  private  Button mBTpoint;
  private  Button mBTline;
  private  Button mBTarea;
  private  Button mBTleft;
  private  Button mBTright;
  // private  Button mBTcancel;
  // private  Button mBTok;

  private Context mContext;
  private DrawingActivity mParent;

  //* private ListView    mList = null;
  private GridView    mList = null;
  private GridView    mGrid = null;
  private ItemAdapter mPointAdapter;
  private ItemAdapter mLineAdapter;
  private ItemAdapter mAreaAdapter;
  private boolean mUseText = false;
  private ItemAdapter mAdapter = null;

  // static int mLinePos;
  // static int mAreaPos;

  /**
   * @param context   context
   * @param parent    DrawingActivity parent
   * @param type      drawing type
   */
  ItemPickerDialog( Context context, DrawingActivity parent, long type )
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
      //* mList = (ListView) findViewById(R.id.item_list);
      mList = (GridView) findViewById(R.id.item_list);
      // mList.setOnItemClickListener( this );
      //* mList.setDividerHeight( 1 );
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
    int np = point_lib.mAnyPointNr;
    int nl = line_lib.mAnyLineNr;
    int na = area_lib.mAnyAreaNr;
    for ( int i=0; i<np; ++i ) {
      SymbolPoint p = point_lib.getAnyPoint( i );
      if ( p.isEnabled() ) {
        mPointAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_POINT, i, p, mUseText ) );
      }
    }
    for ( int j=0; j<nl; ++j ) {
      SymbolLine l = line_lib.getAnyLine( j );
      if ( l.isEnabled() ) {
        mLineAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_LINE, j, l, mUseText ) );
      }
    }
    for ( int k=0; k<na; ++k ) {
      SymbolArea a = area_lib.getAnyArea( k );
      if ( a.isEnabled() ) {
        mAreaAdapter.add( new ItemSymbol( mContext, this, DrawingActivity.SYMBOL_AREA, k, a, mUseText ) );
      }
    }

    mPointAdapter.setSelectedItem( mParent.mCurrentPoint );
    mLineAdapter.setSelectedItem( mParent.mCurrentLine ); 
    mAreaAdapter.setSelectedItem( mParent.mCurrentArea );
  }

  private void updateList()
  {
    // Log.v( TopoDroidApp.TAG, "ItemPickerDialog ... updateList type " + mItemType );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT:
        mAdapter = mPointAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_LINE:
        mAdapter = mLineAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        break;
      case DrawingActivity.SYMBOL_AREA:
        mAdapter = mAreaAdapter;
        mBTpoint.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTline.getBackground().setColorFilter( Color.parseColor( "#cccccc" ), PorterDuff.Mode.DARKEN );
        mBTarea.getBackground().setColorFilter( Color.parseColor( "#ccccff" ), PorterDuff.Mode.LIGHTEN );
        break;
    }
    if ( mAdapter != null ) {
      if ( mList != null ) {
        mList.setAdapter( mAdapter );
        mList.invalidate();
      } else if ( mGrid != null ) {
        mGrid.setAdapter( mAdapter );
        mGrid.invalidate();
      }
    }
  }

  public void setTypeAndItem( int pos )
  {
    // Log.v( TopoDroidApp.TAG, "setTypeAndItem type " + mItemType  + " item " + pos );
    ItemSymbol is;
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        is = mPointAdapter.get( pos );
        // Log.v( TopoDroidApp.TAG, "setTypeAndItem type point pos " + pos + " index " + is.mIndex );
        mParent.mCurrentPoint = is.mIndex;
        mParent.pointSelected( is.mIndex ); // mPointAdapter.getSelectedItem() );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        // mLinePos = pos;
        is = mLineAdapter.get( pos );
        if ( mPlotType != PlotInfo.PLOT_SECTION || is.mIndex != DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
          mParent.mCurrentLine = is.mIndex;
          mParent.lineSelected( is.mIndex ); // mLineAdapter.getSelectedItem() );
        } else {
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        // mAreaPos = pos;
        is = mAreaAdapter.get( pos );
        mParent.mCurrentArea = is.mIndex;
        mParent.areaSelected( is.mIndex ); // mAreaAdapter.getSelectedItem() );
        break;
    }
    // cancel();
  }

  private void setTypeFromCurrent( )
  {
    // Log.v( TopoDroidApp.TAG, "setTypeFromCurrent type " + mItemType  );
    switch ( mItemType ) {
      case DrawingActivity.SYMBOL_POINT: 
        // mParent.mCurrentPoint = posx;
        mParent.pointSelected( mParent.mCurrentPoint );
        break;
      case DrawingActivity.SYMBOL_LINE: 
        if ( mPlotType != PlotInfo.PLOT_SECTION ) {
          mParent.lineSelected( mParent.mCurrentLine );
        } else {
        }
        break;
      case DrawingActivity.SYMBOL_AREA: 
        mParent.areaSelected( mParent.mCurrentArea );
        break;
    }
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
    // Log.v("DistoX", "ItemPicker onClick()" );
    switch (view.getId()) {
      case R.id.item_point:
        if ( mItemType != DrawingActivity.SYMBOL_POINT ) {
          mItemType = DrawingActivity.SYMBOL_POINT;
          updateList();
          setTypeFromCurrent( );
        }
        break;
      case R.id.item_line:
        if ( mItemType != DrawingActivity.SYMBOL_LINE ) {
          mItemType = DrawingActivity.SYMBOL_LINE;
          updateList();
          setTypeFromCurrent( );
        }
        break;
      case R.id.item_area:
        if ( mItemType != DrawingActivity.SYMBOL_AREA ) {
          mItemType = DrawingActivity.SYMBOL_AREA;
          updateList();
          setTypeFromCurrent( );
        }
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
      //   setTypeFromCurrent();
      //   break;
      default: 
        // if ( mAdapter != null ) mAdapter.doClick( view );
        // if ( mList != null ) mList.invalidate();
        break;
    }
    // dismiss();
  }

  // @Override
  // public void onItemClick( AdapterView adapter, View view, int pos, long id )
  // {
  //    Log.v( "DistoX", "ItemPicker onItemCLick()" );
  //    if ( mAdapter != null ) mAdapter.doClick( view );
  // }
}
