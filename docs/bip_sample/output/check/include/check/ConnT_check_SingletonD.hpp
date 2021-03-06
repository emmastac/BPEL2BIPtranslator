#ifndef CHECK_CONNT__CHECK__SINGLETOND_HPP_
#define CHECK_CONNT__CHECK__SINGLETOND_HPP_

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:26:0
// include package "master" header
#include <check.hpp>

#include <Connector.hpp>
#include <Interaction.hpp>
#include <PortValue.hpp>

// User include given in @cpp annotation
#include <stdio.h>

#include <check/QPR_check_e0b0port.hpp>
#include <check/ConnPort_check_e0b0port.hpp>
#include <check/InterV_check_SingletonD.hpp>

class ConnT_check_SingletonD : public Connector {
public:
    ConnT_check_SingletonD(const string &name, QPR_check_e0b0port &p, ConnPort_check_e0b0port &xpr);
    virtual ~ConnT_check_SingletonD();

    virtual PortValue &up(const InteractionValue &interactionValue) const;
    virtual void down(InteractionValue &interactionValue) const;
    virtual void down(InteractionValue &interactionValue, PortValue &portValue) const;

    virtual Interaction &createInteraction() const;
    virtual Interaction &createInteraction(const vector<Port *> &ports) const;
    virtual void releaseInteraction(Interaction &interaction) const;

    virtual InteractionValue &createInteractionValue(const Interaction &interactionValue, const vector<PortValue *> &values) const;
    virtual void releaseInteractionValue(InteractionValue &interactionValue) const;

    virtual bool guard(const InteractionValue &interactionValue) const;
    virtual const vector<Interaction *> &interactions() const;
    virtual bool canUpOnlyMaximalInteractions() const {
        return false;
    }

private:
    QPR_check_e0b0port &p;

    ConnPort_check_e0b0port &xpr;



    mutable vector<Interaction *> definedInteractions;
};
#endif // CHECK_CONNT__CHECK__SINGLETOND_HPP_
