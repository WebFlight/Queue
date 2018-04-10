package queue.helpers;

import com.mendix.logging.ILogNode;

import queue.entities.QueueConfiguration;
import queue.factories.QueueThreadFactory;
import queue.factories.QueueThreadPoolFactory;
import queue.repositories.QueueRepository;

public class QueueInitializer {
	
	public boolean initialize(ILogNode logger, QueueConfiguration configuration, QueueThreadPoolFactory threadPoolFactory, QueueThreadFactory threadFactory, QueueValidator queueValidator, QueueRepository queueRepository) {
			boolean valid = queueValidator.isValid(queueRepository, configuration.getName(), configuration.getCorePoolSize(), configuration.getPriority());
		
			if (valid == false) {
				return false;
			}
			
			queueRepository.newQueue(configuration, threadPoolFactory, threadFactory);
			
			logger.info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
			
			return true;
	}
}
