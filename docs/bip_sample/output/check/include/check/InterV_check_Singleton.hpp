#ifndef INCLUDE_CHECK_INTERV__CHECK__SINGLETON_HPP_
#define INCLUDE_CHECK_INTERV__CHECK__SINGLETON_HPP_


#include <check/Inter_check_Singleton.hpp>

class Port;
class PortValue;
class ConnT_check_Singleton;

class InterV_check_Singleton : public InteractionValue {
public:
    virtual ~InterV_check_Singleton();

    // getters for references (declared pure virtual in interface)
    virtual const Interaction &interaction() const { return mInteraction; }
    virtual const vector<PortValue *> &portValues() const;
    virtual bool hasPortValues() const;

    // specific operations
    const vector<Port *> &ports() const { return mInteraction.ports(); }

    // Only allow the connector to call the ctor here (see createInteractionValue() method)
    friend class ConnT_check_Singleton;

protected:
    InterV_check_Singleton(const ConnT_check_Singleton &connector, const Interaction& interaction, const vector<PortValue *> &values);
    Inter_check_Singleton mInteraction;
    vector<PortValue *> &portValues();
    vector<PortValue *> mPortValues;

    void commonRecycle(const Interaction& interaction, const vector<PortValue *> &values);

}; 

inline
const vector<PortValue *> &InterV_check_Singleton::portValues() const {
    return this->mPortValues;
}

inline
bool InterV_check_Singleton::hasPortValues() const {
    return (!this->mPortValues.empty());
}

inline
vector<PortValue *> &InterV_check_Singleton::portValues() {
    return mPortValues;
}

#endif // INCLUDE_CHECK_INTERV__CHECK__SINGLETON_HPP_
