/**
 * 
 */
package de.csw.ontology.aood.ontometrics.oh;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ralph
 */
public class Test {

	/**
	 * 
	 */
	public Test() {
		Map<String, Integer> ages = new HashMap<>();
		ages.put("Timothy", 9);
		ages.put("Broccoli", 9);
		ages.put("Mark", 47);
		
		ages.forEach(Birthday::celebrateBirthday);
	}
	
	
	static class Birthday {
		public static void celebrateBirthday(String name, int age) {
			System.out.println(name + " turned " + (age + 1) + " today!");
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> myList = Arrays.asList("element1","element2","element3");
		myList.forEach(element -> System.out.println(element));
		System.out.println();
		new Test();
	}

}
