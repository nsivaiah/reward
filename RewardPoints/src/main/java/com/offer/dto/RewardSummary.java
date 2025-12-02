package com.offer.dto;

import java.util.List;
import java.util.Map;

public class RewardSummary {
	
	private String customerId;
	private String customerName;
	private String customerMail;
	private Map<String,Long> pointsPerMonth;
	private Long totalPoints;
	private List<RewardTransactionPoints> transactions;
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerMail() {
		return customerMail;
	}
	public void setCustomerMail(String customerMail) {
		this.customerMail = customerMail;
	}
	public Map<String, Long> getPointsPerMonth() {
		return pointsPerMonth;
	}
	public void setPointsPerMonth(Map<String, Long> pointsPerMonth) {
		this.pointsPerMonth = pointsPerMonth;
	}
	public Long getTotalPoints() {
		return totalPoints;
	}
	public void setTotalPoints(Long totalPoints) {
		this.totalPoints = totalPoints;
	}
	public List<RewardTransactionPoints> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<RewardTransactionPoints> transactions) {
		this.transactions = transactions;
	}
	public RewardSummary(String customerId, String customerName, String customerMail, Map<String, Long> pointsPerMonth,
			Long totalPoints, List<RewardTransactionPoints> transactions) {
		super();
		this.customerId = customerId;
		this.customerName = customerName;
		this.customerMail = customerMail;
		this.pointsPerMonth = pointsPerMonth;
		this.totalPoints = totalPoints;
		this.transactions = transactions;
	}
	@Override
	public String toString() {
		return "RewardSummary [customerId=" + customerId + ", customerName=" + customerName + ", customerMail="
				+ customerMail + ", pointsPerMonth=" + pointsPerMonth + ", totalPoints=" + totalPoints
				+ ", transactions=" + transactions + "]";
	}
	public RewardSummary() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
