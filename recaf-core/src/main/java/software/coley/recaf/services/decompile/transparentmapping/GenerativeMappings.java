package software.coley.recaf.services.decompile.transparentmapping;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import software.coley.recaf.services.mapping.Mappings;
import software.coley.recaf.workspace.model.Workspace;

import java.util.Locale;

/**
 * Mappings which are generated, new names for classes, fields, and methods using a bijective function for easy reversability.
 * 
 * @author Janmm14
 */
public class GenerativeMappings implements Mappings {
	private static final int LOWERCASE_HEX_ALPHA_OFFSET = 'a' - 10; // subtract 10 to shift the value of a-f from 0-5 to 10-15
	static final String PREFIX = "recaf_" + RandomStringUtils.randomAlphanumeric(7) + "_";
	private static final int PREFIX_LENGTH = PREFIX.length();
	private static final String SUFFIX_START = "__"; // hardcoded inside parser of reverseTransformIdentifier
	static final String SUFFIX = SUFFIX_START + RandomStringUtils.randomAlphanumeric(5) + "_facer";
	private static final int SUFFIX_LENGTH = SUFFIX.length();
	private final Workspace workspace;

	public GenerativeMappings(@Nonnull Workspace workspace) {
		this.workspace = workspace;
	}

	@Nullable
	@Override
	public String getMappedClassName(@Nonnull String internalName) {
		if (!workspace.getPrimaryResource().getJvmClassBundle().containsKey(internalName)) {
			if (internalName.startsWith(PREFIX)) {
				throw new IllegalStateException("Owner name already transformed: " + internalName);
			}
			return internalName;
		}
		return transformIdentifier(internalName, true);
	}

	@Nullable
	@Override
	public String getMappedFieldName(@Nonnull String ownerName, @Nonnull String fieldName, @Nonnull String fieldDesc) {
		if (!workspace.getPrimaryResource().getJvmClassBundle().containsKey(ownerName)) {
			if (ownerName.startsWith(PREFIX)) {
				throw new IllegalStateException("Owner name already transformed: " + ownerName);
			}
			return fieldName;
		}
		return transformIdentifier(fieldName, false);
	}

	@Nullable
	@Override
	public String getMappedMethodName(@Nonnull String ownerName, @Nonnull String methodName, @Nonnull String methodDesc) {
		if (!workspace.getPrimaryResource().getJvmClassBundle().containsKey(ownerName)) {
			if (ownerName.startsWith(PREFIX)) {
				throw new IllegalStateException("Owner name already transformed: " + ownerName);
			}
			return methodName;
		}
		if (methodName.equals("<init>") || methodName.equals("<clinit>") || methodName.equals("main")) {
			return methodName;
		}
		return transformIdentifier(methodName, false);
	}

	@Nullable
	@Override
	public String getMappedVariableName(@Nonnull String className, @Nonnull String methodName, @Nonnull String methodDesc,
										@Nullable String name, @Nullable String desc, int index) {
		return name;
	}

	private static boolean isAllowedCharacter(char c) {
		return CharUtils.isAsciiAlphanumeric(c);
	}

	/**
	 * Transform the identifier to a safe identifier.
	 *
	 * @param identifier
	 * 		Identifier to transform.
	 *
	 * @return Transformed identifier.
	 * @implNote Marks with unique prefix and suffix and unsafe characters are encoded as hex values with {@code _} prefix
	 * and padded to 4 characters.
	 */
	@Nonnull
	public static String transformIdentifier(@Nonnull String identifier, boolean isClass) {
		if (identifier.startsWith(PREFIX) || identifier.endsWith(SUFFIX) || identifier.contains(PREFIX + "/" + SUFFIX)) {
			throw new IllegalArgumentException("Identifier already transformed: " + identifier);
		}
		StringBuilder sb = new StringBuilder(PREFIX);
		for (char c : identifier.toCharArray()) {
			if (isAllowedCharacter(c)) {
				sb.append(c);
			} else if (isClass && c == '/') {
				sb.append(SUFFIX).append('/').append(PREFIX);
			} else {
				String hexString = Integer.toHexString(c);

				// hex string of char shouldn't be longer than 4 characters
				if (hexString.length() > 4) throw new IllegalStateException("Hex string too long: " + hexString);
				// hex string should always return lowercase hex
				if (!hexString.toLowerCase(Locale.ROOT).equals(hexString)) throw new IllegalStateException("Hex string not lowercase: " + hexString);

				sb.append('_').append(StringUtils.leftPad(hexString, 4, '0'));
			}
		}
		return sb.append(SUFFIX).toString();
	}

	/**
	 * Reverse the transformation of the identifier. The method expects only transformed identifiers.
	 *
	 * @param identifier
	 * 		Identifier to reverse.
	 *
	 * @return Reversed identifier.
	 * @implNote Removes unique prefix and suffix and decodes hex values from _XX to the original character.
	 */
	public static String reverseTransformIdentifier(String identifier) {
		if (!identifier.startsWith(PREFIX)) throw new IllegalArgumentException("Identifier does not start with prefix: " + identifier);
		if (!identifier.endsWith(SUFFIX)) throw new IllegalArgumentException("Identifier does not end with suffix: " + identifier);

		StringBuilder sb = new StringBuilder();
		int max = identifier.length() - SUFFIX_LENGTH;
		for (int i = PREFIX.length(); i < max; i++) {
			char c = identifier.charAt(i);
			if (c == '_') {
				// too long to be a hex value, this should never actually happen
				if (i + 4 >= max) {
					sb.append(c);
					continue;
				}
				// two __ in a row signal start of suffix
				char c1 = identifier.charAt(i + 1);
				if (c1 == '_') {
					// skip the upcoming suffix + / + prefix, - 1 due to current char being part of the suffix
					i += SUFFIX_LENGTH + 1 + PREFIX_LENGTH - 1;
					sb.append('/');
					continue;
				}
				// parse the next four characters as a hexadecimal value
				char c2 = identifier.charAt(i + 2);
				char c3 = identifier.charAt(i + 3);
				char c4 = identifier.charAt(i + 4);
				int hex1 = c1 - (c1 <= '9' ? '0' : LOWERCASE_HEX_ALPHA_OFFSET);
				int hex2 = c2 - (c2 <= '9' ? '0' : LOWERCASE_HEX_ALPHA_OFFSET);
				int hex3 = c3 - (c3 <= '9' ? '0' : LOWERCASE_HEX_ALPHA_OFFSET);
				int hex4 = c4 - (c4 <= '9' ? '0' : LOWERCASE_HEX_ALPHA_OFFSET);
				char decoded = (char) ((hex1 << 12) | (hex2 << 8) | (hex3 << 4) | hex4);
				sb.append(decoded);
				i += 4;
				continue;
			}
			sb.append(c);
			if (c == '/') {
				// should not happen as we already skip suffix/prefix when we encounter suffix start
				// skip the upcoming prefix, no - 1 as current char / is not part of the prefix
				i += PREFIX_LENGTH;
			}
		}
		return sb.toString();
	}
}
