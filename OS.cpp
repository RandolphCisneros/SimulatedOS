#include "OS.h"

OS::OS() {
	powerOff = false;
}

OS::~OS(){}


void OS::run(){
	std::cout << "OS is running" << endl;
}

void OS::shutdown(){
	std:: cout << "OS is shutting down" << endl;
	powerOff = true;
}