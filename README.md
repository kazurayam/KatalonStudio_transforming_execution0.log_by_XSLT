# Transforming execution0.log file in Katalon Studio

- @date Jan 2025
- @author kazurayam

## Problem to solve

In most of the Katalon Studio projects, you would have a log file named `execution0.log`. For example, my project currently has the following one at the time of authoring:

- `<projectDir>/Reports/20241230_231944/healthcare-tests - TS_RegressionTest/20241230_231944/execution0.log`

The `exection0.log` file contains all log records of your Test Suite execution. I checked the metrics of this file.

```
$ wc Reports/20241230_231944/healthcare-tests - TS_RegressionTest/20241230_231944/execution0.log
    4041    6383  161611 execution0.log
```

The file contains 6K lines. Its size is 161 Kbytes. This file is fairely large. The file is formated as XML. For example, it looks like this:

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE log SYSTEM "logger.dtd">
<log>
<record>
  <date>2024-12-30T14:19:51.186102Z</date>
  <millis>1735568391186</millis>
  <nanos>102000</nanos>
  <sequence>0</sequence>
  <level>START</level>
  <class>com.kms.katalon.core.logging.XmlKeywordLogger</class>
  <method>startSuite</method>
  <thread>1</thread>
  <message>Start Test Suite : Test Suites/healthcare-tests - TS_RegressionTest</message>
  <nestedLevel>0</nestedLevel>
  <escapedJava>false</escapedJava>
  <property name="rerunTestFailImmediately">false</property>
  <property name="retryCount">0</property>
  <property name="name">healthcare-tests - TS_RegressionTest</property>
  <property name="description"></property>
  <property name="id">Test Suites/healthcare-tests - TS_RegressionTest</property>
</record>
<record>
  <date>2024-12-30T14:19:51.207270Z</date>
  <millis>1735568391207</millis>
  <nanos>270000</nanos>
  <sequence>1</sequence>
  <level>RUN_DATA</level>
...
```

In the Katalon Community, there is a topic titled: ["Configuration on logs execution file" raised by testlms21102024](https://forum.katalon.com/t/configuration-on-logs-execution-file/159728). The original poster wanted to customize the `execution0.log` file so that

- it should print the `<date>` element in shorter format: `2024-12-30T14:19:51.186102Z` -> `2024-12-30 14:19:51`

Unfortunately Katalon Studio does not support customizing the `execution0.log` file. We have to accept the current format. But we would be able to develop a Groovy script that transforms the execution0.log file into another format we like.

How can I write such a transformer script?

## Solution

How to implement a XML-to-XML transformer in Java? I will employ **XSLT** in Java.

XML and XSLT --- Ah, greate outdated technology. Java Platform supported XML and XSLT in [J2SE1.4](https://en.wikipedia.org/wiki/Java_version_history#J2SE_1.4) in 2002. It's 2 decades ago. There are very few people who can write a code that transforms a XML into another using XSLT.

But I can do it. Let me show you a sample.

## Description


## Conclusion

