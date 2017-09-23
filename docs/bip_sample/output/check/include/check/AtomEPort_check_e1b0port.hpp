#ifndef INCLUDE_CHECK_ATOMEPORT__CHECK__E1B0PORT_HPP_
#define INCLUDE_CHECK_ATOMEPORT__CHECK__E1B0PORT_HPP_

#include <AtomExportPort.hpp>
#include <check/AtomIPort_check_e1b0port.hpp>

#include <check/PT_check_e1b0port.hpp>

class AtomEPort_check_e1b0port :
    public virtual Port,
    public AtomExportPort,
    public PT_check_e1b0port {
public:
    AtomEPort_check_e1b0port(const string &name);
    virtual ~AtomEPort_check_e1b0port();
    virtual void addInternalPort(AtomIPort_check_e1b0port &p);

    virtual vector<PortValue *> &portValues();
    virtual const vector<PortValue *> &portValues() const;
    virtual bool hasPortValues() const;
    virtual void addPortValue(PortValue &port);
    virtual void clearPortValues();

    virtual bool isReset() const;
    void setIsReset(bool b);

protected:
// Getting messy to store actual type and return more abstract one (thanks to vector template...)
//    vector<PV_check_e1b0port *> mPortValues;
    vector<PortValue *> mPortValues;
    bool mIsReset;
}; 


inline
void AtomEPort_check_e1b0port::addInternalPort(AtomIPort_check_e1b0port &p) {
    AtomExportPort::addInternalPort(p);
}

inline
vector<PortValue *> &AtomEPort_check_e1b0port::portValues() {
    return mPortValues;
}

inline
const vector<PortValue *> &AtomEPort_check_e1b0port::portValues() const {
    return mPortValues;
}

inline
bool AtomEPort_check_e1b0port::hasPortValues() const {
    return !mPortValues.empty();
}

inline
void AtomEPort_check_e1b0port::addPortValue(PortValue &port) {
    mPortValues.push_back(&port);
}

inline
void AtomEPort_check_e1b0port::clearPortValues() {
    mPortValues.clear();
}

inline
bool AtomEPort_check_e1b0port::isReset() const {
    return mIsReset;
}

inline
void AtomEPort_check_e1b0port::setIsReset(bool b) {
    mIsReset = b;
}
#endif // INCLUDE_CHECK_ATOMEPORT__CHECK__E1B0PORT_HPP_
