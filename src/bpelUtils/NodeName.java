package bpelUtils;

import java.util.HashMap;
import java.util.HashSet;

/*
 * Enumeration of the names of the nodes in BPEL process XML tree, which trigger a translation rule. 
 */


public enum NodeName {

	EMPTY("empty"),
	ON_EVENT("onEvent"),
	ON_MESSAGE("onMessage"),
	ON_ALARM("onAlarm"),
    INVOKE("invoke"),
    PROCESS ("process"), 
	CATCH ("catch"), 
	CATCHALL ("catchAll"), 
	RECEIVE ("receive"), 
	REPLY ("reply"), 
	ASSIGN ("assign"), 
	COPY ("copy"), 
	THROW ("throw"), 
	TERMINATE ("terminate"), 
	EXIT ("exit"), 
	RETHROW ("rethrow"), 
	COMPENSATE ("compensate"), 
	COMPENSATE_SCOPE ("compensateScope"), 
	WAIT ("wait"), 
	IF ("if"), 
	ELSE_IF ("elseif"), 
	ELSE ("else"), 
	WHILE ("while"), 
	REPEAT_UNTIL ("repeatUntil"), 
	FOR_EACH ("forEach"), 
	PICK ("pick"), 
	SWITCH ("switch"), 
	CASE ("case"), 
	FLOW ("flow"), 
	SEQUENCE ("sequence"), 
	SCOPE ("scope"),
	PARTNER_LINKS ("partnerLinks"),
	PARTNER_LINK ("partnerLink"),
    VARIABLE ("variable"),    
    VARIABLES ("variables"),
    MESSAGE_EXCHANGE ("messageExchange"),
    CORRELATION_SET ("correlationSet"), 
    MESSAGE_EXCHANGES ("messageExchanges"),
    CORRELATION_SETS ("correlationSets"),
    FAULT_HANDLERS ("faultHandlers"),
    COMPENSATION_HANDLER ("compensationHandler"),
    TERMINATION_HANDLER ("terminationHandler"),
    EVENT_HANDLERS ("eventHandlers"), 
    SOURCES ("sources"), 
    SOURCE ("source"), 
    TARGETS ("targets"),
    //Oracle Extensions
    BPELX_FLOWN("bpelx:flowN"),
    BPELX_ANNOTATION("bpelx:annotation"),
    BPELX_APPEND("bpelx:append"), 
    OTHERWISE("otherwise"), UNKNOWN(""),
    ;
	
	public static HashMap<String,NodeName> tags2nodeNames = new HashMap<String,NodeName>();
	
	
	
	static{
		for(NodeName at:NodeName.values()){
			tags2nodeNames.put(at.name,at);
		}
	}
     
	private String name;
	
	NodeName(String label){
		this.name=label;
	}
	
	public static String getNodeNameAsString(NodeName node){
		return node.name;
	}
	
	//	private String getName(){
	//		return name;
	//	}
	
	
	
	public static NodeName getNodeName(String name){
		
		if(tags2nodeNames.containsKey( name )){
			return tags2nodeNames.get(name);
		}
		//System.out.println("not found"+name);
		//remove any namespace 
		if(name.contains(":")){
			// CRUDE. Needs better treatment
			name = name.split(":")[1];
			if(tags2nodeNames.containsKey( name )){
				return tags2nodeNames.get(name);
			}
		}
		return UNKNOWN;
	}
    
}