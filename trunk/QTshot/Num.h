/** @file num.h 
 *
 * @author marco corvi
 * @date 1993 - revised 2010
 *
 * @brief survey data processing
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#ifndef NUM_H
#define NUM_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>


// #define scala 50 /* 1:500 */
/* #define scala 20 // 1:200 */

struct branch_t;
struct measure_t;

/** station point in 3D 
 */
struct point_t {
  const char * tag; //!< station name
  int         nms; //!< number of shots
  int         set;
  branch_t  * br; //!< branch 
  measure_t * ms[2];
  double      N, E, V; // , H, X, Y; //!< 3D coords
  point_t   * next;  //!< next point in the list
};

/** shot
 */
struct measure_t {
  const char * tag1; //!< name of "from" station
  const char * tag2; //!< name of "to" station
  point_t * pt1;
  point_t * pt2;
  double    dist, incl, nord;
  int       horz;
  measure_t *next, *prev;
  // measure_t *nest, *prew;
  int used;
};

struct loop_t {
  double x;
  int    nms;
  int    dir; //!< direction
  branch_t  * br;
  measure_t * ms;
  point_t   * pt;
  loop_t    * next, * prev;
};

struct branch_t {
  point_t   * pt;
  int         nms;
  measure_t ** ms;
  branch_t  * next;
};

struct homotopy_t {
  loop_t     * lp;
  homotopy_t * next;
};


class Num
{
  private:
    point_t    *pt00;
    measure_t  *ms00;
    measure_t  *ms20;
    branch_t   *br00;
    // loop_t     *lp00;
    homotopy_t *hm00;
    int measure_number;

  public:
    Num();

    ~Num();

    int MakePoints(int i_lp, const char * fix_point = NULL ); /* Crea la lista dei punti:       */
                                       /* ritorna il n. dei punti        */

    void AddMeasure(const char * from, const char * to,
                    double dist, double nord, double incl );

    void SetPoints();

    void PrintPoints();

    void ClearLists();

    const point_t * GetPoint( const char * name ) const;

  private:
    point_t * SecondPoint( measure_t * ms, point_t * pt );

    measure_t * SecondMeasure( point_t * pt, measure_t * ms );

    int AdjustHomotopy( homotopy_t * hm );

    void MakeHomotopy();

    measure_t * NextMeasure( loop_t * lp, measure_t * ms );

    measure_t * LoopMeasure( loop_t * lp );

    loop_t * IsInLoop( point_t * pt, loop_t * lp );

    void BranchNrMeasures( loop_t * lp );

    void MakeLoop( point_t * pt2, point_t * pt1, measure_t * ms1);

    void MakeBranch( point_t * pt, measure_t * ms );

    point_t * MakePoint(point_t * ptlast, point_t * pt1, measure_t *ms1, int sign);

    void SetPoint(point_t * pt3, point_t * pt1, measure_t * ms1, int sign);

    // void RescalePoints();

    void PrintPoint( point_t * pt );

    point_t * CheckPoint( const char * tag );

    void ResetMeasures();

    void ResetPoints();
};

#endif
