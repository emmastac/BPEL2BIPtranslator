package auxiliary;

import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

public class TestWSDLparsing {

	public static void main(String[] args) {
		try {

			WSDLFactory factory = WSDLFactory.newInstance();
			WSDLReader reader = factory.newWSDLReader();
			reader.setFeature("javax.wsdl.verbose", true);
			reader.setFeature("javax.wsdl.importDocuments", true);
			Definition def = reader.readWSDL(null, "/home/mania/Dropbox/eclipse_workspace/TestingBPEL/bpelContent/TestNamesArtifacts_0.wsdl");

			Map messages = def.getMessages();
			Iterator msgIterator = messages.values().iterator();
			while (msgIterator.hasNext()) {
				Message msg = (Message) msgIterator.next();
				if (!msg.isUndefined()) {
					System.out.println("Msg:" +msg.getQName());
				}
			}
		} catch (WSDLException e) {
			e.printStackTrace();
		}

	}

}
