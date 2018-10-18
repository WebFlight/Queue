package queue.factories;

import com.mendix.core.Core;

import queue.helpers.QueueInfoUpdater;
import queue.proxies.constants.Constants;

public class QueueInfoUpdaterFactory {
	
	public QueueInfoUpdaterFactory() {
	
	}
	
	public QueueInfoUpdater<?> getQueueInfoUpdater() {
		return new QueueInfoUpdater<>(Core.getLogger(Constants.getLOGNODE()),Core.createSystemContext());
	}
}
