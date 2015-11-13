package com.petkit.android.model;

import java.io.Serializable;

public class RankItem implements Serializable{

	private static final long serialVersionUID = -4254913137157137273L;
	
	
	private int rank;
	private String tagId;
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
}
