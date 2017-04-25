//This is a simple pair class to be used in the FreeSpaceTable, organized by the WORST FIT 
//algorithm. Hence, size comes first.	-R. Cisneros
public class SizeAddressPair implements Comparable<SizeAddressPair> {
	
	private int size;
	private int address;
	
	SizeAddressPair(int sz, int a){
		size = sz;
		address = a;
	}
	//accessors and mutators	-R.Cisneros
	public int getSize(){return size;}
	public int getAddress(){return address;}
	public void setSize(int sz){size = sz;}
	public void setAddress(int a){address = a;}
	public void setPair(int sz, int a) {size = sz; address = a;}
	
	//I implemented Comparable for sorting purposes. Just learned this in OOP Class -R. Cisneros
	//*****TRACE LATER AND MAKE SURE ITS RIGHT!!!!!!!
	public int compareTo(SizeAddressPair sAP){
		if(size == sAP.getSize()){
			if(address == sAP.getAddress())
				return 0;
			return address - sAP.getAddress();
		}
		return size - sAP.getSize();
	}
	
}