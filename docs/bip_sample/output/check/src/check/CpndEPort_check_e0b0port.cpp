#include <check/CpndEPort_check_e0b0port.hpp>

#include <check/PV_check_e0b0port.hpp>

CpndEPort_check_e0b0port::CpndEPort_check_e0b0port(const string &name) :
    PortItf(name, COMPOUND_EXPORT),
    Port(name, COMPOUND_EXPORT),
    CompoundExportPort(name),
    PT_check_e0b0port(name, COMPOUND_EXPORT) {
}

CpndEPort_check_e0b0port::~CpndEPort_check_e0b0port() {
}
