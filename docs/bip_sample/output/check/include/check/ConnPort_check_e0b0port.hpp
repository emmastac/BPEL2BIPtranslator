#ifndef INCLUDE_CHECK_CONNPORT__CHECK__E0B0PORT_HPP_
#define INCLUDE_CHECK_CONNPORT__CHECK__E0B0PORT_HPP_

#include <ConnectorExportPort.hpp>

#include <check/PT_check_e0b0port.hpp>

class ConnPort_check_e0b0port : public virtual Port,
                public ConnectorExportPort,
                public PT_check_e0b0port {
public:
    ConnPort_check_e0b0port(const string &name);
    virtual ~ConnPort_check_e0b0port();
}; 

#endif // INCLUDE_CHECK_CONNPORT__CHECK__E0B0PORT_HPP_
