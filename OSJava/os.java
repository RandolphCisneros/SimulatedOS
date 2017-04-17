import java.util.*;

public class os {

	public static final int MAX_FREE_SPACE = 100;

	public static void startup(){
		System.out.println("In startup");
		Stack<Job> processorStack = new Stack<Job>();	//may create a jobqueue class
		Queue<Job> readyQueue = new LinkedList<Job>();
		LinkedList<Job> jobTable = new LinkedList<Job>();
		LinkedList<SizeAddressPair> addressTable = new LinkedList<SizeAddressPair>(); //Maybe have to make this its own class
		
		//This block allocates the new free space in memory and puts it on the address table.
		SizeAddressPair initialFreeSpace = new SizeAddressPair(MAX_FREE_SPACE, 1);
		addressTable.push(initialFreeSpace);
	}

	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
		assignedAddress = addressTable.findFreeSpace(p[3]);//p[3] is the job Size. Based on this we can find space on the table and then add this,
						//but we're using worst fit anyway.
		Job newestJob = new Job(p[1],p[2],p[3],p[4],p[5],assignedAddress);	//takes data and assigns newest job
		jobTable.push(newestJob);				//pushes onto jobTable
		addressTable.assignJob(newestJob);//!!!!!This needs to be created, therefore we have to
						//create a data structure for the addressTable
		
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
