CFLAGS:=-c -Wall -Weffc++ -g
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: bin/encoder.o TClient
	g++ -o bin/TheClient bin/encoder.o bin/ConnectionHandler.o bin/GameClient.o $(LDFLAGS) 

TClient: bin/ConnectionHandler.o bin/GameClient.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/GameClient.o: src/GameClient.cpp
	g++ $(CFLAGS) -o bin/GameClient.o src/GameClient.cpp
	
bin/encoder.o: encoder/encoder.cpp
	g++ $(CFLAGS) -o bin/encoder.o encoder/encoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
