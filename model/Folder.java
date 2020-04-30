package com.totsp.bookworm.model;

public class Folder {

    public long fid;
    public String name;

    public Folder (){
        this.name = "";
    }


    public Folder(final String title) {
        if ((title == null) || (title.length() < 1)) {
            throw new IllegalArgumentException("Error, Title must have a title (minimum size 1)");
        }

        this.name = title;
    }




}
