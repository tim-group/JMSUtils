package com.timgroup.jms;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public interface JMSToolOptions {
    
    @Option(shortName = "v", defaultValue = "false")
    public boolean isVerbose();
    
    @Option(helpRequest = true)
    public boolean getHelp();
    
    @Unparsed(name = "URL COMMAND ARGUMENTS")
    public List<String> getArguments();
    
}
