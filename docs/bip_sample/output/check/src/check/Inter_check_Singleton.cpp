
#include <check/Inter_check_Singleton.hpp>
#include <check/ConnT_check_Singleton.hpp>

const bitset<1> Inter_check_Singleton::predefined(string("1"));

Inter_check_Singleton::Inter_check_Singleton(const ConnT_check_Singleton &connector) : Interaction(connector) {
    involvedPorts.reset();
    port_vector_fresh = true;
    defined = false;
    refresh_defined = false;
    mPorts.reserve(1);
}

Inter_check_Singleton::Inter_check_Singleton(const ConnT_check_Singleton &connector, bool p) : Interaction(connector) {
    involvedPorts.reset();

    defined = true;
    refresh_defined = false;
	port_vector_fresh = false;

    involvedPorts.set(0, p);

    defined = p;
    mPorts.reserve(1);
}

Inter_check_Singleton::Inter_check_Singleton(const ConnT_check_Singleton &connector, const vector<Port *> &ports) : Interaction(connector), port_vector_fresh(false) {
    commonRecycle(ports);
    mPorts.reserve(1);
}

Inter_check_Singleton::~Inter_check_Singleton(){
}

// This one is 'const' but its only role
// is to set mPorts correctly (ie. its only role is to modify the object)
void Inter_check_Singleton::refreshPorts() const {
    const vector<QuotedPortReference *> &parent_ports = this->connector().ports();
    mPorts.clear();
    mPorts.reserve(1);

    if (involvedPorts.test(0)) {
         mPorts.push_back(&(parent_ports[0]->port()));
    }
    port_vector_fresh = true;
}

vector<Port*>& Inter_check_Singleton::ports() {
    if (!port_vector_fresh){
        refreshPorts();
    }

    return mPorts;
}

const vector<Port *> & Inter_check_Singleton::ports() const {
    if (!port_vector_fresh){
        refreshPorts();
    }

    return mPorts;
}

bool Inter_check_Singleton::operator==(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_Singleton *>(&interaction) != NULL);
        const Inter_check_Singleton *other = static_cast<const Inter_check_Singleton *>(&interaction);
        ret = (involvedPorts == other->involvedPorts);
    }
    return ret;
}

bool Inter_check_Singleton::operator<=(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_Singleton *>(&interaction) != NULL);
        const Inter_check_Singleton *other = static_cast<const Inter_check_Singleton *>(&interaction);
        ret = ((involvedPorts & other->involvedPorts) == involvedPorts);
    }

    return ret;
}

bool Inter_check_Singleton::operator<(const Interaction &interaction) const {
    bool ret = false;

    // check if interaction values are from the same connector
    if (&connector() == &interaction.connector()) {
        assert(dynamic_cast<const Inter_check_Singleton *>(&interaction) != NULL);
        const Inter_check_Singleton *other = static_cast<const Inter_check_Singleton *>(&interaction);
        ret = (((involvedPorts &  other->involvedPorts) == involvedPorts) &&
                (involvedPorts != other->involvedPorts));
    }

    return ret;
}

void Inter_check_Singleton::recycle() {
    Interaction::recycle();
    mPorts.clear();
    port_vector_fresh = true;
    defined = false;
    refresh_defined = false;
    involvedPorts.reset();
}

void Inter_check_Singleton::recycle(const vector<Port *> &ports) {
    Interaction::recycle();
    commonRecycle(ports);
}

void Inter_check_Singleton::commonRecycle(const vector<Port *> &ports){
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
