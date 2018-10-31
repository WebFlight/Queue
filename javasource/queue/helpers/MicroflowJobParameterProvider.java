package queue.helpers;

import java.util.Map;

import com.mendix.systemwideinterfaces.core.IDataType;

import queue.repositories.MicroflowRepository;

public class MicroflowJobParameterProvider {
	
	private MicroflowRepository microflowRepository;
	
	public MicroflowJobParameterProvider (MicroflowRepository microflowRepository) {
		this.microflowRepository = microflowRepository;
	}

	public String getJobParameterName(String microflowName) {
		Map<String, IDataType> inputParameters = microflowRepository.getInputParameters(microflowName);
		String parameterName = inputParameters.entrySet().stream().filter(p -> p.getValue().getObjectType().equals("Queue.Job")).findFirst().get().getKey();
		
		return parameterName;
	}
	
}
