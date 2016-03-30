#ifndef _EXCEPTIONS_H_
#define _EXCEPTIONS_H_

#include <stdexcept>

class io_error : public std::runtime_error {
public:
	explicit io_error() : runtime_error("") {}
	explicit io_error(const std::string& msg) : runtime_error(msg) {}
};

#endif
