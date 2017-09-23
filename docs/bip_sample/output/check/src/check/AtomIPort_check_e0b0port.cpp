#include <check/AtomIPort_check_e0b0port.hpp>

AtomIPort_check_e0b0port::AtomIPort_check_e0b0port(const string &name) : AtomInternalPort(name),
    mIsEnabled(false) {
}

void AtomIPort_check_e0b0port::syncFromPortValue() const {
}

void AtomIPort_check_e0b0port::syncToPortValue() const {
}

AtomIPort_check_e0b0port::~AtomIPort_check_e0b0port() {
}
