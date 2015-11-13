package com.petkit.android.model;


import java.io.Serializable;

public class Author implements Serializable {

	private static final long serialVersionUID = -8315023130344291664L;
	
	private String avatar;
	private int gender;
	private String id;
	private String nick;
	private Address addr;
	private int coin;
	private int official;
	
	private UserPoint point;
	
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNick() {
		if(nick == null){
			nick = "u" + id;
		}
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
		if(nick == null){
			nick = "u" + id;
		}
	}
	public Address getAddr() {
		return addr;
	}
	public void setAddr(Address addr) {
		this.addr = addr;
	}
	public int getCoin() {
		return coin;
	}
	public void setCoin(int coin) {
		this.coin = coin;
	}
	public int getOfficial() {
		return official;
	}
	public void setOfficial(int official) {
		this.official = official;
	}
	public UserPoint getPoint() {
		return point;
	}
	public void setPoint(UserPoint point) {
		this.point = point;
	}
	
}
