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
import java.io.PrintWriter;
import java.util.ArrayList;

import android.util.FloatMath;


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
    float len = Length();
    if ( len > 0.0f ) {
      float n = 1.0f / len;
      x *= n;
      y *= n;
      z *= n;
    }
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

  public void sub( Vector b ) 
  {
    x -= b.x;
    y -= b.y;
    z -= b.z;
  }

  public void times( float f )
  {
    x *= f;
    y *= f;
    z *= f;
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

  // projection in the plane orthogonal to this vector (supposed nomralized)
  public Vector orthogonal( Vector b )
  {
    float f = this.dot( b );
    return new Vector( b.x - f*x, b.y - f*y, b.z - f*z );
  }

  // euclidean distance from another point
  float distance( Vector p )
  {
    float a = x - p.x;
    float b = y - p.y;
    float c = z - p.z;
    return FloatMath.sqrt( a*a + b*b + c*c );
  }

  // as 3D point (X,Y,Z) are east, south, vert(down) 
  // Y and Z are reversed in Therion
  void toTherion( PrintWriter pw )
  {
    pw.format( "  %.2f %.2f %.2f\n", x, -y, -z );
  }

  /** The plane of a path is: a0*x + b0*y + c0*z = 1
   * There is an unresolved ambiguity: the normal to the plane could be
   * reversed and the plane still be the same.
   * Need to require that the points are traversed righthand wise,
   * going around the normal.
   */
  static Vector computeVectorsNormal( ArrayList<Vector> pts )
  {
    float x0 = 0.0f;
    float y0 = 0.0f;
    float z0 = 0.0f;
    for ( Vector p : pts ) {
      x0 += p.x;
      y0 += p.y;
      z0 += p.z;
    }
    // note (x0, y0, z0) is the center of mass of the points

    Vector n = new Vector( 0, 0, 0 );
    Vector q = pts.get( pts.size() - 1 );
    for ( Vector p : pts ) {
      n.x += (q.y-y0)*(p.z-z0) - (q.z-z0)*(p.y-y0);
      n.y += (q.z-z0)*(p.x-x0) - (q.x-x0)*(p.z-z0);
      n.z += (q.x-x0)*(p.y-y0) - (q.y-y0)*(p.x-x0);
    }
    n.Normalized();
    return n;
  }
  
  /** compute the mean vector (CoM) of the vectors of an array
   */
  static Vector computeMeanVector( ArrayList<Vector> pts )
  {
    float x0 = 0.0f;
    float y0 = 0.0f;
    float z0 = 0.0f;
    for ( Vector p : pts ) {
      x0 += p.x;
      y0 += p.y;
      z0 += p.z;
    }
    int n = pts.size();
    return new Vector( x0/n, y0/n, z0/n );
  }

  static float computeLength( ArrayList<Vector> pts )
  {
    float d = 0f;
    Vector p0 = pts.get(0);
    int npts = pts.size();
    for ( int n = 1; n<npts; ++n ) {
      Vector p = pts.get( n );
      d += p0.distance( p );
      p0 = p;
    }
    return d;
  }

  /** compute the angle of the projections on a plane of a list of vectors around this vector
   * @param pts2 list of vectors
   * @param normal  normal to the plane
   */
  float angleAroundVectors( ArrayList<Vector> pts2, Vector normal )
  {
    Vector last_point = pts2.get( pts2.size() - 1 );
    Vector w1 = normal.orthogonal( this );
    Vector w0 = w1.minus( last_point );
    float a = 0.0f;
    w0.Normalized();
    for ( Vector p : pts2 ) {
      Vector w2 = w1.minus( p );
      w2.Normalized();
      float s = normal.dot( w0.cross(w2) );
      float c = w0.dot(w2);
      a += (float)Math.atan2( s, c );
      w0 = w2;
    }
    return a;
  }

}
