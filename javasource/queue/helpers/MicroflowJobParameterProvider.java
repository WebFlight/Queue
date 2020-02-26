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
		String parameterName = inputParameters.entrySet().stream().filter(p -> Core.getMetaObject(p.getValue().getObjectType()).isSubClassOf(Core.getMetaObject("Queue.Job"))).findFirst().get().getKey();
		
		return parameterName;
	}
	
}
