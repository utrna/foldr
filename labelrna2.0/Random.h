/* 
   A C++ port (plus other modifications) of...
   
   MT19937, with initialization improved 2002/2/10.  (mt19937ar-cok)
   Coded by Takuji Nishimura and Makoto Matsumoto.
   This is a faster version by taking Shawn Cokus's optimization,
   Matthe Bellew's simplification, Isaku Wada's real version.

   Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji Nishimura,
   All rights reserved.                          

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.

     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.

     3. The names of its contributors may not be used to endorse or promote 
        products derived from this software without specific prior written 
        permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
   TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#ifndef _RANDOM_H_
#define _RANDOM_H_

class Random {
public:
	// Seed RNG with the current system time
	Random();
	// Seed RNG from a 32-bit state space
	Random(unsigned long seed);
	// Seed RNG from a larger state space
	Random(unsigned long key[], unsigned long keyLength);
	Random(const Random& mt);
	~Random();

	// Generates a random number on [0, 0xffffffff] 
	unsigned long next32();
	// Generates a random number on [0, 0x7fffffff]
	long next31();
	// Generates a random number on [0, 1]  --  32-bit resolution
	double nextClosed();
	// Generates a random number on [0, 1)  --  32-bit resolution
	double nextHalf();
	// Generates a random number on (0, 1)  --  32-bit resolution
	double nextOpen();
	// Generates a random number on [0, 1)  --  53-bit resolution
	double nextHalf53();
	// Generates a random number from a Gaussian distribution
	// with mean 0 and standard deviation 1 --  53-bit resolution
	double nextGaussian();
	// Generates a random number on [min, max],
	// where 0 <= min <= max <= 0xffffffff
	unsigned long next32(unsigned long min, unsigned long max);
	Random& operator=(const Random& mt);

private:
	void init(unsigned long seed);
	void nextState();
	static unsigned long mixbits(unsigned long u, unsigned long v)
	{
		return (u & UMASK) | (v & LMASK);
	}
	static unsigned long twist(unsigned long u, unsigned long v)
	{
		return (mixbits(u, v) >> 1) ^ (v&1UL ? MATRIX_A : 0UL);
	}

	static const unsigned int N;
	static const unsigned int M;
	static const unsigned long MATRIX_A;
	static const unsigned long UMASK;
	static const unsigned long LMASK;

	unsigned long *state;
	int left;
	unsigned long *next;
};

extern Random g_Random;

#endif
