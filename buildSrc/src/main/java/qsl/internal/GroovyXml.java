package qsl.internal;

import groovy.util.Node;
import groovy.xml.QName;

public final class GroovyXml {
	public static Node getOrCreateNode(Node parent, String name) {
		for (Object object : parent.children()) {
			if (object instanceof Node && isSameName(((Node) object).name(), name)) {
				return (Node) object;
			}
		}

		return parent.appendNode(name);
	}

	@SuppressWarnings("deprecation")
	private static boolean isSameName(Object nodeName, String givenName) {
		if (nodeName instanceof String) {
			return nodeName.equals(givenName);
		}

		// xml QName
		if (nodeName instanceof QName) {
			return ((QName) nodeName).matches(givenName);
		}

		// New groovy 3 (gradle 7) class
		if (nodeName instanceof groovy.namespace.QName) {
			return ((groovy.namespace.QName) nodeName).matches(givenName);
		}

		throw new UnsupportedOperationException("Cannot determine if " + nodeName.getClass() + " is the same as a String");
	}

	private GroovyXml() {
	}
}
