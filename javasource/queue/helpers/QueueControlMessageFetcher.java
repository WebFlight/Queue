package queue.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.constants.Constants;

public class QueueControlMessageFetcher<R> extends CoreAction<R> {
	
	private ILogNode logger;

	public QueueControlMessageFetcher(ILogNode logger, IContext arg0) {
		super(arg0);
	}

	@Override
	public R execute() throws Exception {
		IContext context = this.getContext();
		List<IMendixObject> queueControlMessages = Core.retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + Core.getXASId() + "']");
		queueControlMessages.forEach(o ->
			{
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("QueueControlMessage", o);
				try {
					Core.executeAsync(context, "Queue.IVK_ProcessQueueControlMessage", true, inputMap);
				} catch (CoreException e) {
					logger.error("Could not process Queue Control Message with ID " + o.getId().toLong() + ".", e);
				}
			}
		);
		
		return null;
	}
	
}
