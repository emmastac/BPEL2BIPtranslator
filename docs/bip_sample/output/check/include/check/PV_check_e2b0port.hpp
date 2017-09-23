#ifndef INCLUDE_CHECK_PV__CHECK__E2B0PORT_HPP_
#define INCLUDE_CHECK_PV__CHECK__E2B0PORT_HPP_

#include <PortValue.hpp>

#include <check/PT_check_e2b0port.hpp>

class PV_check_e2b0port : public PortValue {
public:
    PV_check_e2b0port(int _m_msg1, int _m_msg2);
    PV_check_e2b0port();
    virtual ~PV_check_e2b0port();

    virtual string toString() const;


    // get/set for data.
    const int& get_msg1() const;
    int& get_msg1();
    void set_msg1(const int &_m_msg1);

    const int& get_msg2() const;
    int& get_msg2();
    void set_msg2(const int &_m_msg2);

private:
    // data fields from Port type.
    int m_msg1;
    int m_msg2;
}; 



    // get/set for data.
inline
const int& PV_check_e2b0port::get_msg1() const {
    return m_msg1;
}
inline
int& PV_check_e2b0port::get_msg1() {
    return m_msg1;
}
inline
void PV_check_e2b0port::set_msg1(const int &_m_msg1) {
    m_msg1 = _m_msg1;
}

inline
const int& PV_check_e2b0port::get_msg2() const {
    return m_msg2;
}
inline
int& PV_check_e2b0port::get_msg2() {
    return m_msg2;
}
inline
void PV_check_e2b0port::set_msg2(const int &_m_msg2) {
    m_msg2 = _m_msg2;
}


#endif // INCLUDE_CHECK_PV__CHECK__E2B0PORT_HPP_
