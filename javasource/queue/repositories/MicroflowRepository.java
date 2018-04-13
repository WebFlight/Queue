package queue.repositories;

import java.util.Set;

import com.mendix.core.Core;

public class MicroflowRepository {
	
	public Set<String> getMicroflowNames() {
		return Core.getMicroflowNames();
	}
}
