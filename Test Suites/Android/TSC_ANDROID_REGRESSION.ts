<?xml version="1.0" encoding="UTF-8"?>
<TestSuiteCollectionEntity>
   <description>Run all current MyDigicel Android feature suites as one regression execution.</description>
   <name>TSC_ANDROID_REGRESSION</name>
   <tag>regression,android</tag>
   <delayBetweenInstances>0</delayBetweenInstances>
   <executionMode>SEQUENTIAL</executionMode>
   <maxConcurrentInstances>8</maxConcurrentInstances>
   <testSuiteRunConfigurations>
      <TestSuiteRunConfiguration>
         <configuration>
            <profileName>default</profileName>
            <requireConfigurationData>false</requireConfigurationData>
         </configuration>
         <runEnabled>true</runEnabled>
         <testSuiteEntity>Test Suites/Android/Authentication/TS_LOGIN</testSuiteEntity>
      </TestSuiteRunConfiguration>
      <TestSuiteRunConfiguration>
         <configuration>
            <profileName>default</profileName>
            <requireConfigurationData>false</requireConfigurationData>
         </configuration>
         <runEnabled>true</runEnabled>
         <testSuiteEntity>Test Suites/Android/Bill Payment/TS_BILL_PAYMENT_POSTPAID</testSuiteEntity>
      </TestSuiteRunConfiguration>
      <TestSuiteRunConfiguration>
         <configuration>
            <profileName>default</profileName>
            <requireConfigurationData>false</requireConfigurationData>
         </configuration>
         <runEnabled>true</runEnabled>
         <testSuiteEntity>Test Suites/Android/Bill Payment/TS_BILL_PAYMENT_HOME</testSuiteEntity>
      </TestSuiteRunConfiguration>
   </testSuiteRunConfigurations>
</TestSuiteCollectionEntity>
