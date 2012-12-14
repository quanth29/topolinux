/* @file Vector.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3 vector
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.android.DistoX;

import java.lang.Math;

public class Vector
{
  public float x,y,z;

  public static Vector zero = new Vector(0.0f, 0.0f, 0.0f);

  public Vector()
  {
    x = 0.0f;
    y = 0.0f;
    z = 0.0f;
  }

  public Vector( float x0, float y0, float z0 )
  {
    x = x0;
    y = y0;
    z = z0;
  }

  public Vector( Vector a )
  {
    x = a.x;
    y = a.y;
    z = a.z;
  }

  public float Length()
  {
    return (float)Math.sqrt( x*x + y*y + z*z );
  }

  public float Abs( ) { return Length(); }

  public Vector TurnX( float s, float c )
  {
    return new Vector( x, c*y - s*z, c*z + s*y );
  }

  public Vector TurnY( float s, float c )
  {
    return new Vector( c*x + s*z, y, c*z - s*x );
  }

  public Vector TurnZ( float s, float c )
  {
    return new Vector( c*x - s*y, c*y + s*x, z );
  }


  public void Normalized( )
  {
     float n = 1.0f / Length();
     x *= n;
     y *= n;
     z *= n;
  }

  public float MaxDiff( Vector b )
  {
    float dx = (float)Math.abs( x - b.x );
    float dy = (float)Math.abs( y - b.y );
    float dz = (float)Math.abs( z - b.z );
    if ( dx < dy ) { dx = dy; }
    if ( dx < dz ) { dx = dz; }
    return dx;
  }

  public void copy( Vector b ) // copy assignment
  {
    x = b.x;
    y = b.y;
    z = b.z;
  }

  public void add( Vector b ) 
  {
    x += b.x;
    y += b.y;
    z += b.z;
  }

  public Vector plus( Vector b ) 
  {
    return new Vector( x+b.x, y+b.y, z+b.z );
  }

  public Vector minus( Vector b ) 
  {
    return new Vector( x-b.x, y-b.y, z-b.z );
  }

  // MULTIPLICATION: this * b
  public Vector mult( float b )
  {
    return new Vector(x*b, y*b, z*b );
  }

  // DOT PRODUCT: this * b
  public float dot( Vector b )
  {
    return x*b.x + y*b.y + z*b.z;
  }

  // CROSS PRODUCT: this % b
  public Vector cross( Vector b )
  {
    return new Vector( y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x );
  }
 
}
