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
	private static int jobsOnCore;
	private static int transferDirection;
	private static int totalTime;
	private static int timeElapsed;
	private static int lastCurrentTime;
	
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
		jobToRun = new Job();
		jobCompletingIO = new Job();
		jobRequestingService = new Job();
	}

	//This method receives job info. It creates a "Job" instance, and then attempts to assign it an address.
	//If successful, it is added to the ReadyQueue; otherwise it is added to the waiting queue.
	//Regardless it is added to our main jobTable.
	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
		sos.ontrace();	//remove this later
		getTimeElapsed(p);				//1. Set elapsed time.
		setRunningJobTime();				//2. Set last running Job's time, if any.
		
		Job newestJob = new Job(p[1],p[2],p[3],p[4]);	//3. Assign input to newestJob.
		if (addressTable.assignJob(newestJob)){		//4a. addressTable checks if there's enough free space. If there is it gets allocated free space and put on the readyqueue
			System.out.println("Putting job on core");
			transferDirection = 0;
			sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), transferDirection);		//3a. Don't know if I should do this with siodrum. Puts job on core (memory)
			readyQueue.add(newestJob);							//***!!!May have to move this line to Drmint
			System.out.println("Job max time: " + newestJob.getMaxCpuTime());
			/*System.out.println("Job address: " + newestJob.getJobAddress());
			System.out.println("Job size: " + newestJob.getJobSize());
			System.out.println("Job is addressed correctly");*/
		}
		else{
			waitingQueue.add(newestJob);																								//2b. If not, then it gets put on the waitingQueue.
			//Commenting this out since it is likely to cause problems in the future: sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), 1);		//***!!!3b. Don't know if I should do this with siodrum. Puts job on backing store. I may not have to do this if there's not room on the core
		}
		jobTable.add(newestJob);																											//4 Push onto jobTable
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
		getTimeElapsed(p);
		setRunningJobTime();
		jobCompletingIO = iOQueue.poll();
		readyQueue.add(jobCompletingIO);
		dispatcher(a,p);		
	}

	public static void Drmint (int[]a, int[]p){
		System.out.println("In Drmint");
		getTimeElapsed(p);
		setRunningJobTime();
		if (transferDirection == 0){
			jobsOnCore += 1;
			System.out.println("Incremented jobsOnCore");
		}
		else if (transferDirection == 1){
			jobsOnCore -= 1;
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
		System.out.println("In Tro");
		getTimeElapsed(p);
		setRunningJobTime();
			//must find job to run in readyQueue and job Table, set time, check if 0. If 0, proceed with removal process.
		dispatcher(a,p);
	}
	
	public static void Svc (int[]a, int[]p){
		System.out.println("In Svc");
		timeElapsed = getTimeElapsed(p);
		setRunningJobTime();
		jobRequestingService = jobToRun;
		if (a[0] == 5){								//can turn this whole process into a function
			//Commenting this out because we don't go to Drmint: transferDirection = 1;
			readyQueue.remove(jobRequestingService);			//may have to traverse the whole queue to get to this
			addressTable.removeJob(jobRequestingService);		//function may not work perfectly
			jobsOnCore -= 1;
			//commenting out for test purposessos.siodrum(jobRequestingService.getJobNumber(), jobRequestingService.getJobSize(), jobRequestingService.getJobAddress(), 1);	//I remove from drum after, but this should still work properly
		}
		else if (a[0] == 6) {
			sos.siodisk(jobRequestingService.getJobNumber());
		}
		else {	//a[0] == 7
			readyQueue.remove(jobRequestingService);
			iOQueue.add(jobRequestingService);
			//block Job? Maybe create a block flag?
		}
		dispatcher(a,p);	
	}
	
	/////////////////////////////////////////////////////////////
	//////////////////END OF INTERRUPTS///////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	//I put dispatcher into its own function to avoid repeating code.
	public static void dispatcher(int[]a, int[]p){
		System.out.println("In dispatcher");
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
		
		System.out.println("TotalTime = " + totalTime);
		System.out.println("Last Current Time = " + lastCurrentTime);
		System.out.println("Time elapsed since last interrupt: " + timeElapsed);
	}
	
	//Method to set job's current running time
	public static void setRunningJobTime(){ 
		System.out.println("In setRunningJobTime");
		if (!readyQueue.isEmpty() && jobsOnCore > 0){			//Possible logic error here
			jobToRun.setCurrentTime(jobToRun.getCurrentTime() + timeElapsed);
			System.out.println("Last running job's current time: " + jobToRun.getCurrentTime());
			System.out.println("Last running job's max time: " + jobToRun.getMaxCpuTime());
		}
		else
			return;
	}
		
}
