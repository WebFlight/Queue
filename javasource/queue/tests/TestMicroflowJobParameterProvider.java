package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;

import queue.helpers.MicroflowJobParameterProvider;
import queue.repositories.MicroflowRepository;

public class TestMicroflowJobParameterProvider {
	
	MicroflowRepository microflowRepository = mock(MicroflowRepository.class);
	IDataType dataType = mock(IDataType.class);
	IMetaObject metaObject = mock(IMetaObject.class);
	MicroflowJobParameterProvider microflowJobParameterProvider = new MicroflowJobParameterProvider(microflowRepository);

	@Test
	public void getJobParameterName() {
		String microflowName = "Microflow";
		String expectedInputParameterName = "InputParam";
		
		Map<String, IDataType> inputParameters = new HashMap<>();
		inputParameters.put(expectedInputParameterName, dataType);
		
		when(microflowRepository.getInputParameters(microflowName)).thenReturn(inputParameters);
		when(microflowRepository.getMetaObject("Queue.Job")).thenReturn(metaObject);
		when(dataType.getObjectType()).thenReturn("Queue.Job");
		when(metaObject.isSubClassOf(metaObject)).thenReturn(true);
		
		
		String actualInputParameterName = microflowJobParameterProvider.getJobParameterName(microflowName);
		
		assertEquals(actualInputParameterName, expectedInputParameterName);
	}
	
}
