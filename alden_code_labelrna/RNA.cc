#include <fstream>
#ifdef USE_PLOTTER
#include <plotter.h>
#endif
#include "util.h"
#include "RNA.h"

using namespace std;

const RNA::Binding RNA::UNBOUND = -1;

RNA::RNA() {
}

RNA::RNA(const char* filename) throw(io_error) {
	if (!load(filename))
		throw io_error("");
}

bool RNA::load(const char* filename) {
	sequence.clear();
	bindings.clear();

	//
	// Load the BPSEQ file
	//
	ifstream in(filename);
	if (!in || in.is_open() == 0)
		return false;
	in >> detecteof;
	while (in.good()) {
		if (!isdigit(in.peek())) {
			in >> endl >> detecteof;
			continue;
		}
		Binding b;
		char c;
		in >> b >> c >> b >> endl >> detecteof;
		sequence.add(c);
		bindings.add(b-1);
	}
	in.close();
	return true;
}

typedef RNA::Label* LabelPtr;

int sortByTouches(const LabelPtr* a, const LabelPtr* b) {
	if ((*a)->touches.size() > (*b)->touches.size())
		return -1;
	if ((*a)->touches.size() < (*b)->touches.size())
		return 1;
	return 0;
}

struct KnotInfo {
//	RNA::Label *label;
	size_t crossed, internal, length;
//	size_t captured;
	Bag<size_t> touches;
	char isHelix;
	size_t futureContribution;
	double score;
};

int cmp(const KnotInfo& a, const KnotInfo& b) {
	if (a.crossed > b.crossed)
		return -1;
	if (a.crossed < b.crossed)
		return 1;
	if (a.length < b.length)
		return -1;
	if (a.length > b.length)
		return 1;
//	if (a.captured < b.captured)
//		return -1;
//	if (a.captured > b.captured)
//		return 1;
	if (a.internal < 0.7 * b.internal)
		return 1;
	if (0.7 * a.internal > b.internal)
		return -1;
	return 0;
}

bool RNA::computeTiered() {
	size_t i, j, k, m;

	//
	// Break sequence into segments.
	// Each segment is a region of contiguous (un)paired bases.
	// A segment of paired bases forms one side of a helix.
	//
	segments.resize(1);
	segments[0].start = 0;
	segments[0].label = NULL;
	for (i = 1; i < bindings.size(); i++) {
		if (bindings[i] == i) {
			cerr << "Error: found self-binding nucleotide." << endl;
			return false;
		}
		if (bindings[i] == UNBOUND) {
			if (bindings[i-1] == UNBOUND)
				continue;
		} else {
			if (bindings[i] >= bindings.size()) {
				cerr << "Error: out-of-range pairing." << endl;
				return false;
			}
			if (bindings[bindings[i]] != i) {
				cerr << "Error: found non-reciprocal pairing." << endl;
				return false;
			}
			if (bindings[i-1] != UNBOUND && bindings[i] == bindings[i-1] - 1)
				continue;
		}
		segments.last().end = i - 1;
		segments.expand(1);
		segments.last().start = i;
		segments.last().label = NULL;
	}
	segments.last().end = i - 1;
	assert(segments.size() > 1);

	//
	// Build map from binding positions to segments.
	//
	bindingsToSegments.resize(bindings.size());
	for (i = 0; i < segments.size(); i++)
		for (j = segments[i].start; j <= segments[i].end; j++)
			bindingsToSegments[j] = &segments[i];

	//
	// Build helix labels
	//
	labels.clear();
	labels.reserve(segments.size());	// upper bound on number
	                                   	// of labels to prevent
	                                   	// capacity change
	for (i = 0; i < segments.size(); i++) {
		if (bindings[segments[i].start] == UNBOUND ||
		    segments[i].label != NULL)
			continue;
		labels.expand(1);
		labels.last().segments.clear();
		labels.last().touches.clear();
		labels.last().type = Label::HELIX;
		labels.last().resolved = true;
		labels.last().segments.add(&segments[i]);
		labels.last().segments.add(bindingsToSegments[bindings[segments[i].start]]);
//		labels.last().segments[0]->label = &labels.last();
		segments[i].label = &labels.last();
		labels.last().segments[1]->label = &labels.last();
	}

	//
	// Find and relabel helices in psuedoknots
	//
	for (i = 0; i < labels.size() - 1; i++)
		for (j = i + 1; j < labels.size(); j++)
			if (cmp(labels[i], labels[j]) == CROSS) {
				labels[i].type = Label::KNOT;
				labels[i].resolved = false;
				labels[i].touches.add(&labels[j]);
				labels[j].type = Label::KNOT;
				labels[j].touches.add(&labels[i]);
				labels[i].resolved = false;
			}
	for (i = 0; i < labels.size(); i++) {
		if (labels[i].resolved)
			continue;
		Bag<Label*> group, nnew;
		group.add(&labels[i]);
		nnew.add(&labels[i]);
		while (nnew.size() > 0) {
			for (j = 0; j < nnew[0]->touches.size(); j++) {
				for (k = 0; k < group.size(); k++)
					if (nnew[0]->touches[j] == group[k])
						break;
				if (k == group.size()) {
					group.add(nnew[0]->touches[j]);
					nnew.add(nnew[0]->touches[j]);
				}
			}
			nnew.remove(0);
		}
		cout << "group: " << group.size() << endl;
		mergesort(group.asNative(), group.size(), sortByTouches);
		Bag<KnotInfo> knotInfo(group.size());
		for (j = 0; j < group.size(); j++) {
			group[j]->resolved = true;
//			knotInfo[j].label = group[j];
			knotInfo[j].crossed = group[j]->touches.size();
			knotInfo[j].internal = group[j]->segments[1]->start - group[j]->segments[0]->start - 1;
			knotInfo[j].length = group[j]->segments[0]->end - group[j]->segments[0]->start + 1;
//			knotInfo[j].captured = 0;
//			k = group[j]->segments[0]->start;
//			while (k < group[j]->segments[1]->start) {
//				if (bindingsToSegments[k]->label != NULL &&
//				    bindingsToSegments[k]->label->type == Label::HELIX &&
//				    bindingsToSegments[k]->label->segments[0]->start > group[j]->segments[0]->start &&
//				    bindingsToSegments[k]->label->segments[1]->end < group[j]->segments[1]->end)
//					knotInfo[j].captured++;
//				k = bindingsToSegments[k]->end + 1;
//			}
		}
		for (j = 0; j < group.size(); j++) {
			knotInfo[j].isHelix = -1;
			for (k = 0; k < group[j]->touches.size(); k++)
				for (m = 0; m < j; m++)
					if (group[j]->touches[k] == group[m]) {
						knotInfo[j].touches.add(m);
						break;
					}
		}
		knotInfo.last().futureContribution = 0;
		for (j = knotInfo.size() - 2; j > 0; j--)
			knotInfo[j].futureContribution = knotInfo[j+1].futureContribution + knotInfo[j+1].touches.size();
		knotInfo[0].futureContribution = knotInfo[1].futureContribution + knotInfo[1].touches.size();

		long configurationsSearched = 0;
		Bag<bool*> bestStates;
		double bestScore = 0;
		j = 0;
		while (true) {
			if (j == knotInfo.size()) {
				// evaluation complete
				configurationsSearched++;
				j--;
				if (knotInfo[j].score > bestScore) {
					for (k = 0; k < bestStates.size(); k++)
						delete [] bestStates[k];
					bestStates.clear();
					bestScore = knotInfo[j].score;
				}
				bool *tmp = new bool[group.size()];
				for (k = 0; k < knotInfo.size(); k++)
					if (knotInfo[k].isHelix == 1)
						tmp[k] = true;
					else {
						assert(knotInfo[k].isHelix == 0);
						tmp[k] = false;
					}
				bestStates.add(tmp);
			} else if (knotInfo[j].isHelix == 0) {
				// ascend
				knotInfo[j].isHelix = -1;
				if (j == 0)
					break;
				j--;
			} else {
				if (knotInfo[j].isHelix == -1) {
					// decend
					knotInfo[j].isHelix = 1;
					for (k = 0; k < knotInfo[j].touches.size(); k++) {
						assert(knotInfo[j].touches[k] < j);
						if (knotInfo[knotInfo[j].touches[k]].isHelix == 1)
							break;
					}
					if (k != knotInfo[j].touches.size())
						continue;
				} else {
					// decend
					knotInfo[j].isHelix = 0;
				}
				if (j == 0)
					knotInfo[j].score = 0;
				else
					knotInfo[j].score = knotInfo[j-1].score;
				for (k = 0; k < knotInfo[j].touches.size(); k++) {
					assert(knotInfo[j].isHelix == 0 || knotInfo[knotInfo[j].touches[k]].isHelix == 0);
					switch (::cmp(knotInfo[j], knotInfo[knotInfo[j].touches[k]])) {
					case -1:
						if (knotInfo[j].isHelix == 0 &&
						    knotInfo[knotInfo[j].touches[k]].isHelix == 1)
							knotInfo[j].score++;
						break;
					case 1:
						if (knotInfo[j].isHelix == 1 &&
						    knotInfo[knotInfo[j].touches[k]].isHelix == 0)
							knotInfo[j].score++;
					case 0:
						if (knotInfo[j].isHelix == 0 &&
						    knotInfo[knotInfo[j].touches[k]].isHelix == 0)
							knotInfo[j].score += 0.5;
					}
				}
				if (knotInfo[j].score + knotInfo[j].futureContribution >= bestScore)
					j++;
				else
					configurationsSearched++;
			}
		}

		cout << "configurationsSearched: " << configurationsSearched << endl;
		cout << "bestStates: " << bestStates.size() << endl;
		assert(bestStates.size() == 1);
		for (j = 0; j < group.size(); j++)
			if (bestStates[0][j] == true) {
				for (k = 0; k < group[j]->touches.size(); k++)
					for (m = 0; m < group[j]->touches[k]->touches.size(); m++)
						if (group[j] == group[j]->touches[k]->touches[m]) {
							group[j]->touches[k]->touches.remove(m);
							break;
						}
				group[j]->type = Label::HELIX;
				group[j]->touches.clear();
			}
		delete [] bestStates[0];
	}

	//
	// Build tail loop labels
	//
	if (segments[0].label == NULL) {
		labels.expand(1);
		labels.last().segments.clear();
		labels.last().touches.clear();
		segments[0].label = &labels.last();
		labels.last().type = Label::TAIL;
		labels.last().segments.add(&segments[0]);
		assert(segments[1].label->isHelix());
		labels.last().touches.add(segments[1].label);
	}
	if (segments.last().label == NULL) {
		labels.expand(1);
		labels.last().segments.clear();
		labels.last().touches.clear();
		segments.last().label = &labels.last();
		labels.last().type = Label::TAIL;
		labels.last().segments.add(&segments.last());
		assert(segments[segments.size()-2].label->isHelix());
		labels.last().touches.add(segments[segments.size()-2].label);
	}

	//
	// Build bulge, hairpin and internal loop labels
	//
	for (i = 0; i < segments.size(); i++) {
		if (segments[i].label != NULL)
			continue;
		assert(segments[i-1].label->isHelix());
		assert(segments[i+1].label->isHelix());
		size_t left = i - 1;
		size_t right = i + 1;
		bool jumped = false;
		while (segments[left].label != NULL && segments[left].label->type == Label::KNOT && left > 0) {
			left--;	
			jumped = true;
		}
		while (segments[right].label != NULL && segments[right].label->type == Label::KNOT && right < segments.size() - 1) {
			right++;
			jumped = true;
		}
		if (segments[left].label == NULL || segments[left].label->isLoop() ||
		    segments[right].label == NULL || segments[right].label->isLoop())
			continue;
		if (segments[left].label == segments[right].label) {
			labels.expand(1);
			labels.last().segments.clear();
			labels.last().touches.clear();
			segments[i].label = &labels.last();
			if (jumped)
				labels.last().type = Label::BULGE;
			else
				labels.last().type = Label::HAIRPIN;
			labels.last().segments.add(&segments[i]);
			labels.last().touches.add(segments[left].label);
		} else if (bindings[segments[left].end] == bindings[segments[right].start] + 1) {
			labels.expand(1);
			labels.last().segments.clear();
			labels.last().touches.clear();
			segments[i].label = &labels.last();
			labels.last().type = Label::BULGE;
			labels.last().segments.add(&segments[i]);
			labels.last().touches.add(segments[left].label);
			labels.last().touches.add(segments[right].label);
		} else if (bindings[segments[left].end] > 0 &&
		    bindings[segments[right].start] < bindings.size() - 1 &&
		    bindingsToSegments[bindings[segments[left].end]-1] ==
		    bindingsToSegments[bindings[segments[right].start]+1]) {
			labels.expand(1);
			labels.last().segments.clear();
			labels.last().touches.clear();
			segments[i].label = &labels.last();
			bindingsToSegments[bindings[segments[left].end]-1]->label = &labels.last();
			labels.last().type = Label::INTERNAL;
			labels.last().segments.add(&segments[i]);
			labels.last().segments.add(bindingsToSegments[bindings[segments[left].end]-1]);
			labels.last().touches.add(segments[left].label);
			labels.last().touches.add(segments[right].label);
		}
	}

	//
	// Build multistem loop labels
	//
	for (i = 0; i < segments.size(); i++) {
		if (segments[i].label == NULL || segments[i].label->type != Label::HELIX)
			continue;
		labels.expand(1);
		labels.last().touches.clear();
		labels.last().segments.clear();
		labels.last().touches.add(segments[i].label);
		if (!traceMultistem1(segments[i].label, segments[i].end+1)) {
			labels.shrink(1);
		} else {
			labels.last().type = Label::MULTISTEM;
			for (j = 0; j < labels.last().segments.size(); j++)
				labels.last().segments[j]->label = &labels.last();
		}
	}
	for (i = 0; i < segments.size(); i++) {
		if (segments[i].label == NULL || segments[i].label->isLoop())
			continue;
		labels.expand(1);
		labels.last().touches.clear();
		labels.last().segments.clear();
		labels.last().touches.add(segments[i].label);
		if (segments[i].start == 1004) {
			i++;
			--i;
		}
		if (!traceMultistem2(segments[i].label, segments[i].end+1)) {
			labels.shrink(1);
		} else {
			for (j = 0; j < labels.last().touches.size(); j++)
				if (labels.last().touches[j]->type == Label::KNOT)
					break;
			if (j == labels.last().touches.size())
				labels.shrink(1);
			else {
				labels.last().type = Label::MULTISTEM;
				for (j = 0; j < labels.last().segments.size(); j++)
					labels.last().segments[j]->label = &labels.last();
			}
		}
	}
	for (i = 0; i < segments.size(); i++) {
		if (segments[i].label == NULL || segments[i].label->isLoop())
			continue;
		labels.expand(1);
		labels.last().touches.clear();
		labels.last().segments.clear();
		labels.last().touches.add(segments[i].label);
		if (segments[i].start == 1004) {
			i++;
			--i;
		}
		if (!traceMultistem3(segments[i].label, segments[i].end+1)) {
			labels.shrink(1);
		} else {
			for (j = 0; j < labels.last().touches.size(); j++)
				if (labels.last().touches[j]->type == Label::KNOT)
					break;
			if (j == labels.last().touches.size())
				labels.shrink(1);
			else {
				labels.last().type = Label::MULTISTEM;
				for (j = 0; j < labels.last().segments.size(); j++)
					labels.last().segments[j]->label = &labels.last();
			}
		}
	}

	//
	// Build free loop labels
	//
	for (i = 0; i < segments.size(); i++) {
		if (segments[i].label != NULL)
			continue;
		assert(segments[i-1].label->isHelix());
		assert(segments[i+1].label->isHelix());
		labels.expand(1);
		labels.last().segments.clear();
		labels.last().touches.clear();
		segments[i].label = &labels.last();
		labels.last().type = Label::FREE;
		labels.last().segments.add(&segments[i]);
		labels.last().touches.add(segments[i-1].label);
		labels.last().touches.add(segments[i+1].label);
	}

	//
	// Build parent references
	//
	for (i = 0; i < labels.size(); i++) {
		if (labels[i].type == Label::KNOT) {
			labels[i].parent = NULL;
			continue;
		}
		Segment *seg;
		if (labels[i].segments.size() == 0)
			seg = labels[i].touches[0]->segments[0];
		else
			seg = labels[i].segments[0];
		do {
			if (seg->start == 0)
				seg = NULL;
			else 
				seg = bindingsToSegments[seg->start-1];
		} while (seg != NULL && seg->label->type == Label::KNOT);
		if (seg == NULL)
			labels[i].parent = NULL;
		else
			labels[i].parent = seg->label;
	}

	return true;
}

bool RNA::traceMultistem1(Label* start, Binding pos) {
	while (true) {
		if (pos == bindings.size())
			return false;
		if (bindingsToSegments[pos]->label == start)
			break;
		if (pos < start->segments[0]->start)
			return false;
		if (bindingsToSegments[pos]->label == NULL) {
			labels.last().segments.add(bindingsToSegments[pos]);
			pos = bindingsToSegments[pos]->end + 1;
		} else if (bindingsToSegments[pos]->label->type != Label::HELIX)
			return false;
		else {
			labels.last().touches.add(bindingsToSegments[pos]->label);
			size_t oSegments = labels.last().segments.size();
			size_t oTouches = labels.last().touches.size();
			if (traceMultistem1(start, bindingsToSegments[pos]->label->segments[1]->end + 1))
				break;
			labels.last().segments.resize(oSegments);
			labels.last().touches.resize(oTouches);
			if (pos == bindingsToSegments[pos]->label->segments[0]->start) {
				if (traceMultistem1(start, bindingsToSegments[pos]->label->segments[0]->end + 1))
					break;
				labels.last().segments.resize(oSegments);
				labels.last().touches.resize(oTouches);
			}
			return false;
		}
	}
	size_t nonKnots = 0;
	for (size_t i = 0; i < labels.last().touches.size(); i++)
		if (labels.last().touches[i]->type == Label::HELIX) {
			nonKnots++;
			if (nonKnots == 2)
				return true;
		}
	return false;
}

bool RNA::traceMultistem2(Label* start, Binding pos) {
	while (true) {
		if (pos == bindings.size())
			return false;
		if (bindingsToSegments[pos]->label == start)
			break;
		if (pos < start->segments[0]->start)
			return false;
		if (bindingsToSegments[pos]->label == NULL) {
			labels.last().segments.add(bindingsToSegments[pos]);
			pos = bindingsToSegments[pos]->end + 1;
		} else if (bindingsToSegments[pos]->label->isLoop())
			return false;
		else {
			labels.last().touches.add(bindingsToSegments[pos]->label);
			size_t oSegments = labels.last().segments.size();
			size_t oTouches = labels.last().touches.size();
			if (bindingsToSegments[pos]->label->type == Label::KNOT) {
				if (traceMultistem2(start, bindingsToSegments[pos]->end + 1))
					break;
				labels.last().segments.resize(oSegments);
				labels.last().touches.resize(oTouches);
			} else {
				if (traceMultistem2(start, bindingsToSegments[pos]->label->segments[1]->end + 1))
					break;
				labels.last().segments.resize(oSegments);
				labels.last().touches.resize(oTouches);
				if (pos == bindingsToSegments[pos]->label->segments[0]->start) {
					if (traceMultistem2(start, bindingsToSegments[pos]->label->segments[0]->end + 1))
						break;
					labels.last().segments.resize(oSegments);
					labels.last().touches.resize(oTouches);
				}
			}
			return false;
		}
	}
	size_t nonKnots = 0;
	for (size_t i = 0; i < labels.last().touches.size(); i++)
		if (labels.last().touches[i]->type == Label::HELIX) {
			nonKnots++;
			if (nonKnots == 2)
				return true;
		}
	return false;
}

bool RNA::traceMultistem3(Label* start, Binding pos) {
	while (true) {
		if (pos == bindings.size())
			return false;
		if (bindingsToSegments[pos]->label == start)
			break;
		if (pos < start->segments[0]->start)
			return false;
		if (bindingsToSegments[pos]->label == NULL) {
			labels.last().segments.add(bindingsToSegments[pos]);
			pos = bindingsToSegments[pos]->end + 1;
		} else if (bindingsToSegments[pos]->label->isLoop())
			return false;
		else {
			labels.last().touches.add(bindingsToSegments[pos]->label);
			size_t oSegments = labels.last().segments.size();
			size_t oTouches = labels.last().touches.size();
			if (bindingsToSegments[pos]->label->type == Label::KNOT &&
			    pos == bindingsToSegments[pos]->label->segments[0]->start) {
				if (traceMultistem3(start, bindingsToSegments[pos]->label->segments[0]->end + 1))
					break;
				labels.last().segments.resize(oSegments);
				labels.last().touches.resize(oTouches);
			}
			if (traceMultistem3(start, bindingsToSegments[pos]->label->segments[1]->end + 1))
				break;
			labels.last().segments.resize(oSegments);
			labels.last().touches.resize(oTouches);
			if (bindingsToSegments[pos]->label->type == Label::HELIX &&
			    pos == bindingsToSegments[pos]->label->segments[0]->start) {
				if (traceMultistem3(start, bindingsToSegments[pos]->label->segments[0]->end + 1))
					break;
				labels.last().segments.resize(oSegments);
				labels.last().touches.resize(oTouches);
			}
			return false;
		}
	}
	size_t nonKnots = 0;
	for (size_t i = 0; i < labels.last().touches.size(); i++)
		if (labels.last().touches[i]->type == Label::HELIX) {
			nonKnots++;
			if (nonKnots == 2)
				return true;
		}
	return false;
}

RNA::HelixRel RNA::cmp(const Label& a, const Label& b) {
	assert(a.isHelix());
	assert(b.isHelix());

	if (a.segments[1]->end < b.segments[0]->start ||
	    a.segments[0]->start > b.segments[1]->end)
		return DISJOINT;
	if (a.segments[0]->start > b.segments[0]->end &&
	    a.segments[1]->end < b.segments[1]->start)
		return AINB;
	if (a.segments[0]->end < b.segments[0]->start &&
	    a.segments[1]->start > b.segments[1]->end)
		return BINA;
	return CROSS;
}

#ifdef USE_PLOTTER
const double HELIX_LINE_FLARE = 0.3;

bool RNA::plot(const char* filename) const {
	if (labels.size() == 0)
		return false;
	ofstream out(filename);
	if (!out || !out.is_open()) {
		cerr << "Error: unable to open file '" << filename << "'" << endl;
		return false;
	}

	PSPlotter *plotter = new PSPlotter(cin, out, cerr);
	plotter->openpl();	
	plotter->space(0,0,sequence.size(),sequence.size());
	plotter->ffontsize(0.9);
	plotter->capmod("round");
	plotter->flinewidth(0.2);
	char letter[2];
	letter[1] = 0;
	size_t i;
	for (i = 0; i < sequence.size(); i++) {
		plotter->move(i, 0);
		letter[0] = sequence[i];
		plotter->alabel('c', 'x', letter);
	}
	i = 0;
	while (i < labels.size() && labels[i].isHelix()) {
		double leftStart = labels[i].segments[0]->start;
		double leftEnd = labels[i].segments[0]->end;
		double leftMid = (leftStart + leftEnd) / 2.0;
		double rightStart = labels[i].segments[1]->start;
		double rightEnd = labels[i].segments[1]->end;
		double rightMid = (rightStart + rightEnd) / 2.0;
		double height = (rightStart - leftEnd) / 5.0 + 2;
	
		plotter->fline(leftStart - HELIX_LINE_FLARE, 1, leftEnd + HELIX_LINE_FLARE, 1);
		plotter->fline(rightStart - HELIX_LINE_FLARE, 1, rightEnd + HELIX_LINE_FLARE, 1);
		plotter->fline(leftMid, 1, leftMid, height);
		plotter->fline(rightMid, 1, rightMid, height);
		plotter->fline(leftMid, height, rightMid, height);
		i++;
	}
	plotter->closepl();
	delete plotter;
	out.close();
	return true;
}
#endif
