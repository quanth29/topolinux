/* @file DistoXNum.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid centerline computation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120530 loop closures
 * 20120601 more loop closure
 * 20120702 surface shots
 */
package com.android.DistoX;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import java.io.StringWriter;
import java.io.PrintWriter;

// import android.util.Log;

class DistoXNum
{
  // private static final String TAG = "DistoX Num";
  private static final float grad2rad = TopoDroidApp.GRAD2RAD_FACTOR;

  /* bounding box */
  private float mSmin; // south
  private float mSmax;
  private float mEmin; // east
  private float mEmax;
  private float mVmin; // vertical - including duplicate shots
  private float mVmax;
  private float mHmin; // horizontal
  private float mHmax;

  /* statistics - not includnf survey shts */
  private float mZmin; // Z depth 
  private float mZmax;
  private float mLength; // survey length 

  /** survey point
   */
  public class SurveyPoint
  {
    public float s; // south Y downward
    public float e; // east X rightward
    public float v; // Z vertical
    public float h; // horizontal ???

    SurveyPoint()
    {
      s = 0.0f;
      e = 0.0f;
      v = 0.0f;
      h = 0.0f;
    }
  }

  /** survey station
   */
  public class Station extends SurveyPoint
  {
    String name;  // station name
    float dist;   // loop closure distance (shortest-path algo)
    // Station path; // previous station in the shortest path

    Station( String id )
    {
      super();
      name = id;
    }

    Station( String id, Station from, float d, float b, float c, int extend )
    {
      // Log.v( TAG, "station " + id + " from " + from + " (extend " + extend + ")" );
      name = id;
      v = from.v - d * (float)Math.sin(c * grad2rad);
      float h0 = d * (float)Math.abs( Math.cos(c * grad2rad) );
      h = from.h + extend * h0;
      s = from.s - h0 * (float)Math.cos( b * grad2rad );
      e = from.e + h0 * (float)Math.sin( b * grad2rad );
    }
  }

  public class Shot 
  {
    public Station from;
    public Station to;
    public DistoXDBlock block;

    Shot( Station f, Station t, DistoXDBlock blk )
    {
      from = f;
      to   = t;
      block = blk;
    }
  }

  public class Splay extends SurveyPoint
  {
    public Station from;
    public DistoXDBlock block;

    Splay( Station f, float d, float b, float c, int extend, DistoXDBlock blk )
    {
      from = f;
      v = from.v - d * (float)Math.sin(c * grad2rad);
      float h0 = d * (float)Math.abs( Math.cos(c * grad2rad) );
      h = from.h + extend * h0;
      s = from.s - h0 * (float)Math.cos( b * grad2rad );
      e = from.e + h0 * (float)Math.sin( b * grad2rad );
      block = blk;
    }
  }

  public class TmpShot
  {
    boolean used;
    public String from;
    public String to;
    public float d;
    public float b;
    public float c;
    public int extend;
    public boolean duplicate;
    public boolean surface;
    public DistoXDBlock block;

    public TmpShot( DistoXDBlock blk )
    { 
      used = false;
      duplicate = false;
      surface = false;
      block = blk;
    }
  }

  /** loop-closure error
   */
  public class Closure
  {
    public Station station; // loop-closure station 
    public float ds;        // south DY downward
    public float de;        // east DX rightward
    public float dv;        // DZ vert. displacement error 
    public float dh;        // horiz. displacement error
    public float dl;        // total displacement error
    public float error;     // loop percent error

    // FIXME need the loop length to compute the percent error
    Closure( Station at, Station from, float d, float b, float c, float len )
    {
      station = at;
      dv = (float)Math.abs( from.v - d * (float)Math.sin(c * grad2rad) - at.v );
      float h0 = d * (float)Math.abs( Math.cos(c * grad2rad) );
      ds = (float)Math.abs( from.s - h0 * (float)Math.cos( b * grad2rad ) - at.s );
      de = (float)Math.abs( from.e + h0 * (float)Math.sin( b * grad2rad ) - at.e );
      dh = ds*ds + de*de;
      dl = (float)Math.sqrt( dh + dv*dv );
      dh = (float)Math.sqrt( dh );
      error = (dl*100) / len;
    }

    public String toString()
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      pw.format("%s %.2f [%.2f %.2f] %.2f%%", station.name, dl, dh, dv, error );
      return sw.getBuffer().toString();
    }
  }

  private List<Station> mStations;
  private List<Shot>    mShots;
  private List<Splay>   mSplays;
  private List<Closure> mClosures;
  private int mDupNr; // number of duplicate shots
  private int mSurfNr; // number of surface shots

  public int stationsNr()  { return mStations.size(); }
  public int shotsNr()     { return mShots.size(); }
  public int duplicateNr() { return mDupNr; }
  public int surfaceNr()   { return mSurfNr; }
  public int splaysNr()    { return mSplays.size(); }
  public int loopNr()      { return mClosures.size(); }

  public float surveyLength() { return mLength; }
  public float surveyTop()    { return -mZmin; } // top must be positive
  public float surveyBottom() { return -mZmax; } // bottom must be negative

  DistoXNum( List<DistoXDBlock> data, String start )
  {
    compute( data, start );
    // Log.v( TAG, " length " + mLength + " depth " + mZmin + " " + mZmax );
  }

  public List<Station> getStations() { return mStations; }
  public List<Shot> getShots() { return mShots; }
  public List<Splay> getSplays() { return mSplays; }
  public List<Closure> getClosures() { return mClosures; }

  // public void dump()
  // {
  //   Log.v( TAG, "Stations:" );
  //   for ( Station st : mStations ) {
  //     Log.v( TAG, "   " + st.name + " S: " + st.s + " E: " + st.e );
  //   }
  //   Log.v( TAG, "Shots:" );
  //   for ( Shot sh : mShots ) {
  //     Log.v( TAG, "   From: " + sh.from.name + " To: " + sh.to.name );
  //   }
  // } 


  /** shortest-path algo
   * @param s1  first station
   * @param s2  second station
   */
  private float shortestPath( Station s1, Station s2 )
  {
    Stack<Station> stack = new Stack<Station>();
    for ( Station s : mStations ) {
      s.dist = 100000.0f;
      // s.path = null;
    }
    s1.dist = 0.0f;
    stack.push( s1 );
    while ( ! stack.empty() ) {
      Station s = stack.pop();
      for ( Shot e : mShots ) {
        if ( e.from == s && e.to != null ) {
          float d = s.dist + e.block.mLength;
          if ( d < e.to.dist ) {
            e.to.dist = d;
            // e.to.path = from;
            stack.push( e.to );
          }
        } else if ( e.to == s && e.from != null ) {
          float d = s.dist + e.block.mLength;
          if ( d < e.from.dist ) {
            e.from.dist = d;
            // e.from.path = from;
            stack.push( e.from );
          }
        }
      }
    }
    return s2.dist;
  }

  private Station getStation( String id ) 
  {
    for (Station st : mStations ) {
      if ( id.equals(st.name) ) { return st; }
    }
    return null;
  }

  private void updateBBox( SurveyPoint s )
  {
    if ( s.s < mSmin ) mSmin = s.s;
    if ( s.s > mSmax ) mSmax = s.s;
    if ( s.e < mEmin ) mEmin = s.e;
    if ( s.e > mEmax ) mEmax = s.e;
    if ( s.h < mHmin ) mHmin = s.h;
    if ( s.h > mHmax ) mHmax = s.h;
    if ( s.v < mVmin ) mVmin = s.v;
    if ( s.v > mVmax ) mVmax = s.v;
  }

  public float surveySmin() { return mSmin; }
  public float surveySmax() { return mSmax; }
  public float surveyEmin() { return mEmin; }
  public float surveyEmax() { return mEmax; }
  public float surveyHmin() { return mHmin; }
  public float surveyHmax() { return mHmax; }
  public float surveyVmin() { return mVmin; }
  public float surveyVmax() { return mVmax; }

  private void compute( List<DistoXDBlock> data, String start )
  {
    mSmin = 0.0f; // clear BBox
    mSmax = 0.0f;
    mEmin = 0.0f;
    mEmax = 0.0f;
    mHmin = 0.0f;
    mHmax = 0.0f;
    mVmin = 0.0f;
    mVmax = 0.0f;
    mZmin = 0.0f;
    mZmax = 0.0f;
    mLength = 0.0f;
    mDupNr = 0;

    mStations = new ArrayList< Station >();
    mShots    = new ArrayList< Shot >();
    mSplays   = new ArrayList< Splay >();
    mClosures = new ArrayList< Closure >();
    List<TmpShot> tmpshots  = new ArrayList< TmpShot >();
    List<TmpShot> tmpsplays = new ArrayList< TmpShot >();
    for ( DistoXDBlock block : data ) {
      switch ( block.type() ) {
        case DistoXDBlock.BLOCK_SPLAY:
          TmpShot ts = new TmpShot( block );
          ts.from = block.mFrom;
          ts.to   = null;
          ts.d = block.mLength;
          ts.b = block.mBearing;
          ts.c = block.mClino;
          ts.extend = (int)(block.mExtend);
          if ( ts.from == null || ts.from.length() == 0 ) { // reversed splay
            ts.from = block.mTo;
            ts.d = - ts.d;
          }
          if ( ts.from != null && ts.from.length() > 0 ) { 
            tmpsplays.add( ts );
          }
          break;
        case DistoXDBlock.BLOCK_CENTERLINE:
          ts = new TmpShot( block );
          ts.from = block.mFrom;
          ts.to   = block.mTo;
          ts.d = block.mLength;
          ts.b = block.mBearing;
          ts.c = block.mClino;
          ts.extend = (int)(block.mExtend);
          ts.duplicate = ( block.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
          ts.surface   = ( block.mFlag == DistoXDBlock.BLOCK_SURFACE );
          tmpshots.add( ts );
          break;
      }
    }
    // Log.v( TAG, " tmp-shots " + tmpshots.size() + " tmp-splays " + tmpsplays.size() );

    mStations.add( new Station( start ) );
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( TmpShot ts : tmpshots ) {
        if ( ts.used ) continue;
        Station sf = getStation( ts.from );
        Station st = getStation( ts.to );
        if ( sf != null ) {
          if ( st != null ) { // close loop
            Shot sh = new Shot( sf, st, ts.block );
            mShots.add( sh );
            if ( ts.duplicate ) {
              ++mDupNr;
            } else if ( ts.surface ) {
              ++mSurfNr;
            } else {
              mLength += ts.d;
            }
            // FIXME need the loop length to compute the fractional closure error
            float length = shortestPath( sf, st) + ts.d;
            Closure cl = new Closure( st, sf, ts.d, ts.b, ts.c, length );
            mClosures.add( cl );
            ts.used = true;
            repeat = true;
          } else { // add from-->to
            st = new Station( ts.to, sf, ts.d, ts.b, ts.c, ts.extend );
            updateBBox( st );
            if ( ts.duplicate ) {
              ++mDupNr;
            } else if ( ts.surface ) {
              ++mSurfNr;
            } else {
              mLength += ts.d;
              if ( st.v < mZmin ) { mZmin = st.v; }
              if ( st.v > mZmax ) { mZmax = st.v; }
            }
            mStations.add( st );
            Shot sh = new Shot( sf, st, ts.block );
            mShots.add( sh );
            ts.used = true;
            repeat = true;
          }
        } else if ( st != null ) {
          sf = new Station( ts.from, st, -ts.d, ts.b, ts.c, ts.extend );
          updateBBox( sf );
          if ( ts.duplicate ) {
            ++mDupNr;
          } else if ( ts.surface ) {
            ++mSurfNr;
          } else {
            mLength += ts.d;
            if ( sf.v < mZmin ) { mZmin = sf.v; }
            if ( sf.v > mZmax ) { mZmax = sf.v; }
          }
          mStations.add( sf );
          Shot sh = new Shot( st, sf, ts.block );
          mShots.add( sh );
          ts.used = true;
          repeat = true;
        }
      }
    }
    // Log.v( TAG, " done leg shots ");

    for ( TmpShot ts : tmpsplays ) {
      Station sf = getStation( ts.from );
      if ( sf != null ) {
        // Log.v( TAG, "splay from " + ts.from + " " + ts.d + " " + ts.b + " " + ts.c
        //             + " (extend " + ts.extend + ")" );
        Splay sh = new Splay( sf, ts.d, ts.b, ts.c, ts.extend, ts.block );
        mSplays.add( sh );
      }
    }
  }
}
