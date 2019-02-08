package queue.helpers;

import com.mendix.logging.ILogNode;

import queue.entities.QueueConfiguration;
import queue.factories.QueueThreadFactory;
import queue.factories.QueueThreadPoolFactory;
import queue.repositories.ConstantsRepository;
import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class QueueInitializer {
	
	public boolean initialize(ILogNode logger, QueueConfiguration configuration, QueueThreadPoolFactory threadPoolFactory, QueueThreadFactory threadFactory, QueueValidator queueValidator, QueueRepository queueRepository, CoreUtility coreUtility, ConstantsRepository constantsRepository) {
			boolean valid = queueValidator.isValid(queueRepository, configuration.getName(), configuration.getCorePoolSize(), configuration.getPriority());
		
			if (valid == false) {
				logger.error("QueueValidator returned false. Queue "+ configuration.getName() + " will not be initialized.");
				return false;
			}
			
			if (constantsRepository.isClusterSupport() == false) {
				if (coreUtility.getInstanceIndex() > 0) {
					logger.warn("Your Mendix application runs on multiple instances. Please consider to set CLUSTER_SUPPORT to true.");
				}
			}
			
			if (constantsRepository.isClusterSupport() == true) {
				if (coreUtility.getInstanceIndex() < 0) {
					logger.warn("Could not detect instance index. Please consider to disable CLUSTER_SUPPORT.");
				}
			}
			
			queueRepository.newQueue(configuration, threadPoolFactory, threadFactory);
			
			logger.info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
			
			return true;
	}
}
