/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maicard.site.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Paging utility.
 * 
 * <pre>
 * Paging paging = new Paging();
 * // set total results to 100, current page to 3.
 * paging.setTotalResults(100);
 * paging.setCurrentPage(3);
 * </pre>
 * 
 * @author Byeongkil Woo
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paging implements Serializable {

	private static final long serialVersionUID = 2439137595310411068L;

	public static final int INITED_CURRENT_PAGE = 1;

	public static final int DEFAULT_MAX_RESULTS = 40;

	public static final int DEFAULT_PAGING_LINKS = 5;

	public static final int PAGING_LINKS_STYLE_STATIC = 0;

	public static final int PAGING_LINKS_STYLE_DYNAMIC = 1;

	private boolean inited = false;

	private int currentPage = INITED_CURRENT_PAGE;

	private int maxResults = DEFAULT_MAX_RESULTS;

	private int pagingLinks = DEFAULT_PAGING_LINKS;

	private int pagingLinksStyle = PAGING_LINKS_STYLE_STATIC;

	private int totalResults = 0;

	public Paging() {
		this(DEFAULT_MAX_RESULTS, DEFAULT_PAGING_LINKS, PAGING_LINKS_STYLE_STATIC);
	}

	public Paging(int maxResults) {
		this(maxResults, DEFAULT_PAGING_LINKS, PAGING_LINKS_STYLE_STATIC);
	}

	public Paging(int maxResults, int pagingLinks) {
		this(maxResults, pagingLinks, PAGING_LINKS_STYLE_STATIC);
	}

	public Paging(int maxResults, int pagingLinks, int pagingLinkStyle) {
		if (maxResults < 1) {
			throw new IllegalArgumentException("maxResults must not smaller than 1 but " + maxResults);
		}

		if (pagingLinks < 1) {
			throw new IllegalArgumentException("pagingLinks must not smaller than 1 but " + pagingLinks);
		}

		if (pagingLinkStyle != PAGING_LINKS_STYLE_STATIC && pagingLinkStyle != PAGING_LINKS_STYLE_DYNAMIC) {
			throw new IllegalArgumentException("pagingLinkStyle must be " + PAGING_LINKS_STYLE_STATIC + " or " + PAGING_LINKS_STYLE_DYNAMIC + " but " + pagingLinkStyle);
		}

		this.maxResults = maxResults;
		this.pagingLinks = pagingLinks;
		this.pagingLinksStyle = pagingLinkStyle;
	}

	public int getFirstResult() {
		return Math.max((getCurrentPage() - 1) * maxResults, 0);
	}

	public int getFirstRownum() {
		return getFirstResult() + 1;
	}

	public int getLastRownum() {
		return getFirstResult() + maxResults;
	}

	public int getFirstPage() {
		return Math.min(totalResults, 1);
	}

	public int getCurrentResultNum() {
		return getTotalResults() - ((getCurrentPage() - 1) * getMaxResults());
	}

	public int getLastPage() {
		return (int) Math.ceil((double) totalResults / maxResults);
	}

	public int getStartPagingLink() {
		if (pagingLinksStyle == PAGING_LINKS_STYLE_DYNAMIC) {
			return Math.max(getCurrentPage() - pagingLinks, getFirstPage());
		}

		return (getCurrentPage() - 1) / pagingLinks * pagingLinks + 1;
	}

	public int getEndPagingLink() {
		if (pagingLinksStyle == PAGING_LINKS_STYLE_DYNAMIC) {
			return Math.min(getCurrentPage() + pagingLinks, getLastPage());
		}

		return Math.min(getStartPagingLink() + pagingLinks - 1, getLastPage());
	}

	public int getPrelinkPage() {
		return Math.max(getStartPagingLink() - 1, getFirstPage());
	}

	public int getPostlinkPage() {
		return Math.min(getEndPagingLink() + 1, getLastPage());
	}

	public int getCurrentPage() {
		if (isInited()) {
			return Math.min(currentPage, getLastPage());
		}
		else {
			return currentPage;
		}
	}

	public void setCurrentPage(int currentPage) {
		if (currentPage < 1) {
			return;
		}

		this.currentPage = currentPage;
	}

	public int getMaxResults() {
		return maxResults;
	}

	protected void setMaxResults(int maxResults) {
		if (maxResults < 1) {
			return;
		}

		this.maxResults = maxResults;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
		this.inited = true;
	}

	public int getPagingLinks() {
		return pagingLinks;
	}

	protected void setPagingLinks(int pagingLinks) {
		if (pagingLinks < 1) {
			return;
		}

		this.pagingLinks = pagingLinks;
	}

	public int getPagingLinksStyle() {
		return pagingLinksStyle;
	}

	public void setPagingLinksStyle(int pagingLinksStyle) {
		if (pagingLinksStyle != PAGING_LINKS_STYLE_STATIC && pagingLinksStyle != PAGING_LINKS_STYLE_DYNAMIC) {
			return;
		}

		this.pagingLinksStyle = pagingLinksStyle;
	}

	public boolean isInited() {
		return inited;
	}

	public void setInited(boolean inited) {
		this.inited = inited;
	}

	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
			"(" + 
			"firstPage=" + "'" + getFirstPage() + "'" + ", " + 
			"lastPage=" + "'" + getLastPage() + "'" + ", " + 
			"currentPage=" + "'" + getCurrentPage() + "'" + ", " + 
			"startPagingLink=" + "'" + getStartPagingLink() + "'" + ", " + 
			"endPagingLink=" + "'" + getEndPagingLink() + "'" + 
			")";
	}

}

