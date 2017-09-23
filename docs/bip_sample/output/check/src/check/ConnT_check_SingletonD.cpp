// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:26:0

#include <check/ConnT_check_SingletonD.hpp>

#include <check/PV_check_e0b0port.hpp>

// for exported port.
#include <check/CPV_SingletonD__e0b0port.hpp>
#include <check/PV_check_e0b0port.hpp>
#include <check/PT_check_e0b0port.hpp>

static inline PortValue * getPortValue(const QuotedPortReference & qpr, const InteractionValue &civ){
    Port &p = qpr.port();
    vector<Port *>::size_type idx = 0;
    for (vector<Port *>::const_iterator i = civ.ports().begin();
         i != civ.ports().end();
         i++, idx++){
        if (&p == *i) break;
    }
    assert (idx < civ.ports().size()); // means we couldn't find corresponding port value 
    return civ.portValues()[idx];
}

static inline PV_check_e0b0port * get_p(const QPR_check_e0b0port & qpr, const InterV_check_SingletonD &civ){
  return dynamic_cast<PV_check_e0b0port *>(getPortValue(qpr, civ));
}


ConnT_check_SingletonD::ConnT_check_SingletonD(const string &name, QPR_check_e0b0port &p, ConnPort_check_e0b0port &xpr) : Connector(name), p(p), xpr(xpr) {
    this->addPort(p);
    this->setExportedPort(xpr);
}

ConnT_check_SingletonD::~ConnT_check_SingletonD() {
    for (vector<Interaction *>::const_iterator interactionIt = definedInteractions.begin() ;
         interactionIt != definedInteractions.end() ;
         ++interactionIt) {
        delete *interactionIt;
    }
    definedInteractions.clear();
}

PortValue &ConnT_check_SingletonD::up(const InteractionValue &interactionValue) const {
    assert(dynamic_cast<const InterV_check_SingletonD *>(&interactionValue) != NULL);
    const InterV_check_SingletonD *c_interaction_v __attribute__((unused)) = static_cast<const InterV_check_SingletonD *>(&interactionValue);
    const Inter_check_SingletonD *c_interaction __attribute__((unused)) = &(c_interaction_v->mInteraction);
    CPV_SingletonD__e0b0port *cpv __attribute__((unused))= &(c_interaction_v->mExportedPortValue);
    
    
    return *cpv;
}

void ConnT_check_SingletonD::down(InteractionValue &interactionValue) const {
    // You should *never* reach this. Calling this method denotes an error in
    // scheduling (the down(interactionvalue, portvalue) should have been used)
    assert(false);
}

void ConnT_check_SingletonD::down(InteractionValue &interactionValue, PortValue &portValue) const {
    assert(dynamic_cast<CPV_SingletonD__e0b0port *>(&portValue) != NULL);
    CPV_SingletonD__e0b0port *cpv __attribute__((unused))= static_cast<CPV_SingletonD__e0b0port *>(&portValue);
    
}

Interaction &ConnT_check_SingletonD::createInteraction() const{
  return *new Inter_check_SingletonD(*this);
}

Interaction &ConnT_check_SingletonD::createInteraction(const vector<Port *> &ports) const{
    return *new Inter_check_SingletonD(*this, ports);
}

InteractionValue &ConnT_check_SingletonD::createInteractionValue(const Interaction &interaction, const vector<PortValue *> &values) const {
    return *new InterV_check_SingletonD(*this, interaction, values);
}

void ConnT_check_SingletonD::releaseInteraction(Interaction &interaction) const {
    delete &interaction;
}

void ConnT_check_SingletonD::releaseInteractionValue(InteractionValue &interactionValue) const {
    delete &interactionValue;
}

bool ConnT_check_SingletonD::guard(const InteractionValue &interactionValue) const {


    // default to true. May not be the wisest choice.
    return true;
}

const vector<Interaction*>& ConnT_check_SingletonD::interactions() const {
  if (definedInteractions.empty()) {
    definedInteractions.push_back(new Inter_check_SingletonD(*this, true));
  }

  // check that the number of defined interactions is correct
  assert(definedInteractions.size() == 1);

  return definedInteractions;
}
