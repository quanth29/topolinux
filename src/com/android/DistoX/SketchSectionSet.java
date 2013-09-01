/** @file SketchSectionSet.java
 *
 * @author marco corvi
 * @date jul 2013
 *
 * @brief TopoDroid 3d sketch: set of sections
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20130310 created
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.util.Log;

class SketchSectionSet
{
  int mType;         // sections type: cannot mix sections of different type
  NumStation mFrom;
  NumStation mTo;
  ArrayList< SketchSection > mSections; // ordered by the line ascissa (section position)

  SketchSectionSet( int type, NumStation from, NumStation to )
  {
    mType = type;
    mFrom = from;
    mTo   = to;
    mSections = new ArrayList< SketchSection >();
  }

  // insert the section in the sections array keeping this sorted
  // by the distance from the "from" station
  boolean addSection( SketchSection section ) 
  {
    if ( mType != section.mType ) return false;
    int n = 0;
    while ( n < mSections.size() && section.mPosition > mSections.get(n).mPosition ) ++ n;
    // n = index of the first section "after" "section"
    // Log.v("DistoX", "add section at index " + n );
    mSections.add( n, section );
    return true;
  }

  void removeSection( SketchSection section )
  {
    if ( section.mSet != this ) return;
    int n = size() - 1;
    if ( n < 0 || section != mSections.get(n) ) return;
    mSections.remove( n );
  }

  /** get the numbewr of sections
   */
  int size() { return mSections.size(); }

  /** get a specific section (sections are kept in order from station1 to station2)
   * @param n   section index
   */
  SketchSection getSection(int n ) { return mSections.get(n); }

  /** remove all sections
   */
  void clearSections()
  {
    mSections.clear();
  }

}
