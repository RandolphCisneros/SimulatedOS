#include <iostream>

void siodisk(int jobnum);

void soidrum(int jobnum, int jobsize, int coreaddress, int direction);

void ontrace();
void offtrace();

//initializes static variables; only called at start of simulation
void startup(){
	std::cout << "Starting up" << endl;
}

void Crint(int &a, int p[]){
	std::cout << "In Crint" << endl;
}

void Dskint(int &a, int p[]){
	std::cout << "In dskint" << endl;
}

void Tro(int &a, int p[]){
	std::cout << "in tro" << endl;
}

void Svc(int &a, int p[]){
	std::cout << "in svc" << endl;
}

