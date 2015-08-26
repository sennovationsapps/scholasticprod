package base.utils;

import java.util.Iterator;
import java.util.Map;

public class DebugUtils {
	
	private DebugUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static void callout() {
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
	}
	
	public static void printMap(Map<String, String> map) {
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
		    String key = (String) iterator.next();
		    System.out.println(key + " -> " + map.get(key));
		}
	}
}
