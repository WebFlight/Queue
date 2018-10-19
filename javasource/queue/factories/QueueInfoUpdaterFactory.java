package queue.factories;

import com.mendix.core.Core;

import queue.helpers.QueueInfoUpdaterExecutor;
import queue.proxies.constants.Constants;
import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;
import queue.utilities.QueueInfoUpdater;

public class QueueInfoUpdaterFactory {
	
	public QueueInfoUpdaterFactory() {
	
	}
	
	public QueueInfoUpdater<?> getQueueInfoUpdater() {
		CoreUtility coreUtility = new CoreUtility();
		QueueRepository queueRepository = QueueRepository.getInstance();
		QueueInfoUpdaterExecutor queueInfoUpdaterExecutor = new QueueInfoUpdaterExecutor();
		return new QueueInfoUpdater<>(Core.getLogger(Constants.getLOGNODE()),Core.createSystemContext(), coreUtility, queueRepository, queueInfoUpdaterExecutor);
	}
}
