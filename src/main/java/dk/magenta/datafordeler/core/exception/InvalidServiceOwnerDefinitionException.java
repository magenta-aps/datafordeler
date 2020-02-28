package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.plugin.Plugin;

import java.util.regex.Pattern;

public class InvalidServiceOwnerDefinitionException extends InvalidPluginDefinitionException {
    public InvalidServiceOwnerDefinitionException(Plugin plugin, String ownerDefinition, Pattern validationRegex) {
        super(plugin, "\"" + ownerDefinition + "\" is not a valid owner definition, must conform to regex /" + validationRegex.pattern() + "/");
    }

    @Override
    public String getCode() {
        return "datafordeler.plugin.invalid_owner_definition";
    }
}
