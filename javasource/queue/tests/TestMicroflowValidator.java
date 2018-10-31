package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Mockito;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IDataType.DataTypeEnum;

import queue.helpers.MicroflowValidator;
import queue.repositories.MicroflowRepository;

public class TestMicroflowValidator {
	
	MicroflowRepository microflowRepository = mock(MicroflowRepository.class);
	ILogNode logger = mock(ILogNode.class);
	@SuppressWarnings("rawtypes")
	Map inputParameterMap = mock(Map.class);
	@SuppressWarnings("rawtypes")
	Collection collection = mock(Collection.class);
	@SuppressWarnings({ "unchecked" })
	Stream<IDataType> stream = mock(Stream.class);

	@SuppressWarnings("unchecked")
	@Test
	public void validateTrue() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		microflowNames.add(existingMicroflow);
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		when(microflowRepository.getInputParameters(existingMicroflow)).thenReturn(inputParameterMap);
		when(inputParameterMap.values()).thenReturn(collection);
		when(collection.stream()).thenReturn(stream);
		when(stream.anyMatch(Mockito.any())).thenReturn(true);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate(existingMicroflow, logger);
		
		assertTrue(actualResult);
		verify(logger, times(1)).debug("Validating microflow " + existingMicroflow + ": microflow exists.");
	}
	
	@Test
	public void validateFalse() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		microflowNames.add(existingMicroflow);
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate("nonExistingMicroflow", logger);
		
		assertFalse(actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
	}
	
	@Test
	public void validateFalseEmpty() {
		Set<String> microflowNames = new HashSet<>();
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate("nonExistingMicroflow", logger);
		
		assertFalse(actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
		verify(logger, times(1)).error("Microflow nonExistingMicroflow could not be found.");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateFalseNoObjectInput() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		microflowNames.add(existingMicroflow);
	
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		when(microflowRepository.getInputParameters(existingMicroflow)).thenReturn(inputParameterMap);
		when(inputParameterMap.values()).thenReturn(collection);
		when(collection.stream()).thenReturn(stream);
		when(stream.anyMatch(Mockito.any())).thenReturn(false);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate("ExistingMicroflow", logger);
		
		assertFalse(actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
		verify(logger, times(1)).error("Validating microflow ExistingMicroflow: has no input parameter with data type Object and Entity Queue.Job.");
	}
	
	@Test
	public void hasInputParameterOfTypeObjectTrueTrue() {
		Map<String, IDataType> inputParameters = new HashMap<>();
		IDataType dataType = mock(IDataType.class);
		inputParameters.put("Param", dataType);
		
		when(dataType.getObjectType()).thenReturn("Queue.Job");
		when(dataType.getType()).thenReturn(DataTypeEnum.Object);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.hasInputParameterOfTypeObject(inputParameters);
		
		assertTrue(actualResult);
	}
	
	@Test
	public void hasInputParameterOfTypeObjectTrueFalse() {
		Map<String, IDataType> inputParameters = new HashMap<>();
		IDataType dataType = mock(IDataType.class);
		inputParameters.put("Param", dataType);
		
		when(dataType.getObjectType()).thenReturn("Queue.Job");
		when(dataType.getType()).thenReturn(DataTypeEnum.String);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.hasInputParameterOfTypeObject(inputParameters);
		
		assertFalse(actualResult);
	}
	
	@Test
	public void hasInputParameterOfTypeObjectFalseTrue() {
		Map<String, IDataType> inputParameters = new HashMap<>();
		IDataType dataType = mock(IDataType.class);
		inputParameters.put("Param", dataType);
		
		when(dataType.getObjectType()).thenReturn("Queue.WrongJob");
		when(dataType.getType()).thenReturn(DataTypeEnum.Object);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.hasInputParameterOfTypeObject(inputParameters);
		
		assertFalse(actualResult);
	}
	
	@Test
	public void hasInputParameterOfTypeObjectFalseFalse() {
		Map<String, IDataType> inputParameters = new HashMap<>();
		IDataType dataType = mock(IDataType.class);
		inputParameters.put("Param", dataType);
		
		when(dataType.getObjectType()).thenReturn("Queue.WrongJob");
		when(dataType.getType()).thenReturn(DataTypeEnum.String);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.hasInputParameterOfTypeObject(inputParameters);
		
		assertFalse(actualResult);
	}
	
	@Test
	public void getClosestMatch() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		String anotherMicroflow = "AnotherMicroflow";
		microflowNames.add(existingMicroflow);
		microflowNames.add(anotherMicroflow);
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		String actualResult = microflowValidator.getClosestMatch("AlmostExistingMicroflow", microflowNames);
		
		assertEquals(existingMicroflow, actualResult);
	}
	
	@Test
	public void getClosestMatchNoMicroflowsExist() {
		Set<String> emptyMicroflowNames = new HashSet<>();
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		String actualResult = microflowValidator.getClosestMatch("NonExistingMicroflow", emptyMicroflowNames);
		
		assertEquals("", actualResult);
	}

	@Test
	public void getClosestMatchOrderChanges() {
		Set<String> microflowNames = new HashSet<>();

		microflowNames.add("StrangeFlow");
		microflowNames.add("AostExistingMicroflow");
		microflowNames.add("AlostExisticroflow");
		microflowNames.add("AlostExistasdfjklasdfngMicroflow");
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		String actualResult = microflowValidator.getClosestMatch("AlmostExistingMicroflow", microflowNames);
		
		assertEquals("AostExistingMicroflow", actualResult);
	}
}
