

CC = g++
CFLAGS = -g -O2 -Wall

OBJS = \
  config.o \
  Language.o \
  GetDate.o 

default: $(OBJS)

.o: .cpp
	$(CC) $(CFLAGS) -o $@ -c $^

clean:
	rm -f *.o 

