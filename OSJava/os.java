import java.util.*;

public class os {

	public static final int TIME_SLICE = 5;

	private static SizeAddressTable addressTable;
	private static Stack<Job> processorStack;		//To be used for interrupts that want to go back.
	public static LinkedList<Job> jobTable;			//I made this global solely because it said to in the handout.
	private static Queue<Job> readyQueue;
	private static Queue<Job> waitingQueue;			//This waiting queue is for if we don't have enough space in the core.
	private static Queue<Job> iOQueue;			//This is the I/O queue to be used when I/O jobs want to be blocked.

	private static Job jobToRun;	
	private static Job jobCompletingIO;
	private static Job jobRequestingService;
	private static Job jobTransferred;
	private static Job lastJobAdded;
	private static int jobsOnCore;
	private static int transferDirection;
	private static int totalTime;
	private static int timeElapsed;
	private static int lastCurrentTime;
	private static boolean comingFromCrint;
	
	//This is to initialize static variables. All variables must be static for the static functions.
	public static void startup(){
		System.out.println("In startup");
		
		//Initialize Containers
		addressTable = new SizeAddressTable();	//This is a container, even though it only takes type Job
		processorStack = new Stack<Job>();	//Haven't found a use for this stack yet
		jobTable = new LinkedList<Job>();
		readyQueue = new LinkedList<Job>();
		waitingQueue = new LinkedList<Job>();
		iOQueue = new LinkedList<Job>();
		
		//static variables, all initialized to 0
		jobsOnCore = 0;
		totalTime = 0;
		lastCurrentTime = 0;
		timeElapsed = 0;
		
		//static Job copies. The default values are 0 and null; they will hold copies of the addresses
		//as the processes enter interrupts.
		jobTransferred = new Job();
		jobToRun = new Job();
		jobCompletingIO = new Job();
		jobRequestingService = new Job();
		lastJobAdded = new Job();
	}

	//This method receives job info. It creates a "Job" instance, and then attempts to assign it an address.
	//If successful, it is added to the ReadyQueue; otherwise it is added to the waiting queue.
	//Regardless it is added to our main jobTable.
	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");

		getTimeElapsed(p);				//1. Set elapsed time.
		setRunningJobTime();				//2. Set last running Job's time, if any.
		
		Job newestJob = new Job(p[1],p[2],p[3],p[4]);	//3. Assign input to newestJob.
		if (addressTable.assignJob(newestJob)){		//4a. addressTable checks if there's enough free space. If there is it gets allocated free space and put on the readyqueue
			System.out.println("Putting job on core");
			transferDirection = 0;
			sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), transferDirection);		//3a. Puts job on core (memory)
			
			System.out.println("Job max time: " + newestJob.getMaxCpuTime());
			/*System.out.println("Job address: " + newestJob.getJobAddress());
			System.out.println("Job size: " + newestJob.getJobSize());
			System.out.println("Job is addressed correctly");*/
		}
		else{
			waitingQueue.add(newestJob);	//2b. If not, then it gets put on the waitingQueue. May change this with swapping method
		}
		jobTable.add(newestJob);		//4 Push onto jobTable
		comingFromCrint = true;
		lastJobAdded = newestJob;
		dispatcher(a, p);
		/*System.out.println("Job address after dispatcher: " + newestJob.getJobAddress());
		System.out.println("Job address currently assigned to dispatcher: " + p[2]);
		System.out.println("Job size after dispatcher: " + newestJob.getJobSize());
		System.out.println("Job size currently assigned to dispatcher : " + p[3]);*/
		return;
	}

	//The job at the front of the I/O queue is removed and assigned to jobCompletingIO.
	//It is then added to the readyQueue.
	//Lastly dispatcher is called and the job in front of the readyQueue is run.
	public static void Dskint (int[]a, int[]p){
		System.out.println("In Dskint");
		sos.ontrace();	//remove later
		getTimeElapsed(p);	//1. Get time elapsed
		setRunningJobTime();	//2. Set running job time. I still call this here because other jobs are running, not
					//	necessarily jobs finishing disk I/O.
		jobCompletingIO = iOQueue.poll();
		jobCompletingIO.setIOFlag(false);
		if (jobCompletingIO.getBlockFlag()){		//3. Poll from IOQueue. All calls to siodisk now get added to iOQueue.
			jobCompletingIO.setBlockFlag(false);	//4. Set the blockFlag to false.
			readyQueue.add(jobCompletingIO);	//5. Put on readyQueue. If a job wasn't blocked, it is already on the queue.
		}
		if (jobCompletingIO.getJobNumber() == jobToRun.getJobNumber()){
			jobToRun.setBlockFlag(false);
			System.out.println("IOFLAG: " + jobToRun.getIOFlag());
		}
		jobCompletingIO.getJobNumber();
		jobToRun.getJobNumber();
		System.out.println(jobCompletingIO.toString());
		System.out.println(jobToRun.toString());
		System.out.println("IOFlag " + jobCompletingIO.getIOFlag());
		System.out.println("JOB HAS FINISHED I/O!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		dispatcher(a,p);	//6. Call dispatcher	
		sos.offtrace();	//remove later
	}

	public static void Drmint (int[]a, int[]p){
		System.out.println("In Drmint");
		getTimeElapsed(p);					//1. Get elapsed time
		setRunningJobTime();					//2. Set running job time. Won't do if no jobsOnCore or readyQueue is empty.
		
		if (transferDirection == 0){				//3. Check transfer direction
			if(comingFromCrint){				//This checks if it was a new job coming in.
				readyQueue.add(lastJobAdded);		//4a. Add job to readyQueue here.
				comingFromCrint = false;
			}
			jobsOnCore += 1;				//5a. Increment jobsOnCore
			System.out.println("Incremented jobsOnCore");
		}
		else if (transferDirection == 1){
			//May need to do this for a swap out. Maybe make a new job: readyQueue.remove(jobToRun);			//4b. Remove last job to run
			jobsOnCore -= 1;				//5b. Decrement jobsOnCore
			System.out.println("Decremented jobsOnCore");
		}
		//System.out.println("Job current time: " + jobToRun.getCurrentTime());
		//System.out.println("Job max time: " + jobToRun.getMaxCpuTime());
		//Still don't know what to do with currentTime: jobToRun.setCurrentTime(jobToRun.getCurrentTime() - p[5]);			//I don't know if I'm setting this correctly. Ask professor
		//System.out.println("Time is now: " + jobToRun.getMaxCpuTime());
		System.out.println("Jobs on core: " + jobsOnCore);
		dispatcher(a,p);
	}
	
	
	public static void Tro (int[]a, int[]p){
		//System.out.println("In Tro");
		getTimeElapsed(p);
		setRunningJobTime();
		System.out.println("IOFlag: " + jobToRun.getIOFlag());
			//must find job to run in readyQueue and job Table, set time, check if 0. If 0, proceed with removal process.
		dispatcher(a,p);
	}
	
	public static void Svc (int[]a, int[]p){
		System.out.println("In Svc");
		sos.ontrace();	//remove this later
		getTimeElapsed(p);			//1. Set timeElapsed
		setRunningJobTime();			//2. Set running time for job
		
		jobRequestingService = jobToRun;	//3. Assign jobRequestingService
		if (a[0] == 5){						//4a. It requested termination
			System.out.println("Job requesting termination");
			readyQueue.remove(jobRequestingService);	//5a. I may have to traverse the whole queue to get to this job, and then iterate over again to get back where I was. Check documentation
			addressTable.removeJob(jobRequestingService);	//function may not work perfectly
			jobsOnCore -= 1;
		}
		else if (a[0] == 6) {						//4b. It requests disk i/o. Dskint will come after,
			System.out.println("Job requesting unblocked IO");
			sos.siodisk(jobRequestingService.getJobNumber());	//5b. but job stays on ReadyQueue.
			System.out.println("JOB STARTING I/O!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			iOQueue.add(jobRequestingService);	//6b. Still add to iOQueue, but leave blockFlag alone
			jobRequestingService.setIOFlag(true);
			System.out.println("IOFlag: " + jobRequestingService.getIOFlag());
			System.out.println(jobRequestingService.toString());
		}
		else {							//4c. a[0] == 7, job wants to be blocked for i/o
			System.out.println("Job requesting blocked IO");
			System.out.println("IOFlag: " + jobRequestingService.getIOFlag());
			if(jobRequestingService.getIOFlag()){
				readyQueue.remove(jobRequestingService);	//5c. Remove from ReadyQueue.
				iOQueue.add(jobRequestingService);		//6c. Add to I/OQueue.
				jobRequestingService.setBlockFlag(true);	//7c. Set blockFlag to true. It is blocked.
			}
		}
		sos.offtrace();	//remove later
		dispatcher(a,p);	//Last, call dispatcher.
	}
	
	/////////////////////////////////////////////////////////////
	//////////////////END OF INTERRUPTS///////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	//I put dispatcher into its own function to avoid repeating code.
	public static void dispatcher(int[]a, int[]p){
		//System.out.println("In dispatcher");
		//This block of code checks if the core is empty; should really be used once
		if (jobsOnCore == 0){
			//System.out.println("No jobs on core");
			a[0] = 1;		//1a. Set a to 1  if there are no jobs on core
		}	
		else if (!readyQueue.isEmpty()){	//1b. We check the readyQueue because this hold jobs on the core and not blocked.
			a[0] = 2;			//2b. Set a[0] to 2
			jobToRun = readyQueue.poll();	//3b. Set job to run to job in front of ready queue.
			p[2]  = jobToRun.getJobAddress();	//4b. Set p[2] to address of job to run
			p[3] = jobToRun.getJobSize();		//5b. Set p[3] to size of job to run
			p[4] = TIME_SLICE;			//6b. Set time slice. I'm doing round robin so this will stay the same.
			/*System.out.println("jobToRun address: " + jobToRun.getJobAddress());
			System.out.println("jobToRun Size: " + jobToRun.getJobSize());*/
			readyQueue.add(jobToRun);	//5. Put job to run in back of queue. When dispatcher is called again, jobToRun will be assigned the next job in the queue
		}
		else {	//If a job is blocked and on the core, and there are no unblocked jobs on the core, we go to this.
			System.out.println("ReadyQueue is empty");
			a[0] = 1;					//Keep idle
		}
	}
	
	//Method to set timeElapsed. Don't return anything because timeElapsed is a static variable.
	public static void getTimeElapsed(int []p){
		lastCurrentTime = totalTime;			//1. get the lastCurrentTime
		totalTime = p[5];				//2. set totalTime to the overall time elapsed
		timeElapsed = totalTime - lastCurrentTime;	//3. Time elapsed is totalTime minus lastCurrentTime
		
		/*System.out.println("TotalTime = " + totalTime);
		System.out.println("Last Current Time = " + lastCurrentTime);
		System.out.println("Time elapsed since last interrupt: " + timeElapsed);*/
	}
	
	//Method to set job's current running time
	public static void setRunningJobTime(){ 
		//System.out.println("In setRunningJobTime");
		if (!readyQueue.isEmpty() && jobsOnCore > 0){			//Possible logic error here
			jobToRun.setCurrentTime(jobToRun.getCurrentTime() + timeElapsed);
			/*System.out.println("Last running job's current time: " + jobToRun.getCurrentTime());
			System.out.println("Last running job's max time: " + jobToRun.getMaxCpuTime());*/
		}
		else
			return;
	}
		
}
