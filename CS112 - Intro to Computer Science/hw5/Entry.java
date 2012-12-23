/*
 * Entry.java - defines the Entry class, which contains a word and its alphagram
 * 
 * Code by: Tim Duffy <timmahd@bu.edu>
 */

public class Entry {
    String alphagram;
    String stringName;
    
    Entry(String newKey, String newValue) {alphagram = newKey; stringName = newValue;}
    String getKey(){ return alphagram; }
    String getValue(){ return stringName; }
}