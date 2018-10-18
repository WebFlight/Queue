package queue.tests;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mendix.logging.ILogNode;

import queue.factories.QueueControlMessageFetcherFactory;
import queue.factories.QueueInfoUpdaterFactory;
import queue.helpers.ClusterSupportInitializer;
import queue.helpers.QueueInfoUpdater;
import queue.repositories.ConstantsRepository;
import queue.utilities.CoreUtility;

import static org.mockito.Mockito.*;


public class TestClusterSupportInitializer {
	
	private ConstantsRepository constantsRepository = mock(ConstantsRepository.class);
	private CoreUtility coreUtility = mock(CoreUtility.class);
	private QueueControlMessageFetcherFactory queueControlMessageFetcherFactory = mock(QueueControlMessageFetcherFactory.class);
	private QueueInfoUpdaterFactory queueInfoUpdaterFactory = mock(QueueInfoUpdaterFactory.class);
	@SuppressWarnings("rawtypes")
	private QueueInfoUpdater queueInfoUpdater = mock(QueueInfoUpdater.class);
	private ILogNode logger = mock(ILogNode.class);
	
	@Test
	public void initializeWithClusterSupport() {
		when(constantsRepository.isClusterSupport()).thenReturn(true);
		when(queueInfoUpdaterFactory.getQueueInfoUpdater()).thenReturn(queueInfoUpdater);
		ClusterSupportInitializer clusterSupportInitializer = new ClusterSupportInitializer(logger, constantsRepository, coreUtility, queueInfoUpdaterFactory, queueControlMessageFetcherFactory);
		clusterSupportInitializer.initialize();
		verify(constantsRepository, times(1)).isClusterSupport();
	}
	
	@Test
	public void initializeWithoutClusterSupport() {
		when(constantsRepository.isClusterSupport()).thenReturn(false);
	}

}
