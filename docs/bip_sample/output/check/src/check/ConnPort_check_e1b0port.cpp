#include <check/ConnPort_check_e1b0port.hpp>

ConnPort_check_e1b0port::ConnPort_check_e1b0port(const string &name) : 
    PortItf(name, CONNECTOR_EXPORT),
    Port(name, CONNECTOR_EXPORT),
    ConnectorExportPort(name),
    PT_check_e1b0port(name, CONNECTOR_EXPORT) {
}

ConnPort_check_e1b0port::~ConnPort_check_e1b0port() {
}
