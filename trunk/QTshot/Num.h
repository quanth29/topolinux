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

struct NumBranch;
struct NumMeasure;

struct NumStats
{
  int n_shots;
  int n_stations;
  int n_loops;
  double z_max, z_min;
  double n_max, n_min;
  double e_max, e_min;
  double delta_z()     const { return z_max - z_min; }
  double delta_north() const { return n_max - n_min; }
  double delta_east()  const { return e_max - e_min; }

  NumStats()
    : n_shots( 0 )
    , n_stations( 0 )
    , n_loops( 0 )
    , z_max( -10000 )
    , z_min( 10000 )
    , n_max( 1000000 )
    , n_min( -1000000 )
    , e_max( 1000000 )
    , e_min( -1000000 )
  { }

  void Reset()
  {
    n_shots = 0;
    n_stations = 0;
    n_loops = 0;
    z_max =   -10000;
    z_min =    10000;
    n_max = -1000000;
    n_min =  1000000;
    e_max = -1000000;
    e_min =  1000000;
  }
};


/** station point in 3D 
 */
struct NumPoint {
  const char * tag; //!< station name
  int         nms; //!< number of shots
  int         set;
  NumBranch  * br; //!< branch 
  NumMeasure * ms[2];
  double      N, E, V; // , H, X, Y; //!< 3D coords
  NumPoint   * next;  //!< next point in the list
};

/** shot
 */
struct NumMeasure {
  const char * tag1; //!< name of "from" station
  const char * tag2; //!< name of "to" station
  NumPoint * pt1;
  NumPoint * pt2;
  double    dist, incl, nord;
  int       horz;
  NumMeasure *next, *prev;
  // NumMeasure *nest, *prew;
  int used;
};

struct NumLoop {
  double x;
  int    nms;
  int    dir; //!< direction
  NumBranch  * br;
  NumMeasure * ms;
  NumPoint   * pt;
  NumLoop    * next, * prev;
};

struct NumBranch {
  NumPoint   * pt;
  int         nms;
  NumMeasure ** ms;
  NumBranch  * next;
};

struct NumHomotopy {
  NumLoop     * lp;
  NumHomotopy * next;
};


class Num
{
  private:
    NumPoint    *pt00;
    NumMeasure  *ms00;
    NumMeasure  *ms20;
    NumBranch   *br00;
    // NumLoop     *lp00;
    NumHomotopy *hm00;
    int measure_number;
    NumStats   stats;

  public:
    Num();

    ~Num();

    const NumStats & getStats() const { return stats; }

    int makePoints(int i_lp, const char * fix_point = NULL ); /* Crea la lista dei punti:       */
                                       /* ritorna il n. dei punti        */

    void addMeasure(const char * from, const char * to,
                    double dist, double nord, double incl );

    void setPoints();

    void printPoints();

    void clearLists();

    const NumPoint * getPoint( const char * name ) const;

  private:
    NumPoint * secondPoint( NumMeasure * ms, NumPoint * pt );

    NumMeasure * secondMeasure( NumPoint * pt, NumMeasure * ms );

    int adjustHomotopy( NumHomotopy * hm );

    void makeHomotopy();

    NumMeasure * nextMeasure( NumLoop * lp, NumMeasure * ms );

    NumMeasure * loopMeasure( NumLoop * lp );

    NumLoop * isInLoop( NumPoint * pt, NumLoop * lp );

    void branchNrMeasures( NumLoop * lp );

    void makeLoop( NumPoint * pt2, NumPoint * pt1, NumMeasure * ms1);

    void makeBranch( NumPoint * pt, NumMeasure * ms );

    NumPoint * makePoint(NumPoint * ptlast, NumPoint * pt1, NumMeasure *ms1, int sign);

    void setPoint(NumPoint * pt3, NumPoint * pt1, NumMeasure * ms1, int sign);

    // void rescalePoints();

    void printPoint( NumPoint * pt );

    NumPoint * checkPoint( const char * tag );

    void resetMeasures();

    void resetPoints();
};

#endif
