#include <check/AtomIPort_check_e1b0port.hpp>

AtomIPort_check_e1b0port::AtomIPort_check_e1b0port(const string &name) : AtomInternalPort(name),
    mIsEnabled(false) {
}

void AtomIPort_check_e1b0port::syncFromPortValue() const {
    // data fields from Port type.
    *m_msg1 = mPortValue->get_msg1();
}

void AtomIPort_check_e1b0port::syncToPortValue() const {
    // data fields from Port type.
    mPortValue->set_msg1(*m_msg1);
}

AtomIPort_check_e1b0port::~AtomIPort_check_e1b0port() {
}
