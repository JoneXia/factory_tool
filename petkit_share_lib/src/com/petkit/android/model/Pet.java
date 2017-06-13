package com.petkit.android.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Pet implements Serializable{

	private static final long serialVersionUID = 5644325620781431705L;
	
	private String avatar;
	private String birth;
	private Device device;
	private Food food;
	private PrivateFood privateFood;
	private int gender;
	private String id;
	private String name;
	private String createdAt;
	private String updatedAt;
	private String weight;
	private CategoryInfo category;
	private PetType type;
	private String femaleState;
	private String maleState;
	private String pregnantStart;
	private String lactationStart;
	private int activeDegree;
	private Author owner;
	private String weightLabel;
	private int emotion;
	private PetSize size;
	private int weightControl;//体重控制，1增肥，2减肥，3保持
	private ArrayList<Integer> states;//1生病，2发情，3怀孕，4哺乳

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public PrivateFood getPrivateFood() {
		return privateFood;
	}

	public void setPrivateFood(PrivateFood privateFood) {
		this.privateFood = privateFood;
	}

	public void setWeightLabel(String weightLabel) {
		this.weightLabel = weightLabel;
	}

	public void setEmotion(int emotion) {
		this.emotion = emotion;
	}

	public void setSize(PetSize size) {
		this.size = size;
	}

	public String getWeightLabel() {
		return weightLabel;
	}

	public int getEmotion() {
		return emotion;
	}

	public PetSize getSize() {
		return size;
	}

	public String getAvatar() {
		if(avatar == null || avatar.length() == 0){
			if(category != null){
				return category.getAvatar();
			}
		}
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getBirth() {
		return birth;
	}
	public void setBirth(String birth) {
		this.birth = birth;
	}
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public Author getOwner() {
		return owner;
	}
	public void setOwner(Author owner) {
		this.owner = owner;
	}
	public CategoryInfo getCategory() {
		return category;
	}
	public void setCategory(CategoryInfo category) {
		this.category = category;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public PetType getType() {
		return type;
	}
	public void setType(PetType type) {
		this.type = type;
	}
	public String getFemaleState() {
		return femaleState;
	}
	public void setFemaleState(String femaleState) {
		this.femaleState = femaleState;
	}
	public String getMaleState() {
		return maleState;
	}
	public void setMaleState(String maleState) {
		this.maleState = maleState;
	}
	public void setPregnantStart(String pregnantStart) {
		this.pregnantStart = pregnantStart;
	}
	public String getPregnantStart() {
		return pregnantStart;
	}
	public void setLactationStart(String lactationStart) {
		this.lactationStart = lactationStart;
	}
	public String getLactationStart() {
		return lactationStart;
	}
	public void setActiveDegree(int activeDegree) {
		this.activeDegree = activeDegree;
	}
	public int getActiveDegree() {
		return activeDegree;
	}
	public int getWeightControl() {
		return weightControl;
	}
	public void setWeightControl(int weightControl) {
		this.weightControl = weightControl;
	}
	public ArrayList<Integer> getStates() {
		return states;
	}
	public void setStates(ArrayList<Integer> states) {
		this.states = states;
	}
}
