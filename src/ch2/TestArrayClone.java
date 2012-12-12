package ch2;

public class TestArrayClone {
	
	public static void main(String[] args) {
		int[] i1 = {1,2};
		
		int[] i2 = i1.clone();
		int[] i3 = i1;
		System.out.println(i1);
		System.out.println(i2);
		System.out.println(i3);
		
	}

}
