//Ask professor Jones about the meaing of []a and how it might effect my Job structure.
//This is the individual job structure to be used in the job table, which will be a list of jobs.
//I implement Comparable again because I don't know if I'm gonna prioritize some jobs or not;
//we can make it rather efficient by using priorities and job size.
public class Job  implements Comparable <Job> {
	//p[1]
	private int jobNumber;
	//p[2]
	private int jobPriority;
	//p[3]
	private int jobSize;
	//p[4]
	private int maxCpuTime;
	//p[5]
	private int currentTime;
	//adding my own address variable
	private int address;
	
	Job(int jN, int jP, int jS, int mCT, int cT){
		jobNumber = jN;
		jobPriority = jP;
		jobSize = jS;
		maxCpuTime = mCT;
		currentTime = cT;
		address = -1;					//we don't assign an address in the constructor, the table will do that for us
	}

	//accessors and mutators
	public int getJobNumber(){ return jobNumber;}
	public int getJobPriority(){ return jobPriority;}
	public int getJobSize(){ return jobSize;}
	public int getMaxCpuTime(){ return maxCpuTime;}
	public int currentTime(){ return currentTime;}
	
	public int getJobAddress(){return address;}
	
	public void setJobNumber(int jN){jobNumber = jN;}
	public void setJobPriority(int jP){jobPriority = jP;}
	public void setJobSize(int jS){jobSize = jS;}
	public void setMaxCpuTime(int mCT){maxCpuTime = mCT;}
	public void setCurrentTime(int cT){currentTime = cT;}
	public void setJobAddress(int a){address = a;}
	
	/*		May be used to compare jobs by size and priority
	public int compareTo(Job j){
	//Put more code here
	}*/
}
