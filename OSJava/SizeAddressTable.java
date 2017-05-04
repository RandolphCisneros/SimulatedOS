import java.util.*;

class SizeAddressTable {
	
  public static final int MAX_FREE_SPACE = 99;
  public static final int START_OF_MEMORY = 0;

  private static SizeAddressPair largestRemainingFreeSpace;    //initialize to 0,100. This tracks the largest remaining free space
  LinkedList<SizeAddressPair> freeSpaceList = new LinkedList<SizeAddressPair>();//data structure which tracks remaining free spaces
  LinkedList<Job> jobsAddressed = new LinkedList<Job>();	//Going to use this to track the job addresses
  
  //constructor initializes largest free space and adds it to the table.
  //I used a default constructor here since it will only be used once in the OS
  public SizeAddressTable(){
    largestRemainingFreeSpace = new SizeAddressPair(MAX_FREE_SPACE, START_OF_MEMORY);	//There should only be one instance of this
    freeSpaceList.add(largestRemainingFreeSpace);
  }
  
  //this method finds free space for the requesting job. I'm using WORST FIT algorithm so it relies on the largest
  //remaining free space. It returns an error if there is no memory for the job size.
    /*			commenting out because I didn't use this in the OS code
  public int findFreeSpace(int JobSize){
      try { if(largestRemainingFreeSpace.getSize() > jobSize)
              return largestREmainingFreeSpace.getAddress();
          }
    catch (IndexOutOfBoundsException e){
        System.out.println("Not enough memory for job " +e.getMessage();)					
    }
  }*/
  
 //This keeps track of the addresses of assigned jobs. If the jobs are removed then we know which address will get free space.
  public boolean assignJob(Job newJob){
	if ((newJob.getJobAddress() < 0) && (largestRemainingFreeSpace.getSize() > newJob.getJobSize())){	//1. Check that we have free space and it's not already assigned
		newJob.setJobAddress(largestRemainingFreeSpace.getAddress());					//2a. If there is, we set the address to the largest remaining free space
		jobsAddressed.add(newJob);									//3. We add it to our list of jobs with addresses
		int newFreeSpaceSize = largestRemainingFreeSpace.getSize() - newJob.getJobSize();
		largestRemainingFreeSpace.setSize(newFreeSpaceSize);		//4. We subtract the size of the job from the "largest remaining free space"
		largestRemainingFreeSpace.setAddress(largestRemainingFreeSpace.getAddress() + newJob.getJobSize());	//5. We changed the address of the "largest remaining free space" to the previous address + the size of the added job
		findNewLargestRemainingFreeSpace();																			//6. We find the real largest remaining freespace
		return true;																													//7. Return true for O.S. to allocate it to the correct queue
	}
	else if (newJob.getJobAddress() < 0 && largestRemainingFreeSpace.getSize() < newJob.getJobSize()){	//2b. If there's not enough space, throw message and return false. O.S. puts it in waiting queue.
		//The problem here is that as I get more jobs I need to swap in and out. This will  have to be changed.
		System.out.println("Not enough space for job");
		return false;
	}
	else{
		System.out.println("Job already assigned");																		//2c. This shouldn't happen, but if it's already assigned then it will return false.
		return false;
	}	
  }
  
  //this function will be used when a job is terminated; it will removed from the list and the free space will be re-allocated
  public void removeJob(Job completedJob){

	if (jobsAddressed.contains(completedJob)){
		int completedJobSize = completedJob.getJobSize();
		int completedJobAddress = completedJob.getJobAddress();
		for(int i = 0; i < freeSpaceList.size(); i++){
			SizeAddressPair current = freeSpaceList.get(i);				
			if((completedJobSize + completedJobAddress) == current.getAddress()){	//check for free space after the job
				completedJobSize += current.getSize();
				freeSpaceList.remove(current);
			}
		}
		for(int j = 0; j < freeSpaceList.size(); j++){
			SizeAddressPair current = freeSpaceList.get(j);
			if ((current.getAddress() + current.getSize()) == completedJobAddress){		//check for free space before the job
				current.setSize(current.getSize() + completedJobSize);
				freeSpaceList.remove(completedJob);
				return;
			}
		}
		SizeAddressPair newFreeSpace = new SizeAddressPair(completedJobSize, completedJobAddress);
		freeSpaceList.add(newFreeSpace);
		findNewLargestRemainingFreeSpace();		//finds the new largest remaining free space after
  
	}
  }
  
  //After a job has been assigned, this function will be used to find and assign the new largest remaining free space
  public void findNewLargestRemainingFreeSpace(){
	for (int i = 0; i < freeSpaceList.size(); i++){				//1. Iterate through freeSpaceList
		SizeAddressPair current = freeSpaceList.get(i);			//2. Create pointer to object
		if (current.getSize() > largestRemainingFreeSpace.getSize())	//3. Compare sizes. If current is largest, re-assign largest remaining free space
			largestRemainingFreeSpace = current;
	}
 }
}
