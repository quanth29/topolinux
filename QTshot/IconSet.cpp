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
#include <assert.h>

#include "IconSet.h"

extern "C" {
#include "../icons/logo6-64.xpm"
#include "../icons/logo6c-64.xpm"

#include "../pixmaps/t_icon.xpm"
#include "../pixmaps/c_icon.xpm"
#include "../pixmaps/brush_wavy_blue.xpm"
#include "../pixmaps/brush_wavy_black.xpm"
#include "../pixmaps/brush_dash_black.xpm"
#include "../pixmaps/brush_star_black.xpm"
#include "../pixmaps/brush_cross_black.xpm"
#include "../pixmaps/brush_dot_green.xpm"
#include "../pixmaps/brush_dot_yellow.xpm"
#include "../pixmaps/brush_debris.xpm"
#include "../pixmaps/brush_pebbles.xpm"

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
#include "../pixmaps/fileview.xpm"

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

#ifdef HAS_THP_ICONS
  #include "../pixmaps/thp_air_draught.xpm"
  #include "../pixmaps/thp_blocks.xpm"
  #include "../pixmaps/thp_clay.xpm"
  #include "../pixmaps/thp_crystal.xpm"
  #include "../pixmaps/thp_debris.xpm"
  #include "../pixmaps/thp_entrance.xpm"
  #include "../pixmaps/thp_flowstone.xpm"
  #include "../pixmaps/thp_helictite.xpm"
  #include "../pixmaps/thp_ice.xpm"
  #include "../pixmaps/thp_pebbles.xpm"
  #include "../pixmaps/thp_sand.xpm"
  #include "../pixmaps/thp_snow.xpm"
  #include "../pixmaps/thp_stalactite.xpm"
  #include "../pixmaps/thp_stalagmite.xpm"
  #include "../pixmaps/thp_user.xpm"
  #include "../pixmaps/thp_water_flow.xpm"
  #include "../pixmaps/thp_station.xpm"
#endif
}



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
  , dash_gray( Qt::gray )
  , dash_violet( Qt::magenta )
  , dash_yellow( Qt::darkYellow )
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
  dash_gray.setStyle( Qt::DotLine );
  dash_violet.setStyle( Qt::DotLine );
  dash_yellow.setStyle( Qt::DotLine );

  // pen_chimney.setStyle( Qt::DotLine );
  T_brush = QBrush( Qt::magenta, QPixmap( (const char **)t_icon_xpm ) );
  C_brush = QBrush( Qt::magenta, QPixmap( (const char **)c_icon_xpm ) );
  brush_wavy_blue   = QBrush( Qt::blue, QPixmap( (const char **)brush_wavy_blue_xpm ) );
  brush_wavy_black  = QBrush( Qt::blue, QPixmap( (const char **)brush_wavy_black_xpm ) );
  brush_star_black  = QBrush( Qt::gray, QPixmap( (const char **)brush_star_black_xpm ) );
  brush_cross_black = QBrush( Qt::gray, QPixmap( (const char **)brush_cross_black_xpm ) );
  brush_dot_green   = QBrush( Qt::green, QPixmap( (const char **)brush_dot_green_xpm ) );
  brush_dot_yellow  = QBrush( Qt::green, QPixmap( (const char **)brush_dot_yellow_xpm ) );
  brush_dash_black  = QBrush( Qt::gray, QPixmap( (const char **)brush_dash_black_xpm ) );
  brush_debris      = QBrush( Qt::gray, QPixmap( (const char **)brush_debris_xpm ) );
  brush_pebbles     = QBrush( Qt::gray, QPixmap( (const char **)brush_pebbles_xpm ) );

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

  scrapIcon  = QPixmap( (const char **)filescrap_xpm );
  selectIcon = QPixmap( (const char **)fileselect_xpm );
  pointIcon  = QPixmap( (const char **)filepoint_xpm );
  lineIcon   = QPixmap( (const char **)fileline_xpm );
  areaIcon   = QPixmap( (const char **)filearea_xpm );

  okIcon = QPixmap( (const char **)fileok_xpm );
  leftIcon = QPixmap( (const char **)fileleft_xpm );
  rightIcon = QPixmap( (const char **)fileright_xpm );
  upIcon = QPixmap( (const char **)fileup_xpm );
  downIcon = QPixmap( (const char **)filedown_xpm );
  viewIcon = QPixmap( (const char **)fileview_xpm );

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

  #ifdef HAS_THP_ICONS
    thpAirDraught = QPixmap( (const char **)thp_air_draught_xpm );
    thpBlocks= QPixmap( (const char **)thp_blocks_xpm );
    thpClay = QPixmap( (const char **)thp_clay_xpm );
    // thpContinuation =
    thpCrystal = QPixmap( (const char **)thp_crystal_xpm );
    thpDebris = QPixmap( (const char **)thp_debris_xpm );
    thpFlowstone = QPixmap( (const char **)thp_flowstone_xpm );
    thpHelictite = QPixmap( (const char **)thp_helictite_xpm );
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
    thpStation = QPixmap( (const char **)thp_station_xpm );
    // thp= QPixmap( (const char **)thp__xpm );
  #else
    polygon[Therion::THP_AIR_DRAUGHT].setPoints(
      11, -3,6, -4,4, -2,2, -2,-3, -3,-2, -4,-3, 0,-6, 4,-3, 3,-2, 2,-3, 2,2); 
    polygon[Therion::THP_ANCHOR].setPoints(
      10, -4,-1, 0,-1, 1.5,-4, 4.5,-4, 6,-1, 6,1, 4.5,4, 1.5,4, 0,1, -4,1 );
    polygon[Therion::THP_BLOCKS].setPoints(
      10, -1,3, 5,3, 5,-5, -4,-5, -4,3, -1,3, -1,7, 8,7, 8,-3, -1,-3 );
    polygon[Therion::THP_BREAKDOWN_CHOKE].setPoints(
      10, 0,-2, -4,1, -4,-3, 0,-2, 1,-1, 5,-2, 5,2, 5,-1, 3,1, -1,2 ); 
    polygon[Therion::THP_CLAY].setPoints(
      14, 7,-1, 5,-3, 3,-3, 1,0, -1,3, -3,3, -5,1, -6,2, -4,4, -2,4, 0,1, 2,-2, 4,-2, 6,0);
    polygon[Therion::THP_CLAY_CHOKE].setPoints(
      8, 7,-1, 5,-3, 3,0, -4,-4, -7,1, -5,3, -3,0, 4,4);
    // polygon[Therion::THP_CONTINUATION].setPoints(...);
    polygon[Therion::THP_CRYSTAL].setPoints(
      5, -4,-2, -4,3, 0,5, 4,3, 4,-2, 0,-4 );
    polygon[Therion::THP_CURTAIN].setPoints(
      15, -3,-6, 0,-3, 3,-6, 4,-4, 1,-1, 1,0, -1,2, 1,4, 1,5, -1,5, -1,4, -3,2, -1,0, -1,-1, -4,-4 );
    polygon[Therion::THP_DEBRIS].setPoints(
      8, 0,-2, -5,-2, -5,3, 0,3, 0,-4, 5,-4, 5,1, 0,1 );
    polygon[Therion::THP_DIG].setPoints(
      8, -4,0, -6,-2, -2,-6, 0,-4, -1,-3, 5,3, 3,5, -3,-1 );
    polygon[Therion::THP_ENTRANCE].setPoints(
      3, -2, 3, 0, -5, 2, 3 );
    polygon[Therion::THP_FLOWSTONE].setPoints(
      8,  -7,-3, -3,-3, -3,3, 3,3, 3,-3, 7,-3, 7,0, -7,0 );
    polygon[Therion::THP_FLOWSTONE_CHOKE].setPoints(
      8,  -7,-3, -3,-3, -3,3, 3,3, 3,-3, 7,-3, 7,0, -7,0 );
    polygon[Therion::THP_HELICTITE].setPoints(
      17, -3,-6, 0,-3, 3,-6, 4,-4, 1,-1, 1,1, 3,1, 3,2, 1,2, 
          1,5, -1,5, -1,2, -3,2, -3,1, -1,1, -1,-1, -4,-4 );
    polygon[Therion::THP_ICE].setPoints(
      8, 0,5, -1,1, -5,0, -1,-1, 0,-5, 1,-1, 5,0, 1,1 );
    polygon[Therion::THP_NARROW_END].setPoints(
      12, -2,0, -2,-4, -3,-4, -3,4, -2,4, -2,0, 2,0, 2,4, 3,4, 3,-4, 2,-4, 2,0 ); 
    polygon[Therion::THP_PEBBLES].setPoints(
      8, -7,1, -5,-4, -3,0, 0,6, 2,3, 4,-3, 7,2, -3,0 );
    polygon[Therion::THP_PILLAR].setPoints(
      12, -1,-2, -3,-6, 0,-3, 3,-6, 4,-4, 1,-2, 1,2, 4,4, 3,6, 0,3, -3,6, -1,2 );
    polygon[Therion::THP_POPCORN].setPoints(
      18, -5,0, -3,0, -3,-2, -4,-3, -3,-4, -2,-4, -1,-3, -2,-2, -2,0,
           2,0, 2,-2, 1,-3, 2,-4, 3,-4, 4,-3, 3,-2, 3,0, 5,0 );
    polygon[Therion::THP_LABEL].setPoints(
      6, -3,4, 4,4, 4,1, 0,1, 0,-7, -3,-7);
    polygon[Therion::THP_SAND].setPoints(
      6, -5,0, -5,3, 0,3, 0,-3, 5,-3, 5,0);
    polygon[Therion::THP_SNOW].setPoints(
      9, -4,-2, 4,2, 0,0, 4,-2, -4,2, 0,0, 0,6, 0,-6, 0,0 );
    polygon[Therion::THP_STALACTITE].setPoints(
      9, -3,-6, 0,-3, 3,-6, 4,-4, 1,-1, 1,5, -1,5, -1,-1, -4,-4 );
    polygon[Therion::THP_STALAGMITE].setPoints(
      9, -3, 6, 0, 3, 3, 6, 4, 4, 1, 1, 1,-5, -1,-5, -1,1, -4,4 );
    polygon[Therion::THP_USER].setPoints(
      11, -4,-6, -2,-6, -2,0, 0,2, 2,0, 2,-6, 4,-6, 4,2, 2,4, -2,4, -4,2);
    polygon[Therion::THP_WATER_FLOW].setPoints(
      7, 2,6, -2,6, -2,-3, -4,-3, 0,-6, 4,-3, 2,-3 );
    /* stationmuts be last */
    polygon[Therion::THP_STATION].setPoints(
      12, 1,1, 1,6, -1,6, -1,1, -6,1, -6,-1, -1,-1, -1,-6, 1,-6, 1,-1, 6,-1, 6,1 );

    path[ Therion::THP_AIR_DRAUGHT ].moveTo( 2, 2 );
    path[ Therion::THP_AIR_DRAUGHT ].lineTo( 0, 0 );
    path[ Therion::THP_AIR_DRAUGHT ].lineTo(  0, -8 );
    path[ Therion::THP_AIR_DRAUGHT ].moveTo( -3, -5 );
    path[ Therion::THP_AIR_DRAUGHT ].lineTo(  0, -8 );
    path[ Therion::THP_AIR_DRAUGHT ].lineTo(  3, -5 );

    path[ Therion::THP_ANCHOR ].moveTo( -6, -0.5 );
    path[ Therion::THP_ANCHOR ].lineTo(  0, -0.5 );
    path[ Therion::THP_ANCHOR ].cubicTo( 0,-3.5, 5,-3, 5,0);
    path[ Therion::THP_ANCHOR ].cubicTo( 5,3, 0,3.5, 0,0.5);
    path[ Therion::THP_ANCHOR ].lineTo( -6, 0.5 );
    path[ Therion::THP_ANCHOR ].closeSubpath();

    path[ Therion::THP_BLOCKS ].addRect( -5, -5, 7, 7 );
    path[ Therion::THP_BLOCKS ].addRect( -3, -2, 7, 7 );

    path[ Therion::THP_BREAKDOWN_CHOKE ].addRect( -5,-3, 7, 5 );
    path[ Therion::THP_BREAKDOWN_CHOKE ].addRect( -0,-2, 7, 5 );
    path[ Therion::THP_BREAKDOWN_CHOKE ].moveTo( -2,-5 );
    path[ Therion::THP_BREAKDOWN_CHOKE ].lineTo( -2,5 );
    path[ Therion::THP_BREAKDOWN_CHOKE ].moveTo( 4,-5 );
    path[ Therion::THP_BREAKDOWN_CHOKE ].lineTo( 4,5 );
    

    path[ Therion::THP_CLAY ].moveTo( -4, 0 );
    path[ Therion::THP_CLAY ].cubicTo( -2,-2, -2,-2, 0, 0 );
    path[ Therion::THP_CLAY ].cubicTo( 2,2, 2,2, 4,0 );

    path[ Therion::THP_CLAY_CHOKE ].moveTo( -4, 0 );
    path[ Therion::THP_CLAY_CHOKE ].cubicTo( -2,-2, -2,-2, 0, 0 );
    path[ Therion::THP_CLAY_CHOKE ].cubicTo( 2,2, 2,2, 4,0 );
    path[ Therion::THP_CLAY_CHOKE ].moveTo( -1,4 );
    path[ Therion::THP_CLAY_CHOKE ].lineTo( -1,-4 );
    path[ Therion::THP_CLAY_CHOKE ].moveTo( 1,-4 );
    path[ Therion::THP_CLAY_CHOKE ].lineTo( 1,4 );

    path[ Therion::THP_CRYSTAL ].moveTo( 0, 4 );
    path[ Therion::THP_CRYSTAL ].lineTo( 0, -4 );
    path[ Therion::THP_CRYSTAL ].moveTo( 3, 2 );
    path[ Therion::THP_CRYSTAL ].lineTo( -3, -2 );
    path[ Therion::THP_CRYSTAL ].moveTo( 3, -2 );
    path[ Therion::THP_CRYSTAL ].lineTo( -3, 2 );

    path[ Therion::THP_CURTAIN ].moveTo( -3,-6 );
    path[ Therion::THP_CURTAIN ].lineTo( 0, -3 );
    path[ Therion::THP_CURTAIN ].lineTo( 3,-6 );
    path[ Therion::THP_CURTAIN ].moveTo( 0,-3 );
    path[ Therion::THP_CURTAIN ].lineTo( 0,-1 );
    path[ Therion::THP_CURTAIN ].cubicTo( -2,-1, -2,3, 0,3 );
    path[ Therion::THP_CURTAIN ].lineTo( 0,5 );
    
    path[ Therion::THP_DEBRIS ].addRect( -4, -4, 4, 4 );
    path[ Therion::THP_DEBRIS ].addRect( 0, -3, 4, 4 );

    path[ Therion::THP_DIG ].moveTo( -4,0 );
    path[ Therion::THP_DIG ].lineTo( -5,-1 );
    path[ Therion::THP_DIG ].cubicTo( -4,-2, -4,-2, -5,-3 );
    path[ Therion::THP_DIG ].lineTo( -2,-6 );
    path[ Therion::THP_DIG ].lineTo( 0, -4 );
    path[ Therion::THP_DIG ].lineTo( -1.5, -2.5 );
    path[ Therion::THP_DIG ].lineTo( 4, 3);
    path[ Therion::THP_DIG ].lineTo( 3, 4);
    path[ Therion::THP_DIG ].lineTo( -2.5, -1.5 );
    path[ Therion::THP_DIG ].closeSubpath();

    path[ Therion::THP_ENTRANCE ].moveTo( -3, 3 );
    path[ Therion::THP_ENTRANCE ].lineTo( 3, 3 );
    path[ Therion::THP_ENTRANCE ].lineTo( 0, -6 );
    path[ Therion::THP_ENTRANCE ].closeSubpath();
   
    path[ Therion::THP_FLOWSTONE ].moveTo( 0, 0 );
    path[ Therion::THP_FLOWSTONE ].lineTo( 4, 0 );
    path[ Therion::THP_FLOWSTONE ].moveTo( 3, 2 );
    path[ Therion::THP_FLOWSTONE ].lineTo( 7, 2 );
    path[ Therion::THP_FLOWSTONE ].moveTo( -3, 2 );
    path[ Therion::THP_FLOWSTONE ].lineTo( 1, 2 );

    path[ Therion::THP_FLOWSTONE_CHOKE ].moveTo( -1, 0 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].lineTo( 5, 0 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].moveTo( 3, 2 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].lineTo( 7, 2 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].moveTo( -3, 2 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].lineTo( 1, 2 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].moveTo( -0, -3 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].lineTo( -0, 5 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].moveTo(  4, -3 );
    path[ Therion::THP_FLOWSTONE_CHOKE ].lineTo(  4, 5 );

    path[ Therion::THP_HELICTITE ].moveTo( 0, -4 );
    path[ Therion::THP_HELICTITE ].lineTo( 0,  4 );
    path[ Therion::THP_HELICTITE ].moveTo( 2, -3 );
    path[ Therion::THP_HELICTITE ].lineTo( 2,  0 );
    path[ Therion::THP_HELICTITE ].lineTo( -2,  0 );
    path[ Therion::THP_HELICTITE ].lineTo( -2,  3 );

    path[ Therion::THP_ICE ].moveTo( 1, 1 );
    path[ Therion::THP_ICE ].lineTo( 4, 0 );
    path[ Therion::THP_ICE ].lineTo( 1, -1 );
    path[ Therion::THP_ICE ].lineTo( 0, -4 );
    path[ Therion::THP_ICE ].lineTo( -1, -1 );
    path[ Therion::THP_ICE ].lineTo( -4, 0 );
    path[ Therion::THP_ICE ].lineTo( -1, 1 );
    path[ Therion::THP_ICE ].lineTo( 0, 4 );
    path[ Therion::THP_ICE ].closeSubpath();

    path[ Therion::THP_NARROW_END].moveTo(-2,-5);
    path[ Therion::THP_NARROW_END].lineTo(-2,5);
    path[ Therion::THP_NARROW_END].moveTo(2,-5);
    path[ Therion::THP_NARROW_END].lineTo(2,5);

    path[ Therion::THP_PEBBLES ].moveTo( -3, -2 );
    path[ Therion::THP_PEBBLES ].arcTo( -3,-3, 4,3, 0.0, 360);
    path[ Therion::THP_PEBBLES ].moveTo( 3, -1 );
    path[ Therion::THP_PEBBLES ].arcTo( 3,-2, 4,3, 0.0, 360);
    path[ Therion::THP_PEBBLES ].moveTo( -1, 0 );
    path[ Therion::THP_PEBBLES ].arcTo( -1,1, 4,3, 0.0, 360);

    path[ Therion::THP_PILLAR ].moveTo( -3,-6 );
    path[ Therion::THP_PILLAR ].lineTo( 0,-3 );
    path[ Therion::THP_PILLAR ].lineTo( 3,-6 );
    path[ Therion::THP_PILLAR ].moveTo( 0,-3 );
    path[ Therion::THP_PILLAR ].lineTo( 0,3 );
    path[ Therion::THP_PILLAR ].lineTo( 3,6 );
    path[ Therion::THP_PILLAR ].moveTo( 0,3 );
    path[ Therion::THP_PILLAR ].lineTo( -3,6 );

    path[ Therion::THP_POPCORN].moveTo(-5,0);
    path[ Therion::THP_POPCORN].lineTo(5,0);
    path[ Therion::THP_POPCORN].moveTo(-3,0);
    path[ Therion::THP_POPCORN].lineTo(-3,-3);
    path[ Therion::THP_POPCORN].cubicTo(-4.5,-3,-4.5,-6, -3,-6);
    path[ Therion::THP_POPCORN].cubicTo(-1.5,-6,-1.5,-3, -3,-3);
    path[ Therion::THP_POPCORN].moveTo(2,0);
    path[ Therion::THP_POPCORN].lineTo(2,-3);
    path[ Therion::THP_POPCORN].cubicTo(3.5,-3,3.5,-6, 2,-6);
    path[ Therion::THP_POPCORN].cubicTo(0.5,-6,0.5,-3, 2,-3);
    
    path[ Therion::THP_SAND ].moveTo( 1, -3 );
    path[ Therion::THP_SAND ].arcTo( -1, -4, 2, 2, 0.0, 360 );
    path[ Therion::THP_SAND ].moveTo( -1, 1 );
    path[ Therion::THP_SAND ].arcTo( -3, 0, 2, 2, 0.0, 360 );
    path[ Therion::THP_SAND ].moveTo( 5, 2 );
    path[ Therion::THP_SAND ].arcTo( 3, 1, 2, 2, 0.0, 360 );

    path[ Therion::THP_SNOW ].moveTo( -4, 0 );
    path[ Therion::THP_SNOW ].lineTo( 4, 0 );
    path[ Therion::THP_SNOW ].moveTo( -2,  3 );
    path[ Therion::THP_SNOW ].lineTo(  2, -3 );
    path[ Therion::THP_SNOW ].moveTo( -2, -3 );
    path[ Therion::THP_SNOW ].lineTo(  2,  3 );
    path[ Therion::THP_SNOW ].moveTo( -2, -2 ); // ticks
    path[ Therion::THP_SNOW ].lineTo( -1, -3 );
    path[ Therion::THP_SNOW ].moveTo( -2,  2 );
    path[ Therion::THP_SNOW ].lineTo( -1,  3 );
    path[ Therion::THP_SNOW ].moveTo(  2, -2 );
    path[ Therion::THP_SNOW ].lineTo(  1, -3 );
    path[ Therion::THP_SNOW ].moveTo(  2,  2 );
    path[ Therion::THP_SNOW ].lineTo(  1,  3 );
    path[ Therion::THP_SNOW ].moveTo( -3,  1 );
    path[ Therion::THP_SNOW ].lineTo( -3, -1 );
    path[ Therion::THP_SNOW ].moveTo(  3,  1 );
    path[ Therion::THP_SNOW ].lineTo(  3, -1 );

    path[ Therion::THP_STALACTITE ].moveTo( -3, -4 );
    path[ Therion::THP_STALACTITE ].lineTo(  0, 0 );
    path[ Therion::THP_STALACTITE ].lineTo(  0, 6 );
    path[ Therion::THP_STALACTITE ].moveTo( 3, -4 );
    path[ Therion::THP_STALACTITE ].lineTo( 0, 0 );

    path[ Therion::THP_STALAGMITE ].moveTo( -3, 4 );
    path[ Therion::THP_STALAGMITE ].lineTo( 0, 0 );
    path[ Therion::THP_STALAGMITE ].lineTo( 0, -6 );
    path[ Therion::THP_STALAGMITE ].moveTo( 3, 4 );
    path[ Therion::THP_STALAGMITE ].lineTo( 0, 0 );
    path[ Therion::THP_STALAGMITE ].closeSubpath();

    path[ Therion::THP_USER ].addEllipse( -4, -4, 8, 8 );
    path[ Therion::THP_USER ].closeSubpath();
    
    path[ Therion::THP_WATER_FLOW ].moveTo( -3, -5 );
    path[ Therion::THP_WATER_FLOW ].lineTo(  0, -8 );
    path[ Therion::THP_WATER_FLOW ].lineTo(  3, -5 );
    path[ Therion::THP_WATER_FLOW ].moveTo(  0, -8 );
    path[ Therion::THP_WATER_FLOW ].lineTo(  0, -6 );
    path[ Therion::THP_WATER_FLOW ].cubicTo( -1.5, -4.5, -1.5, -3.5, 0, -2);
    path[ Therion::THP_WATER_FLOW ].cubicTo(  1.5, -0.5,  1.5,  0.5, 0,  2);
    path[ Therion::THP_WATER_FLOW ].cubicTo( -1.5,  3.5, -1.5,  4.5, 0,  6);
    // path[ Therion::THP_WATER_FLOW ].arcTo( -2, 0, 4, 4, 315, 90 );

    path[ Therion::THP_STATION ].moveTo(2,2);
    path[ Therion::THP_STATION ].addEllipse( -3, -3, 6, 6 );
    path[ Therion::THP_STATION ].moveTo(0, 4);
    path[ Therion::THP_STATION ].lineTo(0, -4);
    path[ Therion::THP_STATION ].moveTo(4, 0);
    path[ Therion::THP_STATION ].lineTo(-4, 0);
    
  #endif
#if 0
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
#endif
  brush[Therion::THP_AIR_DRAUGHT] = QBrush( Qt::cyan );
  brush[Therion::THP_ANCHOR] = QBrush( Qt::gray );
  brush[Therion::THP_BLOCKS] = QBrush( Qt::gray );
  brush[Therion::THP_BREAKDOWN_CHOKE] = QBrush( Qt::gray );
  brush[Therion::THP_CLAY] = QBrush( Qt::white );
  brush[Therion::THP_CLAY_CHOKE] = QBrush( Qt::white );
  brush[Therion::THP_CONTINUATION] = QBrush( Qt::black );
  brush[Therion::THP_CRYSTAL] = QBrush( Qt::gray );
  brush[Therion::THP_CURTAIN] = QBrush( Qt::darkYellow );
  brush[Therion::THP_DEBRIS] = QBrush( Qt::lightGray );
  brush[Therion::THP_DIG] = QBrush( Qt::black );
  brush[Therion::THP_ENTRANCE] = QBrush( Qt::black );
  brush[Therion::THP_FLOWSTONE] = QBrush( Qt::black );
  brush[Therion::THP_FLOWSTONE_CHOKE] = QBrush( Qt::black );
  brush[Therion::THP_HELICTITE] = QBrush( Qt::white );
  brush[Therion::THP_ICE] = QBrush( Qt::white );
  brush[Therion::THP_LABEL] = QBrush( Qt::black );
  brush[Therion::THP_PEBBLES] = QBrush( Qt::lightGray );
  brush[Therion::THP_PILLAR] = QBrush( Qt::white );
  brush[Therion::THP_POPCORN] = QBrush( Qt::darkYellow );
  brush[Therion::THP_SAND] = QBrush( Qt::darkYellow );
  brush[Therion::THP_SNOW] = QBrush( Qt::white );
  brush[Therion::THP_STALACTITE] = QBrush( Qt::white );
  brush[Therion::THP_STALAGMITE] = QBrush( Qt::white );
  brush[Therion::THP_USER] = QBrush( Qt::green );
  brush[Therion::THP_WATER_FLOW] = QBrush( Qt::blue );
  brush[Therion::THP_STATION] = QBrush( Qt::white );   // leave station at last

  thp_pen[Therion::THP_AIR_DRAUGHT]  = QPen( Qt::blue );
  thp_pen[Therion::THP_ANCHOR]       = QPen( Qt::black );
  thp_pen[Therion::THP_BLOCKS]       = QPen( Qt::darkGray );
  thp_pen[Therion::THP_BREAKDOWN_CHOKE] = QPen( Qt::black );
  thp_pen[Therion::THP_CLAY]         = QPen( Qt::black );
  thp_pen[Therion::THP_CLAY_CHOKE]   = QPen( Qt::black );
  thp_pen[Therion::THP_CONTINUATION] = QPen( Qt::black );
  thp_pen[Therion::THP_CRYSTAL]      = QPen( Qt::black );
  thp_pen[Therion::THP_CURTAIN]      = QPen( Qt::black );
  thp_pen[Therion::THP_DEBRIS]       = QPen( Qt::darkGray );
  thp_pen[Therion::THP_DIG]          = QPen( Qt::black );
  thp_pen[Therion::THP_ENTRANCE]     = QPen( Qt::black );
  thp_pen[Therion::THP_FLOWSTONE]    = QPen( Qt::black );
  thp_pen[Therion::THP_FLOWSTONE_CHOKE] = QPen( Qt::black );
  thp_pen[Therion::THP_HELICTITE]    = QPen( Qt::black );
  thp_pen[Therion::THP_ICE]          = QPen( Qt::black );
  thp_pen[Therion::THP_LABEL]        = QPen( Qt::black );
  thp_pen[Therion::THP_PEBBLES]      = QPen( Qt::darkGray );
  thp_pen[Therion::THP_PILLAR]       = QPen( Qt::black );
  thp_pen[Therion::THP_POPCORN]      = QPen( Qt::black );
  thp_pen[Therion::THP_SAND]         = QPen( Qt::darkYellow );
  thp_pen[Therion::THP_SNOW]         = QPen( Qt::black );
  thp_pen[Therion::THP_STALACTITE]   = QPen( Qt::black );
  thp_pen[Therion::THP_STALAGMITE]   = QPen( Qt::black );
  thp_pen[Therion::THP_USER]         = QPen( Qt::green );
  thp_pen[Therion::THP_WATER_FLOW]   = QPen( Qt::blue );
  thp_pen[Therion::THP_STATION]      = QPen( Qt::black );   // leave station at last

  arrow_end[0].setPoints(3,  0,-3, 3,0, -3,0 );  // up
  arrow_end[1].setPoints(3,  2,-2, -3,-2, 2,3 ); // up/right
  arrow_end[2].setPoints(3,  3,0, 0,-3, 0,3 ); // right
  arrow_end[3].setPoints(3,  2,2, 2,-3, -3,2 );  // down/right
  arrow_end[4].setPoints(3,  0,3, -3,0, 3,0 ); // down
  arrow_end[5].setPoints(3,  -2,2, -2,-3, 3,2 ); // down/left
  arrow_end[6].setPoints(3,  -3,0, 0,-3, 0,3 );  // left
  arrow_end[7].setPoints(3,  -2,-2, 3,-2, -2,3 );// up/left

}

#ifdef HAS_THP_ICONS
const QPixmap & 
IconSet::ThpPixmap( Therion::PointType type ) const
{
  switch ( type ) {
    case Therion::THP_AIR_DRAUGHT:     return ThpAirDraught(); 
    case Therion::THP_BLOCKS:          return ThpBlocks();
    case Therion::THP_CLAY:            return ThpClay();
    case Therion::THP_CONTINUATION:    assert( ! "cannot call IconSet::ThpPixmap() with THP_CONTINUATION" );
    case Therion::THP_CRYSTAL:         return ThpCrystal();
    case Therion::THP_DEBRIS:          return ThpDebris();
    case Therion::THP_ENTRANCE:        return ThpEntrance();
    case Therion::THP_FLOWSTONE:       return ThpFlowstone();
    case Therion::THP_HELICTITE:       return ThpHelictite();
    case Therion::THP_ICE:             return ThpIce();
    case Therion::THP_LABEL:           assert( ! "cannot call IconSet::ThpPixmap() with THP_LABEL" );
    case Therion::THP_PEBBLES:         return ThpPebbles();  
    case Therion::THP_SAND:            return ThpSand();
    case Therion::THP_SNOW:            return ThpSnow();
    case Therion::THP_STALACTITE:      return ThpStalactite();
    case Therion::THP_STALAGMITE:      return ThpStalagmite();
    case Therion::THP_USER:            return ThpUser();
    case Therion::THP_WATER_FLOW:      return ThpWaterFlow();
    case Therion::THP_STATION:         return ThpStation();
    case Therion::THP_PLACEMARK:       assert ( ! "cannot call IconSet::ThpPixmap() with THP_PLACEMARK" );
  }
  return ThpStation(); // suppress warning
}
#else
void
IconSet::ThpStyle1( Therion::PointType t, QPolygon & poly, QBrush & brush ) const
{
  poly  = ThpSymbol1( t );
  brush = ThpBrush( t );
}

void
IconSet::ThpStyle2( Therion::PointType t, 
                    QPainterPath & path, QBrush & brush, QPen & pen ) const
{
  path = ThpSymbol2( t );
  brush = ThpBrush( t );
  pen  = ThpPen( t );
}
#endif

void
IconSet::AreaStyle( Therion::AreaType t, QColor & color, QPen & pen, QBrush & brush )
{
  color = Qt::black;
  pen   = PenBlack();
  switch ( t ) {
    case Therion::THA_CLAY:
      color = Qt::gray;
      pen   = PenGray();
      brush = BrushWavyBlack();
      break;
    case Therion::THA_DEBRIS:
      color = Qt::darkGray;
      pen   = PenGray();
      brush = BrushDebris();
      break;
    case Therion::THA_FLOWSTONE:
      color = Qt::darkYellow;
      pen = PenYellow();
      brush = BrushDashBlack();
      break;
    case Therion::THA_ICE:
      color = Qt::gray;
      pen = PenDashGray();
      brush = BrushCrossBlack();
      break;
    case Therion::THA_PEBBLES:
      color = Qt::darkGray;
      pen   = PenGray();
      brush = BrushPebbles();
      break;
    case Therion::THA_SAND:
      color = Qt::darkYellow;
      pen = PenDashYellow();
      brush = BrushDotYellow();
      break;
    case Therion::THA_SNOW:
      color = Qt::gray;
      pen = PenDashGray();
      brush = BrushStarBlack();
      break;
    case Therion::THA_USER:
      color = Qt::green;
      pen = PenGreen();
      brush = BrushDotGreen();
      break;
    case Therion::THA_WATER:
      color = Qt::blue;
      pen = PenBlue();
      brush = BrushWavyBlue();
      break;
    case Therion::THA_PLACEMARK:
      break;
  }
}

QColor 
IconSet::LinePointColor( Therion::LineType type ) 
{
  QColor color = Qt::black;
  switch ( type ) {
    case Therion::THL_ARROW:
      // color = Qt::black;
      break;
    case Therion::THL_BORDER:
      color = Qt::magenta;
      break;
    case Therion::THL_CHIMNEY:
      color = Qt::magenta;
      break;
    case Therion::THL_PIT:
      color = Qt::magenta;
      break;
    case Therion::THL_USER:
      color = Qt::green;
      break;
    case Therion::THL_WALL:
      // color = Qt::black;
      break;
    case Therion::THL_ROCK:
      color = Qt::gray;
      break;
    case Therion::THL_SLOPE:
      color = Qt::darkGray;
      break;
    case Therion::THL_CONTOUR:
      color = Qt::magenta;
      break;
    case Therion::THL_PLACEMARK:
      break;
  }
  return color;
}

// pen is the segment pen
// colo1r is the point color
void
IconSet::LineStyle( Therion::LineType t, QPen & pen, QColor & color )
{
  IconSet * icon = IconSet::Get();
  switch ( t ) {
    case Therion::THL_ARROW:
      color = Qt::black;
      pen = icon->PenBlack();
      break;
    case Therion::THL_BORDER:
      color = Qt::magenta;
      pen   = icon->PenViolet();
      break;
    case Therion::THL_CHIMNEY:
      color = Qt::magenta;
      pen   = icon->PenChimney();
      break;
    case Therion::THL_PIT:
      color = Qt::magenta;
      pen   = icon->PenPit();
      break;
    case Therion::THL_USER:
      color = Qt::green;
      pen   = icon->PenGreen();
      break;
    case Therion::THL_WALL:
      color = Qt::black;
      pen   = icon->PenBlack();
      break;
    case Therion::THL_ROCK:
      color = Qt::gray;
      pen   = icon->PenDarkGray();
      break;
    case Therion::THL_SLOPE:
      color = Qt::darkGray;
      pen   = icon->PenDashBlack();
      break;
    case Therion::THL_CONTOUR:
      color = Qt::magenta;
      pen   = icon->PenDashViolet();
      break;
    case Therion::THL_PLACEMARK:
      break;
  }
}

