#start
cmake_minimum_required(VERSION 2.8)

##set(ATOM_FILES)
##set(COMPOUND_FILES)
set(PORT_FILES)
set(TYPE_FILES)
##set(CONNECTOR_FILES)

set(EXTRA_SRC)
set(EXTRA_OBJ)

# user include dir
include_directories(/home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/src-ext)




# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:43:0
list(APPEND TYPE_FILES include/check/AT_check_Atom1.hpp src/check/AT_check_Atom1.cpp)


# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:57:0
list(APPEND TYPE_FILES include/check/AT_check_Atom2.hpp src/check/AT_check_Atom2.cpp)


# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:73:0
list(APPEND TYPE_FILES include/check/CT_check_comp.hpp src/check/CT_check_comp.cpp)


# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:26:0
list(APPEND TYPE_FILES include/check/ConnT_check_SingletonD.hpp src/check/ConnT_check_SingletonD.cpp)


list (APPEND PORT_FILES src/check/Inter_check_SingletonD.cpp 
                        include/check/Inter_check_SingletonD.hpp)

list (APPEND PORT_FILES src/check/InterV_check_SingletonD.cpp 
                        include/check/InterV_check_SingletonD.hpp)

# CPV
list (APPEND PORT_FILES src/check/CPV_SingletonD__e0b0port.cpp
                        include/check/CPV_SingletonD__e0b0port.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:31:0
list(APPEND TYPE_FILES include/check/ConnT_check_Singleton.hpp src/check/ConnT_check_Singleton.cpp)


list (APPEND PORT_FILES src/check/Inter_check_Singleton.cpp 
                        include/check/Inter_check_Singleton.hpp)

list (APPEND PORT_FILES src/check/InterV_check_Singleton.cpp 
                        include/check/InterV_check_Singleton.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:35:0
list(APPEND TYPE_FILES include/check/ConnT_check_BRDCAST2.hpp src/check/ConnT_check_BRDCAST2.cpp)


list (APPEND PORT_FILES src/check/Inter_check_BRDCAST2.cpp 
                        include/check/Inter_check_BRDCAST2.hpp)

list (APPEND PORT_FILES src/check/InterV_check_BRDCAST2.cpp 
                        include/check/InterV_check_BRDCAST2.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:39:0
list(APPEND TYPE_FILES include/check/ConnT_check_RDV2.hpp src/check/ConnT_check_RDV2.cpp)


list (APPEND PORT_FILES src/check/Inter_check_RDV2.cpp 
                        include/check/Inter_check_RDV2.hpp)

list (APPEND PORT_FILES src/check/InterV_check_RDV2.cpp 
                        include/check/InterV_check_RDV2.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:22:0
list(APPEND TYPE_FILES include/check/PT_check_e0b0port.hpp src/check/PT_check_e0b0port.cpp)


list (APPEND PORT_FILES src/check/ConnPort_check_e0b0port.cpp
                        include/check/ConnPort_check_e0b0port.hpp)

list (APPEND PORT_FILES src/check/CpndEPort_check_e0b0port.cpp 
                        include/check/CpndEPort_check_e0b0port.hpp)

list (APPEND PORT_FILES src/check/AtomEPort_check_e0b0port.cpp 
                        include/check/AtomEPort_check_e0b0port.hpp)

list (APPEND PORT_FILES src/check/AtomIPort_check_e0b0port.cpp
                        include/check/AtomIPort_check_e0b0port.hpp)

list (APPEND PORT_FILES src/check/PV_check_e0b0port.cpp
                        include/check/PV_check_e0b0port.hpp)

list (APPEND PORT_FILES src/check/QPR_check_e0b0port.cpp
                        include/check/QPR_check_e0b0port.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:23:0
list(APPEND TYPE_FILES include/check/PT_check_e1b0port.hpp src/check/PT_check_e1b0port.cpp)


list (APPEND PORT_FILES src/check/ConnPort_check_e1b0port.cpp
                        include/check/ConnPort_check_e1b0port.hpp)

list (APPEND PORT_FILES src/check/CpndEPort_check_e1b0port.cpp 
                        include/check/CpndEPort_check_e1b0port.hpp)

list (APPEND PORT_FILES src/check/AtomEPort_check_e1b0port.cpp 
                        include/check/AtomEPort_check_e1b0port.hpp)

list (APPEND PORT_FILES src/check/AtomIPort_check_e1b0port.cpp
                        include/check/AtomIPort_check_e1b0port.hpp)

list (APPEND PORT_FILES src/check/PV_check_e1b0port.cpp
                        include/check/PV_check_e1b0port.hpp)

list (APPEND PORT_FILES src/check/QPR_check_e1b0port.cpp
                        include/check/QPR_check_e1b0port.hpp)

# /home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/check.bip:24:0
list(APPEND TYPE_FILES include/check/PT_check_e2b0port.hpp src/check/PT_check_e2b0port.cpp)


list (APPEND PORT_FILES src/check/ConnPort_check_e2b0port.cpp
                        include/check/ConnPort_check_e2b0port.hpp)

list (APPEND PORT_FILES src/check/CpndEPort_check_e2b0port.cpp 
                        include/check/CpndEPort_check_e2b0port.hpp)

list (APPEND PORT_FILES src/check/AtomEPort_check_e2b0port.cpp 
                        include/check/AtomEPort_check_e2b0port.hpp)

list (APPEND PORT_FILES src/check/AtomIPort_check_e2b0port.cpp
                        include/check/AtomIPort_check_e2b0port.hpp)

list (APPEND PORT_FILES src/check/PV_check_e2b0port.cpp
                        include/check/PV_check_e2b0port.hpp)

list (APPEND PORT_FILES src/check/QPR_check_e2b0port.cpp
                        include/check/QPR_check_e2b0port.hpp)

include_directories("include")
add_library(pack_check 
                      ${TYPE_FILES}
                      ${PORT_FILES}
                      ${EXTRA_SRC}
                      ${EXTRA_OBJ})
