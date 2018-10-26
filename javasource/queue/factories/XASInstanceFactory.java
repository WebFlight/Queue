package queue.factories;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;

import system.proxies.XASInstance;

public class XASInstanceFactory {

	public XASInstance load(IContext context, IMendixIdentifier identifier) throws CoreException {
		return XASInstance.load(context, identifier);
	}
	
}
