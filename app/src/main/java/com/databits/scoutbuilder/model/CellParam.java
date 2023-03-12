package com.databits.scoutbuilder.model;

import java.util.List;

public class CellParam {
    private String cellType;
    private int cellDefault;
    private int cellMax;
    private int cellMin;
    private int cellUnit;
    private int cellSegments;
    private List<String> cellSegmentLabels;
    private int cellTotalEntries;
    private List<String> cellEntryLabels;
    private boolean cellTextHidden;
    private String cellTextHint;

    private String helpText;

    public CellParam(String cellType) {
        this.cellType = cellType;
    }

    public String getType() {
        return cellType;
    }

    public void setType(String cellType) {
        this.cellType = cellType;
    }

    public int getDefault() {
        return cellDefault;
    }

    public void setDefault(int cellDefault) {
        this.cellDefault = cellDefault;
    }

    public int getMax() {
        return cellMax;
    }

    public void setMax(int cellMax) {
        this.cellMax = cellMax;
    }

    public int getMin() {
        return cellMin;
    }

    public void setMin(int cellMin) {
        this.cellMin = cellMin;
    }

    public int getUnit() {
        return cellUnit;
    }

    public void setUnit(int cellUnit) {
        this.cellUnit = cellUnit;
    }

    public int getSegments() {
        return cellSegments;
    }

    public void setSegments(int cellSegments) {
        this.cellSegments = cellSegments;
    }

    public List<String> getSegmentLabels() {
        return cellSegmentLabels;
    }

    public void setSegmentLabels(List<String> cellSegmentLabels) {
        this.cellSegmentLabels = cellSegmentLabels;
    }

    public int getTotalEntries() {
        return cellTotalEntries;
    }

    public void setTotalEntries(int cellTotalEntries) {
        this.cellTotalEntries = cellTotalEntries;
    }

    public List<String> getEntryLabels() {
        return cellEntryLabels;
    }

    public void setEntryLabels(List<String> cellEntryLabels) {
        this.cellEntryLabels = cellEntryLabels;
    }

    public boolean isTextHidden() {
        return cellTextHidden;
    }

    public void setTextHidden(boolean cellTextHidden) {
        this.cellTextHidden = cellTextHidden;
    }

    public String getTextHint() {
        return cellTextHint;
    }

    public void setTextHint(String cellTextHint) {
        this.cellTextHint = cellTextHint;
    }

    public  String getHelpText() {return helpText;}

    public void setHelpText(String helpText) {this.helpText = helpText;}
}