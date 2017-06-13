package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	//delete by v6.3
//	private ArrayList<String> imgs;

	private int myfavor;
	private Tag tag;
	private String title;
	private ArrayList<Author> latestFavorUsers;
	private ArrayList<Tag> tags;
	private ArrayList<RankItem> ranks;
	private int myCollect;
	private int followed;
	
	private ArrayList<Topic> topics;

	//delete from v6.3
//	private Comment postComment;
	
	private String link;
	private String summary;
	
	private Video video;
	private int videoSource;
	
	//5.4 add
	private Poi poi;

	//v6.3 add
	private int distance;
	private List<Comment> comments;
	private ArrayList<ImageDetail> images;

	//v6.5 add
	private boolean hideTime;

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
//	public ArrayList<String> getImgs() {
//		return imgs;
//	}
//	public void setImgs(ArrayList<String> imgs) {
//		this.imgs = imgs;
//	}
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
//	public Comment getPostComment() {
//		return postComment;
//	}
//	public void setPostComment(Comment postComment) {
//		this.postComment = postComment;
//	}
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

	public void setVideoSource(int videoSource){
		this.videoSource = videoSource;
	}

	public int getVideoSource(){
		return videoSource;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void addComment(Comment comment) {
		if(comments == null) {
			comments = new ArrayList<>();
		} else if(comments.size() == 2) {
			comments.remove(0);
		}

		comments.add(comment);
	}

	public void addFavorUser(Author author) {
		if(latestFavorUsers == null) {
			latestFavorUsers = new ArrayList<>();
		}

		latestFavorUsers.add(author);
	}

	public void removeFavorUser(Author author) {
		if(latestFavorUsers != null && author != null && author.getId() != null) {
			for (Author author1 : latestFavorUsers) {
				if(author1.getId().equals(author.getId())) {
					latestFavorUsers.remove(author1);
					break;
				}
			}
		}
	}

	public ArrayList<ImageDetail> getImages() {
		return images;
	}

	public void setImages(ArrayList<ImageDetail> images) {
		this.images = images;
	}

	public void setHideTime(boolean hideTime) {
		this.hideTime = hideTime;
	}

	public boolean isHideTime() {
		return hideTime;
	}
}
