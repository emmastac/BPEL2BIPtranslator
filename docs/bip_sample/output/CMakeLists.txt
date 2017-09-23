cmake_minimum_required(VERSION 2.8)
# 2.8.3 has "CMAKE_CURRENT_LIST_DIR" variable. We have 2.8.2 deployed, 
# so using "CMAKE_SOURCE_DIR" instead.
set(GEN_ROOT ${CMAKE_SOURCE_DIR})

# Select flags.
SET(CMAKE_CXX_FLAGS "-Wall -std=c++0x")
SET(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-O3 -g -std=c++0x")
SET(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG -std=c++0x")
SET(CMAKE_CXX_FLAGS_DEBUG  "-O0 -g -std=c++0x")

SET(CMAKE_CXX_FLAGS_PROFILING "-O0 -g -pg")
SET(CMAKE_EXE_LINKER_FLAGS_PROFILING "-pg")

# if none provided elsewhere, defaults to
# values from environment variables
IF(NOT BIP2_ENGINE_GENERIC_DIR)
  SET(BIP2_ENGINE_GENERIC_DIR $ENV{BIP2_ENGINE_GENERIC_DIR})
ENDIF()

IF(NOT BIP2_ENGINE_SPECIFIC_DIR)
  SET(BIP2_ENGINE_SPECIFIC_DIR $ENV{BIP2_ENGINE_SPECIFIC_DIR})
ENDIF()

IF(NOT BIP2_ENGINE_LIB_DIR)
  SET(BIP2_ENGINE_LIB_DIR $ENV{BIP2_ENGINE_LIB_DIR})
ENDIF()

include_directories(${BIP2_ENGINE_GENERIC_DIR})
include_directories(${BIP2_ENGINE_SPECIFIC_DIR})

# user include dir
include_directories(/home/dsg003/Dropbox/eclipse_workspace/BIP2LATEX_revised2/docs/bip_sample/src-ext)

# For package check
include_directories(${GEN_ROOT}/check/include)

add_subdirectory(${GEN_ROOT}/check)

set(CUSTOM_LIB_DIRS )

# Following only for building from instances.
find_library(libengine_path engine ${BIP2_ENGINE_LIB_DIR} ${CUSTOM_LIB_DIRS})


set(USER_EXTRA_SRC)

set(USR_EXTRA_OBJ)

add_executable(system
  ${GEN_ROOT}/Deploy/Deploy.cpp
  ${USER_EXTRA_SRC}
  ${USER_EXTRA_OBJ})

target_link_libraries(system 
  pack_check ${libengine_path}
)
