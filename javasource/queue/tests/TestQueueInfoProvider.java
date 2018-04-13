package queue.tests;

import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Test;

import com.mendix.systemwideinterfaces.core.IContext;

import queue.factories.QueueInfoFactory;
import queue.helpers.QueueInfoProvider;
import queue.proxies.QueueInfo;

public class TestQueueInfoProvider {

	IContext context = mock(IContext.class);
	@SuppressWarnings({ "unchecked" })
	ConcurrentHashMap<String, ScheduledThreadPoolExecutor> queueMap = mock(ConcurrentHashMap.class);
	QueueInfoFactory queueInfoFactory = mock(QueueInfoFactory.class);
	@SuppressWarnings("rawtypes")
	Set entrySet = mock(Set.class);
	@SuppressWarnings("rawtypes")
	Iterator iterator = mock(Iterator.class);
	@SuppressWarnings("rawtypes")
	Entry entry = mock(Entry.class);
	ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
	@SuppressWarnings("rawtypes")
	BlockingQueue blockingQueue = mock(BlockingQueue.class);
	QueueInfo queueInfo = mock(QueueInfo.class);
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetQueueInfo() {
		QueueInfoProvider queueInfoProvider = new QueueInfoProvider(queueInfoFactory);
		
		when(queueMap.entrySet()).thenReturn(entrySet);
		when(entrySet.iterator()).thenReturn(iterator);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(entry);
		when(entry.getValue()).thenReturn(executor);
		when(entry.getKey()).thenReturn("TestQueue");
		when(executor.isShutdown()).thenReturn(false);
		when(executor.isTerminated()).thenReturn(false);
		when(executor.getActiveCount()).thenReturn(9);
		when(executor.getCorePoolSize()).thenReturn(8);
		when(executor.getPoolSize()).thenReturn(8);
		when(executor.getCompletedTaskCount()).thenReturn(4821L);
		when(executor.getTaskCount()).thenReturn(1354L);
		when(executor.getQueue()).thenReturn(blockingQueue);
		when(blockingQueue.size()).thenReturn(15);
		when(queueInfoFactory.newQueueInfo(context)).thenReturn(queueInfo);
		
		queueInfoProvider.getQueueInfo(context, queueMap);
		
		verify(queueInfo, times(1)).setIsTerminated(false);
		verify(queueInfo, times(1)).setName("TestQueue");
		verify(queueInfo, times(1)).setIsShutDown(false);
		verify(queueInfo, times(1)).setActiveThreads(9);
		verify(queueInfo, times(1)).setCorePoolSize(8);
		verify(queueInfo, times(1)).setPoolSize(8);
		verify(queueInfo, times(1)).setCompletedJobCount(4821L);
		verify(queueInfo, times(1)).setTotalJobCount(1354L);
		verify(queueInfo, times(1)).setJobsInQueue(15);	
	}
}
