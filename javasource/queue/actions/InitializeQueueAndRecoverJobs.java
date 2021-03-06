// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package queue.actions;

import java.util.HashMap;
import java.util.Map;
import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import static queue.proxies.microflows.Microflows.aSU_InitializeQueue;

/**
 * Use this action to initialize a Queue during the After startup microflow in your application. If you do not use this action in the after startup microflow, the action cannot guarantee initialization of the Queue on all instances (relevant for multi-instance setup). 
 * 
 * The action will look for Jobs that have not been completed (Open, Queued or Running) and add those to the Queue that has been initialized.
 */
public class InitializeQueueAndRecoverJobs extends CustomJavaAction<java.lang.Boolean>
{
	private java.lang.String name;
	private java.lang.Long poolSize;
	private java.lang.Long priority;

	public InitializeQueueAndRecoverJobs(IContext context, java.lang.String name, java.lang.Long poolSize, java.lang.Long priority)
	{
		super(context);
		this.name = name;
		this.poolSize = poolSize;
		this.priority = priority;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		return aSU_InitializeQueue(this.context(), name, poolSize, priority);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "InitializeQueueAndRecoverJobs";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
