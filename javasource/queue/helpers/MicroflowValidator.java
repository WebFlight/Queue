package queue.helpers;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IDataType.DataTypeEnum;

import queue.repositories.MicroflowRepository;

public class MicroflowValidator {
	
	private MicroflowRepository microflowRepository;
	
	public MicroflowValidator(MicroflowRepository microflowRepository) {
		this.microflowRepository = microflowRepository;
	}
	
	public boolean validate(String microflowName, ILogNode logger) {
		boolean isValid = microflowExists(microflowName, logger) &&
				hasInputParameterJob(microflowName, logger);
				
				return isValid;
	}
		
	public boolean microflowExists(String microflowName, ILogNode logger) {
		Set<String> microflowNames = microflowRepository.getMicroflowNames();
		boolean exists = microflowNames.contains(microflowName);
		
		if(exists) {
			logger.debug("Validating microflow " + microflowName + ": microflow exists.");
			return true;
		}
		
		String microflowSuggestion = getClosestMatch(microflowName, microflowNames);
		
		if  (microflowSuggestion.equals("")) {
			logger.error("Microflow " + microflowName + " could not be found.");
			return false;
		}
		
		logger.error("Microflow " + microflowName + " could not be found. Did you mean " + microflowSuggestion + "?");
		return false;
	}
	
	public boolean hasInputParameterJob(String microflowName, ILogNode logger) {
		Map<String, IDataType> inputParameters = microflowRepository.getInputParameters(microflowName);
		boolean hasInputParameterOfTypeObject = hasInputParameterOfTypeObject(inputParameters);
		
		if (hasInputParameterOfTypeObject) {
			logger.debug("Validating microflow " + microflowName + ": has input parameter with data type Object and entity Queue.Job.");
		}
		
		if (!hasInputParameterOfTypeObject) {
			logger.error("Validating microflow " + microflowName + ": has no input parameter with data type Object and Entity Queue.Job.");
		}
		
		return hasInputParameterOfTypeObject;
	}
	
	public boolean hasInputParameterOfTypeObject(Map<String, IDataType> inputParameters) {
		return inputParameters.values().stream().anyMatch(p -> p.getObjectType().equals("Queue.Job") && p.getType() == DataTypeEnum.Object);
	}
	
	public String getClosestMatch(String microflowName, Set<String> microflowNames) {
		String microflowSuggestion = "";
		
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
