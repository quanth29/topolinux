/* @file INewPlot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid NewPlot interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 */
package com.android.DistoX;

public interface INewPlot 
{
  public void makeNewPlot( String name, long type, String start, String view );

  public void makeNewSketch3d(  String name, String start, String next );

}


