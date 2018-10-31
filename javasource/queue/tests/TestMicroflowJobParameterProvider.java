package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.mendix.systemwideinterfaces.core.IDataType;

import queue.helpers.MicroflowJobParameterProvider;
import queue.repositories.MicroflowRepository;

public class TestMicroflowJobParameterProvider {
	
	MicroflowRepository microflowRepository = mock(MicroflowRepository.class);
	IDataType dataType = mock(IDataType.class);
	MicroflowJobParameterProvider microflowJobParameterProvider = new MicroflowJobParameterProvider(microflowRepository);

	@Test
	public void getJobParameterName() {
		String microflowName = "Microflow";
		String expectedInputParameterName = "InputParam";
		
		Map<String, IDataType> inputParameters = new HashMap<>();
		inputParameters.put(expectedInputParameterName, dataType);
		
		when(microflowRepository.getInputParameters(microflowName)).thenReturn(inputParameters);
		when(dataType.getObjectType()).thenReturn("Queue.Job");
		
		String actualInputParameterName = microflowJobParameterProvider.getJobParameterName(microflowName);
		
		assertEquals(actualInputParameterName, expectedInputParameterName);
	}
	
}
