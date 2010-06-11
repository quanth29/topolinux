/****************************************************************************
** Meta object code from reading C++ file 'QTcalib.h'
**
** Created: Sun May 30 19:58:57 2010
**      by: The Qt Meta Object Compiler version 59 (Qt 4.4.3)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "QTcalib.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'QTcalib.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 59
#error "This file was generated using the moc from 4.4.3. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_CalibTable[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       2,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      31,   12,   11,   11, 0x0a,
      72,   64,   11,   11, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CalibTable[] = {
    "CalibTable\0\0row,col,button,pos\0"
    "cell_clicked(int,int,int,QPoint)\0"
    "section\0header_clicked(int)\0"
};

const QMetaObject CalibTable::staticMetaObject = {
    { &QTABLE::staticMetaObject, qt_meta_stringdata_CalibTable,
      qt_meta_data_CalibTable, 0 }
};

const QMetaObject *CalibTable::metaObject() const
{
    return &staticMetaObject;
}

void *CalibTable::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CalibTable))
        return static_cast<void*>(const_cast< CalibTable*>(this));
    return QTABLE::qt_metacast(_clname);
}

int CalibTable::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QTABLE::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: cell_clicked((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< int(*)>(_a[2])),(*reinterpret_cast< int(*)>(_a[3])),(*reinterpret_cast< const QPoint(*)>(_a[4]))); break;
        case 1: header_clicked((*reinterpret_cast< int(*)>(_a[1]))); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_QTcalibWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
      13,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      15,   14,   14,   14, 0x0a,
      24,   14,   14,   14, 0x0a,
      33,   14,   14,   14, 0x0a,
      41,   14,   14,   14, 0x0a,
      50,   14,   14,   14, 0x0a,
      59,   14,   14,   14, 0x0a,
      68,   14,   14,   14, 0x0a,
      77,   14,   14,   14, 0x0a,
      88,   14,   14,   14, 0x0a,
      97,   14,   14,   14, 0x0a,
     107,   14,   14,   14, 0x0a,
     120,   14,   14,   14, 0x0a,
     140,  132,   14,   14, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_QTcalibWidget[] = {
    "QTcalibWidget\0\0doHelp()\0doQuit()\0"
    "doNew()\0doOpen()\0doSave()\0doEval()\0"
    "doData()\0doToggle()\0doRead()\0doWrite()\0"
    "doCoverage()\0doComment()\0row,col\0"
    "value_changed(int,int)\0"
};

const QMetaObject QTcalibWidget::staticMetaObject = {
    { &QMAINWINDOW::staticMetaObject, qt_meta_stringdata_QTcalibWidget,
      qt_meta_data_QTcalibWidget, 0 }
};

const QMetaObject *QTcalibWidget::metaObject() const
{
    return &staticMetaObject;
}

void *QTcalibWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_QTcalibWidget))
        return static_cast<void*>(const_cast< QTcalibWidget*>(this));
    if (!strcmp(_clname, "DistoXListener"))
        return static_cast< DistoXListener*>(const_cast< QTcalibWidget*>(this));
    return QMAINWINDOW::qt_metacast(_clname);
}

int QTcalibWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QMAINWINDOW::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doHelp(); break;
        case 1: doQuit(); break;
        case 2: doNew(); break;
        case 3: doOpen(); break;
        case 4: doSave(); break;
        case 5: doEval(); break;
        case 6: doData(); break;
        case 7: doToggle(); break;
        case 8: doRead(); break;
        case 9: doWrite(); break;
        case 10: doCoverage(); break;
        case 11: doComment(); break;
        case 12: value_changed((*reinterpret_cast< int(*)>(_a[1])),(*reinterpret_cast< int(*)>(_a[2]))); break;
        }
        _id -= 13;
    }
    return _id;
}
static const uint qt_meta_data_CommentWidget[] = {

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

static const char qt_meta_stringdata_CommentWidget[] = {
    "CommentWidget\0\0doOK()\0doCancel()\0"
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
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_DownloadDialog[] = {

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

static const char qt_meta_stringdata_DownloadDialog[] = {
    "DownloadDialog\0\0doOK()\0doCancel()\0"
};

const QMetaObject DownloadDialog::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_DownloadDialog,
      qt_meta_data_DownloadDialog, 0 }
};

const QMetaObject *DownloadDialog::metaObject() const
{
    return &staticMetaObject;
}

void *DownloadDialog::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_DownloadDialog))
        return static_cast<void*>(const_cast< DownloadDialog*>(this));
    return QDialog::qt_metacast(_clname);
}

int DownloadDialog::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
static const uint qt_meta_data_WriteDialog[] = {

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

static const char qt_meta_stringdata_WriteDialog[] = {
    "WriteDialog\0\0doOK()\0doCancel()\0"
};

const QMetaObject WriteDialog::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_WriteDialog,
      qt_meta_data_WriteDialog, 0 }
};

const QMetaObject *WriteDialog::metaObject() const
{
    return &staticMetaObject;
}

void *WriteDialog::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_WriteDialog))
        return static_cast<void*>(const_cast< WriteDialog*>(this));
    return QDialog::qt_metacast(_clname);
}

int WriteDialog::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
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
    "MyFileDialog\0\0doOK()\0doCancel()\0"
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
        case 0: doOK(); break;
        case 1: doCancel(); break;
        }
        _id -= 2;
    }
    return _id;
}
static const uint qt_meta_data_CoeffWidget[] = {

 // content:
       1,       // revision
       0,       // classname
       0,    0, // classinfo
       1,   10, // methods
       0,    0, // properties
       0,    0, // enums/sets

 // slots: signature, parameters, type, tag, flags
      13,   12,   12,   12, 0x0a,

       0        // eod
};

static const char qt_meta_stringdata_CoeffWidget[] = {
    "CoeffWidget\0\0doOK()\0"
};

const QMetaObject CoeffWidget::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_CoeffWidget,
      qt_meta_data_CoeffWidget, 0 }
};

const QMetaObject *CoeffWidget::metaObject() const
{
    return &staticMetaObject;
}

void *CoeffWidget::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_CoeffWidget))
        return static_cast<void*>(const_cast< CoeffWidget*>(this));
    return QDialog::qt_metacast(_clname);
}

int CoeffWidget::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: doOK(); break;
        }
        _id -= 1;
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
QT_END_MOC_NAMESPACE
