// here we should have includes for all used types
// from all packages.
#include "DeployTypes.hpp"

Component* deploy(int argc, char **argv);
bool isSerializeEnabled();
void serialize(char **buf, size_t *len);
void deserialize(const char *buf, size_t len);

namespace staticallocated{

    // data param for Comp_ROOT./
    

    AtomIPort_check_e0b0port iport_ROOT_A1_AtoB("AtoB");

    AtomIPort_check_e0b0port iport_ROOT_A1_CtoB("CtoB");

    AtomIPort_check_e0b0port iport_ROOT_A1_BtoA("BtoA");

    AtomIPort_check_e0b0port iport_ROOT_A1_AtoC("AtoC");

    AtomEPort_check_e0b0port port_ROOT_A1_AtoB("AtoB");

    AtomEPort_check_e0b0port port_ROOT_A1_CtoB("CtoB");

    // static init for Atom: Comp_ROOT_A1
    

    AT_check_Atom1 Comp_ROOT_A1(
     "A1"
     , iport_ROOT_A1_AtoB, iport_ROOT_A1_CtoB, iport_ROOT_A1_BtoA, iport_ROOT_A1_AtoC
     , port_ROOT_A1_AtoB, port_ROOT_A1_CtoB
     
     
    );


    AtomIPort_check_e0b0port iport_ROOT_A2_AtoB("AtoB");

    AtomIPort_check_e0b0port iport_ROOT_A2_CtoB("CtoB");

    AtomIPort_check_e0b0port iport_ROOT_A2_BtoA("BtoA");

    AtomIPort_check_e0b0port iport_ROOT_A2_AtoC("AtoC");

    AtomEPort_check_e0b0port port_ROOT_A2_AtoB("AtoB");

    AtomEPort_check_e0b0port port_ROOT_A2_CtoB("CtoB");

    // static init for Atom: Comp_ROOT_A2
    

    AT_check_Atom2 Comp_ROOT_A2(
     "A2"
     , iport_ROOT_A2_AtoB, iport_ROOT_A2_CtoB, iport_ROOT_A2_BtoA, iport_ROOT_A2_AtoC
     , port_ROOT_A2_AtoB, port_ROOT_A2_CtoB
     
     
    );


    QPR_check_e0b0port ref_ROOT_bothAB_p1(port_ROOT_A1_AtoB, false);

    QPR_check_e0b0port ref_ROOT_bothAB_p2(port_ROOT_A2_AtoB, false);

    ConnT_check_RDV2 Conn_ROOT_bothAB(
        "bothAB",
        ref_ROOT_bothAB_p1, ref_ROOT_bothAB_p2
        
        );

    QPR_check_e0b0port ref_ROOT_brdcastCB_p1(port_ROOT_A1_CtoB, false);

    QPR_check_e0b0port ref_ROOT_brdcastCB_p2(port_ROOT_A2_CtoB, false);

    ConnT_check_BRDCAST2 Conn_ROOT_brdcastCB(
        "brdcastCB",
        ref_ROOT_brdcastCB_p1, ref_ROOT_brdcastCB_p2
        
        );

    // static init for Compound: Comp_ROOT

    CT_check_comp Comp_ROOT(
      "ROOT"
     , Comp_ROOT_A1, Comp_ROOT_A2
     
     , Conn_ROOT_bothAB, Conn_ROOT_brdcastCB
     
     
     
     
    );


// End of namespace.
};
