package com.example;

public class Book {
	private long isbn;
	private int stock;
	private String title;
	private String author;
	
	public Book(long isbn, String title,String author,int stock){
		setIsbn(isbn);
		setTitle(title);
		setAuthor(author);
		setStock(stock);
	}

	public long getIsbn() {
		return isbn;
	}

	public final void setIsbn(long isbn) {
		this.isbn = isbn;
	}

	public  int getStock() {
		return stock;
	}

	public final void setStock(int stock) {
		if (stock <0){
			throw new IllegalArgumentException("The stock can't be less than 0");
		}
		this.stock = stock;
	}

	public String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		if(title==null||title.trim().length()==0){
			throw new IllegalArgumentException("The title can't be empty");
		}
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public final void setAuthor(String author) {
		if(author==null||author.trim().length()==0){
			throw new IllegalArgumentException("The author can't be empty");
		}
		this.author = author;
	}
	
	
	
	
	

}
