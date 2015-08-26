package base.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.Donation;

public class SortUtils {
	
	private SortUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static Map<Long, Donation.DonationsByPfp> sortDonationsByPfp(Map<Long, Donation.DonationsByPfp> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<Long, Donation.DonationsByPfp>> list = 
			new LinkedList<Map.Entry<Long, Donation.DonationsByPfp>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Long, Donation.DonationsByPfp>>() {
			public int compare(Map.Entry<Long, Donation.DonationsByPfp> left,
                                           Map.Entry<Long, Donation.DonationsByPfp> right) {
	            int cmp1 = Integer.compare(right.getValue().amount, left.getValue().amount);
	    		if (cmp1 != 0) {
	    			return cmp1;
	    		} else {
	    			return left.getValue().name.compareTo(right.getValue().name);
	    		}
			}
		});
 
		// Convert sorted map back to a Map
		Map<Long, Donation.DonationsByPfp> sortedMap = new LinkedHashMap<Long, Donation.DonationsByPfp>();
		for (Iterator<Map.Entry<Long, Donation.DonationsByPfp>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Long, Donation.DonationsByPfp> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static Map<Long, Donation.DonationsByTeam> sortDonationsByTeam(Map<Long, Donation.DonationsByTeam> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<Long, Donation.DonationsByTeam>> list = 
			new LinkedList<Map.Entry<Long, Donation.DonationsByTeam>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Long, Donation.DonationsByTeam>>() {
			public int compare(Map.Entry<Long, Donation.DonationsByTeam> left,
                                           Map.Entry<Long, Donation.DonationsByTeam> right) {
	            int cmp1 = Integer.compare(right.getValue().amount, left.getValue().amount);
	    		if (cmp1 != 0) {
	    			return cmp1;
	    		} else {
	    			return left.getValue().name.compareTo(right.getValue().name);
	    		}
			}
		});
 
		// Convert sorted map back to a Map
		Map<Long, Donation.DonationsByTeam> sortedMap = new LinkedHashMap<Long, Donation.DonationsByTeam>();
		for (Iterator<Map.Entry<Long, Donation.DonationsByTeam>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Long, Donation.DonationsByTeam> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static Map<String, String> sortDonationsByName(Map<String, String> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<String, String>> list = 
			new LinkedList<Map.Entry<String, String>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> left,
                                           Map.Entry<String, String> right) {
	    		return left.getValue().compareTo(right.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		for (Iterator<Map.Entry<String, String>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
}
