#ifndef INCLUDE_CHECK_PV__CHECK__E0B0PORT_HPP_
#define INCLUDE_CHECK_PV__CHECK__E0B0PORT_HPP_

#include <PortValue.hpp>

#include <check/PT_check_e0b0port.hpp>

class PV_check_e0b0port : public PortValue {
public:
    PV_check_e0b0port();
    virtual ~PV_check_e0b0port();

    virtual string toString() const;


private:
}; 




#endif // INCLUDE_CHECK_PV__CHECK__E0B0PORT_HPP_
