package qsl.internal.json;

import java.io.IOException;

import org.gradle.api.Action;
import org.quiltmc.json5.JsonWriter;

public final class ModJsonObject {
	private final JsonWriter writer;

	public ModJsonObject(JsonWriter writer) {
		this.writer = writer;
	}

	public void string(String key, String value) throws IOException {
		this.writer.name(key);
		this.writer.value(value);
	}

	public void bool(String key, boolean value) throws IOException {
		this.writer.name(key);
		this.writer.value(value);
	}

	public void jnull(String key) throws IOException {
		this.writer.name(key);
		this.writer.nullValue();
	}

	public void number(String key, Number number) throws IOException {
		this.writer.name(key);
		this.writer.value(number);
	}

	public void array(String key, Action<ModJsonArray> action) throws IOException {
		this.writer.name(key);
		this.writer.beginArray();
		action.execute(new ModJsonArray(this.writer));
		this.writer.endArray();
	}

	public void object(String key, Action<ModJsonObject> action) throws IOException {
		this.writer.name(key);
		this.writer.beginObject();
		action.execute(new ModJsonObject(this.writer));
		this.writer.endObject();
	}
}
