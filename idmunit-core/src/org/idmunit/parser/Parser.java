package org.idmunit.parser;

import junit.framework.Test;

public interface Parser {

	Test createSuite(Class<?> testClass);

}
