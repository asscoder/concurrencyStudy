package ch5;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<Integer>();
		
		for (int i = 0; i < 10; ++i) {
			 list.add((int)(10 * Math.random()));
		}
		
		System.out.println(list);
		
		Iterator<Integer> i = list.iterator();
		try {
			while (i.hasNext()) {
				if (i.next() % 2 == 0) i.remove();
			}
		} catch (Exception e) {
			System.out.println(e.getClass().getName());
		}

		System.out.println(list);
		
		Iterator<Integer> i2 = list.iterator();
		while (i2.hasNext()) {
			Integer v = i2.next();
			if (v % 3 ==  0) list.remove(v);
		}

		System.out.println(list);
	}
	

}
