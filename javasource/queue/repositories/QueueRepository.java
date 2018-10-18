package queue.repositories;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.entities.QueueConfiguration;
import queue.factories.QueueControlMessageFetcherFactory;
import queue.factories.QueueInfoFactory;
import queue.factories.QueueInfoUpdaterFactory;
import queue.factories.QueueThreadFactory;
import queue.factories.QueueThreadPoolFactory;
import queue.helpers.ClusterSupportInitializer;
import queue.helpers.JobToQueueAdder;
import queue.helpers.QueueInfoProvider;
import queue.proxies.constants.Constants;
import queue.usecases.QueueHandler;
import queue.utilities.CoreUtility;

public final class QueueRepository {
	
	private static QueueRepository queueRepository;
	private static final Object lock = new Object();
	private volatile ConcurrentHashMap<String, ScheduledThreadPoolExecutor> queueMap = new ConcurrentHashMap<>();
	private QueueInfoProvider queueInfoProvider = new QueueInfoProvider(new QueueInfoFactory());
	
	private QueueRepository() {
		ConstantsRepository constantsRepository = new ConstantsRepository();
		CoreUtility coreUtility = new CoreUtility();
		QueueInfoUpdaterFactory queueInfoUpdaterFactory = new QueueInfoUpdaterFactory();
		QueueControlMessageFetcherFactory queueControlMessageFetcherFactory = new QueueControlMessageFetcherFactory();
		ILogNode logger = Core.getLogger(Constants.getLOGNODE());
		ClusterSupportInitializer clusterSupportInitializer = new ClusterSupportInitializer(logger, constantsRepository, coreUtility, queueInfoUpdaterFactory, queueControlMessageFetcherFactory);
		clusterSupportInitializer.initialize();
	}
	
	public static QueueRepository getInstance() {
		QueueRepository instance = queueRepository;
		
		if(instance == null) {
			synchronized(lock) {
				instance = queueRepository;
				if (instance == null) {
					instance = new QueueRepository();
					queueRepository = instance;
				}
			}
		}
		
		return instance;
	}
	
	public void newQueue (QueueConfiguration configuration, QueueThreadPoolFactory poolFactory, QueueThreadFactory threadFactory) {
		queueMap.put(configuration.getName(), (ScheduledThreadPoolExecutor) poolFactory.newScheduledThreadPool(configuration, threadFactory));
	}
	
	public ScheduledExecutorService getQueue(String name) {
		return queueMap.get(name);
	}
	
	public boolean queueExists(String name) {
		return queueMap.keySet().contains(name);
	}
	
	public List<IMendixObject> getQueueInfos(IContext context) {
		return queueInfoProvider.getQueueInfo(context, queueMap);
	}
	
	public QueueHandler getQueueHandler(ILogNode logger, JobToQueueAdder jobToQueueAdder, ScheduledJobRepository scheduledJobRepository, QueueRepository queueRepository, JobRepository jobRepository, IMendixIdentifier jobId) {
		return new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobId);
	}
	
	public IContext getSystemContext() {
		return Core.createSystemContext();
	}
}