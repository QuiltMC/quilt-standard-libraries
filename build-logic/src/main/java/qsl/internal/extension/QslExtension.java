package qsl.internal.extension;

import org.gradle.api.Project;
import qsl.internal.Versions;

public class QslExtension {
	private static final String RESOLVE_QUILTFLOWER_TASK = "resolveQuiltflower";
	private static final String GEN_SOURCES_WITH_QUILTFLOWER_TASK = "genSourcesWithQuiltflower";
	private static final String[] TASKS_TO_DISABLE = {RESOLVE_QUILTFLOWER_TASK, "genSourcesWithFernFlower"};
	protected final Project project;

	public QslExtension(Project project) {
		this.project = project;

		this.project.afterEvaluate(p -> {
			for (var task : TASKS_TO_DISABLE) {
				p.getTasks().findByName(task).setEnabled(false);
			}

			p.getTasks().findByName(GEN_SOURCES_WITH_QUILTFLOWER_TASK).dependsOn(p.getRootProject().getTasks().findByName(RESOLVE_QUILTFLOWER_TASK));
		});
	}

	public void setVersion(String version) {
		this.project.setVersion(version + '+' + Versions.MINECRAFT_VERSION.version()
				+ (System.getenv("SNAPSHOTS_URL") != null ? "-SNAPSHOT" : ""));
	}
}
