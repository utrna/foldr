#ifndef _RNA_H_
#define _RNA_H_

#include "Array.h"
#include "exceptions.h"

class RNA {
public:
	typedef int Binding;
	static const Binding UNBOUND;

	struct Label;
	struct Segment {
		Binding start, end;
		Label* label;
	};
	struct Label {
		enum Type {HELIX, KNOT, FREE, TAIL, BULGE, HAIRPIN, INTERNAL, MULTISTEM} type;
		// FREE, TAIL, BULGE, HAIRPIN use segments[0]
		// HELIX, INTERNAL use segments[0-1]
		// MULTISTEM uses segments[0-2+]
		Array<Segment*> segments;
		// Used by knots to record crossing knots
		// Used by loops to record the helices they touch
		Array<Label*> touches;
		// Points to the label preceding the 5' end of this label
		Label* parent;
		// False if pseudoknot requires further processing
		bool resolved;

		bool isHelix() const {
			return type == HELIX || type == KNOT;
		}
		bool isLoop() const {
			return !isHelix();
		}
	};

	RNA();
	RNA(const RNA&) {assert(false);}
	RNA(const char* filename) throw(io_error);

	bool load(const char* filename);
	bool computeTiered();

#ifdef USE_PLOTTER
	// Produces an EPS image
	bool plot(const char* filename) const;
#endif

//private:
	// multistems with no knots
	bool traceMultistem1(Label* start, Binding pos);
	// multistems that skip over knots
	bool traceMultistem2(Label* start, Binding pos);
	// multistems that bridge knots
	bool traceMultistem3(Label* start, Binding pos);
	enum HelixRel {AINB, BINA, DISJOINT, CROSS};
	static HelixRel cmp(const Label& a, const Label& b);

	// Tier 1
	Array<char> sequence;
	Array<Binding> bindings;
	// Tier 2
	Array<Segment> segments;
	Array<Segment*> bindingsToSegments;
	// Tier 3
	Array<Label> labels;
};

#endif
