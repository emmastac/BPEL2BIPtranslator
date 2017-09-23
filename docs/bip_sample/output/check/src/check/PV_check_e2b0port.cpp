#include <check/PV_check_e2b0port.hpp>

#include <iostream>
#include <sstream>

PV_check_e2b0port::PV_check_e2b0port(int _m_msg1, int _m_msg2) : PortValue(),  m_msg1(_m_msg1),  m_msg2(_m_msg2) {
}

PV_check_e2b0port::PV_check_e2b0port() : PortValue(){
}


string PV_check_e2b0port::toString() const {
    ostringstream oss;

    oss << "msg1=" << m_msg1 << ';';
    oss << "msg2=" << m_msg2 << ';';
    return oss.str();
}

PV_check_e2b0port::~PV_check_e2b0port() {
}


