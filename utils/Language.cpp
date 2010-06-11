/** @file Language.cpp
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief has table of strings
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include "Language.h"

Language * 
Language::the_lexicon = NULL;


void
Language::init( const char * filename )
{
  fprintf(stderr, "Language::init() file %s \n", filename );
  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    fprintf(stderr, "Language::init() cannot open file %s \n", filename );
    return;
  }

  char line[256];
  while ( fgets(line, 255, fp ) ) {
    char * ch = line; // key
    while ( *ch && isspace(*ch) ) ++ch;
    if ( *ch == 0 || *ch == '#' ) continue;
    char * ch1 = ch; // value
    while ( *ch1 && ! isspace(*ch1) ) ++ch1;
    if ( *ch1 == 0 ) continue;
    *ch1 = 0;
    ++ch1;
    while ( *ch1 && isspace(*ch1) ) ++ch1;
    if ( *ch1 == 0 ) continue;
    char * ch2 = ch1;
    while ( *ch2 && *ch2 != '\n' ) ++ch2;
    *ch2 = 0;
    Replace( ch, ch1 );
  }
  fclose( fp );
}


void 
Language::Insert( const char * k, const char * v )
{
  if ( words == NULL ) {
    words = new Idiom( k, v );
  } else {
    if ( words->equal( k ) ) { 
      fprintf(stderr, "Language: insert duplicate key %s\n", k );
    } else if ( words->after( k ) ) {
      Idiom * idiom = new Idiom( k, v );
      idiom->next = words;
      words = idiom;
    } else {
      Idiom * w = words;
      while ( w->next && w->next->before( k ) ) w = w->next;
      if ( w->next == NULL ) {
        w->next = new Idiom( k, v );
      } else if ( w->next->equal( k ) ) {
        fprintf(stderr, "Language: insert duplicate key %s\n", k );
      } else {
        Idiom * idiom = new Idiom( k, v );
        idiom->next = w->next;
        w->next = idiom;
      }
    }
  }
}

void 
Language::Replace( const char * k, const char * v )
{
  Idiom * w = words;
  while ( w != NULL && ! w->equal( k ) ) w = w->next;
  if ( w == NULL ) {
    fprintf(stderr, "Language: replace key not found %s\n", k );
  } else {
    w->value = v;
  }
}

Language::Language() 
  : words( NULL )
{
  Insert( "3d", "3D");
  Insert( "angle_units", "Angle units ");
  Insert( "append_shots", "Append shots  ");
  Insert( "azimuth", "Azimuth");
  Insert( "azimuth+", "Azimuth +");
  Insert( "azimuth-", "Azimuth -");
  Insert( "backward", " backward");
  Insert( "bad_file_format", "Unrecognized file type.\nOnly raw or TLX survey files are supported.");
  Insert( "base_station", "Start plot from here");
  Insert( "calibration_data_no_save", "calibration data. Failed saving to file " );
  Insert( "calibration_data_save", "calibration data. Saving to file " );
  Insert( "cancel", "Cancel");
  Insert( "canvas_mode", "QTopo - Plot mode");
  Insert( "centerline_commands", "Centerline commands");
  Insert( "clean_scrap", "Do you really want to clear the scrap?");
  Insert( "clean_shots", "Do you really want to clear the survey?");
  Insert( "clear", "Clear");
  Insert( "clino", "Clino");
  Insert( "close", "Close");
  Insert( "coeff_backup", "Calibration backed up to file." );
  Insert( "comment", "Comment");
  Insert( "cover", "Coverage" );
  Insert( "cross_section", "Cross section");
  Insert( "description", "Description" );
  Insert( "D", "D" );
  Insert( "data_download_failed", "Calibration data download failed.\nCheck the connection to the DistoX.");
  Insert( "date", "Date" );
  Insert( "declination", "Declination" );
  Insert( "default_dat", "Using file /tmp/data.dat" );
  Insert( "default_none", "Undefined export" );
  Insert( "default_th", "Using file /tmp/data.th" );
  Insert( "default_top", "Using file /tmp/data.top" );
  Insert( "default_svx", "Using file /tmp/data.svx" );
  Insert( "deg", "deg");
  Insert( "delete_shot", "Delete shot");
  Insert( "delta", "Delta " );
  Insert( "device", "Device ");
  Insert( "distox_backup", "  Backup old coeffiecients " );
  Insert( "distox_calib", "DistoX in calibration mode." );
  Insert( "distox_download", "QTopo - DistoX");
  Insert( "distox_mode", "DistoX - Mode" );
  Insert( "distox_normal", "DistoX in normal mode." );
  Insert( "distox_write", "DistoX - Write" );
  Insert( "distox_write2", "Write calibration to the DistoX" );

  Insert( "down", "Down");
  Insert( "download", "Download");
  Insert( "download_shots", "Download shots from DistoX");
  Insert( "duplicate", "duplicate shot" );
  Insert( "edit_descr", "Calibration description:" );
  Insert( "enter_filename", "Enter the filename:");
  Insert( "error", "Error" );
  Insert( "eval", "Compute" );
  Insert( "exit", "Exit");
  Insert( "exit_question", "Do you really want to exit?");
  Insert( "export", "Export");
  Insert( "ext", "Ext");
  Insert( "ext_box", "Extend");
  Insert( "extended", "Extended");
  Insert( "failed_backup", "Failed to backup calibration." );
  Insert( "failed_disto", "DistoX connection failure");
  Insert( "failed_mode", "Failed to get DistoX mode.\nCheck the connection to the DistoX." );
  Insert( "failed_open", "Failed to open backup file.\nCheck folder permissions." );
  Insert( "failed_read", "Failed to read calibration from DistoX.\nCheck the connection to the DistoX." );
  Insert( "failed_read_default", "Failed to read calibration from DistoX.\nUsing default." );
  Insert( "failed_write", "Failed to write calibration to DistoX.\nCheck the connection to the DistoX." );
  Insert( "failed_toggle", "Failed to switch DistoX mode.\nCheck the connection to the DistoX." );
  Insert( "file_open_failed", "Failed to open file.\nCheck file permissions.");
  Insert( "flag", "Flg");
  Insert( "flag_box", "Flag");
  Insert( "from", "From");
  Insert( "from_station", " FROM station");
  Insert( "ft", "ft");
  Insert( "grad", "grad");
  Insert( "grid", "Grid");
  Insert( "guess_centerline", "Guess centerline ");
  Insert( "help", "Help" );
  Insert( "help_index", "See file help\\en\\index.htm");
  Insert( "horizontal", "Horizontal");
  Insert( "I", "I" );
  Insert( "ignore", "Ignore");
  Insert( "image_open_failed", "Failed to open image file.\nCheck file type and properties.");
  Insert( "incl+", "Incl. +");
  Insert( "incl-", "Incl. -");
  Insert( "insert", "Insert");
  Insert( "insert_before", "Insert before ");
  Insert( "insert_shot", "Insert shot");
  Insert( "insert_shot_at", "Insert shot at");
  Insert( "iterations", "Iterations " );
  Insert( "L", "L" );
  Insert( "left", "Left");
  Insert( "length_units", "Length units ");
  Insert( "line", "Line ");
  Insert( "LRUD", "LRUD");
  Insert( "m", "m");
  Insert( "merge_next", "Merge w. next");
  Insert( "mode", "Mode");
  Insert( "mode_calib", "Calibration mode");
  Insert( "mode_silent", "Silent mode");
  Insert( "N", "-" );
  Insert( "name", "Name");
  Insert( "no", "No");
  Insert( "no_export_type", "Unexpected export type.\nPlease report your problem.");
  Insert( "no_saved_dat", "Failed to save compass file.\nUnable to open file.");
  Insert( "no_saved_svx", "Failed to save survex file.\nUnable to open file.");
  Insert( "no_saved_th", "Failed to save therion file.\nUnable to open file.");
  Insert( "no_saved_top", "Failed to save pockettopo file.\nUnable to open file.");
  Insert( "none", "None");
  Insert( "number", "Stations");
  Insert( "ok", "OK");
  Insert( "one_station", "Cannot add sketch with fewer than two stations" );
  Insert( "open", "Open");
  Insert( "open_file", "Open file");
  Insert( "open_sketch", "Open sketch");
  Insert( "open_survey", "Open survey file");
  Insert( "orientation", "Orientation" );
  Insert( "options", "Options");
  Insert( "plan", "Plan");
  Insert( "point", "Point ");
  Insert( "prefix", "Stations prefix");
  Insert( "prefix_too_long", "More than 12 char in a station name\nUse a shorter prefix and save again");
  Insert( "properties", "Comment and options" );

// window titles
  Insert( "qtopo_3d", "QTopo - 3D");
  Insert( "qtopo_centerline", "QTopo - Survey" );
  Insert( "qtopo_clean_scrap", "QTopo - Clear scrap");
  Insert( "qtopo_clean_shots", "QTopo - Clear Survey");
  Insert( "qtopo_comment", "QTopo - Comment");
  Insert( "qtopo_continuation", "QTopo - Continuation text");
  Insert( "qtopo_e_line", "QTopo E-section - Line ");
  Insert( "qtopo_e_point", "QTopo E-section - Point ");
  Insert( "qtopo_e_select", "QTopo E-section - Select");
  Insert( "qtopo_edit_station", "QTopo - Edit station");
  Insert( "qtopo_exit", "QTopo - Exit" );
  Insert( "qtopo_extend", "QTopo - Extend");
  Insert( "qtopo_help", "QTopo - Help" );
  Insert( "qtopo_insert_LRUD", "QTopo - LRUD");
  Insert( "qtopo_insert_shot", "QTopo - Shot insert");
  Insert( "qtopo_label", "QTopo - Label text");
  Insert( "qtopo_options", "QTopo - Options");
  Insert( "qtopo_p_line", "QTopo Plan - Line ");
  Insert( "qtopo_p_point", "QTopo Plan - Point ");
  Insert( "qtopo_p_select", "QTopo Plan - Select");
  Insert( "qtopo_plot", "QTopo - Plot" );
  Insert( "qtopo_scrap", "QTopo - Scrap" );
  Insert( "qtopo_select", "QTopo Plot - Select");
  Insert( "qtopo_shot", "QTopo - Shot" );
  Insert( "qtopo_sketch", "QTopo - Sketch");
  Insert( "qtopo_station", "QTopo - Station");
  Insert( "qtopo_station_comment", "QTopo - Station Comment");
  Insert( "qtopo_x_line", "QTopo X-section - Line ");
  Insert( "qtopo_x_point", "QTopo X-section - Point ");
  Insert( "qtopo_x_select", "QTopo X-section - Select");

  Insert( "qtopoc_calib", "QTopo - Calib");
  Insert( "qtopoc_coeff", "QTopo - Calibration");
  Insert( "qtopoc_comment", "QTopo - Description" );
  Insert( "qtopoc_data", "Download calibration data from DistoX.");
  Insert( "qtopoc_guess", "  Guess the data groups");
  Insert( "qtopoc_guess2", "  Use DistoX calibration to guess");

  Insert( "R", "R" );
  Insert( "raw_read_failed", "Failed to read raw survey file.\nWrong file format.");
  Insert( "read", "Read" );
  Insert( "read_", "Read" );
  Insert( "read_data", "calibration data from the DistoX." );
  Insert( "remove_station", "Remove station");
  Insert( "renumber", "Renumber");
  Insert( "reversed", " reversed");
  Insert( "right", "Right");
  Insert( "S", "S" );
  Insert( "save", "Save as");
  Insert( "save_file", "Save file");
  Insert( "save_th2", "Save th2 file");
  Insert( "saved_dat", "Saved compass file");
  Insert( "saved_svx", "Saved survex file");
  Insert( "saved_th", "Saved therion file");
  Insert( "saved_top", "Saved pockettopo file");
  Insert( "scrap_name", "Scrap name ");
  Insert( "scrap_prefix_e", "ext");
  Insert( "scrap_prefix_h", "horiz");
  Insert( "scrap_prefix_p", "plan");
  Insert( "scrap_prefix_x", "cross");
  Insert( "select", "Select");
  Insert( "set", "Set" );
  Insert( "shot_comment", "QTopo - Shot");
  Insert( "shot_download_failed", "Shot download failed.\nCheck the connection to the DistoX.");
  Insert( "shots_saving", "shots. Saving to file" );
  Insert( "shots_saving_failed", "shots. Failed saving to file" );
  Insert( "single_survey", "Single survey" );
  Insert( "sketch", "Sketch");
  Insert( "skip", "Skip" );
  Insert( "splay", "Toggle splay");
  Insert( "splay_shots", "Splay shots at  ");
  Insert( "split", "Split");
  Insert( "station", "Station");
  Insert( "surface", "surface shot" );
  Insert( "survey_commands", "Survey commands");
  Insert( "survey_info", "QTopo - Survey info");
  Insert( "swap_from_to", "swap stations" );
  Insert( "tape", "Tape");
  Insert( "team", "Team");
  Insert( "title", "Title");
  Insert( "tlx_read_failed", "Failed to read TLX survey file.\nWrong file format.");
  Insert( "to", "To");
  Insert( "to_station", " TO station");
  Insert( "toggle", "Toggle" );
  Insert( "toggle_mode", "Toggle DistoX modes" );
  Insert( "too_few_data", "Not enough calibration data.\nNeed at least 16." );
  Insert( "undo", "Undo");
  Insert( "up", "Up");
  Insert( "V", "V" );
  Insert( "vertical", "Vertical");
  Insert( "warn_scrap", 
    "Scrap has fewer that two control points.\nIf you continue you must fix its scale in xtherion.");
  Insert( "what_do", "What do you want to do?");
  Insert( "write", "Write" );
  Insert( "write_LRUD", "Write LRUD below");
  Insert( "write_no", "Calibration NOT written to DistoX." );
  Insert( "write_ok", "Written calibration to DistoX." );
  Insert( "written", "Written" );

  Insert( "x-section", "X-section");
  Insert( "yes", "Yes");
  Insert( "zero_data", "Read no calibration data." );
  Insert( "zoom_in", "Zoom in");
  Insert( "zoom_out", "Zoom out");

// prompt


// window titles
}
