/** @flile Sketch3dInfo.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3D sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130220 created
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.graphics.PointF;
import android.graphics.Path;

import android.util.FloatMath;
import android.util.Log;

class Sketch3dInfo extends SketchShot
{
  static final float mXScale = 1.0f;
  static final float DEG2RAD = (float)(Math.PI / 180);
  long   surveyId;
  long   id;
  String name;   // sketch name
  String start;  // start station (origin of ref. system, for NUM)
  // String st1;    // current station
  // String st2;    // forward station
  float  xoffset_top,  yoffset_top,  zoom_top;  // 2d scene offset, and zoom
  float  xoffset_side, yoffset_side, zoom_side;  // 2d scene offset, and zoom
  float  xoffset_3d,   yoffset_3d,   zoom_3d;  // 2d scene offset, and zoom
  float  east, south, vert; // 3d origin
  float  azimuth, clino;    // 3d view angles [deg]

  float xcenter;
  float ycenter;
  float sin_alpha; // for the SIDE view
  float cos_alpha;
  float sin_gamma;
  float cos_gamma; // for the TOP view

  NumStation station1;  // station 1
  NumStation station2;  // station 2
  // NumShot    shot;      // current shot
  // float  sa0, ca0;      // azimuth of the shot
  // float  sg0, cg0;      // clino of the shot
  float  ne, ns, nv;    // unit vector in the direction of sight
  float  nxx, nxy, nxz; // unit vector of X-axis in the projection
  float  nyx, nyy, nyz; // unit vector of Y-axis in the projection
  float  de1, ds1, dh1, dv1; // difference: Station2 - Station1
  float  dvdh;               // ratio DV/DH
  float h0;
  private float x1, y1, z1;  // station1 - origin (world coords)
  private float ux, uy, uz;  // shot unit vector
  private PointF p1, p2;     // work points

  Sketch3dInfo()
  {
    super(null, null);
    p1 = new PointF();
    p2 = new PointF();
    start = "0";
  }

  
  /** check if a triangle is forward
   */
  boolean isForward( SketchTriangle t )
  {
    return t.normal.x * ne + t.normal.y * ns + t.normal.z * nv > 0.0f;
  }

  
  /** the shot dircetion is     N0 = (sa*cg, ca*cg, sg)
   *  the orthonormal triad has N1 = ( -ca, sa, 0 ) horizontal
   *                            N2 = ( -sa*sg, -ca*sg, cg ) = N0 x N1
   *   
   *        |
   *        +---------------------> east
   *     .'/|\  
   * N1.' / | \   
   *     /  |  \ N0
   *   s    V vert
   *
   * "horizontal" points have c=+1 (right) and -1 (left)
   * vertical points have s=+1 (down) and -1 (up)
   * therefore the angles are:
   *   0 right, PI/2 down, PI left, 3PI/2 up
   */
  float averageAngle( SketchLinePath line ) 
  {
    float c = 0.0f;
    float s = 0.0f;
    for ( Vector p : line.mLine.points ) {
      float x = p.x - station1.e;
      float y = p.y - station1.s;
      float z = p.z - station1.v;
      c += - x * cos_alpha + y * sin_alpha;
           // dot product with n1 = ( -cos_alpha, sin_alpha, 0.0 )
      s += - ( x * sin_alpha + y * cos_alpha ) * sin_gamma + z * cos_gamma;
           // dot product with n2 = ( - sin_alpha*sin_gamma, -cos_alpha*sin_gamma, cos_gamma )
    }
    float d = FloatMath.sqrt( c*c + s*s );
    c /= d;
    s /= d;
    return (float)Math.atan2( s, c );
  }

  void resetDirection()
  {
    azimuth = 0.0f;
    clino = 0.0f;
    setDirection();
  }


  /**
   * @param da   variation of azimuth
   * @param dc   variation of clino
   */
  void rotateBy3d( float da, float dc )
  { 
    worldToScene( x1, y1, z1, p1 );
    azimuth -= da;
    if ( azimuth > 360 ) azimuth -= 360;
    if ( azimuth < 0 ) azimuth += 360;
    dc += clino;
    if ( dc >  180 ) dc -= 360;
    if ( dc < -180 ) dc += 360;
    clino = dc;

    setDirection();

    worldToScene( x1, y1, z1, p2 );
    xoffset_3d += (p1.x - p2.x);
    yoffset_3d += (p1.y - p2.y);
  }

  /** compute the triplet of unit vectors
   *           ^
         -vert |
               |   ,(sa*cc, -ca*cc, -sc)_esv = -No
Nx=(-ca,-sa,0) | ,' 
              \|_________ east
              / \
       south /   \ Ny = No ^ Nx
   */
  void setDirection()
  {
    float cc = FloatMath.cos( clino * DEG2RAD ); // cos and sin of clino and azimuth
    float sc = FloatMath.sin( clino * DEG2RAD );
    float ca = FloatMath.cos( azimuth * DEG2RAD );
    float sa = FloatMath.sin( azimuth * DEG2RAD );
    ne = - sa * cc;
    ns =   ca * cc;
    nv =   sc;
    nxx = - ca;
    nxy = - sa;
    nxz = 0;
    nyx =   sa * sc;
    nyy = - ca * sc;
    nyz =   cc;
  }

  String getDirectionString()
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( "%.0f %.0f", azimuth, clino );
    return sw.getBuffer().toString();
  }
  
  void setStations( NumStation s1, NumStation s2, boolean set_origin, int view )
  {
    if ( set_origin ) {
      east  = s1.e;
      south = s1.s;
      vert  = s1.v;
      xoffset_top  = xcenter;
      yoffset_top  = ycenter;
      xoffset_side = xcenter;
      yoffset_side = ycenter;
      xoffset_3d   = xcenter;
      yoffset_3d   = ycenter;
    } else {
      // if ( station1 != null ) {
      //   if ( view != SketchDef.VIEW_TOP ) {
      //     xoffset_top += s1.e - station1.e;
      //     yoffset_top += s1.s - station1.s;
      //   } 
      //   if ( view != SketchDef.VIEW_SIDE ) {
      //     xoffset_side += (s1.e - station1.e)*sin_alpha + (s1.s - station1.s)*cos_alpha;
      //     yoffset_side += s1.v - station1.v;
      //   }
      //   if ( view != SketchDef.VIEW_3D ) {
      //     xoffset_3d += (s1.e - station1.e)*nxx + (s1.s - station1.s)*nxy + (s1.v - station1.v)*nxz;
      //     yoffset_3d += (s1.e - station1.e)*nyx + (s1.s - station1.s)*nyy + (s1.v - station1.v)*nyz;
      //   }
      // }

      // // } else {
      //   if ( view == SketchDef.VIEW_TOP ) {
      //     xoffset += s1.e - east;
      //     yoffset += s1.s - south;
      //   } else if ( view == SketchDef.VIEW_SIDE ) {
      //     float x = s1.e - east;
      //     float y = s1.s - south;
      //     xoffset += FloatMath.sqrt( x*x + y*y );
      //     yoffset += s1.v - vert;
      //   } else if ( view == SketchDef.VIEW_3D ) {
      //   } else {
      //   }
      // }
    }

    st1 = s1.name;
    st2 = s2.name;
    station1 = s1;
    station2 = s2;
    de1 = station2.e - station1.e;
    ds1 = station2.s - station1.s;
    dv1 = station2.v - station1.v;
    dh1 = FloatMath.sqrt( de1*de1 + ds1*ds1 );
    if ( dh1 < 0.01f ) dh1 += 0.01f; // regularize by adding 1 cm
    sin_alpha = de1/dh1;
    cos_alpha = ds1/dh1;
    dvdh = dv1 / dh1;
    // len is guaranteed non-zero (since dh1 >= 0.01)
    float len = FloatMath.sqrt( dh1*dh1 + dv1*dv1 );
    sin_gamma = dv1 / len; // == uz
    cos_gamma = dh1 / len;
    azimuth = 0.0f;
    clino   = 0.0f;
    x1 = (station1.e - east);
    y1 = (station1.s - south);
    z1 = (station1.v - vert);

    ux = de1 / len;
    uy = ds1 / len;
    uz = dv1 / len;

    // float det = 1.0f/(ds1*ds1 + de1*de1);
    // float a1 = station1.e * ds1 - station1.s * de1;
    h0 = (station1.e-east)*sin_alpha + (station1.s-south)*cos_alpha;
  }

  float distance3d( Vector v )
  {
    float c = v.x * ux + v.y * uy + v.z * uz;
    float x = v.x - ux * c;
    float y = v.y - uy * c;
    float z = v.z - uz * c;
    return FloatMath.sqrt( x*x + y*y + z*z );
  }

  float worldToSceneOrigin( Vector v, PointF p )
  {
    if ( v == null ) return 0f;
    return worldToSceneOrigin( v.x, v.y, v.z, p );
  }

  float worldToSceneOrigin( float x, float y, float z, PointF p ) 
  {
    x -= east;
    y -= south;
    z -= vert;
    p.x = nxx * x + nxy * y + nxz * z;
    p.y = nyx * x + nyy * y + nyz * z;
    return ne * x + ns * y + nv * z;
  }

  private void worldToScene( float x, float y, float z, PointF p ) 
  {
    p.x = nxx * x + nxy * y + nxz * z;
    p.y = nyx * x + nyy * y + nyz * z;
  }

  float canvasToSceneX( float x, int view ) 
  { 
    switch ( view ) {
      case SketchDef.VIEW_TOP:  return (x)/zoom_top  - xoffset_top;
      case SketchDef.VIEW_SIDE: return (x)/zoom_side - xoffset_side;
      case SketchDef.VIEW_3D:   return (x)/zoom_3d   - xoffset_3d;
    }
    return x;
  }

  float canvasToSceneY( float y, int view )
  {
    switch ( view ) {
      case SketchDef.VIEW_TOP:  return (y)/zoom_top  - yoffset_top; 
      case SketchDef.VIEW_SIDE: return (y)/zoom_side - yoffset_side;
      case SketchDef.VIEW_3D:   return (y)/zoom_3d   - yoffset_3d; 
    }
    return y;
  }

  float sceneToCanvasX( float x, int view ) 
  {
    switch ( view ) {
      case SketchDef.VIEW_TOP:  return (x+xoffset_top)  * zoom_top;
      case SketchDef.VIEW_SIDE: return (x+xoffset_side) * zoom_side;
      case SketchDef.VIEW_3D:   return (x+xoffset_3d)   * zoom_3d; 
    }
    return x;
  }

  float sceneToCanvasYtop( float y, int view )
  { 
    switch ( view ) {
      case SketchDef.VIEW_TOP: return  (y+yoffset_top)  * zoom_top; 
      case SketchDef.VIEW_SIDE: return (y+yoffset_side) * zoom_side;
      case SketchDef.VIEW_3D: return   (y+yoffset_3d)   * zoom_3d; 
    }
    return y;
  }

  float projTop( LinePoint p ) 
  {
    return p.mX*sin_alpha + p.mY*cos_alpha;
  }

  // Vector sceneToWorld( PointF p ) 
  // {
  //   return new Vector( nxx * p.x + nyx * p.y, nxy * p.x + nyy * p.y, nxz * p.x + nyz * p.y );
  // }

  Vector sceneToWorld( LinePoint p ) 
  {
    return new Vector( nxx * p.mX + nyx * p.mY, nxy * p.mX + nyy * p.mY, nxz * p.mX + nyz * p.mY );
  }

  void shiftOffsettop( float x, float y )
  {
    xoffset_top += x / zoom_top;
    yoffset_top += y / zoom_top;
  }
  void shiftOffsetside( float x, float y )
  {
    xoffset_side += x / zoom_side;
    yoffset_side += y / zoom_side;
  }
  void shiftOffset3d( float x, float y )
  {
    xoffset_3d += x / zoom_3d;
    yoffset_3d += y / zoom_3d;
  }


  void changeZoomtop( float f )
  {
    float z = zoom_top;
    zoom_top *= f;
    z = 1/z - 1/zoom_top;
    xoffset_top -= xcenter * z;
    yoffset_top -= ycenter * z;
  }
  void changeZoomside( float f )
  {
    float z = zoom_side;
    zoom_side *= f;
    z = 1/z - 1/zoom_side;
    xoffset_side -= xcenter * z;
    yoffset_side -= ycenter * z;
  }
  void changeZoom3d( float f )
  {
    float z = zoom_3d;
    zoom_3d *= f;
    z = 1/z - 1/zoom_3d;
    xoffset_3d -= xcenter * z;
    yoffset_3d -= ycenter * z;
  }

  void resetZoomtop( float x, float y, float z )
  {
    xoffset_top = x/2;
    yoffset_top = y/2;
    zoom_top = z; 
  }
  void resetZoomside( float x, float y, float z )
  {
    xoffset_side = x/2;
    yoffset_side = y/2;
    zoom_side = z; 
  }
  void resetZoom3d( float x, float y, float z )
  {
    xoffset_3d = x/2;
    yoffset_3d = y/2;
    zoom_3d = z; 
  }

  Vector topTo3d( LinePoint p )
  {
    float x0 = east  + (float)p.mX * cos_gamma / mXScale;
    float y0 = south + (float)p.mY * cos_gamma / mXScale;
    float hh = (x0-station1.e) * sin_alpha + (y0-station1.s) * cos_alpha;
    float z0 = station1.v + hh * dvdh;
    return new Vector( x0, y0, z0 );
  }

  Vector sideTo3d( LinePoint p ) 
  {
    float h = (float)p.mX / mXScale - h0;
    float z = vert  + (float)p.mY;
    float x = station1.e + h * sin_alpha;
    float y = station1.s + h * cos_alpha;
    return new Vector( x, y, z );
  }

  /** the line point(s) are already in the scene reference frame
   */
  Vector projTo3d( LinePoint p )
  {
    return sceneToWorld( p );
  }

  void topPathOffset( Path path, float x, float y )
  {
    x = x/cos_gamma - east;
    y = y/cos_gamma - south;
    path.offset( x, y );
  }

  void topPathMoveTo( Path path, Vector p )
  {
    float x = p.x/cos_gamma - east;
    float y = p.y/cos_gamma - south;
    path.moveTo( x, y );
  }
     
  void topPathLineTo( Path path, Vector p )
  {
    float x = p.x/cos_gamma - east;
    float y = p.y/cos_gamma - south;
    path.lineTo( x, y );
  }

  void sidePathOffset( Path path, float x, float y, float z )
  {
    float e = x - east;
    float s = y - south;
    float xx = e * sin_alpha + s * cos_alpha;
    float yy = z - vert;
    path.offset( xx, yy );
  }

  void sidePathMoveTo( Path path, Vector p )
  {
    float e = p.x - east;
    float s = p.y - south;
    float x = e * sin_alpha + s * cos_alpha;
    float y = p.z - vert;
    path.moveTo( x, y );
  }

  void sidePathLineTo( Path path, Vector p )
  {
    float e = p.x - east;
    float s = p.y - south;
    float x = e * sin_alpha + s * cos_alpha;
    float y = p.z - vert;
    path.lineTo( x, y );
  }
}
