import java.util.*;

class SizeAddressTable {
	
  	public static final int MAX_FREE_SPACE = 99;
  	public static final int START_OF_MEMORY = 0;

  	private static SizeAddressPair largestRemainingFreeSpace;    //initialize to 0,100; tracks the largest free space on list
  	LinkedList<SizeAddressPair> freeSpaceList = new LinkedList<SizeAddressPair>();//list that tracks remaining free spaces
  	LinkedList<Job> jobsAddressed = new LinkedList<Job>();	//List of jobs to track the job addresses
  	private static Job largestJob;
  	private static Job swapJob;
  	private static int shortestTimeSlice;
	private static int completedJobSize;
	private static int completedJobAddress;
  
  	//constructor initializes largest free space and adds it to the table.
  	//I used a default constructor here since it will only be used once in the OS
  	public SizeAddressTable(){
		freeSpaceList = new LinkedList<SizeAddressPair>();
    		largestRemainingFreeSpace = new SizeAddressPair(MAX_FREE_SPACE, START_OF_MEMORY);	//There should only be one instance of this
    		freeSpaceList.add(largestRemainingFreeSpace);
		largestJob = new Job();
		swapJob = new Job();
  	}
  
 	//This method attempts to find an address for a job. Returns true if successful and gives the job the address. Also adds to table.
  	public boolean assignJob(Job newJob){
		if ((newJob.getJobAddress() < 0) && (largestRemainingFreeSpace.getSize() > newJob.getJobSize())){	//1. Check that we have free space and it's not already assigned
			newJob.setJobAddress(largestRemainingFreeSpace.getAddress());					//2a. If there is, we set the address to the largest remaining free space
			jobsAddressed.add(newJob);									//3. We add it to our list of jobs with addresses
			int newFreeSpaceSize = largestRemainingFreeSpace.getSize() - newJob.getJobSize();
			largestRemainingFreeSpace.setSize(newFreeSpaceSize);		//4. We subtract the size of the job from the "largest remaining free space"
			largestRemainingFreeSpace.setAddress(largestRemainingFreeSpace.getAddress() + newJob.getJobSize());	//5. We changed the address of the "largest remaining free space" to the previous address + the size of the added job
			findNewLargestRemainingFreeSpace();	//6. We find the real largest remaining freespace
			findShortestTimeSlice();
			return true;																													//7. Return true for O.S. to allocate it to the correct queue
		}
		else if (newJob.getJobAddress() < 0 && largestRemainingFreeSpace.getSize() < newJob.getJobSize())	//2b. If there's not enough space, throw message and return false. O.S. puts it in waiting queue.
			return false;
		else																	//2c. This shouldn't happen, but if it's already assigned then it will return false.
			return false;	
  	}
  
  	//this function will be used when a job is terminated; it will removed from the list and the free space will be re-allocated
  	public void removeJob(Job completedJob){
		if (jobsAddressed.contains(completedJob)){
			completedJobSize = completedJob.getJobSize();
			completedJobAddress = completedJob.getJobAddress();
			checkRearFreeSpace();
			//Can't make this into a method because it returns to O.S. if it adds free space to the front.
			for(int j = 0; j < freeSpaceList.size(); j++){
				SizeAddressPair current = freeSpaceList.get(j);
				if ((current.getAddress() + current.getSize()) == completedJobAddress){		//check for free space before the job
					current.setSize(current.getSize() + completedJobSize);
					jobsAddressed.remove(completedJob);
					findNewLargestRemainingFreeSpace();
					findShortestTimeSlice();
					return;
				}
			}
			addNewFreeSpace();
		}
  	}
  
	//If there is no adjacent free space in the front, this method adds the new free space to the free space table.
	public void addNewFreeSpace(){
		SizeAddressPair newFreeSpace = new SizeAddressPair(completedJobSize, completedJobAddress);
		freeSpaceList.add(newFreeSpace);
		jobsAddressed.remove(completedJob);
		findNewLargestRemainingFreeSpace();
		findShortestTimeSlice();
	}
	
  	//After a job has been assigned, this function will be used to find and assign the new largest remaining free space
 	public void findNewLargestRemainingFreeSpace(){
		for (int i = 0; i < freeSpaceList.size(); i++){				//1. Iterate through freeSpaceList
			SizeAddressPair current = freeSpaceList.get(i);			//2. Create pointer to object
			if (current.getSize() > largestRemainingFreeSpace.getSize()){	//3. Compare sizes. If current is largest, re-assign largest remaining free space
				largestRemainingFreeSpace = current;
			}
		}
  	}
	
	//Checks for free space in the rear of the new free space.
	public void checkRearFreeSpace(){
		for(int i = 0; i < freeSpaceList.size(); i++){
			SizeAddressPair current = freeSpaceList.get(i);				
			if((completedJobSize + completedJobAddress) == current.getAddress()){	//check for free space after the job
				completedJobSize += current.getSize();
				freeSpaceList.remove(current);	
			}
		}		
	}
	
	//This is a new method to cut down on Tro interrupts. whenever a job is added or removed, we call this and it sets the shortest time slice.
	public void findShortestTimeSlice(){
		if(!jobsAddressed.isEmpty()){
			Job shortestJob = jobsAddressed.get(0);
			int minimumTimeSlice = shortestJob.getMaxCpuTime();
			if(jobsAddressed.size() > 1){
				for (int i = 1; i < jobsAddressed.size(); i++){
					Job current = jobsAddressed.get(i);
					int currentTimeSlice = current.getMaxCpuTime();
					if(currentTimeSlice < minimumTimeSlice){
						minimumTimeSlice = currentTimeSlice;
					}
				}
			}
			shortestTimeSlice = minimumTimeSlice;
		}
	}
	
	//Sorts our addressed job. When I swap, I use a BEST FIT algorithm to replace the smallest possible job.
	public void sortJobsAddressed(){
		for (int i = 0; i < jobsAddressed.size() - 1; i++){
			Job iJob = jobsAddressed.get(i);
			int min = i;
			for (int j = i + 1; j < jobsAddressed.size(); j++){
				Job jJob = jobsAddressed.get(j);
				if (jJob.getJobSize() < iJob.getJobSize()){
					min = j;
				}
			}
			if(min != i){
				Job temp = jobsAddressed.get(i);
				jobsAddressed.set(i, jobsAddressed.get(min));
				jobsAddressed.set(min, temp);
			}
		}
	}
	
	//returns true if there is a job available that the calling job can swap out with. Assigns that job.
	public boolean canSwap(Job jobSwappingIn){
		sortJobsAddressed();
		for(int i = 0; i < jobsAddressed.size(); i++){
			Job current = jobsAddressed.get(i);
			if(current.getJobSize() > jobSwappingIn.getJobSize() && (!current.getIOFlag() && !current.getBlockFlag())){
				swapJob = current;
				return true;
			}
		}
		return false;
	}
	
	public Job getSwapJob(){return swapJob;}
	public int getShortestTimeSlice(){return shortestTimeSlice;}
}
