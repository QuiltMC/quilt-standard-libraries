package qsl.internal.json;

import java.io.IOException;

import org.gradle.api.Action;
import org.quiltmc.json5.JsonWriter;

public final class ModJsonArray {
	private final JsonWriter writer;

	public ModJsonArray(JsonWriter writer) {
		this.writer = writer;
	}

	public void string(String value) throws IOException {
		this.writer.value(value);
	}

	public void bool(boolean value) throws IOException {
		this.writer.value(value);
	}

	public void jnull() throws IOException {
		this.writer.nullValue();
	}

	public void number(Number number) throws IOException {
		this.writer.value(number);
	}

	public void array(Action<ModJsonArray> action) throws IOException {
		this.writer.beginArray();
		action.execute(new ModJsonArray(this.writer));
		this.writer.endArray();
	}

	public void object(Action<ModJsonObject> action) throws IOException {
		this.writer.beginObject();
		action.execute(new ModJsonObject(this.writer));
		this.writer.endObject();
	}
}
