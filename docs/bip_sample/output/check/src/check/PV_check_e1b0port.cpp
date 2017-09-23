#include <check/PV_check_e1b0port.hpp>

#include <iostream>
#include <sstream>

PV_check_e1b0port::PV_check_e1b0port(int _m_msg1) : PortValue(),  m_msg1(_m_msg1) {
}

PV_check_e1b0port::PV_check_e1b0port() : PortValue(){
}


string PV_check_e1b0port::toString() const {
    ostringstream oss;

    oss << "msg1=" << m_msg1 << ';';
    return oss.str();
}

PV_check_e1b0port::~PV_check_e1b0port() {
}


