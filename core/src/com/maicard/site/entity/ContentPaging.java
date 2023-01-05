package com.maicard.site.entity;


/**
 * 用于文档内容的分页，继承自数据记录分页
 * @author NetSnake
 * @date 2012-5-16
 */
public class ContentPaging extends Paging{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int totalRows;
	private int totalPage;
	private int currentPage;
	private int rowsPerPage;
	private int displayFirstPage;
	private int displayLastPage;
	private int displayPageCount;
	
	public ContentPaging(){}
	
	public ContentPaging(int totalRows){
		this.totalRows = totalRows;
		
	}
	public int getFirstPage() {
		return 1;
	}
	public int getLastPage() {
		return totalPage;
	}
	
	
	public int getTotalPage() {
		return (int)(Math.ceil((double)this.totalRows / (double)this.rowsPerPage));
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
		
	}
	public int getRowsPerPage() {
		return rowsPerPage;
	}
	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}
	public int getDisplayPageCount() {
		return displayPageCount;
	}
	public void setDisplayPageCount(int displayPageCount) {
		this.displayPageCount = displayPageCount;
	}
	
	public void caculateDisplayPage(){
		try{
			int middle = (int)Math.floor(this.displayPageCount / 2);
			this.totalPage = (int)(Math.ceil((double)this.totalRows / (double)this.rowsPerPage));
			if(this.currentPage > middle && totalPage - this.currentPage > middle){
				//System.out.println("s1");
				this.displayFirstPage = this.currentPage - middle;
				this.displayLastPage = Math.min(this.displayFirstPage + this.displayPageCount,this.totalPage);
				return;
			} else {
				if(this.currentPage <= middle){
					//System.out.println("s2");
					this.displayFirstPage = 1;
					this.displayLastPage = Math.min(this.displayFirstPage + this.displayPageCount, this.totalPage);
					return;
				} else {
					//System.out.println("s3");
					this.displayLastPage = totalPage;
					this.displayFirstPage = Math.max(this.displayLastPage - this.displayPageCount,1);
					return;
				}
			}
			/*if(totalPage - this.currentPage > middle){
				this.displayLastPage = this.currentPage + middle;
				this.displayFirstPage = this.displayLastPage - this.displayPageCount;
				return;
			}*/
			
		}catch(Exception e){
			
		}
	}

	public static void main(String[] argv){
		ContentPaging contentPaging = new ContentPaging(98);
		contentPaging.setRowsPerPage(10);
		contentPaging.setCurrentPage(5);
		contentPaging.setDisplayPageCount(5);
		contentPaging.caculateDisplayPage();
		System.out.println(contentPaging.getDisplayFirstPage() + "-" + contentPaging.getDisplayLastPage());
	}
	public int getDisplayFirstPage() {
		return displayFirstPage;
	}
	public int getDisplayLastPage() {
		return displayLastPage;
	}

}
