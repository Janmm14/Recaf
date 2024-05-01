package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import software.coley.recaf.services.mapping.format.MappingFileFormat;

/**
 * Outline of intermediate mappings, allowing for clear retrieval regardless of internal storage of mappings.
 * <br>
 * <h2>Relevant noteworthy points</h2>
 * <b>Incomplete mappings</b>: When imported from a {@link MappingFileFormat} not all formats are made equal.
 * Some contain less information than others. See the note in {@link MappingFileFormat} for more information.
 * <br><br>
 * <b>Member references pointing to child sub-types</b>: References to class members can point to child sub-types of
 * the class that defines the member. You may need to check the owner's type hierarchy to see if the field or method
 * is actually defined by a parent class.
 *
 * @author Matt Coley
 */
public interface ExportableMappings extends Mappings {
	/**
	 * Generally this is implemented under the assumption that {@link ExportableMappings} is used to model data explicitly.
	 * For instance, if we have a workspace with a class {@code Person} using this we can see the {@code Person}
	 * in the resulting {@link IntermediateMappings#getClasses()}.
	 * <br>
	 * However, when {@link ExportableMappings} is used to pattern-match and replace <i>(Like replacing a prefix/package
	 * in a class name)</i> then there is no way to model this since we don't know all possible matches beforehand.
	 * In such cases, we should <i>avoid using this method</i>.
	 * But for API consistency an empty {@link IntermediateMappings} should be returned.
	 *
	 * @return Object representation of mappings.
	 */
	@Nonnull
	IntermediateMappings exportIntermediate();
}
