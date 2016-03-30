#ifndef _BAG_H_
#define _BAG_H_

#include <cassert>
#include <new>

//
// Implementation of an unordered collection.
//

template <class T> class Bag {
public:
	Bag(size_t size = 0);
	Bag(const Bag<T>& b);
	~Bag();

	// Core Bag functions

	// Return the number of elements in the Bag.
	size_t size() const;
	// Return element lvalue.
	T& operator[](size_t index);
	const T& operator[](size_t index) const;
	T& last();
	const T& last() const;

	// Add an element to the end of the Bag.
	// Returns index of added element.
	size_t add(const T& e);
	// Insert a value into the Bag.
	// Previous element (if any) at index is moved to the end.
	void insert(size_t index, const T& e);
	// Remove an element from the Bag.
	// Last element is moved to index.
	void remove(size_t index);
	// Empty the Bag; remove all elements.
	void clear();
	Bag<T>& operator=(const Bag<T>& b);

	// Useful Bag functions

	// Return the actual size of the underlying native array.
	size_t capacity() const;
	// Give access to the underlying native array, use at your own risk.
	T* asNative();
	const T* asNative() const;

	// Conditionally (increase) the capacity of the underlying native array.
	void reserve(size_t capacity);
	// Specify a new logical size.
	void resize(size_t size);
	// Increase the logical size.
	void expand(size_t size);
	// Decrease the logical size.
	void shrink(size_t size);
	// Remove unused space in the underlying native array.
	void compact();

protected:
	// Set (increase) the capacity of the underlying native array.
	void setCapacity(size_t capacity);

	size_t _size, _capacity;
	T *store;
};

template <class T> inline Bag<T>::Bag(size_t size)
: _size(0), _capacity(0), store(NULL) {
	resize(size);
}

template <class T> inline Bag<T>::Bag(const Bag<T>& b)
: _size(0), _capacity(0), store(NULL) {
	*this = b;
}

template <class T> inline Bag<T>::~Bag() {
	delete [] store;
}

template <class T> inline size_t Bag<T>::size() const {
	return _size;
}

template <class T> inline T& Bag<T>::operator[](size_t index) {
	assert(index < _size);
	return store[index];
}

template <class T> inline const T& Bag<T>::operator[](size_t index) const {
	assert(index < _size);
	return store[index];
}

template <class T> inline T& Bag<T>::last() {
	assert(_size > 0);
	return store[_size-1];
}

template <class T> inline const T& Bag<T>::last() const {
	assert(_size > 0);
	return store[_size-1];
}

template <class T> inline size_t Bag<T>::add(const T& e) {
	if (_size == _capacity)
		reserve(_size+1);
	store[_size] = e;
	return _size++;
}

template <class T> void Bag<T>::insert(size_t index, const T& e) {
	assert(index <= _size);
	if (_size == _capacity)
		reserve(_size+1);
	store[_size++] = store[index];
	store[index] = e;
}

template <class T> inline void Bag<T>::remove(size_t index) {
	assert(index < _size);
	store[index] = store[--_size];
}

template <class T> inline void Bag<T>::clear() {
	_size = 0;
}

template <class T> Bag<T>& Bag<T>::operator=(const Bag<T>& b) {
	if (this != &b) {
		reserve(b._size);
		_size = b._size;
		for (size_t i = 0; i < _size; i++)
			store[i] = b.store[i];
	}
	return *this;
}

template <class T> inline size_t Bag<T>::capacity() const {
	return _capacity;
}

template <class T> inline T* Bag<T>::asNative() {
	return store;
}

template <class T> inline const T* Bag<T>::asNative() const {
	return store;
}

template <class T> void Bag<T>::reserve(size_t capacity) {
	if (capacity > _capacity) {
		size_t nextSize = 2 * _capacity + 1;
		if (nextSize >= capacity)
			setCapacity(nextSize);
		else
			setCapacity(capacity);
	}
}

template <class T> void Bag<T>::resize(size_t size) {
//	assert(size >= _size);
	if (size > _capacity)
		reserve(size);
	_size = size;
}

template <class T> inline void Bag<T>::expand(size_t size) {
	resize(_size + size);
}

template <class T> inline void Bag<T>::shrink(size_t size) {
	assert(size <= _size);
	resize(_size - size);
}

template <class T> inline void Bag<T>::compact() {
	setCapacity(_size);
}

template <class T> void Bag<T>::setCapacity(size_t capacity) {
	if (capacity < _size || capacity == _capacity)
		return;

	T *temp = new T[capacity];
	if (store != NULL) {
		for (size_t i = 0; i < _size; i++)
			temp[i] = store[i];
		delete [] store;
	}
	store = temp;
	_capacity = capacity;
}

#endif
