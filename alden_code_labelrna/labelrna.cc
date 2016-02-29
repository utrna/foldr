#include <iostream>
#include "util.h"
#include "RNA.h"

using namespace std;

const size_t LIST_LIMIT = 16;

void showUsage();
ostream& operator<<(ostream& out, const RNA::Segment& segment);
ostream& operator<<(ostream& out, const RNA::Label& label);
ostream& operator<<(ostream& out, const RNA& rna);
void savewithoutknots(ostream& out, const RNA& rna);

int main(int argc, char* argv[]) {
	bool rp = false;

	if (argc == 1) {
		showUsage();
		return 1;
	}
	if (strcmp(argv[1], "--help") == 0) {
		showUsage();
		return 0;
	}
	size_t processedFiles = 0;
	RNA rna;
	for (size_t i = 1; i < argc; i++) {
		if (strcmp(argv[i], "-rp") == 0) {
			rp = true;
			continue;
		}
		if (!rna.load(argv[i])) {
			cerr << "Error: unable to load '" << argv[i] << "'" << endl;
			continue;
		}
		cout << "Processing '" << argv[i] << "'" << endl;
		if (!rna.computeTiered())
			continue;

		if (rp) {
			string ofname;
			if (pathExtension(argv[i]) == "bpseq")
				ofname = pathTrimExtension(argv[i]);
			else
				ofname = argv[i];
			ofname += ".noknots.bpseq";
			ofstream out(ofname.c_str());
			if (!out || !out.is_open()) {
				cerr << "Error: unable to open '" << ofname << "'" << endl;
				continue;
			}
			out << "A modified version of " << argv[i] << " with all psuedoknots removed." << endl;
			savewithoutknots(out, rna);
			out.close();
		} else {
			string ofname;
			if (pathExtension(argv[i]) == "bpseq")
				ofname = pathTrimExtension(argv[i]);
			else
				ofname = argv[i];
			ofname += ".labels.csv";
			ofstream out(ofname.c_str());
			if (!out || !out.is_open()) {
				cerr << "Error: unable to open '" << ofname << "'" << endl;
				continue;
			}
			out << rna;
			out.close();
		}
		processedFiles++;
	}
	cout << "Successfully processed " << processedFiles << " files." << endl;
	if (processedFiles > 0)
		return 0;
	else
		return 1;
}

void showUsage() {
	cout << "Usage: labelrna [-rp] <bpseq-file>..." << endl;
	cout << "  -rp   remove pseudoknots" << endl;
}

ostream& operator<<(ostream& out, const RNA::Segment& segment) {
	return out << segment.start+1 << "," << segment.end+1;
}

ostream& operator<<(ostream& out, const RNA::Label& label) {
	size_t i, limit;

	out << '"';
	switch (label.type) {
	case RNA::Label::HELIX: out << "HELIX"; break;
	case RNA::Label::KNOT: out << "HELIX-KNOT"; break;
	case RNA::Label::FREE: out << "FREE"; break;
	case RNA::Label::TAIL: out << "TAIL"; break;
	case RNA::Label::BULGE: out << "BULGE"; break;
	case RNA::Label::HAIRPIN: out << "HAIRPIN"; break;
	case RNA::Label::INTERNAL: out << "INTERNAL"; break;
	case RNA::Label::MULTISTEM: out << "MULTISTEM"; break;
	}
	out << "\"," << (long)(&label) << ',';
	if (label.parent != NULL)
		out << (long)(label.parent);
	limit = label.segments.size();
	if (limit > LIST_LIMIT) {
		cerr << "Warning: truncated region list" << endl;
		limit = LIST_LIMIT;
	}
	for (i = 0; i < limit; i++)
		out << ',' << *label.segments[i];
	for (; i < LIST_LIMIT; i++)
		out << ",,";
	limit = label.touches.size();
	if (limit > LIST_LIMIT) {
		cerr << "Warning: truncated touches list" << endl;
		limit = LIST_LIMIT;
	}
	for (i = 0; i < limit; i++)
		out << ',' << (long)(label.touches[i]);
	for (; i < LIST_LIMIT; i++)
		out << ',';
	return out;
}

ostream& operator<<(ostream& out, const RNA& rna) {
	for (size_t i = 0; i < rna.segments.size(); i++)
		out << rna.segments[i] << "," << *rna.segments[i].label << endl;
	for (size_t i = 0; i < rna.labels.size(); i++)
		if (rna.labels[i].segments.size() == 0)
			out << ",," << rna.labels[i] << endl;
	return out;
}

void savewithoutknots(ostream& out, const RNA& rna) {
	for (size_t i = 0; i < rna.sequence.size(); i++) {
		out << i+1 << ' ' << rna.sequence[i] << ' ';
		if (rna.bindingsToSegments[i]->label->type == RNA::Label::KNOT)
			out << 0;
		else
			out << rna.bindings[i]+1;
		out << endl;
	}
}
