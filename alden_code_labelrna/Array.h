#ifndef _ARRAY_H_
#define _ARRAY_H_

#include "Bag.h"

//
// Implementation of an ordered collection.
//

template <class T> class Array : public Bag<T> {
public:
	Array(size_t size = 0);
	Array(const Bag<T>& b);

	// Overriden Bag functions

	// Insert a value into the Array.
	// All elements >= index are shifted up one position.
	void insert(size_t index, const T& e);
	// Remove an element from the Array.
	// All elements > index are shifted down one position.
	void remove(size_t index);
};

template <class T> inline Array<T>::Array(size_t size) : Bag<T>(size) {
}

template <class T> inline Array<T>::Array(const Bag<T>& b) : Bag<T>(b) {
}

template <class T> void Array<T>::insert(size_t index, const T& e) {
	assert(index <= this->_size);
	if (this->_size == this->_capacity)
		reserve(this->_size+1);
	for (size_t i = this->_size++; i > index; i--)
		this->store[i] = this->store[i-1];
	this->store[index] = e;
}

template <class T> void Array<T>::remove(size_t index) {
	assert(index < this->_size);
	this->_size--;
	for (size_t i = index; i < this->_size; i++)
		this->store[i] = this->store[i+1];
}

#endif
