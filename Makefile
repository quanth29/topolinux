
# -------------------------------------
#  DIRS

MAKE_DIRS = \
  distox \
  basic \
  calib \
  utils \
  QTshot \
  QTcalib

EXTRA_MAKE_DIRS = \
  warp \
  PTopo \
  experimental

QTOPO_DIRS = \
  bin \
  i18n \
  help

DIST_DIRS = $(MAKE_DIRS) $(QTOPO_DIRS) \
  pixmaps \
  icons 

TAR_DIRS = $(DIST_DIRS) \
  $(EXTRA_MAKE_DIRS) 

# ------------------------------------
#  FILES

QTOPO_FILES = \
  rfcomm.sh \
  COPYING \
  AUTHOR \
  VERSION \
  qtopo.rc

DIST_FILES = $(QTOPO_FILES) \
  Makefile \
  CHANGES \
  README \
  TODO

TAR_FILES = $(DIST_FILES) \
  reduce.pl

# ---------------------------------------

default:
	- echo "Possible targets: tar qtopo topolinux all clean distclean"

all:
	for i in $(MAKE_DIRS); do cd $$i; make; cd - ; done

clean:	 
	for i in $(MAKE_DIRS); do cd $$i; make clean; cd - ; done

distclean:
	for i in $(MAKE_DIRS); do cd $$i; make distclean; cd - ; done
	rm -f bin/*

tar:
	make distclean
	tar -czf ./disto.tgz $(TAR_DIRS) $(TAR_FILES)

qtopo: 
	make all
	tar -czf ./qtopo.tgz $(QTOPO_DIRS) $(QTOPO_FILES)

topolinux: 
	make distclean
	tar -czf ./topolinux.tgz $(DIST_DIRS) $(DIST_FILES)

