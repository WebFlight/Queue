package queue.tests;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.QueueControlMessageFetcherExecutor;
import queue.utilities.CoreUtility;

public class TestQueueControlMessageFetcherExecutor {
	
	private ILogNode logger = mock(ILogNode.class);
	private IContext context = mock(IContext.class);
	private QueueControlMessageFetcherExecutor queueControlMessageFetcherExecutor;
	private CoreUtility coreUtility = mock(CoreUtility.class);
	private final String XASId = "ABCDEabcde12345";
	private IMendixObject queueControlMessage = mock(IMendixObject.class);
	@SuppressWarnings({ "rawtypes" })
	private HashMap inputMap = mock(HashMap.class);
	private IMendixIdentifier identifier = mock(IMendixIdentifier.class);
	private long identifierLong = 12345678L;

	@Before
	public void setup() throws CoreException {
		this.queueControlMessageFetcherExecutor = new QueueControlMessageFetcherExecutor();
		List<IMendixObject> queueControlMessages = new ArrayList<>();
		queueControlMessages.add(queueControlMessage);
		when(coreUtility.retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + this.XASId + "']")).thenReturn(queueControlMessages);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecute() throws Exception {
		queueControlMessageFetcherExecutor.execute(context, coreUtility, logger, inputMap);
		verify(coreUtility, times(1)).retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + this.XASId + "']");
		verify(inputMap, times(1)).put("QueueControlMessage", queueControlMessage);
		verify(coreUtility, times(1)).executeAsync(context, "Queue.IVK_ProcessQueueControlMessage", true, inputMap);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteException() throws Exception {
		Exception e = new CoreException();
		when(coreUtility.executeAsync(context, "Queue.IVK_ProcessQueueControlMessage", true, inputMap)).thenThrow(e);
		when(queueControlMessage.getId()).thenReturn(identifier);
		when(identifier.toLong()).thenReturn(identifierLong);
		
		queueControlMessageFetcherExecutor.execute(context, coreUtility, logger, inputMap);
		
		verify(coreUtility, times(1)).retrieveXPathQuery(context, "//Queue.QueueControlMessage[Queue.QueueControlMessage_XASInstance/System.XASInstance/XASId='" + this.XASId + "']");
		verify(inputMap, times(1)).put("QueueControlMessage", queueControlMessage);
		verify(coreUtility, times(1)).executeAsync(context, "Queue.IVK_ProcessQueueControlMessage", true, inputMap);
		verify(logger, times(1)).error("Could not process Queue Control Message with ID " + identifierLong + ".", e);
	}

}
