######################################################################
# Automatically generated by qmake (1.07a) Sat May 16 21:14:00 2009
######################################################################

CONFIG += qt

# QT += qt3support

TEMPLATE = app
INCLUDEPATH += . ../distox ../utils ../Therion

# DEFINES += EXPERIMENTAL

# Input
HEADERS += CalibList.h CBlock.h CTransform.h \
           Coverage.h QTcalib.h \
           Locale.h IconSet.h Calibration.h TherionPoint.h 

# Matrix.h Vector.h Factors.h
# BufferQueue.h Serial.h Protocol.h DistoX.h
# config.h

SOURCES += CalibList.cpp CTransform.cpp \
           Coverage.cpp IconSet.cpp \
           Locale.cpp QTcalib.cpp Calibration.cpp 

OBJECTS += ../distox/Vector.o \
        ../distox/Matrix.o \
        ../distox/Serial.o \
        ../distox/Protocol.o \
        ../utils/Language.o \
        ../utils/GetDate.o \
        ../utils/config.o \
        ../Therion/ThPointType.o

# Matrix.cpp Vector.cpp 
# Serial.cpp Protocol.cpp

TARGET = ../bin/qtcalib
