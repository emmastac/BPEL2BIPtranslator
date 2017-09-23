#ifndef INCLUDE_CHECK_ATOMIPORT__CHECK__E1B0PORT_HPP_
#define INCLUDE_CHECK_ATOMIPORT__CHECK__E1B0PORT_HPP_

#include <AtomInternalPort.hpp>

#include <check/PT_check_e1b0port.hpp>
#include <check/PV_check_e1b0port.hpp>

class AtomIPort_check_e1b0port : public AtomInternalPort {
public:
    AtomIPort_check_e1b0port(const string &name);
    virtual ~AtomIPort_check_e1b0port();

    PortValue &portValue() const;
    bool hasPortValue() const;
    void setPortValue(PortValue &portValue);
    void clearPortValue();

    // data fields from Port type.
    void setPtr_msg1(int *_val);
    void syncFromPortValue() const;
    void syncToPortValue() const;

    bool isEnabled() const;
    void setIsEnabled(bool b);

protected:
    PV_check_e1b0port *mPortValue;

private:
    bool mIsEnabled;
    // data fields from Port type.
    int *m_msg1;
}; 

inline
PortValue &AtomIPort_check_e1b0port::portValue() const {
    return *mPortValue;
}

inline
bool AtomIPort_check_e1b0port::hasPortValue() const {
    return mPortValue != NULL;
}

inline
void AtomIPort_check_e1b0port::setPortValue(PortValue &portValue) {
    assert(dynamic_cast<PV_check_e1b0port *>(&portValue) != NULL);
    mPortValue = static_cast<PV_check_e1b0port *> (&portValue);
}
inline
void AtomIPort_check_e1b0port::clearPortValue() {
    mPortValue = NULL;
}

inline
bool AtomIPort_check_e1b0port::isEnabled() const {
    return mIsEnabled;
}

inline
void AtomIPort_check_e1b0port::setIsEnabled(bool b) {
    mIsEnabled = b;
}

    // data fields from Port type.
inline
void AtomIPort_check_e1b0port::setPtr_msg1(int *_val) {
    m_msg1 = _val;
}

#endif // INCLUDE_CHECK_ATOMIPORT__CHECK__E1B0PORT_HPP_
