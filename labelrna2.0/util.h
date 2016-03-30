#ifndef _UTIL_H_
#define _UTIL_H_

#include <cstdlib>
#include <fstream>
#include <iostream>
#include <new>
#include <sstream>
#include <string>
#include <sys/stat.h>
#include "exceptions.h"

// const long double PI = 3.141592653589793238;

// discards characters until non-whitespace (exclusive) or newline (inclusive)
// is found
std::istream& wsline(std::istream& in);
// discards characters until newline (inclusive) is found
std::istream& endl(std::istream& in);
// damn silly hack for Condor
std::istream& detecteof(std::istream& in);
std::istream& getline(std::istream& in, std::ostream& out) throw(io_error);
std::istream& skipLine(std::istream& in, int numLines = 1);
std::string& trim(std::string& str);
void status(std::ios& subject, std::ostream& out);

//std::istream& operator>>(std::istream& in, const char& c);

bool exists(const char* path);
bool readable(const char* path);
bool isDirectory(const char* path);
// like mkdir but will create parent directories
bool makedir(const char* path, mode_t mode = S_IRWXU | S_IRWXG | S_IRWXO);
// wrapper for mkstemp returning temporary filename
std::string maketemp();
// wrapper for mkdtemp returning temporary directory
std::string maketempdir();
// returns the directory portion of a path
std::string pathDirectory(const char* path);
// returns the file portion of a path
std::string pathFilename(const char* path);
// returns the file basename portion of a path
std::string pathBase(const char* path);
// returns the file extension portion of a path
std::string pathExtension(const char* path);
// returns the path with the file extension removed,
// a.k.a. pathDirectory + pathBase
std::string pathTrimExtension(const char* path);

#if !defined(__SGI_STL_INTERNAL_ALGOBASE_H) && !defined(_ALGOBASE_H) && !defined(__GLIBCPP_INTERNAL_ALGOBASE_H)
template <class T> inline void swap(T& a, T& b) {
	T r = a;
	a = b;
	b = r;
}

template <class T> inline const T& min(const T& a, const T& b) {
	return a <= b ? a : b;
}

template <class T> inline const T& max(const T& a, const T& b) {
	return a >= b ? a : b;
}
#endif

template <class T> inline T& min(T& a, T& b) {
	return a <= b ? a : b;
}

template <class T> inline T& max(T& a, T& b) {
	return a >= b ? a : b;
}

template <class T> inline T min(const T a, const T b) {
	return a <= b ? a : b;
}

template <class T> inline T max(const T a, const T b) {
	return a >= b ? a : b;
}

template <class T> size_t max(T* a, size_t size) {
	size_t best = 0;

	for (size_t i = 1; i < size; i++)
		if (a[i] > a[best])
			best = i;

	return best;
}

template <class T> size_t min(T* a, size_t size) {
	size_t best = 0;

	for (size_t i = 1; i < size; i++)
		if (a[i] < a[best])
			best = i;

	return best;
}


template <class T> bool load(const char* filename, T* t) {
	std::ifstream in(filename);
	if (!in || !in.is_open())
		return false;
	in >> *t;
	bool retVal = !in.fail() && !in.bad();
//	bool retVal = (!in.fail() || in.eof()) && !in.bad();
//	in.clear();
	in.close();
	return retVal;
}

template <class T> bool save(const char* filename, const T* t) {
	std::ofstream out(filename);
	if (!out || !out.is_open())
		return false;
	out << *t;
	bool retVal = out.good();
//	out.clear();
	out.close();
	return retVal;
}

// stable merge sort (ascending)
template <class T> void mergesort(T* array, size_t size) {
	T* tmp = new T[size];
	for (size_t i = 0; i < size; i++)
		tmp[i] = array[i];
	mergesort_aux(tmp, array, size);
	delete [] tmp;
}

template <class T> void mergesort_aux(T* src, T* dest, size_t size) {
	if (size <= 1)
		return;
	size_t leftSize = size/2, rightSize = size - leftSize;
	mergesort_aux(dest, src, leftSize);
	mergesort_aux(&dest[leftSize], &src[leftSize], rightSize);
	size_t left = 0, right = leftSize;
	while (left < leftSize && right < size)
		if (src[right] < src[left])
			*dest++ = src[right++];
		else
			*dest++ = src[left++];
	while (left < leftSize)
		*dest++ = src[left++];
	while (right < size)
		*dest++ = src[right++];
}

// comparison function as per qsort, though only < 0, ! < 0 used.
template <class T> void mergesort(T* array, size_t size, int (*cmp)(const T*, const T*)) {
	T* tmp = new T[size];
	for (size_t i = 0; i < size; i++)
		tmp[i] = array[i];
	mergesort_aux(tmp, array, size, cmp);
	delete [] tmp;
}

template <class T> void mergesort_aux(T* src, T* dest, size_t size, int (*cmp)(const T*, const T*)) {
	if (size <= 1)
		return;
	size_t leftSize = size/2, rightSize = size - leftSize;
	mergesort_aux(dest, src, leftSize, cmp);
	mergesort_aux(&dest[leftSize], &src[leftSize], rightSize, cmp);
	size_t left = 0, right = leftSize;
	while (left < leftSize && right < size)
		if (cmp(&src[right], &src[left]) < 0)
			*dest++ = src[right++];
		else
			*dest++ = src[left++];
	while (left < leftSize)
		*dest++ = src[left++];
	while (right < size)
		*dest++ = src[right++];
}

template <class T, class U> void mergesort(T* array, size_t size, int (*cmp)(const T*, const T*, const U), const U arg) {
	T* tmp = new T[size];
	for (size_t i = 0; i < size; i++)
		tmp[i] = array[i];
	mergesort_aux(tmp, array, size, cmp, arg);
	delete [] tmp;
}

template <class T, class U> void mergesort_aux(T* src, T* dest, size_t size, int (*cmp)(const T*, const T*, const U), const U arg) {
	if (size <= 1)
		return;
	size_t leftSize = size/2, rightSize = size - leftSize;
	mergesort_aux(dest, src, leftSize, cmp, arg);
	mergesort_aux(&dest[leftSize], &src[leftSize], rightSize, cmp, arg);
	size_t left = 0, right = leftSize;
	while (left < leftSize && right < size)
		if (cmp(&src[right], &src[left], arg) < 0)
			*dest++ = src[right++];
		else
			*dest++ = src[left++];
	while (left < leftSize)
		*dest++ = src[left++];
	while (right < size)
		*dest++ = src[right++];
}

class StreamFormat {
public:
	StreamFormat();

	friend std::ostream& operator<<(std::ostream& out, StreamFormat& fmt);

private:
	bool store;
	std::ios::fmtflags flags;
	std::streamsize precision;
};

#endif
