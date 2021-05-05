# Mendix Queue module

Welcome to the Mendix Queue module. This module can be used in [Mendix](http://www.mendix.com) apps to run jobs in single or multi threaded job queues. The app uses the Java ScheduledThreadPoolExecutor for queue management. The Queue contains an intelligent retry mechanism using the exponential backoff algorithm. Minimum database interactions are implemented to increase performance.

![Queue logo][1]

# Table of Contents

* [Demo](#demo)
* [Getting Started](#getting-started)
* [Features](#features)
* [Job configuration](#job-configuration)
* [Security roles](#security-roles)
* [Not implemented](#not-implemented)
* [Logging](#logging)
* [Dependencies](#dependencies)
* [Development notes](#development-notes)

# Demo
Check the demo application at https://queue100.mxapps.io and login using **demo_user** and **hXPC3NKz49**.

For an Administrator role account, please request at [Menno de Haas](mailto:menno.dehaas@webflight.nl). An Administrator can shutdown a queue, which makes it unavailable for others to experiment with.

# Getting started
1. The *Queue* module can be downloaded from within the Mendix Business Modeler in the Mendix Appstore into any model that is build with Mendix 7.13.1+.
2. Use the documents in the _USE_ME folder or the actions in the Queue section of the toolbox:
	- The InitializeQueueAndRecoverJobs action will initiate a new queue, with a specific name, thread pool size, thread priority. Use this in a startup microflow. Otherwise this will lead to issues when running a multi-instance cluster.
	- The AddJobToQueue action will add a new job in a queue that has been initialized before. Make sure the microflow to be executed has an input parameter with data type Object and entity Queue.Job or an entity that has Queue.Job as a Generalization. If you would like to enter the microflow name as a String, use the AddJobToQueueTextToMicroflow action in the private folder. This action extracts the name from the MicroflowName attribute in the Job.
	- Add the snippets or pages to your application for queue or job monitoring. Do not forget to assign the module roles to the relevant project security roles.
3. Set the CLUSTER_SUPPORT constant if you are using the queue in an application that runs on a cluster of multiple instances. When cluster support is enabled, each instance will check for new queue control messages in the background that correspond to that Instance Index. When performing control activities from the front-end (shutdown queue, cancel or abort job), these will create queue control messages in the database that will be processed by the relevant instances. In addition, the QueueInfo will be updated from each instance independently.
4. Set the TIMEZONE_ID constant if you would like to use a specific time zone during executions of microflows from the queue. By default, GMT will be used. This feature is equivalent to the setting [Scheduled event time zone](https://docs.mendix.com/refguide/project-settings#3-8-scheduled-event-time-zone), but applies only to microflows executed by the Queue. [Google Java TimeZoneIDs](http://lmgtfy.com/?q=Java+TimeZone+IDs) to browse possible values or check your System.TimeZone database table (the Code attribute equals the Java TimeZoneID). The value for The Netherlands should be Europe/Amsterdam.
5. Set the USEDST_IFAPPLICABLE constant to true if you would like to use Daylight Saving Time. This will only correct the session time in case Daylight Saving Time is present and active in the timezone set with the TIMEZONE_ID constant.
6. If you would like to set a Job's status to Error, use the SetErrorForJob action. Changing the Job's Status attribute in the microflow will not have any effect if the SetErrorForJob Java action is not used.

# Features
* Queue control
	- Shutdown queue
	- Forced shutdown queue
	- Java does not allow to reuse a queue that has been shut down. Queue should be re-initialized after shutdown.
* Queue displayed in UI based on queue instance state (not stored in database)
* Job retry mechanism when Job fails (delay calculated based on exponential backoff)
* Throw errors from microflow in queue with or without stack trace (use the SetErrorForJob Java action)
* Job is cancelled (cancel or abort depending on status) using a before commit event in case Job is deleted
* Job control UI
	- Cancel Job when queued
	- Cancel Job when running (abort)
	- Clean Jobs
	- Add Jobs to Queue when Open, Done, Cancelled or Error

# Job configuration
To add a Job to the queue, create a new object of the *Job* entity. The Job entity has the following attributes:

| Attribute | Description |
|------|------|
| IdJob | AutoNumber of the Job. This value is not internally used by the Queue logic. |
| Queue | Name of the Queue to add the Job to. This name should be identical to the name used in the IVK_InitializeQueue microflow. |
| MicroflowName | The full name of the microflow to execute when the Job runs. For instance: Module.QUE_MicroflowName. The microflow will run within a transaction and from sudo/admin context, which means the $currentUser token returns null. The microflow should have a Job entity input parameter (name of parameter is irrelevant), which can be used for retrieval of associated objects. When the name of the microflow is incorrectly spelled, the log will display a suggestion for the closest match. |
| Retry | The number of times the Job has been retried. By default set to 0. Will be increased when microflow returns an error while executing. |
| MaxRetries | The maximum number of times the Job is rescheduled when an error occurs. Can be set to an arbitrary positive integer. |
| Description | Free format text for description of the Job. |
| CurrentDelay | The (minimum) delay when Job becomes available for the next execution in the queue. Value will be set by the Queue based on BaseDelay and Retry. |
| BaseDelay | BaseDelay is used for the calculation of exponential backoff and can be adjusted according to preference. The next delay equals BaseDelay x 2 ^ Retry. Default value is 500. Delay for the first retry equals  500 x 2 ^ 1 = 500, the second 500 x 2 ^ 2 = 2000 and third 500 x 2 ^ 3 = 4000 until maxRetries is reached. |
| DelayUnit | The TimeUnit used for delay calculations. Default is Milliseconds. |
| Status | The status of the Job. The Queue logic will change the status according to result and should not be set by the user. |
| InstanceIndex | The index of the instance the Job is running on. Will be set when the Job is added to a Queue. Instance index -1 means the index could not be detected. This can occur due a variety of reasons, for instance when running locally or when running in Sandbox mode (the Sandbox Java policy does not allow access to the environment variable CF_INSTANCE_INDEX). Ohterwise, instance index 0 is the master instance running all scheduled events. When using multiple instances, index > 0 are slave instances. Value should not be changed by the user. |

# Security roles
The module contains two security roles:

* Administrator
	- All rights
* User
	- Cannot delete Jobs
	- Cannot abort running jobs
	- Cannot clean jobs

# Not implemented
* Job audit trail (can be easily implemented by adding logic or implement a specialization of the Job entity)
* Initialization of a Queue in the UI as this is not fool proof
* Jobs that return an error and are rescheduled, will be added to the end of the queue. Java implements a BlockingQueue (in contrast to a BlockingDequeue), which does not allow entries to be added to the head of the queue. If a FIFO mechanism is required, cancel and re-add all Jobs in the correct order in case of an error or implement the retry mechanism inside the microflow instead of using the retry mechanism provided by the Queue.

# Logging
* The *JobQueue* log node is available for logging. Set to Debug or Trace for more details.

# Dependencies
No userlib compile dependencies.

# Development notes
* Functionality is tested using JUnit and Mockito. The *queue.helpers* and *queue.usecase* packages contain business logic and have a 100% coverage.
* For contributions, fork the repository, make changes, fix unit tests with 100% coverage and issue a pull request to the develop branch (Gitflow).

 [1]: docs/Queue.png
