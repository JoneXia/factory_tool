package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Topic implements Serializable {

	private static final long serialVersionUID = 6763207922004985977L;
	
	private String authorId;
	private int collect;
	private int collectCount;
	private String createdAt;
	private int followCount;
	private int joinCount;
	private int recommend;
	private String topicId;
	private String topicname;
	private int visitCount;
	private int vitality;
	private String hotImg;
	private String imgs;
	private String onelevelId;
	private String secondaryId;
	private Author author;
	private String describe;
	private ArrayList<PostItem> posts;
	
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public int getCollect() {
		return collect;
	}
	public void setCollect(int collect) {
		this.collect = collect;
	}
	public int getCollectCount() {
		return collectCount;
	}
	public void setCollectCount(int collectCount) {
		this.collectCount = collectCount;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public int getFollowCount() {
		return followCount;
	}
	public void setFollowCount(int followCount) {
		this.followCount = followCount;
	}
	public int getJoinCount() {
		return joinCount;
	}
	public void setJoinCount(int joinCount) {
		this.joinCount = joinCount;
	}
	public int getRecommend() {
		return recommend;
	}
	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public String getTopicname() {
		return topicname;
	}
	public void setTopicname(String topicname) {
		this.topicname = topicname;
	}
	public int getVisitCount() {
		return visitCount;
	}
	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}
	public int getVitality() {
		return vitality;
	}
	public void setVitality(int vitality) {
		this.vitality = vitality;
	}
	public String getImgs() {
		return imgs;
	}
	public void setImgs(String imgs) {
		this.imgs = imgs;
	}
	public String getOnelevelId() {
		return onelevelId;
	}
	public void setOnelevelId(String onelevelId) {
		this.onelevelId = onelevelId;
	}
	public String getSecondaryId() {
		return secondaryId;
	}
	public void setSecondaryId(String secondaryId) {
		this.secondaryId = secondaryId;
	}
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public ArrayList<PostItem> getPosts() {
		return posts;
	}
	public void setPosts(ArrayList<PostItem> posts) {
		this.posts = posts;
	}
	public String getHotImg() {
		return hotImg;
	}
	public void setHotImg(String hotImg) {
		this.hotImg = hotImg;
	}
	
}
