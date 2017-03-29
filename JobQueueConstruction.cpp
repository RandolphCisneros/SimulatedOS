//This constructs the job queue for Crint to push a job onto the queue
//we include list because the underlying structure of the queue will be a list
#include <list>
#include <queue>

//I want to use pair for the free space table; maybe a vector of pairs
#include <utility> 				//for pairs

	//constructs jobqueue. jobqueue should be same as readyqueue, just ask
	std::queue<int> jobQueue = new std::queue<int>;
	std::queue<int> readyQueue = new std::queue<int>;
	
	//now figure out how to construct free space table; WORST FIT first
	std::pair <int, int> sizeAndAddress;	//maybe use make_pair

//job comes in interrupt
void Crint(int &a, int p[]){
	//add job to job queue?
	jobqueue.push(a); //need jobqueue data structure in OS
	//MVP; find space for job on free space table
	//we are using WORST FIT algorithm
	//jobqueue += a;
}
