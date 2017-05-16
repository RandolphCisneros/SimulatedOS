import java.util.*;

class SizeAddressTable {
	
  public static final int MAX_FREE_SPACE = 99;
  public static final int START_OF_MEMORY = 0;

  private static SizeAddressPair largestRemainingFreeSpace;    //initialize to 0,100; tracks the largest free space on list
  LinkedList<SizeAddressPair> freeSpaceList = new LinkedList<SizeAddressPair>();//list that tracks remaining free spaces
  LinkedList<Job> jobsAddressed = new LinkedList<Job>();	//List of jobs to track the job addresses
  private static Job largestJob;
  private static int shortestTimeSlice;
  
  //constructor initializes largest free space and adds it to the table.
  //I used a default constructor here since it will only be used once in the OS
  public SizeAddressTable(){
	freeSpaceList = new LinkedList<SizeAddressPair>();
    	largestRemainingFreeSpace = new SizeAddressPair(MAX_FREE_SPACE, START_OF_MEMORY);	//There should only be one instance of this
    	freeSpaceList.add(largestRemainingFreeSpace);
	largestJob = new Job();
  }
  
 //This keeps track of the addresses of assigned jobs. If the jobs are removed then we know which address will get free space.
  public boolean assignJob(Job newJob){
	 //System.out.println("Largest Remaing Free Space in assignJob: " + largestRemainingFreeSpace.getAddress() + " " + largestRemainingFreeSpace.getSize());
	if ((newJob.getJobAddress() < 0) && (largestRemainingFreeSpace.getSize() > newJob.getJobSize())){	//1. Check that we have free space and it's not already assigned
		System.out.println("Adding job " + newJob.getJobNumber() + " at " + largestRemainingFreeSpace.getAddress());
		newJob.setJobAddress(largestRemainingFreeSpace.getAddress());					//2a. If there is, we set the address to the largest remaining free space
		jobsAddressed.add(newJob);									//3. We add it to our list of jobs with addresses
		int newFreeSpaceSize = largestRemainingFreeSpace.getSize() - newJob.getJobSize();
		largestRemainingFreeSpace.setSize(newFreeSpaceSize);		//4. We subtract the size of the job from the "largest remaining free space"
		largestRemainingFreeSpace.setAddress(largestRemainingFreeSpace.getAddress() + newJob.getJobSize());	//5. We changed the address of the "largest remaining free space" to the previous address + the size of the added job
		findNewLargestRemainingFreeSpace();	//6. We find the real largest remaining freespace
		findShortestTimeSlice();
		return true;																													//7. Return true for O.S. to allocate it to the correct queue
	}
	else if (newJob.getJobAddress() < 0 && largestRemainingFreeSpace.getSize() < newJob.getJobSize()){	//2b. If there's not enough space, throw message and return false. O.S. puts it in waiting queue.
		//The problem here is that as I get more jobs I need to swap in and out. This will  have to be changed.
		//System.out.println("Not enough space for job");
		return false;
	}
	else{
		System.out.println("Job already assigned");																		//2c. This shouldn't happen, but if it's already assigned then it will return false.
		return false;
	}	
  }
  
  //this function will be used when a job is terminated; it will removed from the list and the free space will be re-allocated
  public void removeJob(Job completedJob){
	System.out.println("Removing job " + completedJob.getJobNumber() + " from " + completedJob.getJobAddress() + " " + completedJob.getJobSize());
	if (jobsAddressed.contains(completedJob)){
		int completedJobSize = completedJob.getJobSize();
		int completedJobAddress = completedJob.getJobAddress();
		System.out.println("Checking for adjacent free space in the back");
		for(int i = 0; i < freeSpaceList.size(); i++){
			SizeAddressPair current = freeSpaceList.get(i);				
			if((completedJobSize + completedJobAddress) == current.getAddress()){	//check for free space after the job
				System.out.println("Found free adjacent free space in the back " + current.getAddress() + " " + current.getSize());
				completedJobSize += current.getSize();
				freeSpaceList.remove(current);	
			}
		}
		System.out.println("Checking for adjacent free space in the front");
		for(int j = 0; j < freeSpaceList.size(); j++){
			SizeAddressPair current = freeSpaceList.get(j);
			if ((current.getAddress() + current.getSize()) == completedJobAddress){		//check for free space before the job
				System.out.println("Found free adjacent space in the front " + current.getAddress() + " " + current.getSize());
				current.setSize(current.getSize() + completedJobSize);
				jobsAddressed.remove(completedJob);
				findNewLargestRemainingFreeSpace();
				return;
			}
		}
		SizeAddressPair newFreeSpace = new SizeAddressPair(completedJobSize, completedJobAddress);
		System.out.println("New free space created at: " + newFreeSpace.getAddress() + " " + newFreeSpace.getSize());
		freeSpaceList.add(newFreeSpace);
		jobsAddressed.remove(completedJob);
		findNewLargestRemainingFreeSpace();		//finds the new largest remaining free space after
		findShortestTimeSlice();
  
	}
  }
  
  //After a job has been assigned, this function will be used to find and assign the new largest remaining free space
 	 public void findNewLargestRemainingFreeSpace(){
		System.out.println("Printing all free spaces");
		for (int i = 0; i < freeSpaceList.size(); i++){				//1. Iterate through freeSpaceList
			SizeAddressPair current = freeSpaceList.get(i);			//2. Create pointer to object
			System.out.println(current.getAddress() + " " + current.getSize());
			if (current.getSize() > largestRemainingFreeSpace.getSize()){	//3. Compare sizes. If current is largest, re-assign largest remaining free space
				largestRemainingFreeSpace = current;
			}
		System.out.println("Largest Remaining Free Space: " + largestRemainingFreeSpace.getAddress() + " " + largestRemainingFreeSpace.getSize());
		}
  	}
	
	public void findNewLargestJob(){
		for (int i = 0; i < jobsAddressed.size(); i++){
			Job current = jobsAddressed.get(i);
			if (current.getJobSize() > largestJob.getJobSize()){
				largestJob = current;
			}
		}
		System.out.println("Largest Job is : " + largestJob.getJobNumber() + largestJob.getJobAddress() + largestJob.getJobSize());
	}
	
	//This is a new method to cut down on Tro interrupts. whenever a job is added or removed, we call this and it sets the shortest time slice.
	public void findShortestTimeSlice(){
		if(!jobsAddressed.isEmpty()){
			Job shortestJob = jobsAddressed.get(0);
			int minimumTimeSlice = shortestJob.getMaxCpuTime() / 4;
			if(jobsAddressed.size() > 1){
				for (int i = 1; i < jobsAddressed.size(); i++){
					Job current = jobsAddressed.get(i);
					int currentTimeSlice = current.getMaxCpuTime() / 4;
					if(currentTimeSlice < minimumTimeSlice){
						minimumTimeSlice = currentTimeSlice;
					}
				}
			}
			shortestTimeSlice = minimumTimeSlice;
			System.out.println("Shortest time slice is: " + shortestTimeSlice);
		}
	}
			
	public Job getLargestJob(){return largestJob;}
	public int getShortestTimeSlice(){return shortestTimeSlice;}
}
