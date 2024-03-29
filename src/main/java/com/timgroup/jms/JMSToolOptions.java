package com.timgroup.jms;

import java.util.List;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

public interface JMSToolOptions {
    
    @Option(shortName = "v", defaultValue = "false")
    public boolean isVerbose();
    
    @Option(helpRequest = true)
    public boolean getHelp();
    
    @Option(shortName = "a", defaultToNull = true)
    public List<String> getAlternateBrokers();
    
    @Unparsed(name = "URL COMMAND ARGUMENTS", defaultValue={})
    public List<String> getArguments();
    
}
