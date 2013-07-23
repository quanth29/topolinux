/** @file SketchDef.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: defines
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20130310 created
 */
package com.android.DistoX;

class SketchDef
{
    public static final float POINT_STEP = 0.5f; // 0.5 m between line 3d points
    public static final float BORDER_STEP = 0.2f; // 0.2 m between line 3d points
    public static final int BORDER       = 50;
    public static final float CLOSE_GAP  = 1.0f;
    public static final int SHAPE_STEP   = 20;  
    public static final int POINT_MIN    =  4; //  4 minimum number of 3D points on a line
    public static final int POINT_MAX    = 12; // 12 maximum number of 3D points on a line
    public static final float MIN_DISTANCE = 20.0f; // minimum closeness distance (select at)

    public static final float ZOOM_INC = 1.4f;
    public static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public final static int DISPLAY_NGBH = 0;
    public final static int DISPLAY_SINGLE = 1;
    public final static int DISPLAY_ALL = 2;
    public final static int DISPLAY_MAX = 3;

    public static final int SYMBOL_POINT = 1;
    public static final int SYMBOL_LINE  = 2;
    public static final int SYMBOL_AREA  = 3;

    public static final int MODE_NONE = 0;
    public static final int MODE_DRAW = 1;
    public static final int MODE_MOVE = 2;
    public static final int MODE_STEP = 3;
    public static final int MODE_ROTATE = 4;
    public static final int MODE_SHOT = 5;

    public static final int TOUCH_NONE = 0;
    public static final int TOUCH_MOVE = 2;
    public static final int TOUCH_ZOOM = 5;

    public static final int VIEW_NONE  = 0;
    public static final int VIEW_TOP   = 1; // plan
    public static final int VIEW_SIDE  = 2; // profile
    public static final int VIEW_3D    = 3; 
    public static final int VIEW_EXTRUDE = 4;
    public static final int VIEW_CROSS = 5;

    public static final int EDIT_NONE = 0;
    public static final int EDIT_CUT = 1;
    public static final int EDIT_STRETCH = 2;
    public static final int EDIT_EXTRUDE = 3;

    static final String[] mode_name = { "none", "draw", "move", "step", "rotate", "shot" };
    static final String[] view_type = { "none", "top", "side", "3d", "extrude", "cross" };
    static final String[] edit_name = { "none", "cut", "stretch", "extrude" };

}
