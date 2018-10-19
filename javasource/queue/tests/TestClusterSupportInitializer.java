package queue.tests;

import org.junit.Before;
import org.junit.Test;

import com.mendix.logging.ILogNode;

import queue.factories.QueueControlMessageFetcherFactory;
import queue.factories.QueueInfoUpdaterFactory;
import queue.helpers.ClusterSupportInitializer;
import queue.helpers.QueueControlMessageFetcher;
import queue.helpers.QueueInfoUpdater;
import queue.repositories.ConstantsRepository;
import queue.utilities.CoreUtility;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;


public class TestClusterSupportInitializer {
	
	private ConstantsRepository constantsRepository = mock(ConstantsRepository.class);
	private CoreUtility coreUtility = mock(CoreUtility.class);
	private QueueControlMessageFetcherFactory queueControlMessageFetcherFactory = mock(QueueControlMessageFetcherFactory.class);
	private QueueInfoUpdaterFactory queueInfoUpdaterFactory = mock(QueueInfoUpdaterFactory.class);
	@SuppressWarnings("rawtypes")
	private QueueInfoUpdater queueInfoUpdater = mock(QueueInfoUpdater.class);
	@SuppressWarnings("rawtypes")
	private QueueControlMessageFetcher queueControlMessageFetcher = mock(QueueControlMessageFetcher.class);
	private ILogNode logger = mock(ILogNode.class);
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		when(queueInfoUpdaterFactory.getQueueInfoUpdater()).thenReturn(queueInfoUpdater);
		when(queueControlMessageFetcherFactory.getQueueControlMessageFetcher(logger)).thenReturn(queueControlMessageFetcher);
	}
	
	@Test
	public void initializeWithClusterSupport() {
		when(constantsRepository.isClusterSupport()).thenReturn(true);
		ClusterSupportInitializer clusterSupportInitializer = new ClusterSupportInitializer(logger, constantsRepository, coreUtility, queueInfoUpdaterFactory, queueControlMessageFetcherFactory);
		clusterSupportInitializer.initialize();
		verify(constantsRepository, times(1)).isClusterSupport();
		verify(queueInfoUpdaterFactory, times(1)).getQueueInfoUpdater();
		verify(queueControlMessageFetcherFactory, times(1)).getQueueControlMessageFetcher(logger);
		verify(coreUtility, times(1)).scheduleAtFixedRate(queueInfoUpdater, 10L, 5L, TimeUnit.SECONDS);
		verify(coreUtility, times(1)).scheduleAtFixedRate(queueControlMessageFetcher, 10L, 5L, TimeUnit.SECONDS);
		verify(logger, times(1)).info("Support enabled for Mendix Clustered Runtime.");
	}
	
	@Test
	public void initializeWithoutClusterSupport() {
		when(constantsRepository.isClusterSupport()).thenReturn(false);
		ClusterSupportInitializer clusterSupportInitializer = new ClusterSupportInitializer(logger, constantsRepository, coreUtility, queueInfoUpdaterFactory, queueControlMessageFetcherFactory);
		clusterSupportInitializer.initialize();
		verify(constantsRepository, times(1)).isClusterSupport();
		verify(queueInfoUpdaterFactory, times(0)).getQueueInfoUpdater();
		verify(queueControlMessageFetcherFactory, times(0)).getQueueControlMessageFetcher(logger);
		verify(coreUtility, times(0)).scheduleAtFixedRate(queueInfoUpdater, 10L, 5L, TimeUnit.SECONDS);
		verify(coreUtility, times(0)).scheduleAtFixedRate(queueControlMessageFetcher, 10L, 5L, TimeUnit.SECONDS);
		verify(logger, times(0)).info("Support enabled for Mendix Clustered Runtime.");
	}

}
