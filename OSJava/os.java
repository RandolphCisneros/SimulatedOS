import java.util.*;

public class os {

	public static final int TIME_SLICE = 5;

	private static SizeAddressTable addressTable;
	private static Stack<Job> processorStack;					//To be used for interrupts that want to go back
	public static LinkedList<Job> jobTable;		//I made this global solely because it said to in the handout.
	private static Queue<Job> readyQueue;
	private static Queue<Job> waitingQueue;				//a waiting queue for if we don't have enough space or something like that
	private static Queue<Job> iOQueue;						//this is the I/O queue.
	private static boolean emptyCoreFlag;

	private static Job jobToRun;												//this will be the first static object; I can't initialize in startup because there's nothing to initialize
	private static Job jobCompletingIO;
	
	//This is to initialize static variables. NOTE: I haven't set them all to static,
	//but I guess I'll find out the consequences later.
	public static void startup(){
		System.out.println("In startup");
		addressTable = new SizeAddressTable();
		processorStack = new Stack<Job>();
		jobTable = new LinkedList<Job>();
		readyQueue = new LinkedList<Job>();
		waitingQueue = new LinkedList<Job>();
		iOQueue = new LinkedList<Job>();
		emptyCoreFlag = true;
		
		jobToRun = new Job();
		jobCompletingIO = new Job();
	}

	//This method receives job info. It creates a "Job" instance, and then attempts to assign it an address.
	//If successful, it is added to the ReadyQueue; otherwise it is added to the waiting queue.
	//Regardless it is added to our main jobTable.
	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
		sos.ontrace();
		Job newestJob = new Job(p[1],p[2],p[3],p[4],p[5]);																		//1. Job arrives. We take the parameters.
		if (addressTable.assignJob(newestJob)){										//2a. addressTable checks if there's enough free space. If there is it gets allocated free space and put on the readyqueue
			System.out.println("Putting job on core");
			sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), 0);		//3a. Don't know if I should do this with siodrum. Puts job on core (memory)
			readyQueue.add(newestJob);
			/*System.out.println("Job address: " + newestJob.getJobAddress());
			System.out.println("Job size: " + newestJob.getJobSize());
			System.out.println("Job is addressed correctly");*/
		}
		else{
			waitingQueue.add(newestJob);																								//2b. If not, then it gets put on the waitingQueue.
			sos.siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), 1);		//3b. Don't know if I should do this with siodrum. Puts job on backing store
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
		jobCompletingIO = iOQueue.poll();
		readyQueue.add(jobCompletingIO);
		dispatcher(a,p);		
	}

	public static void Drmint (int[]a, int[]p){
		System.out.println("In Drmint");
		jobToRun.setCurrentTime(jobToRun.getCurrentTime() - p[5]);
		System.out.println("Time is now: " + jobToRun.getMaxCpuTime());
		dispatcher(a,p);
	}
	
	
	public static void Tro (int[]a, int[]p){
		System.out.println("In Tro");
			//must find job to run in readyQueue and job Table, set time, check if 0. If 0, proceed with removal process.
		dispatcher(a,p);
	}
	public static void Svc (int[]a, int[]p){
		System.out.println("In Svc");
	}
	
	//I put dispatcher into its own function to avoid repeating code.
	public static void dispatcher(int[]a, int[]p){
		
		jobToRun = readyQueue.poll();				//1. Set job to run to job in front of ready queue.
		if (emptyCoreFlag){
			a[0] = 1;						//2. Set a to 1  if emptyCoreFlag shows 1
			if(!(readyQueue.isEmpty()))
			   emptyCoreFlag = false;
		}
		else
			a[0] = 2;						//2b. Else set a[0] to 2
		p[2]  = jobToRun.getJobAddress();				//3. Set p[2] to address of job to run
		p[3] = jobToRun.getJobSize();						//4. Set p[3] to size of job to run
		p[4] = TIME_SLICE;								//5. Set time slice. I'm doing round robin so this will stay the same.
		System.out.println("jobToRun address: " + jobToRun.getJobAddress());
		System.out.println("jobToRun Size: " + jobToRun.getJobSize());
		readyQueue.add(jobToRun);					//5. Put job to run in back of queue. When dispatcher is called again, jobToRun will be assigned the next job in the queue
	}
}
