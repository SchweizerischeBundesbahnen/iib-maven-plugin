/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2015.
 */
package ch.sbb.maven.plugins.iib.utils;

/**
 *
 * 
 *
 * @author u219237 (Pascal Moser)
 * @version $Id: $
 * @since 30.11, 2015
 */
public enum ProjectType {

    APPLICATION("app"), LIBRARY("lib");

    private final String type;

    ProjectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
