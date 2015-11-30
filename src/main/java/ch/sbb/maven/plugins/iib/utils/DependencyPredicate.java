/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2015.
 */
package ch.sbb.maven.plugins.iib.utils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * 
 *
 * @author u219237 (Pascal Moser)
 * @version $Id: $
 * @since 3.1, 2015
 */
public class DependencyPredicate implements Predicate {

    private Object expected;
    private String propertyName;

    public DependencyPredicate(String propertyName, Object expected) {
        super();
        this.propertyName = propertyName;
        this.expected = expected;
    }

    public boolean evaluate(Object object) {
        try {
            return expected.equals(PropertyUtils.getProperty(object, propertyName));
        } catch (Exception e) {
            return false;
        }
    }
}
