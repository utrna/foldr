#include <cmath>
#include <cstring>
#include <ctime>
#include <new>
#include "Random.h"

Random g_Random;

const unsigned int Random::N = 624;
const unsigned int Random::M = 397;
const unsigned long Random::MATRIX_A = 0x9908b0dfUL;
const unsigned long Random::UMASK = 0x80000000UL;
const unsigned long Random::LMASK = 0x7fffffffUL;

Random::Random() {
	init(time(NULL));
}

Random::Random(unsigned long seed) {
	init(seed);
}

Random::Random(unsigned long key[], unsigned long keyLength) {
	unsigned int i, j, k;
	init(19650218UL);
	i=1; j=0;
	k = (N > keyLength ? N : keyLength);
	for (; k; k--) {
		state[i] = (state[i] ^ ((state[i-1] ^ (state[i-1] >> 30)) * 1664525UL)) + key[j] + j;
		state[i] &= 0xffffffffUL;
		i++; j++;
		if (i>=N) { state[0] = state[N-1]; i=1; }
		if (j>=keyLength) j=0;
	}
	for (k=N-1; k; k--) {
		state[i] = (state[i] ^ ((state[i-1] ^ (state[i-1] >> 30)) * 1566083941UL)) - i;
		state[i] &= 0xffffffffUL;
		i++;
		if (i>=N) { state[0] = state[N-1]; i=1; }
	}
	state[0] = 0x80000000UL;
}

Random::Random(const Random& mt) {
	state = new unsigned long[N];
	*this = mt;
}

Random::~Random() {
	delete [] state;
}

unsigned long Random::next32() {
    unsigned long y;

    if (--left == 0) nextState();
    y = *next++;

    y ^= (y >> 11);
    y ^= (y << 7) & 0x9d2c5680UL;
    y ^= (y << 15) & 0xefc60000UL;
    y ^= (y >> 18);

    return y;
}

long Random::next31() {
	return static_cast<long>(next32() >> 1);
}

double Random::nextClosed() {
	return static_cast<double>(next32()) * (1.0/4294967295.0);
}

double Random::nextHalf() {
	return static_cast<double>(next32()) * (1.0/4294967296.0);
}

double Random::nextOpen() {
	return (static_cast<double>(next32()) + 0.5) * (1.0/4294967296.0);
}

double Random::nextHalf53() {
	unsigned long a = next32()>>5, b = next32()>>6; 
	return (a * 67108864.0 + b) * (1.0 / 9007199254740992.0); 
}

double Random::nextGaussian() {
	double x, y, r;

	do {
		x = 2 * nextHalf53() - 1;
		y = 2 * nextHalf53() - 1;
		r = x * x + y * y;
	} while (r > 1 || r == 0);
	return x * sqrt(-2 * log(r) / r);
}

unsigned long Random::next32(unsigned long min, unsigned long max) {
	if (min == max)
		return min;
	return min + static_cast<unsigned long>((max-min+1) * nextHalf());
}

Random& Random::operator=(const Random& mt) {
	memcpy(state, mt.state, N * sizeof(unsigned long));
	left = mt.left;
	next = state + (mt.next - mt.state);
	return *this;
}

void Random::init(unsigned long seed) {
	state = new unsigned long[N];
	unsigned int j;
	state[0]= seed & 0xffffffffUL;
	for (j=1; j<N; j++) {
		state[j] = (1812433253UL * (state[j-1] ^ (state[j-1] >> 30)) + j); 
		state[j] &= 0xffffffffUL;
	}
	left = 1;
}

void Random::nextState() {
    unsigned long *p=state;
    int j;

    left = N;
    next = state;
    
    for (j=N-M+1; --j; p++) 
        *p = p[M] ^ twist(p[0], p[1]);

    for (j=M; --j; p++) 
        *p = p[M-N] ^ twist(p[0], p[1]);

    *p = p[M-N] ^ twist(p[0], state[0]);
}
