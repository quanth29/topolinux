/** @file num_ut.c
 *
 * @author marco corvi
 * @date 1993
 *
 * @brief survey data processing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include "Num.h"

#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>

#ifndef M_PI
  #define M_PI 3.14159265358979323846 
#endif

#include "ArgCheck.h"


#define NEW_PT(PT) ((PT) = (NumPoint *)malloc(sizeof(NumPoint)))
#define NEW_MS(MS) ((MS) = (NumMeasure *)malloc(sizeof(NumMeasure)))
#define NEW_LP(LP) ((LP) = (NumLoop *)malloc(sizeof(NumLoop)))
#define NEW_BR(BR) ((BR) = (NumBranch *)malloc(sizeof(NumBranch)))
#define NEW_HM(HM) ((HM) = (NumHomotopy *)malloc(sizeof(NumHomotopy)))

/*
double Vmin,Vmax,Vmed,Vdif,
       Hmin,Hmax,Hmed,Hdif,
       Nmin,Nmax,Nmed,Ndif,
       Emin,Emax,Emed,Edif,
       Xmin,Xmax,Xmed,
       Ymin,Ymax,Ymed;
*/

Num::Num()
  : pt00( NULL )
  , ms00( NULL )
  , ms20( NULL )
  , br00( NULL )
  // , lp00( NULL )
  , hm00( NULL )
  , measure_number( 0 )
{ }

Num::~Num()
{
  clearLists();
}

/* ----------------------------------------------------------------- */
double COSD( double x )
{ return( cos(x*M_PI/180.0) ); }

double SIND( double x )
{ return( sin(x*M_PI/180.0) ); }

double ATAND2( double y, double x )
{ return( (atan2(y,x))*180.0/M_PI ); }

/* ----------------------------------------------------------------- */
double LENGTH( double V, double N, double E)
{ return(sqrt(V*V+N*N+E*E)); }

void POLAR_RECT(double D, double I, double A, double * V, double * N, double * E )
{ 
  ARG_CHECK( V == NULL, );
  ARG_CHECK( N == NULL, );
  ARG_CHECK( E == NULL, );

  double H;
  *V = D * SIND(I);
   H = D * COSD(I);
  *N = H * COSD(A);
  *E = H * SIND(A);
}

void RECT_POLAR(double V, double N, double E, double * D, double * I, double * A)
{
  ARG_CHECK( D == NULL, );
  ARG_CHECK( I == NULL, );
  ARG_CHECK( A == NULL, );

  double H;
  *D = LENGTH(V,N,E);
  *A = ATAND2(E,N);
  if (*A < 0.0) *A += 360.0;
   H = sqrt(N*N + E*E);
  *I = ATAND2(V,H);
}

/* ----------------------------------------------------------------- */
void 
Num::clearLists()
{
  while ( pt00 ) {
    NumPoint * pt1 = pt00->next;
    free( pt00 );
    pt00 = pt1;
  }

  while ( ms00 ) {
    NumMeasure * ms1 = ms00->next;
    free( ms00 );
    ms00 = ms1;
  }
  ms20 = NULL;

  while ( br00 ) {
    NumBranch * br1 = br00->next;
    free( br00 );
    br00 = br1;
  }

  while ( hm00 ) {
    NumLoop * lp1 = hm00->lp;
    while ( lp1 ) {
      NumLoop * lp2 = lp1->next;
      free( lp1 );
      lp1 = lp2;
    } 
    NumHomotopy * hm1 = hm00->next;
    free( hm00 );
    hm00 = hm1;
  }

  measure_number = 0;
}

/* ----------------------------------------------------------------- */
#if 0
int FIND_BBOX()        /* Trova i limiti dei punti (in mm.) per      */
{                      /* centrare il disegno.                       */
  NumPoint *pt1;

  Vmin = Vmax = pt00->V;
  Hmin = Hmax = pt00->H;
  Nmin = Nmax = pt00->N;
  Emin = Emax = pt00->E;
  for (pt1=pt00->next; pt1!=NULL; pt1=pt1->next) {
      if (pt1->N > Nmax) Nmax = pt1->N;  if (pt1->N < Nmin) Nmin = pt1->N;
      if (pt1->E > Emax) Emax = pt1->E;  if (pt1->E < Emin) Emin = pt1->E;
      if (pt1->H > Hmax) Hmax = pt1->H;  if (pt1->H < Hmin) Hmin = pt1->H;
      if (pt1->V > Vmax) Vmax = pt1->V;  if (pt1->V < Vmin) Vmin = pt1->V;
  }
  Vmed = (Vmax+Vmin)/2;   Vdif = Vmax-Vmin; 
  Hmed = (Hmax+Hmin)/2;   Hdif = Hmax-Hmin;
  Nmed = (Nmax+Nmin)/2;   Ndif = Nmax-Nmin;
  Emed = (Emax+Emin)/2;   Edif = Emax-Emin;
  printf(" %6.2f  %6.2f  %6.2f  %6.2f \n",Vmin,Vmax,Hmin,Hmax);
  printf(" %6.2f  %6.2f  %6.2f  %6.2f \n",Nmin,Nmax,Emin,Emax);
}
#endif
/* ================================================================= */
NumPoint * 
Num::secondPoint( NumMeasure * ms, NumPoint * pt )
{
  // ARG_CHECK( ms == NULL, NULL );
  ARG_CHECK( pt == NULL, NULL );
  
  NumPoint *pt1, *pt2;
  if (ms == NULL) return(NULL);
  pt1 = ms->pt1;
  pt2 = ms->pt2;
  if (pt == pt1) return(pt2);
  if (pt == pt2) return(pt1);
  return NULL;
}
/* ----------------------------------------------------------------- */
NumMeasure * 
Num::secondMeasure( NumPoint * pt, NumMeasure * ms)
{
  ARG_CHECK( pt == NULL, NULL );
  ARG_CHECK( ms == NULL, NULL );
  
  NumMeasure *ms1, *ms2;
  if (pt == NULL) return(NULL);
  ms1 = pt->ms[0];
  ms2 = pt->ms[1];
  if (ms == ms1) return(ms2);
  if (ms == ms2) return(ms1);
  return NULL;
}
/* ----------------------------------------------------------------- */
/* ----------------------------------------------------------------- */
int 
Num::adjustHomotopy( NumHomotopy * hm )
{
  ARG_CHECK( hm == NULL, 0 );
  
  NumLoop *lp1;
  double V,N,E, V1,N1,E1, D,I,A;

  // printf("AdjustHomotopy \n");
  V=N=E=0.0;
  for (lp1=hm->lp; lp1!=NULL; lp1=lp1->next) {
    // printf("  (%s %s) D %.2f N %.2f I %.2f dir %d x %.2f\n",
    //   lp1->ms->tag1, lp1->ms->tag2, lp1->ms->dist, lp1->ms->nord, lp1->ms->incl, lp1->dir, lp1->x );
    POLAR_RECT(lp1->ms->dist,lp1->ms->incl,lp1->ms->nord, &V1, &N1, &E1);
    // printf("     N %.2f E %.2f V %.2f \n", N1, E1, V1 );
    V -= V1 * lp1->dir;
    N -= N1 * lp1->dir;
    E -= E1 * lp1->dir;
  }
  if (LENGTH(V,N,E) < 0.01) {
    // printf("Loop length %.2f too small\n", LENGTH(V,N,E) );
    return(0);
  }
  // printf("Loop displacement: N %.2f E %.2f V %.2f \n", N, E, V );
  for (lp1=hm->lp; lp1!=NULL; lp1=lp1->next) {
    POLAR_RECT(lp1->ms->dist,lp1->ms->incl,lp1->ms->nord, &V1, &N1, &E1);
    V1 += V*lp1->x * lp1->dir;
    E1 += E*lp1->x * lp1->dir;
    N1 += N*lp1->x * lp1->dir;
    RECT_POLAR(V1,N1,E1,&D,&I,&A);
    // printf("     N %.2f E %.2f V %.2f D %.2f I %.2f A %.2f\n", N1, E1, V1, D, I, A );
    lp1->ms->dist = D;
    lp1->ms->incl = I;
    lp1->ms->nord = A;  
    // printf("  (%s %s) D %.2f N %.2f I %.2f dir %d\n",
    //   lp1->ms->tag1, lp1->ms->tag2, lp1->ms->dist, lp1->ms->nord, lp1->ms->incl, lp1->dir );
  }
  return(1);
}
/* ----------------------------------------------------------------- */
void 
Num::makeHomotopy()
{
  int flag=1;
  NumHomotopy *hm1;
  while (flag) {
    flag=0;
    for (hm1=hm00; hm1!=NULL; hm1=hm1->next) 
      if (adjustHomotopy(hm1)) flag=1;
  }
}
/* ================================================================= */
NumMeasure * 
Num::nextMeasure( NumLoop * lp, NumMeasure * ms )
{
  ARG_CHECK( lp == NULL, NULL );
  ARG_CHECK( ms == NULL, NULL );

  NumMeasure * ms1;
  NumPoint   * pt1;
  pt1=lp->pt;
  if (lp->br) { 
    if ( lp->nms == lp->br->nms ) 
      return NULL;
    ms1 = (lp->br->ms)[ lp->nms ];
    while ( secondPoint(ms1,lp->pt) == (lp->prev)->pt) {
      ++ lp->nms;
      if ( lp->nms == lp->br->nms ) 
        return NULL;
      ms1 = (lp->br->ms)[ lp->nms ];
    }
  } else {
    ms1 = secondMeasure(pt1,ms);
  }
  return(ms1);
}
/* ----------------------------------------------------------------- */
NumMeasure * 
Num::loopMeasure( NumLoop * lp )
{
  ARG_CHECK( lp == NULL, NULL );

  NumMeasure * ms1 = NULL;
  if (lp->br) {
    if ( lp->nms != lp->br->nms ) {
      ms1 = (lp->br->ms)[ lp->nms ];
      // printf("LoopMeasure branch %s nms %d/%d ms1 %s %s\n",
      //   lp->pt->tag, lp->nms, lp->br->nms, ms1->tag1, ms1->tag2 );
    }
  } else {
    if ( lp->nms < lp->pt->nms ) {
      ms1 = lp->pt->ms[ lp->nms ];
      // printf("LoopMeasure point  %s nms %d ms1 %s %s\n",
      //   lp->pt->tag, lp->nms, ms1->tag1, ms1->tag2 );
    }
  }
  lp->ms = ms1;
  return(ms1);
}
/* ----------------------------------------------------------------- */
NumLoop * 
Num::isInLoop( NumPoint * pt, NumLoop * lp )
{
  ARG_CHECK( pt == NULL, NULL );
  ARG_CHECK( lp == NULL, NULL );

  NumLoop *lp1;
  for (lp1=lp; lp1!=NULL; lp1=lp1->next)
    if (lp1->pt == pt) 
      return lp1;
  return NULL;
}
/* ----------------------------------------------------------------- */
void 
Num::branchNrMeasures( NumLoop * lp )
{ 
  ARG_CHECK( lp == NULL, );

  // if ( (lp->nms <= lp->br->nms) &&
  if ( (lp->nms < lp->br->nms) &&
       (lp->prev != NULL) &&
       (secondPoint( (lp->br->ms)[lp->nms], lp->pt ) == NULL ) )
       // FIXME original has: SECOND_PT(lp->br->ms+lp->nms, lp->pt)
     ++ lp->nms;
  // printf("after BranchNrMeasures %s nms %d/%d\n", 
  //   lp->pt->tag, lp->nms, lp->br->nms );
}
/* ----------------------------------------------------------------- */
void 
Num::makeLoop( NumPoint * pt2, NumPoint * pt1, NumMeasure * ms1)
{
  ARG_CHECK( pt2 == NULL, );
  ARG_CHECK( pt1 == NULL, );
  ARG_CHECK( ms1 == NULL, );

  NumPoint    *pt3, *pt4;
  NumLoop     *lp0, *lp2, *lp3, *lp4;
  NumMeasure  *ms3; // , *ms4;
  NumHomotopy *hm1;
  double     x;
  int forward = 1;

  // printf("MakeLoop at %s %s ms %s %s \n", pt2->tag, pt1->tag, ms1->tag1, ms1->tag2 );
  // PrintPoints();
  // { int i; scanf("%d", &i ); }

  NEW_LP(lp0);
  stats.n_loops ++;  // STATISTICS

  lp3 = lp0;
  pt3 = lp3->pt = pt2;
  lp3->next = lp3->prev = NULL;
  lp3->br = pt2->br;

  lp3->nms = 0; // using measure nr. 0
  ms3 = loopMeasure(lp3);
  // start from pt2 and try to get to pt1 following ms3
  while (pt3 != pt1) {
    pt4 = secondPoint(ms3,pt3);
    // printf("  next point %s \n", pt4 ? pt4->tag : "NULL" );

    if (pt4 == NULL) {
      // printf("  NULL pt4: backtracing ...\n");
      while ((lp3!=lp0) && ((lp3->br==NULL) || (lp3->nms >= lp3->br->nms)) ) {
        lp4=lp3->prev; // backtrace
        free(lp3);
        lp3=lp4;
        lp3->next = NULL;
        ++ lp3->nms;
        if (lp3->br) branchNrMeasures(lp3);
        // printf("  ... back to %s (nms %d) br->nms %d \n",
        //   lp3 ? lp3->pt->tag : NULL,
        //   lp3->nms,
        //   lp3->br ? lp3->br->nms : 0  );
      }
      pt3 = lp3->pt;
      // printf("  branched back to %s \n", pt3->tag );
      ms3 = loopMeasure(lp3);
      // { int i; scanf("%d", &i ); }
    } else if ( (lp2 = isInLoop(pt4,lp0)) != NULL ) {
      // printf("  lp2 %s is in loop %s - %s back to lp2\n", lp2->pt->tag, pt4->tag, lp0->pt->tag );
      ++ lp3->nms;
      if (lp3->br) branchNrMeasures(lp3);
      while ((lp3!=lp2) && ((lp3->br==NULL) || (lp3->nms >= lp3->br->nms)) ) {
        lp4 = lp3->prev;
        free(lp3);
        lp3 = lp4;
        lp3->next = NULL;
        ++ lp3->nms;
        // printf("  ... back to %s\n", lp3 ? lp3->pt->tag : NULL );
        if (lp3->br) branchNrMeasures(lp3);
      }
      pt3 = lp3->pt;
      // printf("  point is in loop lp0: go to %s \n", pt3->tag );
      ms3 = loopMeasure(lp3);
      // { int i; scanf("%d", &i ); }
    } else {
      NEW_LP(lp4);
      stats.n_loops ++;  // STATISTICS
      lp3->next = lp4; lp4->next = NULL; lp4->prev = lp3;
      lp3 = lp4;
      lp3->pt = pt3 = pt4;
      lp3->br = pt3->br;
      lp3->nms = 0;
      // printf("  new loop point at %s \n", pt4->tag );
      if (pt4 != pt1) lp3->ms=ms3=nextMeasure(lp3,ms3);
    }
  }

  ms1->pt1=pt1;
  ms1->pt2=pt2;
  lp3->ms=ms1;

  NEW_HM(hm1);
  hm1->next=hm00;
  hm00=hm1;
  hm1->lp=lp0;

  forward = 1;
  x=0.0;
  for (lp3=lp0; lp3!=NULL; lp3=lp3->next) {
    x += lp3->ms->dist;
    if ( strcmp( lp3->ms->tag1, lp3->pt->tag) == 0 ) {
      lp3->dir = 1;
    } else {
      lp3->dir = -1;
    }
  }
  for (lp3=lp0; lp3!=NULL; lp3=lp3->next) {
    lp3->x = lp3->ms->dist / x;
  }
  /*
  printf(" Homotopy: ");
  for (lp3=lp0; lp3!=NULL; lp3=lp3->next) 
    printf(" %s (%d)", lp3->pt->tag, lp3->dir);
  printf(" Length %6.2f \n",x);
  */
}
/* ================================================================= */
void 
Num::makeBranch( NumPoint * pt, NumMeasure * ms )
{
  ARG_CHECK( pt == NULL, );
  ARG_CHECK( ms == NULL, );

  NumBranch *br2;

  // printf("MakeBranch at %s \n", pt->tag );
  if ( (br2=pt->br) == NULL) {
    NEW_BR(br2);
    br2->next = br00; // insert at head of the list
    br00=br2;
    br2->nms = pt->nms;
    br2->pt  = pt;
    pt->br   = br2;
    br2->ms  = (NumMeasure **)malloc((br2->nms)*sizeof(NumMeasure *));
    (br2->ms)[0]   = pt->ms[0];
    (br2->ms)[1]   = pt->ms[1];
  } else {
    br2->ms  = (NumMeasure **)realloc(br2->ms,(br2->nms+1)*sizeof(NumMeasure *));
  }
  (br2->ms)[br2->nms] = ms;
  ++ br2->nms;
  /*
  { int i;
    printf("Branch (%2d) Pt %s: ",br2->nms,br2->pt->tag);
    for (i=0; i<br2->nms; i++)
        printf(" %s-%s ",((br2->ms)[i])->tag1,((br2->ms)[i])->tag2);
    printf("\n"); 
  }
  */
}            
/* ================================================================= */
NumPoint * 
Num::checkPoint( const char * tag )
{ 
  ARG_CHECK( tag == NULL, NULL );

  NumPoint *pt1;
  for ( pt1=pt00; pt1!=NULL; pt1=pt1->next) {
    if ( strcmp(pt1->tag,tag) == 0 ) return pt1;
  }
  return NULL;
}
/* ----------------------------------------------------------------- */
void 
Num::resetMeasures()
{ 
  NumMeasure *ms1;
  for (ms1=ms00; ms1!=NULL; ms1=ms1->next) {
    // ms1->next=ms1->nest;
    // ms1->prev=ms1->prew;
    ms1->used = 0;
  }
}
/* ----------------------------------------------------------------- */
/* Crea il punto pt3 a partire dal punto pt1, con misura ms1.
 * Assegna i punti pt1 e pt3 alla misura ms1. 
 */
NumPoint * 
Num::makePoint(NumPoint * ptlast, NumPoint * pt1, NumMeasure *ms1, int sign)
{
  ARG_CHECK( ptlast == NULL, NULL );
  ARG_CHECK( pt1 == NULL, NULL );
  ARG_CHECK( ms1 == NULL, NULL );

  NumPoint *pt2;

  // printf("MakePoint from %s with ms %s %s %.2f %.2f %.2f (sign %d)\n",
  //   pt1->tag, ms1->tag1, ms1->tag2, ms1->dist, ms1->nord, ms1->incl, sign );

  assert( ptlast->next==NULL);
  NEW_PT(pt2);
  pt2->next=NULL;
  ptlast->next=pt2;
  ptlast=pt2;

  pt2->br=NULL;
  pt2->ms[0]=ms1;
  pt2->ms[1]=NULL;
  pt2->nms=1;
  pt2->set=0;

  // add measure to point-1
  if (pt1->nms > 1)  makeBranch(pt1,ms1);
  else               pt1->ms[pt1->nms] = ms1;
  pt1->nms++;

  if (sign==1) { pt2->tag = ms1->tag2;
                 ms1->pt2 = pt2;
                 ms1->pt1 = pt1;
  } else { pt2->tag = ms1->tag1;
           ms1->pt1 = pt2;
           ms1->pt2 = pt1;
  }
  return ptlast;
}
/* ----------------------------------------------------------------- */
int 
Num::makePoints( int i_lp, const char * fix_point )      /* Crea la lista dei punti:       */
{                                  /* pt00 e` il primo punto,        */
  NumPoint * pt1;
  NumPoint * pt2;
  NumPoint * ptlast;           
  NumMeasure *ms0;
  int added = 1;
  int pt_cnt = 1;

  // fprintf(stderr, "makePoints() ms00 %p fix %s\n", ms00, fix_point );
  if ( ms00 == NULL && fix_point == NULL ) {
    return -1;
  }

  NEW_PT(pt00);
  pt00->V = /* pt00->H = */ pt00->N = pt00->E = 0.0;

  pt00->next= NULL;
  if ( fix_point == NULL ) {
    pt00->tag = ms00->tag1;
    ms00->pt1= pt00;
  } else {
    pt00->tag = fix_point;
  }
  pt00->br  = NULL;
  pt00->nms = 0;
  pt00->ms[0] = NULL;
  pt00->ms[1] = NULL;
  pt00->set = 1;
  ptlast = pt00;

  added = 1;
  resetMeasures();
  while (added == 1) {
    // printf("MakePoints pts %d lp %d ms %d \n", pt_cnt, i_lp, measure_number );
    added = 0;
    for (ms0 = ms00; ms0!=NULL; ms0=ms0->next ) {
      if ( ms0->used == 1) continue;
      pt1 = checkPoint(ms0->tag1); // point with tag ms0->tag1
      pt2 = checkPoint(ms0->tag2);
      if (pt1 != NULL && pt2 != NULL ) {
        // points pt1 and pt2 already inserted ==> make a loop
        if (i_lp) makeLoop(pt2,pt1,ms0);
        else      ptlast = makePoint(ptlast,pt1,ms0,1);
        ms0->used = 1;
      } else if (pt1 != NULL && pt2 == NULL ) {
        ptlast = makePoint(ptlast,pt1,ms0,1);
        ms0->used = 1;
        added = 1;
        ++ pt_cnt;
      } else if (pt1 == NULL && pt2 != NULL ) {
        ptlast = makePoint(ptlast,pt2,ms0,-1);
        ms0->used = 1;
        added = 1;
        ++ pt_cnt;
      } else {
        /* nothing */
      }
    }
  } 
  // fprintf(stderr, "makePoints() points %d \n", pt_cnt );
  // resetMeasures();
  /* FIXME bug in MakeHomotopy() or used routines
   */
  if (i_lp) makeHomotopy();

  stats.n_stations = pt_cnt; // STATISTICS
  return pt_cnt;
}
/* ================================================================= */
void 
Num::setPoint(NumPoint * pt3, NumPoint * pt1, NumMeasure * ms1, int sign)
{
  ARG_CHECK( pt3 == NULL, );
  ARG_CHECK( pt1 == NULL, );
  ARG_CHECK( ms1 == NULL, );

  double H= ms1->dist * COSD(ms1->incl);

  // printf("NumSetPoint %s from %s (%s %s) N %.2f E %.2f V %.2f (sign %d)\n",
  //   pt3->tag, pt1->tag, ms1->tag1, ms1->tag2, pt1->N, pt1->E, pt1->V, sign );

  pt3->V = pt1->V + sign * ms1->dist * SIND(ms1->incl);
  // pt3->H = pt1->H + ms1->horz * H;
  pt3->N = pt1->N + sign * H * COSD(ms1->nord);
  pt3->E = pt1->E + sign * H * SIND(ms1->nord);
  pt3->set = 1;
  // printf("  M %.2f %.2f %.2f N  %.2f E %.2f V %.2f\n",
  //  ms1->dist, ms1->nord, ms1->incl, pt3->N, pt3->E, pt3->V );
  if ( stats.z_min > pt3->V ) stats.z_min = pt3->V; // STATISTICS
  if ( stats.z_max < pt3->V ) stats.z_max = pt3->V;
  if ( stats.n_min > pt3->N ) stats.n_min = pt3->N;
  if ( stats.n_max < pt3->N ) stats.n_max = pt3->N;
  if ( stats.e_min > pt3->E ) stats.e_min = pt3->E;
  if ( stats.e_max < pt3->E ) stats.e_max = pt3->E;
}
/* ----------------------------------------------------------------- */
#if 0
void 
Num::rescalePoints()
{ 
  NumPoint *pt1;

  for (pt1=pt00; pt1!=NULL; pt1=pt1->next) {
      pt1->V /= scala;    pt1->H /= scala;
      pt1->N /= scala;    pt1->E /= scala;
  }
}
#endif
/* ----------------------------------------------------------------- */
void 
Num::resetPoints()
{ 
  NumPoint *pt1;
  for (pt1=pt00->next; pt1!=NULL; pt1=pt1->next) pt1->set=0;
  pt00->set = 1;
  stats.z_min = stats.z_max = 0; // STATISTICS
  stats.n_min = stats.n_max = 0;
  stats.e_min = stats.e_max = 0;
}
/* ----------------------------------------------------------------- */
void 
Num::setPoints()
{ 
  // NumMeasure *ms0;
  NumMeasure *ms1;
  NumPoint   *pt1, *pt2;
  int added = 1;

  resetPoints();
  while ( added == 1 ) {
    added = 0;
    for ( ms1=ms00; ms1!=NULL; ms1=ms1->next ) {
      pt1 = ms1->pt1;
      pt2 = ms1->pt2;
      if ( pt1 == NULL || pt2 == NULL ) 
        continue;
      if ( (pt1->set) && !(pt2->set) ) {
        setPoint(pt2,pt1,ms1,1);
        added = 1;
      } else if ( !(pt1->set) && (pt2->set) ) {
        setPoint(pt1,pt2,ms1,-1);
        added = 1;
      } else if ( (pt1->set) && (pt2->set) ) {
        // this is a loop
        // break;
      } else {
        /* nothing */
      }
    }
  }
  // printf(" Now RESET MEASURES \n");

  // ResetMeasures();
  // RescalePoints();
  // FIND_BBOX();
}
/* ----------------------------------------------------------------- */
void 
Num::printPoint( NumPoint * pt )
{
  ARG_CHECK( pt == NULL, );
  printf("%s: N %.2f E %.2f V %.2f \n", pt->tag, pt->N, pt->E, pt->V );
}
/* ----------------------------------------------------------------- */
void 
Num::printPoints()
{
  fprintf(stderr, "printPoints() pt00 %p \n", pt00 );
  NumPoint *pt1;
  for (pt1=pt00; pt1!=NULL; pt1=pt1->next) {
    printPoint(pt1);
  }
}
/* ----------------------------------------------------------------- */
const NumPoint * 
Num::getPoint( const char * name ) const
{
  ARG_CHECK( name == NULL, NULL );

  NumPoint * pt = pt00;
  for ( ; pt != NULL; pt=pt->next) {
    if ( strcmp( pt->tag, name ) == 0 ) break;
  }
  // fprintf(stderr, "getPoint( %s ) %p \n", name, pt );
  return pt;
}
/* ----------------------------------------------------------------- */
void 
Num::addMeasure( const char * from, const char * to, 
                 double dist, double nord, double incl )
{
  ARG_CHECK( from == NULL, );
  ARG_CHECK( to == NULL, );

  // skip shots with Dist=0 and identical stations
  if ( strcmp(from, to) == 0 && dist < 0.00001 ) return;

  NumMeasure * ms1;
  measure_number ++;
  stats.n_shots ++;  // STATISTICS

  // printf("Add measure %d: %s %s \n", measure_number, from, to );
  NEW_MS(ms1);
  ms1->tag1 = from;
  ms1->tag2 = to;
  ms1->pt1 =NULL;   ms1->pt2 =NULL;
  ms1->horz=1;      ms1->dist=dist;
  ms1->incl=incl;   ms1->nord=nord;
  ms1->next=NULL;   // ms1->nest=NULL;
  // svil += dist;
  if (ms00==NULL) {
    ms00=ms1;
    ms1->prev=NULL;   // ms1->prew=NULL;
  }
  else {
    ms20->next=ms1;    // ms20->nest=ms1;
    ms1->prev=ms20;    // ms1->prew=ms20;
  }
  ms20=ms1;
}

