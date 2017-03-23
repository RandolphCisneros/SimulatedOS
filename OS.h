#ifndef OS_H
#define OS_H

//header file for my OS simulation project.
#include <iostream>
using namespace std;

class OS {
	private:
		bool powerOff;

	public:
		OS();
		~OS();

		void run();
		void shutdown();
};
#endif