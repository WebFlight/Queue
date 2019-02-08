package queue.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.QueueInfoUpdaterExecutor;
import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class TestQueueInfoUpdaterExecutor {

	private ILogNode logger = mock(ILogNode.class);
	private IContext context = mock(IContext.class);
	private QueueInfoUpdaterExecutor queueInfoUpdaterExecutor;
	private CoreUtility coreUtility = mock(CoreUtility.class);
	private final String XASId = "ABCDEabcde12345";
	private QueueRepository queueRepository = mock(QueueRepository.class);
	private IMendixObject queueInfo = mock(IMendixObject.class);
	private IMendixObject xasInstance = mock(IMendixObject.class);
	private IMendixIdentifier identifier = mock(IMendixIdentifier.class);
	private long identifierLong = 12345678L;
	private List<IMendixObject> xasInstances;
	private List<IMendixObject> queueInfos;
	@SuppressWarnings("unchecked")
	private List<IMendixObject> oldQueueInfos = mock(List.class);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() throws CoreException {
		this.queueInfoUpdaterExecutor = new QueueInfoUpdaterExecutor();
		queueInfos = new ArrayList<>();
		queueInfos.add(queueInfo);
		when(queueRepository.getQueueInfos(context)).thenReturn(queueInfos);
		xasInstances = new ArrayList<>();
		xasInstances.add(xasInstance);
		when(coreUtility.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + XASId + "']")).thenReturn(xasInstances);
		when(xasInstance.getId()).thenReturn(identifier);
		when(identifier.toLong()).thenReturn(identifierLong);
		when(coreUtility.retrieveXPathQuery(context, "//Queue.QueueInfo[Queue.QueueInfo_XASInstance=" + identifierLong + "]")).thenReturn(oldQueueInfos);
	}
	
	@Test
	public void testExecute() throws Exception {
		queueInfoUpdaterExecutor.execute(context, logger, queueRepository, coreUtility);
		verify(queueRepository, times(1)).getQueueInfos(context);
		verify(coreUtility, times(1)).retrieveXPathQuery(context, "//System.XASInstance[XASId='" + XASId + "']");
		verify(xasInstance, times(1)).getId();
		verify(identifier, times(1)).toLong();
		verify(coreUtility, times(1)).retrieveXPathQuery(context, "//Queue.QueueInfo[Queue.QueueInfo_XASInstance=" + identifierLong + "]");
		verify(coreUtility, times(1)).delete(context, oldQueueInfos);
		verify(queueInfo, times(1)).setValue(context, "Queue.QueueInfo_XASInstance", identifier);
		verify(coreUtility, times(1)).commit(context, queueInfos);
	}
	
	@Test
	public void testExecuteExceptionXAS() throws Exception {
		CoreException e = new CoreException();
		when(coreUtility.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + XASId + "']")).thenThrow(e);
		expectedException.expect(CoreException.class);
		
		queueInfoUpdaterExecutor.execute(context, logger, queueRepository, coreUtility);
	}
	
	@Test
	public void testExecuteExceptionQueueInfos() throws Exception {
		CoreException e = new CoreException();
		when(coreUtility.retrieveXPathQuery(context, "//Queue.QueueInfo[Queue.QueueInfo_XASInstance=" + identifierLong + "]")).thenThrow(e);
		expectedException.expect(CoreException.class);
		
		queueInfoUpdaterExecutor.execute(context, logger, queueRepository, coreUtility);
	}
	
}
