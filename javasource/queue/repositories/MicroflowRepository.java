package queue.repositories;

import java.util.Map;
import java.util.Set;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IDataType;

public class MicroflowRepository {
	
	public Set<String> getMicroflowNames() {
		return Core.getMicroflowNames();
	}
	
	public Map<String, IDataType> getInputParameters(String microflowName) {
		return Core.getInputParameters(microflowName);
	}
}
