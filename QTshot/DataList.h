/** @file DataList.h
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux measurements data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef DATA_LIST_H
#define DATA_LIST_H

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <assert.h>

#include <map>
#include <string>

#include "DBlock.h"
#include "DistoX.h"
#include "CenterlineInfo.h"
#include "Units.h"

#include "PlotDrawer.h"
#include "PlotStatus.h"

#include "Num.h"

/** list of logical shots (= blocks)
 */
class DataList
{
  private:
    DBlock * head;        //!< head of the list
    DBlock * base_block;  //!< base shot for centerline numerics
    int list_size;             //!< number of logical shots on the list
    int shot;             //!< number of logical shots w. more than one measure
    std::map< std::string, std::string > station_comment;

    bool need_num;             //!< whether needs to do num
    int  num_measures;         //!< number of shots in num
    Num num;                   //!< centerline numerics

  public:
    DataList()
    : head( NULL )
    , base_block( NULL )
    , list_size( 0 )
    , shot( 0 )
    , need_num( false )  // empty data list does not need num
    , num_measures( 0 )
  { }

  ~DataList()
  {
    clear();
  }

  /** get the head of the list of data
   * @return the head block
   */
  DBlock * listHead() { return head; }

  /** get the number of shots
   * @return the size of the list
   */
  int listSize() const { return list_size; }

  /** get the number of centerline shots
   * @return the number of shots with more than one measure
   *
  int centerlineSize() const { return shot; }
   */

  /** check if a block is the base block
   * @param block block to check
   * @return true if the the block is the base block
   */
  bool isBaseBlock( DBlock * block ) const { return block == base_block; }

  /** get the base block
   * @return the base block (or NULL if not set)
   */
  DBlock * getBaseBlock() { return base_block; }

  /** set the base block
   * @param block  new base block (use NULL to unset)
   */
  void setBaseBlock( DBlock * block )
  {
    base_block = block;
  }

  /** get a reference to the numerical engine
   * @return a const ref to the numerical engine
   */
  const Num & getNum() const { return num; }

  /** set the flag that indicates the need to recompute the survey
   */
  void resetNum() { need_num = true; }

  /** compute the survey
   * @force  force recomputing the centerline [default false]
   * @return the number of measures (= centerline shots)
   */
  int doNum( bool force = false );


  /** check if the list has a shot with given stations
   * @param from   FROM station name
   * @param to     TO station name
   * @return true if the list has a shot from-to, 
   */
  bool hasBlock( const char * from, const char * to )
  {
    DBlock * b = head;
    while ( b != NULL ) {
      if ( b->fromStation() == from && b->toStation() == to ) return true;
      if ( b->fromStation() == to   && b->toStation() == from ) return true;
      b = b->next_block;
    }
    return false;
  }

  /** drop and delete a block
   * @param block block to delete
   */
  void dropBlock( DBlock * block )
  {
    if ( block != NULL ) {
      if ( head == block ) {
        head = block->next_block;
        -- list_size;
        if ( block->count > 1 ) --shot;
      } else {
        DBlock * b = head;
        while ( b && b->next_block != block ) b=b->next_block;
        if ( b != NULL ) {
          b->next_block = block->next_block;
          -- list_size;
          if ( block->count > 1 ) --shot;
        }
      }
      if ( block == base_block ) { // don't leave base_block dangling
        base_block = NULL;
      }
      delete block;
      need_num = true;
    }
  } 

  /** accessor: get the i-th block
   * @param pos   index of the block
   * @return pointer to the block
   */
  DBlock * getBlock( size_t pos );

  /** clear the list of blocks (and delete them)
   */
  void clear();

  /** write the data to a file (TopoLinux format)
   * @param c_info   centerline info
   * @return true if successful
   */ 
  bool saveTlx( CenterlineInfo & c_info );

  /** insert a block before or after another block
   * @param blk   the block where to insert
   * @param d     distance
   * @param b     conmpass
   * @param c     clino
   * @param r     roll
   * @param before whether to insert before blk
   *
   * Used by QTshotWidget to insert a block before or after another.
   */
  void insertBlock( DBlock * blk, double d, double b, double c, double r, bool before );

  /** load the data from a file 
   * @param drawer   plot drawer object
   * @param filename name of the file
   * @param append   whether to append the data to the list 
   * @param info     centerline info
   * @return error code:
   *   - 0 ok
   *   - 1 failed open
   *   - 2 failed read raw file
   *   - 3 failed read tlx file
   *   - 4 unsupported file type
   *
   * @note Supported formats: 
   *   - TopoLinux (v.1 and v.2),
   *   - raw (memory_dump),
   *   - PocketTopo (v.3)
   */
  int loadFile( PlotDrawer * drawer, const char * filename, bool append, CenterlineInfo * info = NULL );

  /** load the data directly from the DistoX class
   * @param disto DistoX class
   * @param append   whether to append the data to the list
   * @param smart    whether to use smart station assignment
   * @param splay    which station to assign splay shots (1: FROM, 2: TO)
   * @param backward whether shots are backward
   * @return true if successful
   */
  bool loadDisto( DistoX & disto, bool append, bool smart, int splay, bool backward );

  private:
    /** initialize the "from" and "to" values and the pointer to the
     * last block of the list
     * @param from    "from" index [output]
     * @param to      "to"index [output]
     * @param append   whether to append the data to the list
     * @return last block of the l;ist, or NULL if the list is empty
     *
     * @note "from" and "to" are always initialized so that to > from
     *       (usually, to = from+1); whether the shot is taken backward
     *       or is a splay-shot is taken care by the method createBlock()
     */
   DBlock * initFromTo( int & from, int & to, bool append );

    /** load the data from a TopoLinux file
     * @param fp       file stream
     * @param append   whether to append the data to the list 
     * @param info     centerline info
     * @param version  TLX file version
     * @return true if successful
     */
    bool loadTlx( FILE * fp, bool append, CenterlineInfo * info, int version );

    /** load the data from a PocketTopo file
     * @param drawer   plot drawer object 
     * @param fp       file stream
     * @return true if successful
     */
    bool loadPocketTopo( PlotDrawer * drawer, FILE * fp, CenterlineInfo * info );

    /** load the data from a raw file downloaded from DistoX
     * @param fp       file stream
     * @param append   whether to append the data to the list
     * @param smart    whether to use smart station assignment
     * @param splay    which station to attach splay shots (1: FROM, 2: TO)
     * @param backward whether shots are backward
     * @return true if successful
     */
    bool loadRaw( FILE * fp, bool append, bool smart, int splay, bool backward );

  public:

  /** update the info in a block
   * @param r  row index (block index)
   * @param c  column index (0: From, 1: To, 2-3-4 illegal, 5: extend, 6: flag, 7: comment
   * @param txt text (new value of the modified item)
   */
  void updateBlock( int r, int c, const char * txt );

  /** compute the extended flag for a splay shot 
   * @param b splay shot
   * @note this method requires that the extended flags have been
   *       already computed for the centerline shots
   */
  void evalSplayExtended( DBlock * b );
   
  /** compute the the extended flag for all splay shots
   *
  void evalSplayExtended()
  {
    // fprintf(stderr, "evalSplayExtended()\n");
    for ( DBlock * b = head; b != NULL; b=b->Next() ) {
      if ( b->hasFromStation() && ! b->hasToStation() ) {
        evalSplayExtended( b );
      }
    }
  }
  */

  void dump();

  /** update the "extend" value of a block
   * @param blk   block
   * @param extend new extend value
   */
  void updateExtend( DBlock * blk, int extend )
  {
    if ( ! blk ) return;
    // fprintf(stderr, "updateExtend() Block %s-%s extend %d\n", 
    //   blk->from.c_str(), blk->to.c_str(), extend );
    blk->extend = extend;
    need_num = true;
  }

  private:
    void computeAverage( double * d0, double * b0, double * c0, double * r0, int cnt,
                         double & dave, double & bave, double & cave, double & rave );

    /** construct a block from a set of shots
     * @param from first station
     * @param to   second station
     * @param d0   array of shots distances
     * ...
     * @param r0   roll(s)
     * @param cnt  number of shots
     * @return new block
     */
    DBlock * shotToBlock( int from, int to,
                          double * d0, double * b0, double * c0, double * r0, int cnt );

    /** check if two shots are close
     * @param d1 first shot distance
     * @param b1 first shot compass
     * ...
     * @return true if te two shots are close
     */
    bool shotIsClose( double d1, double b1, double c1, double d2, double b2, double c2 );

    /** create and insert a block
     * @param from     FROM index
     * @param to       TO index
     * @param d0       distance(s)
     * @param b0       compass(es)
     * @param c0       clino(s)
     * @param r0       roll(s)
     * @param cnt      number of shots of the block
     * @param splay    which station to attach if splay shot
     * @param backward whether shot is backward
     * @param last     last block
     * @param start    first block
     *
     * @note this method handles the forward/backward of the shots (in the
     *       "backward" case the "from" and "to" are swapped), and when the
     *       shot is splay. Notice that with backward sighting a play shot
     *       at "to" means a splay at the sighting station, while a splay
     *       at "from" is a splay at the sighted station.
     */
    void createBlock( int from, int to,
                      double * d0, double * b0, double * c0, double * r0, int cnt,
                      int splay, bool backward,
                      DBlock ** last, DBlock ** start );

    void insertBlock( DBlock * bb, DBlock ** last, DBlock ** start )
    {
      if ( (*last) == NULL ) {
        assert( head == NULL );
        head = bb; 
      } else {
        (*last)->next_block = bb;
      }
      if ( (*start) == NULL ) (*start) = bb;
      (*last) = bb;
      ++ list_size;
      if ( bb->count > 1) ++shot;
      need_num = true;
    }

  public:
    /** split a block into its blocklets
     * @param bb   block to split
     */
    bool splitBlock( DBlock * bb );

    /** merge a block with its following block
     * @param bb    block to merge with its following block
     */
    bool mergeBlock( DBlock * bb );

    /** check if there is a comment with a point
     * @param name station name
     * @return true if the point has a comment
     */
    bool hasStationComment( const char * name ) const 
    {
      return station_comment.find( name ) != station_comment.end();
    }

    /** get the comment of a point
     * @param name station name
     * @return the point comment
     */
    const char * getStationComment( const char * name )
    {
      return station_comment[name].c_str(); 
    }

    /** set a point comment
     * @param name station name
     * @param c    comment
     */
    void setStationComment(  const char * name, const char * c ) 
    { 
      if ( strlen(c) == 0 ) {
        std::map<std::string, std::string>::iterator it = station_comment.find( name );
        if ( it != station_comment.end() )
          station_comment.erase( it );
      } else {
        station_comment[name] = c;
      }
    }

    /** recompute the values for the multimeasure blocks
     * @param start where to start to recompute
     */
    void recomputeMultimeasureBlocks( DBlock * start );


};

#endif
