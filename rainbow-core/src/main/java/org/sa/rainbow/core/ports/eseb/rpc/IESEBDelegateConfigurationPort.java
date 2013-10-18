package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;

public interface IESEBDelegateConfigurationPort extends IDelegateConfigurationPort {
    @Override
    @ParametersTypeMapping ({ "map<string,string>", "list<probe_description>", "list<effector_description>",
            "list<gauge_instance>" })
    public abstract void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors, List<GaugeInstanceDescription> gauges);
}