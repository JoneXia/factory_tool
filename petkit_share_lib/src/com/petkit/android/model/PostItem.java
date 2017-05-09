package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PostItem implements Serializable{
	private static final long serialVersionUID = -4490724465877684976L;
	
	private Author author;
	private int comment;
	private String cover;
	private String lastUpdate;
	private String createdAt;
	private String detail;
	private int recommend;
	private int favor;
	private String id;
	private ArrayList<String> imgs;
	private int myfavor;
	private Tag tag;
	private String title;
	private ArrayList<Author> latestFavorUsers;
	private ArrayList<Tag> tags;
	private ArrayList<RankItem> ranks;
	private int myCollect;
	private int followed;
	
	private ArrayList<Topic> topics;
	private Comment postComment;
	
	private String link;
	private String summary;
	
	private Video video;
	
	//5.4 add
	private Poi poi;
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public int getComment() {
		return comment;
	}
	public void setComment(int comment) {
		this.comment = comment;
	}
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
	public int getFavor() {
		return favor;
	}
	public void setFavor(int favor) {
		this.favor = favor;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<String> getImgs() {
		return imgs;
	}
	public void setImgs(ArrayList<String> imgs) {
		this.imgs = imgs;
	}
	public int getMyfavor() {
		return myfavor;
	}
	public void setMyfavor(int myfavor) {
		this.myfavor = myfavor;
	}
	public Tag getTag() {
		return tag;
	}
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public ArrayList<Author> getLatestFavorUsers() {
		return latestFavorUsers;
	}
	public void setLatestFavorUsers(ArrayList<Author> latestFavorUsers) {
		this.latestFavorUsers = latestFavorUsers;
	}
	public ArrayList<Tag> getTags() {
		return tags;
	}
	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
	}
	public ArrayList<RankItem> getRanks() {
		return ranks;
	}
	public void setRanks(ArrayList<RankItem> ranks) {
		this.ranks = ranks;
	}
	public int getMyCollect() {
		return myCollect;
	}
	public void setMyCollect(int myCollect) {
		this.myCollect = myCollect;
	}
	public ArrayList<Topic> getTopics() {
		return topics;
	}
	public void setTopics(ArrayList<Topic> topics) {
		this.topics = topics;
	}
	public Comment getPostComment() {
		return postComment;
	}
	public void setPostComment(Comment postComment) {
		this.postComment = postComment;
	}
	public int getFollowed() {
		return followed;
	}
	public void setFollowed(int followed) {
		this.followed = followed;
	}
	public int getRecommend() {
		return recommend;
	}
	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Video getVideo() {
		return video;
	}
	public void setVideo(Video video) {
		this.video = video;
	}
	public Poi getPoi() {
		return poi;
	}
	public void setPoi(Poi poi) {
		this.poi = poi;
	}
	
	
	
}
