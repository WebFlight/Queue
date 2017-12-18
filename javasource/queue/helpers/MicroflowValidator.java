package queue.helpers;

import java.util.Iterator;
import java.util.Set;

import com.mendix.core.Core;

public class MicroflowValidator {
	
	public boolean validate(String microflowName) {
		return Core.getMicroflowNames().contains(microflowName);
	}
	
	public String getClosestMatch(String microflowName) {
		String microflowSuggestion = "";
		Set<String> microflowNames = Core.getMicroflowNames();
		
		if (microflowNames.size() == 0) {
			return "";
		}
		
		Iterator<String> it = microflowNames.iterator();
		int minDistance = 0;
		
		
		while(it.hasNext()) {
			String microflowNameToCompare = it.next();
			int distance = computeLevenshteinDistance(microflowName, microflowNameToCompare);
				
			if (minDistance == 0) {
				microflowSuggestion = microflowNameToCompare;
				minDistance = distance; 
				continue;
			}
			
			if (distance < minDistance) {
				microflowSuggestion = microflowNameToCompare;
				minDistance = distance;
				continue;
			}
		}
		
		return microflowSuggestion;
	}

	private int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {      
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];        
                                                                                 
        for (int i = 0; i <= lhs.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= rhs.length(); j++)                                 
            distance[0][j] = j;                                                  
                                                                                 
        for (int i = 1; i <= lhs.length(); i++)                                 
            for (int j = 1; j <= rhs.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));
                                                                                 
        return distance[lhs.length()][rhs.length()];                           
    }
	
	private int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }   
}
