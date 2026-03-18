package com.example;

public class Log {
    int idlog;
    String entries;
    String timestamp;

    public Log(int i, String e, String t){
        this.idlog = i;
        this.entries = e;
        this.timestamp = t;
    }

    public int getIdlog() { return idlog; }

    public String getEntries() {return entries; }

    public String getTimestamp() { return timestamp; }

}
