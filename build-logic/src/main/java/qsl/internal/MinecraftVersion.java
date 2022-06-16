package qsl.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record MinecraftVersion(String version, String versionEdition) {
	private static final Pattern RELEASE_PATTERN = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?$");
	private static final Pattern PRE_RELEASE_PATTERN = Pattern.compile(".+(?:-pre| Pre-[Rr]elease )(\\d+)$");
	private static final Pattern RELEASE_CANDIDATE_PATTERN = Pattern.compile(".+(?:-rc| [Rr]elease Candidate )(\\d+)$");
	private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(?:Snapshot )?(\\d+)w0?(0|[1-9]\\d*)([a-z])$");
	private static final Pattern EXPERIMENTAL_PATTERN = Pattern.compile("(?:.*[Ee]xperimental [Ss]napshot )(\\d+)");

	public MinecraftVersion(String version) {
		this(version, version);
	}

	public boolean isSnapshot() {
		return SNAPSHOT_PATTERN.matcher(this.version).matches();
	}

	public String getSemVer() {
		Matcher matcher;

		if ((matcher = SNAPSHOT_PATTERN.matcher(this.version)).matches()) {
			return this.versionEdition + String.format("-alpha.%s.%s.%s", matcher.group(1), matcher.group(2), matcher.group(3));
		} else if ((matcher = PRE_RELEASE_PATTERN.matcher(this.version)).matches()) {
			return this.version.substring(0, this.version.indexOf("-") + 1) + String.format("beta.%s", matcher.group(1));
		} else if ((matcher = RELEASE_CANDIDATE_PATTERN.matcher(this.version)).matches()) {
			return this.version.substring(0, this.version.indexOf("-") + 1) + String.format("rc.%s", matcher.group(1));
		} else if (RELEASE_PATTERN.matcher(this.version).matches()) {
			return this.version;
		} else {
			throw new IllegalArgumentException("Cannot give a semver-compliant string for version " + this.version);
		}
	}

	public String getFancyString() {
		if (this.isSnapshot()) {
			return this.version;
		}

		String[] version = this.version.split("\\.");

		int index;
		if ((index = version[1].indexOf("-pre")) != -1 || (index = version[1].indexOf("-rc")) != -1) {
			version[1] = version[1].substring(0, index);
		}

		return version[0] + '.' + version[1];
	}
}
