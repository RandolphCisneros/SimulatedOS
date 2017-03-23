#include <iostream>
#include <string>
#include "OS.h"

#using namespace std;

int main(){
	OS testOS;
	string command = "";
	
	do {
		testOS.run();
		std::cout << "Type exit to quit" << endl;
		std::cin >> command;
	} while (command != "exit");	//make sure this test case works

	testOS.shutdown();
	~testOS();

	


	return 0;
}