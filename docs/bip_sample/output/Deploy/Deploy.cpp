#include "Deploy.hpp"

/*
 * The "static" includes (the one we use inconditionnaly)
 */

Component* deploy(int argc __attribute__((unused)), char **argv __attribute__((unused))){
    // Top is Comp_ROOT





    staticallocated::port_ROOT_A1_AtoB.addInternalPort(staticallocated::iport_ROOT_A1_AtoB);

    staticallocated::port_ROOT_A1_CtoB.addInternalPort(staticallocated::iport_ROOT_A1_CtoB);

    // Runtime init for Atom: Comp_ROOT_A1
    // staticallocated::Comp_ROOT_A1






    staticallocated::port_ROOT_A2_AtoB.addInternalPort(staticallocated::iport_ROOT_A2_AtoB);

    staticallocated::port_ROOT_A2_CtoB.addInternalPort(staticallocated::iport_ROOT_A2_CtoB);

    // Runtime init for Atom: Comp_ROOT_A2
    // staticallocated::Comp_ROOT_A2








    // Runtime init for Compound: Comp_ROOT
    // staticallocated::Comp_ROOT

    // Finished: Comp_ROOT
    return &(staticallocated::Comp_ROOT);
}

bool isSerializeEnabled() {
    return true;
}

void serialize(char **cbuf __attribute__((unused)), size_t *clen __attribute__((unused))){
    size_t len = 0;
    char *buf;
    len += staticallocated::Comp_ROOT_A1.bytesSize();
    len += staticallocated::Comp_ROOT_A2.bytesSize();
    len += 2 * sizeof(size_t); // for size interleaving
    buf = new char[ len ];
    *cbuf = buf;
    *clen = len;

    // do the actual serialization
    staticallocated::Comp_ROOT_A1.toBytes(buf+sizeof(size_t), (size_t*)buf);
    buf += *((size_t*)buf) + sizeof(size_t);
    staticallocated::Comp_ROOT_A2.toBytes(buf+sizeof(size_t), (size_t*)buf);
    buf += *((size_t*)buf) + sizeof(size_t);
}

void deserialize(const char *buf __attribute__((unused)), size_t len __attribute__((unused))){
    staticallocated::Comp_ROOT_A1.fromBytes(buf+sizeof(size_t), *((size_t*)buf));
    buf += *((size_t*)buf) + sizeof(size_t);
    staticallocated::Comp_ROOT_A2.fromBytes(buf+sizeof(size_t), *((size_t*)buf));
    buf += *((size_t*)buf) + sizeof(size_t);
}
