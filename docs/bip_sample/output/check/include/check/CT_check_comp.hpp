#ifndef CHECK_CT__CHECK__COMP_HPP_
#define CHECK_CT__CHECK__COMP_HPP_

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:73:0
// include package "master" header
#include <check.hpp>

#include <Compound.hpp>

// User include given in @cpp annotation
#include <stdio.h>

// for component types
#include <check/AT_check_Atom1.hpp>
#include <check/AT_check_Atom2.hpp>

// for connector types
#include <check/ConnT_check_RDV2.hpp>
#include <check/ConnT_check_BRDCAST2.hpp>


class CT_check_comp : public Compound {
public:
    CT_check_comp (const string &name, AT_check_Atom1 &_comp_decl__A1, AT_check_Atom2 &_comp_decl__A2
                                  , ConnT_check_RDV2 &_conn_decl__bothAB, ConnT_check_BRDCAST2 &_conn_decl__brdcastCB
                                  
                                  
                                  
                                  
    
    );
    virtual ~CT_check_comp();


private:

    // SubComponent decls
    AT_check_Atom1 &_comp_decl__A1;
    AT_check_Atom2 &_comp_decl__A2;

    // connector decls
    ConnT_check_RDV2 &_conn_decl__bothAB;
    ConnT_check_BRDCAST2 &_conn_decl__brdcastCB;
};



#endif // CHECK_CT__CHECK__COMP_HPP_
