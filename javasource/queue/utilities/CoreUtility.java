package queue.utilities;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

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
}
