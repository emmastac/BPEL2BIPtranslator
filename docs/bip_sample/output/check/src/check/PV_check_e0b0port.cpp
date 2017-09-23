#include <check/PV_check_e0b0port.hpp>

#include <iostream>
#include <sstream>

PV_check_e0b0port::PV_check_e0b0port() : PortValue() {
}


string PV_check_e0b0port::toString() const {
    ostringstream oss;

    return oss.str();
}

PV_check_e0b0port::~PV_check_e0b0port() {
}


