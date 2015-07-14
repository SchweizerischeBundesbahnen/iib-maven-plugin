package com.ibm.broker.config.appdev;


/**
 * Wrapper around CommandProcessor to enable simple access to proctected methods without introspection
 * 
 * @author u209936 (Jamie Townsend)
 * @since 2.1, 2015
 */
public class CommandProcessorPublicWrapper extends CommandProcessor {

    /**
     * @param args
     */
    public CommandProcessorPublicWrapper(String[] args) {
        super(args);
    }

    @Override
    public void process() {
        super.process();
    }

}
