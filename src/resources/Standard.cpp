#include <stdio.h>
#include<iostream>
#include<set>
#include<string>
#include<ostream>


long bool_sizeof(const bool& q){
	return 1;
}
void bool_unmarshalling(bool* t, const char* c){
	if(c[0]==1)
		*t=true;
	else
		*t=false;
}
void bool_marshalling(char* c, const bool* q){
	if(q)
		c[0] = '1';
	else
		c[0] = '0';
}

int c = 0;

int increase(){
   return c++;
}



#define PRINT_STATE 1
int OUTPUT_MODE = PRINT_STATE;

#define VERSION_NUM 1
int VAR_UPDATE_MODE = VERSION_NUM;

void printState(char* start, char* end, char* port, int id){
	if(OUTPUT_MODE==PRINT_STATE){
		printf("%s %s %s\n", start, end, port);
	}else{


	}
}

void updateVar(int& to, int& from...){
	if(VAR_UPDATE_MODE==VERSION_NUM){
		to++; 
	}else{

	}
}

void open_msg_file(int id){

string file;
if(id==11){
	file = "airline.FlightAvailability.req.xml";
} else{
	file = "airline.FlightTicketCallback.req.xml";
}
string line;
  ifstream myfile (file);
  if (myfile.is_open())
  {
    while ( getline (myfile,line) )
    {
      cout << line << '\n';
    }
    myfile.close();
  }
}



//void Rectangle::set_values (string x,int y) {
//  name = x;
//  initialize = y;
//}
//
//string getName (void){
//
//	return name;
//}
//int getInitialize (void){
//			return initialize;
//}

/*
void CS_setValues(CorrelationSet& cs,string s,int i){
	cs.name=s; cs.initialize=i;
}
int CS_getInitialize(CorrelationSet cs){
	return cs.initialize;
}
string CS_getName(CorrelationSet cs){
	return cs.name;
}
*/
