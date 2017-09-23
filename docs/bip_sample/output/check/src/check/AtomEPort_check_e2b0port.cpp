
#include <check/AtomEPort_check_e2b0port.hpp>
#include <check/PV_check_e2b0port.hpp>

AtomEPort_check_e2b0port::AtomEPort_check_e2b0port(const string &name) :
    PortItf(name, ATOM_EXPORT),
    Port(name, ATOM_EXPORT),
    AtomExportPort(name),
    PT_check_e2b0port(name, ATOM_EXPORT),
    mIsReset(false) {
}

AtomEPort_check_e2b0port::~AtomEPort_check_e2b0port(){
}
