package translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bpelUtils.NodeName;

public class XMLDomParser {

	private Document dom;
	private String filename;

	public static Set<String> activities = new HashSet<String>();

	public XMLDomParser(String filename) {
		this.filename = filename;
	}

	public static boolean pathEquals(String path1, String path2) {

		if (!path1.endsWith("/")) {
			path1 += "/";
		}
		if (!path2.endsWith("/")) {
			path2 += "/";
		}

		return path1.equals(path2);
	}

	/**
	 * Remove last slash
	 * @param path
	 * @return
	 */
	public static String transPath(String path) {

		if (!path.endsWith("/")) {
			path += "/";
		}
		return  path;
	}

	public void parseXmlFile() {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse(filename);
			dom.normalizeDocument();

		} catch (ParserConfigurationException pce) {
			System.err.println(pce);
			pce.printStackTrace();

		} catch (SAXException se) {

			System.err.println(se);
			se.printStackTrace();
		} catch (IOException ioe) {
			System.err.println(ioe);
			ioe.printStackTrace();
		}
	}



	private void parseDocument() {
		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("variable");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the employee element
				Element el = (Element) nl.item(i);

				System.out.println("n " + el.getNodeName());
			}
		}
	}

	public Document getDom() {
		return dom;
	}

	public static void main(String[] args) {
		XMLDomParser dparser = new XMLDomParser("docs\\BPEL1.bpel");
		dparser.parseXmlFile();
		dparser.parseDocument();
	}

}
