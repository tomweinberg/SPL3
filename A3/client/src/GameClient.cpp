/*
 * GameClient.cpp
 *
 *  Created on: Jan 11, 2016
 *      Author: tomwein
 */

#include "../include/GameClient.h"
#include <stdlib.h>
#include <boost/locale.hpp>
#include "../include/ConnectionHandler.h"
#include "../encoder/utf8.h"
#include "../encoder/encoder.h"
#include <boost/thread.hpp>
#include <iostream>
#include <string>
#include <iostream>

using namespace std;
using namespace boost;


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
bool notconnected;            //~ shouldendforsure
bool needToClose;          // shouldend

void run(ConnectionHandler *connectionHandler){
  while(!notconnected){
	const short bufsize = 1024;
	char buf[bufsize];
	if(!needToClose){
	  cin.getline(buf, bufsize);
	  string line(buf);
	  if(line.length()>0){
	      if (!(*connectionHandler).sendLine(line)) {
		    std::cout << "Disconnected. Exiting...\n" << std::endl;
		    //connected=false;
		    break;
	      }
	    if(line =="QUIT"){
		needToClose= true;
	    }
	  }
	}
  }
}


int main (int argc, char *argv[]) {
   if (argc < 2) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host =argv[1];
    unsigned short port = atoi(argv[2]);

    ConnectionHandler* connectionHandler=new ConnectionHandler(host, port);
    if (!(*connectionHandler).connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    notconnected=false;
    needToClose=false;
  //  run(connectionHandler);
    boost::thread sendingThread(run, connectionHandler);


    while (1) {
        string answer;
        if (!(*connectionHandler).getLine(answer)) {
        	notconnected=true;
		std::cout << "Disconnected. Exiting...\n" << std::endl;
		break;
        }
        answer.resize(answer.length()-1);
        std::cout << answer  << std::endl;
        if(answer=="SYSMSG QUIT ACCEPTED"){
		notconnected=true;
		(*connectionHandler).close();
		break;
        }
        needToClose=false;
    }
    sendingThread.join();
    delete connectionHandler;

    return 0;
}
