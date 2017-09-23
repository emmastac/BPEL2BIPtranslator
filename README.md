
                         BPEL2BIP tool

 
  What is it?
  -----------

  BPEL2BIP is a tool that translates BPEL programs (with their WSDL   descriptions) into BIP models. 


  Latest Version
  ------------------

  Currently, only one version is available and can be downloaded at https://github.com/emmastac/BPEL2BIPtranslator.git

Dependencies
--------------
-antlr-3.4-complete.jar
-commons-cli-1.3.jar
-ode-bpel-compiler-1.3.6.jar
-org.eclipse.jdt.core_3.10.2.v20150120-1634.jar
-org.eclipse.jface.text_3.5.89.jar
-org.eclipse.text_3.5.0.jar
-stringtemplate-3.0.jar
-wsdl4j.jar



 Contents
  -----------

 This .rar file includes:
  - A /src folder with the BPEL2BIP's classes. This jar is used for the (i) translation of BPEL programs, and for (ii) the analysis of the BIP state space exploration's output.
  - A bpelHome_sample folder with BPEL programs.
  - A translated2_sample folder with BIP models derived from the BPEL programs within bpelHome.
  - A LICENCE file that states the tool's licencing information
  - A README file that summarizes all details of the tool's current realease. 

  Installation
  ------------

  No installation is needed for BPEL2BIP. A Java version (1.7+) is required. However, the New BIP tools are needed in order to analyze the produced models, which can be found under http://www-verimag.imag.fr/New-BIP-tools.html

  Execution
  ------------

  BPEL2BIP has only a command line interface.
  
   A.For translating BPEL programs to BIP, you need to:
	1. put BPEL programs inside a folder called 'bpelHome', which should be placed at the same folder as the bpel2bip.jar (the bpelHome_sample folder can be used if it is renamed as bpelHome)
	2. for each BPEL program, put all the needed WSDL files inside the BPEL program's parent folder (not in subfolders)
	3. create a file named prog_cfg.txt. Write at each line, the path of each BPEL program that should be translated. Paths should be relative to 'bpelHome' (excluding 'bpelHome').
	4. run translation with the following command:
	   $ java -jar bpel2bip.jar -translate 
	5. when translation is finished, a folder called 'translated2' will have been created, that contains the produced BIP models and a 'prog_cfg_out.txt' file with the paths of the produced models. Each BIP model, along its build.sh and source files is placed within a seaparte subfolder that is named after its associated BPEL program.

  B. For building and analyzing the BIP models, you need to
	1. go into the folder of each BIP model. 
	2. build the model with the following command:
	  $ ./build.sh
	3. when the model is built, run state space exploration with the following command:
	  $ ./build/system --explore >& exploreOut.txt
	4. when state space exploration is finished, the 'exploreOut.txt' file will contain the exploration's output.
	

  C.For analyzing the output of state space exploration, you need to:
	1. make sure that BIP models are built and are inside the translated2 folder, which is found at the same folder as the bpel2bip.jar. (if A and B were followed, then this applies always)
	2. make sure that the 'prog_cfg_out.txt' file is found inside the 'translated2' folder (not in subfolders) (if A and B were followed, then this applies always)
	3. run analysis with the following command:
	   $ java -jar bpel2bip.jar -analyze 
	4. when analysis is finished, the 'analysisOut.txt' file with the exploration's analyzed output will have been created.  

  Licensing
  ---------

  This information is in a separate file named LICENSE.

  Contact
  --------

   If you want to provide us with bug reports, or if you need support with BPEL2BIP's execution, feel free to contact me (Emmanouela Stachtiari, Ph.D student) at emmastac@csd.auth.gr.


