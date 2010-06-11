/****************************************************************************
** Meta object code from reading C++ file 'PlotCanvas.h'
**
** Created: Sat May 29 19:38:14 2010
**      by: The Qt Meta Object Compiler version 59 (Qt 4.4.3)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "PlotCanvas.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'PlotCanvas.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.4.3. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_PlotCanvas[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
      15,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      12,   11,   11,   11, 0x0a,
      21,   11,   11,   11, 0x0a,
      31,   11,   11,   11, 0x0a,
      42,   11,   11,   11, 0x0a,
      54,   11,   11,   11, 0x0a,
      67,   11,   11,   11, 0x0a,
      79,   11,   11,   11, 0x0a,
      88,   11,   11,   11, 0x0a,
      97,   11,   11,   11, 0x0a,
     109,   11,   11,   11, 0x0a,
     118,   11,   11,   11, 0x0a,
     132,   11,   11,   11, 0x0a,
     147,   11,   11,   11, 0x0a,
     159,   11,   11,   11, 0x0a,
     172,   11,   11,   11, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_PlotCanvas[] = {
    "PlotCanvas\0\0onUndo()\0onSplay()\0"
    "onZoomIn()\0onZoomOut()\0onClearTh2()\0"
    "onSaveTh2()\0onMode()\0onGrid()\0onNumbers()\0"
    "onQuit()\0onThetaPlus()\0onThetaMinus()\0"
    "onPhiPlus()\0onPhiMinus()\0update()\0"
};

const QMetaObject PlotCanvas::staticMetaObject = {
    { &QMAINWINDOW::staticMetaObject, qt_meta_stringdata_PlotCanvas,
      qt_meta_data_PlotCanvas, 0 }
};

const QMetaObject *PlotCanvas::metaObject() const
{
    return &staticMetaObject;
}

void *PlotCanvas::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_PlotCanvas))
        return static_cast<void*>(const_cast< PlotCanvas*>(this));
    return QMAINWINDOW::qt_metacast(_clname);
}

int PlotCanvas::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QMAINWINDOW::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: onUndo(); break;
        case 1: onSplay(); break;
        case 2: onZoomIn(); break;
        case 3: onZoomOut(); break;
        case 4: onClearTh2(); break;
        case 5: onSaveTh2(); break;
        case 6: onMode(); break;
        case 7: onGrid(); break;
        case 8: onNumbers(); break;
        case 9: onQuit(); break;
        case 10: onThetaPlus(); break;
        case 11: onThetaMinus(); break;
        case 12: onPhiPlus(); break;
        case 13: onPhiMinus(); break;
        case 14: update(); break;
        }
        _id -= 15;
    }
    return _id;
}
static const uint qt_meta_data_CanvasCommandWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       4,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      21,   20,   20,   20, 0x0a,
      28,   20,   20,   20, 0x0a,
      41,   39,   20,   20, 0x0a,
      54,   39,   20,   20, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CanvasCommandWidget[] = {
    "CanvasCommandWidget\0\0doOK()\0doCancel()\0"
    "i\0doPoint(int)\0doLine(int)\0"
};

const QMetaObject CanvasCommandWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CanvasCommandWidget,
      qt_meta_data_CanvasCommandWidget, 0 }
};

const QMetaObject *CanvasCommandWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CanvasCommandWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CanvasCommandWidget))
        return static_cast<void*>(const_cast< CanvasCommandWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CanvasCommandWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        case 2: doPoint((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 3: doLine((*reinterpret_cast< int(*)>(_a[1]))); break;
        }
        _id -= 4;
    }
    return _id;
}
static const uint qt_meta_data_MyFileDialogCV[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      16,   15,   15,   15, 0x0a,
      23,   15,   15,   15, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_MyFileDialogCV[] = {
    "MyFileDialogCV\0\0onOK()\0onCancel()\0"
};

const QMetaObject MyFileDialogCV::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_MyFileDialogCV,
      qt_meta_data_MyFileDialogCV, 0 }
};

const QMetaObject *MyFileDialogCV::metaObject() const
{
    return &staticMetaObject;
}

void *MyFileDialogCV::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_MyFileDialogCV))
        return static_cast<void*>(const_cast< MyFileDialogCV*>(this));
    return QDialog::qt_metacast(_clname);
}

int MyFileDialogCV::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: onOK(); break;
        case 1: onCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_ExtendWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      14,   13,   13,   13, 0x0a,
      21,   13,   13,   13, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_ExtendWidget[] = {
    "ExtendWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject ExtendWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_ExtendWidget,
      qt_meta_data_ExtendWidget, 0 }
};

const QMetaObject *ExtendWidget::metaObject() const
{
    return &staticMetaObject;
}

void *ExtendWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_ExtendWidget))
        return static_cast<void*>(const_cast< ExtendWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int ExtendWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_CleanScrapWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      18,   17,   17,   17, 0x0a,
      25,   17,   17,   17, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CleanScrapWidget[] = {
    "CleanScrapWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject CleanScrapWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CleanScrapWidget,
      qt_meta_data_CleanScrapWidget, 0 }
};

const QMetaObject *CleanScrapWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CleanScrapWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CleanScrapWidget))
        return static_cast<void*>(const_cast< CleanScrapWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CleanScrapWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_ScrapWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      13,   12,   12,   12, 0x0a,
      20,   12,   12,   12, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_ScrapWidget[] = {
    "ScrapWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject ScrapWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_ScrapWidget,
      qt_meta_data_ScrapWidget, 0 }
};

const QMetaObject *ScrapWidget::metaObject() const
{
    return &staticMetaObject;
}

void *ScrapWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_ScrapWidget))
        return static_cast<void*>(const_cast< ScrapWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int ScrapWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_LabelWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      13,   12,   12,   12, 0x0a,
      20,   12,   12,   12, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_LabelWidget[] = {
    "LabelWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject LabelWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_LabelWidget,
      qt_meta_data_LabelWidget, 0 }
};

const QMetaObject *LabelWidget::metaObject() const
{
    return &staticMetaObject;
}

void *LabelWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_LabelWidget))
        return static_cast<void*>(const_cast< LabelWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int LabelWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_StationCommentWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      22,   21,   21,   21, 0x0a,
      29,   21,   21,   21, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_StationCommentWidget[] = {
    "StationCommentWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject StationCommentWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_StationCommentWidget,
      qt_meta_data_StationCommentWidget, 0 }
};

const QMetaObject *StationCommentWidget::metaObject() const
{
    return &staticMetaObject;
}

void *StationCommentWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_StationCommentWidget))
        return static_cast<void*>(const_cast< StationCommentWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int StationCommentWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
QT_END_MOC_NAMESPACE
