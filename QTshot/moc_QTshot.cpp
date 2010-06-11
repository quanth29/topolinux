/****************************************************************************
** Meta object code from reading C++ file 'QTshot.h'
**
** Created: Sat May 29 19:38:09 2010
**      by: The Qt Meta Object Compiler version 59 (Qt 4.4.3)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "QTshot.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'QTshot.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.4.3. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_QTshotWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
      18,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      14,   13,   13,   13, 0x0a,
      23,   13,   13,   13, 0x0a,
      32,   13,   13,   13, 0x0a,
      41,   13,   13,   13, 0x0a,
      49,   13,   13,   13, 0x0a,
      58,   13,   13,   13, 0x0a,
      67,   13,   13,   13, 0x0a,
      78,   13,   13,   13, 0x0a,
      91,   13,   13,   13, 0x0a,
     103,   13,   13,   13, 0x0a,
     114,   13,   13,   13, 0x0a,
     127,   13,   13,   13, 0x0a,
     136,   13,   13,   13, 0x0a,
     149,   13,   13,   13, 0x0a,
     180,  156,   13,   13, 0x0a,
     229,  214,   13,   13, 0x2a,
     266,  258,   13,   13, 0x0a,
     310,  289,   13,   13, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_QTshotWidget[] = {
    "QTshotWidget\0\0doHelp()\0doQuit()\0"
    "doOpen()\0doNew()\0doSave()\0doData()\0"
    "doExport()\0doExportOK()\0doOptions()\0"
    "doToggle()\0doCollapse()\0doPlan()\0"
    "doExtended()\0do3D()\0block,reversed,vertical\0"
    "doCrossSection(DBlock*,bool,bool)\0"
    "block,reversed\0doCrossSection(DBlock*,bool)\0"
    "row,col\0value_changed(int,int)\0"
    "row,col,btn,mousePos\0"
    "double_clicked(int,int,int,QPoint)\0"
};

const QMetaObject QTshotWidget::staticMetaObject = {
    { &QMAINWINDOW::staticMetaObject, qt_meta_stringdata_QTshotWidget,
      qt_meta_data_QTshotWidget, 0 }
};

const QMetaObject *QTshotWidget::metaObject() const
{
    return &staticMetaObject;
}

void *QTshotWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_QTshotWidget))
        return static_cast<void*>(const_cast< QTshotWidget*>(this));
    if (!strcmp(_clname, "DistoXListener"))
        return static_cast< DistoXListener*>(const_cast< QTshotWidget*>(this));
    if (!strcmp(_clname, "PlotDrawer"))
        return static_cast< PlotDrawer*>(const_cast< QTshotWidget*>(this));
    return QMAINWINDOW::qt_metacast(_clname);
}

int QTshotWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QMAINWINDOW::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doHelp(); break;
        case 1: doQuit(); break;
        case 2: doOpen(); break;
        case 3: doNew(); break;
        case 4: doSave(); break;
        case 5: doData(); break;
        case 6: doExport(); break;
        case 7: doExportOK(); break;
        case 8: doOptions(); break;
        case 9: doToggle(); break;
        case 10: doCollapse(); break;
        case 11: doPlan(); break;
        case 12: doExtended(); break;
        case 13: do3D(); break;
        case 14: doCrossSection((*reinterpret_cast< DBlock*(*)>(_a[1])),(*reinterpret_cast< bool(*)>(_a[2])),(*reinterpret_cast< bool(*)>(_a[3]))); break;
        case 15: doCrossSection((*reinterpret_cast< DBlock*(*)>(_a[1])),(*reinterpret_cast< bool(*)>(_a[2]))); break;
        case 16: value_changed((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< int(*)>(_a[2]))); break;
        case 17: double_clicked((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< int(*)>(_a[2])),(*reinterpret_cast< int(*)>(_a[3])),(*reinterpret_cast< const QPoint(*)>(_a[4]))); break;
        }
        _id -= 18;
    }
    return _id;
}
static const uint qt_meta_data_MyFileDialog[] = {

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

static const char qt_meta_stringdata_MyFileDialog[] = {
    "MyFileDialog\0\0onOK()\0onCancel()\0"
};

const QMetaObject MyFileDialog::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_MyFileDialog,
      qt_meta_data_MyFileDialog, 0 }
};

const QMetaObject *MyFileDialog::metaObject() const
{
    return &staticMetaObject;
}

void *MyFileDialog::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_MyFileDialog))
        return static_cast<void*>(const_cast< MyFileDialog*>(this));
    return QDialog::qt_metacast(_clname);
}

int MyFileDialog::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_SplashWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       3,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      14,   13,   13,   13, 0x0a,
      23,   13,   13,   13, 0x0a,
      32,   13,   13,   13, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_SplashWidget[] = {
    "SplashWidget\0\0doOpen()\0doData()\0"
    "doCancel()\0"
};

const QMetaObject SplashWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_SplashWidget,
      qt_meta_data_SplashWidget, 0 }
};

const QMetaObject *SplashWidget::metaObject() const
{
    return &staticMetaObject;
}

void *SplashWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_SplashWidget))
        return static_cast<void*>(const_cast< SplashWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int SplashWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOpen(); break;
        case 1: doData(); break;
        case 2: doCancel(); break;
        }
        _id -= 3;
    }
    return _id;
}
static const uint qt_meta_data_ToggleWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       3,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      20,   14,   13,   13, 0x0a,
      33,   14,   13,   13, 0x0a,
      47,   13,   13,   13, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_ToggleWidget[] = {
    "ToggleWidget\0\0state\0doCalib(int)\0"
    "doSilent(int)\0doClose()\0"
};

const QMetaObject ToggleWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_ToggleWidget,
      qt_meta_data_ToggleWidget, 0 }
};

const QMetaObject *ToggleWidget::metaObject() const
{
    return &staticMetaObject;
}

void *ToggleWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_ToggleWidget))
        return static_cast<void*>(const_cast< ToggleWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int ToggleWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doCalib((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 1: doSilent((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 2: doClose(); break;
        }
        _id -= 3;
    }
    return _id;
}
static const uint qt_meta_data_OptionsWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      15,   14,   14,   14, 0x0a,
      22,   14,   14,   14, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_OptionsWidget[] = {
    "OptionsWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject OptionsWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_OptionsWidget,
      qt_meta_data_OptionsWidget, 0 }
};

const QMetaObject *OptionsWidget::metaObject() const
{
    return &staticMetaObject;
}

void *OptionsWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_OptionsWidget))
        return static_cast<void*>(const_cast< OptionsWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int OptionsWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_InsertWidget[] = {

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

static const char qt_meta_stringdata_InsertWidget[] = {
    "InsertWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject InsertWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_InsertWidget,
      qt_meta_data_InsertWidget, 0 }
};

const QMetaObject *InsertWidget::metaObject() const
{
    return &staticMetaObject;
}

void *InsertWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_InsertWidget))
        return static_cast<void*>(const_cast< InsertWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int InsertWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_ExitWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      12,   11,   11,   11, 0x0a,
      19,   11,   11,   11, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_ExitWidget[] = {
    "ExitWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject ExitWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_ExitWidget,
      qt_meta_data_ExitWidget, 0 }
};

const QMetaObject *ExitWidget::metaObject() const
{
    return &staticMetaObject;
}

void *ExitWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_ExitWidget))
        return static_cast<void*>(const_cast< ExitWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int ExitWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_DataWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       4,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      12,   11,   11,   11, 0x0a,
      19,   11,   11,   11, 0x0a,
      30,   11,   11,   11, 0x0a,
      45,   11,   11,   11, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_DataWidget[] = {
    "DataWidget\0\0doOK()\0doCancel()\0"
    "doSplay1(bool)\0doSplay2(bool)\0"
};

const QMetaObject DataWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_DataWidget,
      qt_meta_data_DataWidget, 0 }
};

const QMetaObject *DataWidget::metaObject() const
{
    return &staticMetaObject;
}

void *DataWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_DataWidget))
        return static_cast<void*>(const_cast< DataWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int DataWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        case 2: doSplay1((*reinterpret_cast< bool(*)>(_a[1]))); break;
        case 3: doSplay2((*reinterpret_cast< bool(*)>(_a[1]))); break;
        }
        _id -= 4;
    }
    return _id;
}
static const uint qt_meta_data_CommentWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       6,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      15,   14,   14,   14, 0x0a,
      22,   14,   14,   14, 0x0a,
      38,   33,   14,   14, 0x0a,
      64,   57,   14,   14, 0x0a,
      83,   78,   14,   14, 0x0a,
      95,   14,   14,   14, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CommentWidget[] = {
    "CommentWidget\0\0doOK()\0doCancel()\0text\0"
    "doComment(QString)\0extend\0doExtend(int)\0"
    "flag\0doFlag(int)\0doSwap(bool)\0"
};

const QMetaObject CommentWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CommentWidget,
      qt_meta_data_CommentWidget, 0 }
};

const QMetaObject *CommentWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CommentWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CommentWidget))
        return static_cast<void*>(const_cast< CommentWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CommentWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        case 1: doCancel(); break;
        case 2: doComment((*reinterpret_cast< const QString(*)>(_a[1]))); break;
        case 3: doExtend((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 4: doFlag((*reinterpret_cast< int(*)>(_a[1]))); break;
        case 5: doSwap((*reinterpret_cast< bool(*)>(_a[1]))); break;
        }
        _id -= 6;
    }
    return _id;
}
static const uint qt_meta_data_SurveyInfoWidget[] = {

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

static const char qt_meta_stringdata_SurveyInfoWidget[] = {
    "SurveyInfoWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject SurveyInfoWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_SurveyInfoWidget,
      qt_meta_data_SurveyInfoWidget, 0 }
};

const QMetaObject *SurveyInfoWidget::metaObject() const
{
    return &staticMetaObject;
}

void *SurveyInfoWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_SurveyInfoWidget))
        return static_cast<void*>(const_cast< SurveyInfoWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int SurveyInfoWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_CleanShotsWidget[] = {

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

static const char qt_meta_stringdata_CleanShotsWidget[] = {
    "CleanShotsWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject CleanShotsWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CleanShotsWidget,
      qt_meta_data_CleanShotsWidget, 0 }
};

const QMetaObject *CleanShotsWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CleanShotsWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CleanShotsWidget))
        return static_cast<void*>(const_cast< CleanShotsWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CleanShotsWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_CenterlineWidget[] = {

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

static const char qt_meta_stringdata_CenterlineWidget[] = {
    "CenterlineWidget\0\0doOK()\0doCancel()\0"
};

const QMetaObject CenterlineWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CenterlineWidget,
      qt_meta_data_CenterlineWidget, 0 }
};

const QMetaObject *CenterlineWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CenterlineWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CenterlineWidget))
        return static_cast<void*>(const_cast< CenterlineWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CenterlineWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
