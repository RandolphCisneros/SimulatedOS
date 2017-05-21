//This is the individual job structure to be used in the job table, which will be a list of jobs.
public class Job {
	private int jobNumber;	//p[1]
	private int jobPriority;//p[2]
	private int jobSize;	//p[3]
	private int maxCpuTime;	//p[4]
	
	private int currentTime;	//current running time on job
	private int address;	
	private int timeSlice;
	private boolean blockFlag;
	private boolean IOFlag;
	private boolean timeFinished;
	private boolean passed;
	
	Job(){
		jobNumber = 0;
		jobPriority = 0;
		jobSize = 0;
		maxCpuTime = 0;
		currentTime = 0;
		address = -1;
		blockFlag = false;
		IOFlag = false;
		timeFinished = false;
		passed = false;
		timeSlice = 5;
	}
	
	Job(int jN, int jP, int jS, int mCT){
		jobNumber = jN;
		jobPriority = jP;
		jobSize = jS;
		maxCpuTime = mCT;
		currentTime = 0;
		address = -1;		//If job has no legal address, this number is negative
		blockFlag = false;
		IOFlag = false;
		timeFinished = false;
		passed = false;
		timeSlice = 5;
	}

	//accessors and mutators
	public boolean getPassed(){return passed;}
	public boolean getTimeFinished(){return timeFinished;}
	public int getJobNumber(){ return jobNumber;}
	public int getJobPriority(){ return jobPriority;}
	public int getJobSize(){ return jobSize;}
	public int getMaxCpuTime(){ return maxCpuTime;}
	public int getCurrentTime(){ return currentTime;}
	public int getJobAddress(){return address;}
	public int getTimeSlice(){return timeSlice;}
	public boolean getBlockFlag(){return blockFlag;}
	public boolean getIOFlag(){return IOFlag;}
	
	public void setIOFlag(boolean IOF){IOFlag = IOF;}
	public void setJobNumber(int jN){jobNumber = jN;}
	public void setJobPriority(int jP){jobPriority = jP;}
	public void setJobSize(int jS){jobSize = jS;}
	public void setMaxCpuTime(int mCT){maxCpuTime = mCT;}
	public void setCurrentTime(int cT){currentTime = cT;}
	public void setJobAddress(int a){address = a;}
	public void setBlockFlag(boolean bF){blockFlag = bF;}
	public void setTimeFinished(boolean tF){timeFinished = tF;}
	public void setTimeSlice(int tS){timeSlice = tS;}
	public void setPassed(boolean p){passed = p;}
	
}
