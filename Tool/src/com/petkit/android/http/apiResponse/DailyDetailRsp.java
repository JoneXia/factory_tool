package com.petkit.android.http.apiResponse;

import java.util.List;

import com.petkit.android.model.DailyDetailResult;


public class DailyDetailRsp extends BaseRsp {

	private List<DailyDetailResult> result;
	
	
	public DailyDetailRsp(List<DailyDetailResult> result) {
		super();
		this.result = result;
	}

	public List<DailyDetailResult> getResult() {
		return result;
	}

	public void setResult(List<DailyDetailResult> result) {
		this.result = result;
	}

}
