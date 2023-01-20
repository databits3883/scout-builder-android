package com.databits.scoutbuilder.model;

import java.util.List;

public class PageContainer {
    public Integer totalCells;
    private boolean Dark_Theme;
    private String Page_Title;
    private double Version;
    public List<Cell> cell;

    public PageContainer(Integer totalCells, Boolean dark_theme, String page_title, double version, List<Cell> cell) {
        this.totalCells = totalCells;
        this.Dark_Theme = dark_theme;
        this.Page_Title = page_title;
        this.Version = version;
        this.cell = cell;
    }

    public Integer gettotalCells() {
        return totalCells;
    }

    public void setTotalCells(Integer totalCells) {
        this.totalCells = totalCells;
    }

    public boolean getDark_theme() {
        return Dark_Theme;
    }

    public void setDark_theme(boolean dark_theme) {
        this.Dark_Theme = dark_theme;
    }

    public String getPage_title() {
        return Page_Title;
    }

    public void setPage_title(String page_title) {
        this.Page_Title = page_title;
    }

    public double getVersion() {
        return Version;
    }

    public void setVersion(double version) {
        this.Version = version;
    }

    public List<Cell> getCell() {
        return cell;
    }

    public void setCell(List<Cell> cell) {
        this.cell = cell;
    }
}
