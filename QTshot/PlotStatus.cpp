/** @file PlotStatus.cpp
 *
 * @author marco corvi
 * @date dec 2009
 *
 * @brief plot status
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
#include "PlotStatus.h"

#include "PlotScale.h"

#ifdef HAS_POCKETTOPO
void
PlotStatus::exportPTfile( PTdrawing & drawing )
{
  #include "../PTopo/PTexportPS.impl"
}
#endif


