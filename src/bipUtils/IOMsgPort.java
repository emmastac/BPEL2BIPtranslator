package bipUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import bpelUtils.XMLFile;
import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateMaker;

public class IOMsgPort extends Port {

	protected String operation = "";
	protected String partnerLink = "";
	protected String messageExchange = "";
	protected ArrayList < String [ ] > orderedCSs = new ArrayList < String [ ] >( );
	protected HashMap < String , Integer > CSsNumPerScopeID = new HashMap < String , Integer >( );
	protected ArrayList < String > orderedCSLabels = new ArrayList < String >( );
	protected HashMap < String , Integer > CSLabelsNumPerScopeID = new HashMap < String , Integer >( );
	protected ArrayList < String > varParts = new ArrayList < String >( );

	protected String varPartsScope = "";
	protected String partnerRole = ""; // only for INVOMA
	protected boolean withCS;

	protected String initializePRole = ""; // only for INVOMA
	protected boolean isRcvIO = false;
	protected String openIMAScope = "";
	protected String partnerLinkScope = "";


	public IOMsgPort clonePort( ) {

		IOMsgPort port = new IOMsgPort( );
		port.compIndex = this.compIndex;
		port.compName = this.compName;
		port.operation = this.operation;
		port.partnerLink = this.partnerLink;
		port.messageExchange = this.messageExchange;
		port.varPartsScope = this.varPartsScope;
		port.partnerRole = this.partnerRole;
		port.initializePRole = this.initializePRole;
		port.isRcvIO = this.isRcvIO;
		port.openIMAScope = this.openIMAScope;
		port.partnerLinkScope = this.partnerLinkScope;
		port.withCS = this.withCS;
		port.orderedCSs.addAll( this.orderedCSs );
		port.CSsNumPerScopeID.putAll( this.CSsNumPerScopeID );
		port.orderedCSLabels.addAll( this.orderedCSLabels );
		port.CSLabelsNumPerScopeID = new HashMap < String , Integer >( );
		port.varParts.addAll( this.varParts );

		return port;
	}


	public String getPartnerLinkScope( ) {

		return partnerLinkScope;
	}


	public void setPartnerLinkScope( String partnerLinkScope ) {

		this.partnerLinkScope = partnerLinkScope;
	}


	public String getVarPartsScope( ) {

		return varPartsScope;
	}


	public void setVarPartsScope( String varPartsScope ) {

		this.varPartsScope = varPartsScope;
	}


	public ArrayList < String > getVarParts( ) {

		return varParts;
	}


	public boolean isWithCS( ) {

		return withCS;
	}


	public void setWithCS( boolean withCS ) {

		this.withCS = withCS;
	}


	public String getOpenIMAScope( ) {

		return openIMAScope;
	}


	public void setOpenIMAScope( String openIMAScope ) {

		this.openIMAScope = openIMAScope;
	}


	public IOMsgPort() {

	}


	public boolean isRcvIO( ) {

		return isRcvIO;
	}


	public void setRcvIO( boolean isRcvIO ) {

		this.isRcvIO = isRcvIO;
	}


	public IOMsgPort( String compName , String compIndex , String operation , String partnerLink , String messageExchange ) {

		super( compName, compIndex );
		this.operation = operation;
		this.partnerLink = partnerLink;
		this.messageExchange = messageExchange;
	}


	@Override
	public ArrayList < String > toArray4Template( String template ) {

		ArrayList < String > array = null;
		if ( template.equals( "expIma" ) || template.equals( "expOma" ) ) {
			array = new ArrayList < String >( 3 );
			if ( orderedCSs.size( ) > 0 ) {
				array.add( String.valueOf( orderedCSs.size( ) ) ); // pos: 2
			}
		} else if ( template.equals( "corrIma" ) || template.equals( "corrOma" ) || template.equals( "corrInv" ) ) {
			array = new ArrayList < String >( orderedCSs.size( ) + 3 );
			array.add( String.valueOf( orderedCSs.size( ) ) ); // pos: 2
			for ( String [ ] corrSet : orderedCSs ) {// pos: 3..
				array.add( corrSet[ 1 ] );
			}
		} else if ( template.equals( "sndMsgPorts" ) || template.equals( "ioma" ) ) {
			array = new ArrayList < String >( 3 );
			array.add( getMessageExchangeLabel( ) );
		} else if ( template.equals( "oma" ) ) {
			array = new ArrayList < String >( orderedCSs.size( ) + 4 );
			array.add( getMessageExchangeLabel( ) );// pos:2
			array.add( String.valueOf( orderedCSs.size( ) ) ); // pos: 3
			for ( String [ ] corrSet : orderedCSs ) {// pos: 4..
				array.add( corrSet[ 1 ] );
			}
		} else if ( template.equals( "expInv" ) ) {
			array = new ArrayList < String >( 5 );
			array.add( this.getPartnerLink( ) );// pos:3
			array.add( this.getInitializePRole( ) );// pos:4
			if ( orderedCSs.size( ) > 0 ) {
				array.add( String.valueOf( orderedCSs.size( ) ) ); // pos: 5
			}
		} else if ( template.equals( "inv" ) ) {
			array = new ArrayList < String >( 3 );
			array.add( this.getPartnerLink( ) );// pos:3
			array.add( this.getInitializePRole( ) );// pos:4
		} else if ( template.equals( "rcvMsgPorts" ) ) {
			// not such an entry in any template, but it is checked in the
			// temlate among expRcvMsgPorts
			array = new ArrayList < String >( 3 );
			array.add( this.getMessageExchangeLabel( ) );
		} else {
			// such as RcvMsgPort
			array = new ArrayList < String >( 2 );
		}
		this.toArrayBasic( array );
		return array;
	}


	public boolean isExported( String entry ) {

		if ( entry.equals( "expIma" ) ) {
			return ( this.orderedCSLabels.size( ) + this.orderedCSs.size( ) > 0 || !this.partnerLinkScope.equals( "" ) );

		} else if ( entry.equals( "disIma" ) ) {
			return ( this.orderedCSLabels.size( ) > 0 || !this.partnerLinkScope.equals( "" ) );
		}
		return false;
	}


	public String getMessageExchangeLabel( ) {

		return partnerLink + "_" + operation + "_" + messageExchange;
	}


	public String getCorrelationSetLabel( String CS ) {

		if ( CS.equals( "_" ) ) {
			CS = ""; // local assignment
		}
		return partnerLink + "_" + operation + "_" + CS;
	}


	// public static void main(String[] args) {
	// IOMAPort oma = new IOMAPort();
	// oma.setCorrelationSets(new TreeMap<String, String>());
	// oma.getCorrelationSets().put("la", "lo");
	// oma.getCorrelationSets().put("li", "le");
	// System.out.println(oma.toArray4Template(null).toString());
	//
	// }

	// ///////////////////////////////////////////////////////////////////

	public String getOperation( ) {

		return operation;
	}


	public void setOperation( String operation ) {

		this.operation = operation;
	}


	public String getPartnerLink( ) {

		return partnerLink;
	}


	public void setPartnerLink( String partnerLink ) {

		this.partnerLink = partnerLink;
	}


	public String getMessageExchange( ) {

		return messageExchange;
	}


	public void setMessageExchange( String messageExchange ) {

		this.messageExchange = messageExchange;
	}


	public ArrayList < String [ ] > getOrderedCSs( ) {

		return orderedCSs;
	}


	public void setOrderedCSs( ArrayList < String [ ] > orderedCSs ) {

		this.orderedCSs = orderedCSs;
	}


	public HashMap < String , Integer > getCSsNumPerScopeID( ) {

		return CSsNumPerScopeID;
	}


	public ArrayList < String > getOrderedCSLabels( ) {

		return orderedCSLabels;
	}


	public HashMap < String , Integer > getCSLabelsNumPerScopeID( ) {

		return CSLabelsNumPerScopeID;
	}


	public String getPartnerRole( ) {

		return partnerRole;
	}


	public void setPartnerRole( String partnerRole ) {

		this.partnerRole = partnerRole;
	}


	public String getInitializePRole( ) {

		return initializePRole;
	}


	public void setInitializePRole( String initializePRole ) {

		this.initializePRole = initializePRole;
	}


	public void setScopePartnerLink( ArrayDeque < HashMap < String , ArrayList >> stack , String partnerLink ) {

		// search the name of the scope where the openIMA is checked
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );

			if ( vars.containsKey( MapKeys.PARTNER_LINKS ) ) {
				Set < String > scopePartnerLinks = ( ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.PARTNER_LINKS ) ).keySet( );
				if ( scopePartnerLinks.contains( partnerLink ) ) {
					String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
					this.setPartnerLinkScope( scopeID );
					break;
				}
			}

		}
	}


	public void setScopeOpenIMA( ArrayDeque < HashMap < String , ArrayList >> stack , String partnerLink , String messageExchange ) {

		// search the name of the scope where the openIMA is checked
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			if ( vars.containsKey( MapKeys.PARTNER_LINKS ) ) {
				Set < String > scopePartnerLinks = ( ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.PARTNER_LINKS ) ).keySet( );
				if ( scopePartnerLinks.contains( partnerLink ) ) {
					String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
					this.setOpenIMAScope( scopeID );
					break;
				}
			}
			if ( vars.containsKey( MapKeys.MESSAGE_EXCHANGES ) ) {
				Set < String > scopeMessageExchanges = ( ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.MESSAGE_EXCHANGES ) ).keySet( );
				if ( scopeMessageExchanges.contains( messageExchange ) ) {
					String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
					this.setOpenIMAScope( scopeID );
					break;
				}
			}
		}
	}


	/**
	 * Called when a new IO port is created.
	 * 
	 * @param extractedCSs
	 * @param ioPort
	 * @param attributeName
	 */
	public void addIMAInfo( Element elem , Map < String , String > extractedCSs , StringTemplate template ,
			ArrayDeque < HashMap < String , ArrayList >> stack , String attributeName ) {

		String partnerLink = elem.getAttribute( "partnerLink" );

		// create a temporary set, that initially has all unorderd CSs
		Set < String > unorderedCSs = new HashSet < String >( extractedCSs.keySet( ) );

		if ( unorderedCSs.size( ) > 0 ) {
			this.withCS = true;
		}

		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			// check the correlations sets in the current node's HashMap

			if ( vars.containsKey( MapKeys.PARTNER_LINKS ) ) {
				Set < String > scopePartnerLinks = ( ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.PARTNER_LINKS ) ).keySet( );
				if ( scopePartnerLinks.contains( partnerLink ) ) {
					// if partnerLink is found, add all CSs to this scope
					for ( String CSname : unorderedCSs ) {
						// add each CS to the rightmost (end) of the list
						this.orderedCSLabels.add( CSname );
					}
					// add the scopeID and their number in the
					String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
					int scopeCSLabelsAdded = unorderedCSs.size( );
					this.CSLabelsNumPerScopeID.put( scopeID , scopeCSLabelsAdded );
					unorderedCSs.clear( );

					this.partnerLinkScope = scopeID;
				}
			} else {
				// if parterLink is not found, search for CSs in scope
				HashMap < String , ArrayList < String >> CSs = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.CORRELATION_SETS );
				ArrayList < String > csEntry = null;
				if ( CSs != null && CSs.size( ) > 0 ) {
					// add the CSs that we still search
					Set < String > CSNeededAndInScope = new HashSet < String >( unorderedCSs );
					// interesect with CSs in scope
					CSNeededAndInScope.retainAll( CSs.keySet( ) );
					// remove the CSs found in scope
					unorderedCSs.removeAll( CSNeededAndInScope );
					for ( String CSname : CSNeededAndInScope ) {
						// add each CS to the rightmost (end) of the list
						this.orderedCSLabels.add( CSname ); // exported come
															// after
					}
					int scopeCSLabelsAdded = CSNeededAndInScope.size( );
					if ( scopeCSLabelsAdded > 0 ) {
						// add the scopeID and their number in the
						String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
						this.CSLabelsNumPerScopeID.put( scopeID , scopeCSLabelsAdded );
					}

				}
			}
			if ( unorderedCSs.size( ) == 0 && !this.partnerLinkScope.equals( "" ) ) {
				break;// all the CSLabels are ordered
			}

		}
		// sort the CSLabels : END

		// sort the CSs : StringTemplateART
		unorderedCSs = new HashSet < String >( extractedCSs.keySet( ) );
		// iterate the HashMaps of nodes looking for corrSetLabels

		it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			// check the correlations sets in the current node's HashMap
			HashMap < String , ArrayList < String >> CSs = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
					MapKeys.CORRELATION_SETS );
			ArrayList < String > csEntry = null;
			if ( CSs != null && CSs.size( ) > 0 ) {
				// interesect with CSs in scope
				Set < String > CSNeededAndInScope;
				( CSNeededAndInScope = new HashSet < String >( unorderedCSs ) ).retainAll( CSs.keySet( ) );
				// remove the CSs found in scope
				unorderedCSs.removeAll( CSNeededAndInScope );
				for ( String CSname : CSNeededAndInScope ) {
					String initiateValue = extractedCSs.get( CSname );

					int initiateDiscr = 0; // 0 for no
					if ( initiateValue.equals( "yes" ) )
						initiateDiscr = 1;
					else if ( initiateValue.equals( "join" ) )
						initiateDiscr = 2;

					// add each CS to the rightmost (end) of the list
					orderedCSs.add( new String [ ] { CSname , String.valueOf( initiateDiscr ) } );
				}

				int scopeCSsAdded = CSNeededAndInScope.size( );
				if ( scopeCSsAdded > 0 ) {
					// add the scopeID and their number in the
					String scopeID = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
					this.CSsNumPerScopeID.put( scopeID , scopeCSsAdded );
				}

				if ( unorderedCSs.size( ) == 0 ) {
					// all the corrSets are ordered
					break;
				}
			}
		}
		// sort the CSs : END

		if ( this.orderedCSs.size( ) > 0 ) {
			// add initiate values to template attribute initiateList
			for ( String [ ] cs : this.orderedCSs ) {
				template.setAttribute( attributeName , cs[ 1 ] );
			}
			// add the number of initiate values at the end of the initiateList
			template.setAttribute( attributeName , this.orderedCSs.size( ) );
		}

	}


	/**
	 * Add the parts of a referenced variable found in invoke, receive and reply
	 * activities into the writePort.
	 * 
	 * @param stack
	 * @param elem
	 * @param writePort
	 * @throws Exception
	 */
	public void addVarParts( ArrayDeque < HashMap < String , ArrayList >> stack , Element elem ) {

		/* receive will either have a variable attribute or a fromParts element */
		if ( elem.hasAttribute( "variable" ) ) { // in 'receive', 'reply' or
													// 'copy'
			if ( elem.hasAttribute( "part" ) ) { // in'copy'
				this.varParts.add( elem.hasAttribute( "variable" ) + "_" + elem.getAttribute( "part" ) );
			} else if ( elem.hasAttribute( "property" ) ) { // in'copy'
				this.varParts.add( elem.hasAttribute( "variable" ) + "_" + elem.getAttribute( "property" ) );
			} else { // in all
				this.varParts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "variable" ) ) );
			}
		} else if ( elem.hasAttribute( "outputVariable" ) ) { // in 'invoke'
			this.varParts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "outputVariable" ) ) );
		} else {
			ArrayList < Element > toPartElems;
			toPartElems = XMLFile.getChildrenInWrapper( elem , "fromParts" , "fromPart" );

			if ( toPartElems.size( ) > 0 ) {
				for ( Element toPart : toPartElems ) {
					String part = toPart.getAttribute( "toVariable" ) + "_" + toPart.getAttribute( "part" );
					this.varParts.add( part );
				}
			} else { // in 'copy'
				String variableString = elem.getTextContent( );
				if ( variableString != null ) { // only in
					int ind = variableString.indexOf( "." );
					if ( ind > 0 ) {
						String variable = variableString.replace( "$" , "" );
						String part = variable.replace( '.' , '_' );
						this.varParts.add( part );
					} else {
						String variable = variableString.replace( "$" , "" );
						this.varParts.addAll( MapUtils.getVariableParts( stack , variable ) );
					}
				}
			}
		}

		// if there are variable parts to read, find in stack the index of the
		// scope where variable is declared
		if ( this.varParts.size( ) > 0 ) {
			String variable = this.varParts.get( 0 ).split( "_" )[ 0 ];
			Iterator it = stack.iterator( );
			while ( it.hasNext( ) ) {
				HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
				HashMap < String , ArrayList < String >> variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.VARIABLES );
				if ( variables != null && variables.size( ) > 0 && ( this.varParts = variables.get( variable ) ) != null && this.varParts.size( ) > 0 ) {
					this.setVarPartsScope( ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID ) );
					break;
				}
				variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars , MapKeys.PARTNER_LINKS );
				if ( variables != null && variables.size( ) > 0 && ( this.varParts = variables.get( variable ) ) != null && this.varParts.size( ) > 0 ) {
					this.setVarPartsScope( ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID ) );
					break;
				}
			}

		}
	}

}
