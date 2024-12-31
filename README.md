# Transforming execution0.log file in Katalon Studio

- @date Jan 2025
- @author kazurayam
- used Katalon Studio v9.0.0 Free, on maxOS 14.7.2

## Problem to solve

In most Katalon Studio projects, you would have files named `execution0.log` under the `Reports` folder. For example, my project currently has the following one at the time of authoring:

- `<projectDir>/Reports/20241230_231944/healthcare-tests - TS_RegressionTest/20241230_231944/execution0.log`

The `exection0.log` file contains all log records of your Test Suite execution. I checked the metrics of this file.

```
$ wc Reports/20241230_231944/healthcare-tests - TS_RegressionTest/20241230_231944/execution0.log
    4041    6383  161611 execution0.log
```

The file contains 6K lines. Its size is 161 Kbytes. This file is too large to look at at a glance. The file is formated as XML. For example, it looks like this:

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

In the Katalon Community, there was a topic titled: ["Configuration on logs execution file" raised by testlms21102024](https://forum.katalon.com/t/configuration-on-logs-execution-file/159728). The original poster wanted to customize the `execution0.log` file so that it should print the `<date>` text in a shorter format: `2024-12-30T14:19:51.186102Z` -> `2024-12-30 14:19:51`.

Unfortunately Katalon Studio does not support customizing the `execution0.log` file. We have to accept the current format. But we would be able to develop a Groovy script that transforms the `execution0.log` file into another format.

How can I write such a transformer script?

## Solution

How to implement a XML-to-XML transformer in Java? Well, I will employ **XSLT** in Java.

XML and XSLT --- Ah, these technologies look outdated. Java Platform supported XML and XSLT in [J2SE1.4](https://en.wikipedia.org/wiki/Java_version_history#J2SE_1.4) in 2002. It's 2 decades ago. I believe that very few people nowadays can write a code that drives XSLT.

But I can do it. I love XSLT. Let me show you how to program XSLT in Katalon Studio.

## Description

### Processing overview

![sequence](https://kazurayam.github.io/KatalonStudio_transforming_execution0.log_by_XSLT/diagrams/out/sequence/running_XSLT_in_Katalon_Studio.png)

1. You want to open this project, run the `Test Cases/processLog`
2. The `processLog` invokes XSLT while giving the `src/test/xslt/log-compaction.xsl` file as the stylesheet.
3. The stylesheet reads the `execution0.log` file as input
4. The stylesheet transforms it.
   - It will filter `<record>` elements by the child `<level>` element. It will pick the `<record>` with child `<level>FAILED</level>`. Other `<record>` elements will be ignored.
   - It will convert the content text of `<date>` element from `2024-12-30T14:20:34.nnnnnnZ` -> `2024-12-30 14:20:34`. It chomps off the `T` and `Z` characters. It trims the digits as milli-seconds.
5. The stylesheet writes an output XML file, which is far smaller in size.

### Test Case script as the transformer

The following is the Test Case script that executes XSLT throw [Java API for XML Processing]()

- [Test Cases/processLog](https://github.com/kazurayam/KatalonStudio_transforming_execution0.log_by_XSLT/blob/main/Scripts/processLog/Script1735564727924.groovy)

```
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Result;
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import com.kms.katalon.core.configuration.RunConfiguration

Path projectDir = Paths.get(RunConfiguration.getProjectDir())

// XSL Stylesheet
Path stylesheet = projectDir.resolve("src/test/xslt/log-compaction.xsl")
Source xsltSource = new StreamSource(stylesheet.toFile())

// input XML
Path log = projectDir.resolve("Reports/20241230_231944/healthcare-tests - TS_RegressionTest/20241230_231944/execution0.log")
Source xmlSource = createSAXSourceIgnoringDTD(log)

// output XML
Path outputDir = projectDir.resolve("build/xslt-output")
Files.createDirectories(outputDir)
Path outputFile = outputDir.resolve("compact-log.xml")
Result result = new StreamResult(outputFile.toFile())

// perform XSL Transformation (from XML to XML conversion)
TransformerFactory trfactory = TransformerFactory.newInstance()
Transformer transformer = trfactory.newTransformer(xsltSource)
transformer.transform(xmlSource, result)

/**
 * create an instance of javax.xml.transform.Source out of an XML file
 * while ignoring DTD contained in it if any
 */
Source createSAXSourceIgnoringDTD(Path xml) {
	SAXParserFactory spfactory = SAXParserFactory.newInstance()
	spfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
	spfactory.setNamespaceAware(true)
	SAXParser saxParser = spfactory.newSAXParser()
	XMLReader xmlReader = saxParser.getXMLReader()
	InputSource inputSource = new InputSource(Files.newInputStream(xml))
	Source source = new SAXSource(xmlReader, inputSource)
	return source
}
```

To learn more about XSLT in Java, read the following web article:

- [Baeldung, "Understanding XSLT Processing in Java"](https://www.baeldung.com/java-extensible-stylesheet-language-transformations)


### Input XML

The input XML is large. So please click the following link to have a look at it.

- [Reports/20241230_231944/healthcare-tests%20-%20TS_RegressionTest/20241230_231944/execution0.log](https://github.com/kazurayam/KatalonStudio_transforming_execution0.log_by_XSLT/blob/main/Reports/20241230_231944/healthcare-tests%20-%20TS_RegressionTest/20241230_231944/execution0.log)

This file was created by the `Test Suites/healthcare-tests - TS_RegressionTest`.

### XSL Stylesheets

- [src/test/xslt/log-compaction.xsl](https://github.com/kazurayam/KatalonStudio_transforming_execution0.log_by_XSLT/blob/main/src/test/xslt/log-compaction.xsl)

```
<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="./identity-transform.xsl"/>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
  	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="log">
    <log>
      <xsl:apply-templates select="record[contains(level,'FAILED')]"/>
    </log>
  </xsl:template>

  <xsl:template match="record">
  	<record>
    	<xsl:apply-templates select="date"/>
      <xsl:apply-templates select="level"/>
    	<xsl:apply-templates select="message"/>
    </record>
  </xsl:template>

  <!--
  we will convert
    <date>2024-12-30T14:20:35.937395Z</date>
  to
    <date>2024-12-30 14:20:35</date>
  -->
  <xsl:template match="date">
    <date><xsl:value-of select="concat(substring-before(.,'T'),
                                ' ',
                                substring(substring-after(.,'T'), 1, 8))" /></date>
  </xsl:template>

</xsl:transform>
```

The stylesheet does everything to transform the input XML into the output. It's such concise. I love the expressiveness of XSLT.

### Output XML

- [build/xslt-output/compact-log.xml](https://github.com/kazurayam/KatalonStudio_transforming_execution0.log_by_XSLT/blob/main/build/xslt-output/compact-log.xml)

```
<?xml version="1.0" encoding="UTF-8"?><log>
    <record>
        <date>2024-12-30 14:20:34</date>
        <level>FAILED</level>
        <message>Text &amp;apos;Appointment Confirmation&amp;apos; is present on page  (Root cause: com.kms.katalon.core.exception.StepFailedException: Text &amp;apos;Appointment Confirmation&amp;apos; is present on page
	at com.kms.katalon.core.webui.keyword.internal.WebUIKeywordMain.stepFailed(WebUIKeywordMain.groovy:64)
	at com.kms.katalon.core.webui.keyword.builtin.VerifyTextNotPresentKeyword$_verifyTextNotPresent_closure1.doCall(VerifyTextNotPresentKeyword.groovy:77)
	at com.kms.katalon.core.webui.keyword.builtin.VerifyTextNotPresentKeyword$_verifyTextNotPresent_closure1.call(VerifyTextNotPresentKeyword.groovy)
	at com.kms.katalon.core.webui.keyword.internal.WebUIKeywordMain.runKeyword(WebUIKeywordMain.groovy:20)
	at com.kms.katalon.core.webui.keyword.builtin.VerifyTextNotPresentKeyword.verifyTextNotPresent(VerifyTextNotPresentKeyword.groovy:82)
	at com.kms.katalon.core.webui.keyword.builtin.VerifyTextNotPresentKeyword.execute(VerifyTextNotPresentKeyword.groovy:68)
	at com.kms.katalon.core.keyword.internal.KeywordExecutor.executeKeywordForPlatform(KeywordExecutor.groovy:74)
	at com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords.verifyTextNotPresent(WebUiBuiltInKeywords.groovy:1756)
	at TC2_Verify Successful Appointment.run(TC2_Verify Successful Appointment:50)
	at com.kms.katalon.core.main.ScriptEngine.run(ScriptEngine.java:194)
	at com.kms.katalon.core.main.ScriptEngine.runScriptAsRawText(ScriptEngine.java:119)
	at com.kms.katalon.core.main.TestCaseExecutor.runScript(TestCaseExecutor.java:448)
	at com.kms.katalon.core.main.TestCaseExecutor.doExecute(TestCaseExecutor.java:439)
	at com.kms.katalon.core.main.TestCaseExecutor.processExecutionPhase(TestCaseExecutor.java:418)
	at com.kms.katalon.core.main.TestCaseExecutor.accessMainPhase(TestCaseExecutor.java:410)
	at com.kms.katalon.core.main.TestCaseExecutor.execute(TestCaseExecutor.java:285)
	at com.kms.katalon.core.common.CommonExecutor.accessTestCaseMainPhase(CommonExecutor.java:65)
	at com.kms.katalon.core.main.TestSuiteExecutor.accessTestSuiteMainPhase(TestSuiteExecutor.java:148)
	at com.kms.katalon.core.main.TestSuiteExecutor.execute(TestSuiteExecutor.java:106)
	at com.kms.katalon.core.main.TestCaseMain.startTestSuite(TestCaseMain.java:187)
	at TempTestSuite1735568384756.run(TempTestSuite1735568384756.groovy:36)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
)</message>
    </record>
...
```

## Conclusion

I demonstrated a Test Case script in Katalon Studio, that transforms a larget `execution0.log` file into a far smaller XML file. The Test Case script employed XSLT. The XSLT processing in Java is powerful; but I am afraid that very few people understand it. I hope this demonstration would motivate you to look into this state-of-the art.
