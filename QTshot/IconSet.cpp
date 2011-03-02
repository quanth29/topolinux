/** @file IconSet.h
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief holder of icons
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */

#include "IconSet.h"

extern "C" {
#include "../icons/logo6-64.xpm"
#include "../icons/logo6c-64.xpm"

#include "../pixmaps/t_icon.xpm"
#include "../pixmaps/c_icon.xpm"
#include "../pixmaps/brush_wavy.xpm"
#include "../pixmaps/brush_star.xpm"
#include "../pixmaps/brush_cross.xpm"
#include "../pixmaps/brush_dot.xpm"

#include "../pixmaps/filescrap.xpm"
#include "../pixmaps/fileselect.xpm"
#include "../pixmaps/filepoint.xpm"
#include "../pixmaps/fileline.xpm"
#include "../pixmaps/filearea.xpm"

#include "../pixmaps/filenew2.xpm"
#include "../pixmaps/filenewoff2.xpm"
#include "../pixmaps/filesave2.xpm"
#include "../pixmaps/filesaveoff2.xpm"
#include "../pixmaps/fileopen2.xpm"
#include "../pixmaps/fileclose.xpm"
#include "../pixmaps/filedata2.xpm"
#include "../pixmaps/filedata3.xpm"
#include "../pixmaps/filedata4.xpm"
#include "../pixmaps/fileexport2.xpm"
#include "../pixmaps/fileexportoff2.xpm"
#include "../pixmaps/fileexport3.xpm"
#include "../pixmaps/fileexportoff3.xpm"
#include "../pixmaps/fileexport4.xpm"
#include "../pixmaps/fileexportoff4.xpm"
#include "../pixmaps/fileexport5.xpm"
#include "../pixmaps/fileexportoff5.xpm"
#include "../pixmaps/filecollapse2.xpm"
#include "../pixmaps/filecollapseoff2.xpm"
#include "../pixmaps/filehelp.xpm"
#include "../pixmaps/filequit2.xpm"
#include "../pixmaps/fileplan2.xpm"
#include "../pixmaps/fileplanoff2.xpm"
#include "../pixmaps/fileextended2.xpm"
#include "../pixmaps/fileextendedoff2.xpm"
#include "../pixmaps/file3d2.xpm"
#include "../pixmaps/file3doff2.xpm"
#include "../pixmaps/fileoptions2.xpm"
#include "../pixmaps/fileok.xpm"
#include "../pixmaps/filegrid.xpm"
#include "../pixmaps/filenumber.xpm"
#include "../pixmaps/fileleft.xpm"
#include "../pixmaps/fileright.xpm"
#include "../pixmaps/fileup.xpm"
#include "../pixmaps/filedown.xpm"

#include "../pixmaps/fileimage.xpm"

#include "../pixmaps/filezoomin.xpm"
#include "../pixmaps/filezoomout.xpm"
#include "../pixmaps/fileundo2.xpm"
#include "../pixmaps/fileundooff2.xpm"
#include "../pixmaps/filemode1.xpm"
#include "../pixmaps/filemode2.xpm"
#include "../pixmaps/filemode3.xpm"
#include "../pixmaps/filemode4.xpm"

#include "../pixmaps/filethetap.xpm"
#include "../pixmaps/filethetam.xpm"
#include "../pixmaps/filephip.xpm"
#include "../pixmaps/filephim.xpm"

#include "../pixmaps/cursor_pen.xpm"
#include "../pixmaps/cursor_line.xpm"

#include "../pixmaps/fileeval2.xpm"
#include "../pixmaps/fileevaloff2.xpm"
#include "../pixmaps/filetoggle2.xpm"
#include "../pixmaps/fileread2.xpm"
#include "../pixmaps/filewrite2.xpm"
#include "../pixmaps/filecover2.xpm"
#include "../pixmaps/filecoveroff2.xpm"
#include "../pixmaps/filecomment2.xpm"
#include "../pixmaps/filecommentoff2.xpm"

#include "../pixmaps/filewhite.xpm"
#include "../pixmaps/fileblue.xpm"
#include "../pixmaps/filegreen.xpm"
#include "../pixmaps/filedarkblue.xpm"
// #include "../pixmaps/filepit.xpm"

#include "../pixmaps/thp_air_draught.xpm"
#include "../pixmaps/thp_blocks.xpm"
#include "../pixmaps/thp_clay.xpm"
#include "../pixmaps/thp_debris.xpm"
#include "../pixmaps/thp_entrance.xpm"
#include "../pixmaps/thp_ice.xpm"
#include "../pixmaps/thp_pebbles.xpm"
#include "../pixmaps/thp_sand.xpm"
#include "../pixmaps/thp_snow.xpm"
#include "../pixmaps/thp_stalactite.xpm"
#include "../pixmaps/thp_stalagmite.xpm"
#include "../pixmaps/thp_user.xpm"
#include "../pixmaps/thp_water_flow.xpm"
}

// #include "TherionPoint.h"


IconSet *
IconSet::the_icon_set = NULL;

IconSet::IconSet()
  : pen_red( Qt::red )        // centerline
  , pen_black( Qt::black )    // splay
  , pen_blue( Qt::blue )      // wall THL_WALL
  , pen_violet( Qt::magenta ) // other lines
  , pen_yellow( Qt::yellow )  // yellow points
  , pen_green( Qt::green )    // user line
  , pen_gray( Qt::gray )      // rock-border lines
  , dash_black( Qt::black )
  , dash_blue( Qt::blue )
  , dash_violet( Qt::magenta )
  // , pit_pixmap( (const char **)filepit_xpm )
  // , pit_brush( Qt::black, pit_pixmap )
  // , pit_pen( pit_brush )
  , dark_red( Qt::darkRed )
  , dark_gray( Qt::darkGray )
  , pen_pit( Qt::magenta )
  , pen_chimney( Qt::magenta )
{
  dash_black.setStyle( Qt::DotLine );
  dash_blue.setStyle( Qt::DotLine );
  dash_violet.setStyle( Qt::DotLine );
/*
  pen_red.setWidth( 0 );
  pen_black.setWidth( 0 );
  pen_blue.setWidth( 0 );
  pen_violet.setWidth( 0 );
  pen_green.setWidth( 0 );
  pen_gray.setWidth( 0 );
  dash_black.setWidth( 0 );
  dash_blue.setWidth( 0 );
  dash_violet.setWidth( 0 );
  dark_red.setWidth( 0 );
  dark_gray.setWidth( 0 );
*/
  // pen_chimney.setStyle( Qt::DotLine );
  T_brush = QBrush( Qt::magenta, QPixmap( (const char **)t_icon_xpm ) );
  C_brush = QBrush( Qt::magenta, QPixmap( (const char **)c_icon_xpm ) );
  brush_wavy = QBrush( Qt::blue, QPixmap( (const char **)brush_wavy_xpm ) );
  brush_star = QBrush( Qt::gray, QPixmap( (const char **)brush_star_xpm ) );
  brush_cross = QBrush( Qt::gray, QPixmap( (const char **)brush_cross_xpm ) );
  brush_dot = QBrush( Qt::green, QPixmap( (const char **)brush_dot_xpm ) );
  // T_brush.setStyle( Qt::SolidPattern );
  pen_pit.setBrush( T_brush );
  pen_pit.setWidth(10);
  pen_pit.setCapStyle( Qt::FlatCap );
  pen_chimney.setBrush( C_brush );
  pen_chimney.setWidth(10);
  pen_chimney.setCapStyle( Qt::FlatCap );

  qtshotIcon = QPixmap( (const char **)logo6_64_xpm );
  qtcalibIcon = QPixmap( (const char **)logo6c_64_xpm );

  newIcon = QPixmap( (const char **)filenew2_xpm );
  newOffIcon = QPixmap( (const char **)filenewoff2_xpm );
  openIcon = QPixmap( (const char **)fileopen2_xpm );
  closeIcon = QPixmap( (const char **)fileclose_xpm );
  saveIcon = QPixmap( (const char **)filesave2_xpm );
  saveOffIcon = QPixmap( (const char **)filesaveoff2_xpm );
  dataIcon = QPixmap( (const char **)filedata2_xpm );
  data3Icon = QPixmap( (const char **)filedata3_xpm );
  data4Icon = QPixmap( (const char **)filedata4_xpm );
  exportThIcon = QPixmap( (const char **)fileexport2_xpm );
  exportThOffIcon = QPixmap( (const char **)fileexportoff2_xpm );
  exportDatIcon = QPixmap( (const char **)fileexport3_xpm );
  exportDatOffIcon = QPixmap( (const char **)fileexportoff3_xpm );
  exportSvxIcon = QPixmap( (const char **)fileexport4_xpm );
  exportSvxOffIcon = QPixmap( (const char **)fileexportoff4_xpm );
  exportTopIcon = QPixmap( (const char **)fileexport5_xpm );
  exportTopOffIcon = QPixmap( (const char **)fileexportoff5_xpm );
  collapseIcon = QPixmap( (const char **)filecollapse2_xpm );
  collapseOffIcon = QPixmap( (const char **)filecollapseoff2_xpm );
  gridIcon = QPixmap( (const char **)filegrid_xpm );
  numberIcon = QPixmap( (const char **)filenumber_xpm );
  helpIcon = QPixmap( (const char **)filehelp_xpm );
  quitIcon = QPixmap( (const char **)filequit2_xpm );
  optionsIcon = QPixmap( (const char **)fileoptions2_xpm );
  planIcon = QPixmap( (const char **)fileplan2_xpm );
  planOffIcon = QPixmap( (const char **)fileplanoff2_xpm );
  extendedIcon = QPixmap( (const char **)fileextended2_xpm );
  extendedOffIcon = QPixmap( (const char **)fileextendedoff2_xpm );
  _3dIcon = QPixmap( (const char **)file3d2_xpm );
  _3dOffIcon = QPixmap( (const char **)file3doff2_xpm );
  zoomInIcon = QPixmap( (const char **)filezoomin_xpm );
  zoomOutIcon = QPixmap( (const char **)filezoomout_xpm );
  thetaPlusIcon  = QPixmap( (const char **)filethetap_xpm );
  thetaMinusIcon = QPixmap( (const char **)filethetam_xpm );
  phiPlusIcon    = QPixmap( (const char **)filephip_xpm );
  phiMinusIcon   = QPixmap( (const char **)filephim_xpm );
  undoIcon    = QPixmap( (const char **)fileundo2_xpm );
  undoOffIcon = QPixmap( (const char **)fileundooff2_xpm );
  imageIcon   = QPixmap( (const char **)fileimage_xpm );
  mode1Icon   = QPixmap( (const char **)filemode1_xpm );
  mode2Icon   = QPixmap( (const char **)filemode2_xpm );
  mode3Icon   = QPixmap( (const char **)filemode3_xpm );
  mode4Icon   = QPixmap( (const char **)filemode4_xpm );

  scrapIcon   = QPixmap( (const char **)filescrap_xpm );
  selectIcon   = QPixmap( (const char **)fileselect_xpm );
  pointIcon   = QPixmap( (const char **)filepoint_xpm );
  lineIcon   = QPixmap( (const char **)fileline_xpm );
  areaIcon   = QPixmap( (const char **)filearea_xpm );

  okIcon = QPixmap( (const char **)fileok_xpm );
  leftIcon = QPixmap( (const char **)fileleft_xpm );
  rightIcon = QPixmap( (const char **)fileright_xpm );
  upIcon = QPixmap( (const char **)fileup_xpm );
  downIcon = QPixmap( (const char **)filedown_xpm );

  evalIcon   = QPixmap( (const char **)fileeval2_xpm );
  evalOffIcon   = QPixmap( (const char **)fileevaloff2_xpm );
  readIcon   = QPixmap( (const char **)fileread2_xpm );
  writeIcon   = QPixmap( (const char **)filewrite2_xpm );
  toggleIcon   = QPixmap( (const char **)filetoggle2_xpm );
  coverIcon   = QPixmap( (const char **)filecover2_xpm );
  coverOffIcon   = QPixmap( (const char **)filecoveroff2_xpm );
  commentIcon   = QPixmap( (const char **)filecomment2_xpm );
  commentOffIcon   = QPixmap( (const char **)filecommentoff2_xpm );

  whiteIcon = QPixmap( (const char **)filewhite_xpm );
  blueIcon = QPixmap( (const char **)fileblue_xpm );
  greenIcon = QPixmap( (const char **)filegreen_xpm );
  darkBlueIcon = QPixmap( (const char **)filedarkblue_xpm );

  penUpIcon   = QPixmap( (const char **)cursor_pen_xpm );
  penDownIcon = QPixmap( (const char **)cursor_line_xpm );

  penUpCursor = QCursor( penUpIcon, 7, 16 );
  penDownCursor = QCursor( penDownIcon, 0, 16 );

  thpAirDraught = QPixmap( (const char **)thp_air_draught_xpm );
  thpBlocks= QPixmap( (const char **)thp_blocks_xpm );
  thpClay = QPixmap( (const char **)thp_clay_xpm );
  // thpContinuation =
  thpDebris = QPixmap( (const char **)thp_debris_xpm );
  thpPebbles = QPixmap( (const char **)thp_pebbles_xpm );
  // thpLabel
  thpSand = QPixmap( (const char **)thp_sand_xpm );
  thpSnow = QPixmap( (const char **)thp_snow_xpm );
  thpIce = QPixmap( (const char **)thp_ice_xpm );
  thpStalactite = QPixmap( (const char **)thp_stalactite_xpm );
  thpStalagmite = QPixmap( (const char **)thp_stalagmite_xpm );
  thpUser = QPixmap( (const char **)thp_user_xpm );
  thpWaterFlow = QPixmap( (const char **)thp_water_flow_xpm );
  thpEntrance = QPixmap( (const char **)thp_entrance_xpm );
  // thp= QPixmap( (const char **)thp__xpm );

#ifdef OLD_POLYGON
  // polygon[THP_AIR].setPoints(
  //   11, -6,-3, -4,-4, -2,-2, 3,-2, 2,-3, 3,-4, 6,0, 3,4, 2,3, 3,2, -2,2); 
  polygon[Therion::THP_BLOCKS].setPoints(4,  6,6, -5, 5, -5, -6, 6, -5 );
  polygon[Therion::THP_CLAY].setPoints(8, 7,-1, 5,-3, 3,0, -4,-4, -7,1, -5,3, -3,0, 4,4);
  polygon[Therion::THP_DEBRIS].setPoints(5, -7,3, 7,3, 3,-4, 0,0, -3,-4 );
  polygon[Therion::THP_PEBBLES].setPoints(
    8, -7,1, -5,-4, -3,0, 0,6, 2,3, 4,-3, 7,2, -3,0 ); 
  polygon[Therion::THP_LABEL].setPoints(6, -3,4, 4,4, 4,1, 0,1, 0,-7, -3,-7);
  polygon[Therion::THP_SAND].setPoints(6, -5,0, -5,3, 0,3, 0,-3, 5,-3, 5,0);
  polygon[Therion::THP_SNOW].setPoints(
    9, -4,-2, 4,2, 0,0, 4,-2, -4,2, 0,0, 0,6, 0,-6, 0,0 );
  polygon[Therion::THP_ICE].setPoints(
    8, 0,5, -1,1, -5,0, -1,-1, 0,-5, 1,-1, 5,0, 1,1 );
  polygon[Therion::THP_STALACTITE].setPoints(
    9, -3,-6, 0,-3, 3,-6, 4,-4, 1,-1, 1,5, -1,5, -1,-1, -4,-4 );
  polygon[Therion::THP_STALAGMITE].setPoints(
    9, -3, 6, 0, 3, 3, 6, 4, 4, 1, 1, 1,-5, -1,-5, -1,1, -4,4 );
  polygon[Therion::THP_USER].setPoints(
    11, -4,-6, -2,-6, -2,0, 0,2, 2,0, 2,-6, 4,-6, 4,2, 2,4, -2,4, -4,2);
  // polygon[Therion::THP_WATER]
  // polygon[Therion::THP_ENTRANCE].setPoints(3, -2, 3, 0, -5, 2, 3 );
  // polygon[Therion::THP_CONTINUATION].setPoints(...);
  /* stationmuts be last */
  polygon[Therion::THP_STATION].setPoints(
    12, 1,1, 1,6, -1,6, -1,1, -6,1, -6,-1, -1,-1, -1,-6, 1,-6, 1,-1, 6,-1, 6,1 );

  // arrows: air-draught and water-flow
  arrow[0].setPoints(7, -2, 7, -2,-2, -5,-2, 0,-7, 5,-2, 2,-2, 2, 7); // up
  arrow[1].setPoints(7, -5, 3,  2,-4,  0,-6, 6,-6, 6, 0, 4,-2,-3, 5); // 45
  arrow[2].setPoints(7, -7,-2,  2,-2,  2,-5, 7, 0, 2, 5, 2, 2,-7, 2); // right
  arrow[3].setPoints(7, -5,-3,  2, 4,  0, 6, 6, 6, 6, 0, 4, 2,-3,-5); // 135
  arrow[4].setPoints(7, -2,-7, -2, 2, -5, 2, 0, 7, 5, 2, 2, 2, 2,-7); // down
  arrow[5].setPoints(7,  3,-5, -4, 2, -6, 0,-6, 6, 0, 6,-2, 4, 5,-3); // 225
  arrow[6].setPoints(7,  7,-2, -2,-2, -2,-5,-7, 0,-2, 5,-2, 2, 7, 2); // left
  arrow[7].setPoints(7,  5, 3, -2,-4,  0,-6,-6,-6,-6, 0,-4,-2, 3, 5); // 315

  // triangular fat_arrows: entrance
  fat_arrow[0].setPoints(3, -5, 5,  0,-10, 5, 5);  // up
  fat_arrow[1].setPoints(3, -6, 0,  8,-8,  0, 6);
  fat_arrow[2].setPoints(3, -5,-5, 10, 0, -5, 5);  // right
  fat_arrow[3].setPoints(3,  0,-6,  8, 8, -6, 0);
  fat_arrow[4].setPoints(3,  5,-5,  0,10, -5,-5);  // down
  fat_arrow[5].setPoints(3,  6, 0, -8, 8,  0,-6);
  fat_arrow[6].setPoints(3,  5, 5,-10, 0,  5,-5);  // left
  fat_arrow[7].setPoints(3,  0, 6, -8,-8,  6, 0);

  brush[Therion::THP_AIR_DRAUGHT] = QBrush( Qt::cyan );
  brush[Therion::THP_BLOCKS] = QBrush( Qt::gray );
  brush[Therion::THP_CLAY] = QBrush( Qt::darkGray );
  brush[Therion::THP_DEBRIS] = QBrush( Qt::lightGray );
  brush[Therion::THP_PEBBLES] = QBrush( Qt::lightGray );
  brush[Therion::THP_LABEL] = QBrush( Qt::black );
  brush[Therion::THP_SAND] = QBrush( Qt::darkYellow );
  // brush[Therion::THP_SNOW] = QBrush( Qt::gray );
  // brush[Therion::THP_ICE] = QBrush( Qt::gray );
  // brush[Therion::THP_STALACTITE] = QBrush( Qt::black );
  brush[Therion::THP_STALAGMITE] = QBrush( Qt::black );
  brush[Therion::THP_USER] = QBrush( Qt::green );
  brush[Therion::THP_WATER_FLOW] = QBrush( Qt::blue );
  brush[Therion::THP_ENTRANCE] = QBrush( Qt::black );
  brush[Therion::THP_CONTINUATION] = QBrush( Qt::black );

  brush[Therion::THP_STATION] = QBrush( Qt::black );   // leave station at last
#endif

  arrow_end[0].setPoints(3,  0,-3, 3,0, -3,0 );  // up
  arrow_end[1].setPoints(3,  2,-2, -3,-2, 2,3 ); // up/right
  arrow_end[2].setPoints(3,  3,0, 0,-3, 0,3 ); // right
  arrow_end[3].setPoints(3,  2,2, 2,-3, -3,2 );  // down/right
  arrow_end[4].setPoints(3,  0,3, -3,0, 3,0 ); // down
  arrow_end[5].setPoints(3,  -2,2, -2,-3, 3,2 ); // down/left
  arrow_end[6].setPoints(3,  -3,0, 0,-3, 0,3 );  // left
  arrow_end[7].setPoints(3,  -2,-2, 3,-2, -2,3 );// up/left

}


