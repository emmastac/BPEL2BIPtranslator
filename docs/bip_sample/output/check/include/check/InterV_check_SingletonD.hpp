#ifndef INCLUDE_CHECK_INTERV__CHECK__SINGLETOND_HPP_
#define INCLUDE_CHECK_INTERV__CHECK__SINGLETOND_HPP_


#include <check/Inter_check_SingletonD.hpp>
// port values for exported port.
#include <check/CPV_SingletonD__e0b0port.hpp>

class Port;
class PortValue;
class ConnT_check_SingletonD;

class InterV_check_SingletonD : public InteractionValue {
public:
    virtual ~InterV_check_SingletonD();

    // getters for references (declared pure virtual in interface)
    virtual const Interaction &interaction() const { return mInteraction; }
    virtual const vector<PortValue *> &portValues() const;
    virtual bool hasPortValues() const;

    // specific operations
    const vector<Port *> &ports() const { return mInteraction.ports(); }

    // Only allow the connector to call the ctor here (see createInteractionValue() method)
    friend class ConnT_check_SingletonD;

protected:
    InterV_check_SingletonD(const ConnT_check_SingletonD &connector, const Interaction& interaction, const vector<PortValue *> &values);
    Inter_check_SingletonD mInteraction;
    vector<PortValue *> &portValues();
    vector<PortValue *> mPortValues;

    void commonRecycle(const Interaction& interaction, const vector<PortValue *> &values);

    mutable CPV_SingletonD__e0b0port mExportedPortValue;
}; 

inline
const vector<PortValue *> &InterV_check_SingletonD::portValues() const {
    return this->mPortValues;
}

inline
bool InterV_check_SingletonD::hasPortValues() const {
    return (!this->mPortValues.empty());
}

inline
vector<PortValue *> &InterV_check_SingletonD::portValues() {
    return mPortValues;
}

#endif // INCLUDE_CHECK_INTERV__CHECK__SINGLETOND_HPP_
