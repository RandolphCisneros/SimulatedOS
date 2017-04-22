class SizeAddressTable {

  SizeAddressPair largestRemainingFreeSpace;    //initialize to 0,100. This tracks the largest remaining free space
  LinkedList<SizeAddressPair> freeSpaceList = new LinkedList<SizeAddressPair>();//data structure which tracks remaining free spaces
  
  //constructor initializes largest free space and adds it to the table.
  //I used a default constructor here since it will only be used once in the OS
  SizeAddressTable(){
    largestRemainingFreeSpace.setPair(0,100);
    freeSpaceList.push(largestRemainingFreeSpace);
  }
  
  //this method finds free space for the requesting job. I'm using WORST FIT algorithm so it relies on the largest
  //remaining free space. It returns an error if there is no memory for the job size.
  public int findFreeSpace(int JobSize){
      try { if(largestRemainingFreeSpace.getSize() > jobSize)
              return largestREmainingFreeSpace.getAddress();
          }
    catch (largestRemainingFreeSpace.getSize() < jobSize){
        System.out.println("Not enough memory for job")
    }
  }
  
  //I need somewhere to put the job data, mainly so I know when free space pops up in
  //later on and I can assign more free spaces to the free space list.
  public void assignJob(Job newJob){
  //add job to address space?
  }
  
  public void findLargestRemainingFreeSpace


}
