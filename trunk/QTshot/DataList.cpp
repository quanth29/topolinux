/** @file DataList.cpp
 *
 * @author marco corvi
 * @date apr 2009
 *
 * @brief topolinux measurements data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include <string.h>
#include <math.h>

#include <sstream>
#include <map>
#include <string>

#include "ArgCheck.h"
#include "shorthands.h"

#include "Locale.h"
#include "GetDate.h"
#include "SplayAt.h"

#include "DataList.h"

#include "Flags.h"
#include "Extend.h"
#ifdef HAS_POCKETTOPO
  #include "PTfile.h"
#endif
#include "PlotScale.h"

#ifndef M_PI
  #define M_PI 3.1415926536
#endif


#ifdef WIN32
  #define strncasecmp strncmp
#endif

void 
DataList::clear()
{
  DBlock * next;
  while ( head ) {
    next = head->next_block;
    delete head;
    head = next;
  }
  list_size = 0;
  shot = 0;
  need_num   = false;
  base_block = NULL;
}

bool 
DataList::saveTlx( CenterlineInfo & c_info )
{
  FILE * fp = fopen( c_info.fileName.TO_CHAR(), "w" );
  if ( fp == NULL ) {
    DBG_CHECK("Failed to open file \"%s\"\n", c_info.fileName.TO_CHAR() );
    return false;
  }
  fprintf(fp, "tlx2\n"); // TopoLinux file format version 2

  fprintf(fp, "# date %04d.%02d.%02d \n", c_info.year, c_info.month, c_info.day );
  if ( c_info.description.size() > 0 ) {
    char * tmp = new char[ c_info.description.size()+1];
    memcpy( tmp, c_info.description.c_str(), c_info.description.size() );
    tmp[c_info.description.size()] = 0;
    char * ch  = tmp;
    char * ch1 = ch;
    while ( *ch ) {
      while ( *ch && *ch != '\n' ) ++ch;
      if ( *ch == '\n' ) {
        *ch = 0;
        fprintf(fp, "# %s\n", ch1 );
        *ch = '\n';
        ++ch;
      } else {
        fprintf(fp, "# %s\n", ch1 );
        break;
      }
      ch1 = ch;
    }
    delete[] tmp;
  }

  DBlock * b = head;
  while ( b ) {
    fprintf(fp, "\"%s\" \"%s\" %.2f %.2f %.2f %.2f %d %d %d\n",
      b->fromStation(), b->toStation(),
      b->distance, b->compass, b->clino, b->roll, 
      b->extend, b->flag, b->count );
    if ( b->count > 1 ) {
      DBlocklet * blk =b->blocklet;
      for (int k=0; k<b->count; ++k ) {
        if ( blk == NULL ) {
          break;
        }
        fprintf(fp, "@ %.2f %.2f %.2f %.2f\n", blk->distance, blk->compass, blk->clino, blk->roll);
        blk = blk->next_blocklet;
      }

      #ifdef HAS_LRUD
        if ( b->lrud_from ) {
          fprintf(fp, "#F %.2f %.2f %.2f %.2f\n", 
            b->lrud_from->left, b->lrud_from->right,
            b->lrud_from->up, b->lrud_from->down );
        }
        if ( b->lrud_to ) {
          fprintf(fp, "#T %.2f %.2f %.2f %.2f\n",
            b->lrud_to->left, b->lrud_to->right,
            b->lrud_to->up, b->lrud_to->down );
        }
      #endif

      if ( b->comment.size() > 0 ) {
        fprintf(fp, "# %s\n", b->comment.c_str() );
      }
    }
    b = b->next_block;
  }
  fclose( fp );
  return true;
}

DBlock * 
DataList::getBlock( size_t pos ) 
{
  DBlock * b = head;
  while ( b && pos > 0 ) { b=b->next_block; --pos; }
  return b;
}

void 
DataList::insertBlock( DBlock * blk, double d, double b, double c, double r, bool before )
{
  ARG_CHECK( blk == NULL, );

  DBlock * b1 = new DBlock( NULL, NULL, d, b, c, r, 0, 0, 0 );
  ++list_size;
  need_num = true;

  if ( head == NULL ) {
    head = b1;
    return;
  }
  if ( before ) {
    if ( head == blk ) { // b1 --> head --> head_next
      b1->next_block = head;
      head = b1;
    } else {
      DBlock * b0 = head;
      while ( b0->next_block && b0->next_block != blk ) b0 = b0->next_block;
      b1->next_block = b0->next_block;
      b0->next_block = b1;
    }
  } else { // after
    DBlock * b0 = head;
    while ( b0->next_block && b0 != blk ) b0 = b0->next_block;
    // N.B. if b0->next_block == NULL then b0 is the last Block
    b1->next_block = b0->next_block;
    b0->next_block = b1;
  }
}

void
DataList::computeAverage( double * d0, double * b0, double * c0, double * r0, int cnt,
                          double & dave, double & bave, double & cave, double & rave )
{
  ARG_CHECK( d0 == NULL,  );
  ARG_CHECK( b0 == NULL,  );
  ARG_CHECK( c0 == NULL,  );
  ARG_CHECK( r0 == NULL,  );

  dave = d0[0];
  bave = b0[0];
  cave = c0[0];
  rave = r0[0];
  if ( cnt > 1 ) {
    for ( int k=1; k<cnt; ++k) {
      dave += d0[k];
      cave += c0[k];
      if ( bave/k < 90 && b0[k] > 270 ) {
        bave += (b0[k] - 360.0);
      } else if ( bave/k > 270 && b0[k] < 90 ) {
        bave += (b0[k] + 360.0);
      } else {
        bave += b0[k];
      }
      rave += r0[k];
    }
    dave /= cnt;
    bave /= cnt;
    cave /= cnt;
    rave /= cnt;
    if ( bave < 0.0 ) bave += 360.0;
    else if ( bave > 360.0 ) bave -= 360.0;
    if ( rave < 0.0 ) rave += 360.0;
    else if ( rave > 360.0 ) rave -= 360.0;
  }
}

// called by createBlock()
DBlock *
DataList::shotToBlock( int from, int to, 
       double * d0, double * b0, double * c0, double * r0, int cnt )
{
  ARG_CHECK( cnt <= 0, NULL );
  ARG_CHECK( d0 == NULL, NULL );
  ARG_CHECK( b0 == NULL, NULL );
  ARG_CHECK( c0 == NULL, NULL );
  ARG_CHECK( r0 == NULL, NULL );

  char sfrom[64];
  char sto[64];
  if ( cnt == 1 ) {
    sprintf( sfrom, "%d", from);
    sto[0] = 0;
    return new DBlock( sfrom, sto, d0[0], b0[0], c0[0], r0[0], 0, 0, 1 );
  } else {
    double da, ba, ca, ra;
    computeAverage( d0, b0, c0, r0, cnt, da, ba, ca, ra );
    sprintf( sfrom, "%d", from);
    sprintf( sto, "%d", to);
    DBlock * b = new DBlock( sfrom, sto, da, ba, ca, ra, 0, 0, cnt );
    for (int k=0; k<cnt; ++k ) {
      DBlocklet * blket = new DBlocklet( d0[k], b0[k], c0[k], r0[k] );
      b->addBlocklet( blket );
    }
    return b;
  }
  return NULL;
}

bool 
DataList::shotIsClose( double d1, double b1, double c1, double d2, double b2, double c2 )
{
  double thr = 2.0;
  double thr2 = 2 * thr;
  if ( fabs( c1 - c2 ) > thr ) return false;
  if ( fabs( d1 - d2 ) > thr*d1/60.0 ) return false;
  if ( b1 < thr2 && b2 > 360.0 - thr2 ) {
    if ( fabs( b1 - b2 + 360.0 ) > thr ) return false;
  } else if ( b1 > 360.0 - thr2 && b2 < thr2 ) {
    if ( fabs( b2 - b1 + 360.0 ) > thr ) return false;
  } else {
    if ( fabs( b1 - b2 ) > thr ) return false;
  }
  return true;
}

void
DataList::createBlock( int from, int to, 
		       double * d0, double * b0, double * c0, double * r0, 
		       int cnt,
		       int splay_at, bool backward,
		       DBlock ** last, DBlock ** start )
{
  ARG_CHECK( d0 == NULL,  );
  ARG_CHECK( b0 == NULL,  );
  ARG_CHECK( c0 == NULL,  );
  ARG_CHECK( r0 == NULL,  );
  ARG_CHECK( cnt < 0,  );

  DBlock * bb;
  if ( cnt == 0 ) return;
  if ( cnt > 1 ) {
    if ( backward ) {
      bb = shotToBlock( to, from, d0, b0, c0, r0, cnt );
    } else {
      bb = shotToBlock( from, to, d0, b0, c0, r0, cnt );
    }
  } else {
    if ( splay_at == SPLAY_AT_TO ) {
      bb = shotToBlock( to, from, d0, b0, c0, r0, cnt );
    } else {
      bb = shotToBlock( from, to, d0, b0, c0, r0, cnt );
    }
  }
  insertBlock( bb, last, start );
  // need_num = true; // done by insertBlock()
}

DBlock * 
DataList::initFromTo( int & from, int & to, bool append )
{
  DBlock * last = NULL;
  from = 0;
  to   = 1;
  if ( append ) {
    DBlock * last = head;
    if ( last ) {
      while ( last->next_block ) {
	last = last->next_block;
	// initialize "from" and "to"
      }
      if ( last->hasFromStation() ) {
	from = atoi( last->fromStation() );
	if ( last->hasToStation() ) to = atoi( last->toStation() );
	else to = from+1;
      }
    }
  } else {
    clear();
  }

  if ( to < from ) {
    int tmp = from; from = to; to = tmp;
  } 

  return last;
}


bool
DataList::loadDisto( DistoX & disto, bool append, bool smart, int splay_at, bool backward )
{
  if ( disto.measurementSize() == 0 ) {
    return false;
  }
  // DBlock * bb = NULL;      // temporary block pointer
  DBlock * start = NULL;  // first block inserted
  int from, to;
  DBlock * last = initFromTo( from, to, append );

  // NOTE Max 20 measures for a shot
  /* static */ double d0[20], b0[20], c0[20], r0[20];
  /* static */ int cnt = 0;
  unsigned int xd, xb, xc, xr;
  double d, b, c, r;
  while ( disto.nextMeasurement( xd, xb, xc, xr, d, b, c, r ) ) {
    double dave, bave, cave, rave;
    computeAverage( d0, b0, c0, r0, cnt, dave, bave, cave, rave );
    if ( cnt > 0 ) {
      // if ( cnt > 1 || !smart ) { // 2011.02.13 increment only when insert block
      //   ++ from;
      //   ++ to;
      // }
      if ( ! smart || ! shotIsClose( dave, bave, cave, d, b, c ) ) {
	// if (d,b,c) is not close to the average
	// write out the array of previous shots
	// and restart counting from 0
        if ( cnt > 1 || !smart ) {
          ++ from;
          ++ to;
        }
	createBlock( from, to, d0, b0, c0, r0, cnt, splay_at, backward, &last, &start );
	cnt = 0;
      }
    }
    if ( cnt < 20 ) {
      d0[cnt] = d;
      b0[cnt] = b;
      c0[cnt] = c;
      r0[cnt] = r;
      ++ cnt;  // number of shots in the set
    }
  }
  if ( cnt > 0 ) {
    if ( cnt > 1 || !smart ) { // 2011.02.13
      ++ from;
      ++ to;
    }
    createBlock( from, to, d0, b0, c0, r0, cnt, splay_at, backward, &last, &start );
  }
  // need_num = true; // done by createBlock() --> insertBlock()
  return true;
}


bool 
DataList::loadRaw( FILE * in, bool append, bool smart, int splay_at, bool backward )
{
  ARG_CHECK( in == NULL, false );

  DBG_CHECK("loadRaw() append data: %s\n", append ? "yes" : "no");

  // DBlock * bb = NULL;      // temporary block pointer
  DBlock * start = NULL;  // first block inserted
  int from, to;
  DBlock * last = initFromTo( from, to, append );

  char line[128];
  double d0[20], b0[20], c0[20], r0[20];
  int cnt = 0;
  while ( fgets( line, 128, in ) != NULL ) {
    unsigned int xd, xb, xc, xr;
    double d, b, c, r;
    double dave, bave, cave, rave;
    // first try to read 8 values (raw-format 2.0)
    // if it fails try to read six values (raw-format 1.0)
    if ( sscanf( line, "%x %x %x %x %lf %lf %lf %lf", &xd, &xb, &xc, &xr, &d, &b, &c, &r ) != 8 ) {
      xr = 0;
      r = 0.0;
      if ( sscanf( line, "%x %x %x %lf %lf %lf", &xd, &xb, &xc, &d, &b, &c ) != 6 ) {
	break;
      }
    }

    computeAverage( d0, b0, c0, r0, cnt, dave, bave, cave, rave );
    if ( cnt > 0 ) {
      // if ( cnt > 1 || !smart ) {
      //   from = to;
      //   ++ to;
      // }
      if ( ! smart || ! shotIsClose( dave, bave, cave, d, b, c ) ) {
        if ( cnt > 1 || !smart ) { // 2011.02.13
          from = to;
          ++ to;
        }
	// if (d,b,c) is not close to the average
	// write out the array of previous shots
	// and restart counting from 0
	createBlock( from, to, d0, b0, c0, r0, cnt, splay_at, backward, &last, &start );
	cnt = 0;
      }
    }
    if ( cnt < 20 ) {
      d0[cnt] = d;
      b0[cnt] = b;
      c0[cnt] = c;
      r0[cnt] = r;
      ++ cnt;  // number of shots in the set
    }
  }
  fclose( in );

  if ( cnt > 0 ) {
    if ( cnt > 1 || !smart ) { // 2011.02.13
      from = to;
      ++ to;
    }
    createBlock( from, to, d0, b0, c0, r0, cnt, splay_at, backward, &last, &start );
  }
  // need_num = true; // done by createBlock() --> insertBlock()
  return true;
}

int
DataList::loadFile( PlotDrawer * drawer, const char * filename, bool append, CenterlineInfo * info )
{
  ARG_CHECK( drawer == NULL, 1 );
  ARG_CHECK( filename == NULL, 1 );
  ARG_CHECK( info == NULL, 1 );

  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    DBG_CHECK("loadFile Failed to open file \"%s\"\n", filename );
    return 1;
  } 
  char ch[5];
  // read four bytes and try to recognize a supported file format:
  //   - raw data (v 1 and v 2): first byte is '0' (ascii 48)
  //   - topolinux v.1: first byte is '#'
  //   - topolinux v.2: 't' 'l' 'x' '2'
  //   - PocketTopo v.3: 'T' 'o' 'p' 3
  //
  if ( fread( ch, 1, 4, fp) == 4 ) {
    ch[4] = 0;
    rewind( fp );
    if ( ch[0] == '0' ) { // raw data file (version 1.0 or 2.0)
      return loadRaw( fp, append, true, 1, false )? 0 : 2;
    } else if ( ch[0] == '"' || ch[0] == '#' ) { // topolinux data file v. 1
      return loadTlx( fp, append, info, 1 )? 0 : 3;
    } else if ( strncmp( ch, "tlx2", 4 ) == 0 ) { // topolinux v. 2
      return loadTlx( fp, append, info, 2 )? 0 : 3;
#ifdef HAS_POCKETTOPO
    } else if ( strncmp( ch, "Top", 3 ) == 0 && ch[3] == 3 ) { // pockettopo v. 3
      return loadPocketTopo( drawer, fp, info )? 0 : 4;
#endif
    } else {
      DBG_CHECK("loadFile unrecognized data file: start with %x %x %x %x\n", ch[0], ch[1], ch[2], ch[3]);
    }
  }
  fclose( fp );
  return 4;
}

/** TopoLinux format
 *
 * - tlx2     header line "tlx2" (only version 2)
 * - # optional initial comment(s): date, name, etc.
 * - " start a data line:
 *       "from" "to" dist azimuth clino ext flag nr (v. 1)
 *       "from" "to" dist azimuth clino roll ext flag nr (v. 2)
 *                   length [m accuracy cm]
 *                   angles [deg. accuracy 1/100 deg]
 *                   "to" is "" if there is no TO station
 *                   ext: extend flag
 *                   flag: shot flag
 *                   nr: number of shot in the data. If >1 the line is followed by the shotlet lines
 * - @ dist azimuth clino
 * - # optional shot comment may follow:
 *        #F left right up down   LRUD at station FROM
 *        #T left right up down   LRUD at station TO
 *        #  shot comment
 *
 * additional empty lines and lines begiinning with # are skipped.
 */
bool
DataList::loadTlx( FILE * fp, bool append, CenterlineInfo * info, int version )
{
  ARG_CHECK( fp == NULL, false );
  ARG_CHECK( info == NULL, false );

  DBG_CHECK("loadTlx() v.%d: append: %s\n", version, append ? "yes" : "no");

  DBlock * b = NULL;      // temporary block pointer
  DBlock * start = NULL;  // first block inserted
  DBlock * last = NULL;   // last block on the list

  // if ( append ) {
  //   last = head;
  //   if ( last ) while ( last->next_block ) last = last->next_block;
  // } else {
    clear();
  // }

  char line[256];
  char from[64];
  char to[64];
  double dist, comp, clino;
  double roll=0.0; // TopoLinux v. 1 does not store roll
  int ext, flg, cnt;
  int line_nr = 0;
  std::ostringstream comment;

  if ( version > 1 ) {      // skip version header line
    if ( fgets( line, 256, fp ) == NULL ) {
      fclose( fp );
      return false;
    }
  }

  while ( fgets( line, 256, fp ) ) {
    if ( line[0] == '#' && b == NULL ) { // skip initial comments
      comment << line+1;
      continue;
    }
    ++ line_nr;
    int len = strlen( line );
    while ( len > 0 && isspace( line[len-1] ) ) {
      line[len-1] = 0;
      --len;
    }
    if ( len == 0 ) continue; // skip empty lines
    if (line[0] != '"') {
      if ( b != NULL ) {
	if ( line[0] == '#' ) { // LRUD or data comment
	  #ifdef HAS_LRUD
	    if ( line[1] == 'F' ) {
	      double l, r, u, d;
	      if ( sscanf(line+3, "%lf %lf %lf %lf", &l, &r, &u, &d ) != 4 ) {
		break;
	      }
	      b->lrud_from = new LRUD(l,r,u,d);
	    } else if ( line[1] == 'T' ) {
	      double l, r, u, d;
	      if ( sscanf(line+3, "%lf %lf %lf %lf", &l, &r, &u, &d ) != 4 ) {
		break;
	      }
	      b->lrud_to = new LRUD(l,r,u,d);
	    } else {
	      b->setComment( line+2 );
	    }
	  #else
	    b->setComment( line+2 );
	  #endif
	} else if ( line[0] == '@' ) { // data blocklet
	  bool ok = false;
	  if ( version == 1 ) {
	    ok = sscanf(line+2, "%lf %lf %lf", &dist, &comp, &clino ) == 3;
	    roll = 0.0;
	  } else if ( version == 2 ) {
	    ok = sscanf(line+2, "%lf %lf %lf %lf", &dist, &comp, &clino, &roll ) == 4;
	  }
	  if ( ! ok ) {
	    break;
	  }
	  DBlocklet * blket = new DBlocklet( dist, comp, clino, roll );
	  b->addBlocklet( blket );
	} else {
	  DBG_CHECK("ERROR: unexpected line %d. Please report it with your data.\n", line_nr);
	  DBG_CHECK("LINE: %s", line);
	}
      }
      continue;
    }
    int k=0;
    char * ch = line+1;
    while ( ch[0] != '"' || ch[1] != ' ' ) {
      from[k] = *ch;
      ++k;
      ++ch;
    }
    from[k] = 0;
    ch += 3;
    k = 0;
    while ( ch[0] != '"' || ch[1] != ' ' ) {
      to[k] = *ch;
      ++k;
      ++ch;
    }
    to[k] = 0;
    ch += 2;
    bool ok = false;
    if ( version == 1 ) {
      ok = sscanf(ch, "%lf %lf %lf %d %d %d", &dist, &comp, &clino, &ext, &flg, &cnt ) == 6;
      roll = 0.0;
    } else if ( version == 2 ) {
      ok = sscanf(ch, "%lf %lf %lf %lf %d %d %d", &dist, &comp, &clino, &roll, &ext, &flg, &cnt ) == 7;
    }
    if ( ! ok ) {
      DBG_CHECK("WARNING: wrong line %d \n", line_nr );
      DBG_CHECK("LINE: %s\n", line );
      break;
    }
    // DBG_CHECK("READ %2s %2s %8.2f %8.2f %8.2f %d %d %d\n", 
    //   from, to, dist, comp, clino, ext, flg, cnt );
    b = new DBlock( from, to, dist, comp, clino, roll, ext, flg, cnt );
    insertBlock( b, &last, &start );
    // need_num = true; // done by insertBlock()
  }
  fclose( fp );

  // centerline info
  if ( comment.str().size() > 0 ) {
    // DBG_CHECK("COMMENT:\n%s", comment.str().c_str() );
    if ( info != NULL && ! append ) {
      char * tmp = new char[ comment.str().size() + 1 ];
      memcpy( tmp, comment.str().c_str(), comment.str().size() );
      tmp[ comment.str().size() ] = 0;
      std::ostringstream oss;
      char * ch = tmp;
      while ( isspace(*ch) ) ch++;
      if ( strncmp(ch, "date", 4) == 0 ) {
	Locale::FromDate( ch+5, info->year, info->month, info->day );
	// ssize_t r = sscanf(ch+5, "%d %d %d", &(info->year), &(info->month), &(info->day) );
	// r = r;
	// DBG_CHECK("Date: %d %d %d\n", info->year, info->month, info->day);
      }
      while ( ch && *ch != 0 ) {
	while ( *ch != '\n' && *ch != 0 ) ++ch;
	if ( *ch == 0 ) break;
	ch ++;
	if ( *ch == 0 ) break;
	ch ++;
	if ( *ch == 0 ) break;
	char * ch1 = ch;
	while ( *ch != '\n' && *ch != 0 ) ++ch;
	char ch2 = *ch;
	*ch = 0;
	// DBG_CHECK("%s\n", ch1 );
	oss << ch1 << " "; // replace '\n' by ' ' (space) 
	*ch = ch2;
      }
      info->description = oss.str();
      delete[] tmp;
    }
  }

  recomputeMultimeasureBlocks( start );

  // DBG_CHECK("data load size %d \n", list_size );
  return true;
}

bool
DataList::loadPocketTopo( PlotDrawer * drawer, FILE * fp, CenterlineInfo * info )
{
  ARG_CHECK( drawer == NULL, false );
  ARG_CHECK( fp == NULL, false );
  ARG_CHECK( info == NULL, false );

#ifdef HAS_POCKETTOPO 
  clear(); // erase everything
  #include "../PTopo/PTimport.impl"
#endif
  return true;
}

void
DataList::recomputeMultimeasureBlocks( DBlock * start )
{
  ARG_CHECK( start == NULL, );

  for ( DBlock * b = start; b; b=b->next_block ) {
    if ( b->count > 1 ) {
      DBlocklet * bket = b->blocklet;
      if ( bket == NULL ) {
	b->count = 1;
	continue;
      }
      double d0 = bket->distance;
      double b0 = bket->compass;
      double c0 = bket->clino;
      double r0 = bket->roll;
      bket = bket->next_blocklet;
      b->count = 1;
      while ( bket ) {
	d0 += bket->distance;
	c0 += bket->clino;

	if ( bket->compass > 270 && (b0 / b->count) < 90 ) {
	  b0 += ( bket->compass - 360.0 );
	} else if ( bket->compass < 90 && (b0 / b->count) > 270 ) {
	  b0 += ( bket->compass + 360.0 );
	} else {
	  b0 += bket->compass;
	}

	if ( bket->roll > 270 && (r0 / b->count) < 90 ) {
	  r0 += ( bket->roll - 360.0 );
	} else if ( bket->roll < 90 && (r0 / b->count) > 270 ) {
	  r0 += ( bket->roll + 360.0 );
	} else {
	  r0 += bket->roll;
	}

	b->count ++;
	bket = bket->next_blocklet;
      }
      b->distance = d0 / b->count;
      b->compass  = b0 / b->count;
      b->clino    = c0 / b->count;
      b->roll     = r0 / b->count;
    }
  }
}

  /** 
   * @param r  row index (block index)
   * @param c  column index (0: From, 1: To, 2-3-4 illegal, 5: extend, 6: flag, 7: comment
   * @param txt text
   */
void 
DataList::updateBlock( int r, int c, const char * txt )
{
  ARG_CHECK( txt == NULL, );

  DBlock * b = head;
  while ( b && r > 0 ) { --r; b = b->next_block; }
  if ( b ) {
    if ( c == 0 ) { // From
      b->setFromStation( txt );
      need_num = true;
    } else if ( c == 1 ) { // To
      b->setToStation( txt );
      need_num = true;
    } else if ( c == 5 ) { // ext FIXME LANGUAGE
      if ( strncasecmp( txt, "L", 1 ) == 0 ) {
	b->extend = EXTEND_LEFT;
      } else if ( strncasecmp( txt, "R", 1 ) == 0 ) {
	b->extend = EXTEND_RIGHT;
      } else if ( strncasecmp( txt, "V", 1 ) == 0 ) {
	b->extend = EXTEND_VERT;
      } else if ( strncasecmp( txt, "I", 1 ) == 0 ) {
	b->extend = EXTEND_IGNORE;
      } else {
	b->extend = EXTEND_NONE;
      }
      need_num = true;
    } else if ( c == 6 ) { // flg FIXME Language
      if ( strncasecmp( txt, "S", 1 ) == 0 ) {
	b->flag = FLAG_SURFACE;
      } else if ( strncasecmp( txt, "D", 1 ) == 0 ) {
	b->flag = FLAG_DUPLICATE;
      } else {
	b->flag = FLAG_NONE;
      }
    } else if ( c == 7 ) { // comment
      b->setComment( txt );
    }
  }
}

#if 1
void 
DataList::evalSplayExtended( DBlock * b )
{
  ARG_CHECK( b == NULL, );

  if ( ! b->isSplay() ) 
    return; // TODO
  if ( b->hasNoStation() )
    return;
  
  if ( b->Extend() != EXTEND_NONE ) {
    b->extended = b->Extend();
  } else {
    // pick extend by the centerline shots
    const std::string & name = ( b->hasFromStation() )? b->fromStation() : b->toStation();
    // [1] get "average" left and right directions
    double left  = -1;
    double right = -1;
    for ( DBlock * b1 = head; b1; b1=b1->next_block ) {
      if ( b1->isSplay() ) continue;
      if ( b1->hasFromStation( name ) ) {
	if ( b1->Extended() == EXTEND_RIGHT ) {
	  if ( right < 0 ) {
	    right =  b1->Compass();
	  } else if ( fabs(right - b1->Compass() ) < 180 ) {
	    right = (right + b1->Compass())/2.0;
	} else {
	    right = ( right + b1->Compass() + 360 )/2.0;
	    if ( right > 360 ) right -= 360.0;
	  }
	} else if ( b1->Extended() == EXTEND_LEFT ) {
	  if ( left < 0 ) {
	    left =  b1->Compass();
	} else if ( fabs(left - b1->Compass() ) < 180 ) {
	    left = (left + b1->Compass())/2.0;
	} else {
	    left = ( left + b1->Compass() + 360 )/2.0;
	    if ( left > 360 ) left -= 360.0;
	  }
	}
      } else if ( b1->hasToStation( name ) ) {
	if ( b1->Extended() == EXTEND_LEFT ) {
	  if ( left < 0 ) {
	    left = b1->Compass();
	  } else if ( fabs(left - b1->Compass() ) < 180 ) {
	    left = (left + b1->Compass())/2.0;
	  } else {
	    left = ( left + b1->Compass() + 360 )/2.0;
	    if ( left > 360 ) left -= 360.0;
	  }
	} else if ( b1->extended == EXTEND_RIGHT ) {
	  if ( right < 0 ) {
	    right = b1->Compass();
	  } else if ( fabs(right - b1->Compass() ) < 180 ) {
	    right = (right + b1->Compass())/2.0;
	  } else {
	    right = ( right + b1->Compass() + 360 )/2.0;
	    if ( right > 360 ) right -= 360.0;
	  }
	}
      }
    }
    // [2] choose extend
    if ( right >= 0 && left >= 0 ) {
      double dr = fabs( b->Compass() - right );
      double dl = fabs( b->Compass() - left );
      if ( dr > 180 ) dr -= 180;
      if ( dl > 180 ) dl -= 180;
      if ( dr < dl/2 ) {
	b->extended = EXTEND_RIGHT;
      } else if ( dl < dr/2 ) {
	b->extended = EXTEND_LEFT;
      } else {
	b->extended = EXTEND_VERT;
      }
    } else if ( right < 0 ) {
      double dl = fabs( b->compass - left );
      if ( dl > 180 ) dl -= 180;
      if ( dl < 45 ) {
	b->extended = EXTEND_LEFT;
      } else if ( dl > 135 ) {
	b->extended = EXTEND_RIGHT;
      } else {
	b->extended = EXTEND_VERT;
      }
    } else if ( left < 0 ) {
      double dr = fabs( b->compass - right );
      if ( dr > 180 ) dr -= 180;
      if ( dr < 45 ) {
	b->extended = EXTEND_RIGHT;
      } else if ( dr > 135 ) {
	b->extended = EXTEND_LEFT;
      } else {
	b->extended = EXTEND_VERT;
      }
    }
  }
}
#endif


void 
DataList::dump()
{
  fprintf(stderr, "DataList %d \n", list_size );
  int k=0;
  for ( DBlock * bl = head; bl; bl=bl->next_block ) {
    fprintf(stderr, "\"%s=%s\" ", bl->fromStation(),  bl->toStation() );
    ++k;
    if ( (k%10) == 0 ) fprintf(stderr, "\n");
  }
  fprintf(stderr, "\n");
}


int
DataList::doNum( bool force )
{
  DBG_CHECK("DoNum() needed %d, n. measures %d\n", need_num, num_measures );

  if ( ! force && ! need_num ) return num_measures;

  num_measures = 0;
  num.clearLists();
  need_num = false;
  for ( DBlock * bl = head; bl; bl=bl->next() ) {
    if ( bl->isSplay() ) continue;
    // DBG_CEHCK("DataList::doNum() add %s %s\n", bl->From(), bl->To() );
    num.addMeasure( bl->fromStation(), bl->toStation(), bl->Tape(), bl->Compass(), bl->Clino() );
    ++ num_measures;
  }
  // DBG_CHECK("DataList::doNum() measures %d\n", n_measures );
  if ( num_measures == 0 ) {
    return 0;
  }
  if ( base_block && base_block->hasFromStation() ) {
    num.makePoints( 1, base_block->fromStation() );
  } else {
    num.makePoints( 1 );
  }
  num.setPoints();
  // DBG_CHECK("DataList::doNum() made points %d \n", n_pts );
  // NumPrintPoints();
  
  DBG_CHECK("DoNum() done: n. measures %d\n", num_measures );

  return num_measures;
}

bool 
DataList::mergeBlock( DBlock * bb )
{
  ARG_CHECK( bb == NULL, false );

  DBlock * bn = bb->next_block;
  if ( bn == NULL ) return false;
  if ( bb->count == 1 ) {
    if ( bn->count == 1 ) {
      ++shot;
    }
    bb->blocklet = new DBlocklet( bb->distance, bb->compass, bb->clino, bb->roll );
  } else {
    if ( bn->count > 1 ) {
      --shot;
    }
  }
  size_t cn = bn->count;
  size_t cb = bb->count;
  size_t ct = cb + cn;
  bb->distance  = ( cb * bb->distance + cn * bn->distance )/ct;
  bb->clino     = ( cb * bb->clino    + cn * bn->clino    )/ct;
  if ( fabs( bb->compass - bn->compass ) < 180 ) {
    bb->compass   = ( cb * bb->compass  + cn * bn->compass  )/ct;
  } else if ( bb->compass > bn->compass ) {
    bb->compass   = ( cb * bb->compass  + cn * ( bn->compass + 360.0 )  )/ct;
    if ( bb->compass >= 360.0 ) bb->compass -= 360.0;
  } else {
    bb->compass   = ( cb * bb->compass  + cn * ( bn->compass - 360.0 )  )/ct;
    if ( bb->compass < 0.0 ) bb->compass += 360.0;
  }
  bb->count = ct;
  DBlocklet * b = bb->blocklet;
  while ( b->next_blocklet ) b=b->next_blocklet;
  if ( cn == 1 ) {
    b->next_blocklet = new DBlocklet( bn->distance, bn->compass, bn->clino, bn->roll );
  } else {
    b->next_blocklet = bn->blocklet;
  }
  bb->next_block = bn->next_block;
  delete bn;
  -- list_size;

  need_num = true;
  return true;
}

bool 
DataList::splitBlock( DBlock * bb )
{
  ARG_CHECK( bb == NULL, false );

  if ( bb->count == 1 ) return false;
  --shot;
  DBlocklet * b = bb->blocklet->next_blocklet;
  DBlock * bn  = bb->next_block;
  DBlock * b1 = bb;
  while ( b ) {
    b1->next_block = new DBlock( "", "", b->distance, b->compass, b->clino, b->roll,
                           bb->extend, bb->flag, 1 );
    b1 = b1->next_block;
    b  = b->next_blocklet;
    ++list_size;
  }
  b1->next_block = bn;
  bb->distance = bb->blocklet->distance;
  bb->compass  = bb->blocklet->compass;
  bb->clino    = bb->blocklet->clino;
  bb->count = 1;
  while ( bb->blocklet ) {
    b = bb->blocklet->next_blocklet;
    delete bb->blocklet;
    bb->blocklet = b;
  }
  need_num = true;
  return true;
}

