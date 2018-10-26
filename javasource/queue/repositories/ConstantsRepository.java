package queue.repositories;

import queue.proxies.constants.Constants;

public class ConstantsRepository {
	
	public boolean isClusterSupport() {
		return Constants.getCLUSTER_SUPPORT();
	}

}
