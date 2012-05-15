
# -------------------------------------
#  DIRS

MAKE_DIRS = \
  distox \
  basic \
  calib \
  memory \
  utils \
  Therion \
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

IMAGE_DIRS = \
  pixmaps \
  icons 

DIST_DIRS = $(MAKE_DIRS) $(QTOPO_DIRS) $(IMAGE_DIRS)

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

TAR_FILES = $(DIST_FILES) 

# ---------------------------------------

default:
	- echo "Possible targets: tar qtopo topolinux all clean distclean"
	- echo "  tar       a complete tar with all sources"
	- echo "  topolinux a tar with the distributable sources"
	- echo "  qtopo     the gui programs"
	- echo "  all       all the programs"
	- echo "  runtime   a tar of the runtime"
	- echo "  clean     a cleanup"
	- echo "  distclean a cleanup for distributing"

all:
	for i in $(EXTRA_MAKE_DIRS); do cd $$i; make; cd - ; done
	for i in $(MAKE_DIRS); do cd $$i; make; cd - ; done

clean:	 
	for i in $(MAKE_DIRS); do cd $$i; make clean; cd - ; done
	for i in $(EXTRA_MAKE_DIRS); do cd $$i; make clean; cd - ; done

distclean:
	for i in $(MAKE_DIRS); do cd $$i; make distclean; cd - ; done
	for i in $(EXTRA_MAKE_DIRS); do cd $$i; make distclean; cd - ; done
	rm -f bin/*

tar:
	make distclean
	tar -czf ./disto.tgz $(TAR_DIRS) $(TAR_FILES)

qtopo: 
	make all
	tar -chzf ./qtopo.tgz $(QTOPO_DIRS) $(QTOPO_FILES)

runtime:
	tar -chzf ./runtime.tgz $(QTOPO_DIRS) $(IMAGE_DIRS) $(DIST_FILES)

topolinux: 
	make distclean
	tar --exclude="\.svn" -chzf ./topolinux.tgz $(DIST_DIRS) $(DIST_FILES)

