package com.petkit.android.model;

import java.io.Serializable;

import com.petkit.android.model.Author;

public class Comment implements Serializable{

	private static final long serialVersionUID = 6774807145651074305L;
	
	private Author commentor;
	private String createdAt;
	private String detail;
	private String id;
	private String img;
	private Author replyTo;
	
	
	public Author getCommentor() {
		return commentor;
	}
	public void setCommentor(Author commentor) {
		this.commentor = commentor;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getDetail() {
		if(detail == null || detail.length() == 0){
			return " ";
		}
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
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
	public Author getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(Author replyTo) {
		this.replyTo = replyTo;
	}
	
}
