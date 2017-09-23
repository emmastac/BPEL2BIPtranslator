#ifndef CHECK_AT__CHECK__ATOM1_HPP_
#define CHECK_AT__CHECK__ATOM1_HPP_

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:43:0
// include package "master" header
#include <check.hpp>

#include <Atom.hpp>

// User include given in @cpp annotation
#include <stdio.h>

#include <check/AtomIPort_check_e0b0port.hpp>

#include <check/AtomEPort_check_e0b0port.hpp>

#include <check/PV_check_e0b0port.hpp>


// for memcpy
#include <string.h>
// for marshalling, need BipError
#include <CycleInAtomPrioritiesError.hpp>
#include <NonDeterministicPetriNetError.hpp>
#include <NonOneSafePetriNetError.hpp>

class AT_check_Atom1 : public Atom {
private:
   // internal ports & associated port values
    AtomIPort_check_e0b0port &_iport_decl__AtoB;
    PV_check_e0b0port _iport_decl_pv__AtoB;
    AtomIPort_check_e0b0port &_iport_decl__CtoB;
    PV_check_e0b0port _iport_decl_pv__CtoB;
    AtomIPort_check_e0b0port &_iport_decl__BtoA;
    PV_check_e0b0port _iport_decl_pv__BtoA;
    AtomIPort_check_e0b0port &_iport_decl__AtoC;
    PV_check_e0b0port _iport_decl_pv__AtoC;

    // exported ports
    AtomEPort_check_e0b0port &_eport_decl__AtoB;
    AtomEPort_check_e0b0port &_eport_decl__CtoB;
public:
    AT_check_Atom1(const string &name , AtomIPort_check_e0b0port &_iport_decl__AtoB, AtomIPort_check_e0b0port &_iport_decl__CtoB, AtomIPort_check_e0b0port &_iport_decl__BtoA, AtomIPort_check_e0b0port &_iport_decl__AtoC
                             , AtomEPort_check_e0b0port &_eport_decl__AtoB, AtomEPort_check_e0b0port &_eport_decl__CtoB
                             
                             );
    virtual ~AT_check_Atom1();

    virtual BipError& execute(PortValue &portValue);

    virtual BipError& initialize();
    virtual string toString() const;
    BipError& updatePortValues();

    size_t bytesSize() const;
    char* toBytes(char*buf, size_t *len) const;
    bool fromBytes(const char *buf, size_t len);
    bool disposeBytes(const char *buf) const;
protected:
    const static size_t bvector_size = 3/(8*sizeof(int))+((3%(8*sizeof(int))) > 0 ? 1 : 0);
    int __statesbv[ bvector_size ];

    int _id__c;
    int _id__d;


    bool transid_1;
    bool transid_2;
    bool transid_3;
    bool transid_4;

    bool atA () const;
    bool toA ();
    bool fromA ();
    bool atB () const;
    bool toB ();
    bool fromB ();
    bool atC () const;
    bool toC ();
    bool fromC ();
};


// for inlining

inline size_t AT_check_Atom1::bytesSize() const {
    size_t size = (0 + sizeof(int)+sizeof(int)) + // basic data
                  ((bvector_size*sizeof(int))) + // internals
                  (0); // non basic
    return size;
}

inline char * AT_check_Atom1::toBytes(char *buf, size_t *len) const{
    char *c; 
    *len = bytesSize();
    if (buf == NULL) {
        c = new char[ *len ];
    } else {
        c = buf;
    }

    size_t idx = 0;
    // store pnet state
    memcpy(c+idx, __statesbv, bvector_size*sizeof(int));
    idx += bvector_size*sizeof(int);

    // copy basic data
    memcpy(c+idx, &_id__c, sizeof(int));
    idx += sizeof(int);
    memcpy(c+idx, &_id__d, sizeof(int));
    idx += sizeof(int);

    assert(*len == idx);
    return c;
}

inline bool AT_check_Atom1::fromBytes(const char *buf, size_t len) {
    const char *c = buf;
    size_t idx = 0;
    // restore pnet state
    memcpy(__statesbv, c, bvector_size*sizeof(int));
    c+= bvector_size*sizeof(int);
    idx += bvector_size*sizeof(int);
    // basic data
    memcpy(&_id__c, c, sizeof(int));
    idx += sizeof(int);
    c += sizeof(int);
    memcpy(&_id__d, c, sizeof(int));
    idx += sizeof(int);
    c += sizeof(int);

    assert(len == idx);

    _iport_decl__AtoB.clearPortValue();
    _iport_decl__CtoB.clearPortValue();
    _iport_decl__BtoA.clearPortValue();
    _iport_decl__AtoC.clearPortValue();
    BipError *ret = &BipError::NoError;

    
    if (ret->type() != NO_ERROR) return false;

    return (updatePortValues().type() != NO_ERROR);
}

inline bool AT_check_Atom1::disposeBytes(const char *buf) const {
    delete[] buf;
    return true;
}

#endif // CHECK_AT__CHECK__ATOM1_HPP_
