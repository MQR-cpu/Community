package com.mqr.community.entity;

public class PageBean {

    private int currentPage = 1;
    private int limit = 10;
    private int rows;
    private int totalPages;

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage > 0) {
            this.currentPage = currentPage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit > 0 && limit < 100)
           this.limit = limit;
    }

    public int getTotalPages() {
        return rows % limit == 0 ? rows / limit : rows / limit + 1;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getOffset() {
        return (currentPage - 1) * limit;
    }

    public int getFrom() {
        int from = currentPage - 2;
        return from < 1 ? 1 : from;
    }

    public int getTo() {
        int to = currentPage + 3;
        int totalPages = getTotalPages();
        return to > totalPages ? totalPages : to;
    }
}
