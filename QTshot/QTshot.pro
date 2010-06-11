######################################################################
# Automatically generated by qmake (1.07a) Sat May 16 10:42:24 2009
######################################################################

CONFIG += qt

QT += qt3support

TEMPLATE = app
INCLUDEPATH += . ../distox ../utils 
# INCLUDEPATH += ../PTopo
# INCLUDEPATH += ../warp

# DEFINES += QT3_SUPPORT
# DEFINES += HAS_POCKETTOPO
# DEFINES += HAS_LRUD
# DEFINES += HAS_BACKIMAGE

# Input
HEADERS += QTshot.h Locale.h IconSet.h Units.h ExportType.h ArgCheck.h
HEADERS += DBlock.h DataList.h Num.h Extend.h SplayAt.h
HEADERS += Plot.h PlotCanvas.h PlotDrawer.h PlotScale.h PLotFrame.h
HEADERS += PlotThExport.h
HEADERS += DataThExport.h DataDatExport.h DataSvxExport.h DataTopExport.h
HEADERS += CanvasMode.h CanvasPoint.h CanvasSegment.h
HEADERS += BackgroundImage.h ImageTransform.h
HEADERS += TherionPoint.h TherionLine.h ThPointType.h ThLineType.h PTcolors.h

# BufferQueue.h  Factors.h Protocol.h Vector.h Matrix.h Serial.h DistoX.h
# PTfile.h 

SOURCES += DataList.cpp Plot.cpp Num.cpp IconSet.cpp CanvasPoint.cpp
SOURCES += PlotCanvas.cpp BackgroundImage.cpp ImageTransform.cpp QTshot.cpp 
SOURCES += PlotFrame.cpp PlotStatus.cpp Locale.cpp Units.cpp  PlotThExport.cpp
SOURCES += DataThExport.cpp DataDatExport.cpp DataSvxExport.cpp DataTopExport.cpp

OBJECTS += ../distox/Vector.o \
           ../distox/Matrix.o \
           ../distox/Protocol.o \
           ../distox/Serial.o
OBJECTS += ../utils/config.o \
           ../utils/Language.o \
           ../utils/GetDate.o
# OBJECTS += ../PTopo/PTfile.o
# OBJECTS += ../warp/thinfnan.o \
#            ../warp/thtrans.o \
#            ../warp/thwarppme.o \
#            ../warp/thwarppt.o 

TARGET = ../bin/qtshot

