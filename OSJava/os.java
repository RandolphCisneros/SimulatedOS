import java.util.*;

public class os {

	public static final int MAX_FREE_SPACE = 100;

	Stack<Job> processorStack = new Stack<Job>();	//may create a jobqueue class
	Queue<Job> readyQueue = new LinkedList<Job>();
	LinkedList<Job> jobTable = new LinkedList<Job>();
	SizeAddressTable addressTable = new SizeAddressTable();
	
	
	
	//This is to initialize static variables; so far I haven't really come up with many
	public static void startup(){
		System.out.println("In startup");


	}

	//
	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
		Job newestJob = new Job(p[1],p[2],p[3],p[4],p[5]);
		addressTable.assignJob(newestJob);//addressTable will find free space and allocate it for the job, set the job's address, and keep track of free space
		jobTable.add(newestJob);				//pushes onto jobTable
		siodrum(newestJob.getJobNumber(), newestJob.getJobSize(), newestJob.getJobAddress(), 0);
		
	}

	public static void Dskint (int[]a, int[]p){
		System.out.println("In Dskint");
	}

	public static void Drmint (int[]a, int[]p){
		System.out.println("In Drmint");
	}
	public static void Tro (int[]a, int[]p){
		System.out.println("In Tro");
	}
	public static void Svc (int[]a, int[]p){
		System.out.println("In Svc");
	}
}