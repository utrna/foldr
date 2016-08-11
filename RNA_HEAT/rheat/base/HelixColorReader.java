/*
 * HelixColorReader.java
 *
 * Created on July 16, 2016.
 */

package rheat.base;

import java.io.*;
import java.util.*;

/**
 * Reads a file in ".helixcolor" format and annotates helices in an
 * RNA object accordingly.
 * @author Kevin Grant
 */
public class HelixColorReader {

    private enum ParserState {
        INIT, EXP_START, EXP_END, EXP_TAGS
    }

    private enum LineType {
        COMMENT, TAG_LIST
    }

    /**
     * Convenience method to create a file-reader and then
     * call the other parse() method with that reader.
     * @param absPath the ".helixcolor" file to open
     * @param toAnnotate the RNA in which to update helix tags
     */
    static public void parse(String absPath, RNA toAnnotate) throws IOException {
        parse(new BufferedReader(new FileReader(absPath)), toAnnotate);
    }

    /**
     * Annotates RNA data from the given ".helixcolor" file.  Note,
     * since this depends on the presence of helices, a base-pair
     * filter must have been applied to the loaded RNA beforehand.
     * @param dataSource the source of data in ".helixcolor" format
     * @param toAnnotate the RNA in which to update helix tags
     */
    static public void parse(BufferedReader dataSource, RNA toAnnotate) {
        final BufferedReader threadDataSource = dataSource;
        final RNA threadRNA = toAnnotate;
        try {
            Thread t = new Thread() {
                public void run() {
                    try {
                        String lineString = null;
                        LineType lineType = LineType.COMMENT;
                        while (true) {
                            lineString = threadDataSource.readLine();
                            if (lineString == null) {
                                break;
                            }
                            int commentIndex = lineString.indexOf("#");
                            if (commentIndex >= 0) {
                                String commentString = lineString.substring(commentIndex);
                                lineString = lineString.substring(0, commentIndex);
                                //AppMain.log(AppMain.INFO, "Ignoring comment: " + commentString); // debug
                            }
                            lineString = lineString.trim();
                            if (lineString.isEmpty()) {
                                continue;
                            }
                            lineType = LineType.TAG_LIST;
                            ParserState state = ParserState.INIT;
                            //AppMain.log(0, "read: " + lineString); // debug
                            Scanner scanner = new Scanner(lineString);
                            if (scanner.hasNext()) {
                                // can expect tokens now
                                state = ParserState.EXP_START;
                            }
                            boolean scanError = false;
                            int start1 = -1;
                            int start2 = -1;
                            int end1 = -1;
                            int end2 = -1;
                            Set<String> tags = new HashSet<String>();
                            // read entire line
                            while (scanner.hasNext() && (!scanError)) {
                                String token = scanner.next();
                                int v1 = -1;
                                int v2 = -1;
                                //AppMain.log(0, "parsed token: '" + token + "'"); // debug
                                if (token.contains("-")) {
                                    // a range
                                    Scanner subScan = new Scanner(token);
                                    subScan.useDelimiter("-");
                                    v1 = subScan.nextInt();
                                    v2 = subScan.nextInt();
                                    //AppMain.log(0, "parsed range: " + v1 + " to " + v2); // debug
                                } else {
                                    // for convenience, see if this is a number and
                                    // capture the number (otherwise, ignore for now)
                                    try {
                                        v1 = Integer.parseInt(token);
                                    } catch (NumberFormatException e) {
                                        // not a number (fine)
                                    }
                                }
                                // the parser may expect different things in
                                // different states; report errors accordingly
                                switch (state) {
                                case INIT:
                                    // should not be in this state anymore
                                    assert false;
                                    break;
                                case EXP_START:
                                    // expecting to see the start of a sequence
                                    if (v1 == -1) {
                                        AppMain.log(AppMain.ERROR, "Expected to see a number or range to identify the start of a sequence but saw: " + lineString);
                                        scanError = true;
                                    } else {
                                        start1 = v1;
                                        start2 = v2; // optional
                                        if (start2 == -1) {
                                            start2 = start1;
                                        }
                                        //AppMain.log(0, "start range [" + start1 + ", " + start2 + "]"); // debug
                                        state = ParserState.EXP_END;
                                    }
                                    break;
                                case EXP_END:
                                    // expecting to see the end of a sequence
                                    if (v1 == -1) {
                                        AppMain.log(AppMain.ERROR, "Expected to see a number or range to identify the end of a sequence but saw: " + lineString);
                                        scanError = true;
                                    } else {
                                        end1 = v1;
                                        end2 = v2; // optional
                                        if (end2 == -1) {
                                            end2 = end1;
                                        }
                                        //AppMain.log(0, "end range [" + end1 + ", " + end2 + "]"); // debug
                                        state = ParserState.EXP_TAGS;
                                    }
                                    break;
                                case EXP_TAGS:
                                    // any remaining tokens will be treated as tags
                                    //AppMain.log(0, "tag: '" + token + "'"); // debug
                                    tags.add(token);
                                    break;
                                default:
                                    // ???
                                    // unhandled case (programmer error)
                                    assert false;
                                    break;
                                }
                            }
                            // now use final values generated by parsing the line
                            if (lineType == LineType.TAG_LIST) {
                                if ((start1 <= 0) || (start2 <= 0) ||
                                    (end1 <= 0) || (end2 <= 0)) {
                                    // NOTE: scanners above should fix most cases of this
                                    AppMain.log(AppMain.ERROR, "Helix not specified correctly.");
                                } else {
                                    // construct an annotation for given helix and tag(s)
                                    SortedPair p1 = new SortedPair(start1, start2);
                                    SortedPair p2 = new SortedPair(end1, end2);
                                    for (String tag : tags) {
                                        threadRNA.addHelixAnnotation(p1, p2, tag);
                                    }
                                }
                            } else if (lineType == LineType.COMMENT) {
                                // nothing required
                            } else {
                                // ???
                                // unhandled case (programmer error)
                                assert false;
                            }
                        }
                        // now request that the RNA data be updated to
                        // match the set of annotations (this second
                        // call exists to be more efficient)
                        threadRNA.processHelixAnnotations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            t.join();
            // FIXME: annotate
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
