package queue.tests;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.JobCanceller;
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;

public class TestJobCanceller {
	
	IMendixObject jobObject = mock(IMendixObject.class);
	IContext context = mock(IContext.class);
	ScheduledJobRepository scheduledJobRepository = mock(ScheduledJobRepository.class);
	Job job = mock(Job.class);
	@SuppressWarnings("rawtypes")
	ScheduledFuture future = mock(ScheduledFuture.class);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@SuppressWarnings("unchecked")
	@Test
	public void cancelJobRemoveWhenRunningTrue() throws CoreException {
		// Given the following behavior of input objects
		when(job.getMendixObject()).thenReturn(jobObject);
		when(scheduledJobRepository.get(context, jobObject)).thenReturn(future);
		when(future.cancel(true)).thenReturn(true);
		
		// When job is the cancel method is invoked with removeWhenRunning = true
		JobCanceller jobCanceller = new JobCanceller();
		boolean actualResult = jobCanceller.cancel(context, scheduledJobRepository, job, true);
		
		// Then the following methods are called
		verify(job, times(1)).getMendixObject();
		verify(scheduledJobRepository, times(1)).get(context, jobObject);
		verify(future, times(1)).cancel(true);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Cancelled);
		verify(job, times(1)).commit(context);
		verify(scheduledJobRepository, times(1)).remove(context, job.getMendixObject());
		
		// And result is true
		assertTrue(actualResult);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void cancelJobRemoveWhenRunningFalse() throws CoreException {
		// Given the following behavior of input objects
		when(job.getMendixObject()).thenReturn(jobObject);
		when(scheduledJobRepository.get(context, jobObject)).thenReturn(future);
		when(future.cancel(false)).thenReturn(false);
		
		// When job is the cancel method is invoked with removeWhenRunning = true
		JobCanceller jobCanceller = new JobCanceller();
		boolean actualResult = jobCanceller.cancel(context, scheduledJobRepository, job, false);
		
		// Then the following methods are called
		verify(job, times(1)).getMendixObject();
		verify(scheduledJobRepository, times(1)).get(context, jobObject);
		verify(future, times(1)).cancel(false);
		verify(job, times(0)).setStatus(context, ENU_JobStatus.Cancelled);
		verify(job, times(0)).commit(context);
		verify(scheduledJobRepository, times(1)).remove(context, job.getMendixObject());
		
		// And result is true
		assertTrue(actualResult);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void cancelJobFutureDoesNotExist() throws CoreException {
		// Given the following behavior of input objects
		when(job.getMendixObject()).thenReturn(jobObject);
		when(scheduledJobRepository.get(context, jobObject)).thenReturn(null);
		
		// When job is the cancel method is invoked with removeWhenRunning = true
		JobCanceller jobCanceller = new JobCanceller();
		
		expectedException.expect(CoreException.class);
		boolean actualResult = jobCanceller.cancel(context, scheduledJobRepository, job, true);
	
		
		// Then the following methods are called
		verify(job, times(1)).getMendixObject();
		verify(scheduledJobRepository, times(1)).get(context, jobObject);
	}
}
