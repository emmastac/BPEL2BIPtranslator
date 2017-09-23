#ifndef INCLUDE_CHECK_ATOMIPORT__CHECK__E0B0PORT_HPP_
#define INCLUDE_CHECK_ATOMIPORT__CHECK__E0B0PORT_HPP_

#include <AtomInternalPort.hpp>

#include <check/PT_check_e0b0port.hpp>
#include <check/PV_check_e0b0port.hpp>

class AtomIPort_check_e0b0port : public AtomInternalPort {
public:
    AtomIPort_check_e0b0port(const string &name);
    virtual ~AtomIPort_check_e0b0port();

    PortValue &portValue() const;
    bool hasPortValue() const;
    void setPortValue(PortValue &portValue);
    void clearPortValue();

    void syncFromPortValue() const;
    void syncToPortValue() const;

    bool isEnabled() const;
    void setIsEnabled(bool b);

protected:
    PV_check_e0b0port *mPortValue;

private:
    bool mIsEnabled;
}; 

inline
PortValue &AtomIPort_check_e0b0port::portValue() const {
    return *mPortValue;
}

inline
bool AtomIPort_check_e0b0port::hasPortValue() const {
    return mPortValue != NULL;
}

inline
void AtomIPort_check_e0b0port::setPortValue(PortValue &portValue) {
    assert(dynamic_cast<PV_check_e0b0port *>(&portValue) != NULL);
    mPortValue = static_cast<PV_check_e0b0port *> (&portValue);
}
inline
void AtomIPort_check_e0b0port::clearPortValue() {
    mPortValue = NULL;
}

inline
bool AtomIPort_check_e0b0port::isEnabled() const {
    return mIsEnabled;
}

inline
void AtomIPort_check_e0b0port::setIsEnabled(bool b) {
    mIsEnabled = b;
}


#endif // INCLUDE_CHECK_ATOMIPORT__CHECK__E0B0PORT_HPP_
