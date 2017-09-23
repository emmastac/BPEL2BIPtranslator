
#include <check/Inter_check_RDV2.hpp>
#include <check/ConnT_check_RDV2.hpp>

const bitset<2> Inter_check_RDV2::predefined(string("11"));

Inter_check_RDV2::Inter_check_RDV2(const ConnT_check_RDV2 &connector) : Interaction(connector) {
    involvedPorts.reset();
    port_vector_fresh = true;
    defined = false;
    refresh_defined = false;
    mPorts.reserve(2);
}

Inter_check_RDV2::Inter_check_RDV2(const ConnT_check_RDV2 &connector, bool p1, bool p2) : Interaction(connector) {
    involvedPorts.reset();

    defined = true;
    refresh_defined = false;
	port_vector_fresh = false;

    involvedPorts.set(0, p1);
    involvedPorts.set(1, p2);

    defined = p1 && p2;
    mPorts.reserve(2);
}

Inter_check_RDV2::Inter_check_RDV2(const ConnT_check_RDV2 &connector, const vector<Port *> &ports) : Interaction(connector), port_vector_fresh(false) {
    commonRecycle(ports);
    mPorts.reserve(2);
}

Inter_check_RDV2::~Inter_check_RDV2(){
}

// This one is 'const' but its only role
// is to set mPorts correctly (ie. its only role is to modify the object)
void Inter_check_RDV2::refreshPorts() const {
    const vector<QuotedPortReference *> &parent_ports = this->connector().ports();
    mPorts.clear();
    mPorts.reserve(2);

    if (involvedPorts.test(0)) {
         mPorts.push_back(&(parent_ports[0]->port()));
    }
    if (involvedPorts.test(1)) {
         mPorts.push_back(&(parent_ports[1]->port()));
    }
    port_vector_fresh = true;
}

vector<Port*>& Inter_check_RDV2::ports() {
    if (!port_vector_fresh){
        refreshPorts();
    }

    return mPorts;
}

const vector<Port *> & Inter_check_RDV2::ports() const {
    if (!port_vector_fresh){
        refreshPorts();
    }

    return mPorts;
}

bool Inter_check_RDV2::operator==(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_RDV2 *>(&interaction) != NULL);
        const Inter_check_RDV2 *other = static_cast<const Inter_check_RDV2 *>(&interaction);
        ret = (involvedPorts == other->involvedPorts);
    }
    return ret;
}

bool Inter_check_RDV2::operator<=(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_RDV2 *>(&interaction) != NULL);
        const Inter_check_RDV2 *other = static_cast<const Inter_check_RDV2 *>(&interaction);
        ret = ((involvedPorts & other->involvedPorts) == involvedPorts);
    }

    return ret;
}

bool Inter_check_RDV2::operator<(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_RDV2 *>(&interaction) != NULL);
        const Inter_check_RDV2 *other = static_cast<const Inter_check_RDV2 *>(&interaction);
        ret = (((involvedPorts &  other->involvedPorts) == involvedPorts) &&
                (involvedPorts != other->involvedPorts));
    }

    return ret;
}

void Inter_check_RDV2::recycle() {
    Interaction::recycle();
    mPorts.clear();
    port_vector_fresh = true;
    defined = false;
    refresh_defined = false;
    involvedPorts.reset();
}

void Inter_check_RDV2::recycle(const vector<Port *> &ports) {
    Interaction::recycle();
    commonRecycle(ports);
}

void Inter_check_RDV2::commonRecycle(const vector<Port *> &ports){
    involvedPorts.reset();

    for (vector<Port *>::const_iterator portIt = ports.begin() ;
         portIt != ports.end() ;
         ++portIt) {
        const vector<Port *>::size_type post_shift = findPort(*portIt);

        involvedPorts.set(post_shift);
    }

    port_vector_fresh = false;
    refresh_defined = true;
}
