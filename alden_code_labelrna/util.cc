#include <cassert>
#include <cctype>
#include <cerrno>
#include <sstream>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include "util.h"

using namespace std;

istream& wsline(istream& in) {
	while (in.good()) {
		int c = in.get();
		if (c == EOF) {
//			in.clear(~ios::failbit & in.rdstate());
			break;
		} else if (!isspace(c)) {
			in.unget();
			break;
		} else if (c == '\n')
			break;
	}

	return in;
}

istream& endl(istream& in) {
	while (in.good()) {
		int c = in.get();
		if (c == EOF) {
//			in.clear(~ios::failbit & in.rdstate());
			break;
		} else if (c == '\n')
			break;
	}

	return in;
}

std::istream& detecteof(std::istream& in) {
	if (in.good()) {
		char c = in.get();
		if (in.eof())
			in.clear(~ios::failbit & in.rdstate());
		else
			in.putback(c);
	}
	return in;
}

istream& getline(istream& in, ostream& out) throw(io_error) {
	if (in.bad() || in.fail() || out.bad() || out.fail())
		throw io_error();

	string s;
	getline(in, s);
	if (in.bad() || in.fail())
		throw io_error();
	in >> detecteof;
	out << s;
	if (out.bad() || out.fail())
		throw io_error();
	return in;
}

istream& skipLine(istream& in, int numLines) {
	assert(numLines >= 0);
	for (int i = 0; in && i < numLines; i++)
		while (in.get() != '\n');
	return in;
}

string& trim(string& str) {
	size_t left = 0, right = str.length();
	if (right == 0)
		return str;
	else
		right--;
	while (left < right && isspace(str.at(left)))
		left++;
	while (left < right && isspace(str.at(right)))
		right--;
	str.erase(right + 1, str.length() - right);
	str.erase(0, left);
	return str;
}

void status(ios& subject, ostream& out) {
	bool first = true;
	if (subject.good()) {
		out << "GOOD";
		first = false;
	}
	if (subject.eof()) {
		if (!first)
			out << " ";
		out << "EOF";
		first = false;
	}
	if (subject.fail()) {
		if (!first)
			out << " ";
		out << "FAIL";
		first = false;
	}
	if (subject.bad()) {
		if (!first)
			out << " ";
		out << "BAD";
	}
}

/*
std::istream& operator>>(std::istream& in, const char& c) {
	if (in.good()) {
		in >> ws;
		if (in.get() != c) {
			in.unget();
			in.setstate(ios::failbit);
		}
	}
	return in;
}
*/

bool exists(const char* path) {
	struct stat buf;
	if (stat(path, &buf) == -1)
		return false;
	return true;
}

bool readable(const char* path) {
	int r = open(path, O_RDONLY);
	if (r < 0)
		return false;
	close(r);
	return true;
}

bool isDirectory(const char* path) {
	struct stat buf;
	if (*path == 0)
		path = ".";
	if (stat(path, &buf) == -1)
		return false;
	return S_ISDIR(buf.st_mode);
}

bool makedir(const char* path, mode_t mode) {
	struct stat fs;
	char *buf = new char[strlen(path)+1];
	strcpy(buf, path);
	char *p = strtok(buf, "/");
	while (p != NULL) {
		if (p != buf)
			p[-1] = '/';
		int r = stat(buf, &fs);
		if (r == 0) {
			if (!S_ISDIR(fs.st_mode)) {
				delete [] buf;
				return false;
			}
		} else if (errno == ENOENT)
			break;
		else {
			delete [] buf;
			return false;
		}
		p = strtok(NULL, "/");
	}

	while (p != NULL) {
		if (p != buf)
			p[-1] = '/';
		if (strcmp(p, ".") != 0 && strcmp(p, "..") != 0) {
			int r = mkdir(buf, mode);
			if (r != 0) {
				delete [] buf;
				return false;
			}
		}
		p = strtok(NULL, "/");
	}

	delete [] buf;
	return true;
}

string maketemp() {
	char buf[] = "/tmp/tmp.XXXXXX";
	int fd = mkstemp(buf);
	if (fd == -1)
		return string("");
	close(fd);
	return string(buf);
}

string maketempdir() {
	char buf[] = "/tmp/tmp.XXXXXX";
	char *mod = mkdtemp(buf);
	if (mod == NULL)
		return string("");
	return string(buf);
}

string pathDirectory(const char* path) {
	const char *p = strrchr(path, '/');
	if (p == NULL)
		return string();
	else
		return string(path, static_cast<size_t>(p - path + 1));
}

string pathFilename(const char* path) {
	const char *p = strrchr(path, '/');
	if (p == NULL)
		return string(path);
	else
		return string(p+1);
}

string pathBase(const char* path) {
	const char *p = strrchr(path, '/');
	if (p != NULL)
		path = p+1;
	p = strrchr(path, '.');
	if (p == NULL)
		return string(path);
	else
		return string(path, static_cast<size_t>(p - path));
}

string pathExtension(const char* path) {
	path = strrchr(path, '.');
	if (path == NULL)
		return string();
	else
		return string(path+1);
}

string pathTrimExtension(const char* path) {
	const char *p = strrchr(path, '.');
	if (p == NULL)
		return string(path);
	else
		return string(path, static_cast<size_t>(p - path));
}


StreamFormat::StreamFormat()
: store(true) {
}

ostream& operator<<(ostream& out, StreamFormat& fmt) {
	if (fmt.store) {
		fmt.flags = out.flags();
		fmt.precision = out.precision();
		out.flags(ios::dec);
		out.setf(ios::left, ios::adjustfield);
		out.setf(ios::showbase);
		out.setf(ios::showpoint);
		out.precision(6);
	} else {
		out.flags(fmt.flags);
		out.precision(fmt.precision);
	}
	fmt.store = !fmt.store;
	return out;
}
