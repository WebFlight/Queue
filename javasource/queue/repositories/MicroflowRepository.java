package queue.repositories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.MicroflowJobParameterProvider;

public class MicroflowRepository {
	
	public Set<String> getMicroflowNames() {
		return Core.getMicroflowNames();
	}
	
	public Map<String, IDataType> getInputParameters(String microflowName) {
		return Core.getInputParameters(microflowName);
	}
	
	public HashMap<String, Object> getJobInput(IMendixObject jobObject, String microflowName) {
		MicroflowJobParameterProvider microflowJobParameterProvider = new MicroflowJobParameterProvider(this);
		String jobParameterName = microflowJobParameterProvider.getJobParameterName(microflowName);
		
		HashMap<String, Object> jobInput = new HashMap<>();
		jobInput.put(jobParameterName, jobObject);
		
		return jobInput;
	}
}
