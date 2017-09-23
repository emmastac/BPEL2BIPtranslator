package observers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import translator.TemplateMaker;
import bpelUtils.BPELFile;

public abstract class BIPPropertyObserver {

	protected String name;
	protected TemplateMaker tm;


	public BIPPropertyObserver( String name , TemplateMaker tm ) {

		this.name = name;
		this.tm = tm;
	}


	public void pre_observeActivity( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		return;
	}


	public void post_observeActivity( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret )
			throws Exception {

		return;
	}


	public boolean configure( String bpelFile , String sourceHome ) throws Exception {
		return true;
	}


	protected File getConfigFile( String bpelFile , String sourceProjectsPath , String configName ) {
	
		String  confpath = new File( bpelFile ).getParentFile( ).toString( )+"/"+configName;
		return new File( confpath );
	}
}
