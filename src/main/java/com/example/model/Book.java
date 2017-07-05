package com.example.model;

public class Book {
	
	private int id;
	private String isbn;
	private String title;
	private String author;
	private int stock;		
	
	public Book(int id, String isbn, String title, String author, int stock) {
		this.id = id;
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.stock = stock;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	
	

}
