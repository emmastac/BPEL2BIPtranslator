package auxiliary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

//import org.apache.ode.bpel.compiler.BpelC;
//import org.apache.ode.bpel.compiler.api.CompilationException;


import org.w3c.dom.*;
import org.xml.sax.*;

import bpelUtils.XMLFile;

import javax.xml.parsers.*;
import javax.xml.xpath.*;


public class XPParser {



	public static void main(String[] args) {

		XPParser xp = new XPParser();

		try {
			
			// Create a DocumentBuilder
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbFactory.newDocumentBuilder();

			// option1
			 //Create a Document from a file or stream
			//Document doc = getSampleInput(builder);
			

			// option2
			File input = new File("BPELProjectsCustom/TravelApproval_41/TravelApproval.bpel");
			Document doc = builder.parse(input);
			doc.getDocumentElement().normalize();

			// Build XPath
			XPath xPath = XPathFactory.newInstance().newXPath();

			// Prepare Path expression and evaluate it
			//String expression = "bpws:getVariableData('index') &lt; ora:countNodes('TravelRequest','travelRequest','/client:TravelApproval/airlineData/client:AirlineLink')";
			String expression = "/client:TravelApproval/airlineData/client:AirlineLink";
			XPathExpression xpathExp = (XPathExpression) xPath.compile(expression);
			//XPathFunctionResolver res = xPath.getXPathFunctionResolver();
			
			
			//System.out.println(xpathExp.toString());
			NodeList nodeList = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);
			System.out.println(nodeList.getLength());
			// Iterate over NodeList
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				// Examine attributes

				// returns a Map (table) of names/values
				NamedNodeMap nMap = nNode.getAttributes();
				// returns specific attribute
				String attrValue = ((Element) nNode).getAttribute("sth");

				// Examine sub-elements
				// returns a list of all child nodes
				NodeList nList = nNode.getChildNodes();
				// returns a list of subelements of specified name
		
				NodeList nListTag = ((Element) nNode).getElementsByTagName("name");
			}
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Document getSampleInput(DocumentBuilder builder){
		 StringBuilder xmlStringBuilder = new StringBuilder();
		 xmlStringBuilder.append("<?xml version=\"1.0\"?> <class> </class>");
		try {
			ByteArrayInputStream input = new
			 ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
		
		return builder.parse(input);
		
		} catch ( SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
