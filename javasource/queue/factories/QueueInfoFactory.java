package queue.factories;

import com.mendix.systemwideinterfaces.core.IContext;

import queue.proxies.QueueInfo;

public class QueueInfoFactory {
	
	public QueueInfo newQueueInfo(IContext context) {
		return new QueueInfo(context);
	}
}
