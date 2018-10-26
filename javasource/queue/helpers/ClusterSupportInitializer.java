package queue.helpers;

import java.util.concurrent.TimeUnit;

import com.mendix.logging.ILogNode;

import queue.factories.QueueControlMessageFetcherFactory;
import queue.factories.QueueInfoUpdaterFactory;
import queue.repositories.ConstantsRepository;
import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class ClusterSupportInitializer {
	
	private ConstantsRepository constantsRepository;
	private CoreUtility coreUtility;
	private QueueInfoUpdaterFactory queueInfoUpdaterFactory;
	private QueueControlMessageFetcherFactory queueControlMessageFetcherFactory;
	private ILogNode logger;
	private QueueRepository queueRepository;
	
	public ClusterSupportInitializer (ILogNode logger, ConstantsRepository constantsRepository, CoreUtility coreUtility, QueueInfoUpdaterFactory queueInfoUpdaterFactory, QueueControlMessageFetcherFactory queueControlMessageFetcherFactory, QueueRepository queueRepository) {
		this.constantsRepository = constantsRepository;
		this.coreUtility = coreUtility;
		this.queueInfoUpdaterFactory = queueInfoUpdaterFactory;
		this.queueControlMessageFetcherFactory = queueControlMessageFetcherFactory;
		this.logger = logger;
		this.queueRepository = queueRepository;
	}
	
	public void initialize () {
		if (constantsRepository.isClusterSupport()) {
			coreUtility.scheduleAtFixedRate(queueInfoUpdaterFactory.getQueueInfoUpdater(queueRepository), 10L, 5L, TimeUnit.SECONDS);
			coreUtility.scheduleAtFixedRate(queueControlMessageFetcherFactory.getQueueControlMessageFetcher(logger, coreUtility), 10L, 5L, TimeUnit.SECONDS);
			logger.info("Support enabled for Mendix Clustered Runtime.");
		}
	}
}