import java.util.*;

public class os {

	private static SizeAddressTable addressTable;	//My addressTable. Holds free space and assigned jobs
	public static LinkedList<Job> jobTable;		//I made this global solely because it said to in the handout.
	private static Queue<Job> readyQueue;		//These are jobs onCore and ready to run.
	private static List<Job> waitingQueue;		//This waiting queue is for if the drum is busy or there's no space.
	private static Queue<Job> iOQueue;		//This is the I/O queue to be used when I/O jobs want to be blocked.

	private static Job jobToRun;			//This is the current job to run in dispatcher
	private static Job jobCompletingIO;		//This is the job finishing I/O in Dskint
	private static Job jobRequestingService;	//This is the job calling for service
	private static Job jobForDrum;			//Tracks job requesting drum service.
	private static Job swapIn;			//Tracks job swapping in
	private static Job swapOut;			//Tracks job swapping out
	private static Job jobForDisk;
	
	private static int jobsOnCore;			//Used to track whether there's an empty core.
	private static int transferDirection;		//Used to hold last transferDirection for Drumint
	private static int totalTime;			//Holds the time from start to finish
	private static int timeElapsed;			//Holds the amount of time elapsed from last run to last interrupt
	private static int lastCurrentTime;		//Holds the time from the last interrupt
	
	private static boolean drumBusy;		//Flags if drum in use
	private static boolean diskBusy;		//Flags if disk in use
	private static boolean swappingIn;		//Flags if there's a job waiting to swap in
	private static boolean swappingOut;		//Flags if there's a job waiting to swap out
	private static boolean swapping;		//Flags if there's a general swapping process occurring.
	
	//This is to initialize static variables. All variables must be static for the static functions.
	public static void startup(){
		initializeContainers();
		initializeVariables();
		initializeJobObjects();
	}

	//A new job has entered the system anc caused this interrupt.
	//This method receives job info. It creates a "Job" instance, and then attempts to assign it an address.
	//If successful, it is added to the ReadyQueue; otherwise it is added to the waiting queue.
	//Regardless it is added to our main jobTable.
	public static void Crint(int[]a, int[]p){
		getTimeElapsed(p);				//1. Set elapsed time.
		setRunningJobTime();				//2. Set last running Job's time, if any. Other checks done.
		
		Job newestJob = new Job(p[1],p[2],p[3],p[4]);	//3. Assign input to newestJob.
		if ((!drumBusy && waitingQueue.isEmpty()) && addressTable.assignJob(newestJob)){//4a. Check drumBusy, freeSpace, waitingQueue. If so, get address.
			transferDirection = 0;							//5a. Set transferDirection for drum-to-Core. It holds this.
			sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), transferDirection);	//6a. Puts job on core
			drumBusy = true;							//7a. Flag drum as busy 	
			jobForDrum = newestJob;							//8a. Set the jobForDrum as the newestJob	
		}
		else
			waitingQueue.add(newestJob);	//4b. If not, then it gets put on the waitingQueue.
		jobTable.add(newestJob);		//5. Push onto jobTable	
		dispatcher(a, p);			//6. Call dispatcher
		return;
	}

	//A job has completed I/O and generated this interrupt.
	//The job at the front of the I/O queue is removed and assigned to jobCompletingIO.
	//It is then added to the readyQueue.
	//Lastly dispatcher is called and the job in front of the readyQueue is run.
	public static void Dskint (int[]a, int[]p){
		getTimeElapsed(p);	//1. Get time elapsed
		setRunningJobTime();	//2. Set running job time.
		
		jobCompletingIO = iOQueue.remove();	//3. Remove from IOQueue
		jobCompletingIO.setIOFlag(false);	//4. Set IOFlag for job as false
		diskBusy = false;			//5. Set diskBusy flag as false
		if(jobCompletingIO.getTimeFinished()){	//6a. Check if Job is finished. If so, it will be removed from core.
			jobsOnCore -= 1;		//7a. Decrement jobsOnCore
			addressTable.removeJob(jobCompletingIO);	//8a. Remove finished job from address table
		}
		else if (jobCompletingIO.getBlockFlag()){	//6b. Check if job was blocked.
			jobCompletingIO.setBlockFlag(false);	//7b. If it was, we can set the blockFlag to false.
			readyQueue.add(jobCompletingIO);	//8b. Put back on readyQueue.
		}
		dispatcher(a,p);	//7. Call dispatcher	
	}

	//The drum has finished either putting a job on the core or taking it out, generating this interrupt.
	//It sets the drumBusy flag as false, checks the direction of transfer,
	//and either adds it to the waitingQueue or the readyQueue, depending on direction.
	public static void Drmint (int[]a, int[]p){
		getTimeElapsed(p);		//1. Get elapsed time
		setRunningJobTime();		//2. Set running job time
		
		drumBusy = false;		//3. Set drumBusy to false.	
		if (transferDirection == 0)			//4a. Transfer direction from drum-to-core
			coreIn();
		else if (transferDirection == 1)		//4b. Transfer direction from core-to-drum
			coreOut();
		dispatcher(a,p);
	}
	
	//Timer run out. It checks the elapsed time at the start and then sets the time for the last running job.
	public static void Tro (int[]a, int[]p){
		getTimeElapsed(p);	//1. Set time elapsed.
		setRunningJobTime();	//2. Set job time.
		
		//The time finished flag is checked in the checkTimeOut() method in setRunningJobTime(). 
		//If there is no I/O then we decrement jobsOnCore. Removal from addressTable is done in Drumint
		if(jobToRun.getTimeFinished() && !jobToRun.getIOFlag())
			jobsOnCore -=1;
		dispatcher(a,p);
	}
	
	//Service interrupt. Job signals for service and O.S. acts accordingly.
	public static void Svc (int[]a, int[]p){
		getTimeElapsed(p);			//1. Set timeElapsed
		setRunningJobTime();			//2. Set running time for job
		
		jobRequestingService = jobToRun;	//3. Assign jobRequestingService
		if (a[0] == 5)				//4a. It requests termination
			terminateService();
		else if (a[0] == 6) 			//4b. It requests disk i/o. Goes to iOService method.
			iOService();
		else					//4c. a[0] == 7, job wants to be blocked for i/o
			blockService();
		dispatcher(a,p);	//Last, call dispatcher.
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////END OF INTERRUPTS/////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Dispatcher is its own function. We check disk status, drum status,
	//and readyQueue status to see what operations we should do.
	public static void dispatcher(int[]a, int[]p){
		checkDrum();	//1. Check if we can/should do drum operations
		checkDisk();	//2. Check if we can/should do disk operations	
				
		if (jobsOnCore == 0)	//3a. If core is empty, do wait.
			a[0] = 1;		//4a. Set a to 1 if there are no jobs on core
		else if (!readyQueue.isEmpty()){	//3b. If there are jobs on the core, check if they're ready.
			a[0] = 2;			//4b. Set a[0] to 2
			jobToRun = readyQueue.poll();	//5b. Set job to run to job in front of ready queue.
			p[2]  = jobToRun.getJobAddress();	//6b. Set p[2] to address of job to run
			p[3] = jobToRun.getJobSize();		//7b. Set p[3] to size of job to run
			p[4] = jobToRun.getTimeSlice();			//8b. Set time slice.
			readyQueue.add(jobToRun);	//9b. Add job to back of queue for after service finish.
		}
		else	//3c. If a job is blocked and on the core, and there are no unblocked jobs on the core, we go to this.
			a[0] = 1;					//4c. Keep idle
	}
	
	//Method to set timeElapsed. Don't return anything because timeElapsed is a static variable.
	public static void getTimeElapsed(int []p){
		lastCurrentTime = totalTime;			//1. get the lastCurrentTime
		totalTime = p[5];				//2. set totalTime to the overall time elapsed
		timeElapsed = totalTime - lastCurrentTime;	//3. Time elapsed is totalTime minus lastCurrentTime
	}
	
	//Method to set job's current running time
	public static void setRunningJobTime(){ 
		if (!readyQueue.isEmpty() && jobsOnCore > 0){	//1. Call every time, but only run it there's a running job.
			jobToRun.setCurrentTime(jobToRun.getCurrentTime() + timeElapsed);//2. Add elapsed time to current time.
			jobToRun.setTimeSlice(addressTable.getShortestTimeSlice());	//3. Assign to shortest time slice in job table first
			checkTimeout();							//4. This method checks if we're at or close to maxtime.
		}
		else
			return;
	}
	
	//Method to check if the job has reached its max time or if it will exceed it with current time slice
	public static void checkTimeout(){
		int timeTotal = jobToRun.getCurrentTime() + jobToRun.getTimeSlice();	//1. Get the current time + time slice
		if(jobToRun.getCurrentTime() == jobToRun.getMaxCpuTime()){		//2a. Check if the current time equals max time	
			jobToRun.setTimeFinished(true);					//3a. If so, set TimeFinished flag to true.
			readyQueue.remove(jobToRun);					//4a. Remove from readyQueue.
			if(jobToRun.getIOFlag() == false)				//5a. Check if it's not doing i/o
				addressTable.removeJob(jobToRun);			//6a. If not, remove from table completely.
		}
		else if (timeTotal > jobToRun.getMaxCpuTime())				//2b. Check if time slice exceeds max
			jobToRun.setTimeSlice(jobToRun.getMaxCpuTime() - jobToRun.getCurrentTime());	//3b. If it does, set timeSlice to difference.
	}		
	
	//This function checks if the drum is busy. If not, it polls from the waiting queue and adds a job to core if possible.
	//Essentially the swapper.
	public static void checkDrum() {
		sortWaitingQueue();	//0. Sort the waiting queue by Shortest Job Next
		if (!drumBusy && !waitingQueue.isEmpty()){	//1. Only run this code if the drum is not busy and there is something on the waitingQueue
			jobForDrum = waitingQueue.get(0);	//Changed this from peek to poll
			waitingQueue.remove(jobForDrum);
			if (!jobForDrum.getPassed() && !swapping)	//2a. if job for drum has not been passed once, mark it as passed and put in back of queue.
				markPassed();
			else if (!swapping){	//2b. If job has been marked as passed, check if we're in the middle of a swap.
				trySwap();
			}
			//If we're in the middle of a swap, finish the swap. Check if we're swapping and not swapping out
			else if (swapping && !swappingOut)
				commenceSwapIn();		
		}
	}

	/////////////////////////////SWAP OUT METHODS///////////////////////////////////////////////////////////////////////////
	//This method tries to assign the job directly. If it can't, mark it as passed.
	public static void markPassed(){
		if (addressTable.assignJob(jobForDrum)){
			transferDirection = 0;
			sos.siodrum(jobForDrum.getJobNumber(), jobForDrum.getJobSize(), jobForDrum.getJobAddress(), transferDirection);
			drumBusy = true;
		}
		else{
			jobForDrum.setPassed(true);
			waitingQueue.add(jobForDrum);
		}
	}
	
	//Again, attempts to add directly. If it can't, attempts to swap. If it can't, adds back to the queue.
	public static void trySwap(){
		if (addressTable.assignJob(jobForDrum)){	//3ba. If we're not swapping, check if we can assign the job directly without swapping.
			transferDirection = 0;
			sos.siodrum(jobForDrum.getJobNumber(), jobForDrum.getJobSize(), jobForDrum.getJobAddress(), transferDirection);
			drumBusy = true;
		}		
		else {						//3bb. If we can't assign directly, start swap.
			if(addressTable.canSwap(jobForDrum))	//3bba. Check if there's a job we can swap and prepare the data values
				commenceSwapOut();	
			else 	//3bbb. If There's no job we can swap it with, put it on the back of the waitingQueue and try again next cycle
				waitingQueue.add(jobForDrum);
		}
	}
	
	//Sets all flags and processes the swap out portion of the swap. Drumint will finish remaining swap processes.
	public static void commenceSwapOut(){
		swapping = true;
		swappingIn = true;
		swapIn = jobForDrum;
		swapOut = addressTable.getSwapJob();
		swappingOut = true;
		drumBusy = true;
		transferDirection = 1;
		readyQueue.remove(swapOut);
		sos.siodrum(swapOut.getJobNumber(), swapOut.getJobSize(), swapOut.getJobAddress(), transferDirection);
	}
	
	//Sets all flags and processes for the swap in portion of the swap. Drumint will finish remaining swap processes.
	public static void commenceSwapIn(){
		waitingQueue.add(jobForDrum);	//puts removed job back on waitingQueue
		drumBusy = true;
		transferDirection = 0;
		jobForDrum = swapIn;
		addressTable.assignJob(jobForDrum);
		sos.siodrum(jobForDrum.getJobNumber(), jobForDrum.getJobSize(), jobForDrum.getJobAddress(), transferDirection);		
	}
		
//////////This method checks if disk is ready. If so, then job gets added from iOQueue.	///////////////////////////////
	public static void checkDisk() {
		if ((!diskBusy) && (!iOQueue.isEmpty())){
			jobForDisk = iOQueue.peek();		//Must peek rather than poll here. We poll later in Dskint.
			sos.siodisk(jobForDisk.getJobNumber());
			diskBusy = true;
		}
	}	
	
/////////////////////////////////////////DRUMINT METHODS/////////////////////////////////////////////////////////////////////
	//Method to complete a job being swapped in to the core. Adds to readyQueue and updates flags/semaphores
	public static void coreIn() {
		readyQueue.add(jobForDrum);		//4b. Add job to readyQueue here.
		jobsOnCore += 1;			//5a. Increment jobsOnCore
		if(swapping){				//6a. Check swap code. flag accordingly
			swapping = false;
			swappingIn = false;
		}
	}
	
	//Method to complete a job being swapped out of the core. Moves to waitingQueue and sets flags/semaphores accordingly.
	public static void coreOut(){
		jobsOnCore -= 1;			//5b. Decrement jobsOnCore
		swapOut.setPassed(false);		//6b. Set the pass flag for the job to false.
		addressTable.removeJob(swapOut);	//8b. Remove from addressTable
		waitingQueue.add(swapOut);		//9b. Add to the waitingQueue
		swappingOut = false;			//10b. Set swappingOut flag to false.
		swapOut.setJobAddress(-1);			
	}	
	
/////////////////////////////////////////SERVICE METHODS////////////////////////////////////////////////////////////////////////////
	//Terminate service. Removes from readyQueue, sets timeFinished flag, and removes from addressTable
	//if it is not doing I/O
	public static void terminateService() {
		readyQueue.remove(jobRequestingService);	//1. Remove from ReadyQueue.	
		jobRequestingService.setTimeFinished(true);	//2. Set timeFinished in case it's still doing I/O.
		if (!jobRequestingService.getIOFlag()){		//3. If it's not doing I/O, remove from table and decrement jobsOnCore.
			addressTable.removeJob(jobRequestingService);
			jobsOnCore -= 1;
		}
	}
	
	//IOService. Puts on disk if not busy, otherwise adds it to the queue and sets flag.
	public static void iOService(){
		if(!diskBusy){
			sos.siodisk(jobRequestingService.getJobNumber());	//5b. but job stays on ReadyQueue.
			diskBusy = true;
		}
		iOQueue.add(jobRequestingService);	//6b. Still add to iOQueue, but leave blockFlag alone
		jobRequestingService.setIOFlag(true);
	}
	
	//Block service. Removes from ReadyQueue and sets a block flag.
	public static void blockService(){
		if(jobRequestingService.getIOFlag()){
			readyQueue.remove(jobRequestingService);	//5c. Remove from ReadyQueue.
			jobRequestingService.setBlockFlag(true);	//6c. Set blockFlag to true. It is blocked.
		}
	}
	
//////////////////////////////////STARTUP METHODS/////////////////////////////////////////////////////////////////////
	//Initialize Containers. Simple method to add modularity.
	public static void initializeContainers(){
		addressTable = new SizeAddressTable();
		jobTable = new LinkedList<Job>();
		readyQueue = new LinkedList<Job>();
		waitingQueue = new LinkedList<Job>();
		iOQueue = new LinkedList<Job>();
	}

	//static variables, all initialized to 0 or false
	public static void initializeVariables(){
		jobsOnCore = 0;
		totalTime = 0;
		lastCurrentTime = 0;
		timeElapsed = 0;
		drumBusy = false;
		diskBusy = false;
		swappingIn = false;
		swappingOut = false;
		swapping = false;
	}
	
	//static Job copies. The default values are 0 and null; they will hold Job addresses
	//as the processes enter interrupts.	
	public static void initializeJobObjects(){
		jobToRun = new Job();
		jobCompletingIO = new Job();
		jobRequestingService = new Job();
		jobForDrum = new Job();
		jobForDisk = new Job();
		swapIn = new Job();
		swapOut = new Job();
	}
		
//////////////////////////////////////SHORTEST JOB NEXT IMPLEMENTATION/////////////////////////////////////////////////////////////////	
	//Sorts by shortest job next. Kind of cheating, but we just go by the maxCpuTime. Last effort to
	//get my O.S. to process jobs faster.
	public static void sortWaitingQueue(){
		if(!waitingQueue.isEmpty()){
			for(int i = 0; i < waitingQueue.size(); i++){
				Job iJob = waitingQueue.get(i);
				int min = i;
				for(int j = i + 1; j < waitingQueue.size(); j++){
					Job jJob = waitingQueue.get(j);
					if(jJob.getMaxCpuTime() < iJob.getMaxCpuTime())
						min = j;
				}
				if(min != i){
					Job temp = waitingQueue.get(i);
					waitingQueue.set(i, waitingQueue.get(min));
					waitingQueue.set(min, temp);
				}
			}
		}
	}
///////FINISH		
}
