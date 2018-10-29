package queue.repositories;

import queue.proxies.constants.Constants;

public class ConstantsRepository {
	
	public boolean isClusterSupport() {
		return Constants.getCLUSTER_SUPPORT();
	}
	
	public String getTimeZoneID() {
		return Constants.getTIMEZONE_ID();
	}

}
