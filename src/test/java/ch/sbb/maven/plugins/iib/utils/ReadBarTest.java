/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2015.
 */
package ch.sbb.maven.plugins.iib.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

/**
 * @author u209936 Jamie Townsend
 * @since 2.1, 2015
 */
public class ReadBarTest {


    @Test
    public void GetConfigurablePropertiesTest() throws IOException {
        ConfigurableProperties properties = ReadBar.getOverridableProperties(getClass().getResource("/estaint-logging-test-bar-2.0.bar").getFile());

        assertEquals(getExpectedProperties(), properties);
    }

    /**
     * @return properties as tediously extracted from mqsireadbar command and reconstructed here
     */
    private Properties getExpectedProperties() {
        Properties expectedProperties = new Properties();
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.validateMaster", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#LogLevel", "ERROR");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#ApplicationId", "APP_ID");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#LogMessage", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#FullLogging", "true");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#filePath", "/opt/source/delivery/wsm/traces/${LocalEnvironment.Estaint.Log.Properties.ApplicationId}/logging.trc");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.validateMaster", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.validateMaster", "");
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_SetApplicationLogLevel#SetApplicationLogLevel.validateMaster", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#LogLevel", "ERROR");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#ApplicationId", "APP_ID");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#LogMessage", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#FullLogging", "true");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#filePath", "/opt/source/delivery/wsm/traces/${LocalEnvironment.Estaint.Log.Properties.ApplicationId}/logging.trc");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#IsLoggingActive.validateMaster", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.dataSource", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.Sub_ApplicationLogging#InitializeTrace.validateMaster", "");
        expectedProperties.put("startMode", "");
        expectedProperties.put("javaIsolation", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#additionalInstances", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#commitCount", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#commitInterval", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#coordinatedTransaction", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#consumerPolicySet", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#providerPolicySet", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#consumerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#providerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#securityProfileName", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#monitoringProfile", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#startMode", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#startInstancesWhenFlowStarts", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#maximumRateMsgsPerSec", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#wlmPolicy", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#notificationThresholdMsgsPerSec", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#processingTimeoutSec", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest_2.0#processingTimeoutAction", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.additionalInstances", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.componentLevel", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.queueName", "ESTAINT.LOGGING.TEST.REQ.L");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.resetBrowseTimeout", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.securityProfileName", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.serializationToken", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.topicProperty", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Input.validateMaster", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.queueManagerName", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.queueName", "ESTAINT.LOGGING.TEST.RES.L");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.replyToQ", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.replyToQMgr", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.securityProfileName", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#MQ Output.validateMaster", "");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#Sub_ApplicationLogging.ApplicationId", "TEST");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#Sub_ApplicationLogging.FullLogging", "false");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#Sub_ApplicationLogging.LogLevel", "ERROR");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#Sub_ApplicationLogging.LogMessage", "log message my message partial logging");
        expectedProperties.put("estaint.logging.PartialApplicationLoggingExceptionTest#Sub_ApplicationLogging.filePath", "/opt/source/delivery/wsm/traces/TEST/logging.trc");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#additionalInstances", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#commitCount", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#commitInterval", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#coordinatedTransaction", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#consumerPolicySet", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#providerPolicySet", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#consumerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#providerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#monitoringProfile", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#startMode", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#startInstancesWhenFlowStarts", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#maximumRateMsgsPerSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#wlmPolicy", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#notificationThresholdMsgsPerSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#processingTimeoutSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest_2.0#processingTimeoutAction", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.additionalInstances", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.componentLevel", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.queueName", "ESTAINT.LOGGING.TEST.REQ.L");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.resetBrowseTimeout", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.serializationToken", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.topicProperty", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Input.validateMaster", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.queueManagerName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.queueName", "ESTAINT.LOGGING.TEST.RES.L");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.replyToQ", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.replyToQMgr", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#MQ Output.validateMaster", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#Sub_ApplicationLogging.ApplicationId", "TEST");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#Sub_ApplicationLogging.FullLogging", "true");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#Sub_ApplicationLogging.LogLevel", "ERROR");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#Sub_ApplicationLogging.LogMessage", "log message my message with exception");
        expectedProperties.put("estaint.logging.ApplicationLoggingExceptionTest#Sub_ApplicationLogging.filePath", "/opt/source/delivery/wsm/traces/TEST/logging.trc");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#additionalInstances", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#commitCount", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#commitInterval", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#coordinatedTransaction", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#consumerPolicySet", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#providerPolicySet", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#consumerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#providerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#monitoringProfile", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#startMode", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#startInstancesWhenFlowStarts", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#maximumRateMsgsPerSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#wlmPolicy", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#notificationThresholdMsgsPerSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#processingTimeoutSec", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest_2.0#processingTimeoutAction", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#DebugLogging.ApplicationId", "TEST");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#DebugLogging.FullLogging", "true");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#DebugLogging.LogLevel", "DEBUG");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#DebugLogging.LogMessage", "log message in debug mode, should not be logged.");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#DebugLogging.filePath", "/opt/source/delivery/wsm/traces/TEST/logging.trc");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#ErrorLogging.ApplicationId", "TEST");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#ErrorLogging.FullLogging", "true");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#ErrorLogging.LogLevel", "ERROR");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#ErrorLogging.LogMessage", "log message my message");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#ErrorLogging.filePath", "/opt/source/delivery/wsm/traces/TEST/logging.trc");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.additionalInstances", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.componentLevel", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.queueName", "ESTAINT.LOGGING.TEST.REQ.L");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.resetBrowseTimeout", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.serializationToken", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.topicProperty", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Input.validateMaster", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.queueManagerName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.queueName", "ESTAINT.LOGGING.TEST.RES.L");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.replyToQ", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.replyToQMgr", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.securityProfileName", "");
        expectedProperties.put("estaint.logging.ApplicationLoggingTest#MQ Output.validateMaster", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#additionalInstances", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#commitCount", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#commitInterval", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#coordinatedTransaction", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#consumerPolicySet", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#providerPolicySet", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#consumerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#providerPolicySetBindings", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#securityProfileName", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#monitoringProfile", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#startMode", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#startInstancesWhenFlowStarts", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#maximumRateMsgsPerSec", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#wlmPolicy", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#notificationThresholdMsgsPerSec", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#processingTimeoutSec", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest_2.0#processingTimeoutAction", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute.dataSource", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute.validateMaster", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute1.connectDatasourceBeforeFlowStarts", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute1.dataSource", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#Compute1.validateMaster", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.additionalInstances", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.componentLevel", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.queueName", "ESTAINT.LOGGING.TEST.REQ.L");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.resetBrowseTimeout", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.securityProfileName", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.serializationToken", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.topicProperty", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Input.validateMaster", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.queueManagerName", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.queueName", "ESTAINT.LOGGING.TEST.RES.L");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.replyToQ", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.replyToQMgr", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.securityProfileName", "");
        expectedProperties.put("estaint.logging.SetLogLevelTest#MQ Output.validateMaster", "");
        return expectedProperties;
    }
}
