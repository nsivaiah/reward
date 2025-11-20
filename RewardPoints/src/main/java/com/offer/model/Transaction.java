package com.offer.model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
	
	private final String id;
	private String customerId;
	private double amount;
	private LocalDate date;
	
	public String getId() {
		return id;
	}
		public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	@Override
	public String toString() {
		return "Transaction [id=" + id + ", customerId=" + customerId + ", amount=" + amount + ", date=" + date + "]";
	}
	
	
	public Transaction(String customerId, double amount, LocalDate date) {
		super();
		this.id = UUID.randomUUID().toString();
		this.customerId = customerId;
		this.amount = amount;
		this.date = date;
	}
	
	

}
