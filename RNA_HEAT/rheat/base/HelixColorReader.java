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

    /**
     * Indicates one or more problems reading the file.
     */
    static public class ParseError
    extends Exception {

        ParseError(String messages) {
            super(messages);
        }

    }

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
    static public void parse(String absPath, RNA toAnnotate) throws IOException, ParseError {
        parse(new BufferedReader(new FileReader(absPath)), toAnnotate);
    }

    /**
     * Annotates RNA data from the given ".helixcolor" file.  Note,
     * since this depends on the presence of helices, a base-pair
     * filter must have been applied to the loaded RNA beforehand.
     * @param dataSource the source of data in ".helixcolor" format
     * @param toAnnotate the RNA in which to update helix tags
     */
    static public void parse(BufferedReader dataSource, RNA toAnnotate) throws ParseError {
        final BufferedReader threadDataSource = dataSource;
        final RNA threadRNA = toAnnotate;
        try {
            Map<String, String> tagKV = new HashMap<String, String>(); // reused below
            String lineString = null;
            StringBuilder errorMessages = new StringBuilder();
            LineType lineType = LineType.COMMENT;
            while (true) {
                lineString = threadDataSource.readLine();
                if (lineString == null) {
                    break;
                }
                int commentIndex1 = lineString.indexOf("#");
                int commentIndex2 = lineString.indexOf("//");
                int commentIndex = ((commentIndex1 >= 0)
                                    ? commentIndex1
                                    : ((commentIndex2 >= 0)
                                       ? commentIndex2
                                       : -1));
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
                // read entire line
                tagKV.clear(); // reused each time
                while (scanner.hasNext() && (!scanError)) {
                    String token = scanner.next();
                    //AppMain.log(0, "parsed token: '" + token + "'"); // debug
                    // the parser may expect different things in
                    // different states; report errors accordingly
                    switch (state) {
                    case INIT:
                        // should not be in this state anymore
                        assert false;
                        break;
                    case EXP_START:
                        // expecting to see the start of a sequence
                        Scanner subScanR1= new Scanner(token);
                        subScanR1.useDelimiter("-");
                        start1 = subScanR1.nextInt();
                        start2 = subScanR1.nextInt();
                        if (start1 == -1) {
                            errorMessages.append("Expected to see a number or range to identify the start of a sequence but saw: " + lineString + "\n");
                            scanError = true;
                        } else {
                            if (start2 == -1) {
                                start2 = start1;
                            }
                            //AppMain.log(0, "start range [" + start1 + ", " + start2 + "]"); // debug
                            state = ParserState.EXP_END;
                        }
                        break;
                    case EXP_END:
                        // expecting to see the end of a sequence
                        Scanner subScanR2 = new Scanner(token);
                        subScanR2.useDelimiter("-");
                        end1 = subScanR2.nextInt();
                        end2 = subScanR2.nextInt();
                        if (end1 == -1) {
                            errorMessages.append("Expected to see a number or range to identify the end of a sequence but saw: " + lineString + "\n");
                            scanError = true;
                        } else {
                            if (end2 == -1) {
                                end2 = end1;
                            }
                            //AppMain.log(0, "end range [" + end1 + ", " + end2 + "]"); // debug
                            state = ParserState.EXP_TAGS;
                        }
                        break;
                    case EXP_TAGS:
                        // any remaining tokens will be treated as tags
                        //AppMain.log(0, "tag token: '" + token + "'"); // debug
                        int equalsIndex = token.indexOf("=");
                        if (equalsIndex >= 0) {
                            // parse key=value form
                            String value = token.substring(equalsIndex + 1);
                            String key = token.substring(0, equalsIndex);
                            //AppMain.log(0, "tag key '" + key + "', value '" + value + "'"); // debug
                            // add tag with value
                            tagKV.put(key, value); // TEST: what happens if these are all ignored?
                        } else {
                            // assume key-only form but warn about certain punctuation
                            // that might be useful for future expansion
                            //AppMain.log(0, "tag key-only '" + token + "'"); // debug
                            if (token.contains(",") || token.contains(":")) {
                                AppMain.log(AppMain.WARN, "Token '" + token + "' contains punctuation that might conflict with future extensions to the file format; more basic tag names are recommended.");
                            }
                            // add tag with no value
                            tagKV.put(token, null);
                        }
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
                        errorMessages.append("Helix not specified correctly.\n");
                    } else {
                        // construct an annotation for given helix and tag(s)
                        SortedPair p1 = new SortedPair(start1, start2);
                        SortedPair p2 = new SortedPair(end1, end2);
                        for (String tag : tagKV.keySet()) {
                            threadRNA.addHelixAnnotation(p1, p2, tag, tagKV.get(tag));
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
            if (errorMessages.length() > 0) {
                throw new ParseError(errorMessages.toString());
            }
            // now request that the RNA data be updated to
            // match the set of annotations (this second
            // call exists to be more efficient)
            threadRNA.processHelixAnnotations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
