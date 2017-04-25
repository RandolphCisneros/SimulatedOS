import java.util.*;

public class os {

	public static final int MAX_FREE_SPACE = 100;
	public static final int TIME_SLICE = 5;

	public static SizeAddressTable addressTable = new SizeAddressTable();
	Stack<Job> processorStack = new Stack<Job>();					//To be used for interrupts that want to go back
	LinkedList<Job> jobTable = new LinkedList<Job>();
	Queue<Job> readyQueue = new LinkedList<Job>();
	Queue<Job> waitingQueue = new Linked List<Job>();				//a waiting queue for if we don't have enough space or something like that
	Queue<Job> iOQueue = new LinkedList<Job>();						//this is the I/O queue.

	static Job jobToRun = new Job();												//this will be the first static object; I can't initialize in startup because there's nothing to initialize
	static Job jobCompletingIO = new Job();
	
	//This is to initialize static variables; so far I haven't really come up with many
	public static void startup(){
		System.out.println("In startup");


	}

	//This method receives job info. It creates a "Job" instance, and then attempts to assign it an address.
	//If successful, it is added to the ReadyQueue; otherwise it is added to the waiting queue.
	//Regardless it is added to our main jobTable.

	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
		Job newestJob = new Job(p[1],p[2],p[3],p[4],p[5]);				//1. Job arrives. We take the parameters.
		if (addressTable.assignJob(newestJob))									//2a. addressTable checks if there's enough free space. If there is it gets allocated free space and put on the readyqueue
			readyQueue.add(newestJob);
		else
			waitingQueue.add(newestJob);											//2b. If not, then it gets put on the waitingQueue.
		jobTable.add(newestJob);														//3. Push onto jobTable
		//siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), 0);		//Don't know if I should do this with siodrum
		dispatcher(a, p);
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
	}
	
	
	public static void Tro (int[]a, int[]p){
		System.out.println("In Tro");
			//must find job to run in readyQueue and job Table, set time, check if 0. If 0, proceed with removal process.
	}
	public static void Svc (int[]a, int[]p){
		System.out.println("In Svc");
	}
	
	//I put dispatcher into its own function to avoid repeating code.
	public static void dispatcher(int[]a, int[]p){
		jobToRun = readyQueue.poll();				//1. Set job to run to job in front of ready queue.
		a = 2;													//2. Set a to 2 to run job
		p[2]  = jobToRun.getAddress();				//3. Set p[2] to address of job to run
		p[3] = jobToRun.getSize();						//4. Set p[3] to size of job to run
		p[4] = TIME_SLICE;								//5. Set time slice. I'm doing round robin so this will stay the same.
		readyQueue.add(jobToRun);					//5. Put job to run in back of queue. When dispatcher is called again, jobToRun will be assigned the next job in the queue
	}
}