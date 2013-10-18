package org.sa.rainbow.core.ports.eseb;

import org.sa.rainbow.core.gauges.CommandRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

public class ESEBCommandHelper implements ESEBConstants {

    public static void command2Message (IRainbowModelCommandRepresentation command, RainbowESEBMessage msg) {
        msg.setProperty (MODEL_NAME_KEY, command.getModelName ());
        msg.setProperty (COMMAND_NAME_KEY, command.getCommandName ());
        msg.setProperty (MODEL_TYPE_KEY, command.getModelType ());
        msg.setProperty (COMMAND_TARGET_KEY, command.getTarget ());
        msg.setProperty (COMMAND_PARAMETER_KEY + "_size", command.getParameters ().length);
        for (int i = 0; i < command.getParameters ().length; i++) {
            msg.setProperty (COMMAND_PARAMETER_KEY + i, command.getParameters ()[i]);
        }
    }

    public static IRainbowModelCommandRepresentation msgToCommand (RainbowESEBMessage msg) {
        String modelName = (String )msg.getProperty (MODEL_NAME_KEY);
        String modelType = (String )msg.getProperty (MODEL_TYPE_KEY);
        String commandName = (String )msg.getProperty (COMMAND_NAME_KEY);
        String target = (String )msg.getProperty (COMMAND_TARGET_KEY);
        int numParams = (Integer )msg.getProperty (COMMAND_PARAMETER_KEY + "_size");
        String[] parameters = new String[numParams];
        for (int i = 0; i < numParams; i++) {
            parameters[i] = (String )msg.getProperty (COMMAND_PARAMETER_KEY + i);
        }
        return new CommandRepresentation (commandName, commandName, modelName, modelType, target, parameters);
    }

}