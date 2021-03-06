#ifndef INCLUDE_CHECK_QPR__CHECK__E2B0PORT_HPP_
#define INCLUDE_CHECK_QPR__CHECK__E2B0PORT_HPP_

#include <QuotedPortReference.hpp>

#include <check/PT_check_e2b0port.hpp>

class QPR_check_e2b0port : public QuotedPortReference {
public:
    QPR_check_e2b0port(PT_check_e2b0port &port, const bool &trigger);
    virtual ~QPR_check_e2b0port();
}; 

#endif // INCLUDE_CHECK_QPR__CHECK__E2B0PORT_HPP_
