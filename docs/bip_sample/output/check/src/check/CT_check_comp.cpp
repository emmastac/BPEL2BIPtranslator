// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:73:0

#include <check/CT_check_comp.hpp>

CT_check_comp::CT_check_comp (const string &name, AT_check_Atom1 &_comp_decl__A1, AT_check_Atom2 &_comp_decl__A2
                              , ConnT_check_RDV2 &_conn_decl__bothAB, ConnT_check_BRDCAST2 &_conn_decl__brdcastCB
                              
                              
                              
                              

) : ComponentItf(name, COMPOUND), Compound(name), _comp_decl__A1(_comp_decl__A1), _comp_decl__A2(_comp_decl__A2), _conn_decl__bothAB(_conn_decl__bothAB), _conn_decl__brdcastCB(_conn_decl__brdcastCB) {
    this->addComponent(_comp_decl__A1);
    this->addComponent(_comp_decl__A2);
    this->addConnector(_conn_decl__bothAB);
    this->addConnector(_conn_decl__brdcastCB);
}

CT_check_comp::~CT_check_comp() {
}
