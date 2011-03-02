######################################################################
# Automatically generated by qmake (1.07a) Sat May 16 10:42:24 2009
######################################################################

CONFIG += qt

# QT += qt3support

TEMPLATE = app
INCLUDEPATH += . ../distox ../utils 
INCLUDEPATH += ../PTopo
# INCLUDEPATH += ../warp

# DEFINES += QT3_SUPPORT
DEFINES += HAS_POCKETTOPO
# DEFINES += HAS_LRUD
DEFINES += HAS_BACKIMAGE

# Input
HEADERS += QTshot.h Locale.h IconSet.h Units.h ExportType.h ArgCheck.h
HEADERS += DBlock.h DataList.h Num.h Extend.h SplayAt.h
HEADERS += Plot.h PlotCanvas.h PlotDrawer.h PlotScale.h 
HEADERS += PlotThExport.h PlotCanvasScene.h PlotList.h
HEADERS += DataThExport.h DataDatExport.h DataSvxExport.h DataTopExport.h
HEADERS += CanvasMode.h PlotPoint.h PlotSegment.h CanvasExtend.h CanvasUndo.h
HEADERS += BackgroundImage.h ImageTransform.h BackgroundImageStation.h
HEADERS += BackgroundImageStationSet.h
HEADERS += TherionPoint.h TherionLine.h TherionArea.h TherionScrap.h
HEADERS += ThPointType.h ThLineType.h ThAreaType.h PTcolors.h

# BufferQueue.h  Factors.h Protocol.h Vector.h Matrix.h Serial.h DistoX.h
# PTfile.h PLotFrame.h 

SOURCES += DataList.cpp Plot.cpp Num.cpp IconSet.cpp PlotPoint.cpp
SOURCES += PlotCanvas.cpp QTshot.cpp CanvasExtend.cpp PlotCanvasScene.cpp
SOURCES += PlotStatus.cpp Locale.cpp Units.cpp  PlotThExport.cpp PlotList.cpp
SOURCES += ThPointType.cpp ThLineType.cpp ThAreaType.cpp TherionScrap.cpp
SOURCES += DataThExport.cpp DataDatExport.cpp DataSvxExport.cpp DataTopExport.cpp
SOURCES += BackgroundImage.cpp ImageTransform.cpp 

# PlotFrame.cpp 

OBJECTS += ../distox/Vector.o \
           ../distox/Matrix.o \
           ../distox/Protocol.o \
           ../distox/Serial.o
OBJECTS += ../utils/config.o \
           ../utils/Language.o \
           ../utils/GetDate.o
OBJECTS += ../PTopo/PTfile.o
# OBJECTS += ../warp/thinfnan.o \
#            ../warp/thtrans.o \
#            ../warp/thwarppme.o \
#            ../warp/thwarppt.o 

TARGET = ../bin/qtshot

