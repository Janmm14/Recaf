package software.coley.recaf.services.decompile.transparentmapping;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.recaf.config.BasicConfigContainer;
import software.coley.recaf.config.ConfigGroups;
import software.coley.recaf.services.ServiceConfig;

/**
 * Config for {@link TransparentMapping}
 * 
 * @author Janmm14
 */
@ApplicationScoped
public class TransparentMappingConfig extends BasicConfigContainer implements ServiceConfig {
	@Inject
	public TransparentMappingConfig() {
		super(ConfigGroups.SERVICE_DECOMPILE, TransparentMapping.SERVICE_ID + CONFIG_SUFFIX);
	}
}
