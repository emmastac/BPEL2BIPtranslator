// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:31:0

#include <check/ConnT_check_Singleton.hpp>

#include <check/PV_check_e0b0port.hpp>


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

static inline PV_check_e0b0port * get_p(const QPR_check_e0b0port & qpr, const InterV_check_Singleton &civ){
  return dynamic_cast<PV_check_e0b0port *>(getPortValue(qpr, civ));
}


ConnT_check_Singleton::ConnT_check_Singleton(const string &name, QPR_check_e0b0port &p) : Connector(name), p(p) {
    this->addPort(p);
}

ConnT_check_Singleton::~ConnT_check_Singleton() {
    for (vector<Interaction *>::const_iterator interactionIt = definedInteractions.begin() ;
         interactionIt != definedInteractions.end() ;
         ++interactionIt) {
        delete *interactionIt;
    }
    definedInteractions.clear();
}

PortValue &ConnT_check_Singleton::up(const InteractionValue &interactionValue) const {
    // THIS SHOULD *NEVER* HAPPEN.
    assert(false);
    // to avoid warnings
    return *(new PortValue());
}

void ConnT_check_Singleton::down(InteractionValue &interactionValue) const {
    
}

void ConnT_check_Singleton::down(InteractionValue &interactionValue, PortValue &portValue) const {
    // You should *never* reach this. Calling this method denotes an error in
    // scheduling (the down(interactionvalue) should have been used
    assert(false);
}

Interaction &ConnT_check_Singleton::createInteraction() const{
  return *new Inter_check_Singleton(*this);
}

Interaction &ConnT_check_Singleton::createInteraction(const vector<Port *> &ports) const{
    return *new Inter_check_Singleton(*this, ports);
}

InteractionValue &ConnT_check_Singleton::createInteractionValue(const Interaction &interaction, const vector<PortValue *> &values) const {
    return *new InterV_check_Singleton(*this, interaction, values);
}

void ConnT_check_Singleton::releaseInteraction(Interaction &interaction) const {
    delete &interaction;
}

void ConnT_check_Singleton::releaseInteractionValue(InteractionValue &interactionValue) const {
    delete &interactionValue;
}

bool ConnT_check_Singleton::guard(const InteractionValue &interactionValue) const {


    // default to true. May not be the wisest choice.
    return true;
}

const vector<Interaction*>& ConnT_check_Singleton::interactions() const {
  if (definedInteractions.empty()) {
    definedInteractions.push_back(new Inter_check_Singleton(*this, true));
  }

  // check that the number of defined interactions is correct
  assert(definedInteractions.size() == 1);

  return definedInteractions;
}
