
#include <check/InterV_check_RDV2.hpp>
#include <check/ConnT_check_RDV2.hpp>

InterV_check_RDV2::InterV_check_RDV2(const ConnT_check_RDV2 &connector, const Interaction& interaction, const vector<PortValue *> &values) :
        InteractionValue(),
        mInteraction(connector, interaction.ports()) {
    this->mPortValues.reserve(2);
    commonRecycle(interaction, values);
}


void InterV_check_RDV2::commonRecycle(const Interaction& interaction, const vector<PortValue *> &values) {
	this->mPortValues.clear();
    for (vector<PortValue *>::const_iterator valueIt = values.begin() ;
         valueIt != values.end() ;
         ++valueIt) {
        this->mPortValues.push_back(*valueIt);
    }
}


InterV_check_RDV2::~InterV_check_RDV2(){
}


