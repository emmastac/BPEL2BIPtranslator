// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:43:0

#include <check/AT_check_Atom1.hpp>

#include <iostream>
#include <sstream>

#include <CycleInAtomPrioritiesError.hpp>
#include <NonDeterministicPetriNetError.hpp>
#include <NonOneSafePetriNetError.hpp>


bool AT_check_Atom1::atA () const {
    return __statesbv[ 0/(8*sizeof(int))] & 1<< (0%(8*sizeof(int)));
}

bool AT_check_Atom1::toA () {
    if (atA()) return false;
    __statesbv[ 0/(8*sizeof(int))] |= 1<< (0%(8*sizeof(int)));
    return true;
}

bool AT_check_Atom1::fromA () {
    if (!atA()) return false;
    __statesbv[ 0/(8*sizeof(int))] &= ~(1<< (0%(8*sizeof(int))));
    return true;
}

bool AT_check_Atom1::atB () const {
    return __statesbv[ 1/(8*sizeof(int))] & 1<< (1%(8*sizeof(int)));
}

bool AT_check_Atom1::toB () {
    if (atB()) return false;
    __statesbv[ 1/(8*sizeof(int))] |= 1<< (1%(8*sizeof(int)));
    return true;
}

bool AT_check_Atom1::fromB () {
    if (!atB()) return false;
    __statesbv[ 1/(8*sizeof(int))] &= ~(1<< (1%(8*sizeof(int))));
    return true;
}

bool AT_check_Atom1::atC () const {
    return __statesbv[ 2/(8*sizeof(int))] & 1<< (2%(8*sizeof(int)));
}

bool AT_check_Atom1::toC () {
    if (atC()) return false;
    __statesbv[ 2/(8*sizeof(int))] |= 1<< (2%(8*sizeof(int)));
    return true;
}

bool AT_check_Atom1::fromC () {
    if (!atC()) return false;
    __statesbv[ 2/(8*sizeof(int))] &= ~(1<< (2%(8*sizeof(int))));
    return true;
}

AT_check_Atom1::AT_check_Atom1(const string &name , AtomIPort_check_e0b0port &_iport_decl__AtoB, AtomIPort_check_e0b0port &_iport_decl__CtoB, AtomIPort_check_e0b0port &_iport_decl__BtoA, AtomIPort_check_e0b0port &_iport_decl__AtoC
                         , AtomEPort_check_e0b0port &_eport_decl__AtoB, AtomEPort_check_e0b0port &_eport_decl__CtoB
                         
                         ) :  ComponentItf(name, ATOM), Atom(name) , _iport_decl__AtoB(_iport_decl__AtoB), _iport_decl__CtoB(_iport_decl__CtoB), _iport_decl__BtoA(_iport_decl__BtoA), _iport_decl__AtoC(_iport_decl__AtoC), _eport_decl__AtoB(_eport_decl__AtoB), _eport_decl__CtoB(_eport_decl__CtoB) {
    this->addInternalPort(_iport_decl__AtoB);

    // link data internal to internal port
    this->addInternalPort(_iport_decl__CtoB);

    // link data internal to internal port
    this->addInternalPort(_iport_decl__BtoA);

    // link data internal to internal port
    this->addInternalPort(_iport_decl__AtoC);
    // export port
    this->addPort(_eport_decl__AtoB);
    this->addPort(_eport_decl__CtoB);
}

BipError& AT_check_Atom1::execute(PortValue &portValue) {
    BipError *ret = &BipError::NoError;
    bool something_happened = false;

    // sync data from portvalue to internal data
        if (&_iport_decl_pv__AtoB == &portValue) {
		assert(_iport_decl__AtoB.hasPortValue());
        _iport_decl__AtoB.syncFromPortValue();
    } else     if (&_iport_decl_pv__CtoB == &portValue) {
		assert(_iport_decl__CtoB.hasPortValue());
        _iport_decl__CtoB.syncFromPortValue();
    } else     if (&_iport_decl_pv__BtoA == &portValue) {
		assert(_iport_decl__BtoA.hasPortValue());
        _iport_decl__BtoA.syncFromPortValue();
    } else     if (&_iport_decl_pv__AtoC == &portValue) {
		assert(_iport_decl__AtoC.hasPortValue());
        _iport_decl__AtoC.syncFromPortValue();
    }
    // transition triggered by port
        if (_iport_decl__AtoB.hasPortValue() && (&_iport_decl_pv__AtoB == &portValue) &&
        atA() &&
        transid_1) {
        if (something_happened) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__AtoB);
            return *r;
        }


        // reset exported data

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:51:27
        _id__c = (_id__c)  +  (1);
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:51:34
        printf("name:Atom1,place:B,c:%d,d:%d", _id__c, _id__d);

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:51:1
        if (! fromA()) {
            NonOneSafePetriNetError *r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__AtoB);
            return *r;
        }

        if (ret != &BipError::NoError) return *ret;

        if (! toB() ){
            NonOneSafePetriNetError * r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__AtoB);
            return *r;
        }
        if (ret != &BipError::NoError) return *ret;
        something_happened = true;
    } else     if (_iport_decl__AtoC.hasPortValue() && (&_iport_decl_pv__AtoC == &portValue) &&
        atA() &&
        transid_2) {
        if (something_happened) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__AtoC);
            return *r;
        }


        // reset exported data

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:52:27
        _id__d = (_id__d)  +  (1);
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:52:34
        printf("name:Atom1,place:C,c:%d,d:%d", _id__c, _id__d);

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:52:1
        if (! fromA()) {
            NonOneSafePetriNetError *r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__AtoC);
            return *r;
        }

        if (ret != &BipError::NoError) return *ret;

        if (! toC() ){
            NonOneSafePetriNetError * r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__AtoC);
            return *r;
        }
        if (ret != &BipError::NoError) return *ret;
        something_happened = true;
    } else     if (_iport_decl__CtoB.hasPortValue() && (&_iport_decl_pv__CtoB == &portValue) &&
        atC() &&
        transid_3) {
        if (something_happened) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__CtoB);
            return *r;
        }


        // reset exported data

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:53:27
        _id__d = (_id__d)  +  (1);
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:53:34
        printf("name:Atom1,place:B,c:%d,d:%d", _id__c, _id__d);

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:53:1
        if (! fromC()) {
            NonOneSafePetriNetError *r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__CtoB);
            return *r;
        }

        if (ret != &BipError::NoError) return *ret;

        if (! toB() ){
            NonOneSafePetriNetError * r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__CtoB);
            return *r;
        }
        if (ret != &BipError::NoError) return *ret;
        something_happened = true;
    } else     if (_iport_decl__BtoA.hasPortValue() && (&_iport_decl_pv__BtoA == &portValue) &&
        atB() &&
        transid_4) {
        if (something_happened) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__BtoA);
            return *r;
        }


        // reset exported data

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:54:26
        _id__c = 0;
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:54:31
        _id__d = 0;
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:54:35
        printf("name:Atom1,place:A,c:%d,d:%d", _id__c, _id__d);

// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:54:1
        if (! fromB()) {
            NonOneSafePetriNetError *r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__BtoA);
            return *r;
        }

        if (ret != &BipError::NoError) return *ret;

        if (! toA() ){
            NonOneSafePetriNetError * r = new NonOneSafePetriNetError(*this);
            r->setPort(_iport_decl__BtoA);
            return *r;
        }
        if (ret != &BipError::NoError) return *ret;
        something_happened = true;
    }
    assert(something_happened == true);
    
    if (ret->type() != NO_ERROR) return *ret;

    return updatePortValues();
}

BipError& AT_check_Atom1::initialize() {
    BipError *ret = &BipError::NoError;

    //_iport_decl__AtoB.setPortValue(_iport_decl_pv__AtoB);
    _iport_decl__AtoB.clearPortValue();
    //_iport_decl__CtoB.setPortValue(_iport_decl_pv__CtoB);
    _iport_decl__CtoB.clearPortValue();
    //_iport_decl__BtoA.setPortValue(_iport_decl_pv__BtoA);
    _iport_decl__BtoA.clearPortValue();
    //_iport_decl__AtoC.setPortValue(_iport_decl_pv__AtoC);
    _iport_decl__AtoC.clearPortValue();

    // initial action
// /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:50:19
    printf("name:Atom1,place:A,c:%d,d:%d", _id__c, _id__d);

    for (unsigned int idx = 0; idx < bvector_size; idx++){
        __statesbv[idx] = 0;
    }

    toA();

    
    if (ret->type() != NO_ERROR) return *ret;

    return updatePortValues();
}
string AT_check_Atom1::toString() const {
    ostringstream oss;
    bool first=true;
    if (atA()) {
      if (!first) oss << ", ";
      else {
        first = false;
        oss << "at ";
      }
      oss << "A";
    }
    if (atB()) {
      if (!first) oss << ", ";
      else {
        first = false;
        oss << "at ";
      }
      oss << "B";
    }
    if (atC()) {
      if (!first) oss << ", ";
      else {
        first = false;
        oss << "at ";
      }
      oss << "C";
    }

    if (first) first = false;
    else oss << std::endl;
    oss << "c=" << _id__c;
    if (first) first = false;
    else oss << std::endl;
    oss << "d=" << _id__d;

    return oss.str(); 
}

BipError& AT_check_Atom1::updatePortValues() {
    BipError *ret = &BipError::NoError;

    // reset enabled status of internal ports
    this->_iport_decl__AtoB.setIsEnabled(false);
    this->_iport_decl__CtoB.setIsEnabled(false);
    this->_iport_decl__BtoA.setIsEnabled(false);
    this->_iport_decl__AtoC.setIsEnabled(false);
    
    // update enabled status of internal ports
    if (atA()

        ) {
        // check we don't have 2 transitions on the same port possible at the same time.
        if (_iport_decl__AtoB.isEnabled()) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__AtoB);
            // it is the caller's responsibility to handle memory.
            // there need to be a 'delete' somewhere. 
            return *r;
        }
        transid_1 = true;
        _iport_decl__AtoB.setIsEnabled(true);
    } else {
        transid_1 = false;
    }
    if (atA()

        ) {
        // check we don't have 2 transitions on the same port possible at the same time.
        if (_iport_decl__AtoC.isEnabled()) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__AtoC);
            // it is the caller's responsibility to handle memory.
            // there need to be a 'delete' somewhere. 
            return *r;
        }
        transid_2 = true;
        _iport_decl__AtoC.setIsEnabled(true);
    } else {
        transid_2 = false;
    }
    if (atC()

        ) {
        // check we don't have 2 transitions on the same port possible at the same time.
        if (_iport_decl__CtoB.isEnabled()) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__CtoB);
            // it is the caller's responsibility to handle memory.
            // there need to be a 'delete' somewhere. 
            return *r;
        }
        transid_3 = true;
        _iport_decl__CtoB.setIsEnabled(true);
    } else {
        transid_3 = false;
    }
    if (atB()

        ) {
        // check we don't have 2 transitions on the same port possible at the same time.
        if (_iport_decl__BtoA.isEnabled()) {
            NonDeterministicPetriNetError *r = new NonDeterministicPetriNetError(*this);
            r->setPort(_iport_decl__BtoA);
            // it is the caller's responsibility to handle memory.
            // there need to be a 'delete' somewhere. 
            return *r;
        }
        transid_4 = true;
        _iport_decl__BtoA.setIsEnabled(true);
    } else {
        transid_4 = false;
    }

    // update port values of internal ports
    if (this->_iport_decl__AtoB.isEnabled()) {
        _iport_decl__AtoB.setPortValue(_iport_decl_pv__AtoB);
        _iport_decl__AtoB.syncToPortValue();
    }
    else {
        this->_iport_decl__AtoB.clearPortValue();
    }
    if (this->_iport_decl__CtoB.isEnabled()) {
        _iport_decl__CtoB.setPortValue(_iport_decl_pv__CtoB);
        _iport_decl__CtoB.syncToPortValue();
    }
    else {
        this->_iport_decl__CtoB.clearPortValue();
    }
    if (this->_iport_decl__BtoA.isEnabled()) {
        _iport_decl__BtoA.setPortValue(_iport_decl_pv__BtoA);
        _iport_decl__BtoA.syncToPortValue();
    }
    else {
        this->_iport_decl__BtoA.clearPortValue();
    }
    if (this->_iport_decl__AtoC.isEnabled()) {
        _iport_decl__AtoC.setPortValue(_iport_decl_pv__AtoC);
        _iport_decl__AtoC.syncToPortValue();
    }
    else {
        this->_iport_decl__AtoC.clearPortValue();
    }
    

    

    // update port values and reset status of exported ports
    _eport_decl__AtoB.setIsReset(true);
    this->_eport_decl__AtoB.portValues().clear();
    if (this->_iport_decl__AtoB.hasPortValue()) {
        this->_eport_decl__AtoB.addPortValue(_iport_decl_pv__AtoB);
    }
    _eport_decl__CtoB.setIsReset(true);
    this->_eport_decl__CtoB.portValues().clear();
    if (this->_iport_decl__CtoB.hasPortValue()) {
        this->_eport_decl__CtoB.addPortValue(_iport_decl_pv__CtoB);
    }

    return *ret;
}

AT_check_Atom1::~AT_check_Atom1() {
}
