package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.helpers.QueueControlMessageFetcherExecutor;
import queue.utilities.CoreUtility;

public class TestQueueControlMessageFetcher {
	
	private ILogNode logger = mock(ILogNode.class);
	private IContext context = mock(IContext.class);
	private QueueControlMessageFetcherExecutor queueControlMessageFetcherExecutor;
	private CoreUtility coreUtility = mock(CoreUtility.class);
	private final String XASId = "ABCDEabcde12345";

	@Before
	public void setup() {
		this.queueControlMessageFetcherExecutor = new QueueControlMessageFetcherExecutor();
		when(coreUtility.getXASId()).thenReturn(XASId);
	}
	
	@Test
	public void test() throws Exception {
		queueControlMessageFetcherExecutor.execute(context, coreUtility, logger);
		verify(coreUtility, times(1)).retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + this.XASId + "']");
	}

}
