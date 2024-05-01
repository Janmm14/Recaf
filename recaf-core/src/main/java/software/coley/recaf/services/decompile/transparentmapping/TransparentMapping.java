package software.coley.recaf.services.decompile.transparentmapping;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.cdi.EagerInitialization;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.Service;
import software.coley.recaf.services.ServiceConfig;
import software.coley.recaf.services.decompile.DecompilerManager;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.services.mapping.BasicMappingsRemapper;
import software.coley.recaf.services.mapping.WorkspaceClassRemapper;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Provide transparent mapping of classes in decompilation
 * 
 * @author Janmm14
 */
@EagerInitialization
@ApplicationScoped
public class TransparentMapping implements Service {
	public static final String SERVICE_ID = "transparentmapping";
	private static final Logger logger = Logging.get(TransparentMapping.class);
	private final TransparentMappingConfig config;

	@Inject
	public TransparentMapping(@Nonnull DecompilerManager decompilerManager,
							  @Nonnull TransparentMappingConfig config) {
		this.config = config;
		decompilerManager.addWorkspaceBytecodeFilter(new WorkspaceJvmBytecodeFilter() {
			@Nonnull
			@Override
			public String filterClassName(@Nonnull Workspace workspace, @Nonnull String className) {
				if (!workspace.getPrimaryResource().getJvmClassBundle().containsKey(className)) {
					return className;
				}
				if (className.startsWith(GenerativeMappings.PREFIX)) {
					throw new IllegalStateException("filterClassName: Class name already transformed: " + className);
//					return className;
				}
				return GenerativeMappings.transformIdentifier(className, true);
			}

			@Nonnull
			@Override
			public String unfilterClassName(@Nonnull Workspace workspace, @Nonnull String className) {
				if (className.startsWith(GenerativeMappings.PREFIX)) {
					String unfiltered = GenerativeMappings.reverseTransformIdentifier(className);
					if (workspace.getPrimaryResource().getJvmClassBundle().containsKey(unfiltered)) {
						return unfiltered;
					}
				}
				return className;
			}

			@Nonnull
			@Override
			public byte[] filter(@Nonnull Workspace workspace, @Nonnull JvmClassInfo initialClassInfo, @Nonnull byte[] bytecode) {
				String initialName = initialClassInfo.getName();
				if (!workspace.getPrimaryResource().getJvmClassBundle().containsKey(initialName)) {
					return bytecode;
				}
				ClassReader cr = new ClassReader(bytecode);
				ClassWriter cw = new ClassWriter(0);
				GenerativeMappings mappings = new GenerativeMappings(workspace);
				WorkspaceClassRemapper remapVisitor = new WorkspaceClassRemapper(cw, workspace, mappings,
						new BasicMappingsRemapper(mappings));
				cr.accept(remapVisitor, 0);
				return cw.toByteArray();
			}
		});
	}

	@Nonnull
	@Override
	public String getServiceId() {
		return SERVICE_ID;
	}

	@Nonnull
	@Override
	public ServiceConfig getServiceConfig() {
		return config;
	}
}
