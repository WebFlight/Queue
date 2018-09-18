package queue.utilities;

import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

public class CoreUtility {
	
	public List<IMendixObject> retrieveXPathQuery(IContext context, String xPath) throws CoreException {
		return Core.retrieveXPathQuery(context, xPath);
	}

}
