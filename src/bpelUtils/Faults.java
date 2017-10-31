package bpelUtils;

import java.util.HashMap;


public class Faults {
	
	private static HashMap < String , Integer > faults2codes = new HashMap < String , Integer >( );
	
	private static String [ ] standardFaults = new String [ ] { "ambiguousReceive" , "completionConditionFailure" , "conflictingReceive" ,
			"conflictingRequest" , "correlationViolation" , "invalidBranchCondition" , "joinFailure" , "missingReply" , "missingRequest" ,
			"uninitializedPartnerRole" , "uninitializedVariable" , "subLanguageExecutionFault" , "invalidExpressionValue" , "invalidVariables" ,
			"mismatchedAssignmentFailure" , "selectionFailure" };

	
	public Faults(){
		for ( int i = 0 ; i < standardFaults.length ; i++ ) {
			faults2codes.put( standardFaults[ i ] , i + 1 );
		}
	}

	public void addFault(String faultName){
		faults2codes.put( faultName , faults2codes.size( ) );
	}
	
	public int getFaultName( String faultName ) {

		String matchedFaultName = "";
		if ( this.faults2codes.containsKey( faultName ) ) {
			matchedFaultName = faultName;
		} else {
			for ( String s : this.faults2codes.keySet( ) ) {
				// match with the biggest completeFaultName found in queried
				// faultName
				if ( matchedFaultName.length( ) < s.length( ) && faultName.contains( s ) ) {
					matchedFaultName = s;
				}
			}
		}

		Integer faultCode = this.faults2codes.get( matchedFaultName );
		if ( faultCode == null ) {
			faultCode = this.faults2codes.size( ) + 1;
			this.faults2codes.put( faultName , faultCode );
		}
		return faultCode;
	}
	
}
