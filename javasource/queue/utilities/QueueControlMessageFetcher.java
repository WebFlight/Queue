package queue.utilities;

import java.util.HashMap;
import java.util.Map;

import com.mendix.core.actionmanagement.CoreAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.helpers.QueueControlMessageFetcherExecutor;

public class QueueControlMessageFetcher<R> extends CoreAction<R> {
	
	private ILogNode logger;
	private CoreUtility coreUtility;
	private QueueControlMessageFetcherExecutor queueControlMessageFetcherExecutor;

	public QueueControlMessageFetcher(ILogNode logger, IContext context, CoreUtility coreUtility, QueueControlMessageFetcherExecutor queueControlMessageFetcherExecutor) {
		super(context);
		this.logger = logger;
		this.coreUtility = coreUtility;
		this.queueControlMessageFetcherExecutor = queueControlMessageFetcherExecutor;
	}

	@Override
	public R execute() throws Exception {
		Map<String, Object> inputMap = new HashMap<>();
		queueControlMessageFetcherExecutor.execute(this.getContext(), coreUtility, logger, inputMap);
		return null;
	}
	
}
