/** @file DBlock.h
 *
 * @author marco corvi
 * @date aug. 2008
 *
 * @brief survey shot data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DBLOCK_H
#define DBLOCK_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include <string>

#include "ArgCheck.h"

/** data blocklet - a survey shot
 */
struct DBlocklet
{
  double distance;
  double compass;
  double clino;
  double roll;       //!< roll
  DBlocklet * next;

    DBlocklet( double d, double b, double c, double r )
      : distance( d )
      , compass( b )
      , clino( c )
      , roll( r )
      , next( NULL )
    { 
      DBG_CHECK("new blocklet D: %.2f B: %.2f C: %.2f R %.2f\n", d, b, c, r );
    }

    /** get the next blocklet on the list
     * @return the next blocklet (or NULL)
     */
    DBlocklet * Next() { return next; }

    /** link a blocklet to the end of the list
     * @param blk blocklet to link to (may be NULL)
     */
    void Link( DBlocklet * blk )
    {
      if ( blk ) {
        DBlocklet * b = this;
        while ( b->next ) b=b->next;
        b->next = blk;
      }
    }

};

#ifdef HAS_LRUD
/** set of LRUD associated to a block
 */
struct LRUD
{
  double left;
  double right;
  double up;
  double down;

  LRUD( )
    : left( 0.0 )
    , right( 0.0 )
    , up( 0.0 )
    , down( 0.0 )
  { }

  LRUD( double l, double r, double u, double d )
    : left( l )
    , right( r )
    , up( u )
    , down( d )
  { }

  void Set( double l, double r, double u, double d )
  {
    left = l;
    right = r;
    up = u;
    down = d;
  }

  void Merge( const LRUD * lrud )
  {
    if ( lrud == NULL ) return;
    left  = (left + lrud->left)/ 2.0;
    right = (right + lrud->right)/ 2.0;
    up    = (up + lrud->up)/ 2.0;
    down  = (down + lrud->down)/ 2.0;
  }

};
#endif // HAS_LRUD

/** data block - a logical shot
 * several survey shot may correspond to a single logical shot,
 * eg, forward and backward, or repeated measurements
 */
class DBlock 
{
  friend class DataList;

  private:
    std::string from;       //!< FROM station name
    std::string to;         //!< TO station name
    std::string comment;    //!< shot comment
    double distance;        //!< tape [meters]
    double compass;         //!< azimuth [degrees]
    double clino;           //!< inclination [degrees]
    double roll;            //!< roll [degrees]
    unsigned char extend;   // 0 none, 1 left, 2 right, 3 vertical, 4 ignore
    unsigned char extended; // extend value use in therion export (for centerline shots only)
    unsigned char flag;     // 0 none, 1 surface, 2 duplicate
    int count;    // number of blocklets ?
    int need_paint; 
    DBlocklet * blocklet; //!< survey shots of this shot
#ifdef HAS_LRUD
    LRUD * lrud_from;       //!< optional LRUD data
    LRUD * lrud_to;         //!< optional LRUD data
#endif
    DBlock * next;

  public:
  
  /** cstr
   * @param f    from station
   * @param r    to station
   * @param d    distance
   * @param b    compass [degrees]
   * @param c    clino [degrees]
   * @param r    roll {degrees]
   * @param ext  extend in the extended section plot
   * @param flg  shot flags
   * @param cnt  number of blocklets(?)
   */
  DBlock( const char * f, const char * t,
          double d, double b, double c, double r,
          int ext, int flg, int cnt )
    : distance( d )
    , compass( b )
    , clino( c )
    , roll( r )
    , extend( ext ) 
    , extended( ext )
    , flag( flg )
    , count( cnt )
    , blocklet( NULL )
#ifdef HAS_LRUD
    , lrud_from( NULL )
    , lrud_to( NULL )
#endif
    , next( NULL )
  {
    setFrom( f );
    setTo( t );
    
    DBG_CHECK("new block D. %.2f B. %.2f C. %.2f R %.2f ext %d flag %d count %d\n",
              d, b, c, r, ext, flg, cnt );
  }

    /** destructor
     */
    ~DBlock()
    {
      ClearBlocklets();
      #ifdef HAS_LRUD
        if ( lrud_from ) delete lrud_from;
        if ( lrud_to ) delete lrud_to;
      #endif
    }

#ifdef HAS_LRUD
    /** set the LRUD data
     * @param l left
     * @param r right
     * @param u up
     * @param d down
     * @param at_from whether the LRUD are at the "from" station
     */
    void SetLRUD( double l, double r, double u, double d, bool at_from = true )
    {
      if ( at_from ) {
        if ( lrud_from == NULL ) {
          lrud_from = new LRUD( l, r, u, d );
        } else {
          lrud_from->Set( l, r, u, d );
        }
      } else {
        if ( lrud_to == NULL ) {
          lrud_to = new LRUD( l, r, u, d );
        } else {
          lrud_to->Set( l, r, u, d );
        }
      }
    }

    /** get the LRUD structure
     * @return the lrud
     */
    LRUD * LRUD_From () { return lrud_from; }
    LRUD * LRUD_To () { return lrud_to; }

  private:
    /** release the LRUD to the caller 
     * @param at_from whether the release the "From" LRUD
     * @return the LRUD that has been released
     */
    LRUD * ReleaseLRUD( bool at_from = true )
    {
      LRUD * ret = NULL;
      if ( at_from ) {
        ret = lrud_from;
        lrud_from = NULL;
      } else {
        ret = lrud_to;
        lrud_to = NULL;
      }
      return ret;
    }
#endif

  public:
    /** merge with the next block
     * @return true if merged
     */
    bool mergeNext()
    {
      if ( next == NULL ) return false;
      if ( count == 1 ) {
        blocklet = new DBlocklet( distance, compass, clino, roll );
      }
      DBlock * n = next;
      size_t cn = n->count;
      size_t c2 = count + cn;
      distance  = ( count * distance + cn * n->distance )/c2;
      clino     = ( count * clino    + cn * n->clino    )/c2;
      roll      = 0.0; // FIXME
      if ( fabs( compass - n->compass ) < 180 ) {
        compass   = ( count * compass  + cn * n->compass  )/c2;
      } else if ( compass > n->compass ) {
        compass   = ( count * compass  + cn * ( n->compass + 360.0 )  )/c2;
        if ( compass >= 360.0 ) compass -= 360.0;
      } else {
        compass   = ( count * compass  + cn * ( n->compass - 360.0 )  )/c2;
        if ( compass < 0.0 ) compass += 360.0;
      }
      count = c2;
      DBlocklet * b = blocklet;
      while ( b->Next() ) b=b->Next();
      if ( cn == 1 ) {
        b->next = new DBlocklet( n->distance, n->compass, n->clino, n->roll );
      } else {
        b->next = n->blocklet;
      }
      #ifdef HAS_LRUD
        LRUD * lrud = n->ReleaseLRUD( true );
        if ( lrud_from && lrud ) {
          lrud_from->Merge( lrud );
          delete lrud;
        } else {
          lrud_from = lrud;
        }
        lrud = n->ReleaseLRUD( false );
        if ( lrud_to && lrud ) {
          lrud_to->Merge( lrud );
          delete lrud;
        } else {
          lrud_to = lrud;
        }
      #endif
      next = n->next;
      delete n;
      return true;
    }

  /** split the block in the component blocklets
   */
  bool split()
  {
    DBG_CHECK("DBlock::split() count %d \n", count );
    if ( count == 1 ) return false;
    DBlocklet * b = blocklet->Next();
    DBlock * n  = next;
    DBlock * b1 = this;
    while ( b ) {
      b1->next = new DBlock( "", "",
                             b->distance, b->compass, b->clino, b->roll, 
                             extend, flag, 1 );
      b1 = b1->next;
      b  = b->next;
    }
    b1->next = n;
    distance = blocklet->distance;
    compass  = blocklet->compass;
    clino    = blocklet->clino;
    roll     = blocklet->roll;
    count = 1;
    while ( blocklet ) {
      b = blocklet->Next();
      delete blocklet;
      blocklet = b;
    }
    return true;
  }
    

  /** renumber from the following block
   * @note applies only if both from and to are non-empty
   */
  void renumber()
  {
    size_t size = from.size();
    size_t size_to = to.size();
    if ( size == 0 || size_to == 0 ) {
      return;
    }
    if ( to.size() > size ) size = to.size();
    size += 8;
    char * from0 = (char *)malloc( size );
    char * to0   = (char *)malloc( size );
    char * tmp   = (char *)malloc( size );
    strcpy( from0, from.c_str() );
    strcpy( to0,   to.c_str() );
    for ( DBlock * b = next; b; b=b->next ) {
      if ( b->to.size() == 0 ) {
        b->from = from0;
      } else {
        int f=0, t=0;
        char * cf = from0;
        while ( *cf && ! isdigit( *cf ) ) ++cf;
        if ( ! cf || ! (*cf) ) break;
        char * cf1 = cf;
        while ( *cf1 && isdigit(*cf1) ) {
          f = 10*f + ((*cf1)-'0');
          ++cf1;
        }
        char * ct = to0;
        while ( *ct && ! isdigit( *ct ) ) ++ct;
        if ( ! ct || ! (*ct) ) break;
        char * ct1 = ct;
        while ( *ct1 && isdigit(*ct1) ) {
          t = 10*t + ((*ct1)-'0');
          ++ct1;
        }
        ++f; 
        ++t;
        if ( *cf1 ) {
          strcpy(tmp, cf1 );
          sprintf(cf, "%d%s", f, tmp);
        } else {
          sprintf(cf, "%d", f );
        }
        if ( *ct1 ) {
          strcpy(tmp, ct1 );
          sprintf(ct, "%d%s", t, tmp);
        } else {
          sprintf(ct, "%d", t);
        }
        b->from = from0;
        b->to = to0;
      }
    }
    free( from0 );
    free( to0 );
    free( tmp );
  }   

  /** append a blocklet (take ownership)
   * @param blk blocklet to append
   *
   * @note inefficient, but blocklet list should be very short (2 or 3)
   */
  void addBlocklet( DBlocklet * blk )
  {
    if ( blocklet == NULL ) {
      blocklet = blk;
    } else {
      DBlocklet * blk_prev = blocklet;
      while ( blk_prev->next ) blk_prev = blk_prev->next;
      blk_prev->next = blk;
    }
  }

  /** clear the list of blocklets (and delete them)
   */
  void ClearBlocklets()
  {
    DBlocklet * blk = blocklet;
    DBlocklet * blk_next;
    while ( blk ) {
      blk_next = blk->next;
      delete blk;
      blk = blk_next;
    }
    blocklet = NULL;
  }

    /** compute need_paint and return it
     * @return 0 if both from and to are empty
     *         1 if from is not empty but to is empty
     *         2 if from is empty but to is not empty
     *         3 if both from and to are not empty
     */
    int evalNeedPaint()
    {
      need_paint = 0;
      if ( from.size() > 0 ) need_paint += 1;
      if ( to.size() > 0 ) need_paint += 2;
      return need_paint;
    }

    int NeedPaint() const { return need_paint; }

    void setNeedPaint( int np ) { need_paint = np; }

    /** check if this shot is splay
     * @return true if the sjpt is splay or not-used
     */
    bool isSplay() { return to.size() == 0 || from.size() == 0; }
 
    bool hasNoStation() { return to.size() == 0 && from.size() == 0; }

    bool hasFrom() const { return from.size() != 0; }

    bool hasTo() const { return to.size() != 0; }

    bool hasFrom( const char * f ) const { return from == f; }

    bool hasFrom( const std::string & f ) const { return from == f; }

    bool hasTo( const char * t ) const { return to == t; }

    bool hasTo( const std::string & t ) const { return to == t; }

    /** swaps stations
     * @return true if the two stations have been exchanged
     */
    bool swapStations()
    {
      if ( to.size() > 0 && from.size() > 0 ) {
        std::string tmp = to;
        to = from;
        from = tmp;
        return true;
      }
      return false;
    }

  // ---------------------------------------------------
  // ACCESSORS

    const char * From() const { return from.c_str(); }
  
    const char * To() const { return to.c_str(); }
 
    /** accessor to the shot comment
     * @return the shot comment
     */
    const char * Comment() const { return comment.c_str(); }

    bool hasComment() const { return comment.size() > 0; }

    double Tape() const { return distance; }
    double Compass() const { return compass; }
    double Clino() const { return clino; }
    double Roll() const { return roll; }
    unsigned char Extend() const { return extend; }
    unsigned char Extended() const { return extended; }
    unsigned char Flag() const { return flag; }

    DBlock * Next() { return next; }

    DBlocklet * Blocklets() { return blocklet; }
    int NrBlocklets() const { return count; }

  // ---------------------------------------------------
  // modifiers

    /** set the name of the FROM station
     * @param f  new name of FROM
     */
    void setFrom( const char * f )
    {
      if ( f && strlen(f) > 0 ) {
        while ( f && *f != 0 && isspace( *f ) ) ++f;
        const char * ch = f + strlen(f);
        while ( ch > f && isspace( *(ch-1) ) ) --ch;
        if ( ch > f ) {
          from = f;
          from.resize( ch - f );
        }
      }
    }

    /** set the name of the TO station
     * @param t  new name of TO
     */
    void setTo( const char * t )
    {
      if ( t && strlen(t) > 0 ) {
        while ( t && *t != 0 && isspace( *t ) ) ++t;
        const char * ch = t + strlen(t);
        while ( ch > t && isspace( *(ch-1) ) ) --ch;
        if ( ch > t ) {
          to = t;
          to.resize( ch - t );
        }
      }
    }

    /** set the name of both stations
     * @param f  new name of FROM
     * @param t  new name of TO
     */
    void setStations( const char * f, const char * t = NULL )
    {
      setFrom( f );
      setTo( t );
    }

    /** accessor: modify the shot comment
     * @param cmt new comment
     */
    void setComment( const char * cmt ) { if ( cmt ) comment = cmt; }

    void setExtend( unsigned char e ) 
    { 
      extend   = e;
      extended = e;
    }

    void setExtended( unsigned char e ) { extended = e; }

    void setFlag( unsigned char f ) { flag = f; }

};

#endif 

