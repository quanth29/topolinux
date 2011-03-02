

# CC = arm-linux-g++
# STRIP = arm-linux-strip
CC = g++
STRIP = strip

CFLAGS = -g -O2 -Wall -I../distox -I../basic

EXES = \
  tlx_dump_memory \
  tlx_clear_memory \
  tlx_set_memory \
  tlx_reset_memory \
  tlx_read_queue

SERIAL_OBJS = \
  ../distox/Serial.o

DISTOX_OBJS = \
  ../distox/Serial.o \
  ../distox/Protocol.o

VECTOR_OBJS = \
  ../distox/Vector.o \
  ../distox/Matrix.o

default: $(EXES)

all: $(EXES)


tlx_dump_memory: dump_memory.cpp $(SERIAL_OBJS)
	$(CC) $(CFLAGS) -o $@ $^ 
	$(STRIP) $@

tlx_clear_memory: clear_memory.cpp $(SERIAL_OBJS)
	$(CC) $(CFLAGS) -o $@ $^
	$(STRIP) $@

tlx_reset_memory: reset_memory.cpp $(SERIAL_OBJS)
	$(CC) $(CFLAGS) -o $@ $^
	$(STRIP) $@

tlx_set_memory: set_memory.cpp $(SERIAL_OBJS)
	$(CC) $(CFLAGS) -o $@ $^
	$(STRIP) $@

tlx_read_queue: read_queue.cpp $(SERIAL_OBJS)
	$(CC) $(CFLAGS) -o $@ $^
	$(STRIP) $@

clean:
	rm -f *.o $(EXES)

distclean:
	rm -f *.o $(EXES)

