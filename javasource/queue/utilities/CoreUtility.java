package queue.utilities;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.constants.Constants;

public class CoreUtility {
	
	public List<IMendixObject> retrieveXPathQuery(IContext context, String xPath) throws CoreException {
		return Core.retrieveXPathQuery(context, xPath);
	}
	
	public String getXASId() {
		return Core.getXASId();
	}
	
	public void scheduleAtFixedRate(CoreAction<?> coreAction, long initialDelay, long period, java.util.concurrent.TimeUnit timeUnit) {
		Core.scheduleAtFixedRate(coreAction, initialDelay, period, timeUnit);
	}
	
	public Future<Object> executeAsync(IContext context, String action, boolean inTransaction, Map<String, Object> inputMap) throws CoreException {
		return Core.executeAsync(context, action, inTransaction, inputMap);
	}
	
	public void commit(IContext context, List<IMendixObject> objectList) {
		Core.commit(context, objectList);
	}

	public void delete(IContext context, List<IMendixObject> objectList) {
		Core.delete(context, objectList);
	}
	
	public long getInstanceIndex() {
		ILogNode logger = Core.getLogger(Constants.getLOGNODE());
		
		long cfInstanceIndex = -1L;
		
		try {
			cfInstanceIndex = Long.parseLong(System.getenv("CF_INSTANCE_INDEX"));
		} catch(SecurityException securityException) {
			logger.info("GetCFInstanceIndex: Could not access environment variable CF_INSTANCE_INDEX, permission denied. Value of -1 is returned.");
		} catch(NumberFormatException numberFormatException) {
			logger.info("GetCFInstanceIndex: Could not parse value of environment variable CF_INSTANCE_INDEX as Long. Value of -1 is returned.");
		} catch(NullPointerException nullPointerException) {
			logger.info("GetCFInstanceIndex: Could not find value for environment variable CF_INSTANCE_INDEX. This could indicate a local deployment is running. Value of -1 is returned.");
		}
		
		return cfInstanceIndex;
	}
}
