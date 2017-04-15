
public class os {
	


	
	
	public static void startup(){
		System.out.println("In startup");
		static Stack<Job> processorStack = new Stack<Job>();	//may create a jobqueue class
		static JTable jobTable = new JTable(Job, jobNumber);		//may arrange this differently
		static Queue<Job> readyQueue = new ArrayList<Job>();
	}

	public static void Crint(int[]a, int[]p){
		System.out.println ("In Crint");
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
