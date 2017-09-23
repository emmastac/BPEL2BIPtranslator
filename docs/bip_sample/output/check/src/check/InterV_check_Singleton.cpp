
#include <check/InterV_check_Singleton.hpp>
#include <check/ConnT_check_Singleton.hpp>

InterV_check_Singleton::InterV_check_Singleton(const ConnT_check_Singleton &connector, const Interaction& interaction, const vector<PortValue *> &values) :
        InteractionValue(),
        mInteraction(connector, interaction.ports()) {
    this->mPortValues.reserve(1);
    commonRecycle(interaction, values);
}


void InterV_check_Singleton::commonRecycle(const Interaction& interaction, const vector<PortValue *> &values) {
	this->mPortValues.clear();
    for (vector<PortValue *>::const_iterator valueIt = values.begin() ;
         valueIt != values.end() ;
         ++valueIt) {
        this->mPortValues.push_back(*valueIt);
    }
}


InterV_check_Singleton::~InterV_check_Singleton(){
}


