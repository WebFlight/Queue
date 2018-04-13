package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import queue.helpers.MicroflowValidator;
import queue.repositories.MicroflowRepository;

public class TestMicroflowValidator {
	
	MicroflowRepository microflowRepository = mock(MicroflowRepository.class);

	@Test
	public void validateTrue() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		microflowNames.add(existingMicroflow);
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate(existingMicroflow);
		
		assertTrue(actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
	}
	
	@Test
	public void validateFalse() {
		Set<String> microflowNames = new HashSet<>();
		String existingMicroflow = "ExistingMicroflow";
		microflowNames.add(existingMicroflow);
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		boolean actualResult = microflowValidator.validate("nonExistingMicroflow");
		
		assertFalse(actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
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
		
		String actualResult = microflowValidator.getClosestMatch("AlmostExistingMicroflow");
		
		assertEquals(existingMicroflow, actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
	}
	
	@Test
	public void getClosestMatchNoMicroflowsExist() {
		Set<String> microflowNames = new HashSet<>();
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		String actualResult = microflowValidator.getClosestMatch("NonExistingMicroflow");
		
		assertEquals("", actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
	}

	@Test
	public void getClosestMatchOrderChanges() {
		Set<String> microflowNames = new HashSet<>();

		microflowNames.add("StrangeFlow");
		microflowNames.add("AostExistingMicroflow");
		microflowNames.add("AlostExisticroflow");
		microflowNames.add("AlostExistasdfjklasdfngMicroflow");
		
		when(microflowRepository.getMicroflowNames()).thenReturn(microflowNames);
		
		MicroflowValidator microflowValidator = new MicroflowValidator(microflowRepository);
		
		String actualResult = microflowValidator.getClosestMatch("AlmostExistingMicroflow");
		
		assertEquals("AostExistingMicroflow", actualResult);
		verify(microflowRepository, times(1)).getMicroflowNames();
	}
}
