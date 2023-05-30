package qsl.internal.json;

import java.io.Serial;
import java.io.Serializable;

public enum Environment implements Serializable {
	CLIENT_ONLY("client"),
	DEDICATED_SERVER_ONLY("dedicated_server"),
	ANY("*");

	@Serial
	// increment when changing this class to properly invalidate the generateQmj task
	private static final long serialVersionUID = 1L;
	public final String qmj;

	Environment(String qmj) {
		this.qmj = qmj;
	}
}
