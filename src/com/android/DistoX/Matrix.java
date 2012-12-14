/* @file Matrix.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3x3 matrix
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.android.DistoX;

public class Matrix
{ 
  public Vector x,y,z;
 
  public static Matrix zero = new Matrix( Vector.zero, Vector.zero, Vector.zero );

  public static Matrix one = new Matrix( new Vector(1.0f, 0.0f, 0.0f),
                                         new Vector(0.0f, 1.0f, 0.0f),
                                         new Vector(0.0f, 0.0f, 1.0f) );

  // Default cstr: zero matrix
  public Matrix()
  {
    x = new Vector();
    y = new Vector();
    z = new Vector();
  }

  public Matrix( Vector x0, Vector y0, Vector z0 ) 
  {
    x = new Vector(x0);
    y = new Vector(y0);
    z = new Vector(z0);
  }

  // OUTER PRODUCT: a & b
  public Matrix( Vector a, Vector b ) 
  {
    x = b.mult(a.x);
    y = b.mult(a.y);
    z = b.mult(a.z);
  }

  public Matrix( Matrix a )
  {
    x = new Vector( a.x );
    y = new Vector( a.y );
    z = new Vector( a.z );
  }

  public void add( Matrix b ) 
  {
    x.add( b.x );
    y.add( b.y );
    z.add( b.z );
  }
  public Matrix plus( Matrix b )
  {
    return new Matrix( x.plus(b.x), y.plus(b.y), z.plus(b.z) );
  }

  public Matrix minus( Matrix b )
  {
    return new Matrix( x.minus(b.x), y.minus(b.y), z.minus(b.z) );
  }

  public Matrix mult( float b )
  {
    return new Matrix( x.mult(b), y.mult(b), z.mult(b) );
  }

  public Vector times( Vector b )
  {
    return new Vector( x.dot(b), y.dot(b), z.dot(b) );
  }

  public Matrix times( Matrix b )
  {
    return new Matrix( b.times(x), b.times(y), b.times(z) );
  }

  public Matrix Inverse()
  {
    Matrix ad = new Matrix( y.cross(z), z.cross(x), x.cross(y) );
    float inv_det = 1.0f / ( x.dot( ad.x ) );
    return ad.mult( inv_det );
  }

  public float MaxDiff( Matrix b )
  {
    float dx = x.MaxDiff( b.x );
    float dy = y.MaxDiff( b.y );
    float dz = z.MaxDiff( b.z );
    if ( dx < dy ) { dx = dy; }
    if ( dx < dz ) { dx = dz; }
    return dx;
  }

}
