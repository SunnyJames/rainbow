package org.sa.rainbow.model.acme.znn.commands;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetCaptchaResponseCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    public SetCaptchaResponseCmd (String name, AcmeModelInstance modelInstance, String client, String response) {
        super (name, modelInstance, client, response);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        if (client == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The load balancer ''{0}'' could not be found in the model", getTarget ()));
        if (!client.declaresType ("CaptchaHandlerT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The client ''{0}'' is not of the right type. It does not have a property ''captcha''",
                    getTarget ()));

        try {
            int response = Integer.valueOf (getParameters ()[0]);
            IAcmeProperty property = client.getProperty ("captcha");
            IAcmeIntValue acmeVal = PropertyHelper.toAcmeVal (response);
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            if (propertyValueChanging (property, acmeVal)) {
                m_command = client.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
                cmds.add (m_command);
            }
            return cmds;
        }
        catch (NumberFormatException e) {
            throw new RainbowModelException (MessageFormat.format ("Could not parse ''{0]'' as an integer",
                    getParameters ()[0]));
        }
    }


}
