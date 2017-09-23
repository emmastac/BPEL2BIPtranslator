package stUtils;

public class MapKeys {

	// FLAGS
	public static final String SUPPRESS_JOIN_FAILURE = "suppressJoinFailure";
	public static final String INSIDE_EVENT_HANDLER = "inEH";
	public static final String EXIT_ON_STANDARD_FAULT = "exitOnStandardFault";
	// GENERAL
	public static final String FAULTS_LIST = "faultsList";
	public static final String SCOPE_ROLES = "scopeRoles";
	public static final String TAG = "tag";
	public static final String SCOPE_IDS = "scope_ids";
	public static final String SCOPE_NAME = "scope_names";
	public static final String CHILD_COMP = "childComp";
	public static final String BIP_CODE = "BIPCode";
	public static final String CPP_CODE = "CPPCode";
	public static final String HPP_CODE = "HPPCode";

	// PORTS
	public static final String ON_MESSAGE_PORTS = "onMsgPorts";
	public static final String RCV_MESSAGE_PORTS = "rcvMsgPorts";
	public static final String SND_MESSAGE_PORTS = "sndMsgPorts";
	public static final String CONFL_IMA_PORTS = "conflImaPorts";
	public static final String OMA_PORTS = "oma";
	public static final String IMA_PORTS = "ima";
	public static final String INV_PORTS = "inv";
	public static final String INV_IMA_PORTS = "invIma";
	public static final String ON_IMA_PORTS = "onIMAs";

	public static final String WRITE_PORTS = "writePorts";
	public static final String READ_PORTS = "readPorts";
	public static final String WRITE_LINK_PORTS = "writeLinkPorts";
	public static final String READ_LINK_PORTS = "readLinkPorts";

	public static final String COMPEN_PORTS = "compenPorts";
	public static final String COMPENSCOPE_PORTS = "compenScopePorts";
	public static final String RVRS_PORTS = "rvrsPorts";
	public static final String EXIT_PORTS = "exitPorts";
	// public static final String THROW_PORTS = "throwPorts";
	public static final String RETHROW_PORTS = "rethrowPorts";

	// SPECIAL
	public static final String LINKS = "links";
	public static final String CATCH_COMPLETE_NAMES = "catchCompleteNames";

	// SCOPE INFO
	public static final String MESSAGE_EXCHANGES = "messageExchanges";
	public static final String VARIABLES = "variables";
	public static final String VARIABLES_MSG = "variablesMsg";
	// partnerLinkName -> [partnerLinkType, partnerLinkRole, initializeRole ]
	public static final String PARTNER_LINKS = "partnerLinks";
	public static final String CORRELATION_SETS = "correlationSets";
	public static final String ID = "ID";
	public static final String VARIABLES_HASH = "varHash";
	public static final String DATA_LIST = "dataList";
	// LOOP INFO
	public static final String COUNTER_NAME = "counterName";

	// PROCESS INFO
	// public static final String MESSAGES = "messages";
	// public static final String PORT_TYPES = "portTypes";
	// public static final String PARTNER_LINK_TYPES = "partnerLinkTypes";
	// NSPACE2LOC : String-> ; e.g. ora -> {
	// BPELSample/Airline_project/Airline.wsdl, ..../AirlineRef.wsdl }
	public static final String NSPACEL_TO_PATHS = "nspaceL_to_paths";


	public static boolean isHashMapEntry( String key ) {

		if ( key.equals( MapKeys.CONFL_IMA_PORTS ) || key.equals( MapKeys.NSPACEL_TO_PATHS ) ) {
			return true;
		}
		return false;
	}


	public static boolean isSoloMapEntry( String key ) {

		if ( key.equals( MapKeys.TAG ) ) {
			return true;
		}
		return false;
	}

}
