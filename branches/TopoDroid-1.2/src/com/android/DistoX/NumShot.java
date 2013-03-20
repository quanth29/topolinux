/** @file NumShot.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created fron DistoXNum
 */
package com.android.DistoX;

public class NumShot 
{
  NumStation from;
  NumStation to;
  DistoXDBlock block;
  int mBranchDir; // branch direction
  int mDirection; // direction of the block (1 same, -1 opposite)
  NumBranch branch;
  boolean mUsed;  // whether the shot has been used in the station coords recomputation after loop-closure
  boolean mIgnoreExtend;

  NumShot( NumStation f, NumStation t, DistoXDBlock blk, int dir )
  {
    from = f;
    to   = t;
    block = blk;
    mIgnoreExtend = ( blk.mExtend == DistoXDBlock.EXTEND_IGNORE);
    mUsed = false;
    mDirection = dir;
    mBranchDir = 0;
    branch = null;
  }

  float length() { return block.mLength; }

}

