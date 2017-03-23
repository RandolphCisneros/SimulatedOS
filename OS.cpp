#include "OS.h"

//initialize powerOff to false
OS::OS() {
	powerOff = false;
}

OS::~OS(){}

//simple message while it is running. More methods will come in here; this constantly loops and "listens" for interrupts
void OS::run(){
	std::cout << "OS is running" << endl;
}

//This sets poweroff to true. I don't know if I want this outside of run or inside.
void OS::shutdown(){
	std:: cout << "OS is shutting down" << endl;
	powerOff = true;
}
