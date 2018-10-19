package queue.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

	@Before
	public void setup() throws CoreException {
		this.queueInfoUpdaterExecutor = new QueueInfoUpdaterExecutor();
		when(coreUtility.getXASId()).thenReturn(XASId);
		List<IMendixObject> queueInfos = new ArrayList<>();
		queueInfos.add(queueInfo);
		when(queueRepository.getQueueInfos(context)).thenReturn(queueInfos);
		List<IMendixObject> xasInstances = new ArrayList<>();
		xasInstances.add(xasInstance);
		when(coreUtility.retrieveXPathQuery(context, "//System.XASInstance[XASId='" + XASId + "']")).thenReturn(xasInstances);
		when(xasInstance.getId()).thenReturn(identifier);
		when(identifier.toLong()).thenReturn(identifierLong);
	}
	
	@Test
	public void testExecute() throws Exception {
		queueInfoUpdaterExecutor.execute(context, logger, queueRepository, coreUtility);
	}
	
	@Test
	public void testExecuteException() throws Exception {
		
	}

	
}
