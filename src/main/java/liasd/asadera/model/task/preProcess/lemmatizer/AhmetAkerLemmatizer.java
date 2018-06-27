/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.liasd.asadera.model.task.preProcess.lemmatizer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import fi.seco.hfst.Transducer;
import fi.seco.hfst.Transducer.Result;
import fi.seco.hfst.TransducerAlphabet;
import fi.seco.hfst.TransducerHeader;
import fi.seco.hfst.UnweightedTransducer;
import fi.seco.hfst.WeightedTransducer;

/**
 * Support de, fr, it, en lemmatization
 * source : http://staffwww.dcs.shef.ac.uk/people/A.Aker/activityNLPProjects.html
 * @author ahmetaker
 */
public class AhmetAkerLemmatizer {

    private Transducer transducer = null;
    public final static long TRANSITION_TARGET_TABLE_START = 2147483648l; // 2^31 or UINT_MAX/2 rounded up
    public final static long NO_TABLE_INDEX = 4294967295l;
    public final static float INFINITE_WEIGHT = (float) 4294967295l; // this is hopefully the same as
    // static_cast<float>(UINT_MAX) in C++
    public final static int NO_SYMBOL_NUMBER = 65535; // this is USHRT_MAX

    public static enum FlagDiacriticOperator {
        P, N, R, D, C, U
    };
    
    public AhmetAkerLemmatizer(String aResourceFolder, String aLang) throws IOException {
    	this.transducer = initTransducer(aResourceFolder, aLang);
	}
    
    public static Transducer initTransducer(String aResourceFolder, String aLang) throws IOException {
    	Transducer transducer = null;
        FileInputStream transducerfile = null;
        transducerfile = new FileInputStream(aResourceFolder + File.separator + "lemmaModels" + File.separator + aLang + ".hfst.ol");
        DataInputStream charstream = new DataInputStream(transducerfile);
        TransducerHeader h = new TransducerHeader(charstream);
        TransducerAlphabet a = new TransducerAlphabet(charstream, h.getSymbolCount());
        if (h.isWeighted()) {
            transducer = new WeightedTransducer(charstream, h, a);
        } else {
            transducer = new UnweightedTransducer(charstream, h, a);
        }
        return transducer;
    }
    
    public String getLemma(String aWord, String aLang, String aPOSType) {
    	return getLemma(transducer, aWord, aLang, aPOSType, false);
    }

    public static String getLemma(Transducer transducer, String aWord, String aLang, String aPOSType, boolean verbose) {
        Collection<Result> analyses = transducer.analyze(aWord);
        if (verbose)
        	for (Result results : analyses) {
                System.out.println(results);
        }
        
        for (Result results : analyses) {
        	String analysis = String.join("", results.getSymbols());
            if ("en".equalsIgnoreCase(aLang)) {
                String grammar = "NONE";
                String grammarCheck = "NONE";
                if ("NOUN".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\[N\\]\\+N.*";
                    grammarCheck = "[N]+N";
                } else if ("VERB".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\[V\\]\\+V.*";
                    grammarCheck = "[V]+V";
                } else if ("ADJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\[ADJ\\]\\+ADJ.*";
                    grammarCheck = "[ADJ]+ADJ";
                } else if ("ADV".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\[ADV\\]\\+ADV.*";
                    grammarCheck = "[ADV]+ADV";
                }
                //System.out.println(analysis);
                if (analysis.contains(grammarCheck)) {
                    String lemma = analysis.replaceAll(grammar, "");
                    if ((lemma.contains("+") && !lemma.contains("-")) && (aWord.contains("-") && !aWord.contains("+"))) {
                        lemma = lemma.replaceAll("\\+", "-");
                    }
                    if (lemma.contains("+") && !aWord.contains("+")) {
                        lemma = lemma.replaceAll("\\+", "");
                    }
                    return lemma.toLowerCase();
                }
            } else if ("de".equalsIgnoreCase(aLang)) {
                String grammar = "NONE";
                String grammar2 = ">";
                String grammarCheck = "NONE";
                if ("NOUN".equalsIgnoreCase(aPOSType)) {
                    grammar = "<\\+NN>.*";
                    grammarCheck = "<+NN>";
                } else if ("VERB".equalsIgnoreCase(aPOSType)) {
                    grammar = "<\\+V>.*";
                    grammarCheck = "<+V>";
                } else if ("ADJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "<\\+ADJ>.*";
                    grammarCheck = "<+ADJ>";
                } else if ("ADV".equalsIgnoreCase(aPOSType)) {
                    grammar = "<\\+ADV>.*";
                    grammarCheck = "<+ADV>";
                } else if ("CONJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "<\\+KONJ>.*";
                    grammarCheck = "<+KONJ>";
                }
                //System.out.println(analysis);
                if (analysis.contains(grammarCheck)) {
                    String remaining = analysis.replaceAll(grammar, "");
                    String vals[] = remaining.split(grammar2);
                    StringBuffer buffer = new StringBuffer();
                    String suffix = "";
                    for (int i = 0; i < vals.length - 1; i++) {
                        String val = vals[i];
                        //System.out.println(val);
                        if (!val.startsWith("<CAP")) {
                            val = val.replaceAll("<.*", "");
                            buffer.append(val.toLowerCase());
                        }
                    }
                    String lastWord = vals[vals.length - 1].toString().replaceAll("<.*", "");
                    if (lastWord.endsWith("<SUFF")) {
                        suffix = lastWord.toLowerCase();
                    }
                    String result = null;
//                    if (aWord.toLowerCase().startsWith(buffer.toString() + "s") && !buffer.toString().trim().equals("") && !secondWord.startsWith("s")) {
//                        result = buffer.append("s").append(vals[vals.length - 1].toLowerCase()).toString().replaceAll("<.*", "");
//                    } else 
                    if (aWord.toLowerCase().equals(buffer.toString())) {
                        return aWord.toLowerCase();
                    } else {
                        String lastChar = lastWord.substring(lastWord.length()-1, lastWord.length());
                        String local = buffer.toString() + lastChar;
                        //System.out.println(local);
                        if (local.equalsIgnoreCase(aWord)) {
                            return local;
                        }
                        String last2Char = lastWord.substring(lastWord.length()-2, lastWord.length());
                        local = buffer.toString() + last2Char;
                        //System.out.println(local);
                        if (local.equalsIgnoreCase(aWord)) {
                            return local;
                        }
                    }
                    if (aWord.toLowerCase().startsWith(buffer.toString()) && !buffer.toString().trim().equals("")) {
                        String wordRemaining = aWord.toLowerCase().replaceAll(buffer.toString(), "");
                        wordRemaining = wordRemaining.replaceAll(lastWord.toLowerCase(), "");
                        if (!wordRemaining.trim().equals("") && wordRemaining.trim().length() <= 2) {                            
                            if (!suffix.equals("")) {
                                result = buffer.append(wordRemaining).toString();
                            } else {
                                String local = buffer.toString() + lastWord.toLowerCase().toString();
                                if (aWord.toLowerCase().startsWith(local)) {
                                    result = local;
                                } else {
                        //System.out.println("hep " + aWord + " _ " + buffer.toString() + " _ " + vals[vals.length - 1].toLowerCase().toString().replaceAll("<.*", "") + " _ " + wordRemaining);
                                result = buffer.append(wordRemaining).append(lastWord.toLowerCase()).toString();                                    
                                }
                            }
                        } else {
                            result = buffer.append(lastWord.toLowerCase()).toString();

                        }
                    } else if (buffer.toString().trim().equals("")) {
                        result = buffer.append(vals[vals.length - 1].toLowerCase()).toString().replaceAll("<.*", "");
                    }

                    if (result != null) {
                        result = result.replaceAll("\\{", "").replaceAll("\\}", "");
                    }
                    return result;
                }
            } else if ("it".equalsIgnoreCase(aLang)) {

                String grammar = "NONE";
                String grammarCheck = "NONE";
                if ("NOUN".equalsIgnoreCase(aPOSType)) {
                    grammar = "#NOUN.*";
                    grammarCheck = "#NOUN";
                } else if ("VERB".equalsIgnoreCase(aPOSType)) {
                    grammar = "#VER.*";
                    grammarCheck = "#VER";
                } else if ("ADJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "#ADJ.*";
                    grammarCheck = "#ADJ";
                } else if ("ADV".equalsIgnoreCase(aPOSType)) {
                    grammar = "#ADV.*";
                    grammarCheck = "#ADV";
                } else if ("CONJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "#CON.*";
                    grammarCheck = "#CON";

                }
                //System.out.println(analysis);
                if (analysis.contains(grammarCheck)) {
                    String lemma = analysis.replaceAll(grammar, "");
                    if ((lemma.contains("+") && !lemma.contains("-")) && (aWord.contains("-") && !aWord.contains("+"))) {
                        lemma = lemma.replaceAll("\\+", "-");
                    }
                    if (lemma.contains("+") && !aWord.contains("+")) {
                        lemma = lemma.replaceAll("\\+", "");
                    }
                    return lemma.toLowerCase();
                }
            } else if ("fr".equalsIgnoreCase(aLang)) {
                String grammar = "NONE";
                String grammarCheck = "NONE";
                if ("NOUN".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\+commonNoun.*";
                    grammarCheck = "+commonNoun";
                } else if ("VERB".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\+verb+.*";
                    grammarCheck = "+verb+";
                } else if ("ADJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\+adjective.*";
                    grammarCheck = "+adjective";
                } else if ("ADV".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\+adverb.*";
                    grammarCheck = "+adverb";
                } else if ("PRON".equalsIgnoreCase(aPOSType) || "CONJ".equalsIgnoreCase(aPOSType)) {
                    grammar = "\\+functionWord.*";
                    grammarCheck = "+functionWord";

                }
                //System.out.println(analysis);
                if (analysis.contains(grammarCheck)) {
                    String lemma = analysis.replaceAll(grammar, "");
                    if ((lemma.contains("+") && !lemma.contains("-")) && (aWord.contains("-") && !aWord.contains("+"))) {
                        lemma = lemma.replaceAll("\\+", "-");
                    }
                    if (lemma.contains("+") && !aWord.contains("+")) {
                        lemma = lemma.replaceAll("\\+", "");
                    }
                    return lemma.toLowerCase();
                }
            }
        }
        if (analyses.isEmpty()) {
            return null;
        }
        return null;
    }

    public static void main(String args[]) throws IOException {
        String lemma = AhmetAkerLemmatizer.getLemma(AhmetAkerLemmatizer.initTransducer("docs", "fr"), "sommes", "fr", "VERB", true);
        //lemma = "M" + lemma.substring(1);
        System.out.println(lemma);
    }
}
