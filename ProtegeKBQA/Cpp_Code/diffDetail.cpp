#include <fstream>
#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include <map>
#include <set>
#include <ctime>
#include <cstdlib>

using namespace std;

void populateFilterMap ( map<string, set<string> > & , vector<string> &  );
void populateDetailsMap ( map<string, set<string> > & , vector<string> & );

int main (int argc, char * argv[])
{

	vector<string> commLine;
	for ( int i = 0; i < argc; i++)
		commLine.push_back(*argv++);
	if( commLine.size() != 3 ) {
		cerr << "Error:  incorrect number of command line parameters." << endl << endl 
			<< "Usage:" << "\t" 
			<< "diffDetail details_file filter_file" << endl << endl;
		exit(0);
		}

	clock_t tstart, tend;
	tstart = clock();

	vector<string> details_list;
	vector<string> filter_list;
	string inputLine;

	/* VERIFY THAT DETAILS FILE EXISTS
	*/
	ifstream detailsFile;
	detailsFile.open( commLine[1].c_str() );
	if( !detailsFile.good() ) {
		cerr << "Can't open file '" << commLine[1].c_str() << "'" << endl;
		exit(0);
		}
	/* READ DETAILS FILE
	*/
	while( getline(detailsFile, inputLine), !detailsFile.eof())
		details_list.push_back(inputLine);
	detailsFile.close();

	/* VERIFY THAT FILTER FILE EXISTS
	*/
	ifstream filterFile;
	filterFile.open( commLine[2].c_str() );
	if( !filterFile.good() ) {
		cerr << "Can't open file '" << commLine[2].c_str() << "'" << endl;
		exit(0);
		}
	/* READ FILTER FILE
	*/
	while( getline(filterFile, inputLine), !filterFile.eof())
		filter_list.push_back(inputLine);
	filterFile.close();


	/* POPULATE DETAILS MAP, THE HEADER FOR EACH SECTION IS A KEY IN THE MAP
	*/
	map<string, set<string> > details_map;
	populateDetailsMap( details_map, details_list );


	/* POPULATE FILTER MAP
	*/
	map<string, set<string> > filter_map;
	populateFilterMap( filter_map, filter_list);	


	/* FINALLY, FILTER THE DETAILS DATA FILE AGAINST THE FILTER FILE.
		FOR EACH KEYED SET IN DETAILS, FIND IF ELEMENT IS PRESENT
		IN THE THE EQUIVALENT KEYED SET OF FILTER, AND IF PRESENT
		DON'T OUTPUT TO COUT (I.E. OUTPUT TO COUT IF ABSENT).
	*/
	unsigned strPos;
	string key;
	map<string, set<string> >::iterator pDetail;
	set<string>::iterator pSItem;
	pDetail = details_map.begin();
	for( ; pDetail != details_map.end(); ++pDetail ) {
		cout << pDetail->first << endl;
		key = pDetail->first;
		strPos = key.find_first_of(":", 0);
		if( strPos != string::npos )
			key = key.substr(0, strPos);
		set<string> & locSet = filter_map[key];
		for( pSItem = pDetail->second.begin(); pSItem != pDetail->second.end(); ++pSItem ) {
			if( locSet.find(*pSItem) == locSet.end() )
				cout << *pSItem << endl; 
			}
		cout << endl;
		}


	tend = clock();
	cerr << (tend-tstart)/1000.0 << " seconds elapsed time." << endl;

	return 0;
}


void populateDetailsMap ( map<string, set<string> > & inMap, vector<string> & listItems )
{
	/* 	Find data sections of file, stored in listItems; the header lines for each 
		data section are used for the keys in the map
		A) First line of file heads a section,
		B) other section headers are preceeded by one empty line
		C) otherwise, each line is data to populate each set within the map
		D) assume the initial config file has at least one line
	*/

	set<string> tmpSet;
	tmpSet.clear();
	vector<string>::iterator pItem = listItems.begin();
	string key = *pItem;
	++pItem;
	for( ; pItem != listItems.end(); pItem++ ) {
		if( pItem->empty() && !key.empty() ) {
			inMap.insert(map<string, set<string> >::value_type(key, tmpSet) );
			tmpSet.clear();
			key = "";
			++pItem;
			if( pItem != listItems.end() && !pItem->empty() ) {
				key = *pItem;
				}
			--pItem;
			}
		else if( *pItem != key ) {
			tmpSet.insert(*pItem);
			}
		}
	if( !key.empty() && tmpSet.size() > 0 ) {
		inMap.insert(map<string, set<string> >::value_type(key, tmpSet) );
		}

	return;
}

void populateFilterMap ( map<string, set<string> > & inMap, vector<string> & listItems )
{
/*	This is here in case the format of the Filter file ever changes independently of
	the format of the Details report file.  Otherwise this method is unnecessary.
*/
	populateDetailsMap (inMap, listItems);
	return;
}
