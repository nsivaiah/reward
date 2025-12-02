package com.offer.dto;

import java.time.LocalDate;

public class RewardTransactionPoints {
	
	private String transactionId;
	private LocalDate date;
	private double amount;
	private long points;
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public long getPoints() {
		return points;
	}
	public void setPoints(long points) {
		this.points = points;
	}
	public RewardTransactionPoints(String transactionId, LocalDate date, double amount, long points) {
		super();
		this.transactionId = transactionId;
		this.date = date;
		this.amount = amount;
		this.points = points;
	}
	@Override
	public String toString() {
		return "TransactionWithPoints [transactionId=" + transactionId + ", date=" + date + ", amount=" + amount
				+ ", points=" + points + "]";
	}
	public RewardTransactionPoints() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
