class SizeAddressTable {

  SizeAddressPair largestRemainingFreeSpace;    //initialize to 0,100. This tracks the largest remaining free space
  LinkedList<SizeAddressPair> freeSpaceList = new LinkedList<SizeAddressPair>();//data structure which tracks remaining free spaces
  LinkedList<Job> jobsAddressed = new LinkedList<Job>();	//Going to use this to track the job addresses
  
  //constructor initializes largest free space and adds it to the table.
  //I used a default constructor here since it will only be used once in the OS
  public SizeAddressTable(){
    largestRemainingFreeSpace.setPair(0,100);
    freeSpaceList.add(largestRemainingFreeSpace);
  }
  
  //this method finds free space for the requesting job. I'm using WORST FIT algorithm so it relies on the largest
  //remaining free space. It returns an error if there is no memory for the job size.
  public int findFreeSpace(int JobSize){
      try { if(largestRemainingFreeSpace.getSize() > jobSize)
              return largestREmainingFreeSpace.getAddress();
          }
    catch (IndexOutOfBoundsException e){
        System.out.println("Not enough memory for job " +e.getMessage());)					
    }
  }
  
  //I need somewhere to put the job data, mainly so I know when free space pops up in
  //later on and I can assign more free spaces to the free space list.
  public void assignJob(Job newJob){
	if (newJob.getAddress() == 0){
		newJob.setJobAddress(largestRemainingFreeSpace.getAddress());
		jobsAddressed.add(newJob);
		largestRemainingFreeSpace.setSize(largestRemaingFreeSpace.getSize() - newJob.getJobSize());
		largestRemainingFreeSpace.setAddress(largestRemainingFreeSpace.getAddress() + newJob.getJobSize());
		findNewLargestRemainingFreeSpace();
	}
	else
		System.out.println("Job already assigned");			//change to try catch
  }
  
  //this function will be used when a job is terminated; it will removed from the list and the free space will be re-allocated
  public void removeJob(Job completedJob){

	if (jobsAddressed.contains(completedJob)){
		int completedJobSize = completedJob.getSize();
		int completedJobAddress = completedJob.getAddress();
		for(int i = 0; i < freeSpaceList.size(); i++){
			SizeAddressPair current = freeSpaceList.get(i);				
			if((completedJobSize + completedJobAddress) == current.getAddress()){	//check for free space after the job
				completedJobSize += current.getSize();
				freeSpaceList.remove(current);
			}
		}
		for(int j = 0; j < freeSpaceList.size(); j++){
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
  
  //After a job has been assigned, this function will be used to find the new largest remaining free space
  //and set it
  public void findNewLargestRemainingFreeSpace(){
	for (int i = 0; i < freeSpaceList.size(); i++){
		SizeAddressPair current = freeSpaceList.get(i);
		if (current.getSize() > largestRemainingFreeSpace.getSize())
			largestRemainingFreeSpace = current;
		}
	}


}