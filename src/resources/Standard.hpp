#include<set>
#include <string>
#include<ostream>

long bool_sizeof(const bool& q);
void bool_unmarshalling(bool* t, const char* c);
void bool_marshalling(char* c, const bool* q);

int increase();



void printState(char* start, char* end, char* port,int id); 
void updateVar(int& to, int& from...);

void open_msg_file(int);


/*
class CorrelationSet {
    string name;
    int initialize;
  public:
 //   void set_values (string,int);
//    string getName (void);
//    int getInitialize (void);
}


void CS_setValues(CorrelationSet&,string,int);
int CS_getInitialize(CorrelationSet);
string CS_getName(CorrelationSet);
*/
