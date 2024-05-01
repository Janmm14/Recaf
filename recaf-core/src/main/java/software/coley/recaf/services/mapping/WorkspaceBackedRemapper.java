package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.util.Handles;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Enhanced {@link BasicMappingsRemapper} for cases where additional information
 * needs to be pulled from a {@link Workspace}.
 *
 * @author Matt Coley
 */
public class WorkspaceBackedRemapper extends BasicMappingsRemapper {
	private final Workspace workspace;


	/**
	 * @param workspace
	 * 		Workspace to pull class info from when additional context is needed.
	 * @param mappings
	 * 		Mappings wrapper to pull values from.
	 */
	public WorkspaceBackedRemapper(@Nonnull Workspace workspace,
								   @Nonnull Mappings mappings) {
		super(mappings);
		this.workspace = workspace;
	}

	@Override
	public String mapAnnotationAttributeName(String descriptor, String name) {
		String annotationName = Type.getType(descriptor).getInternalName();
		ClassPathNode classPath = workspace.findClass(annotationName);

		// Not found, probably not intended to be renamed.
		if (classPath == null)
			return name;

		// Get the declaration and, if found, treat as normal method mapping.
		ClassInfo info = classPath.getValue();
		MethodMember attributeMethod = info.getMethods().stream()
				.filter(method -> method.getName().equals(name))
				.findFirst().orElse(null);

		// Not found, shouldn't generally happen.
		if (attributeMethod == null)
			return name;

		// Use the method mapping from the annotation class's declared methods.
		return mapMethodName(annotationName, name, attributeMethod.getDescriptor());
	}

	@Override
	public String mapInvokeDynamicMethodName(String name, String descriptor) {
		throw new IllegalStateException("Enhanced 'mapInvokeDynamicMethodName(...)' usage required, missing handle arg");
	}
}
