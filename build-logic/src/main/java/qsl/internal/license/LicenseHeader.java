package qsl.internal.license;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.jetbrains.annotations.Nullable;
import qsl.internal.Git;

/**
 * Represents a license header.
 */
public class LicenseHeader {
	private static final String YEAR_KEY = "$YEAR";
	private static final String MATCH_FROM_KEY = ";;match_from: ";
	private final List<Rule> rules;

	public LicenseHeader(Rule... rules) {
		this.rules = List.of(rules);
	}

	public LicenseHeader(List<Rule> rules) {
		this.rules = new ArrayList<>(rules);
	}

	public boolean validate(Path path) {
		String source = readFile(path);

		for (var rule : this.rules) {
			if (rule.match(source)) {
				return rule.validate(source);
			}
		}

		return false;
	}

	public boolean format(Project project, Path rootPath, Path path) {
		String source = readFile(path);

		for (var rule : this.rules) {
			if (rule.match(source)) {
				return rule.formatFile(project, rootPath, path);
			}
		}

		return false;
	}

	private static String escapeRegexControl(String input) {
		return input.replace("(", "\\(")
				.replace(")", "\\)")
				.replace(".", "\\.")
				.replace("/", "\\/");
	}

	private static Pattern getValidator(String headerFormat) {
		String pattern = escapeRegexControl(headerFormat)
				.replace(YEAR_KEY, "(\\d{4}(, \\d{4})*)");
		String[] lines = getHeaderLines(pattern.split("\n"));
		var patternBuilder = new StringBuilder("\\/\\*\n");

		for (var line : lines) {
			if (line.isEmpty()) {
				patternBuilder.append(" \\*\n");
			} else {
				patternBuilder.append(" \\* ").append(line).append('\n');
			}
		}

		patternBuilder.append(" \\*\\/\n\n");

		return Pattern.compile("^" + patternBuilder);
	}

	private static @Nullable Pattern getMatcher(String headerFormat) {
		String[] lines = headerFormat.split("\n");
		var match = new StringBuilder();

		for (var line : lines) {
			if (line.startsWith(MATCH_FROM_KEY)) {
				if (!match.isEmpty()) {
					match.append('|');
				}
				match.append('(').append(line.substring(MATCH_FROM_KEY.length())).append(')');
			}
		}

		if (match.isEmpty()) {
			return null;
		}

		return Pattern.compile("^" + match);
	}

	private static String[] getHeaderLines(String[] lines) {
		int max = lines.length;
		for (int i = lines.length - 1; i > 0; i--) {
			if (lines[i].startsWith(";;") || lines[i].isEmpty()) {
				max = i;
			} else {
				break;
			}
		}

		var headerLines = new String[max];
		System.arraycopy(lines, 0, headerLines, 0, max);
		return headerLines;
	}

	private static String readFile(Path path) {
		byte[] bytes;

		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			throw new GradleException(String.format("Failed to load file %s", path), e);
		}

		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static int getYearStrict(Project project) {
		RevCommit latestCommit = Git.getLatestCommit(project);

		if (latestCommit == null) {
			return Calendar.getInstance().get(Calendar.YEAR);
		} else {
			PersonIdent authorIdent = latestCommit.getAuthorIdent();
			Date authorDate = authorIdent.getWhen();
			TimeZone authorTimeZone = authorIdent.getTimeZone();

			var calendar = Calendar.getInstance(authorTimeZone);
			calendar.setTime(authorDate);
			return calendar.get(Calendar.YEAR);
		}
	}

	/**
	 * Represents a license header rule, it describes one valid license header format.
	 */
	public static class Rule {
		private final String headerFormat;
		private final Pattern validator;
		private final @Nullable Pattern matcher;

		public Rule(String headerFormat) {
			this.headerFormat = headerFormat;
			this.validator = getValidator(headerFormat);
			this.matcher = getMatcher(headerFormat);
		}

		public static Rule fromFile(Path path) {
			byte[] bytes;

			try {
				bytes = Files.readAllBytes(path);
			} catch (IOException e) {
				throw new GradleException(String.format("Failed to load license header %s", path), e);
			}

			return new Rule(new String(bytes, StandardCharsets.UTF_8));
		}

		/**
		 * Returns whether this rule has a special file matching.
		 *
		 * @return {@code true} if this rule has a special file matching, otherwise {@code false}
		 */
		public final boolean hasSpecialMatching() {
			return this.matcher != null;
		}

		/**
		 * Returns whether the file's licensing should respect this rule.
		 *
		 * @param source the source of the file
		 * @return {@code true} if the file's licensing should respect this rule, otherwise {@code false}
		 */
		public boolean match(String source) {
			if (!this.hasSpecialMatching()) {
				return true;
			}

			if (this.matcher.matcher(source).find()) {
				return true;
			} else {
				return this.validator.matcher(source).find();
			}
		}

		public boolean validate(String source) {
			return this.validator.matcher(source).find();
		}

		public boolean formatFile(Project project, Path rootPath, Path path) {
			String source = readFile(path);
			String year = this.getYearString(project, source);

			int delimiter = source.indexOf("package");
			if (delimiter != -1) {
				var newSource = this.getLicenseString(year) + source.substring(delimiter);

				if (newSource.equals(source)) {
					return false;
				}

				var backupPath = getBackupPath(project, rootPath, path);

				if (backupPath == null) {
					throw new GradleException("Cannot backup file " + path + ", abandoning formatting.");
				}

				try {
					if (!Files.isDirectory(backupPath.getParent())) {
						Files.createDirectories(backupPath.getParent());
					}

					Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new GradleException("Cannot backup file " + path + ", abandoning formatting.", e);
				}

				try {
					Files.writeString(path, newSource);
				} catch (IOException e) {
					throw new GradleException("Failed to write updated file " + path + ", abandoning formatting.", e);
				}

				return true;
			}

			return false;
		}

		private String getLicenseString(String year) {
			var builder = new StringBuilder();
			var lines = getHeaderLines(this.headerFormat.replace(YEAR_KEY, year).split("\n"));

			builder.append("/*\n");
			for (var line : lines) {
				if (line.isEmpty()) {
					builder.append(" *\n");
				} else {
					builder.append(" * ").append(line).append('\n');
				}
			}

			builder.append(" */\n\n");

			return builder.toString();
		}

		private String getYearString(Project project, String source) {
			int lastModifiedYear = getYearStrict(project);

			var matcher = this.validator.matcher(source);

			if (matcher.matches()) {
				String[] serializedYears = matcher.group(1).split("\n");

				int min = -1;
				for (var serializedYear : serializedYears) {
					try {
						int year = Integer.parseInt(serializedYear);

						if (min == -1 || year < min) {
							min = year;
						}
					} catch (NumberFormatException ignored) {
						// ignore
					}
				}

				if (min == -1) {
					return String.valueOf(lastModifiedYear);
				} else {
					return min + "-" + lastModifiedYear;
				}
			}

			return String.valueOf(lastModifiedYear);
		}
	}

	private static @Nullable Path getBackupPath(Project project, Path rootPath, Path path) {
		var backupDir = new File(project.getBuildDir(), "qsl/formatter");
		backupDir.mkdirs();
		var pathAsString = path.toAbsolutePath().toString();
		var rootPathAsString = rootPath.toString();

		if (pathAsString.startsWith(rootPathAsString)) {
			return backupDir.toPath().resolve(Paths.get(pathAsString.substring(rootPathAsString.length() + 1)))
					.normalize();
		}

		return null;
	}
}
