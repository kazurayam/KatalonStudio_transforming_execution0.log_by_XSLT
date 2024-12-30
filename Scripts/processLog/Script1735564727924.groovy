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

import org.xml.sax.XMLReader
import org.xml.sax.InputSource

import com.kms.katalon.core.configuration.RunConfiguration

/**
 * 
 */

Path projectDir = Paths.get(RunConfiguration.getProjectDir())

// XSL Stylesheet
Path stylesheet = projectDir.resolve("src/test/xslt/log-compaction.xsl")
Source xsltSource = new StreamSource(stylesheet.toFile())

// input XML
Path log = projectDir.resolve("Reports/20241230_155630/healthcare-tests - TS_RegressionTest/20241230_155630/execution0.log")
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
