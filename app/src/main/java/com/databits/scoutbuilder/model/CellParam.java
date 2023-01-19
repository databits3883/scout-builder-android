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

    public CellParam(String cellType) {
        this.cellType = cellType;
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType;
    }

    public int getCellDefault() {
        return cellDefault;
    }

    public void setCellDefault(int cellDefault) {
        this.cellDefault = cellDefault;
    }

    public int getCellMax() {
        return cellMax;
    }

    public void setCellMax(int cellMax) {
        this.cellMax = cellMax;
    }

    public int getCellMin() {
        return cellMin;
    }

    public void setCellMin(int cellMin) {
        this.cellMin = cellMin;
    }

    public int getCellUnit() {
        return cellUnit;
    }

    public void setCellUnit(int cellUnit) {
        this.cellUnit = cellUnit;
    }

    public int getCellSegments() {
        return cellSegments;
    }

    public void setCellSegments(int cellSegments) {
        this.cellSegments = cellSegments;
    }

    public List<String> getCellSegmentLabels() {
        return cellSegmentLabels;
    }

    public void setCellSegmentLabels(List<String> cellSegmentLabels) {
        this.cellSegmentLabels = cellSegmentLabels;
    }

    public int getCellTotalEntries() {
        return cellTotalEntries;
    }

    public void setCellTotalEntries(int cellTotalEntries) {
        this.cellTotalEntries = cellTotalEntries;
    }

    public List<String> getCellEntryLabels() {
        return cellEntryLabels;
    }

    public void setCellEntryLabels(List<String> cellEntryLabels) {
        this.cellEntryLabels = cellEntryLabels;
    }

    public boolean isCellTextHidden() {
        return cellTextHidden;
    }

    public void setCellTextHidden(boolean cellTextHidden) {
        this.cellTextHidden = cellTextHidden;
    }

    public String getCellTextHint() {
        return cellTextHint;
    }

    public void setCellTextHint(String cellTextHint) {
        this.cellTextHint = cellTextHint;
    }
}