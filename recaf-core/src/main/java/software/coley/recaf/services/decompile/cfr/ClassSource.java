package software.coley.recaf.services.decompile.cfr;

import jakarta.annotation.Nonnull;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.util.visitors.ClassHollowingVisitor;
import software.coley.recaf.workspace.model.Workspace;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * CFR class source. Provides access to workspace clases.
 *
 * @author Matt Coley
 */
public class ClassSource implements ClassFileSource {
	private final Workspace workspace;
	private final List<WorkspaceJvmBytecodeFilter> workspaceFilters;
	private final String targetClassNameFiltered;
	private final byte[] targetClassBytecode;

	/**
	 * Constructs a CFR class source.
	 *
	 * @param workspace
	 * 		Workspace to pull classes from.
	 * @param targetClassName
	 * 		Name to override.
	 * @param targetClassBytecode
	 * 		Bytecode to override.
	 */
	public ClassSource(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters,
					   @Nonnull String targetClassName, @Nonnull byte[] targetClassBytecode) {
		this.workspace = workspace;
		this.workspaceFilters = workspaceFilters;
		for (WorkspaceJvmBytecodeFilter filter : workspaceFilters) {
			targetClassName = filter.filterClassName(workspace, targetClassName);
		}
		this.targetClassNameFiltered = targetClassName;
		this.targetClassBytecode = targetClassBytecode;
	}

	@Override
	public void informAnalysisRelativePathDetail(String usePath, String specPath) {
	}

	@Override
	public Collection<String> addJar(String jarPath) {
		return Collections.emptySet();
	}

	@Override
	public String getPossiblyRenamedPath(String path) {
		return path;
	}

	@Override
	public Pair<byte[], String> getClassFileContent(String inputPath) {
		String className = inputPath.substring(0, inputPath.indexOf(".class"));
		byte[] code;
		if (className.equals(targetClassNameFiltered)) {
			code = targetClassBytecode;
			return new Pair<>(code, inputPath);
		}
		className = WorkspaceJvmBytecodeFilter.unapplyClassNameFilters(workspace, workspaceFilters, className);
		ClassPathNode result = workspace.findClass(className);
		if (result == null) {
			return new Pair<>(null, inputPath);
		}
		JvmClassInfo jvmClass = result.getValue().asJvmClass();

		// Simply CFR's work-load by gutting supporting class internals
		ClassWriter writer = new ClassWriter(0);
		ClassHollowingVisitor hollower = new ClassHollowingVisitor(writer);
		jvmClass.getClassReader().accept(hollower, ClassReader.SKIP_CODE);
		code = writer.toByteArray();

		code = WorkspaceJvmBytecodeFilter.applyFilters(workspace, workspaceFilters, jvmClass, code);
		return new Pair<>(code, inputPath);
	}
}
