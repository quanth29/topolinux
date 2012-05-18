/* @file DistoXDBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX survey data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

// import java.lang.Long;
import java.io.StringWriter;
import java.io.PrintWriter;


public class DistoXDBlock
{
  private static final float grad2rad = TopoDroidApp.GRAD2RAD_FACTOR;

  public static final char[] mExtendTag = { '<', '|', '>', '-' };

  long   mId;
  long   mSurveyId;
  // private String mName;
  String mFrom;
  String mTo;
  float mLength;   // meters
  float mBearing;  // degrees
  float mClino;    // degrees
  float mRoll;     // degrees
  String mComment;
  long   mExtend;
  long   mFlag;     

  public static final int BLOCK_BLANK      = 0;
  public static final int BLOCK_CENTERLINE = 1;
  public static final int BLOCK_SPLAY      = 2;

  // block colors: blank, centerline, splay, ...
  private static int[] colors = { 0xffffcccc, 0xffffffff, 0xffccccff, 0xffccffcc };

  public static final int BLOCK_SURVEY     = 0; // flags
  public static final int BLOCK_SURFACE    = 1;
  public static final int BLOCK_DUPLICATE  = 2;

  public boolean isSurvey() { return mFlag == BLOCK_SURVEY; }
  public boolean isSurface() { return mFlag == BLOCK_SURFACE; }
  public boolean isDuplicate() { return mFlag == BLOCK_DUPLICATE; }

  public DistoXDBlock()
  {
    mId = 0;
    mSurveyId = 0;
    // mName = "";
    mFrom = "";
    mTo   = "";
    mLength = 0.0f;
    mBearing = 0.0f;
    mClino = 0.0f;
    mRoll = 0.0f;
    mComment = "";
    mExtend = 0;
    mFlag   = BLOCK_SURVEY;
  }

  public void setId( long shot_id, long survey_id )
  {
    mId       = shot_id;
    mSurveyId = survey_id;
  }

  public void setName( String from, String to ) 
  {
    mFrom = from.trim();
    mTo   = to.trim();
  }
  public String Name() { return mFrom + "-" + mTo; }
  
  public void setBearing( float x ) {
    mBearing = x;
    if ( mBearing < 3.14 ) {  // east to the right, west to the left
      mExtend = 1;
    } else {
      mExtend = -1;
    }
  }

  public int type() 
  {
    if ( mFrom == null || mFrom.length() == 0 ) {
      if ( mTo == null || mTo.length() == 0 ) {
        return BLOCK_BLANK;
      }
      return BLOCK_SPLAY;
    }
    if ( mTo == null || mTo.length() == 0 ) {
      return BLOCK_SPLAY;
    }
    return BLOCK_CENTERLINE;
  }

  public int color()
  {
    return colors[ type() ];
  }

  public float relativeDistance( DistoXDBlock b )
  {
    if ( b == null ) return 10000.0f; // a large distance
    float cc = (float)Math.cos(mClino * grad2rad);
    float sc = (float)Math.sin(mClino * grad2rad);
    float cb = (float)Math.cos(mBearing * grad2rad); 
    float sb = (float)Math.sin(mBearing * grad2rad); 
    Vector v1 = new Vector( mLength * cc * sb, mLength * cc * cb, mLength * sc );
    cc = (float)Math.cos(b.mClino * grad2rad);
    sc = (float)Math.sin(b.mClino * grad2rad);
    cb = (float)Math.cos(b.mBearing * grad2rad); 
    sb = (float)Math.sin(b.mBearing * grad2rad); 
    Vector v2 = new Vector( b.mLength * cc * sb, b.mLength * cc * cb, b.mLength * sc );
    float dist = (v1.minus(v2)).Length();
    return dist/mLength + dist/b.mLength; 
  }

  public String toString()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("%d <%s-%s> %.2f %.1f %.1f [%c]",
      mId, mFrom, mTo, mLength, mBearing, mClino, mExtendTag[ (int)(mExtend) + 1 ] );
    if ( mFlag == 1 ) pw.format( " *" );
    return sw.getBuffer().toString();
  }

}
