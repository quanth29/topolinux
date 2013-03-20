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
 * 20120719 added check whether survey is attached
 * 20120726 TopoDroid log
 * 20130110 loop closure-error compensation
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

  /* statistics - not including survey shots */
  private float mZmin; // Z depth 
  private float mZmax;
  private float mLength; // survey length 

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

  // FIXME make mStations a hashmap (key station name)
  private ArrayList<NumStation> mStations;
  private ArrayList<NumShot>    mShots;
  private ArrayList<NumSplay>   mSplays;
  private ArrayList<String>     mClosures;
  private ArrayList<NumNode>    mNodes;

  private int mDupNr;  // number of duplicate shots
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

  public boolean surveyAttached; //!< whether the survey is attached

  DistoXNum( List<DistoXDBlock> data, String start )
  {
    surveyAttached = compute( data, start );
    // TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "DistoXNum cstr length " + mLength + " depth " + mZmin + " " + mZmax );
  }

  public List<NumStation> getStations() { return mStations; }
  public List<NumShot> getShots() { return mShots; }
  public List<NumSplay> getSplays() { return mSplays; }
  public List<String> getClosures() { return mClosures; }

  // public void dump()
  // {
  //   TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "DistoXNum Stations:" );
  //   for ( NumStation st : mStations ) {
  //     TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "   " + st.name + " S: " + st.s + " E: " + st.e );
  //   }
  //   TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "Shots:" );
  //   for ( NumShot sh : mShots ) {
  //     TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "   From: " + sh.from.name + " To: " + sh.to.name );
  //   }
  // } 

  /** shortest-path algo
   * @param s1  first station
   * @param s2  second station
   */
  private float shortestPath( NumStation s1, NumStation s2 )
  {
    Stack<NumStation> stack = new Stack<NumStation>();
    for ( NumStation s : mStations ) {
      s.mShortpathDist = 100000.0f;
      // s.path = null;
    }
    s1.mShortpathDist = 0.0f;
    stack.push( s1 );
    while ( ! stack.empty() ) {
      NumStation s = stack.pop();
      for ( NumShot e : mShots ) {
        if ( e.from == s && e.to != null ) {
          float d = s.mShortpathDist + e.block.mLength;
          if ( d < e.to.mShortpathDist ) {
            e.to.mShortpathDist = d;
            // e.to.path = from;
            stack.push( e.to );
          }
        } else if ( e.to == s && e.from != null ) {
          float d = s.mShortpathDist + e.block.mLength;
          if ( d < e.from.mShortpathDist ) {
            e.from.mShortpathDist = d;
            // e.from.path = from;
            stack.push( e.from );
          }
        }
      }
    }
    return s2.mShortpathDist;
  }

  // FIXME use hashmap
  NumStation getStation( String id ) 
  {
    for (NumStation st : mStations ) {
      if ( id.equals(st.name) ) { return st; }
    }
    return null;
  }

  // NumShot getShot( String s1, String s2 )
  // {
  //   for (NumShot sh : mShots ) {
  //     if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) return sh;
  //     if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) return sh;
  //   }
  //   return null;
  // }

  private void updateBBox( NumSurveyPoint s )
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

  public float surveyNorth() { return (mSmin < 0)? -mSmin : 0; }
  public float surveySouth() { return mSmax; }
  public float surveyWest() { return (mEmin < 0)? -mEmin : 0; }
  public float surveyEast() { return mEmax; }
  public float surveySmin() { return mSmin; }
  public float surveySmax() { return mSmax; }
  public float surveyEmin() { return mEmin; }
  public float surveyEmax() { return mEmax; }
  public float surveyHmin() { return mHmin; }
  public float surveyHmax() { return mHmax; }
  public float surveyVmin() { return mVmin; }
  public float surveyVmax() { return mVmax; }

  // add a shot to a station (possibly forwarded to the station's node)
  private void addShotToStation( NumShot sh, NumStation st )
  {
    if ( st.s1 == null ) {
      st.s1 = sh;
      return;
    }
    if ( st.s2 == null ) {
      st.s2 = sh;
      return;
    } 
    if ( st.node == null ) {
      st.node = new NumNode( NumNode.NODE_CROSS, st );
      mNodes.add( st.node );
    }
    st.node.addShot( sh );
  }
  private void addShotToStations( NumShot sh, NumStation st1, NumStation st2 )
  {
    addShotToStation( sh, st1 );
    addShotToStation( sh, st2 );
    mShots.add( sh );
  }

  // loop closure-error compensation
  
  class NumStep
  { 
    NumBranch b;
    NumNode n;
    int k;
  
    NumStep( NumBranch b0, NumNode n0, int k0 )
    {
      b = b0;
      n = n0;
      k = k0;
    }
  }

  class NumStack
  {
    int pos;
    int max;
    NumStep[] data;
   
    NumStack( int m )
    {
      max = m;
      data = new NumStep[max];
    }

    int size() { return pos; }

    void push( NumStep step )
    {
      data[pos] = step;
      step.b.use = 1;
      step.n.use = 1;
      ++ pos;
    }

    NumStep top() 
    {
      return ( pos > 0 )? data[pos-1] : null;
    }

    boolean empty() { return pos == 0; }
  
    void pop()
    {
      if ( pos > 0 ) { 
        --pos;
      }
    }
  }

  private NumCycle buildCycle( NumStack stack )
  {
    int sz = stack.size();
    NumCycle cycle = new NumCycle( sz );
    for (int k=0; k<sz; ++k ) {
      cycle.addBranch( stack.data[k].b, stack.data[k].n );
    }
    return cycle;
  }

  private void  makeCycles( ArrayList<NumCycle> cycles, ArrayList<NumBranch> branches ) 
  {
    int bs = branches.size();
    NumStack stack = new NumStack( bs );
    for ( int k0 = 0; k0 < bs; ++k0 ) {
      NumBranch b0 = branches.get(k0);
      if ( b0.use == 2 ) continue;
      NumNode n0 = b0.n1;
      b0.use = 1;
      b0.use = 0;
      stack.push( new NumStep(b0, b0.n2, k0 ) );
      while ( ! stack.empty() ) {
        NumStep s1 = stack.top();
        NumNode n1 = s1.n;
        s1.k ++;
        int k1 = s1.k;
        if ( n1 == n0 ) {
          cycles.add( buildCycle( stack ) );
          s1.b.use = 0;
          s1.n.use = 0;
          stack.pop();
        } else {
          int k2 = s1.k;
          for ( ; k2<bs; ++k2 ) {
            NumBranch b2 = branches.get(k2);
            if ( b2.use != 0 ) continue;
            NumNode n2 = b2.otherNode( n1 );
            if ( n2 != null && n2.use == 0 ) {
              stack.push( new NumStep( b2, n2, k0 ) );
              s1.k = k2;
              break;
            }
          }
          if ( k2 == bs ) {
            s1.b.use = 0;
            s1.n.use = 0;
            stack.pop();
          }
        }
      }
      b0.use = 2;
      n0.use = 2;
    }
  }


  private void makeSingleLoops( ArrayList<NumBranch> branches, ArrayList<NumShot> shots )
  {
    for ( NumShot shot : shots ) {
      if ( shot.branch != null ) continue;
      // start a branch END_END or LOOP
      NumBranch branch = new NumBranch( NumBranch.BRANCH_LOOP, null );
      NumShot sh0 = shot;
      NumStation sf0 = sh0.from;
      NumStation st0 = sh0.to;
      sh0.mBranchDir = 1;
      // Log.v( TopoDroidApp.TAG, "Start branch from station " + sf0.name );
      while ( st0 != sf0 ) { // follow the shot 
        branch.addShot( sh0 ); // add shot to branch and find next shot
        sh0.branch = branch;
        NumShot sh1 = st0.s1;
        if ( sh1 == sh0 ) { sh1 = st0.s2; }
        if ( sh1 == null ) { // dangling station: END_END branch --> drop
          // mEndBranches.add( branch );
          break;
        }
        if ( sh1.from == st0 ) { // move forward
          sh1.mBranchDir = 1;
          st0 = sh1.to;
        } else { 
          sh1.mBranchDir = -1; // swap
          st0 = sh1.from;
        }
        sh0 = sh1; // move forward
      }
      if ( st0 == sf0 ) { // closed-loop branch has no node
        branch.addShot( sh0 ); // add last shot to branch
        sh0.branch = branch;
        branches.add( branch );
      }
    }
  }

  private void compensateSingleLoops( ArrayList<NumBranch> branches )
  {
    for ( NumBranch br : branches ) {
      br.computeError();
      // Log.v( TopoDroidApp.TAG, "Single loop branch error " + br.e + " " + br.s + " " + br.v );
      br.compensateError( br.e, br.s, br.v );
    }
  }

  // from the list of nodes make he branches of type cross-cross
  // FIXME there is a flaw:
  // this method does not detect single loops with no hair attached
  void makeBranches( ArrayList<NumBranch> branches, ArrayList<NumNode> nodes )
  {
    for ( NumNode node : mNodes ) {
      for ( NumShot shot : node.shots ) {
        if ( shot.branch != null ) continue;
        NumBranch branch = new NumBranch( NumBranch.BRANCH_CROSS_CROSS, node );
        NumStation sf0 = node.station;
        NumShot sh0    = shot;
        NumStation st0 = sh0.to;
        if ( sh0.from == sf0 ) { 
          sh0.mBranchDir = 1;
          // st0 = sh0.to;
        } else {
          sh0.mBranchDir = -1; // swap stations
          st0 = sh0.from;
        }
        while ( st0 != sf0 ) { // follow the shot 
          branch.addShot( sh0 ); // add shot to branch and find next shot
          sh0.branch = branch;
          if ( st0.node != null ) { // end-of-branch
            branch.setLastNode( st0.node );
            branches.add( branch );
            break;
          }
          NumShot sh1 = st0.s1;
          if ( sh1 == sh0 ) { sh1 = st0.s2; }
          if ( sh1 == null ) { // dangling station: CROSS_END branch --> drop
            // mEndBranches.add( branch );
            break;
          }
          if ( sh1.from == st0 ) { // move forward
            sh1.mBranchDir = 1;
            st0 = sh1.to; 
          } else {  
            sh1.mBranchDir = -1; // swap
            st0 = sh1.from;
          }
          sh0 = sh1;
        }
        if ( st0 == sf0 ) { // closed-loop ???
        }
      }
    }
  }

  float invertMatrix( float[] a, int nr, int nc, int nd)
  {
    float  det_val = 1.0f;                /* determinant value */
    int     ij, jr, ki, kj;
    int ii  = 0;                          /* index of diagonal */
    int ir  = 0;                          /* index of row      */

    for (int i = 0; i < nr; ++i, ir += nd, ii = ir + i) {
      det_val *= a[ii];                 /* new value of determinant */
      /* ------------------------------------ */
      /* if value is zero, might be underflow */
      /* ------------------------------------ */
      if (det_val == 0.0f) {
        if (a[ii] == 0.0f) {
          break;                    /* error - exit now */
        } else {                    /* must be underflow */
          det_val = 1.0e-15f;
        }
      }
      float r = 1.0f / a[ii];   /* Calculate Pivot --------------- */
      a[ii] = 1.0f;
      ij = ir;                          /* index of pivot row */
      for (int j = 0; j < nc; ++j) {
        a[ij] = r * a[ij];
        ++ij;                         /* index of next row element */
      }
      ki = i;                           /* index of ith column */
      jr = 0;                           /* index of jth row    */
      for (int k = 0; k < nr; ++k, ki += nd, jr += nd) {
        if (i != k && a[ki] != 0.0f) {
          r = a[ki];                /* pivot target */
          a[ki] = 0.0f;
          ij = ir;                  /* index of pivot row */
          kj = jr;                  /* index of jth row   */
          for (int j = 0; j < nc; ++j) { /* subtract multiples of pivot row from jth row */
            a[kj] -= r * a[ij];
            ++ij;
            ++kj;
          }
        }
      }
    }
    return det_val;
  }

  void doLoopCompensation( ArrayList< NumNode > nodes, ArrayList< NumShot > shots )
  {
    ArrayList< NumBranch > branches = new ArrayList<NumBranch>();
    makeBranches( branches, nodes );


    ArrayList< NumBranch > singleBranches = new ArrayList<NumBranch>();
    makeSingleLoops( singleBranches, shots ); // check all shots without branch
    compensateSingleLoops( singleBranches );

    ArrayList<NumCycle> cycles = new ArrayList<NumCycle>();
    makeCycles( cycles, branches );

    for ( NumBranch b : branches ) { // compute branches and cycles errors
      b.computeError();
    }
    for ( NumCycle c : cycles ) {
      c.computeError();
      // Log.v( TopoDroidApp.TAG, "cycle error " + c.e + " " + c.s + " " + c.v ); 
    }

    ArrayList<NumCycle> indep_cycles = new ArrayList<NumCycle>(); // independent cycles
    for ( NumCycle cl : cycles ) {
      if ( ! cl.isBranchCovered( indep_cycles ) ) {
        indep_cycles.add( cl );
      }
    }

    int ls = indep_cycles.size();
    int bs = branches.size();

    float[] alpha = new float[ bs * ls ]; // cycle = row-index, branch = col-index
    float[] aa = new float[ ls * ls ];    // 

    for (int y=0; y<ls; ++y ) {  // branch-cycle matrix
      NumCycle cy = indep_cycles.get(y);
      for (int x=0; x<bs; ++x ) {
        NumBranch bx = branches.get(x);
        alpha[ y*bs + x] = 0.0f;
        int k = cy.branchIndex( bx );
        if ( k < cy.mSize ) {
          // alpha[ y*bs + x] = ( bx.n2 == cy.getNode(k) )? 1.0f : -1.0f;
          alpha[ y*bs + x] = cy.dirs[k];
        }
      }
    }

    for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
      for (int y2=0; y2<ls; ++y2 ) {
        float a = 0.0f;
        for (int x=0; x<bs; ++x ) a += alpha[ y1*bs + x] * alpha[ y2*bs + x];
        aa[ y1*ls + y2] = a;
      }
    }
    // for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
    //   StringBuilder sb = new StringBuilder();
    //   for (int y2=0; y2<ls; ++y2 ) {
    //     sb.append( Float.toString(aa[ y1*ls + y2]) ).append("  ");
    //   }
    //   Log.v( TopoDroidApp.TAG, "AA " + sb.toString() );
    // }

    float det = invertMatrix( aa, ls, ls, ls );

    // for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
    //   StringBuilder sb = new StringBuilder();
    //   for (int y2=0; y2<ls; ++y2 ) {
    //     sb.append( Float.toString(aa[ y1*ls + y2]) ).append("  ");
    //   }
    //   Log.v( TopoDroidApp.TAG, "invAA " + sb.toString() );
    // }


    for (int y=0; y<ls; ++y ) { // compute the closure compensation values
      NumCycle cy = indep_cycles.get(y);
      cy.ce = 0.0f; // corrections
      cy.cs = 0.0f;
      cy.cv = 0.0f;
      for (int x=0; x<ls; ++x ) {
        NumCycle cx = indep_cycles.get(x);
        cy.ce += aa[ y*ls + x] * cx.e;
        cy.cs += aa[ y*ls + x] * cx.s;
        cy.cv += aa[ y*ls + x] * cx.v;
      }
      // Log.v( TopoDroidApp.TAG, "cycle correction " + cy.ce + " " + cy.cs + " " + cy.cv ); 
    }
    
    for (int x=0; x<bs; ++x ) { // correct branches
      NumBranch bx = branches.get(x);
      float e = 0.0f;
      float s = 0.0f;
      float v = 0.0f;
      for (int y=0; y<ls; ++y ) {
        NumCycle cy = indep_cycles.get(y);
        e += alpha[ y*bs + x ] * cy.ce;
        s += alpha[ y*bs + x ] * cy.cs;
        v += alpha[ y*bs + x ] * cy.cv;
      }
      bx.compensateError( -e, -s, -v );
    }
  }


  // need the loop length to compute the percent error
  private String getClosureError( NumStation at, NumStation fr, float d, float b, float c, float len )
  {
    float dv = (float)Math.abs( fr.v - d * (float)Math.sin(c * grad2rad) - at.v );
    float h0 = d * (float)Math.abs( Math.cos(c * grad2rad) );
    float ds = (float)Math.abs( fr.s - h0 * (float)Math.cos( b * grad2rad ) - at.s );
    float de = (float)Math.abs( fr.e + h0 * (float)Math.sin( b * grad2rad ) - at.e );
    float dh = ds*ds + de*de;
    float dl = (float)Math.sqrt( dh + dv*dv );
    dh = (float)Math.sqrt( dh );
    float error = (dl*100) / len;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%s-%s %.2f [%.2f %.2f] %.2f%%", fr.name, at.name,  dl, dh, dv, error );
    return sw.getBuffer().toString();
  }

  // ==========================================================================
  /** survey data reduction 
   * return true if all shots are attached
   */
  private boolean compute( List<DistoXDBlock> data, String start )
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

    mStations = new ArrayList< NumStation >();
    mShots    = new ArrayList< NumShot >();
    mSplays   = new ArrayList< NumSplay >();
    mClosures = new ArrayList< String >();
    mNodes    = new ArrayList< NumNode >();

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
    // TopoDroidApp.Log( TopoDroiaApp.LOC_NUM,
    //   "DistoXNum::compute tmp-shots " + tmpshots.size() + " tmp-splays " + tmpsplays.size() );


    NumStation start_station = new NumStation( start );
    mStations.add( start_station );
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( TmpShot ts : tmpshots ) {
        if ( ts.used ) continue;
        NumStation sf = getStation( ts.from );
        NumStation st = getStation( ts.to );
        if ( sf != null ) {
          if ( st != null ) { // close loop
            if ( /* TopoDroidApp.mAutoStations || */ ! TopoDroidApp.mLoopClosure ) {
              NumStation st1 = new NumStation( ts.to, sf, ts.d, ts.b, ts.c, ts.extend );
              st1.mDuplicate = true;
              mStations.add( st1 );
              addShotToStations( new NumShot( sf, st1, ts.block, 1 ), st1, sf );
            } else { // loop-closure
              addShotToStations( new NumShot( sf, st, ts.block, 1 ), sf, st );
            }
            if ( ts.duplicate ) {
              ++mDupNr;
            } else {
              if ( ts.surface ) {
                ++mSurfNr;
              } else {
                mLength += ts.d;
              }
            }

            // do close loop also on duplicate shots
            // need the loop length to compute the fractional closure error
            float length = shortestPath( sf, st) + ts.d;
            mClosures.add( getClosureError( st, sf, ts.d, ts.b, ts.c, length ) );
            
            ts.used = true;
            repeat = true;
          } else { // add from-->to
            st = new NumStation( ts.to, sf, ts.d, ts.b, ts.c, ts.extend );
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
            addShotToStations( new NumShot( sf, st, ts.block, 1 ), st, sf );
            ts.used = true;
            repeat = true;
          }
        } else if ( st != null ) {
          sf = new NumStation( ts.from, st, -ts.d, ts.b, ts.c, ts.extend );
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
          addShotToStations( new NumShot( st, sf, ts.block, -1), sf, st );
          ts.used = true;
          repeat = true;
        }
      }
    }
    // TopoDroidApp.Log( TopoDroiaApp.LOC_NUM, "DistoXNum::compute done leg shots ");

    if ( TopoDroidApp.mLoopClosure ) {
      doLoopCompensation( mNodes, mShots );
  
      // recompute station positions
      for ( NumShot sh : mShots ) {
        sh.mUsed = false;
      }
      for ( NumStation st : mStations ) { // mark stations as unset
        st.mHasCoords = false;
      }
      start_station.mHasCoords = true;
      repeat = true;
      while ( repeat ) {
        repeat = false;
        for ( NumShot sh : mShots ) {
          if ( ! sh.mUsed ) {
            NumStation s1 = sh.from;
            NumStation s2 = sh.to;
            if ( s1.mHasCoords && ! s2.mHasCoords ) {
              // reset s2 values from the shot
              float d = sh.block.mLength * sh.mDirection;
              float v = - d * (float)Math.sin( sh.block.mClino * grad2rad );
              float h = d * (float)Math.cos( sh.block.mClino * grad2rad );
              float e = h * (float)Math.sin( sh.block.mBearing * grad2rad );
              float s = - h * (float)Math.cos( sh.block.mBearing * grad2rad );
              s2.e = s1.e + e;
              s2.s = s1.s + s;
              s2.v = s1.v + v;
              s2.mHasCoords = true;
              sh.mUsed = true;
              repeat = true;
              // Log.v("DistoX", "reset " + s1.name + "->" + s2.name + " " + e + " " + s + " " + v );
            } else if ( s2.mHasCoords && ! s1.mHasCoords ) {
              // reset s1 values from the shot
              float d = - sh.block.mLength * sh.mDirection;
              float v = - d * (float)Math.sin( sh.block.mClino * grad2rad );
              float h = d * (float)Math.cos( sh.block.mClino * grad2rad );
              float e = h * (float)Math.sin( sh.block.mBearing * grad2rad );
              float s = - h * (float)Math.cos( sh.block.mBearing * grad2rad );
              s1.e = s2.e + e;
              s1.s = s2.s + s;
              s1.v = s2.v + v;
              s1.mHasCoords = true;
              sh.mUsed = true;
              repeat = true;
              // Log.v("DistoX", "reset " + s1.name + "<-" + s2.name + " " + e + " " + s + " " + v );
            }
          }
        }
      }
    }

    for ( TmpShot ts : tmpsplays ) {
      NumStation sf = getStation( ts.from );
      if ( sf != null ) {
        // TopoDroidApp.Log( TopoDroiaApp.LOC_NUM,
        //   "DistoXNum::compute splay from " + ts.from + " " + ts.d + " " + ts.b + " " + ts.c + " (extend " + ts.extend + ")" );
        mSplays.add( new NumSplay( sf, ts.d, ts.b, ts.c, ts.extend, ts.block ) );
      }
    }


    return (mShots.size() == tmpshots.size() );
  }
}


