#ifndef INCLUDE_CHECK_PV__CHECK__E1B0PORT_HPP_
#define INCLUDE_CHECK_PV__CHECK__E1B0PORT_HPP_

#include <PortValue.hpp>

#include <check/PT_check_e1b0port.hpp>

class PV_check_e1b0port : public PortValue {
public:
    PV_check_e1b0port(int _m_msg1);
    PV_check_e1b0port();
    virtual ~PV_check_e1b0port();

    virtual string toString() const;


    // get/set for data.
    const int& get_msg1() const;
    int& get_msg1();
    void set_msg1(const int &_m_msg1);

private:
    // data fields from Port type.
    int m_msg1;
}; 



    // get/set for data.
inline
const int& PV_check_e1b0port::get_msg1() const {
    return m_msg1;
}
inline
int& PV_check_e1b0port::get_msg1() {
    return m_msg1;
}
inline
void PV_check_e1b0port::set_msg1(const int &_m_msg1) {
    m_msg1 = _m_msg1;
}


#endif // INCLUDE_CHECK_PV__CHECK__E1B0PORT_HPP_
