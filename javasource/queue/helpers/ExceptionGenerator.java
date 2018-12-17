package queue.helpers;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.UserException;
import com.mendix.systemwideinterfaces.core.UserException.ExceptionCategory;

public class ExceptionGenerator {
	
	public void throwException(boolean includeStacktrace, String message) {
		if(includeStacktrace) {
			throw new MendixRuntimeException(message);
		}
		
		throw new UserException(ExceptionCategory.Custom, message);
	}

}
