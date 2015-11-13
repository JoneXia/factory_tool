package com.petkit.android.model;

import java.io.Serializable;

public class Tag implements Serializable{

	private static final long serialVersionUID = 8368586137648556903L;
	
	private String createdAt;
	private String detail;
	private int hot;
	private String id;
	private String img;
	private String img2;
	private String name;
	private int newly;
	private int priority;
	private int status;
	private int type;
	private int postCount;
	private int expired;
	private String since;
	private String until;
	
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public int getHot() {
		return hot;
	}
	public void setHot(int hot) {
		this.hot = hot;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNewly() {
		return newly;
	}
	public void setNewly(int newly) {
		this.newly = newly;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getPostCount() {
		return postCount;
	}
	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}
	public int getExpired() {
		return expired;
	}
	public void setExpired(int expired) {
		this.expired = expired;
	}
	public String getSince() {
		return since;
	}
	public void setSince(String since) {
		this.since = since;
	}
	public String getUntil() {
		return until;
	}
	public void setUntil(String until) {
		this.until = until;
	}
	public String getImg2() {
		return img2;
	}
	public void setImg2(String img2) {
		this.img2 = img2;
	}
	
	
	
}
