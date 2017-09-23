
#include <check/InterV_check_SingletonD.hpp>
#include <check/ConnT_check_SingletonD.hpp>

InterV_check_SingletonD::InterV_check_SingletonD(const ConnT_check_SingletonD &connector, const Interaction& interaction, const vector<PortValue *> &values) :
        InteractionValue(),
        mInteraction(connector, interaction.ports()) {
    this->mPortValues.reserve(1);
    commonRecycle(interaction, values);
}


void InterV_check_SingletonD::commonRecycle(const Interaction& interaction, const vector<PortValue *> &values) {
	this->mPortValues.clear();
    for (vector<PortValue *>::const_iterator valueIt = values.begin() ;
         valueIt != values.end() ;
         ++valueIt) {
        this->mPortValues.push_back(*valueIt);
    }
}


InterV_check_SingletonD::~InterV_check_SingletonD(){
}


